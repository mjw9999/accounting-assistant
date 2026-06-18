CREATE DATABASE IF NOT EXISTS accounting_assistant
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE accounting_assistant;
SET NAMES utf8mb4;

DROP TABLE IF EXISTS fenxiang;
DROP TABLE IF EXISTS shuhui_jilu;
DROP TABLE IF EXISTS touzi_jilu;
DROP TABLE IF EXISTS caiwu;
DROP TABLE IF EXISTS licaichanpin;
DROP TABLE IF EXISTS jizhang_fenlei;
DROP TABLE IF EXISTS yonghu;
DROP TABLE IF EXISTS admin;

CREATE TABLE admin (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '编号',
  username VARCHAR(64) NOT NULL UNIQUE COMMENT '管理员账号',
  password VARCHAR(120) NOT NULL COMMENT '管理员密码，支持明文演示或BCrypt密文',
  real_name VARCHAR(64) DEFAULT NULL COMMENT '管理员姓名',
  phone VARCHAR(32) DEFAULT NULL COMMENT '联系方式',
  status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED启用，DISABLED停用',
  created_at DATETIME NOT NULL COMMENT '创建时间',
  updated_at DATETIME NOT NULL COMMENT '更新时间'
) COMMENT='admin管理员表';

CREATE TABLE yonghu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '编号',
  username VARCHAR(64) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(120) NOT NULL COMMENT '用户密码，支持明文演示或BCrypt密文',
  real_name VARCHAR(64) NOT NULL COMMENT '姓名',
  phone VARCHAR(32) DEFAULT NULL COMMENT '联系方式',
  address VARCHAR(200) DEFAULT NULL COMMENT '家庭住址',
  avatar_url VARCHAR(300) DEFAULT NULL COMMENT '头像地址',
  status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED启用，DISABLED停用',
  created_at DATETIME NOT NULL COMMENT '创建时间',
  updated_at DATETIME NOT NULL COMMENT '更新时间'
) COMMENT='yonghu用户表';

CREATE TABLE jizhang_fenlei (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '编号',
  name VARCHAR(80) NOT NULL COMMENT '分类名称',
  type VARCHAR(16) NOT NULL COMMENT '类型：INCOME收入，EXPENSE支出',
  status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '状态',
  remark VARCHAR(200) DEFAULT NULL COMMENT '备注',
  created_at DATETIME NOT NULL COMMENT '创建时间',
  updated_at DATETIME NOT NULL COMMENT '更新时间',
  INDEX idx_category_type(type)
) COMMENT='记账分类表';

CREATE TABLE caiwu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '编号',
  type VARCHAR(16) NOT NULL COMMENT '类型：INCOME收入，EXPENSE支出',
  category_id BIGINT DEFAULT NULL COMMENT '分类编号',
  category_name VARCHAR(80) NOT NULL COMMENT '分类名称',
  amount DECIMAL(12,2) NOT NULL COMMENT '金额',
  remark VARCHAR(300) DEFAULT NULL COMMENT '备注',
  user_id BIGINT NOT NULL COMMENT '添加人编号',
  created_by VARCHAR(64) NOT NULL COMMENT '添加人',
  record_date DATE NOT NULL COMMENT '记账日期',
  created_at DATETIME NOT NULL COMMENT '创建时间',
  updated_at DATETIME NOT NULL COMMENT '更新时间',
  INDEX idx_caiwu_user(user_id),
  INDEX idx_caiwu_date(record_date),
  CONSTRAINT fk_caiwu_user FOREIGN KEY(user_id) REFERENCES yonghu(id)
) COMMENT='caiwu财务表';

CREATE TABLE licaichanpin (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '编号',
  name VARCHAR(100) NOT NULL COMMENT '名称',
  type VARCHAR(80) NOT NULL COMMENT '类型',
  annual_rate DECIMAL(8,4) NOT NULL COMMENT '年收益率，单位百分比',
  publisher VARCHAR(64) DEFAULT NULL COMMENT '发布者',
  min_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '最小投资金额',
  max_amount DECIMAL(12,2) NOT NULL DEFAULT 100000 COMMENT '最大投资金额',
  term_days INT DEFAULT 30 COMMENT '理财天数',
  risk_level VARCHAR(24) DEFAULT NULL COMMENT '风险等级',
  status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '状态',
  remark VARCHAR(300) DEFAULT NULL COMMENT '备注',
  created_at DATETIME NOT NULL COMMENT '创建时间',
  updated_at DATETIME NOT NULL COMMENT '更新时间'
) COMMENT='licaichanpin理财产品表';

