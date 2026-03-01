# Davis Contract System — Decisions

## Expiry Reminder Days
- DECISION: Add `private transient Integer reminderDays` field to CmsContract (not persisted)
- getStatus() uses this field instead of hardcoded 30
- Service enriches it from ISysConfigService.selectConfigByKey("cms.reminder.days")
- Default fallback: 30 days if config not found

## Province/City/District Data
- DECISION: Static JSON file at ruoyi-ui/src/assets/json/china-area.json
- No external API dependency, works offline
- Format: [{value:'省', label:'省', children:[{value:'市', children:[{value:'区'}]}]}]

## Price Negotiation
- DECISION: Modify CmsTask.currentAmount, NOT original contract
- Original contract amount (originalAmount) is immutable
- currentAmount tracks negotiated price

## Notification System
- DECISION: New cms_notification table (NOT reusing sys_notice)
- sys_notice lacks per-user and read/unread semantics
- Fields: notification_id, user_id, title, content, notification_type, related_id, is_read, create_time

## Financial Ledger
- DECISION: No separate ledger table — query from cms_contract + cms_task
- DECISION: No real-time refresh — manual only
- DECISION: Admin-only via @PreAuthorize("@ss.hasPermi('cms:ledger:list')")

## Amount Hiding
- DECISION: Dual enforcement — backend nullifies + frontend v-if
- Contract list: admin-only amount/profit columns
- Task list: admin + accountant can see amounts (accountant needs it for negotiation)

## Data Scoping
- Accountant: only sees contracts where ownerId = current user
- Sales: only sees contracts where createBy = current user login name
- Admin: sees all
- Implemented in Service layer (NOT Mapper XML, NOT @DataScope)
