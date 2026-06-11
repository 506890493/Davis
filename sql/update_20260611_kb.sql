-- ============================================================================
-- 知识库（Knowledge Base）建表 + 字典 + 默认分类 + 菜单 + 角色绑定
-- 日期：2026-06-11
-- 说明：
--   1. 创建 5 张知识库表（目录、文件、文档、版本、附件）
--   2. 插入 3 个字典类型（kb_doc_type / kb_doc_status / kb_required）+ 6 条字典数据
--   3. 插入 4 个默认一级分类种子
--   4. 挂菜单：父菜单"知识库" + 3 个管理端子菜单（目录/文档/回收站）+ 1 个阅读端入口
--   5. 角色绑定：admin 全量 / manager 全量（除 purge）/ sales / account 仅读+下载+上传
--   6. 角色约定：admin=1, manager=2, account=3, sales=4
-- ============================================================================
-- ============================================================================
-- KB 模块初始化脚本 (idempotent: 可重复执行)
-- ============================================================================
-- 重跑时所有 INSERT 走 INSERT IGNORE，重复主键会被跳过，不会中断。
-- 适用于：开发环境反复初始化 / 测试库清理后重新装载。
-- ============================================================================

-- ============================================================
-- Step 1: 创建 5 张 KB 表
-- ============================================================

