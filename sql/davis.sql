create table QRTZ_CALENDARS
(
    sched_name    varchar(120) not null comment '调度名称',
    calendar_name varchar(200) not null comment '日历名称',
    calendar      blob         not null comment '存放持久化calendar对象',
    primary key (sched_name, calendar_name)
)
    comment '日历信息表';

create table QRTZ_FIRED_TRIGGERS
(
    sched_name        varchar(120) not null comment '调度名称',
    entry_id          varchar(95)  not null comment '调度器实例id',
    trigger_name      varchar(200) not null comment 'qrtz_triggers表trigger_name的外键',
    trigger_group     varchar(200) not null comment 'qrtz_triggers表trigger_group的外键',
    instance_name     varchar(200) not null comment '调度器实例名',
    fired_time        bigint       not null comment '触发的时间',
    sched_time        bigint       not null comment '定时器制定的时间',
    priority          int          not null comment '优先级',
    state             varchar(16)  not null comment '状态',
    job_name          varchar(200) null comment '任务名称',
    job_group         varchar(200) null comment '任务组名',
    is_nonconcurrent  varchar(1)   null comment '是否并发',
    requests_recovery varchar(1)   null comment '是否接受恢复执行',
    primary key (sched_name, entry_id)
)
    comment '已触发的触发器表';

create table QRTZ_JOB_DETAILS
(
    sched_name        varchar(120) not null comment '调度名称',
    job_name          varchar(200) not null comment '任务名称',
    job_group         varchar(200) not null comment '任务组名',
    description       varchar(250) null comment '相关介绍',
    job_class_name    varchar(250) not null comment '执行任务类名称',
    is_durable        varchar(1)   not null comment '是否持久化',
    is_nonconcurrent  varchar(1)   not null comment '是否并发',
    is_update_data    varchar(1)   not null comment '是否更新数据',
    requests_recovery varchar(1)   not null comment '是否接受恢复执行',
    job_data          blob         null comment '存放持久化job对象',
    primary key (sched_name, job_name, job_group)
)
    comment '任务详细信息表';

create table QRTZ_LOCKS
(
    sched_name varchar(120) not null comment '调度名称',
    lock_name  varchar(40)  not null comment '悲观锁名称',
    primary key (sched_name, lock_name)
)
    comment '存储的悲观锁信息表';

create table QRTZ_PAUSED_TRIGGER_GRPS
(
    sched_name    varchar(120) not null comment '调度名称',
    trigger_group varchar(200) not null comment 'qrtz_triggers表trigger_group的外键',
    primary key (sched_name, trigger_group)
)
    comment '暂停的触发器表';

create table QRTZ_SCHEDULER_STATE
(
    sched_name        varchar(120) not null comment '调度名称',
    instance_name     varchar(200) not null comment '实例名称',
    last_checkin_time bigint       not null comment '上次检查时间',
    checkin_interval  bigint       not null comment '检查间隔时间',
    primary key (sched_name, instance_name)
)
    comment '调度器状态表';

create table QRTZ_TRIGGERS
(
    sched_name     varchar(120) not null comment '调度名称',
    trigger_name   varchar(200) not null comment '触发器的名字',
    trigger_group  varchar(200) not null comment '触发器所属组的名字',
    job_name       varchar(200) not null comment 'qrtz_job_details表job_name的外键',
    job_group      varchar(200) not null comment 'qrtz_job_details表job_group的外键',
    description    varchar(250) null comment '相关介绍',
    next_fire_time bigint       null comment '上一次触发时间（毫秒）',
    prev_fire_time bigint       null comment '下一次触发时间（默认为-1表示不触发）',
    priority       int          null comment '优先级',
    trigger_state  varchar(16)  not null comment '触发器状态',
    trigger_type   varchar(8)   not null comment '触发器的类型',
    start_time     bigint       not null comment '开始时间',
    end_time       bigint       null comment '结束时间',
    calendar_name  varchar(200) null comment '日程表名称',
    misfire_instr  smallint     null comment '补偿执行的策略',
    job_data       blob         null comment '存放持久化job对象',
    primary key (sched_name, trigger_name, trigger_group),
    constraint qrtz_triggers_ibfk_1
        foreign key (sched_name, job_name, job_group) references QRTZ_JOB_DETAILS (sched_name, job_name, job_group)
)
    comment '触发器详细信息表';

