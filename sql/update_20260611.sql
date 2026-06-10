-- ============================================================
-- 合同管理：开放 manager/admin 修改/删除权限
-- 日期：2026-06-11
-- 说明：
--   1. 修正合同按钮菜单的 perms 字段与代码保持一致（system:contract:*）
--      之前可能因为菜单管理页面维护时改成 cms:contract:*，导致前端 v-hasPermi 失效
--   2. 给 manager 角色(role_id=2) 显式分配合同按钮权限
--   3. 给 admin 角色(role_id=1) 显式分配合同按钮权限（保险起见，避免依赖 * 通配）
--   4. 删除按钮已经二次确认（前端 $modal.confirm），不需修改
-- ============================================================

-- 1. 修正 sys_menu 中合同按钮的 perms 字符串（避免历史 cms: 前缀问题）
UPDATE sys_menu
SET perms = 'system:contract:list'
WHERE menu_name = '合同管理查询' AND perms != 'system:contract:list';

UPDATE sys_menu
SET perms = 'system:contract:query'
WHERE menu_name = '合同管理查询' AND perms != 'system:contract:query';

UPDATE sys_menu
SET perms = 'system:contract:add'
WHERE menu_name = '合同管理新增' AND perms != 'system:contract:add';

UPDATE sys_menu
SET perms = 'system:contract:edit'
WHERE menu_name = '合同管理修改' AND perms != 'system:contract:edit';

UPDATE sys_menu
SET perms = 'system:contract:remove'
WHERE menu_name = '合同管理删除' AND perms != 'system:contract:remove';

UPDATE sys_menu
SET perms = 'system:contract:export'
WHERE menu_name = '合同管理导出' AND perms != 'system:contract:export';

UPDATE sys_menu
SET perms = 'system:contract:import'
WHERE (menu_name = '合同管理导入' OR menu_name = '批量导入合同') AND perms != 'system:contract:import';

-- 2. 给 manager 角色（role_id=2）确保有所有合同按钮权限（按 perms 字符串匹配）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 2, m.menu_id
FROM sys_menu m
WHERE m.perms IN (
    'system:contract:list', 'system:contract:query',
    'system:contract:add', 'system:contract:edit', 'system:contract:remove',
    'system:contract:export', 'system:contract:import'
)
AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm
    WHERE rm.role_id = 2 AND rm.menu_id = m.menu_id
);

-- 3. 给 admin 角色（role_id=1）确保有所有合同按钮权限（保险起见）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT 1, m.menu_id
FROM sys_menu m
WHERE m.perms IN (
    'system:contract:list', 'system:contract:query',
    'system:contract:add', 'system:contract:edit', 'system:contract:remove',
    'system:contract:export', 'system:contract:import'
)
AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm
    WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
);
