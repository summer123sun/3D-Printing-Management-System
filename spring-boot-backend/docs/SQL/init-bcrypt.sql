-- ============================================
-- 3D 打印科创会管理系统 v2 - 一键初始化脚本（BCrypt 版）
-- 密码统一为：123456（用 BCrypt 加密）
-- 生成时间：2026-06-25 16:50:09
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

-- ============================================
-- 模拟数据
-- ============================================


-- 成员数据（密码统一 123456，BCrypt 加密）
-- BCrypt hash: $2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby
-- 验证：BCrypt.checkpw('123456', hash) == true
INSERT INTO `member` (`student_id`, `name`, `password`, `role`, `skill_level`, `join_date`, `total_prints`, `phone`, `email`, `status`) VALUES
('2023010001', '张明', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 1, 4, '2025-03-01', 45, '13800138001', 'zhangming@example.com', 1),
('2023010002', '李强', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 2, 4, '2025-03-01', 38, '13800138002', 'liqiang@example.com', 1),
('2023010003', '王芳', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 2, 3, '2025-03-15', 32, '13800138003', 'wangfang@example.com', 1),
('2023010004', '赵磊', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 2, 3, '2025-03-15', 28, '13800138004', 'zhaolei@example.com', 1),
('2023010005', '刘洋', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 3, 2, '2025-09-01', 15, '13800138005', 'liuyang@example.com', 1),
('2023010006', '陈静', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 3, 2, '2025-09-01', 12, '13800138006', 'chenjing@example.com', 1),
('2023010007', '周杰', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 3, 2, '2025-09-01', 10, '13800138007', 'zhoujie@example.com', 1),
('2023010008', '吴昊', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 3, 1, '2025-09-01', 8, '13800138008', 'wuhao@example.com', 1),
('2023010009', '郑欣', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 3, 1, '2025-09-01', 6, '13800138009', 'zhengxin@example.com', 1),
('2023010010', '孙伟', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 3, 1, '2025-09-15', 5, '13800138010', 'sunwei@example.com', 1),
('2023010011', '杨帆', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 3, 1, '2025-09-15', 4, '13800138011', 'yangfan@example.com', 1),
('2023010012', '朱琳', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 3, 0, '2025-09-15', 3, '13800138012', 'zhulin@example.com', 1),
('2023010013', '黄磊', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 3, 0, '2025-10-01', 2, '13800138013', 'huanglei@example.com', 1),
('2023010014', '林峰', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 3, 0, '2025-10-01', 1, '13800138014', 'linfeng@example.com', 1),
('2023010015', '徐婷', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 3, 0, '2025-10-01', 0, '13800138015', 'xuting@example.com', 1),
('2024010001', '马超', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 4, 0, '2026-03-01', 0, '13800138016', 'machao@example.com', 1),
('2024010002', '高洁', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 4, 0, '2026-03-01', 0, '13800138017', 'gaojie@example.com', 1),
('2024010003', '罗军', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 4, 0, '2026-03-01', 0, '13800138018', 'luojun@example.com', 1),
('2024010004', '梁雨', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 4, 0, '2026-03-01', 0, '13800138019', 'liangyu@example.com', 1),
('2024010005', '宋雪', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 4, 0, '2026-03-15', 0, '13800138020', 'songxue@example.com', 1),
('2024010006', '唐明', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 4, 0, '2026-03-15', 0, '13800138021', 'tangming@example.com', 1),
('2024010007', '韩冰', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 4, 0, '2026-03-15', 0, '13800138022', 'hanbing@example.com', 1),
('2024010008', '冯磊', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 4, 0, '2026-04-01', 0, '13800138023', 'fenglei@example.com', 1),
('2024010009', '董洋', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 4, 0, '2026-04-01', 0, '13800138024', 'dongyang@example.com', 1),
('2024010010', '萧然', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 4, 0, '2026-04-01', 0, '13800138025', 'xiaoran@example.com', 1),
('2023010016', '曾伟', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 3, 2, '2025-09-01', 18, '13800138026', 'zengwei@example.com', 2),
('2023010017', '彭丽', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 3, 1, '2025-09-15', 7, '13800138027', 'pengli@example.com', 2),
('2023010018', '蒋波', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 2, 4, '2025-03-01', 42, '13800138028', 'jiangbo@example.com', 1),
('2023010019', '蔡琴', '$2b$10$QzaxxHKHlbj.kBg.ZkZYuOS2hV9rFgQzmg7QHVDxqQCadeO/eBaby', 3, 3, '2025-09-01', 22, '13800138029', 'caiqin@example.com', 1);


