# CMS E2E 测试实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 搭建 H2 内存数据库驱动的 API 端到端测试框架，覆盖合同、客户、任务三大模块 6 条核心业务链路

**Architecture:** Spring Boot Test + MockMvc + H2 (MODE=MySQL) 内存库。不涉及浏览器 UI。通过 MockMvc 请求后处理器（RequestPostProcessor）设置 LoginUser 安全上下文，完整执行 Spring Security 权限校验。每个测试类 `@Transactional` 自动回滚数据。

**Tech Stack:** JUnit 5, AssertJ, MockMvc, H2 1.4.200, Spring Security Test, Maven (new test dependencies in ruoyi-admin)

---

## 文件结构

```
ruoyi-admin/
├── pom.xml                                                # MODIFY: 添加 spring-boot-starter-test + h2
└── src/
    └── test/
        ├── java/com/ruoyi/web/controller/davis/
        │   ├── BaseControllerTest.java                    # CREATE: 测试基类（MockMvc、认证、断言工具）
        │   ├── CustomerLifecycleFlowTest.java             # CREATE: 流程1 客户全生命周期
        │   ├── ContractApprovalFlowTest.java              # CREATE: 流程2 合同审批流程
        │   ├── ContractManagementFlowTest.java            # CREATE: 流程3 合同管理流程  
        │   ├── CollectionTaskFlowTest.java                # CREATE: 流程4 催收任务全流程
        │   ├── RenewalTaskFlowTest.java                   # CREATE: 流程5 续费任务流程
        │   └── TerminationFlowTest.java                   # CREATE: 流程6 终止合作流程
        └── resources/
            ├── application-test.yml                       # CREATE: 测试 profile（H2 + 关Redis + 关Quartz）
            └── sql/
                ├── schema-h2.sql                          # CREATE: H2 兼容建表（davis + ruoyi 系统表）
                └── data-init.sql                          # CREATE: 基础数据（用户、角色、字典、菜单权限）
```

---

### Task 1: pom.xml 添加测试依赖

**Files:**
- Modify: `ruoyi-admin/pom.xml`（在 `<dependencies>` 末尾，`</dependencies>` 前添加）

- [ ] **Step 1: 添加 spring-boot-starter-test + h2 依赖**

在 `ruoyi-admin/pom.xml` 的 `</dependencies>` 前添加：

```xml
        <!-- 测试 -->
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

- [ ] **Step 2: 验证本地 Maven 能找到依赖**

运行：
```bash
cd D:/GitHub/ruoyi-davis && mvn dependency:resolve -pl ruoyi-admin -am
```

确认输出包含 `org.springframework.boot:spring-boot-starter-test:jar:2.5.15` 和 `com.h2database:h2:jar:1.4.200`。

- [ ] **Step 3: 提交**

```bash
git add ruoyi-admin/pom.xml
git commit -m "test: 添加 spring-boot-starter-test 和 h2 测试依赖"
```

---

### Task 2: 创建 application-test.yml

**Files:**
- Create: `ruoyi-admin/src/test/resources/application-test.yml`

- [ ] **Step 1: 写入测试配置文件**

```yaml
# 测试环境配置 — 使用 H2 内存数据库，关闭 Redis
spring:
  profiles:
    include: test-security  # 空占位，不加载 druid profile

  datasource:
    driver-class-name: org.h2.Driver
    druid:
      master:
        url: jdbc:h2:mem:davis-test;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
        username: sa
        password:
      slave:
        enabled: false
      # 最小化连接池
      initial-size: 1
      min-idle: 1
      max-active: 2
      max-wait: 60000

  # 关闭 Redis — 测试环境不需要令牌缓存
  redis:
    enabled: false

  # 禁用 flyway/liquibase（如果有）
  flyway:
    enabled: false

  # 允许重复 bean 覆盖（测试环境可能需要）
  main:
    allow-bean-definition-overriding: true

# 关闭 MyBatis 缓存，调试方便
mybatis:
  configuration:
    cache-enabled: false

# 关闭验证码
ruoyi:
  captchaEnabled: false
```

- [ ] **Step 2: 提交**

```bash
git add ruoyi-admin/src/test/resources/application-test.yml
git commit -m "test: 添加测试环境配置（H2 + 关 Redis）"
```

---

### Task 3: 创建 schema-h2.sql（H2 兼容建表）

**Files:**
- Create: `ruoyi-admin/src/test/resources/sql/schema-h2.sql`

需要包含的建表语句（从 `sql/ry_20250522.sql` + `sql/davis.sql` 提取，做 H2 适配）：

**适配规则**：
- 去掉 `engine=innodb auto_increment=N comment='...'` → H2 不支持 `engine`，注释改 `--` 行尾
- `bigint(20)` → `bigint`（H2 忽略显示宽度）
- `int(4)` → `int`
- `tinyint(1)` → `tinyint`
- `datetime` → `timestamp`（H2 MODE=MySQL 可保留 `datetime`）
- `json` 类型 → `clob`（H2 1.4.x 不支持 JSON）
- `text` → `clob`
- `varchar(500)` → OK
- `char(1)` → OK
- `comment 'xxx'` 列注释 → H2 不支持列注释，去掉（改用 `--` 行注释）

- [ ] **Step 1: 罗列需要建表的完整清单**

系统表（从 `ry_20250522.sql`）：
  - sys_dept, sys_user, sys_post, sys_role, sys_menu, sys_role_menu, sys_user_role, sys_dict_type, sys_dict_data, sys_config, sys_logininfor, sys_oper_log

CMS 业务表（从 `davis.sql` + `add_customer_table.sql` + 后续变更）：
  - cms_customer, cms_contract, cms_task, cms_approval, cms_file, cms_communication, cms_notification, cms_task_log

- [ ] **Step 2: 写 system 表建表语句（适配 H2）**

```sql
-- schema-h2.sql
-- H2 兼容建表语句 (MODE=MySQL)

-- 1. 部门表
drop table if exists sys_dept;
create table sys_dept (
  dept_id      bigint auto_increment primary key,
  parent_id    bigint default 0,
  ancestors    varchar(50) default '',
  dept_name    varchar(30) default '',
  order_num    int default 0,
  leader       varchar(20) default null,
  phone        varchar(11) default null,
  email        varchar(50) default null,
  status       char(1) default '0',
  del_flag     char(1) default '0',
  create_by    varchar(64) default '',
  create_time  timestamp null,
  update_by    varchar(64) default '',
  update_time  timestamp null
);

-- 2. 用户信息表
drop table if exists sys_user;
create table sys_user (
  user_id      bigint auto_increment primary key,
  dept_id      bigint default null,
  user_name    varchar(30) not null,
  nick_name    varchar(30) not null,
  user_type    varchar(2) default '00',
  email        varchar(50) default '',
  phonenumber  varchar(11) default '',
  sex          char(1) default '0',
  avatar       varchar(100) default '',
  password     varchar(100) default '',
  status       char(1) default '0',
  del_flag     char(1) default '0',
  login_ip     varchar(128) default '',
  login_date   timestamp null,
  pwd_update_date timestamp null,
  create_by    varchar(64) default '',
  create_time  timestamp null,
  update_by    varchar(64) default '',
  update_time  timestamp null,
  remark       varchar(500) default null
);

-- 3. 岗位表
drop table if exists sys_post;
create table sys_post (
  post_id     bigint auto_increment primary key,
  post_code   varchar(64) not null,
  post_name   varchar(50) not null,
  post_sort   int not null,
  status      char(1) not null,
  create_by   varchar(64) default '',
  create_time timestamp null,
  update_by   varchar(64) default '',
  update_time timestamp null,
  remark      varchar(500) default null
);

