/*
 Navicat Premium Dump SQL

 Source Server         : LOCAL_MYSQL
 Source Server Type    : MySQL
 Source Server Version : 80023 (8.0.23)
 Source Host           : localhost:3306
 Source Schema         : pk

 Target Server Type    : MySQL
 Target Server Version : 80023 (8.0.23)
 File Encoding         : 65001

 Date: 12/08/2024 11:47:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for pk_question
-- ----------------------------
DROP TABLE IF EXISTS `pk_question`;
CREATE TABLE `pk_question`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT,
  `type` int UNSIGNED NOT NULL DEFAULT 1,
  `score` int NOT NULL DEFAULT 1,
  `answer` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `true_answer` char(2) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `info` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `question` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `gmt_create` datetime NULL DEFAULT NULL,
  `gmt_modify` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 202 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `uid` int NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `mobile` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
  `avatar` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '头像',
  `gender` int NOT NULL DEFAULT 0 COMMENT '性别(0未知，1男，2女)',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态',
  `intro` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '这个人很懒，没留下什么' COMMENT '个性签名',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`uid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
