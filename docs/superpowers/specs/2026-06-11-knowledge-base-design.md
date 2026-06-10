# 知识库（Knowledge Base）设计方案

> 日期：2026-06-11
> 适用范围：Davis（拓荒牛）— RuoYi-Vue v3.9.0
> 状态：待复核 → 通过后由 writing-plans 生成实施计划

---

## 1. 背景与目标

为 Davis 系统新增**知识库模块**，让管理员 / 经理可上传操作手册、撰写教学文章、上传教学视频，供新员工（以及全体销售/会计/经理）按目录学习代账、工商、会计等专业知识。

核心要求：

- 内容沉淀在 `davis-backend` 库，**不依赖外部 SaaS**；
- 管理员/经理（非技术人员）**可视化操作**即可维护；
- 内容支持**文件 + 富文本文章**两种形态；
- 单树多级目录，预置 4 个一级分类；
- 具备**草稿/发布、回收站、版本历史、「新员工必读」置顶** 4 项能力；
- 与现有 RuoYi 体系（菜单、权限、操作日志、字典、分页、文件上传）**完全复用**。

---

## 2. 需求摘要

| 维度 | 决策 |
|---|---|
| 内容形态 | 文件上传（PDF/Word/Excel/PPT/MP4） + 站内富文本文章 |
| 视频/文件存储 | 全部站内上传到 `uploadPath/kb/yyyyMM/` |
| 富文本编辑器 | WangEditor（npm 包） |
| 目录模型 | 单树多级 + 预置 4 个一级分类 |
| 可见性 | 全部已登录员工（不按角色隔离） |
| 搜索 | MySQL LIKE（标题 / 摘要 / 标签） |
| 在线预览 | 仅 `image/*` 走图片预览；`video/*` 走视频播放（HTML5 `<video>`，支持倍速/全屏）；其他 mime 走下载 |
| 状态机 | 草稿 / 已发布 / 已下架 |
| 回收站 | 30 天可恢复，过期物理清理 |
| 版本历史 | 每次保存生成新版本，可回滚（回滚 = 产生新版本号） |
| 必读置顶 | 目录级 + 文档级 `is_required` 联合判定 |
| 默认分类 | 系统操作手册 / 代账知识 / 会计知识 / 工商知识 |

---

## 3. 架构总览

```
┌─────────────────────────────────────────────────────────────────┐
│   前端 (Vue 2.6 + Element UI)                                    │
│   ┌──────────────┐   ┌──────────────┐   ┌─────────────────────┐ │
│   │ 阅读端 Portal │   │ 管理端 (kb-admin) │  │ 共享组件 (WangEditor)│
│   │ /kb/view     │   │ /kb/admin    │   │ components/WangEditor│
│   └──────────────┘   └──────────────┘   └─────────────────────┘ │
└──────────────────────────────┬──────────────────────────────────┘
                               │  Axios
┌──────────────────────────────┴──────────────────────────────────┐
│   后端 (Spring Boot 2.5.15)                                      │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │ CmsKbPortalController     阅读端 API（全员可访问）         │   │
│   │ CmsKbCategoryController   目录 CRUD + 拖拽排序            │   │
│   │ CmsKbDocumentController   文档 CRUD + 发布/下架           │   │
│   │ CmsKbVersionController    版本历史 + 回滚                │   │
│   │ CmsKbRecycleController    回收站（30 天可恢复）           │   │
│   └─────────────────────────────────────────────────────────┘   │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │ 复用 RuoYi 既有能力                                       │   │
│   │ • FileUtils / common/upload  文件上传到 /app/uploadPath   │   │
│   │ • @PreAuthorize + @ss.hasPermi  权限校验                  │   │
│   │ • startPage / getDataTable  分页                          │   │
│   │ • @Log(title=..., businessType=...)  操作日志              │   │
│   │ • SysDictDataServiceImpl  字典（文档类型/状态/必读）       │   │
│   └─────────────────────────────────────────────────────────┘   │
└──────────────────────────────┬──────────────────────────────────┘
                               │  MyBatis
┌──────────────────────────────┴──────────────────────────────────┐
│   MySQL davis-backend                                            │
│   cms_kb_category / cms_kb_document / cms_kb_document_version    │
│   cms_kb_attachment / cms_kb_file (文件元数据)                    │
│   物理文件: /app/uploadPath/kb/{yyyyMM}/{uuid}.{ext}             │
└─────────────────────────────────────────────────────────────────┘
```

### 3.1 模块拆分（单一职责）

