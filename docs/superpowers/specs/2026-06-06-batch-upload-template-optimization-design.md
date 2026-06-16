# 批量上传模板优化设计文档

**日期**：2026-06-06
**状态**：待审查
**分支**：0531-e2e-test-optimization

---

## 1. 背景与问题

### 1.1 现状

当前批量上传合同功能存在以下问题：

1. **模板字段冗余**：下载的模板文件基于 `CmsContract` 实体的 `@Excel` 注解生成，包含了全部 27 个字段，其中 8 个为系统自动生成或关联字段，用户在手动填写模板时无法也不需要填写
2. **合同类型未区分**：代账合同（contractType=1）和地址出售合同（contractType=2）共用同一个模板，两种类型的特有字段混在一起，增加用户填写难度和出错风险
3. **导入逻辑不完善**：导入时未对系统字段（审核状态、催交状态、归属人等）自动设置默认值

### 1.2 当前模板字段（27个）

```
合同编号, 合同/公司名称, 合同类型, 法人, 联系人, 联系电话, 联系邮箱,
收费标准, 实际收款金额, 付款周期, 收款日期, 收款方式, 合同开始日期,
合同结束日期, 税务类型, 成立日期, 租赁地址, 是否已出租, 利润,
归属人ID, 归属部门ID, 父合同ID, 客户ID, 客户名称, 审核状态, 催交状态, 附件列表
```

### 1.3 用户在用的实际表格（12列）

```
序号, 公司名称, 开始日期, 截至日期, 法人, 成立日期, 身份证号码,
收费标准, 收款日期, 税务类型, 备注, 做账会计
```

---

## 2. 设计目标

1. 按合同类型生成不同的导入模板（代账合同 vs 地址出售合同）
2. 从模板中移除用户无需填写的系统字段
3. 导入时自动设置系统字段默认值
4. 保持向后兼容，不传类型参数时默认下载代账合同模板
5. 增强导入数据校验和错误提示

---

## 3. 字段设计

### 3.1 字段分类

#### 通用字段（16个，两种类型都需要）

| 字段 | Java属性 | 必填 | 说明 |
|------|----------|------|------|
| 合同编号 | contractCode | 否 | 系统可自动生成 |
| 合同/公司名称 | contractName | 是 | |
| 合同类型 | contractType | 是 | 字典 `cms_contract_type` |
| 法人 | legalPerson | 否 | |
| 联系人 | contactPerson | 是 | |
| 联系电话 | contactPhone | 是 | |
| 联系邮箱 | contactEmail | 否 | |
| 收费标准 | amount | 是 | 必须>0 |
| 实际收款金额 | actualAmount | 否 | |
| 付款周期 | paymentCycle | 是 | 字典 `cms_pay_cycle` |
| 收款日期 | paymentDate | 否 | |
| 收款方式 | paymentMethod | 否 | 字典 `cms_pay_method` |
| 合同开始日期 | startDate | 是 | |
| 合同结束日期 | endDate | 是 | 必须≥开始日期 |
| 利润 | profit | 否 | |
| 备注 | remark | 否 | |

#### 代账合同特有字段（2个）

| 字段 | Java属性 | 必填 | 说明 |
|------|----------|------|------|
| 税务类型 | taxType | 是 | 字典 `cms_tax_type`，仅 contractType=1 时出现在模板 |
| 成立日期 | establishmentDate | 否 | 仅 contractType=1 时出现在模板 |

#### 地址出售合同特有字段（2个）

| 字段 | Java属性 | 必填 | 说明 |
|------|----------|------|------|
| 租赁地址 | rentalAddress | 是 | 仅 contractType=2 时出现在模板 |
| 是否已出租 | isRented | 是 | 0=否, 1=是，仅 contractType=2 时出现在模板 |

#### 系统字段（从模板中移除，8个）

| 字段 | Java属性 | 替代逻辑 |
|------|----------|----------|
| 归属人ID | ownerId | 导入时设为当前登录用户 |
| 归属部门ID | deptId | 导入时设为当前用户所属部门 |
| 父合同ID | parentId | 默认 null |
| 客户ID | customerId | 默认 null |
| 客户名称 | customerName | 默认 null |
| 审核状态 | auditStatus | 默认 "0"（待审批） |
| 催交状态 | reminderStatus | 默认 "0" |
| 附件列表 | annex | 默认 null |

### 3.2 各模板最终字段