-- 打印机数据
INSERT INTO `printer` (`printer_id`, `model`, `brand`, `purchase_date`, `status`, `total_print_hours`, `location`, `nozzle_size`, `build_volume`) VALUES
('P-001', 'Ender-3 V2', 'Creality', '2025-03-01', 1, 856.50, '社团活动室A区', 0.4, '220x220x250'),
('P-002', 'Anycubic Kobra 2', 'Anycubic', '2025-09-01', 1, 234.00, '社团活动室B区', 0.4, '250x220x220'),
('P-003', 'Prusa i3 MK4', 'Prusa Research', '2026-01-15', 2, 45.50, '社团活动室A区', 0.4, '250x210x220');

-- 耗材初始入库
INSERT INTO `material_log` (`material_type`, `color`, `weight_change`, `balance`, `operation_type`, `operator_id`, `remark`) VALUES
('PLA', '白色', 1000.00, 1000.00, 1, '2023010001', '初始入库-白色PLA'),
('PLA', '黑色', 1000.00, 1000.00, 1, '2023010001', '初始入库-黑色PLA'),
('PLA', '红色', 500.00, 500.00, 1, '2023010001', '初始入库-红色PLA'),
('PLA', '蓝色', 500.00, 500.00, 1, '2023010001', '初始入库-蓝色PLA'),
('PLA', '金色', 500.00, 500.00, 1, '2023010001', '初始入库-金色PLA'),
('PETG', '透明', 500.00, 500.00, 1, '2023010001', '初始入库-透明PETG'),
('TPU', '黑色', 250.00, 250.00, 1, '2023010001', '初始入库-黑色TPU'),
('ABS', '白色', 300.00, 300.00, 1, '2023010001', '初始入库-白色ABS');

