-- 2026-01-17
-- Contract Collection and Renewal Process

-- 1. Modify cms_task table
ALTER TABLE `cms_task`
ADD COLUMN `source_contract_id` bigint(20) NULL COMMENT '关联发起的原合同' AFTER `contract_id`,
ADD COLUMN `target_contract_id` bigint(20) NULL COMMENT '关联续签后的新合同' AFTER `source_contract_id`,
MODIFY COLUMN `task_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '任务类型（''0''=普通, ''1''=催收）' AFTER `contract_id`,
MODIFY COLUMN `deadline` datetime NULL COMMENT '任务截止时间' AFTER `assigned_to`;

-- 2. Modify cms_contract table
ALTER TABLE `cms_contract`
ADD COLUMN `parent_id` bigint(20) NULL COMMENT '用于记录该合同是续签自哪个旧合同' AFTER `dept_id`,
ADD COLUMN `audit_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '审核状态（''0''=待审批, ''1''=通过, ''2''=驳回）' AFTER `status`;