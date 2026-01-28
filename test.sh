#!/bin/bash

# 에러 발생 시 즉시 중단
set -e

echo "=== 기존 컨테이너 종료 및 삭제 ==="
docker compose down --remove-orphans
# 컨테이너가 없을 경우 에러 메시지를 숨기기 위해 || true 사용
docker rm -f redis_cache local-postgres_db spot-gateway spot-user spot-store spot-order spot-payment 2>/dev/null || true

echo "=== 각 MSA 서비스 빌드 ==="
for service in spot-gateway spot-user spot-store spot-order spot-payment; do
    echo ">> $service 빌드 시작"
    (cd "$service" && ./gradlew bootJar -x test)
done

echo "=== Docker 이미지 빌드 및 컨테이너 시작 ==="
docker compose up --build -d

echo "=== 실행 중인 컨테이너 확인 ==="
docker compose ps

echo "=== 로그 확인 (./logs 폴더에 저장됨) ==="
mkdir -p ./logs
# 파일명을 변수로 빼서 가독성을 높였습니다.
LOG_FILE="./logs/current_logs_$(date +'%Y%m%d_%H%M%S').txt"

docker compose logs -f | \
    grep --line-buffered -v -E "redis_cache|local-postgres_db" | \
    tee -a "$LOG_FILE"