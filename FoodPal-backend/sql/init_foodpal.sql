-- ============================================
-- FoodPal 高校食堂通 - 数据库初始化脚本
-- 数据库名称: foodpal
-- 说明: 仅包含食堂功能相关表，不包含陪诊功能
-- ============================================

-- 创建数据库
create database if not exists foodpal character set utf8mb4 collate utf8mb4_unicode_ci;
use foodpal;

-- ============================================
-- 1. 用户相关表
-- ============================================

-- 用户表（学生/商户/监督管理员/管理员）
drop table if exists user;
create table user(
    id bigint auto_increment comment 'id' primary key,
    user_account varchar(256) not null unique comment '账号',
    user_password varchar(512) not null comment '密码',
    user_name varchar(256) null comment '用户昵称',
    user_avatar varchar(1024) null comment '用户头像',
    user_profile varchar(512) null comment '用户简介',
    user_role varchar(256) default 'user' not null comment '用户角色：student/merchant/supervisor/admin',
    user_phone varchar(20) null unique comment '手机号',
    user_email varchar(256) null unique comment '邮箱',
    gender varchar(10) null comment '性别',
    age int null comment '年龄',
    status tinyint default 1 not null comment '账户状态：1启用 0禁用',
    union_id varchar(256) null unique comment '开放平台id（微信 UnionID）',
    mp_open_id varchar(256) null unique comment '小程序openId',
    create_by varchar(256) null comment '创建人',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_by varchar(256) null comment '更新人',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete tinyint default 0 not null comment '是否删除',
    index idx_user_phone (user_phone),
    index idx_user_account (user_account),
    index idx_create_time (create_time)
) comment '用户表' collate = utf8mb4_unicode_ci;

-- ============================================
-- 2. 商户/食堂模块表
-- ============================================

-- 商户资料表
drop table if exists merchant_profile;
create table merchant_profile(
    id bigint auto_increment comment '主键' primary key,
    user_id bigint not null comment '关联用户ID(商户账号)',
    merchant_name varchar(128) not null comment '店铺名称',
    contact_name varchar(64) null comment '联系人',
    contact_phone varchar(20) null comment '联系电话',
    avatar varchar(1024) null comment '店铺头像',
    business_hours varchar(128) null comment '营业时间',
    location varchar(256) null comment '地理位置',
    description varchar(512) null comment '备注描述',
    status tinyint default 1 not null comment '状态:1启用 0停用',
    audit_status varchar(32) default 'approved' comment '审核状态:pending/approved/rejected',
    pending_data text null comment '待审核变更(JSON)',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete tinyint default 0 not null comment '逻辑删除',
    unique key uk_merchant_user (user_id),
    index idx_merchant_name (merchant_name)
) comment '商户资料表' collate = utf8mb4_unicode_ci;

-- 菜品表
drop table if exists dish;
create table dish(
    id bigint auto_increment comment '主键' primary key,
    merchant_id bigint not null comment '商户ID',
    dish_name varchar(128) not null comment '菜品名称',
    dish_price decimal(10,2) not null comment '原价',
    ingredients varchar(512) null comment '食材组成',
    nutrition_info varchar(512) null comment '营养成分',
    dish_image varchar(1024) null comment '菜品图片',
    category varchar(64) null comment '分类：主食/面食/汤类/凉菜/小吃等',
    supply_status tinyint default 1 not null comment '供应状态:1可供应 0不可供应',
    special_price decimal(10,2) null comment '特价',
    special_start_time datetime null comment '特价开始时间',
    special_end_time datetime null comment '特价结束时间',
    purchase_limit int null comment '限购数量',
    like_count int default 0 not null comment '点赞数',
    favorite_count int default 0 not null comment '收藏数',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete tinyint default 0 not null comment '逻辑删除',
    index idx_dish_merchant (merchant_id),
    index idx_dish_name (dish_name),
    index idx_dish_category (category)
) comment '菜品表' collate = utf8mb4_unicode_ci;

-- 菜品收藏表
drop table if exists dish_favorite;
create table dish_favorite(
    id bigint auto_increment comment '主键' primary key,
    user_id bigint not null comment '学生用户ID',
    dish_id bigint not null comment '菜品ID',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    is_delete tinyint default 0 not null comment '逻辑删除',
    unique key uk_user_dish_favorite (user_id, dish_id),
    index idx_favorite_dish (dish_id)
) comment '菜品收藏表' collate = utf8mb4_unicode_ci;

