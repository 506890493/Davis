-- ============================================================================
-- 修复 cms_customer_type 字典值双重 UTF-8 编码 bug
-- 日期：2026-06-13
-- 背景：sys_dict_data.dict_label 字段是 utf8mb4，但写入时连接字符集错误，
--      导致 "公司" 的 UTF-8 字节被当作 Latin-1 字符再次编码为 UTF-8 存入。
--      HEX("公司") 正常应为 e5 85 ac e5 8f b8，现在错误为 c3 a5 e2 80 a6 c2 ac ...
-- 修复：将错乱的字节强制按 latin1 → utf8mb4 转换回正确的中文。
-- ============================================================================

-- 安全检查：当前是双重编码状态？
-- SELECT dict_code, HEX(dict_label) FROM sys_dict_data
--   WHERE dict_type='cms_customer_type' ORDER BY dict_code;
-- 期望 HEX 是 c3a5e280a6c2ac... (双重编码)，若是 e5 85 ac... 则已经正确无需修复

-- 1) 修复 4 条字典的 dict_label（用 CONVERT 强制按 latin1→utf8mb4 还原）
UPDATE sys_dict_data
SET    dict_label = CONVERT(CAST(CONVERT(dict_label USING latin1) AS BINARY) USING utf8mb4)
WHERE  dict_type = 'cms_customer_type'
  AND  (HEX(dict_label) LIKE 'C3A5%' OR HEX(dict_label) LIKE 'C3A4%'
     OR HEX(dict_label) LIKE 'C3A6%' OR HEX(dict_label) LIKE 'C2%');

-- 2) 同样修复 dict_type 列本身（如果是双重编码），但通常这条不会有问题
-- UPDATE sys_dict_type SET dict_type = CONVERT(CAST(CONVERT(dict_type USING latin1) AS BINARY) USING utf8mb4)
--   WHERE dict_type LIKE CONCAT('%', CHAR(0xC3A5), '%');  -- 含乱码的 dict_type

-- 3) 验证：执行后 HEX 应是 e5 85 ac e5 8f b8 (公司), e4 b8 aa e4 bd 93 e6 88 b7 (个体户) 等
-- SELECT dict_code, dict_label, HEX(dict_label) FROM sys_dict_data
--   WHERE dict_type='cms_customer_type' ORDER BY dict_code;

-- 4) 后续写入预防：
--    永久方案 1：在 my.cnf / docker-compose.yml 设置 client charset=utf8mb4
--    永久方案 2：每次 mysql 命令行加 --default-character-set=utf8mb4
--    永久方案 3：用环境变量 LANG/LC_ALL=en_US.UTF-8 让 docker exec 不丢编码
