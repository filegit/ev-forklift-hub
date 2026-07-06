-- 宿主机 MySQL 补丁（Java 服务实际连接的库）
-- 用法: docker run --rm -i mysql:5.7 mysql -h host.docker.internal -uroot -p123456 < fix-host-db.sql

USE efh_user_0;

CREATE TABLE IF NOT EXISTS `user_points` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `total_points` int DEFAULT 0,
  `used_points` int DEFAULT 0,
  `available_points` int DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分表';

CREATE TABLE IF NOT EXISTS `points_exchange` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `item_name` varchar(100) NOT NULL,
  `points` int NOT NULL,
  `status` varchar(20) DEFAULT 'pending',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分兑换表';

CREATE TABLE IF NOT EXISTS `user_address` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `receiver_name` VARCHAR(50) NOT NULL,
  `phone` VARCHAR(20) NOT NULL,
  `province` VARCHAR(50) NOT NULL DEFAULT '',
  `city` VARCHAR(50) NOT NULL DEFAULT '',
  `district` VARCHAR(50) NOT NULL DEFAULT '',
  `detail` VARCHAR(200) NOT NULL,
  `is_default` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址';

USE efh_parts;

SET @db = 'efh_parts';
SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='parts_order_item' AND COLUMN_NAME='update_time')=0,
  'ALTER TABLE parts_order_item ADD COLUMN update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='parts_shipment_trace' AND COLUMN_NAME='update_time')=0,
  'ALTER TABLE parts_shipment_trace ADD COLUMN update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='parts_payment' AND COLUMN_NAME='third_trade_no')=0,
  'ALTER TABLE parts_payment ADD COLUMN third_trade_no varchar(64) DEFAULT NULL COMMENT ''第三方流水号''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

DELETE FROM parts;
INSERT INTO `parts` (`id`, `seller_id`, `name`, `description`, `category`, `brand`, `model`, `price`, `stock`, `images`, `status`, `sales_count`) VALUES
(1, 1, '锂电池组 48V 400Ah', '适用于各类电动叉车，续航持久，充电快速', '电池', '宁德时代', 'CB48-400', 12800.00, 50, 'https://via.placeholder.com/400x300?text=Battery', 1, 128),
(2, 1, '永磁同步电机 15kW', '高效节能电机，低噪音运行，维护成本低', '电机', '汇川', 'PM15K', 8600.00, 30, 'https://via.placeholder.com/400x300?text=Motor', 1, 86),
(3, 1, '智能控制器', '精准控制，多重保护，延长设备使用寿命', '控制器', '松正', 'SC-200', 3200.00, 100, 'https://via.placeholder.com/400x300?text=Controller', 1, 256),
(4, 1, '液压油缸', '高品质液压油缸，承载能力强', '液压', '力士乐', 'HY-80', 4500.00, 40, 'https://via.placeholder.com/400x300?text=Hydraulic', 1, 45),
(5, 1, '充电机 48V', '智能充电，过充保护', '充电设备', '英飞源', 'CH48-100', 2800.00, 60, 'https://via.placeholder.com/400x300?text=Charger', 1, 72);

USE efh_community_0;

DELETE FROM post_0;
INSERT INTO post_0 (id, user_id, title, content, category, view_count, like_count, comment_count, status) VALUES
(1, 1, '欢迎来到新能源叉车社区', '这是一个专注于新能源叉车的技术交流社区，欢迎大家分享经验！', 1, 100, 10, 5, 1),
(2, 1, '如何选择合适的电动叉车？', '选择电动叉车需要考虑载重、续航、充电时间等多个因素。', 1, 50, 8, 3, 1),
(3, 2, '叉车电池保养经验分享', '定期充电、避免过放可以显著延长电池寿命。', 3, 30, 5, 2, 1);

