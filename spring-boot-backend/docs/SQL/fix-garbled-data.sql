-- =====================================================
-- 清理历史乱码任务数据 + 检测/修复字符集
-- =====================================================
-- 适用：之前没修编码时存的旧数据（比如 latin1 编码的 UTF-8 字节）
-- 使用方法：
--   1. 在 MySQL 客户端跑这个文件
--   2. 看输出，确认后再执行清理
-- =====================================================

-- ============ 第一步：检查字符集现状 ============
SELECT
    TABLE_NAME,
    TABLE_COLLATION,
    CREATE_OPTIONS
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
ORDER BY TABLE_NAME;

-- ============ 第二步：检查 print_task 表的乱码情况 ============
-- 找出 title 字段是乱码的行（典型特征：包含大量非 UTF-8 字符）
SELECT
    task_id,
    title,
    HEX(LEFT(title, 20)) AS title_hex,
    CHAR_LENGTH(title) AS char_len,
    LENGTH(title) AS byte_len
FROM print_task
WHERE
    -- 1. 包含 latin1 编码的中文特征字节（UTF-8 中文字符用 3 字节，latin1 1 字节，所以 byte/char > 3）
    (LENGTH(title) > CHAR_LENGTH(title) * 3 AND title REGEXP '[\\x80-\\xFF]')
    -- 2. 或者包含 latin1 转 UTF-8 后的典型乱码字符（Ã©, Ã¨, Ã , Â 等）
    OR title REGEXP '(Ã©|Ã¨|Ã |Â|â€™|â€œ)'
LIMIT 20;

-- ============ 第三步：把 print_task 数据库/表/列都改成 utf8mb4 ============
-- 数据库级
ALTER DATABASE print_club_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 表级（print_task）
ALTER TABLE print_task CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 列出所有需要改的表（手动跑）
SELECT CONCAT('ALTER TABLE ', TABLE_NAME, ' CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;') AS fix_sql
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_COLLATION NOT LIKE 'utf8mb4%';

-- ============ 第四步：清理纯字母数字的脏测试数据 ============
-- 这些是没标题/纯测试名/明显是占位符的
DELETE FROM print_task
WHERE
    -- 纯 ASCII 字母数字（说明当时 title 没存进中文）
    title REGEXP '^[a-zA-Z0-9_\\-\\s]+$'
    -- 或者包含乱码字符
    OR title REGEXP '(Ã©|Ã¨|Ã |Â|â€™|â€œ|锘縞|??)'
LIMIT 100;

-- ============ 第五步：清理 material_log 里的乱码关联 ============
DELETE FROM material_log
WHERE related_task_id NOT IN (SELECT task_id FROM print_task);

-- ============ 第六步：清理 artwork 里的孤儿记录 ============
DELETE FROM artwork
WHERE task_id NOT IN (SELECT task_id FROM print_task);

-- ============ 第七步：验证清理结果 ============
SELECT COUNT(*) AS remaining_tasks FROM print_task;
SELECT COUNT(*) AS dirty_tasks FROM print_task
WHERE title REGEXP '(Ã©|Ã¨|Ã |Â|â€™|â€œ|锘縞|\\?\\?\\?)';

-- ============ 验证当前连接字符集 ============
SHOW VARIABLES LIKE 'character_set%';
SHOW VARIABLES LIKE 'collation%';

-- =====================================================
-- 预期输出：
--   character_set_client = utf8mb4
--   character_set_connection = utf8mb4
--   character_set_database = utf8mb4
--   character_set_results = utf8mb4
--   character_set_server = utf8mb4
-- =====================================================
