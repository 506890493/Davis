# 知识库模块实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 Davis 系统新增知识库模块（cms_kb_*），让管理员/经理上传操作手册、撰写富文本文章、上传录屏/图片，员工按目录浏览、学习、下载。

**Architecture:** 复用 RuoYi-Vue 体系（`@PreAuthorize` + `startPage/getDataTable` + `sys_menu` 动态路由 + 现有 `FileUtils` 上传到 `uploadPath`）。5 张新表 + 6 个 Controller + 4+3 个前端页 + 1 个 WangEditor 共享组件。文件统一走后端代理 `/kb/file/raw/{id}`，按 mime 决定 `inline`（image/video）还是 `attachment`（其他）。

**Tech Stack:** Java 8, Spring Boot 2.5.15, MyBatis, MySQL 8, Vue 2.6 + Element UI, WangEditor 5, JUnit 5 + Mockito + MockMvc

**Spec:** `docs/superpowers/specs/2026-06-11-knowledge-base-design.md`

---

## 文件结构（先于任务分解）

### 后端新增

```
ruoyi-system/src/main/java/com/ruoyi/system/
├── domain/
│   ├── CmsKbCategory.java
│   ├── CmsKbDocument.java
│   ├── CmsKbDocumentVersion.java
│   ├── CmsKbAttachment.java
│   └── CmsKbFile.java
├── mapper/
│   ├── CmsKbCategoryMapper.java
│   ├── CmsKbDocumentMapper.java
│   ├── CmsKbDocumentVersionMapper.java
│   ├── CmsKbAttachmentMapper.java
│   └── CmsKbFileMapper.java
└── service/
    ├── ICmsKbCategoryService.java
    ├── ICmsKbDocumentService.java
    ├── ICmsKbDocumentVersionService.java
    ├── ICmsKbAttachmentService.java
    ├── ICmsKbFileService.java
    ├── ICmsKbPortalService.java          # 阅读端聚合
    └── impl/
        ├── CmsKbCategoryServiceImpl.java
        ├── CmsKbDocumentServiceImpl.java
        ├── CmsKbDocumentVersionServiceImpl.java
        ├── CmsKbAttachmentServiceImpl.java
        ├── CmsKbFileServiceImpl.java
        └── CmsKbPortalServiceImpl.java

ruoyi-system/src/main/resources/mapper/system/
├── CmsKbCategoryMapper.xml
├── CmsKbDocumentMapper.xml
├── CmsKbDocumentVersionMapper.xml
├── CmsKbAttachmentMapper.xml
└── CmsKbFileMapper.xml

ruoyi-admin/src/main/java/com/ruoyi/web/controller/davis/
├── CmsKbCategoryController.java
├── CmsKbDocumentController.java
├── CmsKbVersionController.java
├── CmsKbRecycleController.java
├── CmsKbFileController.java              # 上传 + /raw/{id} 代理
└── CmsKbPortalController.java            # 阅读端

ruoyi-quartz/src/main/java/com/ruoyi/quartz/task/
└── KbRecycleCleanTask.java               # 30 天过期清理
```

### 前端新增

```
ruoyi-ui/src/
├── views/kb/
│   ├── admin/
│   │   ├── index.vue                     # 多 Tab 容器
│   │   ├── category.vue                  # 目录管理（树+拖拽）
│   │   ├── document.vue                  # 文档列表
│   │   ├── documentForm.vue              # 文档表单（文件 / 文章）
│   │   ├── version.vue                   # 版本时间线
│   │   └── recycle.vue                   # 回收站
│   ├── portal/
│   │   ├── index.vue                     # 左侧树 + 右侧列表
│   │   ├── detail.vue                    # 文档详情（图/视频/下载）
│   │   ├── required.vue                  # 新员工必读
│   │   └── components/
│   │       ├── KbImagePreview.vue
│   │       └── KbVideoPlayer.vue
│   └── system/kb/index.vue               # 系统管理菜单下的入口（导入 admin）
└── api/kb/
    ├── portal.js
    ├── category.js
    ├── document.js
    ├── version.js
    ├── recycle.js
    └── file.js
```

### 数据库 & 配置

```
sql/update_20260611_kb.sql                # 5 表 + 3 字典 + 4 默认分类 + 菜单 + 角色绑定
ruoyi-admin/src/test/resources/sql/data-init-kb.sql   # 测试种子（菜单+权限+4 分类）
```

---

## 实施阶段概览

| 阶段 | Task | 主题 | 估时 |
|---|---|---|---|
| P1 | T1 | 数据库脚本 | 0.5d |
| P2 | T2 | Domain + Mapper 层 | 1d |
| P3 | T3 | Service 层（业务规则 + 单元测试） | 1.5d |
| P4 | T4 | Controller 层（5 个管理 + 1 个门户 + 1 个文件代理） | 1.5d |
| P5 | T5 | 定时清理任务 | 0.5d |
| P6 | T6 | 前端：6 个 API 文件 + WangEditor 共享组件 | 0.5d |
| P7 | T7 | 前端：管理端 6 页 | 1.5d |
| P8 | T8 | 前端：阅读端 3 页 + 必读首页 Banner | 1d |
| P9 | T9 | 集成测试 + E2E | 2d |
| **合计** | | | **~10d** |

---

## Task 1: 数据库脚本（5 表 + 字典 + 默认数据 + 菜单）

**Files:**
- Create: `sql/update_20260611_kb.sql`
- Modify: `ruoyi-admin/src/test/resources/sql/data-init.sql`（追加 KB 测试种子）

- [ ] **Step 1: 创建 5 张 KB 表（cms_kb_category / cms_kb_file / cms_kb_document / cms_kb_document_version / cms_kb_attachment）**

```sql
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
    INDEX idx_doc (document_id, sort_num)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库-文章附件';
```

- [ ] **Step 2: 插入 3 个字典（kb_doc_type / kb_doc_status / kb_required）**

```sql
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
```

- [ ] **Step 3: 插入 4 个默认一级分类种子**

```sql
INSERT INTO cms_kb_category(parent_id, name, icon, order_num, is_required, status, create_by, create_time, remark)
VALUES
(0, '系统操作手册', 'documentation', 1, 1, 1, 'admin', NOW(), 'Davis 系统使用手册'),
(0, '代账知识',     'money',         2, 0, 1, 'admin', NOW(), '代账业务知识'),
(0, '会计知识',     'reading',       3, 0, 1, 'admin', NOW(), '财务/会计'),
(0, '工商知识',     'office-building',4, 0, 1, 'admin', NOW(), '公司注册/变更/注销');
```

- [ ] **Step 4: 插入菜单节点（管理端 + 阅读端）**

```sql
-- 父菜单：知识库（系统管理下，parent_id=1）
INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
('知识库', 1, 9, 'kb', null, 1, 0, 'M', '0', '0', '', 'documentation', 'admin', NOW(), '知识库目录');

-- 取刚插入的父菜单 ID（实际执行时通过 LAST_INSERT_ID() 获取）
SET @kb_pid = LAST_INSERT_ID();

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
-- 管理端
('目录管理',  @kb_pid, 1, 'category', 'kb/admin/category', 1, 0, 'C', '0', '0', 'kb:category:list', '#', 'admin', NOW(), ''),
('文档管理',  @kb_pid, 2, 'document', 'kb/admin/document', 1, 0, 'C', '0', '0', 'kb:document:list', '#', 'admin', NOW(), ''),
('回收站',    @kb_pid, 3, 'recycle',  'kb/admin/recycle',  1, 0, 'C', '0', '0', 'kb:recycle:list',  '#', 'admin', NOW(), ''),
-- 阅读端（顶层显示，parent_id=0）
('知识库学习', 0,     5, 'view',     'kb/portal/index',    1, 0, 'C', '0', '0', 'kb:portal:view',  'reading', 'admin', NOW(), '');
```

