-- ============================================
-- 3D 打印科创会管理系统 v2 - 数据库设计文档
-- ============================================
-- 这是 **schema only** 版本（无数据、无 DROP TABLE），用作设计参考。
--
-- 完整可执行的脚本（含数据 + DROP TABLE）见：
--   spring-boot-backend/docs/SQL/init-bcrypt.sql
--
-- 11 张表（按外键依赖顺序）：
--   1. member            成员
--   2. printer           打印机
--   3. print_task        打印任务
--   4. material_log      耗材流水
--   5. artwork           作品库
--   6. project           项目
--   7. project_member    项目成员（多对多）
--   8. project_progress  项目阶段
--   9. project_file      项目文件
--  10. maintenance       打印机维护记录
--  11. system_log        系统审计日志
--
-- 字符集：utf8mb4
-- 排序：utf8mb4_unicode_ci
-- 引擎：InnoDB
-- 数据库：print_club_db
-- ============================================

CREATE DATABASE IF NOT EXISTS `print_club_db`
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `print_club_db`;

-- 删表（按外键依赖倒序）
DROP TABLE IF EXISTS `system_log`;
DROP TABLE IF EXISTS `maintenance`;
DROP TABLE IF EXISTS `project_file`;
DROP TABLE IF EXISTS `project_progress`;
DROP TABLE IF EXISTS `project_member`;
DROP TABLE IF EXISTS `project`;
DROP TABLE IF EXISTS `artwork`;
DROP TABLE IF EXISTS `material_log`;
DROP TABLE IF EXISTS `print_task`;
DROP TABLE IF EXISTS `printer`;
DROP TABLE IF EXISTS `member`;

