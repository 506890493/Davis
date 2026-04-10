---
name: git.commit
description: 为 Fleet 项目生成符合规范的 Git commit message。**仅在用户明确请求时使用**，
  如用户说 "commit"、"backend commit"、"frontend commit"、"test commit"、"生成提交信息"、
  "/git.commit" 时触发。根据用户输入或 git 暂存区变更自动判断目标 repo（fleet-backend /
  fleet-portal / fleet-test），默认为 fleet-backend。自动从分支名提取 Jira ticket ID，
  遵循 Conventional Commits 格式。生成后询问用户是否直接提交。
---

# Fleet Git Commit Message 生成

## 任务目标
根据 Fleet 项目的代码变更，生成符合项目规范的 Git commit message。

**重要提醒**：
- ✅ 只生成 commit message 文本
- ❌ 不要直接执行 `git commit` 命令（生成后询问用户）
- ✅ 支持三个子模块：`fleet-backend/`、`fleet-portal/`、`fleet-test/`

---

## 工作流程

### 步骤 0: 读取用户附加说明（可选）

用户调用时可附加希望 message 重点体现的内容，例如：

```
commit 重点体现对 outstanding 计算逻辑的修正
backend commit 说明这次是为了兼容新的 Kafka schema
frontend commit 这次修复了表格分页的 bug
```

若用户提供了附加说明：
- **优先**将其体现在 subject 或 body 中
- 附加说明代表用户对本次提交意图的判断，**不可忽略**

---

### 步骤 1: 确定目标 Repo（三层优先级）

**Layer 1: 用户明确指定（最高优先级）**

| 用户输入关键词 | 目标 Repo |
|-------------|-----------|
| `backend`、`后端`、`fleet-backend` | `fleet-backend` |
| `frontend`、`前端`、`portal`、`fleet-portal` | `fleet-portal` |
| `test`、`测试`、`fleet-test` | `fleet-test` |

**Layer 2: 自动检测暂存区变更**

若用户未明确指定，检查各 repo 的暂存区：

```bash
git -C fleet-backend diff --cached --name-only
git -C fleet-portal diff --cached --name-only
git -C fleet-test diff --cached --name-only
```

- 只有一个 repo 有暂存变更 → 自动选择该 repo
- **多个** repo 有暂存变更 → 询问用户选择哪个 repo
- 所有 repo 暂存区为空 → 进入 Layer 3

**Layer 3: 默认（兜底）**

- 无明确指定且无暂存变更 → 默认 **fleet-backend**

---

### 步骤 2: 获取上下文信息

确定目标 repo 后，在该 repo 目录下执行：

**提取 Jira Ticket（使用辅助脚本）：**

```bash
/Users/jianxingzhang/projects/fleet-meta/.claude/skills/git.commit/scripts/extract_jira_ticket.sh \
  /Users/jianxingzhang/projects/fleet-meta/<target-repo>
```

脚本输出：成功返回 `FLEET-XXX`，失败返回 `FLEET-000`

**获取 Git 变更信息：**

```bash
# 获取已暂存的变更（优先）
git -C <target-repo> diff --cached

# 如果暂存区为空，获取工作区变更
git -C <target-repo> diff

# 获取最近的提交记录（参考格式）
git -C <target-repo> log --oneline -5
```

---

### 步骤 3: 分析变更确定 Type 和 Scope

**判断 Type（类型）：**

| 变更内容 | Type | 说明 |
|----------|------|------|
| 添加新功能、API | `feat` | 新增功能 |
| 修复 bug、错误处理 | `fix` | 修复问题 |
| README、文档 | `docs` | 仅文档 |
| 格式化、空格、import 顺序 | `style` | 代码风格 |
| 重命名、提取方法 | `refactor` | 结构调整 |
| 优化查询、缓存 | `perf` | 性能提升 |
| 添加测试、mock | `test` | 测试相关 |
| go.mod、Makefile、package.json | `build` | 构建依赖 |
| .github/workflows、CI 脚本 | `ci` | CI/CD |
| 配置文件、注释、杂项 | `chore` | 其他 |

**判断 Scope（按目标 Repo）：**

*fleet-backend:*

| 变更文件路径 | Scope |
|-------------|-------|
| `server/webapi/` | `server` |
| `service/` | `service` |
| `repository/` | `repo` |
| `worker/` | `worker` |
| `internal/task/` | `task` |
| 认证、授权、JWT、Casbin | `auth` |
| `config/`、配置文件 | `config` |
| `model/` | `model` |
| 多个模块 | 省略或选最主要的一个 |

*fleet-portal:*

| 变更文件路径 | Scope |
|-------------|-------|
| `src/api/` | `api` |
| `src/store/` | `store` |
| `src/views/` | `view` |
| `src/components/` | `component` |
| `src/hooks/` | `hooks` |
| `src/types/` | `types` |
| 多个模块 | 省略或选最主要的一个 |

*fleet-test:*

| 变更文件路径 | Scope |
|-------------|-------|
| integration 测试 | `integration` |
| fixtures | `fixtures` |
| utils | `utils` |

---

### 步骤 4: 生成 Commit Message

**格式（所有 repo 统一）：**
```
FLEET-XXX <type>(<scope>): <subject>

<body（可选）>
```

**Header 规范：**
- `FLEET-XXX`：Jira ticket（必选）
- `<type>`：类型（必选）
- `(<scope>)`：范围（可选）
- `: `：冒号加空格
- `<subject>`：主题（必选）

**Subject 规范：**
- 使用祈使现在时态："add" 而不是 "added"
- 首字母小写
- 不超过 50 字符
- 末尾不加句点

**Body（可选）：**
- 使用祈使现在时态
- 说明**为什么**做这个变更（动机）
- 与之前行为的对比
- 1-2 行，简明扼要

---

## 示例

### 后端示例

```
FLEET-123 feat(server): add user list endpoint
```

```
FLEET-456 fix(repo): prevent database connection leak

Ensure rows are closed on error path to avoid connection leaks.
```

```
FLEET-789 refactor(service): extract password validation to separate method
```

### 前端示例

```
FLEET-512 feat(view): add vehicle list pagination

Support page size selection and page navigation in vehicle table.
```

```
FLEET-512 fix(component): correct table sort order on reload
```

### 测试示例

```
FLEET-600 test(integration): add driver assignment flow test
```

---

## 输出格式

首先输出生成的 commit message：

```
生成的 commit message（fleet-backend）:

FLEET-123 feat(server): add user registration endpoint

Add endpoint to list all users with pagination support.
```

然后询问用户：

```
是否使用此 commit message 直接提交？

选项：
1. 是 - 使用上述 message 直接执行 git commit
2. 否 - 只显示 message，由用户自行处理
3. 编辑 - 允许用户修改 message 后再提交
```

### 用户响应处理

| 用户选择 | 操作 |
|----------|------|
| "是"、"1"、"y" | 在目标 repo 目录下执行 `git commit -m "<message>"` |
| "否"、"2"、"n" | 只输出 message，不执行 git 命令 |
| "编辑"、"3"、"e" | 提示用户输入修改后的 message，再提交 |

---

## 故障处理

| 问题 | 解决方案 |
|------|----------|
| 暂存区为空 | 使用 `git diff` 获取未暂存的变更 |
| 无 Jira ticket | 使用 `FLEET-000` |
| 变更跨多个模块 | 选择最主要的 scope 或省略 |
| 不确定 type | 参考 Conventional Commits 规范 |
| subject 超过 50 字符 | 简化措辞，移除冗余词汇 |
| 多个 repo 有暂存变更 | 询问用户选择目标 repo |