-- 菜品点赞表
drop table if exists dish_like;
create table dish_like(
    id bigint auto_increment comment '主键' primary key,
    user_id bigint not null comment '学生用户ID',
    dish_id bigint not null comment '菜品ID',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    is_delete tinyint default 0 not null comment '逻辑删除',
    unique key uk_user_dish_like (user_id, dish_id),
    index idx_like_dish (dish_id)
) comment '菜品点赞表' collate = utf8mb4_unicode_ci;

-- 食堂投诉表
drop table if exists canteen_complaint;
create table canteen_complaint(
    id bigint auto_increment comment '主键' primary key,
    complaint_no varchar(64) not null comment '投诉编号',
    user_id bigint not null comment '学生ID',
    merchant_id bigint not null comment '商户ID',
    complaint_title varchar(128) not null comment '投诉标题',
    complaint_content text not null comment '投诉内容',
    evidence_urls text null comment '证据(JSON数组)',
    status varchar(32) default 'pending_review' comment '状态:pending_review/pending_rectify/rectified/completed/rejected',
    process_progress varchar(128) null comment '处理进度描述',
    rectify_requirement text null comment '整改要求',
    rectify_result text null comment '整改结果',
    feedback text null comment '反馈内容',
    result_rating int null comment '处理结果评价:1不满意/2一般/3满意',
    process_by bigint null comment '处理人',
    process_time datetime null comment '处理时间',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete tinyint default 0 not null comment '逻辑删除',
    unique key uk_complaint_no (complaint_no),
    index idx_complaint_status (status),
    index idx_complaint_merchant (merchant_id),
    index idx_complaint_user (user_id)
) comment '食堂投诉表' collate = utf8mb4_unicode_ci;

-- 食堂动态表
drop table if exists canteen_dynamic;
create table canteen_dynamic(
    id bigint auto_increment comment '主键' primary key,
    publisher_id bigint not null comment '发布人用户ID',
    publisher_type varchar(32) not null comment '发布主体类型:merchant/admin',
    merchant_id bigint null comment '商户ID',
    title varchar(128) not null comment '标题',
    content text not null comment '内容',
    cover_image varchar(1024) null comment '封面图',
    media_urls text null comment '图片/视频(JSON数组)',
    news_type varchar(32) null comment '资讯类型:new_dish/activity/notice',
    audit_status varchar(32) default 'pending' comment '审核状态:pending/approved/rejected',
    audit_reason text null comment '审核意见',
    status varchar(32) default 'draft' comment '发布状态:draft/published/unpublished',
    expire_time datetime null comment '展示截止时间',
    like_count int default 0 not null comment '点赞数',
    comment_count int default 0 not null comment '评论数',
    share_count int default 0 not null comment '分享数',
    publish_time datetime null comment '发布时间',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete tinyint default 0 not null comment '逻辑删除',
    index idx_dynamic_status (status),
    index idx_dynamic_publish_time (publish_time),
    index idx_dynamic_merchant (merchant_id)
) comment '食堂动态表' collate = utf8mb4_unicode_ci;

-- 食堂动态点赞表
drop table if exists canteen_dynamic_like;
create table canteen_dynamic_like(
    id bigint auto_increment comment '主键' primary key,
    dynamic_id bigint not null comment '动态ID',
    user_id bigint not null comment '用户ID',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    is_delete tinyint default 0 not null comment '逻辑删除',
    unique key uk_dynamic_like (dynamic_id, user_id),
    index idx_dynamic_like_dynamic (dynamic_id),
    index idx_dynamic_like_user (user_id)
) comment '食堂动态点赞表' collate = utf8mb4_unicode_ci;

-- 食堂动态评论表
drop table if exists canteen_dynamic_comment;
create table canteen_dynamic_comment(
    id bigint auto_increment comment '主键' primary key,
    dynamic_id bigint not null comment '动态ID',
    user_id bigint not null comment '用户ID',
    content varchar(500) not null comment '评论内容',
    parent_id bigint null comment '父评论ID',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete tinyint default 0 not null comment '逻辑删除',
    index idx_dynamic_comment_dynamic (dynamic_id),
    index idx_dynamic_comment_user (user_id)
) comment '食堂动态评论表' collate = utf8mb4_unicode_ci;