-- 4. 角色表
drop table if exists sys_role;
create table sys_role (
  role_id             bigint auto_increment primary key,
  role_name           varchar(30) not null,
  role_key            varchar(100) not null,
  role_sort           int not null,
  data_scope          char(1) default '1',
  menu_check_strictly tinyint default 1,
  dept_check_strictly tinyint default 1,
  status              char(1) not null,
  del_flag            char(1) default '0',
  create_by           varchar(64) default '',
  create_time         timestamp null,
  update_by           varchar(64) default '',
  update_time         timestamp null,
  remark              varchar(500) default null
);

-- 5. 菜单表
drop table if exists sys_menu;
create table sys_menu (
  menu_id     bigint auto_increment primary key,
  menu_name   varchar(50) not null,
  parent_id   bigint default 0,
  order_num   int default 0,
  path        varchar(200) default '',
  component   varchar(255) default null,
  query       varchar(255) default null,
  route_name  varchar(50) default '',
  is_frame    int default 1,
  is_cache    int default 0,
  menu_type   char(1) default '',
  visible     char(1) default '0',
  status      char(1) default '0',
  perms       varchar(100) default null,
  icon        varchar(100) default '#',
  create_by   varchar(64) default '',
  create_time timestamp null,
  update_by   varchar(64) default '',
  update_time timestamp null,
  remark      varchar(500) default null
);

-- 6. 角色菜单关联表
drop table if exists sys_role_menu;
create table sys_role_menu (
  role_id bigint not null,
  menu_id bigint not null,
  primary key (role_id, menu_id)
);

-- 7. 用户角色关联表
drop table if exists sys_user_role;
create table sys_user_role (
  user_id bigint not null,
  role_id bigint not null,
  primary key (user_id, role_id)
);

-- 8. 用户岗位关联表
drop table if exists sys_user_post;
create table sys_user_post (
  user_id bigint not null,
  post_id bigint not null,
  primary key (user_id, post_id)
);

-- 9. 字典类型表
drop table if exists sys_dict_type;
create table sys_dict_type (
  dict_id     bigint auto_increment primary key,
  dict_name   varchar(100) default '',
  dict_type   varchar(100) not null,
  status      char(1) default '0',
  create_by   varchar(64) default '',
  create_time timestamp null,
  update_by   varchar(64) default '',
  update_time timestamp null,
  remark      varchar(500) default null
);

-- 10. 字典数据表
drop table if exists sys_dict_data;
create table sys_dict_data (
  dict_code   bigint auto_increment primary key,
  dict_sort   int default 0,
  dict_label  varchar(100) default '',
  dict_value  varchar(100) default '',
  dict_type   varchar(100) default '',
  css_class   varchar(255) default null,
  list_class  varchar(255) default null,
  is_default  char(1) default 'N',
  status      char(1) default '0',
  create_by   varchar(64) default '',
  create_time timestamp null,
  update_by   varchar(64) default '',
  update_time timestamp null,
  remark      varchar(500) default null
);

-- 11. 系统配置表
drop table if exists sys_config;
create table sys_config (
  config_id    bigint auto_increment primary key,
  config_name  varchar(100) default '',
  config_key   varchar(100) default '',
  config_value varchar(500) default '',
  config_type  char(1) default 'N',
  create_by    varchar(64) default '',
  create_time  timestamp null,
  update_by    varchar(64) default '',
  update_time  timestamp null,
  remark       varchar(500) default null
);

-- 12. 操作日志表
drop table if exists sys_oper_log;
create table sys_oper_log (
  oper_id        bigint auto_increment primary key,
  title          varchar(50) default '',
  business_type  int default 0,
  method         varchar(100) default '',
  request_method varchar(10) default '',
  operator_type  int default 0,
  oper_name      varchar(50) default '',
  dept_name      varchar(50) default '',
  oper_url       varchar(255) default '',
  oper_ip        varchar(128) default '',
  oper_location  varchar(255) default '',
  oper_param     varchar(2000) default '',
  json_result    varchar(2000) default '',
  status         int default 0,
  error_msg      varchar(2000) default '',
  oper_time      timestamp null,
  cost_time      bigint default 0,
  primary key (oper_id)
);

-- 13. 登录日志表
drop table if exists sys_logininfor;
create table sys_logininfor (
  info_id        bigint auto_increment primary key,
  user_name      varchar(50) default '',
  ipaddr         varchar(128) default '',
  login_location varchar(255) default '',
  browser        varchar(50) default '',
  os             varchar(50) default '',
  status         char(1) default '0',
  msg            varchar(255) default '',
  login_time     timestamp null
);
```

- [ ] **Step 3: 写 CMS 业务表建表语句**

```sql
-- 14. 客户表
drop table if exists cms_customer;
create table cms_customer (
  customer_id    bigint auto_increment primary key,
  customer_name  varchar(100) not null,
  customer_type  varchar(10) default null,
  contact_person varchar(50) default null,
  contact_phone  varchar(20) default null,
  contact_email  varchar(100) default null,
  address        varchar(255) default null,
  remark         varchar(500) default null,
  owner_id       bigint default null,
  status         char(1) default '0',
  del_flag       char(1) default '0',
  create_by      varchar(64) default '',
  create_time    timestamp null,
  update_by      varchar(64) default '',
  update_time    timestamp null
);

-- 15. 合同管理表
drop table if exists cms_contract;
create table cms_contract (
  contract_id        bigint auto_increment primary key,
  contract_code      varchar(64) default null,
  contract_name      varchar(100) not null,
  contract_type      char(1) not null,
  legal_person       varchar(50) default null,
  contact_person     varchar(50) default null,
  contact_phone      varchar(20) default null,
  contact_email      varchar(100) default null,
  amount             decimal(10,2) default 0.00,
  actual_amount      decimal(12,2) default null,
  payment_cycle      char(1) default null,
  payment_date       timestamp null,
  payment_method     char(1) default null,
  start_date         timestamp null,
  end_date           timestamp null,
  tax_type           char(1) default null,
  establishment_date timestamp null,
  rental_address     varchar(255) default null,
  is_rented          char(1) default '0',
  profit             decimal(10,2) default 0.00,
  owner_id           bigint default null,
  dept_id            bigint default null,
  parent_id          bigint default null,
  customer_id        bigint default null,
  audit_status       char(1) default '0',
  reminder_status    char(1) default '0',
  del_flag           char(1) default '0',
  annex              clob default null,
  create_by          varchar(64) default '',
  create_time        timestamp null,
  update_by          varchar(64) default '',
  update_time        timestamp null,
  remark             varchar(500) default null
);

-- 16. 附件明细表
drop table if exists cms_file;
create table cms_file (
  file_id       bigint auto_increment primary key,
  contract_id   bigint not null,
  file_name     varchar(255) not null,
  file_path     varchar(500) not null,
  file_url      varchar(500) default null,
  file_suffix   varchar(20) default null,
  file_size     bigint default 0,
  file_category char(1) default '1',
  create_by     varchar(64) default '',
  create_time   timestamp null
);

-- 17. 任务管理表
drop table if exists cms_task;
create table cms_task (
  task_id            bigint auto_increment primary key,
  task_title         varchar(200) not null,
  contract_id        bigint not null,
  source_contract_id bigint default null,
  target_contract_id bigint default null,
  task_type          char(1) default '0',
  priority           char(1) default '2',
  original_amount    decimal(10,2) default null,
  current_amount     decimal(10,2) default null,
  adjust_amount      decimal(10,2) default null,
  after_amount       decimal(10,2) default null,
  attachment         clob default null,
  actual_amount      decimal(10,2) default null,
  receive_remark     varchar(500) default null,
  assigned_to        bigint not null,
  deadline           timestamp null,
  status             char(2) default '0',
  del_flag           char(1) default '0',
  create_by          varchar(64) default '',
  create_time        timestamp null,
  update_by          varchar(64) default '',
  update_time        timestamp null,
  remark             varchar(500) default null
);