create table QRTZ_BLOB_TRIGGERS
(
    sched_name    varchar(120) not null comment '调度名称',
    trigger_name  varchar(200) not null comment 'qrtz_triggers表trigger_name的外键',
    trigger_group varchar(200) not null comment 'qrtz_triggers表trigger_group的外键',
    blob_data     blob         null comment '存放持久化Trigger对象',
    primary key (sched_name, trigger_name, trigger_group),
    constraint qrtz_blob_triggers_ibfk_1
        foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS (sched_name, trigger_name, trigger_group)
)
    comment 'Blob类型的触发器表';

create table QRTZ_CRON_TRIGGERS
(
    sched_name      varchar(120) not null comment '调度名称',
    trigger_name    varchar(200) not null comment 'qrtz_triggers表trigger_name的外键',
    trigger_group   varchar(200) not null comment 'qrtz_triggers表trigger_group的外键',
    cron_expression varchar(200) not null comment 'cron表达式',
    time_zone_id    varchar(80)  null comment '时区',
    primary key (sched_name, trigger_name, trigger_group),
    constraint qrtz_cron_triggers_ibfk_1
        foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS (sched_name, trigger_name, trigger_group)
)
    comment 'Cron类型的触发器表';

create table QRTZ_SIMPLE_TRIGGERS
(
    sched_name      varchar(120) not null comment '调度名称',
    trigger_name    varchar(200) not null comment 'qrtz_triggers表trigger_name的外键',
    trigger_group   varchar(200) not null comment 'qrtz_triggers表trigger_group的外键',
    repeat_count    bigint       not null comment '重复的次数统计',
    repeat_interval bigint       not null comment '重复的间隔时间',
    times_triggered bigint       not null comment '已经触发的次数',
    primary key (sched_name, trigger_name, trigger_group),
    constraint qrtz_simple_triggers_ibfk_1
        foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS (sched_name, trigger_name, trigger_group)
)
    comment '简单触发器的信息表';

create table QRTZ_SIMPROP_TRIGGERS
(
    sched_name    varchar(120)   not null comment '调度名称',
    trigger_name  varchar(200)   not null comment 'qrtz_triggers表trigger_name的外键',
    trigger_group varchar(200)   not null comment 'qrtz_triggers表trigger_group的外键',
    str_prop_1    varchar(512)   null comment 'String类型的trigger的第一个参数',
    str_prop_2    varchar(512)   null comment 'String类型的trigger的第二个参数',
    str_prop_3    varchar(512)   null comment 'String类型的trigger的第三个参数',
    int_prop_1    int            null comment 'int类型的trigger的第一个参数',
    int_prop_2    int            null comment 'int类型的trigger的第二个参数',
    long_prop_1   bigint         null comment 'long类型的trigger的第一个参数',
    long_prop_2   bigint         null comment 'long类型的trigger的第二个参数',
    dec_prop_1    decimal(13, 4) null comment 'decimal类型的trigger的第一个参数',
    dec_prop_2    decimal(13, 4) null comment 'decimal类型的trigger的第二个参数',
    bool_prop_1   varchar(1)     null comment 'Boolean类型的trigger的第一个参数',
    bool_prop_2   varchar(1)     null comment 'Boolean类型的trigger的第二个参数',
    primary key (sched_name, trigger_name, trigger_group),
    constraint qrtz_simprop_triggers_ibfk_1
        foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS (sched_name, trigger_name, trigger_group)
)
    comment '同步机制的行锁表';

create index sched_name
    on QRTZ_TRIGGERS (sched_name, job_name, job_group);

