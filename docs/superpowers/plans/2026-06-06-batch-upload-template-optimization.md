# 批量上传模板优化实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 按合同类型（代账/地址出售）生成不同的导入模板，移除系统字段，导入时自动设置默认值。

**Architecture:** 后端 Controller 增加 `contractType` 参数调用 `ExcelUtil.hideColumn()` 过滤字段；Service `importCmsContract` 增加系统字段默认值赋值；前端下载按钮改为下拉菜单。不改实体注解、不改工具类、不改数据库。

**Tech Stack:** Java 8, Spring Boot 2.5.15, Apache POI, Vue 2.6 + Element UI

**Spec:** `docs/superpowers/specs/2026-06-06-batch-upload-template-optimization-design.md`

---

### Task 1: 修改 Controller — importTemplate 增加 contractType 参数

**Files:**
- Modify: `ruoyi-admin/src/main/java/com/ruoyi/web/controller/davis/CmsContractController.java:80-86`

- [ ] **Step 1: 改造 importTemplate 方法**

将原来的方法：

```java
@PreAuthorize("@ss.hasPermi('system:contract:import')")
@PostMapping("/importTemplate")
public void importTemplate(HttpServletResponse response)
{
    ExcelUtil<CmsContract> util = new ExcelUtil<CmsContract>(CmsContract.class);
    util.importTemplateExcel(response, "合同数据");
}
```

替换为：

```java
@PreAuthorize("@ss.hasPermi('system:contract:import')")
@PostMapping("/importTemplate")
public void importTemplate(HttpServletResponse response,
                          @RequestParam(required = false, defaultValue = "1") String contractType)
{
    ExcelUtil<CmsContract> util = new ExcelUtil<CmsContract>(CmsContract.class);
    // 系统字段：全部隐藏
    util.hideColumn("parentId", "customerId", "customerName",
            "auditStatus", "reminderStatus", "annex", "ownerId", "deptId");
    if ("1".equals(contractType))
    {
        // 代账合同：再隐藏地址出售特有字段
        util.hideColumn("parentId", "customerId", "customerName",
                "auditStatus", "reminderStatus", "annex", "ownerId", "deptId",
                "rentalAddress", "isRented");
        util.importTemplateExcel(response, "代账合同数据");
    }
    else
    {
        // 地址出售合同：再隐藏代账特有字段
        util.hideColumn("parentId", "customerId", "customerName",
                "auditStatus", "reminderStatus", "annex", "ownerId", "deptId",
                "taxType", "establishmentDate");
        util.importTemplateExcel(response, "地址出售合同数据");
    }
}
```

注意：`hideColumn` 只接受单次调用，所以每个分支内需要把全部要隐藏的字段在一次调用中传入。需要确认 `@RequestParam` 已 import（来自 `org.springframework.web.bind.annotation.RequestParam`）。

- [ ] **Step 2: 编译验证**

```powershell
mvn compile -pl ruoyi-admin -am -Dmaven.test.skip=true
```

预期：BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add ruoyi-admin/src/main/java/com/ruoyi/web/controller/davis/CmsContractController.java
git commit -m "feat: importTemplate接口增加contractType参数，按合同类型生成不同模板

- 代账合同模板：排除租赁地址、是否已出租 + 8个系统字段
- 地址出售合同模板：排除税务类型、成立日期 + 8个系统字段
- 不传参数默认代账合同模板（向后兼容）

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 2: 修改 Service — 导入时自动设置系统字段默认值

**Files:**
- Modify: `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsContractServiceImpl.java:267-323`

- [ ] **Step 1: 在 importCmsContract 方法中增加默认值设置**

在 `importCmsContract` 方法的 for 循环开始处（第279行 `try {` 之后），`SecurityValidationUtil` 校验之前，插入系统字段默认值设置：

```java
// 自动设置系统字段默认值
// 审核状态：统一设为待审批
contract.setAuditStatus("0");
// 催交状态：默认值
if (StringUtils.isEmpty(contract.getReminderStatus())) {
    contract.setReminderStatus("0");
}
// 归属人：当前登录用户ID
contract.setOwnerId(SecurityUtils.getUserId());
// 归属部门：当前用户所属部门
contract.setDeptId(SecurityUtils.getDeptId());
// 附件列表：默认 null（模板中已排除此字段）
contract.setAnnex(null);
```

插入位置示意（原代码第276-284行之间）：