-- 18. 审批表
drop table if exists cms_approval;
create table cms_approval (
  approval_id      bigint auto_increment primary key,
  apply_no         varchar(64) default null,
  applicant_id     bigint not null,
  contract_id      bigint default null,
  task_id          bigint default null,
  approval_type    char(1) not null,
  content_snapshot clob default null,
  status           char(1) default '0',
  approver_id      bigint default null,
  approval_time    timestamp null,
  approval_msg     varchar(500) default null,
  create_by        varchar(64) default '',
  create_time      timestamp null,
  update_by        varchar(64) default '',
  update_time      timestamp null
);

-- 19. 沟通记录表
drop table if exists cms_communication;
create table cms_communication (
  communication_id   bigint auto_increment primary key,
  contract_id        bigint not null,
  user_id            bigint default null,
  content            clob default null,
  communication_type char(1) default null,
  create_by          varchar(64) default '',
  create_time        timestamp null,
  remark             varchar(500) default null
);

-- 20. 通知表
drop table if exists cms_notification;
create table cms_notification (
  notification_id   bigint auto_increment primary key,
  notification_type varchar(20) not null,
  title             varchar(200) not null,
  content           clob default null,
  user_id           bigint default null,
  sender_id         bigint default null,
  is_read           char(1) default '0',
  read_time         timestamp null,
  task_id           bigint default null,
  contract_id       bigint default null,
  create_time       timestamp null
);

-- 21. 任务日志表
drop table if exists cms_task_log;
create table cms_task_log (
  log_id     bigint auto_increment primary key,
  task_id    bigint not null,
  action     varchar(50) not null,
  operator   bigint default null,
  content    clob default null,
  create_time timestamp null
);
```

- [ ] **Step 4: 提交**

```bash
git add ruoyi-admin/src/test/resources/sql/schema-h2.sql
git commit -m "test: 添加 H2 兼容建表语句（System + CMS 业务表）"
```

---

### Task 4: 创建 data-init.sql（基础数据）

**Files:**
- Create: `ruoyi-admin/src/test/resources/sql/data-init.sql`

需要初始化：部门 → 角色 → 用户 → 用户角色 → 字典 → 菜单 → 角色菜单

- [ ] **Step 1: 写入部门、用户、角色基础数据**

```sql
-- data-init.sql
-- 测试基础数据：用户、角色、字典、菜单权限

-- ========== 部门 ==========
insert into sys_dept(dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time) 
values(100, 0, '0', '总公司', 1, 'manager', '13800000001', 'manager@test.com', '0', '0', 'manager', now());
insert into sys_dept(dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time) 
values(101, 100, '0,100', '销售部', 1, 'lisi', '13800000002', 'lisi@test.com', '0', '0', 'manager', now());
insert into sys_dept(dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time) 
values(102, 100, '0,100', '会计部', 2, 'zhangsan', '13800000003', 'zhangsan@test.com', '0', '0', 'manager', now());

-- ========== 角色 ==========
-- role_id=2: manager (业务管理员)
insert into sys_role(role_id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time)
values(2, '业务管理员', 'manager', 1, '1', '0', '0', 'manager', now());
-- role_id=3: account (会计)
insert into sys_role(role_id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time)
values(3, '会计', 'account', 2, '4', '0', '0', 'manager', now());
-- role_id=4: sales (销售)
insert into sys_role(role_id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time)
values(4, '销售', 'sales', 3, '4', '0', '0', 'manager', now());

-- ========== 用户 ==========
-- user_id=2: manager (业务管理员)
insert into sys_user(user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, password, status, del_flag, create_by, create_time)
values(2, 100, 'manager', '经理', '00', 'manager@test.com', '13800000001', '1', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', now());
-- user_id=3: zhangsan (会计)
insert into sys_user(user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, password, status, del_flag, create_by, create_time)
values(3, 102, 'zhangsan', '张三', '00', 'zhangsan@test.com', '13800000003', '1', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', now());
-- user_id=4: lisi (销售)
insert into sys_user(user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, password, status, del_flag, create_by, create_time)
values(4, 101, 'lisi', '李四', '00', 'lisi@test.com', '13800000002', '1', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', now());

-- ========== 用户角色 ==========
insert into sys_user_role(user_id, role_id) values(2, 2);  -- manager → manager
insert into sys_user_role(user_id, role_id) values(3, 3);  -- zhangsan → account
insert into sys_user_role(user_id, role_id) values(4, 4);  -- lisi → sales
```

- [ ] **Step 2: 写入字典数据（CMS 业务字典）**

```sql
-- ========== 字典类型 ==========
insert into sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time)
values(10, '合同类型', 'cms_contract_type', '0', 'admin', now());
insert into sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time)
values(11, '付款周期', 'cms_pay_cycle', '0', 'admin', now());
insert into sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time)
values(12, '收款方式', 'cms_pay_method', '0', 'admin', now());
insert into sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time)
values(13, '任务类型', 'cms_task_type', '0', 'admin', now());
insert into sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time)
values(14, '任务状态', 'cms_task_status', '0', 'admin', now());
insert into sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time)
values(15, '任务优先级', 'cms_task_priority', '0', 'admin', now());
insert into sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time)
values(16, '审批类型', 'cms_approval_type', '0', 'admin', now());
insert into sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time)
values(17, '客户类型', 'cms_customer_type', '0', 'admin', now());
insert into sys_dict_type(dict_id, dict_name, dict_type, status, create_by, create_time)
values(18, '催收状态', 'cms_reminder_status', '0', 'admin', now());

-- ========== 字典数据 ==========
-- 合同类型
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(100, 1, '代账报税', '1', 'cms_contract_type', 'N', '0', 'admin', now());
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(101, 2, '地址出售', '2', 'cms_contract_type', 'N', '0', 'admin', now());

-- 付款周期
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(110, 1, '年付', '1', 'cms_pay_cycle', 'Y', '0', 'admin', now());
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(111, 2, '半年付', '2', 'cms_pay_cycle', 'N', '0', 'admin', now());
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(112, 3, '季付', '3', 'cms_pay_cycle', 'N', '0', 'admin', now());
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(113, 4, '月付', '4', 'cms_pay_cycle', 'N', '0', 'admin', now());

-- 收款方式
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(120, 1, '微信', '1', 'cms_pay_method', 'N', '0', 'admin', now());
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(121, 2, '支付宝', '2', 'cms_pay_method', 'N', '0', 'admin', now());
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(122, 3, '公户', '3', 'cms_pay_method', 'N', '0', 'admin', now());

-- 任务类型
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(130, 1, '催收', '1', 'cms_task_type', 'Y', '0', 'admin', now());
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(131, 2, '续费', '2', 'cms_task_type', 'N', '0', 'admin', now());
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(132, 3, '终止', '3', 'cms_task_type', 'N', '0', 'admin', now());

-- 任务状态
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(140, 1, '待处理', '0', 'cms_task_status', 'Y', '0', 'admin', now());
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(141, 2, '进行中', '1', 'cms_task_status', 'N', '0', 'admin', now());
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(142, 3, '待审批', '2', 'cms_task_status', 'N', '0', 'admin', now());
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(143, 4, '已退回', '3', 'cms_task_status', 'N', '0', 'admin', now());
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(144, 5, '已完成', '4', 'cms_task_status', 'N', '0', 'admin', now());

