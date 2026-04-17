USE foodpal;
SET NAMES utf8mb4;

START TRANSACTION;

-- Cleanup for repeatable seeding
DELETE FROM canteen_dynamic_comment;
DELETE FROM canteen_dynamic_like;
DELETE FROM canteen_dynamic_share;
DELETE FROM canteen_dynamic;
DELETE FROM dish_like;
DELETE FROM dish_favorite;
DELETE FROM dish;
DELETE FROM canteen_complaint;
DELETE FROM merchant_profile;
DELETE FROM chat_message;
DELETE FROM notification;
DELETE FROM canteen_announcement;
DELETE FROM system_config;
DELETE FROM user;

ALTER TABLE user AUTO_INCREMENT = 1;
ALTER TABLE merchant_profile AUTO_INCREMENT = 1;
ALTER TABLE dish AUTO_INCREMENT = 1;
ALTER TABLE dish_favorite AUTO_INCREMENT = 1;
ALTER TABLE dish_like AUTO_INCREMENT = 1;
ALTER TABLE canteen_complaint AUTO_INCREMENT = 1;
ALTER TABLE canteen_dynamic AUTO_INCREMENT = 1;
ALTER TABLE canteen_dynamic_like AUTO_INCREMENT = 1;
ALTER TABLE canteen_dynamic_comment AUTO_INCREMENT = 1;
ALTER TABLE canteen_dynamic_share AUTO_INCREMENT = 1;
ALTER TABLE canteen_announcement AUTO_INCREMENT = 1;
ALTER TABLE chat_message AUTO_INCREMENT = 1;
ALTER TABLE notification AUTO_INCREMENT = 1;
ALTER TABLE system_config AUTO_INCREMENT = 1;

-- 1) user (20 rows)
INSERT INTO user (
  id, user_account, user_password, user_name, user_avatar, user_profile, user_role,
  user_phone, user_email, status, union_id, mp_open_id,
  create_by, create_time, update_by, update_time, is_delete
) VALUES
(1, 'admin_main', '0192023a7bbd73250516f069df18b500', '平台管理员-林老师', '/api/file/preview/user_avatar/1/admin_main.jpg', '负责平台配置与用户治理', 'admin', '13800001001', 'admin.main@foodpal.edu.cn', 1, 'union_admin_main_001', 'mp_admin_main_001', 'seed', '2026-02-20 09:00:00', 'seed', '2026-03-01 08:30:00', 0),
(2, 'admin_ops', '0192023a7bbd73250516f069df18b500', '运营管理员-周老师', '/api/file/preview/user_avatar/2/admin_ops.jpg', '负责活动运营与内容发布', 'admin', '13800001002', 'admin.ops@foodpal.edu.cn', 1, 'union_admin_ops_002', 'mp_admin_ops_002', 'seed', '2026-02-20 09:10:00', 'seed', '2026-03-01 08:35:00', 0),
(3, 'supervisor_chen', '5f4dcc3b5aa765d61d8327deb882cf99', '监督员-陈老师', '/api/file/preview/user_avatar/3/supervisor_chen.jpg', '负责投诉核查与整改闭环', 'supervisor', '13800001003', 'supervisor.chen@foodpal.edu.cn', 1, 'union_supervisor_chen_003', 'mp_supervisor_chen_003', 'seed', '2026-02-20 09:20:00', 'seed', '2026-03-01 08:40:00', 0),
(4, 'supervisor_wu', '5f4dcc3b5aa765d61d8327deb882cf99', '监督员-吴老师', '/api/file/preview/user_avatar/4/supervisor_wu.jpg', '负责食品安全抽检', 'supervisor', '13800001004', 'supervisor.wu@foodpal.edu.cn', 1, 'union_supervisor_wu_004', 'mp_supervisor_wu_004', 'seed', '2026-02-20 09:25:00', 'seed', '2026-03-01 08:45:00', 0),
(5, 'merchant001', 'c8837b23ff8aaa8a2dde915473ce0991', '王师傅', '/api/file/preview/user_avatar/5/merchant001.jpg', '第一食堂川味窗口负责人', 'merchant', '13800002001', 'merchant001@foodpal.edu.cn', 1, 'union_merchant_001', 'mp_merchant_001', 'seed', '2026-02-20 09:30:00', 'seed', '2026-03-01 09:00:00', 0),
(6, 'merchant002', 'c8837b23ff8aaa8a2dde915473ce0991', '李师傅', '/api/file/preview/user_avatar/6/merchant002.jpg', '第一食堂面食窗口负责人', 'merchant', '13800002002', 'merchant002@foodpal.edu.cn', 1, 'union_merchant_002', 'mp_merchant_002', 'seed', '2026-02-20 09:35:00', 'seed', '2026-03-01 09:05:00', 0),
(7, 'merchant003', 'c8837b23ff8aaa8a2dde915473ce0991', '赵师傅', '/api/file/preview/user_avatar/7/merchant003.jpg', '第二食堂清真窗口负责人', 'merchant', '13800002003', 'merchant003@foodpal.edu.cn', 1, 'union_merchant_003', 'mp_merchant_003', 'seed', '2026-02-20 09:40:00', 'seed', '2026-03-01 09:10:00', 0),
(8, 'merchant004', 'c8837b23ff8aaa8a2dde915473ce0991', '孙师傅', '/api/file/preview/user_avatar/8/merchant004.jpg', '第二食堂轻食窗口负责人', 'merchant', '13800002004', 'merchant004@foodpal.edu.cn', 1, 'union_merchant_004', 'mp_merchant_004', 'seed', '2026-02-20 09:45:00', 'seed', '2026-03-01 09:15:00', 0),
(9, 'merchant005', 'c8837b23ff8aaa8a2dde915473ce0991', '钱师傅', '/api/file/preview/user_avatar/9/merchant005.jpg', '第三食堂湘菜窗口负责人', 'merchant', '13800002005', 'merchant005@foodpal.edu.cn', 1, 'union_merchant_005', 'mp_merchant_005', 'seed', '2026-02-20 09:50:00', 'seed', '2026-03-01 09:20:00', 0),
(10, 'merchant006', 'c8837b23ff8aaa8a2dde915473ce0991', '郑师傅', '/api/file/preview/user_avatar/10/merchant006.jpg', '第三食堂粤菜窗口负责人', 'merchant', '13800002006', 'merchant006@foodpal.edu.cn', 1, 'union_merchant_006', 'mp_merchant_006', 'seed', '2026-02-20 09:55:00', 'seed', '2026-03-01 09:25:00', 0),
(11, 'merchant007', 'c8837b23ff8aaa8a2dde915473ce0991', '冯师傅', '/api/file/preview/user_avatar/11/merchant007.jpg', '夜宵档口负责人', 'merchant', '13800002007', 'merchant007@foodpal.edu.cn', 1, 'union_merchant_007', 'mp_merchant_007', 'seed', '2026-02-20 10:00:00', 'seed', '2026-03-01 09:30:00', 0),
(12, 'merchant008', 'c8837b23ff8aaa8a2dde915473ce0991', '褚师傅', '/api/file/preview/user_avatar/12/merchant008.jpg', '民族风味窗口负责人', 'merchant', '13800002008', 'merchant008@foodpal.edu.cn', 1, 'union_merchant_008', 'mp_merchant_008', 'seed', '2026-02-20 10:05:00', 'seed', '2026-03-01 09:35:00', 0),
(13, 'merchant009', 'c8837b23ff8aaa8a2dde915473ce0991', '卫师傅', '/api/file/preview/user_avatar/13/merchant009.jpg', '教工餐厅家常窗口负责人', 'merchant', '13800002009', 'merchant009@foodpal.edu.cn', 1, 'union_merchant_009', 'mp_merchant_009', 'seed', '2026-02-20 10:10:00', 'seed', '2026-03-01 09:40:00', 0),
(14, 'merchant010', 'c8837b23ff8aaa8a2dde915473ce0991', '蒋师傅', '/api/file/preview/user_avatar/14/merchant010.jpg', '西式简餐窗口负责人', 'merchant', '13800002010', 'merchant010@foodpal.edu.cn', 0, 'union_merchant_010', 'mp_merchant_010', 'seed', '2026-02-20 10:15:00', 'seed', '2026-03-01 09:45:00', 0),
(15, 'student001', 'cd0c03b85e3f2f9d16fab86c343fb860', '张晨', '/api/file/preview/user_avatar/15/student001.jpg', '计算机学院2023级', 'student', '13800003001', 'student001@stu.foodpal.edu.cn', 1, 'union_student_001', 'mp_student_001', 'seed', '2026-02-20 10:20:00', 'seed', '2026-03-01 09:50:00', 0),
(16, 'student002', 'cd0c03b85e3f2f9d16fab86c343fb860', '李悦', '/api/file/preview/user_avatar/16/student002.jpg', '法学院2022级', 'student', '13800003002', 'student002@stu.foodpal.edu.cn', 1, 'union_student_002', 'mp_student_002', 'seed', '2026-02-20 10:25:00', 'seed', '2026-03-01 09:55:00', 0),
(17, 'student003', 'cd0c03b85e3f2f9d16fab86c343fb860', '王浩', '/api/file/preview/user_avatar/17/student003.jpg', '机械学院2021级', 'student', '13800003003', 'student003@stu.foodpal.edu.cn', 1, 'union_student_003', 'mp_student_003', 'seed', '2026-02-20 10:30:00', 'seed', '2026-03-01 10:00:00', 0),
(18, 'student004', 'cd0c03b85e3f2f9d16fab86c343fb860', '刘婷', '/api/file/preview/user_avatar/18/student004.jpg', '经管学院2024级', 'student', '13800003004', 'student004@stu.foodpal.edu.cn', 1, 'union_student_004', 'mp_student_004', 'seed', '2026-02-20 10:35:00', 'seed', '2026-03-01 10:05:00', 0),
(19, 'student005', 'cd0c03b85e3f2f9d16fab86c343fb860', '周宁', '/api/file/preview/user_avatar/19/student005.jpg', '外国语学院2023级', 'student', '13800003005', 'student005@stu.foodpal.edu.cn', 1, 'union_student_005', 'mp_student_005', 'seed', '2026-02-20 10:40:00', 'seed', '2026-03-01 10:10:00', 0),
(20, 'student006', 'cd0c03b85e3f2f9d16fab86c343fb860', '陈曦', '/api/file/preview/user_avatar/20/student006.jpg', '医学院2022级', 'student', '13800003006', 'student006@stu.foodpal.edu.cn', 0, 'union_student_006', 'mp_student_006', 'seed', '2026-02-20 10:45:00', 'seed', '2026-03-01 10:15:00', 0);

