# CLAUDE.md

拓荒牛(Davis)企业服务合同管理系统

## Communication Language

**所有交互必须使用中文回答。** 这是根本要求，包括代码注释、commit 信息、PR 描述等均应使用中文。

## Project Overview

**Davis** (拓荒牛管理系统) — a customized fork of RuoYi-Vue v3.9.0 for accounting and rental contract management.
- Java 8, Spring Boot 2.5.15, Spring Security 5.7, MyBatis, Druid, Redis, JWT
- Vue 2.6, Element UI 2.15, Vuex 3.6, Axios 0.28

Detailed code conventions and patterns are in `AGENTS.md` — read it before writing code.

## Build & Run

```bash
# Backend (Maven — skip tests, none configured)
mvn clean package -Dmaven.test.skip=true
mvn clean package -Dmaven.test.skip=true -pl ruoyi-admin -am  # admin module only
java -jar ruoyi-admin/target/ruoyi-admin.jar

# Frontend
cd ruoyi-ui
npm install
npm run dev           # localhost:80, proxies /dev-api → localhost:8080
npm run build:prod    # production build → dist/
npm run build:stage   # staging build
```

## Local Infrastructure (Docker)

```bash
docker compose -f docker-compose.yml up -d    # MySQL 8.0 + Redis 7.0
docker compose -f docker-compose-local.yml up -d  # adds Redis Cluster (6 nodes)
```

## Architecture

### Maven Modules (dependency order)
```
ruoyi-common          # shared utils, annotations, exceptions
  ↑
ruoyi-system          # domain entities, services, mappers, CMS business logic
  ↑
ruoyi-framework       # security config, AOP, datasource, interceptors
  ↑
ruoyi-admin           # Spring Boot entry point, controllers, application.yml
ruoyi-quartz          # scheduled tasks (depends on common)
ruoyi-generator       # code generator (depends on common)
```

The custom Davis CMS (contracts, customers, ledger, tasks, approvals, notifications) is spread across:
- **Controllers**: `ruoyi-admin/.../web/controller/davis/Cms*Controller.java`
- **Services + Mappers**: `ruoyi-system/.../service/impl/Cms*ServiceImpl.java` + `ruoyi-system/.../mapper/Cms*Mapper.java`
- **Views + API**: `ruoyi-ui/src/views/system/{contract,customer,ledger,task}/` + `ruoyi-ui/src/api/system/{contract,customer,ledger,task}.js`
- **Schema**: `sql/davis.sql`

### Backend Layering (every feature follows this)
```
Controller (extends BaseController)
  → Service Interface (I*Service)
    → ServiceImpl
      → Mapper Interface
        → Mapper XML (classpath*:mapper/**/*Mapper.xml)
```

### Request/Response Conventions
- **Paginated list**: `startPage()` (PageHelper) → query → `getDataTable(list)` returns `TableDataInfo`
- **CRUD result**: `toAjax(int)` or `AjaxResult.success(data)` / `AjaxResult.error(msg)`
- **Every endpoint** requires `@PreAuthorize("@ss.hasPermi('module:sub:action')")`

### Frontend Route Loading
Routes are dynamically loaded from backend `/getRouters` API at login. The backend returns a JSON route tree; `permission.js` store module converts it to Vue components. Never hardcode routes that should be permission-gated.

### Frontend Auth Flow
1. Login → get token, store in cookie as `Admin-Token`
2. `GetInfo` → user profile, roles, permissions array
3. `GenerateRoutes` → `/getRouters` → `router.addRoutes()`
4. Permission checking via `v-hasPermi` directive and `$auth.hasPermi()` method

## Key Config Files

| File | Purpose |
|------|---------|
| `ruoyi-admin/src/main/resources/application.yml` | Server port, Redis, token, MyBatis, thread pool |
| `ruoyi-admin/src/main/resources/application-druid.yml` | DB connection, Druid pool, slow SQL logging |
| `ruoyi-ui/vue.config.js` | Dev server proxy, webpack chunks, Gzip |
| `ruoyi-ui/src/utils/request.js` | Axios instance, interceptors, auth header |
| `ruoyi-ui/src/permission.js` | Navigation guard, route generation trigger |
| `ruoyi-ui/src/settings.js` | Layout defaults (sidebar, tagsView, theme) |

## Database

- **Database name**: `davis-backend`
- **Migrations**: apply `sql/ruoyi.sql` first (base schema), then `sql/davis.sql` (CMS tables), then incremental `sql/ry_*.sql` and `sql/update_*.sql` files
- **Druid console**: `http://localhost:8080/druid/` (login: `ruoyi` / `123456`)

## Environment Variables

| Variable | Default | Used by |
|----------|---------|---------|
| `DB_HOST` | `localhost` | Backend |
| `DB_USER` | `root` | Backend |
| `DB_PASSWORD` | `123456` | Backend |
| `REDIS_HOST` | `127.0.0.1` | Backend |
| `REDIS_PASSWORD` | `Davis_Redis_Str0ngPass!2026` | Backend |
| `VUE_APP_BASE_API` | `/dev-api` | Frontend |

## CI/CD

GitHub Actions (`.github/workflows/deploy.yml`): manual trigger only (`workflow_dispatch`). Builds Docker image → pushes to `ghcr.io` → SSHs to server → `docker compose up -d --no-deps web`.

## Role & Permission System

系统涉及四种角色，权限逐级包含：

| 角色 | 权限范围 |
|------|---------|
| **admin**（超级管理员） | 拥有 manager 全部权限 + 系统配置管理（用户管理、角色管理、菜单管理、字典管理、系统参数等）。可以看到和操作所有数据。 |
| **manager**（业务管理员） | 拥有所有业务模块权限（合同、客户、任务、审批、催收派发等），但不包含系统配置管理。可以看到和操作所有业务数据。**业务流程测试应以 manager 为主。** |
| **account**（会计） | 只能看到分配给自己的任务（`assigned_to` = 自己）。可执行：退回讲价、确认收款、完成续签、发起终止请求。**不能**看到别人的任务。 |
| **sales**（销售） | 只能看到自己创建的合同（`create_by` = 自己）。可新增客户和合同。审批通过前可编辑/删除自己的合同。 |

**测试角色使用原则**：
- 业务端到端测试以 **manager、sales、account** 三种角色为主
- manager 代表管理视角（全部数据可见、可审批、可派发）
- sales 代表销售视角（数据归属隔离）
- account 代表会计视角（任务执行者，数据隔离）
- admin 仅用于测试系统配置相关功能

## Code Change Rules

**所有代码修改必须先询问是否需要写计划文件到 `docs/davis/plan/` 目录，用户确认后方可执行。** 计划文件应包含：问题根因、修复方案、影响范围、验证方法。

## Tests
后端:
** 单元测试覆盖率达到90%
系统：
** 集成测试覆盖率90%
** E2E测试覆盖率90%