-- 审批类型
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(150, 1, '新合同', '1', 'cms_approval_type', 'Y', '0', 'admin', now());
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(151, 2, '续费', '2', 'cms_approval_type', 'N', '0', 'admin', now());
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(152, 3, '变更', '3', 'cms_approval_type', 'N', '0', 'admin', now());

-- 客户类型
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(160, 1, '企业', '1', 'cms_customer_type', 'Y', '0', 'admin', now());
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(161, 2, '个人', '2', 'cms_customer_type', 'N', '0', 'admin', now());

-- 催收状态
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(170, 1, '未催收', '0', 'cms_reminder_status', 'Y', '0', 'admin', now());
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(171, 2, '催收中', '1', 'cms_reminder_status', 'N', '0', 'admin', now());
insert into sys_dict_data(dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
values(172, 3, '已完成', '3', 'cms_reminder_status', 'N', '0', 'admin', now());
```

- [ ] **Step 3: 写入菜单权限数据**

需要覆盖所有 Controller `@PreAuthorize` 中使用的权限标识。

```sql
-- ========== 菜单权限 ==========
-- 父菜单：合同管理
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, create_time)
values(200, '合同管理', 0, 1, 'contract', 'system/contract/index', 'C', '0', '0', 'system:contract:list', 'admin', now());
-- 合同子按钮
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
values(201, '合同查询', 200, 1, '#', 'F', '0', '0', 'system:contract:query', 'admin', now());
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
values(202, '合同新增', 200, 2, '#', 'F', '0', '0', 'system:contract:add', 'admin', now());
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
values(203, '合同修改', 200, 3, '#', 'F', '0', '0', 'system:contract:edit', 'admin', now());
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
values(204, '合同删除', 200, 4, '#', 'F', '0', '0', 'system:contract:remove', 'admin', now());
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
values(205, '合同导入', 200, 5, '#', 'F', '0', '0', 'system:contract:import', 'admin', now());
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
values(206, '合同审批', 200, 6, '#', 'F', '0', '0', 'cms:contract:audit', 'admin', now());

-- 父菜单：客户管理
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, create_time)
values(210, '客户管理', 0, 2, 'customer', 'system/customer/index', 'C', '0', '0', 'system:customer:list', 'admin', now());
-- 客户子按钮
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
values(211, '客户查询', 210, 1, '#', 'F', '0', '0', 'system:customer:query', 'admin', now());
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
values(212, '客户新增', 210, 2, '#', 'F', '0', '0', 'system:customer:add', 'admin', now());
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
values(213, '客户修改', 210, 3, '#', 'F', '0', '0', 'system:customer:edit', 'admin', now());
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
values(214, '客户删除', 210, 4, '#', 'F', '0', '0', 'system:customer:remove', 'admin', now());

-- 父菜单：任务管理
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, create_time)
values(220, '任务管理', 0, 3, 'task', 'system/task/index', 'C', '0', '0', 'system:task:list', 'admin', now());
-- 任务子按钮
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
values(221, '任务查询', 220, 1, '#', 'F', '0', '0', 'system:task:query', 'admin', now());
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
values(222, '任务新增', 220, 2, '#', 'F', '0', '0', 'system:task:add', 'admin', now());
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
values(223, '任务修改', 220, 3, '#', 'F', '0', '0', 'system:task:edit', 'admin', now());
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
values(224, '任务删除', 220, 4, '#', 'F', '0', '0', 'system:task:remove', 'admin', now());
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
values(225, '任务导出', 220, 5, '#', 'F', '0', '0', 'system:task:export', 'admin', now());
-- 任务派发和审核
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
values(226, '任务派发', 220, 6, '#', 'F', '0', '0', 'cms:task:dispatch', 'admin', now());
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
values(227, '任务审批', 220, 7, '#', 'F', '0', '0', 'cms:task:audit', 'admin', now());

-- ========== 角色菜单 ==========
-- manager 拥有所有菜单
insert into sys_role_menu(role_id, menu_id)
select 2, menu_id from sys_menu;
-- account 拥有任务相关菜单（不含派发和审批）
insert into sys_role_menu(role_id, menu_id) values(3, 220);
insert into sys_role_menu(role_id, menu_id) values(3, 221);
insert into sys_role_menu(role_id, menu_id) values(3, 222);
insert into sys_role_menu(role_id, menu_id) values(3, 223);
insert into sys_role_menu(role_id, menu_id) values(3, 224);
insert into sys_role_menu(role_id, menu_id) values(3, 225);
-- sales 拥有合同+客户相关菜单（不含审批）
insert into sys_role_menu(role_id, menu_id) values(4, 200);
insert into sys_role_menu(role_id, menu_id) values(4, 201);
insert into sys_role_menu(role_id, menu_id) values(4, 202);
insert into sys_role_menu(role_id, menu_id) values(4, 203);
insert into sys_role_menu(role_id, menu_id) values(4, 204);
insert into sys_role_menu(role_id, menu_id) values(4, 210);
insert into sys_role_menu(role_id, menu_id) values(4, 211);
insert into sys_role_menu(role_id, menu_id) values(4, 212);
insert into sys_role_menu(role_id, menu_id) values(4, 213);
insert into sys_role_menu(role_id, menu_id) values(4, 214);

