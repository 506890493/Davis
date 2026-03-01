# Davis Contract System — Learnings

## Database State (Verified via MySQL)
- 10 dict types already exist: cms_audit_status, cms_contract_status, cms_contract_type, cms_file_category, cms_pay_cycle, cms_pay_method, cms_reminder_status, cms_task_priority, cms_task_status, cms_tax_type
- 4 roles exist: admin(role_id=1), accountant(role_id=2), manager(role_id=100), sales(role_id=101)
- Menus: 合同管理(2010, parent_id=0)→代账合同(2023)+地址出售(2024), 任务管理(2016), 审批管理(2004)
- 5 tables: cms_contract, cms_task, cms_approval, cms_file, cms_communication
- Menu component path pattern: `system/contract/index` (uses `system/` prefix, NOT `cms/`)
- DB connection: `docker exec mysql8 mysql -u root -p123456 --default-character-set=utf8mb4 davis-backend -e "SQL" 2>&1`

## Missing (to be created)
- cms_task_type dict (普通/催收/续费/终止)
- cms.reminder.days sys_config param
- 总账报表 menu (parent_id=2010, component=system/ledger/index)
- cms:task:dispatch button perm
- cms:contract:audit button perm
- cms_notification table

## Code Conventions
- Controllers extend BaseController, Entities extend BaseEntity
- Pattern: Controller → Service Interface → ServiceImpl → Mapper → Mapper XML
- Every endpoint: @PreAuthorize("@ss.hasPermi('...')")
- CUD operations: @Log(title, businessType)
- RequestMapping path: /system/feature (NOT /cms/feature)
- Service interface: I[Entity]Service, Impl: [Entity]ServiceImpl
- List query: select[Entity]List, Get by ID: select[Entity]ById
- Paginated: startPage() + getDataTable(list) → TableDataInfo
- CRUD single: AjaxResult via toAjax() or success()

## Task Type Values
- CmsTask.status: '0'=待处理, '1'=进行中, '2'=待审批, '3'=已退回, '4'=已完成
- CmsTask.taskType: '0'=普通, '1'=催收, '2'=续费, '3'=终止

## Worktree
- All work done in: /Users/shipeter/codes/davis/Davis-work
- Main repo at: /Users/shipeter/codes/davis/Davis