-- 食堂动态分享记录表
drop table if exists canteen_dynamic_share;
create table canteen_dynamic_share(
    id bigint auto_increment comment '主键' primary key,
    dynamic_id bigint not null comment '动态ID',
    user_id bigint not null comment '用户ID',
    share_channel varchar(32) null comment '分享渠道',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    is_delete tinyint default 0 not null comment '逻辑删除',
    index idx_dynamic_share_dynamic (dynamic_id),
    index idx_dynamic_share_user (user_id)
) comment '食堂动态分享记录表' collate = utf8mb4_unicode_ci;

-- 食堂公告表
drop table if exists canteen_announcement;
create table canteen_announcement(
    id bigint auto_increment comment '主键' primary key,
    title varchar(128) not null comment '标题',
    content text not null comment '正文',
    cover_image varchar(1024) null comment '封面图',
    announcement_type varchar(32) default 'system' comment '公告类型:system/canteen',
    expire_time datetime null comment '展示截止时间',
    publish_time datetime null comment '发布时间',
    is_top tinyint default 0 not null comment '是否置顶:1是 0否',
    sort_no int default 0 not null comment '排序值',
    status varchar(32) default 'published' comment '状态:published/unpublished',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete tinyint default 0 not null comment '逻辑删除',
    index idx_ann_status (status),
    index idx_ann_top (is_top),
    index idx_ann_publish_time (publish_time)
) comment '食堂公告表' collate = utf8mb4_unicode_ci;

-- ============================================
-- 3. 聊天消息表
-- ============================================

-- 聊天消息表（用于存储离线消息）
drop table if exists chat_message;
create table chat_message(
    id bigint auto_increment comment '主键' primary key,
    sender_id bigint not null comment '发送者ID',
    receiver_id bigint not null comment '接收者ID',
    content text not null comment '消息内容',
    message_type varchar(32) default 'text' comment '消息类型：text-文本, image-图片, file-文件',
    image_url varchar(1024) null comment '图片URL',
    file_url varchar(1024) null comment '文件URL',
    is_read tinyint default 0 comment '是否已读：0-未读, 1-已读',
    read_time datetime null comment '阅读时间',
    is_delivered tinyint default 0 comment '是否已送达：0-未送达, 1-已送达',
    delivered_time datetime null comment '送达时间',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete tinyint default 0 not null comment '逻辑删除',
    index idx_sender_id (sender_id),
    index idx_receiver_id (receiver_id),
    index idx_create_time (create_time),
    index idx_is_read (is_read)
) comment '聊天消息表' collate = utf8mb4_unicode_ci;

-- ============================================
-- 4. 消息通知表
-- ============================================

-- 消息通知表
drop table if exists notification;
create table notification(
    id bigint auto_increment comment 'id' primary key,
    user_id bigint not null comment '用户id',
    type varchar(50) not null default 'system' comment '消息类型：complaint-投诉, system-系统, activity-活动',
    title varchar(256) not null comment '消息标题',
    content text not null comment '消息内容',
    related_type varchar(50) null comment '关联类型：complaint-投诉, merchant-商户',
    related_id bigint null comment '关联ID',
    status varchar(20) default 'unread' comment '状态：unread-未读, read-已读',
    read_time datetime null comment '阅读时间',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete tinyint default 0 not null comment '是否删除',
    index idx_user_id (user_id),
    index idx_status (status),
    index idx_type (type),
    index idx_create_time (create_time)
) comment '消息通知表' collate = utf8mb4_unicode_ci;

-- ============================================
-- 4. 系统配置表
-- ============================================

-- 系统设置表
drop table if exists system_config;
create table system_config(
    id bigint auto_increment comment 'id' primary key,
    config_key varchar(256) not null unique comment '配置键',
    config_value text not null comment '配置值',
    config_type varchar(50) null comment '配置类型',
    description varchar(512) null comment '描述',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_config_key (config_key)
) comment '系统设置表' collate = utf8mb4_unicode_ci;

-- ============================================
-- 5. 初始化数据
-- ============================================

