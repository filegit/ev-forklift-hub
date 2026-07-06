-- 知识库数据库
CREATE DATABASE IF NOT EXISTS `efh_knowledge` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `efh_knowledge`;

CREATE TABLE IF NOT EXISTS `knowledge_doc` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL COMMENT '标题',
  `summary` varchar(500) DEFAULT NULL COMMENT '摘要',
  `category` varchar(50) DEFAULT NULL COMMENT '分类',
  `file_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `file_path` varchar(500) DEFAULT NULL COMMENT '存储路径',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(20) DEFAULT NULL COMMENT '文件类型',
  `access_type` tinyint NOT NULL DEFAULT 0 COMMENT '0免费 1积分 2付费 3积分或付费',
  `points_price` int DEFAULT 0 COMMENT '积分价格',
  `money_price` decimal(10,2) DEFAULT 0.00 COMMENT '现金价格',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0草稿 1已发布 2已下架',
  `download_count` int DEFAULT 0,
  `view_count` int DEFAULT 0,
  `created_by` bigint DEFAULT NULL COMMENT '上传管理员',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档';

CREATE TABLE IF NOT EXISTS `knowledge_unlock` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `doc_id` bigint NOT NULL,
  `unlock_type` varchar(20) NOT NULL COMMENT 'free/points/alipay',
  `points_cost` int DEFAULT 0,
  `money_paid` decimal(10,2) DEFAULT 0.00,
  `pay_no` varchar(64) DEFAULT NULL,
  `third_trade_no` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_doc` (`user_id`, `doc_id`),
  KEY `idx_pay_no` (`pay_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档解锁记录';
