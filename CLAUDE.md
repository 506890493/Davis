# CLAUDE.md（精简版）

## 项目概述
**Davis**（拓荒牛管理系统）— 基于 RuoYi-Vue v3.9.0 定制的会计与租赁合同管理系统。

## 技术栈
- 后端：Java 8, Spring Boot 2.5.15, Spring Security, MyBatis, Druid, Redis, JWT
- 前端：Vue 2.6, Element UI, Vuex, Axios

## 构建与运行
```bash
# 后端
mvn clean package -Dmaven.test.skip=true
java -jar ruoyi-admin/target/ruoyi-admin.jar

# 前端
cd ruoyi-ui
npm install
npm run dev      # localhost:80 → /dev-api → 后端8080
```

## 架构
- Maven模块：common → system → framework → admin（主启动类）
- 业务代码分布：
    - Controller → `ruoyi-admin/.../controller/davis/`
    - Service/Mapper → `ruoyi-system/.../service/impl/` 和 `mapper/`
    - 前端视图 → `ruoyi-ui/src/views/system/`，API → `src/api/system/`
- 后端分层：Controller → Service → Mapper → XML
- 分页：`startPage()` → `getDataTable(list)` 返回 `TableDataInfo`
- 权限：每个接口需 `@PreAuthorize("@ss.hasPermi('模块:子模块:操作')")`

## 前端路由与权限
- 路由由后端 `/getRouters` 动态加载，登录后存入 store，`permission.js` 生成菜单
- 权限控制：`v-hasPermi` 指令 + `$auth.hasPermi()` 方法

## 关键配置文件
| 文件 | 作用 |
|------|------|
| `application.yml` | 端口、Redis、token、MyBatis |
| `application-druid.yml` | 数据库连接、Druid |
| `vue.config.js` | 代理、webpack |
| `request.js` | Axios 拦截器、认证头 |
| `permission.js` | 路由守卫 |

## 数据库
- 库名：`davis-backend`
- 初始化：先执行 `sql/ruoyi.sql`，再执行 `sql/davis.sql`
- Druid控制台：`/druid/`（ruoyi/123456）

## 环境变量
| 变量 | 默认值 |
|------|--------|
| `DB_HOST` | `localhost` |
| `DB_USER` | `root` |
| `DB_PASSWORD` | `123456` |
| `REDIS_HOST` | `127.0.0.1` |
| `REDIS_PASSWORD` | `Davis_Redis_Str0ngPass!2026` |
| `VUE_APP_BASE_API` | `/dev-api` |

## CI/CD
GitHub Actions（手动触发）：构建镜像 → 推送到 ghcr.io → SSH 部署 `docker compose up -d --no-deps web`

## 角色与权限
| 角色 | 权限范围 |
|------|----------|
| **admin** | 全部业务 + 系统配置（用户/角色/菜单/字典等） |
| **manager** | 全部业务模块（不含系统配置），所有数据可见，可审批、派发 |
| **account** | 仅自己 `assigned_to` 的任务（退回讲价、确认收款、续签、终止） |
| **sales** | 仅自己创建的合同（`create_by`），可增删改（审批通过前） |

> 测试原则：端到端以 **manager、sales、account** 为主，admin 仅测系统配置。

## 代码修改规则
**必须事先询问是否需要编写计划文件到 `docs/davis/plan/`**，确认后方可执行。计划需包含：根因、方案、影响范围、验证方法。

## 测试要求
- 单元测试覆盖率 ≥90%
- 集成测试覆盖率 ≥90%
- E2E测试覆盖率 ≥90%

## 关键事实（避免反复纠正）
- **本地运行，不是基于 dist 文件**——前端在 webpack-dev-server / `npm run dev` 跑，**不要让用户构建 dist** 来验证修复。HMR 卡死时建议**重启 dev server**。
- **用户的 token 存在 Cookies，不是 localStorage**——`getToken()` 走 `js-cookie` 读 `Admin-Token` cookie。诊断登录态时**先查 `document.cookie`** 而不是 `localStorage`。