| 模块 | 职责 | 不做什么 |
|---|---|---|
| `kb-category` | 目录树的 CRUD/排序/必读标记 | 不涉及文档内容 |
| `kb-document` | 文档主表（标题/类型/状态/必读） | 不存正文/文件二进制 |
| `kb-version` | 富文本内容 + 文件引用 + 历史快照 | 不存目录 |
| `kb-attachment` | 文章内嵌附件元数据 + 引用 file | 不直接管文件 |
| `kb-recycle` | 30 天软删除/恢复/真删 | 不涉及历史 |
| `kb-file` | 复用 RuoYi upload 路径，记录元数据 | 不与业务耦合 |
| `kb-portal`（阅读端） | 浏览/搜索/下载/必读入口 | 不含管理操作 |

---

## 4. 数据模型

> 所有表名 `cms_kb_*`，沿用项目前缀。所有 `del_flag` 字段为软删标记。所有表均带 `create_by / create_time / update_by / update_time`。

### 4.1 `cms_kb_category` — 知识库目录

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT PK | |
| `parent_id` | BIGINT | 0=一级 |
| `name` | VARCHAR(64) | |
| `icon` | VARCHAR(255) | Element UI 图标名 |
| `order_num` | INT | 排序 |
| `is_required` | TINYINT(1) | 新员工必读：0否 1是 |
| `status` | TINYINT(1) | 0停用 1正常 |
| `del_flag` | TINYINT(1) | 0正常 1删除 |

索引：`(parent_id, del_flag, order_num)`

### 4.2 `cms_kb_file` — 文件元数据

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT PK | |
| `original_name` | VARCHAR(255) | 原始文件名 |
| `stored_name` | VARCHAR(255) | uuid.ext |
| `rel_path` | VARCHAR(512) | `kb/202606/uuid.pdf` |
| `file_size` | BIGINT | 字节 |
| `mime_type` | VARCHAR(128) | |
| `sha256` | CHAR(64) | 查重 / 秒传 |
| `bucket` | VARCHAR(32) | 默认 `kb` |
| `del_flag` | TINYINT(1) | |

唯一键：`(sha256, del_flag)`

### 4.3 `cms_kb_document` — 文档主表

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT PK | |
| `category_id` | BIGINT | |
| `title` | VARCHAR(255) | |
| `doc_type` | TINYINT | 1=文件 2=富文本文章 |
| `summary` | VARCHAR(500) | 摘要（搜索用） |
| `tags` | VARCHAR(255) | 逗号分隔 |
| `cover_image_id` | BIGINT | 封面图（引用 cms_kb_file.id） |
| `primary_file_id` | BIGINT | 主文件（doc_type=1 时） |
| `is_required` | TINYINT(1) | 新员工必读 |
| `status` | TINYINT | 0草稿 1已发布 2已下架 |
| `published_time` | DATETIME | |
| `view_count` | INT | |
| `current_version` | INT | 当前指向的版本号 |
| `del_flag` | TINYINT(1) | |
| `delete_time` | DATETIME | 回收站清理依据 |

索引：`(category_id, status, del_flag, order_num)`

### 4.4 `cms_kb_document_version` — 文档版本

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT PK | |
| `document_id` | BIGINT | |
| `version_no` | INT | 1, 2, 3 … |
| `title` | VARCHAR(255) | |
| `content` | LONGTEXT | doc_type=2 时的富文本 HTML |
| `primary_file_id` | BIGINT | doc_type=1 时的文件快照 |
| `summary` | VARCHAR(500) | |
| `tags` | VARCHAR(255) | |
| `save_reason` | VARCHAR(255) | 自动 / 手动 / 回滚 |
| `is_current` | TINYINT(1) | 是否当前版本 |

唯一键：`(document_id, version_no)`

### 4.5 `cms_kb_attachment` — 文章内嵌附件

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT PK | |
| `document_id` | BIGINT | |
| `version_id` | BIGINT | 随版本快照 |
| `file_id` | BIGINT | 引用 cms_kb_file.id |
| `display_name` | VARCHAR(255) | |
| `sort_num` | INT | |

索引：`(document_id, sort_num)`

### 4.6 字典（sys_dict_type / sys_dict_data）

- `kb_doc_type`：1=文件、2=富文本文章
- `kb_doc_status`：0=草稿、1=已发布、2=已下架
- `kb_required`：0=否、1=新员工必读

### 4.7 设计要点

