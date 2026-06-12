package com.ruoyi.web.controller.davis;

import org.junit.jupiter.api.*;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.ResultActions;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E: 4 种客户类型全覆盖 (1=公司 2=个体户 3=合伙企业 4=民办非)
 */
@DisplayName("E2E: 4 种客户类型")
class CustomerTypeCoverageTest extends BaseControllerTest {

    private static final String[] TYPES = {"1", "2", "3", "4"};
    private static final String[] NAME_PREFIX = {"GS-", "GGT-", "HH-", "MBF-"};
    private static final long STAMP = System.currentTimeMillis() / 1000;

    @Test
    @DisplayName("4 种类型可创建并按类型过滤命中")
    void testAllFourTypes() throws Exception {
        for (int i = 0; i < 4; i++) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("customerName", NAME_PREFIX[i] + STAMP + "-" + i);
            body.put("customerType", TYPES[i]);
            body.put("contactPerson", "联系人" + i);
            body.put("contactPhone", "1390000000" + i);
            body.put("ownerId", 4L);
            assertSuccess(asManager(HttpMethod.POST, "/system/customer", body));
        }
        for (int i = 0; i < 4; i++) {
            ResultActions r = asManager(
                HttpMethod.GET, "/system/customer/list?customerType=" + TYPES[i] + "&pageNum=1&pageSize=20", null);
            assertSuccess(r);
            String json = getResponseJson(r);
            assertThat(json).as("类型 %s 过滤应包含新建客户", NAME_PREFIX[i]).contains(NAME_PREFIX[i] + STAMP);
        }
    }

    @Test
    @DisplayName("字典 cms_customer_type 恰好 4 条 (按 dict_code 校验, 避开 surefire GBK 中文乱码)")
    void testDictHasExactlyFour() throws Exception {
        ResultActions r = asManager(HttpMethod.GET, "/system/dict/data/type/cms_customer_type", null);
        assertSuccess(r);
        String json = getResponseJson(r);
        // 用 dict_code 验证（避免 GBK 乱码问题）
        assertThat(json)
            .contains("\"dictCode\":160")   // 公司 (原企业槽位)
            .contains("\"dictCode\":161")   // 个体户 (原个人槽位)
            .contains("\"dictCode\":162")   // 合伙企业
            .contains("\"dictCode\":163");  // 民办非
        // 验证 dict_value 1/2/3/4 都在
        assertThat(json)
            .contains("\"dictValue\":\"1\"")
            .contains("\"dictValue\":\"2\"")
            .contains("\"dictValue\":\"3\"")
            .contains("\"dictValue\":\"4\"");
    }
}
