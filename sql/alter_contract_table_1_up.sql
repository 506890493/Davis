ALTER TABLE cms_contract
    ADD COLUMN reminder_status char(1) DEFAULT '0' COMMENT '催交状态（字典：cms_reminder_status）';

-- 1. 插入字典类型
insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time)
values ('催交状态', 'cms_reminder_status', '0', 'admin', sysdate());

-- 2. 插入字典数据
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time) values
                                                                                                                         (1, '未催交',   '0', 'cms_reminder_status', 'info',    '0', 'admin', sysdate()),
                                                                                                                         (2, '已催交',   '1', 'cms_reminder_status', 'warning', '0', 'admin', sysdate()),
                                                                                                                         (3, '催交完成', '2', 'cms_reminder_status', 'success', '0', 'admin', sysdate());