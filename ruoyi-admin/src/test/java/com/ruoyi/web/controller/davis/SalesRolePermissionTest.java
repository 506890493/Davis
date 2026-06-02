package com.ruoyi.web.controller.davis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.ResultActions;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E: 销售角色权限测试。
 *
 * 验证销售角色的数据隔离和权限：
 * - 只能看到自己创建的合同（create_by = 自己）
 * - 可新增客户和合同
 * - 审批通过前可编辑/删除自己的合同
 * - 不能看到别人创建的合同
 * - 不能审批合同
 */
@DisplayName("E2E: 销售角色权限测试")
public class SalesRolePermissionTest extends BaseControllerTest {

    @Test
    @Order(1)
    @DisplayName("销售可以创建客户和合同")
    void testSalesCanCreateCustomerAndContract() throws Exception {
        Long customerId = createCustomerAsSales("销售客户-A");
        Long contractId = createContractAsSales(customerId, "销售合同-A", "1", 12000.00);

        assertThat(customerId).isNotNull();
        assertThat(contractId).isNotNull();

        String contractList = getResponseJson(asSales(HttpMethod.GET, "/system/contract/list", null));
        assertThat(contractList).contains("销售合同-A");
    }

    @Test
    @Order(2)
    @DisplayName("销售只能看到自己创建的合同")
    void testSalesOnlySeeOwnContracts() throws Exception {
        Long salesCustomerId = createCustomerAsSales("销售客户-B");
        Long salesContractId = createContractAsSales(salesCustomerId, "销售合同-B", "1", 15000.00);

        Long managerCustomerId = createCustomerAsManager("经理客户-C");
        Long managerContractId = createContractAsManager(managerCustomerId, "经理合同-C", "1", 20000.00);

        String salesView = getResponseJson(asSales(HttpMethod.GET, "/system/contract/list", null));
        assertThat(salesView).contains("销售合同-B");
        assertThat(salesView).doesNotContain("经理合同-C");

        String managerView = getResponseJson(asManager(HttpMethod.GET, "/system/contract/list", null));
        assertThat(managerView).contains("销售合同-B");
        assertThat(managerView).contains("经理合同-C");
    }

    @Test
    @Order(3)
    @DisplayName("销售可以编辑未审批的合同")
    void testSalesCanEditPendingContract() throws Exception {
        Long customerId = createCustomerAsSales("销售客户-D");
        Long contractId = createContractAsSales(customerId, "销售合同-D", "1", 12000.00);

        Map<String, Object> update = new LinkedHashMap<>();
        update.put("contractId", contractId);
        update.put("contractName", "销售合同-D-修改");
        update.put("contractType", "1");
        update.put("customerId", customerId);
        update.put("amount", 14000.00);
        update.put("paymentCycle", "1");
        update.put("paymentMethod", "3");
        update.put("ownerId", USER_ID_LISI);

        assertSuccess(asSales(HttpMethod.PUT, "/system/contract", update));

        String updated = getResponseJson(asSales(HttpMethod.GET, "/system/contract/" + contractId, null));
        assertThat(updated).contains("销售合同-D-修改", "\"amount\":14000.0");
    }

    @Test
    @Order(4)
    @DisplayName("销售不能编辑已审批通过的合同")
    void testSalesCannotEditApprovedContract() throws Exception {
        Long customerId = createCustomerAsSales("销售客户-E");
        Long contractId = createContractAsSales(customerId, "销售合同-E", "1", 12000.00);

        Map<String, Object> audit = new LinkedHashMap<>();
        audit.put("contractId", contractId);
        audit.put("auditStatus", "1");
        assertSuccess(asManager(HttpMethod.POST, "/system/contract/audit", audit));

        Map<String, Object> update = new LinkedHashMap<>();
        update.put("contractId", contractId);
        update.put("contractName", "销售合同-E-修改");
        update.put("contractType", "1");
        update.put("customerId", customerId);
        update.put("amount", 15000.00);
        update.put("paymentCycle", "1");
        update.put("paymentMethod", "3");
        update.put("ownerId", USER_ID_LISI);

        asSales(HttpMethod.PUT, "/system/contract", update);

        String response = getResponseJson(asSales(HttpMethod.GET, "/system/contract/" + contractId, null));
        assertThat(response).contains("销售合同-E");
        assertThat(response).doesNotContain("销售合同-E-修改");
    }