-- ========== 系统配置 ==========
insert into sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time)
values(1, '用户密码最大错误次数', 'sys.account.password.maxRetryCount', '5', 'Y', 'admin', now());
insert into sys_config(config_id, config_name, config_key, config_value, config_type, create_by, create_time)
values(2, '用户密码锁定时间', 'sys.account.password.lockTime', '10', 'Y', 'admin', now());
```

- [ ] **Step 4: 提交**

```bash
git add ruoyi-admin/src/test/resources/sql/data-init.sql
git commit -m "test: 添加测试基础数据（用户/角色/字典/菜单权限）"
```

---

### Task 5: 创建 BaseControllerTest.java

**Files:**
- Create: `ruoyi-admin/src/test/java/com/ruoyi/web/controller/davis/BaseControllerTest.java`

- [ ] **Step 1: 写入测试基类**

```java
package com.ruoyi.web.controller.davis;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.framework.web.service.SysPermissionService;
import com.ruoyi.system.domain.SysRole;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * CMS E2E 测试基类。
 * <p>
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

    // ========== Mock 用户数据 ==========

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
     * manager 拥有所有业务权限。
     */
    protected ResultActions asManager(String method, String url, Object body) throws Exception {
        return performRequest(method, url, body, USERNAME_MANAGER, 2L, "manager");
    }

    /**
     * 以 zhangsan（会计）身份执行 MockMvc 请求。
     * account 只能看到分配给自己的任务。
     */
    protected ResultActions asAccountant(String method, String url, Object body) throws Exception {
        return performRequest(method, url, body, USERNAME_ZHANGSAN, 3L, "account");
    }

    /**
     * 以 lisi（销售）身份执行 MockMvc 请求。
     * sales 只能看到自己创建的合同。
     */
    protected ResultActions asSales(String method, String url, Object body) throws Exception {
        return performRequest(method, url, body, USERNAME_LISI, 4L, "sales");
    }

    /**
     * 以指定角色执行请求的通用方法。
     * 通过 SecurityMockMvcRequestPostProcessors.authentication() 设置安全上下文，
     * 使 @PreAuthorize 权限校验可以正常执行。
     */
    private ResultActions performRequest(String method, String url, Object body,
                                          String username, Long userId, String roleKey) throws Exception {
        // 构建 LoginUser（com.ruoyi.framework.web.service.LoginUser 的简化版本）
        // 这里使用 UsernamePasswordAuthenticationToken 直接设置安全上下文
        LoginUserForTest loginUser = new LoginUserForTest();
        loginUser.setUserId(userId);
        loginUser.setUsername(username);
        loginUser.setUser(getSysUser(username));
        // 加载该角色拥有的权限
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
        switch (username) {
            case USERNAME_MANAGER: return managerUser;
            case USERNAME_ZHANGSAN: return zhangsanUser;
            case USERNAME_LISI: return lisiUser;
            default: return null;
        }
    }

    /**
     * 获取角色的权限标识集合。
     * 管理角色拥有所有权限（"*:*:*"），业务角色有具体的权限集合。
     */
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

    /**
     * 断言 API 返回成功（code == 200 且 msg == "操作成功"）。
     */
    protected void assertSuccess(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.code").value(200))
              .andExpect(jsonPath("$.msg").value("操作成功"));
    }

    /**
     * 断言 API 返回错误（code != 200 或包含指定错误消息）。
     */
    protected void assertError(ResultActions result, String expectedMsg) throws Exception {
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.not(200)));
    }

    /**
     * 从 AjaxResult 响应中提取 data 字段并反序列化为指定类型。
     */
    protected <T> T getData(ResultActions result, Class<T> clazz) throws Exception {
        String json = result.andReturn().getResponse().getContentAsString();
        AjaxResult ajax = objectMapper.readValue(json, AjaxResult.class);
        Object data = ajax.get("data");
        if (data == null) return null;
        // 如果 data 已经是 LinkedHashMap，直接转换
        if (data instanceof Map) {
            return objectMapper.convertValue(data, clazz);
        }
        return clazz.cast(data);
    }

    /**
     * 从 ResultActions 中提取响应 JSON 字符串。
     */
    protected String getResponseJson(ResultActions result) throws Exception {
        return result.andReturn().getResponse().getContentAsString();
    }

    /**
     * 快速创建 POST JSON 请求。
     */
    protected MockHttpServletRequestBuilder postJson(String url, Object body) throws Exception {
        String json = objectMapper.writeValueAsString(body);
        return MockMvcRequestBuilders.post(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json);
    }

    /**
     * 快速创建 PUT JSON 请求。
     */
    protected MockHttpServletRequestBuilder putJson(String url, Object body) throws Exception {
        String json = objectMapper.writeValueAsString(body);
        return MockMvcRequestBuilders.put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json);
    }

    // ========== 内部类：简化的 LoginUser ==========

    /**
     * 测试专用的 LoginUser 简化实现。
     * 仅包含测试所需的最小字段集合。
     */
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
```

- [ ] **Step 2: 提交**

```bash
git add ruoyi-admin/src/test/java/com/ruoyi/web/controller/davis/BaseControllerTest.java
git commit -m "test: 创建 BaseControllerTest 测试基类（MockMvc + 多角色认证 + 断言工具）"
```

---

### Task 6: 创建 CustomerLifecycleFlowTest.java

**Files:**
- Create: `ruoyi-admin/src/test/java/com/ruoyi/web/controller/davis/CustomerLifecycleFlowTest.java`

- [ ] **Step 1: 写出测试类骨架**

```java
package com.ruoyi.web.controller.davis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E: 客户全生命周期流程。
 * <p>
 * 新增客户 → 查询列表 → 查看详情 → 修改客户 → 软删除 → 验证删除后不可见。
 * 执行角色：manager（业务管理员）
 */
@DisplayName("E2E: 客户全生命周期流程")
public class CustomerLifecycleFlowTest extends BaseControllerTest {

    @Test
    @Order(1)
    @DisplayName("流程1: 新增客户 → 查询列表 → 修改客户 → 软删除 → 确认不可见")
    void testCustomerLifecycle() throws Exception {
        // ====== 1. 新增客户 ======
        Map<String, Object> newCustomer = new LinkedHashMap<>();
        newCustomer.put("customerName", "测试科技有限公司");
        newCustomer.put("customerType", "1");
        newCustomer.put("contactPerson", "王经理");
        newCustomer.put("contactPhone", "13900000001");
        newCustomer.put("contactEmail", "test@tech.com");
        newCustomer.put("address", "深圳市南山区科技园");
        newCustomer.put("ownerId", 4L);  // 归属 lisi（销售）

        ResultActions addResult = asManager("POST", "/system/customer", newCustomer);
        assertSuccess(addResult);

        // 提取 customerId
        Long customerId = getField(addResult, "data.customerId", Long.class);
        assertThat(customerId).isNotNull();

        // ====== 2. 查询列表，验证客户存在 ======
        ResultActions listResult = asManager("GET", "/system/customer/list", null);
        assertSuccess(listResult);
        String listJson = getResponseJson(listResult);
        assertThat(listJson).contains("测试科技有限公司");

        // ====== 3. 查看详情（含合同列表） ======
        ResultActions detailResult = asManager("GET", "/system/customer/detail/" + customerId, null);
        assertSuccess(detailResult);
        String detailJson = getResponseJson(detailResult);
        assertThat(detailJson).contains("测试科技有限公司");

        // ====== 4. 修改客户信息 ======
        Map<String, Object> updateCustomer = new LinkedHashMap<>();
        updateCustomer.put("customerId", customerId);
        updateCustomer.put("customerName", "测试科技有限公司-改名");
        updateCustomer.put("customerType", "1");
        updateCustomer.put("contactPerson", "张总");
        updateCustomer.put("contactPhone", "13900000002");
        updateCustomer.put("ownerId", 4L);

        ResultActions updateResult = asManager("PUT", "/system/customer", updateCustomer);
        assertSuccess(updateResult);

        // ====== 5. 查询详情验证已更新 ======
        ResultActions checkResult = asManager("GET", "/system/customer/" + customerId, null);
        assertSuccess(checkResult);
        String checkJson = getResponseJson(checkResult);
        assertThat(checkJson).contains("测试科技有限公司-改名");
        assertThat(checkJson).contains("张总");

        // ====== 6. 软删除 ======
        ResultActions deleteResult = asManager("DELETE", "/system/customer/" + customerId, null);
        assertSuccess(deleteResult);

        // ====== 7. 列表查询，验证已删除客户不出现 ======
        ResultActions listAfterDelete = asManager("GET", "/system/customer/list", null);
        assertSuccess(listAfterDelete);
        String listAfterJson = getResponseJson(listAfterDelete);
        assertThat(listAfterJson).doesNotContain("测试科技有限公司");

        // ====== 8. 按 ID 查询，应返回错误或空 ======
        // 注：selectCmsCustomerById 在改造后加 del_flag='0' 条件，逻辑删除的客户查不到
        ResultActions getAfterDelete = asManager("GET", "/system/customer/" + customerId, null);
        // 预期：返回 code != 200（找不到数据）
        getAfterDelete.andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.not(200)));
    }

    /** 从 JSON path 提取字段值 */
    @SuppressWarnings("unchecked")
    private <T> T getField(ResultActions result, String jsonPath, Class<T> clazz) throws Exception {
        // 简单实现：从 response 中解析 data 对象
        String json = getResponseJson(result);
        Map<String, Object> response = objectMapper.readValue(json, Map.class);
        Object data = response.get("data");
        if (data == null) return null;
        if (data instanceof Map) {
            String field = jsonPath.replace("data.", "");
            return (T) ((Map<String, Object>) data).get(field);
        }
        return (T) data;
    }
}
```

完整测试类包含 `jsonPath` 导入（`import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;`）。

- [ ] **Step 2: 提交**

```bash
git add ruoyi-admin/src/test/java/.../CustomerLifecycleFlowTest.java
git commit -m "test: 客户全生命周期 E2E 测试（CRUD + 软删除验证）"
```

---

### Task 7: 创建 ContractApprovalFlowTest.java

**Files:**
- Create: `ruoyi-admin/src/test/java/com/ruoyi/web/controller/davis/ContractApprovalFlowTest.java`

- [ ] **Step 1: 写入合同审批流程测试类**

```java
package com.ruoyi.web.controller.davis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E: 合同审批流程。
 * <p>
 * sales 创建客户+合同 → manager 审批通过/驳回 → sales 修改重新提交。
 * 涉及两个角色的数据隔离验证。
 */
