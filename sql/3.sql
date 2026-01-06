-- 假设你之前的父级菜单 '合同管理' 的 ID 是 2000 (如果不是，请替换为实际ID)
-- 1. 先将父级菜单确保为 'M' (目录) 类型，组件路径设为空或 Layout
UPDATE sys_menu
SET menu_type = 'M', component = 'Layout', path = 'contract', is_frame = 1
WHERE menu_id = 2010;

-- 2. 删除旧的单一 "合同列表" 菜单 (假设之前建过，避免重复)
DELETE FROM sys_menu WHERE parent_id = 2010 AND path = 'contract';

-- 3. 插入 "代账报税合同" 菜单
-- 注意：component 指向同一个文件 cms/contract/index
-- 注意：query 字段传入了参数 {"contractType": "1"}
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES
    ('代账报税合同', 2000, 1, 'accounting', 'cms/contract/index', '{"contractType": "1"}', 1, 0, 'C', '0', '0', 'cms:contract:list', 'documentation', 'admin', sysdate());

-- 4. 插入 "地址出售合同" 菜单
-- 注意：component 依然指向 cms/contract/index
-- 注意：query 字段传入了参数 {"contractType": "2"}
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES
    ('地址出售合同', 2000, 2, 'rental', 'cms/contract/index', '{"contractType": "2"}', 1, 0, 'C', '0', '0', 'cms:contract:list', 'guide', 'admin', sysdate());