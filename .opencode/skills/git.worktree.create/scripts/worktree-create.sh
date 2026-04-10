#!/usr/bin/env bash
# Usage: ./.claude/skills/git.worktree.create/scripts/worktree-create.sh <FEATURE_ID>
# Example: worktree-create.sh FLEET-123
# Note: This script is located at .claude/skills/git.worktree.create/scripts/
set -euo pipefail

# ── 1. 解析参数 ────────────────────────────────────────────────────────────────
FEATURE_RAW="${1:-}"
if [[ -z "$FEATURE_RAW" ]]; then
  echo "Usage: $0 <FEATURE_ID>  (e.g. 123, FLEET-123, fleet-123)" >&2
  exit 1
fi

# 标准化为 FLEET-XXX
FEATURE_ID=$(echo "$FEATURE_RAW" | sed -E 's/^[Ff][Ll][Ee][Ee][Tt]-?/FLEET-/' | sed -E 's/^([0-9]+)$/FLEET-\1/')

# ── 2. 计算路径与分支名 ────────────────────────────────────────────────────────
PROJECT_ROOT="$(git rev-parse --show-toplevel)"
WORKTREE_PARENT="$(dirname "$PROJECT_ROOT")"
USERNAME="$(git config user.name | tr '[:upper:]' '[:lower:]' | tr -cd 'a-z0-9')"
BRANCH_NAME="feature/${FEATURE_ID}-${USERNAME}"
WORKTREE_DIR="${WORKTREE_PARENT}/fleet-meta-${FEATURE_ID}"

# ── 3. 前置检查 ────────────────────────────────────────────────────────────────
if [[ -d "$WORKTREE_DIR" ]]; then
  for sub in fleet-backend fleet-portal fleet-test; do
    if [[ -d "$WORKTREE_DIR/$sub" ]]; then
      echo "Error: $WORKTREE_DIR/$sub already exists. Aborting." >&2
      exit 1
    fi
  done
fi

# ── 4. 创建目录 + 软链接 ───────────────────────────────────────────────────────
mkdir -p "$WORKTREE_DIR"

# 跳过列表：子仓库、.git、.idea（.idea 单独处理）
SKIP_ITEMS=(.git fleet-backend fleet-portal fleet-test .idea)

for src in "$PROJECT_ROOT"/* "$PROJECT_ROOT"/.*; do
  item="$(basename "$src")"
  [[ "$item" == "." || "$item" == ".." ]] && continue
  skip=false
  for s in "${SKIP_ITEMS[@]}"; do
    [[ "$item" == "$s" ]] && skip=true && break
  done
  $skip && continue
  ln -sf "$src" "$WORKTREE_DIR/$item"
done

# .idea 复制（避免两个 worktree 共享 IDE 配置）
if [[ -d "$PROJECT_ROOT/.idea" ]]; then
  cp -r "$PROJECT_ROOT/.idea" "$WORKTREE_DIR/.idea"
fi

echo "✓ 软链接和目录结构已创建"

# ── 6. 创建子仓 worktree ───────────────────────────────────────────────────────
for sub in fleet-backend fleet-portal fleet-test; do
  repo="$PROJECT_ROOT/$sub"
  base="$(git -C "$repo" rev-parse --abbrev-ref HEAD)"

  # fetch 基线分支确保最新
  echo "→ [$sub] fetching origin/$base ..."
  git -C "$repo" fetch origin "$base" --quiet

  # 若分支已存在则先删除，确保从最新 base 重新创建
  if git -C "$repo" show-ref --verify --quiet "refs/heads/$BRANCH_NAME"; then
    echo "→ [$sub] branch $BRANCH_NAME exists, deleting ..."
    git -C "$repo" branch -D "$BRANCH_NAME"
  fi

  echo "→ [$sub] creating worktree on branch $BRANCH_NAME (base: $base) ..."
  git -C "$repo" worktree add -b "$BRANCH_NAME" "$WORKTREE_DIR/$sub" "origin/$base"
  echo "✓ [$sub] done"
done

# ── 7. 复制 .env 文件 ──────────────────────────────────────────────────────────
for sub in fleet-backend fleet-portal fleet-test; do
  for env_file in .env .env.local .env.development; do
    src="$PROJECT_ROOT/$sub/$env_file"
    [[ -f "$src" ]] || continue
    cp "$src" "$WORKTREE_DIR/$sub/$env_file"
    echo "✓ copied $sub/$env_file"
  done
done

# ── 8. 总结 ────────────────────────────────────────────────────────────────────
echo ""
echo "═══════════════════════════════════════════"
echo "  Worktree 创建成功！"
echo "  分支: $BRANCH_NAME"
echo "  路径: $WORKTREE_DIR"
echo "  下一步: cd $WORKTREE_DIR"
echo "═══════════════════════════════════════════"
