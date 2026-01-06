drop table if exists cms_file;
create table cms_file (
                                   file_id           bigint(20)      not null auto_increment    comment '文件ID',
                                   contract_id       bigint(20)      not null                   comment '关联合同ID',

                                   file_name         varchar(255)    not null                   comment '原始文件名',
                                   file_path         varchar(500)    not null                   comment '文件存储路径 (相对路径)',
                                   file_url          varchar(500)    default null               comment '完整访问URL',
                                   file_suffix       varchar(20)     default null               comment '文件后缀 (jpg, pdf, png)',
                                   file_size         bigint(20)      default 0                  comment '文件大小',

    -- 区分文件类型（可选，方便分类展示）
                                   file_category     char(1)         default '1'                comment '分类（1合同扫描件 2支付凭证 3其他）',

                                   create_by         varchar(64)     default ''                 comment '上传者',
                                   create_time       datetime                                   comment '上传时间',
                                   primary key (file_id)
) engine=innodb auto_increment=1 comment = '附件明细表';

-- 字典类型
insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time)
values ('附件分类', 'cms_file_category', '0', 'admin', sysdate());

-- 字典数据
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time) values
                                                                                                             (1, '合同扫描件', '1', 'cms_file_category', '0', 'admin', sysdate()),
                                                                                                             (2, '支付/转账截图', '2', 'cms_file_category', '0', 'admin', sysdate()),
                                                                                                             (3, '其他资料', '3', 'cms_file_category', '0', 'admin', sysdate());



ALTER TABLE cms_contract
    ADD COLUMN annex text DEFAULT NULL COMMENT '附件列表（JSON格式存储，包含文件ID和路径，用于列表快速展示）';