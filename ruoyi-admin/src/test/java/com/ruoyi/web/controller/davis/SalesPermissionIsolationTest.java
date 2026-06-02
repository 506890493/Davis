package com.ruoyi.web.controller.davis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 验证销售角色权限隔离 - 专门测试权限功能
 */
@DisplayName("验证修复：销售角色权限隔离")
public class SalesPermissionIsolationTest extends BaseControllerTest {

    @BeforeEach
    void setup() {
        // 确保测试数据干净
        cleanupTestData();
    }

    @Test
    @DisplayName("测试销售数据隔离：销售只能看到自己创建的合同")
    void testSalesDataIsolation() throws Exception {
        // 创建销售合同
        Long salesCustomerId = createCustomerAsSales("销售客户-隔离测试");
        Long salesContractId = createContractAsSales(salesCustomerId, "销售合同-隔离测试", "1", 12000.00);
        
        // 创建经理合同
        Long managerCustomerId = createCustomerAsManager("经理客户-隔离测试");
        Long managerContractId = createContractAsManager(managerCustomerId, "经理合同-隔离测试", "1", 20000.00);
        
        // 验证销售只能看到自己的合同
        String salesView = getResponseJson(asSales(HttpMethod.GET, "/system/contract/list", null));
        System.out.println("Sales view: " + salesView);
        assertTrue(salesView.contains("销售合同-隔离测试"));
        assertFalse(salesView.contains("经理合同-隔离测试"));
        
        // 验证经理可以看到所有合同
        String managerView = getResponseJson(asManager(HttpMethod.GET, "/system/contract/list", null));
        System.out.println("Manager view: " + managerView);
        assertTrue(managerView.contains("销售合同-隔离测试"));
        assertTrue(managerView.contains("经理合同-隔离测试"));
    }

    @Test
    @DisplayName("测试审批保护：销售不能编辑已审批的合同")
    void testApprovalProtection() throws Exception {
        // 创建销售合同
        Long customerId = createCustomerAsSales("销售客户-审批测试");
        Long contractId = createContractAsSales(customerId, "销售合同-审批测试", "1", 12000.00);
        
        // 审批合同
        Map<String, Object> audit = new LinkedHashMap<>();
        audit.put("contractId", contractId);
        audit.put("auditStatus", "1");
        assertSuccess(asManager(HttpMethod.POST, "/system/contract/audit", audit));
        
        // 尝试编辑已审批的合同
        Map<String, Object> update = new LinkedHashMap<>();
        update.put("contractId", contractId);
        update.put("contractName", "销售合同-审批测试-修改");
        update.put("contractType", "1");
        update.put("customerId", customerId);
        update.put("amount", 15000.00);
        update.put("paymentCycle", "1");
        update.put("paymentMethod", "3");
        update.put("ownerId", USER_ID_LISI);
        
        // 验证不能编辑已审批的合同
        String response = getResponseJson(asSales(HttpMethod.PUT, "/system/contract", update));
        System.out.println("Approval protection response: " + response);
        assertTrue(response.contains("已审批通过的合同不能修改"));
    }

    @Test
    @DisplayName("测试引用保护：销售不能删除有关联合同的客户")
    void testReferenceProtection() throws Exception {
        // 创建客户和合同
        Long customerId = createCustomerAsSales("销售客户-引用测试");
        Long contractId = createContractAsSales(customerId, "销售合同-引用测试", "1", 12000.00);
        
        // 尝试删除客户
        String response = getResponseJson(asSales(HttpMethod.DELETE, "/system/customer/" + customerId, null));
        System.out.println("Reference protection response: " + response);
        assertTrue(response.contains("该客户有关联合同，不能删除"));
    }

    @Test
    @DisplayName("测试删除保护：销售不能删除已审批的合同")
    void testDeleteProtection() throws Exception {
        // 创建销售合同
        Long customerId = createCustomerAsSales("销售客户-删除测试");
        Long contractId = createContractAsSales(customerId, "销售合同-删除测试", "1", 12000.00);
        
        // 审批合同
        Map<String, Object> audit = new LinkedHashMap<>();
        audit.put("contractId", contractId);
        audit.put("auditStatus", "1");
        assertSuccess(asManager(HttpMethod.POST, "/system/contract/audit", audit));
        
        // 尝试删除已审批的合同
        String response = getResponseJson(asSales(HttpMethod.DELETE, "/system/contract/" + contractId, null));
        System.out.println("Delete protection response: " + response);
        assertTrue(response.contains("已审批通过的合同不能删除"));
    }

