#!/bin/bash

echo "=== 기존 컨테이너 종료 및 삭제 ==="
docker compose down --remove-orphans
docker rm -f redis_cache local-postgres_db spot-gateway spot-user spot-store spot-order spot-payment spot-mono 2>/dev/null || true

echo "=== 각 MSA 서비스 빌드 ==="
echo ">> spot-gateway 빌드"
(cd spot-gateway && ./gradlew bootJar -x test)

echo ">> spot-user 빌드"
(cd spot-user && ./gradlew bootJar -x test)

echo ">> spot-store 빌드"
(cd spot-store && ./gradlew bootJar -x test)

echo ">> spot-order 빌드"
(cd spot-order && ./gradlew bootJar -x test)

echo ">> spot-payment 빌드"
(cd spot-payment && ./gradlew bootJar -x test)

echo ">> spot-mono 빌드"
(cd spot-mono && ./gradlew bootJar -x test)

echo "=== Docker 이미지 빌드 및 컨테이너 시작 ==="
docker compose up --build -d

echo "=== 실행 중인 컨테이너 확인 ==="
docker compose ps

echo "=== 로그 확인 ==="
docker compose logs -f | grep --line-buffered -v -E "redis_cache|local-posgre_db" | tee -a current_logs.txt