-- 2) merchant_profile (10 rows)
INSERT INTO merchant_profile (
  id, user_id, merchant_name, contact_name, contact_phone, avatar, business_hours,
  location, description, status, audit_status, pending_data,
  create_time, update_time, is_delete
) VALUES
(1, 5, '第一食堂-川味窗口', '王师傅', '13910010001', '/api/file/preview/merchant_avatar/1/chuanwei.jpg', '07:00-21:00', '第一食堂2楼A区', '主打川菜与快餐套餐，翻台快', 1, 'approved', '{"changeRequest":"none"}', '2026-02-21 08:00:00', '2026-03-01 11:00:00', 0),
(2, 6, '第一食堂-面食窗口', '李师傅', '13910010002', '/api/file/preview/merchant_avatar/2/noodle.jpg', '06:30-20:30', '第一食堂1楼B区', '手工面、盖浇面、拌面', 1, 'approved', '{"changeRequest":"none"}', '2026-02-21 08:10:00', '2026-03-01 11:05:00', 0),
(3, 7, '第二食堂-清真窗口', '赵师傅', '13910010003', '/api/file/preview/merchant_avatar/3/qingzhen.jpg', '07:00-20:00', '第二食堂1楼', '清真牛羊肉与面点', 1, 'approved', '{"changeRequest":"none"}', '2026-02-21 08:20:00', '2026-03-01 11:10:00', 0),
(4, 8, '第二食堂-轻食窗口', '孙师傅', '13910010004', '/api/file/preview/merchant_avatar/4/light.jpg', '09:00-19:30', '第二食堂2楼', '低脂沙拉与谷物碗', 1, 'approved', '{"changeRequest":"none"}', '2026-02-21 08:30:00', '2026-03-01 11:15:00', 0),
(5, 9, '第三食堂-湘菜窗口', '钱师傅', '13910010005', '/api/file/preview/merchant_avatar/5/xiang.jpg', '07:00-21:00', '第三食堂1楼', '香辣湘菜与蒸菜', 1, 'pending', '{"merchant_name":"第三食堂-湘味升级窗口","business_hours":"06:30-21:30"}', '2026-02-21 08:40:00', '2026-03-01 11:20:00', 0),
(6, 10, '第三食堂-粤菜窗口', '郑师傅', '13910010006', '/api/file/preview/merchant_avatar/6/yue.jpg', '07:00-20:30', '第三食堂2楼', '清淡粤菜与炖汤', 1, 'approved', '{"changeRequest":"none"}', '2026-02-21 08:50:00', '2026-03-01 11:25:00', 0),
(7, 11, '夜宵档口-烧烤', '冯师傅', '13910010007', '/api/file/preview/merchant_avatar/7/night.jpg', '17:00-23:30', '南门美食街1号', '夜间烧烤与小炒', 1, 'rejected', '{"reason":"排烟系统证明材料不完整"}', '2026-02-21 09:00:00', '2026-03-01 11:30:00', 0),
(8, 12, '民族风味-西北面点', '褚师傅', '13910010008', '/api/file/preview/merchant_avatar/8/xibei.jpg', '07:30-20:30', '东区食堂1楼', '手抓饭、拉条子、烤馕', 0, 'pending', '{"description":"申请补充清真认证文件"}', '2026-02-21 09:10:00', '2026-03-01 11:35:00', 0),
(9, 13, '教工餐厅-家常菜', '卫师傅', '13910010009', '/api/file/preview/merchant_avatar/9/home.jpg', '10:30-14:00,16:30-20:00', '教工餐厅3楼', '家常热炒与商务套餐', 1, 'approved', '{"changeRequest":"none"}', '2026-02-21 09:20:00', '2026-03-01 11:40:00', 0),
(10, 14, '西式简餐-轻烘焙', '蒋师傅', '13910010010', '/api/file/preview/merchant_avatar/10/west.jpg', '08:00-20:00', '图书馆北侧商业街', '意面、三明治、咖啡', 0, 'rejected', '{"reason":"食品经营许可即将到期，需更新"}', '2026-02-21 09:30:00', '2026-03-01 11:45:00', 0);

