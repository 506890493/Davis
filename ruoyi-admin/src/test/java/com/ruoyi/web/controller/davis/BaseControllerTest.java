package com.ruoyi.web.controller.davis;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * CMS E2E 测试基类。
 *
 * 提供 MockMvc 实例、三种角色的认证后处理器、JSON 断言工具。
 * 每个测试方法执行后自动回滚事务（@Transactional）。
 * 类级别初始化建表+基础数据（@Sql BEFORE_TEST_CLASS）。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Sql(scripts = {
    "classpath:sql/schema-h2.sql",
    "classpath:sql/data-init.sql"
}, executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // 测试用户 ID 常量
    protected static final Long USER_ID_MANAGER = 2L;
    protected static final Long USER_ID_ZHANGSAN = 3L;
    protected static final Long USER_ID_LISI = 4L;

    protected static final String USERNAME_MANAGER = "manager";
    protected static final String USERNAME_ZHANGSAN = "zhangsan";
    protected static final String USERNAME_LISI = "lisi";

    protected static SysUser managerUser;
    protected static SysUser zhangsanUser;
    protected static SysUser lisiUser;

    @BeforeAll
    static void setupUsers() {
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
     * 以 manager（业务管理员）身份执行 MockMvc 请求。
     */
    protected ResultActions asManager(String method, String url, Object body) throws Exception {
        return performRequest(method, url, body, USERNAME_MANAGER, USER_ID_MANAGER, "manager");
    }

    /**
     * 以 account（会计 / zhangsan）身份执行 MockMvc 请求。
     */
    protected ResultActions asAccountant(String method, String url, Object body) throws Exception {
        return performRequest(method, url, body, USERNAME_ZHANGSAN, USER_ID_ZHANGSAN, "account");
    }

    /**
     * 以 sales（销售 / lisi）身份执行 MockMvc 请求。
     */
    protected ResultActions asSales(String method, String url, Object body) throws Exception {
        return performRequest(method, url, body, USERNAME_LISI, USER_ID_LISI, "sales");
    }

    private ResultActions performRequest(String method, String url, Object body,
                                          String username, Long userId, String roleKey) throws Exception {
        LoginUserForTest loginUser = new LoginUserForTest();
        loginUser.setUserId(userId);
        loginUser.setUsername(username);
        loginUser.setUser(getSysUser(username));
        loginUser.setPermissions(getPermissionsForRole(roleKey));

        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());

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
        if (USERNAME_MANAGER.equals(username)) return managerUser;
        if (USERNAME_ZHANGSAN.equals(username)) return zhangsanUser;
        if (USERNAME_LISI.equals(username)) return lisiUser;
        return null;
    }

    private Set<String> getPermissionsForRole(String roleKey) {
        Set<String> perms = new HashSet<>();
        switch (roleKey) {
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
                perms.add("system:task:list");
                perms.add("system:task:query");
                perms.add("system:task:add");
                perms.add("system:task:edit");
                perms.add("system:task:remove");
                perms.add("system:task:export");
                perms.add("cms:task:dispatch");
                perms.add("cms:task:audit");
                break;
            case "account":
                perms.add("system:task:list");
                perms.add("system:task:query");
                perms.add("system:task:add");
                perms.add("system:task:edit");
                perms.add("system:task:remove");
                perms.add("system:task:export");
                break;
            case "sales":
                perms.add("system:contract:list");
                perms.add("system:contract:query");
                perms.add("system:contract:add");
                perms.add("system:contract:edit");
                perms.add("system:contract:remove");
                perms.add("system:customer:list");
                perms.add("system:customer:query");
                perms.add("system:customer:add");
                perms.add("system:customer:edit");
                perms.add("system:customer:remove");
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

    protected String getResponseJson(ResultActions result) throws Exception {
        return result.andReturn().getResponse().getContentAsString();
    }

    // ========== 内部类：简化的 LoginUser ==========

    public static class LoginUserForTest implements org.springframework.security.core.userdetails.UserDetails {
        private Long userId;
        private String username;
        private SysUser user;
        private Set<String> permissions;
        private String password;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        @Override public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public SysUser getUser() { return user; }
        public void setUser(SysUser user) { this.user = user; }
        public Set<String> getPermissions() { return permissions; }
        public void setPermissions(Set<String> permissions) { this.permissions = permissions; }
        @Override public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        @Override
        public Collection<org.springframework.security.core.GrantedAuthority> getAuthorities() {
            List<org.springframework.security.core.GrantedAuthority> authorities = new ArrayList<>();
            if (permissions != null) {
                for (String perm : permissions) {
                    authorities.add(() -> perm);
                }
            }
            return authorities;
        }
        @Override public boolean isAccountNonExpired() { return true; }
        @Override public boolean isAccountNonLocked() { return true; }
        @Override public boolean isCredentialsNonExpired() { return true; }
        @Override public boolean isEnabled() { return true; }
    }
}
