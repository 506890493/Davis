#!/bin/bash
# 从指定 repo 的当前分支名提取 Jira ticket ID
# 用法: ./extract_jira_ticket.sh [repo_path]

REPO_PATH="${1:-$(pwd)}"

BRANCH=$(git -C "$REPO_PATH" branch --show-current 2>/dev/null || echo "")

if [[ -z "$BRANCH" ]]; then
    echo "FLEET-000"
    exit 0
fi

TICKET=$(echo "$BRANCH" | grep -oE '(FLEET|fleet)-[0-9]+' | head -n 1 || echo "")

if [[ -n "$TICKET" ]]; then
    echo "$TICKET" | tr '[:lower:]' '[:upper:]'
else
    echo "FLEET-000"
fi
