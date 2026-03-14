-- 创建业务数据库初始化脚本

-- 用户服务数据库
CREATE DATABASE IF NOT EXISTS efh_user_0 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS efh_user_1 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 社区服务数据库
CREATE DATABASE IF NOT EXISTS efh_community_0 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS efh_community_1 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 配件服务数据库
CREATE DATABASE IF NOT EXISTS efh_parts DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 维修服务数据库
CREATE DATABASE IF NOT EXISTS efh_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 注意：建表语句请参考 database.sql 文件
-- 这里只创建数据库，表结构由应用启动时自动创建或手动执行 database.sql
