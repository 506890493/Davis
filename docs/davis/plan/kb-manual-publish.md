# 开发计划：把 Davis 系统操作手册发布到知识库

**日期**: 2026-07-03
**状态**: 待实施
**优先级**: 中
**父方案**: `C:\Users\ShiPengTao\.claude\plans\smooth-soaring-rocket.md`（已批准）

---

## 根因

Davis 系统已有 12 章节操作手册（`docs/davis/manual/`），并通过 `sql/migration/003_seed_manual_kb.sql` 在 KB 库中创建了 14 篇"系统操作手册"分类下的文档**元数据**。但用户实际访问 KB 详情页时看到的却是**空白页**——根因有两个：

### 根因 #1：KB 详情接口未返回正文
- `CmsKbDocument` 实体**没有 `content` 字段**
- `CmsKbPortalServiceImpl.getDetail(id)` 只查 `cms_kb_document` 主表，**没查 `cms_kb_document_version.content`**
- 结果：`portal/detail.vue` 的 `this.doc.content` 永远是 `null`，`richContent` 永远是空字符串

### 根因 #2：003 迁移脚本存的是 Markdown 占位
- `cms_kb_document_version.content` 存的是 `# 03-合同管理\n\n## 合同类型\n- ...` 这类原始 Markdown 文本
- 前端 `portal/detail.vue` 用 `<div v-html="richContent">` 渲染，**只渲染 HTML，不渲染 Markdown**
- 即使根因 #1 修复了，页面会直接显示原始的 `# 标题` 字符，体验极差

---

## 方案

**核心思路**：后端引入 Markdown→HTML 转换库（flexmark-java），让 KB 文档在**入库前**完成转换，前端 v-html 直接拿到已渲染的 HTML。同时修复 KB 详情接口拿不到正文的问题。

### 实施步骤

#### 步骤 1：后端增加 Markdown 转换能力
1. `ruoyi-system/pom.xml` 加依赖：`com.vladsch.flexmark:flexmark-all:0.64.8`
2. 新增 `ruoyi-system/.../util/MarkdownToHtmlConverter.java`，提供 `convert()` / `convertSafe()` 静态方法，启用 GFM 表格/任务列表/删除线扩展
3. 新增单元测试 `MarkdownToHtmlConverterTest.java`，覆盖 8 种 Markdown 语法 + 1 个 XSS 转义用例，覆盖率 ≥90%

#### 步骤 2：修复 KB 详情页拿不到正文
1. `CmsKbDocument.java` 新增**瞬态字段** `private String content;`（不映射到表，加 `@JsonProperty("content")`）
2. `CmsKbDocumentVersionMapper.java` 确认/新增 `selectCurrentByDocumentId(Long documentId)` 方法，对应 XML
3. `CmsKbPortalServiceImpl.java` 注入 `CmsKbDocumentVersionMapper`，`getDetail` 查文档后补充查当前版本 content 并注入 doc
4. **验证点**：启动后端，访问任一已发布 KB 文档，详情页应显示 003 脚本中的占位 Markdown 文字（丑但能看见）

#### 步骤 3：把 12 份 Markdown 打包到 classpath
1. 把 `docs/davis/manual/00-术语与概念.md` ~ `12-注意事项与常见问题.md` 复制到 `ruoyi-system/src/main/resources/docs/manual/`
2. 资源路径：`classpath:docs/manual/*.md`（Spring `PathMatchingResourcePatternResolver` 扫描）

#### 步骤 4：实现 KB 手册启动初始化器
1. `application.yml` 加配置 `davis.kb.manual.enabled=true` / `force-overwrite=false`
2. 新增 `ruoyi-system/.../initializer/KbManualInitializer.java`，实现 `CommandLineRunner`
3. 启动逻辑：
   - 开关关闭则直接 return
   - 确保存在"系统操作手册"分类（不存在则创建）
   - 扫描 `classpath:docs/manual/*.md`
   - 对每份 md：用文件名（去 `.md`）当 title，查 KB 是否已有该文档
     - 不存在 → 创建 cms_kb_document（status=1） + cms_kb_document_version（v1, is_current=1, content 为 Markdown→HTML 结果）
     - 存在但 content 是空/占位文字 → 更新 version.content
     - 存在且 content 是真实内容 → 跳过
   - 记录初始化日志：新建数 / 更新数 / 失败原因
4. 新增单测 `KbManualInitializerTest.java`，Mockito 模拟 Service/Mapper，覆盖 6 种分支场景

#### 步骤 5：前端富文本样式优化
在 `ruoyi-ui/src/views/system/kb/portal/detail.vue` 的 `<style scoped>` 内追加表格/代码块/任务列表/标题/引用等 CSS 样式（深选择器 `:deep()`），让 Markdown 渲染出来的 HTML 排版美观。

---

## 影响范围

### 新增文件
| 文件 | 作用 |
|------|------|
| `ruoyi-system/src/main/java/com/ruoyi/system/util/MarkdownToHtmlConverter.java` | Markdown→HTML 工具类 |
| `ruoyi-system/src/main/java/com/ruoyi/system/initializer/KbManualInitializer.java` | 启动时自动初始化 KB 手册 |
| `ruoyi-system/src/test/java/com/ruoyi/system/util/MarkdownToHtmlConverterTest.java` | 工具类单测 |
| `ruoyi-system/src/test/java/com/ruoyi/system/initializer/KbManualInitializerTest.java` | 初始化器单测 |
| `ruoyi-system/src/main/resources/docs/manual/00-术语与概念.md` 等 12 份 | 手册源文件 |

