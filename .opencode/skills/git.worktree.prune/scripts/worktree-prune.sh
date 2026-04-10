#!/usr/bin/env bash
# Usage: ./.claude/skills/git.worktree.prune/scripts/worktree-prune.sh [--force]
# --force: skip confirmation and prune all invalid worktrees + delete branches
set -euo pipefail

FORCE=false
[[ "${1:-}" == "--force" ]] && FORCE=true

PROJECT_ROOT="$(git rev-parse --show-toplevel)"
REPOS=(fleet-backend fleet-portal fleet-test)

# ── 1. 用 --porcelain 格式检测各子仓的无效 worktree 及关联分支 ────────────────
# porcelain 格式每个 worktree 块以空行分隔，prunable 块包含 "prunable" 字段
declare -A PRUNABLE_PATHS   # repo -> 换行分隔的无效路径
declare -A PRUNABLE_BRANCHES # repo -> 换行分隔的关联分支（去重后）
HAS_ANY=false

for repo in "${REPOS[@]}"; do
  repo_path="$PROJECT_ROOT/$repo"
  if [[ ! -d "$repo_path" ]]; then
    echo "⚠  跳过 $repo：目录不存在"
    continue
  fi

  # 解析 --porcelain 输出，收集 prunable 块的路径和分支
  paths=""
  branches=""
  current_path=""
  current_branch=""
  is_prunable=false

  while IFS= read -r line; do
    if [[ -z "$line" ]]; then
      # 块结束，若该块是 prunable 则记录
      if $is_prunable; then
        paths+="$current_path"$'\n'
        if [[ -n "$current_branch" ]]; then
          branches+="$current_branch"$'\n'
        fi
      fi
      current_path=""
      current_branch=""
      is_prunable=false
    elif [[ "$line" == worktree\ * ]]; then
      current_path="${line#worktree }"
    elif [[ "$line" == branch\ * ]]; then
      current_branch="${line#branch refs/heads/}"
    elif [[ "$line" == prunable\ * ]]; then
      is_prunable=true
    fi
  done < <(git -C "$repo_path" worktree list --porcelain; echo "")

  if [[ -n "$paths" ]]; then
    PRUNABLE_PATHS[$repo]="$paths"
    # 去重分支
    PRUNABLE_BRANCHES[$repo]=$(echo "$branches" | sort -u | grep -v '^$' || true)
    HAS_ANY=true
  fi
done

# ── 2. 若无需清理则退出 ─────────────────────────────────────────────────────────
if ! $HAS_ANY; then
  echo "✓ 所有子仓都没有发现无效的 worktree 引用，无需清理。"
  exit 0
fi

# ── 3. 展示发现的无效引用和关联分支 ────────────────────────────────────────────
echo ""
echo "发现以下无效 worktree 引用（目录已删除但 Git 引用仍存在）："
echo ""
for repo in "${REPOS[@]}"; do
  [[ -z "${PRUNABLE_PATHS[$repo]:-}" ]] && continue
  echo "  [$repo] 无效路径："
  while IFS= read -r p; do
    [[ -z "$p" ]] && continue
    echo "    - $p"
  done <<< "${PRUNABLE_PATHS[$repo]}"
  if [[ -n "${PRUNABLE_BRANCHES[$repo]:-}" ]]; then
    echo "  [$repo] 将删除的分支："
    while IFS= read -r b; do
      [[ -z "$b" ]] && continue
      echo "    - $b"
    done <<< "${PRUNABLE_BRANCHES[$repo]}"
  fi
  echo ""
done
echo "说明：将清理 Git worktree 引用并删除关联的本地分支（未合并的分支会报错并跳过）。"
echo ""

# ── 4. 确认 ────────────────────────────────────────────────────────────────────
if ! $FORCE; then
  read -r -p "确认清理？[y/N] " answer
  case "$answer" in
    [yY]|[yY][eE][sS]) ;;
    *) echo "已取消。"; exit 0 ;;
  esac
fi

# ── 5. 执行清理（prune + 删除分支）────────────────────────────────────────────
echo ""
for repo in "${REPOS[@]}"; do
  [[ -z "${PRUNABLE_PATHS[$repo]:-}" ]] && continue
  repo_path="$PROJECT_ROOT/$repo"

  echo "→ [$repo] 清理 worktree 引用..."
  git -C "$repo_path" worktree prune
  echo "✓ [$repo] worktree 引用已清理"

  if [[ -n "${PRUNABLE_BRANCHES[$repo]:-}" ]]; then
    while IFS= read -r branch; do
      [[ -z "$branch" ]] && continue
      if git -C "$repo_path" branch -d "$branch" 2>/dev/null; then
        echo "✓ [$repo] 分支已删除: $branch"
      else
        echo "⚠  [$repo] 分支未合并，跳过: $branch（如需强制删除请手动执行 git branch -D $branch）"
      fi
    done <<< "${PRUNABLE_BRANCHES[$repo]}"
  fi

  echo ""
done

echo "═══════════════════════════════════════"
echo "  Worktree 引用清理完成！"
echo "═══════════════════════════════════════"
