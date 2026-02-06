#!/bin/bash

clear

# 에러 발생 시 즉시 중단
set -e

echo "=== 기존 컨테이너 종료 및 삭제 ==="
docker compose down --remove-orphans
# kafka 데이터 볼륨 삭제
docker volume ls -q | grep "kafka-data" | xargs -r docker volume rm
# 컨테이너가 없을 경우 에러 메시지를 숨기기 위해 || true 사용
docker rm -f redis_cache local-postgres_db spot-gateway spot-user spot-store spot-order spot-payment 2>/dev/null || true

echo "=== Observability Stack 먼저 실행 (fluent-bit, loki, grafana) ==="
docker compose up -d fluent-bit loki grafana
sleep 3

echo "=== 각 MSA 서비스 빌드 ==="
for service in spot-gateway spot-user spot-store spot-order spot-payment; do
    echo ">> $service 빌드 시작"
    (cd "$service" && ./gradlew clean bootJar -x test)
done

clear

echo "=== Docker 이미지 빌드 및 컨테이너 시작 ==="
docker compose up --build

echo "=== 실행 중인 컨테이너 확인 ==="
docker compose ps

echo "=== 로그 확인 (./logs 폴더에 저장됨) ==="
mkdir -p ./logs
# 파일명을 변수로 빼서 가독성을 높였습니다.
LOG_FILE="./logs/current_logs_$(date +'%Y%m%d_%H%M%S').txt"

#docker compose logs -f | \
#    grep --line-buffered -v -E "redis_cache|local-postgres_db" | \
#    tee -a "$LOG_FILE"