### 修改文件
| 文件 | 改动 |
|------|------|
| `ruoyi-system/pom.xml` | 新增 flexmark-all 依赖 |
| `ruoyi-system/.../domain/CmsKbDocument.java` | 新增瞬态 content 字段 |
| `ruoyi-system/.../service/impl/CmsKbPortalServiceImpl.java` | 注入 VersionMapper，getDetail 补充 content |
| `ruoyi-system/.../mapper/CmsKbDocumentVersionMapper.java` + XML | 确认/新增 selectCurrentByDocumentId |
| `ruoyi-ui/src/views/system/kb/portal/detail.vue` | 追加富文本 CSS 样式 |
| `ruoyi-admin/src/main/resources/application.yml` | 新增 davis.kb.manual 配置项 |

### 业务影响
- **KB 详情页**：从"空白"变成"可正常阅读 12 篇手册"
- **启动时间**：首次启动会多 1-2 秒（读取 12 份 md + 转换），后续启动检测无变化则跳过
- **数据库**：`cms_kb_document_version.content` 字段会被填充 HTML 字符串（每篇约 5-20 KB）
- **现有 KB 文档管理功能**：不变（用户仍可通过 KB 后台富文本编辑器手动编辑）
- **003 迁移脚本**：保持不变，新初始化器检测占位文字后自动更新

### 风险
| 风险 | 等级 | 缓解 |
|------|------|------|
| 启动 IO + 转换拖慢启动 | 低 | 仅在检测到缺失/占位时执行；生产可设 `enabled=false` 关闭 |
| flexmark 库 ~2MB | 低 | 一次性依赖，KB 是核心模块值得 |
| Markdown 表格/代码块渲染不美观 | 中 | 步骤 5 加 CSS 样式 |
| 中文文件名在不同 OS 资源加载差异 | 低 | 用 `InputStream` 读取，不依赖文件系统路径 |
| docType=2 但 content 异常时 v-html 渲染出错 | 低 | 转换器做 XSS 转义；占位文字检测后强制更新 |

---

## 验证方法

### 单元测试
```bash
cd ruoyi-system
mvn test -Dtest=MarkdownToHtmlConverterTest
mvn test -Dtest=KbManualInitializerTest
```
- 工具类 ≥90% 行覆盖
- 初始化器覆盖：开关关闭/分类自动创建/文档新建/占位更新/幂等跳过/资源缺失

### 端到端测试（admin/manager/accountant/sales 四角色）
1. **全新环境**：
   - 删除 KB 中"系统操作手册"分类下所有文档
   - `mvn clean package -Dmaven.test.skip=true` + `java -jar ruoyi-admin.jar`
   - 启动日志应输出：`[KB Manual Init] 初始化完成，新建 12 篇，更新 0 篇`
   - 数据库 `cms_kb_document_version` 12 条 v1 记录，content 字段含 `<table>` / `<h1>` 等 HTML 标签

2. **详情页渲染**：
   - 浏览器打开 `http://localhost/view/detail/{某文档ID}`
   - 验证：标题层级、表格边框、列表缩进、代码块背景色、引用左竖线全部正确
   - 验证：页面上**没有** `# 标题` `## 子标题` 这类原始字符
   - 重点验证 03-合同管理 / 04-客户管理 / 05-任务管理 三篇

3. **幂等性**：
   - 第二次重启后端
   - 启动日志：`[KB Manual Init] 初始化完成，新建 0 篇，更新 0 篇`
   - 已发布文档无任何破坏

4. **源文件同步**：
   - 修改 `classpath:docs/manual/00-术语与概念.md`（添加 `## 测试章节\n\n验证内容`）
   - 重启后端
   - KB 详情页应显示新增的"测试章节"

5. **角色权限**：
   - accountant 账号访问手册：`***` 文字原样保留（手册里金额脱敏说明是按设计如此）
   - sales 账号同上
   - 4 个角色都应能阅读手册（KB 是全员学习模块）

### 集成测试（可选）
`ruoyi-system/src/test/java/com/ruoyi/system/integration/KbPortalIntegrationTest.java`
- `@SpringBootTest` 启动真实容器
- 调 `KbManualInitializer.run()`
- 通过 `ICmsKbPortalService.getDetail(id)` 取文档
- 断言 content 不为空、含 HTML 标签、status=1

---

## 实施顺序与时间

| 步骤 | 预计耗时 | 依赖 |
|------|----------|------|
| 1. Markdown 转换工具类 + 单测 | 0.5h | 无 |
| 2. 修复 KB 详情接口拿不到正文 | 0.5h | 无 |
| 3. 复制 Markdown 到 classpath | 0.1h | 步骤 1 完成 |
| 4. 启动初始化器 + 单测 | 1h | 步骤 1+2+3 完成 |
| 5. 前端富文本样式 | 0.3h | 步骤 4 完成（需要看到真实渲染效果） |
| 6. 端到端验证 | 0.5h | 步骤 5 完成 |
| **合计** | **~3h** | — |

---

## 后续优化（不在本计划范围）

- 把 Markdown 源文件托管在 Git，CI 自动跑 `mvn` 同步到 KB
- KB 增加 Markdown 源码预览模式（保留源 + HTML 双版本）
- 关键文档（合同/任务/客户）配流程图截图
- 给手册加目录锚点（Markdown 标题自动生成 id）

---

## 关联文档
- 父方案：`C:\Users\ShiPengTao\.claude\plans\smooth-soaring-rocket.md`
- 源手册：`docs/davis/manual/`
- 现有迁移脚本：`sql/migration/003_seed_manual_kb.sql`
- KB 模块设计：`docs/superpowers/specs/2026-06-11-knowledge-base-design.md`
