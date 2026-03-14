-- 创建数据库
CREATE DATABASE IF NOT EXISTS ev_forklift_hub DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ev_forklift_hub;

-- 用户表
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `user_type` INT NOT NULL DEFAULT 1 COMMENT '用户类型：1-普通用户 2-技师 3-商家',
  `status` INT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
  `points` INT NOT NULL DEFAULT 0 COMMENT '积分',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 帖子表
CREATE TABLE `post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容',
  `category` INT NOT NULL COMMENT '分类：1-技术交流 2-故障求助 3-经验分享 4-其他',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
  `status` INT NOT NULL DEFAULT 1 COMMENT '状态：0-待审核 1-已发布 2-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category` (`category`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';

-- 评论表
CREATE TABLE `comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父评论ID，0表示一级评论',
  `content` VARCHAR(500) NOT NULL COMMENT '内容',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 零部件表
CREATE TABLE `parts` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
  `name` VARCHAR(200) NOT NULL COMMENT '名称',
  `description` TEXT COMMENT '描述',
  `category` VARCHAR(50) NOT NULL COMMENT '分类',
  `brand` VARCHAR(100) COMMENT '品牌',
  `model` VARCHAR(100) COMMENT '型号',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `stock` INT NOT NULL DEFAULT 0 COMMENT '库存',
  `images` VARCHAR(1000) COMMENT '图片',
  `status` INT NOT NULL DEFAULT 1 COMMENT '状态：0-下架 1-上架',
  `sales_count` INT NOT NULL DEFAULT 0 COMMENT '销量',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='零部件表';

-- 零部件订单表
CREATE TABLE `parts_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
  `buyer_id` BIGINT NOT NULL COMMENT '买家ID',
  `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
  `parts_id` BIGINT NOT NULL COMMENT '零部件ID',
  `parts_name` VARCHAR(200) NOT NULL COMMENT '零部件名称',
  `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
  `quantity` INT NOT NULL COMMENT '数量',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '总金额',
  `status` INT NOT NULL DEFAULT 0 COMMENT '状态：0-待付款 1-待发货 2-待收货 3-已完成 4-已取消',
  `address` VARCHAR(500) NOT NULL COMMENT '收货地址',
  `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_seller_id` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='零部件订单表';

-- 服务工单表
CREATE TABLE `service_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `technician_id` BIGINT DEFAULT NULL COMMENT '技师ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '工单号',
  `service_type` INT NOT NULL COMMENT '服务类型：1-维修 2-保养 3-咨询',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `description` TEXT NOT NULL COMMENT '描述',
  `images` VARCHAR(1000) COMMENT '图片',
  `address` VARCHAR(500) NOT NULL COMMENT '服务地址',
  `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
  `status` INT NOT NULL DEFAULT 0 COMMENT '状态：0-待接单 1-已接单 2-服务中 3-已完成 4-已取消',
  `feedback` TEXT COMMENT '用户反馈',
  `rating` INT DEFAULT NULL COMMENT '评分：1-5',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_technician_id` (`technician_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务工单表';

-- 插入测试数据
INSERT INTO `user` (`username`, `password`, `nickname`, `user_type`, `status`, `points`) VALUES
('admin', '21232f297a57a5a743894a0e4a801fc3', '管理员', 1, 1, 0),
('technician', '5f4dcc3b5aa765d61d8327deb882cf99', '技师张三', 2, 1, 100),
('merchant', '5f4dcc3b5aa765d61d8327deb882cf99', '商家李四', 3, 1, 200);
