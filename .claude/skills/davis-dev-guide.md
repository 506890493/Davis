---
name: davis-dev-guide
description: Davis 达维斯管理系统（RuoYi-Vue 定制版）功能开发指南。当需要在 Davis 项目中新增功能、修改页面、添加字段或调用后端接口时使用此 skill。
---

# Davis 项目开发指南

## 项目定位

Davis 是基于 RuoYi-Vue v3.9.0 定制开发的代账与地址租赁合同管理系统。

## 技术栈

| 层 | 技术 |
|---|------|
| 后端框架 | Spring Boot 2.5.15, MyBatis, Spring Security 5.7 |
| 前端框架 | Vue 2.6, Element UI 2.15, Vuex 3.6 |
| 数据库 | MySQL 8.0, Redis 7.0, Druid 连接池 |
| 安全 | JWT 认证, `@PreAuthorize` 权限注解 |

## 项目模块结构

```
ruoyi-common          # 共享工具类、注解、异常
  ↑
ruoyi-system          # 领域实体、Service、Mapper、CMS 业务逻辑
  ↑
ruoyi-framework       # 安全配置、AOP、数据源、拦截器
  ↑
ruoyi-admin           # Spring Boot 入口、Controller、配置文件
ruoyi-quartz          # 定时任务
ruoyi-generator       # 代码生成器
```

Davis CMS 业务代码分布：
- **Controller**: `ruoyi-admin/.../web/controller/davis/Cms*Controller.java`
- **Service + Mapper**: `ruoyi-system/.../service/impl/Cms*ServiceImpl.java` + `ruoyi-system/.../mapper/Cms*Mapper.java`
- **前端页面**: `ruoyi-ui/src/views/system/{contract,customer,ledger,task}/`
- **前端 API**: `ruoyi-ui/src/api/system/{contract,customer,ledger,task}.js`
- **数据库 DDL**: `sql/davis.sql`

## 后端开发模式

### 标准分层（每层必经过）

```
Controller (extends BaseController)
  → Service Interface (I*Service)
    → ServiceImpl
      → Mapper Interface
        → Mapper XML (classpath*:mapper/**/*Mapper.xml)
```

### Controller 规范
- 每个接口必须加 `@PreAuthorize("@ss.hasPermi('module:sub:action')")`
- 分页查询：`startPage()` → 查询 → `getDataTable(list)` 返回 `TableDataInfo`
- CRUD 结果：`toAjax(int)` 或 `AjaxResult.success(data)` / `AjaxResult.error(msg)`
- 新增/修改操作加 `@Transactional`

### Service 规范
- 接口定义在 `ICms*Service.java`
- 实现在 `Cms*ServiceImpl.java`
- 需要事务的方法加 `@Transactional`
- 参数校验抛出 `ServiceException`

### Mapper 规范
- Mapper 接口在 `ruoyi-system/.../mapper/`
- Mapper XML 在 `ruoyi-system/src/main/resources/mapper/system/`
- 如果 Mapper 方法返回关联查询结果，确保 XML 中定义了对应的 resultMap

## 前端开发模式

### 页面组件结构
```
<template>
  <div class="app-container">
    <el-page-header @back="cancel" content="页面标题" />
    <el-card class="mt20">
      <!-- 搜索表单 / 详情 / 编辑表单 -->
    </el-card>
  </div>
</template>
```

### 字典系统
1. 在组件的 `dicts` 数组中注册字典类型名
2. 后端需在 `sys_dict_data` 表中存在对应 `dict_type` 的数据
3. 模板中使用 `<dict-tag :options="dict.type.xxx" :value="value" />` 渲染
4. 表单中使用 `<el-option v-for="dict in dict.type.xxx" .../>` 渲染选项

### API 调用
- API 函数定义在 `ruoyi-ui/src/api/system/` 下对应模块的 `.js` 文件
- 使用 `@/utils/request` 封装的 axios 实例
- 分页参数统一使用 `{ pageNum, pageSize, ...query }`

### 路由
- 路由由后端 `/getRouters` API 动态生成，前端不需要手动配置路由
- 权限指令：`v-hasPermi` 控制按钮显示，`$auth.hasPermi()` 方法级判断

### 组件间跳转
- 关闭当前页并打开新页：`this.$tab.closeOpenPage({ path: '/xxx' })`
- 关闭当前页返回上一页：`this.$tab.closePage()`

## 数据库变更

- 新表或字段变更统一写入 `sql/davis.sql`（已有表结构后追加 ALTER 语句或新 CREATE TABLE）
- 基础表在 `sql/ruoyi.sql`
- 增量更新在 `sql/update_*.sql`

## 开发流程（必须遵守）

1. **先写计划文件** 到 `docs/davis/plan/` 目录，包含：问题根因、修复方案、影响范围、验证方法
2. **用户确认计划** 后才能执行代码修改
3. **修改完成后验证** 功能是否正常

## 本地运行

```bash
# 后端
mvn clean package -Dmaven.test.skip=true -pl ruoyi-admin -am
java -jar ruoyi-admin/target/ruoyi-admin.jar

# 前端
cd ruoyi-ui
npm install
npm run dev        # localhost:80，代理 /dev-api → localhost:8080

# 基础设施
docker compose -f docker-compose.yml up -d   # MySQL + Redis
```
