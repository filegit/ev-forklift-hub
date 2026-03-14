-- ==========================================
-- 修复社区服务数据库表结构
-- 将 post 表的 id 字段设置为自增
-- ==========================================

USE efh_community_0;

-- 修改 post_0 表
ALTER TABLE post_0 MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT COMMENT '帖子ID';

-- 查看表结构
DESC post_0;

-- 显示完成信息
SELECT '✅ efh_community_0.post_0 表修复完成！' AS message;
