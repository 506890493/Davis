-- ----------------------------
-- 1. 合同主表 (集成若依规范)
-- ----------------------------
create table `davis-backend`.cms_contract
(
    contract_id        bigint auto_increment comment '合同ID'
        primary key,
    contract_code      varchar(64)                 null comment '合同编号',
    contract_name      varchar(100)                not null comment '合同/公司名称',
    contract_type      char                        not null comment '合同类型（字典：cms_contract_type 1代账报税 2地址出售）',
    legal_person       varchar(50)                 null comment '法人',
    contact_person     varchar(50)                 null comment '联系人',
    contact_phone      varchar(20)                 null comment '联系电话',
    contact_email      varchar(100)                null comment '联系邮箱',
    amount             decimal(10, 2) default 0.00 null comment '收费标准',
    payment_cycle      char                        null comment '付款周期（字典：cms_pay_cycle）',
    payment_date       datetime                    null comment '收款日期',
    payment_method     char                        null comment '收款方式（字典：cms_pay_method 1微信 2支付宝 3公户）',
    start_date         datetime                    null comment '合同开始日期',
    end_date           datetime                    null comment '合同结束日期',
    tax_type           char                        null comment '税务类型（字典：cms_tax_type）',
    establishment_date datetime                    null comment '成立日期',
    rental_address     varchar(255)                null comment '租赁地址',
    is_rented          char           default '0'  null comment '是否已出租（0否 1是）',
    profit             decimal(10, 2) default 0.00 null comment '利润',
    owner_id           bigint                      null comment '归属人ID (关联sys_user)',
    dept_id            bigint                      null comment '归属部门ID (用于数据权限)',
    status             char           default '0'  null comment '状态（字典：cms_contract_status 0正常 1停用 2过期）',
    del_flag           char           default '0'  null comment '删除标志（0代表存在 2代表删除）',
    create_by          varchar(64)    default ''   null comment '创建者',
    create_time        datetime                    null comment '创建时间',
    update_by          varchar(64)    default ''   null comment '更新者',
    update_time        datetime                    null comment '更新时间',
    remark             varchar(500)                null comment '备注',
    reminder_status    char           default '0'  null comment '催交状态（字典：cms_reminder_status）',
    annex              text                        null comment '附件列表（JSON格式存储，包含文件ID和路径，用于列表快速展示）'
)
    comment '合同管理表';





-- ----------------------------
-- 2. 任务表 (关联 sys_user 和 cms_contract)
-- ----------------------------
drop table if exists cms_task;
create table cms_task (
                          task_id           bigint(20)      not null auto_increment    comment '任务ID',
                          task_title        varchar(200)    not null                   comment '任务标题',
                          contract_id       bigint(20)      not null                   comment '关联同ID',

    -- 任务类型与属性
                          task_type         char(1)         default null               comment '任务类型（字典：cms_task_type）',
                          priority          char(1)         default '2'                comment '优先级（字典：cms_task_priority 1高 2中 3低）',

    -- 金额变动记录
                          original_amount   decimal(10,2)   default null               comment '原金额',
                          current_amount    decimal(10,2)   default null               comment '当前协商金额',

    -- 执行流
                          assigned_to       bigint(20)      not null                   comment '执行人ID (关联sys_user)',
                          deadline          datetime                                   comment '截止时间',
                          status            char(2)         default '0'                comment '任务状态（字典：cms_task_status 0待处理 1进行中 2待审批 3已退回 4已完成）',

    -- 若依标准字段
                          del_flag          char(1)         default '0'                comment '删除标志',
                          create_by         varchar(64)     default ''                 comment '创建者',
                          create_time       datetime                                   comment '创建时间',
                          update_by         varchar(64)     default ''                 comment '更新者',
                          update_time       datetime                                   comment '更新时间',
                          remark            varchar(500)    default null               comment '任务描述/备注',
                          primary key (task_id)
) engine=innodb auto_increment=1 comment = '任务管理表';

