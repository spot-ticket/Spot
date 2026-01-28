#!/bin/bash
set -ex

# 로그 설정
exec > >(tee /var/log/user-data.log) 2>&1

echo "=== Installing Docker ==="
dnf update -y
dnf install -y docker
systemctl enable docker
systemctl start docker
usermod -aG docker ec2-user

echo "=== Creating Kafka directories ==="
mkdir -p /data/kafka
chmod 777 /data/kafka

echo "=== Getting Private IP ==="
TOKEN=$(curl -s -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 21600")
PRIVATE_IP=$(curl -s -H "X-aws-ec2-metadata-token: $TOKEN" http://169.254.169.254/latest/meta-data/local-ipv4)

echo "=== Starting Apache Kafka (KRaft Mode - Single Node) ==="

docker run -d \
  --name kafka \
  --restart unless-stopped \
  --network host \
  -e KAFKA_NODE_ID=1 \
  -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@$${PRIVATE_IP}:${kraft_port} \
  -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:${kafka_port},CONTROLLER://0.0.0.0:${kraft_port} \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://$${PRIVATE_IP}:${kafka_port} \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT \
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT \
  -e KAFKA_KRAFT_CLUSTER_ID=${kafka_cluster_id} \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  -e KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1 \
  -e KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=1 \
  -e KAFKA_LOG_RETENTION_HOURS=${log_retention_hours} \
  -e KAFKA_LOG_RETENTION_BYTES=$((${log_retention_gb} * 1024 * 1024 * 1024)) \
  -e KAFKA_AUTO_CREATE_TOPICS_ENABLE=true \
  -v /data/kafka:/var/lib/kafka/data \
  apache/kafka:3.7.0

echo "=== Waiting for Kafka to start ==="
sleep 30

docker logs kafka
echo "=== Kafka setup complete ==="