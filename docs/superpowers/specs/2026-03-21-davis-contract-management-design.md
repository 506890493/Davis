# 达维斯管理系统 - 功能扩展设计文档

**日期：** 2026-03-21  
**状态：** 已确认

---

## 一、背景与目标

### 业务需求
- **合同管理**：管理代账合同和地址租赁合同
- **任务分配**：经理通过首页观察公司数据，及时分配催缴任务给会计
- **角色支持**：经理(admin/manager)、会计(accountant)、销售(sales)都能使用系统

### 核心角色
| 角色 | 主要功能 |
|------|----------|
| Admin/Manager | 观察数据、分配催缴任务 |
| Accountant | 跟进催缴任务、确认收款 |
| Sales | 录入新客户、查看业绩 |

---

## 二、首页重构

### 2.1 Admin/Manager 视图

**统计卡片：**
| 卡片 | 数据 | 说明 |
|------|------|------|
| 本月应完成金额 | sum(amount) | 本月到期合同总金额 |
| 本月实际完成金额 | sum(实际收款) | 已确认收款的金额 |
| 合同总数 | count(*) | 代账 + 地址总数 |
| 即将到期数 | count(*) | 30天内到期合同数 |

**下方列表：**
- 即将到期合同表格
- 每行操作：**"创建催缴任务"按钮**
  - 点击弹出对话框
  - 自动填入合同信息
  - 可选择分配给哪个会计

### 2.2 Accountant 视图

**统计卡片：**
| 卡片 | 数据 | 说明 |
|------|------|------|
| 本月应收金额 | 待收款总金额 |
| 本月已收金额 | 已确认收款总金额 |
| 代账应收家数 | 待处理的代账合同数 |
| 代账已收家数 | 已完成的代账合同数 |

**下方列表：**
- 待处理合同表格
- 每行操作：**"确认收款"按钮**
  - 点击弹出对话框
  - 记录实际收款金额（可修改）
  - 记录收款原因/备注
  - 确认后任务完成

### 2.3 Sales 视图

**统计卡片：**
| 卡片 | 数据 | 说明 |
|------|------|------|
| 我的客户总数 | count(customer) |
| 本月目标金额 | 销售本月目标 |
| 本月完成金额 | 已完成收款金额 |

**新增功能：**
- **"录入新客户"按钮** - 一键跳转到客户+合同录入页面

---

## 三、新增功能模块

### 3.1 客户管理（新增）

**目的：** 统一管理客户信息，避免重复录入

**客户表 (cms_customer)：**
| 字段 | 类型 | 说明 |
|------|------|------|
| customer_id | Long | 主键 |
| customer_name | String | 客户名称 |
| customer_type | String | 客户类型（个人/企业） |
| contact_person | String | 联系人 |
| contact_phone | String | 电话 |
| contact_email | String | 邮箱 |
| address | String | 地址 |
| remark | String | 备注 |
| owner_id | Long | 归属销售 |
| create_by | String | 创建人 |
| create_time | Date | 创建时间 |
| update_by | String | 更新人 |
| update_time | Date | 更新时间 |

**页面功能：**
1. 客户列表 - 分页展示所有客户
2. 客户详情 - 查看该客户的所有合同
3. 新增客户 - 新增客户信息
4. 编辑客户 - 修改客户信息

**关联关系：**
- 客户表与合同表关联：一个客户可有多份合同
- 合同表增加 customer_id 字段

### 3.2 催缴任务（优化）

**目的：** 从首页快速创建催缴任务

**生成方式：**
- 方式A：首页手动点击"创建催缴任务"
- 方式B：定时任务自动生成（到期前30天）

**任务分配：**
- 自动分配给合同归属的会计（owner_id）
- 或经理手动选择分配

**任务状态：**
| 状态 | 说明 |
|------|------|
| 待处理 | 任务刚创建 |
| 进行中 | 会计正在跟进 |
| 已完成 | 客户已续费/已付款 |
| 已超时 | 跟进超时未处理 |

**任务表 (cms_task) 已有字段优化：**
| 字段 | 类型 | 说明 |
|------|------|------|
| task_type | String | 任务类型（催缴/其他） |
| contract_id | Long | 关联合同 |
| original_amount | BigDecimal | 原合同金额 |
| actual_amount | BigDecimal | 实际收款金额 |
| receive_remark | String | 收款备注/原因 |
| status | String | 状态 |

### 3.3 收款确认（新增）

**目的：** 会计完成任务后确认收款

**流程：**
1. 会计在任务列表点击"确认收款"
2. 弹出对话框：
   - 实际收款金额（可修改）
   - 收款备注（记录折扣、减免等原因）
3. 确认后：
   - 任务状态变为"已完成"
   - 更新合同实际收款金额
   - 更新Dashboard统计数据

