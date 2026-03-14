-- ==========================================
-- 快速修复用户表结构
-- 直接复制此脚本到 MySQL 客户端执行
-- ==========================================

-- 修复 efh_user_0 数据库
USE efh_user_0;

-- 添加 user_type 字段
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `user_type` TINYINT DEFAULT 1 COMMENT '用户类型：1-普通用户，2-技师，3-商家' AFTER `gender`;

-- 添加 points 字段
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `points` INT DEFAULT 0 COMMENT '积分' AFTER `status`;

-- 修改 id 为自增
ALTER TABLE `user` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID';

-- 验证表结构
DESC user;

-- 修复 efh_user_1 数据库
USE efh_user_1;

-- 添加 user_type 字段
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `user_type` TINYINT DEFAULT 1 COMMENT '用户类型：1-普通用户，2-技师，3-商家' AFTER `gender`;

-- 添加 points 字段
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `points` INT DEFAULT 0 COMMENT '积分' AFTER `status`;

-- 修改 id 为自增
ALTER TABLE `user` MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID';

-- 验证表结构
DESC user;

-- 显示完成信息
SELECT '✅ 用户表结构修复完成！' AS message;