    @Test
    @DisplayName("测试权限检查：销售只能删除自己创建的合同")
    void testDeletePermissionCheck() throws Exception {
        // 创建销售创建的合同
        Long salesCustomerId = createCustomerAsSales("销售客户-权限测试");
        Long salesContractId = createContractAsSales(salesCustomerId, "销售合同-权限测试", "1", 12000.00);
        
        // 创建经理创建的合同
        Long managerCustomerId = createCustomerAsManager("经理客户-权限测试");
        Long managerContractId = createContractAsManager(managerCustomerId, "经理合同-权限测试", "1", 20000.00);
        
        // 测试删除销售自己的合同 - 应该成功
        assertSuccess(asSales(HttpMethod.DELETE, "/system/contract/" + salesContractId, null));
        
        // 测试删除经理的合同 - 应该抛出异常
        String response = getResponseJson(asSales(HttpMethod.DELETE, "/system/contract/" + managerContractId, null));
        System.out.println("Delete permission check response: " + response);
        assertTrue(response.contains("只有合同创建者或管理员才能删除合同"));
    }

    @Test
    @DisplayName("测试create_by字段正确设置")
    void testCreateByField() throws Exception {
        // 创建销售合同
        Long customerId = createCustomerAsSales("销售客户-createBy测试");
        Long contractId = createContractAsSales(customerId, "销售合同-createBy测试", "1", 12000.00);
        
        // 查询合同详情
        String response = getResponseJson(asSales(HttpMethod.GET, "/system/contract/" + contractId, null));
        System.out.println("Contract details response: " + response);
        assertTrue(response.contains("createBy\":\"lisi\""));
    }

    // ========== 辅助方法 ==========

    private void cleanupTestData() {
        // 在实际测试中，可能需要清理测试数据
        // 这里可以添加清理逻辑，确保测试数据隔离
    }

    private Long createCustomerAsSales(String name) throws Exception {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("customerName", name);
        customer.put("customerType", "1");
        customer.put("contactPerson", "销售联系人");
        customer.put("contactPhone", "13900001000");
        customer.put("ownerId", USER_ID_LISI);
        assertSuccess(asSales(HttpMethod.POST, "/system/customer", customer));
        
        // 查询刚创建的客户
        ResultActions listResult = asSales(HttpMethod.GET, "/system/customer/list", null);
        assertListSuccess(listResult);
        Long customerId = getIdFromList(listResult, "customerId");
        assertNotNull(customerId);
        return customerId;
    }

    private Long createCustomerAsManager(String name) throws Exception {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("customerName", name);
        customer.put("customerType", "1");
        customer.put("contactPerson", "经理联系人");
        customer.put("contactPhone", "13900002000");
        customer.put("ownerId", USER_ID_MANAGER);
        assertSuccess(asManager(HttpMethod.POST, "/system/customer", customer));
        
        // 查询刚创建的客户
        ResultActions listResult = asManager(HttpMethod.GET, "/system/customer/list", null);
        assertListSuccess(listResult);
        Long customerId = getIdFromList(listResult, "customerId");
        assertNotNull(customerId);
        return customerId;
    }

    private Long createContractAsSales(Long customerId, String contractName, String contractType, Double amount) throws Exception {
        Map<String, Object> contract = new LinkedHashMap<>();
        contract.put("contractName", contractName);
        contract.put("contractType", contractType);
        contract.put("customerId", customerId);
        contract.put("amount", amount);
        contract.put("paymentCycle", "1");
        contract.put("paymentMethod", "3");
        contract.put("startDate", "2026-01-01");
        contract.put("endDate", "2027-01-01");
        contract.put("ownerId", USER_ID_LISI);
        
        assertSuccess(asSales(HttpMethod.POST, "/system/contract", contract));
        
        // 查询刚创建的合同
        ResultActions listResult = asSales(HttpMethod.GET, "/system/contract/list", null);
        assertListSuccess(listResult);
        Long contractId = getIdFromList(listResult, "contractId");
        assertNotNull(contractId);
        return contractId;
    }

    private Long createContractAsManager(Long customerId, String contractName, String contractType, Double amount) throws Exception {
        Map<String, Object> contract = new LinkedHashMap<>();
        contract.put("contractName", contractName);
        contract.put("contractType", contractType);
        contract.put("customerId", customerId);
        contract.put("amount", amount);
        contract.put("paymentCycle", "1");
        contract.put("paymentMethod", "3");
        contract.put("startDate", "2026-01-01");
        contract.put("endDate", "2027-01-01");
        contract.put("ownerId", USER_ID_MANAGER);
        
        assertSuccess(asManager(HttpMethod.POST, "/system/contract", contract));
        
        // 查询刚创建的合同
        ResultActions listResult = asManager(HttpMethod.GET, "/system/contract/list", null);
        assertListSuccess(listResult);
        Long contractId = getIdFromList(listResult, "contractId");
        assertNotNull(contractId);
        return contractId;
    }
}