@DisplayName("E2E: 合同审批流程")
public class ContractApprovalFlowTest extends BaseControllerTest {

    @Test
    @Order(1)
    @DisplayName("sales 创建合同 → manager 审批通过 → 查询验证状态")
    void testContractApproval() throws Exception {
        Long customerId = createCustomer();
        Long contractId = createContract(customerId, "代账服务合同-001");

        // ====== manager 查询待审批合同列表 ======
        ResultActions listResult = asManager("GET", "/system/contract/list", null);
        assertSuccess(listResult);
        String listJson = getResponseJson(listResult);
        assertThat(listJson).contains("代账服务合同-001");

        // ====== manager 审批通过 ======
        Map<String, Object> auditPass = new LinkedHashMap<>();
        auditPass.put("contractId", contractId);
        auditPass.put("auditStatus", "1");
        ResultActions auditResult = asManager("POST", "/system/contract/audit", auditPass);
        assertSuccess(auditResult);

        // ====== 验证合同状态为通过 ======
        ResultActions detailResult = asManager("GET", "/system/contract/" + contractId, null);
        assertSuccess(detailResult);
        String detailJson = getResponseJson(detailResult);
        assertThat(detailJson).contains("\"auditStatus\":\"1\"");
    }

    @Test
    @Order(2)
    @DisplayName("sales 重新提交被驳回的合同 → manager 再次审批通过")
    void testContractRejectAndResubmit() throws Exception {
        Long customerId = createCustomer();
        Long contractId = createContract(customerId, "地址出租合同-002");

        // ====== manager 审批驳回 ======
        Map<String, Object> auditReject = new LinkedHashMap<>();
        auditReject.put("contractId", contractId);
        auditReject.put("auditStatus", "2");
        asManager("POST", "/system/contract/audit", auditReject);

        // ====== 验证状态为驳回 ======
        ResultActions rejected = asManager("GET", "/system/contract/" + contractId, null);
        String rejectedJson = getResponseJson(rejected);
        assertThat(rejectedJson).contains("\"auditStatus\":\"2\"");

        // ====== sales 修改重新提交 ======
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
        asSales("PUT", "/system/contract", update);

        // ====== manager 再次审批通过 ======
        Map<String, Object> auditPass = new LinkedHashMap<>();
        auditPass.put("contractId", contractId);
        auditPass.put("auditStatus", "1");
        asManager("POST", "/system/contract/audit", auditPass);

        // ====== 最终验证 ======
        ResultActions finalDetail = asManager("GET", "/system/contract/" + contractId, null);
        String finalJson = getResponseJson(finalDetail);
        assertThat(finalJson).contains("\"auditStatus\":\"1\"");
        assertThat(finalJson).contains("地址出租合同-002-修改版");
    }

    // ========== 辅助方法 ==========

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
        if (data instanceof Map) {
            return ((Number) ((Map) data).get("customerId")).longValue();
        }
        return null;
    }

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
        if (data instanceof Map) {
            return ((Number) ((Map) data).get("contractId")).longValue();
        }
        return null;
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add ruoyi-admin/src/test/java/.../ContractApprovalFlowTest.java
git commit -m "test: 合同审批流程 E2E 测试（sales 提交、manager 审批通过/驳回/再审批）"
```

---

### Task 8: 创建 ContractManagementFlowTest.java

**Files:**
- Create: `ruoyi-admin/src/test/java/com/ruoyi/web/controller/davis/ContractManagementFlowTest.java`

- [ ] **Step 1: 写入合同管理流程测试类**

```java
package com.ruoyi.web.controller.davis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E: 合同管理流程。
 * <p>
 * 分页查询 → 类型筛选 → 修改金额 → 删除合同 → 验证删除后不可见。
 * 验证：合同软删除后客户详情中不显示已删除合同。
 */
@DisplayName("E2E: 合同管理流程")
public class ContractManagementFlowTest extends BaseControllerTest {

    @Test
    @Order(1)
    @DisplayName("合同 CRUD + 软删除 + 关联客户详情联动验证")
    void testContractManagement() throws Exception {
        // ====== 准备数据：客户 + 两个合同（代账 + 地址） ======
        Long customerId = createCustomer("管理测试客户");
        Long contract1Id = createContract(customerId, "代账合同-A", "1", 12000.00);
        Long contract2Id = createContract(customerId, "地址合同-B", "2", 8000.00);

        // ====== 1. 查询列表 ======
        ResultActions listResult = asManager("GET", "/system/contract/list", null);
        assertSuccess(listResult);
        String listJson = getResponseJson(listResult);
        assertThat(listJson).contains("代账合同-A", "地址合同-B");

        // ====== 2. 按类型筛选（代账 type=1） ======
        ResultActions filtered = asManager("GET", "/system/contract/list?contractType=1", null);
        assertSuccess(filtered);
        String filteredJson = getResponseJson(filtered);
        assertThat(filteredJson).contains("代账合同-A");
        assertThat(filteredJson).doesNotContain("地址合同-B");

        // ====== 3. 修改合同金额 ======
        Map<String, Object> update = new LinkedHashMap<>();
        update.put("contractId", contract1Id);
        update.put("contractName", "代账合同-A");
        update.put("contractType", "1");
        update.put("customerId", customerId);
        update.put("amount", 15000.00);
        update.put("paymentCycle", "1");
        update.put("paymentMethod", "3");
        update.put("ownerId", 2L);
        ResultActions updateResult = asManager("PUT", "/system/contract", update);
        assertSuccess(updateResult);

        // ====== 4. 查询详情验证金额已更新 ======
        ResultActions detailResult = asManager("GET", "/system/contract/" + contract1Id, null);
        assertSuccess(detailResult);
        String detailJson = getResponseJson(detailResult);
        assertThat(detailJson).contains("\"amount\":15000.0");

        // ====== 5. 删除合同 ======
        ResultActions deleteResult = asManager("DELETE", "/system/contract/" + contract1Id, null);
        assertSuccess(deleteResult);

        // ====== 6. 列表验证已删除 ======
        ResultActions listAfter = asManager("GET", "/system/contract/list", null);
        assertSuccess(listAfter);
        String listAfterJson = getResponseJson(listAfter);
        assertThat(listAfterJson).doesNotContain("代账合同-A");
        assertThat(listAfterJson).contains("地址合同-B");

        // ====== 7. 按 ID 查已删除合同 ======
        ResultActions getDeleted = asManager("GET", "/system/contract/" + contract1Id, null);
        getDeleted.andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.not(200)));

        // ====== 8. 查看客户详情，已删除合同不出现 ======
        ResultActions customerDetail = asManager("GET", "/system/customer/detail/" + customerId, null);
        assertSuccess(customerDetail);
        String customerDetailJson = getResponseJson(customerDetail);
        assertThat(customerDetailJson).doesNotContain("代账合同-A");
        assertThat(customerDetailJson).contains("地址合同-B");
    }

    private Long createCustomer(String name) throws Exception {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("customerName", name);
        customer.put("customerType", "1");
        customer.put("contactPerson", "联系人");
        customer.put("contactPhone", "13900000001");
        customer.put("ownerId", 2L);
        ResultActions result = asManager("POST", "/system/customer", customer);
        String json = getResponseJson(result);
        Map<String, Object> resp = objectMapper.readValue(json, Map.class);
        Object data = resp.get("data");
        return data instanceof Map ? ((Number) ((Map) data).get("customerId")).longValue() : null;
    }

    private Long createContract(Long customerId, String name, String type, Double amount) throws Exception {
        Map<String, Object> contract = new LinkedHashMap<>();
        contract.put("contractName", name);
        contract.put("contractType", type);
        contract.put("customerId", customerId);
        contract.put("amount", amount);
        contract.put("paymentCycle", "1");
        contract.put("paymentMethod", "3");
        contract.put("startDate", "2026-06-01");
        contract.put("endDate", "2027-05-31");
        contract.put("ownerId", 2L);
        ResultActions result = asManager("POST", "/system/contract", contract);
        // 创建后需审批通过
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
}
```

- [ ] **Step 2: 提交**

```bash
git add ruoyi-admin/src/test/java/.../ContractManagementFlowTest.java
git commit -m "test: 合同管理流程 E2E 测试（CRUD + 类型筛选 + 软删除联动）"
```

---

### Task 9: 创建 CollectionTaskFlowTest.java（核心链路）

**Files:**
- Create: `ruoyi-admin/src/test/java/com/ruoyi/web/controller/davis/CollectionTaskFlowTest.java`

- [ ] **Step 1: 写入催收任务全流程测试类**

```java
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
 * <p>
 * 创建催收任务 → 会计退回讲价 → 管理员重新派发 → 会计确认收款。
 * 涉及三个角色（manager/sales/account）及数据隔离验证。
 */
