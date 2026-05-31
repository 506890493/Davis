# 合同、客户、任务逻辑删除改造设计

**日期**：2026-05-30
**状态**：待实现

---

## 1. 背景

当前 `cms_contract`、`cms_customer`、`cms_task` 三张表虽然都有 `del_flag` 列，但实际删除行为不一致：
- **合同**：Mapper 使用硬删除（`DELETE FROM`），且列表查询不过滤 `del_flag`
- **客户**：已实现软删除（`UPDATE SET del_flag = '1'`），但删除标记值用 `'1'`，与 RuoYi 惯例 `'2'` 不一致
- **任务**：Mapper 使用硬删除，`del_flag` 列完全未被使用

前端需要统一只展示未删除的数据。

## 2. 目标

1. 三张表统一使用软删除（`UPDATE SET del_flag = '2'`）
2. 所有查询统一过滤 `del_flag = '0'`
3. 前端列表自动只展示未删除数据（后端过滤，前端无需改动）
4. `del_flag` 删除标记值统一为 RuoYi 惯例：`'0'` = 存在，`'2'` = 删除

## 3. 数据库变更

三张表 `del_flag` 列均已存在，无需新增字段。

**数据修正 SQL**（新迁移文件 `sql/update_20260530.sql`）：

```sql
-- 统一已有软删除数据：客户表之前用 '1'，改为 '2'
UPDATE cms_customer SET del_flag = '2' WHERE del_flag = '1';
```

## 4. 后端变更

### 4.1 CmsContractMapper.xml

| 操作 | 当前 | 改为 |
|------|------|------|
| `selectCmsContractList` | 无 del_flag 过滤 | 加 `AND c.del_flag = '0'` |
| `selectCmsContractByContractId` | 无 del_flag 过滤 | 加 `AND del_flag = '0'` |
| `deleteCmsContractByContractId` | `DELETE FROM cms_contract` | `UPDATE cms_contract SET del_flag = '2'` |
| `deleteCmsContractByContractIds` | `DELETE FROM cms_contract` | `UPDATE cms_contract SET del_flag = '2'` |

**注意**：Dashboard 相关查询（`selectExpiringContracts` 等）已经过滤 `del_flag = '0'`，无需改动。

### 4.2 CmsContractServiceImpl.java

移除 `deleteCmsContractByContractIds()` 和 `deleteCmsContractByContractId()` 中的级联硬删除附件调用（`deleteCmsFileByContractIds`），合同软删除后附件保留，可通过 `contract_id` 找回。

### 4.3 CmsCustomerMapper.xml

| 操作 | 当前 | 改为 |
|------|------|------|
| `selectCmsCustomerById` | 无 del_flag 过滤 | 加 `AND del_flag = '0'` |
| `deleteCmsCustomerById` | `SET del_flag = '1'` | `SET del_flag = '2'` |
| `deleteCmsCustomerByIds` | `SET del_flag = '1'` | `SET del_flag = '2'` |

**注意**：列表查询 `selectCmsCustomerList` 和统计查询已过滤 `del_flag = '0'`，无需改动。

### 4.4 CmsTaskMapper.xml

| 操作 | 当前 | 改为 |
|------|------|------|
| `selectCmsTaskList` | 无 del_flag 过滤 | 加 `AND t.del_flag = '0'` |
| `selectCmsTaskByTaskId` | 无 del_flag 过滤 | 加 `AND del_flag = '0'` |
| `deleteCmsTaskByTaskId` | `DELETE FROM cms_task` | `UPDATE cms_task SET del_flag = '2'` |
| `deleteCmsTaskByTaskIds` | `DELETE FROM cms_task` | `UPDATE cms_task SET del_flag = '2'` |

### 4.5 不改动的文件

- `CmsFile`：附件不参与本次改造，保留硬删除，但合同软删除后不再触发附件级联删除
- `CmsApproval`：审批记录作为审计数据，保留硬删除
- `CmsNotification`：无删除操作
- `CmsTaskLog`：操作日志作为审计数据，保留硬删除

## 5. 前端变更

**无需改动**。后端查询统一加 `del_flag = '0'` 后，列表接口返回的数据天然不含已删除记录，前端删除按钮调用后端接口即可，无需感知软删除变化。

## 6. 影响范围

| 维度 | 说明 |
|------|------|
| 接口兼容 | 删除接口返回值不变（`AjaxResult`），对前端透明 |
| 数据恢复 | 软删除后数据仍在表中，可通过 SQL 直接恢复：`UPDATE xxx SET del_flag = '0' WHERE del_flag = '2'` |
| 附件恢复 | 合同软删除后附件保留，通过 `contract_id` 仍可查回 |
| 唯一索引 | 当前三表无基于业务字段的唯一索引，软删除不会导致"已删除数据阻碍新数据插入"的问题 |

## 7. 涉及文件清单

```
sql/update_20260530.sql                              # 新建迁移文件
ruoyi-system/src/main/resources/mapper/system/CmsContractMapper.xml
ruoyi-system/src/main/java/.../service/impl/CmsContractServiceImpl.java
ruoyi-system/src/main/resources/mapper/system/CmsCustomerMapper.xml
ruoyi-system/src/main/resources/mapper/system/CmsTaskMapper.xml
```
