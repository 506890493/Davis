# Fleet Git Worktree 命名规范

## FEATURE_ID 转换

| 输入格式 | 示例 | 转换结果 |
|---------|------|---------|
| 纯数字 | `256` | `FLEET-256` |
| 小写带连字符 | `fleet-256` | `FLEET-256` |
| 小写无连字符 | `fleet256` | `FLEET-256` |
| 已规范化 | `FLEET-256` | `FLEET-256` |
| Plan 路径 | `.opencode/plans/fleet-256/database.md` | `FLEET-256` |

**转换逻辑**: 提取数字部分，加上 `FLEET-` 前缀，全部大写。

## SUBTASK 提取（仅路径模式）

| 路径格式 | SUBTASK | 说明 |
|---------|---------|------|
| `.opencode/plans/fleet-256/database.md` | `database` | 文件名去掉扩展名 |
| `.opencode/plans/fleet-256/api-spec.md` | `api-spec` | 文件名去掉扩展名 |
| `.opencode/plans/fleet-256/main.md` | _(空)_ | 特殊文件名 → 主分支 |
| `.opencode/plans/fleet-256/index.md` | _(空)_ | 特殊文件名 → 主分支 |
| `.opencode/plans/fleet-256/README.md` | _(空)_ | 特殊文件名 → 主分支 |
| 非路径输入（如 `256`） | _(空)_ | 无 SUBTASK → 主分支 |

**特殊文件名**: `main.md`、`index.md`、`README.md` 视为主分支，不提取 SUBTASK。

## 分支命名

| 类型 | 格式 | 示例 |
|------|------|------|
| 主分支 | `feature/FLEET-{ID}-{USERNAME}` | `feature/FLEET-256-jianxingzhang` |
| 子分支 | `feature/FLEET-{ID}-{SUBTASK}-{USERNAME}` | `feature/FLEET-256-database-jianxingzhang` |

## Worktree 目录命名

目录位于项目根目录的**父目录**下：

| 类型 | 格式 | 示例 |
|------|------|------|
| 主分支 | `fleet-meta-FLEET-{ID}` | `../fleet-meta-FLEET-256` |
| 子分支 | `fleet-meta-FLEET-{ID}-{SUBTASK}` | `../fleet-meta-FLEET-256-database` |

## USERNAME 获取方式

运行以下命令获取并转换：
```
git config user.name → 转小写 → 去除非字母数字字符
```
例如: `jianxing zhang` → `jianxingzhang`
