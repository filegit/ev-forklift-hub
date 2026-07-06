USE efh_user_0;
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `user_type` TINYINT DEFAULT 1 COMMENT '用户类型' AFTER `gender`;
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `points` INT DEFAULT 0 COMMENT '积分' AFTER `status`;
UPDATE `user` SET user_type = 9, password = '$2a$10$9yJT16f8BegQ0e2kALAxYehmRTU.pSD4snwcF93oY79/HOrVONv9C' WHERE username = 'admin';

USE efh_user_1;
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `user_type` TINYINT DEFAULT 1 COMMENT '用户类型' AFTER `gender`;
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `points` INT DEFAULT 0 COMMENT '积分' AFTER `status`;

INSERT INTO efh_user_0.`user` (`username`, `password`, `nickname`, `phone`, `email`, `gender`, `user_type`, `status`, `points`)
SELECT 'kbadmin', '$2a$10$9yJT16f8BegQ0e2kALAxYehmRTU.pSD4snwcF93oY79/HOrVONv9C', '知识库管理员', '13900000001', 'kbadmin@efh.com', 0, 9, 1, 5000
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM efh_user_0.`user` WHERE username = 'kbadmin');
