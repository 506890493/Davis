-- 客户表
CREATE TABLE IF NOT EXISTS `cms_customer` (
    `customer_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '客户ID',
    `customer_name` VARCHAR(100) NOT NULL COMMENT '客户名称',
    `customer_type` VARCHAR(10) DEFAULT NULL COMMENT '客户类型 dict_value: 1=公司 2=个体户 3=合伙企业 4=民办非',
    `contact_person` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `contact_email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `owner_id` BIGINT(20) DEFAULT NULL COMMENT '归属销售',
    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- 修改合同表，增加customer_id和actual_amount字段
ALTER TABLE `cms_contract` ADD COLUMN `customer_id` BIGINT(20) COMMENT '关联客户ID' AFTER `parent_id`;
ALTER TABLE `cms_contract` ADD COLUMN `actual_amount` DECIMAL(12,2) COMMENT '实际收款金额' AFTER `amount`;

-- 修改任务表，增加字段
ALTER TABLE `cms_task` ADD COLUMN `task_type` VARCHAR(20) DEFAULT '催缴' COMMENT '任务类型' AFTER `task_status`;
ALTER TABLE `cms_task` ADD COLUMN `original_amount` DECIMAL(12,2) COMMENT '原合同金额' AFTER `current_amount`;
ALTER TABLE `cms_task` ADD COLUMN `actual_amount` DECIMAL(12,2) COMMENT '实际收款金额' AFTER `original_amount`;
ALTER TABLE `cms_task` ADD COLUMN `receive_remark` VARCHAR(500) COMMENT '收款备注' AFTER `actual_amount`;