package com.ruoyi.web.controller.davis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import org.springframework.http.HttpMethod;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * E2E: 客户全生命周期流程。
 *
 * 新增客户 → 查询列表 → 查看详情 → 修改客户 → 软删除 → 验证删除后不可见。
 * 执行角色：manager（业务管理员）
 */
@DisplayName("E2E: 客户全生命周期流程")
public class CustomerLifecycleFlowTest extends BaseControllerTest {

    @Test
    @Order(1)
    @DisplayName("新增客户 → 查询列表 → 修改客户 → 软删除 → 确认不可见")
    void testCustomerLifecycle() throws Exception {
        final String customerName = "测试科技有限公司";

        // 1. 新增客户
        Map<String, Object> newCustomer = new LinkedHashMap<>();
        newCustomer.put("customerName", customerName);
        newCustomer.put("customerType", "1");
        newCustomer.put("contactPerson", "王经理");
        newCustomer.put("contactPhone", "13900000001");
        newCustomer.put("contactEmail", "test@tech.com");
        newCustomer.put("address", "深圳市南山区科技园");
        newCustomer.put("ownerId", 4L);
        assertSuccess(asManager(HttpMethod.POST, "/system/customer", newCustomer));

        // 2. 查询列表获取 ID（POST 返回 toAjax(int)，不含实体 ID）
        ResultActions listResult = asManager(HttpMethod.GET, "/system/customer/list", null);
        assertSuccess(listResult);
        assertThat(getResponseJson(listResult)).contains(customerName);
        Long customerId = getIdFromList(listResult, "customerId");
        assertThat(customerId).isNotNull();

        // 3. 查看详情
        assertSuccess(asManager(HttpMethod.GET, "/system/customer/detail/" + customerId, null));
        assertThat(getResponseJson(asManager(HttpMethod.GET, "/system/customer/detail/" + customerId, null)))
            .contains(customerName);

        // 4. 修改客户
        Map<String, Object> updateCustomer = new LinkedHashMap<>();
        updateCustomer.put("customerId", customerId);
        updateCustomer.put("customerName", "测试科技有限公司-改名");
        updateCustomer.put("customerType", "1");
        updateCustomer.put("contactPerson", "张总");
        updateCustomer.put("contactPhone", "13900000002");
        updateCustomer.put("ownerId", 4L);
        assertSuccess(asManager(HttpMethod.PUT, "/system/customer", updateCustomer));

        // 5. 验证已更新
        String checkJson = getResponseJson(asManager(HttpMethod.GET, "/system/customer/" + customerId, null));
        assertThat(checkJson).contains("测试科技有限公司-改名");
        assertThat(checkJson).contains("张总");

        // 6. 软删除
        assertSuccess(asManager(HttpMethod.DELETE, "/system/customer/" + customerId, null));

        // 7. 列表不显示已删除（检查改名后的名称）
        assertThat(getResponseJson(asManager(HttpMethod.GET, "/system/customer/list", null)))
            .doesNotContain("测试科技有限公司-改名");

        // 8. 按 ID 查不到（result 为 AjaxResult.success(null)，data=null）
        String deletedJson = getResponseJson(asManager(HttpMethod.GET, "/system/customer/" + customerId, null));
        Map<String, Object> deletedResp = objectMapper.readValue(deletedJson, Map.class);
        assertThat(deletedResp.get("data")).isNull();
    }
}
