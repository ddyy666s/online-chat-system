
-- =============================================
-- 在线聊天系统 - 数据库初始化脚本
-- 包含：建库、建表、测试数据
-- =============================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS `chat_db` 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `chat_db`;

-- =============================================
-- 2. 用户表（包含管理员字段）
-- =============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `signature` VARCHAR(100) DEFAULT NULL COMMENT '个性签名',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色: user-普通用户, admin-管理员',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0禁用,1正常',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 3. 好友关系表
-- =============================================
DROP TABLE IF EXISTS `friend`;
CREATE TABLE `friend` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关系ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `friend_id` BIGINT NOT NULL COMMENT '好友ID',
    `group_name` VARCHAR(50) NOT NULL DEFAULT '我的好友' COMMENT '分组名称',
    `remark` VARCHAR(50) DEFAULT NULL COMMENT '好友备注',
    `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶:0否,1是',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '成为好友时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_friend_id` (`friend_id`),
    CONSTRAINT `fk_friend_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_friend_friend` FOREIGN KEY (`friend_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系表';

-- =============================================
-- 4. 聊天记录表
-- =============================================
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `from_user_id` BIGINT NOT NULL COMMENT '发送者ID',
    `to_user_id` BIGINT NOT NULL COMMENT '接收者ID',
    `message_type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型:1文字,2图片,3文件',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读:0未读,1已读',
    `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
    `recall_time` DATETIME DEFAULT NULL COMMENT '撤回时间(不为空表示已撤回)',
    `send_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    KEY `idx_from_to_time` (`from_user_id`, `to_user_id`, `send_time`),
    KEY `idx_to_read` (`to_user_id`, `is_read`),
    KEY `idx_send_time` (`send_time`),
    CONSTRAINT `fk_message_from` FOREIGN KEY (`from_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_message_to` FOREIGN KEY (`to_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天记录表';

-- =============================================
-- 5. 好友印象评价表
-- =============================================
DROP TABLE IF EXISTS `impression`;
CREATE TABLE `impression` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `from_user_id` BIGINT NOT NULL COMMENT '评价者ID',
    `to_user_id` BIGINT NOT NULL COMMENT '被评价者ID',
    `content` VARCHAR(100) NOT NULL COMMENT '评价内容',
    `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除:0正常,1删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    PRIMARY KEY (`id`),
    KEY `idx_to_user` (`to_user_id`),
    KEY `idx_from_user` (`from_user_id`),
    CONSTRAINT `fk_impression_from` FOREIGN KEY (`from_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_impression_to` FOREIGN KEY (`to_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友印象评价表';

-- =============================================
-- 6. 好友申请表
-- =============================================
DROP TABLE IF EXISTS `friend_request`;
CREATE TABLE `friend_request` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请ID',
    `from_user_id` BIGINT NOT NULL COMMENT '申请者ID',
    `to_user_id` BIGINT NOT NULL COMMENT '接收者ID',
    `message` VARCHAR(100) DEFAULT NULL COMMENT '验证消息',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态:0待处理,1已同意,2已拒绝,3已过期',
    `handled_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    PRIMARY KEY (`id`),
    KEY `idx_to_status` (`to_user_id`, `status`),
    KEY `idx_from_user` (`from_user_id`),
    CONSTRAINT `fk_request_from` FOREIGN KEY (`from_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_request_to` FOREIGN KEY (`to_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友申请表';

-- =============================================
-- 7. 未读消息统计视图
-- =============================================
DROP VIEW IF EXISTS `unread_message_stats`;
CREATE VIEW `unread_message_stats` AS
SELECT 
    `to_user_id` AS `user_id`,
    `from_user_id` AS `friend_id`,
    COUNT(*) AS `unread_count`,
    MAX(`send_time`) AS `last_send_time`
FROM `message`
WHERE `is_read` = 0 AND `recall_time` IS NULL
GROUP BY `to_user_id`, `from_user_id`;

-- =============================================
-- 8. 测试数据
-- =============================================

-- 8.1 插入管理员账号
-- 密码: admin123 (BCrypt加密: $2a$10$NkMJU3xZJ2QJ2QJ2QJ2QuJ2QJ2QJ2QJ2QJ2Q)
-- 注意：实际使用时请替换为真实的BCrypt加密密码
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`) VALUES 
('admin', '$2a$10$NkMJU3xZJ2QJ2QJ2QJ2QuJ2QJ2QJ2QJ2QJ2Q', '系统管理员', 'admin', 1);

-- 8.2 插入普通用户测试数据
INSERT INTO `user` (`username`, `password`, `nickname`, `signature`, `email`, `status`) VALUES 
('zhangsan', '$2a$10$NkMJU3xZJ2QJ2QJ2QJ2QuJ2QJ2QJ2QJ2QJ2Q', '张三', '好好学习，天天向上', 'zhangsan@example.com', 1),
('lisi', '$2a$10$NkMJU3xZJ2QJ2QJ2QJ2QuJ2QJ2QJ2QJ2QJ2Q', '李四', '代码改变世界', 'lisi@example.com', 1),
('wangwu', '$2a$10$NkMJU3xZJ2QJ2QJ2QJ2QuJ2QJ2QJ2QJ2QJ2Q', '王五', '热爱生活', 'wangwu@example.com', 1),
('zhaoliu', '$2a$10$NkMJU3xZJ2QJ2QJ2QJ2QuJ2QJ2QJ2QJ2QJ2Q', '赵六', '健身爱好者', 'zhaoliu@example.com', 1);

-- 8.3 插入好友关系数据
-- 张三(2) 和 李四(3) 互为好友
INSERT INTO `friend` (`user_id`, `friend_id`, `group_name`, `remark`) VALUES 
(2, 3, '同学', '老李'),
(3, 2, '同学', '老张');

-- 张三(2) 和 王五(4) 互为好友
INSERT INTO `friend` (`user_id`, `friend_id`, `group_name`, `remark`) VALUES 
(2, 4, '同事', '王工'),
(4, 2, '同事', '张工');

-- 李四(3) 和 王五(4) 互为好友
INSERT INTO `friend` (`user_id`, `friend_id`, `group_name`) VALUES 
(3, 4, '球友'),
(4, 3, '球友');

-- 8.4 插入聊天记录数据
-- 张三(2) 和 李四(3) 的聊天记录
INSERT INTO `message` (`from_user_id`, `to_user_id`, `content`, `is_read`, `send_time`) VALUES 
(2, 3, '李四，下午开会记得来', 1, '2026-05-07 09:00:00'),
(3, 2, '好的，收到！', 1, '2026-05-07 09:05:00'),
(2, 3, '项目进度怎么样了？', 1, '2026-05-07 10:00:00'),
(3, 2, '完成了80%，明天可以提交', 0, '2026-05-07 10:30:00'),
(3, 2, '对了，晚上一起吃饭？', 0, '2026-05-07 11:00:00');

-- 张三(2) 和 王五(4) 的聊天记录
INSERT INTO `message` (`from_user_id`, `to_user_id`, `content`, `is_read`, `send_time`) VALUES 
(2, 4, '王工，代码review一下', 1, '2026-05-07 08:00:00'),
(4, 2, '好的，我看看', 1, '2026-05-07 08:15:00'),
(4, 2, '整体不错，有几个小问题改一下', 0, '2026-05-07 08:30:00');

-- 8.5 插入好友印象评价
-- 张三 对 李四 的评价
INSERT INTO `impression` (`from_user_id`, `to_user_id`, `content`) VALUES 
(2, 3, '靠谱'),
(2, 3, '技术大牛');

-- 李四 对 张三 的评价
INSERT INTO `impression` (`from_user_id`, `to_user_id`, `content`) VALUES 
(3, 2, '热心肠'),
(3, 2, '工作认真');

-- 王五 对 张三 的评价
INSERT INTO `impression` (`from_user_id`, `to_user_id`, `content`) VALUES 
(4, 2, '好大哥');

-- 8.6 插入好友申请数据
-- 赵六(5) 向 张三(2) 发送好友申请
INSERT INTO `friend_request` (`from_user_id`, `to_user_id`, `message`, `status`, `expire_time`) VALUES 
(5, 2, '你好，我是赵六，认识一下', 0, DATE_ADD(NOW(), INTERVAL 7 DAY));

-- 王五(4) 向 赵六(5) 发送好友申请（已同意）
INSERT INTO `friend_request` (`from_user_id`, `to_user_id`, `message`, `status`, `handled_time`) VALUES 
(4, 5, '一起打球吗？', 1, NOW());

-- =============================================
-- 9. 验证数据
-- =============================================
-- 查看所有用户
SELECT * FROM `user`;

-- 查看好友关系
SELECT u1.nickname AS '用户', u2.nickname AS '好友', f.group_name AS '分组', f.remark AS '备注'
FROM `friend` f
JOIN `user` u1 ON f.user_id = u1.id
JOIN `user` u2 ON f.friend_id = u2.id;

-- 查看未读消息统计
SELECT * FROM `unread_message_stats`;

-- 查看好友印象
SELECT u1.nickname AS '评价人', u2.nickname AS '被评价人', i.content AS '评价内容'
FROM `impression` i
JOIN `user` u1 ON i.from_user_id = u1.id
JOIN `user` u2 ON i.to_user_id = u2.id
WHERE i.is_delete = 0;
```
