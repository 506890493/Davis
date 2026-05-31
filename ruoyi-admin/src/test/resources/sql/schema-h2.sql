-- schema-h2.sql
-- H2 兼容建表语句 (MODE=MySQL)
-- 用于测试环境初始化内存数据库

-- ========== 系统表 ==========

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
  cost_time      bigint default 0
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

-- ========== CMS 业务表 ==========

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
  user_id           bigint not null,
  title             varchar(200) not null,
  content           clob default null,
  notification_type varchar(20) not null,
  related_id        bigint default null,
  is_read           char(1) default '0',
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

-- 22. 定时任务调度表
drop table if exists sys_job;
create table sys_job (
  job_id           bigint auto_increment,
  job_name         varchar(64) default '' not null,
  job_group        varchar(64) default 'DEFAULT' not null,
  invoke_target    varchar(500) not null,
  cron_expression  varchar(255) default '' null,
  misfire_policy   varchar(20) default '3' null,
  concurrent       char(1) default '1' null,
  status           char(1) default '0' null,
  create_by        varchar(64) default '' null,
  create_time      timestamp null,
  update_by        varchar(64) default '' null,
  update_time      timestamp null,
  remark           varchar(500) default '' null,
  primary key (job_id, job_name, job_group)
);

-- 23. 定时任务日志表
drop table if exists sys_job_log;
create table sys_job_log (
  job_log_id      bigint auto_increment primary key,
  job_name        varchar(64) not null,
  job_group       varchar(64) not null,
  invoke_target   varchar(500) not null,
  job_message     varchar(500) null,
  status          char(1) default '0' null,
  exception_info  clob default null,
  create_time     timestamp null
);