### 3.4 续费任务（定时生成）

**目的：** 合同到期前自动生成续费提醒

**生成方式：**
- 定时任务每天凌晨执行
- 检查所有有效合同
- 到期前30天的合同自动创建续费任务

**分配方式：**
- 自动分配给合同归属的会计
- 或经理手动分配

### 3.5 业绩报表（新增）

**目的：** 会计和销售查看业绩明细

**页面结构：**
- 顶部汇总卡片
- 下方详细表格

**会计业绩表：**
| 字段 | 说明 |
|------|------|
| 月份 | 统计月份 |
| 应收金额 | 该月应收总额 |
| 实收金额 | 该月实收总额 |
| 完成率 | 实收/应收百分比 |

**销售业绩表：**
| 字段 | 说明 |
|------|------|
| 月份 | 统计月份 |
| 目标金额 | 该月目标 |
| 完成金额 | 该月完成 |
| 完成率 | 完成/目标百分比 |

---

## 四、数据表设计

### 4.1 新增表

**客户表 (cms_customer)**
```sql
CREATE TABLE cms_customer (
    customer_id     BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '客户ID',
    customer_name   VARCHAR(100)    NOT NULL                   COMMENT '客户名称',
    customer_type   VARCHAR(10)     DEFAULT NULL               COMMENT '客户类型（个人/企业）',
    contact_person  VARCHAR(50)     DEFAULT NULL               COMMENT '联系人',
    contact_phone   VARCHAR(20)     DEFAULT NULL               COMMENT '联系电话',
    contact_email   VARCHAR(100)    DEFAULT NULL               COMMENT '邮箱',
    address         VARCHAR(255)    DEFAULT NULL               COMMENT '地址',
    remark          VARCHAR(500)    DEFAULT NULL               COMMENT '备注',
    owner_id        BIGINT(20)     DEFAULT NULL               COMMENT '归属销售',
    del_flag        CHAR(1)        DEFAULT '0'                COMMENT '删除标志',
    create_by       VARCHAR(64)    DEFAULT ''                 COMMENT '创建者',
    create_time     DATETIME       DEFAULT NULL               COMMENT '创建时间',
    update_by       VARCHAR(64)    DEFAULT ''                 COMMENT '更新者',
    update_time     DATETIME       DEFAULT NULL               COMMENT '更新时间',
    PRIMARY KEY (customer_id)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';
```

### 4.2 修改表

**合同表 (cms_contract) - 增加字段**
```sql
ALTER TABLE cms_contract ADD COLUMN customer_id BIGINT(20) COMMENT '关联客户ID';
ALTER TABLE cms_contract ADD COLUMN actual_amount DECIMAL(12,2) COMMENT '实际收款金额';
```

**任务表 (cms_task) - 增加/修改字段**
```sql
ALTER TABLE cms_task ADD COLUMN task_type VARCHAR(20) COMMENT '任务类型（催缴/其他）';
ALTER TABLE cms_task ADD COLUMN original_amount DECIMAL(12,2) COMMENT '原合同金额';
ALTER TABLE cms_task ADD COLUMN actual_amount DECIMAL(12,2) COMMENT '实际收款金额';
ALTER TABLE cms_task ADD COLUMN receive_remark VARCHAR(500) COMMENT '收款备注';
```

---

## 五、页面结构

```
达维斯管理系统
├── 首页（Dashboard）
│   ├── Admin/Manager视图
│   ├── Accountant视图
│   └── Sales视图
│
├── 客户管理（新增）
│   ├── 客户列表
│   ├── 客户详情
│   └── 新增客户
│
├── 合同管理
│   ├── 代账合同
│   ├── 地址出售
│   └── 新增合同
│
├── 任务管理
│   └── 任务列表 + 创建任务
│
├── 业绩报表（新增）
│   ├── 会计业绩
│   └── 销售业绩
│
└── 总账报表
```

---

## 六、实现优先级

### 第一阶段（核心功能）
1. 客户管理 CRUD
2. 合同表增加 customer_id 关联
3. 首页"创建催缴任务"按钮
4. 收款确认功能

### 第二阶段（完善功能）
1. 业绩报表
2. 定时任务生成续费提醒
3. 首页优化

### 第三阶段（优化体验）
1. 销售录入新客户优化
2. 数据统计优化

---

## 七、技术要点

### 7.1 定时任务
- 使用 Spring @Scheduled
- 每天凌晨2点执行
- 检查合同到期情况
- 自动创建续费任务

### 7.2 权限控制
- 客户管理：所有角色可访问
- 业绩报表：根据角色过滤数据
- 催缴任务：经理可创建，会计可处理

### 7.3 数据隔离
- 会计：只能看和处理自己的任务
- 销售：只能看自己的客户和合同
- 经理：admin权限，看所有数据
