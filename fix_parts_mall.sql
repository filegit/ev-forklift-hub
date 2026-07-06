-- MySQL 5.7 兼容：配件商城表结构补丁（可重复执行部分步骤）

USE efh_parts;

-- 补全 parts 表字段
SET @db = 'efh_parts';
SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='parts' AND COLUMN_NAME='seller_id')=0,
  'ALTER TABLE parts ADD COLUMN seller_id BIGINT NOT NULL DEFAULT 1 COMMENT ''卖家ID''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='parts' AND COLUMN_NAME='sales_count')=0,
  'ALTER TABLE parts ADD COLUMN sales_count INT NOT NULL DEFAULT 0 COMMENT ''销量''', 'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

DELETE FROM parts;
INSERT INTO `parts` (`id`, `seller_id`, `name`, `description`, `category`, `brand`, `model`, `price`, `stock`, `images`, `status`, `sales_count`) VALUES
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
