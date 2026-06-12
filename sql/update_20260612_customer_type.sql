-- ============================================================================
-- 客户类型字典：原 2 项 label 升级为 公司/个体户 + 补 合伙企业/民办非
-- 日期：2026-06-12  适用范围：开发/测试环境  幂等：可反复执行
-- 约束：不修改 cms_customer 已有行（业务数据原样保留，UI 自动按新 label 翻译）
-- ============================================================================

-- 1) 槽位 160：企业 → 公司（value=1 不变，仅改 label/sort）
--    兼容：dev DB 上可能没有 160 槽位（首次部署），用 INSERT ... ON DUPLICATE KEY UPDATE 一次完成
INSERT INTO sys_dict_data
    (dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES
    (160, 1, '公司', '1', 'cms_customer_type', 'N', '0', 'admin', NOW(), '有限责任公司 / 股份有限公司等')
ON DUPLICATE KEY UPDATE
    dict_label = VALUES(dict_label),
    dict_sort  = VALUES(dict_sort),
    is_default = VALUES(is_default),
    remark     = VALUES(remark);

-- 2) 槽位 161：个人 → 个体户（value=2 不变，仅改 label/sort）
INSERT INTO sys_dict_data
    (dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES
    (161, 2, '个体户', '2', 'cms_customer_type', 'N', '0', 'admin', NOW(), '个体工商户')
ON DUPLICATE KEY UPDATE
    dict_label = VALUES(dict_label),
    dict_sort  = VALUES(dict_sort),
    is_default = VALUES(is_default),
    remark     = VALUES(remark);

-- 3) 162 / 163 新增（INSERT IGNORE 幂等）
INSERT IGNORE INTO sys_dict_data
    (dict_code, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES
    (162, 3, '合伙企业', '3', 'cms_customer_type', 'N', '0', 'admin', NOW(), '普通合伙 / 有限合伙'),
    (163, 4, '民办非',   '4', 'cms_customer_type', 'N', '0', 'admin', NOW(), '民办非企业单位');

-- 4) 不迁移 cms_customer 历史行（用户决策）

-- 5) 验证
-- SELECT dict_code, dict_sort, dict_label, dict_value, is_default
--   FROM sys_dict_data WHERE dict_type = 'cms_customer_type' ORDER BY dict_sort;
-- 期望 4 行：160=1=公司  161=2=个体户  162=3=合伙企业  163=4=民办非
