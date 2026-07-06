#!/bin/bash
cd /opt/ev-forklift-hub
for f in docker/mysql/init/01-nacos.sql docker/mysql/init/init.sql docker/mysql/init/02-business.sql docker/mysql/init/03-create-new-tables.sql docker/mysql/init/04-parts-mall.sql docker/mysql/init/05-knowledge.sql docker/mysql/init/06-agent.sql fix-host-db.sql scripts/fix-service-table.sql; do
  [ -f "$f" ] || continue
  echo "RUN $f"
  docker exec -i efh-mysql mysql -uroot -p123456 < "$f" 2>&1 | tail -1
done
docker exec -i efh-mysql mysql -uroot -p123456 < scripts/fix-service-table.sql 2>/dev/null || true
docker exec -i efh-mysql mysql -uroot -p123456 < scripts/fix-parts-table.sql 2>/dev/null || true
docker exec -i efh-mysql mysql -uroot -p123456 < scripts/fix-community-post-table.sql 2>/dev/null || true
echo "=== efh_community_0 tables ==="
docker exec efh-mysql mysql -uroot -p123456 -e "SHOW TABLES FROM efh_community_0;" 2>/dev/null
echo "=== efh_parts tables ==="
docker exec efh-mysql mysql -uroot -p123456 -e "SHOW TABLES FROM efh_parts;" 2>/dev/null