# -- 插入系统配置
# insert into system_config (config_key, config_value, config_type, description) values
# ('app_name', 'FoodPal', 'system', '应用名称'),
# ('app_version', '1.0.0', 'system', '应用版本'),
# ('service_phone', '400-800-8888', 'contact', '客服电话'),
# ('service_email', 'service@foodpal.com', 'contact', '客服邮箱');
#
# -- 创建管理员账号（密码: admin123）
# insert ignore into user (user_account, user_password, user_name, user_role, status, create_time) values
# ('admin', '8d969eef6ecad3c29a3a873fba8f4f7f', '系统管理员', 'admin', 1, NOW());
#
# -- 创建示例商户账号（密码: merchant123）
# insert ignore into user (user_account, user_password, user_name, user_role, status, create_time) values
# ('merchant001', 'c8837b23ff8aaa8a2dde915473ce0991', '示例商户', 'merchant', 1, NOW());
#
# -- 创建示例学生账号（密码: student123）
# insert ignore into user (user_account, user_password, user_name, user_role, status, create_time) values
# ('student001', 'cd0c03b85e3f2f9d16fab86c343fb860', '张同学', 'student', 1, NOW()),
# ('student002', 'cd0c03b85e3f2f9d16fab86c343fb860', '李同学', 'student', 1, NOW());
#
# -- 创建示例监督管理员账号（密码: supervisor123）
# insert ignore into user (user_account, user_password, user_name, user_role, status, create_time) values
# ('supervisor001', '5f4dcc3b5aa765d61d8327deb882cf99', '监督管理员', 'supervisor', 1, NOW());
#
# -- 创建示例商户资料
# insert ignore into merchant_profile (user_id, merchant_name, contact_name, contact_phone, business_hours, location, description, status, audit_status, create_time) values
# ((select id from user where user_account = 'merchant001'), '第一食堂-川味窗口', '王老板', '13800138001', '07:00-21:00', '第一食堂2楼', '正宗川菜，麻辣鲜香', 1, 'approved', NOW());
#
# -- 创建示例菜品
# insert ignore into dish (merchant_id, dish_name, dish_price, ingredients, nutrition_info, category, supply_status, like_count, favorite_count, create_time) values
# ((select id from merchant_profile where merchant_name = '第一食堂-川味窗口'), '宫保鸡丁', 15.00, '鸡肉、花生、青椒、干辣椒', '蛋白质: 20g, 脂肪: 12g, 碳水化合物: 8g', '主食', 1, 128, 56, NOW()),
# ((select id from merchant_profile where merchant_name = '第一食堂-川味窗口'), '麻婆豆腐', 8.00, '豆腐、牛肉末、豆瓣酱、花椒', '蛋白质: 12g, 脂肪: 8g, 碳水化合物: 6g', '主食', 1, 98, 42, NOW()),
# ((select id from merchant_profile where merchant_name = '第一食堂-川味窗口'), '酸辣汤', 6.00, '豆腐、木耳、鸡蛋、醋、辣椒', '蛋白质: 6g, 脂肪: 3g, 碳水化合物: 8g', '汤类', 1, 76, 28, NOW()),
# ((select id from merchant_profile where merchant_name = '第一食堂-川味窗口'), '凉拌黄瓜', 5.00, '黄瓜、蒜、醋、香油', '蛋白质: 2g, 脂肪: 1g, 碳水化合物: 4g', '凉菜', 1, 65, 35, NOW());
#
# -- 创建示例公告
# insert ignore into canteen_announcement (title, content, announcement_type, is_top, sort_no, status, publish_time, create_time) values
# ('FoodPal高校食堂通正式上线！', '欢迎使用FoodPal高校食堂通系统，本系统提供菜品查询、投诉反馈、商户动态等功能。如有问题请联系客服。', 'system', 1, 0, 'published', NOW(), NOW()),
# ('关于食堂卫生检查的通报', '本周学校后勤部门对各食堂进行了卫生检查，整体情况良好。请各商户继续保持良好的卫生习惯。', 'canteen', 0, 1, 'published', NOW(), NOW());
#
# -- 商户违规记录表
# drop table if exists merchant_violation;
# create table merchant_violation(
#     id bigint auto_increment comment '主键' primary key,
#     merchant_id bigint not null comment '商户ID',
#     violation_type varchar(64) not null comment '违规类型:info_false/not_rectify/not_update/other',
#     violation_reason text not null comment '违规原因',
#     penalty_measures text null comment '处罚措施',
#     violation_level varchar(32) default 'normal' comment '违规等级:low/normal/high',
#     supervisor_id bigint not null comment '处理人ID',
#     process_status varchar(32) default 'pending' comment '处理状态:pending/processing/completed',
#     create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
#     update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
#     is_delete tinyint default 0 not null comment '逻辑删除',
#     index idx_violation_merchant (merchant_id),
#     index idx_violation_status (process_status),
#     index idx_violation_time (create_time)
# ) comment '商户违规记录表' collate = utf8mb4_unicode_ci;
#
# -- ============================================
# -- 初始化完成
# -- ============================================
# select 'FoodPal数据库初始化完成！' as message;
