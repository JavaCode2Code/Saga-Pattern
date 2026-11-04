#!/usr/bin/env bash
# Starts all four services in the background. Build first: mvn clean package -DskipTests
cd "$(dirname "$0")"
mkdir -p logs
for s in inventory-service payment-service delivery-service order-service; do
  java -jar $s/target/$s-1.0.0.jar > logs/$s.log 2>&1 &
  echo $! > logs/$s.pid
  echo "started $s (pid $(cat logs/$s.pid))"
done
echo "Order API: http://localhost:9001/api/orders"