@DisplayName("E2E: 催收任务全流程（核心链路）")
public class CollectionTaskFlowTest extends BaseControllerTest {

    @Test
    @Order(1)
    @DisplayName("催收任务创建 → 退回讲价 → 重新派发 → 确认收款")
    void testCollectionTaskFullFlow() throws Exception {
        // ====== 准备：客户 + 合同 ======
        Long customerId = createCustomer();
        Long contractId = createContract(customerId);

        // ====== 步骤1: manager 创建催收任务（分配给 zhangsan） ======
        Map<String, Object> task = new LinkedHashMap<>();
        task.put("taskTitle", "催收任务-代账服务费");
        task.put("contractId", contractId);
        task.put("taskType", "1");
        task.put("priority", "2");
        task.put("originalAmount", 12000.00);
        task.put("currentAmount", 12000.00);
        task.put("assignedTo", 3L);  // zhangsan
        task.put("status", "0");
        ResultActions createTaskResult = asManager("POST", "/system/task", task);
        assertSuccess(createTaskResult);

        // 提取 taskId
        String createJson = getResponseJson(createTaskResult);
        Map<String, Object> createResp = objectMapper.readValue(createJson, Map.class);
        Object data = createResp.get("data");
        Long taskId = data instanceof Map ? ((Number) ((Map) data).get("taskId")).longValue() : null;
        assertThat(taskId).isNotNull();

        // ====== 验证合同 reminderStatus 变为 "1"（催收中） ======
        ResultActions contractCheck = asManager("GET", "/system/contract/" + contractId, null);
        String contractJson = getResponseJson(contractCheck);
        assertThat(contractJson).contains("\"reminderStatus\":\"1\"");

        // ====== 步骤2: zhangsan 查看任务列表（应有该任务） ======
        ResultActions taskListForZhangsan = asAccountant("GET", "/system/task/list", null);
        assertSuccess(taskListForZhangsan);
        String zhangsanTaskJson = getResponseJson(taskListForZhangsan);
        assertThat(zhangsanTaskJson).contains("催收任务-代账服务费");

        // ====== 步骤3: zhangsan 设置进行中 → 退回讲价 ======
        Map<String, Object> inProgress = new LinkedHashMap<>();
        inProgress.put("taskId", taskId);
        inProgress.put("status", "1");
        asAccountant("PUT", "/system/task", inProgress);

        Map<String, Object> returnToAdmin = new LinkedHashMap<>();
        returnToAdmin.put("taskId", taskId);
        returnToAdmin.put("currentAmount", 10000.00);  // 协商降到 10000
        ResultActions returnResult = asAccountant("POST", "/system/task/returnToAdmin", returnToAdmin);
        assertSuccess(returnResult);

        // 验证任务状态变 "2"（待审批）
        ResultActions afterReturn = asManager("GET", "/system/task/" + taskId, null);
        assertThat(getResponseJson(afterReturn)).contains("\"status\":\"2\"");

        // ====== 步骤4: manager 重新派发 ======
        Map<String, Object> redispatch = new LinkedHashMap<>();
        redispatch.put("taskId", taskId);
        redispatch.put("assignedTo", 3L);
        ResultActions redispatchResult = asManager("POST", "/system/task/redispatch", redispatch);
        assertSuccess(redispatchResult);

        // 验证任务状态变回 "0"（待处理）
        ResultActions afterRedispatch = asManager("GET", "/system/task/" + taskId, null);
        assertThat(getResponseJson(afterRedispatch)).contains("\"status\":\"0\"");

        // ====== 步骤5: zhangsan 确认收款 ======
        Map<String, Object> confirmPayment = new LinkedHashMap<>();
        confirmPayment.put("taskId", taskId);
        confirmPayment.put("actualAmount", 10000.00);
        confirmPayment.put("receiveRemark", "银行转账收款，已到账");
        ResultActions paymentResult = asAccountant("POST", "/system/task/confirmPayment", confirmPayment);
        assertSuccess(paymentResult);

        // 验证任务状态变为 "4"（已完成）
        ResultActions afterPayment = asManager("GET", "/system/task/" + taskId, null);
        assertThat(getResponseJson(afterPayment)).contains("\"status\":\"4\"");

        // 验证合同 reminderStatus 变为 "3"（已完成）
        ResultActions finalContract = asManager("GET", "/system/contract/" + contractId, null);
        assertThat(getResponseJson(finalContract)).contains("\"reminderStatus\":\"3\"");
    }

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
}
```

- [ ] **Step 2: 提交**

```bash
git add ruoyi-admin/src/test/java/.../CollectionTaskFlowTest.java
git commit -m "test: 催收任务全流程 E2E 测试（退回讲价 + 重新派发 + 确认收款）"
```

---

### Task 10: 创建 RenewalTaskFlowTest.java

**Files:**
- Create: `ruoyi-admin/src/test/java/com/ruoyi/web/controller/davis/RenewalTaskFlowTest.java`

- [ ] **Step 1: 写入续费任务流程测试类**

```java
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
 * <p>
 * 创建续费任务 → 会计完成续签（生成新合同） → 验证新合同基于原合同复制。
 */
@DisplayName("E2E: 续费任务流程")
public class RenewalTaskFlowTest extends BaseControllerTest {

