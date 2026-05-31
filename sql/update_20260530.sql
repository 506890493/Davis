-- 统一已有软删除数据：客户表之前用 '1'，改为 '2'（与 RuoYi 框架惯例一致）
UPDATE cms_customer SET del_flag = '2' WHERE del_flag = '1';