-- 1. 知识库目录
DROP TABLE IF EXISTS cms_kb_category;
CREATE TABLE cms_kb_category (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id     BIGINT       DEFAULT 0   COMMENT '0=一级',
    name          VARCHAR(64)  NOT NULL,
    icon          VARCHAR(255)             COMMENT 'Element UI 图标名',
    order_num     INT          DEFAULT 0,
    is_required   TINYINT(1)   DEFAULT 0   COMMENT '新员工必读 0否 1是',
    status        TINYINT(1)   DEFAULT 1   COMMENT '0停用 1正常',
    create_by     VARCHAR(64),
    create_time   DATETIME,
    update_by     VARCHAR(64),
    update_time   DATETIME,
    remark        VARCHAR(255)             COMMENT '备注',
    del_flag      TINYINT(1)   DEFAULT 0,
    INDEX idx_parent (parent_id, del_flag, order_num)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库-目录';

-- 2. 文件元数据
DROP TABLE IF EXISTS cms_kb_file;
CREATE TABLE cms_kb_file (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    original_name  VARCHAR(255) NOT NULL,
    stored_name    VARCHAR(255) NOT NULL,
    rel_path       VARCHAR(512) NOT NULL,
    file_size      BIGINT       NOT NULL,
    mime_type      VARCHAR(128),
    sha256         CHAR(64),
    bucket         VARCHAR(32)  DEFAULT 'kb',
    create_by      VARCHAR(64),
    create_time    DATETIME,
    update_by      VARCHAR(64),
    update_time    DATETIME,
    remark         VARCHAR(255),
    del_flag       TINYINT(1)   DEFAULT 0,
    UNIQUE KEY uk_sha (sha256, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库-文件元数据';

-- 3. 文档主表
DROP TABLE IF EXISTS cms_kb_document;
CREATE TABLE cms_kb_document (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id     BIGINT       NOT NULL,
    title           VARCHAR(255) NOT NULL,
    doc_type        TINYINT      NOT NULL  COMMENT '1=文件 2=富文本文章',
    summary         VARCHAR(500),
    tags            VARCHAR(255),
    cover_image_id  BIGINT,
    primary_file_id BIGINT,
    is_required     TINYINT(1)   DEFAULT 0,
    status          TINYINT      DEFAULT 0 COMMENT '0草稿 1已发布 2已下架',
    published_time  DATETIME,
    view_count      INT          DEFAULT 0,
    current_version INT          DEFAULT 1,
    create_by       VARCHAR(64),
    create_time     DATETIME,
    update_by       VARCHAR(64),
    update_time     DATETIME,
    del_flag        TINYINT(1)   DEFAULT 0,
    delete_time     DATETIME,
    INDEX idx_cat (category_id, status, del_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库-文档';

-- 4. 文档版本
DROP TABLE IF EXISTS cms_kb_document_version;
CREATE TABLE cms_kb_document_version (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_id     BIGINT       NOT NULL,
    version_no      INT          NOT NULL,
    title           VARCHAR(255) NOT NULL,
    content         LONGTEXT,
    primary_file_id BIGINT,
    summary         VARCHAR(500),
    tags            VARCHAR(255),
    save_reason     VARCHAR(255),
    is_current      TINYINT(1)   DEFAULT 0,
    create_by       VARCHAR(64),
    create_time     DATETIME,
    update_by       VARCHAR(64),
    update_time     DATETIME,
    remark          VARCHAR(255),
    UNIQUE KEY uk_doc_ver (document_id, version_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库-文档版本';

-- 5. 文章内嵌附件
DROP TABLE IF EXISTS cms_kb_attachment;
CREATE TABLE cms_kb_attachment (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_id  BIGINT  NOT NULL,
    version_id   BIGINT,
    file_id      BIGINT  NOT NULL,
    display_name VARCHAR(255),
    sort_num     INT     DEFAULT 0,
    create_by    VARCHAR(64),
    create_time  DATETIME,
    update_by    VARCHAR(64),
    update_time  DATETIME,
    remark       VARCHAR(255),
    INDEX idx_doc (document_id, sort_num)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库-文章附件';

-- ============================================================
-- Step 2: 插入 3 个字典
-- ============================================================
INSERT IGNORE INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, remark)
VALUES
(200, '知识库文档类型', 'kb_doc_type', '0', 'admin', NOW(), '文件/富文本文章'),
(201, '知识库文档状态', 'kb_doc_status', '0', 'admin', NOW(), '草稿/已发布/已下架'),
(202, '知识库是否必读', 'kb_required', '0', 'admin', NOW(), '新员工必读标记');

INSERT IGNORE INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES
(2001, 1, '文件',     '1', 'kb_doc_type',  'Y', '0', 'admin', NOW(), '主文档为上传文件'),
(2002, 2, '富文本文章','2', 'kb_doc_type',  'N', '0', 'admin', NOW(), '主文档为 WangEditor 内容'),
(2011, 1, '草稿',     '0', 'kb_doc_status','N', '0', 'admin', NOW(), '仅作者可见'),
(2012, 2, '已发布',   '1', 'kb_doc_status','Y', '0', 'admin', NOW(), '全员可见'),
(2013, 3, '已下架',   '2', 'kb_doc_status','N', '0', 'admin', NOW(), '不再展示'),
(2021, 1, '否',       '0', 'kb_required',  'Y', '0', 'admin', NOW(), ''),
(2022, 2, '是',       '1', 'kb_required',  'N', '0', 'admin', NOW(), '新员工必读');

-- ============================================================
-- Step 3: 插入 4 个默认一级分类种子
-- ============================================================
-- cms_kb_category 主键是自增 id，没有 name 唯一约束 → 用 WHERE NOT EXISTS 避免重跑重复插入
INSERT INTO cms_kb_category(parent_id, name, icon, order_num, is_required, status, create_by, create_time, remark)
SELECT 0, '系统操作手册',   'documentation',  1, 1, 1, 'admin', NOW(), 'Davis 系统使用手册' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM cms_kb_category WHERE name='系统操作手册' AND del_flag=0);

INSERT INTO cms_kb_category(parent_id, name, icon, order_num, is_required, status, create_by, create_time, remark)
SELECT 0, '代账知识',       'money',          2, 0, 1, 'admin', NOW(), '代账业务知识' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM cms_kb_category WHERE name='代账知识' AND del_flag=0);

INSERT INTO cms_kb_category(parent_id, name, icon, order_num, is_required, status, create_by, create_time, remark)
SELECT 0, '会计知识',       'reading',        3, 0, 1, 'admin', NOW(), '财务/会计' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM cms_kb_category WHERE name='会计知识' AND del_flag=0);

INSERT INTO cms_kb_category(parent_id, name, icon, order_num, is_required, status, create_by, create_time, remark)
SELECT 0, '工商知识',       'office-building',4, 0, 1, 'admin', NOW(), '公司注册/变更/注销' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM cms_kb_category WHERE name='工商知识' AND del_flag=0);

-- ============================================================
-- Step 4: 插入菜单节点（管理端 + 阅读端）
-- ============================================================
-- 父菜单：知识库（系统管理下，parent_id=1） — sys_menu 只有 PK(menu_id)，无唯一约束 → 用 NOT EXISTS 避免重跑重复
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '知识库', 1, 9, 'kb', null, 1, 0, 'M', '0', '0', '', 'documentation', 'admin', NOW(), '知识库目录' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='知识库' AND parent_id=1);

-- 知识库父菜单（已存在则取已有 ID，避免重跑时丢 ID）
SET @kb_pid = (SELECT menu_id FROM sys_menu WHERE menu_name = '知识库' AND parent_id = 1 LIMIT 1);

-- 子菜单：sys_menu 无唯一约束 → 每行用 NOT EXISTS 避免重跑重复
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '目录管理',  @kb_pid, 1, 'category', 'system/kb/category', 1, 0, 'C', '0', '0', 'kb:category:list', '#', 'admin', NOW(), '' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='kb:category:list');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '文档管理',  @kb_pid, 2, 'document', 'system/kb/document', 1, 0, 'C', '0', '0', 'kb:document:list', '#', 'admin', NOW(), '' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='kb:document:list');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '回收站',    @kb_pid, 3, 'recycle',  'system/kb/recycle',  1, 0, 'C', '0', '0', 'kb:recycle:list',  '#', 'admin', NOW(), '' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='kb:recycle:list');