create table cms_approval
(
    approval_id      bigint auto_increment comment '审批ID'
        primary key,
    apply_no         varchar(64)             null comment '申请编号',
    applicant_id     bigint                  not null comment '申请人ID (sys_user)',
    contract_id      bigint                  null comment '关联原合同ID',
    task_id          bigint                  null comment '关联任务ID',
    approval_type    char                    not null comment '审批类型（字典：cms_approval_type 1新合同 2续费 3变更）',
    content_snapshot json                    null comment '申请内容快照',
    status           char        default '0' null comment '审批状态（0待审批 1通过 2拒绝）',
    approver_id      bigint                  null comment '审批人ID',
    approval_time    datetime                null comment '审批时间',
    approval_msg     varchar(500)            null comment '审批意见',
    create_by        varchar(64) default ''  null comment '创建者',
    create_time      datetime                null comment '创建时间',
    update_by        varchar(64) default ''  null comment '更新者',
    update_time      datetime                null comment '更新时间'
)
    comment '业务审批表';

create table if not exists cms_communication (
  communication_id   bigint(20)  not null auto_increment comment '沟通记录ID',
  contract_id        bigint(20)  not null comment '合同ID',
  user_id            bigint(20)  comment '操作人ID',
  content            text        comment '沟通内容',
  communication_type char(1)     comment '类型(0=电话 1=微信 2=面谈)',
  create_by          varchar(64) default '' comment '创建者',
  create_time        datetime    comment '创建时间',
  remark             varchar(500) default null comment '备注',
  primary key (communication_id)
) engine=innodb auto_increment=1 comment = '沟通记录表';

create table cms_contract
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
    parent_id          bigint                      null comment '用于记录该合同是续签自哪个旧合同',
    del_flag           char           default '0'  null comment '删除标志（0代表存在 2代表删除）',
    create_by          varchar(64)    default ''   null comment '创建者',
    create_time        datetime                    null comment '创建时间',
    update_by          varchar(64)    default ''   null comment '更新者',
    update_time        datetime                    null comment '更新时间',
    remark             varchar(500)                null comment '备注',
    reminder_status    char           default '0'  null comment '催交状态（字典：cms_reminder_status）',
    annex              text                        null comment '附件列表（JSON格式存储，包含文件ID和路径，用于列表快速展示）',
    audit_status       char           default '0'  null comment '审核状态（''0''=待审批, ''1''=通过, ''2''=驳回）'
)
    comment '合同管理表';

create table cms_file
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

create table cms_task
(
    task_id            bigint auto_increment comment '任务ID'
        primary key,
    task_title         varchar(200)            not null comment '任务标题',
    contract_id        bigint                  not null comment '关联同ID',
    task_type          char        default '0' null comment '任务类型（''0''=普通, ''1''=催收）',
    source_contract_id bigint                  null comment '关联发起的原合同',
    target_contract_id bigint                  null comment '关联续签后的新合同',
    priority           char        default '2' null comment '优先级（字典：cms_task_priority 1高 2中 3低）',
    original_amount    decimal(10, 2)          null comment '原金额',
    current_amount     decimal(10, 2)          null comment '当前协商金额',
    assigned_to        bigint                  not null comment '执行人ID (关联sys_user)',
    deadline           datetime                null comment '任务截止时间',
    status             char(2)     default '0' null comment '任务状态（字典：cms_task_status 0待处理 1进行中 2待审批 3已退回 4已完成）',
    del_flag           char        default '0' null comment '删除标志',
    create_by          varchar(64) default ''  null comment '创建者',
    create_time        datetime                null comment '创建时间',
    update_by          varchar(64) default ''  null comment '更新者',
    update_time        datetime                null comment '更新时间',
    remark             varchar(500)            null comment '任务描述/备注'
)
    comment '任务管理表';

