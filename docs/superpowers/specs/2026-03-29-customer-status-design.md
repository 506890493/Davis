# 客户状态字段设计方案

## 概述

为客户表(cms_customer)新增状态字段，用于标识客户是否为"正常"合作状态。

## 现状

当前 `cms_customer` 表结构：

| 字段 | 类型 | 说明 |
|------|------|------|
| customer_id | bigint | 客户ID |
| customer_name | varchar | 客户名称 |
| customer_type | varchar | 客户类型（个人/企业） |
| contact_person | varchar | 联系人 |
| contact_phone | varchar | 联系电话 |
| contact_email | varchar | 邮箱 |
| address | varchar | 地址 |
| remark | varchar | 备注 |
| owner_id | bigint | 归属销售 |
| del_flag | char | 删除标志 |

## 方案设计

### 1. 数据库变更

```sql
ALTER TABLE cms_customer ADD COLUMN `status` char(1) NOT NULL DEFAULT '0' COMMENT '客户状态（0=正常, 1=非正常）';
```

### 2. 字典配置

新增字典 `cms_customer_status`：

| 字典编码 | 字典名称 | 字典值 | 字典文本 |
|----------|----------|--------|----------|
| cms_customer_status | 客户状态 | 0 | 正常 |
| cms_customer_status | 客户状态 | 1 | 非正常 |

> **业务含义**：状态由用户手动管理。"正常"表示客户正在合作中，"非正常"表示客户已停止合作（如流失、终止合作等）。具体判断标准由业务人员根据实际情况决定。

### 3. 后端改动

#### 3.1 字典数据 SQL

> **注意**：dict_code 值需根据现有数据最大值+1确定，以下为示例值。执行前请先查询：`SELECT MAX(dict_code) FROM sys_dict_data`

```sql
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES ((SELECT IFNULL(MAX(dict_code), 0) + 1 FROM sys_dict_data LIMIT 1), 1, '正常', '0', 'cms_customer_status', '', 'primary', 'Y', '0', 'admin', NOW(), '客户状态-正常');

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES ((SELECT IFNULL(MAX(dict_code), 0) + 2 FROM sys_dict_data LIMIT 1), 2, '非正常', '1', 'cms_customer_status', '', 'danger', 'N', '0', 'admin', NOW(), '客户状态-非正常');
```

### 3. 后端改动

#### 3.1 Entity (CmsCustomer.java)
- 新增字段 `status`
- 添加 getter/setter
- 添加 `@Excel` 注解用于导出
- 添加字典注解

#### 3.2 Mapper XML
- 在 `selectCmsCustomerList` 中增加 status 查询条件
- 在 `insertCmsCustomer` 中包含 status 字段
- 在 `updateCmsCustomer` 中包含 status 字段

#### 3.3 Controller
- 无需变更（status 作为查询条件自动透传）

### 4. 前端改动

#### 4.1 客户列表页 (customer/index.vue)
- 在表格中增加"状态"列，使用 `<dict-tag>` 展示
- 在搜索区域增加"状态"下拉筛选器
- 在导入/导出中包含状态字段

#### 4.2 客户表单页 (customer/form.vue)
- 在表单中增加"状态"下拉选择器
- 新增时默认选中"正常"(0)
- 编辑时可切换状态

## 实现步骤

1. 数据库：执行 ALTER TABLE 添加 status 字段
2. 后端：修改 CmsCustomer.java 实体类
3. 后端：修改 CmsCustomerMapper.xml
4. 后端：修改 SysDictData.sql 插入字典数据
5. 前端：修改客户列表页
6. 前端：修改客户表单页
7. 测试验证

## 约束

- 非正常状态的客户仍可正常查看和操作，不影响现有业务流程
- 新增客户默认状态为"正常"
