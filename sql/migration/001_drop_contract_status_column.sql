-- ============================================================
-- 迁移脚本：删除 cms_contract 表中的 status 物理字段
-- 日期：2026-02-25
-- 说明：合同状态（未开始/进行中/即将到期/已过期）改为
--       基于 start_date 和 end_date 动态计算，不再物理存储。
--       sys_dict_data 中 cms_contract_status 字典数据保留，
--       前端展示仍引用该字典进行标签/样式渲染。
-- ============================================================

-- 1. 删除 cms_contract 表中的 status 字段
ALTER TABLE cms_contract DROP COLUMN status;