create table gen_table
(
    table_id          bigint auto_increment comment '编号'
        primary key,
    table_name        varchar(200) default ''     null comment '表名称',
    table_comment     varchar(500) default ''     null comment '表描述',
    sub_table_name    varchar(64)                 null comment '关联子表的表名',
    sub_table_fk_name varchar(64)                 null comment '子表关联的外键名',
    class_name        varchar(100) default ''     null comment '实体类名称',
    tpl_category      varchar(200) default 'crud' null comment '使用的模板（crud单表操作 tree树表操作）',
    tpl_web_type      varchar(30)  default ''     null comment '前端模板类型（element-ui模版 element-plus模版）',
    package_name      varchar(100)                null comment '生成包路径',
    module_name       varchar(30)                 null comment '生成模块名',
    business_name     varchar(30)                 null comment '生成业务名',
    function_name     varchar(50)                 null comment '生成功能名',
    function_author   varchar(50)                 null comment '生成功能作者',
    gen_type          char         default '0'    null comment '生成代码方式（0zip压缩包 1自定义路径）',
    gen_path          varchar(200) default '/'    null comment '生成路径（不填默认项目路径）',
    options           varchar(1000)               null comment '其它生成选项',
    create_by         varchar(64)  default ''     null comment '创建者',
    create_time       datetime                    null comment '创建时间',
    update_by         varchar(64)  default ''     null comment '更新者',
    update_time       datetime                    null comment '更新时间',
    remark            varchar(500)                null comment '备注'
)
    comment '代码生成业务表';

create table gen_table_column
(
    column_id      bigint auto_increment comment '编号'
        primary key,
    table_id       bigint                    null comment '归属表编号',
    column_name    varchar(200)              null comment '列名称',
    column_comment varchar(500)              null comment '列描述',
    column_type    varchar(100)              null comment '列类型',
    java_type      varchar(500)              null comment 'JAVA类型',
    java_field     varchar(200)              null comment 'JAVA字段名',
    is_pk          char                      null comment '是否主键（1是）',
    is_increment   char                      null comment '是否自增（1是）',
    is_required    char                      null comment '是否必填（1是）',
    is_insert      char                      null comment '是否为插入字段（1是）',
    is_edit        char                      null comment '是否编辑字段（1是）',
    is_list        char                      null comment '是否列表字段（1是）',
    is_query       char                      null comment '是否查询字段（1是）',
    query_type     varchar(200) default 'EQ' null comment '查询方式（等于、不等于、大于、小于、范围）',
    html_type      varchar(200)              null comment '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
    dict_type      varchar(200) default ''   null comment '字典类型',
    sort           int                       null comment '排序',
    create_by      varchar(64)  default ''   null comment '创建者',
    create_time    datetime                  null comment '创建时间',
    update_by      varchar(64)  default ''   null comment '更新者',
    update_time    datetime                  null comment '更新时间'
)
    comment '代码生成业务表字段';

create table sys_config
(
    config_id    int auto_increment comment '参数主键'
        primary key,
    config_name  varchar(100) default ''  null comment '参数名称',
    config_key   varchar(100) default ''  null comment '参数键名',
    config_value varchar(500) default ''  null comment '参数键值',
    config_type  char         default 'N' null comment '系统内置（Y是 N否）',
    create_by    varchar(64)  default ''  null comment '创建者',
    create_time  datetime                 null comment '创建时间',
    update_by    varchar(64)  default ''  null comment '更新者',
    update_time  datetime                 null comment '更新时间',
    remark       varchar(500)             null comment '备注'
)
    comment '参数配置表';

create table sys_dept
(
    dept_id     bigint auto_increment comment '部门id'
        primary key,
    parent_id   bigint      default 0   null comment '父部门id',
    ancestors   varchar(50) default ''  null comment '祖级列表',
    dept_name   varchar(30) default ''  null comment '部门名称',
    order_num   int         default 0   null comment '显示顺序',
    leader      varchar(20)             null comment '负责人',
    phone       varchar(11)             null comment '联系电话',
    email       varchar(50)             null comment '邮箱',
    status      char        default '0' null comment '部门状态（0正常 1停用）',
    del_flag    char        default '0' null comment '删除标志（0代表存在 2代表删除）',
    create_by   varchar(64) default ''  null comment '创建者',
    create_time datetime                null comment '创建时间',
    update_by   varchar(64) default ''  null comment '更新者',
    update_time datetime                null comment '更新时间'
)
    comment '部门表';

