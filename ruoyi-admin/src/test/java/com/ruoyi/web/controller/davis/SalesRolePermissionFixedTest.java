package com.ruoyi.web.controller.davis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证修复后的销售角色权限
 */
@DisplayName("验证修复：销售角色权限")
public class SalesRolePermissionFixedTest extends BaseControllerTest {

    // 禁用事务回滚，确保测试数据能够持久化

    @Test
    @Order(1)
    @DisplayName("测试销售数据隔离：销售只能看到自己创建的合同")
    @Rollback(false)
    void testSalesDataIsolation() throws Exception {
        // 创建销售合同
        Long salesCustomerId = createCustomerAsSales("销售客户-隔离测试");
        Long salesContractId = createContractAsSales(salesCustomerId, "销售合同-隔离测试", "1", 12000.00);
        
        // 创建经理合同
        Long managerCustomerId = createCustomerAsManager("经理客户-隔离测试");
        Long managerContractId = createContractAsManager(managerCustomerId, "经理合同-隔离测试", "1", 20000.00);
        
        // 验证销售只能看到自己的合同
        String salesView = getResponseJson(asSales(HttpMethod.GET, "/system/contract/list", null));
        assertThat(salesView).contains("销售合同-隔离测试");
        assertThat(salesView).doesNotContain("经理合同-隔离测试");
        
        // 验证经理可以看到所有合同
        String managerView = getResponseJson(asManager(HttpMethod.GET, "/system/contract/list", null));
        assertThat(managerView).contains("销售合同-隔离测试");
        assertThat(managerView).contains("经理合同-隔离测试");
    }

    @Test
    @Order(2)
    @DisplayName("测试审批保护：销售不能编辑已审批的合同")
    @Rollback(false)
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
        assertThat(response).contains("已审批通过的合同不能修改");
    }

    @Test
    @Order(3)
    @DisplayName("测试引用保护：销售不能删除有关联合同的客户")
    @Rollback(false)
    void testReferenceProtection() throws Exception {
        // 创建客户和合同
        Long customerId = createCustomerAsSales("销售客户-引用测试");
        Long contractId = createContractAsSales(customerId, "销售合同-引用测试", "1", 12000.00);
        
        // 尝试删除客户
        String response = getResponseJson(asSales(HttpMethod.DELETE, "/system/customer/" + customerId, null));
        assertThat(response).contains("该客户有关联合同，不能删除");
    }

    @Test
    @Order(4)
    @DisplayName("测试删除保护：销售不能删除已审批的合同")
    @Rollback(false)
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
        assertThat(response).contains("已审批通过的合同不能删除");
    }

    private Long createCustomerAsSales(String name) throws Exception {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("customerName", name);
        customer.put("customerType", "1");
        customer.put("contactPerson", "销售联系人");
        customer.put("contactPhone", "13900001000");
        customer.put("ownerId", USER_ID_LISI);
        assertSuccess(asSales(HttpMethod.POST, "/system/customer", customer));
        ResultActions listResult = asSales(HttpMethod.GET, "/system/customer/list", null);
        assertListSuccess(listResult);
        Long customerId = getIdFromList(listResult, "customerId");
        assertThat(customerId).isNotNull();
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
        ResultActions listResult = asManager(HttpMethod.GET, "/system/customer/list", null);
        assertListSuccess(listResult);
        Long customerId = getIdFromList(listResult, "customerId");
        assertThat(customerId).isNotNull();
        return customerId;
    }

    private Long createContractAsSales(Long customerId, String name, String type, Double amount) throws Exception {
        Map<String, Object> contract = new LinkedHashMap<>();
        contract.put("contractName", name);
        contract.put("contractType", type);
        contract.put("customerId", customerId);
        contract.put("amount", amount);
        contract.put("paymentCycle", "1");
        contract.put("paymentMethod", "3");
        contract.put("startDate", "2026-06-01");
        contract.put("endDate", "2027-05-31");
        contract.put("ownerId", USER_ID_LISI);
        assertSuccess(asSales(HttpMethod.POST, "/system/contract", contract));
        ResultActions listResult = asSales(HttpMethod.GET, "/system/contract/list", null);
        assertListSuccess(listResult);
        Long contractId = getIdFromList(listResult, "contractId");
        assertThat(contractId).isNotNull();
        return contractId;
    }

    private Long createContractAsManager(Long customerId, String name, String type, Double amount) throws Exception {
        Map<String, Object> contract = new LinkedHashMap<>();
        contract.put("contractName", name);
        contract.put("contractType", type);
        contract.put("customerId", customerId);
        contract.put("amount", amount);
        contract.put("paymentCycle", "1");
        contract.put("paymentMethod", "3");
        contract.put("startDate", "2026-06-01");
        contract.put("endDate", "2027-05-31");
        contract.put("ownerId", USER_ID_MANAGER);
        assertSuccess(asManager(HttpMethod.POST, "/system/contract", contract));
        ResultActions listResult = asManager(HttpMethod.GET, "/system/contract/list", null);
        assertListSuccess(listResult);
        Long contractId = getIdFromList(listResult, "contractId");
        assertThat(contractId).isNotNull();
        return contractId;
    }
}