#!/bin/bash
# 数据库初始化脚本 - 在服务器上执行

echo "开始初始化数据库..."

# 等待 MySQL 启动
echo "等待 MySQL 启动..."
sleep 30

# 创建社区数据库表
echo "1. 创建社区数据库表..."

# 创建 post 分表
docker exec efh-mysql mysql -uroot -p123456 efh_community_0 -e "
CREATE TABLE IF NOT EXISTS post_0 (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  title varchar(200) NOT NULL,
  content text NOT NULL,
  category varchar(50),
  view_count int DEFAULT 0,
  like_count int DEFAULT 0,
  comment_count int DEFAULT 0,
  collection_count int DEFAULT 0,
  status int DEFAULT 1,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_user_id (user_id),
  KEY idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"

docker exec efh-mysql mysql -uroot -p123456 efh_community_0 -e "CREATE TABLE IF NOT EXISTS post_1 LIKE post_0;"
docker exec efh-mysql mysql -uroot -p123456 efh_community_0 -e "CREATE TABLE IF NOT EXISTS post_2 LIKE post_0;"
docker exec efh-mysql mysql -uroot -p123456 efh_community_0 -e "CREATE TABLE IF NOT EXISTS post_3 LIKE post_0;"

echo "   ✓ 帖子分表创建完成"

# 创建评论表
docker exec efh-mysql mysql -uroot -p123456 efh_community_0 -e "
CREATE TABLE IF NOT EXISTS comment (
  id bigint NOT NULL AUTO_INCREMENT,
  post_id bigint NOT NULL,
  user_id bigint NOT NULL,
  parent_id bigint DEFAULT 0,
  content text NOT NULL,
  like_count int DEFAULT 0,
  status int DEFAULT 1,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_post_id (post_id),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"

echo "   ✓ 评论表创建完成"

# 创建点赞表
docker exec efh-mysql mysql -uroot -p123456 efh_community_0 -e "
CREATE TABLE IF NOT EXISTS post_like (
  id bigint NOT NULL AUTO_INCREMENT,
  post_id bigint NOT NULL,
  user_id bigint NOT NULL,
  status int DEFAULT 1,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_post_user (post_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"

echo "   ✓ 点赞表创建完成"

# 创建收藏表
docker exec efh-mysql mysql -uroot -p123456 efh_community_0 -e "
CREATE TABLE IF NOT EXISTS post_collection (
  id bigint NOT NULL AUTO_INCREMENT,
  post_id bigint NOT NULL,
  user_id bigint NOT NULL,
  status int DEFAULT 1,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_post_user (post_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"

echo "   ✓ 收藏表创建完成"

# 创建评论点赞表
docker exec efh-mysql mysql -uroot -p123456 efh_community_0 -e "
CREATE TABLE IF NOT EXISTS comment_like (
  id bigint NOT NULL AUTO_INCREMENT,
  comment_id bigint NOT NULL,
  user_id bigint NOT NULL,
  status int DEFAULT 1,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_comment_user (comment_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"

echo "   ✓ 评论点赞表创建完成"

# 创建用户数据库表
echo "2. 创建用户数据库表..."

docker exec efh-mysql mysql -uroot -p123456 efh_user_0 -e "
CREATE TABLE IF NOT EXISTS user (
  id bigint NOT NULL AUTO_INCREMENT,
  username varchar(50) NOT NULL,
  password varchar(100) NOT NULL,
  nickname varchar(50),
  avatar varchar(200),
  email varchar(100),
  gender int DEFAULT 0,
  user_type int DEFAULT 1,
  phone varchar(20),
  status int DEFAULT 1,
  points int DEFAULT 0,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"

echo "   ✓ 用户表创建完成"

docker exec efh-mysql mysql -uroot -p123456 efh_user_0 -e "
CREATE TABLE IF NOT EXISTS user_points (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  total_points int DEFAULT 0,
  used_points int DEFAULT 0,
  available_points int DEFAULT 0,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"

echo "   ✓ 积分表创建完成"

docker exec efh-mysql mysql -uroot -p123456 efh_user_0 -e "
CREATE TABLE IF NOT EXISTS points_exchange (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  item_name varchar(100) NOT NULL,
  points int NOT NULL,
  status varchar(20) DEFAULT 'pending',
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"

echo "   ✓ 积分兑换表创建完成"

# 插入测试数据
echo "3. 插入测试数据..."

docker exec efh-mysql mysql -uroot -p123456 efh_community_0 -e "
INSERT IGNORE INTO post_1 (id, user_id, title, content, category, view_count, like_count, comment_count) VALUES 
(1, 1, '欢迎来到新能源叉车社区', '这是一个专注于新能源叉车的技术交流社区，欢迎大家分享经验！', '公告', 100, 10, 5);"

docker exec efh-mysql mysql -uroot -p123456 efh_community_0 -e "
INSERT IGNORE INTO post_2 (id, user_id, title, content, category, view_count, like_count, comment_count) VALUES 
(2, 1, '如何选择合适的电动叉车？', '选择电动叉车需要考虑载重、续航、充电时间等多个因素...', '技术交流', 50, 8, 3);"

docker exec efh-mysql mysql -uroot -p123456 efh_community_0 -e "
INSERT IGNORE INTO post_3 (id, user_id, title, content, category, view_count, like_count, comment_count) VALUES 
(3, 2, '我的叉车维修经验分享', '最近维修了一台叉车，遇到了一些问题，在这里分享给大家...', '维修保养', 30, 5, 2);"

echo "   ✓ 帖子数据插入完成"

docker exec efh-mysql mysql -uroot -p123456 efh_user_0 -e "
INSERT IGNORE INTO user (id, username, password, nickname, email, phone, points) VALUES 
(1, 'admin', '\$2a\$10\$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 'admin@example.com', '13800138000', 1000),
(2, 'user1', '\$2a\$10\$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '用户1', 'user1@example.com', '13800138001', 500),
(3, 'user2', '\$2a\$10\$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '用户2', 'user2@example.com', '13800138002', 300);"

echo "   ✓ 用户数据插入完成"

# 验证
echo "4. 验证数据..."
docker exec efh-mysql mysql -uroot -p123456 efh_community_0 -e "SHOW TABLES;"
docker exec efh-mysql mysql -uroot -p123456 efh_user_0 -e "SHOW TABLES;"

echo "✅ 数据库初始化完成！"