-- 打印任务数据（v2 加了 title/priority 字段）
INSERT INTO `print_task` (`task_id`, `applicant_id`, `approver_id`, `printer_id`, `project_id`, `title`, `model_name`, `stl_file_path`, `material_type`, `color`, `layer_height`, `infill_rate`, `need_support`, `priority`, `est_weight`, `est_time`, `apply_time`, `approve_time`, `approve_comment`, `status`, `actual_weight`, `actual_time`, `finish_time`, `quality_score`, `pickup_time`) VALUES
('P20250601-001', '2023010005', '2023010002', 'P-001', 1, '刘洋-机械键盘键帽', '机械键盘键帽', '/uploads/stl/keycap_v2.stl', 'PLA', '白色', 0.2, 30, 0, 2, 15.50, 120, '2026-06-22 09:00:00', '2025-06-01 09:30:00', '参数合理，排队打印', 5, 16.20, 135, '2025-06-01 14:00:00', 5, '2025-06-01 16:00:00'),
('P20250602-001', '2023010006', '2023010003', 'P-001', 1, '陈静-极简手机支架', '手机支架', '/uploads/stl/phone_stand.stl', 'PLA', '黑色', 0.2, 20, 0, 2, 45.00, 180, '2026-06-23 10:00:00', '2025-06-02 10:15:00', '通过', 5, 48.50, 195, '2025-06-02 16:00:00', 4, '2025-06-02 18:00:00'),
('P20250603-001', '2023010007', '2023010002', 'P-002', 2, '周杰-机械关节龙', '机械关节龙', '/uploads/stl/flexi_dragon_v3.stl', 'PLA', '红色', 0.15, 15, 1, 1, 120.00, 480, '2026-06-23 08:00:00', '2025-06-03 08:20:00', '支撑设置正确，紧急优先级', 5, 125.30, 510, '2025-06-04 10:00:00', 5, '2025-06-04 12:00:00'),
('P20250605-001', '2023010008', '2023010004', 'P-001', NULL, '吴昊-六边形笔筒', '笔筒', '/uploads/stl/pen_holder.stl', 'PLA', '蓝色', 0.2, 20, 0, 2, 35.00, 150, '2026-06-24 14:00:00', '2025-06-05 14:10:00', '简单模型，通过', 5, 36.80, 160, '2025-06-05 19:00:00', 4, '2025-06-05 20:00:00'),
('P20250606-001', '2023010009', '2023010003', 'P-001', NULL, '郑欣-齿轮组', '齿轮组', '/uploads/stl/gear_set.stl', 'PLA', '白色', 0.15, 50, 0, 2, 25.00, 90, '2026-06-25 09:00:00', '2025-06-06 09:20:00', '精度要求高，建议用0.15层高', 5, 26.50, 95, '2026-06-25 17:00:00', 5, NULL),
('P20250606-002', '2023010010', '2023010002', 'P-002', 3, '孙伟-社团徽章', '社团徽章', '/uploads/stl/club_badge.stl', 'PLA', '金色', 0.1, 100, 0, 1, 8.00, 60, '2026-06-25 10:00:00', '2025-06-06 10:30:00', '定制订单，紧急插队', 5, 8.50, 65, '2025-06-06 13:00:00', 5, '2025-06-06 14:00:00'),
('P20250607-001', '2023010011', '2023010004', NULL, NULL, '杨帆-收纳盒', '收纳盒', '/uploads/stl/storage_box.stl', 'PETG', '透明', 0.2, 15, 0, 2, 80.00, 300, '2026-06-26 11:00:00', '2025-06-07 11:15:00', 'PETG打印注意翘边', 3, NULL, NULL, NULL, NULL, NULL),
('P20250608-001', '2023010012', '2023010003', 'P-001', NULL, '朱琳-花瓶', '花瓶', '/uploads/stl/vase_v2.stl', 'PLA', '红色', 0.2, 0, 0, 2, 65.00, 240, '2026-06-26 13:00:00', '2025-06-08 13:30:00', '螺旋花瓶，0填充即可', 5, 68.00, 255, '2026-06-27 22:00:00', 5, NULL),
('P20250609-001', '2023010013', NULL, NULL, NULL, '黄磊-小恐龙', '小恐龙', '/uploads/stl/mini_dino.stl', 'PLA', '绿色', 0.3, 10, 0, 2, 12.00, 45, '2026-06-27 15:00:00', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL),
('P20250609-002', '2023010014', NULL, NULL, NULL, '林峰-挂钩', '挂钩', '/uploads/stl/wall_hook.stl', 'PLA', '黑色', 0.2, 30, 0, 2, 18.00, 75, '2026-06-27 16:00:00', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL),
('P20250601-002', '2023010019', '2023010002', 'P-002', 1, '蔡琴-键帽底座', '键帽底座', '/uploads/stl/keycap_base.stl', 'PLA', '白色', 0.2, 25, 0, 2, 20.00, 90, '2026-06-22 11:00:00', '2025-06-01 11:20:00', '通过', 5, 21.50, 95, '2025-06-01 15:00:00', 4, '2025-06-01 16:30:00'),
('P20250604-001', '2023010005', '2023010003', 'P-001', 2, '刘洋-龙身第一节', '龙身第一节', '/uploads/stl/dragon_body1.stl', 'PLA', '红色', 0.15, 15, 1, 2, 45.00, 200, '2026-06-24 09:00:00', '2025-06-04 09:15:00', '通过', 5, 47.20, 210, '2025-06-04 15:00:00', 5, '2025-06-04 16:00:00'),
('P20250604-002', '2023010006', '2023010004', 'P-002', 2, '陈静-龙身第二节', '龙身第二节', '/uploads/stl/dragon_body2.stl', 'PLA', '红色', 0.15, 15, 1, 2, 45.00, 200, '2026-06-24 10:00:00', '2025-06-04 10:10:00', '通过', 5, 46.80, 205, '2025-06-05 08:00:00', 5, '2025-06-05 09:00:00'),
('P20250610-001', '2024010001', NULL, NULL, NULL, '马超-姓名牌', '姓名牌', '/uploads/stl/nameplate.stl', 'PLA', '蓝色', 0.2, 20, 0, 2, 10.00, 40, '2026-06-27 09:00:00', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL),
('P20250610-002', '2024010002', NULL, NULL, NULL, '高洁-书签', '书签', '/uploads/stl/bookmark.stl', 'PLA', '白色', 0.2, 15, 0, 2, 5.00, 25, '2026-06-28 10:00:00', NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL),
('P20250520-001', '2023010005', '2023010002', 'P-001', NULL, '刘洋-校准测试方块', '测试方块', '/uploads/stl/test_cube.stl', 'PLA', '白色', 0.2, 20, 0, 3, 8.00, 30, '2026-06-21 09:00:00', '2025-05-20 14:10:00', '测试打印机状态', 5, 8.50, 32, '2025-05-20 15:00:00', 5, '2025-05-20 15:30:00'),
('P20250525-001', '2023010007', '2023010003', 'P-002', NULL, '周杰-线材夹', '线材夹', '/uploads/stl/filament_clip.stl', 'PLA', '黑色', 0.2, 30, 0, 3, 3.00, 15, '2026-06-21 10:00:00', '2025-05-25 09:05:00', '通过', 5, 3.20, 16, '2025-05-25 10:00:00', 4, '2025-05-25 10:30:00'),
('P20250601-003', '2023010011', '2023010004', 'P-001', NULL, '杨帆-TPUU盘壳', 'U盘外壳', '/uploads/stl/usb_case.stl', 'TPU', '黑色', 0.2, 30, 0, 2, 12.00, 60, '2026-06-22 16:00:00', '2025-06-01 16:15:00', 'TPU注意回抽设置', 5, 13.00, 65, '2025-06-01 19:00:00', 4, '2025-06-01 20:00:00'),
('P20250603-002', '2023010013', '2023010002', 'P-001', NULL, '黄磊-风扇支架', '风扇支架', '/uploads/stl/fan_bracket.stl', 'ABS', '白色', 0.2, 40, 1, 2, 55.00, 240, '2026-06-23 14:00:00', '2025-06-03 14:20:00', 'ABS需要封闭腔体', 2, NULL, NULL, NULL, NULL, NULL),
('P20250605-002', '2023010019', '2023010003', 'P-002', 3, '蔡琴-徽章底座', '徽章底座', '/uploads/stl/badge_base.stl', 'PLA', '黑色', 0.15, 100, 0, 2, 15.00, 80, '2026-06-25 09:00:00', '2025-06-05 09:10:00', '通过', 5, 16.00, 85, '2025-06-05 12:00:00', 5, '2025-06-05 13:00:00');

