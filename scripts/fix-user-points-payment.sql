USE efh_user_0;

CREATE TABLE IF NOT EXISTS `points_payment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `pay_no` varchar(64) NOT NULL,
  `package_id` int NOT NULL,
  `points` int NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `pay_channel` varchar(20) DEFAULT 'alipay',
  `status` tinyint DEFAULT 0 COMMENT '0 pending, 1 success, 2 failed',
  `pay_time` datetime DEFAULT NULL,
  `third_trade_no` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pay_no` (`pay_no`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='points purchase payment';
