package com.ruoyi.web.controller.davis;

import com.ruoyi.common.core.domain.AjaxResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.ByteArrayOutputStream;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 合同批量上传功能集成测试
 *
 * 测试范围：Controller → Service → Mapper 完整流程
 * 使用 MockMvc + H2 内存数据库
 */
@DisplayName("集成测试: 合同批量上传")
public class CmsContractControllerIntegrationTest extends BaseControllerTest {

    @Test
    @DisplayName("上传有效的 Excel 文件，验证导入成功")
    void testImportData_Success_WithValidExcelFile() throws Exception {
        // 构造有效的 Excel 文件
        MockMultipartFile validFile = createValidExcelFile();

        // 以 manager 身份上传
        ResultActions result = mockMvc.perform(
            MockMvcRequestBuilders.multipart("/system/contract/importData")
                .file(validFile)
                .param("updateSupport", "false")
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                })
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(USERNAME_MANAGER).roles("manager"))
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        // 验证返回成功
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.code").value(200))
              .andExpect(jsonPath("$.msg").value("导入成功 2 条"));

        // 验证数据库中已插入记录
        ResultActions listResult = asManager(HttpMethod.GET, "/system/contract/list?contractCode=TEST_IMPORT_001", null);
        String json = getResponseJson(listResult);
        assertThat(json).contains("TEST_IMPORT_001");
        assertThat(json).contains("测试导入合同A");
    }

    @Test
    @DisplayName("上传空文件，验证返回错误")
    void testImportData_Fail_WithEmptyFile() throws Exception {
        // 构造空 Excel 文件（只有表头，无数据行）
        MockMultipartFile emptyFile = createEmptyExcelFile();

        // 以 manager 身份上传
        ResultActions result = mockMvc.perform(
            MockMvcRequestBuilders.multipart("/system/contract/importData")
                .file(emptyFile)
                .param("updateSupport", "false")
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                })
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(USERNAME_MANAGER).roles("manager"))
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        // 验证返回错误（空数据列表会抛出 ServiceException）
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.code").value(500))
              .andExpect(jsonPath("$.msg").value("导入合同数据不能为空！"));
    }

    @Test
    @DisplayName("上传非 Excel 文件，验证返回错误")
    void testImportData_Fail_WithInvalidFileType() throws Exception {
        // 构造非 Excel 文件（.txt）
        MockMultipartFile txtFile = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "这是一个文本文件，不是Excel".getBytes()
        );

        // 以 manager 身份上传
        ResultActions result = mockMvc.perform(
            MockMvcRequestBuilders.multipart("/system/contract/importData")
                .file(txtFile)
                .param("updateSupport", "false")
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                })
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(USERNAME_MANAGER).roles("manager"))
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        // 验证返回错误（ExcelUtil 会抛出异常）
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("上传包含非法字符的 Excel 文件，验证被拦截")
    void testImportData_Fail_WithIllegalCharacters() throws Exception {
        // 构造包含 SQL 注入字符的 Excel 文件
        MockMultipartFile maliciousFile = createMaliciousExcelFile();

        // 以 manager 身份上传
        ResultActions result = mockMvc.perform(
            MockMvcRequestBuilders.multipart("/system/contract/importData")
                .file(maliciousFile)
                .param("updateSupport", "false")
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                })
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(USERNAME_MANAGER).roles("manager"))
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        // 验证返回错误（SecurityValidationUtil 会拦截）
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.code").value(500));

        String responseJson = getResponseJson(result);
        // 验证错误信息包含"非法字符"相关描述
        assertThat(responseJson).containsPattern("非法SQL字符|XSS脚本|非法控制字符");
    }

    @Test
    @DisplayName("测试下载导入模板功能")
    void testImportTemplate_Success() throws Exception {
        // 以 manager 身份下载模板
        ResultActions result = mockMvc.perform(
            MockMvcRequestBuilders.post("/system/contract/importTemplate")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(USERNAME_MANAGER).roles("manager"))
        );

        // 验证返回 Excel 文件
        result.andExpect(status().isOk())
              .andExpect(header().exists("Content-Disposition"));

        // 验证返回的是 Excel 文件（通过 Content-Type）
        String contentType = result.andReturn().getResponse().getContentType();
        assertThat(contentType).contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @Test
    @DisplayName("上传包含重复合同编码的文件，不开启更新，验证失败")
    void testImportData_Fail_WithDuplicateContractCode_WithoutUpdateSupport() throws Exception {
        // 先上传一次
        MockMultipartFile firstFile = createValidExcelFile();
        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/system/contract/importData")
                .file(firstFile)
                .param("updateSupport", "false")
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                })
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(USERNAME_MANAGER).roles("manager"))
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        // 再次上传相同的文件（不开启更新）
        MockMultipartFile duplicateFile = createValidExcelFile();
        ResultActions result = mockMvc.perform(
            MockMvcRequestBuilders.multipart("/system/contract/importData")
                .file(duplicateFile)
                .param("updateSupport", "false")
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                })
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(USERNAME_MANAGER).roles("manager"))
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        // 验证返回错误（合同编码已存在）
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.code").value(500));

        String responseJson = getResponseJson(result);
        assertThat(responseJson).contains("合同编码已存在且未开启更新");
    }

    @Test
    @DisplayName("上传包含重复合同编码的文件，开启更新，验证成功")
    void testImportData_Success_WithDuplicateContractCode_WithUpdateSupport() throws Exception {
        // 先上传一次
        MockMultipartFile firstFile = createValidExcelFile();
        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/system/contract/importData")
                .file(firstFile)
                .param("updateSupport", "false")
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                })
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(USERNAME_MANAGER).roles("manager"))
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        // 再次上传相同的文件（开启更新）
        MockMultipartFile duplicateFile = createValidExcelFile();
        ResultActions result = mockMvc.perform(
            MockMvcRequestBuilders.multipart("/system/contract/importData")
                .file(duplicateFile)
                .param("updateSupport", "true")
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                })
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(USERNAME_MANAGER).roles("manager"))
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        // 验证返回成功
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.code").value(200))
              .andExpect(jsonPath("$.msg").value("导入成功 2 条"));
    }

    // ========== 辅助方法：构造测试文件 ==========

    /**
     * 创建有效的 Excel 文件（包含2条合同数据）
     */
    private MockMultipartFile createValidExcelFile() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("合同数据");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"合同编码", "合同名称", "客户ID", "合同金额", "开始日期", "结束日期", "审批状态"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 创建数据行1
        Row dataRow1 = sheet.createRow(1);
        dataRow1.createCell(0).setCellValue("TEST_IMPORT_001");
        dataRow1.createCell(1).setCellValue("测试导入合同A");
        dataRow1.createCell(2).setCellValue(1);
        dataRow1.createCell(3).setCellValue(10000.0);
        dataRow1.createCell(4).setCellValue("2026-01-01");
        dataRow1.createCell(5).setCellValue("2026-12-31");
        dataRow1.createCell(6).setCellValue("0");

        // 创建数据行2
        Row dataRow2 = sheet.createRow(2);
        dataRow2.createCell(0).setCellValue("TEST_IMPORT_002");
        dataRow2.createCell(1).setCellValue("测试导入合同B");
        dataRow2.createCell(2).setCellValue(1);
        dataRow2.createCell(3).setCellValue(20000.0);
        dataRow2.createCell(4).setCellValue("2026-02-01");
        dataRow2.createCell(5).setCellValue("2027-01-31");
        dataRow2.createCell(6).setCellValue("0");

        // 转换为字节数组
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "contracts_valid.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    /**
     * 创建空 Excel 文件（只有表头，无数据行）
     */
    private MockMultipartFile createEmptyExcelFile() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("合同数据");

        // 只创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"合同编码", "合同名称", "客户ID", "合同金额", "开始日期", "结束日期", "审批状态"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 转换为字节数组
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "contracts_empty.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }

    /**
     * 创建包含恶意字符的 Excel 文件
     */
    private MockMultipartFile createMaliciousExcelFile() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("合同数据");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"合同编码", "合同名称", "客户ID", "合同金额", "开始日期", "结束日期", "审批状态"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 创建包含 SQL 注入的数据行
        Row dataRow1 = sheet.createRow(1);
        dataRow1.createCell(0).setCellValue("TEST_MAL_001");
        dataRow1.createCell(1).setCellValue("恶意合同'; DROP TABLE cms_contract; --");
        dataRow1.createCell(2).setCellValue(1);
        dataRow1.createCell(3).setCellValue(10000.0);
        dataRow1.createCell(4).setCellValue("2026-01-01");
        dataRow1.createCell(5).setCellValue("2026-12-31");
        dataRow1.createCell(6).setCellValue("0");

        // 创建包含 XSS 的数据行
        Row dataRow2 = sheet.createRow(2);
        dataRow2.createCell(0).setCellValue("TEST_MAL_002");
        dataRow2.createCell(1).setCellValue("<script>alert('xss')</script>合同");
        dataRow2.createCell(2).setCellValue(1);
        dataRow2.createCell(3).setCellValue(20000.0);
        dataRow2.createCell(4).setCellValue("2026-02-01");
        dataRow2.createCell(5).setCellValue("2027-01-31");
        dataRow2.createCell(6).setCellValue("0");

        // 转换为字节数组
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
            "file",
            "contracts_malicious.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            outputStream.toByteArray()
        );
    }
}