-- 耗材消耗记录
INSERT INTO `material_log` (`material_type`, `color`, `weight_change`, `balance`, `operation_type`, `related_task_id`, `operator_id`, `remark`) VALUES
('PLA', '白色', -16.20, 983.80, 2, 'P20250601-001', '2023010002', '打印任务消耗'),
('PLA', '黑色', -48.50, 951.50, 2, 'P20250602-001', '2023010003', '打印任务消耗'),
('PLA', '红色', -125.30, 374.70, 2, 'P20250603-001', '2023010002', '打印任务消耗'),
('PLA', '蓝色', -36.80, 463.20, 2, 'P20250605-001', '2023010004', '打印任务消耗'),
('PLA', '金色', -8.50, 491.50, 2, 'P20250606-002', '2023010002', '打印任务消耗'),
('PETG', '透明', -80.00, 420.00, 2, 'P20250607-001', '2023010004', '打印任务消耗'),
('PLA', '红色', -68.00, 306.70, 2, 'P20250608-001', '2023010003', '打印任务消耗'),
('PLA', '白色', -21.50, 962.30, 2, 'P20250601-002', '2023010002', '打印任务消耗'),
('PLA', '红色', -47.20, 259.50, 2, 'P20250604-001', '2023010003', '打印任务消耗'),
('PLA', '红色', -46.80, 212.70, 2, 'P20250604-002', '2023010004', '打印任务消耗'),
('PLA', '白色', -8.50, 953.80, 2, 'P20250520-001', '2023010002', '打印任务消耗'),
('PLA', '黑色', -3.20, 948.30, 2, 'P20250525-001', '2023010003', '打印任务消耗'),
('TPU', '黑色', -13.00, 237.00, 2, 'P20250601-003', '2023010004', '打印任务消耗'),
('PLA', '黑色', -16.00, 932.30, 2, 'P20250605-002', '2023010003', '打印任务消耗');