-- ----------------------------
-- 3. 审批申请表 (支持 JSON 存储快照)
-- ----------------------------
drop table if exists cms_approval;
create table cms_approval (
                              approval_id       bigint(20)      not null auto_increment    comment '审批ID',
                              apply_no          varchar(64)     default null               comment '申请编号',

    -- 关联信息
                              applicant_id      bigint(20)      not null                   comment '申请人ID (sys_user)',
                              contract_id       bigint(20)      default null               comment '关联原合同ID',
                              task_id           bigint(20)      default null               comment '关联任务ID',

                              approval_type     char(1)         not null                   comment '审批类型（字典：cms_approval_type 1新合同 2续费 3变更）',

    -- 核心数据快照 (存储JSON字符串，记录修改前后的数据)
                              content_snapshot  json            default null               comment '申请内容快照',

    -- 审批结果
                              status            char(1)         default '0'                comment '审批状态（0待审批 1通过 2拒绝）',
                              approver_id       bigint(20)      default null               comment '审批人ID',
                              approval_time     datetime                                   comment '审批时间',
                              approval_msg      varchar(500)    default null               comment '审批意见',

    -- 若依标准字段
                              create_by         varchar(64)     default ''                 comment '创建者',
                              create_time       datetime                                   comment '创建时间',
                              update_by         varchar(64)     default ''                 comment '更新者',
                              update_time       datetime                                   comment '更新时间',
                              primary key (approval_id)
) engine=innodb auto_increment=1 comment = '业务审批表';

-- ----------------------------
-- 4. 沟通记录表
-- ----------------------------
drop table if exists cms_communication;
create table cms_communication (
                                   comm_id           bigint(20)      not null auto_increment    comment '记录ID',
                                   task_id           bigint(20)      not null                   comment '关联任务ID',
                                   user_id           bigint(20)      not null                   comment '记录人ID',
                                   comm_type         char(1)         default '0'                comment '沟通方式（字典：cms_comm_type 0电话 1微信 2邮件 3面谈）',
                                   content           text            not null                   comment '沟通内容',

                                   create_time       datetime        default null               comment '记录时间',
                                   primary key (comm_id)
) engine=innodb auto_increment=1 comment = '任务沟通记录表';

-- ----------------------------
-- 5. 附件表 (若依通常有 sys_oss，如果没有则用此表)
-- ----------------------------

create table `davis-backend`.cms_file
(
    file_id       bigint auto_increment comment '文件ID'
        primary key,
    contract_id   bigint                  not null comment '关联合同ID',
    file_name     varchar(255)            not null comment '原始文件名',
    file_path     varchar(500)            not null comment '文件存储路径 (相对路径)',
    file_url      varchar(500)            null comment '完整访问URL',
    file_suffix   varchar(20)             null comment '文件后缀 (jpg, pdf, png)',
    file_size     bigint      default 0   null comment '文件大小',
    file_category char        default '1' null comment '分类（1合同扫描件 2支付凭证 3其他）',
    create_by     varchar(64) default ''  null comment '上传者',
    create_time   datetime                null comment '上传时间'
)
    comment '附件明细表';




insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time) values
                                                                                     ('合同类型', 'cms_contract_type', '0', 'admin', sysdate()),
                                                                                     ('付款周期', 'cms_pay_cycle', '0', 'admin', sysdate()),
                                                                                     ('税务类型', 'cms_tax_type', '0', 'admin', sysdate()),
                                                                                     ('合同状态', 'cms_contract_status', '0', 'admin', sysdate()),
                                                                                     ('任务优先级', 'cms_task_priority', '0', 'admin', sysdate()),
                                                                                     ('任务状态', 'cms_task_status', '0', 'admin', sysdate());

