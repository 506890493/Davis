---
name: git.worktree.remove
description: 安全删除 Fleet 项目的 Git Worktree 及关联分支。支持单个/多个/通配符删除，删除前展示未提交更改和未合并提交警告。用户可调用：/git.worktree.remove <FEATURE_ID> [FORCE=true]。
user-invocable: true
allowed-tools: "Bash"
---

# Git Worktree Remove

## 1. 核心要求
*   **交互语言**: 必须使用 **中文** 与用户交流。
*   **代码语言**: Shell 命令、分支名、文件路径必须使用 **英文**。
*   **执行方式**: 使用 Bash 工具逐步执行 git 命令，使用 question 工具进行确认交互。
*   **禁止事项**: 不要编写脚本文件，不要探索代码库，不要阅读无关文件。
*   **安全第一**: 删除前必须展示详细信息并获得用户确认。

## 2. 输入解析规则

用户在命令后提供的参数（即 `$ARGUMENTS`）支持以下格式：

### 输入格式

| 输入示例 | 说明 |
|---------|------|
| `256` 或 `FLEET-256` | 删除单个主分支的 worktree |
| `.opencode/plans/fleet-256/database.md` | 删除单个子分支的 worktree |
| `FLEET-256-*` | 通配符模式，删除所有 FLEET-256 相关的 worktree |
| `FLEET-256 FLEET-257` | 多个参数，删除多个指定的 worktree |
| 末尾追加 `FORCE=true` | 强制模式，跳过合并检查，使用 `git branch -D` |

### FEATURE_ID 转换与 SUBTASK 提取

> 详见 references/naming-conventions.md（与 create 逻辑相同）

**特殊文件名**: `main.md`、`index.md`、`README.md` 视为主分支，不提取 SUBTASK。

## 3. 命名规范

> 详见 references/naming-conventions.md

## 4. 执行步骤

**严格按以下顺序执行，每一步都使用 Bash 工具运行对应命令。**

### 步骤 1: 解析输入

1. 从 `$ARGUMENTS` 中分离出目标列表和 `FORCE` 标志。
2. 对每个目标，根据命名规范解析出 `FEATURE_ID` 和 `SUBTASK`。
3. 如果输入无法识别，向用户报错并停止。

### 步骤 2: 获取环境信息

运行以下命令获取变量：
- `git rev-parse --show-toplevel` → `PROJECT_ROOT`
- `dirname "$PROJECT_ROOT"` → `WORKTREE_PARENT`
- `git config user.name` → 转换 → `USERNAME`

### 步骤 3: 查找匹配的 worktree

**精确匹配模式**: 根据命名规范计算出 `BRANCH_NAME` 和 `WORKTREE_DIR`。

对每个子仓（fleet-backend, fleet-portal, fleet-test），运行 `git -C <original_repo> worktree list`，检查是否存在分支名匹配 `BRANCH_NAME` 且路径为 `$WORKTREE_DIR/<subrepo>` 的 worktree。如果找到，记录该 worktree。

**通配符模式（如 `FLEET-256-*`）**: 对每个子仓运行 `git -C <original_repo> worktree list`，筛选分支名中包含对应 FEATURE_ID 的 worktree，记录匹配的 worktree。

如果未找到任何匹配，检查 `$WORKTREE_DIR` 是否存在，如果存在，使用 **question 工具** 确认是否删除该目录（可能包含未清理的子仓 worktree）。选项：`确认删除` / `取消`。用户选择取消则停止。如果不存在，则向用户报错并停止。

### 步骤 4: 收集每个待删除 worktree 的状态

对每个匹配的 worktree，使用 `git -C <subrepo>` 检查（<subrepo> 为该 worktree 所在子仓）：

1. **工作区状态**: `git -C <subrepo> status --porcelain` → 是否有未提交更改及数量。
2. **合并状态**: `git -C <subrepo> log <base>..<branch> --oneline` → 未合并提交数量。如果无未合并提交则视为已合并。（<base> 为子仓基线分支，如 develop 或 main）
3. **最后提交**: `git -C <subrepo> log -1 --oneline --pretty=format:"%h - %s (%cr)"`。

### 步骤 5: 展示删除清单并确认

使用 **question 工具** 展示以下信息并请求确认：

展示内容（每个 worktree）：
- 分支名称和类型
- 工作目录路径
- 工作区状态（干净 / 有 N 个未提交更改）
- 合并状态（已合并 / N 个未合并提交）
- 最后提交

如果有未提交更改或未合并分支，额外显示警告。

选项：`确认删除` / `取消`

用户选择取消则停止执行。

### 步骤 6: 逐个执行删除

对每个目标 worktree **按顺序** 执行以下步骤：

1. **删除子仓 worktree**:
   - 对于该 worktree 所在子仓，运行 `git -C <original_repo> worktree remove <worktree_path>`
   - 如果失败（如有未提交更改），尝试 `git -C <original_repo> worktree remove --force <worktree_path>`

2. **删除根仓目录**:
   - 目录存在时运行 `rm -rf <worktree_dir>`
   - 目录不存在时跳过

3. **删除本地分支**:
   - 对每个相关子仓，先运行 `git -C <original_repo> worktree prune` 以清理无效引用。
   - 然后分支存在时运行 `git -C <original_repo> branch -d <branch_name>`（普通模式）或 `git -C <original_repo> branch -D <branch_name>`（FORCE 模式）
   - 分支不存在时跳过

4. **删除远程分支**（移除此步骤，不删除远程分支）。

每步完成后向用户报告结果。

### 步骤 7: 显示总结

输出删除总结：
- 成功/失败数量
- 当前剩余的 worktree 列表（运行 `git worktree list`）

## 5. 项目常量

| 常量 | 值 |
|------|-----|
| 子仓 | `fleet-backend, fleet-portal, fleet-test` |

---

## 📍 USER INPUT DELIMITER

**重要提示**：当用户调用此命令时，参数将直接附加在命令之后。

例如：
```
/git.worktree.remove 256
                       ↑
                   用户输入参数
```

**AI 处理方式**：
- 读取本文档后，立即解析紧随其后的用户输入
- 用户输入内容 = 命令执行参数
- 无需二次询问，直接按文档规范执行

**支持的输入格式**：
- 单个：`256` 或 `fleet-256`
- 路径：`.opencode/plans/fleet-256/database.md`
- 通配符：`FLEET-256-*`
- 多个：`FLEET-256 FLEET-257`
- 强制：末尾追加 `FORCE=true`