-- 作品库数据
INSERT INTO `artwork` (`task_id`, `author_id`, `artwork_name`, `preview_image`, `finish_photos`, `experience`, `is_recommended`, `view_count`) VALUES
('P20250601-001', '2023010005', '机械键盘键帽', '/uploads/preview/keycap_v2.png', '/uploads/finish/keycap_1.jpg,/uploads/finish/keycap_2.jpg', '第一次打印小尺寸模型，层高0.2效果已经很好，键帽表面很光滑。注意打印方向，字母面朝上。', 1, 156),
('P20250602-001', '2023010006', '极简手机支架', '/uploads/preview/phone_stand.png', '/uploads/finish/phone_stand_1.jpg', '设计时底部加了防滑纹路，实际效果不错。20%填充足够支撑手机重量。', 1, 89),
('P20250603-001', '2023010007', '可活动机械关节龙', '/uploads/preview/flexi_dragon.png', '/uploads/finish/dragon_1.jpg,/uploads/finish/dragon_2.jpg,/uploads/finish/dragon_3.jpg', '这是社团第一个大件作品！支撑设置很关键，我用的树状支撑，拆的时候小心一点。关节活动非常顺滑，15%填充刚好。', 1, 312),
('P20250605-001', '2023010008', '六边形笔筒', '/uploads/preview/pen_holder.png', '/uploads/finish/pen_holder_1.jpg', '简单实用的模型，打印很快。建议底部加裙边防止翘边。', 0, 45),
('P20250606-002', '2023010010', '3D打印科创会徽章', '/uploads/preview/club_badge.png', '/uploads/finish/badge_1.jpg,/uploads/finish/badge_2.jpg', '给学校科技节做的社团徽章，100%填充确保质感。0.1层高细节非常清晰，就是打印时间比较长。', 1, 234),
('P20250601-002', '2023010019', '键帽底座', '/uploads/preview/keycap_base.png', '/uploads/finish/keycap_base_1.jpg', '配合键帽一起做的底座，25%填充刚好，太重了反而不好拔插。', 0, 28),
('P20250604-001', '2023010005', '机械龙-龙身第一节', '/uploads/preview/dragon_body1.png', '/uploads/finish/dragon_body1_1.jpg', '团队项目的一部分，和龙身第二节拼接完美。支撑接触面打磨一下更光滑。', 0, 67),
('P20250604-002', '2023010006', '机械龙-龙身第二节', '/uploads/preview/dragon_body2.png', '/uploads/finish/dragon_body2_1.jpg', '和第一节拼接测试了3次才调整到完美公差，建议预留0.2mm间隙。', 0, 58),
('P20250520-001', '2023010005', '校准测试方块', '/uploads/preview/test_cube.png', '/uploads/finish/test_cube_1.jpg', '每次维护后必打的测试方块，用来验证打印机精度。尺寸误差在0.1mm以内算合格。', 0, 22),
('P20250525-001', '2023010007', '线材固定夹', '/uploads/preview/filament_clip.png', '/uploads/finish/filament_clip_1.jpg', '小工具，打印很快。建议一次打5个备用。', 0, 35),
('P20250601-003', '2023010011', 'TPU软胶U盘壳', '/uploads/preview/usb_case.png', '/uploads/finish/usb_case_1.jpg', '第一次用TPU，回抽距离要调到6mm，速度降到25mm/s，不然会拉丝。软胶手感很好。', 1, 134),
('P20250605-002', '2023010019', '社团徽章底座', '/uploads/preview/badge_base.png', '/uploads/finish/badge_base_1.jpg', '和徽章配套，背面加了别针槽。100%填充确保别针牢固。', 0, 41);

