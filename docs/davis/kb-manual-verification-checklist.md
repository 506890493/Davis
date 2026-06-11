# 知识库模块 — 手工验证清单

> 执行人：开发者本人
> 环境：本地启动 `ruoyi-admin` + `ruoyi-ui` dev server
> 数据库：执行 `sql/update_20260611_kb.sql`（含 Quartz 任务注入）

---

## 启动步骤

```bash
# 1. 启动后端
cd D:/GitHub/ruoyi-davis
mvn clean package -Dmaven.test.skip=true
java -jar ruoyi-admin/target/ruoyi-admin.jar

# 2. 启动前端
cd ruoyi-ui
npm install --legacy-peer-deps   # WangEditor 5 需要
npm run dev
# 访问 http://localhost
```

## 浏览器验证清单

### 1. admin 登录验证
- [ ] 用 `admin / admin123` 登录
- [ ] 「系统管理 → 知识库」菜单可见（4 个子项：目录/文档/回收站 + 知识库学习）
- [ ] 「目录管理」可见 4 个预置分类：系统操作手册 / 代账知识 / 会计知识 / 工商知识
- [ ] 系统操作手册 默认 `is_required=1`，显示 ★
- [ ] 「文档管理」可上传 PDF 文档 → 自动进入草稿状态
- [ ] 草稿文档对 admin 可见，对 sales 不可见（验证 `kb:document:list` 的 read filter）

### 2. 文件 vs 富文本 双模式
- [ ] 「文档管理 → 上传文件」：上传 PDF → 主文件 ID 写入 → 发布
- [ ] 「文档管理 → 撰写文章」：在 WangEditor 中输入富文本（插图/插视频/插附件）→ 发布
- [ ] 文件型文档详情：显示「下载」按钮（触发 attachment）
- [ ] 富文本型文档详情：内容直接渲染
- [ ] 富文本中的 `<img src>` 和 `<video src>` 自动经 `/kb/file/raw/{id}` 代理

### 3. 图片 / 录屏 / 其他 mime 表现
- [ ] 文章中插入图片 → 详情页内联预览（inline）
- [ ] 文章中插入 MP4 录屏 → 详情页内联播放（HTML5 controls）
- [ ] 上传 Word / Excel / PDF → 详情页触发下载（attachment）
- [ ] 验证响应头：`Content-Disposition: inline` vs `attachment`

### 4. 草稿/发布/下架 状态机
- [ ] 新建草稿 → 状态 0，销售不可见
- [ ] 草稿发布 → 状态 1，销售可见
- [ ] 发布后下架 → 状态 2，销售不可见，admin 仍可见
- [ ] 重新发布 → 状态 1

### 5. 版本管理
- [ ] 编辑已发布文档 → 产生 v2，详情页内容变 v2
- [ ] 「历史」按钮 → 看到 v1 + v2 时间线
- [ ] 点击 v1 → 右侧显示 v1 内容
- [ ] 「回滚到 v1」→ 产生 v3，内容同 v1
- [ ] 试图回滚到当前版本（v3）→ 拒绝

### 6. 回收站
- [ ] 删除文档 → 进入回收站（30 天倒计时）
- [ ] 回收站列表显示删除时间 + 剩余天数（< 3 天变红）
- [ ] 「恢复」→ 文档回到正常状态
- [ ] 「永久删除」→ admin 可操作；manager 不可（403）

### 7. 必读 / 首页 Banner
- [ ] 文档设置 `is_required=1` → 出现在 `/kb/portal/required` 页
- [ ] 目录设置 `is_required=1` → 该目录下文档全部进必读
- [ ] 首页顶部 Banner 显示 4 张必读卡，点击进入详情

### 8. 目录树拖拽 + 必读切换
- [ ] el-tree 节点上下/跨级拖动 → 排序生效
- [ ] 「设必读」按钮在节点右侧，切换 `is_required`
- [ ] 父目录必读后，子目录文档自动进必读（service 端处理）

### 9. 权限矩阵验证

| 角色 | 测试 | 预期 |
|---|---|---|
| **admin** | 全部 KB 操作 | 全部成功 |
| **manager** | 全部 KB 操作（除 `kb:recycle:purge`） | purge 返 403，其他成功 |
| **sales** | 仅读 + 下载 + 上传 | 管理端接口全部 403 |
| **account** | 同 sales | 同上 |

### 10. Quartz 定时清理（手动验证）
- [ ] 查看「系统管理 → 定时任务」→ 找到「知识库回收站清理」
- [ ] 检查 cron 表达式：`0 0 2 * * ?`（每日 02:00）
- [ ] 立即执行一次 → 删除 30 天前的软删文档
- [ ] 查看日志：`[KbRecycleCleanTask] cleaned N expired documents`

---

## 自动化测试覆盖回顾

- ✅ Mapper（隐式通过 Service 单测）
- ✅ Service：11 个单测（CmsKbCategoryServiceTest 5 + CmsKbDocumentServiceTest 6）
- ✅ Controller 集成测试：9 个（CmsKbControllerIntegrationTest）
- ✅ Quartz 任务测试：4 个（KbRecycleCleanTaskTest）
- ✅ E2E 全链路：4 个（KnowledgeBaseE2ETest）
- **总计：28 个测试全部通过**

---

## 已知边界 / 后续优化

1. 富文本 XSS 防护：依赖 WangEditor 默认白名单；后端可在 v2 加 JSoup 二次过滤
2. 文件秒传基于 sha256；超 200MB 不允许；可考虑改 NIO 流式计算
3. 视频直链走代理；未做转码 / 加密 / 防盗链
4. 目录无限层级；500+ 节点时再考虑分页
5. 导入 / 导出 Excel：本期未做

---

## 上线 PR

```bash
cd D:/GitHub/ruoyi-davis
git checkout -b feature/knowledge-base
git push origin feature/knowledge-base
gh pr create --title "feat: 知识库模块" --body "..."
```

PR 描述模板：

```
## 简介
为 Davis 系统新增知识库模块：管理员/经理上传操作手册、撰写富文本文章、上传录屏/图片，全员按目录浏览学习。

## 主要功能
- 📂 单树多级目录，预置 4 个一级分类（系统操作手册/代账/会计/工商）
- 📄 文件型文档（PDF/Word/Excel/PPT）+ 富文本文章（WangEditor 5）
- 🖼️ 图片内联预览 / 🎬 录屏内联播放 / 📎 其他 mime 走下载
- 📜 草稿/发布/下架 状态机
- 🕒 版本历史 + 回滚（回滚 = 产生新版本）
- 🗑️ 30 天回收站 + Quartz 每日 02:00 自动清理
- ⭐ 「新员工必读」目录级 + 文档级 + 首页 Banner

## 改动统计
- 后端：5 实体 + 5 mapper + 6 service + 7 controller + 1 quartz + 28 测试
- 前端：6 管理页 + 3 阅读页 + 6 API + 1 WangEditor 共享组件
- 数据库：5 张表 + 3 个字典 + 4 默认分类 + 菜单挂点 + 角色绑定

## 测试
- 28 个测试全部通过（Mapper/Service/Controller/E2E）

## Spec & Plan
- 设计稿：docs/superpowers/specs/2026-06-11-knowledge-base-design.md
- 计划：docs/superpowers/plans/2026-06-11-knowledge-base.md
```