1. **文件与版本解耦**：`cms_kb_file` 是物理文件元数据；版本表只存 `file_id` 引用。文件本身不会被版本覆盖。
2. **每次发布/保存生成新版本**：`is_current=1` 标记当前版本；回滚 = 把另一个版本号置为 `is_current=1`，并新增 `version_no=max+1` 的快照。
3. **回收站用 `del_flag + delete_time`**：定时任务每天扫描 `delete_time < now-30d` 的文档，连带文件移到 `/app/uploadPath/kb/_recycle/`。
4. **目录树扁平 + 索引**：`parent_id` 即可支持无限层级；通过 `(parent_id, del_flag, order_num)` 索引保证拖拽排序查询高效。
5. **必读数据来源**：`category.is_required` ∪ `document.is_required`，任意一个 = 1 都会进入新员工首页。
6. **搜索字段**：标题 + 摘要 + 标签走 MySQL LIKE；后续量大可加 FULLTEXT（ngram）。

---

## 5. 权限模型

完全沿用 RuoYi 的 `@PreAuthorize("@ss.hasPermi('kb:*')")` 模式，新增以下权限点：

| 权限字符串 | 默认绑定角色 | 说明 |
|---|---|---|
| `kb:portal:view` | 全部已登录角色 | 阅读端入口 |
| `kb:category:list` / `query` / `add` / `edit` / `remove` | admin、manager | 目录管理 |
| `kb:document:list` / `query` / `add` / `edit` / `remove` / `publish` / `offline` | admin、manager | 文档 CRUD + 发布/下架 |
| `kb:version:list` / `rollback` | admin、manager | 版本历史与回滚 |
| `kb:recycle:list` / `restore` / `purge` | admin | 回收站管理 |
| `kb:file:upload` | admin、manager、sales、account | 文件上传（自己可创建草稿） |
| `kb:file:download` | 全部已登录角色 | 文件下载 |
| `kb:portal:required` | 全部已登录角色 | 新员工必读入口 |

> 落地 `sys_role_menu`：
> - **admin**：拿全部权限点（含 `kb:recycle:purge`）。
> - **manager**：拿除 `kb:recycle:purge` 之外的全部权限点（即 `kb:portal:*`、`kb:category:*`、`kb:document:*`、`kb:version:*`、`kb:recycle:list/restore`、`kb:file:upload/download`）。
> - **sales / account**：仅 `kb:portal:view`、`kb:portal:required`、`kb:file:download`、`kb:file:upload`（可创建自己的草稿，但不参与审核/发布他人内容）。

---

## 6. 菜单挂点

新增两个菜单节点（自动随 `/getRouters` 推送到前端）：

```
系统管理
└── 知识库                    (icon: documentation, path: kb)
    ├── 知识库管理            (component: kb/admin/index)
    │   ├── 目录管理          (component: kb/admin/category )
    │   ├── 文档管理          (component: kb/admin/document )
    │   ├── 回收站            (component: kb/admin/recycle )
    │   └── 版本对比          (component: kb/admin/version )
    └── 知识库学习            (component: kb/portal/index, path: /kb/view, 顶层显示)
```

新员工登录后首页 Banner 推送「新员工必读」标签卡（按 `is_required=1` 拉取最近 10 条 + 1 条随机推荐）。

---

## 7. 关键接口（REST）

> 命名风格与现有 `system/contract` 完全一致。

### 7.1 阅读端（员工侧，无 management 前缀）

| 方法 | 路径 | 权限 | 用途 |
|---|---|---|---|
| GET | `/kb/portal/tree` | `kb:portal:view` | 拉取目录树（含每目录文档数） |
| GET | `/kb/portal/list` | `kb:portal:view` | 分类下文档分页（已发布） |
| GET | `/kb/portal/detail/{id}` | `kb:portal:view` | 文档详情（文件走 download URL） |
| GET | `/kb/portal/required` | `kb:portal:required` | 新员工必读列表 |
| GET | `/kb/portal/search` | `kb:portal:view` | 关键字搜索（标题/摘要/标签 LIKE） |
| GET | `/kb/file/raw/{id}` | `kb:file:download` | 统一代理：按 mime 返回 inline 预览 / video 播放 / attachment 下载 |
| POST | `/kb/file/upload` | `kb:file:upload` | 通用上传（管理员/经理） |

### 7.2 管理端

