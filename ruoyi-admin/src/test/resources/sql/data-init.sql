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
insert into sys_role(role_id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time)
values(2, '业务管理员', 'manager', 1, '1', '0', '0', 'manager', now());
insert into sys_role(role_id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time)
values(3, '会计', 'account', 2, '4', '0', '0', 'manager', now());
insert into sys_role(role_id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time)
values(4, '销售', 'sales', 3, '4', '0', '0', 'manager', now());

-- ========== 用户 ==========
insert into sys_user(user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, password, status, del_flag, create_by, create_time)
values(2, 100, 'manager', '经理', '00', 'manager@test.com', '13800000001', '1', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', now());
insert into sys_user(user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, password, status, del_flag, create_by, create_time)
values(3, 102, 'zhangsan', '张三', '00', 'zhangsan@test.com', '13800000003', '1', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', now());
insert into sys_user(user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, password, status, del_flag, create_by, create_time)
values(4, 101, 'lisi', '李四', '00', 'lisi@test.com', '13800000002', '1', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', now());

-- ========== 用户角色 ==========
insert into sys_user_role(user_id, role_id) values(2, 2);  -- manager → manager
insert into sys_user_role(user_id, role_id) values(3, 3);  -- zhangsan → account
insert into sys_user_role(user_id, role_id) values(4, 4);  -- lisi → sales

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

-- ========== 菜单权限 ==========
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, create_time)
values(200, '合同管理', 0, 1, 'contract', 'system/contract/index', 'C', '0', '0', 'system:contract:list', 'admin', now());
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

insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, create_time)
values(210, '客户管理', 0, 2, 'customer', 'system/customer/index', 'C', '0', '0', 'system:customer:list', 'admin', now());
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
values(211, '客户查询', 210, 1, '#', 'F', '0', '0', 'system:customer:query', 'admin', now());
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
values(212, '客户新增', 210, 2, '#', 'F', '0', '0', 'system:customer:add', 'admin', now());
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
values(213, '客户修改', 210, 3, '#', 'F', '0', '0', 'system:customer:edit', 'admin', now());
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
values(214, '客户删除', 210, 4, '#', 'F', '0', '0', 'system:customer:remove', 'admin', now());

insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, create_by, create_time)
values(220, '任务管理', 0, 3, 'task', 'system/task/index', 'C', '0', '0', 'system:task:list', 'admin', now());
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
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
values(226, '任务派发', 220, 6, '#', 'F', '0', '0', 'cms:task:dispatch', 'admin', now());
insert into sys_menu(menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
values(227, '任务审批', 220, 7, '#', 'F', '0', '0', 'cms:task:audit', 'admin', now());

-- ========== 角色菜单 ==========
-- manager: 所有菜单
insert into sys_role_menu(role_id, menu_id)
select 2, menu_id from sys_menu;
-- account: 任务管理相关
insert into sys_role_menu(role_id, menu_id) values(3, 220);
insert into sys_role_menu(role_id, menu_id) values(3, 221);
insert into sys_role_menu(role_id, menu_id) values(3, 222);
insert into sys_role_menu(role_id, menu_id) values(3, 223);
insert into sys_role_menu(role_id, menu_id) values(3, 224);
insert into sys_role_menu(role_id, menu_id) values(3, 225);
-- sales: 合同 + 客户
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
