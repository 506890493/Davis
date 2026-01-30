-- 1. Create Roles (if not exist)
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, update_by, update_time, remark)
SELECT 3, '会计/财务', 'accountant', 3, '1', 1, 1, '0', '0', 'admin', sysdate(), '', null, '财务人员'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'accountant');

INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, update_by, update_time, remark)
SELECT 4, '销售人员', 'sales', 4, '5', 1, 1, '0', '0', 'admin', sysdate(), '', null, '销售人员'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'sales');

-- 2. Create Permission Menu (Button type, hidden but assignable)
-- We insert it under the 'System' menu (usually id 1) or 'Home' if feasible, but usually permissions are attached to modules.
-- Since this is a dashboard query, we'll add it as a button under the "System" management or just a standalone permission node.
-- A safe bet is adding it as a child of the top-level System menu (id=1) or creating a new hidden menu entry for it.
-- Let's put it under 'System' (1) -> 'Contract' (if exists) or just 'System'.
-- Assuming 'System' menu ID is 1.

-- Insert 'Dashboard Query' permission
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('仪表盘查询', 1, 100, '', '', 1, 0, 'F', '0', '0', 'system:dashboard:query', '#', 'admin', sysdate(), '', null, '');

-- 3. Assign Permission to Roles
-- Get the menu_id we just inserted (since we can't use variables easily in raw SQL without stored procedures, we might need to assume a high ID or run this manually).
-- For this script, I will use a subquery to find the ID.

-- Assign to Admin (Role 1 usually has * but strict mode might require it) - Admin usually has all via Super Admin check.

-- Assign to Accountant
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r, sys_menu m
WHERE r.role_key = 'accountant' AND m.perms = 'system:dashboard:query'
AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = r.role_id AND menu_id = m.menu_id);

-- Assign to Sales
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r, sys_menu m
WHERE r.role_key = 'sales' AND m.perms = 'system:dashboard:query'
AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = r.role_id AND menu_id = m.menu_id);