-- 3) dish (20 rows)
INSERT INTO dish (
  id, merchant_id, dish_name, dish_price, ingredients, nutrition_info, dish_image,
  category, supply_status, special_price, special_start_time, special_end_time,
  purchase_limit, like_count, favorite_count, create_time, update_time, is_delete
) VALUES
(1, 1, '宫保鸡丁', 15.00, '鸡腿肉、花生、青椒、干辣椒', '蛋白质:20g,脂肪:12g,碳水:9g,热量:280kcal', '/api/file/preview/dish/1/dish_001_gongbao.jpg', '主食', 1, 12.90, '2026-03-01 11:00:00', '2026-03-01 13:30:00', 2, 0, 0, '2026-02-22 11:00:00', '2026-03-01 11:00:00', 0),
(2, 1, '麻婆豆腐', 8.00, '嫩豆腐、牛肉末、豆瓣酱、花椒', '蛋白质:12g,脂肪:7g,碳水:6g,热量:170kcal', '/api/file/preview/dish/1/dish_002_mapo.jpg', '主食', 1, 7.20, '2026-03-01 17:00:00', '2026-03-01 19:00:00', 3, 0, 0, '2026-02-22 11:05:00', '2026-03-01 11:05:00', 0),
(3, 2, '红烧牛肉面', 14.00, '手擀面、牛腩、白萝卜、香菜', '蛋白质:24g,脂肪:10g,碳水:45g,热量:420kcal', '/api/file/preview/dish/2/dish_003_niurou_mian.jpg', '面食', 1, 12.00, '2026-03-01 10:30:00', '2026-03-01 12:30:00', 2, 0, 0, '2026-02-22 11:10:00', '2026-03-01 11:10:00', 0),
(4, 2, '番茄鸡蛋面', 10.00, '挂面、鸡蛋、番茄、葱花', '蛋白质:14g,脂肪:6g,碳水:36g,热量:300kcal', '/api/file/preview/dish/2/dish_004_tomato_egg_noodle.jpg', '面食', 1, 9.00, '2026-03-01 11:30:00', '2026-03-01 13:00:00', 3, 0, 0, '2026-02-22 11:15:00', '2026-03-01 11:15:00', 0),
(5, 3, '孜然羊肉饭', 18.00, '羊腿肉、洋葱、孜然、米饭', '蛋白质:26g,脂肪:14g,碳水:50g,热量:520kcal', '/api/file/preview/dish/3/dish_005_ziran_yangrou.jpg', '主食', 1, 15.00, '2026-03-01 12:00:00', '2026-03-01 14:00:00', 2, 0, 0, '2026-02-22 11:20:00', '2026-03-01 11:20:00', 0),
(6, 3, '牛肉拉条子', 16.00, '面条、牛肉、彩椒、洋葱', '蛋白质:22g,脂肪:11g,碳水:48g,热量:470kcal', '/api/file/preview/dish/3/dish_006_latiaozi.jpg', '面食', 1, 14.00, '2026-03-01 17:30:00', '2026-03-01 20:00:00', 2, 0, 0, '2026-02-22 11:25:00', '2026-03-01 11:25:00', 0),
(7, 4, '烟熏鸡胸沙拉', 13.00, '鸡胸肉、生菜、玉米、紫甘蓝', '蛋白质:23g,脂肪:5g,碳水:12g,热量:220kcal', '/api/file/preview/dish/4/dish_007_smoked_chicken_salad.jpg', '轻食', 1, 11.00, '2026-03-01 10:00:00', '2026-03-01 12:00:00', 4, 0, 0, '2026-02-22 11:30:00', '2026-03-01 11:30:00', 0),
(8, 4, '牛油果金枪鱼碗', 17.00, '金枪鱼、牛油果、糙米、海苔', '蛋白质:20g,脂肪:13g,碳水:28g,热量:360kcal', '/api/file/preview/dish/4/dish_008_tuna_bowl.jpg', '轻食', 0, 16.00, '2026-03-01 11:00:00', '2026-03-01 14:00:00', 3, 0, 0, '2026-02-22 11:35:00', '2026-03-01 11:35:00', 0),
(9, 5, '剁椒鱼头套餐', 22.00, '花鲢鱼头、剁椒、姜蒜、米饭', '蛋白质:30g,脂肪:15g,碳水:46g,热量:560kcal', '/api/file/preview/dish/5/dish_009_duojiao_yutou.jpg', '主食', 1, 19.90, '2026-03-01 12:00:00', '2026-03-01 13:30:00', 1, 0, 0, '2026-02-22 11:40:00', '2026-03-01 11:40:00', 0),
(10, 5, '小炒黄牛肉', 20.00, '黄牛肉、杭椒、蒜苗', '蛋白质:28g,脂肪:13g,碳水:8g,热量:340kcal', '/api/file/preview/dish/5/dish_010_xiaochao_niurou.jpg', '主食', 1, 18.00, '2026-03-01 17:00:00', '2026-03-01 19:30:00', 2, 0, 0, '2026-02-22 11:45:00', '2026-03-01 11:45:00', 0),
(11, 6, '白切鸡饭', 16.00, '走地鸡、姜葱汁、油菜、米饭', '蛋白质:25g,脂肪:8g,碳水:42g,热量:430kcal', '/api/file/preview/dish/6/dish_011_baiqieji.jpg', '主食', 1, 14.50, '2026-03-01 11:00:00', '2026-03-01 13:00:00', 2, 0, 0, '2026-02-22 11:50:00', '2026-03-01 11:50:00', 0),
(12, 6, '瑶柱冬瓜汤', 9.00, '冬瓜、瑶柱、猪骨清汤', '蛋白质:10g,脂肪:4g,碳水:7g,热量:120kcal', '/api/file/preview/dish/6/dish_012_yaozhu_donggua.jpg', '汤类', 1, 8.00, '2026-03-01 12:30:00', '2026-03-01 14:30:00', 4, 0, 0, '2026-02-22 11:55:00', '2026-03-01 11:55:00', 0),
(13, 7, '孜然烤鸡翅', 12.00, '鸡中翅、孜然粉、辣椒粉', '蛋白质:18g,脂肪:9g,碳水:5g,热量:230kcal', '/api/file/preview/dish/7/dish_013_kaoji_chi.jpg', '小吃', 1, 10.00, '2026-03-01 20:00:00', '2026-03-01 23:00:00', 5, 0, 0, '2026-02-22 12:00:00', '2026-03-01 12:00:00', 0),
(14, 7, '蒜香烤茄子', 10.00, '长茄子、蒜蓉、香葱、酱油', '蛋白质:4g,脂肪:5g,碳水:18g,热量:150kcal', '/api/file/preview/dish/7/dish_014_kaoqiezi.jpg', '小吃', 0, 9.00, '2026-03-01 19:00:00', '2026-03-01 22:00:00', 5, 0, 0, '2026-02-22 12:05:00', '2026-03-01 12:05:00', 0),
(15, 8, '羊肉抓饭', 19.00, '羊肉、胡萝卜、洋葱、长粒米', '蛋白质:27g,脂肪:12g,碳水:52g,热量:540kcal', '/api/file/preview/dish/8/dish_015_zhuafan.jpg', '主食', 1, 17.00, '2026-03-01 12:00:00', '2026-03-01 14:00:00', 2, 0, 0, '2026-02-22 12:10:00', '2026-03-01 12:10:00', 0),
(16, 8, '手工烤馕', 6.00, '面粉、芝麻、酵母、盐', '蛋白质:7g,脂肪:3g,碳水:42g,热量:230kcal', '/api/file/preview/dish/8/dish_016_kaonang.jpg', '小吃', 1, 5.50, '2026-03-01 10:00:00', '2026-03-01 12:00:00', 6, 0, 0, '2026-02-22 12:15:00', '2026-03-01 12:15:00', 0),
(17, 9, '农家小炒肉', 17.00, '五花肉、青椒、豆豉', '蛋白质:21g,脂肪:16g,碳水:9g,热量:360kcal', '/api/file/preview/dish/9/dish_017_xiaochao_rou.jpg', '主食', 1, 15.00, '2026-03-01 11:30:00', '2026-03-01 13:30:00', 2, 0, 0, '2026-02-22 12:20:00', '2026-03-01 12:20:00', 0),
(18, 9, '莲藕排骨汤', 11.00, '排骨、莲藕、枸杞、姜片', '蛋白质:15g,脂肪:7g,碳水:9g,热量:190kcal', '/api/file/preview/dish/9/dish_018_ou_pai_gu_tang.jpg', '汤类', 1, 10.00, '2026-03-01 11:30:00', '2026-03-01 14:30:00', 3, 0, 0, '2026-02-22 12:25:00', '2026-03-01 12:25:00', 0),
(19, 10, '奶油蘑菇意面', 21.00, '意大利面、蘑菇、奶油、培根', '蛋白质:19g,脂肪:17g,碳水:58g,热量:590kcal', '/api/file/preview/dish/10/dish_019_mushroom_pasta.jpg', '西式', 1, 18.90, '2026-03-01 12:00:00', '2026-03-01 15:00:00', 2, 0, 0, '2026-02-22 12:30:00', '2026-03-01 12:30:00', 0),
(20, 10, '鸡肉凯撒卷', 14.00, '全麦卷饼、鸡胸肉、生菜、凯撒酱', '蛋白质:16g,脂肪:8g,碳水:31g,热量:320kcal', '/api/file/preview/dish/10/dish_020_caesar_wrap.jpg', '西式', 1, 12.80, '2026-03-01 10:30:00', '2026-03-01 13:30:00', 3, 0, 0, '2026-02-22 12:35:00', '2026-03-01 12:35:00', 0);