CREATE TABLE touzi_jilu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '编号',
  product_id BIGINT NOT NULL COMMENT '理财编号',
  product_name VARCHAR(100) NOT NULL COMMENT '名称',
  product_type VARCHAR(80) NOT NULL COMMENT '类型',
  annual_rate DECIMAL(8,4) NOT NULL COMMENT '年收益率',
  amount DECIMAL(12,2) NOT NULL COMMENT '投入金额',
  investor_id BIGINT NOT NULL COMMENT '理财人编号',
  investor_name VARCHAR(64) NOT NULL COMMENT '理财人',
  start_date DATE NOT NULL COMMENT '开始日期',
  expected_redeem_date DATE NOT NULL COMMENT '预计收益时间',
  actual_days INT DEFAULT 0 COMMENT '实际理财天数',
  expected_income DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '预计收益',
  actual_income DECIMAL(12,2) DEFAULT NULL COMMENT '实际收益',
  status VARCHAR(16) NOT NULL DEFAULT 'HOLDING' COMMENT '状态：HOLDING持有中，REDEEMED已赎回',
  created_at DATETIME NOT NULL COMMENT '创建时间',
  updated_at DATETIME NOT NULL COMMENT '更新时间',
  INDEX idx_touzi_user(investor_id),
  CONSTRAINT fk_touzi_product FOREIGN KEY(product_id) REFERENCES licaichanpin(id),
  CONSTRAINT fk_touzi_user FOREIGN KEY(investor_id) REFERENCES yonghu(id)
) COMMENT='投资记录表';

CREATE TABLE shuhui_jilu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '编号',
  investment_id BIGINT NOT NULL COMMENT '投资记录编号',
  product_id BIGINT NOT NULL COMMENT '理财编号',
  product_name VARCHAR(100) NOT NULL COMMENT '名称',
  amount DECIMAL(12,2) NOT NULL COMMENT '投入金额',
  actual_days INT NOT NULL COMMENT '实际理财天数',
  income DECIMAL(12,2) NOT NULL COMMENT '赎回收益',
  redeemer_id BIGINT NOT NULL COMMENT '赎回人编号',
  redeemer_name VARCHAR(64) NOT NULL COMMENT '赎回人',
  redeem_date DATE NOT NULL COMMENT '赎回日期',
  remark VARCHAR(300) DEFAULT NULL COMMENT '备注',
  created_at DATETIME NOT NULL COMMENT '创建时间',
  updated_at DATETIME NOT NULL COMMENT '更新时间',
  CONSTRAINT fk_shuhui_investment FOREIGN KEY(investment_id) REFERENCES touzi_jilu(id),
  CONSTRAINT fk_shuhui_user FOREIGN KEY(redeemer_id) REFERENCES yonghu(id)
) COMMENT='赎回记录表';

CREATE TABLE fenxiang (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '编号',
  investment_id BIGINT NOT NULL COMMENT '投资记录编号',
  product_id BIGINT NOT NULL COMMENT '理财编号',
  product_name VARCHAR(100) NOT NULL COMMENT '名称',
  investor_name VARCHAR(64) NOT NULL COMMENT '理财人',
  amount DECIMAL(12,2) NOT NULL COMMENT '投入金额',
  income DECIMAL(12,2) NOT NULL COMMENT '投资收益',
  title VARCHAR(120) NOT NULL COMMENT '分享标题',
  content VARCHAR(1000) NOT NULL COMMENT '心得内容',
  user_id BIGINT NOT NULL COMMENT '分享人编号',
  user_name VARCHAR(64) NOT NULL COMMENT '分享人',
  status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '状态',
  created_at DATETIME NOT NULL COMMENT '创建时间',
  updated_at DATETIME NOT NULL COMMENT '更新时间',
  CONSTRAINT fk_fenxiang_investment FOREIGN KEY(investment_id) REFERENCES touzi_jilu(id),
  CONSTRAINT fk_fenxiang_user FOREIGN KEY(user_id) REFERENCES yonghu(id)
) COMMENT='fenxiang分享表';

