package com.ruoyi.web.controller.davis;

import com.ruoyi.system.domain.CmsContract;
import com.ruoyi.system.service.impl.CmsContractServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 验证修复后的销售角色权限 - 使用Mock测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("验证修复：销售角色权限 - Mock测试")
public class SalesRolePermissionMockTest {

    @Mock
    private com.ruoyi.system.mapper.CmsContractMapper cmsContractMapper;

    @InjectMocks
    private CmsContractServiceImpl cmsContractService;

    @BeforeEach
    void setUp() {
        // 模拟销售用户身份
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("lisi");
        
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("测试销售数据隔离：销售只能看到自己创建的合同")
    void testSalesDataIsolation() {
        // 模拟数据库中有两个合同：一个由销售创建，一个由经理创建
        CmsContract salesContract = new CmsContract();
        salesContract.setContractId(1L);
        salesContract.setContractName("销售合同");
        salesContract.setCreateBy("lisi");
        
        CmsContract managerContract = new CmsContract();
        managerContract.setContractId(2L);
        managerContract.setContractName("经理合同");
        managerContract.setCreateBy("manager");
        
        List<CmsContract> allContracts = Arrays.asList(salesContract, managerContract);
        
        // 模拟Mapper返回所有合同
        when(cmsContractMapper.selectCmsContractList(any())).thenReturn(allContracts);
        
        // 执行查询
        CmsContract queryCondition = new CmsContract();
        List<CmsContract> result = cmsContractService.selectCmsContractList(queryCondition);
        
        // 验证结果
        assertEquals(1, result.size());
        assertEquals("销售合同", result.get(0).getContractName());
        assertEquals("lisi", result.get(0).getCreateBy());
        
        // 验证只查询销售自己的合同
        verify(cmsContractMapper).selectCmsContractList(argThat(contract -> 
            "lisi".equals(contract.getCreateBy())
        ));
    }

    @Test
    @DisplayName("测试合同创建：create_by字段正确设置")
    void testContractCreationCreateBy() {
        // 创建合同对象
        CmsContract contract = new CmsContract();
        contract.setContractName("测试合同");
        contract.setContractCode("TEST001");
        contract.setCustomerId(1L);
        contract.setAmount(new BigDecimal("10000.0"));
        contract.setPaymentCycle("1");
        contract.setPaymentMethod("3");
        contract.setStartDate(new Date());
        contract.setEndDate(new Date());
        contract.setOwnerId(4L);
        
        // 模拟Mapper插入成功
        when(cmsContractMapper.insertCmsContract(any())).thenReturn(1);
        
        // 执行插入
        int result = cmsContractService.insertCmsContract(contract);
        
        // 验证结果
        assertEquals(1, result);
        
        // 验证create_by字段被正确设置
        assertEquals("lisi", contract.getCreateBy());
        
        // 验证Mapper被调用时create_by字段已设置
        verify(cmsContractMapper).insertCmsContract(argThat(c -> 
            "lisi".equals(c.getCreateBy())
        ));
    }

    @Test
    @DisplayName("测试审批保护：销售不能编辑已审批的合同")
    void testApprovalProtection() {
        // 创建已审批的合同
        CmsContract approvedContract = new CmsContract();
        approvedContract.setContractId(1L);
        approvedContract.setContractName("已审批合同");
        approvedContract.setCreateBy("lisi"); // 销售创建的合同
        approvedContract.setAuditStatus("1"); // 已审批
        
        // 模拟Mapper查询到合同
        when(cmsContractMapper.selectCmsContractByContractId(1L)).thenReturn(approvedContract);
        
        // 尝试修改已审批的合同
        CmsContract updateContract = new CmsContract();
        updateContract.setContractId(1L);
        updateContract.setContractName("修改后的合同");
        updateContract.setCustomerId(1L);
        updateContract.setAmount(new BigDecimal("15000.0"));
        updateContract.setPaymentCycle("1");
        updateContract.setPaymentMethod("3");
        updateContract.setStartDate(new Date());
        updateContract.setEndDate(new Date());
        updateContract.setOwnerId(4L);
        
        // 验证抛出异常
        Exception exception = assertThrows(RuntimeException.class, () -> {
            cmsContractService.updateCmsContract(updateContract);
        });
        
        assertEquals("已审批通过的合同不能修改", exception.getMessage());
    }

    @Test
    @DisplayName("测试删除保护：销售不能删除已审批的合同")
    void testDeleteProtection() {
        // 创建已审批的合同
        CmsContract approvedContract = new CmsContract();
        approvedContract.setContractId(1L);
        approvedContract.setContractName("已审批合同");
        approvedContract.setCreateBy("manager"); // 经理创建的合同
        approvedContract.setAuditStatus("1"); // 已审批
        
        // 模拟Mapper查询到合同
        when(cmsContractMapper.selectCmsContractByContractId(1L)).thenReturn(approvedContract);
        
        // 验证抛出异常
        Exception exception = assertThrows(RuntimeException.class, () -> {
            cmsContractService.deleteCmsContractByContractIds(new Long[]{1L});
        });
        
        assertEquals("已审批通过的合同不能删除", exception.getMessage());
    }

    @Test
    @DisplayName("测试权限检查：销售只能删除自己创建的合同")
    void testDeletePermissionCheck() {
        // 创建销售创建的合同
        CmsContract salesContract = new CmsContract();
        salesContract.setContractId(1L);
        salesContract.setContractName("销售合同");
        salesContract.setCreateBy("lisi"); // 销售创建
        salesContract.setAuditStatus("0"); // 未审批
        
        // 创建经理创建的合同
        CmsContract managerContract = new CmsContract();
        managerContract.setContractId(2L);
        managerContract.setContractName("经理合同");
        managerContract.setCreateBy("manager"); // 经理创建
        managerContract.setAuditStatus("0"); // 未审批
        
        // 模拟Mapper查询到合同
        when(cmsContractMapper.selectCmsContractByContractId(1L)).thenReturn(salesContract);
        when(cmsContractMapper.selectCmsContractByContractId(2L)).thenReturn(managerContract);
        
        // 测试删除销售自己的合同 - 应该成功
        assertDoesNotThrow(() -> {
            cmsContractService.deleteCmsContractByContractIds(new Long[]{1L});
        });
        
        // 测试删除经理的合同 - 应该抛出异常
        Exception exception = assertThrows(RuntimeException.class, () -> {
            cmsContractService.deleteCmsContractByContractIds(new Long[]{2L});
        });
        
        assertEquals("只有合同创建者或管理员才能删除合同", exception.getMessage());
    }
}