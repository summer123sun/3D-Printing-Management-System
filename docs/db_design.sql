-- ============================================================
-- 3D打印科创会管理系统 数据库设计
-- 数据库：MySQL 8.0（兼容 5.7）
-- 字符集：utf8mb4
-- 共 4 张核心表
-- ============================================================

DROP DATABASE IF EXISTS print_club;
CREATE DATABASE print_club DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE print_club;

-- ============================================================
-- 1. 成员表 member
-- 说明：存储社团成员信息、登录密码、角色、技能等级
-- 角色：1 管理员（社长）/ 2 技术骨干 / 3 普通社员
-- ============================================================
DROP TABLE IF EXISTS member;
CREATE TABLE member (
    student_id   VARCHAR(20)  NOT NULL                COMMENT '学号',
    name         VARCHAR(50)  NOT NULL                COMMENT '姓名',
    password     VARCHAR(100) NOT NULL                COMMENT '登录密码（MD5加密）',
    role         TINYINT      NOT NULL DEFAULT 3      COMMENT '角色：1管理员 2技术骨干 3普通社员',
    skill_level  TINYINT      NOT NULL DEFAULT 0      COMMENT '技能等级 0未入门 1入门 2熟练 3专家',
    phone        VARCHAR(20)                          COMMENT '手机号',
    status       TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1正常 2休会',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成员表';

-- ============================================================
-- 2. 打印机表 printer
-- 说明：打印机台账，记录设备型号、状态、累计使用时长
-- 状态：1 正常 / 2 维修中 / 3 报废
-- ============================================================
DROP TABLE IF EXISTS printer;
CREATE TABLE printer (
    printer_id        VARCHAR(10)  NOT NULL             COMMENT '打印机编号',
    model             VARCHAR(50)  NOT NULL             COMMENT '型号',
    brand             VARCHAR(30)                       COMMENT '品牌',
    status            TINYINT       NOT NULL DEFAULT 1  COMMENT '状态：1正常 2维修中 3报废',
    total_print_hours DECIMAL(8,2)  NOT NULL DEFAULT 0  COMMENT '累计打印时长（小时）',
    current_task_id   VARCHAR(20)                      COMMENT '当前执行任务编号',
    location          VARCHAR(50)                       COMMENT '放置位置',
    create_time       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (printer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打印机表';

-- ============================================================
-- 3. 打印任务表 print_task
-- 说明：核心业务单据
-- 状态：0 待审批 / 1 已通过 / 2 已驳回 / 3 排队中 / 4 打印中 / 5 已完成 / 6 已取消
-- ============================================================
DROP TABLE IF EXISTS print_task;
CREATE TABLE print_task (
    task_id          VARCHAR(20)  NOT NULL                COMMENT '任务编号（P+年月日+序号）',
    applicant_id     VARCHAR(20)  NOT NULL                COMMENT '申请人学号',
    approver_id      VARCHAR(20)                          COMMENT '审批人学号',
    printer_id       VARCHAR(10)                          COMMENT '分配的打印机编号',
    model_name       VARCHAR(100) NOT NULL                COMMENT '模型名称',
    stl_file_path    VARCHAR(255) NOT NULL                COMMENT 'STL文件存储路径',
    material_type    VARCHAR(20)  NOT NULL                COMMENT '材料类型：PLA/PETG/TPU',
    color            VARCHAR(20)                          COMMENT '颜色',
    layer_height     DECIMAL(3,1) NOT NULL DEFAULT 0.2    COMMENT '层高（mm）',
    infill_rate      TINYINT      NOT NULL DEFAULT 20     COMMENT '填充率（%）',
    est_weight       DECIMAL(6,2)                         COMMENT '预估重量（g）',
    est_time         INT                                 COMMENT '预估耗时（分钟）',
    apply_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    approve_time     DATETIME                             COMMENT '审批时间',
    approve_comment  VARCHAR(255)                         COMMENT '审批意见',
    status           TINYINT      NOT NULL DEFAULT 0      COMMENT '状态：0待审批 1已通过 2已驳回 3排队中 4打印中 5已完成 6已取消',
    actual_weight    DECIMAL(6,2)                         COMMENT '实际耗材（g）',
    actual_time      INT                                 COMMENT '实际耗时（分钟）',
    finish_time      DATETIME                             COMMENT '完成时间',
    quality_score    TINYINT                             COMMENT '1-5星质量评分',
    PRIMARY KEY (task_id),
    INDEX idx_applicant (applicant_id),
    INDEX idx_status (status),
    INDEX idx_apply_time (apply_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打印任务表';

-- ============================================================
-- 4. 耗材记录表 material_log
-- 说明：耗材入库、消耗流水，每条记录保存当前余额
-- 类型：1 入库 / 2 消耗
-- ============================================================
DROP TABLE IF EXISTS material_log;
CREATE TABLE material_log (
    log_id           INT          NOT NULL AUTO_INCREMENT       COMMENT '记录编号',
    material_type    VARCHAR(20)  NOT NULL                      COMMENT '材料类型',
    color            VARCHAR(20)  NOT NULL                      COMMENT '颜色',
    weight_change    DECIMAL(8,2) NOT NULL                      COMMENT '变化重量（g），正数入库负数消耗',
    balance          DECIMAL(8,2) NOT NULL                      COMMENT '变动后库存余额（g）',
    operation_type   TINYINT      NOT NULL                      COMMENT '操作类型：1入库 2消耗',
    related_task_id  VARCHAR(20)                               COMMENT '关联打印任务编号',
    operator_id      VARCHAR(20)  NOT NULL                      COMMENT '操作人学号',
    remark           VARCHAR(255)                               COMMENT '备注',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (log_id),
    INDEX idx_material (material_type, color),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='耗材记录表';

-- ============================================================
-- 初始化测试数据
-- ============================================================

-- 成员（密码统一为 123456 的 MD5：e10adc3949ba59abbe56e057f20f883e）
INSERT INTO member (student_id, name, password, role, skill_level, phone) VALUES
('2023001', '张三（社长）',   'e10adc3949ba59abbe56e057f20f883e', 1, 4, '13800000001'),
('2023002', '李四（技术骨干）', 'e10adc3949ba59abbe56e057f20f883e', 2, 3, '13800000002'),
('2023003', '王五（技术骨干）', 'e10adc3949ba59abbe56e057f20f883e', 2, 3, '13800000003'),
('2023004', '赵六',           'e10adc3949ba59abbe56e057f20f883e', 3, 2, '13800000004'),
('2023005', '钱七',           'e10adc3949ba59abbe56e057f20f883e', 3, 1, '13800000005'),
('2023006', '孙八',           'e10adc3949ba59abbe56e057f20f883e', 3, 0, '13800000006');

-- 打印机
INSERT INTO printer (printer_id, model, brand, status, total_print_hours, location) VALUES
('P001', 'Ender-3 V2',    'Creality',    1, 120.5, 'A101 实验室 1号位'),
('P002', 'Prusa MK4',     'Prusa',       1,  45.0, 'A101 实验室 2号位'),
('P003', 'Bambu X1C',     'Bambu Lab',   2, 230.8, 'A101 实验室 3号位（维修中）');

-- 耗材初始库存
INSERT INTO material_log (material_type, color, weight_change, balance, operation_type, operator_id, remark) VALUES
('PLA', '白色', 2000.00, 2000.00, 1, '2023001', '初始入库'),
('PLA', '黑色', 1500.00, 1500.00, 1, '2023001', '初始入库'),
('PETG', '透明', 1000.00, 1000.00, 1, '2023001', '初始入库');

-- 打印任务示例
INSERT INTO print_task (task_id, applicant_id, model_name, stl_file_path, material_type, color, layer_height, infill_rate, est_weight, est_time, status, approve_comment) VALUES
('P20260622001', '2023004', '立方体校准件', 'models/cube.stl', 'PLA', '白色', 0.2, 20, 15.5, 60, 5, '模型OK，正常打印'),
('P20260622002', '2023005', '齿轮模型',   'models/gear.stl', 'PLA', '黑色', 0.2, 30, 25.0, 90, 0, NULL);

-- ============================================================
-- 常用查询示例（供 DAO 层参考）
-- ============================================================

-- 当前库存（按材料+颜色聚合最新余额）
-- SELECT material_type, color, balance
-- FROM material_log t1
-- WHERE log_id = (
--     SELECT MAX(log_id) FROM material_log t2
--     WHERE t2.material_type = t1.material_type AND t2.color = t1.color
-- );

-- 待审批任务列表
-- SELECT * FROM print_task WHERE status = 0 ORDER BY apply_time ASC;

-- 某社员的全部任务
-- SELECT * FROM print_task WHERE applicant_id = ? ORDER BY apply_time DESC;

-- 本月打印任务统计
-- SELECT COUNT(*) FROM print_task
-- WHERE apply_time >= DATE_FORMAT(NOW(), '%Y-%m-01');

-- ============================================================
-- 结束
-- ============================================================