-- 4) dish_favorite (12 rows)
INSERT INTO dish_favorite (id, user_id, dish_id, create_time, is_delete) VALUES
(1, 15, 1, '2026-03-01 12:10:00', 0),
(2, 15, 2, '2026-03-01 12:12:00', 0),
(3, 16, 3, '2026-03-01 12:15:00', 0),
(4, 16, 5, '2026-03-01 12:18:00', 0),
(5, 17, 6, '2026-03-01 12:20:00', 0),
(6, 17, 8, '2026-03-01 12:25:00', 0),
(7, 18, 9, '2026-03-01 12:28:00', 0),
(8, 18, 11, '2026-03-01 12:30:00', 0),
(9, 19, 12, '2026-03-01 12:32:00', 0),
(10, 19, 14, '2026-03-01 12:36:00', 0),
(11, 20, 15, '2026-03-01 12:40:00', 0),
(12, 20, 18, '2026-03-01 12:42:00', 0);

-- 5) dish_like (14 rows)
INSERT INTO dish_like (id, user_id, dish_id, create_time, is_delete) VALUES
(1, 15, 1, '2026-03-01 12:45:00', 0),
(2, 15, 3, '2026-03-01 12:46:00', 0),
(3, 15, 4, '2026-03-01 12:47:00', 0),
(4, 16, 2, '2026-03-01 12:48:00', 0),
(5, 16, 5, '2026-03-01 12:49:00', 0),
(6, 16, 6, '2026-03-01 12:50:00', 0),
(7, 17, 7, '2026-03-01 12:51:00', 0),
(8, 17, 8, '2026-03-01 12:52:00', 0),
(9, 18, 9, '2026-03-01 12:53:00', 0),
(10, 18, 10, '2026-03-01 12:54:00', 0),
(11, 19, 11, '2026-03-01 12:55:00', 0),
(12, 19, 12, '2026-03-01 12:56:00', 0),
(13, 20, 13, '2026-03-01 12:57:00', 0),
(14, 20, 20, '2026-03-01 12:58:00', 0);

-- 6) canteen_complaint (12 rows)
INSERT INTO canteen_complaint (
  id, complaint_no, user_id, merchant_id, complaint_title, complaint_content, evidence_urls,
  status, process_progress, rectify_requirement, rectify_result, feedback,
  result_rating, process_by, process_time, create_time, update_time, is_delete
) VALUES
(1, 'CP202603010001', 15, 1, '米饭偏硬影响就餐体验', '3月1日中午在川味窗口购买套餐，米饭明显偏硬，口感较差。', '["/api/file/preview/complaint/15/cp0001_1.jpg","/api/file/preview/complaint/15/cp0001_2.jpg"]', 'pending_review', '已受理，等待监督员核查现场出品流程', '核查蒸饭时间和保温流程，提交当日出餐记录', '商户已提交初步说明，待复核', '感谢反馈，后续会跟进回访', 2, 3, '2026-03-01 14:10:00', '2026-03-01 13:10:00', '2026-03-01 14:10:00', 0),
(2, 'CP202603010002', 16, 2, '面汤偏咸', '番茄鸡蛋面汤底偏咸，建议后厨统一盐度标准。', '["/api/file/preview/complaint/16/cp0002_1.jpg"]', 'pending_rectify', '监督员已下发整改通知，商户整改中', '72小时内完成口味标准化并提交记录', '已完成首轮培训，待抽查', '期待后续改善', 2, 4, '2026-03-01 15:20:00', '2026-03-01 13:20:00', '2026-03-01 15:20:00', 0),
(3, 'CP202603010003', 17, 3, '菜品标价与结算价不一致', '窗口标价16元，结算时显示18元。', '["/api/file/preview/complaint/17/cp0003_1.jpg","/api/file/preview/complaint/17/cp0003_2.jpg"]', 'rectified', '价格牌已更换，系统价格已同步', '建立每日开档前价签核对流程', '已整改，连续3天抽查通过', '已恢复信任', 3, 3, '2026-03-01 16:00:00', '2026-03-01 13:30:00', '2026-03-01 16:00:00', 0),
(4, 'CP202603010004', 18, 4, '沙拉出品等待时间过长', '晚高峰排队后等待近25分钟，影响上课。', '["/api/file/preview/complaint/18/cp0004_1.jpg"]', 'completed', '已优化备料流程并增加取餐叫号提示', '高峰期增设一名打包员', '高峰平均等待时长降至8分钟', '体验明显改善', 3, 4, '2026-03-01 17:10:00', '2026-03-01 13:40:00', '2026-03-01 17:10:00', 0),
(5, 'CP202603010005', 19, 5, '菜品过辣且无提示', '剁椒鱼头套餐辣度较高，未在菜单提示。', '["/api/file/preview/complaint/19/cp0005_1.jpg"]', 'rejected', '核查后确认菜单页已有辣度标识', '继续优化辣度标识位置与字体', '保留现有标准并新增“微辣可选”说明', '接受处理结果', 2, 3, '2026-03-01 17:30:00', '2026-03-01 13:50:00', '2026-03-01 17:30:00', 0),
(6, 'CP202603010006', 20, 6, '汤品温度偏低', '晚餐时段购买冬瓜汤温度不够热。', '["/api/file/preview/complaint/20/cp0006_1.jpg"]', 'pending_review', '已接单，等待后厨温控记录调取', '检查保温桶设定温度并复测', '商户提交保温设备巡检照片', '希望尽快解决', 2, 4, '2026-03-01 18:00:00', '2026-03-01 14:00:00', '2026-03-01 18:00:00', 0),
(7, 'CP202603010007', 15, 7, '夜宵油烟味重', '烧烤档口周边油烟较大，影响排队体验。', '["/api/file/preview/complaint/15/cp0007_1.jpg","/api/file/preview/complaint/15/cp0007_2.jpg"]', 'pending_rectify', '已通知商户检修排烟系统', '48小时内提供排烟净化维护报告', '已预约第三方清洗维护', '等待整改完成', 1, 3, '2026-03-01 18:20:00', '2026-03-01 14:10:00', '2026-03-01 18:20:00', 0),
(8, 'CP202603010008', 16, 8, '抓饭分量偏少', '同价位下分量偏少，建议明确克重。', '["/api/file/preview/complaint/16/cp0008_1.jpg"]', 'rectified', '已新增标准克重勺并公示分量', '公示主菜和米饭标准克重', '抽查符合公示标准', '满意', 3, 4, '2026-03-01 18:40:00', '2026-03-01 14:20:00', '2026-03-01 18:40:00', 0),
(9, 'CP202603010009', 17, 9, '套餐配送漏配小菜', '点餐备注小菜，但实际漏配。', '["/api/file/preview/complaint/17/cp0009_1.jpg"]', 'completed', '已补偿并完善打包复核清单', '打包前执行双人复核', '漏配率降至0.2%', '处理及时', 3, 3, '2026-03-01 19:00:00', '2026-03-01 14:30:00', '2026-03-01 19:00:00', 0),
(10, 'CP202603010010', 18, 10, '意面偏生', '奶油蘑菇意面口感偏硬，疑似烹煮时间不足。', '["/api/file/preview/complaint/18/cp0010_1.jpg"]', 'rejected', '复核后判定熟度符合门店标准', '建议商户在点单页增加口感偏好选项', '已增加“软硬度偏好”备注项', '已知悉', 2, 4, '2026-03-01 19:20:00', '2026-03-01 14:40:00', '2026-03-01 19:20:00', 0),
(11, 'CP202603010011', 19, 1, '餐具补给不及时', '高峰期餐具区勺子短缺。', '["/api/file/preview/complaint/19/cp0011_1.jpg"]', 'completed', '已安排专人每15分钟补给巡检', '建立餐具补给看板机制', '连续一周无短缺反馈', '满意', 3, 3, '2026-03-01 19:40:00', '2026-03-01 14:50:00', '2026-03-01 19:40:00', 0),
(12, 'CP202603010012', 20, 2, '外带包装渗漏', '汤面外带时包装渗漏，影响携带。', '["/api/file/preview/complaint/20/cp0012_1.jpg"]', 'rectified', '已更换加厚封口碗并培训打包流程', '使用双层封口并抽检', '抽检通过，渗漏率显著下降', '基本满意', 3, 4, '2026-03-01 20:00:00', '2026-03-01 15:00:00', '2026-03-01 20:00:00', 0);

