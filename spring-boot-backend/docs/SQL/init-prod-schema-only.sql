-- ============================================
-- 3D 打印科创会管理系统 v2 - 生产环境 Schema 初始化
--
-- ✅ v2.12 新增：仅建表，不灌数据
--
-- 与 init-DEV-ONLY.sql 的区别：
--   - 这个文件：只建 11 张表 + 索引 + 外键，生产环境用
--   - init-DEV-ONLY.sql：建表 + 灌 30 测试账号 + 12 作品 + 4 项目，本地开发用
--
-- 生产部署流程：
--   1. 跑这个文件建表
--   2. 设置环境变量：
--        export DB_PASSWORD=<生产 DB 密码>
--        export JWT_SECRET=$(openssl rand -base64 48)
--   3. 启动 spring-boot-backend --spring.profiles.active=prod
--   4. 用 admin/addMember API 一个个加真实成员
--   5. 第一个加的成员在代码里硬编码为 PRESIDENT
-- ============================================

CREATE DATABASE IF NOT EXISTS `print_club_db`
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `print_club_db`;

-- 1. 成员表
CREATE TABLE `member` (
    `student_id` VARCHAR(20) NOT NULL COMMENT '学号',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `password` VARCHAR(60) NOT NULL COMMENT 'BCrypt 密码',
    `role` TINYINT NOT NULL DEFAULT 4 COMMENT '1社长 2技术骨干 3普通社员 4新成员',
    `skill_level` TINYINT DEFAULT 0 COMMENT '技能等级 0-5',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `join_date` DATE DEFAULT NULL COMMENT '入社日期',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`student_id`),
    INDEX `idx_role` (`role`),
    INDEX `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成员表';

-- 2. 打印任务表
CREATE TABLE `print_task` (
    `task_id` VARCHAR(20) NOT NULL COMMENT '任务编号 P+YYYYMMDD+XXXX',
    `applicant_id` VARCHAR(20) NOT NULL COMMENT '申请人',
    `title` VARCHAR(100) NOT NULL COMMENT '任务标题',
    `model_name` VARCHAR(100) DEFAULT NULL COMMENT '模型名',
    `stl_file_path` VARCHAR(255) DEFAULT NULL COMMENT 'STL 文件路径',
    `material_type` VARCHAR(20) NOT NULL COMMENT '材料 PLA|PETG|TPU|ABS',
    `color` VARCHAR(20) NOT NULL COMMENT '颜色',
    `layer_height` DECIMAL(3,2) DEFAULT 0.20 COMMENT '层高 mm',
    `infill_rate` INT DEFAULT 20 COMMENT '填充率 %',
    `need_support` TINYINT DEFAULT 0 COMMENT '是否需要支撑 0/1',
    `priority` TINYINT DEFAULT 1 COMMENT '1低 2普通 3紧急',
    `est_weight` DECIMAL(10,2) DEFAULT NULL COMMENT '预计耗材 g',
    `est_time` INT DEFAULT NULL COMMENT '预计耗时 min',
    `actual_weight` DECIMAL(10,2) DEFAULT NULL COMMENT '实际耗材 g',
    `actual_time` INT DEFAULT NULL COMMENT '实际耗时 min',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待审批 1已通过 2已驳回 3排队 4打印中 5已完成 6已取消 7已签收',
    `project_id` INT DEFAULT NULL COMMENT '关联项目',
    `apply_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `approve_time` DATETIME DEFAULT NULL,
    `start_time` DATETIME DEFAULT NULL,
    `finish_time` DATETIME DEFAULT NULL,
    `pickup_time` DATETIME DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`task_id`),
    INDEX `idx_applicant` (`applicant_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_apply_time` (`apply_time`),
    FOREIGN KEY (`applicant_id`) REFERENCES `member`(`student_id`),
    FOREIGN KEY (`project_id`) REFERENCES `project`(`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打印任务表';

-- 3. 打印机表
CREATE TABLE `printer` (
    `printer_id` VARCHAR(20) NOT NULL COMMENT '打印机编号',
    `model` VARCHAR(50) NOT NULL COMMENT '型号',
    `brand` VARCHAR(50) DEFAULT NULL COMMENT '品牌',
    `nozzle_size` DECIMAL(2,1) DEFAULT 0.4 COMMENT '喷嘴 mm',
    `build_volume` VARCHAR(50) DEFAULT NULL COMMENT '构建尺寸',
    `location` VARCHAR(100) DEFAULT NULL COMMENT '位置',
    `purchase_date` DATE DEFAULT NULL COMMENT '采购日期',
    `total_print_hours` DECIMAL(10,2) DEFAULT 0 COMMENT '累计打印小时',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1在用 2维修 3报废',
    `remark` VARCHAR(500) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`printer_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打印机表';

-- 4. 耗材流水表
CREATE TABLE `material_log` (
    `log_id` INT NOT NULL AUTO_INCREMENT,
    `material_type` VARCHAR(20) NOT NULL COMMENT 'PLA|PETG|TPU|ABS',
    `color` VARCHAR(20) NOT NULL,
    `weight_change` DECIMAL(10,2) NOT NULL COMMENT '正入库 负出库',
    `balance` DECIMAL(10,2) NOT NULL COMMENT '本次操作后余额',
    `operation_type` TINYINT NOT NULL COMMENT '1入库 2打印消耗 3预扣 4退还',
    `related_task_id` VARCHAR(20) DEFAULT NULL,
    `operator_id` VARCHAR(20) NOT NULL,
    `operator_name` VARCHAR(50) DEFAULT NULL,
    `remark` VARCHAR(255) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`log_id`),
    INDEX `idx_material_color` (`material_type`, `color`),
    INDEX `idx_create_time` (`create_time`),
    FOREIGN KEY (`related_task_id`) REFERENCES `print_task`(`task_id`),
    FOREIGN KEY (`operator_id`) REFERENCES `member`(`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='耗材流水表';

-- 5. 作品库表
CREATE TABLE `artwork` (
    `artwork_id` INT NOT NULL AUTO_INCREMENT,
    `task_id` VARCHAR(20) NOT NULL,
    `author_id` VARCHAR(20) NOT NULL,
    `artwork_name` VARCHAR(100) NOT NULL,
    `preview_image` VARCHAR(255) DEFAULT NULL,
    `finish_photos` VARCHAR(500) DEFAULT NULL,
    `experience` TEXT DEFAULT NULL,
    `is_recommended` TINYINT DEFAULT 0,
    `view_count` INT DEFAULT 0,
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
    `project_id` INT NOT NULL AUTO_INCREMENT,
    `project_name` VARCHAR(100) NOT NULL,
    `project_type` TINYINT NOT NULL COMMENT '1作品创作 2竞赛备赛 3定制订单 4社团活动',
    `leader_id` VARCHAR(20) NOT NULL,
    `start_date` DATE NOT NULL,
    `end_date` DATE DEFAULT NULL,
    `actual_end_date` DATE DEFAULT NULL,
    `budget` DECIMAL(10,2) DEFAULT 0,
    `actual_cost` DECIMAL(10,2) DEFAULT 0,
    `description` TEXT DEFAULT NULL,
    `deliverables` VARCHAR(500) DEFAULT NULL,
    `cover_image` VARCHAR(255) DEFAULT NULL,
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0筹备 1进行 2完成 3取消',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`project_id`),
    INDEX `idx_leader` (`leader_id`),
    INDEX `idx_status` (`status`),
    FOREIGN KEY (`leader_id`) REFERENCES `member`(`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- 7. 项目成员表
CREATE TABLE `project_member` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `project_id` INT NOT NULL,
    `member_id` VARCHAR(20) NOT NULL,
    `member_name` VARCHAR(50) DEFAULT NULL,
    `role_in_project` TINYINT NOT NULL DEFAULT 3 COMMENT '1负责人 2核心 3参与者',
    `contribution` VARCHAR(500) DEFAULT NULL,
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1进行 2退出 3完成',
    `join_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_project` (`project_id`),
    INDEX `idx_member` (`member_id`),
    FOREIGN KEY (`project_id`) REFERENCES `project`(`project_id`),
    FOREIGN KEY (`member_id`) REFERENCES `member`(`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目成员表';

-- 8. 项目阶段表
CREATE TABLE `project_progress` (
    `progress_id` INT NOT NULL AUTO_INCREMENT,
    `project_id` INT NOT NULL,
    `stage_order` INT NOT NULL,
    `stage_name` VARCHAR(100) NOT NULL,
    `description` TEXT DEFAULT NULL,
    `responsible_id` VARCHAR(20) DEFAULT NULL,
    `responsible_name` VARCHAR(50) DEFAULT NULL,
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0未开始 1进行 2完成',
    `start_time` DATETIME DEFAULT NULL,
    `end_time` DATETIME DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`progress_id`),
    INDEX `idx_project` (`project_id`),
    FOREIGN KEY (`project_id`) REFERENCES `project`(`project_id`),
    FOREIGN KEY (`responsible_id`) REFERENCES `member`(`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目阶段表';

-- 9. 项目文件表
CREATE TABLE `project_file` (
    `file_id` INT NOT NULL AUTO_INCREMENT,
    `project_id` INT NOT NULL,
    `file_name` VARCHAR(255) NOT NULL,
    `file_type` TINYINT NOT NULL COMMENT '1设计图 2STL 3照片 4文档 5其他',
    `file_size` INT DEFAULT NULL,
    `file_path` VARCHAR(500) NOT NULL,
    `uploader_id` VARCHAR(20) NOT NULL,
    `uploader_name` VARCHAR(50) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`file_id`),
    INDEX `idx_project` (`project_id`),
    FOREIGN KEY (`project_id`) REFERENCES `project`(`project_id`),
    FOREIGN KEY (`uploader_id`) REFERENCES `member`(`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目文件表';

-- 10. 系统日志表
CREATE TABLE `system_log` (
    `log_id` INT NOT NULL AUTO_INCREMENT,
    `user_id` VARCHAR(20) DEFAULT NULL,
    `username` VARCHAR(50) DEFAULT NULL,
    `operation` VARCHAR(50) NOT NULL,
    `resource_type` VARCHAR(50) DEFAULT NULL,
    `resource_id` VARCHAR(100) DEFAULT NULL,
    `description` VARCHAR(500) DEFAULT NULL,
    `ip_address` VARCHAR(50) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`log_id`),
    INDEX `idx_user` (`user_id`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_operation` (`operation`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志表';

-- 11. 申请加入项目表
CREATE TABLE `project_apply` (
    `apply_id` INT NOT NULL AUTO_INCREMENT,
    `project_id` INT NOT NULL,
    `applicant_id` VARCHAR(20) NOT NULL,
    `applicant_name` VARCHAR(50) DEFAULT NULL,
    `reason` TEXT DEFAULT NULL,
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待审 1通过 2驳回',
    `review_time` DATETIME DEFAULT NULL,
    `reviewer_id` VARCHAR(20) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`apply_id`),
    INDEX `idx_project` (`project_id`),
    INDEX `idx_applicant` (`applicant_id`),
    FOREIGN KEY (`project_id`) REFERENCES `project`(`project_id`),
    FOREIGN KEY (`applicant_id`) REFERENCES `member`(`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申请加入项目表';

-- ============================================
-- 初始化完成
--
-- 下一步：
--   1. 启动后端：java -jar app.jar --spring.profiles.active=prod
--      并确保环境变量 DB_PASSWORD / JWT_SECRET 已设置
--   2. 用管理后台加成员 / 项目 / 任务
-- ============================================
