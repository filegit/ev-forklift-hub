#!/bin/bash
TOKEN=$(curl -s -X POST http://127.0.0.1:8080/user/api/login -H 'Content-Type: application/json' --data-binary @/tmp/login.json | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
echo "TOKEN=${#TOKEN} chars"
cat > /tmp/part.json <<'JSON'
{"name":"测试配件","description":"desc","category":"电池","brand":"测试","model":"T1","price":100,"stock":10,"images":""}
JSON
echo "=== direct 8083 ==="
curl -s -X POST http://127.0.0.1:8083/api/parts -H 'Content-Type: application/json' -H "X-User-Id: 1" --data-binary @/tmp/part.json
echo
echo "=== gateway 8080 ==="
curl -s -X POST http://127.0.0.1:8080/parts/api/parts -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" --data-binary @/tmp/part.json
echo
echo "=== nginx 8888 ==="
curl -s -X POST http://127.0.0.1:8888/api/parts/api/parts -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" --data-binary @/tmp/part.json
echo
docker exec efh-mysql mysql -uroot -p123456 -e 'DESCRIBE efh_parts.parts;' 2>/dev/null | grep seller
tail -15 /opt/ev-forklift-hub/logs/efh-parts-1.0.0.log