| 方法 | 路径 | 权限 | 用途 |
|---|---|---|---|
| GET | `/kb/category/list` | `kb:category:list` | 目录树查询 |
| POST | `/kb/category` | `kb:category:add` | 新增目录 |
| PUT | `/kb/category` | `kb:category:edit` | 修改目录 |
| DELETE | `/kb/category/{ids}` | `kb:category:remove` | 删除目录（拒有子节点） |
| PUT | `/kb/category/order` | `kb:category:edit` | 拖拽排序（批量改 parent_id/order_num） |
| GET | `/kb/document/list` | `kb:document:list` | 文档分页（含草稿/下架） |
| GET | `/kb/document/{id}` | `kb:document:query` | 文档详情 |
| POST | `/kb/document` | `kb:document:add` | 新增文档（自动产生版本 1） |
| PUT | `/kb/document` | `kb:document:edit` | 修改文档（产生新版本） |
| DELETE | `/kb/document/{ids}` | `kb:document:remove` | 软删（进回收站） |
| PUT | `/kb/document/publish` | `kb:document:publish` | 发布（status=1） |
| PUT | `/kb/document/offline` | `kb:document:publish` | 下架（status=2） |
| GET | `/kb/version/{docId}` | `kb:version:list` | 历史版本列表 |
| GET | `/kb/version/{docId}/{ver}` | `kb:version:list` | 指定版本内容 |
| POST | `/kb/version/{docId}/{ver}/rollback` | `kb:version:rollback` | 回滚 |
| GET | `/kb/recycle/list` | `kb:recycle:list` | 回收站分页 |
| POST | `/kb/recycle/restore` | `kb:recycle:restore` | 恢复 |
| DELETE | `/kb/recycle/purge` | `kb:recycle:purge` | 物理删除（admin only） |

> 文件下载统一封装：`CmsKbFileService.buildDownloadResponse(fileId)`，并校验 `del_flag=0`。
>
> 预览/播放/下载**统一走后端代理路径**（`/kb/file/raw/{id}`），由 Controller 根据 `cms_kb_file.mime_type` 决定响应头：
> - `image/*` → `Content-Type: image/...` + `inline`（浏览器内联渲染）
> - `video/*` → `Content-Type: video/...` + `inline`（HTML5 `<video>` 可拖动进度条、倍速、全屏）
> - 其他 → `Content-Disposition: attachment; filename="<original_name>"`
>
> 优势：① 不暴露 `/app/uploadPath` 绝对路径；② 统一鉴权 + 操作日志；③ 后续可挂防盗链 / 限速。

---

## 8. 前端结构

```
ruoyi-ui/src/
├── views/
│   ├── kb/
│   │   ├── admin/                     # 管理端（受权限保护）
│   │   │   ├── index.vue              # 多 Tab：目录 / 文档 / 回收站
│   │   │   ├── category.vue           # el-tree + 拖拽 + 必读开关
│   │   │   ├── document.vue           # 表格 + 新建/编辑
│   │   │   ├── documentForm.vue       # 表单：标题/类型/必读/摘要/标签/封面
│   │   │   ├── articleEditor.vue      # 嵌入 WangEditor
│   │   │   ├── fileEditor.vue         # 上传主文件（复用 upload 组件）
│   │   │   ├── version.vue            # 历史版本时间线 + 差异
│   │   │   └── recycle.vue
│   │   └── portal/                    # 阅读端（员工）
│   │       ├── index.vue              # 左侧树 + 右侧列表
│   │       ├── detail.vue             # 文章详情 / 文件下载卡
│   │       ├── required.vue           # 新员工必读
│   │       └── components/
│   │           ├── KbImagePreview.vue # el-image 列表预览（缩放/旋转）
│   │           └── KbVideoPlayer.vue  # HTML5 <video controls>（倍速/全屏/进度）
│   └── system/kb/                     # 系统管理菜单下的知识库入口
└── api/
    └── kb/
        ├── portal.js
        ├── category.js
        ├── document.js
        ├── version.js
        ├── recycle.js
        └── file.js
```

### 8.1 非技术管理员体验要点

1. **一键上传**：拖文件到上传区 → 选目录 → 点发布 = 3 步完成一篇「文件」文档。
2. **可视化富文本**：WangEditor 工具栏图标 + 上传视频/图片/附件按钮，操作与 Word 接近。
3. **拖拽排序目录**：左侧树支持上下/跨级拖动，自动写 `order_num` 和 `parent_id`。
4. **目录级必读**：勾选后整条目录树进新员工首页，无需逐个文档勾。
5. **30 天回收站**：删除前提示「将在 30 天后清除」，避免误删。
6. **版本对比**：保存自动产生新版本（`save_reason=自动`），手动回滚 = 选定版本 → 点「回滚到此版本」。

---

## 9. 错误处理

