/*
 Navicat Premium Data Transfer

 Source Server         : demo1
 Source Server Type    : MySQL
 Source Server Version : 50726
 Source Host           : localhost:3306
 Source Schema         : accounting_assistant

 Target Server Type    : MySQL
 Target Server Version : 50726
 File Encoding         : 65001

 Date: 13/05/2026 15:28:06
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '管理员账号',
  `password` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '管理员密码，支持明文演示或BCrypt密文',
  `real_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '管理员姓名',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系方式',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED启用，DISABLED停用',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'admin管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'admin', 'admin123', '系统管理员', '13800000000', 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `admin` VALUES (2, 'caiwu_admin', 'admin123', '财务管理员', '13800000001', 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `admin` VALUES (3, 'product_admin', 'admin123', '理财管理员', '13800000002', 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `admin` VALUES (4, 'share_admin', 'admin123', '分享管理员', '13800000003', 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `admin` VALUES (5, 'audit_admin', 'admin123', '审核管理员', '13800000004', 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `admin` VALUES (6, 'data_admin', 'admin123', '数据管理员', '13800000005', 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `admin` VALUES (7, 'ops_admin', 'admin123', '运营管理员', '13800000006', 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `admin` VALUES (8, 'test_admin1', 'admin123', '测试管理员一', '13800000007', 'DISABLED', '2026-05-11 18:36:06', '2026-05-13 15:12:03');
INSERT INTO `admin` VALUES (9, 'test_admin2', 'admin123', '测试管理员二', '13800000008', 'DISABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `admin` VALUES (10, 'test_admin3', 'admin123', '测试管理员三', '13800000009', 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');

-- ----------------------------
-- Table structure for caiwu
-- ----------------------------
DROP TABLE IF EXISTS `caiwu`;
CREATE TABLE `caiwu`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型：INCOME收入，EXPENSE支出',
  `category_id` bigint(20) NULL DEFAULT NULL COMMENT '分类编号',
  `category_name` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `amount` decimal(12, 2) NOT NULL COMMENT '金额',
  `remark` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `user_id` bigint(20) NOT NULL COMMENT '添加人编号',
  `created_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '添加人',
  `record_date` date NOT NULL COMMENT '记账日期',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  `record_source` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_caiwu_user`(`user_id`) USING BTREE,
  INDEX `idx_caiwu_date`(`record_date`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 58 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'caiwu财务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of caiwu
-- ----------------------------
INSERT INTO `caiwu` VALUES (1, 'INCOME', 1, '工资', 7800.00, '五月工资', 1, '普通测试员', '2026-05-01', '2026-05-11 18:36:06', '2026-05-13 15:17:50', 'MANUAL');
INSERT INTO `caiwu` VALUES (2, 'INCOME', 3, '收红包', 500.00, '生活费', 1, '普通测试员', '2026-05-02', '2026-05-11 18:36:06', '2026-05-13 15:17:50', 'MANUAL');
INSERT INTO `caiwu` VALUES (3, 'EXPENSE', 7, '三餐', 86.50, '工作日餐饮', 1, '普通测试员', '2026-05-03', '2026-05-11 18:36:06', '2026-05-13 15:17:50', 'MANUAL');
INSERT INTO `caiwu` VALUES (4, 'EXPENSE', 13, '交通', 120.00, '地铁公交', 1, '普通测试员', '2026-05-04', '2026-05-11 18:36:06', '2026-05-13 15:17:50', 'MANUAL');
INSERT INTO `caiwu` VALUES (5, 'EXPENSE', 10, '日用品', 236.80, '洗护用品', 2, '李思', '2026-05-05', '2026-05-11 18:36:06', '2026-05-13 14:41:11', 'MANUAL');
INSERT INTO `caiwu` VALUES (6, 'INCOME', 4, '外快', 900.00, '周末兼职', 2, '李思', '2026-05-06', '2026-05-11 18:36:06', '2026-05-13 14:41:11', 'MANUAL');
INSERT INTO `caiwu` VALUES (7, 'EXPENSE', 11, '住房', 2500.00, '房租', 3, '王武', '2026-05-07', '2026-05-11 18:36:06', '2026-05-13 14:41:11', 'MANUAL');
INSERT INTO `caiwu` VALUES (8, 'EXPENSE', 12, '医疗', 168.00, '药品', 4, '赵柳', '2026-05-07', '2026-05-11 18:36:06', '2026-05-13 14:41:11', 'MANUAL');
INSERT INTO `caiwu` VALUES (9, 'INCOME', 5, '股票基金', 320.60, '基金收益', 5, '孙琪', '2026-05-08', '2026-05-11 18:36:06', '2026-05-13 14:41:11', 'MANUAL');
INSERT INTO `caiwu` VALUES (10, 'EXPENSE', 14, '水电煤气', 198.30, '水电燃气', 6, '周芭', '2026-05-09', '2026-05-11 18:36:06', '2026-05-13 14:41:11', 'MANUAL');
INSERT INTO `caiwu` VALUES (11, 'EXPENSE', NULL, '交通', 35.00, '高铁票', 1, '普通测试员', '2026-05-11', '2026-05-11 18:37:52', '2026-05-13 15:17:50', 'MANUAL');
INSERT INTO `caiwu` VALUES (12, 'INCOME', 2, '生活费', 500.00, 'test', 11, '毛毛', '2026-05-06', '2026-05-12 18:40:12', '2026-05-13 14:41:11', 'MANUAL');
INSERT INTO `caiwu` VALUES (13, 'EXPENSE', 16, '其他支出', 78.00, '', 1, '普通测试员', '2026-05-04', '2026-05-12 20:09:21', '2026-05-13 15:17:50', 'MANUAL');
INSERT INTO `caiwu` VALUES (14, 'EXPENSE', 14, '水电煤气', 377.00, '', 1, '普通测试员', '2024-01-01', '2026-05-12 20:09:49', '2026-05-13 15:17:50', 'MANUAL');
INSERT INTO `caiwu` VALUES (15, 'EXPENSE', 8, '话费', 70.00, '', 1, '普通测试员', '2026-05-01', '2026-05-12 20:10:35', '2026-05-13 15:17:50', 'MANUAL');
INSERT INTO `caiwu` VALUES (16, 'INCOME', 5, '股票基金', 6000.00, '', 1, '普通测试员', '2026-05-12', '2026-05-12 21:56:42', '2026-05-13 15:17:50', 'MANUAL');
INSERT INTO `caiwu` VALUES (17, 'INCOME', 4, '外快', 500.00, '', 13, '毛', '2026-05-12', '2026-05-12 22:10:30', '2026-05-13 14:41:11', 'MANUAL');
INSERT INTO `caiwu` VALUES (18, 'INCOME', 2, '生活费', 0.01, '', 1, '普通测试员', '2026-02-04', '2026-05-12 22:15:41', '2026-05-13 15:17:50', 'MANUAL');
INSERT INTO `caiwu` VALUES (19, 'EXPENSE', 11, '住房', 2500.00, '', 1, '普通测试员', '2025-07-01', '2026-05-12 22:16:08', '2026-05-13 15:17:50', 'MANUAL');
INSERT INTO `caiwu` VALUES (22, 'INCOME', 17, '理财收益', 41.42, '理财产品【再生能源】赎回收益自动入账（理财编号：ZQ202605120002）', 1, '普通测试员', '2026-05-01', '2026-05-13 11:26:50', '2026-05-13 15:17:50', 'AUTO_REDEMPTION');
INSERT INTO `caiwu` VALUES (21, 'INCOME', 1, '工资', 5000.00, '', 1, '普通测试员', '2025-07-02', '2026-05-12 22:24:28', '2026-05-13 15:17:50', 'MANUAL');
INSERT INTO `caiwu` VALUES (23, 'INCOME', 17, '理财收益', 30.58, '理财产品【稳健添利】赎回收益自动入账（理财编号：ZQ202605120004）', 3, '王武', '2026-04-02', '2026-05-13 11:26:50', '2026-05-13 14:41:11', 'AUTO_REDEMPTION');
INSERT INTO `caiwu` VALUES (24, 'INCOME', 17, '理财收益', 17.26, '理财产品【安心月享】赎回收益自动入账（理财编号：HB202605120006）', 5, '孙琪', '2026-05-01', '2026-05-13 11:26:50', '2026-05-13 14:41:11', 'AUTO_REDEMPTION');
INSERT INTO `caiwu` VALUES (25, 'INCOME', 17, '理财收益', 69.62, '理财产品【绿色债券】赎回收益自动入账（理财编号：ZQ202605120010）', 9, '陈一', '2026-05-15', '2026-05-13 11:26:50', '2026-05-13 14:41:11', 'AUTO_REDEMPTION');
INSERT INTO `caiwu` VALUES (26, 'INCOME', 17, '理财收益', 12.43, '理财产品【再生能源】赎回收益自动入账（理财编号：ZQ202605120002）', 1, '普通测试员', '2026-04-01', '2026-05-13 11:26:50', '2026-05-13 15:17:50', 'AUTO_REDEMPTION');
INSERT INTO `caiwu` VALUES (27, 'INCOME', 17, '理财收益', 11.22, '理财产品【稳健添利】赎回收益自动入账（理财编号：ZQ202605120004）', 3, '王武', '2026-04-05', '2026-05-13 11:26:50', '2026-05-13 14:41:11', 'AUTO_REDEMPTION');
INSERT INTO `caiwu` VALUES (28, 'INCOME', 17, '理财收益', 4.49, '理财产品【安心月享】赎回收益自动入账（理财编号：HB202605120006）', 5, '孙琪', '2026-04-25', '2026-05-13 11:26:50', '2026-05-13 14:41:11', 'AUTO_REDEMPTION');
INSERT INTO `caiwu` VALUES (29, 'INCOME', 17, '理财收益', 40.50, '理财产品【绿色债券】赎回收益自动入账（理财编号：ZQ202605120010）', 9, '陈一', '2026-05-02', '2026-05-13 11:26:50', '2026-05-13 14:41:11', 'AUTO_REDEMPTION');
INSERT INTO `caiwu` VALUES (30, 'INCOME', 17, '理财收益', 20.71, '理财产品【再生能源】赎回收益自动入账（理财编号：ZQ202605120002）', 1, '普通测试员', '2026-04-20', '2026-05-13 11:26:50', '2026-05-13 15:17:50', 'AUTO_REDEMPTION');
INSERT INTO `caiwu` VALUES (31, 'INCOME', 17, '理财收益', 13.76, '理财产品【稳健添利】赎回收益自动入账（理财编号：ZQ202605120004）', 3, '王武', '2026-03-20', '2026-05-13 11:26:50', '2026-05-13 14:41:11', 'AUTO_REDEMPTION');
INSERT INTO `caiwu` VALUES (44, 'INCOME', 17, '理财收益', 0.13, '理财产品【医疗健康】赎回收益自动入账（理财编号：JJ202605120009）', 1, '普通测试员', '2026-05-13', '2026-05-13 11:54:03', '2026-05-13 15:17:50', 'AUTO_REDEMPTION');
INSERT INTO `caiwu` VALUES (45, 'INCOME', 17, '理财收益', 41.42, '理财产品【再生能源】赎回收益自动入账（理财编号：ZQ202605120002）', 1, '普通测试员', '2026-05-01', '2026-05-13 12:18:34', '2026-05-13 15:17:50', 'AUTO_REDEMPTION');
INSERT INTO `caiwu` VALUES (46, 'INCOME', 17, '理财收益', 30.58, '理财产品【稳健添利】赎回收益自动入账（理财编号：ZQ202605120004）', 3, '王武', '2026-04-02', '2026-05-13 12:18:34', '2026-05-13 14:41:11', 'AUTO_REDEMPTION');
INSERT INTO `caiwu` VALUES (47, 'INCOME', 17, '理财收益', 17.26, '理财产品【安心月享】赎回收益自动入账（理财编号：HB202605120006）', 5, '孙琪', '2026-05-01', '2026-05-13 12:18:34', '2026-05-13 14:41:11', 'AUTO_REDEMPTION');
INSERT INTO `caiwu` VALUES (48, 'INCOME', 17, '理财收益', 69.62, '理财产品【绿色债券】赎回收益自动入账（理财编号：ZQ202605120010）', 9, '陈一', '2026-05-15', '2026-05-13 12:18:34', '2026-05-13 14:41:11', 'AUTO_REDEMPTION');
INSERT INTO `caiwu` VALUES (49, 'INCOME', 17, '理财收益', 12.43, '理财产品【再生能源】赎回收益自动入账（理财编号：ZQ202605120002）', 1, '普通测试员', '2026-04-01', '2026-05-13 12:18:34', '2026-05-13 15:17:50', 'AUTO_REDEMPTION');
INSERT INTO `caiwu` VALUES (50, 'INCOME', 17, '理财收益', 11.22, '理财产品【稳健添利】赎回收益自动入账（理财编号：ZQ202605120004）', 3, '王武', '2026-04-05', '2026-05-13 12:18:34', '2026-05-13 14:41:11', 'AUTO_REDEMPTION');
INSERT INTO `caiwu` VALUES (51, 'INCOME', 17, '理财收益', 4.49, '理财产品【安心月享】赎回收益自动入账（理财编号：HB202605120006）', 5, '孙琪', '2026-04-25', '2026-05-13 12:18:34', '2026-05-13 14:41:11', 'AUTO_REDEMPTION');
INSERT INTO `caiwu` VALUES (52, 'INCOME', 17, '理财收益', 40.50, '理财产品【绿色债券】赎回收益自动入账（理财编号：ZQ202605120010）', 9, '陈一', '2026-05-02', '2026-05-13 12:18:34', '2026-05-13 14:41:11', 'AUTO_REDEMPTION');
INSERT INTO `caiwu` VALUES (53, 'INCOME', 17, '理财收益', 20.71, '理财产品【再生能源】赎回收益自动入账（理财编号：ZQ202605120002）', 1, '普通测试员', '2026-04-20', '2026-05-13 12:18:34', '2026-05-13 15:17:50', 'AUTO_REDEMPTION');
INSERT INTO `caiwu` VALUES (54, 'INCOME', 17, '理财收益', 13.76, '理财产品【稳健添利】赎回收益自动入账（理财编号：ZQ202605120004）', 3, '王武', '2026-03-20', '2026-05-13 12:18:34', '2026-05-13 14:41:11', 'AUTO_REDEMPTION');
INSERT INTO `caiwu` VALUES (55, 'INCOME', 17, '理财收益', 0.08, '理财产品【教育储蓄】赎回收益自动入账（理财编号：CX202605120008）', 1, '普通测试员', '2026-05-12', '2026-05-13 12:18:34', '2026-05-13 15:17:50', 'AUTO_REDEMPTION');
INSERT INTO `caiwu` VALUES (56, 'INCOME', 17, '理财收益', 0.13, '理财产品【医疗健康】赎回收益自动入账（理财编号：JJ202605120009）', 1, '普通测试员', '2026-05-13', '2026-05-13 12:18:34', '2026-05-13 15:17:50', 'AUTO_REDEMPTION');
INSERT INTO `caiwu` VALUES (57, 'EXPENSE', 15, '娱乐', 100.00, '出去聚餐费用', 1, '普通测试员', '2026-05-13', '2026-05-13 14:36:48', '2026-05-13 15:17:50', 'MANUAL');

-- ----------------------------
-- Table structure for fenxiang
-- ----------------------------
DROP TABLE IF EXISTS `fenxiang`;
CREATE TABLE `fenxiang`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `investment_id` bigint(20) NOT NULL COMMENT '投资记录编号',
  `product_id` bigint(20) NOT NULL COMMENT '理财编号',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '名称',
  `investor_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '理财人',
  `amount` decimal(12, 2) NOT NULL COMMENT '投入金额',
  `income` decimal(12, 2) NOT NULL COMMENT '投资收益',
  `title` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分享标题',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '心得内容',
  `user_id` bigint(20) NOT NULL COMMENT '分享人编号',
  `user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分享人',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ENABLED' COMMENT '状态',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_fenxiang_investment`(`investment_id`) USING BTREE,
  INDEX `fk_fenxiang_user`(`user_id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'fenxiang分享表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fenxiang
-- ----------------------------
INSERT INTO `fenxiang` VALUES (1, 1, 1, '海洋化工', '辛媛', 5000.00, 45.00, '海洋化工收益清晰', '投入门槛适中，收益预期展示清楚。', 1, '辛媛', 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `fenxiang` VALUES (2, 2, 2, '再生能源', '辛媛', 3000.00, 41.42, '近期使用最好的产品', 'num1，赎回流程简单，适合稳健投资。', 1, '辛媛', 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `fenxiang` VALUES (3, 3, 3, '货币优选', '李思', 8000.00, 15.12, '现金管理方便', '短期资金放这里比较灵活。', 2, '李思', 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `fenxiang` VALUES (4, 4, 4, '稳健添利', '王武', 6000.00, 30.58, '稳健型体验', '收益不高但波动小。', 3, '王武', 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `fenxiang` VALUES (5, 5, 5, '成长精选', '赵柳', 4000.00, 102.58, '成长产品观察', '适合长期跟踪收益。', 4, '赵柳', 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `fenxiang` VALUES (6, 6, 6, '安心月享', '孙琪', 10000.00, 17.26, '月度理财', '短期管理闲置资金。', 5, '孙琪', 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `fenxiang` VALUES (7, 7, 7, '蓝筹计划', '周芭', 7000.00, 158.79, '蓝筹组合分享', '适合有一定风险承受能力的用户。', 6, '周芭', 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `fenxiang` VALUES (8, 8, 8, '教育储蓄', '吴玖', 12000.00, 336.00, '教育储蓄计划', '周期较长，适合提前规划。', 7, '吴玖', 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `fenxiang` VALUES (9, 9, 9, '医疗健康', '郑诗', 4500.00, 109.36, '医疗主题产品', '用于主题投资观察。', 8, '郑诗', 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `fenxiang` VALUES (10, 10, 10, '绿色债券', '陈一', 5500.00, 69.62, '绿色债券体验', '到期赎回收益明确。', 9, '陈一', 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `fenxiang` VALUES (13, 29, 9, '医疗健康', '测试员1', 1000.00, 0.13, '很好的产品', '收益展示', 1, '测试员1', 'ENABLED', '2026-05-13 11:58:08', '2026-05-13 11:58:08');

-- ----------------------------
-- Table structure for jizhang_fenlei
-- ----------------------------
DROP TABLE IF EXISTS `jizhang_fenlei`;
CREATE TABLE `jizhang_fenlei`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型：INCOME收入，EXPENSE支出',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ENABLED' COMMENT '状态',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category_type`(`type`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '记账分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of jizhang_fenlei
-- ----------------------------
INSERT INTO `jizhang_fenlei` VALUES (1, '工资', 'INCOME', 'ENABLED', '固定工资收入', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `jizhang_fenlei` VALUES (2, '生活费', 'INCOME', 'ENABLED', '家庭或个人生活费收入', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `jizhang_fenlei` VALUES (3, '收红包', 'INCOME', 'ENABLED', '红包收入', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `jizhang_fenlei` VALUES (4, '外快', 'INCOME', 'ENABLED', '兼职外快', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `jizhang_fenlei` VALUES (5, '股票基金', 'INCOME', 'ENABLED', '投资收益类收入', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `jizhang_fenlei` VALUES (6, '其他收入', 'INCOME', 'ENABLED', '其他收入', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `jizhang_fenlei` VALUES (7, '三餐', 'EXPENSE', 'ENABLED', '日常餐饮支出', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `jizhang_fenlei` VALUES (8, '话费', 'EXPENSE', 'ENABLED', '通讯支出', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `jizhang_fenlei` VALUES (9, '学习', 'EXPENSE', 'ENABLED', '学习培训支出', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `jizhang_fenlei` VALUES (10, '日用品', 'EXPENSE', 'ENABLED', '日用品支出', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `jizhang_fenlei` VALUES (11, '住房', 'EXPENSE', 'ENABLED', '租房房贷等支出', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `jizhang_fenlei` VALUES (12, '医疗', 'EXPENSE', 'ENABLED', '医疗健康支出', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `jizhang_fenlei` VALUES (13, '交通', 'EXPENSE', 'ENABLED', '通勤出行支出', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `jizhang_fenlei` VALUES (14, '水电煤气', 'EXPENSE', 'ENABLED', '家庭能源支出', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `jizhang_fenlei` VALUES (15, '娱乐', 'EXPENSE', 'ENABLED', '娱乐消费支出', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `jizhang_fenlei` VALUES (16, '其他支出', 'EXPENSE', 'ENABLED', '其他支出', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `jizhang_fenlei` VALUES (17, '理财收益', 'INCOME', 'ENABLED', '理财的收益', '2026-05-13 11:06:04', '2026-05-13 11:06:04');

-- ----------------------------
-- Table structure for licaichanpin
-- ----------------------------
DROP TABLE IF EXISTS `licaichanpin`;
CREATE TABLE `licaichanpin`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `product_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '名称',
  `type` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型',
  `annual_rate` decimal(8, 4) NOT NULL COMMENT '年收益率，单位百分比',
  `publisher` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发布者',
  `min_amount` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '最小投资金额',
  `max_amount` decimal(12, 2) NOT NULL DEFAULT 100000.00 COMMENT '最大投资金额',
  `term_days` int(11) NULL DEFAULT 30 COMMENT '理财天数',
  `risk_level` varchar(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '风险等级',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ENABLED' COMMENT '状态',
  `remark` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `product_code`(`product_code`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'licaichanpin理财产品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of licaichanpin
-- ----------------------------
INSERT INTO `licaichanpin` VALUES (1, 'LCB202605120001', '海洋化工', '基金', 3.6500, '管理员', 1000.00, 50000.00, 90, '中低风险', 'ENABLED', '测试用理财产品', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `licaichanpin` VALUES (2, 'ZQ202605120002', '再生能源', '债券', 4.2000, '管理员', 500.00, 30000.00, 120, '中风险', 'ENABLED', '新能源主题产品', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `licaichanpin` VALUES (3, 'HB202605120003', '货币优选', '货币', 2.3000, '管理员', 100.00, 100000.00, 30, '低风险', 'ENABLED', '现金管理产品', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `licaichanpin` VALUES (4, 'ZQ202605120004', '稳健添利', '债券', 3.1000, '管理员', 1000.00, 80000.00, 60, '低风险', 'ENABLED', '稳健型产品', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `licaichanpin` VALUES (5, 'JJ202605120005', '成长精选', '基金', 5.2000, '管理员', 1000.00, 40000.00, 180, '中高风险', 'ENABLED', '成长行业主题', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `licaichanpin` VALUES (6, 'HB202605120006', '安心月享', '货币', 2.1000, '管理员', 100.00, 50000.00, 30, '低风险', 'ENABLED', '短期理财', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `licaichanpin` VALUES (7, 'JJ202605120007', '蓝筹计划', '基金', 4.6000, '管理员', 2000.00, 60000.00, 180, '中风险', 'ENABLED', '蓝筹组合', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `licaichanpin` VALUES (8, 'CX202605120008', '教育储蓄', '储蓄', 2.8000, '管理员', 500.00, 20000.00, 365, '低风险', 'ENABLED', '长期储蓄', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `licaichanpin` VALUES (9, 'JJ202605120009', '医疗健康', '基金', 4.9000, '管理员', 1000.00, 50000.00, 180, '中风险', 'ENABLED', '医疗主题', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `licaichanpin` VALUES (10, 'ZQ202605120010', '绿色债券', '债券', 3.8500, '管理员', 1000.00, 70000.00, 120, '中低风险', 'ENABLED', '绿色产业债券', '2026-05-11 18:36:06', '2026-05-11 18:36:06');

-- ----------------------------
-- Table structure for share_comment
-- ----------------------------
DROP TABLE IF EXISTS `share_comment`;
CREATE TABLE `share_comment`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `share_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of share_comment
-- ----------------------------
INSERT INTO `share_comment` VALUES (1, '2026-05-11 20:21:21.973859', '2026-05-11 20:21:21.973859', '太牛逼了', 11, 1, '辛媛');
INSERT INTO `share_comment` VALUES (2, '2026-05-11 20:24:59.420494', '2026-05-11 20:24:59.420494', '自己评论自己', 11, 1, '辛媛');
INSERT INTO `share_comment` VALUES (3, '2026-05-11 20:25:13.358877', '2026-05-11 20:25:13.358877', '可以的', 10, 1, '辛媛');
INSERT INTO `share_comment` VALUES (4, '2026-05-11 20:25:49.783670', '2026-05-11 20:25:49.783670', '1', 11, 1, '系统管理员');
INSERT INTO `share_comment` VALUES (5, '2026-05-12 17:26:24.266836', '2026-05-12 17:26:24.266836', 'sb', 10, 1, '辛媛');

-- ----------------------------
-- Table structure for share_report
-- ----------------------------
DROP TABLE IF EXISTS `share_report`;
CREATE TABLE `share_report`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `detail` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `reason` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `reporter_id` bigint(20) NOT NULL,
  `reporter_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `result` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `share_id` bigint(20) NOT NULL,
  `status` enum('PENDING','PROCESSED','REJECTED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of share_report
-- ----------------------------
INSERT INTO `share_report` VALUES (1, '2026-05-11 20:26:46.317946', '2026-05-11 20:26:46.317946', '他说他父亲是区长', '虚假内容', 1, '辛媛', NULL, 11, 'PENDING');
INSERT INTO `share_report` VALUES (2, '2026-05-12 17:26:32.689306', '2026-05-12 22:28:42.666279', 'sb', '虚假内容', 1, '辛媛', '已下架', 10, 'PROCESSED');
INSERT INTO `share_report` VALUES (3, '2026-05-12 22:29:16.508010', '2026-05-12 22:29:32.760101', '', '虚假内容', 1, '辛媛', '下架', 12, 'PROCESSED');

-- ----------------------------
-- Table structure for shuhui_jilu
-- ----------------------------
DROP TABLE IF EXISTS `shuhui_jilu`;
CREATE TABLE `shuhui_jilu`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `investment_id` bigint(20) NOT NULL COMMENT '投资记录编号',
  `product_id` bigint(20) NOT NULL COMMENT '理财编号',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '名称',
  `amount` decimal(12, 2) NOT NULL COMMENT '投入金额',
  `actual_days` int(11) NOT NULL COMMENT '实际理财天数',
  `income` decimal(12, 2) NOT NULL COMMENT '赎回收益',
  `redeemer_id` bigint(20) NOT NULL COMMENT '赎回人编号',
  `redeemer_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '赎回人',
  `redeem_date` date NOT NULL COMMENT '赎回日期',
  `remark` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_shuhui_investment`(`investment_id`) USING BTREE,
  INDEX `fk_shuhui_user`(`redeemer_id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '赎回记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shuhui_jilu
-- ----------------------------
INSERT INTO `shuhui_jilu` VALUES (1, 2, 2, '再生能源', 3000.00, 120, 41.42, 1, '普通测试员', '2026-05-01', '按期赎回', '2026-05-11 18:36:06', '2026-05-13 15:17:50');
INSERT INTO `shuhui_jilu` VALUES (2, 4, 4, '稳健添利', 6000.00, 60, 30.58, 3, '王武', '2026-04-02', '按期赎回', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `shuhui_jilu` VALUES (3, 6, 6, '安心月享', 10000.00, 30, 17.26, 5, '孙琪', '2026-05-01', '短期赎回', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `shuhui_jilu` VALUES (4, 10, 10, '绿色债券', 5500.00, 120, 69.62, 9, '陈一', '2026-05-15', '到期赎回', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `shuhui_jilu` VALUES (5, 2, 2, '再生能源', 1200.00, 90, 12.43, 1, '普通测试员', '2026-04-01', '演示赎回记录', '2026-05-11 18:36:06', '2026-05-13 15:17:50');
INSERT INTO `shuhui_jilu` VALUES (6, 4, 4, '稳健添利', 2200.00, 60, 11.22, 3, '王武', '2026-04-05', '演示赎回记录', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `shuhui_jilu` VALUES (7, 6, 6, '安心月享', 2600.00, 30, 4.49, 5, '孙琪', '2026-04-25', '演示赎回记录', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `shuhui_jilu` VALUES (8, 10, 10, '绿色债券', 3200.00, 120, 40.50, 9, '陈一', '2026-05-02', '演示赎回记录', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `shuhui_jilu` VALUES (9, 2, 2, '再生能源', 1800.00, 100, 20.71, 1, '普通测试员', '2026-04-20', '演示赎回记录', '2026-05-11 18:36:06', '2026-05-13 15:17:50');
INSERT INTO `shuhui_jilu` VALUES (10, 4, 4, '稳健添利', 3600.00, 45, 13.76, 3, '王武', '2026-03-20', '演示赎回记录', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `shuhui_jilu` VALUES (11, 14, 8, '教育储蓄', 1000.00, 1, 0.08, 1, '普通测试员', '2026-05-12', '演示', '2026-05-12 21:29:51', '2026-05-13 15:17:50');
INSERT INTO `shuhui_jilu` VALUES (17, 23, 10, '绿色债券', 2000.00, 1, 0.21, 1, '普通测试员', '2026-05-13', '测试收益同步', '2026-05-13 11:10:50', '2026-05-13 15:17:50');
INSERT INTO `shuhui_jilu` VALUES (19, 25, 9, '医疗健康', 1000.00, 1, 0.13, 1, '普通测试员', '2026-05-13', '测试收益与财务记录同步', '2026-05-13 11:30:43', '2026-05-13 15:17:50');
INSERT INTO `shuhui_jilu` VALUES (20, 26, 9, '医疗健康', 1000.00, 1, 0.13, 1, '普通测试员', '2026-05-13', '测试赎回收益同步', '2026-05-13 11:36:06', '2026-05-13 15:17:50');
INSERT INTO `shuhui_jilu` VALUES (21, 27, 7, '蓝筹计划', 2000.00, 1, 0.25, 1, '普通测试员', '2026-05-13', '测试同步', '2026-05-13 11:48:57', '2026-05-13 15:17:50');
INSERT INTO `shuhui_jilu` VALUES (22, 28, 9, '医疗健康', 1000.00, 1, 0.13, 1, '普通测试员', '2026-05-13', '测试同步财务', '2026-05-13 11:53:14', '2026-05-13 15:17:50');
INSERT INTO `shuhui_jilu` VALUES (23, 29, 9, '医疗健康', 1000.00, 1, 0.13, 1, '普通测试员', '2026-05-13', '测试同步', '2026-05-13 11:54:03', '2026-05-13 15:17:50');

-- ----------------------------
-- Table structure for touzi_jilu
-- ----------------------------
DROP TABLE IF EXISTS `touzi_jilu`;
CREATE TABLE `touzi_jilu`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `product_id` bigint(20) NOT NULL COMMENT '理财编号',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '名称',
  `product_type` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型',
  `annual_rate` decimal(8, 4) NOT NULL COMMENT '年收益率',
  `amount` decimal(12, 2) NOT NULL COMMENT '投入金额',
  `investor_id` bigint(20) NOT NULL COMMENT '理财人编号',
  `investor_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '理财人',
  `start_date` date NOT NULL COMMENT '开始日期',
  `expected_redeem_date` date NOT NULL COMMENT '预计收益时间',
  `actual_days` int(11) NULL DEFAULT 0 COMMENT '实际理财天数',
  `expected_income` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '预计收益',
  `actual_income` decimal(12, 2) NULL DEFAULT NULL COMMENT '实际收益',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'HOLDING' COMMENT '状态：HOLDING持有中，REDEEMED已赎回',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  `product_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_touzi_user`(`investor_id`) USING BTREE,
  INDEX `fk_touzi_product`(`product_id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '投资记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of touzi_jilu
-- ----------------------------
INSERT INTO `touzi_jilu` VALUES (1, 1, '海洋化工', '基金', 3.6500, 5000.00, 1, '普通测试员', '2026-04-01', '2026-06-30', 90, 45.00, NULL, 'HOLDING', '2026-05-11 18:36:06', '2026-05-13 15:17:50', 'LCB202605120001');
INSERT INTO `touzi_jilu` VALUES (2, 2, '再生能源', '债券', 4.2000, 3000.00, 1, '普通测试员', '2026-01-01', '2026-05-01', 120, 41.42, 41.42, 'REDEEMED', '2026-05-11 18:36:06', '2026-05-13 15:17:50', 'ZQ202605120002');
INSERT INTO `touzi_jilu` VALUES (3, 3, '货币优选', '货币', 2.3000, 8000.00, 2, '李思', '2026-04-10', '2026-05-10', 30, 15.12, NULL, 'HOLDING', '2026-05-11 18:36:06', '2026-05-11 18:36:06', 'HB202605120003');
INSERT INTO `touzi_jilu` VALUES (4, 4, '稳健添利', '债券', 3.1000, 6000.00, 3, '王武', '2026-02-01', '2026-04-02', 60, 30.58, 30.58, 'REDEEMED', '2026-05-11 18:36:06', '2026-05-11 18:36:06', 'ZQ202605120004');
INSERT INTO `touzi_jilu` VALUES (5, 5, '成长精选', '基金', 5.2000, 4000.00, 4, '赵柳', '2026-03-01', '2026-08-28', 180, 102.58, NULL, 'HOLDING', '2026-05-11 18:36:06', '2026-05-11 18:36:06', 'JJ202605120005');
INSERT INTO `touzi_jilu` VALUES (6, 6, '安心月享', '货币', 2.1000, 10000.00, 5, '孙琪', '2026-04-01', '2026-05-01', 30, 17.26, 17.26, 'REDEEMED', '2026-05-11 18:36:06', '2026-05-11 18:36:06', 'HB202605120006');
INSERT INTO `touzi_jilu` VALUES (7, 7, '蓝筹计划', '基金', 4.6000, 7000.00, 6, '周芭', '2026-03-15', '2026-09-11', 180, 158.79, NULL, 'HOLDING', '2026-05-11 18:36:06', '2026-05-11 18:36:06', 'JJ202605120007');
INSERT INTO `touzi_jilu` VALUES (8, 8, '教育储蓄', '储蓄', 2.8000, 12000.00, 7, '吴玖', '2026-01-01', '2027-01-01', 365, 336.00, NULL, 'HOLDING', '2026-05-11 18:36:06', '2026-05-11 18:36:06', 'CX202605120008');
INSERT INTO `touzi_jilu` VALUES (9, 9, '医疗健康', '基金', 4.9000, 4500.00, 8, '郑诗', '2026-02-01', '2026-08-01', 181, 109.36, NULL, 'HOLDING', '2026-05-11 18:36:06', '2026-05-11 18:36:06', 'JJ202605120009');
INSERT INTO `touzi_jilu` VALUES (10, 10, '绿色债券', '债券', 3.8500, 5500.00, 9, '陈一', '2026-01-15', '2026-05-15', 120, 69.62, 69.62, 'REDEEMED', '2026-05-11 18:36:06', '2026-05-11 18:36:06', 'ZQ202605120010');
INSERT INTO `touzi_jilu` VALUES (11, 10, '绿色债券', '债券', 3.8500, 1000.00, 1, '普通测试员', '2026-05-12', '2026-05-31', 19, 2.00, NULL, 'HOLDING', '2026-05-12 20:11:22', '2026-05-13 15:17:50', 'ZQ202605120010');
INSERT INTO `touzi_jilu` VALUES (18, 6, '安心月享', '货币', 2.1000, 1000.00, 1, '普通测试员', '2026-05-13', '2026-06-11', 1, 1.67, 0.06, 'REDEEMED', '2026-05-13 10:52:54', '2026-05-13 15:17:50', 'HB202605120006');
INSERT INTO `touzi_jilu` VALUES (14, 8, '教育储蓄', '储蓄', 2.8000, 1000.00, 1, '普通测试员', '2026-05-12', '2027-05-12', 1, 28.00, 0.08, 'REDEEMED', '2026-05-12 20:14:43', '2026-05-13 15:17:50', 'CX202605120008');
INSERT INTO `touzi_jilu` VALUES (17, 5, '成长精选', '基金', 5.2000, 1000.00, 1, '普通测试员', '2026-05-12', '2026-11-07', 179, 25.50, NULL, 'HOLDING', '2026-05-12 23:18:05', '2026-05-13 15:17:50', 'JJ202605120005');
INSERT INTO `touzi_jilu` VALUES (19, 5, '成长精选', '基金', 5.2000, 1000.00, 1, '普通测试员', '2026-05-13', '2026-11-08', 1, 25.50, 0.14, 'REDEEMED', '2026-05-13 10:54:47', '2026-05-13 15:17:50', 'JJ202605120005');
INSERT INTO `touzi_jilu` VALUES (29, 9, '医疗健康', '基金', 4.9000, 1000.00, 1, '普通测试员', '2026-05-13', '2026-11-08', 1, 24.03, 0.13, 'REDEEMED', '2026-05-13 11:53:56', '2026-05-13 15:17:50', 'JJ202605120009');

-- ----------------------------
-- Table structure for yonghu
-- ----------------------------
DROP TABLE IF EXISTS `yonghu`;
CREATE TABLE `yonghu`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户密码，支持明文演示或BCrypt密文',
  `real_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '姓名',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系方式',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '家庭住址',
  `avatar_url` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像地址',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED启用，DISABLED停用',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'yonghu用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of yonghu
-- ----------------------------
INSERT INTO `yonghu` VALUES (1, 'test', 'xinyuan1', '普通测试员', '13900000001', '杭州市西湖区文三路1号', '/uploads/avatars/20260513/f241ac94-f0d4-4126-8de1-b034d26c730e.jpg', 'ENABLED', '2026-05-11 18:36:06', '2026-05-13 15:17:50');
INSERT INTO `yonghu` VALUES (2, 'lisi', '$2a$10$GjDPuqlWZmfnznJj8aWF8OuDyd5kCH13Z06T2mIu/3bbWxjqV8jty', '李思', '13900000002', '上海市浦东新区张江路2号', NULL, 'ENABLED', '2026-05-11 18:36:06', '2026-05-12 22:03:12');
INSERT INTO `yonghu` VALUES (3, 'wangwu', '123456', '王武', '13900000003', '南京市鼓楼区中山路3号', NULL, 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `yonghu` VALUES (4, 'zhaoliu', '123456', '赵柳', '13900000004', '北京市海淀区学院路4号', NULL, 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `yonghu` VALUES (5, 'sunqi', '123456', '孙琪', '13900000005', '成都市锦江区春熙路5号', NULL, 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `yonghu` VALUES (6, 'zhouba', '123456', '周芭', '13900000006', '武汉市洪山区珞喻路6号', NULL, 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `yonghu` VALUES (7, 'wujiu', '123456', '吴玖', '13900000007', '广州市天河区体育西路7号', NULL, 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `yonghu` VALUES (8, 'zhengshi', '123456', '郑诗', '13900000008', '深圳市南山区科技园8号', NULL, 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `yonghu` VALUES (9, 'chenyi', '123456', '陈一', '13900000009', '苏州市姑苏区人民路9号', NULL, 'ENABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');
INSERT INTO `yonghu` VALUES (10, 'liangliang', '123456', '梁亮', '13900000010', '长沙市岳麓区麓山路10号', NULL, 'DISABLED', '2026-05-11 18:36:06', '2026-05-11 18:36:06');

SET FOREIGN_KEY_CHECKS = 1;