- [ ] **Step 5: 角色绑定（admin / manager / sales / account）**

```sql
-- admin：拿全部 KB 权限
INSERT INTO sys_role_menu(role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'kb:%';
-- manager：除 kb:recycle:purge 之外的全部
INSERT INTO sys_role_menu(role_id, menu_id)
SELECT 2, menu_id FROM sys_menu
WHERE perms LIKE 'kb:%' AND perms NOT IN ('kb:recycle:purge', 'kb:recycle:list');
-- sales / account：仅读 + 下载 + 上传
INSERT INTO sys_role_menu(role_id, menu_id)
SELECT 3, menu_id FROM sys_menu
WHERE perms IN ('kb:portal:view','kb:portal:required','kb:file:download','kb:file:upload');
INSERT INTO sys_role_menu(role_id, menu_id)
SELECT 4, menu_id FROM sys_menu
WHERE perms IN ('kb:portal:view','kb:portal:required','kb:file:download','kb:file:upload');
```

- [ ] **Step 6: 把 KB 测试种子追加到 `data-init.sql`（与原有种子合并）**

在 `ruoyi-admin/src/test/resources/sql/data-init.sql` 末尾追加：

```sql
-- ========== 知识库测试种子（Task 1 Step 6） ==========
DROP TABLE IF EXISTS cms_kb_category;
DROP TABLE IF EXISTS cms_kb_file;
DROP TABLE IF EXISTS cms_kb_attachment;
DROP TABLE IF EXISTS cms_kb_document_version;
DROP TABLE IF EXISTS cms_kb_document;
CREATE TABLE cms_kb_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0, name VARCHAR(64) NOT NULL, icon VARCHAR(255),
    order_num INT DEFAULT 0, is_required TINYINT(1) DEFAULT 0,
    status TINYINT(1) DEFAULT 1,
    create_by VARCHAR(64), create_time DATETIME, update_by VARCHAR(64), update_time DATETIME,
    del_flag TINYINT(1) DEFAULT 0
);
CREATE TABLE cms_kb_file (... 同上精简版 ...);
CREATE TABLE cms_kb_document (... 同上精简版 ...);
CREATE TABLE cms_kb_document_version (... 同上精简版 ...);
CREATE TABLE cms_kb_attachment (... 同上精简版 ...);

INSERT INTO cms_kb_category(parent_id, name, order_num, is_required) VALUES
(0,'系统操作手册',1,1),(0,'代账知识',2,0),(0,'会计知识',3,0),(0,'工商知识',4,0);

INSERT INTO sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon) VALUES
('知识库', 0, 99, 'view', 'kb/portal/index', 1, 0, 'C', '0', '0', 'kb:portal:view', 'reading');
SET @kb_mid = LAST_INSERT_ID();
INSERT INTO sys_role_menu(role_id, menu_id) VALUES (2,@kb_mid),(3,@kb_mid),(4,@kb_mid);
```

（BaseControllerTest 的 `@Sql(scripts = {"classpath:sql/data-init.sql"})` 自动覆盖，无需改测试基类。）

- [ ] **Step 7: 编译验证**

```bash
mvn compile -pl ruoyi-system -am -Dmaven.test.skip=true
```

预期：BUILD SUCCESS（SQL 文件不参与编译，但确保项目能正常 build）

- [ ] **Step 8: 手工验证 SQL（生产库）**

```bash
mysql -uroot -p davis-backend < sql/update_20260611_kb.sql
mysql -uroot -p davis-backend -e "SELECT * FROM cms_kb_category; SELECT * FROM sys_menu WHERE perms LIKE 'kb:%';"
```

预期：4 个分类、约 5 个菜单节点可见。

- [ ] **Step 9: 提交**

```bash
git add sql/update_20260611_kb.sql
git add ruoyi-admin/src/test/resources/sql/data-init.sql
git commit -m "feat(kb): 知识库 SQL 脚本 - 5 表 + 3 字典 + 4 默认分类 + 菜单

- 5 张 KB 表（cms_kb_category/document/document_version/attachment/file）
- 3 个字典（kb_doc_type/status/required）
- 4 个预置一级分类（系统操作手册/代账/会计/工商）
- 知识库菜单挂点 + 4 角色绑定
- 测试种子合并进 data-init.sql

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 2: Domain + Mapper 层（5 套 entity + mapper + xml）

**Files:**
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/domain/CmsKbCategory.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/domain/CmsKbDocument.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/domain/CmsKbDocumentVersion.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/domain/CmsKbAttachment.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/domain/CmsKbFile.java`
- Create: 5 个 mapper interface
- Create: 5 个 mapper xml

- [ ] **Step 1: 创建 `CmsKbCategory.java`**

参照 `CmsContract.java` 的写法（继承 `BaseEntity`）：

```java
package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 知识库-目录
 */
public class CmsKbCategory extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long parentId;
    private String name;
    private String icon;
    private Integer orderNum;
    private Integer isRequired;
    private Integer status;
    // getter / setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    public Integer getIsRequired() { return isRequired; }
    public void setIsRequired(Integer isRequired) { this.isRequired = isRequired; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
```

- [ ] **Step 2: 创建 `CmsKbDocument.java`**

字段：`id, categoryId, title, docType, summary, tags, coverImageId, primaryFileId, isRequired, status, publishedTime, viewCount, currentVersion, delFlag, deleteTime`（继承 BaseEntity，自带 create/update 字段）。

- [ ] **Step 3: 创建 `CmsKbDocumentVersion.java`**

字段：`id, documentId, versionNo, title, content, primaryFileId, summary, tags, saveReason, isCurrent`（继承 BaseEntity）。

- [ ] **Step 4: 创建 `CmsKbAttachment.java`**

字段：`id, documentId, versionId, fileId, displayName, sortNum`（继承 BaseEntity）。

- [ ] **Step 5: 创建 `CmsKbFile.java`**

字段：`id, originalName, storedName, relPath, fileSize, mimeType, sha256, bucket`（继承 BaseEntity）。

- [ ] **Step 6: 创建 5 个 Mapper interface**

每个 mapper 参照 `CmsContractMapper.java` 的标准写法，提供以下方法（以 `CmsKbCategoryMapper` 为例）：

```java
package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.CmsKbCategory;
import java.util.List;

public interface CmsKbCategoryMapper {
    CmsKbCategory selectById(Long id);
    List<CmsKbCategory> selectList(CmsKbCategory query);
    List<CmsKbCategory> selectChildren(Long parentId);
    int insert(CmsKbCategory category);
    int update(CmsKbCategory category);
    int updateOrderNum(@org.apache.ibatis.annotations.Param("id") Long id,
                       @org.apache.ibatis.annotations.Param("parentId") Long parentId,
                       @org.apache.ibatis.annotations.Param("orderNum") Integer orderNum);
    int deleteByIds(@org.apache.ibatis.annotations.Param("ids") Long[] ids);
    int countChildren(@org.apache.ibatis.annotations.Param("parentId") Long parentId);
    int countDocuments(@org.apache.ibatis.annotations.Param("categoryId") Long categoryId);
    List<CmsKbCategory> selectRequired();
}
```

其它 4 个 mapper 接口签名参考 spec §4（CRUD + 各自特殊方法）。

- [ ] **Step 7: 创建 5 个 Mapper XML**

