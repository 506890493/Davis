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
 * E2E: 会计角色权限测试。
 *
 * 验证会计角色的数据隔离和权限：
 * - 只能看到分配给自己的任务（assigned_to = 自己）
 * - 可执行：退回讲价、确认收款、完成续签、发起终止请求
 * - 不能看到别人的任务
 * - 不能修改任务类型、不能派发任务、不能审批
 */
@DisplayName("E2E: 会计角色权限测试")
public class AccountantRolePermissionTest extends BaseControllerTest {

    @Test
    @Order(1)
    @DisplayName("会计只能看到分配给自己的任务")
    void testAccountantOnlySeeOwnTasks() throws Exception {
        Long customerId = createCustomerAsManager("会计测试客户-A");
        Long contractId = createContractAsManager(customerId, "会计测试合同-A", "1", 10000.00);
        Long taskId = createTaskAsManager(contractId, "1", USER_ID_ZHANGSAN);

        Long anotherAccountId = USER_ID_ZHANGSAN + 1;
        Long taskId2 = createTaskAsManager(contractId, "1", anotherAccountId);

        String zhangsanView = getResponseJson(asAccountant(HttpMethod.GET, "/system/task/list", null));
        assertThat(zhangsanView).contains("\"assignedTo\":" + USER_ID_ZHANGSAN);
        assertThat(zhangsanView).doesNotContain("\"assignedTo\":" + anotherAccountId);

        String managerView = getResponseJson(asManager(HttpMethod.GET, "/system/task/list", null));
        assertThat(managerView).contains("\"assignedTo\":" + USER_ID_ZHANGSAN);
        assertThat(managerView).contains("\"assignedTo\":" + anotherAccountId);
    }

    @Test
    @Order(2)
    @DisplayName("会计可以查看任务详情")
    void testAccountantCanViewTaskDetail() throws Exception {
        Long customerId = createCustomerAsManager("会计测试客户-B");
        Long contractId = createContractAsManager(customerId, "会计测试合同-B", "1", 12000.00);
        Long taskId = createTaskAsManager(contractId, "1", USER_ID_ZHANGSAN);

        String taskDetail = getResponseJson(asAccountant(HttpMethod.GET, "/system/task/" + taskId, null));
        assertThat(taskDetail).contains("\"taskId\":" + taskId);
        assertThat(taskDetail).contains("\"taskType\":\"1\"");
        assertThat(taskDetail).contains("\"assignedTo\":" + USER_ID_ZHANGSAN);
        assertThat(taskDetail).contains("\"contractId\":" + contractId);
    }

    @Test
    @Order(3)
    @DisplayName("会计可以将任务改为进行中状态")
    void testAccountantCanUpdateTaskStatus() throws Exception {
        Long customerId = createCustomerAsManager("会计测试客户-C");
        Long contractId = createContractAsManager(customerId, "会计测试合同-C", "1", 15000.00);
        Long taskId = createTaskAsManager(contractId, "1", USER_ID_ZHANGSAN);

        Map<String, Object> update = new LinkedHashMap<>();
        update.put("taskId", taskId);
        update.put("status", "1");
        update.put("contractId", contractId);
        update.put("taskType", "1");
        update.put("assignedTo", USER_ID_ZHANGSAN);

        assertSuccess(asAccountant(HttpMethod.PUT, "/system/task", update));

        String updated = getResponseJson(asAccountant(HttpMethod.GET, "/system/task/" + taskId, null));
        assertThat(updated).contains("\"status\":\"1\"");
    }

    @Test
    @Order(4)
    @DisplayName("会计可以退回讲价")
    void testAccountantCanReturnToAdmin() throws Exception {
        Long customerId = createCustomerAsManager("会计测试客户-D");
        Long contractId = createContractAsManager(customerId, "会计测试合同-D", "1", 20000.00);
        Long taskId = createTaskAsManager(contractId, "1", USER_ID_ZHANGSAN);

        Map<String, Object> returnRequest = new LinkedHashMap<>();
        returnRequest.put("taskId", taskId);
        returnRequest.put("currentAmount", 18000.00);
        returnRequest.put("remark", "客户同意降低金额");
        returnRequest.put("contractId", contractId);
        returnRequest.put("taskType", "1");
        returnRequest.put("assignedTo", USER_ID_ZHANGSAN);

        assertSuccess(asAccountant(HttpMethod.POST, "/system/task/returnToAdmin", returnRequest));

        String updated = getResponseJson(asAccountant(HttpMethod.GET, "/system/task/" + taskId, null));
        assertThat(updated).contains("\"status\":\"2\"");
        assertThat(updated).contains("\"currentAmount\":18000.0");
    }