INSERT INTO admin (username, password, real_name, phone, status, created_at, updated_at) VALUES
('admin', 'admin123', '系统管理员', '13800000000', 'ENABLED', NOW(), NOW()),
('caiwu_admin', 'admin123', '财务管理员', '13800000001', 'ENABLED', NOW(), NOW()),
('product_admin', 'admin123', '理财管理员', '13800000002', 'ENABLED', NOW(), NOW()),
('share_admin', 'admin123', '分享管理员', '13800000003', 'ENABLED', NOW(), NOW()),
('audit_admin', 'admin123', '审核管理员', '13800000004', 'ENABLED', NOW(), NOW()),
('data_admin', 'admin123', '数据管理员', '13800000005', 'ENABLED', NOW(), NOW()),
('ops_admin', 'admin123', '运营管理员', '13800000006', 'ENABLED', NOW(), NOW()),
('test_admin1', 'admin123', '测试管理员一', '13800000007', 'ENABLED', NOW(), NOW()),
('test_admin2', 'admin123', '测试管理员二', '13800000008', 'DISABLED', NOW(), NOW()),
('test_admin3', 'admin123', '测试管理员三', '13800000009', 'ENABLED', NOW(), NOW());

INSERT INTO yonghu (username, password, real_name, phone, address, avatar_url, status, created_at, updated_at) VALUES
('xinyuan', 'xinyuan1', '辛媛', '13900000001', '杭州市西湖区文三路1号', NULL, 'ENABLED', NOW(), NOW()),
('lisi', '123456', '李思', '13900000002', '上海市浦东新区张江路2号', NULL, 'ENABLED', NOW(), NOW()),
('wangwu', '123456', '王武', '13900000003', '南京市鼓楼区中山路3号', NULL, 'ENABLED', NOW(), NOW()),
('zhaoliu', '123456', '赵柳', '13900000004', '北京市海淀区学院路4号', NULL, 'ENABLED', NOW(), NOW()),
('sunqi', '123456', '孙琪', '13900000005', '成都市锦江区春熙路5号', NULL, 'ENABLED', NOW(), NOW()),
('zhouba', '123456', '周芭', '13900000006', '武汉市洪山区珞喻路6号', NULL, 'ENABLED', NOW(), NOW()),
('wujiu', '123456', '吴玖', '13900000007', '广州市天河区体育西路7号', NULL, 'ENABLED', NOW(), NOW()),
('zhengshi', '123456', '郑诗', '13900000008', '深圳市南山区科技园8号', NULL, 'ENABLED', NOW(), NOW()),
('chenyi', '123456', '陈一', '13900000009', '苏州市姑苏区人民路9号', NULL, 'ENABLED', NOW(), NOW()),
('liangliang', '123456', '梁亮', '13900000010', '长沙市岳麓区麓山路10号', NULL, 'DISABLED', NOW(), NOW());

INSERT INTO jizhang_fenlei (name, type, status, remark, created_at, updated_at) VALUES
('工资', 'INCOME', 'ENABLED', '固定工资收入', NOW(), NOW()),
('生活费', 'INCOME', 'ENABLED', '家庭或个人生活费收入', NOW(), NOW()),
('收红包', 'INCOME', 'ENABLED', '红包收入', NOW(), NOW()),
('外快', 'INCOME', 'ENABLED', '兼职外快', NOW(), NOW()),
('股票基金', 'INCOME', 'ENABLED', '投资收益类收入', NOW(), NOW()),
('其他收入', 'INCOME', 'ENABLED', '其他收入', NOW(), NOW()),
('三餐', 'EXPENSE', 'ENABLED', '日常餐饮支出', NOW(), NOW()),
('话费', 'EXPENSE', 'ENABLED', '通讯支出', NOW(), NOW()),
('学习', 'EXPENSE', 'ENABLED', '学习培训支出', NOW(), NOW()),
('日用品', 'EXPENSE', 'ENABLED', '日用品支出', NOW(), NOW()),
('住房', 'EXPENSE', 'ENABLED', '租房房贷等支出', NOW(), NOW()),
('医疗', 'EXPENSE', 'ENABLED', '医疗健康支出', NOW(), NOW()),
('交通', 'EXPENSE', 'ENABLED', '通勤出行支出', NOW(), NOW()),
('水电煤气', 'EXPENSE', 'ENABLED', '家庭能源支出', NOW(), NOW()),
('娱乐', 'EXPENSE', 'ENABLED', '娱乐消费支出', NOW(), NOW()),
('其他支出', 'EXPENSE', 'ENABLED', '其他支出', NOW(), NOW());