-- 项目数据
INSERT INTO `project` (`project_name`, `project_type`, `leader_id`, `start_date`, `end_date`, `actual_end_date`, `budget`, `actual_cost`, `description`, `deliverables`, `cover_image`, `status`) VALUES
('机械键盘定制套装', 1, '2023010005', '2025-06-01', '2025-06-15', '2025-06-14', 200.00, 185.50, '为社团成员定制个性化机械键盘键帽和配件，学习精密小尺寸打印技术。', '20套键帽+5个拔键器+3个键盘支架', '/uploads/project/keyboard_cover.jpg', 2),
('可活动机械关节龙', 1, '2023010007', '2025-06-03', '2025-06-20', '2025-06-18', 300.00, 275.80, '打印一只全身可活动的机械龙，学习关节设计、支撑设置和大件打印技巧。', '1套完整机械龙（12节身体+头部+尾巴+4条腿）', '/uploads/project/dragon_cover.jpg', 2),
('科技节社团徽章定制', 3, '2023010010', '2025-06-05', '2025-06-12', '2025-06-10', 150.00, 142.00, '为学校科技节制作3D打印科创会专属徽章，学习批量生产和质量控制。', '50枚社团徽章', '/uploads/project/badge_cover.jpg', 2),
('智能花盆支架设计', 1, '2023010002', '2025-06-15', '2025-07-15', NULL, 500.00, 120.00, '设计可自动浇水的智能花盆支架，结合3D打印结构和简易电子模块。', '5套智能花盆支架原型+1份设计报告', '/uploads/project/planter_cover.jpg', 1);

-- 项目成员
INSERT INTO `project_member` (`project_id`, `member_id`, `role_in_project`, `contribution`, `status`) VALUES
(1, '2023010005', 1, '项目负责人，负责整体设计和键帽建模', 3),
(1, '2023010019', 2, '核心成员，负责拔键器建模和打印参数优化', 3),
(1, '2023010002', 3, '参与成员，协助打印和质量检查', 3),
(2, '2023010007', 1, '项目负责人，负责龙身建模和关节设计', 3),
(2, '2023010006', 2, '核心成员，负责龙身分段打印和组装', 3),
(2, '2023010005', 2, '核心成员，负责头部和尾巴建模', 3),
(2, '2023010003', 3, '参与成员，负责支撑设置和打印监控', 3),
(3, '2023010010', 1, '项目负责人，负责徽章设计和客户对接', 3),
(3, '2023010019', 2, '核心成员，负责底座建模和批量打印排程', 3),
(3, '2023010004', 3, '参与成员，负责后期打磨和包装', 3),
(4, '2023010002', 1, '项目负责人，负责整体架构设计和电子模块集成', 1),
(4, '2023010003', 2, '核心成员，负责支架结构建模和强度测试', 1),
(4, '2023010019', 2, '核心成员，负责水路设计和3D打印密封测试', 1),
(4, '2023010005', 3, '参与成员，负责文档整理和进度汇报', 1),
(4, '2023010006', 3, '参与成员，协助打印和组装', 1);

