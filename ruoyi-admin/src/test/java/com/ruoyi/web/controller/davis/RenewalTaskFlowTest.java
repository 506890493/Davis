package com.ruoyi.web.controller.davis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E: 续费任务流程。
 *
 * 创建续费任务 → 会计完成续签（生成新合同） → 验证新合同基于原合同复制。
 */
@DisplayName("E2E: 续费任务流程")
public class RenewalTaskFlowTest extends BaseControllerTest {

    @Test
    @Order(1)
    @DisplayName("续费任务创建 → 完成续签 → 新合同生成 → 验证关联")
    void testRenewalFlow() throws Exception {
        Long customerId = createCustomer();
        Long sourceContractId = createContract(customerId);

        // manager 创建续费任务
        Map<String, Object> task = new LinkedHashMap<>();
        task.put("taskTitle", "续费任务-代账服务");
        task.put("contractId", sourceContractId);
        task.put("taskType", "2");
        task.put("priority", "2");
        task.put("originalAmount", 12000.00);
        task.put("currentAmount", 12000.00);
        task.put("assignedTo", 3L);
        task.put("status", "0");
        ResultActions createResult = asManager("POST", "/system/task", task);
        assertSuccess(createResult);
        Long taskId = getTaskId(createResult);
        assertThat(taskId).isNotNull();

        // zhangsan 完成续签并生成新合同
        Map<String, Object> newContract = new LinkedHashMap<>();
        newContract.put("contractName", "续费后新合同");
        newContract.put("amount", 15000.00);
        newContract.put("startDate", "2027-06-01");
        newContract.put("endDate", "2028-05-31");

        Map<String, Object> completeRenewal = new LinkedHashMap<>();
        completeRenewal.put("taskId", taskId);
        completeRenewal.put("generateContract", true);
        completeRenewal.put("newContract", newContract);

        ResultActions renewalResult = asAccountant("POST", "/system/task/completeRenewal", completeRenewal);
        assertSuccess(renewalResult);

        // 查任务获取 targetContractId（completeRenewal 返回 AjaxResult.success() 不含 data）
        String taskJson = getResponseJson(asManager("GET", "/system/task/" + taskId, null));
        assertThat(taskJson).contains("\"status\":\"4\"");
        // 从 JSON 中提取 targetContractId
        Long newContractId = extractLongFromJson(taskJson, "targetContractId");
        assertThat(newContractId).isNotNull();

        // 查新合同验证 parentId 和 auditStatus
        String newContractJson = getResponseJson(asManager("GET", "/system/contract/" + newContractId, null));
        assertThat(newContractJson).contains("\"parentId\":" + sourceContractId);
        assertThat(newContractJson).contains("\"contractName\":\"续费后新合同\"");
        assertThat(newContractJson).contains("\"auditStatus\":\"0\"");

        // 验证任务关联目标合同
        assertThat(taskJson).contains("\"targetContractId\":" + newContractId);
    }

    @SuppressWarnings("unchecked")
    private Long createCustomer() throws Exception {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("customerName", "续费测试客户");
        customer.put("customerType", "1");
        customer.put("contactPerson", "孙总");
        customer.put("contactPhone", "13900000200");
        customer.put("ownerId", 4L);
        ResultActions result = asSales("POST", "/system/customer", customer);
        String json = getResponseJson(result);
        Map<String, Object> resp = objectMapper.readValue(json, Map.class);
        return resp.get("data") instanceof Map ?
            ((Number) ((Map) resp.get("data")).get("customerId")).longValue() : null;
    }

    @SuppressWarnings("unchecked")
    private Long createContract(Long customerId) throws Exception {
        Map<String, Object> contract = new LinkedHashMap<>();
        contract.put("contractName", "原合同-续费测试");
        contract.put("contractType", "1");
        contract.put("customerId", customerId);
        contract.put("amount", 12000.00);
        contract.put("paymentCycle", "1");
        contract.put("paymentMethod", "3");
        contract.put("startDate", "2025-06-01");
        contract.put("endDate", "2026-05-31");
        contract.put("ownerId", 4L);
        ResultActions result = asSales("POST", "/system/contract", contract);
        String json = getResponseJson(result);
        Map<String, Object> resp = objectMapper.readValue(json, Map.class);
        Object data = resp.get("data");
        Long contractId = data instanceof Map ? ((Number) ((Map) data).get("contractId")).longValue() : null;
        if (contractId != null) {
            Map<String, Object> audit = new LinkedHashMap<>();
            audit.put("contractId", contractId);
            audit.put("auditStatus", "1");
            asManager("POST", "/system/contract/audit", audit);
        }
        return contractId;
    }

    @SuppressWarnings("unchecked")
    private Long getTaskId(ResultActions result) throws Exception {
        String json = getResponseJson(result);
        Map<String, Object> resp = objectMapper.readValue(json, Map.class);
        Object data = resp.get("data");
        return data instanceof Map ? ((Number) ((Map) data).get("taskId")).longValue() : null;
    }

    /**
     * 从 JSON 字符串中提取指定字段的 Long 值（支持嵌套查询如 "targetContractId"）。
     */
    @SuppressWarnings("unchecked")
    private Long extractLongFromJson(String json, String field) throws Exception {
        Map<String, Object> resp = objectMapper.readValue(json, Map.class);
        Object data = resp.get("data");
        if (data instanceof Map) {
            Object val = ((Map<String, Object>) data).get(field);
            if (val instanceof Number) {
                return ((Number) val).longValue();
            }
        }
        return null;
    }
}
