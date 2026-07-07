#!/bin/bash
set -e
cd /opt/ev-forklift-hub
export MYSQL_HOST=127.0.0.1 MYSQL_PASSWORD=123456 REDIS_HOST=127.0.0.1 REDIS_PASSWORD=123456 NACOS_SERVER_ADDR=127.0.0.1:8848

if [ -f sms.env ]; then
  set -a
  source sms.env
  set +a
  echo "loaded sms.env"
else
  echo "WARN: sms.env not found"
fi

pkill -f 'efh-user/target/efh-user-1.0.0.jar' 2>/dev/null || true
sleep 3
mkdir -p logs
nohup java -Xms256m -Xmx512m -jar efh-user/target/efh-user-1.0.0.jar >> logs/efh-user-1.0.0.log 2>&1 &
echo "efh-user started pid=$!"