```java
for (CmsContract contract : contractList) {
    index++;
    try {
        // >>> 插入：系统字段默认值设置 >>>
        contract.setAuditStatus("0");
        if (StringUtils.isEmpty(contract.getReminderStatus())) {
            contract.setReminderStatus("0");
        }
        contract.setOwnerId(SecurityUtils.getUserId());
        contract.setDeptId(SecurityUtils.getDeptId());
        contract.setAnnex(null);
        // <<< 插入结束 <<<

        if (StringUtils.isEmpty(contract.getContractCode())
                || StringUtils.isEmpty(contract.getContractName())) {
            // ... 原有逻辑不变
```

- [ ] **Step 2: 编译验证**

```powershell
mvn compile -pl ruoyi-system -am -Dmaven.test.skip=true
```

预期：BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsContractServiceImpl.java
git commit -m "feat: 批量导入时自动设置系统字段默认值

- auditStatus 默认为 '0'（待审批）
- reminderStatus 默认为 '0'
- ownerId 设为当前登录用户
- deptId 设为当前用户所属部门

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 3: 更新 E2E 测试 — 适配新模板字段

**Files:**
- Modify: `ruoyi-admin/src/test/java/com/ruoyi/web/controller/davis/ContractBatchUploadE2ETest.java`

测试文件中的 Excel 辅助方法需要更新表头以匹配新的模板字段，并新增模板下载的 contractType 参数测试。

- [ ] **Step 1: 更新 createValidContractExcel() 方法（第281-348行）**

将 headers 数组从包含全部字段改为只包含代账合同模板的18个字段，并更新数据行：

```java
private byte[] createValidContractExcel() throws Exception {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("代账合同数据");

    // 表头 — 代账合同模板（18列）
    Row headerRow = sheet.createRow(0);
    String[] headers = {
        "合同编号", "合同/公司名称", "合同类型", "法人", "联系人", "联系电话",
        "联系邮箱", "收费标准", "实际收款金额", "付款周期", "收款日期",
        "收款方式", "合同开始日期", "合同结束日期", "利润", "备注",
        "税务类型", "成立日期"
    };
    for (int i = 0; i < headers.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers[i]);
    }

    // 数据行1 — 代账合同
    Row dataRow1 = sheet.createRow(1);
    dataRow1.createCell(0).setCellValue("BATCH001");
    dataRow1.createCell(1).setCellValue("批量测试合同A");
    dataRow1.createCell(2).setCellValue("代账报税");
    dataRow1.createCell(3).setCellValue("张法人");
    dataRow1.createCell(4).setCellValue("联系人A");
    dataRow1.createCell(5).setCellValue("13800138001");
    dataRow1.createCell(6).setCellValue("test1@example.com");
    dataRow1.createCell(7).setCellValue(10000.00);
    dataRow1.createCell(8).setCellValue(10000.00);
    dataRow1.createCell(9).setCellValue("年付");
    dataRow1.createCell(10).setCellValue(formatDate(new Date()));
    dataRow1.createCell(11).setCellValue("银行转账");
    dataRow1.createCell(12).setCellValue("2026-01-01");
    dataRow1.createCell(13).setCellValue("2026-12-31");
    dataRow1.createCell(14).setCellValue(2000.00);
    dataRow1.createCell(15).setCellValue("测试备注A");
    dataRow1.createCell(16).setCellValue("小规模");
    dataRow1.createCell(17).setCellValue("2025-01-01");

    // 数据行2 — 地址出售合同
    Row dataRow2 = sheet.createRow(2);
    dataRow2.createCell(0).setCellValue("BATCH002");
    dataRow2.createCell(1).setCellValue("批量测试合同B");
    dataRow2.createCell(2).setCellValue("地址出售");
    dataRow2.createCell(3).setCellValue("李法人");
    dataRow2.createCell(4).setCellValue("联系人B");
    dataRow2.createCell(5).setCellValue("13800138002");
    dataRow2.createCell(6).setCellValue("test2@example.com");
    dataRow2.createCell(7).setCellValue(15000.00);
    dataRow2.createCell(8).setCellValue(15000.00);
    dataRow2.createCell(9).setCellValue("年付");
    dataRow2.createCell(10).setCellValue(formatDate(new Date()));
    dataRow2.createCell(11).setCellValue("银行转账");
    dataRow2.createCell(12).setCellValue("2026-02-01");
    dataRow2.createCell(13).setCellValue("2027-01-31");
    dataRow2.createCell(14).setCellValue(3000.00);
    dataRow2.createCell(15).setCellValue("测试备注B");
    dataRow2.createCell(16).setCellValue("一般纳税人");
    dataRow2.createCell(17).setCellValue("2025-02-01");

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    workbook.write(outputStream);
    workbook.close();
    return outputStream.toByteArray();
}
```