**代账合同模板（18列）**：
合同编号, 合同/公司名称, 合同类型, 法人, 联系人, 联系电话, 联系邮箱, 收费标准, 实际收款金额, 付款周期, 收款日期, 收款方式, 合同开始日期, 合同结束日期, 利润, 备注, **税务类型**, **成立日期**

**地址出售合同模板（18列）**：
合同编号, 合同/公司名称, 合同类型, 法人, 联系人, 联系电话, 联系邮箱, 收费标准, 实际收款金额, 付款周期, 收款日期, 收款方式, 合同开始日期, 合同结束日期, 利润, 备注, **租赁地址**, **是否已出租**

---

## 4. 接口设计

### 4.1 模板下载接口

```
POST /system/contract/importTemplate
```

**参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| contractType | String | 否 | "1" | 1=代账合同, 2=地址出售合同 |

**响应**：Excel 文件流

**文件名**：
- contractType=1 → `代账合同数据.xlsx`
- contractType=2 → `地址出售合同数据.xlsx`

**向后兼容**：不传 `contractType` 参数时默认下载代账合同模板。

### 4.2 批量导入接口

```
POST /system/contract/importData
```

保持不变，Excel 数据由现有 `ExcelUtil.importExcel()` 解析。

---

## 5. 后端实现方案

### 5.1 Controller 层改造

**文件**：`ruoyi-admin/src/main/java/com/ruoyi/web/controller/davis/CmsContractController.java`

```java
@PreAuthorize("@ss.hasPermi('system:contract:import')")
@PostMapping("/importTemplate")
public void importTemplate(HttpServletResponse response,
                          @RequestParam(required = false, defaultValue = "1") String contractType) {
    ExcelUtil<CmsContract> util = new ExcelUtil<>(CmsContract.class);

    // 系统字段：全部隐藏
    String[] systemFields = {"parentId", "customerId", "customerName",
            "auditStatus", "reminderStatus", "annex", "ownerId", "deptId"};

    // 合同类型特有字段
    String[] accountingFields = {"rentalAddress", "isRented"};  // 代账合同不需要
    String[] addressFields = {"taxType", "establishmentDate"};   // 地址出售合同不需要

    if ("1".equals(contractType)) {
        // 代账合同：隐藏地址出售字段 + 系统字段
        util.hideColumn(combine(addressFields, systemFields));
        util.importTemplateExcel(response, "代账合同数据");
    } else if ("2".equals(contractType)) {
        // 地址出售合同：隐藏代账字段 + 系统字段
        util.hideColumn(combine(accountingFields, systemFields));
        util.importTemplateExcel(response, "地址出售合同数据");
    }
}
```

### 5.2 Service 层改造

**文件**：`ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsContractServiceImpl.java`

在 `importCmsContract` 方法中增加系统字段默认值设置：

```java
for (CmsContract contract : contractList) {
    // 1. 自动生成合同编号（如果用户未填写）
    if (StringUtils.isEmpty(contract.getContractCode())) {
        contract.setContractCode(generateContractCode());
    }

    // 2. 设置审核状态为待审批
    contract.setAuditStatus("0");

    // 3. 设置催交状态为默认值
    if (StringUtils.isEmpty(contract.getReminderStatus())) {
        contract.setReminderStatus("0");
    }

    // 4. 设置归属人和部门（使用当前登录用户）
    contract.setOwnerId(SecurityUtils.getUserId());
    contract.setDeptId(SecurityUtils.getDeptId());

    // 5. 客户ID、客户名称、父合同ID、附件列表保持 null
}
```

### 5.3 利用现有能力

`ExcelUtil` 已提供 `hideColumn(String... fields)` 方法（第224行），无需新增工具方法。该方法的参数接收 Java 属性名，使用 `@Excel` 注解中的属性名（以 Field 名匹配）来排除列。

---

## 6. 前端实现方案

**文件**：`ruoyi-ui/src/views/system/contract/index.vue`

将原来的「下载模板」按钮改为下拉菜单：

```html
<el-dropdown @command="handleDownloadTemplate" style="margin-left: 10px;">
  <el-button type="primary" size="mini">
    下载模板<i class="el-icon-arrow-down el-icon--right"></i>
  </el-button>
  <el-dropdown-menu slot="dropdown">
    <el-dropdown-item command="1">代账合同模板</el-dropdown-item>
    <el-dropdown-item command="2">地址出售合同模板</el-dropdown-item>
  </el-dropdown-menu>
</el-dropdown>
```

