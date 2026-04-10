---
name: git.worktree.create
description: 为 Fleet 项目创建 Git Worktree。当用户输入 FEATURE_ID（如 256、FLEET-256、fleet-256）或 Plan 路径（如 .opencode/plans/fleet-256/task.md）来创建功能分支工作目录时使用。用户可调用：/git.worktree.create <FEATURE_ID>。
user-invocable: true
allowed-tools: "Bash, Write, mcp__claude_ai_Atlassian__getJiraIssue"
---

# Git Worktree Create

## 1. 核心要求
*   **交互语言**: 必须使用 **中文** 与用户交流。
*   **代码语言**: Shell 命令、分支名、文件路径必须使用 **英文**。
*   **执行方式**: 使用 Bash 工具逐步执行 git 命令，使用 question 工具进行确认交互。
*   **禁止事项**: 不要编写脚本文件，不要探索代码库，不要阅读无关文件。

## 2. 输入解析规则

用户在命令后提供的参数（即 `$ARGUMENTS`）按以下规则解析为 `FEATURE_ID` 和 `SUBTASK`：

> 详见 references/naming-conventions.md

## 3. 命名规范

> 详见 references/naming-conventions.md

## 4. 执行步骤

**严格按以下顺序执行，每一步都使用 Bash 工具运行对应命令。**

### 步骤 1: 解析输入

根据 references/naming-conventions.md 的规则，从 `$ARGUMENTS` 解析出 `FEATURE_ID` 和 `SUBTASK`。
如果输入格式无法识别，向用户报错并给出支持的格式列表，然后停止。

### 步骤 2: 获取环境信息

运行以下命令获取所需变量：
- `git rev-parse --show-toplevel` → 得到 `PROJECT_ROOT`
- `dirname "$PROJECT_ROOT"` → 得到 `WORKTREE_PARENT`（worktree 目录的父目录）
- `git config user.name` → 转小写、去除非字母数字字符 → 得到 `USERNAME`
- `git branch --show-current` → 得到 `CURRENT_BRANCH`

根据 references/naming-conventions.md 的命名规范，计算出 `BRANCH_NAME` 和 `WORKTREE_DIR`。

### 步骤 3: 前置检查

依次运行以下检查，**任一失败则停止并向用户报错**：

1. **工作区状态**: 运行 `git status --porcelain`。如果有输出，不中断创建，但必须在确认创建时提示"根仓存在未提交更改，继续创建可能带来冲突"。
2. **分支不存在**: 运行 `git show-ref --verify --quiet "refs/heads/$BRANCH_NAME"`，如果分支已存在，使用 question 工具询问用户选择：使用现有 worktree / 取消操作。
3. **目录不存在或不冲突**: 检查 `$WORKTREE_DIR` 是否已存在，如果存在且包含与子仓冲突的文件/文件夹（fleet-backend, fleet-portal, fleet-test）则报错。

### 步骤 4: 执行创建脚本

直接调用项目内置脚本完成目录创建、软链接、子仓 worktree 和 .env 复制：

```bash
"$PROJECT_ROOT/.claude/skills/git.worktree.create/scripts/worktree-create.sh" "$FEATURE_ID"
```

**脚本说明**（`.claude/skills/git.worktree.create/scripts/worktree-create.sh`）：
- **软链接**：动态遍历根目录所有文件/目录，跳过子仓库、`.git`、`.idea`
- **`.idea` 复制**：单独复制避免两个 worktree 共享 IDE 配置冲突
- **子仓 worktree**：基于各子仓当前所在分支
- **`.env` 复制**：自动复制 `.env`、`.env.local`、`.env.development`（存在时）

如果脚本失败则向用户报错并停止，提示用户检查错误信息。

### 步骤 8: 拉取 Jira 任务信息

使用 `mcp__claude_ai_Atlassian__getJiraIssue` 工具拉取 `$FEATURE_ID` 对应的 Jira ticket，提取以下字段：
- `summary`：任务标题
- `description`：任务描述
- `status`：当前状态
- `assignee`：负责人
- `priority`：优先级
- `labels` / `components`：标签和组件
- `subtasks`：子任务列表（如有）
- `acceptance criteria`：验收条件（通常在 description 中）

如果 Jira ticket 不存在或拉取失败，跳过步骤 9-10，直接进入步骤 11。

### 步骤 9: 创建 README.$FEATURE_ID.md

按照 `references/readme-template.md` 的模板，在 `$WORKTREE_DIR/README.$FEATURE_ID.md` 写入内容（用实际 Jira 字段替换占位符）。

**动态生成"建议的起手式"**：根据 Jira 任务的 summary 和 description 判断任务类型，填写具体建议。

判断规则：

| 任务类型信号 | 推荐起手式 |
|-------------|-----------|
| 描述含"迁移"、"重构"、"升级"、"migrate" | `/opsx:explore` 先研究现有架构，再制定迁移计划 |
| 描述含"bug"、"修复"、"报错"、"fix" | `/opsx:explore` 定位根因，或 `/superpowers:systematic-debugging` |
| 描述含"新增"、"添加"、"支持"、"new"、"add" | `/opsx:explore` 澄清需求 → `/opsx:propose` 生成方案 |
| 有明确 subtasks 且需求清晰 | 直接 `/opsx:propose` 生成实施计划 |

同时根据 description 内容生成 1-2 个具体的提问示例，帮助用户知道该怎么跟 AI 对话：
- 迁移类："`/opsx:explore` 研究 V1 和 V2 的数据模型差异，从 `XxxService.Method` 开始，不要写代码"
- Bug 类："`/opsx:explore` 复现 XXX 场景下的 YYY 错误，分析调用链路"
- 新功能类："`/opsx:explore` 分析现有 XXX 模块的架构，评估添加 YYY 的影响面"

提问示例中应包含具体的函数名、表名等（从 description 中提取），而非泛泛的占位符。

### 步骤 10: 显示总结

向用户输出创建总结，包含：
- 分支名称
- 工作目录路径
- `README.$FEATURE_ID.md` 路径
- 下一步操作提示：`cd $WORKTREE_DIR`

## 5. 项目常量

| 常量 | 值 |
|------|-----|
| PROJECT_NAME | `fleet-meta` |
| 基础分支 | `当前分支` |
| FEATURE_ID 前缀 | `FLEET-` |
| 必须复制的配置 | `.env` |
| 子仓 | `fleet-backend`, `fleet-portal`, `fleet-test` |
| 子仓基线分支 | 各子仓当前所在分支（动态获取） |
| Jira cloudId | `14c1d2a0-1b35-43da-b7ac-4b47ffcc4f4a` |
| Jira 站点 | `go-jek.atlassian.net` |

---

## 📍 USER INPUT DELIMITER

**重要提示**：当用户调用此命令时，参数将直接附加在命令之后。

例如：
```
/git.worktree.create 256
                      ↑
                   用户输入参数
```

**AI 处理方式**：
- 读取本文档后，立即解析紧随其后的用户输入
- 用户输入内容 = 命令执行参数
- 无需二次询问，直接按文档规范执行

**支持的输入格式**：
- 纯数字：`256`
- 带前缀：`fleet-256`, `FLEET-256`
- Plan 路径：`.opencode/plans/fleet-256/main.md`