    @Test
    @Order(5)
    @DisplayName("会计可以确认收款")
    void testAccountantCanConfirmPayment() throws Exception {
        Long customerId = createCustomerAsManager("会计测试客户-E");
        Long contractId = createContractAsManager(customerId, "会计测试合同-E", "1", 25000.00);
        Long taskId = createTaskAsManager(contractId, "1", USER_ID_ZHANGSAN);

        Map<String, Object> confirmPayment = new LinkedHashMap<>();
        confirmPayment.put("taskId", taskId);
        confirmPayment.put("actualAmount", 25000.00);
        confirmPayment.put("receiveRemark", "已全额收款");
        confirmPayment.put("contractId", contractId);
        confirmPayment.put("taskType", "1");
        confirmPayment.put("assignedTo", USER_ID_ZHANGSAN);

        assertSuccess(asAccountant(HttpMethod.POST, "/system/task/confirmPayment", confirmPayment));

        String updated = getResponseJson(asAccountant(HttpMethod.GET, "/system/task/" + taskId, null));
        assertThat(updated).contains("\"status\":\"4\"");

        String contractDetail = getResponseJson(asAccountant(HttpMethod.GET, "/system/contract/" + contractId, null));
        assertThat(contractDetail).contains("\"reminderStatus\":\"3\"");
        assertThat(contractDetail).contains("\"actualAmount\":25000.0");
    }

    @Test
    @Order(6)
    @DisplayName("会计可以完成续签")
    void testAccountantCanCompleteRenewal() throws Exception {
        Long customerId = createCustomerAsManager("会计测试客户-F");
        Long contractId = createContractAsManager(customerId, "会计测试合同-F", "1", 30000.00);
        Long taskId = createTaskAsManager(contractId, "2", USER_ID_ZHANGSAN);

        Map<String, Object> newContract = new LinkedHashMap<>();
        newContract.put("contractName", "会计测试合同-F-续签");
        newContract.put("contractType", "1");
        newContract.put("customerId", customerId);
        newContract.put("amount", 35000.00);
        newContract.put("paymentCycle", "1");
        newContract.put("paymentMethod", "3");
        newContract.put("startDate", "2027-06-01");
        newContract.put("endDate", "2028-05-31");
        newContract.put("parentId", contractId);

        Map<String, Object> completeRenewal = new LinkedHashMap<>();
        completeRenewal.put("taskId", taskId);
        completeRenewal.put("generateContract", true);
        completeRenewal.put("newContract", newContract);

        assertSuccess(asAccountant(HttpMethod.POST, "/system/task/completeRenewal", completeRenewal));

        String updatedTask = getResponseJson(asAccountant(HttpMethod.GET, "/system/task/" + taskId, null));
        assertThat(updatedTask).contains("\"status\":\"4\"");

        String contractList = getResponseJson(asAccountant(HttpMethod.GET, "/system/contract/list", null));
        assertThat(contractList).contains("会计测试合同-F-续签");

        ResultActions listResult = asAccountant(HttpMethod.GET, "/system/contract/list", null);
        assertListSuccess(listResult);
        Long newContractId = getIdFromList(listResult, "contractId");

        String newContractDetail = getResponseJson(asAccountant(HttpMethod.GET, "/system/contract/" + newContractId, null));
        assertThat(newContractDetail).contains("\"parentId\":" + contractId);
        assertThat(newContractDetail).contains("\"amount\":35000.0");
    }

