-- ==========================================
-- 电商问数智能体 - 增强版测试数据（更新版）
-- 提供更丰富的业务场景数据
-- ==========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS data_query_agent CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE data_query_agent;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(100) NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  phone VARCHAR(20),
  role VARCHAR(20) NOT NULL DEFAULT 'user',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建商品分类表
CREATE TABLE IF NOT EXISTS categories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL UNIQUE,
  parent_id BIGINT DEFAULT NULL,
  sort_order INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建商品表
CREATE TABLE IF NOT EXISTS products (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  price DECIMAL(10,2) NOT NULL,
  stock INT NOT NULL DEFAULT 0,
  category_id BIGINT,
  sales_count INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建订单表
CREATE TABLE IF NOT EXISTS orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(50) UNIQUE NOT NULL,
  user_id BIGINT,
  total_amount DECIMAL(10,2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  payment_method VARCHAR(50),
  shipping_address TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建订单商品表
CREATE TABLE IF NOT EXISTS order_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT,
  product_id BIGINT,
  quantity INT NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建查询历史表
CREATE TABLE IF NOT EXISTS query_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  natural_language TEXT NOT NULL,
  sql_query TEXT NOT NULL,
  result_count INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 插入测试数据
-- ==========================================

-- 1. 用户测试数据（10个用户）
INSERT INTO users (username, password, email, phone, role) VALUES
('admin', '123456', 'admin@ecommerce.com', '13900000001', 'admin'),
('李明', '123456', 'liming@example.com', '13900000002', 'user'),
('王芳', '123456', 'wangfang@example.com', '13900000003', 'user'),
('刘强', '123456', 'liuqiang@example.com', '13900000004', 'user'),
('陈静', '123456', 'chenjing@example.com', '13900000005', 'user'),
('杨勇', '123456', 'yangyong@example.com', '13900000006', 'user'),
('黄丽', '123456', 'huangli@example.com', '13900000007', 'user'),
('周杰', '123456', 'zhoujie@example.com', '13900000008', 'user'),
('吴敏', '123456', 'wumin@example.com', '13900000009', 'user'),
('徐涛', '123456', 'xutao@example.com', '13900000010', 'user'),
('bf', '123456', 'bf@dataquery.local', NULL, 'admin');

-- 2. 商品分类测试数据（三级分类）
INSERT INTO categories (name, parent_id, sort_order) VALUES
-- 一级分类
('数码电子', NULL, 1),
('服饰鞋包', NULL, 2),
('家居生活', NULL, 3),
('美食生鲜', NULL, 4),
('运动户外', NULL, 5),
-- 二级分类 - 数码电子
('智能手机', 1, 1),
('笔记本电脑', 1, 2),
('智能手表', 1, 3),
('耳机音响', 1, 4),
-- 二级分类 - 服饰鞋包
('男装服饰', 2, 1),
('女装服饰', 2, 2),
('运动鞋类', 2, 3),
-- 二级分类 - 家居生活
('客厅家具', 3, 1),
('厨房电器', 3, 2),
('清洁工具', 3, 3),
-- 二级分类 - 美食生鲜
('坚果零食', 4, 1),
('新鲜水果', 4, 2),
('茶叶咖啡', 4, 3),
-- 二级分类 - 运动户外
('跑步装备', 5, 1),
('健身器材', 5, 2);

-- 3. 商品测试数据（50个商品）
INSERT INTO products (name, description, price, stock, category_id, sales_count) VALUES
-- 智能手机类
('华为Mate 60 Pro+', '卫星通信，超可靠玄武架构', 8999.00, 80, 6, 165),
('华为P60 Art', 'XMAGE影像，超聚光夜视', 6999.00, 120, 6, 132),
('小米14 Ultra', '徕卡光学Summilux镜头', 5999.00, 150, 6, 148),
('小米14', '小尺寸旗舰，徕卡影像', 4299.00, 250, 6, 175),
('vivo X100 Ultra', '蔡司APO超级长焦', 5999.00, 100, 6, 98),
('OPPO Find X7 Ultra', '双潜望四主摄', 5999.00, 130, 6, 87),
('荣耀Magic6 至臻版', '巨犀玻璃，鹰眼相机', 6999.00, 90, 6, 76),
('一加12', '东方屏，哈苏影像', 4799.00, 180, 6, 112),
('真我GT5 Pro', '骁龙8 Gen3，长焦旗舰', 3999.00, 200, 6, 143),
('iQOO 12 Pro', '自研电竞芯片Q1', 4999.00, 160, 6, 125),

-- 笔记本电脑类
('联想拯救者Y9000P', 'i9-14900HX，RTX4060', 9999.00, 60, 7, 95),
('华硕天选5 Pro', '锐龙9 7940HX，RTX4070', 8999.00, 70, 7, 88),
('惠普暗影精灵10', 'i7-14700HX，RTX4060', 7999.00, 80, 7, 102),
('戴尔游匣G16', 'i9-13900HX，RTX4060', 8499.00, 50, 7, 67),
('机械革命蛟龙16 Pro', '锐龙9 7945HX，RTX4070', 7999.00, 90, 7, 118),

-- 智能手表类
('华为WATCH GT 4 Pro', '专业运动健康，钛金属', 2688.00, 150, 8, 186),
('Apple Watch Ultra 2', '专业户外运动手表', 5999.00, 60, 8, 78),
('小米Watch S3', '百变表圈，独立通信', 899.00, 300, 8, 345),
('OPPO Watch X', '双引擎架构，eSIM独立通信', 1999.00, 120, 8, 156),

-- 耳机音响类
('索尼WH-1000XM5', '行业领先降噪，高解析音质', 2499.00, 180, 9, 235),
('Bose QuietComfort Ultra', '沉浸式空间音频', 2999.00, 100, 9, 168),
('AirPods Pro 2', '自适应音频，USB-C充电', 1899.00, 250, 9, 412),
('华为FreeBuds Pro 3', '星闪连接，超CD级音质', 1499.00, 200, 9, 287),

-- 男装服饰
('海澜之家POLO衫', '商务休闲，纯棉面料', 199.00, 500, 10, 368),
('七匹狼夹克外套', '春秋薄款，防风面料', 499.00, 300, 10, 245),
('雅戈尔西服套装', '商务正装，羊毛面料', 1999.00, 150, 10, 128),
('太平鸟男装休闲裤', '舒适弹力，修身版型', 299.00, 400, 10, 286),

-- 女装服饰
('UR女装连衣裙', '法式复古，印花设计', 399.00, 350, 11, 312),
('MO&Co.西装外套', '职场通勤，剪裁利落', 899.00, 200, 11, 178),
('伊芙丽针织衫', '柔软亲肤，多色可选', 299.00, 450, 11, 395),
('江南布衣半身裙', '文艺复古，棉麻材质', 459.00, 280, 11, 224),

-- 运动鞋类
('Nike Air Jordan 1', '经典复刻，篮球文化', 1299.00, 180, 12, 298),
('Adidas Yeezy 350 V2', 'Boost缓震，潮流设计', 1899.00, 120, 12, 215),
('李宁韦德之道11', '专业篮球鞋，䨻科技', 899.00, 250, 12, 342),
('安踏狂潮5', '水泥地克星，耐磨橡胶', 499.00, 350, 12, 486),
('特步160X 5.0', '碳板跑鞋，竞速之选', 699.00, 280, 12, 378),

-- 客厅家具
('芝华仕头等舱沙发', '真皮材质，电动功能位', 12999.00, 40, 13, 85),
('源氏木语实木茶几', '橡木材质，日式简约', 1999.00, 80, 13, 142),
('全友电视柜组合', '现代简约，大容量储物', 2499.00, 100, 13, 168),
('林氏家居鞋柜', '翻斗设计，超薄贴墙', 899.00, 150, 13, 235),

-- 厨房电器
('老板双腔大吸力油烟机', '26m³/min，变频电机', 4999.00, 80, 14, 176),
('方太嵌入式洗碗机', '13套容量，热风烘干', 5999.00, 60, 14, 145),
('松下微蒸烤一体机', '30L容量，变频微波', 2999.00, 100, 14, 218),
('西门子嵌入式烤箱', '4D热风，智能菜单', 3999.00, 70, 14, 162),

-- 清洁工具
('戴森V15吸尘器', '激光探测，240AW吸力', 4990.00, 120, 15, 285),
('石头G20扫地机器人', '双胶刷，自动集尘', 3999.00, 150, 15, 356),
('追觅洗地机H12 Pro', '双贴边，热水洗滚刷', 2499.00, 180, 15, 412),
('添可芙万3.0', '智能感应，电解水除菌', 3299.00, 130, 15, 298),

-- 坚果零食
('沃隆每日坚果', '7种坚果果干，30日装', 139.00, 800, 16, 756),
('洽洽小黄袋坚果', '益生菌每日坚果', 159.00, 600, 16, 685),
('乐事薯片大礼包', '多口味组合，12包装', 59.90, 1500, 16, 1285),
('卫龙辣条大礼包', '经典口味，怀旧零食', 39.90, 2000, 16, 1680),

-- 新鲜水果
('阳光玫瑰葡萄', '晴王品种，2斤装', 89.00, 600, 17, 568),
('丹东99草莓', '红颜品种，3斤装', 69.00, 800, 17, 892),
('泰国金枕榴莲', 'A级果，4-5斤', 199.00, 400, 17, 456),
('新疆阿克苏苹果', '冰糖心，5斤装', 49.90, 1000, 17, 1125),

-- 茶叶咖啡
('西湖龙井明前茶', '特级绿茶，100g', 399.00, 300, 18, 385),
('云南普洱熟茶饼', '陈年古树，357g', 299.00, 400, 18, 468),
('星巴克咖啡豆', '派克市场烘焙，200g', 89.00, 600, 18, 725),
('三顿半数字星球咖啡', '超即溶，6颗装', 79.00, 800, 18, 956),

-- 跑步装备
('Garmin佳明Forerunner 265', 'AMOLED屏幕，训练建议', 3480.00, 100, 19, 198),
('SUUNTO颂拓Race', '双频GPS，离线地图', 3990.00, 80, 19, 145),
('Compressport压缩腿套', '渐进式压缩，促进恢复', 399.00, 250, 19, 325),
('迪卡侬跑步腰包', '轻薄隐形，防水设计', 59.90, 500, 19, 856),

-- 健身器材
('Keep智能动感单车', '磁控阻力，实景课程', 1999.00, 120, 20, 268),
('小米 WalkingPad走步机', '折叠设计，智能控速', 1499.00, 150, 20, 345),
('李宁可调节哑铃', '20KG一对，快速调重', 599.00, 200, 20, 486),
('迪卡侬引体向上器', '门上安装，多档调节', 199.00, 300, 20, 625);

-- 4. 订单测试数据（50个订单，不同状态和日期）
INSERT INTO orders (order_no, user_id, total_amount, status, payment_method, created_at) VALUES
-- 已完成的订单（最近30天）
('ORD202404010001', 2, 8999.00, '已完成', '支付宝', '2024-04-01 09:15:00'),
('ORD202404010002', 3, 5999.00, '已完成', '微信', '2024-04-01 13:45:00'),
('ORD202404020003', 4, 1299.00, '已完成', '支付宝', '2024-04-02 10:30:00'),
('ORD202404020004', 5, 12999.00, '已完成', '银行卡', '2024-04-02 15:20:00'),
('ORD202404030005', 2, 139.00, '已完成', '微信', '2024-04-03 08:45:00'),
('ORD202404030006', 6, 9999.00, '已完成', '支付宝', '2024-04-03 14:10:00'),
('ORD202404040007', 7, 1999.00, '已完成', '微信', '2024-04-04 11:25:00'),
('ORD202404040008', 3, 6999.00, '已完成', '支付宝', '2024-04-04 16:50:00'),
('ORD202404050009', 8, 699.00, '已完成', '微信', '2024-04-05 09:30:00'),
('ORD202404050010', 9, 4990.00, '已完成', '银行卡', '2024-04-05 14:15:00'),
('ORD202404060011', 2, 299.00, '已完成', '支付宝', '2024-04-06 10:40:00'),
('ORD202404060012', 10, 399.00, '已完成', '微信', '2024-04-06 15:55:00'),
('ORD202404070013', 4, 8999.00, '已完成', '支付宝', '2024-04-07 09:20:00'),
('ORD202404070014', 5, 2499.00, '已完成', '微信', '2024-04-07 13:35:00'),
('ORD202404080015', 6, 3480.00, '已完成', '支付宝', '2024-04-08 11:10:00'),
('ORD202404080016', 7, 5999.00, '已完成', '银行卡', '2024-04-08 16:25:00'),
('ORD202404090017', 3, 9999.00, '已完成', '支付宝', '2024-04-09 10:05:00'),
('ORD202404090018', 8, 89.00, '已完成', '微信', '2024-04-09 14:40:00'),
('ORD202404100019', 9, 4999.00, '已完成', '支付宝', '2024-04-10 09:50:00'),
('ORD202404100020', 2, 2688.00, '已完成', '微信', '2024-04-10 15:15:00'),

-- 待收货的订单
('ORD202404110021', 3, 5999.00, '待收货', '支付宝', '2024-04-11 10:20:00'),
('ORD202404110022', 4, 3999.00, '待收货', '微信', '2024-04-11 14:35:00'),
('ORD202404110023', 5, 899.00, '待收货', '支付宝', '2024-04-11 16:50:00'),
('ORD202404120024', 6, 4999.00, '待收货', '银行卡', '2024-04-12 09:25:00'),
('ORD202404120025', 7, 1999.00, '待收货', '微信', '2024-04-12 13:40:00'),

-- 待发货的订单
('ORD202404120026', 8, 8999.00, '待发货', '支付宝', '2024-04-12 15:55:00'),
('ORD202404120027', 9, 4299.00, '待发货', '微信', '2024-04-12 16:30:00'),
('ORD202404130028', 10, 599.00, '待发货', '支付宝', '2024-04-13 10:45:00'),
('ORD202404130029', 2, 2999.00, '待发货', '微信', '2024-04-13 14:20:00'),
('ORD202404130030', 3, 1599.00, '待发货', '支付宝', '2024-04-13 16:35:00'),

-- 待付款的订单
('ORD202404140031', 4, 12999.00, '待付款', '支付宝', '2024-04-14 09:10:00'),
('ORD202404140032', 5, 1899.00, '待付款', '微信', '2024-04-14 11:25:00'),
('ORD202404140033', 6, 5990.00, '待付款', '银行卡', '2024-04-14 14:40:00'),
('ORD202404150034', 7, 799.00, '待付款', '支付宝', '2024-04-15 10:55:00'),
('ORD202404150035', 8, 7999.00, '待付款', '微信', '2024-04-15 15:10:00'),

-- 已取消的订单
('ORD202404100036', 9, 3999.00, '已取消', '支付宝', '2024-04-10 09:25:00'),
('ORD202404110037', 10, 1999.00, '已取消', '微信', '2024-04-11 13:40:00'),
('ORD202404120038', 2, 6999.00, '已取消', '支付宝', '2024-04-12 16:55:00'),
('ORD202404130039', 3, 499.00, '已取消', '微信', '2024-04-13 11:10:00'),
('ORD202404140040', 4, 9999.00, '已取消', '银行卡', '2024-04-14 14:25:00');

-- 5. 订单商品测试数据（详细订单项）
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES
-- 订单1: 华为Mate 60 Pro+
(1, 1, 1, 8999.00),
-- 订单2: vivo X100 Ultra
(2, 5, 1, 5999.00),
-- 订单3: Nike Air Jordan 1
(3, 40, 1, 1299.00),
-- 订单4: 芝华仕头等舱沙发
(4, 53, 1, 12999.00),
-- 订单5: 沃隆每日坚果
(5, 61, 1, 139.00),
-- 订单6: 联想拯救者Y9000P
(6, 11, 1, 9999.00),
-- 订单7: 海澜之家POLO衫
(7, 25, 1, 1999.00),
-- 订单8: 华为P60 Art
(8, 2, 1, 6999.00),
-- 订单9: 李宁韦德之道11
(9, 42, 1, 699.00),
-- 订单10: 戴森V15吸尘器
(10, 59, 1, 4990.00),
-- 订单11: 七匹狼夹克外套
(11, 26, 1, 299.00),
-- 订单12: UR女装连衣裙
(12, 29, 1, 399.00),
-- 订单13: 华硕天选5 Pro
(13, 12, 1, 8999.00),
-- 订单14: 方太嵌入式洗碗机
(14, 55, 1, 2499.00),
-- 订单15: Garmin佳明Forerunner 265
(15, 69, 1, 3480.00),
-- 订单16: OPPO Find X7 Ultra
(16, 6, 1, 5999.00),
-- 订单17: 惠普暗影精灵10
(17, 13, 1, 9999.00),
-- 订单18: 阳光玫瑰葡萄
(18, 65, 1, 89.00),
-- 订单19: 戴尔游匣G16
(19, 14, 1, 4999.00),
-- 订单20: 华为WATCH GT 4 Pro
(20, 16, 1, 2688.00),
-- 订单21: Apple Watch Ultra 2
(21, 17, 1, 5999.00),
-- 订单22: 老板双腔大吸力油烟机
(22, 54, 1, 3999.00),
-- 订单23: 安踏狂潮5
(23, 43, 1, 899.00),
-- 订单24: 小米14 Ultra
(24, 3, 1, 4999.00),
-- 订单25: MO&Co.西装外套
(25, 30, 1, 1999.00),
-- 订单26: 华为Mate 60 Pro+
(26, 1, 1, 8999.00),
-- 订单27: 小米14
(27, 4, 1, 4299.00),
-- 订单28: 松下微蒸烤一体机
(28, 56, 1, 599.00),
-- 订单29: 源氏木语实木茶几
(29, 54, 1, 2999.00),
-- 订单30: 伊芙丽针织衫
(30, 31, 1, 1599.00),
-- 订单31: 机械革命蛟龙16 Pro
(31, 15, 1, 12999.00),
-- 订单32: 雅戈尔西服套装
(32, 27, 1, 1899.00),
-- 订单33: 荣耀Magic6 至臻版
(33, 7, 1, 5990.00),
-- 订单34: 丹东99草莓
(34, 66, 1, 799.00),
-- 订单35: 石头G20扫地机器人
(35, 60, 1, 7999.00),
-- 订单36-40: 已取消订单的商品
(36, 55, 1, 3999.00),
(37, 30, 1, 1999.00),
(38, 6, 1, 6999.00),
(39, 61, 1, 499.00),
(40, 12, 1, 9999.00);

-- ==========================================
-- 为表字段添加备注（不影响原有表结构和数据）
-- ==========================================

-- 修复id字段的自增属性（如果表已存在但缺少AUTO_INCREMENT）
ALTER TABLE users MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '用户ID';
ALTER TABLE categories MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '分类ID';
ALTER TABLE products MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '商品ID';
ALTER TABLE orders MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '订单ID';
ALTER TABLE order_items MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '明细ID';
ALTER TABLE query_history MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '历史记录ID';

-- 用户表字段备注
ALTER TABLE users MODIFY COLUMN username VARCHAR(50) COMMENT '用户名';
ALTER TABLE users MODIFY COLUMN password VARCHAR(100) COMMENT '密码';
ALTER TABLE users MODIFY COLUMN email VARCHAR(100) COMMENT '邮箱';
ALTER TABLE users MODIFY COLUMN phone VARCHAR(20) COMMENT '手机号';
ALTER TABLE users MODIFY COLUMN role VARCHAR(20) COMMENT '角色（admin/user）';
ALTER TABLE users MODIFY COLUMN created_at TIMESTAMP COMMENT '创建时间';
ALTER TABLE users MODIFY COLUMN updated_at TIMESTAMP COMMENT '更新时间';

-- 商品分类表字段备注
ALTER TABLE categories MODIFY COLUMN name VARCHAR(100) COMMENT '分类名称';
ALTER TABLE categories MODIFY COLUMN parent_id BIGINT COMMENT '父分类ID';
ALTER TABLE categories MODIFY COLUMN sort_order INT COMMENT '排序顺序';
ALTER TABLE categories MODIFY COLUMN created_at TIMESTAMP COMMENT '创建时间';
ALTER TABLE categories MODIFY COLUMN updated_at TIMESTAMP COMMENT '更新时间';

-- 商品表字段备注
ALTER TABLE products MODIFY COLUMN name VARCHAR(255) COMMENT '商品名称';
ALTER TABLE products MODIFY COLUMN description TEXT COMMENT '商品描述';
ALTER TABLE products MODIFY COLUMN price DECIMAL(10,2) COMMENT '价格';
ALTER TABLE products MODIFY COLUMN stock INT COMMENT '库存数量';
ALTER TABLE products MODIFY COLUMN category_id BIGINT COMMENT '分类ID';
ALTER TABLE products MODIFY COLUMN sales_count INT COMMENT '销售数量';
ALTER TABLE products MODIFY COLUMN created_at TIMESTAMP COMMENT '创建时间';
ALTER TABLE products MODIFY COLUMN updated_at TIMESTAMP COMMENT '更新时间';

-- 订单表字段备注
ALTER TABLE orders MODIFY COLUMN order_no VARCHAR(50) COMMENT '订单编号';
ALTER TABLE orders MODIFY COLUMN user_id BIGINT COMMENT '用户ID';
ALTER TABLE orders MODIFY COLUMN total_amount DECIMAL(10,2) COMMENT '订单总额';
ALTER TABLE orders MODIFY COLUMN status VARCHAR(20) COMMENT '订单状态（待付款/待发货/待收货/已完成/已取消）';
ALTER TABLE orders MODIFY COLUMN payment_method VARCHAR(50) COMMENT '支付方式';
ALTER TABLE orders MODIFY COLUMN shipping_address TEXT COMMENT '收货地址';
ALTER TABLE orders MODIFY COLUMN created_at TIMESTAMP COMMENT '创建时间';
ALTER TABLE orders MODIFY COLUMN updated_at TIMESTAMP COMMENT '更新时间';

-- 订单商品表字段备注
ALTER TABLE order_items MODIFY COLUMN order_id BIGINT COMMENT '订单ID';
ALTER TABLE order_items MODIFY COLUMN product_id BIGINT COMMENT '商品ID';
ALTER TABLE order_items MODIFY COLUMN quantity INT COMMENT '数量';
ALTER TABLE order_items MODIFY COLUMN price DECIMAL(10,2) COMMENT '单价';
ALTER TABLE order_items MODIFY COLUMN created_at TIMESTAMP COMMENT '创建时间';

-- 查询历史表字段备注
ALTER TABLE query_history MODIFY COLUMN user_id BIGINT COMMENT '用户ID';
ALTER TABLE query_history MODIFY COLUMN natural_language TEXT COMMENT '自然语言查询';
ALTER TABLE query_history MODIFY COLUMN sql_query TEXT COMMENT 'SQL查询语句';
ALTER TABLE query_history MODIFY COLUMN result_count INT COMMENT '结果数量';
ALTER TABLE query_history MODIFY COLUMN created_at TIMESTAMP COMMENT '创建时间';