`CmsKbCategoryMapper.xml` 示例：

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.ruoyi.system.mapper.CmsKbCategoryMapper">
    <resultMap id="BaseResultMap" type="CmsKbCategory">
        <id property="id" column="id"/>
        <result property="parentId" column="parent_id"/>
        <result property="name" column="name"/>
        <result property="icon" column="icon"/>
        <result property="orderNum" column="order_num"/>
        <result property="isRequired" column="is_required"/>
        <result property="status" column="status"/>
        <result property="createBy" column="create_by"/>
        <result property="createTime" column="create_time"/>
        <result property="updateBy" column="update_by"/>
        <result property="updateTime" column="update_time"/>
    </resultMap>

    <sql id="Base_Column_List">
        id, parent_id, name, icon, order_num, is_required, status,
        create_by, create_time, update_by, update_time
    </sql>

    <select id="selectById" resultMap="BaseResultMap" parameterType="long">
        SELECT <include refid="Base_Column_List"/>
        FROM cms_kb_category
        WHERE id = #{id} AND del_flag = 0
    </select>

    <select id="selectList" resultMap="BaseResultMap" parameterType="CmsKbCategory">
        SELECT <include refid="Base_Column_List"/>
        FROM cms_kb_category
        <where>
            del_flag = 0
            <if test="parentId != null">AND parent_id = #{parentId}</if>
            <if test="name != null and name != ''">AND name LIKE CONCAT('%', #{name}, '%')</if>
        </where>
        ORDER BY parent_id, order_num
    </select>

    <select id="selectChildren" resultMap="BaseResultMap" parameterType="long">
        SELECT <include refid="Base_Column_List"/>
        FROM cms_kb_category
        WHERE parent_id = #{parentId} AND del_flag = 0
        ORDER BY order_num
    </select>

    <insert id="insert" parameterType="CmsKbCategory" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO cms_kb_category(parent_id, name, icon, order_num, is_required, status,
                                    create_by, create_time, update_by, update_time)
        VALUES(#{parentId}, #{name}, #{icon}, #{orderNum}, #{isRequired}, #{status},
               #{createBy}, sysdate(), #{updateBy}, sysdate())
    </insert>

    <update id="update" parameterType="CmsKbCategory">
        UPDATE cms_kb_category
        <set>
            <if test="name != null">name = #{name},</if>
            <if test="icon != null">icon = #{icon},</if>
            <if test="orderNum != null">order_num = #{orderNum},</if>
            <if test="isRequired != null">is_required = #{isRequired},</if>
            <if test="status != null">status = #{status},</if>
            <if test="parentId != null">parent_id = #{parentId},</if>
            update_by = #{updateBy},
            update_time = sysdate()
        </set>
        WHERE id = #{id} AND del_flag = 0
    </update>

    <update id="updateOrderNum">
        UPDATE cms_kb_category
        SET parent_id = #{parentId}, order_num = #{orderNum}
        WHERE id = #{id} AND del_flag = 0
    </update>

    <update id="deleteByIds">
        UPDATE cms_kb_category SET del_flag = 1, update_time = sysdate()
        WHERE id IN
        <foreach collection="ids" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
    </update>

    <select id="countChildren" resultType="int">
        SELECT COUNT(1) FROM cms_kb_category WHERE parent_id = #{parentId} AND del_flag = 0
    </select>

    <select id="countDocuments" resultType="int">
        SELECT COUNT(1) FROM cms_kb_document WHERE category_id = #{categoryId} AND del_flag = 0
    </select>

    <select id="selectRequired" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM cms_kb_category
        WHERE del_flag = 0 AND status = 1 AND is_required = 1
        ORDER BY order_num
    </select>
</mapper>
```

其余 4 个 mapper xml 按相同模式补全（每个约 80-150 行）。

- [ ] **Step 8: 编译验证**

```bash
mvn compile -pl ruoyi-system -am -Dmaven.test.skip=true
```

预期：BUILD SUCCESS

- [ ] **Step 9: 提交**

```bash
git add ruoyi-system/src/main/java/com/ruoyi/system/domain/CmsKb*.java
git add ruoyi-system/src/main/java/com/ruoyi/system/mapper/CmsKb*.java
git add ruoyi-system/src/main/resources/mapper/system/CmsKb*.xml
git commit -m "feat(kb): Domain + Mapper 层 - 5 套 entity + mapper + xml

- CmsKbCategory / CmsKbDocument / CmsKbDocumentVersion / CmsKbAttachment / CmsKbFile
- 全部继承 BaseEntity，CRUD + 业务专用方法
- XML 走 MyBatis 标准 resultMap + 动态 SQL

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 3: Service 层（5 个 Service + 单元测试）

**Files:**
- Create: 5 个 service interface + impl（在 `service/` 与 `service/impl/`）
- Create: 5 个对应的 ServiceTest（在 `src/test/java/com/ruoyi/system/service/impl/` 或 `ruoyi-admin/src/test/.../davis/`）

- [ ] **Step 1: 写 `CmsKbCategoryServiceImpl` 业务规则 + 单测（先测后码 TDD）**

**Test:** `ruoyi-admin/src/test/java/com/ruoyi/web/controller/davis/CmsKbCategoryServiceTest.java`

```java
package com.ruoyi.web.controller.davis;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.CmsKbCategory;
import com.ruoyi.system.service.ICmsKbCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class CmsKbCategoryServiceTest extends BaseControllerTest {

    @Autowired private ICmsKbCategoryService service;

    @Test
    void delete_rejectsCategoryWithChildren() {
        // 假设 data-init-kb.sql 已插入"系统操作手册"（id=1）+ 其下一级子目录
        // 先新建子目录
        CmsKbCategory child = new CmsKbCategory();
        child.setParentId(1L);
        child.setName("子目录");
        child.setOrderNum(1);
        child.setIsRequired(0);
        child.setStatus(1);
        service.insert(child);

        // 删除父目录应当抛 ServiceException
        assertThrows(ServiceException.class, () -> service.deleteByIds(new Long[]{1L}));
    }

    @Test
    void delete_rejectsCategoryWithDocuments() {
        // 假设目录 2 下已有文档（测试 fixture 准备）
        assertThrows(ServiceException.class, () -> service.deleteByIds(new Long[]{2L}));
    }

    @Test
    void move_rejectsCircularReference() {
        // 把目录 2 移到自己的子目录下应当抛异常
        CmsKbCategory update = new CmsKbCategory();
        update.setId(2L);
        update.setParentId(99L); // 假设 99 是 2 的子目录
        assertThrows(ServiceException.class, () -> service.update(update));
    }
}
```

**Impl:** `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsKbCategoryServiceImpl.java`

```java
package com.ruoyi.system.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.CmsKbCategory;
import com.ruoyi.system.mapper.CmsKbCategoryMapper;
import com.ruoyi.system.service.ICmsKbCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CmsKbCategoryServiceImpl implements ICmsKbCategoryService {

    @Autowired private CmsKbCategoryMapper mapper;

    @Override
    public CmsKbCategory selectById(Long id) { return mapper.selectById(id); }

    @Override
    public List<CmsKbCategory> selectList(CmsKbCategory query) { return mapper.selectList(query); }

    @Override
    public List<CmsKbCategory> selectChildren(Long parentId) { return mapper.selectChildren(parentId); }

    @Override
    @Transactional
    public int insert(CmsKbCategory category) {
        if (category.getOrderNum() == null) {
            category.setOrderNum(0);
        }
        return mapper.insert(category);
    }

    @Override
    @Transactional
    public int update(CmsKbCategory category) {
        // 防止循环引用：父 ID 不能是自身，也不能是自身的子孙
        if (category.getParentId() != null && category.getId() != null) {
            if (category.getParentId().equals(category.getId())) {
                throw new ServiceException("KB_CATEGORY_LOOP", "不能把目录移动到自身下");
            }
            if (isDescendant(category.getParentId(), category.getId())) {
                throw new ServiceException("KB_CATEGORY_LOOP", "不能把目录移动到自己的子目录下");
            }
        }
        return mapper.update(category);
    }

    private boolean isDescendant(Long candidate, Long ancestor) {
        // 简单实现：递归查 candidate 的所有父链，看是否包含 ancestor
        Long pid = candidate;
        while (pid != null && pid != 0L) {
            CmsKbCategory c = mapper.selectById(pid);
            if (c == null) return false;
            if (c.getId().equals(ancestor)) return true;
            pid = c.getParentId();
        }
        return false;
    }

    @Override
    @Transactional
    public int updateOrder(Long id, Long parentId, Integer orderNum) {
        return mapper.updateOrderNum(id, parentId, orderNum);
    }

    @Override
    @Transactional
    public int deleteByIds(Long[] ids) {
        for (Long id : ids) {
            if (mapper.countChildren(id) > 0) {
                throw new ServiceException("KB_CATEGORY_NOT_EMPTY",
                    "目录[id=" + id + "]下还有子目录，请先清空");
            }
            if (mapper.countDocuments(id) > 0) {
                throw new ServiceException("KB_CATEGORY_NOT_EMPTY",
                    "目录[id=" + id + "]下还有文档，请先清空");
            }
        }
        return mapper.deleteByIds(ids);
    }

    @Override
    public List<CmsKbCategory> selectRequired() { return mapper.selectRequired(); }
}
```

（interface `ICmsKbCategoryService` 自行定义以上方法签名。）

- [ ] **Step 2: 写 `CmsKbDocumentServiceImpl` 业务规则 + 单测**

**Test:** 验证发布/下架状态机、版本生成、文件秒传（sha256 命中）、删除时软删并写 `delete_time`、草稿不可被阅读端看到。

**Impl 关键方法**：

```java
@Override
@Transactional
public Long insert(CmsKbDocument doc) {
    doc.setStatus(0);  // 默认草稿
    doc.setDelFlag(0);
    doc.setViewCount(0);
    doc.setCurrentVersion(1);
    mapper.insert(doc);
    // 创建版本 1
    CmsKbDocumentVersion v = new CmsKbDocumentVersion();
    v.setDocumentId(doc.getId());
    v.setVersionNo(1);
    v.setTitle(doc.getTitle());
    v.setContent(doc.getContent());
    v.setPrimaryFileId(doc.getPrimaryFileId());
    v.setSummary(doc.getSummary());
    v.setTags(doc.getTags());
    v.setSaveReason("自动");
    v.setIsCurrent(1);
    versionMapper.insert(v);
    return doc.getId();
}

@Override
@Transactional
public int update(CmsKbDocument doc) {
    // 1) 标记旧版本 is_current=0
    versionMapper.clearCurrent(doc.getId());
    // 2) 产生新版本
    int newVerNo = versionMapper.selectMaxVersionNo(doc.getId()) + 1;
    CmsKbDocumentVersion v = new CmsKbDocumentVersion();
    v.setDocumentId(doc.getId());
    v.setVersionNo(newVerNo);
    v.setTitle(doc.getTitle());
    v.setContent(doc.getContent());
    v.setPrimaryFileId(doc.getPrimaryFileId());
    v.setSummary(doc.getSummary());
    v.setTags(doc.getTags());
    v.setSaveReason("自动");
    v.setIsCurrent(1);
    versionMapper.insert(v);
    // 3) 更新主表 current_version
    doc.setCurrentVersion(newVerNo);
    return mapper.update(doc);
}

@Override
@Transactional
public int publish(Long id) {
    CmsKbDocument doc = mapper.selectById(id);
    if (doc == null) return 0;
    doc.setStatus(1);
    doc.setPublishedTime(new Date());
    return mapper.updateStatus(id, 1, new Date());
}

@Override
@Transactional
public int offline(Long id) { return mapper.updateStatus(id, 2, null); }

@Override
@Transactional
public int deleteByIds(Long[] ids) {
    return mapper.softDelete(ids, new Date());  // del_flag=1, delete_time=now
}
```

- [ ] **Step 3: 写 `CmsKbDocumentVersionServiceImpl` + 单测**

**关键方法**：

```java
@Override
@Transactional
public int rollback(Long documentId, Integer targetVersion) {
    // 1) 校验 targetVersion 存在
    CmsKbDocumentVersion target = versionMapper.selectByDocAndVer(documentId, targetVersion);
    if (target == null) throw new ServiceException("KB_VERSION_NOT_FOUND", "目标版本不存在");

    // 2) 校验不是当前版本
    CmsKbDocument doc = documentMapper.selectById(documentId);
    if (doc.getCurrentVersion().equals(targetVersion)) {
        throw new ServiceException("KB_VERSION_CURRENT", "已是当前版本，无需回滚");
    }

    // 3) 产生新版本（复制 target 内容，version_no=max+1）
    int newVerNo = versionMapper.selectMaxVersionNo(documentId) + 1;
    CmsKbDocumentVersion v = new CmsKbDocumentVersion();
    v.setDocumentId(documentId);
    v.setVersionNo(newVerNo);
    v.setTitle(target.getTitle());
    v.setContent(target.getContent());
    v.setPrimaryFileId(target.getPrimaryFileId());
    v.setSummary(target.getSummary());
    v.setTags(target.getTags());
    v.setSaveReason("回滚至 v" + targetVersion);
    v.setIsCurrent(1);
    versionMapper.insert(v);

    // 4) 主表 current_version
    documentMapper.updateCurrentVersion(documentId, newVerNo);
    return 1;
}
```

- [ ] **Step 4: 写 `CmsKbFileServiceImpl` + 单测**

**关键方法**（核心是 sha256 查重 + 后端代理响应）：

```java
@Override
@Transactional
public CmsKbFile registerFile(String originalName, InputStream is) throws IOException {
    // 1) 计算 sha256
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] buf = new byte[8192];
    int n;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    while ((n = is.read(buf)) > 0) {
        digest.update(buf, 0, n);
        baos.write(buf, 0, n);
    }
    String sha = Hex.encodeHexString(digest.digest());
    byte[] bytes = baos.toByteArray();

    // 2) 查重
    CmsKbFile existing = mapper.selectBySha(sha);
    if (existing != null) return existing;

    // 3) 落盘 /app/uploadPath/kb/yyyyMM/uuid.ext
    String yyyymm = new SimpleDateFormat("yyyyMM").format(new Date());
    String ext = FilenameUtils.getExtension(originalName);
    String stored = UUID.randomUUID().toString().replace("-", "") + "." + ext;
    String relPath = "kb/" + yyyymm + "/" + stored;
    File dest = new File(uploadPath, relPath);
    FileUtils.forceMkdirParent(dest);
    Files.write(dest.toPath(), bytes);

    // 4) 检测 mime（用 Files.probeContentType）
    String mime = Files.probeContentType(dest.toPath());
    if (mime == null) mime = "application/octet-stream";

    // 5) 写元数据
    CmsKbFile f = new CmsKbFile();
    f.setOriginalName(originalName);
    f.setStoredName(stored);
    f.setRelPath(relPath);
    f.setFileSize((long) bytes.length);
    f.setMimeType(mime);
    f.setSha256(sha);
    f.setBucket("kb");
    mapper.insert(f);
    return f;
}

@Override
public CmsKbFile selectById(Long id) { return mapper.selectById(id); }

/**
 * 根据 mime 返回 浏览器内联 还是 下载。
 * - image/* → inline
 * - video/* → inline
 * - 其他    → attachment
 */
public ResponseEntity<Resource> buildRawResponse(Long fileId) {
    CmsKbFile f = mapper.selectById(fileId);
    if (f == null || f.getDelFlag() == 1) {
        return ResponseEntity.notFound().build();
    }
    File file = new File(uploadPath, f.getRelPath());
    if (!file.exists()) return ResponseEntity.notFound().build();
    Resource resource = new FileSystemResource(file);
    boolean inline = f.getMimeType() != null &&
        (f.getMimeType().startsWith("image/") || f.getMimeType().startsWith("video/"));
    String disposition = inline ? "inline" : "attachment";
    String encoded = URLEncoder.encode(f.getOriginalName(), "UTF-8").replace("+", "%20");
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(f.getMimeType()))
        .contentLength(f.getFileSize())
        .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + encoded + "\"")
        .header(HttpHeaders.ACCEPT_RANGES, "bytes")  // 支持 Range
        .body(resource);
}
```

> 在 `application.yml` 里需要新增 `kb.upload-path: ${davis.kb.upload-path:${spring.servlet.multipart.location:/app/uploadPath}}`，
> Service 通过 `@Value("${kb.upload-path}")` 注入。

- [ ] **Step 5: 写 `CmsKbAttachmentServiceImpl`（简单 CRUD）**

仅实现 `insert / selectByDocumentId / deleteById`，逻辑直白。

- [ ] **Step 6: 写 `CmsKbPortalServiceImpl`（阅读端聚合）**

提供方法：
- `getTree()` → 整树（包含每个目录的文档数）
- `listPublished(categoryId, pageNum, pageNum)` → 走 PageHelper 走分页
- `getDetail(id)` → 拉文档 + 当前版本内容 + 附件列表（仅 status=1 可见）
- `listRequired()` → `is_required=1 AND status=1` 的文档
- `search(keyword)` → 标题/摘要/标签 LIKE

- [ ] **Step 7: 编译 + 跑所有 Service 单测**

```bash
mvn test -pl ruoyi-admin -am -Dtest="*Kb*ServiceTest" -Dmaven.test.skip=false
```

预期：所有 KB Service 测试通过。

- [ ] **Step 8: 提交**

```bash
git add ruoyi-system/src/main/java/com/ruoyi/system/service/
git add ruoyi-system/src/main/java/com/ruoyi/system/service/impl/
git add ruoyi-admin/src/test/java/com/ruoyi/web/controller/davis/CmsKb*ServiceTest.java
git commit -m "feat(kb): Service 层 - 业务规则 + 单元测试

- CmsKbCategoryServiceImpl：循环引用/有子节点/有文档 拒绝删除
- CmsKbDocumentServiceImpl：草稿/发布/下架 状态机 + 版本自动生成
- CmsKbDocumentVersionServiceImpl：回滚产生新版本
- CmsKbFileServiceImpl：sha256 秒传 + 后端代理 mime-based 响应
- CmsKbAttachmentServiceImpl / CmsKbPortalServiceImpl

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 4: Controller 层（5 个管理 + 1 个门户 + 1 个文件代理）

**Files:**
- Create: 6 个 controller（路径见文件结构）
- Create: 1 个 controller 集成测试 `CmsKbControllerIntegrationTest.java`

- [ ] **Step 1: `CmsKbCategoryController`**

```java
@RestController
@RequestMapping("/kb/category")
public class CmsKbCategoryController extends BaseController {

    @Autowired private ICmsKbCategoryService service;

    @PreAuthorize("@ss.hasPermi('kb:category:list')")
    @GetMapping("/list") public TableDataInfo list(CmsKbCategory q) {
        startPage(); return getDataTable(service.selectList(q));
    }

    @PreAuthorize("@ss.hasPermi('kb:category:query')")
    @GetMapping("/{id}") public AjaxResult getInfo(@PathVariable Long id) {
        return success(service.selectById(id));
    }

    @PreAuthorize("@ss.hasPermi('kb:category:add')")
    @Log(title = "知识库目录", businessType = BusinessType.INSERT)
    @PostMapping public AjaxResult add(@RequestBody CmsKbCategory c) {
        c.setCreateBy(getUsername()); c.setUpdateBy(getUsername());
        return toAjax(service.insert(c));
    }

    @PreAuthorize("@ss.hasPermi('kb:category:edit')")
    @Log(title = "知识库目录", businessType = BusinessType.UPDATE)
    @PutMapping public AjaxResult edit(@RequestBody CmsKbCategory c) {
        c.setUpdateBy(getUsername());
        return toAjax(service.update(c));
    }

    @PreAuthorize("@ss.hasPermi('kb:category:edit')")
    @PutMapping("/order")
    public AjaxResult order(@RequestBody List<CmsKbCategory> list) {
        for (CmsKbCategory c : list) service.updateOrder(c.getId(), c.getParentId(), c.getOrderNum());
        return success();
    }

    @PreAuthorize("@ss.hasPermi('kb:category:remove')")
    @Log(title = "知识库目录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}") public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(service.deleteByIds(ids));
    }
}
```

- [ ] **Step 2: `CmsKbDocumentController`**

类似上面，endpoints：
- `GET /list` (kb:document:list)
- `GET /{id}` (kb:document:query)
- `POST /` (kb:document:add)
- `PUT /` (kb:document:edit)
- `DELETE /{ids}` (kb:document:remove)
- `PUT /publish` body={id} (kb:document:publish)
- `PUT /offline` body={id} (kb:document:publish)

- [ ] **Step 3: `CmsKbVersionController`**

- `GET /{docId}` (kb:version:list) → List<VersionVo>
- `GET /{docId}/{ver}` (kb:version:list) → VersionDetailVo
- `POST /{docId}/{ver}/rollback` (kb:version:rollback)

- [ ] **Step 4: `CmsKbRecycleController`**

- `GET /list` (kb:recycle:list) → TableDataInfo（cms_kb_document where del_flag=1）
- `POST /restore` body=ids (kb:recycle:restore) → 设 del_flag=0, delete_time=null
- `DELETE /purge` body=ids (kb:recycle:purge) → 物理删 cms_kb_document_version + cms_kb_attachment + cms_kb_file（含物理文件），admin only

- [ ] **Step 5: `CmsKbFileController`（文件代理 + 上传）**

```java
@RestController
@RequestMapping("/kb/file")
public class CmsKbFileController extends BaseController {

    @Autowired private ICmsKbFileService service;

    @PreAuthorize("@ss.hasPermi('kb:file:upload')")
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.getSize() > 200 * 1024 * 1024L) {
            return AjaxResult.error("文件不能超过 200 MB");
        }
        CmsKbFile f = service.registerFile(file.getOriginalFilename(), file.getInputStream());
        return AjaxResult.success("上传成功", f);
    }

    @PreAuthorize("@ss.hasPermi('kb:file:download')")
    @GetMapping("/raw/{id}")
    public ResponseEntity<Resource> raw(@PathVariable Long id) {
        return service.buildRawResponse(id);
    }
}
```

- [ ] **Step 6: `CmsKbPortalController`（阅读端）**

- `GET /portal/tree` (kb:portal:view)
- `GET /portal/list?categoryId&pageNum&pageSize` (kb:portal:view)
- `GET /portal/detail/{id}` (kb:portal:view) — 仅返回 status=1
- `GET /portal/required` (kb:portal:required)
- `GET /portal/search?keyword&pageNum&pageSize` (kb:portal:view)

- [ ] **Step 7: 在 `BaseControllerTest` 增加 KB 角色权限**

修改 `getPermissionsForRole`，为 manager / sales / account 各加上对应的 KB 权限（与 SQL 角色绑定一致）。

- [ ] **Step 8: 写 Controller 集成测试 `CmsKbControllerIntegrationTest.java`**

```java
class CmsKbControllerIntegrationTest extends BaseControllerTest {