    @Test
    @Order(7)
    @DisplayName("会计可以发起终止请求")
    void testAccountantCanRequestTermination() throws Exception {
        Long customerId = createCustomerAsManager("会计测试客户-G");
        Long contractId = createContractAsManager(customerId, "会计测试合同-G", "1", 40000.00);
        Long taskId = createTaskAsManager(contractId, "1", USER_ID_ZHANGSAN);

        Map<String, Object> terminationRequest = new LinkedHashMap<>();
        terminationRequest.put("taskId", taskId);
        terminationRequest.put("taskType", "3");
        terminationRequest.put("remark", "客户要求终止合作");
        terminationRequest.put("contractId", contractId);
        terminationRequest.put("assignedTo", USER_ID_ZHANGSAN);

        assertSuccess(asAccountant(HttpMethod.POST, "/system/task/requestTermination", terminationRequest));

        String updated = getResponseJson(asAccountant(HttpMethod.GET, "/system/task/" + taskId, null));
        assertThat(updated).contains("\"taskType\":\"3\"");
        assertThat(updated).contains("\"status\":\"2\"");

        String pendingList = getResponseJson(asManager(HttpMethod.GET, "/system/task/pendingList", null));
        assertThat(pendingList).contains("\"taskId\":" + taskId);
    }

    @Test
    @Order(8)
    @DisplayName("会计不能查看别人的任务详情")
    void testAccountantCannotViewOthersTask() throws Exception {
        Long customerId = createCustomerAsManager("会计测试客户-H");
        Long contractId = createContractAsManager(customerId, "会计测试合同-H", "1", 50000.00);
        Long anotherAccountId = USER_ID_ZHANGSAN + 1;
        Long taskId = createTaskAsManager(contractId, "1", anotherAccountId);

        asAccountant(HttpMethod.GET, "/system/task/" + taskId, null);

        String taskList = getResponseJson(asAccountant(HttpMethod.GET, "/system/task/list", null));
        assertThat(taskList).doesNotContain("\"taskId\":" + taskId);
    }

    @Test
    @Order(9)
    @DisplayName("会计不能修改分配给别人的任务")
    void testAccountantCannotModifyOthersTask() throws Exception {
        Long customerId = createCustomerAsManager("会计测试客户-I");
        Long contractId = createContractAsManager(customerId, "会计测试合同-I", "1", 60000.00);
        Long anotherAccountId = USER_ID_ZHANGSAN + 1;
        Long taskId = createTaskAsManager(contractId, "1", anotherAccountId);

        Map<String, Object> update = new LinkedHashMap<>();
        update.put("taskId", taskId);
        update.put("status", "1");
        update.put("contractId", contractId);
        update.put("taskType", "1");
        update.put("assignedTo", anotherAccountId);

        asAccountant(HttpMethod.PUT, "/system/task", update);

        String taskDetail = getResponseJson(asManager(HttpMethod.GET, "/system/task/" + taskId, null));
        assertThat(taskDetail).contains("\"status\":\"0\"");
    }

    @Test
    @Order(10)
    @DisplayName("会计不能创建任务")
    void testAccountantCannotCreateTask() throws Exception {
        Long customerId = createCustomerAsManager("会计测试客户-J");
        Long contractId = createContractAsManager(customerId, "会计测试合同-J", "1", 70000.00);

        Map<String, Object> task = new LinkedHashMap<>();
        task.put("taskType", "1");
        task.put("contractId", contractId);
        task.put("assignedTo", USER_ID_ZHANGSAN);
        task.put("taskName", "催收任务-J");
        task.put("remark", "测试任务");

        asAccountant(HttpMethod.POST, "/system/task", task);

        String taskList = getResponseJson(asManager(HttpMethod.GET, "/system/task/list", null));
        assertThat(taskList).doesNotContain("催收任务-J");
    }

    @Test
    @Order(11)
    @DisplayName("会计不能派发任务")
    void testAccountantCannotDispatchTask() throws Exception {
        Long customerId = createCustomerAsManager("会计测试客户-K");
        Long contractId = createContractAsManager(customerId, "会计测试合同-K", "1", 80000.00);
        Long taskId = createTaskAsManager(contractId, "1", USER_ID_ZHANGSAN);

        Map<String, Object> returnRequest = new LinkedHashMap<>();
        returnRequest.put("taskId", taskId);
        returnRequest.put("currentAmount", 75000.00);
        returnRequest.put("remark", "客户同意降价");
        returnRequest.put("contractId", contractId);
        returnRequest.put("taskType", "1");
        returnRequest.put("assignedTo", USER_ID_ZHANGSAN);

        assertSuccess(asAccountant(HttpMethod.POST, "/system/task/returnToAdmin", returnRequest));

        Map<String, Object> redispatch = new LinkedHashMap<>();
        redispatch.put("taskId", taskId);
        redispatch.put("status", "0");
        redispatch.put("contractId", contractId);
        redispatch.put("taskType", "1");
        redispatch.put("assignedTo", USER_ID_ZHANGSAN);

        asAccountant(HttpMethod.POST, "/system/task/redispatch", redispatch);

        String taskDetail = getResponseJson(asManager(HttpMethod.GET, "/system/task/" + taskId, null));
        assertThat(taskDetail).contains("\"status\":\"2\"");
    }

