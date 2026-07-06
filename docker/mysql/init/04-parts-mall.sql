-- 配件商城完整版：购物车、订单明细、支付、物流、收货地址

USE efh_parts;

-- 购物车
CREATE TABLE IF NOT EXISTS `cart_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `parts_id` BIGINT NOT NULL COMMENT '配件ID',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_parts` (`user_id`, `parts_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车';

-- 重建订单主表（若已存在旧结构则先备份后重建）
DROP TABLE IF EXISTS `parts_order_item`;
DROP TABLE IF EXISTS `parts_payment`;
DROP TABLE IF EXISTS `parts_shipment_trace`;
DROP TABLE IF EXISTS `parts_shipment`;
DROP TABLE IF EXISTS `parts_order`;

CREATE TABLE `parts_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `buyer_id` BIGINT NOT NULL COMMENT '买家ID',
  `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '商品总额',
  `freight_amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '运费',
  `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '应付金额',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待付款 1待发货 2待收货 3已完成 4已取消',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
  `receiver_address` VARCHAR(500) NOT NULL COMMENT '收货地址',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `pay_time` DATETIME DEFAULT NULL,
  `ship_time` DATETIME DEFAULT NULL,
  `receive_time` DATETIME DEFAULT NULL,
  `cancel_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配件订单';

CREATE TABLE `parts_order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `parts_id` BIGINT NOT NULL,
  `parts_name` VARCHAR(200) NOT NULL,
  `parts_image` VARCHAR(500) DEFAULT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `quantity` INT NOT NULL,
  `subtotal` DECIMAL(10,2) NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细';

CREATE TABLE `parts_payment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `order_no` VARCHAR(64) NOT NULL,
  `pay_no` VARCHAR(64) NOT NULL COMMENT '支付流水号',
  `pay_channel` VARCHAR(20) NOT NULL DEFAULT 'mock',
  `amount` DECIMAL(10,2) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待支付 1成功 2失败',
  `pay_time` DATETIME DEFAULT NULL,
  `third_trade_no` VARCHAR(64) DEFAULT NULL COMMENT '第三方支付流水号',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pay_no` (`pay_no`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录';

CREATE TABLE `parts_shipment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `order_no` VARCHAR(64) NOT NULL,
  `carrier` VARCHAR(50) NOT NULL COMMENT '快递公司',
  `tracking_no` VARCHAR(100) NOT NULL COMMENT '运单号',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待揽收 1运输中 2派送中 3已签收',
  `ship_time` DATETIME DEFAULT NULL,
  `receive_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流信息';

CREATE TABLE `parts_shipment_trace` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `shipment_id` BIGINT NOT NULL,
  `trace_time` DATETIME NOT NULL,
  `location` VARCHAR(100) DEFAULT NULL,
  `description` VARCHAR(500) NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_shipment_id` (`shipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流轨迹';

-- 确保配件表结构完整
CREATE TABLE IF NOT EXISTS `parts` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(200) NOT NULL,
  `description` TEXT,
  `category` VARCHAR(50) NOT NULL,
  `brand` VARCHAR(100) DEFAULT NULL,
  `model` VARCHAR(100) DEFAULT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `stock` INT NOT NULL DEFAULT 0,
  `images` VARCHAR(1000) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配件商品';

ALTER TABLE `parts` ADD COLUMN IF NOT EXISTS `seller_id` BIGINT NOT NULL DEFAULT 1 COMMENT '卖家ID';
ALTER TABLE `parts` ADD COLUMN IF NOT EXISTS `sales_count` INT NOT NULL DEFAULT 0 COMMENT '销量';

INSERT IGNORE INTO `parts` (`id`, `seller_id`, `name`, `description`, `category`, `brand`, `model`, `price`, `stock`, `images`, `status`, `sales_count`) VALUES
(1, 1, '锂电池组 48V 400Ah', '适用于各类电动叉车，续航持久，充电快速', '电池', '宁德时代', 'CB48-400', 12800.00, 50, 'https://via.placeholder.com/400x300?text=Battery', 1, 128),
(2, 1, '永磁同步电机 15kW', '高效节能电机，低噪音运行，维护成本低', '电机', '汇川', 'PM15K', 8600.00, 30, 'https://via.placeholder.com/400x300?text=Motor', 1, 86),
(3, 1, '智能控制器', '精准控制，多重保护，延长设备使用寿命', '控制器', '松正', 'SC-200', 3200.00, 100, 'https://via.placeholder.com/400x300?text=Controller', 1, 256),
(4, 1, '液压油缸', '高品质液压油缸，承载能力强', '液压', '力士乐', 'HY-80', 4500.00, 40, 'https://via.placeholder.com/400x300?text=Hydraulic', 1, 45),
(5, 1, '充电机 48V', '智能充电，过充保护', '充电设备', '英飞源', 'CH48-100', 2800.00, 60, 'https://via.placeholder.com/400x300?text=Charger', 1, 72);

USE efh_user_0;

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