-- 合同类型
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time) values
                                                                                                                         (1, '代账合同', '1', 'cms_contract_type', 'Y', '0', 'admin', sysdate()),
                                                                                                                         (2, '租赁合同', '2', 'cms_contract_type', 'N', '0', 'admin', sysdate());

-- 付款周期
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, create_by, create_time) values
                                                                                                     (1, '月付', '1', 'cms_pay_cycle', 'admin', sysdate()),
                                                                                                     (2, '季付', '2', 'cms_pay_cycle', 'admin', sysdate()),
                                                                                                     (3, '半年付', '3', 'cms_pay_cycle', 'admin', sysdate()),
                                                                                                     (4, '年付', '4', 'cms_pay_cycle', 'admin', sysdate());

-- 任务状态
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, create_by, create_time) values
                                                                                                                 (1, '待处理', '0', 'cms_task_status', 'info', 'admin', sysdate()),
                                                                                                                 (2, '进行中', '1', 'cms_task_status', 'primary', 'admin', sysdate()),
                                                                                                                 (3, '待审批', '2', 'cms_task_status', 'warning', 'admin', sysdate()),
                                                                                                                 (4, '已退回', '3', 'cms_task_status', 'danger', 'admin', sysdate()),
                                                                                                                 (5, '已完成', '4', 'cms_task_status', 'success', 'admin', sysdate());
# gemini 3.0 pro start
-- 1. 顶级菜单：合同管理
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, menu_type, visible, status, perms, icon, create_by, create_time)
values('合同管理', 0, 5, 'cms', null, 1, 'M', '0', '0', '', 'form', 'admin', sysdate());

-- 获取刚才插入的菜单ID (假设是 2000, 实际操作中请查询或使用变量)
-- set @parentId = 2000;

-- 2. 二级菜单：合同列表
insert into sys_menu (menu_name, parent_id, order_num, path, component, menu_type, perms, icon, create_by, create_time)
values('合同列表', 2000, 1, 'contract', 'cms/contract/index', 'C', 'cms:contract:list', 'list', 'admin', sysdate());

-- 3. 二级菜单：任务管理
insert into sys_menu (menu_name, parent_id, order_num, path, component, menu_type, perms, icon, create_by, create_time)
values('任务管理', 2000, 2, 'task', 'cms/task/index', 'C', 'cms:task:list', 'checkbox', 'admin', sysdate());

-- 4. 二级菜单：审批中心
insert into sys_menu (menu_name, parent_id, order_num, path, component, menu_type, perms, icon, create_by, create_time)
values('审批中心', 2000, 3, 'approval', 'cms/approval/index', 'C', 'cms:approval:list', 'drag', 'admin', sysdate());

# ('业务审批导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'system:approval:export',       '#', 'admin', sysdate(), '', null, '');
# gemini 3.0 pro start

-- 1. 插入字典类型
insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time)
values ('收款方式', 'cms_pay_method', '0', 'admin', sysdate());

-- 2. 插入字典数据 (微信、支付宝、公帐)
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time) values
(1, '微信支付', '1', 'cms_pay_method', '0', 'admin', sysdate()),
(2, '支付宝',   '2', 'cms_pay_method', '0', 'admin', sysdate()),
(3, '对公转账', '3', 'cms_pay_method', '0', 'admin', sysdate());


-- 1. 先清理旧的测试数据（如果需要）
DELETE FROM sys_dict_data WHERE dict_type = 'cms_tax_type';

-- 2. 插入整理后的标准税务类型
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark) values
(1, '一般纳税人',   '1', 'cms_tax_type', 'primary', '0', 'admin', sysdate(), '包含：一般纳税人、新升级一般纳税人'),(2, '小规模纳税人', '2', 'cms_tax_type', 'info',    '0', 'admin', sysdate(), '包含：小规模、小规模(有票)'),(3, '个体工商户',   '3', 'cms_tax_type', 'warning', '0', 'admin', sysdate(), '包含：个体户、核定征收');