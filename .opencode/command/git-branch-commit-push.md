# Git 分支创建、提交和推送

创建新分支、提交代码并推送到 GitHub。

## 操作流程

1. 从当前分支创建新分支（格式：MMDD-描述）
2. 暂存所有修改（排除备份文件）
3. 提交代码（中文 commit message）
4. 推送到 GitHub

## 使用方法

```
/git-branch-commit-push MMDD-描述
```

### 参数说明

- `MMDD`：月份和日期，如 `0531`
- `描述`：本次修改的简要描述，如 `e2e-test-optimization`

### 示例

```
/git-branch-commit-push 0531-e2e-test-optimization
```

## 自动执行步骤

1. 检查当前分支状态
2. 创建新分支 `MMDD-描述`
3. 暂存所有修改（`git add -A`）
4. 排除不需要的文件（如 `.tar`、`.codegraph/`）
5. 生成 commit message（中文）
6. 提交代码
7. 推送到 GitHub
8. 返回 PR 创建链接

## 注意事项

- 分支名必须以日期开头（MMDD）
- commit message 必须使用中文
- 自动排除备份文件和临时目录
- 需要预先配置好 GitHub 远程仓库