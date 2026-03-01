# Davis Contract System — Issues

## Known Issues
- sql/davis.sql Section 4 (cms_communication) has copy-paste bug: creates cms_task DDL instead of cms_communication
  - FIX: Replace with correct cms_communication DDL

## Scope Exclusions
- 用户管理: Not in scope — RuoYi provides sufficient user management
- 工商 role: Not implemented
- 部门经理 role: Not implemented
- SMS/email notifications: Not in scope
- WebSocket: Not in scope — use polling (min 30s interval)
