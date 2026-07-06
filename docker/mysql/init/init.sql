-- ==========================================
-- EV-Forklift-Hub 数据库初始化脚本
-- ==========================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==========================================
-- 1. 创建 Nacos 配置数据库
-- ==========================================
CREATE DATABASE IF NOT EXISTS `nacos_config` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `nacos_config`;

-- Nacos 配置表
CREATE TABLE IF NOT EXISTS `config_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) NOT NULL COMMENT 'data_id',
  `group_id` varchar(255) DEFAULT NULL,
  `content` longtext NOT NULL COMMENT 'content',
  `md5` varchar(32) DEFAULT NULL COMMENT 'md5',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `src_user` text COMMENT 'source user',
  `src_ip` varchar(50) DEFAULT NULL COMMENT 'source ip',
  `app_name` varchar(128) DEFAULT NULL,
  `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
  `c_desc` varchar(256) DEFAULT NULL,
  `c_use` varchar(64) DEFAULT NULL,
  `effect` varchar(64) DEFAULT NULL,
  `type` varchar(64) DEFAULT NULL,
  `c_schema` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_configinfo_datagrouptenant` (`data_id`,`group_id`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='config_info';

-- 其他 Nacos 必需表（简化版）
CREATE TABLE IF NOT EXISTS `config_info_aggr` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `data_id` varchar(255) NOT NULL COMMENT 'data_id',
  `group_id` varchar(255) NOT NULL COMMENT 'group_id',
  `datum_id` varchar(255) NOT NULL COMMENT 'datum_id',
  `content` longtext NOT NULL COMMENT '内容',
  `gmt_modified` datetime NOT NULL COMMENT '修改时间',
  `app_name` varchar(128) DEFAULT NULL,
  `tenant_id` varchar(128) DEFAULT '' COMMENT '租户字段',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_configinfoaggr_datagrouptenantdatum` (`data_id`,`group_id`,`tenant_id`,`datum_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='增加租户字段';

-- ==========================================
-- 2. 创建用户服务数据库（分库）
-- ==========================================
CREATE DATABASE IF NOT EXISTS `efh_user_0` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `efh_user_1` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 用户表 - 数据库 0
USE `efh_user_0`;

CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（加密）',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(200) DEFAULT NULL COMMENT '头像URL',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 用户表 - 数据库 1
USE `efh_user_1`;

CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（加密）',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(200) DEFAULT NULL COMMENT '头像URL',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ==========================================
-- 3. 创建社区服务数据库（分库）
-- ==========================================
CREATE DATABASE IF NOT EXISTS `efh_community_0` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `efh_community_1` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 帖子表 - 数据库 0（需要创建 4 个分表）
USE `efh_community_0`;

CREATE TABLE IF NOT EXISTS `post_0` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '分类',
  `images` TEXT DEFAULT NULL COMMENT '图片URL列表（JSON）',
  `view_count` INT DEFAULT 0 COMMENT '浏览量',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT DEFAULT 0 COMMENT '评论数',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-删除，1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category` (`category`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表_0';

CREATE TABLE IF NOT EXISTS `post_1` LIKE `post_0`;
CREATE TABLE IF NOT EXISTS `post_2` LIKE `post_0`;
CREATE TABLE IF NOT EXISTS `post_3` LIKE `post_0`;

-- 评论表 - 数据库 0（需要创建 4 个分表）
CREATE TABLE IF NOT EXISTS `comment_0` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID（0表示一级评论）',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-删除，1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表_0';

CREATE TABLE IF NOT EXISTS `comment_1` LIKE `comment_0`;
CREATE TABLE IF NOT EXISTS `comment_2` LIKE `comment_0`;
CREATE TABLE IF NOT EXISTS `comment_3` LIKE `comment_0`;

-- 帖子表 - 数据库 1（需要创建 4 个分表）
USE `efh_community_1`;

CREATE TABLE IF NOT EXISTS `post_0` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '分类',
  `images` TEXT DEFAULT NULL COMMENT '图片URL列表（JSON）',
  `view_count` INT DEFAULT 0 COMMENT '浏览量',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT DEFAULT 0 COMMENT '评论数',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-删除，1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category` (`category`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表_0';

CREATE TABLE IF NOT EXISTS `post_1` LIKE `post_0`;
CREATE TABLE IF NOT EXISTS `post_2` LIKE `post_0`;
CREATE TABLE IF NOT EXISTS `post_3` LIKE `post_0`;

-- 评论表 - 数据库 1（需要创建 4 个分表）
CREATE TABLE IF NOT EXISTS `comment_0` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID（0表示一级评论）',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-删除，1-正常',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表_0';

CREATE TABLE IF NOT EXISTS `comment_1` LIKE `comment_0`;
CREATE TABLE IF NOT EXISTS `comment_2` LIKE `comment_0`;
CREATE TABLE IF NOT EXISTS `comment_3` LIKE `comment_0`;

-- ==========================================
-- 4. 创建配件服务数据库
-- ==========================================
CREATE DATABASE IF NOT EXISTS `efh_parts` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `efh_parts`;

-- 配件表
CREATE TABLE IF NOT EXISTS `parts` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配件ID',
  `seller_id` BIGINT NOT NULL DEFAULT 1 COMMENT '卖家ID',
  `name` VARCHAR(100) NOT NULL COMMENT '配件名称',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '分类',
  `brand` VARCHAR(50) DEFAULT NULL COMMENT '品牌',
  `model` VARCHAR(100) DEFAULT NULL COMMENT '型号',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `stock` INT DEFAULT 0 COMMENT '库存',
  `images` TEXT DEFAULT NULL COMMENT '图片URL列表（JSON）',
  `description` TEXT DEFAULT NULL COMMENT '描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `sales_count` INT NOT NULL DEFAULT 0 COMMENT '销量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_brand` (`brand`),
  KEY `idx_seller_id` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT='配件表';

-- 配件订单表
CREATE TABLE IF NOT EXISTS `parts_order` (
  `id` BIGINT NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `parts_id` BIGINT NOT NULL COMMENT '配件ID',
  `quantity` INT NOT NULL COMMENT '数量',
  `total_price` DECIMAL(10,2) NOT NULL COMMENT '总价',
  `status` TINYINT DEFAULT 1 COMMENT '状态：1-待支付，2-已支付，3-已发货，4-已完成，5-已取消',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parts_id` (`parts_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配件订单表';

-- ==========================================
-- 5. 创建维修服务数据库
-- ==========================================
CREATE DATABASE IF NOT EXISTS `efh_service` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `efh_service`;

-- 维修工单表（与 ServiceOrder 实体字段一致）
CREATE TABLE IF NOT EXISTS `service_order` (
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
  `status` TINYINT DEFAULT 0 COMMENT '0待处理 1已接单 2服务中 3已完成 4已取消',
  `feedback` VARCHAR(500) DEFAULT NULL,
  `rating` TINYINT DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修工单';

-- 维修师傅表
CREATE TABLE IF NOT EXISTS `technician` (
  `id` BIGINT NOT NULL COMMENT '师傅ID',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '电话',
  `skill_level` TINYINT DEFAULT 1 COMMENT '技能等级：1-初级，2-中级，3-高级',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-离职，1-在职',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='维修师傅表';

-- ==========================================
-- 6. 插入测试数据
-- ==========================================

-- 插入测试用户（密码：123456，BCrypt 加密）
USE `efh_user_0`;
INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `phone`, `email`, `gender`, `status`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', '13800138000', 'admin@efh.com', 1, 1),
(3, 'user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '用户1', '13800138001', 'user1@efh.com', 1, 1);

USE `efh_user_1`;
INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `phone`, `email`, `gender`, `status`) VALUES
(2, 'test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '测试用户', '13800138002', 'test@efh.com', 2, 1),
(4, 'user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '用户2', '13800138003', 'user2@efh.com', 1, 1);

-- 插入测试配件
USE `efh_parts`;
INSERT INTO `parts` (`id`, `seller_id`, `name`, `category`, `brand`, `model`, `price`, `stock`, `description`, `status`, `sales_count`) VALUES
(1, 1, '电池组', '电池', '比亚迪', 'BYD-48V-200AH', 8999.00, 50, '48V 200AH 磷酸铁锂电池组', 1, 0),
(2, 1, '电机', '动力系统', '汇川', 'HC-15KW', 12999.00, 30, '15KW 交流电机', 1, 0),
(3, 1, '控制器', '电控系统', '英威腾', 'YWT-500A', 5999.00, 40, '500A 控制器', 1, 0);

-- 插入测试维修师傅
USE `efh_service`;
INSERT INTO `technician` (`id`, `name`, `phone`, `skill_level`, `status`) VALUES
(1, '张师傅', '13900139001', 3, 1),
(2, '李师傅', '13900139002', 2, 1),
(3, '王师傅', '13900139003', 2, 1);

SET FOREIGN_KEY_CHECKS = 1;

-- 初始化完成
SELECT '数据库初始化完成！' AS message;