create table sys_dict_data
(
    dict_code   bigint auto_increment comment '字典编码'
        primary key,
    dict_sort   int          default 0   null comment '字典排序',
    dict_label  varchar(100) default ''  null comment '字典标签',
    dict_value  varchar(100) default ''  null comment '字典键值',
    dict_type   varchar(100) default ''  null comment '字典类型',
    css_class   varchar(100)             null comment '样式属性（其他样式扩展）',
    list_class  varchar(100)             null comment '表格回显样式',
    is_default  char         default 'N' null comment '是否默认（Y是 N否）',
    status      char         default '0' null comment '状态（0正常 1停用）',
    create_by   varchar(64)  default ''  null comment '创建者',
    create_time datetime                 null comment '创建时间',
    update_by   varchar(64)  default ''  null comment '更新者',
    update_time datetime                 null comment '更新时间',
    remark      varchar(500)             null comment '备注'
)
    comment '字典数据表';

create table sys_dict_type
(
    dict_id     bigint auto_increment comment '字典主键'
        primary key,
    dict_name   varchar(100) default ''  null comment '字典名称',
    dict_type   varchar(100) default ''  null comment '字典类型',
    status      char         default '0' null comment '状态（0正常 1停用）',
    create_by   varchar(64)  default ''  null comment '创建者',
    create_time datetime                 null comment '创建时间',
    update_by   varchar(64)  default ''  null comment '更新者',
    update_time datetime                 null comment '更新时间',
    remark      varchar(500)             null comment '备注',
    constraint dict_type
        unique (dict_type)
)
    comment '字典类型表';

create table sys_file_info
(
    file_id   int auto_increment comment '文件id'
        primary key,
    file_name varchar(50)  default '' null comment '文件名称',
    file_path varchar(255) default '' null comment '文件路径'
)
    comment '文件信息表' charset = utf8mb3;

create table sys_job
(
    job_id          bigint auto_increment comment '任务ID',
    job_name        varchar(64)  default ''        not null comment '任务名称',
    job_group       varchar(64)  default 'DEFAULT' not null comment '任务组名',
    invoke_target   varchar(500)                   not null comment '调用目标字符串',
    cron_expression varchar(255) default ''        null comment 'cron执行表达式',
    misfire_policy  varchar(20)  default '3'       null comment '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
    concurrent      char         default '1'       null comment '是否并发执行（0允许 1禁止）',
    status          char         default '0'       null comment '状态（0正常 1暂停）',
    create_by       varchar(64)  default ''        null comment '创建者',
    create_time     datetime                       null comment '创建时间',
    update_by       varchar(64)  default ''        null comment '更新者',
    update_time     datetime                       null comment '更新时间',
    remark          varchar(500) default ''        null comment '备注信息',
    primary key (job_id, job_name, job_group)
)
    comment '定时任务调度表';

create table sys_job_log
(
    job_log_id     bigint auto_increment comment '任务日志ID'
        primary key,
    job_name       varchar(64)               not null comment '任务名称',
    job_group      varchar(64)               not null comment '任务组名',
    invoke_target  varchar(500)              not null comment '调用目标字符串',
    job_message    varchar(500)              null comment '日志信息',
    status         char          default '0' null comment '执行状态（0正常 1失败）',
    exception_info varchar(2000) default ''  null comment '异常信息',
    create_time    datetime                  null comment '创建时间'
)
    comment '定时任务调度日志表';

create table sys_logininfor
(
    info_id        bigint auto_increment comment '访问ID'
        primary key,
    user_name      varchar(50)  default ''  null comment '用户账号',
    ipaddr         varchar(128) default ''  null comment '登录IP地址',
    login_location varchar(255) default ''  null comment '登录地点',
    browser        varchar(50)  default ''  null comment '浏览器类型',
    os             varchar(50)  default ''  null comment '操作系统',
    status         char         default '0' null comment '登录状态（0成功 1失败）',
    msg            varchar(255) default ''  null comment '提示消息',
    login_time     datetime                 null comment '访问时间'
)
    comment '系统访问记录';