INSERT INTO caiwu (type, category_id, category_name, amount, remark, user_id, created_by, record_date, created_at, updated_at) VALUES
('INCOME', 1, '工资', 7800.00, '五月工资', 1, '辛媛', '2026-05-01', NOW(), NOW()),
('INCOME', 3, '收红包', 500.00, '生活费', 1, '辛媛', '2026-05-02', NOW(), NOW()),
('EXPENSE', 7, '三餐', 86.50, '工作日餐饮', 1, '辛媛', '2026-05-03', NOW(), NOW()),
('EXPENSE', 13, '交通', 120.00, '地铁公交', 1, '辛媛', '2026-05-04', NOW(), NOW()),
('EXPENSE', 10, '日用品', 236.80, '洗护用品', 2, '李思', '2026-05-05', NOW(), NOW()),
('INCOME', 4, '外快', 900.00, '周末兼职', 2, '李思', '2026-05-06', NOW(), NOW()),
('EXPENSE', 11, '住房', 2500.00, '房租', 3, '王武', '2026-05-07', NOW(), NOW()),
('EXPENSE', 12, '医疗', 168.00, '药品', 4, '赵柳', '2026-05-07', NOW(), NOW()),
('INCOME', 5, '股票基金', 320.60, '基金收益', 5, '孙琪', '2026-05-08', NOW(), NOW()),
('EXPENSE', 14, '水电煤气', 198.30, '水电燃气', 6, '周芭', '2026-05-09', NOW(), NOW());

INSERT INTO licaichanpin (name, type, annual_rate, publisher, min_amount, max_amount, term_days, risk_level, status, remark, created_at, updated_at) VALUES
('海洋化工', '基金', 3.6500, '管理员', 1000.00, 50000.00, 90, '中低风险', 'ENABLED', '测试用理财产品', NOW(), NOW()),
('再生能源', '债券', 4.2000, '管理员', 500.00, 30000.00, 120, '中风险', 'ENABLED', '新能源主题产品', NOW(), NOW()),
('货币优选', '货币', 2.3000, '管理员', 100.00, 100000.00, 30, '低风险', 'ENABLED', '现金管理产品', NOW(), NOW()),
('稳健添利', '债券', 3.1000, '管理员', 1000.00, 80000.00, 60, '低风险', 'ENABLED', '稳健型产品', NOW(), NOW()),
('成长精选', '基金', 5.2000, '管理员', 1000.00, 40000.00, 180, '中高风险', 'ENABLED', '成长行业主题', NOW(), NOW()),
('安心月享', '货币', 2.1000, '管理员', 100.00, 50000.00, 30, '低风险', 'ENABLED', '短期理财', NOW(), NOW()),
('蓝筹计划', '基金', 4.6000, '管理员', 2000.00, 60000.00, 180, '中风险', 'ENABLED', '蓝筹组合', NOW(), NOW()),
('教育储蓄', '储蓄', 2.8000, '管理员', 500.00, 20000.00, 365, '低风险', 'ENABLED', '长期储蓄', NOW(), NOW()),
('医疗健康', '基金', 4.9000, '管理员', 1000.00, 50000.00, 180, '中风险', 'ENABLED', '医疗主题', NOW(), NOW()),
('绿色债券', '债券', 3.8500, '管理员', 1000.00, 70000.00, 120, '中低风险', 'ENABLED', '绿色产业债券', NOW(), NOW());

INSERT INTO touzi_jilu (product_id, product_name, product_type, annual_rate, amount, investor_id, investor_name, start_date, expected_redeem_date, actual_days, expected_income, actual_income, status, created_at, updated_at) VALUES
(1, '海洋化工', '基金', 3.6500, 5000.00, 1, '辛媛', '2026-04-01', '2026-06-30', 90, 45.00, NULL, 'HOLDING', NOW(), NOW()),
(2, '再生能源', '债券', 4.2000, 3000.00, 1, '辛媛', '2026-01-01', '2026-05-01', 120, 41.42, 41.42, 'REDEEMED', NOW(), NOW()),
(3, '货币优选', '货币', 2.3000, 8000.00, 2, '李思', '2026-04-10', '2026-05-10', 30, 15.12, NULL, 'HOLDING', NOW(), NOW()),
(4, '稳健添利', '债券', 3.1000, 6000.00, 3, '王武', '2026-02-01', '2026-04-02', 60, 30.58, 30.58, 'REDEEMED', NOW(), NOW()),
(5, '成长精选', '基金', 5.2000, 4000.00, 4, '赵柳', '2026-03-01', '2026-08-28', 180, 102.58, NULL, 'HOLDING', NOW(), NOW()),
(6, '安心月享', '货币', 2.1000, 10000.00, 5, '孙琪', '2026-04-01', '2026-05-01', 30, 17.26, 17.26, 'REDEEMED', NOW(), NOW()),
(7, '蓝筹计划', '基金', 4.6000, 7000.00, 6, '周芭', '2026-03-15', '2026-09-11', 180, 158.79, NULL, 'HOLDING', NOW(), NOW()),
(8, '教育储蓄', '储蓄', 2.8000, 12000.00, 7, '吴玖', '2026-01-01', '2027-01-01', 365, 336.00, NULL, 'HOLDING', NOW(), NOW()),
(9, '医疗健康', '基金', 4.9000, 4500.00, 8, '郑诗', '2026-02-01', '2026-08-01', 181, 109.36, NULL, 'HOLDING', NOW(), NOW()),
(10, '绿色债券', '债券', 3.8500, 5500.00, 9, '陈一', '2026-01-15', '2026-05-15', 120, 69.62, 69.62, 'REDEEMED', NOW(), NOW());

