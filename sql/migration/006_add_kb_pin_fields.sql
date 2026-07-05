-- ============================================================
-- 迁移脚本：给 cms_kb_document 加置顶字段
-- 日期：2026-07-05
-- 说明：新增 is_pinned（是否置顶）+ pinned_at（置顶时间）两列。
--       列表查询会按 is_pinned DESC, pinned_at ASC, title ASC 排序，
--       实现"置顶在前 + 同置顶按时序 + 非置顶按编号"。
-- 幂等：先检查列是否存在再 ALTER，重复执行不会出错。
-- ============================================================

-- 1. 加 is_pinned 列（如不存在）
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'cms_kb_document'
      AND COLUMN_NAME = 'is_pinned'
);

SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE cms_kb_document ADD COLUMN is_pinned TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''是否置顶 0否 1是''',
    'SELECT ''is_pinned already exists'' AS msg'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 加 pinned_at 列（如不存在）
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'cms_kb_document'
      AND COLUMN_NAME = 'pinned_at'
);

SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE cms_kb_document ADD COLUMN pinned_at DATETIME DEFAULT NULL COMMENT ''置顶时间（同置顶层级按此排序）''',
    'SELECT ''pinned_at already exists'' AS msg'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 加复合索引（按 is_pinned + pinned_at 加速排序查询）
SET @idx_exists := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'cms_kb_document'
      AND INDEX_NAME = 'idx_kb_document_pin'
);

SET @ddl := IF(@idx_exists = 0,
    'CREATE INDEX idx_kb_document_pin ON cms_kb_document (is_pinned DESC, pinned_at ASC)',
    'SELECT ''idx_kb_document_pin already exists'' AS msg'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 使用说明：
--   置顶某文档：  UPDATE cms_kb_document SET is_pinned=1, pinned_at=NOW() WHERE id=? AND del_flag=0;
--   取消置顶：    UPDATE cms_kb_document SET is_pinned=0, pinned_at=NULL WHERE id=? AND del_flag=0;
--   查询置顶文档：SELECT * FROM cms_kb_document WHERE is_pinned=1 AND del_flag=0 ORDER BY pinned_at ASC;
-- 列表默认排序（is_pinned DESC, pinned_at ASC, title ASC）：
--   置顶在前 → 同置顶按时序（先置顶的排上）→ 非置顶按 title 字典序（00-术语 < 01-快速 < 02-仪表盘...）
-- ============================================================