- [ ] **Step 2: 更新 createInvalidContractExcel() 方法（第353-382行）**

表头改为与代账合同模板一致，去掉不再需要的字段：

```java
private byte[] createInvalidContractExcel() throws Exception {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("代账合同数据");

    Row headerRow = sheet.createRow(0);
    String[] headers = {
        "合同编号", "合同/公司名称", "合同类型", "法人", "联系人", "联系电话",
        "联系邮箱", "收费标准", "实际收款金额", "付款周期", "收款日期",
        "收款方式", "合同开始日期", "合同结束日期", "利润", "备注",
        "税务类型", "成立日期"
    };
    for (int i = 0; i < headers.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers[i]);
    }

    Row dataRow1 = sheet.createRow(1);
    dataRow1.createCell(0).setCellValue("BATCH_INVALID_001");
    dataRow1.createCell(1).setCellValue("恶意合同'; DROP TABLE cms_contract;");
    dataRow1.createCell(2).setCellValue("代账报税");
    dataRow1.createCell(7).setCellValue(10000.00);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    workbook.write(outputStream);
    workbook.close();
    return outputStream.toByteArray();
}
```

- [ ] **Step 3: 更新 createUpdatedContractExcel() 方法（第387-425行）**

表头改为与代账合同模板一致：

```java
private byte[] createUpdatedContractExcel() throws Exception {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("代账合同数据");

    Row headerRow = sheet.createRow(0);
    String[] headers = {
        "合同编号", "合同/公司名称", "合同类型", "法人", "联系人", "联系电话",
        "联系邮箱", "收费标准", "实际收款金额", "付款周期", "收款日期",
        "收款方式", "合同开始日期", "合同结束日期", "利润", "备注",
        "税务类型", "成立日期"
    };
    for (int i = 0; i < headers.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers[i]);
    }

    Row dataRow1 = sheet.createRow(1);
    dataRow1.createCell(0).setCellValue("BATCH001");
    dataRow1.createCell(1).setCellValue("批量测试合同A（已更新）");
    dataRow1.createCell(2).setCellValue("代账报税");
    dataRow1.createCell(7).setCellValue(25000.00);
    dataRow1.createCell(8).setCellValue(25000.00);

    Row dataRow2 = sheet.createRow(2);
    dataRow2.createCell(0).setCellValue("BATCH002");
    dataRow2.createCell(1).setCellValue("批量测试合同B（已更新）");
    dataRow2.createCell(2).setCellValue("地址出售");
    dataRow2.createCell(7).setCellValue(30000.00);
    dataRow2.createCell(8).setCellValue(30000.00);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    workbook.write(outputStream);
    workbook.close();
    return outputStream.toByteArray();
}
```

- [ ] **Step 4: 新增测试方法 — 下载代账合同模板**

在测试类中新增（建议放在 testDownloadTemplate_Success 之后）：

```java
@Test
@DisplayName("3b. 下载代账合同模板 - 包含税务类型、成立日期字段")
void testDownloadTemplate_AccountingType() throws Exception {
    ResultActions result = mockMvc.perform(
        MockMvcRequestBuilders.post("/system/contract/importTemplate")
            .param("contractType", "1")
            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                .authentication(createManagerAuth()))
    );

    result.andExpect(status().isOk())
          .andExpect(header().exists("Content-Disposition"));

    // 读取响应 Excel 内容验证表头
    byte[] excelBytes = result.andReturn().getResponse().getContentAsByteArray();
    java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(excelBytes);
    Workbook workbook = new XSSFWorkbook(bis);
    Sheet sheet = workbook.getSheetAt(0);
    Row headerRow = sheet.getRow(0);

    // 收集所有表头
    java.util.Set<String> headerSet = new java.util.HashSet<>();
    for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
        Cell cell = headerRow.getCell(i);
        if (cell != null) {
            headerSet.add(cell.getStringCellValue());
        }
    }

    // 验证代账合同特有字段存在
    assertThat(headerSet).contains("税务类型");
    assertThat(headerSet).contains("成立日期");
    // 验证地址出售特有字段不存在
    assertThat(headerSet).doesNotContain("租赁地址");
    assertThat(headerSet).doesNotContain("是否已出租");
    // 验证系统字段不存在
    assertThat(headerSet).doesNotContain("父合同ID");
    assertThat(headerSet).doesNotContain("客户ID");
    assertThat(headerSet).doesNotContain("审核状态");
    assertThat(headerSet).doesNotContain("催交状态");
    assertThat(headerSet).doesNotContain("附件列表");
    assertThat(headerSet).doesNotContain("归属人ID (关联sys_user)");

    workbook.close();
}
```

