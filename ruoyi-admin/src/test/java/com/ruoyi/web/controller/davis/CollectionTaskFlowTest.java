package com.ruoyi.web.controller.davis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E: 催收任务全流程。
 *
 * 创建催收任务 → 会计退回讲价 → 管理员重新派发 → 会计确认收款。
 * 涉及三个角色（manager/sales/account）及数据隔离验证。
 */
@DisplayName("E2E: 催收任务全流程（核心链路）")
public class CollectionTaskFlowTest extends BaseControllerTest {

    @Test
    @Order(1)
    @DisplayName("催收任务创建 → 退回讲价 → 重新派发 → 确认收款")
    void testCollectionTaskFullFlow() throws Exception {
        Long customerId = createCustomer();
        Long contractId = createContract(customerId);

        // 1. manager 创建催收任务（分配给 zhangsan）
        Map<String, Object> task = new LinkedHashMap<>();
        task.put("taskTitle", "催收任务-代账服务费");
        task.put("contractId", contractId);
        task.put("taskType", "1");
        task.put("priority", "2");
        task.put("originalAmount", 12000.00);
        task.put("currentAmount", 12000.00);
        task.put("assignedTo", 3L);
        task.put("status", "0");

        ResultActions createResult = asManager("POST", "/system/task", task);
        assertSuccess(createResult);
        Long taskId = getTaskId(createResult);
        assertThat(taskId).isNotNull();

        // 验证合同 reminderStatus 变为 "1"（催收中）
        assertThat(getResponseJson(asManager("GET", "/system/contract/" + contractId, null)))
            .contains("\"reminderStatus\":\"1\"");

        // 2. zhangsan 查看任务列表
        assertThat(getResponseJson(asAccountant("GET", "/system/task/list", null)))
            .contains("催收任务-代账服务费");

        // 3. zhangsan 设置进行中 → 退回讲价
        Map<String, Object> inProgress = new LinkedHashMap<>();
        inProgress.put("taskId", taskId);
        inProgress.put("status", "1");
        asAccountant("PUT", "/system/task", inProgress);

        Map<String, Object> returnToAdmin = new LinkedHashMap<>();
        returnToAdmin.put("taskId", taskId);
        returnToAdmin.put("currentAmount", 10000.00);
        assertSuccess(asAccountant("POST", "/system/task/returnToAdmin", returnToAdmin));

        // 验证任务状态变 "2"（待审批）
        assertThat(getResponseJson(asManager("GET", "/system/task/" + taskId, null)))
            .contains("\"status\":\"2\"");

        // 4. manager 重新派发
        Map<String, Object> redispatch = new LinkedHashMap<>();
        redispatch.put("taskId", taskId);
        redispatch.put("assignedTo", 3L);
        assertSuccess(asManager("POST", "/system/task/redispatch", redispatch));

        // 验证任务状态变回 "0"
        assertThat(getResponseJson(asManager("GET", "/system/task/" + taskId, null)))
            .contains("\"status\":\"0\"");

        // 5. zhangsan 确认收款
        Map<String, Object> confirmPayment = new LinkedHashMap<>();
        confirmPayment.put("taskId", taskId);
        confirmPayment.put("actualAmount", 10000.00);
        confirmPayment.put("receiveRemark", "银行转账收款，已到账");
        assertSuccess(asAccountant("POST", "/system/task/confirmPayment", confirmPayment));

        // 验证任务状态 "4"（已完成）
        assertThat(getResponseJson(asManager("GET", "/system/task/" + taskId, null)))
            .contains("\"status\":\"4\"");

        // 验证合同 reminderStatus 变为 "3"（已完成）
        assertThat(getResponseJson(asManager("GET", "/system/contract/" + contractId, null)))
            .contains("\"reminderStatus\":\"3\"");
    }

    @SuppressWarnings("unchecked")
    private Long createCustomer() throws Exception {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("customerName", "催收测试客户");
        customer.put("customerType", "1");
        customer.put("contactPerson", "钱总");
        customer.put("contactPhone", "13900000100");
        customer.put("ownerId", 4L);
        ResultActions result = asSales("POST", "/system/customer", customer);
        String json = getResponseJson(result);
        Map<String, Object> resp = objectMapper.readValue(json, Map.class);
        Object data = resp.get("data");
        return data instanceof Map ? ((Number) ((Map) data).get("customerId")).longValue() : null;
    }

    @SuppressWarnings("unchecked")
    private Long createContract(Long customerId) throws Exception {
        Map<String, Object> contract = new LinkedHashMap<>();
        contract.put("contractName", "催收测试合同");
        contract.put("contractType", "1");
        contract.put("customerId", customerId);
        contract.put("amount", 12000.00);
        contract.put("paymentCycle", "1");
        contract.put("paymentMethod", "3");
        contract.put("startDate", "2026-06-01");
        contract.put("endDate", "2027-05-31");
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
}