-- 评论表补全 update_time 字段
SET @db = 'efh_community_0';
SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='comment_0' AND COLUMN_NAME='update_time')=0,
  'ALTER TABLE comment_0 ADD COLUMN update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='comment_1' AND COLUMN_NAME='update_time')=0,
  'ALTER TABLE comment_1 ADD COLUMN update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='comment_2' AND COLUMN_NAME='update_time')=0,
  'ALTER TABLE comment_2 ADD COLUMN update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='comment_3' AND COLUMN_NAME='update_time')=0,
  'ALTER TABLE comment_3 ADD COLUMN update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ========== 知识库 ==========
CREATE DATABASE IF NOT EXISTS `efh_knowledge` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `efh_knowledge`;

CREATE TABLE IF NOT EXISTS `knowledge_doc` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL,
  `summary` varchar(500) DEFAULT NULL,
  `category` varchar(50) DEFAULT NULL,
  `file_name` varchar(255) DEFAULT NULL,
  `file_path` varchar(500) DEFAULT NULL,
  `file_size` bigint DEFAULT NULL,
  `file_type` varchar(20) DEFAULT NULL,
  `access_type` tinyint NOT NULL DEFAULT 0,
  `points_price` int DEFAULT 0,
  `money_price` decimal(10,2) DEFAULT 0.00,
  `status` tinyint NOT NULL DEFAULT 0,
  `download_count` int DEFAULT 0,
  `view_count` int DEFAULT 0,
  `created_by` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `knowledge_unlock` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `doc_id` bigint NOT NULL,
  `unlock_type` varchar(20) NOT NULL,
  `points_cost` int DEFAULT 0,
  `money_paid` decimal(10,2) DEFAULT 0.00,
  `pay_no` varchar(64) DEFAULT NULL,
  `third_trade_no` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_doc` (`user_id`, `doc_id`),
  KEY `idx_pay_no` (`pay_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

USE efh_user_0;
SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='efh_user_0' AND TABLE_NAME='user' AND COLUMN_NAME='user_type')=0,
  'ALTER TABLE `user` ADD COLUMN `user_type` TINYINT DEFAULT 1 COMMENT ''用户类型'' AFTER `gender`', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

INSERT INTO `user` (`username`, `password`, `nickname`, `phone`, `email`, `gender`, `user_type`, `status`)
SELECT 'kbadmin', '$2a$10$9yJT16f8BegQ0e2kALAxYehmRTU.pSD4snwcF93oY79/HOrVONv9C', '知识库管理员', '13900000001', 'kbadmin@efh.com', 0, 9, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE username = 'kbadmin');

INSERT INTO `user_points` (`user_id`, `total_points`, `used_points`, `available_points`)
SELECT u.id, 5000, 0, 5000 FROM `user` u WHERE u.username = 'kbadmin'
AND NOT EXISTS (SELECT 1 FROM `user_points` p WHERE p.user_id = u.id);

UPDATE `user_points` SET `total_points` = GREATEST(`total_points`, 1000), `available_points` = GREATEST(`available_points`, 1000)
WHERE `user_id` IN (SELECT id FROM `user` WHERE username IN ('shopuser4', 'test', 'user1'));

-- ========== 维修服务库（宿主机 MySQL，与 Java 实体字段一致）==========
CREATE DATABASE IF NOT EXISTS `efh_service` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `efh_service`;

DROP TABLE IF EXISTS `service_order`;
CREATE TABLE `service_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(50) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `technician_id` BIGINT DEFAULT NULL,
  `service_type` TINYINT DEFAULT 1,
  `title` VARCHAR(100) DEFAULT NULL,
  `description` TEXT,
  `images` TEXT DEFAULT NULL,
  `address` VARCHAR(200) DEFAULT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `status` TINYINT DEFAULT 0,
  `feedback` VARCHAR(500) DEFAULT NULL,
  `rating` TINYINT DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修工单';

USE efh_community_0;

CREATE TABLE IF NOT EXISTS `post_collection` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `status` TINYINT DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子收藏';