-- 7) canteen_dynamic (15 rows)
INSERT INTO canteen_dynamic (
  id, publisher_id, publisher_type, merchant_id, title, content, cover_image, media_urls,
  news_type, audit_status, audit_reason, status, expire_time,
  like_count, comment_count, share_count, publish_time, create_time, update_time, is_delete
) VALUES
(1, 5, 'merchant', 1, '川味窗口本周特价上线', '宫保鸡丁与麻婆豆腐午餐时段限时优惠，欢迎到店。', '/api/file/preview/dynamic/1/dyn_001_cover.jpg', '["/api/file/preview/dynamic/1/dyn_001_1.jpg","/api/file/preview/dynamic/1/dyn_001_2.jpg"]', 'activity', 'approved', '审核通过，内容合规', 'published', '2026-03-07 23:59:59', 0, 0, 0, '2026-03-01 10:00:00', '2026-03-01 09:40:00', '2026-03-01 10:00:00', 0),
(2, 6, 'merchant', 2, '手工面新品试吃', '红烧牛肉面升级汤底，今天11:00后可免费加面一次。', '/api/file/preview/dynamic/2/dyn_002_cover.jpg', '["/api/file/preview/dynamic/2/dyn_002_1.jpg"]', 'new_dish', 'approved', '审核通过，信息准确', 'published', '2026-03-05 23:59:59', 0, 0, 0, '2026-03-01 10:30:00', '2026-03-01 09:50:00', '2026-03-01 10:30:00', 0),
(3, 7, 'merchant', 3, '清真窗口延长营业通知', '本周考试周，晚餐营业延长至20:30。', '/api/file/preview/dynamic/3/dyn_003_cover.jpg', '["/api/file/preview/dynamic/3/dyn_003_1.jpg"]', 'notice', 'approved', '审核通过', 'published', '2026-03-10 23:59:59', 0, 0, 0, '2026-03-01 11:00:00', '2026-03-01 10:10:00', '2026-03-01 11:00:00', 0),
(4, 8, 'merchant', 4, '轻食窗口买一赠一活动', '每日16:00-17:00指定沙拉第二份半价。', '/api/file/preview/dynamic/4/dyn_004_cover.jpg', '["/api/file/preview/dynamic/4/dyn_004_1.jpg","/api/file/preview/dynamic/4/dyn_004_2.jpg"]', 'activity', 'approved', '审核通过，活动时间明确', 'published', '2026-03-06 23:59:59', 0, 0, 0, '2026-03-01 11:20:00', '2026-03-01 10:20:00', '2026-03-01 11:20:00', 0),
(5, 9, 'merchant', 5, '湘菜窗口辣度分级上线', '新增微辣/中辣/重辣可选，点单更灵活。', '/api/file/preview/dynamic/5/dyn_005_cover.jpg', '["/api/file/preview/dynamic/5/dyn_005_1.jpg"]', 'notice', 'approved', '审核通过', 'published', '2026-03-20 23:59:59', 0, 0, 0, '2026-03-01 11:40:00', '2026-03-01 10:35:00', '2026-03-01 11:40:00', 0),
(6, 10, 'merchant', 6, '粤菜窗口炖汤日', '本周三至周五推出双人炖汤套餐。', '/api/file/preview/dynamic/6/dyn_006_cover.jpg', '["/api/file/preview/dynamic/6/dyn_006_1.jpg"]', 'activity', 'approved', '审核通过', 'published', '2026-03-08 23:59:59', 0, 0, 0, '2026-03-01 12:00:00', '2026-03-01 10:45:00', '2026-03-01 12:00:00', 0),
(7, 11, 'merchant', 7, '夜宵档口新品烤串', '新增低脂鸡胸串和香菇串，晚间供应。', '/api/file/preview/dynamic/7/dyn_007_cover.jpg', '["/api/file/preview/dynamic/7/dyn_007_1.jpg"]', 'new_dish', 'approved', '审核通过', 'published', '2026-03-09 23:59:59', 0, 0, 0, '2026-03-01 12:20:00', '2026-03-01 10:55:00', '2026-03-01 12:20:00', 0),
(8, 12, 'merchant', 8, '民族风味窗口恢复营业通知', '已完成设备维护，明日起恢复全品类供应。', '/api/file/preview/dynamic/8/dyn_008_cover.jpg', '["/api/file/preview/dynamic/8/dyn_008_1.jpg"]', 'notice', 'approved', '审核通过', 'published', '2026-03-12 23:59:59', 0, 0, 0, '2026-03-01 12:40:00', '2026-03-01 11:05:00', '2026-03-01 12:40:00', 0),
(9, 13, 'merchant', 9, '教工餐厅午市套餐升级', '新增三款家常套餐，支持堂食与外带。', '/api/file/preview/dynamic/9/dyn_009_cover.jpg', '["/api/file/preview/dynamic/9/dyn_009_1.jpg"]', 'new_dish', 'approved', '审核通过', 'published', '2026-03-15 23:59:59', 0, 0, 0, '2026-03-01 13:00:00', '2026-03-01 11:20:00', '2026-03-01 13:00:00', 0),
(10, 14, 'merchant', 10, '西式简餐菜单更新草稿', '草拟新增法棍三明治与无糖拿铁，待审核后发布。', '/api/file/preview/dynamic/10/dyn_010_cover.jpg', '["/api/file/preview/dynamic/10/dyn_010_1.jpg"]', 'new_dish', 'pending', '待审核：需补充过敏原提示', 'draft', '2026-03-20 23:59:59', 0, 0, 0, '2026-03-03 10:00:00', '2026-03-01 11:35:00', '2026-03-01 11:35:00', 0),
(11, 9, 'merchant', 5, '湘菜窗口促销文案（下线）', '历史促销文案已过期，保留存档。', '/api/file/preview/dynamic/5/dyn_011_cover.jpg', '["/api/file/preview/dynamic/5/dyn_011_1.jpg"]', 'activity', 'rejected', '驳回：活动时间与规则不完整', 'unpublished', '2026-02-28 23:59:59', 0, 0, 0, '2026-02-27 17:00:00', '2026-02-27 16:30:00', '2026-03-01 09:00:00', 0),
(12, 1, 'admin', 1, '关于食堂高峰错峰就餐倡议', '请同学们尽量错峰就餐，提升取餐效率。', '/api/file/preview/dynamic/admin/dyn_012_cover.jpg', '["/api/file/preview/dynamic/admin/dyn_012_1.jpg"]', 'notice', 'approved', '审核通过', 'published', '2026-03-31 23:59:59', 0, 0, 0, '2026-03-01 09:00:00', '2026-03-01 08:30:00', '2026-03-01 09:00:00', 0),
(13, 2, 'admin', 1, '三月食品安全主题周', '本月将开展食品安全开放日活动，欢迎报名。', '/api/file/preview/dynamic/admin/dyn_013_cover.jpg', '["/api/file/preview/dynamic/admin/dyn_013_1.jpg","/api/file/preview/dynamic/admin/dyn_013_2.jpg"]', 'activity', 'approved', '审核通过', 'published', '2026-03-20 23:59:59', 0, 0, 0, '2026-03-01 09:30:00', '2026-03-01 08:45:00', '2026-03-01 09:30:00', 0),
(14, 2, 'admin', 1, '四月窗口评优预告', '评优规则草案公示中，收集建议后正式发布。', '/api/file/preview/dynamic/admin/dyn_014_cover.jpg', '["/api/file/preview/dynamic/admin/dyn_014_1.jpg"]', 'notice', 'pending', '待审核：需补充评优指标', 'draft', '2026-04-10 23:59:59', 0, 0, 0, '2026-03-15 10:00:00', '2026-03-01 09:10:00', '2026-03-01 09:10:00', 0),
(15, 5, 'merchant', 1, '川味窗口上月活动归档', '二月限时活动已结束，感谢同学支持。', '/api/file/preview/dynamic/1/dyn_015_cover.jpg', '["/api/file/preview/dynamic/1/dyn_015_1.jpg"]', 'activity', 'approved', '审核通过', 'unpublished', '2026-02-28 23:59:59', 0, 0, 0, '2026-02-28 21:00:00', '2026-02-28 20:00:00', '2026-03-01 08:50:00', 0);

