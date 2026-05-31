package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.CmsContract;
import com.ruoyi.system.mapper.CmsContractMapper;
import com.ruoyi.system.service.ICmsContractService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CmsContractServiceTest {

    @Mock
    private CmsContractMapper cmsContractMapper;

    @InjectMocks
    private CmsContractServiceImpl cmsContractService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testImportContract_Success_WithValidData() {
        CmsContract contract = new CmsContract();
        contract.setContractCode("TEST001");
        contract.setContractName("测试合同");
        contract.setCustomerId(1L);
        contract.setAmount(new BigDecimal("10000.00"));
        contract.setStartDate(new Date());
        contract.setEndDate(new Date());
        contract.setStatus("0");

        when(cmsContractMapper.selectCmsContractByContractCode(anyString())).thenReturn(null);
        when(cmsContractMapper.insertCmsContract(any())).thenReturn(1);

        String result = cmsContractService.importCmsContract(
            Collections.singletonList(contract),
            false,
            "test_operator"
        );

        assertTrue(result.contains("导入成功 1 条"));
        verify(cmsContractMapper, times(1)).insertCmsContract(any());
    }

    @Test
    void testImportContract_Fail_WhenContractCodeIsNull() {
        CmsContract contract = new CmsContract();
        contract.setContractCode(null);
        contract.setContractName("测试合同");

        assertThrows(com.ruoyi.common.exception.ServiceException.class, () -> {
            cmsContractService.importCmsContract(
                Collections.singletonList(contract),
                false,
                "test_operator"
            );
        });

        verify(cmsContractMapper, never()).insertCmsContract(any());
    }

    @Test
    void testImportContract_Fail_WhenContractNameIsNull() {
        CmsContract contract = new CmsContract();
        contract.setContractCode("TEST001");
        contract.setContractName(null);

        assertThrows(com.ruoyi.common.exception.ServiceException.class, () -> {
            cmsContractService.importCmsContract(
                Collections.singletonList(contract),
                false,
                "test_operator"
            );
        });

        verify(cmsContractMapper, never()).insertCmsContract(any());
    }

    @Test
    void testImportContract_Fail_WhenContractCodeExists_WithoutUpdateSupport() {
        CmsContract contract = new CmsContract();
        contract.setContractCode("TEST001");
        contract.setContractName("测试合同");

        CmsContract existingContract = new CmsContract();
        existingContract.setContractId(1L);

        when(cmsContractMapper.selectCmsContractByContractCode("TEST001")).thenReturn(existingContract);

        assertThrows(com.ruoyi.common.exception.ServiceException.class, () -> {
            cmsContractService.importCmsContract(
                Collections.singletonList(contract),
                false,
                "test_operator"
            );
        });

        verify(cmsContractMapper, never()).insertCmsContract(any());
        verify(cmsContractMapper, never()).updateCmsContract(any());
    }

    @Test
    void testImportContract_Success_WhenContractCodeExists_WithUpdateSupport() {
        CmsContract contract = new CmsContract();
        contract.setContractCode("TEST001");
        contract.setContractName("更新测试合同");
        contract.setAmount(new BigDecimal("15000.00"));

        CmsContract existingContract = new CmsContract();
        existingContract.setContractId(1L);
        existingContract.setContractCode("TEST001");

        when(cmsContractMapper.selectCmsContractByContractCode("TEST001")).thenReturn(existingContract);
        when(cmsContractMapper.updateCmsContract(any())).thenReturn(1);

        String result = cmsContractService.importCmsContract(
            Collections.singletonList(contract),
            true,
            "test_operator"
        );

        assertTrue(result.contains("导入成功 1 条"));
        verify(cmsContractMapper, times(1)).updateCmsContract(any());
        verify(cmsContractMapper, never()).insertCmsContract(any());
    }

    @Test
    void testImportContract_Fail_WithEmptyList() {
        assertThrows(com.ruoyi.common.exception.ServiceException.class, () -> {
            cmsContractService.importCmsContract(
                Collections.emptyList(),
                false,
                "test_operator"
            );
        });
    }

    @Test
    void testImportContract_Fail_WithSqlInjection() {
        CmsContract contract = new CmsContract();
        contract.setContractCode("TEST003");
        contract.setContractName("合同'; DROP TABLE cms_contract;");

        when(cmsContractMapper.selectCmsContractByContractCode(anyString())).thenReturn(null);
        when(cmsContractMapper.insertCmsContract(any())).thenThrow(new RuntimeException("SQL Error"));

        assertThrows(com.ruoyi.common.exception.ServiceException.class, () -> {
            cmsContractService.importCmsContract(
                Collections.singletonList(contract),
                false,
                "test_operator"
            );
        });

        verify(cmsContractMapper, times(1)).insertCmsContract(any());
    }

    @Test
    void testImportContract_Fail_WithXssInjection() {
        CmsContract contract = new CmsContract();
        contract.setContractCode("TEST004");
        contract.setContractName("<script>alert('xss')</script>合同");

        when(cmsContractMapper.selectCmsContractByContractCode(anyString())).thenReturn(null);

        assertThrows(com.ruoyi.common.exception.ServiceException.class, () -> {
            cmsContractService.importCmsContract(
                Collections.singletonList(contract),
                false,
                "test_operator"
            );
        });

        verify(cmsContractMapper, never()).insertCmsContract(any());
    }

    @Test
    void testImportContract_Fail_WithControlCharacters() {
        CmsContract contract = new CmsContract();
        contract.setContractCode("TEST005");
        contract.setContractName("合同\n\r\t名称");

        when(cmsContractMapper.selectCmsContractByContractCode(anyString())).thenReturn(null);
        when(cmsContractMapper.insertCmsContract(any())).thenThrow(new RuntimeException("Data Error"));

        assertThrows(com.ruoyi.common.exception.ServiceException.class, () -> {
            cmsContractService.importCmsContract(
                Collections.singletonList(contract),
                false,
                "test_operator"
            );
        });

        verify(cmsContractMapper, times(1)).insertCmsContract(any());
    }

    @Test
    void testImportContract_Fail_WithInvalidAmountFormat() {
        CmsContract contract = new CmsContract();
        contract.setContractCode("TEST006");
        contract.setContractName("测试合同");
        contract.setAmount(new BigDecimal("-1000.00"));

        when(cmsContractMapper.selectCmsContractByContractCode(anyString())).thenReturn(null);
        when(cmsContractMapper.insertCmsContract(any())).thenThrow(new RuntimeException("Invalid Amount"));

        assertThrows(com.ruoyi.common.exception.ServiceException.class, () -> {
            cmsContractService.importCmsContract(
                Collections.singletonList(contract),
                false,
                "test_operator"
            );
        });

        verify(cmsContractMapper, times(1)).insertCmsContract(any());
    }

    @Test
    void testImportContract_Fail_WithInvalidDateFormat() {
        CmsContract contract = new CmsContract();
        contract.setContractCode("TEST007");
        contract.setContractName("测试合同");
        contract.setStartDate(null);
        contract.setEndDate(null);

        when(cmsContractMapper.selectCmsContractByContractCode(anyString())).thenReturn(null);
        when(cmsContractMapper.insertCmsContract(any())).thenThrow(new RuntimeException("Invalid Date"));

        assertThrows(com.ruoyi.common.exception.ServiceException.class, () -> {
            cmsContractService.importCmsContract(
                Collections.singletonList(contract),
                false,
                "test_operator"
            );
        });

        verify(cmsContractMapper, times(1)).insertCmsContract(any());
    }
}