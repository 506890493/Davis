package com.ruoyi.web.controller.davis;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.system.service.ISysDictTypeService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CMS E2E 测试基类。
 *
 * 提供 MockMvc 实例、三种角色的认证后处理器、JSON 断言工具。
 * 每个测试方法执行后自动回滚事务（@Transactional）。
 * 类级别初始化建表+基础数据（@Sql BEFORE_TEST_METHOD）。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Sql(scripts = {
    "classpath:sql/data-init.sql"
})
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private RedisCache redisCache;

    @Autowired
    private ISysDictTypeService sysDictTypeService;

    private final Map<String, Object> cacheMap = new ConcurrentHashMap<>();

    @BeforeEach
    void setUpMockRedis() {
        // 使用 Mockito doAnswer 使 setCacheObject 实际存储到 HashMap
        Mockito.doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Object value = invocation.getArgument(1);
            cacheMap.put(key, value);
            return null;
        }).when(redisCache).setCacheObject(Mockito.anyString(), Mockito.any());

        Mockito.doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Object value = invocation.getArgument(1);
            Integer timeout = invocation.getArgument(2);
            TimeUnit timeUnit = invocation.getArgument(3);
            cacheMap.put(key, value);
            return null;
        }).when(redisCache).setCacheObject(Mockito.anyString(), Mockito.any(), Mockito.anyInt(), Mockito.any(TimeUnit.class));

        // getCacheObject 从 HashMap 读取
        Mockito.when(redisCache.getCacheObject(Mockito.anyString()))
            .thenAnswer(invocation -> {
                Object value = cacheMap.get(invocation.getArgument(0));
                // DictUtils stores List but retrieves as JSONArray (Redis JSON serializer).
                // Simulate the ser/deser so getDictCache gets the expected type.
                if (value instanceof List) {
                    return new JSONArray((List<?>) value);
                }
                return value;
            });

        // deleteObject 从 HashMap 删除
        Mockito.when(redisCache.deleteObject(Mockito.anyString()))
            .thenAnswer(invocation -> cacheMap.remove(invocation.getArgument(0)) != null);

        // 重新从数据库加载字典缓存（@Sql 已在此时执行完毕）
        sysDictTypeService.resetDictCache();
    }

    // 测试用户 ID 常量
    protected static final Long USER_ID_ADMIN = 1L;
    protected static final Long USER_ID_MANAGER = 2L;
    protected static final Long USER_ID_ZHANGSAN = 3L;
    protected static final Long USER_ID_LISI = 4L;

    protected static final String USERNAME_ADMIN = "admin";
    protected static final String USERNAME_MANAGER = "manager";
    protected static final String USERNAME_ZHANGSAN = "zhangsan";
    protected static final String USERNAME_LISI = "lisi";

    protected static SysUser adminUser;
    protected static SysUser managerUser;
    protected static SysUser zhangsanUser;
    protected static SysUser lisiUser;

    @BeforeAll
    static void setupUsers() {
        adminUser = new SysUser();
        adminUser.setUserId(USER_ID_ADMIN);
        adminUser.setUserName(USERNAME_ADMIN);
        adminUser.setNickName("管理员");
        adminUser.setDeptId(1L);

        managerUser = new SysUser();
        managerUser.setUserId(USER_ID_MANAGER);
        managerUser.setUserName(USERNAME_MANAGER);
        managerUser.setNickName("经理");
        managerUser.setDeptId(100L);

        zhangsanUser = new SysUser();
        zhangsanUser.setUserId(USER_ID_ZHANGSAN);
        zhangsanUser.setUserName(USERNAME_ZHANGSAN);
        zhangsanUser.setNickName("张三");
        zhangsanUser.setDeptId(102L);

        lisiUser = new SysUser();
        lisiUser.setUserId(USER_ID_LISI);
        lisiUser.setUserName(USERNAME_LISI);
        lisiUser.setNickName("李四");
        lisiUser.setDeptId(101L);
    }

    /**
     * 以 admin（系统管理员）身份执行 MockMvc 请求。
     */
    protected ResultActions asAdmin(HttpMethod method, String url, Object body) throws Exception {
        return performRequest(method, url, body, USERNAME_ADMIN, USER_ID_ADMIN, "admin");
    }

    /**
     * 以 manager（业务管理员）身份执行 MockMvc 请求。
     */
    protected ResultActions asManager(HttpMethod method, String url, Object body) throws Exception {
        return performRequest(method, url, body, USERNAME_MANAGER, USER_ID_MANAGER, "manager");
    }

    /**
     * 以 account（会计 / zhangsan）身份执行 MockMvc 请求。
     */
    protected ResultActions asAccountant(HttpMethod method, String url, Object body) throws Exception {
        return performRequest(method, url, body, USERNAME_ZHANGSAN, USER_ID_ZHANGSAN, "account");
    }

    /**
     * 以 sales（销售 / lisi）身份执行 MockMvc 请求。
     */
    protected ResultActions asSales(HttpMethod method, String url, Object body) throws Exception {
        return performRequest(method, url, body, USERNAME_LISI, USER_ID_LISI, "sales");
    }

    private ResultActions performRequest(HttpMethod method, String url, Object body,
                                          String username, Long userId, String roleKey) throws Exception {
        // 创建完整的 LoginUser 对象
        SysUser sysUser = getSysUser(username);
        LoginUser loginUser = new LoginUser(sysUser, getPermissionsForRole(roleKey));
        loginUser.setUserId(userId);
        loginUser.setDeptId(sysUser.getDeptId());
        
        // 设置完整的认证信息
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        
        // 确保SecurityContext正确设置
        SecurityContextHolder.getContext().setAuthentication(auth);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
            .request(method, url)
            .with(SecurityMockMvcRequestPostProcessors.authentication(auth))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);

        if (body != null) {
            String json = objectMapper.writeValueAsString(body);
            requestBuilder.content(json);
        }

        return mockMvc.perform(requestBuilder);
    }

    private SysUser getSysUser(String username) {
        if (USERNAME_ADMIN.equals(username)) return adminUser;
        if (USERNAME_MANAGER.equals(username)) return managerUser;
        if (USERNAME_ZHANGSAN.equals(username)) return zhangsanUser;
        if (USERNAME_LISI.equals(username)) return lisiUser;
        return null;
    }

    private Set<String> getPermissionsForRole(String roleKey) {
        Set<String> perms = new HashSet<>();
        switch (roleKey) {
            case "admin":
                // 知识库（admin：含物理删除的全量权限）
                perms.add("kb:portal:view");
                perms.add("kb:portal:required");
                perms.add("kb:category:list");
                perms.add("kb:category:query");
                perms.add("kb:category:add");
                perms.add("kb:category:edit");
                perms.add("kb:category:remove");
                perms.add("kb:document:list");
                perms.add("kb:document:query");
                perms.add("kb:document:add");
                perms.add("kb:document:edit");
                perms.add("kb:document:remove");
                perms.add("kb:document:publish");
                perms.add("kb:version:list");
                perms.add("kb:version:rollback");
                perms.add("kb:recycle:list");
                perms.add("kb:recycle:restore");
                perms.add("kb:recycle:purge");
                perms.add("kb:file:upload");
                perms.add("kb:file:download");
                break;
            case "manager":
                perms.add("system:contract:list");
                perms.add("system:contract:query");
                perms.add("system:contract:add");
                perms.add("system:contract:edit");
                perms.add("system:contract:remove");
                perms.add("system:contract:import");
                perms.add("system:contract:export");
                perms.add("cms:contract:audit");
                perms.add("system:customer:list");
                perms.add("system:customer:query");
                perms.add("system:customer:add");
                perms.add("system:customer:edit");
                perms.add("system:customer:remove");
                perms.add("system:customer:export");
                perms.add("system:task:list");
                perms.add("system:task:query");
                perms.add("system:task:add");
                perms.add("system:task:edit");
                perms.add("system:task:remove");
                perms.add("system:task:export");
                perms.add("cms:task:dispatch");
                perms.add("cms:task:audit");
                // 知识库（manager：除 purge 之外的全部）
                perms.add("kb:portal:view");
                perms.add("kb:portal:required");
                perms.add("kb:category:list");
                perms.add("kb:category:query");
                perms.add("kb:category:add");
                perms.add("kb:category:edit");
                perms.add("kb:category:remove");
                perms.add("kb:document:list");
                perms.add("kb:document:query");
                perms.add("kb:document:add");
                perms.add("kb:document:edit");
                perms.add("kb:document:remove");
                perms.add("kb:document:publish");
                perms.add("kb:version:list");
                perms.add("kb:version:rollback");
                perms.add("kb:recycle:list");
                perms.add("kb:recycle:restore");
                perms.add("kb:file:upload");
                perms.add("kb:file:download");
                break;
            case "account":
                perms.add("system:task:list");
                perms.add("system:task:query");
                perms.add("system:task:export");
                perms.add("cms:task:edit");
                perms.add("system:contract:query");
                perms.add("system:contract:list");
                perms.add("system:task:log");
                // 知识库（account：仅读 + 下载 + 上传）
                perms.add("kb:portal:view");
                perms.add("kb:portal:required");
                perms.add("kb:file:upload");
                perms.add("kb:file:download");
                break;
            case "sales":
                perms.add("system:contract:list");
                perms.add("system:contract:query");
                perms.add("system:contract:add");
                perms.add("system:contract:edit");
                perms.add("system:contract:remove");
                perms.add("system:contract:export");
                perms.add("system:customer:list");
                perms.add("system:customer:query");
                perms.add("system:customer:add");
                perms.add("system:customer:edit");
                perms.add("system:customer:remove");
                perms.add("system:customer:export");
                // 知识库（sales：仅读 + 下载 + 上传）
                perms.add("kb:portal:view");
                perms.add("kb:portal:required");
                perms.add("kb:file:upload");
                perms.add("kb:file:download");
                break;
        }
        return perms;
    }

    // ========== 断言工具 ==========

    protected void assertSuccess(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.code").value(200))
              .andExpect(jsonPath("$.msg").value("操作成功"));
    }

    protected void assertError(ResultActions result, String expectedMsg) throws Exception {
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.not(200)));
    }

    /**
     * 断言列表查询成功（TableDataInfo 格式，msg="查询成功"）。
     * 用于 Contract/Task 等返回 TableDataInfo 而非 AjaxResult 的端点。
     */
    protected void assertListSuccess(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.code").value(200));
    }

    public String getResponseJson(ResultActions result) throws Exception {
        return result.andReturn().getResponse().getContentAsString();
    }

    /**
     * 从列表查询响应中提取第一条匹配记录的 ID。
     * 支持两种格式：
     *   1. AjaxResult > data > rows（如 CmsCustomerController）
     *   2. TableDataInfo > rows 直接（如 CmsContractController、CmsTaskController）
     */
    @SuppressWarnings("unchecked")
    public Long getIdFromList(ResultActions listResult, String idField) throws Exception {
        String json = getResponseJson(listResult);
        Map<String, Object> resp = objectMapper.readValue(json, Map.class);
        // 尝试从 data.rows 提取（AjaxResult 包装格式）
        Object dataObj = resp.get("data");
        if (dataObj instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) dataObj;
            Object rowsObj = dataMap.get("rows");
            if (rowsObj instanceof List && !((List) rowsObj).isEmpty()) {
                List<Map<String, Object>> rows = (List<Map<String, Object>>) rowsObj;
                Object id = rows.get(0).get(idField);
                return id instanceof Number ? ((Number) id).longValue() : null;
            }
        }
        // 尝试从 rows 直接提取（TableDataInfo 直接返回格式）
        Object rowsObj = resp.get("rows");
        if (rowsObj instanceof List && !((List) rowsObj).isEmpty()) {
            List<Map<String, Object>> rows = (List<Map<String, Object>>) rowsObj;
            Object id = rows.get(0).get(idField);
            return id instanceof Number ? ((Number) id).longValue() : null;
        }
        return null;
    }

    /**
     * 检查响应 data 字段是否为 int（toAjax 返回值）。
     */
    @SuppressWarnings("unchecked")
    protected boolean isIntData(ResultActions result) throws Exception {
        String json = getResponseJson(result);
        Map<String, Object> resp = objectMapper.readValue(json, Map.class);
        return resp.get("data") instanceof Integer;
    }
}
