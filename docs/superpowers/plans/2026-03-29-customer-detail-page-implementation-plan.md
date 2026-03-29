# 客户详情页实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新增客户详情页，展示客户详细信息和关联合同列表

**Architecture:** 
- 客户详情页通过独立路由访问
- 后端提供详情接口返回客户信息+合同列表
- 前端详情页使用 tabs 展示代账合同和地址合同

**Tech Stack:** Java (Spring Boot), MyBatis, Vue.js 2, Element UI

---

## Task 1: 后端 - 新增客户详情接口

**Files:**
- Modify: `ruoyi-admin/src/main/java/com/ruoyi/web/controller/davis/CmsCustomerController.java`
- Modify: `ruoyi-system/src/main/java/com/ruoyi/system/service/ICmsCustomerService.java`
- Modify: `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsCustomerServiceImpl.java`
- Modify: `ruoyi-system/src/main/java/com/ruoyi/system/mapper/CmsCustomerMapper.java`
- Modify: `ruoyi-system/src/main/resources/mapper/system/CmsCustomerMapper.xml`

- [ ] **Step 1: Controller 新增接口**

```java
@GetMapping("/detail/{customerId}")
public AjaxResult getDetail(@PathVariable Long customerId)
{
    // 返回客户信息 + 代账合同列表 + 地址合同列表
}
```

- [ ] **Step 2: Service 接口新增方法**

```java
public Map<String, Object> getCustomerDetail(Long customerId);
```

- [ ] **Step 3: Mapper 新增查询**

根据 customerId 查询客户的代账合同和地址合同

---

## Task 2: 前端 - 新增客户详情页面

**Files:**
- Create: `ruoyi-ui/src/views/system/customer/detail.vue`

- [ ] **Step 1: 创建详情页面结构**

```vue
<template>
  <div class="app-container">
    <!-- 客户基本信息 -->
    <el-card>
      <div slot="header">客户信息</div>
      <!-- 基本信息展示 -->
    </el-card>
    
    <!-- 合同列表 -->
    <el-card>
      <el-tabs>
        <el-tab-pane label="代账合同">...</el-tab-pane>
        <el-tab-pane label="地址合同">...</el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>
```

- [ ] **Step 2: 实现数据获取和展示**

---

## Task 3: 前端 - 列表页客户名称可点击

**Files:**
- Modify: `ruoyi-ui/src/views/system/customer/index.vue`

- [ ] **Step 1: 修改客户名称列**

```vue
<el-table-column label="客户名称" align="center" key="customerName" prop="customerName" :show-overflow-tooltip="true">
  <template slot-scope="scope">
    <el-link type="primary" @click="handleDetail(scope.row)">{{ scope.row.customerName }}</el-link>
  </template>
</el-table-column>
```

- [ ] **Step 2: 添加跳转方法**

```javascript
handleDetail(row) {
  this.$router.push({ path: `/system/customer/detail/${row.customerId}` })
}
```

---

## Task 4: 构建验证

- [ ] **Step 1: 编译后端**

- [ ] **Step 2: 测试验证**
  - 访问客户列表，点击客户名称
  - 确认详情页显示客户信息
  - 确认合同列表正确显示
