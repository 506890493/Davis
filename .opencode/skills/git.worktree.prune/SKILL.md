---
name: git.worktree.prune
description: 清理 Fleet 项目中无效的 Git Worktree 引用。当 worktree 目录已被手动删除但 Git 引用仍存在时使用。用户可调用：/git.worktree.prune（无参数）。
user-invocable: true
allowed-tools: "Bash"
---

# Git Worktree Prune

## 核心要求

- **交互语言**: 使用**中文**与用户交流。
- **执行方式**: 调用脚本完成全部操作，无需手动逐步执行 git 命令。

## 什么是无效 Worktree

当 worktree 引用指向的目录已被手动删除（而非通过 `git worktree remove`），Git 仍保留该引用记录。`prune` 命令用于清理这些无效引用。

## 执行步骤

直接运行脚本，脚本会自动完成检测、展示、确认、清理全流程：

```bash
bash .claude/skills/git.worktree.prune/scripts/worktree-prune.sh
```

脚本流程：
1. 对三个子仓（fleet-backend / fleet-portal / fleet-test）运行 `git worktree prune --dry-run` 检测无效引用
2. 若无需清理，直接退出
3. 展示发现的无效引用，等待用户确认（输入 `y` 确认）
4. 执行 `git worktree prune` 并显示清理后的 worktree 列表

若需跳过确认直接清理，可加 `--force`：

```bash
bash .claude/skills/git.worktree.prune/scripts/worktree-prune.sh --force
```

## 📍 USER INPUT DELIMITER

此命令不接受额外参数，直接执行脚本即可：

```
/git.worktree.prune
```