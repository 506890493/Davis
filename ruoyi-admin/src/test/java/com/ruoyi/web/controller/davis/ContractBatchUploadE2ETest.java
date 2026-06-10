package com.ruoyi.web.controller.davis;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * E2E 测试：批量上传合同功能
 *
 * 测试场景：
 * 1. 上传有效的 Excel 文件，验证成功导入
 * 2. 上传包含非法字符的文件，验证错误提示显示具体行号
 * 3. 下载导入模板，验证文件下载成功
 * 4. 上传重复合同编码（未开启更新支持），验证错误提示
 * 5. 上传重复合同编码（开启更新支持），验证成功更新
 */
@DisplayName("E2E: 批量上传合同功能")
public class ContractBatchUploadE2ETest extends BaseControllerTest {

    @Test
    @DisplayName("1. 上传有效合同文件 - 成功导入")
    void testUploadValidContractFile_Success() throws Exception {
        // 准备有效的 Excel 文件
        byte[] excelData = createValidContractExcel();
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "contracts_valid.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            excelData
        );

        // 执行上传
        ResultActions result = mockMvc.perform(
            MockMvcRequestBuilders.multipart("/system/contract/importData")
                .file(file)
                .param("updateSupport", "false")
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                })
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                    .authentication(createManagerAuth()))
        );

        // 验证响应
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.code").value(200))
              .andExpect(jsonPath("$.msg").value("操作成功"));

        String responseJson = getResponseJson(result);
        assertThat(responseJson).contains("导入成功 2 条");

        // 验证数据库中已插入合同
        ResultActions listResult = asManager(HttpMethod.GET, "/system/contract/list", null);
        String listJson = getResponseJson(listResult);
        assertThat(listJson)
            .contains("BATCH001")
            .contains("批量测试合同A")
            .contains("BATCH002")
            .contains("批量测试合同B");
    }

    @Test
    @DisplayName("2. 上传包含非法字符的文件 - 显示具体错误行号")
    void testUploadInvalidCharacterFile_ShowsLineNumber() throws Exception {
        // 准备包含非法字符的 Excel 文件
        byte[] excelData = createInvalidContractExcel();
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "contracts_invalid.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            excelData
        );

        // 执行上传
        ResultActions result = mockMvc.perform(
            MockMvcRequestBuilders.multipart("/system/contract/importData")
                .file(file)
                .param("updateSupport", "false")
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                })
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                    .authentication(createManagerAuth()))
        );

        // 验证响应 - 应该返回失败，并显示具体行号
        String responseJson = getResponseJson(result);
        Map<String, Object> response = objectMapper.readValue(responseJson, Map.class);

        // 错误响应的 code 不是 200
        assertThat(response.get("code")).isNotEqualTo(200);

        // 错误消息应包含行号和具体错误
        String errorMsg = response.get("msg").toString();
        assertThat(errorMsg).contains("第1行");
        // 错误消息应包含 SQL 或 XSS 相关的提示
        assertThat(errorMsg.contains("SQL") || errorMsg.contains("XSS")).isTrue();
    }

    @Test
    @DisplayName("3a. 下载导入模板（默认代账合同）— 向后兼容")
    void testDownloadTemplate_Success() throws Exception {
        // 不传 contractType 参数，默认下载代账合同模板
        ResultActions result = mockMvc.perform(
            MockMvcRequestBuilders.post("/system/contract/importTemplate")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                    .authentication(createManagerAuth()))
        );

        // 验证响应
        result.andExpect(status().isOk())
              .andExpect(header().exists("Content-Disposition"));

        // 验证内容类型
        String contentType = result.andReturn().getResponse().getContentType();
        assertThat(contentType).contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

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

        assertThat(headerSet).contains("税务类型");
        assertThat(headerSet).contains("成立日期");
        assertThat(headerSet).doesNotContain("租赁地址");
        assertThat(headerSet).doesNotContain("是否已出租");
        assertThat(headerSet).doesNotContain("父合同ID");
        assertThat(headerSet).doesNotContain("客户ID");
        assertThat(headerSet).doesNotContain("审核状态");
        assertThat(headerSet).doesNotContain("催交状态");
        assertThat(headerSet).doesNotContain("附件列表");
        assertThat(headerSet).doesNotContain("归属人ID (关联sys_user)");

        workbook.close();
    }

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

        assertThat(headerSet).contains("租赁地址");
        assertThat(headerSet).contains("是否已出租");
        assertThat(headerSet).doesNotContain("税务类型");
        assertThat(headerSet).doesNotContain("成立日期");
        assertThat(headerSet).doesNotContain("父合同ID");
        assertThat(headerSet).doesNotContain("客户ID");
        assertThat(headerSet).doesNotContain("审核状态");
        assertThat(headerSet).doesNotContain("催交状态");
        assertThat(headerSet).doesNotContain("附件列表");
        assertThat(headerSet).doesNotContain("归属人ID (关联sys_user)");

        workbook.close();
    }

    @Test
    @DisplayName("4. 上传重复合同编码（未开启更新）- 失败")
    void testUploadDuplicateContractCode_WithoutUpdate_Fails() throws Exception {
        // 先上传一次有效文件
        byte[] excelData = createValidContractExcel();
        MockMultipartFile file1 = new MockMultipartFile(
            "file",
            "contracts_valid.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            excelData
        );

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/system/contract/importData")
                .file(file1)
                .param("updateSupport", "false")
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                })
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                    .authentication(createManagerAuth()))
        );

        // 再次上传相同文件（未开启更新）
        MockMultipartFile file2 = new MockMultipartFile(
            "file",
            "contracts_valid.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            excelData
        );

        ResultActions result = mockMvc.perform(
            MockMvcRequestBuilders.multipart("/system/contract/importData")
                .file(file2)
                .param("updateSupport", "false")
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                })
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                    .authentication(createManagerAuth()))
        );

        // 验证失败响应
        String responseJson = getResponseJson(result);
        assertThat(responseJson).contains("已存在");
        // 应该显示具体的行号（第1行或第2行）
        assertThat(responseJson.contains("第1行") || responseJson.contains("第2行")).isTrue();
    }

    @Test
    @DisplayName("5. 上传重复合同编码（开启更新）- 成功更新")
    void testUploadDuplicateContractCode_WithUpdate_Success() throws Exception {
        // 先上传一次
        byte[] excelData1 = createValidContractExcel();
        MockMultipartFile file1 = new MockMultipartFile(
            "file",
            "contracts_valid.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            excelData1
        );

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/system/contract/importData")
                .file(file1)
                .param("updateSupport", "false")
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                })
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                    .authentication(createManagerAuth()))
        );

        // 准备更新的数据（金额修改）
        byte[] excelData2 = createUpdatedContractExcel();
        MockMultipartFile file2 = new MockMultipartFile(
            "file",
            "contracts_updated.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            excelData2
        );

        // 上传更新（开启更新支持）
        ResultActions result = mockMvc.perform(
            MockMvcRequestBuilders.multipart("/system/contract/importData")
                .file(file2)
                .param("updateSupport", "true")
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                })
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                    .authentication(createManagerAuth()))
        );

        // 验证成功
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.code").value(200));

        String responseJson = getResponseJson(result);
        assertThat(responseJson).contains("导入成功 2 条");

        // 验证数据已更新（金额变化）
        ResultActions listResult = asManager(HttpMethod.GET, "/system/contract/list", null);
        String listJson = getResponseJson(listResult);
        assertThat(listJson).contains("25000"); // 更新后的金额
    }

    @Test
    @DisplayName("6. 上传空文件 - 失败")
    void testUploadEmptyFile_Fails() throws Exception {
        byte[] emptyExcel = createEmptyExcel();
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            emptyExcel
        );

        ResultActions result = mockMvc.perform(
            MockMvcRequestBuilders.multipart("/system/contract/importData")
                .file(file)
                .param("updateSupport", "false")
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                })
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                    .authentication(createManagerAuth()))
        );

        // 验证失败响应
        String responseJson = getResponseJson(result);
        Map<String, Object> response = objectMapper.readValue(responseJson, Map.class);
        assertThat(response.get("code")).isNotEqualTo(200);
        assertThat(response.get("msg").toString()).contains("为空");
    }

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

        ResultActions importResult = mockMvc.perform(
            MockMvcRequestBuilders.multipart("/system/contract/importData")
                .file(file)
                .param("updateSupport", "false")
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                })
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                    .authentication(createManagerAuth()))
        );

        // 验证导入成功
        importResult.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

        String importJson = getResponseJson(importResult);
        assertThat(importJson).contains("导入成功");

        ResultActions listResult = asManager(HttpMethod.GET, "/system/contract/list", null);
        String listJson = getResponseJson(listResult);
        assertThat(listJson).contains("\"auditStatus\":\"0\"");
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建有效的合同 Excel 文件
     */
    private byte[] createValidContractExcel() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("代账合同数据");

        // 创建表头 — 代账合同模板（18列）
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

        // 创建数据行1 — 代账报税
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

        // 创建数据行2 — 地址出售
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

    /**
     * 创建包含非法字符的合同 Excel 文件
     */
    private byte[] createInvalidContractExcel() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("代账合同数据");

        // 创建表头 — 代账合同模板（18列）
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

        // 创建包含 SQL 注入字符的数据行
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

    /**
     * 创建更新的合同 Excel 文件（金额修改）
     */
    private byte[] createUpdatedContractExcel() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("代账合同数据");

        // 创建表头 — 代账合同模板（18列）
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

        // 创建数据行 - 相同编号但金额不同
        Row dataRow1 = sheet.createRow(1);
        dataRow1.createCell(0).setCellValue("BATCH001");
        dataRow1.createCell(1).setCellValue("批量测试合同A（已更新）");
        dataRow1.createCell(2).setCellValue("代账报税");
        dataRow1.createCell(7).setCellValue(25000.00); // 更新金额
        dataRow1.createCell(8).setCellValue(25000.00);

        Row dataRow2 = sheet.createRow(2);
        dataRow2.createCell(0).setCellValue("BATCH002");
        dataRow2.createCell(1).setCellValue("批量测试合同B（已更新）");
        dataRow2.createCell(2).setCellValue("地址出售");
        dataRow2.createCell(7).setCellValue(30000.00); // 更新金额
        dataRow2.createCell(8).setCellValue(30000.00);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    /**
     * 创建空 Excel 文件
     */
    private byte[] createEmptyExcel() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("代账合同数据");

        // 只创建表头，没有数据行
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "合同编号", "合同/公司名称", "合同类型"
        };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    /**
     * 格式化日期为字符串
     */
    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    /**
     * 创建 manager 角色的认证对象
     */
    private org.springframework.security.core.Authentication createManagerAuth() {
        com.ruoyi.common.core.domain.entity.SysUser sysUser = new com.ruoyi.common.core.domain.entity.SysUser();
        sysUser.setUserId(USER_ID_MANAGER);
        sysUser.setUserName(USERNAME_MANAGER);
        sysUser.setDeptId(100L);

        com.ruoyi.common.core.domain.model.LoginUser loginUser =
            new com.ruoyi.common.core.domain.model.LoginUser(sysUser, getPermissionsForRole("manager"));
        loginUser.setUserId(USER_ID_MANAGER);
        loginUser.setDeptId(100L);

        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            loginUser, null, loginUser.getAuthorities()
        );
    }

    /**
     * 获取角色权限集合
     */
    private java.util.Set<String> getPermissionsForRole(String roleKey) {
        java.util.Set<String> perms = new java.util.HashSet<>();
        perms.add("system:contract:list");
        perms.add("system:contract:query");
        perms.add("system:contract:add");
        perms.add("system:contract:edit");
        perms.add("system:contract:remove");
        perms.add("system:contract:import");
        perms.add("system:contract:export");
        return perms;
    }
}
