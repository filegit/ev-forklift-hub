#!/bin/bash
TOKEN=$(curl -s -X POST http://127.0.0.1:8080/user/api/login -H 'Content-Type: application/json' --data-binary @/tmp/login.json | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
cat > /tmp/post.json <<'JSON'
{"title":"测试帖子","content":"测试内容","category":1}
JSON
echo "=== create post gateway ==="
curl -s -X POST http://127.0.0.1:8080/community/api/post -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" --data-binary @/tmp/post.json
echo
echo "=== create post nginx ==="
curl -s -X POST http://127.0.0.1:8888/api/community/api/post -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" --data-binary @/tmp/post.json
echo
grep -iE 'createPost|error|exception|Unknown column|Field' /opt/ev-forklift-hub/logs/efh-community-1.0.0.log | tail -12
docker exec efh-mysql mysql -uroot -p123456 -e 'DESCRIBE efh_community_0.post_0;' 2>/dev/null | head -15