create index idx_sys_logininfor_lt
    on sys_logininfor (login_time);

create index idx_sys_logininfor_s
    on sys_logininfor (status);

create table sys_menu
(
    menu_id     bigint auto_increment comment '菜单ID'
        primary key,
    menu_name   varchar(50)              not null comment '菜单名称',
    parent_id   bigint       default 0   null comment '父菜单ID',
    order_num   int          default 0   null comment '显示顺序',
    path        varchar(200) default ''  null comment '路由地址',
    component   varchar(255)             null comment '组件路径',
    query       varchar(255)             null comment '路由参数',
    route_name  varchar(50)  default ''  null comment '路由名称',
    is_frame    int          default 1   null comment '是否为外链（0是 1否）',
    is_cache    int          default 0   null comment '是否缓存（0缓存 1不缓存）',
    menu_type   char         default ''  null comment '菜单类型（M目录 C菜单 F按钮）',
    visible     char         default '0' null comment '菜单状态（0显示 1隐藏）',
    status      char         default '0' null comment '菜单状态（0正常 1停用）',
    perms       varchar(100)             null comment '权限标识',
    icon        varchar(100) default '#' null comment '菜单图标',
    create_by   varchar(64)  default ''  null comment '创建者',
    create_time datetime                 null comment '创建时间',
    update_by   varchar(64)  default ''  null comment '更新者',
    update_time datetime                 null comment '更新时间',
    remark      varchar(500) default ''  null comment '备注'
)
    comment '菜单权限表';

create table sys_notice
(
    notice_id      int auto_increment comment '公告ID'
        primary key,
    notice_title   varchar(50)             not null comment '公告标题',
    notice_type    char                    not null comment '公告类型（1通知 2公告）',
    notice_content longblob                null comment '公告内容',
    status         char        default '0' null comment '公告状态（0正常 1关闭）',
    create_by      varchar(64) default ''  null comment '创建者',
    create_time    datetime                null comment '创建时间',
    update_by      varchar(64) default ''  null comment '更新者',
    update_time    datetime                null comment '更新时间',
    remark         varchar(255)            null comment '备注'
)
    comment '通知公告表';

create table sys_oper_log
(
    oper_id        bigint auto_increment comment '日志主键'
        primary key,
    title          varchar(50)   default '' null comment '模块标题',
    business_type  int           default 0  null comment '业务类型（0其它 1新增 2修改 3删除）',
    method         varchar(200)  default '' null comment '方法名称',
    request_method varchar(10)   default '' null comment '请求方式',
    operator_type  int           default 0  null comment '操作类别（0其它 1后台用户 2手机端用户）',
    oper_name      varchar(50)   default '' null comment '操作人员',
    dept_name      varchar(50)   default '' null comment '部门名称',
    oper_url       varchar(255)  default '' null comment '请求URL',
    oper_ip        varchar(128)  default '' null comment '主机地址',
    oper_location  varchar(255)  default '' null comment '操作地点',
    oper_param     varchar(2000) default '' null comment '请求参数',
    json_result    varchar(2000) default '' null comment '返回参数',
    status         int           default 0  null comment '操作状态（0正常 1异常）',
    error_msg      varchar(2000) default '' null comment '错误消息',
    oper_time      datetime                 null comment '操作时间',
    cost_time      bigint        default 0  null comment '消耗时间'
)
    comment '操作日志记录';

create index idx_sys_oper_log_bt
    on sys_oper_log (business_type);

create index idx_sys_oper_log_ot
    on sys_oper_log (oper_time);

create index idx_sys_oper_log_s
    on sys_oper_log (status);

create table sys_post
(
    post_id     bigint auto_increment comment '岗位ID'
        primary key,
    post_code   varchar(64)            not null comment '岗位编码',
    post_name   varchar(50)            not null comment '岗位名称',
    post_sort   int                    not null comment '显示顺序',
    status      char                   not null comment '状态（0正常 1停用）',
    create_by   varchar(64) default '' null comment '创建者',
    create_time datetime               null comment '创建时间',
    update_by   varchar(64) default '' null comment '更新者',
    update_time datetime               null comment '更新时间',
    remark      varchar(500)           null comment '备注'
)
    comment '岗位信息表';