-- 8) canteen_dynamic_like (12 rows)
INSERT INTO canteen_dynamic_like (id, dynamic_id, user_id, create_time, is_delete) VALUES
(1, 1, 15, '2026-03-01 13:05:00', 0),
(2, 1, 16, '2026-03-01 13:06:00', 0),
(3, 2, 15, '2026-03-01 13:07:00', 0),
(4, 2, 17, '2026-03-01 13:08:00', 0),
(5, 3, 18, '2026-03-01 13:09:00', 0),
(6, 4, 19, '2026-03-01 13:10:00', 0),
(7, 5, 20, '2026-03-01 13:11:00', 0),
(8, 6, 15, '2026-03-01 13:12:00', 0),
(9, 7, 16, '2026-03-01 13:13:00', 0),
(10, 8, 17, '2026-03-01 13:14:00', 0),
(11, 12, 18, '2026-03-01 13:15:00', 0),
(12, 13, 19, '2026-03-01 13:16:00', 0);

-- 9) canteen_dynamic_comment (14 rows)
INSERT INTO canteen_dynamic_comment (
  id, dynamic_id, user_id, content, parent_id, create_time, update_time, is_delete
) VALUES
(1, 1, 15, '中午去吃了，宫保鸡丁确实不错。', 0, '2026-03-01 13:20:00', '2026-03-01 13:20:00', 0),
(2, 1, 16, '我也觉得这次口味更稳定。', 1, '2026-03-01 13:22:00', '2026-03-01 13:22:00', 0),
(3, 2, 17, '加面活动很实在，点赞。', 0, '2026-03-01 13:24:00', '2026-03-01 13:24:00', 0),
(4, 2, 15, '今天排队比平时快。', 3, '2026-03-01 13:26:00', '2026-03-01 13:26:00', 0),
(5, 3, 18, '延时营业对晚课同学很友好。', 0, '2026-03-01 13:28:00', '2026-03-01 13:28:00', 0),
(6, 4, 19, '轻食窗口建议再增加一款低糖酱料。', 0, '2026-03-01 13:30:00', '2026-03-01 13:30:00', 0),
(7, 5, 20, '辣度分级很有必要。', 0, '2026-03-01 13:32:00', '2026-03-01 13:32:00', 0),
(8, 6, 15, '炖汤日想试试双人套餐。', 0, '2026-03-01 13:34:00', '2026-03-01 13:34:00', 0),
(9, 7, 16, '夜宵新品什么时候上架？', 0, '2026-03-01 13:36:00', '2026-03-01 13:36:00', 0),
(10, 8, 17, '恢复营业太好了。', 0, '2026-03-01 13:38:00', '2026-03-01 13:38:00', 0),
(11, 9, 18, '午市套餐搭配很均衡。', 0, '2026-03-01 13:40:00', '2026-03-01 13:40:00', 0),
(12, 12, 19, '错峰建议很实用。', 0, '2026-03-01 13:42:00', '2026-03-01 13:42:00', 0),
(13, 13, 20, '开放日活动怎么报名？', 0, '2026-03-01 13:44:00', '2026-03-01 13:44:00', 0),
(14, 13, 2, '报名入口将在本周公告中发布。', 13, '2026-03-01 13:46:00', '2026-03-01 13:46:00', 0);

-- 10) canteen_dynamic_share (10 rows)
INSERT INTO canteen_dynamic_share (id, dynamic_id, user_id, share_channel, create_time, is_delete) VALUES
(1, 1, 15, 'wechat', '2026-03-01 13:50:00', 0),
(2, 2, 16, 'qq', '2026-03-01 13:51:00', 0),
(3, 3, 17, 'circle', '2026-03-01 13:52:00', 0),
(4, 4, 18, 'link', '2026-03-01 13:53:00', 0),
(5, 5, 19, 'wechat', '2026-03-01 13:54:00', 0),
(6, 6, 20, 'wecom', '2026-03-01 13:55:00', 0),
(7, 7, 15, 'wechat', '2026-03-01 13:56:00', 0),
(8, 8, 16, 'circle', '2026-03-01 13:57:00', 0),
(9, 12, 17, 'link', '2026-03-01 13:58:00', 0),
(10, 13, 18, 'wechat', '2026-03-01 13:59:00', 0);