| 场景 | 行为 | 提示文案 |
|---|---|---|
| 父目录不存在 / 循环引用 | 拒绝保存，返回 biz 码 `KB_CATEGORY_LOOP` | 「不能把目录移动到自己的子目录下」 |
| 文件重复（按 sha256） | 复用旧 `cms_kb_file`，不重新落盘 | 「已复用同名文件，无需重复上传」 |
| 文件超 200 MB | 拒绝上传 | 「单文件不能超过 200 MB」 |
| 上传中网络中断 | 客户端 axios retry 1 次 + 提示「请重试」 | — |
| 删除目录时存在子目录/文档 | 拒绝并列出 | 「该目录下还有 X 个子目录、Y 篇文档，请先清空」 |
| 删除文档时已存在版本 | 软删（不影响历史） | 「文档已移入回收站，30 天后清除」 |
| 回滚到当前版本 | 拒绝 | 「已是当前版本，无需回滚」 |
| 草稿被阅读端访问 | 拒绝（仅 status=1 可见） | 「文档尚未发布」 |
| 角色无权限访问接口 | 403 | 沿用 RuoYi 默认 |
| 视频格式浏览器不支持（如 rmvb/avi 老格式） | 浏览器原生提示，前端 fallback 提示「请下载到本地播放」 | 「当前浏览器无法在线播放，请下载查看」 |
| 视频过大（>200MB）流式加载慢 | Range 请求支持 + 提示 | 「视频较大，请稍候或下载查看」 |
| 图片 EXIF 隐私泄露 | 上传时通过 `thumbnailator` / `imgscalr` 去除 EXIF | 静默处理 |
| 回收站过期清理 | 物理删除文件 + 移除 `cms_kb_file` | 后台日志 |

所有异常走 `@RestControllerAdvice` 统一返回 `AjaxResult.error("KB_XXX", message)`。

---

## 10. 测试策略

> 沿用项目已通过的 BaseControllerTest 体系。

| 测试层级 | 目标 | 关键用例（节选） |
|---|---|---|
| **Mapper 单测** | SQL 正确性 | 目录拖动后 order_num 连续无空洞；软删不被搜索；回收站过期清理 |
| **Service 单测** | 业务规则 | 文件秒传（sha256 命中复用）；版本回滚产生新 version_no+1；删除目录拒绝有子节点 |
| **Controller 集成测** | HTTP + 权限 | manager 可发布；sales 调 `kb:category:add` 返 403；草稿不可被阅读端看到；下载受 `kb:file:download` 限制；`/kb/file/raw/{id}` 对 image 返回 `inline` + 正确 `Content-Type`，对 video 同样，对其他返回 `attachment` |
| **E2E 端到端** | 全链路 | 新员工首登 → 首页必读 → 钻目录 → 读文章 → 预览图片 → 播放录屏 → 下载手册；manager 上传文件→发布→全员可见→编辑→保存版本→回滚→对比；admin 删除→回收站→恢复→再删除→过期清理 |

每层覆盖率目标 ≥ 90%（沿用项目基线）。

---

## 11. 实施拆分（概要）

> 详细步骤由 `writing-plans` 阶段生成。

| 阶段 | 内容 | 估时 |
|---|---|---|
| P1 | SQL 脚本（5 张表 + 3 个字典 + 4 个默认分类种子） | 0.5d |
| P2 | 后端：5 个 domain + 5 个 mapper + 5 个 service + 5 个 controller | 2d |
| P3 | 后端：定时清理任务（30 天回收站） | 0.5d |
| P4 | 前端：管理端 6 个页面 + 1 个 WangEditor 共享组件 | 2d |
| P5 | 前端：阅读端 3 个页面 + 必读首页 Banner | 1d |
| P6 | 前端：6 个 API 文件 + 菜单挂点 + 权限绑定 | 0.5d |
| P7 | 单测 + 集成测 + E2E 测试 | 2d |
| **合计** | | **~8.5 人天** |

---

## 12. 风险与权衡

| 风险 | 缓解 |
|---|---|
| 大文件占满磁盘 | 单文件 200 MB 上限 + sha256 查重 + 30 天回收站 |
| 富文本 XSS | WangEditor 默认白名单；后端二次过滤 `<script>` |
| 视频播放体验差 | 提供 HTML5 `<video controls>` 即可（倍速、全屏、音量由浏览器原生支持）；不做转码、不做 DRM |
| 目录无限层级导致性能下降 | 树形查询走 parent_id 索引 + 一次返回全树；超过 500 节点时再考虑分页 |
| 旧文章迁移 | 暂不提供导入工具，后续如需要可加 `importFromWord` |
| 误删数据 | 30 天回收站 + 软删；admin 误操作可恢复 |
| 跨域/安全 | 文件下载走后端代理，不暴露 `uploadPath` 绝对路径 |