create table sys_role
(
    role_id             bigint auto_increment comment '角色ID'
        primary key,
    role_name           varchar(30)             not null comment '角色名称',
    role_key            varchar(100)            not null comment '角色权限字符串',
    role_sort           int                     not null comment '显示顺序',
    data_scope          char        default '1' null comment '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
    menu_check_strictly tinyint(1)  default 1   null comment '菜单树选择项是否关联显示',
    dept_check_strictly tinyint(1)  default 1   null comment '部门树选择项是否关联显示',
    status              char                    not null comment '角色状态（0正常 1停用）',
    del_flag            char        default '0' null comment '删除标志（0代表存在 2代表删除）',
    create_by           varchar(64) default ''  null comment '创建者',
    create_time         datetime                null comment '创建时间',
    update_by           varchar(64) default ''  null comment '更新者',
    update_time         datetime                null comment '更新时间',
    remark              varchar(500)            null comment '备注'
)
    comment '角色信息表';

create table sys_role_dept
(
    role_id bigint not null comment '角色ID',
    dept_id bigint not null comment '部门ID',
    primary key (role_id, dept_id)
)
    comment '角色和部门关联表';

create table sys_role_menu
(
    role_id bigint not null comment '角色ID',
    menu_id bigint not null comment '菜单ID',
    primary key (role_id, menu_id)
)
    comment '角色和菜单关联表';

create table sys_user
(
    user_id         bigint auto_increment comment '用户ID'
        primary key,
    dept_id         bigint                    null comment '部门ID',
    user_name       varchar(30)               not null comment '用户账号',
    nick_name       varchar(30)               not null comment '用户昵称',
    user_type       varchar(2)   default '00' null comment '用户类型（00系统用户）',
    email           varchar(50)  default ''   null comment '用户邮箱',
    phonenumber     varchar(11)  default ''   null comment '手机号码',
    sex             char         default '0'  null comment '用户性别（0男 1女 2未知）',
    avatar          varchar(100) default ''   null comment '头像地址',
    password        varchar(100) default ''   null comment '密码',
    status          char         default '0'  null comment '账号状态（0正常 1停用）',
    del_flag        char         default '0'  null comment '删除标志（0代表存在 2代表删除）',
    login_ip        varchar(128) default ''   null comment '最后登录IP',
    login_date      datetime                  null comment '最后登录时间',
    pwd_update_date datetime                  null comment '密码最后更新时间',
    create_by       varchar(64)  default ''   null comment '创建者',
    create_time     datetime                  null comment '创建时间',
    update_by       varchar(64)  default ''   null comment '更新者',
    update_time     datetime                  null comment '更新时间',
    remark          varchar(500)              null comment '备注'
)
    comment '用户信息表';

create table sys_user_post
(
    user_id bigint not null comment '用户ID',
    post_id bigint not null comment '岗位ID',
    primary key (user_id, post_id)
)
    comment '用户与岗位关联表';

create table sys_user_role
(
    user_id bigint not null comment '用户ID',
    role_id bigint not null comment '角色ID',
    primary key (user_id, role_id)
)
    comment '用户和角色关联表';


