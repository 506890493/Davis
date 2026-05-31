package com.ruoyi.web.controller.davis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E: 合同审批流程。
 *
 * sales 创建客户+合同 → manager 审批通过/驳回 → sales 修改后重新提交。
 */
@DisplayName("E2E: 合同审批流程")
public class ContractApprovalFlowTest extends BaseControllerTest {

    @Test
    @Order(1)
    @DisplayName("sales 创建合同 → manager 审批通过 → 查询验证状态")
    void testContractApproval() throws Exception {
        Long customerId = createCustomer();
        Long contractId = createContract(customerId, "代账服务合同-001");

        // manager 查看待审批合同列表
        assertThat(getResponseJson(asManager("GET", "/system/contract/list", null)))
            .contains("代账服务合同-001");

        // manager 审批通过
        Map<String, Object> auditPass = new LinkedHashMap<>();
        auditPass.put("contractId", contractId);
        auditPass.put("auditStatus", "1");
        assertSuccess(asManager("POST", "/system/contract/audit", auditPass));

        // 验证合同状态为通过
        assertThat(getResponseJson(asManager("GET", "/system/contract/" + contractId, null)))
            .contains("\"auditStatus\":\"1\"");
    }

    @Test
    @Order(2)
    @DisplayName("sales 重新提交被驳回的合同 → manager 再次审批通过")
    void testContractRejectAndResubmit() throws Exception {
        Long customerId = createCustomer();
        Long contractId = createContract(customerId, "地址出租合同-002");

        // manager 审批驳回
        Map<String, Object> auditReject = new LinkedHashMap<>();
        auditReject.put("contractId", contractId);
        auditReject.put("auditStatus", "2");
        assertSuccess(asManager("POST", "/system/contract/audit", auditReject));

        // 验证状态为驳回
        assertThat(getResponseJson(asManager("GET", "/system/contract/" + contractId, null)))
            .contains("\"auditStatus\":\"2\"");

        // sales 修改重新提交
        Map<String, Object> update = new LinkedHashMap<>();
        update.put("contractId", contractId);
        update.put("contractName", "地址出租合同-002-修改版");
        update.put("contractType", "2");
        update.put("customerId", customerId);
        update.put("amount", 15000.00);
        update.put("paymentCycle", "1");
        update.put("paymentMethod", "3");
        update.put("startDate", "2026-06-01");
        update.put("endDate", "2027-05-31");
        update.put("auditStatus", "0");
        assertSuccess(asSales("PUT", "/system/contract", update));

        // manager 再次审批通过
        Map<String, Object> auditPass = new LinkedHashMap<>();
        auditPass.put("contractId", contractId);
        auditPass.put("auditStatus", "1");
        assertSuccess(asManager("POST", "/system/contract/audit", auditPass));

        // 最终验证
        String finalJson = getResponseJson(asManager("GET", "/system/contract/" + contractId, null));
        assertThat(finalJson).contains("\"auditStatus\":\"1\"");
        assertThat(finalJson).contains("地址出租合同-002-修改版");
    }

    @SuppressWarnings("unchecked")
    private Long createCustomer() throws Exception {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("customerName", "审批测试客户");
        customer.put("customerType", "1");
        customer.put("contactPerson", "赵总");
        customer.put("contactPhone", "13900000100");
        customer.put("ownerId", 4L);
        ResultActions result = asSales("POST", "/system/customer", customer);
        String json = getResponseJson(result);
        Map<String, Object> resp = objectMapper.readValue(json, Map.class);
        Object data = resp.get("data");
        return data instanceof Map ? ((Number) ((Map) data).get("customerId")).longValue() : null;
    }

    @SuppressWarnings("unchecked")
    private Long createContract(Long customerId, String name) throws Exception {
        Map<String, Object> contract = new LinkedHashMap<>();
        contract.put("contractName", name);
        contract.put("contractType", "1");
        contract.put("customerId", customerId);
        contract.put("amount", 12000.00);
        contract.put("paymentCycle", "1");
        contract.put("paymentMethod", "3");
        contract.put("startDate", "2026-06-01");
        contract.put("endDate", "2027-05-31");
        contract.put("legalPerson", "法定代表人");
        contract.put("contactPerson", "联系人");
        contract.put("contactPhone", "13900000099");
        contract.put("ownerId", 4L);
        ResultActions result = asSales("POST", "/system/contract", contract);
        String json = getResponseJson(result);
        Map<String, Object> resp = objectMapper.readValue(json, Map.class);
        Object data = resp.get("data");
        return data instanceof Map ? ((Number) ((Map) data).get("contractId")).longValue() : null;
    }
}
