# README.$FEATURE_ID.md 模板

创建 worktree 时，将此模板写入 `$WORKTREE_DIR/README.$FEATURE_ID.md`，用实际 Jira 字段替换占位符。

---

```markdown
# $FEATURE_ID: <summary>
## 任务信息
- **状态**: <status>
- **负责人**: <assignee>
- **优先级**: <priority>
- **标签**: <labels>
- **组件**: <components>
- **Jira 链接**: https://go-jek.atlassian.net/browse/$FEATURE_ID

## 描述
<description>

## 验收条件
<从 description 中提取，或列出 subtasks>

## 子任务
<subtasks 列表，如有>

## AI 协作指南

### OpenSpec 工作流程
1. **探索**: `/opsx:explore` — 分析现状、澄清需求（不写代码）
2. **提案**: `/opsx:propose <name>` — 生成 proposal / design / tasks
3. **实施**: `/opsx:apply` — 按 tasks.md 逐步实现
4. **测试**: `/go.gen.test <路径>` — 为 Go 代码生成测试
5. **提交**: `/git.commit` — 生成规范的 commit message
6. **归档**: `/opsx:archive` — 合并 delta spec 到主 spec

### 建议的起手式
<由 AI 根据 Jira 任务内容动态生成，见 SKILL.md 步骤 9>
```
