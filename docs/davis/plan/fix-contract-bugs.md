# 修复代账合同创建的两个 Bug

## Bug 1: 创建代账合同时没有通知提醒

### 根因
`CmsContractServiceImpl.insertCmsContract()` 方法中完全没有调用通知服务。通知系统 `ICmsNotificationService.createNotification()` 已完整可用，但从未在合同创建场景被调用。

### 修复方案
在 `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsContractServiceImpl.java` 中：

1. 注入 `ICmsNotificationService` 和 `ISysUserService`
2. 在 `insertCmsContract()` 方法的 `return rows;` 前，查询所有拥有 **`admin` 或 `manager`** 角色的用户，逐一调用 `createNotification()` 发送审批提醒

通知参数：
- type: `"approval"`
- title: `"新合同待审批: [合同名称]"`
- content: `"合同 [合同编号] - [公司名称] 已提交，请及时审批"`
- relatedId: `contractId`

### 影响范围
- `CmsContractServiceImpl.java`：新增 2 个字段注入 + 通知发送逻辑

---

## Bug 2: 审批通过后 contract_type=0 导致前端不显示

### 根因
- `add.vue` 和 `edit.vue` 用了 `label="0"` 表示代账，`label="1"` 表示地址租赁
- `index.vue` 列表页期望 `dictAccounting: "1"`（代账）和 `dictRent: "2"`（地址租赁）
- 域模型注释也明确：`1代账报税 2地址出售`
- 创建时存入了 `contract_type = "0"`，审批后在代账列表（`contractType = "1"`）和地址租赁列表（`contractType = "2"`）都查不到

### 修复方案

**前端**（2 个文件）：
1. `add.vue`：radio `label="0"` → `label="1"`，`label="1"` → `label="2"`，默认值 `"0"` → `"1"`，模板 `v-if` 条件同步修改
2. `edit.vue`：同上修改

**数据库迁移**（手动执行）：
```sql
UPDATE cms_contract SET contract_type = '1' WHERE contract_type = '0';
```

### 影响范围
- `add.vue`：第 10-11 行 radio label + 第 40/83 行 v-if 条件 + 第 181 行默认值
- `edit.vue`：第 10-11 行 radio label + 第 40/83 行 v-if 条件 + 第 182 行默认值