-- 1. 成员表
CREATE TABLE `member` (
    `student_id` VARCHAR(20) NOT NULL COMMENT '学号',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `password` VARCHAR(100) NOT NULL COMMENT '登录密码(BCrypt)',
    `role` TINYINT NOT NULL DEFAULT 4 COMMENT '角色：1社长 2技术骨干 3普通社员 4新成员',
    `skill_level` TINYINT NOT NULL DEFAULT 0 COMMENT '技能等级：0未入门 1Tinkercad 2Fusion360 3Blender 4调机熟练',
    `join_date` DATE NOT NULL COMMENT '入社日期',
    `total_prints` INT NOT NULL DEFAULT 0 COMMENT '累计打印次数',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(50) DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 2退出',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`student_id`),
    INDEX `idx_role` (`role`),
    INDEX `idx_skill` (`skill_level`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成员表';

-- 2. 打印机表
CREATE TABLE `printer` (
    `printer_id` VARCHAR(10) NOT NULL COMMENT '打印机编号',
    `model` VARCHAR(50) NOT NULL COMMENT '型号',
    `brand` VARCHAR(30) DEFAULT NULL COMMENT '品牌',
    `purchase_date` DATE DEFAULT NULL COMMENT '购买日期',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 2维修中 3报废',
    `total_print_hours` DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT '累计打印时长(小时)',
    `location` VARCHAR(50) DEFAULT NULL COMMENT '放置位置',
    `nozzle_size` DECIMAL(2,1) DEFAULT 0.4 COMMENT '喷嘴尺寸(mm)',
    `build_volume` VARCHAR(30) DEFAULT NULL COMMENT '成型尺寸',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`printer_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打印机表';

-- 3. 打印任务表
CREATE TABLE `print_task` (
    `task_id` VARCHAR(20) NOT NULL COMMENT '任务编号',
    `applicant_id` VARCHAR(20) NOT NULL COMMENT '申请人',
    `approver_id` VARCHAR(20) DEFAULT NULL COMMENT '审批人',
    `printer_id` VARCHAR(10) DEFAULT NULL COMMENT '分配的打印机',
    `project_id` INT DEFAULT NULL COMMENT '关联项目',
    `title` VARCHAR(100) NOT NULL COMMENT '任务标题',
    `model_name` VARCHAR(100) NOT NULL COMMENT '模型名称',
    `stl_file_path` VARCHAR(255) NOT NULL COMMENT 'STL文件路径',
    `material_type` VARCHAR(20) NOT NULL COMMENT '材料类型',
    `color` VARCHAR(20) DEFAULT NULL COMMENT '颜色',
    `layer_height` DECIMAL(3,1) NOT NULL DEFAULT 0.2 COMMENT '层高',
    `infill_rate` TINYINT NOT NULL DEFAULT 20 COMMENT '填充率',
    `need_support` TINYINT NOT NULL DEFAULT 0 COMMENT '是否需要支撑',
    `priority` TINYINT NOT NULL DEFAULT 2 COMMENT '优先级：1紧急 2普通 3低优',
    `est_weight` DECIMAL(6,2) DEFAULT NULL COMMENT '预估重量(g)',
    `est_time` INT DEFAULT NULL COMMENT '预估耗时(分钟)',
    `apply_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `approve_time` DATETIME DEFAULT NULL COMMENT '审批时间',
    `approve_comment` VARCHAR(255) DEFAULT NULL COMMENT '审批意见',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待审批 1已通过 2已驳回 3排队中 4打印中 5已完成 6已取消',
    `actual_weight` DECIMAL(6,2) DEFAULT NULL COMMENT '实际耗材(g)',
    `actual_time` INT DEFAULT NULL COMMENT '实际耗时(分钟)',
    `finish_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `quality_score` TINYINT DEFAULT NULL COMMENT '质量评分1-5',
    `pickup_time` DATETIME DEFAULT NULL COMMENT '取件时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`task_id`),
    INDEX `idx_applicant` (`applicant_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_printer` (`printer_id`),
    INDEX `idx_project` (`project_id`),
    INDEX `idx_apply_time` (`apply_time`),
    INDEX `idx_priority_status` (`priority`, `status`),
    FOREIGN KEY (`applicant_id`) REFERENCES `member`(`student_id`),
    FOREIGN KEY (`approver_id`) REFERENCES `member`(`student_id`),
    FOREIGN KEY (`printer_id`) REFERENCES `printer`(`printer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打印任务表';

-- 4. 耗材记录表
CREATE TABLE `material_log` (
    `log_id` INT NOT NULL AUTO_INCREMENT COMMENT '记录编号',
    `material_type` VARCHAR(20) NOT NULL COMMENT '材料类型',
    `color` VARCHAR(20) NOT NULL COMMENT '颜色',
    `weight_change` DECIMAL(8,2) NOT NULL COMMENT '重量变化(g)',
    `balance` DECIMAL(8,2) NOT NULL COMMENT '变动后库存',
    `operation_type` TINYINT NOT NULL COMMENT '1入库 2消耗',
    `related_task_id` VARCHAR(20) DEFAULT NULL COMMENT '关联任务',
    `operator_id` VARCHAR(20) NOT NULL COMMENT '操作人',
    `remark` VARCHAR(255) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`log_id`),
    INDEX `idx_material_color` (`material_type`, `color`),
    INDEX `idx_operator` (`operator_id`),
    INDEX `idx_related_task` (`related_task_id`),
    FOREIGN KEY (`related_task_id`) REFERENCES `print_task`(`task_id`),
    FOREIGN KEY (`operator_id`) REFERENCES `member`(`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='耗材记录表';

-- 5. 作品库表
CREATE TABLE `artwork` (
    `artwork_id` INT NOT NULL AUTO_INCREMENT COMMENT '作品编号',
    `task_id` VARCHAR(20) NOT NULL COMMENT '关联打印任务',
    `author_id` VARCHAR(20) NOT NULL COMMENT '作者',
    `artwork_name` VARCHAR(100) NOT NULL COMMENT '作品名称',
    `preview_image` VARCHAR(255) DEFAULT NULL COMMENT '预览图',
    `finish_photos` VARCHAR(500) DEFAULT NULL COMMENT '成品照片',
    `experience` TEXT DEFAULT NULL COMMENT '心得总结',
    `is_recommended` TINYINT DEFAULT 0 COMMENT '是否推荐案例',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`artwork_id`),
    INDEX `idx_author` (`author_id`),
    INDEX `idx_recommended` (`is_recommended`),
    FOREIGN KEY (`task_id`) REFERENCES `print_task`(`task_id`),
    FOREIGN KEY (`author_id`) REFERENCES `member`(`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作品库表';

-- 6. 项目表
CREATE TABLE `project` (
    `project_id` INT NOT NULL AUTO_INCREMENT COMMENT '项目编号',
    `project_name` VARCHAR(100) NOT NULL COMMENT '项目名称',
    `project_type` TINYINT NOT NULL COMMENT '1作品创作 2竞赛备赛 3定制订单 4社团活动',
    `leader_id` VARCHAR(20) NOT NULL COMMENT '项目负责人',
    `start_date` DATE NOT NULL COMMENT '开始日期',
    `end_date` DATE DEFAULT NULL COMMENT '预计结束日期',
    `actual_end_date` DATE DEFAULT NULL COMMENT '实际结束日期',
    `budget` DECIMAL(8,2) DEFAULT 0 COMMENT '预算(元)',
    `actual_cost` DECIMAL(8,2) DEFAULT 0 COMMENT '实际花费(元)',
    `description` TEXT DEFAULT NULL COMMENT '项目描述',
    `deliverables` VARCHAR(500) DEFAULT NULL COMMENT '交付物清单',
    `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0筹备中 1进行中 2已完成 3已取消',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`project_id`),
    INDEX `idx_leader` (`leader_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_type` (`project_type`),
    FOREIGN KEY (`leader_id`) REFERENCES `member`(`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- 7. 项目成员表
CREATE TABLE `project_member` (
    `pm_id` INT NOT NULL AUTO_INCREMENT COMMENT '记录编号',
    `project_id` INT NOT NULL COMMENT '项目',
    `member_id` VARCHAR(20) NOT NULL COMMENT '成员',
    `role_in_project` TINYINT NOT NULL DEFAULT 3 COMMENT '1负责人 2核心成员 3参与成员',
    `contribution` VARCHAR(255) DEFAULT NULL COMMENT '贡献描述',
    `join_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `status` TINYINT DEFAULT 1 COMMENT '1进行中 2已退出 3已完成',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`pm_id`),
    UNIQUE KEY `uk_project_member` (`project_id`, `member_id`),
    INDEX `idx_member` (`member_id`),
    FOREIGN KEY (`project_id`) REFERENCES `project`(`project_id`),
    FOREIGN KEY (`member_id`) REFERENCES `member`(`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目成员表';

-- 8. 项目进度表
CREATE TABLE `project_progress` (
    `progress_id` INT NOT NULL AUTO_INCREMENT COMMENT '记录编号',
    `project_id` INT NOT NULL COMMENT '项目',
    `stage_name` VARCHAR(50) NOT NULL COMMENT '阶段名称',
    `stage_order` TINYINT NOT NULL COMMENT '阶段顺序',
    `description` TEXT DEFAULT NULL COMMENT '阶段描述',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0未开始 1进行中 2已完成',
    `responsible_id` VARCHAR(20) DEFAULT NULL COMMENT '阶段负责人',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`progress_id`),
    INDEX `idx_project` (`project_id`),
    INDEX `idx_responsible` (`responsible_id`),
    FOREIGN KEY (`project_id`) REFERENCES `project`(`project_id`),
    FOREIGN KEY (`responsible_id`) REFERENCES `member`(`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目进度表';

-- 9. 项目文件表
CREATE TABLE `project_file` (
    `file_id` INT NOT NULL AUTO_INCREMENT COMMENT '文件编号',
    `project_id` INT NOT NULL COMMENT '项目',
    `file_name` VARCHAR(100) NOT NULL COMMENT '文件名',
    `file_path` VARCHAR(255) NOT NULL COMMENT '存储路径',
    `file_type` TINYINT NOT NULL COMMENT '1设计图 2STL文件 3照片 4文档 5其他',
    `file_size` BIGINT DEFAULT NULL COMMENT '文件大小(字节)',
    `uploader_id` VARCHAR(20) NOT NULL COMMENT '上传人',
    `upload_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`file_id`),
    INDEX `idx_project` (`project_id`),
    FOREIGN KEY (`project_id`) REFERENCES `project`(`project_id`),
    FOREIGN KEY (`uploader_id`) REFERENCES `member`(`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目文件表';

-- 10. 维护记录表
CREATE TABLE `maintenance` (
    `maint_id` INT NOT NULL AUTO_INCREMENT COMMENT '记录编号',
    `printer_id` VARCHAR(10) NOT NULL COMMENT '打印机',
    `maint_type` TINYINT NOT NULL COMMENT '1保养 2维修 3换件 4校准',
    `content` TEXT NOT NULL COMMENT '维护内容',
    `maintainer_id` VARCHAR(20) NOT NULL COMMENT '维护人',
    `maint_time` DATETIME NOT NULL COMMENT '维护时间',
    `next_maint_date` DATE DEFAULT NULL COMMENT '下次保养日期',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`maint_id`),
    INDEX `idx_printer` (`printer_id`),
    INDEX `idx_maintainer` (`maintainer_id`),
    FOREIGN KEY (`printer_id`) REFERENCES `printer`(`printer_id`),
    FOREIGN KEY (`maintainer_id`) REFERENCES `member`(`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维护记录表';

-- 11. 系统日志表
CREATE TABLE `system_log` (
    `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志编号',
    `user_id` VARCHAR(20) DEFAULT NULL COMMENT '操作人',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
    `operation` VARCHAR(50) NOT NULL COMMENT '操作类型',
    `target_type` VARCHAR(30) DEFAULT NULL COMMENT '对象类型',
    `target_id` VARCHAR(50) DEFAULT NULL COMMENT '对象ID',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `ip_address` VARCHAR(40) DEFAULT NULL COMMENT 'IP地址',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`log_id`),
    INDEX `idx_user` (`user_id`),
    INDEX `idx_time` (`create_time`),
    INDEX `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志表';