- [ ] **Step 5: 新增测试方法 — 下载地址出售合同模板**

```java
@Test
@DisplayName("3c. 下载地址出售合同模板 - 包含租赁地址、是否已出租字段")
void testDownloadTemplate_RentalType() throws Exception {
    ResultActions result = mockMvc.perform(
        MockMvcRequestBuilders.post("/system/contract/importTemplate")
            .param("contractType", "2")
            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                .authentication(createManagerAuth()))
    );

    result.andExpect(status().isOk())
          .andExpect(header().exists("Content-Disposition"));

    byte[] excelBytes = result.andReturn().getResponse().getContentAsByteArray();
    java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(excelBytes);
    Workbook workbook = new XSSFWorkbook(bis);
    Sheet sheet = workbook.getSheetAt(0);
    Row headerRow = sheet.getRow(0);

    java.util.Set<String> headerSet = new java.util.HashSet<>();
    for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
        Cell cell = headerRow.getCell(i);
        if (cell != null) {
            headerSet.add(cell.getStringCellValue());
        }
    }

    // 验证地址出售特有字段存在
    assertThat(headerSet).contains("租赁地址");
    assertThat(headerSet).contains("是否已出租");
    // 验证代账合同特有字段不存在
    assertThat(headerSet).doesNotContain("税务类型");
    assertThat(headerSet).doesNotContain("成立日期");
    // 验证系统字段不存在
    assertThat(headerSet).doesNotContain("父合同ID");
    assertThat(headerSet).doesNotContain("客户ID");
    assertThat(headerSet).doesNotContain("审核状态");
    assertThat(headerSet).doesNotContain("催交状态");
    assertThat(headerSet).doesNotContain("附件列表");

    workbook.close();
}
```

- [ ] **Step 6: 新增测试方法 — 导入后验证系统字段默认值已设置**

```java
@Test
@DisplayName("7. 导入后系统字段默认值自动设置")
void testImportContract_SystemFieldsDefaultValues() throws Exception {
    byte[] excelData = createValidContractExcel();
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "contracts_valid.xlsx",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        excelData
    );

    mockMvc.perform(
        MockMvcRequestBuilders.multipart("/system/contract/importData")
            .file(file)
            .param("updateSupport", "false")
            .with(request -> {
                request.setMethod("POST");
                return request;
            })
            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                .authentication(createManagerAuth()))
    ).andExpect(status().isOk());

    // 查询导入的合同验证默认值
    ResultActions listResult = asManager(HttpMethod.GET, "/system/contract/list", null);
    String listJson = getResponseJson(listResult);
    // 验证审核状态为待审批（'0'）
    assertThat(listJson).contains("\"auditStatus\":\"0\"");
}
```

- [ ] **Step 7: 更新现有 importTemplate 测试（testDownloadTemplate_Success）**

将原来的测试方法增加默认参数行为的验证（向后兼容），`@DisplayName` 更新为：

```java
@Test
@DisplayName("3a. 下载导入模板（默认代账合同）— 向后兼容")
void testDownloadTemplate_Success() throws Exception {
    // 不传 contractType 参数，默认下载代账合同模板
    ResultActions result = mockMvc.perform(
        MockMvcRequestBuilders.post("/system/contract/importTemplate")
            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                .authentication(createManagerAuth()))
    );

    result.andExpect(status().isOk())
          .andExpect(header().exists("Content-Disposition"));

    String contentType = result.andReturn().getResponse().getContentType();
    assertThat(contentType).contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
}
```

- [ ] **Step 8: 运行测试验证**

```powershell
mvn test -pl ruoyi-admin -am -Dtest=ContractBatchUploadE2ETest -Dmaven.test.skip=false
```

预期：所有测试通过（包括新增的4个测试方法 + 更新后的现有测试）