-- 11) canteen_announcement (10 rows)
INSERT INTO canteen_announcement (
  id, title, content, cover_image, announcement_type, expire_time, publish_time,
  is_top, sort_no, status, create_time, update_time, is_delete
) VALUES
(1, 'FoodPal平台三月版本上线公告', '本次更新优化了菜品图片加载、投诉追踪与消息推送能力。', '/api/file/preview/announcement/ann_001.jpg', 'system', '2026-03-31 23:59:59', '2026-03-01 08:00:00', 1, 1, 'published', '2026-03-01 07:50:00', '2026-03-01 08:00:00', 0),
(2, '第一食堂本周检修时段通知', '3月3日14:00-16:00进行后厨电路检修，部分窗口暂停营业。', '/api/file/preview/announcement/ann_002.jpg', 'canteen', '2026-03-04 23:59:59', '2026-03-01 09:00:00', 0, 10, 'published', '2026-03-01 08:40:00', '2026-03-01 09:00:00', 0),
(3, '食品安全主题周报名通道开启', '欢迎师生报名参观后厨与食材检测流程。', '/api/file/preview/announcement/ann_003.jpg', 'system', '2026-03-20 23:59:59', '2026-03-01 09:30:00', 1, 2, 'published', '2026-03-01 09:10:00', '2026-03-01 09:30:00', 0),
(4, '第二食堂错峰就餐倡议', '建议同学们尽量避开11:50-12:20高峰时段。', '/api/file/preview/announcement/ann_004.jpg', 'canteen', '2026-03-15 23:59:59', '2026-03-01 10:00:00', 0, 20, 'published', '2026-03-01 09:40:00', '2026-03-01 10:00:00', 0),
(5, '学生建议征集：菜品口味偏好', '我们将根据问卷结果优化窗口口味配置。', '/api/file/preview/announcement/ann_005.jpg', 'system', '2026-03-25 23:59:59', '2026-03-01 10:20:00', 0, 30, 'published', '2026-03-01 10:05:00', '2026-03-01 10:20:00', 0),
(6, '夜宵档口排烟维护公告', '3月2日17:00前完成排烟系统维护，期间烧烤类暂停。', '/api/file/preview/announcement/ann_006.jpg', 'canteen', '2026-03-03 23:59:59', '2026-03-01 10:40:00', 0, 40, 'published', '2026-03-01 10:25:00', '2026-03-01 10:40:00', 0),
(7, '平台客服服务时间调整', '工作日客服在线时间调整为08:30-21:30。', '/api/file/preview/announcement/ann_007.jpg', 'system', '2026-04-01 23:59:59', '2026-03-01 11:00:00', 0, 50, 'published', '2026-03-01 10:45:00', '2026-03-01 11:00:00', 0),
(8, '清明节食堂营业安排（草案）', '具体安排待后勤处最终确认后发布正式通知。', '/api/file/preview/announcement/ann_008.jpg', 'canteen', '2026-04-06 23:59:59', '2026-03-01 11:20:00', 0, 60, 'unpublished', '2026-03-01 11:05:00', '2026-03-01 11:20:00', 0),
(9, '商户信用评价规则更新（草案）', '拟新增“高峰响应速度”指标，征求意见中。', '/api/file/preview/announcement/ann_009.jpg', 'system', '2026-04-10 23:59:59', '2026-03-01 11:40:00', 0, 70, 'unpublished', '2026-03-01 11:25:00', '2026-03-01 11:40:00', 0),
(10, '三月优秀窗口评选启动', '将从卫生、口味、效率、反馈四维度综合评选。', '/api/file/preview/announcement/ann_010.jpg', 'canteen', '2026-03-31 23:59:59', '2026-03-01 12:00:00', 1, 3, 'published', '2026-03-01 11:45:00', '2026-03-01 12:00:00', 0);

-- 12) chat_message (15 rows)
INSERT INTO chat_message (
  id, sender_id, receiver_id, content, message_type, image_url, file_url,
  is_read, read_time, is_delivered, delivered_time,
  create_time, update_time, is_delete
) VALUES
(1, 15, 5, '师傅，今天宫保鸡丁大概几点补货？', 'text', '/static/none.png', '/static/none.file', 1, '2026-03-01 11:10:00', 1, '2026-03-01 11:09:30', '2026-03-01 11:09:00', '2026-03-01 11:10:00', 0),
(2, 5, 15, '预计11:40补货，欢迎稍后再来。', 'text', '/static/none.png', '/static/none.file', 1, '2026-03-01 11:11:00', 1, '2026-03-01 11:10:30', '2026-03-01 11:10:00', '2026-03-01 11:11:00', 0),
(3, 16, 2, '建议首页增加“今日特价”聚合入口。', 'text', '/static/none.png', '/static/none.file', 0, '2026-03-01 12:00:00', 1, '2026-03-01 11:20:20', '2026-03-01 11:20:00', '2026-03-01 11:20:00', 0),
(4, 3, 9, '请补充昨天整改后的现场照片。', 'text', '/static/none.png', '/static/none.file', 1, '2026-03-01 11:35:00', 1, '2026-03-01 11:34:30', '2026-03-01 11:34:00', '2026-03-01 11:35:00', 0),
(5, 9, 3, '已上传，请查收。', 'image', '/api/file/preview/chat/9/rectify_photo_1.jpg', '/static/none.file', 1, '2026-03-01 11:36:00', 1, '2026-03-01 11:35:30', '2026-03-01 11:35:20', '2026-03-01 11:36:00', 0),
(6, 17, 7, '牛肉拉条子今天还有吗？', 'text', '/static/none.png', '/static/none.file', 1, '2026-03-01 11:50:00', 1, '2026-03-01 11:49:30', '2026-03-01 11:49:00', '2026-03-01 11:50:00', 0),
(7, 7, 17, '有的，晚高峰前供应正常。', 'text', '/static/none.png', '/static/none.file', 0, '2026-03-01 12:30:00', 1, '2026-03-01 11:51:30', '2026-03-01 11:51:00', '2026-03-01 11:51:00', 0),
(8, 18, 1, '投诉进度能否在详情页显示时间轴？', 'text', '/static/none.png', '/static/none.file', 1, '2026-03-01 12:05:00', 1, '2026-03-01 12:04:40', '2026-03-01 12:04:00', '2026-03-01 12:05:00', 0),
(9, 1, 18, '已记录，预计下周版本排期。', 'text', '/static/none.png', '/static/none.file', 1, '2026-03-01 12:06:00', 1, '2026-03-01 12:05:40', '2026-03-01 12:05:20', '2026-03-01 12:06:00', 0),
(10, 19, 10, '可以发一份本周菜单PDF吗？', 'file', '/static/none.png', '/api/file/preview/chat/10/menu_week10.pdf', 1, '2026-03-01 12:15:00', 1, '2026-03-01 12:14:30', '2026-03-01 12:14:00', '2026-03-01 12:15:00', 0),
(11, 10, 19, '已发送，请查看附件。', 'file', '/static/none.png', '/api/file/preview/chat/10/menu_week10_reply.pdf', 1, '2026-03-01 12:16:00', 1, '2026-03-01 12:15:40', '2026-03-01 12:15:20', '2026-03-01 12:16:00', 0),
(12, 20, 6, '冬瓜汤可以做少盐吗？', 'text', '/static/none.png', '/static/none.file', 0, '2026-03-01 12:40:00', 1, '2026-03-01 12:20:30', '2026-03-01 12:20:00', '2026-03-01 12:20:00', 0),
(13, 6, 20, '备注“少盐”即可，我们会单独处理。', 'text', '/static/none.png', '/static/none.file', 0, '2026-03-01 12:41:00', 0, '2026-03-01 12:21:30', '2026-03-01 12:21:00', '2026-03-01 12:21:00', 0),
(14, 2, 5, '请更新商户首页头图，尺寸建议1200x675。', 'image', '/api/file/preview/chat/admin/banner_spec.jpg', '/static/none.file', 1, '2026-03-01 12:50:00', 1, '2026-03-01 12:49:30', '2026-03-01 12:49:00', '2026-03-01 12:50:00', 0),
(15, 5, 2, '已收到，今晚闭店后更新。', 'text', '/static/none.png', '/static/none.file', 1, '2026-03-01 12:51:00', 1, '2026-03-01 12:50:30', '2026-03-01 12:50:00', '2026-03-01 12:51:00', 0);