```javascript
// 下载模板
handleDownloadTemplate(contractType) {
  window.location.href =
    `${process.env.VUE_APP_BASE_API}/system/contract/importTemplate?contractType=` + contractType;
}
```

> **注意**：如果前端未改造，仍可通过不带参数调用原接口，默认下载代账合同模板。

---

## 7. 数据校验规则

### 7.1 字段级校验

| 校验规则 | 触发条件 | 错误提示 |
|----------|----------|----------|
| 合同/公司名称 非空 | 值为空 | `[合同/公司名称] 不能为空` |
| 合同类型 必须为 1 或 2 | 值不为 1 或 2 | `[合同类型] 必须是 1(代账报税) 或 2(地址出售)` |
| 联系人 非空 | 值为空 | `[联系人] 不能为空` |
| 联系电话 非空 | 值为空 | `[联系电话] 不能为空` |
| 收费标准 > 0 | 值 ≤ 0 或为空 | `[收费标准] 必须大于0` |
| 付款周期 有效 | 值不在字典中 | `[付款周期] 不是有效的付款周期` |
| 合同开始日期 非空 | 值为空 | `[合同开始日期] 不能为空` |
| 合同结束日期 非空 | 值为空 | `[合同结束日期] 不能为空` |
| 税务类型 非空 | contractType=1 且值为空 | `[税务类型] 代账合同必须填写税务类型` |
| 租赁地址 非空 | contractType=2 且值为空 | `[租赁地址] 地址出售合同必须填写租赁地址` |
| 是否已出租 非空 | contractType=2 且值为空 | `[是否已出租] 地址出售合同必须填写` |

### 7.2 跨字段校验

| 校验规则 | 触发条件 | 错误提示 |
|----------|----------|----------|
| 结束日期 ≥ 开始日期 | endDate < startDate | `[合同结束日期] 必须晚于开始日期` |

### 7.3 错误信息格式

```
第{N}行数据校验失败：[{字段名}] {具体错误描述}
```

---

## 8. 验收标准

### 8.1 模板下载

- [ ] 下载代账合同模板，包含16个通用字段+2个代账特有字段，不包含8个系统字段和2个地址出售特有字段
- [ ] 下载地址出售合同模板，包含16个通用字段+2个地址出售特有字段，不包含8个系统字段和2个代账特有字段
- [ ] 模板文件名区分：`代账合同数据.xlsx` / `地址出售合同数据.xlsx`
- [ ] 不传 `contractType` 参数时默认下载代账合同模板（向后兼容）

### 8.2 批量导入

- [ ] 导入代账合同数据（填写税务类型等字段），保存成功
- [ ] 导入地址出售合同数据（填写租赁地址等字段），保存成功
- [ ] 未填写合同编号时，系统自动生成
- [ ] 审核状态自动设置为"待审批"（'0'）
- [ ] 催交状态自动设置为默认值
- [ ] 归属人自动设置为当前登录用户
- [ ] 归属部门自动设置为当前用户所属部门

### 8.3 数据校验

- [ ] 必填字段为空时校验失败，提示具体行号和字段
- [ ] 代账合同缺少税务类型时校验失败
- [ ] 地址出售合同缺少租赁地址时校验失败
- [ ] 结束日期早于开始日期时校验失败
- [ ] 合同类型填写无效值时校验失败

### 8.4 兼容性

- [ ] 现有导出功能不受影响
- [ ] 现有单条新增/编辑功能不受影响
- [ ] 前端未改造时，原下载按钮仍然可用

---

## 9. 影响范围

| 文件 | 改动类型 | 说明 |
|------|----------|------|
| `CmsContractController.java` | 修改 | `importTemplate` 增加 `contractType` 参数 |
| `CmsContractServiceImpl.java` | 修改 | 导入时增加系统字段默认值设置 |
| `contract/index.vue` | 修改 | 下载按钮改为下拉菜单 |
| `ContractBatchUploadE2ETest.java` | 修改 | 适配新的模板字段 |

### 不受影响的文件

- `CmsContract.java`（Domain 实体）— 不修改 `@Excel` 注解
- `ExcelUtil.java`（工具类）— 不修改，利用现有 `hideColumn()` 和 `showColumn()` 方法
- `export` 接口 — 不修改
- 数据库表结构 — 不修改
