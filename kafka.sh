#!/bin/bash

echo "===기존 Order,Payment 컨테이너 종료 및 삭제==="
docker compose stop spot-order spot-payment
docker compose rm -f spot-order spot-payment

echo "===Order,Payment 서비스 빌드==="
(cd spot-order && ./gradlew bootJar -x test)
(cd spot-payment && ./gradlew bootJar -x test)

echo "===Order,Payment 빌드 및 시작==="
docker compose build spot-order spot-payment
docker compose up -d spot-order spot-payment