-- 13) notification (16 rows)
INSERT INTO notification (
  id, user_id, type, title, content, related_type, related_id,
  status, read_time, create_time, update_time, is_delete
) VALUES
(1, 15, 'system', '欢迎使用FoodPal', '你的账号已完成初始化，可开始收藏与评价菜品。', 'system', 1, 'read', '2026-03-01 09:05:00', '2026-03-01 09:00:00', '2026-03-01 09:05:00', 0),
(2, 16, 'activity', '轻食窗口限时活动', '16:00-17:00指定沙拉第二份半价。', 'merchant', 4, 'unread', '2026-03-01 11:30:00', '2026-03-01 11:25:00', '2026-03-01 11:25:00', 0),
(3, 17, 'complaint', '投诉已受理', '你的投诉 CP202603010003 已进入整改阶段。', 'complaint', 3, 'read', '2026-03-01 16:05:00', '2026-03-01 16:00:00', '2026-03-01 16:05:00', 0),
(4, 18, 'complaint', '投诉处理完成', '你的投诉 CP202603010004 已完成，请评价。', 'complaint', 4, 'read', '2026-03-01 17:15:00', '2026-03-01 17:10:00', '2026-03-01 17:15:00', 0),
(5, 19, 'complaint', '投诉处理结果', '你的投诉 CP202603010005 已处理完毕。', 'complaint', 5, 'unread', '2026-03-01 17:35:00', '2026-03-01 17:30:00', '2026-03-01 17:30:00', 0),
(6, 20, 'complaint', '投诉已受理', '你的投诉 CP202603010006 正在核查中。', 'complaint', 6, 'unread', '2026-03-01 18:05:00', '2026-03-01 18:00:00', '2026-03-01 18:00:00', 0),
(7, 5, 'system', '商户资料审核通过', '你的商户资料已通过审核，可正常发布动态。', 'merchant', 1, 'read', '2026-03-01 09:10:00', '2026-03-01 09:05:00', '2026-03-01 09:10:00', 0),
(8, 7, 'system', '商户资料审核通过', '你的商户资料审核完成，营业状态正常。', 'merchant', 3, 'read', '2026-03-01 09:20:00', '2026-03-01 09:15:00', '2026-03-01 09:20:00', 0),
(9, 8, 'system', '商户整改提醒', '请补充清真认证文件并重新提交审核。', 'merchant', 8, 'unread', '2026-03-01 11:40:00', '2026-03-01 11:35:00', '2026-03-01 11:35:00', 0),
(10, 10, 'activity', '炖汤日活动排期确认', '本周三至周五炖汤日活动已排入推荐位。', 'merchant', 6, 'read', '2026-03-01 12:05:00', '2026-03-01 12:00:00', '2026-03-01 12:05:00', 0),
(11, 11, 'system', '审核结果通知', '你提交的动态已发布，请关注互动数据。', 'merchant', 7, 'unread', '2026-03-01 12:25:00', '2026-03-01 12:20:00', '2026-03-01 12:20:00', 0),
(12, 12, 'system', '审核中提醒', '你的动态正在审核，预计2小时内完成。', 'merchant', 8, 'unread', '2026-03-01 12:45:00', '2026-03-01 12:40:00', '2026-03-01 12:40:00', 0),
(13, 1, 'activity', '主题周活动报名数据更新', '当前报名人数已达到126人。', 'system', 13, 'read', '2026-03-01 13:00:00', '2026-03-01 12:55:00', '2026-03-01 13:00:00', 0),
(14, 2, 'system', '运营看板提醒', '今日午高峰投诉新增3条，请关注。', 'system', 2, 'read', '2026-03-01 13:05:00', '2026-03-01 13:00:00', '2026-03-01 13:05:00', 0),
(15, 3, 'complaint', '待处理投诉提醒', '你有2条待审核投诉，请及时处理。', 'complaint', 1, 'unread', '2026-03-01 13:15:00', '2026-03-01 13:10:00', '2026-03-01 13:10:00', 0),
(16, 4, 'complaint', '整改复核提醒', '请在今晚前完成整改复核并回填结果。', 'complaint', 2, 'read', '2026-03-01 13:20:00', '2026-03-01 13:15:00', '2026-03-01 13:20:00', 0);

-- 14) system_config (12 rows)
INSERT INTO system_config (
  id, config_key, config_value, config_type, description, create_time, update_time
) VALUES
(1, 'app_name', 'FoodPal', 'system', '应用名称', '2026-02-20 08:00:00', '2026-03-01 08:00:00'),
(2, 'app_version', '1.2.0', 'system', '应用版本号', '2026-02-20 08:05:00', '2026-03-01 08:00:00'),
(3, 'service_phone', '400-800-8899', 'contact', '客服热线', '2026-02-20 08:10:00', '2026-03-01 08:00:00'),
(4, 'service_email', 'support@foodpal.edu.cn', 'contact', '客服邮箱', '2026-02-20 08:15:00', '2026-03-01 08:00:00'),
(5, 'complaint_timeout_hours', '72', 'complaint', '投诉整改默认时限（小时）', '2026-02-20 08:20:00', '2026-03-01 08:00:00'),
(6, 'dynamic_auto_expire_days', '30', 'dynamic', '动态默认过期天数', '2026-02-20 08:25:00', '2026-03-01 08:00:00'),
(7, 'max_upload_size_mb', '20', 'file', '上传文件大小限制(MB)', '2026-02-20 08:30:00', '2026-03-01 08:00:00'),
(8, 'default_avatar', '/api/file/preview/system/default_avatar.png', 'ui', '默认头像地址', '2026-02-20 08:35:00', '2026-03-01 08:00:00'),
(9, 'notice_refresh_interval_sec', '120', 'system', '公告刷新间隔（秒）', '2026-02-20 08:40:00', '2026-03-01 08:00:00'),
(10, 'dish_top_limit', '10', 'dish', '首页推荐菜品数量上限', '2026-02-20 08:45:00', '2026-03-01 08:00:00'),
(11, 'enable_dynamic_audit', 'true', 'dynamic', '是否启用动态审核', '2026-02-20 08:50:00', '2026-03-01 08:00:00'),
(12, 'canteen_opening_notice', '食堂营业时间以公告为准，节假日将提前通知。', 'system', '食堂营业提示', '2026-02-20 08:55:00', '2026-03-01 08:00:00');

-- Recalculate counter fields from relation tables for data consistency
UPDATE dish d
SET
  like_count = (SELECT COUNT(1) FROM dish_like l WHERE l.dish_id = d.id AND l.is_delete = 0),
  favorite_count = (SELECT COUNT(1) FROM dish_favorite f WHERE f.dish_id = d.id AND f.is_delete = 0)
WHERE d.is_delete = 0;

UPDATE canteen_dynamic cd
SET
  like_count = (SELECT COUNT(1) FROM canteen_dynamic_like dl WHERE dl.dynamic_id = cd.id AND dl.is_delete = 0),
  comment_count = (SELECT COUNT(1) FROM canteen_dynamic_comment dc WHERE dc.dynamic_id = cd.id AND dc.is_delete = 0),
  share_count = (SELECT COUNT(1) FROM canteen_dynamic_share ds WHERE ds.dynamic_id = cd.id AND ds.is_delete = 0)
WHERE cd.is_delete = 0;

COMMIT;