-- 项目进度
INSERT INTO `project_progress` (`project_id`, `stage_name`, `stage_order`, `description`, `status`, `responsible_id`, `start_time`, `end_time`) VALUES
(1, '需求分析', 1, '确定键帽规格和设计风格', 2, '2023010005', '2025-06-01 09:00:00', '2025-06-02 18:00:00'),
(1, '建模设计', 2, '使用Fusion 360进行键帽和配件建模', 2, '2023010005', '2025-06-03 09:00:00', '2025-06-05 18:00:00'),
(1, '打印测试', 3, '打印样品进行尺寸和手感测试', 2, '2023010019', '2025-06-06 09:00:00', '2025-06-08 18:00:00'),
(1, '批量打印', 4, '批量生产20套键帽和配件', 2, '2023010002', '2025-06-09 09:00:00', '2025-06-12 18:00:00'),
(1, '质检交付', 5, '质量检查和交付', 2, '2023010005', '2025-06-13 09:00:00', '2025-06-14 18:00:00'),
(2, '概念设计', 1, '确定机械龙的整体造型和关节方案', 2, '2023010007', '2025-06-03 09:00:00', '2025-06-05 18:00:00'),
(2, '分段建模', 2, '将龙身分为12节进行详细建模', 2, '2023010007', '2025-06-06 09:00:00', '2025-06-10 18:00:00'),
(2, '打印测试', 3, '打印第一节进行关节活动测试', 2, '2023010006', '2025-06-11 09:00:00', '2025-06-13 18:00:00'),
(2, '批量打印', 4, '打印所有部件', 2, '2023010003', '2025-06-14 09:00:00', '2025-06-16 18:00:00'),
(2, '组装调试', 5, '组装所有关节并调试活动度', 2, '2023010006', '2025-06-17 09:00:00', '2025-06-18 18:00:00'),
(3, '需求沟通', 1, '与科技节组委会确认徽章规格和数量', 2, '2023010010', '2025-06-05 09:00:00', '2025-06-06 18:00:00'),
(3, '设计定稿', 2, '完成徽章和底座设计', 2, '2023010010', '2025-06-07 09:00:00', '2025-06-08 18:00:00'),
(3, '样品确认', 3, '打印样品给组委会确认', 2, '2023010019', '2025-06-09 09:00:00', '2025-06-09 18:00:00'),
(3, '批量生产', 4, '50枚徽章批量打印', 2, '2023010019', '2025-06-10 09:00:00', '2025-06-10 18:00:00'),
(4, '需求分析', 1, '调研智能花盆功能需求', 2, '2023010002', '2025-06-15 09:00:00', '2025-06-17 18:00:00'),
(4, '结构设计', 2, '设计花盆支架的整体结构', 1, '2023010003', '2025-06-18 09:00:00', NULL),
(4, '电子模块', 3, '集成自动浇水电子模块', 0, '2023010002', NULL, NULL),
(4, '打印测试', 4, '打印支架原型进行密封测试', 0, '2023010019', NULL, NULL),
(4, '组装调试', 5, '组装所有部件并调试功能', 0, '2023010002', NULL, NULL);

-- 项目文件
INSERT INTO `project_file` (`project_id`, `file_name`, `file_path`, `file_type`, `file_size`, `uploader_id`) VALUES
(1, '键帽设计方案v1.pdf', '/uploads/project/keyboard_design_v1.pdf', 1, 2456789, '2023010005'),
(1, 'keycap_v2.stl', '/uploads/project/keycap_v2.stl', 2, 124567, '2023010005'),
(1, '键帽打印测试照片.jpg', '/uploads/project/keyboard_test.jpg', 3, 856432, '2023010019'),
(2, '机械龙概念草图.jpg', '/uploads/project/dragon_sketch.jpg', 1, 678543, '2023010007'),
(2, 'dragon_body1.stl', '/uploads/project/dragon_body1.stl', 2, 234567, '2023010007'),
(2, '关节活动测试视频.mp4', '/uploads/project/dragon_joint_test.mp4', 5, 15678901, '2023010006'),
(3, '徽章设计稿.ai', '/uploads/project/badge_design.ai', 1, 4567890, '2023010010'),
(3, 'club_badge.stl', '/uploads/project/club_badge.stl', 2, 89456, '2023010010'),
(3, '批量生产排程表.xlsx', '/uploads/project/badge_schedule.xlsx', 4, 67890, '2023010019'),
(4, '智能花盆需求文档.docx', '/uploads/project/planter_requirement.docx', 4, 234567, '2023010002'),
(4, '支架结构图.png', '/uploads/project/planter_structure.png', 1, 567890, '2023010003');

-- 维护记录
INSERT INTO `maintenance` (`printer_id`, `maint_type`, `content`, `maintainer_id`, `maint_time`, `next_maint_date`) VALUES
('P-001', 1, '清洁喷嘴、润滑Z轴丝杆、检查皮带张力', '2023010002', '2025-06-01 10:00:00', '2025-07-01'),
('P-001', 4, '打印测试方块，XYZ三轴尺寸校准', '2023010002', '2025-05-20 13:00:00', NULL),
('P-002', 1, '清洁喷嘴、检查热床平整度', '2023010003', '2025-06-05 09:00:00', '2025-07-05'),
('P-003', 2, '挤出机齿轮磨损，更换新齿轮', '2023010002', '2026-05-15 14:00:00', NULL),
('P-001', 3, '更换0.4mm喷嘴为0.6mm喷嘴', '2023010002', '2025-04-10 10:00:00', NULL);

-- ============================================
-- 初始化完成
-- 默认密码：123456
-- 默认管理员账号：2023010001（社长-张明）
-- ============================================