    @Test
    @Order(12)
    @DisplayName("会计不能审批协商价格")
    void testAccountantCannotAuditPrice() throws Exception {
        Long customerId = createCustomerAsManager("会计测试客户-L");
        Long contractId = createContractAsManager(customerId, "会计测试合同-L", "1", 90000.00);
        Long taskId = createTaskAsManager(contractId, "1", USER_ID_ZHANGSAN);

        Map<String, Object> returnRequest = new LinkedHashMap<>();
        returnRequest.put("taskId", taskId);
        returnRequest.put("currentAmount", 85000.00);
        returnRequest.put("remark", "客户同意降价");
        returnRequest.put("contractId", contractId);
        returnRequest.put("taskType", "1");
        returnRequest.put("assignedTo", USER_ID_ZHANGSAN);

        assertSuccess(asAccountant(HttpMethod.POST, "/system/task/returnToAdmin", returnRequest));

        Map<String, Object> rejectPrice = new LinkedHashMap<>();
        rejectPrice.put("taskId", taskId);
        rejectPrice.put("contractId", contractId);
        rejectPrice.put("taskType", "1");
        rejectPrice.put("assignedTo", USER_ID_ZHANGSAN);

        asAccountant(HttpMethod.POST, "/system/task/rejectPrice", rejectPrice);

        String taskDetail = getResponseJson(asManager(HttpMethod.GET, "/system/task/" + taskId, null));
        assertThat(taskDetail).contains("\"status\":\"2\"");
    }

    @Test
    @Order(13)
    @DisplayName("会计不能确认终止合作")
    void testAccountantCannotConfirmTermination() throws Exception {
        Long customerId = createCustomerAsManager("会计测试客户-M");
        Long contractId = createContractAsManager(customerId, "会计测试合同-M", "1", 100000.00);
        Long taskId = createTaskAsManager(contractId, "1", USER_ID_ZHANGSAN);

        Map<String, Object> terminationRequest = new LinkedHashMap<>();
        terminationRequest.put("taskId", taskId);
        terminationRequest.put("taskType", "3");
        terminationRequest.put("remark", "客户要求终止");
        terminationRequest.put("contractId", contractId);
        terminationRequest.put("assignedTo", USER_ID_ZHANGSAN);

        assertSuccess(asAccountant(HttpMethod.POST, "/system/task/requestTermination", terminationRequest));

        Map<String, Object> confirmTermination = new LinkedHashMap<>();
        confirmTermination.put("taskId", taskId);
        confirmTermination.put("approved", true);

        asAccountant(HttpMethod.POST, "/system/task/confirmTermination", confirmTermination);

        String taskDetail = getResponseJson(asManager(HttpMethod.GET, "/system/task/" + taskId, null));
        assertThat(taskDetail).contains("\"status\":\"2\"");
    }

    @Test
    @Order(14)
    @DisplayName("会计可以查看任务操作日志")
    void testAccountantCanViewTaskLog() throws Exception {
        Long customerId = createCustomerAsManager("会计测试客户-N");
        Long contractId = createContractAsManager(customerId, "会计测试合同-N", "1", 110000.00);
        Long taskId = createTaskAsManager(contractId, "1", USER_ID_ZHANGSAN);

        Map<String, Object> update = new LinkedHashMap<>();
        update.put("taskId", taskId);
        update.put("status", "1");
        update.put("contractId", contractId);
        update.put("taskType", "1");
        update.put("assignedTo", USER_ID_ZHANGSAN);
        assertSuccess(asAccountant(HttpMethod.PUT, "/system/task", update));

        Map<String, Object> returnRequest = new LinkedHashMap<>();
        returnRequest.put("taskId", taskId);
        returnRequest.put("currentAmount", 100000.00);
        returnRequest.put("remark", "退回讲价");
        returnRequest.put("contractId", contractId);
        returnRequest.put("taskType", "1");
        returnRequest.put("assignedTo", USER_ID_ZHANGSAN);
        assertSuccess(asAccountant(HttpMethod.POST, "/system/task/returnToAdmin", returnRequest));

        String logList = getResponseJson(asAccountant(HttpMethod.GET, "/system/task/log?taskId=" + taskId, null));
        assertThat(logList).contains("\"taskId\":" + taskId);
    }

