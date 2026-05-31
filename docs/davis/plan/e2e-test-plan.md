# CMS 业务端到端测试设计

**日期**：2026-05-31
**状态**：待实现

---

## 1. 背景

项目当前零测试基础设施（pom.xml 无任何测试依赖，无测试类）。需要搭建测试框架并编写覆盖合同、任务、客户三大模块的端到端 API 层集成测试。

## 2. 测试目标

- 覆盖合同业务（CRUD + 审批流程）
- 覆盖任务管理（催收、续费、终止三条核心链路）
- 覆盖客户管理（CRUD 生命周期）
- 验证跨模块联动（任务更新合同状态、合同关联客户等）
- 验证角色数据隔离（manager 全量可见、sales 只看到自己创建的合同、account 只看到分配给自己的任务）

## 3. 测试层次与策略

**API 层集成测试**——Spring Boot Test + MockMvc + H2 内存数据库。
- 启动完整 Spring 上下文，通过 HTTP 调用 Controller
- 验证真实 API 行为、数据库读写、事务回滚
- 不涉及浏览器 UI

## 4. 技术选型

| 组件 | 选型 | 说明 |
|------|------|------|
| 测试框架 | JUnit 5 | Spring Boot 2.5.15 默认 |
| 断言 | AssertJ | 流式断言，可读性好 |
| Mock | MockMvc | Spring 内置，模拟 HTTP 请求 |
| 数据库 | H2 (MODE=MySQL) | 内存数据库，测试隔离，启动快 |
| 认证 | Spring Security Test `@WithMockUser` | 模拟已登录用户+角色，权限校验完整执行 |
| 事务 | `@Transactional` | 每个测试方法自动回滚 |

## 5. 角色与测试用户

根据项目角色体系，测试用户配置如下：

| 用户名 | 角色 | 用途 |
|--------|------|------|
| `manager` | manager（业务管理员） | **主测试角色**——全部业务数据可见，可审批、派发。用于列表查询、审批、派发操作 |
| `zhangsan` | account（会计） | 任务执行者——只能看到分配给自己的任务。用于退回讲价、确认收款、完成续签、发起终止 |
| `lisi` | sales（销售） | 合同归属者——只能看到自己创建的合同。用于数据隔离验证 |

测试以 **manager** 为主视角，**account** 和 **sales** 用于验证数据权限隔离。

## 6. 测试基础设施

### 6.1 依赖（`ruoyi-admin/pom.xml`）

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

### 6.2 测试配置（`application-test.yml`）

- 数据源切换为 H2（`jdbc:h2:mem:davis-test;MODE=MySQL;DB_CLOSE_DELAY=-1`）
- 关闭 Redis（`spring.redis.enabled=false`）
- 初始化 SQL：`schema-h2.sql`（建表）+ `data-init.sql`（基础数据）
- 排除 Quartz 自动配置

### 6.3 基础数据（`data-init.sql`）

- 系统字典（`sys_dict_data`、`sys_dict_type`）
- 部门（`sys_dept`）
- 角色（`sys_role`）：manager、account、sales
- 用户（`sys_user`）：manager、zhangsan（account）、lisi（sales）
- 角色-用户关联（`sys_user_role`）
- 菜单/权限（`sys_menu`，CMS 业务模块部分）