-- 阅读端（顶层显示，parent_id=0）
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '知识库学习', 0,     5, 'view',     'system/kb/portal/index', 1, 0, 'C', '0', '0', 'kb:portal:view',  'reading', 'admin', NOW(), '' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='kb:portal:view');

-- 按钮级权限（供 sales/account 绑定，menu_type='F'，parent_id=0） — sys_menu 无唯一约束 → NOT EXISTS
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '知识库必读入口', 0, 6, '', '', 1, 0, 'F', '0', '0', 'kb:portal:required', '#', 'admin', NOW(), '' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='kb:portal:required');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '知识库文件下载', 0, 7, '', '', 1, 0, 'F', '0', '0', 'kb:file:download',  '#', 'admin', NOW(), '' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='kb:file:download');

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '知识库文件上传', 0, 8, '', '', 1, 0, 'F', '0', '0', 'kb:file:upload',    '#', 'admin', NOW(), '' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='kb:file:upload');

-- ============================================================
-- Step 5: 角色绑定（admin / manager / sales / account）
-- ============================================================
-- admin：拿全部 KB 权限
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'kb:%';

-- manager：除 kb:recycle:purge 之外的全部（含 kb:recycle:list）
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT 2, menu_id FROM sys_menu
WHERE perms LIKE 'kb:%' AND perms <> 'kb:recycle:purge';

-- sales / account：仅读 + 下载 + 上传
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT 3, menu_id FROM sys_menu
WHERE perms IN ('kb:portal:view','kb:portal:required','kb:file:download','kb:file:upload');

INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT 4, menu_id FROM sys_menu
WHERE perms IN ('kb:portal:view','kb:portal:required','kb:file:download','kb:file:upload');

-- ============================================================
-- Step 6: Quartz 定时任务 — 30 天回收站清理
-- ============================================================
-- sys_job 复合主键含 AUTO_INCREMENT job_id，单纯 INSERT IGNORE 不能阻止按 (job_name,job_group) 重复 → 用 NOT EXISTS
INSERT INTO sys_job(job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT '知识库回收站清理', 'DEFAULT', 'kbRecycleCleanTask.cleanDaily()', '0 0 2 * * ?', '3', '0', '0', 'admin', NOW(), '每日 02:00 物理删除 30 天前进入回收站的文档' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE job_name = '知识库回收站清理' AND job_group = 'DEFAULT');

-- ============================================================================
-- 验证（执行后请 SELECT 看到下面的行数）
-- ============================================================================
-- SELECT COUNT(*) FROM sys_dict_type WHERE dict_id BETWEEN 200 AND 202;       -- 期望 3
-- SELECT COUNT(*) FROM sys_dict_data WHERE dict_code BETWEEN 2001 AND 2022;   -- 期望 7
-- SELECT COUNT(*) FROM cms_kb_category;                                       -- 期望 4
-- SELECT COUNT(*) FROM sys_menu WHERE perms LIKE 'kb:%' OR menu_name='知识库'; -- 期望 8 (1 父 + 3 admin + 1 portal + 3 button)
-- SELECT COUNT(*) FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE perms LIKE 'kb:%' OR menu_name='知识库'); -- 期望 ≥ 12
-- SELECT COUNT(*) FROM sys_job WHERE job_name='知识库回收站清理';              -- 期望 1

-- ============================================================================
-- 修复：仅部分应用过的库（部分行已存在 / 部分缺失）— 安全补全
-- ============================================================================
-- 若上次只跑了一半（如 dict 入了但菜单缺失），重跑本文件即可，所有 INSERT 走
-- INSERT IGNORE 或 NOT EXISTS → 已存在行被跳过，缺失行被补齐。
-- 若 4 张表 cms_kb_category/cms_kb_file/cms_kb_document/cms_kb_document_version
-- /cms_kb_attachment 还没建，可单独执行 Step 1 的 CREATE TABLE。

-- ============================================================================
-- 如需完全重置 (慎用！会删除所有 KB 菜单的角色绑定和数据)
-- ============================================================================
-- DELETE FROM sys_role_menu WHERE menu_id IN (SELECT menu_id FROM sys_menu WHERE perms LIKE 'kb:%' OR menu_name='知识库');
-- DELETE FROM sys_menu WHERE perms LIKE 'kb:%' OR menu_name='知识库';
-- DELETE FROM sys_dict_data WHERE dict_type IN ('kb_doc_type','kb_doc_status','kb_required');
-- DELETE FROM sys_dict_type WHERE dict_type IN ('kb_doc_type','kb_doc_status','kb_required');
-- DELETE FROM cms_kb_category;
-- DELETE FROM sys_job WHERE job_name='知识库回收站清理';
-- -- 之后重新跑本文件即可