- [ ] **Step 9: 提交**

```bash
git add ruoyi-admin/src/test/java/com/ruoyi/web/controller/davis/ContractBatchUploadE2ETest.java
git commit -m "test: 更新E2E测试适配新模板字段，新增合同类型模板下载测试

- 更新 Excel 辅助方法表头为新模板字段（18列）
- 新增代账合同模板下载测试
- 新增地址出售合同模板下载测试
- 新增导入后系统字段默认值验证测试

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 4: 更新前端 Vue 页面 — 下载模板改为下拉菜单

**Files:**
- Modify: `ruoyi-ui/src/views/system/contract/index.vue:178-184,713-718`
- Modify: `ruoyi-ui/src/views/system/contract/pending.vue:178-184,647-652`
- Modify: `ruoyi-ui/src/views/system/contract/rejected.vue:178-184,640-645`

三个文件的修改模式完全相同。

- [ ] **Step 1: 修改 index.vue — 模板部分**

将第178-186行的 `<el-link>` 下载模板替换为下拉菜单：

```html
<el-dropdown @command="importTemplate" style="margin-right: 5px">
  <el-link
    type="primary"
    :underline="false"
    style="font-size: 12px"
    v-hasPermi="['system:contract:import']"
  >
    下载模板<i class="el-icon-arrow-down el-icon--right"></i>
  </el-link>
  <el-dropdown-menu slot="dropdown">
    <el-dropdown-item command="1">代账合同模板</el-dropdown-item>
    <el-dropdown-item command="2">地址出售合同模板</el-dropdown-item>
  </el-dropdown-menu>
</el-dropdown>
```

- [ ] **Step 2: 修改 index.vue — 方法部分**

将第713-718行的 `importTemplate()` 方法改为接受 contractType 参数：

```javascript
importTemplate(contractType) {
  const type = contractType || "1";
  const filename = type === "1" ? "代账合同导入模板.xlsx" : "地址出售合同导入模板.xlsx";
  this.download(
    "system/contract/importTemplate",
    { contractType: type },
    filename
  );
},
```

- [ ] **Step 3: 对 pending.vue 做同样修改**

模板部分（第178-186行）：同上 Step 1 的 HTML 替换。

方法部分（第647-652行）：同上 Step 2 的 JavaScript 替换。

- [ ] **Step 4: 对 rejected.vue 做同样修改**

模板部分（第178-186行）：同上 Step 1 的 HTML 替换。

方法部分（第640-645行）：同上 Step 2 的 JavaScript 替换。

- [ ] **Step 5: 前端构建验证**

```powershell
cd ruoyi-ui; npm run build
```

预期：构建成功，无报错。

- [ ] **Step 6: 提交**

```bash
git add ruoyi-ui/src/views/system/contract/index.vue
git add ruoyi-ui/src/views/system/contract/pending.vue
git add ruoyi-ui/src/views/system/contract/rejected.vue
git commit -m "feat: 前端下载模板按钮改为下拉菜单，支持选择合同类型

- 代账合同模板（contractType=1）
- 地址出售合同模板（contractType=2）

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

### Task 5: 运行全部测试，确认无回归

- [ ] **Step 1: 运行后端全部测试**

```powershell
mvn test -pl ruoyi-admin -am -Dmaven.test.skip=false
```

预期：所有测试通过。

- [ ] **Step 2: 运行后端 E2E 测试专项**

```powershell
mvn test -pl ruoyi-admin -am -Dtest=ContractBatchUploadE2ETest -Dmaven.test.skip=false
```

预期：全部8个测试方法通过。

- [ ] **Step 3: 自检清单**

对照设计文档验收标准逐项确认：

- [x] Controller `importTemplate` 支持 contractType 参数（默认 "1"）
- [x] 代账合同模板排除 rentalAddress、isRented + 8个系统字段
- [x] 地址出售合同模板排除 taxType、establishmentDate + 8个系统字段
- [x] Service `importCmsContract` 自动设置 auditStatus="0"
- [x] Service `importCmsContract` 自动设置 reminderStatus="0"（为空时）
- [x] Service `importCmsContract` 自动设置 ownerId=当前用户
- [x] Service `importCmsContract` 自动设置 deptId=当前部门
- [x] 前端三个页面的下载按钮改为下拉菜单
- [x] E2E 测试适配新模板字段
- [x] 新增模板字段验证测试
