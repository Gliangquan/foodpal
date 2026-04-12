-- ============================================
-- 添加用户性别和年龄字段
-- 执行时间: 2026-03-04
-- ============================================

USE foodpal;

-- 添加性别字段
ALTER TABLE user ADD COLUMN gender VARCHAR(10) NULL COMMENT '性别' AFTER user_email;

-- 添加年龄字段
ALTER TABLE user ADD COLUMN age INT NULL COMMENT '年龄' AFTER gender;

-- 验证字段是否添加成功
SHOW COLUMNS FROM user LIKE 'gender';
SHOW COLUMNS FROM user LIKE 'age';
