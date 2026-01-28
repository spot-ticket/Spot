#!/bin/bash
set -ex

# 로그 기록 설정
exec > >(tee /var/log/user-data.log) 2>&1

echo "=== Installing Docker ==="
dnf update -y
dnf install -y docker jq
systemctl enable docker
systemctl start docker
usermod -aG docker ec2-user

echo "=== Creating Kafka directories ==="
mkdir -p /data/kafka
chmod 777 /data/kafka

echo "=== Getting Instance Metadata ==="
TOKEN=$(curl -s -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 21600")
PRIVATE_IP=$(curl -s -H "X-aws-ec2-metadata-token: $TOKEN" http://169.254.169.254/latest/meta-data/local-ipv4)

NODE_ID=${node_id}
BROKER_COUNT=${broker_count}

echo "=== Starting Apache Kafka (KRaft Mode) ==="

# 아파치 공식 이미지는 환경 변수 형식이 KAFKA_... 입니다.
# 쿼럼 보터(Voters) 설정
if [ "$BROKER_COUNT" -gt 1 ]; then
  CONTROLLER_QUORUM_VOTERS="1@kafka-1.kafka.internal:${kraft_port},2@kafka-2.kafka.internal:${kraft_port},3@kafka-3.kafka.internal:${kraft_port}"
  REPLICATION_FACTOR=3
else
  CONTROLLER_QUORUM_VOTERS="1@$${PRIVATE_IP}:${kraft_port}"
  REPLICATION_FACTOR=1
fi

# 아파치 카프카 컨테이너 실행
# 참고: 아파치 이미지는 /var/lib/kafka/data 를 데이터 디렉토리로 사용합니다.
docker run -d \
  --name kafka \
  --restart unless-stopped \
  --network host \
  -e KAFKA_NODE_ID=$NODE_ID \
  -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS=$CONTROLLER_QUORUM_VOTERS \
  -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:${kafka_port},CONTROLLER://0.0.0.0:${kraft_port},INTERNAL://0.0.0.0:${internal_port} \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://$${PRIVATE_IP}:${kafka_port},INTERNAL://$${PRIVATE_IP}:${internal_port} \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,INTERNAL:PLAINTEXT \
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_INTER_BROKER_LISTENER_NAME=INTERNAL \
  -e KAFKA_KRAFT_CLUSTER_ID=${kafka_cluster_id} \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=$REPLICATION_FACTOR \
  -e KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=$REPLICATION_FACTOR \
  -e KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=1 \
  -e KAFKA_LOG_RETENTION_HOURS=${log_retention_hours} \
  -e KAFKA_LOG_RETENTION_BYTES=$((${log_retention_gb} * 1024 * 1024 * 1024)) \
  -v /data/kafka:/var/lib/kafka/data \
  apache/kafka:3.7.0

echo "=== Kafka setup complete ==="