### 6.4 测试基类（`BaseControllerTest`）

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Sql(scripts = {"classpath:sql/schema-h2.sql", "classpath:sql/data-init.sql"},
     executionPhase = BEFORE_TEST_CLASS)
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    // 以 manager 身份操作（主测试角色）
    protected ResultActions asManager(...);

    // 以 account 身份操作（数据隔离验证）
    protected ResultActions asAccountant(String username, ...);

    // 以 sales 身份操作（数据隔离验证）
    protected ResultActions asSales(String username, ...);

    // 工具方法：断言成功、断言失败、JSON 解析等
    protected void assertSuccess(ResultActions result);
    protected void assertError(ResultActions result, String expectedMsg);
    protected <T> T parseResponse(ResultActions result, Class<T> clazz);
}
```

- `asManager()`：用 `@WithMockUser(username = "manager", roles = {"manager"})`
- `asAccountant(username)`：指定会计用户名，如 `"zhangsan"`
- `asSales(username)`：指定销售用户名，如 `"lisi"`

## 7. 测试流程（6 条业务链路）

### 流程 1：客户全生命周期

**执行角色**：manager

| 步骤 | 操作 | 验证点 |
|------|------|--------|
| 1 | POST `/system/customer` 新增客户 | 返回成功，`customerId` 不为空 |
| 2 | GET `/system/customer/list` 查询列表 | 列表包含新建客户，`customerName` 匹配 |
| 3 | GET `/system/customer/detail/{id}` 查看详情 | 返回客户信息+关联合同列表（此时为空） |
| 4 | PUT `/system/customer` 修改客户名称 | 返回成功 |
| 5 | GET `/system/customer/{id}` 再次查询 | 客户名称已更新 |
| 6 | DELETE `/system/customer/{id}` 软删除 | 返回成功 |
| 7 | GET `/system/customer/list` 查询列表 | 列表中不包含已删除客户 |
| 8 | GET `/system/customer/{id}` 按 ID 查询 | 返回失败（已逻辑删除，查不到） |

### 流程 2：合同审批流程

**执行角色**：sales（创建）+ manager（审批）

| 步骤 | 操作 | 验证点 |
|------|------|--------|
| 1 | POST `/system/customer` 以 sales 身份创建客户 | 客户创建成功 |
| 2 | POST `/system/contract` 以 sales 身份创建合同（关联客户） | 合同创建成功，`auditStatus` = `"0"`，自动生成 `contractCode` |
| 3 | GET `/system/contract/list` 以 manager 身份查看 | 列表中包含新合同，`auditStatus` = `"0"` |
| 4 | POST `/system/contract/audit` 以 manager 身份审批通过（`auditStatus="1"`） | 返回成功 |
| 5 | GET `/system/contract/{id}` 查询详情 | `auditStatus` = `"1"` |
| 6 | POST `/system/contract` 以 sales 身份创建第二个合同 | 合同创建成功 |
| 7 | POST `/system/contract/audit` 以 manager 身份审批驳回（`auditStatus="2"`） | 返回成功 |
| 8 | GET `/system/contract/{id}` 查询详情 | `auditStatus` = `"2"` |
| 9 | PUT `/system/contract` 以 sales 身份修改驳回的合同 | 修改成功（驳回后可重新编辑） |
| 10 | POST `/system/contract/audit` 以 manager 身份再次审批通过 | 通过 |

### 流程 3：合同管理流程

**执行角色**：manager

| 步骤 | 操作 | 验证点 |
|------|------|--------|
| 1 | 准备：新增客户 + 新增合同（审批通过） | 基础数据就绪 |
| 2 | GET `/system/contract/list` 分页查询 | 分页数据正确 |
| 3 | GET `/system/contract/list?contractType=1` 按类型筛选 | 只返回指定类型合同 |
| 4 | PUT `/system/contract` 修改合同金额 | 修改成功 |
| 5 | GET `/system/contract/{id}` 查询详情 | 金额已更新，附件列表正确 |
| 6 | DELETE `/system/contract/{id}` 软删除 | 返回成功 |
| 7 | GET `/system/contract/list` 查询列表 | 已删除合同不在列表中 |
| 8 | GET `/system/contract/{id}` 按 ID 查询 | 返回失败（已逻辑删除） |
| 9 | GET `/system/customer/detail/{customerId}` 查客户详情 | 客户关联的合同列表不含已删除合同 |

### 流程 4：催收任务全流程（核心链路）

**执行角色**：manager（派发 + 审批）+ account/zhangsan（执行）

| 步骤 | 操作 | 验证点 |
|------|------|--------|
| 1 | 准备：以 sales 身份新增客户 → 新增合同 → manager 审批通过 | 合同 `auditStatus="1"`，`reminderStatus=null` |
| 2 | POST `/system/task` 以 manager 身份创建催收任务（关联合同，`taskType="1"`，分配 `assignedTo=zhangsan`） | 任务创建成功 |
| 3 | GET `/system/contract/{id}` 查合同 | 合同 `reminderStatus` 变为 `"1"`（催收中） |
| 4 | GET `/system/task/list` 以 account/zhangsan 身份查任务 | 列表中包含该任务 |
| 5 | GET `/system/task/list` 以另一个会计身份查任务 | 列表中不包含该任务（数据隔离） |
| 6 | PUT `/system/task` 以 account/zhangsan 身份将状态改为 `"1"`（进行中） | 修改成功 |
| 7 | POST `/system/task/returnToAdmin` 以 account/zhangsan 身份退回讲价（含协商金额） | 任务状态变为 `"2"`（待审批），`currentAmount` 更新为协商金额 |
| 8 | GET `/system/task/pendingList` 以 manager 身份查待审批列表 | 列表中包含该任务 |
| 9 | POST `/system/task/redispatch` 以 manager 身份重新派发（同意协商价格） | 任务状态变回 `"0"`（待处理） |
| 10 | POST `/system/task/rejectPrice` 以 manager 身份拒绝协商价格 | 任务状态变回 `"0"`，会计重新协商 |
| 11 | POST `/system/task/returnToAdmin` 以 account/zhangsan 身份再次退回 | 状态 `"2"` |
| 12 | POST `/system/task/redispatch` 以 manager 身份重新派发 | 状态 `"0"` |
| 13 | POST `/system/task/confirmPayment` 以 account/zhangsan 身份确认收款（含 `actualAmount`） | 任务状态 `"4"`（已完成） |
| 14 | GET `/system/contract/{id}` 查合同 | 合同 `reminderStatus` 变为 `"3"`（已完成），`actualAmount` 更新 |
| 15 | GET `/system/task/log` 查操作日志 | 日志记录完整（创建→退回→派发→确认收款） |

### 流程 5：续费任务流程

**执行角色**：manager（派发）+ account/zhangsan（执行）

| 步骤 | 操作 | 验证点 |
|------|------|--------|
| 1 | 准备：新增客户 + 新增合同 + 审批通过 | 原合同就绪 |
| 2 | POST `/system/task` 以 manager 身份创建续费任务（`taskType="2"`，分配 `assignedTo=zhangsan`） | 任务创建成功 |
| 3 | POST `/system/task/completeRenewal` 以 account/zhangsan 身份完成续签 | `generateContract=true`，任务状态 `"4"` |
| 4 | GET `/system/contract/list` 查合同列表 | 新合同已创建，`parentId` 指向原合同 |
| 5 | GET `/system/contract/{newContractId}` 查新合同 | 新合同 `auditStatus="0"`（待审批），基本字段从原合同复制 |
| 6 | GET `/system/task/{taskId}` 查任务 | 任务 `targetContractId` 指向新合同 |

### 流程 6：终止合作流程

**执行角色**：manager（派发）+ account/zhangsan（发起 + 执行）

| 步骤 | 操作 | 验证点 |
|------|------|--------|
| 1 | 准备：新增客户 + 新增合同 + 创建催收任务（assignedTo=zhangsan） | 任务 `status="0"`，`taskType="1"` |
| 2 | POST `/system/task/requestTermination` 以 account/zhangsan 身份发起终止 | 任务 `taskType` 变为 `"3"`，`status` 变为 `"2"`（待审批） |
| 3 | GET `/system/task/pendingList` 以 manager 身份查待审批列表 | 列表中包含该终止请求 |
| 4 | POST `/system/task/confirmTermination` 以 manager 身份确认终止（`approved=true`） | 任务 `status` 变为 `"4"`（已完成） |
| 5 | GET `/system/contract/{id}` 查合同 | 合同 `reminderStatus` 变为 `"3"`（已完成） |
| 6 | （备选）重复步骤 1-2，然后 `confirmTermination` 拒绝（`approved=false`） | 任务 `status` 变为 `"3"`（已退回） |

## 8. SQL 兼容性处理

H2 (MODE=MySQL) 与真实 MySQL 的差异需处理：

| MySQL 语法 | H2 替代 | 处理方式 |
|-----------|---------|---------|
| `IFNULL(a, b)` | `COALESCE(a, b)` | 新建 `schema-h2.sql`，将 Mapper XML 中的 `IFNULL` 统一替换为 `COALESCE`（H2 也支持） |
| `AUTO_INCREMENT` | `AUTO_INCREMENT` | 兼容，无需处理 |
| `date_format()` | `FORMATDATETIME()` | 测试中避免使用此函数，或用 Java 计算日期替代 |
| `JSON` 类型 | `CLOB` | H2 不支持 JSON，`content_snapshot` 列建为 `CLOB` |
| `text` 类型 | `CLOB` | H2 无 `text`，用 `CLOB` 替代 |
| `datetime` | `TIMESTAMP` | 映射兼容 |
| `engine=innodb` | 无 | 去掉建表语句中的 `engine=innodb` |

## 9. 文件结构

```
ruoyi-admin/
├── pom.xml                                          # 添加测试依赖
└── src/
    └── test/
        ├── java/com/ruoyi/web/controller/davis/
        │   ├── BaseControllerTest.java              # 测试基类
        │   ├── CustomerLifecycleFlowTest.java        # 流程1：客户全生命周期
        │   ├── ContractApprovalFlowTest.java         # 流程2：合同审批流程
        │   ├── ContractManagementFlowTest.java       # 流程3：合同管理流程
        │   ├── CollectionTaskFlowTest.java           # 流程4：催收任务全流程
        │   ├── RenewalTaskFlowTest.java              # 流程5：续费任务流程
        │   └── TerminationFlowTest.java              # 流程6：终止合作流程
        └── resources/
            ├── application-test.yml                  # 测试 profile 配置
            ├── sql/
            │   ├── schema-h2.sql                     # H2 兼容建表语句
            │   └── data-init.sql                     # 基础数据（用户、角色、字典、菜单）
            └── logback-test.xml                      # 测试日志
```

## 10. 不覆盖的范围

以下场景不在本次 E2E 测试范围，后续单独补充：
- Excel 导入导出（需要文件上传/下载测试工具）
- Dashboard 统计查询（归属单元测试）
- 通知推送（`CmsNotification`，目前无删除操作，仅可读标记）
- 附件上传/下载（`CmsFile`，跟随合同软删除）
- 令牌过期/刷新场景
- 并发场景（如同一任务两人同时操作）
- 权限枚举穷举（每个 `@PreAuthorize` 标识单独验证）
