-- ============================================================
-- 知识库（Knowledge Base）建表 + 字典 + 默认分类 + 菜单 + 角色绑定
-- 日期：2026-06-11
-- 说明：
--   1. 创建 5 张知识库表（目录、文件、文档、版本、附件）
--   2. 插入 3 个字典类型（kb_doc_type / kb_doc_status / kb_required）+ 6 条字典数据
--   3. 插入 4 个默认一级分类种子
--   4. 挂菜单：父菜单"知识库" + 3 个管理端子菜单（目录/文档/回收站）+ 1 个阅读端入口
--   5. 角色绑定：admin 全量 / manager 全量（除 purge）/ sales / account 仅读+下载+上传
--   6. 角色约定：admin=1, manager=2, account=3, sales=4
-- ============================================================

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
INSERT INTO sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time, remark)
VALUES
(200, '知识库文档类型', 'kb_doc_type', '0', 'admin', NOW(), '文件/富文本文章'),
(201, '知识库文档状态', 'kb_doc_status', '0', 'admin', NOW(), '草稿/已发布/已下架'),
(202, '知识库是否必读', 'kb_required', '0', 'admin', NOW(), '新员工必读标记');

INSERT INTO sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
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
INSERT INTO cms_kb_category(parent_id, name, icon, order_num, is_required, status, create_by, create_time, remark)
VALUES
(0, '系统操作手册',   'documentation',  1, 1, 1, 'admin', NOW(), 'Davis 系统使用手册'),
(0, '代账知识',       'money',          2, 0, 1, 'admin', NOW(), '代账业务知识'),
(0, '会计知识',       'reading',        3, 0, 1, 'admin', NOW(), '财务/会计'),
(0, '工商知识',       'office-building',4, 0, 1, 'admin', NOW(), '公司注册/变更/注销');

-- ============================================================
-- Step 4: 插入菜单节点（管理端 + 阅读端）
-- ============================================================
-- 父菜单：知识库（系统管理下，parent_id=1）
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
('知识库', 1, 9, 'kb', null, 1, 0, 'M', '0', '0', '', 'documentation', 'admin', NOW(), '知识库目录');

-- 取刚插入的父菜单 ID
SET @kb_pid = LAST_INSERT_ID();

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
-- 管理端
('目录管理',  @kb_pid, 1, 'category', 'kb/admin/category', 1, 0, 'C', '0', '0', 'kb:category:list', '#', 'admin', NOW(), ''),
('文档管理',  @kb_pid, 2, 'document', 'kb/admin/document', 1, 0, 'C', '0', '0', 'kb:document:list', '#', 'admin', NOW(), ''),
('回收站',    @kb_pid, 3, 'recycle',  'kb/admin/recycle',  1, 0, 'C', '0', '0', 'kb:recycle:list',  '#', 'admin', NOW(), ''),
-- 阅读端（顶层显示，parent_id=0）
('知识库学习', 0,     5, 'view',     'kb/portal/index',    1, 0, 'C', '0', '0', 'kb:portal:view',  'reading', 'admin', NOW(), '');

-- 按钮级权限（供 sales/account 绑定，menu_type='F'，parent_id=0）
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('知识库必读入口', 0, 6, '', '', 1, 0, 'F', '0', '0', 'kb:portal:required', '#', 'admin', NOW(), ''),
('知识库文件下载', 0, 7, '', '', 1, 0, 'F', '0', '0', 'kb:file:download',  '#', 'admin', NOW(), ''),
('知识库文件上传', 0, 8, '', '', 1, 0, 'F', '0', '0', 'kb:file:upload',    '#', 'admin', NOW(), '');

-- ============================================================
-- Step 5: 角色绑定（admin / manager / sales / account）
-- ============================================================
-- admin：拿全部 KB 权限
INSERT INTO sys_role_menu(role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'kb:%';

-- manager：除 kb:recycle:purge 之外的全部（含 kb:recycle:list）
INSERT INTO sys_role_menu(role_id, menu_id)
SELECT 2, menu_id FROM sys_menu
WHERE perms LIKE 'kb:%' AND perms <> 'kb:recycle:purge';

-- sales / account：仅读 + 下载 + 上传
INSERT INTO sys_role_menu(role_id, menu_id)
SELECT 3, menu_id FROM sys_menu
WHERE perms IN ('kb:portal:view','kb:portal:required','kb:file:download','kb:file:upload');

INSERT INTO sys_role_menu(role_id, menu_id)
SELECT 4, menu_id FROM sys_menu
WHERE perms IN ('kb:portal:view','kb:portal:required','kb:file:download','kb:file:upload');

-- ============================================================
-- Step 6: Quartz 定时任务 — 30 天回收站清理
-- ============================================================
INSERT INTO sys_job(job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES
('知识库回收站清理', 'DEFAULT', 'kbRecycleCleanTask.cleanDaily()', '0 0 2 * * ?', '3', '0', '0', 'admin', NOW(), '每日 02:00 物理删除 30 天前进入回收站的文档');
