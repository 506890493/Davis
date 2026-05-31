package com.ruoyi.web.controller.davis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import org.springframework.http.HttpMethod;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E: 终止合作流程。
 *
 * 创建催收任务 → 会计发起终止 → manager 确认/拒绝终止。
 * 覆盖两种结果：同意（任务完成）和拒绝（任务退回）。
 */
@DisplayName("E2E: 终止合作流程")
public class TerminationFlowTest extends BaseControllerTest {

    @Test
    @Order(1)
    @DisplayName("催收任务 → 发起终止 → 管理员确认终止 → 任务完成 + 合同催收状态变更")
    void testTerminationApproved() throws Exception {
        Long customerId = createCustomer();
        Long contractId = createContract(customerId);
        Long taskId = createCollectionTask(contractId);

        // zhangsan 发起终止请求
        Map<String, Object> terminationReq = new LinkedHashMap<>();
        terminationReq.put("taskId", taskId);
        terminationReq.put("remark", "客户已注销公司，不再合作");
        assertSuccess(asAccountant(HttpMethod.POST, "/system/task/requestTermination", terminationReq));

        // 验证 taskType "3"（终止），status "2"（待审批）
        String afterRequestJson = getResponseJson(asManager(HttpMethod.GET, "/system/task/" + taskId, null));
        assertThat(afterRequestJson).contains("\"taskType\":\"3\"");
        assertThat(afterRequestJson).contains("\"status\":\"2\"");

        // manager 确认终止（approved=true）
        Map<String, Object> confirmApproved = new LinkedHashMap<>();
        confirmApproved.put("taskId", taskId);
        confirmApproved.put("approved", true);
        assertSuccess(asManager(HttpMethod.POST, "/system/task/confirmTermination", confirmApproved));

        // 验证任务 "4"（已完成）
        assertThat(getResponseJson(asManager(HttpMethod.GET, "/system/task/" + taskId, null)))
            .contains("\"status\":\"4\"");

        // 验证合同 reminderStatus "3"（已完成）
        assertThat(getResponseJson(asManager(HttpMethod.GET, "/system/contract/" + contractId, null)))
            .contains("\"reminderStatus\":\"3\"");
    }

    @Test
    @Order(2)
    @DisplayName("催收任务 → 发起终止 → 管理员拒绝 → 任务退回")
    void testTerminationRejected() throws Exception {
        Long customerId = createCustomer();
        Long contractId = createContract(customerId);
        Long taskId = createCollectionTask(contractId);

        // zhangsan 发起终止
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("taskId", taskId);
        req.put("remark", "客户拒绝付款");
        asAccountant(HttpMethod.POST, "/system/task/requestTermination", req);

        // manager 拒绝终止（approved=false）
        Map<String, Object> reject = new LinkedHashMap<>();
        reject.put("taskId", taskId);
        reject.put("approved", false);
        assertSuccess(asManager(HttpMethod.POST, "/system/task/confirmTermination", reject));

        // 验证任务状态 "3"（已退回）
        assertThat(getResponseJson(asManager(HttpMethod.GET, "/system/task/" + taskId, null)))
            .contains("\"status\":\"3\"");
    }

    private Long createCustomer() throws Exception {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("customerName", "终止测试客户");
        customer.put("customerType", "1");
        customer.put("contactPerson", "周总");
        customer.put("contactPhone", "13900000300");
        customer.put("ownerId", 4L);
        assertSuccess(asSales(HttpMethod.POST, "/system/customer", customer));
        ResultActions listResult = asSales(HttpMethod.GET, "/system/customer/list", null);
        assertListSuccess(listResult);
        Long customerId = getIdFromList(listResult, "customerId");
        assertThat(customerId).isNotNull();
        return customerId;
    }

    private Long createContract(Long customerId) throws Exception {
        Map<String, Object> contract = new LinkedHashMap<>();
        contract.put("contractName", "终止测试合同");
        contract.put("contractType", "1");
        contract.put("customerId", customerId);
        contract.put("amount", 12000.00);
        contract.put("paymentCycle", "1");
        contract.put("paymentMethod", "3");
        contract.put("startDate", "2026-01-01");
        contract.put("endDate", "2026-12-31");
        contract.put("ownerId", 4L);
        assertSuccess(asSales(HttpMethod.POST, "/system/contract", contract));
        ResultActions listResult = asSales(HttpMethod.GET, "/system/contract/list", null);
        assertListSuccess(listResult);
        Long contractId = getIdFromList(listResult, "contractId");
        assertThat(contractId).isNotNull();
        Map<String, Object> audit = new LinkedHashMap<>();
        audit.put("contractId", contractId);
        audit.put("auditStatus", "1");
        assertSuccess(asManager(HttpMethod.POST, "/system/contract/audit", audit));
        return contractId;
    }

    @SuppressWarnings("unchecked")
    private Long createCollectionTask(Long contractId) throws Exception {
        Map<String, Object> task = new LinkedHashMap<>();
        task.put("taskTitle", "终止测试-催收任务");
        task.put("contractId", contractId);
        task.put("taskType", "1");
        task.put("priority", "2");
        task.put("originalAmount", 12000.00);
        task.put("currentAmount", 12000.00);
        task.put("assignedTo", 3L);
        task.put("status", "0");
        assertSuccess(asManager(HttpMethod.POST, "/system/task", task));
        // POST 返回 toAjax(int)，查列表获取 ID
        ResultActions listResult = asManager(HttpMethod.GET, "/system/task/list?pageNum=1&pageSize=50", null);
        assertListSuccess(listResult);
        return getIdFromList(listResult, "taskId");
    }
}
