#!/bin/sh
echo "Waiting for Kafka Connect..."
while [ $(curl -s -o /dev/null -w "%{http_code}" http://connect:8083) -ne 200 ]; do
  sleep 3
done

echo "Registering connectors from /configs..."
for file in /configs/*.json; do
  curl -X POST -H "Content-Type: application/json" -d @"$file" http://connect:8083/connectors
done