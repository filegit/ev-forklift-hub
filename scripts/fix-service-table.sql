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
