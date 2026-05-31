package com.ruoyi.web.controller.davis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E: 合同管理流程。
 *
 * 分页查询 → 类型筛选 → 修改金额 → 删除合同 → 验证删除后不可见。
 * 验证合同软删除后客户详情中不显示已删除合同。
 */
@DisplayName("E2E: 合同管理流程")
public class ContractManagementFlowTest extends BaseControllerTest {

    @Test
    @Order(1)
    @DisplayName("合同 CRUD + 软删除 + 关联客户详情联动验证")
    void testContractManagement() throws Exception {
        Long customerId = createCustomer("管理测试客户");
        Long contract1Id = createContract(customerId, "代账合同-A", "1", 12000.00);
        Long contract2Id = createContract(customerId, "地址合同-B", "2", 8000.00);

        // 1. 查询列表包含两个合同
        assertThat(getResponseJson(asManager("GET", "/system/contract/list", null)))
            .contains("代账合同-A", "地址合同-B");

        // 2. 按类型筛选（代账 type=1）
        assertThat(getResponseJson(asManager("GET", "/system/contract/list?contractType=1", null)))
            .contains("代账合同-A")
            .doesNotContain("地址合同-B");

        // 3. 修改合同金额
        Map<String, Object> update = new LinkedHashMap<>();
        update.put("contractId", contract1Id);
        update.put("contractName", "代账合同-A");
        update.put("contractType", "1");
        update.put("customerId", customerId);
        update.put("amount", 15000.00);
        update.put("paymentCycle", "1");
        update.put("paymentMethod", "3");
        update.put("ownerId", 2L);
        assertSuccess(asManager("PUT", "/system/contract", update));

        // 4. 验证金额已更新
        assertThat(getResponseJson(asManager("GET", "/system/contract/" + contract1Id, null)))
            .contains("\"amount\":15000.0");

        // 5. 删除合同
        assertSuccess(asManager("DELETE", "/system/contract/" + contract1Id, null));

        // 6. 列表验证已删除
        String listAfter = getResponseJson(asManager("GET", "/system/contract/list", null));
        assertThat(listAfter).doesNotContain("代账合同-A");
        assertThat(listAfter).contains("地址合同-B");

        // 7. 按 ID 查已删除合同
        asManager("GET", "/system/contract/" + contract1Id, null)
            .andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.not(200)));

        // 8. 客户详情中已删除合同不出现
        String customerDetail = getResponseJson(asManager("GET", "/system/customer/detail/" + customerId, null));
        assertThat(customerDetail).doesNotContain("代账合同-A");
        assertThat(customerDetail).contains("地址合同-B");
    }

    @SuppressWarnings("unchecked")
    private Long createCustomer(String name) throws Exception {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("customerName", name);
        customer.put("customerType", "1");
        customer.put("contactPerson", "联系人");
        customer.put("contactPhone", "13900000001");
        customer.put("ownerId", 2L);
        ResultActions result = asManager("POST", "/system/customer", customer);
        String json = getResponseJson(result);
        Map<String, Object> resp = objectMapper.readValue(json, Map.class);
        Object data = resp.get("data");
        return data instanceof Map ? ((Number) ((Map<String, Object>) data).get("customerId")).longValue() : null;
    }

    @SuppressWarnings("unchecked")
    private Long createContract(Long customerId, String name, String type, Double amount) throws Exception {
        Map<String, Object> contract = new LinkedHashMap<>();
        contract.put("contractName", name);
        contract.put("contractType", type);
        contract.put("customerId", customerId);
        contract.put("amount", amount);
        contract.put("paymentCycle", "1");
        contract.put("paymentMethod", "3");
        contract.put("startDate", "2026-06-01");
        contract.put("endDate", "2027-05-31");
        contract.put("ownerId", 2L);
        ResultActions result = asManager("POST", "/system/contract", contract);
        String json = getResponseJson(result);
        Map<String, Object> resp = objectMapper.readValue(json, Map.class);
        Object data = resp.get("data");
        Long contractId = data instanceof Map ? ((Number) ((Map<String, Object>) data).get("contractId")).longValue() : null;
        if (contractId != null) {
            Map<String, Object> audit = new LinkedHashMap<>();
            audit.put("contractId", contractId);
            audit.put("auditStatus", "1");
            assertSuccess(asManager("POST", "/system/contract/audit", audit));
        }
        return contractId;
    }
}