    @Test
    @Order(15)
    @DisplayName("会计完整催收任务流程")
    void testAccountantFullCollectionFlow() throws Exception {
        Long customerId = createCustomerAsManager("会计测试客户-O");
        Long contractId = createContractAsManager(customerId, "会计测试合同-O", "1", 120000.00);
        Long taskId = createTaskAsManager(contractId, "1", USER_ID_ZHANGSAN);

        Map<String, Object> update = new LinkedHashMap<>();
        update.put("taskId", taskId);
        update.put("status", "1");
        update.put("contractId", contractId);
        update.put("taskType", "1");
        update.put("assignedTo", USER_ID_ZHANGSAN);
        assertSuccess(asAccountant(HttpMethod.PUT, "/system/task", update));

        Map<String, Object> returnRequest = new LinkedHashMap<>();
        returnRequest.put("taskId", taskId);
        returnRequest.put("currentAmount", 115000.00);
        returnRequest.put("remark", "客户同意降价");
        returnRequest.put("contractId", contractId);
        returnRequest.put("taskType", "1");
        returnRequest.put("assignedTo", USER_ID_ZHANGSAN);
        assertSuccess(asAccountant(HttpMethod.POST, "/system/task/returnToAdmin", returnRequest));

        Map<String, Object> redispatch = new LinkedHashMap<>();
        redispatch.put("taskId", taskId);
        redispatch.put("status", "0");
        redispatch.put("contractId", contractId);
        redispatch.put("taskType", "1");
        redispatch.put("assignedTo", USER_ID_ZHANGSAN);
        assertSuccess(asManager(HttpMethod.POST, "/system/task/redispatch", redispatch));

        Map<String, Object> confirmPayment = new LinkedHashMap<>();
        confirmPayment.put("taskId", taskId);
        confirmPayment.put("actualAmount", 115000.00);
        confirmPayment.put("receiveRemark", "已收款");
        confirmPayment.put("contractId", contractId);
        confirmPayment.put("taskType", "1");
        confirmPayment.put("assignedTo", USER_ID_ZHANGSAN);
        assertSuccess(asAccountant(HttpMethod.POST, "/system/task/confirmPayment", confirmPayment));

        String finalTask = getResponseJson(asAccountant(HttpMethod.GET, "/system/task/" + taskId, null));
        assertThat(finalTask).contains("\"status\":\"4\"");

        String finalContract = getResponseJson(asAccountant(HttpMethod.GET, "/system/contract/" + contractId, null));
        assertThat(finalContract).contains("\"reminderStatus\":\"3\"");
        assertThat(finalContract).contains("\"actualAmount\":115000.0");
    }