    @Test
    @Order(5)
    @DisplayName("销售可以删除未审批的合同")
    void testSalesCanDeletePendingContract() throws Exception {
        Long customerId = createCustomerAsSales("销售客户-F");
        Long contractId = createContractAsSales(customerId, "销售合同-F", "1", 12000.00);

        assertSuccess(asSales(HttpMethod.DELETE, "/system/contract/" + contractId, null));

        String listAfter = getResponseJson(asSales(HttpMethod.GET, "/system/contract/list", null));
        assertThat(listAfter).doesNotContain("销售合同-F");
    }

    @Test
    @Order(6)
    @DisplayName("销售不能删除已审批通过的合同")
    void testSalesCannotDeleteApprovedContract() throws Exception {
        Long customerId = createCustomerAsSales("销售客户-G");
        Long contractId = createContractAsSales(customerId, "销售合同-G", "1", 12000.00);

        Map<String, Object> audit = new LinkedHashMap<>();
        audit.put("contractId", contractId);
        audit.put("auditStatus", "1");
        assertSuccess(asManager(HttpMethod.POST, "/system/contract/audit", audit));

        asSales(HttpMethod.DELETE, "/system/contract/" + contractId, null);

        String listAfter = getResponseJson(asSales(HttpMethod.GET, "/system/contract/list", null));
        assertThat(listAfter).contains("销售合同-G");
    }

    @Test
    @Order(7)
    @DisplayName("销售不能审批合同")
    void testSalesCannotAuditContract() throws Exception {
        Long customerId = createCustomerAsSales("销售客户-H");
        Long contractId = createContractAsSales(customerId, "销售合同-H", "1", 12000.00);

        Map<String, Object> audit = new LinkedHashMap<>();
        audit.put("contractId", contractId);
        audit.put("auditStatus", "1");

        asSales(HttpMethod.POST, "/system/contract/audit", audit);

        String contractDetail = getResponseJson(asSales(HttpMethod.GET, "/system/contract/" + contractId, null));
        assertThat(contractDetail).contains("\"auditStatus\":\"0\"");
    }

    @Test
    @Order(8)
    @DisplayName("销售可以查看客户列表")
    void testSalesCanViewCustomerList() throws Exception {
        createCustomerAsSales("销售客户-I");

        String customerList = getResponseJson(asSales(HttpMethod.GET, "/system/customer/list", null));
        assertThat(customerList).contains("销售客户-I");
    }

    @Test
    @Order(9)
    @DisplayName("销售可以编辑自己创建的客户")
    void testSalesCanEditOwnCustomer() throws Exception {
        Long customerId = createCustomerAsSales("销售客户-J");

        Map<String, Object> update = new LinkedHashMap<>();
        update.put("customerId", customerId);
        update.put("customerName", "销售客户-J-修改");
        update.put("customerType", "1");
        update.put("contactPerson", "新联系人");
        update.put("contactPhone", "13900000002");
        update.put("ownerId", USER_ID_LISI);

        assertSuccess(asSales(HttpMethod.PUT, "/system/customer", update));

        String updated = getResponseJson(asSales(HttpMethod.GET, "/system/customer/detail/" + customerId, null));
        assertThat(updated).contains("销售客户-J-修改", "新联系人");
    }

    @Test
    @Order(10)
    @DisplayName("销售不能删除有关联合同的客户")
    void testSalesCannotDeleteCustomerWithContracts() throws Exception {
        Long customerId = createCustomerAsSales("销售客户-K");
        createContractAsSales(customerId, "销售合同-K", "1", 12000.00);

        asSales(HttpMethod.DELETE, "/system/customer/" + customerId, null);

        String customerList = getResponseJson(asSales(HttpMethod.GET, "/system/customer/list", null));
        assertThat(customerList).contains("销售客户-K");
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
        Map<String, Object> audit = new LinkedHashMap<>();
        audit.put("contractId", contractId);
        audit.put("auditStatus", "1");
        assertSuccess(asManager(HttpMethod.POST, "/system/contract/audit", audit));
        return contractId;
    }
}