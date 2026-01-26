#!/bin/bash
set -ex

# =============================================================================
# Kafka KRaft Mode Setup (Multi-Broker Cluster for Production)
# =============================================================================

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

echo "=== Starting Kafka (KRaft Mode - Node $NODE_ID of $BROKER_COUNT) ==="

# Cluster configuration based on broker count
if [ "$BROKER_COUNT" -gt 1 ]; then
  REPLICATION_FACTOR=3
  MIN_ISR=2
else
  REPLICATION_FACTOR=1
  MIN_ISR=1
fi

# For multi-broker, use DNS names for quorum voters
# Initial setup uses placeholder - actual IPs are resolved via Route53
if [ "$BROKER_COUNT" -gt 1 ]; then
  # Using private IPs directly since Route53 may not be ready at boot time
  # The controller quorum voters will be configured using the broker's own IP
  # and will discover other brokers through the cluster
  CONTROLLER_QUORUM_VOTERS="1@kafka-1.kafka.internal:${kraft_port},2@kafka-2.kafka.internal:${kraft_port},3@kafka-3.kafka.internal:${kraft_port}"
else
  CONTROLLER_QUORUM_VOTERS="1@localhost:${kraft_port}"
fi

docker run -d \
  --name kafka \
  --restart unless-stopped \
  --network host \
  -e KAFKA_CFG_NODE_ID=$NODE_ID \
  -e KAFKA_CFG_PROCESS_ROLES=broker,controller \
  -e KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=$CONTROLLER_QUORUM_VOTERS \
  -e KAFKA_CFG_LISTENERS=PLAINTEXT://:${kafka_port},CONTROLLER://:${kraft_port},INTERNAL://:${internal_port} \
  -e KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://$${PRIVATE_IP}:${kafka_port},INTERNAL://$${PRIVATE_IP}:${internal_port} \
  -e KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,INTERNAL:PLAINTEXT \
  -e KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_CFG_INTER_BROKER_LISTENER_NAME=INTERNAL \
  -e KAFKA_KRAFT_CLUSTER_ID=${kafka_cluster_id} \
  -e KAFKA_CFG_LOG_RETENTION_HOURS=${log_retention_hours} \
  -e KAFKA_CFG_LOG_RETENTION_BYTES=$((${log_retention_gb} * 1024 * 1024 * 1024)) \
  -e KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE=true \
  -e KAFKA_CFG_NUM_PARTITIONS=3 \
  -e KAFKA_CFG_DEFAULT_REPLICATION_FACTOR=$REPLICATION_FACTOR \
  -e KAFKA_CFG_MIN_INSYNC_REPLICAS=$MIN_ISR \
  -e KAFKA_CFG_OFFSETS_TOPIC_REPLICATION_FACTOR=$REPLICATION_FACTOR \
  -e KAFKA_CFG_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=$REPLICATION_FACTOR \
  -e KAFKA_CFG_TRANSACTION_STATE_LOG_MIN_ISR=$MIN_ISR \
  -v /data/kafka:/bitnami/kafka \
  bitnami/kafka:${kafka_version}

echo "=== Waiting for Kafka to start ==="
sleep 45

# Health check
docker logs kafka

echo "=== Kafka Node $NODE_ID setup complete ==="
echo "Bootstrap servers: $${PRIVATE_IP}:${kafka_port}"
echo "Broker count: $BROKER_COUNT"
echo "Replication factor: $REPLICATION_FACTOR"