    @Test
    @DisplayName("manager 可发布文档")
    void testPublishDocument() throws Exception {
        // 1) 新建文档
        // 2) 调 PUT /kb/document/publish
        // 3) 验证 status=1, published_time 非空
    }

    @Test
    @DisplayName("sales 调 /kb/category/add 返 403")
    void testSalesCannotAddCategory() throws Exception {
        asSales(HttpMethod.POST, "/kb/category", new CmsKbCategory());
        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("草稿不可被阅读端看到")
    void testDraftNotVisible() throws Exception {
        // 1) manager 创建草稿
        // 2) 调 GET /kb/portal/detail/{id} 返 404
    }

    @Test
    @DisplayName("/kb/file/raw/{id} 对 image 返回 inline")
    void testRawImage() throws Exception {
        // 1) 上传 PNG 文件 → 拿到 fileId
        // 2) 调 GET /kb/file/raw/{id}，验证 Content-Disposition: inline
    }

    @Test
    @DisplayName("/kb/file/raw/{id} 对 docx 返回 attachment")
    void testRawDocx() throws Exception {
        // 1) 上传 docx → 拿到 fileId
        // 2) 验证 Content-Disposition: attachment
    }

    @Test
    @DisplayName("删除目录（无子节点）成功；删除有子节点的目录被拒")
    void testDeleteCategory() throws Exception {
        // ...
    }
}
```

- [ ] **Step 9: 编译 + 跑 Controller 集成测试**

```bash
mvn test -pl ruoyi-admin -am -Dtest=CmsKbControllerIntegrationTest -Dmaven.test.skip=false
```

预期：全部通过。

- [ ] **Step 10: 提交**

```bash
git add ruoyi-admin/src/main/java/com/ruoyi/web/controller/davis/CmsKb*.java
git add ruoyi-admin/src/test/java/com/ruoyi/web/controller/davis/BaseControllerTest.java
git add ruoyi-admin/src/test/java/com/ruoyi/web/controller/davis/CmsKbControllerIntegrationTest.java
git commit -m "feat(kb): Controller 层 - 6 端点 + 集成测试

- CmsKbCategoryController / DocumentController / VersionController / RecycleController
- CmsKbFileController（上传 + /raw/{id} mime-based 代理）
- CmsKbPortalController（阅读端）
- 集成测试覆盖权限矩阵 + 草稿隔离 + 文件代理头

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 5: 定时清理任务（30 天过期回收站）

**Files:**
- Create: `ruoyi-quartz/src/main/java/com/ruoyi/quartz/task/KbRecycleCleanTask.java`
- Create: `sql/update_20260611_kb_quartz.sql`（Quartz 任务 + cron 表达式）

- [ ] **Step 1: 实现 `KbRecycleCleanTask`**

```java
package com.ruoyi.quartz.task;

import com.ruoyi.system.service.ICmsKbRecycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component("kbRecycleCleanTask")
public class KbRecycleCleanTask {

    @Autowired private ICmsKbRecycleService recycleService;

    /**
     * 每天凌晨 2 点扫描 delete_time < now-30d 的文档，物理删除（连同文件）。
     * 调用方：Quartz 任务 kb:recycle:cleanDaily，cron 0 0 2 * * ?
     */
    public void cleanDaily() {
        int n = recycleService.purgeExpired(new Date());
        System.out.println("[KbRecycleCleanTask] cleaned " + n + " expired documents");
    }
}
```

（先在 `CmsKbRecycleService` 增加 `purgeExpired(Date now)` 方法：对 `cms_kb_document WHERE del_flag=1 AND delete_time < now-30d` 的每条记录，物理删 `cms_kb_document_version` / `cms_kb_attachment` / `cms_kb_file` 和物理文件。）

- [ ] **Step 2: 注入 Quartz 任务**

```sql
INSERT INTO sys_job(job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES
('知识库回收站清理', 'DEFAULT', 'kbRecycleCleanTask.cleanDaily()', '0 0 2 * * ?', '3', '0', '0', 'admin', NOW(), '每日 02:00 物理删除 30 天前进入回收站的文档');
```

- [ ] **Step 3: 单测 `KbRecycleCleanTaskTest`**

写一个集成测试，构造 1 条 `delete_time = 31 天前` 的软删文档，调 `cleanDaily()`，验证：
- 文档物理消失
- 关联的 `cms_kb_file.del_flag` 被设 1
- 物理文件被移到 `uploadPath/kb/_recycle/`

- [ ] **Step 4: 编译 + 测**

```bash
mvn test -pl ruoyi-quartz -am -Dtest=KbRecycleCleanTaskTest
mvn test -pl ruoyi-admin -am -Dtest=CmsKbControllerIntegrationTest#testRecyclePurge
```

预期：通过。

- [ ] **Step 5: 提交**

```bash
git add ruoyi-quartz/src/main/java/com/ruoyi/quartz/task/KbRecycleCleanTask.java
git add ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsKbRecycleServiceImpl.java
git add sql/update_20260611_kb_quartz.sql
git add ruoyi-admin/src/test/java/com/ruoyi/web/controller/davis/KbRecycleCleanTaskTest.java
git commit -m "feat(kb): 30 天回收站定时清理任务

- KbRecycleCleanTask.cleanDaily() 物理删除过期文档
- Quartz 任务 cron 0 0 2 * * ?
- 关联文件软删 + 物理文件移到 _recycle/
- 单测验证 31 天前文档被清理

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 6: 前端 API + WangEditor 共享组件

**Files:**
- Create: 6 个 API 文件（`ruoyi-ui/src/api/kb/{portal,category,document,version,recycle,file}.js`）
- Create: `ruoyi-ui/src/components/WangEditor/index.vue`
- Create: `ruoyi-ui/src/components/WangEditor/upload.js`（自定义上传到 /kb/file/upload）

- [ ] **Step 1: 安装 WangEditor**

```bash
cd ruoyi-ui
npm install @wangeditor/editor@5 --save
npm install @wangeditor/editor-for-vue@5 --save
```

- [ ] **Step 2: 6 个 API 文件**

以 `category.js` 为例：

```javascript
import request from '@/utils/request';

// 查询目录列表
export function listCategory(query) {
  return request({
    url: '/kb/category/list',
    method: 'get',
    params: query
  });
}

// 查询目录详情
export function getCategory(id) {
  return request({ url: '/kb/category/' + id, method: 'get' });
}

// 新增目录
export function addCategory(data) {
  return request({ url: '/kb/category', method: 'post', data });
}

// 修改目录
export function updateCategory(data) {
  return request({ url: '/kb/category', method: 'put', data });
}

// 拖拽排序
export function orderCategory(data) {
  return request({ url: '/kb/category/order', method: 'put', data });
}

// 删除目录
export function delCategory(ids) {
  return request({ url: '/kb/category/' + ids, method: 'delete' });
}
```

其它 5 个文件按相同模式照搬后端 endpoint（portal.js、document.js、version.js、recycle.js、file.js）。

- [ ] **Step 3: `WangEditor` 共享组件**

`ruoyi-ui/src/components/WangEditor/index.vue`：

```vue
<template>
  <div ref="editorRef" :style="{ height: height + 'px' }"></div>
</template>

<script>
import '@wangeditor/editor/dist/css/style.css';
import { onBeforeUnmount, ref, shallowRef, watch } from 'vue';
import { Editor, Toolbar } from '@wangeditor/editor-for-vue';
import { uploadFile } from './upload';

export default {
  name: 'WangEditor',
  props: {
    modelValue: { type: String, default: '' },
    height: { type: Number, default: 400 }
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const editorRef = ref(null);
    const editor = shallowRef(null);

    const handleCreated = (e) => { editor.value = e; };

    watch(() => props.modelValue, (v) => {
      if (editor.value && v !== editor.value.getHtml()) {
        editor.value.setHtml(v);
      }
    });

    onBeforeUnmount(() => {
      if (editor.value) editor.value.destroy();
    });

    return {
      editorRef,
      handleCreated,
      editorConfig: {
        placeholder: '请输入内容...',
        MENU_CONF: {
          uploadImage: { customUpload: uploadFile },
          uploadVideo: { customUpload: uploadFile }
        }
      }
    };
  }
};
</script>
```

`upload.js`：

```javascript
import request from '@/utils/request';
import { ElMessage } from 'element-ui';

export function uploadFile(file, insertFn) {
  const fd = new FormData();
  fd.append('file', file);
  request({
    url: '/kb/file/upload',
    method: 'post',
    data: fd,
    headers: { 'Content-Type': 'multipart/form-data' }
  }).then(res => {
    if (res.code === 200) {
      const url = '/kb/file/raw/' + res.data.id;
      insertFn(url, file.name, url);
    } else {
      ElMessage.error(res.msg || '上传失败');
    }
  }).catch(() => ElMessage.error('上传失败'));
}
```

- [ ] **Step 4: 验证构建**

```bash
cd ruoyi-ui
npm run build
```

预期：构建成功，无 ts/eslint 报错。

- [ ] **Step 5: 提交**

```bash
git add ruoyi-ui/src/api/kb/
git add ruoyi-ui/src/components/WangEditor/
git add ruoyi-ui/package.json
git add ruoyi-ui/package-lock.json
git commit -m "feat(kb): 前端 API + WangEditor 共享组件

- 6 个 API 文件（portal/category/document/version/recycle/file）
- WangEditor 5 集成：富文本 + 图片/视频自定义上传
- upload.js 走 /kb/file/upload 拿 fileId → /kb/file/raw/{id}

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 7: 前端管理端（6 页）

**Files:**
- Create: `ruoyi-ui/src/views/kb/admin/{index,category,document,documentForm,version,recycle}.vue`

- [ ] **Step 1: `index.vue`（多 Tab 容器）**

```vue
<template>
  <el-tabs v-model="activeTab" type="border-card">
    <el-tab-pane label="目录管理" name="category">
      <category-tab v-if="activeTab==='category'"/>
    </el-tab-pane>
    <el-tab-pane label="文档管理" name="document">
      <document-tab v-if="activeTab==='document'"/>
    </el-tab-pane>
    <el-tab-pane label="回收站" name="recycle">
      <recycle-tab v-if="activeTab==='recycle'"/>
    </el-tab-pane>
  </el-tabs>
</template>
```

- [ ] **Step 2: `category.vue`（el-tree + 拖拽）**

要点：
- 用 `el-tree` 展示目录（懒加载 `load`）
- 节点支持「新增子目录 / 编辑 / 删除 / 必读切换」
- 节点用 `draggable` 开启拖拽；`@node-drop` 调 `orderCategory` 批量更新

- [ ] **Step 3: `document.vue`（文档列表）**

要点：
- 顶部筛选：目录、状态（草稿/已发布/已下架）、关键字
- 表格列：标题、类型（字典）、目录、状态（字典）、必读、发布时间、操作（编辑/发布/下架/删除/历史）
- 「上传文件文档」按钮 → 弹出 `documentForm`（type=1）
- 「撰写文章」按钮 → 弹出 `documentForm`（type=2）

- [ ] **Step 4: `documentForm.vue`（核心表单）**

要点：
- 公共字段：标题、目录（级联选择）、必读开关、摘要、标签、封面图（图片上传走 `/kb/file/upload`）
- `docType=1`：主文件上传（走 `/kb/file/upload`，显示已选文件名+大小）
- `docType=2`：嵌入 `<WangEditor v-model="form.content">` + 附件管理（多文件上传）
- 提交：`POST /kb/document`（新增） / `PUT /kb/document`（编辑）

- [ ] **Step 5: `version.vue`（版本时间线）**

要点：
- 左侧 `el-timeline` 展示 `cms_kb_document_version` 列表（带版本号、save_reason、create_time、create_by）
- 点击某个版本 → 右侧展示 `content`（富文本渲染）/ `primaryFileId` 文件信息
- 「回滚到此版本」按钮 → 弹确认 → 调 `POST /kb/version/{docId}/{ver}/rollback`

- [ ] **Step 6: `recycle.vue`**

要点：
- 表格列：标题、原目录、删除时间、删除人、剩余天数（`30 - (now - delete_time)`）
- 行内按钮：恢复、永久删除（admin 可见）
- 列表来源：`GET /kb/recycle/list`

- [ ] **Step 7: 在 `system/kb/index.vue` 中引入 admin 容器**

简单 import + 路由跳转（或直接 `<router-view>` 组件重定向）。

- [ ] **Step 8: 构建验证**

```bash
cd ruoyi-ui
npm run build
```

预期：构建成功。

- [ ] **Step 9: 提交**

```bash
git add ruoyi-ui/src/views/kb/admin/
git add ruoyi-ui/src/views/system/kb/
git commit -m "feat(kb): 前端管理端 6 页（目录/文档/表单/版本/回收站）

- el-tree + 拖拽排序目录
- 文档列表 + 多状态过滤
- documentForm 支持文件/富文本双模式
- 版本时间线 + 回滚确认
- 回收站 30 天倒计时 + 恢复/永久删除

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 8: 前端阅读端（3 页 + 必读首页 Banner）

**Files:**
- Create: `ruoyi-ui/src/views/kb/portal/{index,detail,required,components/KbImagePreview,components/KbVideoPlayer}.vue`
- Modify: `ruoyi-ui/src/views/index.vue`（首页加「必读」Banner）

- [ ] **Step 1: `index.vue`（阅读端首页）**

要点：
- 左侧 el-tree 展示已发布目录（用 `GET /kb/portal/tree`）
- 右侧 list 已发布文档（`GET /kb/portal/list?categoryId=...`）
- 顶部搜索框（`GET /kb/portal/search?keyword=...`）

- [ ] **Step 2: `detail.vue`（文档详情）**

要点：
- 顶部面包屑：知识库 / 目录 / 文档标题
- docType=1 → 主卡片显示主文件名+大小+「下载」按钮（链 `/kb/file/raw/{id}`，由后端决定 attachment）
- docType=2 → 渲染富文本（v-html），下方附件列表（文件卡片）
- 富文本中的 `<img src>` 自动走 `/kb/file/raw/{id}` 代理；`<video src>` 同理
- 下方「相关推荐」：同目录下其他 5 条已发布文档

- [ ] **Step 3: `required.vue`（新员工必读）**

要点：
- 顶部 Banner：「📌 新员工必读」
- 列表：所有 `is_required=1` 的目录+文档

- [ ] **Step 4: `KbImagePreview.vue`**

```vue
<template>
  <el-image
    :src="src"
    :preview-src-list="[src]"
    :initial-index="0"
    fit="contain"
    style="max-width: 100%"
  />
</template>
<script>
export default { props: ['src'] };
</script>
```

- [ ] **Step 5: `KbVideoPlayer.vue`**

```vue
<template>
  <video :src="src" controls preload="metadata" style="max-width: 100%">
    您的浏览器不支持 HTML5 视频，请<a :href="src">下载</a>。
  </video>
</template>
<script>
export default { props: ['src'] };
</script>
```

- [ ] **Step 6: 在 `views/index.vue` 首页加必读 Banner**

```vue
<el-card v-if="kbRequired.length" class="kb-required-banner">
  <div slot="header"><i class="el-icon-star-on"></i> 新员工必读</div>
  <el-row :gutter="16">
    <el-col v-for="d in kbRequired" :key="d.id" :span="6">
      <el-card shadow="hover" @click.native="$router.push('/kb/view/detail/' + d.id)">
        <div class="title">{{ d.title }}</div>
        <div class="meta">{{ d.categoryName }}</div>
      </el-card>
    </el-col>
  </el-row>
</el-card>
```

在 `created()` 中调 `listRequired()` 加载。

- [ ] **Step 7: 构建验证**

```bash
cd ruoyi-ui
npm run build
```

预期：构建成功。

- [ ] **Step 8: 提交**

```bash
git add ruoyi-ui/src/views/kb/portal/
git add ruoyi-ui/src/views/index.vue
git commit -m "feat(kb): 前端阅读端 3 页 + 必读首页 Banner

- 左侧树 + 右侧列表 + 搜索
- 文档详情（图片预览/视频播放/附件下载）
- 必读 Banner 出现在首页顶部
- KbImagePreview / KbVideoPlayer 组件

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 9: 集成测试 + E2E 全链路验证

**Files:**
- Create: `ruoyi-admin/src/test/java/com/ruoyi/web/controller/davis/KnowledgeBaseE2ETest.java`

- [ ] **Step 1: 写全链路 E2E**

```java
class KnowledgeBaseE2ETest extends BaseControllerTest {

    @Test
    @DisplayName("E2E: 新员工首登 → 必读 → 钻目录 → 读文章 → 预览图片 → 播放录屏 → 下载手册")
    void testNewEmployeeOnboarding() throws Exception {
        // 1) admin 预置数据：4 分类 + 1 篇必读文章 + 1 张图片 + 1 个录屏 + 1 份 Word 手册
        // 2) 以 sales 身份登录，调 GET /kb/portal/required 验证能看见
        // 3) GET /kb/portal/tree 验证树形
        // 4) GET /kb/portal/list?categoryId=1 验证已发布列表
        // 5) GET /kb/portal/detail/{articleId} 验证 status=1 可见
        // 6) GET /kb/file/raw/{imageId} 验证 Content-Disposition=inline
        // 7) GET /kb/file/raw/{videoId} 验证 Content-Disposition=inline
        // 8) GET /kb/file/raw/{wordId} 验证 Content-Disposition=attachment
    }

    @Test
    @DisplayName("E2E: manager 上传文件→发布→全员可见→编辑→保存版本→回滚→对比")
    void testManagerPublishAndRollback() throws Exception {
        // 1) manager POST /kb/document (status=0 草稿)
        // 2) PUT /kb/document/publish
        // 3) sales 调 GET /kb/portal/detail/{id} 可见
        // 4) manager PUT /kb/document (产生 v2)
        // 5) GET /kb/version/{id} 看到 v1 + v2
        // 6) POST /kb/version/{id}/1/rollback 产生 v3 (复制 v1)
        // 7) GET /kb/version/{id}/3 内容与 v1 一致
    }

    @Test
    @DisplayName("E2E: admin 删除→回收站→恢复→再删除→30 天过期清理")
    void testRecycleFlow() throws Exception {
        // 1) admin 创建文档
        // 2) DELETE /kb/document/{id} → 软删
        // 3) GET /kb/recycle/list 看到
        // 4) POST /kb/recycle/restore/{id} → 恢复
        // 5) 再删 → 模拟 delete_time = 31 天前 → 调 cleanDaily() → 物理消失
    }

    @Test
    @DisplayName("E2E: 权限矩阵 — sales 仅可读+下载+上传；account 同；manager 不可物理删；admin 可")
    void testPermissionMatrix() throws Exception {
        // 矩阵表 + 11 条断言
    }
}
```

- [ ] **Step 2: 跑全量 KB 测试**

```bash
mvn test -pl ruoyi-admin -am -Dtest="*Kb*,KnowledgeBaseE2ETest" -Dmaven.test.skip=false
```

预期：全部通过（目标 ≥ 90% 行覆盖）。

- [ ] **Step 3: 跑全量回归**

```bash
mvn test -pl ruoyi-admin -am -Dmaven.test.skip=false
```

预期：原有测试不受影响（基线绿）。

- [ ] **Step 4: 提交**

```bash
git add ruoyi-admin/src/test/java/com/ruoyi/web/controller/davis/KnowledgeBaseE2ETest.java
git commit -m "test(kb): E2E 全链路测试

- 新员工首登 → 必读 → 钻目录 → 读文章 → 预览/播放/下载
- manager 发布/编辑/版本/回滚全流程
- 回收站恢复/清理全流程
- 4 角色 × 13 权限点矩阵验证

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 10: 端到端手工验证 + 上线

- [ ] **Step 1: 启动后端 + 前端，本地联调**

```bash
# 后端
mvn clean package -Dmaven.test.skip=true
java -jar ruoyi-admin/target/ruoyi-admin.jar

# 前端
cd ruoyi-ui
npm run dev
```

- [ ] **Step 2: 浏览器手工验证清单**

- [ ] admin 登录 → 「系统管理 → 知识库」菜单可见
- [ ] 「目录管理」可看到 4 个预置分类
- [ ] 「文档管理」可新建文件型文档（上传 PDF）→ 发布 → 草稿/已发布/已下架过滤正确
- [ ] 「文档管理」可新建文章型文档（WangEditor 写富文本 + 插图 + 插视频）→ 发布
- [ ] 「版本管理」可看到 v1；编辑后看到 v2；回滚 v1 → 产生 v3
- [ ] 「回收站」可看到 30 天倒计时
- [ ] manager 登录：「知识库学习」可见，可浏览所有发布文档
- [ ] sales 登录：可浏览 + 上传 + 看不到「目录管理」/「文档管理」
- [ ] account 登录：行为同 sales
- [ ] 任意文档详情：图片内联预览、录屏内联播放、Word 触发下载
- [ ] 首页：必读 Banner 出现

- [ ] **Step 3: 提交到 git + 创建 PR**

```bash
git checkout -b feature/knowledge-base
git push origin feature/knowledge-base
gh pr create --title "feat: 知识库模块" --body "..."
```

---

## 自检清单

对照设计稿验收标准（`docs/superpowers/specs/2026-06-11-knowledge-base-design.md`）：

- [ ] §2 决策全部落地（文件/富文本/WangEditor/单树/4 分类/LIKE 搜索/mime 代理/草稿-发布-下架/30 天/版本/必读）
- [ ] §4 数据模型 5 表 + 3 字典均建好
- [ ] §5 权限矩阵（admin 全 / manager 除 purge / sales+account 只读+上传+下载）
- [ ] §6 菜单挂点（系统管理 → 知识库 + 4 子页 + 阅读端）
- [ ] §7 接口（portal/category/document/version/recycle/file/raw 全部 endpoint）
- [ ] §8 前端结构（6 管理 + 3 阅读 + WangEditor）
- [ ] §9 错误处理（KB_CATEGORY_LOOP、sha256 复用、200MB、Range、EXIF 去除）
- [ ] §10 测试（Mapper/Service/Controller/E2E，每层 ≥ 90% 覆盖）
- [ ] §11 实施拆分（10 task，~10 人天）
- [ ] §12 风险（防 XSS、Range、mime 白名单、无 DRM）
