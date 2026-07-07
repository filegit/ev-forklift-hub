#!/bin/bash
set -e
cd /opt/ev-forklift-hub
sed -i 's/\r$//' sms.env restart-user-sms.sh sms-test.json 2>/dev/null || true

docker exec efh-mysql mysql -uroot -p123456 -e "UPDATE efh_user_0.user SET phone='18043070663' WHERE username='admin';"
docker exec efh-mysql mysql -uroot -p123456 -e "SELECT username,phone FROM efh_user_0.user WHERE username='admin';"

bash restart-user-sms.sh
sleep 35

echo "--- SMS API test ---"
curl -s -X POST http://127.0.0.1:8080/user/api/sms/login \
  -H 'Content-Type: application/json' \
  -d @sms-test.json
echo
echo "--- user log ---"
tail -20 logs/efh-user-1.0.0.log | grep -E 'SMS|短信|ERROR|Exception|发送' || tail -10 logs/efh-user-1.0.0.log