-- 合同任务类型
insert into sys_dict_type(dict_name, dict_type, status, create_by, create_time, remark) values('合同任务类型', 'cms_task_type', '0', 'admin', sysdate(), '合同任务类型');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) values(0, '普通', '0', 'cms_task_type', '', 'primary', 'N', '0', 'admin', sysdate(), '');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) values(1, '催收', '1', 'cms_task_type', '', 'warning', 'N', '0', 'admin', sysdate(), '');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) values(2, '续费', '2', 'cms_task_type', '', 'success', 'N', '0', 'admin', sysdate(), '');
insert into sys_dict_data(dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) values(3, '终止', '3', 'cms_task_type', '', 'danger', 'N', '0', 'admin', sysdate(), '');
insert into sys_config(config_name, config_key, config_value, config_type, create_by, create_time, remark) values('合同到期提醒天数', 'cms.reminder.days', '30', 'N', 'admin', sysdate(), '提前N天提醒合同到期');
-- 总账报表菜单
insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values('总账报表', 2010, 4, 'ledger', 'system/ledger/index', 1, 0, 'C', '0', '0', 'cms:ledger:list', 'chart', 'admin', sysdate(), '总账财务报表');
insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values('任务派发', 2016, 5, '#', '#', 1, 0, 'F', '0', '0', 'cms:task:dispatch', '#', 'admin', sysdate(), '');
insert into sys_menu(menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values('合同审批', 2023, 6, '#', '#', 1, 0, 'F', '0', '0', 'cms:contract:audit', '#', 'admin', sysdate(), '');
-- 角色菜单权限分配
-- 会计角色 (role_id=2)
insert into sys_role_menu(role_id, menu_id) values(2, 2010);  -- 合同管理(父菜单)
insert into sys_role_menu(role_id, menu_id) values(2, 2023);  -- 代账合同
insert into sys_role_menu(role_id, menu_id) values(2, 2024);  -- 地址出售
insert into sys_role_menu(role_id, menu_id) values(2, 2016);  -- 任务管理
insert into sys_role_menu(role_id, menu_id) values(2, 2017);  -- 任务管理查询
insert into sys_role_menu(role_id, menu_id) values(2, 2018);  -- 任务管理新增
insert into sys_role_menu(role_id, menu_id) values(2, 2019);  -- 任务管理修改
insert into sys_role_menu(role_id, menu_id) values(2, 2020);  -- 任务管理删除
insert into sys_role_menu(role_id, menu_id) values(2, 2021);  -- 任务管理导出
insert into sys_role_menu(role_id, menu_id) values(2, 2028);  -- 合同管理查询(代账)
insert into sys_role_menu(role_id, menu_id) values(2, 2029);  -- 合同管理新增(代账)
insert into sys_role_menu(role_id, menu_id) values(2, 2030);  -- 合同管理修改(代账)
insert into sys_role_menu(role_id, menu_id) values(2, 2031);  -- 合同管理删除(代账)
insert into sys_role_menu(role_id, menu_id) values(2, 2032);  -- 合同管理导出(代账)
insert into sys_role_menu(role_id, menu_id) values(2, 2033);  -- 合同管理查询(地址)
insert into sys_role_menu(role_id, menu_id) values(2, 2034);  -- 合同管理新增(地址)
insert into sys_role_menu(role_id, menu_id) values(2, 2035);  -- 合同管理修改(地址)
insert into sys_role_menu(role_id, menu_id) values(2, 2036);  -- 合同管理删除(地址)
insert into sys_role_menu(role_id, menu_id) values(2, 2037);  -- 合同管理导出(地址)

-- 销售角色 (role_id=101)
insert into sys_role_menu(role_id, menu_id) values(101, 2010); -- 合同管理(父菜单)
insert into sys_role_menu(role_id, menu_id) values(101, 2023); -- 代账合同
insert into sys_role_menu(role_id, menu_id) values(101, 2024); -- 地址出售
insert into sys_role_menu(role_id, menu_id) values(101, 2028); -- 合同管理查询(代账)
insert into sys_role_menu(role_id, menu_id) values(101, 2029); -- 合同管理新增(代账)
insert into sys_role_menu(role_id, menu_id) values(101, 2030); -- 合同管理修改(代账)
insert into sys_role_menu(role_id, menu_id) values(101, 2032); -- 合同管理导出(代账)
insert into sys_role_menu(role_id, menu_id) values(101, 2033); -- 合同管理查询(地址)
insert into sys_role_menu(role_id, menu_id) values(101, 2034); -- 合同管理新增(地址)
insert into sys_role_menu(role_id, menu_id) values(101, 2035); -- 合同管理修改(地址)
insert into sys_role_menu(role_id, menu_id) values(101, 2037); -- 合同管理导出(地址)