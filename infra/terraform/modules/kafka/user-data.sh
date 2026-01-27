#!/bin/bash
set -ex

# =============================================================================
# Kafka KRaft Mode Setup (Single Node for Dev)
# =============================================================================

# 로그 설정
exec > >(tee /var/log/user-data.log) 2>&1

echo "=== Installing Docker ==="
dnf update -y
dnf install -y docker
systemctl enable docker
systemctl start docker

# Docker 그룹에 ec2-user 추가
usermod -aG docker ec2-user

echo "=== Creating Kafka directories ==="
mkdir -p /data/kafka
chmod 777 /data/kafka

echo "=== Starting Kafka (KRaft Mode) ==="
# 호스트의 private IP 가져오기
PRIVATE_IP=$(TOKEN=$(curl -s -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 21600") && curl -s -H "X-aws-ec2-metadata-token: $TOKEN" http://169.254.169.254/latest/meta-data/local-ipv4)

docker run -d \
  --name kafka \
  --restart unless-stopped \
  -p ${kafka_port}:9092 \
  -p ${kraft_port}:9093 \
  -e KAFKA_CFG_NODE_ID=1 \
  -e KAFKA_CFG_PROCESS_ROLES=broker,controller \
  -e KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=1@localhost:${kraft_port} \
  -e KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
  -e KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://$${PRIVATE_IP}:9092 \
  -e KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT \
  -e KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_CFG_INTER_BROKER_LISTENER_NAME=PLAINTEXT \
  -e KAFKA_KRAFT_CLUSTER_ID=${kafka_cluster_id} \
  -e KAFKA_CFG_LOG_RETENTION_HOURS=${log_retention_hours} \
  -e KAFKA_CFG_LOG_RETENTION_BYTES=$((${log_retention_gb} * 1024 * 1024 * 1024)) \
  -e KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE=true \
  -e KAFKA_CFG_NUM_PARTITIONS=3 \
  -e KAFKA_CFG_DEFAULT_REPLICATION_FACTOR=1 \
  -e KAFKA_CFG_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  -e KAFKA_CFG_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1 \
  -e KAFKA_CFG_TRANSACTION_STATE_LOG_MIN_ISR=1 \
  -v /data/kafka:/bitnami/kafka \
  bitnami/kafka:${kafka_version}

echo "=== Waiting for Kafka to start ==="
sleep 30

# 헬스체크
docker logs kafka

echo "=== Kafka setup complete ==="
echo "Bootstrap servers: $${PRIVATE_IP}:9092"