    @Test
    @Order(1)
    @DisplayName("续费任务创建 → 完成续签 → 新合同生成 → 验证关联")
    void testRenewalFlow() throws Exception {
        // ====== 准备：客户 + 原合同 ======
        Long customerId = createCustomer();
        Long sourceContractId = createContract(customerId);

        // ====== manager 创建续费任务（分配给 zhangsan） ======
        Map<String, Object> task = new LinkedHashMap<>();
        task.put("taskTitle", "续费任务-代账服务");
        task.put("contractId", sourceContractId);
        task.put("taskType", "2");
        task.put("priority", "2");
        task.put("originalAmount", 12000.00);
        task.put("currentAmount", 12000.00);
        task.put("assignedTo", 3L);
        task.put("status", "0");
        ResultActions createResult = asManager("POST", "/system/task", createResult);
        String createJson = getResponseJson(createResult);
        Map<String, Object> createResp = objectMapper.readValue(createJson, Map.class);
        Object taskData = createResp.get("data");
        Long taskId = taskData instanceof Map ? ((Number) ((Map) taskData).get("taskId")).longValue() : null;
        assertThat(taskId).isNotNull();

        // ====== zhangsan 完成续签（生成新合同） ======
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

        // ====== 验证新合同已创建 ======
        String renewalJson = getResponseJson(renewalResult);
        assertThat(renewalJson).contains("newContractId");
        Map<String, Object> renewalResp = objectMapper.readValue(renewalJson, Map.class);
        Object renewalData = renewalResp.get("data");
        Long newContractId = renewalData instanceof Map ?
            ((Number) ((Map) renewalData).get("newContractId")).longValue() : null;
        assertThat(newContractId).isNotNull();

        // ====== 查新合同，验证 parentId 指向原合同 ======
        ResultActions newContractDetail = asManager("GET", "/system/contract/" + newContractId, null);
        String newContractJson = getResponseJson(newContractDetail);
        assertThat(newContractJson).contains("\"parentId\":" + sourceContractId);
        assertThat(newContractJson).contains("\"contractName\":\"续费后新合同\"");
        assertThat(newContractJson).contains("\"auditStatus\":\"0\"");  // 新合同待审批

        // ====== 验证任务已完成 ======
        ResultActions taskDetail = asManager("GET", "/system/task/" + taskId, null);
        assertThat(getResponseJson(taskDetail)).contains("\"status\":\"4\"");

        // ====== 验证任务关联了新合同 ======
        assertThat(getResponseJson(taskDetail)).contains("\"targetContractId\":" + newContractId);
    }

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
}
```

- [ ] **Step 2: 提交**

```bash
git add ruoyi-admin/src/test/java/.../RenewalTaskFlowTest.java
git commit -m "test: 续费任务流程 E2E 测试（完成续签 + 新合同生成验证）"
```

---

### Task 11: 创建 TerminationFlowTest.java

**Files:**
- Create: `ruoyi-admin/src/test/java/com/ruoyi/web/controller/davis/TerminationFlowTest.java`

- [ ] **Step 1: 写入终止合作流程测试类**

```java
package com.ruoyi.web.controller.davis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E: 终止合作流程。
 * <p>
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

        // ====== zhangsan 发起终止请求 ======
        Map<String, Object> terminationReq = new LinkedHashMap<>();
        terminationReq.put("taskId", taskId);
        terminationReq.put("remark", "客户已注销公司，不再合作");
        ResultActions requestResult = asAccountant("POST", "/system/task/requestTermination", terminationReq);
        assertSuccess(requestResult);

        // ====== 验证 taskType 变为 "3"（终止），status 变为 "2"（待审批） ======
        ResultActions afterRequest = asManager("GET", "/system/task/" + taskId, null);
        String afterRequestJson = getResponseJson(afterRequest);
        assertThat(afterRequestJson).contains("\"taskType\":\"3\"");
        assertThat(afterRequestJson).contains("\"status\":\"2\"");

        // ====== manager 确认终止（approved=true） ======
        Map<String, Object> confirmApproved = new LinkedHashMap<>();
        confirmApproved.put("taskId", taskId);
        confirmApproved.put("approved", true);
        ResultActions confirmResult = asManager("POST", "/system/task/confirmTermination", confirmApproved);
        assertSuccess(confirmResult);

        // ====== 验证任务状态 "4"（已完成） ======
        ResultActions finalTask = asManager("GET", "/system/task/" + taskId, null);
        assertThat(getResponseJson(finalTask)).contains("\"status\":\"4\"");

        // ====== 验证合同 reminderStatus "3"（已完成） ======
        ResultActions finalContract = asManager("GET", "/system/contract/" + contractId, null);
        assertThat(getResponseJson(finalContract)).contains("\"reminderStatus\":\"3\"");
    }

    @Test
    @Order(2)
    @DisplayName("催收任务 → 发起终止 → 管理员拒绝 → 任务退回")
    void testTerminationRejected() throws Exception {
        Long customerId = createCustomer();
        Long contractId = createContract(customerId);
        Long taskId = createCollectionTask(contractId);

        // ====== zhangsan 发起终止 ======
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("taskId", taskId);
        req.put("remark", "客户拒绝付款");
        asAccountant("POST", "/system/task/requestTermination", req);

        // ====== manager 拒绝终止（approved=false） ======
        Map<String, Object> reject = new LinkedHashMap<>();
        reject.put("taskId", taskId);
        reject.put("approved", false);
        ResultActions rejectResult = asManager("POST", "/system/task/confirmTermination", reject);
        assertSuccess(rejectResult);

        // ====== 验证任务状态 "3"（已退回） ======
        ResultActions afterReject = asManager("GET", "/system/task/" + taskId, null);
        assertThat(getResponseJson(afterReject)).contains("\"status\":\"3\"");
    }

    private Long createCustomer() throws Exception {
        Map<String, Object> customer = new LinkedHashMap<>();
        customer.put("customerName", "终止测试客户");
        customer.put("customerType", "1");
        customer.put("contactPerson", "周总");
        customer.put("contactPhone", "13900000300");
        customer.put("ownerId", 4L);
        ResultActions result = asSales("POST", "/system/customer", customer);
        String json = getResponseJson(result);
        Map<String, Object> resp = objectMapper.readValue(json, Map.class);
        return resp.get("data") instanceof Map ?
            ((Number) ((Map) resp.get("data")).get("customerId")).longValue() : null;
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
        ResultActions result = asManager("POST", "/system/task", task);
        String json = getResponseJson(result);
        Map<String, Object> resp = objectMapper.readValue(json, Map.class);
        Object data = resp.get("data");
        return data instanceof Map ? ((Number) ((Map) data).get("taskId")).longValue() : null;
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add ruoyi-admin/src/test/java/.../TerminationFlowTest.java
git commit -m "test: 终止合作流程 E2E 测试（发起终止 + 管理员确认/拒绝）"
```

---

### Task 12: 试运行测试 + 修复问题

- [ ] **Step 1: 运行所有测试**

```bash
cd D:/GitHub/ruoyi-davis
mvn test -pl ruoyi-admin -am -Dtest="*FlowTest" -DfailIfNoTests=false
```

预期问题及处理策略：
1. **H2 兼容性问题**：调整 schema-h2.sql 中的类型定义（如 JSON→CLOB）
2. **Spring Security 上下文问题**：调整 BaseControllerTest 中的认证构建方式
3. **Bean 加载冲突**：调整 application-test.yml 排除不需要的自动配置类
4. **MyBatis XML 中 IFNULL 报错**：在 H2 mode=MySQL 下 IFNULL 应兼容，如出错需将 Mapper XML 中 IFNULL 替换为 COALESCE

- [ ] **Step 2: 全部测试通过后，标记改造完成**

```bash
cd D:/GitHub/ruoyi-davis && git status
```

## 自检清单

对照设计文档检查计划覆盖度：

| 设计文档要求 | 对应 Task | 是否覆盖 |
|-------------|-----------|---------|
| 测试依赖 | Task 1 | ✓ |
| 测试配置 | Task 2 | ✓ |
| H2 建表 | Task 3 | ✓ |
| 基础数据 | Task 4 | ✓ |
| 测试基类 | Task 5 | ✓ |
| E2E 客户生命周期 | Task 6 | ✓ |
| E2E 合同审批 | Task 7 | ✓ |
| E2E 合同管理 | Task 8 | ✓ |
| E2E 催收任务 (核心) | Task 9 | ✓ |
| E2E 续费任务 | Task 10 | ✓ |
| E2E 终止合作 | Task 11 | ✓ |
| 试运行+修复 | Task 12 | ✓ |

类型一致性：所有测试类继承自 `BaseControllerTest`，通过 `asManager` / `asAccountant` / `asSales` 统一获取认证后的请求后处理器。

无占位符、无 TBD、无 TODO。
