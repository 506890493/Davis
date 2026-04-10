# 客户状态字段实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为客户表新增状态字段(正常/非正常)，用于标识客户是否为正常合作状态

**Architecture:** 
- 数据库增加 `status` 字段 (char(1), 默认'0')
- 后端 Entity + Mapper XML 变更
- 前端列表页增加状态列和筛选器，表单页增加状态选择器

**Tech Stack:** Java (Spring Boot), MyBatis, Vue.js 2, Element UI

---

## Task 1: 数据库添加 status 字段

**Files:**
- Modify: `MySQL 数据库 davis-backend.cms_customer`

- [ ] **Step 1: 执行 ALTER TABLE 添加字段**

```sql
ALTER TABLE cms_customer ADD COLUMN `status` char(1) NOT NULL DEFAULT '0' COMMENT '客户状态（0=正常, 1=非正常）';
```

执行命令: 使用 davis-docker_execute_sql 工具

---

## Task 2: 后端 - Entity 变更

**Files:**
- Modify: `ruoyi-system/src/main/java/com/ruoyi/system/domain/CmsCustomer.java`

- [ ] **Step 1: 添加 status 字段和相关注解**

在 `ownerName` 字段后添加:

```java
/** 客户状态（0=正常, 1=非正常） */
@Excel(name = "客户状态", dictType = "cms_customer_status")
private String status;
```

- [ ] **Step 2: 添加 getter/setter**

```java
public String getStatus()
{
    return status;
}

public void setStatus(String status)
{
    this.status = status;
}
```

- [ ] **Step 3: 更新 toString 方法**

在 toString 中添加: `", status='" + status + "'`

---

## Task 3: 后端 - Mapper XML 变更

**Files:**
- Modify: `ruoyi-system/src/main/resources/mapper/system/CmsCustomerMapper.xml`

- [ ] **Step 1: 在 resultMap 中添加 status 映射**

```xml
<result property="status"    column="status"    />
```

- [ ] **Step 2: 在 selectCmsCustomerVo 中添加 status 字段**

```xml
select c.customer_id, c.customer_name, c.customer_type, c.contact_person, 
       c.contact_phone, c.contact_email, c.address, c.remark, c.owner_id, 
       u.nick_name as owner_name, c.del_flag, c.create_by, c.create_time, 
       c.update_by, c.update_time, c.status
```

- [ ] **Step 3: 在 selectCmsCustomerList 查询条件中添加 status 筛选**

```xml
<if test="status != null and status != ''"> and c.status = #{status}</if>
```

- [ ] **Step 4: 在 insertCmsCustomer 中添加 status 字段**

```xml
<if test="status != null">status,</if>
```

values 部分:

```xml
<if test="status != null">#{status},</if>
```

- [ ] **Step 5: 在 updateCmsCustomer 中添加 status 字段**

```xml
<if test="status != null">status = #{status},</if>
```

---

## Task 4: 字典数据插入

**Files:**
- Modify: `MySQL 数据库 sys_dict_data`

- [ ] **Step 1: 查询当前 dict_code 最大值**

```sql
SELECT MAX(dict_code) FROM sys_dict_data;
```

- [ ] **Step 2: 插入字典数据**

根据查询结果确定 dict_code 值，假设当前最大值为 100：

```sql
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (101, 1, '正常', '0', 'cms_customer_status', '', 'primary', 'Y', '0', 'admin', NOW(), '客户状态-正常');

INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (102, 2, '非正常', '1', 'cms_customer_status', '', 'danger', 'N', '0', 'admin', NOW(), '客户状态-非正常');
```

---

## Task 5: 前端 - 客户列表页变更

**Files:**
- Modify: `ruoyi-ui/src/views/system/customer/index.vue`

- [ ] **Step 1: 在查询表单中添加状态下拉筛选器**

在 `customerType` 表单项后添加:

```vue
<el-form-item label="客户状态" prop="status">
  <el-select v-model="queryParams.status" placeholder="请选择" clearable size="small">
    <el-option label="正常" value="0" />
    <el-option label="非正常" value="1" />
  </el-select>
</el-form-item>
```

- [ ] **Step 2: 在 queryParams 中添加 status**

```javascript
queryParams: {
  pageNum: 1,
  pageSize: 10,
  customerName: null,
  contactPhone: null,
  customerType: null,
  status: null  // 新增
},
```

- [ ] **Step 3: 在 resetQuery 中重置 status**

```javascript
queryParams = {
  pageNum: 1,
  pageSize: 10,
  customerName: null,
  contactPhone: null,
  customerType: null,
  status: null  // 新增
};
```

- [ ] **Step 4: 在 data 中添加 dicts 声明**

在 `data()` 的 return 中，添加:

```javascript
dicts: ['cms_customer_status'],
```

- [ ] **Step 5: 在表格中添加状态列**

在 `ownerName` 列后添加:

```vue
<el-table-column label="状态" align="center" key="status" prop="status" width="80">
  <template slot-scope="scope">
    <dict-tag :options="dict.type.cms_customer_status" :value="scope.row.status"/>
  </template>
</el-table-column>
```

---

## Task 6: 前端 - 客户表单页变更

**Files:**
- Modify: `ruoyi-ui/src/views/system/customer/index.vue`

- [ ] **Step 1: 在表单中添加状态下拉选择器**

在 `备注` 表单项后添加:

```vue
<el-form-item label="客户状态" prop="status">
  <el-radio-group v-model="form.status">
    <el-radio label="0">正常</el-radio>
    <el-radio label="1">非正常</el-radio>
  </el-radio-group>
</el-form-item>
```

- [ ] **Step 2: 在 reset 方法中初始化 status 默认值**

```javascript
reset() {
  this.form = {
    customerId: null,
    customerName: null,
    customerType: null,
    contactPerson: null,
    contactPhone: null,
    contactEmail: null,
    address: null,
    remark: null,
    status: '0'  // 新增，默认正常
  };
  this.resetForm("form");
}
```

---

## Task 7: 构建验证

**Files:**
- Backend: `ruoyi-admin/target/ruoyi-admin.jar`

- [ ] **Step 1: 编译后端**

```bash
mvn clean package -Dmaven.test.skip=true -pl ruoyi-admin -am
```

- [ ] **Step 2: 启动应用验证**

验证方式: 访问客户管理页面，确认：
1. 列表页显示状态列
2. 可按状态筛选
3. 新增客户默认状态为"正常"
4. 修改客户可切换状态
