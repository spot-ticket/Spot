#!/bin/bash

clear

set -e

echo "=== 기존 컨테이너 종료 및 삭제 ==="
docker compose down --remove-orphans

docker volume ls -q | grep "kafka-data" | xargs -r docker volume rm
docker rm -f redis_cache local-postgres_db spot-gateway spot-user spot-store spot-order spot-payment 2>/dev/null || true

echo "=== 각 MSA 서비스 빌드 ==="
for service in spot-gateway spot-user spot-store spot-order spot-payment; do
    echo ">> $service 빌드 시작"
    (cd "$service" && ./gradlew bootJar -x test)
done

docker compose up --build -d

mkdir -p ./logs
LOG_FILE="./logs/current_logs_$(date +'%Y%m%d_%H%M%S').txt"

docker compose logs | \
    grep --line-buffered -v -E "redis_cache|local-postgres_db" | \
    tee -a "$LOG_FILE"