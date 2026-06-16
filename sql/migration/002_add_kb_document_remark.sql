-- ============================================================
-- 迁移脚本：给 cms_kb_document 表补充 remark 字段
-- 日期：2026-06-14
-- 说明：CmsKbDocument 实体继承 BaseEntity 自带 remark 属性，
--       CmsKbDocumentMapper.xml 在 INSERT/UPDATE 中硬编码引用 remark。
--       漏建导致「保存富文本文档」时 MySQL 报 Unknown column 'remark'。
--
--       同一批次（cms_kb_category / cms_kb_document_version /
--       cms_kb_attachment）建表语句均含 remark 列，
--       仅 cms_kb_document 漏建，与其他 KB 表对齐。
--
--       用 INFORMATION_SCHEMA 判列存在性，保证可重跑。
-- ============================================================

-- 1. 仅在 remark 列不存在时补充
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'cms_kb_document'
      AND COLUMN_NAME = 'remark'
);
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE cms_kb_document ADD COLUMN remark VARCHAR(500) COMMENT ''备注'' AFTER update_time',
    'DO 0'  -- 列已存在,空操作
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

describe cms_kb_document;