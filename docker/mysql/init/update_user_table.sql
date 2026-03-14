-- 为现有用户表添加缺失的字段
-- 执行此脚本来更新数据库结构

USE efh_user_0;

-- 添加 user_type 字段
ALTER TABLE `user` ADD COLUMN `user_type` TINYINT DEFAULT 1 COMMENT '用户类型：1-普通用户，2-技师，3-商家' AFTER `gender`;

-- 添加 points 字段
ALTER TABLE `user` ADD COLUMN `points` INT DEFAULT 0 COMMENT '积分' AFTER `status`;

-- 修改 id 为自增
ALTER TABLE `user` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID';

-- 添加手机号唯一索引
ALTER TABLE `user` ADD UNIQUE KEY `uk_phone` (`phone`);

USE efh_user_1;

-- 添加 user_type 字段
ALTER TABLE `user` ADD COLUMN `user_type` TINYINT DEFAULT 1 COMMENT '用户类型：1-普通用户，2-技师，3-商家' AFTER `gender`;

-- 添加 points 字段
ALTER TABLE `user` ADD COLUMN `points` INT DEFAULT 0 COMMENT '积分' AFTER `status`;

-- 修改 id 为自增
ALTER TABLE `user` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID';

-- 添加手机号唯一索引
ALTER TABLE `user` ADD UNIQUE KEY `uk_phone` (`phone`);