    @Test
    @Order(16)
    @DisplayName("会计完整续费任务流程")
    void testAccountantFullRenewalFlow() throws Exception {
        Long customerId = createCustomerAsManager("会计测试客户-P");
        Long contractId = createContractAsManager(customerId, "会计测试合同-P", "1", 130000.00);
        Long taskId = createTaskAsManager(contractId, "2", USER_ID_ZHANGSAN);

        Map<String, Object> newContract = new LinkedHashMap<>();
        newContract.put("contractName", "会计测试合同-P-续签");
        newContract.put("contractType", "1");
        newContract.put("customerId", customerId);
        newContract.put("amount", 140000.00);
        newContract.put("paymentCycle", "1");
        newContract.put("paymentMethod", "3");
        newContract.put("startDate", "2027-06-01");
        newContract.put("endDate", "2028-05-31");
        newContract.put("parentId", contractId);

        Map<String, Object> completeRenewal = new LinkedHashMap<>();
        completeRenewal.put("taskId", taskId);
        completeRenewal.put("generateContract", true);
        completeRenewal.put("newContract", newContract);

        assertSuccess(asAccountant(HttpMethod.POST, "/system/task/completeRenewal", completeRenewal));

        String finalTask = getResponseJson(asAccountant(HttpMethod.GET, "/system/task/" + taskId, null));
        assertThat(finalTask).contains("\"status\":\"4\"");

        ResultActions listResult = asAccountant(HttpMethod.GET, "/system/contract/list", null);
        assertListSuccess(listResult);
        Long newContractId = getIdFromList(listResult, "contractId");

        String newContractDetail = getResponseJson(asAccountant(HttpMethod.GET, "/system/contract/" + newContractId, null));
        assertThat(newContractDetail).contains("\"parentId\":" + contractId);
        assertThat(newContractDetail).contains("\"amount\":140000.0");
        assertThat(newContractDetail).contains("\"auditStatus\":\"0\"");
    }

    @Test
    @Order(17)
    @DisplayName("会计完整终止流程")
    void testAccountantFullTerminationFlow() throws Exception {
        Long customerId = createCustomerAsManager("会计测试客户-Q");
        Long contractId = createContractAsManager(customerId, "会计测试合同-Q", "1", 150000.00);
        Long taskId = createTaskAsManager(contractId, "1", USER_ID_ZHANGSAN);

        Map<String, Object> terminationRequest = new LinkedHashMap<>();
        terminationRequest.put("taskId", taskId);
        terminationRequest.put("taskType", "3");
        terminationRequest.put("remark", "客户要求终止");
        terminationRequest.put("contractId", contractId);
        terminationRequest.put("assignedTo", USER_ID_ZHANGSAN);

        assertSuccess(asAccountant(HttpMethod.POST, "/system/task/requestTermination", terminationRequest));

        String pendingRequest = getResponseJson(asManager(HttpMethod.GET, "/system/task/pendingList", null));
        assertThat(pendingRequest).contains("\"taskId\":" + taskId);

        Map<String, Object> confirmTermination = new LinkedHashMap<>();
        confirmTermination.put("taskId", taskId);
        confirmTermination.put("approved", true);

        assertSuccess(asManager(HttpMethod.POST, "/system/task/confirmTermination", confirmTermination));

        String finalTask = getResponseJson(asManager(HttpMethod.GET, "/system/task/" + taskId, null));
        assertThat(finalTask).contains("\"status\":\"4\"");

        String finalContract = getResponseJson(asAccountant(HttpMethod.GET, "/system/contract/" + contractId, null));
        assertThat(finalContract).contains("\"reminderStatus\":\"3\"");
    }

    private Long createCustomerAsManager(String name) throws Exception {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("customerName", name);
        customer.put("customerType", "1");
        customer.put("contactPerson", "经理联系人");
        customer.put("contactPhone", "13900000000");
        customer.put("ownerId", USER_ID_MANAGER);
        assertSuccess(asManager(HttpMethod.POST, "/system/customer", customer));
        ResultActions listResult = asManager(HttpMethod.GET, "/system/customer/list", null);
        assertListSuccess(listResult);
        Long customerId = getIdFromList(listResult, "customerId");
        assertThat(customerId).isNotNull();
        return customerId;
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

    private Long createTaskAsManager(Long contractId, String taskType, Long assignedTo) throws Exception {
        Map<String, Object> task = new LinkedHashMap<>();
        task.put("taskTitle", "测试任务-" + taskType);
        task.put("taskType", taskType);
        task.put("contractId", contractId);
        task.put("assignedTo", assignedTo);
        task.put("priority", "2");
        task.put("originalAmount", 10000.00);
        task.put("currentAmount", 10000.00);
        task.put("status", "0");
        task.put("remark", "自动化测试任务");
        assertSuccess(asManager(HttpMethod.POST, "/system/task", task));
        ResultActions listResult = asManager(HttpMethod.GET, "/system/task/list", null);
        assertListSuccess(listResult);
        Long taskId = getIdFromList(listResult, "taskId");
        assertThat(taskId).isNotNull();
        return taskId;
    }
}