INSERT INTO shuhui_jilu (investment_id, product_id, product_name, amount, actual_days, income, redeemer_id, redeemer_name, redeem_date, remark, created_at, updated_at) VALUES
(2, 2, '再生能源', 3000.00, 120, 41.42, 1, '辛媛', '2026-05-01', '按期赎回', NOW(), NOW()),
(4, 4, '稳健添利', 6000.00, 60, 30.58, 3, '王武', '2026-04-02', '按期赎回', NOW(), NOW()),
(6, 6, '安心月享', 10000.00, 30, 17.26, 5, '孙琪', '2026-05-01', '短期赎回', NOW(), NOW()),
(10, 10, '绿色债券', 5500.00, 120, 69.62, 9, '陈一', '2026-05-15', '到期赎回', NOW(), NOW()),
(2, 2, '再生能源', 1200.00, 90, 12.43, 1, '辛媛', '2026-04-01', '演示赎回记录', NOW(), NOW()),
(4, 4, '稳健添利', 2200.00, 60, 11.22, 3, '王武', '2026-04-05', '演示赎回记录', NOW(), NOW()),
(6, 6, '安心月享', 2600.00, 30, 4.49, 5, '孙琪', '2026-04-25', '演示赎回记录', NOW(), NOW()),
(10, 10, '绿色债券', 3200.00, 120, 40.50, 9, '陈一', '2026-05-02', '演示赎回记录', NOW(), NOW()),
(2, 2, '再生能源', 1800.00, 100, 20.71, 1, '辛媛', '2026-04-20', '演示赎回记录', NOW(), NOW()),
(4, 4, '稳健添利', 3600.00, 45, 13.76, 3, '王武', '2026-03-20', '演示赎回记录', NOW(), NOW());

INSERT INTO fenxiang (investment_id, product_id, product_name, investor_name, amount, income, title, content, user_id, user_name, status, created_at, updated_at) VALUES
(1, 1, '海洋化工', '辛媛', 5000.00, 45.00, '海洋化工收益清晰', '投入门槛适中，收益预期展示清楚。', 1, '辛媛', 'ENABLED', NOW(), NOW()),
(2, 2, '再生能源', '辛媛', 3000.00, 41.42, '近期使用最好的产品', 'num1，赎回流程简单，适合稳健投资。', 1, '辛媛', 'ENABLED', NOW(), NOW()),
(3, 3, '货币优选', '李思', 8000.00, 15.12, '现金管理方便', '短期资金放这里比较灵活。', 2, '李思', 'ENABLED', NOW(), NOW()),
(4, 4, '稳健添利', '王武', 6000.00, 30.58, '稳健型体验', '收益不高但波动小。', 3, '王武', 'ENABLED', NOW(), NOW()),
(5, 5, '成长精选', '赵柳', 4000.00, 102.58, '成长产品观察', '适合长期跟踪收益。', 4, '赵柳', 'ENABLED', NOW(), NOW()),
(6, 6, '安心月享', '孙琪', 10000.00, 17.26, '月度理财', '短期管理闲置资金。', 5, '孙琪', 'ENABLED', NOW(), NOW()),
(7, 7, '蓝筹计划', '周芭', 7000.00, 158.79, '蓝筹组合分享', '适合有一定风险承受能力的用户。', 6, '周芭', 'ENABLED', NOW(), NOW()),
(8, 8, '教育储蓄', '吴玖', 12000.00, 336.00, '教育储蓄计划', '周期较长，适合提前规划。', 7, '吴玖', 'ENABLED', NOW(), NOW()),
(9, 9, '医疗健康', '郑诗', 4500.00, 109.36, '医疗主题产品', '用于主题投资观察。', 8, '郑诗', 'ENABLED', NOW(), NOW()),
(10, 10, '绿色债券', '陈一', 5500.00, 69.62, '绿色债券体验', '到期赎回收益明确。', 9, '陈一', 'ENABLED', NOW(), NOW());
