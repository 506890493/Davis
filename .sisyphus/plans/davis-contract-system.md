# 达维斯代账/地址租聘管理系统 - 功能完善工作计划

## TL;DR

> **Quick Summary**: 基于已有70%基础设施的RuoYi项目，完善代账管理、地址租赁管理的业务流程（任务派发、续费、讲价、终止），新增通知铃铛提醒、省市区三级选择器、全面财务报表(总账表)、角色数据权限控制等功能。
> 
> **Deliverables**:
> - 完善的任务派发页面（管理员选择会计、编辑合同、终止选项）
> - 续费/讲价/退回/终止全流程闭环
> - 导航栏通知铃铛（到期提醒）
> - 地址租赁三级省市区级联选择器
> - 角色数据权限（会计/销售隐藏金额）
> - 全面财务报表页面（管理员专用，含趋势图表、导出Excel）
> - 到期提醒天数可配置（sys_config）
> - 合同表单区分代账/地址两种类型
> - SQL修复（cms_communication表）
> 
> **Estimated Effort**: Large
> **Parallel Execution**: YES - 4 waves
> **Critical Path**: Task 1(SQL修复) → Task 4(任务派发后端) → Task 7(讲价退回流程) → Task 10(总账报表) → Final Verification

---

## Context

### Original Request
用户需要将Davis系统打造为完整的合同管理系统，涵盖代账管理、地址出租管理两大模块（用户管理已由RuoYi系统自带功能覆盖，不在本计划范围内）。核心功能包括到期提醒、任务派发、续费/讲价/终止流程，以及按角色区分的Dashboard和总账页面。

### Interview Summary
**Key Discussions**:
- 到期提醒方式: Dashboard展示 + 导航栏通知铃铛图标
- 省市区数据: 使用静态JSON文件，前端离线可用
- 讲价处理: 修改CmsTask的currentAmount字段，不改原合同
- 工商角色: 暂不实现
- 总账表: 全面财务报表（月/年统计、按人汇总、趋势图表、Excel导出）
- 部门经理: 暂不实现

**Research Findings**:
- CmsContract实体已有完整字段，动态计算status(30天硬编码)
- CmsTask已支持催收任务流程(sourceContractId/targetContractId)
- 任务状态流: 待处理(0)→进行中(1)→待审批(2)→已退回(3)→已完成(4)
- CmsApproval存在但与contract.auditStatus关系不清晰，需统一
- Dashboard已有三角色视图，后端统计服务完整
- SQL有copy-paste bug: cms_communication表定义错误复制了cms_task
- CmsTaskServiceImpl已有通知逻辑(SysNotice)，可复用

**数据库实际状态（已通过MySQL验证）**:
- ✅ 已存在10个字典类型: cms_audit_status, cms_contract_status, cms_contract_type, cms_file_category, cms_pay_cycle, cms_pay_method, cms_reminder_status, cms_task_priority, cms_task_status, cms_tax_type
- ✅ 已存在4个角色: admin(role_id=1), accountant(role_id=2), manager(role_id=100), sales(role_id=101)
- ✅ 已存在菜单: 合同管理(menu_id=2010, parent_id=0)→代账合同(2023)+地址出售(2024), 任务管理(2016), 审批管理(2004)
- ✅ 已存在5个表: cms_contract, cms_task, cms_approval, cms_file, cms_communication
- ✅ 菜单component路径模式: `system/contract/index`（使用 `system/` 前缀，非 `cms/`）
- ❌ 不存在: cms_task_type字典（需新增：普通/催收/续费/终止）
- ❌ 不存在: cms.reminder.days系统参数
- ❌ 不存在: 总账报表菜单
- ❌ 不存在: cms:task:dispatch 按钮权限
- ❌ 不存在: cms:contract:audit 按钮权限
- ❌ 不存在: cms_notification 表

### Self-Performed Gap Analysis (替代Metis)
**Identified Gaps** (addressed in plan):
- 合同新增表单未区分代账/地址类型的不同字段展示
- 任务派发缺少管理员专用UI（当前直接在合同列表创建催收任务）
- 讲价退回流程后端逻辑不存在，需新建API
- 终止合作流程缺失
- 金额字段未做角色级隐藏
- 通知铃铛系统不存在（仅用SysNotice，无前端展示入口）
- 总账报表页面不存在
- 到期天数硬编码在CmsContract.getStatus()中
- 会计查询应该只能看到自己的客户（当前Dashboard传null查全部）

---

## Work Objectives

### Core Objective
将Davis系统从70%基础设施补全为可交付的合同管理系统，实现完整的业务流程闭环和角色权限控制。

### Concrete Deliverables
- 后端: 任务派发/退回/终止API、通知提醒API、总账报表API、可配置提醒天数
- 前端: 任务派发页面、通知铃铛组件、省市区级联选择器、总账报表页面(含ECharts图表)、合同表单按类型区分
- 数据库: SQL修复、新增字典数据、新增菜单权限
- 权限: 角色级字段隐藏(金额)、数据权限(仅查看自己客户)

### Definition of Done
- [ ] 管理员可从合同列表发起催收任务，选择指定会计
- [ ] 会计可接受任务、完成续签、发起讲价退回、发起终止
- [ ] 管理员可修改协商金额后重新派发
- [ ] 管理员可确认终止合作
- [ ] 导航栏铃铛显示未读提醒数量，点击展开详情
- [ ] 地址租赁表单有省市区三级选择器
- [ ] 会计/销售看不到金额字段
- [ ] 管理员可查看总账报表（月/年、按人、趋势图、导出Excel）
- [ ] 到期提醒天数通过系统参数配置
- [ ] `mvn clean package -Dmaven.test.skip=true` 编译通过
- [ ] `npm run build:prod` 构建通过

### Must Have
- 任务派发完整流程（派发→接受→完成/退回/终止）
- 通知铃铛到期提醒
- 角色数据权限（金额隐藏、仅看自己客户）
- 总账报表页面
- 省市区三级选择器
- 可配置到期提醒天数

### Must NOT Have (Guardrails)
- 不实现部门经理角色
- 不实现工商角色
- 不实现短信/邮件外部通知
- 不做服务器部署相关工作
- 不引入新的前端框架或升级Vue版本
- 不重构RuoYi核心模块
- 不添加不必要的抽象层
- 不在Controller中写业务逻辑
- 总账报表不做实时刷新（手动刷新即可）

---

## Verification Strategy

> **ZERO HUMAN INTERVENTION** — ALL verification is agent-executed. No exceptions.

### Test Decision
- **Infrastructure exists**: NO (项目无测试框架)
- **Automated tests**: None
- **Framework**: none
- **QA Strategy**: Agent-Executed QA Scenarios only

### QA Policy
Every task MUST include agent-executed QA scenarios.
Evidence saved to `.sisyphus/evidence/task-{N}-{scenario-slug}.{ext}`.

- **Frontend/UI**: Use Playwright (playwright skill) — Navigate, interact, assert DOM, screenshot
- **Backend API**: Use Bash (curl) — Send requests, assert status + response fields
- **Build Verification**: Use Bash — `mvn clean package`, `npm run build:prod`

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Foundation — SQL修复 + 配置 + 数据准备):
├── Task 1: SQL修复 + 新增字典/菜单/配置 [quick]
├── Task 2: 到期提醒天数可配置(后端) [quick]
├── Task 3: 省市区JSON数据 + 级联选择器组件 [quick]
└── Task 4: 角色权限配置(sys_role数据) [quick]

Wave 2 (Core Backend — 核心业务逻辑):
├── Task 5: 任务派发后端完善(选择会计、编辑合同) [unspecified-high]
├── Task 6: 讲价/退回/终止后端流程 [deep]
├── Task 7: 通知提醒后端(到期提醒铃铛API) [unspecified-high]
├── Task 8: 总账报表后端API [unspecified-high]
└── Task 9: 角色数据权限后端(金额隐藏、仅看自己客户) [unspecified-high]

Wave 3 (Frontend — 前端页面实现):
├── Task 10: 任务派发页面(管理员专用) [visual-engineering]
├── Task 11: 通知铃铛前端组件 [visual-engineering]
├── Task 12: 合同表单按类型区分(代账/地址) [visual-engineering]
├── Task 13: 讲价/退回/终止前端交互 [visual-engineering]
├── Task 14: 总账报表前端页面(ECharts+表格+导出) [visual-engineering]
└── Task 15: 角色数据权限前端(金额隐藏) [visual-engineering]

Wave 4 (Integration + Build Verification):
├── Task 16: 后端编译验证 + API集成测试 [unspecified-high]
└── Task 17: 前端构建验证 + UI集成检查 [unspecified-high]

Wave FINAL (Independent Review — 4 parallel):
├── Task F1: Plan compliance audit [oracle]
├── Task F2: Code quality review [unspecified-high]
├── Task F3: Real manual QA [unspecified-high]
└── Task F4: Scope fidelity check [deep]

Critical Path: Task 1 → Task 5/6 → Task 10/13 → Task 16 → F1-F4
Parallel Speedup: ~60% faster than sequential
Max Concurrent: 5 (Wave 2 & 3)
```

### Dependency Matrix

| Task | Depends On | Blocks | Wave |
|------|-----------|--------|------|
| 1 | — | 5,6,7,8,9 | 1 |
| 2 | — | 7,11 | 1 |
| 3 | — | 12 | 1 |
| 4 | 1 | 9,15 | 1 |
| 5 | 1 | 10 | 2 |
| 6 | 1 | 13 | 2 |
| 7 | 1,2 | 11 | 2 |
| 8 | 1 | 14 | 2 |
| 9 | 1,4 | 15 | 2 |
| 10 | 5 | 16 | 3 |
| 11 | 7 | 17 | 3 |
| 12 | 3 | 17 | 3 |
| 13 | 6 | 16 | 3 |
| 14 | 8 | 17 | 3 |
| 15 | 9 | 17 | 3 |
| 16 | 10,13 | F1-F4 | 4 |
| 17 | 11,12,14,15 | F1-F4 | 4 |

### Agent Dispatch Summary

- **Wave 1**: **4** — T1-T4 → `quick`
- **Wave 2**: **5** — T5 → `unspecified-high`, T6 → `deep`, T7-T9 → `unspecified-high`
- **Wave 3**: **6** — T10-T15 → `visual-engineering`
- **Wave 4**: **2** — T16-T17 → `unspecified-high`
- **FINAL**: **4** — F1 → `oracle`, F2 → `unspecified-high`, F3 → `unspecified-high`, F4 → `deep`

---

## TODOs

> Implementation + QA = ONE Task. Never separate.
> EVERY task MUST have: Recommended Agent Profile + Parallelization info + QA Scenarios.

### Wave 1: Foundation

- [ ] 1. SQL修复 + 新增字典/菜单/系统参数配置

  **What to do**:
  - 修复 `sql/davis.sql` 中第4节「沟通记录表」的copy-paste bug：当前错误地创建了 `cms_task` 而非 `cms_communication`。删除重复的cms_task DDL，保留正确的cms_communication表定义
  - 新增字典类型 `cms_task_type`：0=普通, 1=催收, 2=续费, 3=终止（⚠️ 这是唯一缺失的字典类型，其余10个字典已存在于DB中）
  - 新增系统参数 `sys_config`：key=`cms.reminder.days`, value=`30`, remark=`合同到期提醒天数`
  - 新增菜单：「总账报表」(parent_id=**2010**, order_num=4, path=ledger, component=**system/ledger/index**, perms=cms:ledger:list)
  - 新增按钮权限菜单：
    - cms:task:dispatch（派发任务按钮，parent_id=**2016**即任务管理菜单）
    - cms:contract:audit（审批按钮，parent_id=**2023**即代账合同菜单）
  - ⚠️ 以下已存在于DB中，**不要重复创建**：
    - 10个字典类型: cms_audit_status, cms_contract_status, cms_contract_type, cms_file_category, cms_pay_cycle, cms_pay_method, cms_reminder_status, cms_task_priority, cms_task_status, cms_tax_type
    - 4个角色: admin(role_id=1), accountant(role_id=2), manager(role_id=100), sales(role_id=101)
    - 菜单: 合同管理(2010)→代账合同(2023)+地址出售(2024), 任务管理(2016), 审批管理(2004)

  **Must NOT do**:
  - 不修改已有表结构（cms_contract, cms_task, cms_approval, cms_file）
  - 不删除已有字典数据
  - 不重复INSERT已存在的字典/角色/菜单记录

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: SQL脚本修改和INSERT语句，单文件操作
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 2, 3, 4)
  - **Blocks**: Tasks 5, 6, 7, 8, 9
  - **Blocked By**: None

  **References**:

  **Pattern References**:
  - `sql/davis.sql:1-218` - 现有DDL和字典INSERT语句格式，严格遵循已有的INSERT风格
  - `sql/davis.sql:100-127` - 第4节「沟通记录表」bug位置，错误复制了cms_task DDL
  - `sql/davis.sql:179-200` - 菜单INSERT语句格式

  **DB验证数据（已确认）**:
  - 合同管理菜单 menu_id=2010, parent_id=0
  - 代账合同菜单 menu_id=2023, parent_id=2010, component=system/contract/index
  - 地址出售菜单 menu_id=2024, parent_id=2010
  - 任务管理菜单 menu_id=2016, parent_id=0
  - 审批管理菜单 menu_id=2004, parent_id=0
  - 已有菜单component路径模式: `system/` 前缀（非 `cms/`）

  **API/Type References**:
  - `ruoyi-admin/src/main/resources/application.yml` - sys_config参数格式

  **WHY Each Reference Matters**:
  - davis.sql是唯一的数据库脚本文件，所有字典和菜单都在此定义
  - 第4节bug是已知问题，必须修复否则会导致DDL执行失败
  - 菜单INSERT需要遵循parent_id=2010（合同管理顶级菜单）的层级，component使用system/前缀

  **Acceptance Criteria**:

  **QA Scenarios:**

  ```
  Scenario: SQL脚本语法验证
    Tool: Bash
    Preconditions: 无
    Steps:
      1. 读取 sql/davis.sql 文件内容
      2. 检查第4节不再包含重复的 `create table if not exists cms_task` DDL
      3. 检查包含 `cms.reminder.days` 的 INSERT INTO sys_config 语句
      4. 检查包含 `cms:ledger:list` 的菜单INSERT语句，且parent_id=2010
      5. 检查 cms_task_type 字典包含值 0,1,2,3
      6. 检查总账菜单component为 system/ledger/index（非cms/前缀）
      7. 检查cms:task:dispatch按钮权限INSERT存在
      8. 确认不存在重复INSERT已有字典（如cms_reminder_status, cms_audit_status等）
    Expected Result: 所有检查通过，SQL语法无明显错误
    Evidence: .sisyphus/evidence/task-1-sql-validation.txt
  ```

  **Commit**: YES (group with Wave 1)
  - Message: `fix(sql): 修复cms_communication表bug并新增task_type字典和总账菜单`
  - Files: `sql/davis.sql`

---

- [ ] 2. 到期提醒天数可配置（后端）

  **What to do**:
  - 修改 `CmsContract.java` 的 `getStatus()` 方法：将硬编码的 `30` 替换为从Spring容器获取的配置值
  - 由于 `CmsContract` 是POJO不在Spring容器中，采用方案：在 `CmsContractServiceImpl` 中新增方法 `enrichContractStatus()`，查询时从 `sys_config` 读取 `cms.reminder.days` 并设置到每个Contract的计算中
  - 具体方案：在 `CmsContract` 中新增 `private transient Integer reminderDays` 字段（不持久化），`getStatus()` 使用此字段代替硬编码30；Service层查询后遍历设置该值
  - 在 `CmsContractServiceImpl` 中注入 `ISysConfigService`，在 `selectCmsContractList` 和 `selectCmsContractByContractId` 返回前设置 reminderDays
  - 保留30天为默认值（配置不存在时的fallback）

  **Must NOT do**:
  - 不使用静态变量或全局单例
  - 不改变getStatus()的返回值语义（仍为0,1,2,3）

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 修改2个Java文件的小范围逻辑
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 3, 4)
  - **Blocks**: Tasks 7, 11
  - **Blocked By**: None

  **References**:

  **Pattern References**:
  - `ruoyi-system/src/main/java/com/ruoyi/system/domain/CmsContract.java:339-369` - 当前getStatus()方法，硬编码30天
  - `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsContractServiceImpl.java` - Service层，需要在此注入ISysConfigService

  **API/Type References**:
  - `ruoyi-system/src/main/java/com/ruoyi/system/service/ISysConfigService.java` - `selectConfigByKey(String configKey)` 方法
  - `ruoyi-common/src/main/java/com/ruoyi/common/utils/spring/SpringUtils.java` - Spring工具类（备选方案）

  **WHY Each Reference Matters**:
  - getStatus()是核心状态计算逻辑，修改必须保持语义一致
  - ISysConfigService是RuoYi标准的系统参数读取方式

  **Acceptance Criteria**:

  **QA Scenarios:**

  ```
  Scenario: 配置值生效验证
    Tool: Bash (grep)
    Preconditions: Task 1 已添加 cms.reminder.days 到 sys_config
    Steps:
      1. 读取 CmsContract.java 确认 getStatus() 不再包含硬编码 `30`
      2. 确认存在 reminderDays 字段
      3. 读取 CmsContractServiceImpl.java 确认注入了 ISysConfigService
      4. 确认 selectCmsContractList 方法中有设置 reminderDays 的逻辑
    Expected Result: 硬编码30被替换为动态配置值
    Evidence: .sisyphus/evidence/task-2-config-days.txt

  Scenario: 默认值回退验证
    Tool: Bash (grep)
    Preconditions: 无
    Steps:
      1. 在 CmsContract.java 或 CmsContractServiceImpl.java 中搜索默认值30的fallback逻辑
      2. 确认当 sys_config 中无此key时使用默认值30
    Expected Result: 存在 fallback 到 30 的逻辑
    Evidence: .sisyphus/evidence/task-2-default-fallback.txt
  ```

  **Commit**: YES (group with Wave 1)
  - Message: `feat(contract): 到期提醒天数改为sys_config可配置`
  - Files: `CmsContract.java`, `CmsContractServiceImpl.java`

- [ ] 3. 省市区JSON数据 + 级联选择器组件

  **What to do**:
  - 下载或创建中国省市区三级JSON数据文件，放到 `ruoyi-ui/src/assets/json/china-area.json`，格式为 `[{value:'浙江省', label:'浙江省', children:[{value:'杭州市', label:'杭州市', children:[{value:'西湖区', label:'西湖区'}...]}...]}...]`
  - 在 `ruoyi-ui/src/components/` 下新建 `AreaCascader/index.vue` 组件，封装 Element UI 的 `el-cascader`，加载JSON数据，支持 v-model 双向绑定，输出格式为 `省/市/区` 字符串
  - 组件支持 props: `value`(String), `placeholder`(String), `disabled`(Boolean)
  - 在组件底部增加一个 `el-input`，label为"详细地址"，用于手写补充门牌号等信息
  - 全局注册或按需引入

  **Must NOT do**:
  - 不使用外部API获取省市区数据
  - 不引入新的npm依赖
  - JSON文件不超过500KB

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: 创建一个JSON数据文件和一个Vue组件，范围明确
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2, 4)
  - **Blocks**: Task 12
  - **Blocked By**: None

  **References**:

  **Pattern References**:
  - `ruoyi-ui/src/components/` - 组件目录结构，参考已有组件的目录命名惯例
  - `ruoyi-ui/src/views/system/contract/add.vue` - 合同新增表单，了解现有表单元素风格

  **External References**:
  - Element UI Cascader: https://element.eleme.cn/#/zh-CN/component/cascader

  **WHY Each Reference Matters**:
  - 组件风格必须与现有Element UI表单一致
  - 合同新增表单是最终集成位置，了解其结构以确保兼容

  **Acceptance Criteria**:

  **QA Scenarios:**

  ```
  Scenario: JSON数据文件存在且格式正确
    Tool: Bash
    Preconditions: 无
    Steps:
      1. 确认文件存在: ls ruoyi-ui/src/assets/json/china-area.json
      2. 用node验证JSON格式: node -e "const d=require('./ruoyi-ui/src/assets/json/china-area.json'); console.log('provinces:', d.length)"
      3. 验证包含浙江省: node -e "const d=require('./ruoyi-ui/src/assets/json/china-area.json'); console.log(d.find(p=>p.label==='浙江省') ? 'FOUND' : 'MISSING')"
    Expected Result: 文件存在，JSON解析成功，包含浙江省
    Evidence: .sisyphus/evidence/task-3-json-validation.txt

  Scenario: 组件文件结构正确
    Tool: Bash
    Preconditions: 无
    Steps:
      1. 确认组件文件存在: ls ruoyi-ui/src/components/AreaCascader/index.vue
      2. grep 'el-cascader' ruoyi-ui/src/components/AreaCascader/index.vue → 应存在
      3. grep 'v-model\|\$emit' ruoyi-ui/src/components/AreaCascader/index.vue → 应支持双向绑定
    Expected Result: 组件存在，使用el-cascader，支持v-model
    Evidence: .sisyphus/evidence/task-3-component-check.txt
  ```

  **Commit**: YES (group with Wave 1)
  - Message: `feat(ui): 新增省市区三级级联选择器组件`
  - Files: `ruoyi-ui/src/assets/json/china-area.json`, `ruoyi-ui/src/components/AreaCascader/index.vue`

---

- [ ] 4. 角色权限配置（按钮级权限补充 + 角色菜单分配）

  **What to do**:
  - ⚠️ 角色已存在于DB中（admin=1, accountant=2, manager=100, sales=101），**不要重新创建角色**
  - 在 `sql/davis.sql` 中为已有菜单补充**缺失的**按钮级权限（仅新增，不重复已有的）:
    - 合同管理下: cms:contract:audit（审批按钮，parent_id=2023）— 如尚未存在
    - 任务管理下: cms:task:dispatch（派发任务按钮，parent_id=2016）— 如尚未存在
    - 总账报表下: cms:ledger:export（导出按钮，parent_id=总账菜单ID）— Task 1创建总账菜单后补充
  - 在 `sql/davis.sql` 中配置角色与菜单的关联（sys_role_menu表）:
    - accountant角色(role_id=2): 分配合同列表(2023,2024)、任务管理(2016)及其按钮权限（不含审批/总账）
    - sales角色(role_id=101): 分配合同列表(2023,2024)（不含任务管理/审批/总账）
    - admin角色(role_id=1): 已有全部权限，无需修改
  - 确保 `CmsDashboardServiceImpl.determineRoleType()` 中的roleKey判断与DB中的role_key一致（accountant, sales）

  **Must NOT do**:
  - 不创建新角色（角色已存在）
  - 不修改admin角色的权限
  - 不删除已有的菜单或权限记录
  - 不修改RuoYi核心的权限框架代码

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: SQL INSERT语句补充，单文件操作
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2, 3)
  - **Blocks**: Tasks 9, 15
  - **Blocked By**: Task 1（需要Task 1先创建总账菜单）

  **References**:

  **Pattern References**:
  - `sql/davis.sql:180-197` - 已有菜单INSERT格式
  - `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsDashboardServiceImpl.java:157-178` - determineRoleType()方法

  **DB验证数据（已确认）**:
  - 角色: admin(role_id=1, role_key=admin), accountant(role_id=2, role_key=accountant), manager(role_id=100, role_key=manager), sales(role_id=101, role_key=sales)
  - 菜单ID: 合同管理=2010, 代账合同=2023, 地址出售=2024, 任务管理=2016, 审批管理=2004

  **WHY Each Reference Matters**:
  - 角色菜单关联通过sys_role_menu表，需要正确的role_id和menu_id
  - roleKey必须与代码中的判断逻辑一致，否则Dashboard角色识别会失败

  **Acceptance Criteria**:

  **QA Scenarios:**

  ```
  Scenario: 角色菜单权限验证
    Tool: Bash (MySQL query via Docker)
    Preconditions: SQL已执行到DB
    Steps:
      1. 查询 sys_role_menu 中 role_id=2(accountant) 的菜单列表 → 应包含2023,2024,2016
      2. 查询 sys_role_menu 中 role_id=101(sales) 的菜单列表 → 应包含2023,2024
      3. 确认 accountant 不包含总账菜单ID
      4. 确认 sales 不包含任务管理(2016)和总账菜单ID
    Expected Result: 角色菜单分配正确
    Evidence: .sisyphus/evidence/task-4-role-menu.txt

  Scenario: roleKey一致性验证
    Tool: Bash (grep)
    Preconditions: 无
    Steps:
      1. grep 'accountant' CmsDashboardServiceImpl.java → 确认roleKey
      2. grep 'sales' CmsDashboardServiceImpl.java → 确认roleKey
      3. 确认DB中的role_key值与Java代码中一致
    Expected Result: roleKey完全一致
    Evidence: .sisyphus/evidence/task-4-rolekey-consistency.txt
  ```

  **Commit**: YES (group with Wave 1)
  - Message: `feat(auth): 补充按钮权限并配置角色菜单分配`
  - Files: `sql/davis.sql`

---

### Wave 2: Core Backend

- [ ] 5. 任务派发后端完善（选择会计、编辑合同细节）

  **What to do**:
  - 在 `CmsContractController` 的 `createCollectionTask` 方法中完善逻辑：
    - 前端传入 `assignedTo`（会计用户ID）、`originalAmount`（原金额）、`currentAmount`（协商金额，初始等于原金额）、`deadline`（截止时间）
    - 自动设置 `sourceContractId` = 当前合同ID
    - 自动设置 `taskType`：'1'=催收, '2'=续费
    - 生成任务标题：`催收任务: {合同名称}` 或 `续费任务: {合同名称}`
  - 新增API：`GET /system/task/assignableUsers` — 返回可分配的会计列表（roleKey=accountant的用户）
  - 在 `CmsTaskServiceImpl` 中增强幂等性检查，支持多种taskType
  - 在 `ICmsTaskService` 接口中新增 `List<SysUser> getAssignableUsers()` 方法
  - 在 `task.js` 前端API中新增 `getAssignableUsers()` 方法

  **Must NOT do**:
  - 不修改已有的任务状态流转逻辑
  - 不改变CmsTask数据表结构

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 涉及多个Service/Controller/Mapper文件的业务逻辑完善
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 6, 7, 8, 9)
  - **Blocks**: Task 10
  - **Blocked By**: Task 1

  **References**:

  **Pattern References**:
  - `ruoyi-admin/src/main/java/com/ruoyi/web/controller/davis/CmsContractController.java:88-109` - 现有createCollectionTask方法
  - `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsTaskServiceImpl.java:68-104` - insertCmsTask方法，含幂等性检查和通知发送
  - `ruoyi-system/src/main/java/com/ruoyi/system/domain/CmsTask.java:22-67` - Task实体字段（sourceContractId, targetContractId, taskType, assignedTo等）

  **API/Type References**:
  - `ruoyi-system/src/main/java/com/ruoyi/system/service/ISysUserService.java` - 查询用户列表的接口
  - `ruoyi-ui/src/api/system/task.js:46-53` - createCollectionTask前端API

  **WHY Each Reference Matters**:
  - 现有createCollectionTask已有基础逻辑，需在其基础上增强而非重写
  - CmsTask实体已有所需字段，无需修改表结构
  - 需要查询sys_user表获取会计列表，复用ISysUserService

  **Acceptance Criteria**:

  **QA Scenarios:**

  ```
  Scenario: 可分配会计列表API
    Tool: Bash (curl)
    Preconditions: 后端启动，管理员已登录获取token
    Steps:
      1. curl -H 'Authorization: Bearer {token}' http://localhost:8080/system/task/assignableUsers
      2. 验证返回200状态码
      3. 验证返回数据中包含roleKey=accountant的用户
    Expected Result: 返回会计用户列表，每个用户包含userId和userName
    Failure Indicators: 返回空列表或500错误
    Evidence: .sisyphus/evidence/task-5-assignable-users.json

  Scenario: 创建催收任务API增强验证
    Tool: Bash (grep)
    Preconditions: 无
    Steps:
      1. 读取CmsContractController.java确认createCollectionTask方法接受assignedTo参数
      2. 读取CmsTaskServiceImpl.java确认幂等性检查支持多种taskType
      3. 读取task.js确认新增getAssignableUsers方法
    Expected Result: 所有增强逻辑存在
    Evidence: .sisyphus/evidence/task-5-dispatch-api.txt
  ```

  **Commit**: YES (group with Wave 2)
  - Message: `feat(task): 完善任务派发后端逻辑和可分配会计查询`
  - Files: `CmsContractController.java`, `CmsTaskController.java`, `CmsTaskServiceImpl.java`, `ICmsTaskService.java`, `task.js`

---

- [ ] 6. 讲价/退回/终止后端流程

  **What to do**:
  - 新增API `POST /system/task/returnToAdmin`：会计将任务退回管理员
    - 参数：taskId, remark(退回原因), currentAmount(客户期望价格)
    - 逻辑：将task.status设为'3'(已退回)，更新remark和currentAmount
  - 新增API `POST /system/task/redispatch`：管理员修改协商金额后重新派发
    - 参数：taskId, currentAmount(修改后金额), assignedTo(可更换会计)
    - 逻辑：将task.status设为'0'(待处理)，更新currentAmount和assignedTo
  - 新增API `POST /system/task/requestTermination`：会计发起终止合作请求
    - 参数：taskId, remark(终止原因)
    - 逻辑：将task.status设为'2'(待审批)，设置taskType='3'(终止)
  - 新增API `POST /system/task/confirmTermination`：管理员确认终止合作
    - 参数：taskId, approved(boolean)
    - 逻辑：若approved，task.status='4'(已完成)并标记原合同reminderStatus='3'(已完成/终止)；若不approved，task.status='3'(已退回)
  - 新增API `POST /system/task/completeRenewal`：会计完成续签（已有completeCollectionTask基础，需完善）
    - 参数：taskId + 新合同信息(contractName, amount, startDate, endDate, paymentMethod, annex等)
    - 逻辑：创建新合同(parentId=原合同ID, auditStatus='0')，更新task的targetContractId，task.status='4'
  - 所有新API必须有 `@PreAuthorize` 和 `@Log` 注解
  - 在 `CmsTaskServiceImpl` 中实现所有业务逻辑，Controller只做参数校验和调用
  - 在 `task.js` 前端API文件中添加对应的所有方法

  **Must NOT do**:
  - 不修改cms_task表结构（现有字段足够）
  - 不修改已有的任务状态枚举值含义
  - 不在Controller中写业务逻辑

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: 复杂的多步骤业务流程，涉及状态机和多实体联动，需要深度理解
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 5, 7, 8, 9)
  - **Blocks**: Task 13
  - **Blocked By**: Task 1

  **References**:

  **Pattern References**:
  - `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsTaskServiceImpl.java:68-104` - insertCmsTask含幂等性检查+通知逻辑，新方法应遵循相同事务模式
  - `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsTaskServiceImpl.java:112-139` - updateCmsTask含合同状态同步，参考此模式
  - `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsTaskServiceImpl.java:172-195` - completeCollectionTask创建新合同的逻辑，completeRenewal应在此基础上完善
  - `ruoyi-admin/src/main/java/com/ruoyi/web/controller/davis/CmsTaskController.java:66-97` - 现有Controller方法格式（@PreAuthorize + @Log + @PostMapping）

  **API/Type References**:
  - CmsTask.status: '0'=待处理, '1'=进行中, '2'=待审批, '3'=已退回, '4'=已完成
  - CmsTask.taskType: '0'=普通, '1'=催收, '2'=续费, '3'=终止
  - CmsContract.reminderStatus: '0'=无需催收, '1'=待催收/催收中, '2'=催收中, '3'=已完成/终止

  **WHY Each Reference Matters**:
  - 必须遵循已有的事务模式（@Transactional）和通知逻辑
  - 状态值必须与已有字典和前端展示完全一致
  - completeCollectionTask是completeRenewal的基础，需要在其之上增强而非重写

  **Acceptance Criteria**:

  **QA Scenarios:**

  ```
  Scenario: 退回流程API验证
    Tool: Bash (grep)
    Preconditions: 无
    Steps:
      1. 读取CmsTaskController.java确认存在 /returnToAdmin 端点
      2. 读取CmsTaskController.java确认存在 /redispatch 端点
      3. 读取CmsTaskController.java确认存在 /requestTermination 端点
      4. 读取CmsTaskController.java确认存在 /confirmTermination 端点
      5. 读取CmsTaskController.java确认存在 /completeRenewal 端点
      6. 确认所有端点有 @PreAuthorize 和 @Log 注解
    Expected Result: 5个新端点全部存在且有正确注解
    Evidence: .sisyphus/evidence/task-6-api-endpoints.txt

  Scenario: 状态流转逻辑验证
    Tool: Bash (grep)
    Preconditions: 无
    Steps:
      1. 读取CmsTaskServiceImpl.java确认returnToAdmin将status设为'3'
      2. 确认redispatch将status设为'0'
      3. 确认requestTermination将status设为'2'
      4. 确认confirmTermination将status设为'4'
      5. 确认completeRenewal创建新合同并将task.status设为'4'
    Expected Result: 所有状态流转逻辑正确
    Evidence: .sisyphus/evidence/task-6-status-flow.txt

  Scenario: 前端API文件同步
    Tool: Bash (grep)
    Preconditions: 无
    Steps:
      1. grep 'returnToAdmin' ruoyi-ui/src/api/system/task.js → 应存在
      2. grep 'redispatch' ruoyi-ui/src/api/system/task.js → 应存在
      3. grep 'requestTermination' ruoyi-ui/src/api/system/task.js → 应存在
      4. grep 'confirmTermination' ruoyi-ui/src/api/system/task.js → 应存在
      5. grep 'completeRenewal' ruoyi-ui/src/api/system/task.js → 应存在
    Expected Result: 5个前端API方法全部存在
    Evidence: .sisyphus/evidence/task-6-frontend-api.txt
  ```

  **Commit**: YES (group with Wave 2)
  - Message: `feat(task): 讲价退回终止续签全流程后端API`
  - Files: `CmsTaskController.java`, `CmsTaskServiceImpl.java`, `ICmsTaskService.java`, `task.js`

---

- [ ] 7. 通知提醒后端（到期提醒铃铛API）

  **What to do**:
  - 新增 `CmsNotificationController`（或复用 `SysNoticeController`），提供以下API：
    - `GET /system/notification/unreadCount`：返回当前用户未读通知数量
    - `GET /system/notification/list`：返回当前用户通知列表（分页，包含到期提醒和任务通知）
    - `PUT /system/notification/read/{notificationId}`：标记单条通知为已读
    - `PUT /system/notification/readAll`：标记全部已读
  - 新增 `cms_notification` 表（或复用RuoYi的 `sys_notice` 表）：
    - 评估：RuoYi的sys_notice是公告/通知，没有「针对特定用户」和「已读/未读」概念
    - **决策**：新建 `cms_notification` 表，字段：notification_id, user_id(接收人), title, content, notification_type(1=到期提醒, 2=任务通知, 3=审批通知), related_id(关联的合同或任务ID), is_read(0/1), create_time
  - 新增 `CmsNotification` 实体、`ICmsNotificationService`、`CmsNotificationServiceImpl`、`CmsNotificationMapper`、`CmsNotificationMapper.xml`
  - 新增定时任务或在Dashboard查询时自动生成到期提醒通知：
    - 方案：在 `CmsDashboardServiceImpl.getDashboardStats()` 中，查询即将到期合同时，自动为合同归属人和管理员创建通知（防重复：检查related_id+user_id+type是否已存在）
  - 新增前端API `ruoyi-ui/src/api/system/notification.js`
  - 在 `sql/davis.sql` 中添加 `cms_notification` 表DDL

  **Must NOT do**:
  - 不修改RuoYi核心的sys_notice表或相关代码
  - 不引入外部消息队列或定时任务框架（使用已有的逻辑触发）
  - 不发送短信/邮件

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 新建完整模块（实体+Service+Mapper+Controller+XML），但模式与已有CMS模块一致
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 5, 6, 8, 9)
  - **Blocks**: Task 11
  - **Blocked By**: Tasks 1, 2

  **References**:

  **Pattern References**:
  - `ruoyi-system/src/main/java/com/ruoyi/system/domain/CmsTask.java` - CMS实体定义模式（extends BaseEntity, @Excel注解, serialVersionUID）
  - `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsTaskServiceImpl.java:95-103` - 现有通知发送逻辑（使用SysNotice），了解其局限性
  - `ruoyi-system/src/main/resources/mapper/system/CmsTaskMapper.xml` - MyBatis XML映射文件格式
  - `ruoyi-admin/src/main/java/com/ruoyi/web/controller/davis/CmsTaskController.java` - Controller定义模式

  **API/Type References**:
  - `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsDashboardServiceImpl.java:77-94` - buildAdminStats中查询到期合同的逻辑，通知生成应在此时触发
  - `ruoyi-system/src/main/java/com/ruoyi/system/mapper/CmsContractMapper.java` - selectExpiringContracts方法

  **WHY Each Reference Matters**:
  - 新模块必须遵循已有CMS模块的完全相同的代码风格和包结构
  - 通知生成时机利用Dashboard查询来触发，避免引入新的定时任务依赖
  - Mapper XML格式必须与已有的一致

  **Acceptance Criteria**:

  **QA Scenarios:**

  ```
  Scenario: 通知模块文件完整性
    Tool: Bash (ls)
    Preconditions: 无
    Steps:
      1. ls ruoyi-system/src/main/java/com/ruoyi/system/domain/CmsNotification.java → 应存在
      2. ls ruoyi-system/src/main/java/com/ruoyi/system/service/ICmsNotificationService.java → 应存在
      3. ls ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsNotificationServiceImpl.java → 应存在
      4. ls ruoyi-system/src/main/java/com/ruoyi/system/mapper/CmsNotificationMapper.java → 应存在
      5. ls ruoyi-admin/src/main/java/com/ruoyi/web/controller/davis/CmsNotificationController.java → 应存在
      6. ls ruoyi-ui/src/api/system/notification.js → 应存在
    Expected Result: 所有文件存在
    Evidence: .sisyphus/evidence/task-7-file-check.txt

  Scenario: 通知API端点验证
    Tool: Bash (grep)
    Preconditions: 无
    Steps:
      1. grep 'unreadCount' CmsNotificationController.java → 应存在
      2. grep 'readAll' CmsNotificationController.java → 应存在
      3. grep '@PreAuthorize' CmsNotificationController.java → 每个端点应有
    Expected Result: API端点完整且有权限注解
    Evidence: .sisyphus/evidence/task-7-api-check.txt
  ```

  **Commit**: YES (group with Wave 2)
  - Message: `feat(notification): 新增通知提醒模块支持到期提醒铃铛`
  - Files: CmsNotification.java, ICmsNotificationService.java, CmsNotificationServiceImpl.java, CmsNotificationMapper.java, CmsNotificationMapper.xml, CmsNotificationController.java, notification.js, sql/davis.sql

---

- [ ] 8. 总账报表后端API

  **What to do**:
  - 新增 `CmsLedgerController`：
    - `GET /system/ledger/summary`：返回总账汇总数据
      - 参数：year(年份), month(月份，可选，不传则为年度汇总)
      - 返回：代账收入总额、地址租赁收入总额、总收入、总利润、合同总数、代账合同数、地址合同数
    - `GET /system/ledger/byPerson`：按会计/销售分组的业绩统计
      - 参数：year, month(可选)
      - 返回：每人的合同数、收入总额、完成任务数
    - `GET /system/ledger/trend`：趋势数据（月度对比）
      - 参数：year
      - 返回：12个月的收入、合同数趋势数组
    - `POST /system/ledger/export`：导出Excel
      - 参数：year, month(可选)
      - 使用RuoYi的ExcelUtil导出
  - 新增 `ICmsLedgerService` + `CmsLedgerServiceImpl`
  - 新增 `LedgerSummaryVo`、`LedgerByPersonVo`、`LedgerTrendVo` 三个VO类
  - 所有查询直接复用 `CmsContractMapper` 和 `CmsTaskMapper`，新增必要的Mapper方法
  - 所有端点必须有 `@PreAuthorize("@ss.hasPermi('cms:ledger:list')")`（仅管理员）
  - 新增前端API `ruoyi-ui/src/api/system/ledger.js`

  **Must NOT do**:
  - 不创建独立的ledger数据表（基于已有合同和任务表查询即可）
  - 不实现实时刷新（手动刷新即可）
  - 不修改已有的Mapper XML中的查询逻辑

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 新建Controller+Service+VO，涉及多个聚合查询，但模式明确
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 5, 6, 7, 9)
  - **Blocks**: Task 14
  - **Blocked By**: Task 1

  **References**:

  **Pattern References**:
  - `ruoyi-admin/src/main/java/com/ruoyi/web/controller/davis/CmsDashboardController.java` - Controller模式（简单的Service调用+success返回）
  - `ruoyi-system/src/main/java/com/ruoyi/system/domain/vo/DashboardStatsVo.java` - VO定义模式
  - `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsDashboardServiceImpl.java:77-152` - 统计查询模式，Mapper方法调用

  **API/Type References**:
  - `ruoyi-system/src/main/java/com/ruoyi/system/mapper/CmsContractMapper.java` - sumExpiringContractAmount, countExpiringContracts等方法
  - `ruoyi-system/src/main/java/com/ruoyi/system/mapper/CmsTaskMapper.java` - sumCompletedTaskAmount等方法
  - `ruoyi-common/src/main/java/com/ruoyi/common/utils/poi/ExcelUtil.java` - Excel导出工具

  **WHY Each Reference Matters**:
  - Dashboard已有类似的统计查询模式，总账是其更全面的版本
  - 复用已有Mapper方法+新增必要的聚合查询，避免重复造轮子
  - Excel导出复用RuoYi内置工具

  **Acceptance Criteria**:

  **QA Scenarios:**

  ```
  Scenario: 总账API文件完整性
    Tool: Bash (ls + grep)
    Preconditions: 无
    Steps:
      1. ls CmsLedgerController.java → 应存在
      2. ls ICmsLedgerService.java → 应存在
      3. ls CmsLedgerServiceImpl.java → 应存在
      4. ls LedgerSummaryVo.java → 应存在
      5. grep 'cms:ledger:list' CmsLedgerController.java → 应存在
      6. ls ruoyi-ui/src/api/system/ledger.js → 应存在
    Expected Result: 所有文件和权限注解存在
    Evidence: .sisyphus/evidence/task-8-ledger-files.txt

  Scenario: 总账API端点验证
    Tool: Bash (grep)
    Preconditions: 无
    Steps:
      1. grep '/summary' CmsLedgerController.java → 应存在
      2. grep '/byPerson' CmsLedgerController.java → 应存在
      3. grep '/trend' CmsLedgerController.java → 应存在
      4. grep '/export' CmsLedgerController.java → 应存在
    Expected Result: 4个API端点全部存在
    Evidence: .sisyphus/evidence/task-8-ledger-api.txt
  ```

  **Commit**: YES (group with Wave 2)
  - Message: `feat(ledger): 新增总账报表后端API及VO`
  - Files: CmsLedgerController.java, ICmsLedgerService.java, CmsLedgerServiceImpl.java, LedgerSummaryVo.java, LedgerByPersonVo.java, LedgerTrendVo.java, CmsContractMapper.xml(新增查询), ledger.js

---

- [ ] 9. 角色数据权限后端（金额隐藏、仅看自己客户）

  **What to do**:
  - 修改 `CmsContractServiceImpl.selectCmsContractList()`：
    - 获取当前登录用户角色
    - 若角色为accountant：只查询 ownerId = 当前用户ID 的合同（使用DataScope或手动添加WHERE条件）
    - 若角色为sales：只查询 createBy = 当前用户 的合同
    - 若角色为admin：查询全部
  - 修改 `CmsContractServiceImpl.selectCmsContractByContractId()`：
    - 查询后判断权限：非admin且非归属人/创建人则抛出无权限异常
  - 新增 VO `CmsContractListVo`（或使用 `@JsonIgnore` 方案）：
    - **决策**：使用AOP或在Service返回前处理——若当前用户角色为accountant/sales，将amount、profit字段设为null
    - 具体实现：在 `CmsContractServiceImpl` 的列表查询和详情查询返回前，遍历结果，根据角色清空金额字段
  - 修改 `CmsDashboardServiceImpl`：
    - accountant的buildAccountantStats应该只统计该会计负责的合同（当前传null查全部）
    - 获取当前用户ID，传入mapper查询方法
  - 修改 `CmsTaskServiceImpl.selectCmsTaskList()`：
    - 非admin用户只能看到 assignedTo = 当前用户ID 的任务

  **Must NOT do**:
  - 不使用RuoYi的@DataScope注解（因为CMS表没有标准的dept_id关联结构）
  - 不修改Mapper XML的SQL（在Service层做权限过滤）
  - 不创建新的Aspect或Interceptor

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 跨多个Service文件修改，需理解RuoYi的SecurityUtils
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 5, 6, 7, 8)
  - **Blocks**: Task 15
  - **Blocked By**: Tasks 1, 4

  **References**:

  **Pattern References**:
  - `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsDashboardServiceImpl.java:42-69` - 获取当前用户和角色的模式（SecurityUtils.getLoginUser()）
  - `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsDashboardServiceImpl.java:157-178` - determineRoleType()方法，可直接复用
  - `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsContractServiceImpl.java` - 现有的列表查询方法

  **API/Type References**:
  - `ruoyi-common/src/main/java/com/ruoyi/common/utils/SecurityUtils.java` - getLoginUser(), getUserId()
  - CmsContract.ownerId - 合同归属人ID
  - CmsContract.createBy - 合同创建人
  - CmsTask.assignedTo - 任务执行人ID

  **WHY Each Reference Matters**:
  - DashboardServiceImpl已有完整的用户角色判断逻辑，Service层直接复用
  - 权限过滤在Service层而非Mapper层，保持Mapper的通用性

  **Acceptance Criteria**:

  **QA Scenarios:**

  ```
  Scenario: 金额隐藏逻辑验证
    Tool: Bash (grep)
    Preconditions: 无
    Steps:
      1. 读取CmsContractServiceImpl.java
      2. 搜索 setAmount(null) 或 setProfit(null) 的逻辑
      3. 确认仅在非admin角色时执行金额清空
    Expected Result: 存在基于角色的金额隐藏逻辑
    Evidence: .sisyphus/evidence/task-9-amount-hide.txt

  Scenario: 数据隔离逻辑验证
    Tool: Bash (grep)
    Preconditions: 无
    Steps:
      1. 读取CmsContractServiceImpl.java确认selectCmsContractList中有ownerId过滤
      2. 读取CmsTaskServiceImpl.java确认selectCmsTaskList中有assignedTo过滤
      3. 读取CmsDashboardServiceImpl.java确认accountant统计传入userId
    Expected Result: 所有查询有角色级数据隔离
    Evidence: .sisyphus/evidence/task-9-data-isolation.txt
  ```

  **Commit**: YES (group with Wave 2)
  - Message: `feat(auth): 角色数据权限后端金额隐藏和数据隔离`
  - Files: CmsContractServiceImpl.java, CmsTaskServiceImpl.java, CmsDashboardServiceImpl.java

---

### Wave 3: Frontend

- [ ] 10. 任务派发页面（管理员专用）

  **What to do**:
  - 重构 `ruoyi-ui/src/views/system/contract/index.vue` 合同列表中的「创建催收任务」交互：
    - 新增「派发任务」按钮（仅管理员可见，v-hasPermi="['cms:task:dispatch']"）
    - 点击后弹出 `el-dialog`「任务派发」对话框：
      - 显示合同基本信息（公司名称、合同类型、金额、到期日期）—— 只读展示
      - 任务类型选择：催收(1) / 续费(2)
      - 选择会计下拉框（调用 getAssignableUsers API，使用 el-select）
      - 协商金额输入框（默认=原合同金额）
      - 截止日期选择（el-date-picker）
      - 备注输入框
    - 提交后调用 createCollectionTask API
  - 优化任务列表 `ruoyi-ui/src/views/system/task/index.vue`：
    - 管理员视图：显示所有任务，可进行「重新派发」「确认终止」操作
    - 会计视图：显示分配给自己的任务，可进行「开始」「完成续签」「退回(讲价)」「申请终止」操作
    - 根据任务状态(status)动态显示不同的操作按钮
  - 前端文件：修改 contract/index.vue, task/index.vue

  **Must NOT do**:
  - 不创建新的独立页面（在已有页面上增强）
  - 不修改Element UI主题或全局样式
  - 不引入新的npm依赖

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: 前端UI交互设计和实现
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 11, 12, 13, 14, 15)
  - **Blocks**: Task 16
  - **Blocked By**: Task 5

  **References**:

  **Pattern References**:
  - `ruoyi-ui/src/views/system/contract/index.vue` - 现有合同列表，已有「创建催收任务」按钮逻辑
  - `ruoyi-ui/src/views/system/task/index.vue:234-358` - 现有任务编辑对话框和续签对话框
  - `ruoyi-ui/src/views/system/task/index.vue:188-222` - 现有操作按钮模式（开始/修改/完成/删除）

  **Acceptance Criteria**:

  **QA Scenarios:**

  ```
  Scenario: 任务派发对话框存在
    Tool: Bash (grep)
    Preconditions: 无
    Steps:
      1. grep 'cms:task:dispatch' ruoyi-ui/src/views/system/contract/index.vue → 应存在
      2. grep 'getAssignableUsers' ruoyi-ui/src/views/system/contract/index.vue → 应调用此API
      3. grep 'el-select' ruoyi-ui/src/views/system/contract/index.vue → 应有会计选择下拉
    Expected Result: 派发对话框完整
    Evidence: .sisyphus/evidence/task-10-dispatch-dialog.txt

  Scenario: 任务列表角色分化
    Tool: Bash (grep)
    Preconditions: 无
    Steps:
      1. grep 'redispatch\|重新派发' ruoyi-ui/src/views/system/task/index.vue → 管理员操作
      2. grep 'returnToAdmin\|退回' ruoyi-ui/src/views/system/task/index.vue → 会计操作
      3. grep 'requestTermination\|终止' ruoyi-ui/src/views/system/task/index.vue → 会计操作
    Expected Result: 不同角色有不同操作按钮
    Evidence: .sisyphus/evidence/task-10-role-buttons.txt
  ```

  **Commit**: YES (group with Wave 3)
  - Message: `feat(ui): 任务派发对话框和任务列表角色分化`
  - Files: contract/index.vue, task/index.vue

---

- [ ] 11. 通知铃铛前端组件

  **What to do**:
  - 新增组件 `ruoyi-ui/src/components/NotificationBell/index.vue`：
    - 在导航栏右侧显示铃铛图标（使用 el-badge + el-icon-bell）
    - 显示未读通知数量（调用 /system/notification/unreadCount）
    - 点击弹出通知列表下拉面板（el-popover 或 el-drawer）
    - 列表项显示：标题、时间、类型图标（到期/任务/审批）
    - 点击单条可标记已读，底部有「全部标记已读」按钮
    - 每60秒自动轮询刷新未读数量
  - 在 `ruoyi-ui/src/layout/components/Navbar.vue` 中集成 NotificationBell 组件：
    - 放在用户头像/下拉菜单左侧
  - 使用 `ruoyi-ui/src/api/system/notification.js` 调用后端API

  **Must NOT do**:
  - 不使用WebSocket（使用轮询即可）
  - 不修改RuoYi的layout框架结构（仅在Navbar中添加组件）
  - 轮询间隔不低于30秒

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: 前端UI组件开发，需要美观的交互设计
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 10, 12, 13, 14, 15)
  - **Blocks**: Task 17
  - **Blocked By**: Task 7

  **References**:

  **Pattern References**:
  - `ruoyi-ui/src/layout/components/Navbar.vue` - 导航栏组件，找到右侧操作区域的位置
  - `ruoyi-ui/src/components/` - 已有组件目录结构
  - `ruoyi-ui/src/api/system/notification.js` - Task 7创建的前端API

  **Acceptance Criteria**:

  **QA Scenarios:**

  ```
  Scenario: 铃铛组件文件存在
    Tool: Bash (ls + grep)
    Preconditions: 无
    Steps:
      1. ls ruoyi-ui/src/components/NotificationBell/index.vue → 应存在
      2. grep 'NotificationBell' ruoyi-ui/src/layout/components/Navbar.vue → 应被引用
      3. grep 'el-badge' ruoyi-ui/src/components/NotificationBell/index.vue → 应有徽章
      4. grep 'unreadCount' ruoyi-ui/src/components/NotificationBell/index.vue → 应调用未读API
    Expected Result: 组件存在且已集成到导航栏
    Evidence: .sisyphus/evidence/task-11-bell-component.txt
  ```

  **Commit**: YES (group with Wave 3)
  - Message: `feat(ui): 导航栏通知铃铛组件`
  - Files: NotificationBell/index.vue, Navbar.vue

---

- [ ] 12. 合同表单按类型区分（代账/地址）

  **What to do**:
  - 修改 `ruoyi-ui/src/views/system/contract/add.vue`：
    - 在表单顶部增加合同类型选择（contractType: 1=代账, 2=地址），默认不选，选后展示不同字段
    - **代账合同(contractType=1)字段**：
      - 公司名称(contractName) ✓必填
      - 公司类型/税务类型(taxType): 下拉选择（小规模/一般纳税人/个体工商户）✓必填
      - 法人名称(legalPerson)
      - 联系电话(contactPhone) ✓必填
      - 服务期限：开始日期(startDate) + 结束日期(endDate) ✓必填
      - 收费标准(amount) ✓必填
      - 付款周期(paymentCycle)
      - 收款方式(paymentMethod)
      - 代账合同上传（附件：图片/PDF）
      - 付款方式截图上传
      - 备注(remark)
    - **地址租赁(contractType=2)字段**：
      - 公司名称(contractName) ✓必填
      - 联系人(contactPerson) + 联系电话(contactPhone) ✓必填
      - 服务期限：开始日期(startDate) + 结束日期(endDate) ✓必填
      - 注册地址：使用 AreaCascader 组件（Task 3创建）+ 详细地址手写
      - 收费标准(amount) ✓必填
      - 收款方式(paymentMethod)
      - 合同上传（可选：图片/PDF）
      - 付款方式截图上传
      - 备注(remark)
  - 使用 `v-if` / `v-show` 控制字段显隐
  - 同步修改 `edit.vue` 使其也支持按类型区分
  - 文件上传复用现有的 CmsFile 上传逻辑

  **Must NOT do**:
  - 不修改CmsContract后端实体（字段已充足）
  - 不创建新的API端点
  - 不修改合同详情页(detail.vue)的基本结构

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: 表单UI设计和条件渲染
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 10, 11, 13, 14, 15)
  - **Blocks**: Task 17
  - **Blocked By**: Task 3

  **References**:

  **Pattern References**:
  - `ruoyi-ui/src/views/system/contract/add.vue` - 现有新增表单
  - `ruoyi-ui/src/views/system/contract/edit.vue` - 现有编辑表单
  - `ruoyi-ui/src/components/AreaCascader/index.vue` - Task 3创建的组件

  **Acceptance Criteria**:

  **QA Scenarios:**

  ```
  Scenario: 表单类型切换验证
    Tool: Bash (grep)
    Preconditions: 无
    Steps:
      1. grep 'contractType' ruoyi-ui/src/views/system/contract/add.vue → 应有类型选择
      2. grep 'v-if\|v-show' ruoyi-ui/src/views/system/contract/add.vue → 应有条件渲染
      3. grep 'AreaCascader' ruoyi-ui/src/views/system/contract/add.vue → 地址类型应用级联组件
      4. grep 'taxType' ruoyi-ui/src/views/system/contract/add.vue → 代账类型应有税务类型
    Expected Result: 表单按类型区分字段展示
    Evidence: .sisyphus/evidence/task-12-form-type.txt
  ```

  **Commit**: YES (group with Wave 3)
  - Message: `feat(ui): 合同表单按代账/地址类型区分字段展示`
  - Files: add.vue, edit.vue

---

- [ ] 13. 讲价/退回/终止前端交互

  **What to do**:
  - 在 `task/index.vue` 中新增以下交互对话框：
    - **退回(讲价)对话框**：
      - 触发：会计点击「退回」按钮（仅在status='1'进行中时显示）
      - 内容：显示原金额(originalAmount)、客户期望金额输入(currentAmount)、退回原因输入(remark)
      - 提交调用 `returnToAdmin` API
    - **重新派发对话框**：
      - 触发：管理员点击「重新派发」按钮（仅在status='3'已退回时显示）
      - 内容：显示退回原因、原金额、客户期望金额、修改后金额输入(currentAmount)、可更换会计(el-select)、截止日期
      - 提交调用 `redispatch` API
    - **申请终止对话框**：
      - 触发：会计点击「申请终止」按钮
      - 内容：终止原因输入(remark)
      - 提交调用 `requestTermination` API
    - **确认终止对话框**：
      - 触发：管理员点击「审批终止」按钮（仅在status='2'待审批 且 taskType='3'时显示）
      - 内容：显示终止原因、同意/拒绝按钮
      - 提交调用 `confirmTermination` API
  - 操作按钮根据当前用户角色+任务状态动态显示：
    - 获取当前用户角色（从Vuex store的user.roles中读取roleKey）
    - admin看到：重新派发(status=3)、审批终止(status=2&taskType=3)
    - accountant看到：开始(status=0)、完成续签(status=1&taskType∈[1,2])、退回(status=1)、申请终止(status=1)

  **Must NOT do**:
  - 不创建新的路由或页面
  - 不修改已有的「修改」和「删除」操作逻辑

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: 多个对话框交互设计和角色动态按钮
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 10, 11, 12, 14, 15)
  - **Blocks**: Task 16
  - **Blocked By**: Task 6

  **References**:

  **Pattern References**:
  - `ruoyi-ui/src/views/system/task/index.vue:234-358` - 现有对话框模式（el-dialog + el-form + submitForm）
  - `ruoyi-ui/src/views/system/task/index.vue:288-358` - 续签对话框作为参考模板
  - `ruoyi-ui/src/api/system/task.js` - Task 6添加的前端API方法

  **Acceptance Criteria**:

  **QA Scenarios:**

  ```
  Scenario: 所有对话框存在
    Tool: Bash (grep)
    Preconditions: 无
    Steps:
      1. grep '退回' ruoyi-ui/src/views/system/task/index.vue → 应存在退回对话框
      2. grep '重新派发' ruoyi-ui/src/views/system/task/index.vue → 应存在重新派发对话框
      3. grep '申请终止' ruoyi-ui/src/views/system/task/index.vue → 应存在终止申请
      4. grep '确认终止\|审批终止' ruoyi-ui/src/views/system/task/index.vue → 应存在终止确认
    Expected Result: 4种对话框/交互全部存在
    Evidence: .sisyphus/evidence/task-13-dialogs.txt
  ```

  **Commit**: YES (group with Wave 3)
  - Message: `feat(ui): 讲价退回终止前端对话框和角色动态按钮`
  - Files: task/index.vue

---

- [ ] 14. 总账报表前端页面（ECharts+表格+导出）

  **What to do**:
  - 新建 `ruoyi-ui/src/views/system/ledger/index.vue`：
    - 顶部：年份选择器(el-date-picker type=year) + 月份选择器(可选) + 查询按钮 + 导出Excel按钮
    - 区域1：汇总卡片（4个card-panel，样式参考Dashboard）
      - 代账收入总额、地址租赁收入总额、总收入、总利润
    - 区域2：趋势图表（使用ECharts或vue-echarts）
      - 折线图：12个月的收入趋势对比（代账 vs 地址 vs 总计）
      - 需要安装echarts依赖（如项目尚未安装）
    - 区域3：按人员汇总表格（el-table）
      - 列：姓名、角色、合同数、收入总额、完成任务数
    - 调用 Task 8 创建的 ledger.js API
  - 在前端路由中注册（通过菜单系统自动注册）：
    - ⚠️ **已确认**：DB中已有菜单的component路径使用 `system/` 前缀（如 `system/contract/index`），对应实际文件 `views/system/contract/index.vue`
    - Task 1中总账菜单的component已设置为 `system/ledger/index`，与此文件路径 `views/system/ledger/index.vue` 完美匹配
    - **无需额外路由配置，RuoYi菜单系统会自动根据component字段加载对应的Vue组件**

  **Must NOT do**:
  - 不实现自动刷新
  - 仅管理员可见（权限由后端@PreAuthorize控制 + 前端菜单权限控制）
  - 不引入除echarts外的新图表库

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: 图表+表格+卡片的复杂页面布局
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 10, 11, 12, 13, 15)
  - **Blocks**: Task 17
  - **Blocked By**: Task 8

  **References**:

  **Pattern References**:
  - `ruoyi-ui/src/views/index.vue:15-69` - Dashboard管理员视图的card-panel样式，总账复用相同样式
  - `ruoyi-ui/src/views/index.vue:283-381` - Dashboard的SCSS样式定义
  - `sql/davis.sql` - 总账菜单component=`system/ledger/index`，与实际文件路径一致
  - `ruoyi-ui/src/api/system/ledger.js` - Task 8创建的前端API

  **Acceptance Criteria**:

  **QA Scenarios:**

  ```
  Scenario: 总账页面文件存在且结构完整
    Tool: Bash (ls + grep)
    Preconditions: 无
    Steps:
      1. 确认总账页面文件存在（位置匹配菜单component路径）
      2. grep 'echarts' 总账页面文件 → 应引用echarts
      3. grep 'el-table' 总账页面文件 → 应有人员汇总表格
      4. grep 'card-panel' 总账页面文件 → 应有汇总卡片
      5. grep 'export\|导出' 总账页面文件 → 应有导出按钮
    Expected Result: 页面完整包含图表+表格+卡片+导出
    Evidence: .sisyphus/evidence/task-14-ledger-page.txt
  ```

  **Commit**: YES (group with Wave 3)
  - Message: `feat(ui): 总账报表页面含ECharts趋势图和Excel导出`
  - Files: ledger/index.vue, package.json(如需安装echarts)

---

- [ ] 15. 角色数据权限前端（金额隐藏）

  **What to do**:
  - 修改 `ruoyi-ui/src/views/system/contract/index.vue`：
    - 获取当前用户角色（从 this.$store.getters.roles 获取）
    - 「金额」「利润」列使用 v-if="isAdmin" 控制显隐
    - 定义 computed: isAdmin() { return this.$store.getters.roles.includes('admin') }
  - 修改 `ruoyi-ui/src/views/system/contract/detail.vue`：
    - 金额和利润字段使用 v-if="isAdmin" 控制显隐
  - 修改 `ruoyi-ui/src/views/index.vue` Dashboard：
    - 会计视图中如果已有金额展示，确保从后端返回的金额数据正确（后端Task 9已处理）
    - 销售视图中「我的客户」表格不展示金额列（移除或条件隐藏amount列）
  - 修改 `ruoyi-ui/src/views/system/task/index.vue`：
    - 「原金额」「当前协商金额」列使用 v-if="isAdmin" 或 v-if="isAdmin || isAccountant"（会计在任务中需要看到金额来进行催收）
    - **决策**：会计在任务列表中可以看到金额（因为需要催收），但在合同列表中不能看到金额（避免看到其他人的合同金额）
    - 最终方案：合同列表金额列仅admin可见；任务列表金额列admin+accountant可见

  **Must NOT do**:
  - 不修改后端API的返回结构（后端Task 9已做金额null处理）
  - 前端隐藏是「双保险」，不是唯一依赖

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: 多个Vue文件的条件渲染修改
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 10, 11, 12, 13, 14)
  - **Blocks**: Task 17
  - **Blocked By**: Task 9

  **References**:

  **Pattern References**:
  - `ruoyi-ui/src/views/system/contract/index.vue` - 合同列表，含amount和profit列
  - `ruoyi-ui/src/views/system/task/index.vue:155-160` - 任务列表中的金额列
  - `ruoyi-ui/src/views/index.vue:56,139,215` - Dashboard中各视图的金额展示
  - `ruoyi-ui/src/store/getters.js` - Vuex getters定义（roles字段）

  **Acceptance Criteria**:

  **QA Scenarios:**

  ```
  Scenario: 合同列表金额隐藏
    Tool: Bash (grep)
    Preconditions: 无
    Steps:
      1. grep 'isAdmin' ruoyi-ui/src/views/system/contract/index.vue → 应有角色判断
      2. grep 'amount.*v-if\|v-if.*amount' ruoyi-ui/src/views/system/contract/index.vue → 金额列条件渲染
    Expected Result: 金额列有角色条件控制
    Evidence: .sisyphus/evidence/task-15-amount-hide.txt
  ```

  **Commit**: YES (group with Wave 3)
  - Message: `feat(ui): 角色数据权限前端金额隐藏`
  - Files: contract/index.vue, contract/detail.vue, task/index.vue, index.vue

---

### Wave 4: Integration + Build Verification

- [ ] 16. 后端编译验证 + API集成测试

  **What to do**:
  - 运行 `mvn clean package -Dmaven.test.skip=true` 确认编译通过
  - 修复所有编译错误（import缺失、方法签名不匹配、接口未实现等）
  - 检查所有新增Controller的 `@PreAuthorize` 注解完整性
  - 检查所有新增CUD操作的 `@Log` 注解完整性
  - 检查所有新增Mapper XML文件的namespace和SQL语法正确性
  - 如有编译错误，逐个修复直到BUILD SUCCESS

  **Must NOT do**:
  - 不跳过任何编译警告（但不要求零警告）
  - 不添加 @SuppressWarnings
  - 不注释掉代码来解决编译问题

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 需要理解编译错误并正确修复
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Blocks**: F1-F4
  - **Blocked By**: Tasks 10, 13

  **Acceptance Criteria**:

  **QA Scenarios:**

  ```
  Scenario: Maven编译通过
    Tool: Bash
    Preconditions: Wave 1-3全部完成
    Steps:
      1. cd /Users/shipeter/codes/davis/Davis && mvn clean package -Dmaven.test.skip=true
      2. 检查输出包含 BUILD SUCCESS
    Expected Result: BUILD SUCCESS
    Evidence: .sisyphus/evidence/task-16-mvn-build.txt
  ```

  **Commit**: YES (if fixes needed)
  - Message: `fix(build): 修复编译错误确保后端构建通过`

---

- [ ] 17. 前端构建验证 + UI集成检查

  **What to do**:
  - 运行 `cd ruoyi-ui && npm run build:prod` 确认构建通过
  - 修复所有构建错误（import缺失、组件未注册、语法错误等）
  - 检查所有新增Vue组件的props/data/methods定义完整性
  - 检查所有新增API文件的import路径正确性
  - 如需要安装echarts：`npm install echarts --save`
  - 如有构建错误，逐个修复直到Build complete

  **Must NOT do**:
  - 不降级任何已有npm依赖版本
  - 不修改 vue.config.js 的核心配置
  - 不使用 --force 或 --legacy-peer-deps 安装依赖

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: 需要理解前端构建错误并修复
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO (与Task 16串行)
  - **Blocks**: F1-F4
  - **Blocked By**: Tasks 11, 12, 14, 15

  **Acceptance Criteria**:

  **QA Scenarios:**

  ```
  Scenario: npm构建通过
    Tool: Bash
    Preconditions: Wave 1-3全部完成
    Steps:
      1. cd /Users/shipeter/codes/davis/Davis/ruoyi-ui && npm run build:prod
      2. 检查输出包含 Build complete
    Expected Result: Build complete, 无ERROR
    Evidence: .sisyphus/evidence/task-17-npm-build.txt
  ```

  **Commit**: YES (if fixes needed)
  - Message: `fix(build): 修复前端构建错误确保生产构建通过`

---

## Final Verification Wave

> 4 review agents run in PARALLEL. ALL must APPROVE. Rejection → fix → re-run.

- [ ] F1. **Plan Compliance Audit** — `oracle`
  Read the plan end-to-end. For each "Must Have": verify implementation exists (read file, curl endpoint, run command). For each "Must NOT Have": search codebase for forbidden patterns — reject with file:line if found. Check evidence files exist in .sisyphus/evidence/. Compare deliverables against plan.
  Output: `Must Have [N/N] | Must NOT Have [N/N] | Tasks [N/N] | VERDICT: APPROVE/REJECT`

- [ ] F2. **Code Quality Review** — `unspecified-high`
  Run `mvn clean package -Dmaven.test.skip=true` + `npm run build:prod`. Review all changed files for: `@SuppressWarnings`, empty catches, System.out.println in prod, commented-out code, unused imports. Check AI slop: excessive comments, over-abstraction, generic names. Verify all Controller methods have `@PreAuthorize` and CUD operations have `@Log`.
  Output: `Build [PASS/FAIL] | Lint [PASS/FAIL] | Files [N clean/N issues] | VERDICT`

- [ ] F3. **Real Manual QA** — `unspecified-high` (+ `playwright` skill for UI)
  Start from clean state. Execute EVERY QA scenario from EVERY task — follow exact steps, capture evidence. Test cross-task integration (dispatch → accept → negotiate → return → re-dispatch → complete). Test edge cases: empty state, invalid input, rapid actions. Save to `.sisyphus/evidence/final-qa/`.
  Output: `Scenarios [N/N pass] | Integration [N/N] | Edge Cases [N tested] | VERDICT`

- [ ] F4. **Scope Fidelity Check** — `deep`
  For each task: read "What to do", read actual diff (git log/diff). Verify 1:1 — everything in spec was built (no missing), nothing beyond spec was built (no creep). Check "Must NOT do" compliance. Detect cross-task contamination: Task N touching Task M's files. Flag unaccounted changes.
  Output: `Tasks [N/N compliant] | Contamination [CLEAN/N issues] | Unaccounted [CLEAN/N files] | VERDICT`

---

## Commit Strategy

- **Wave 1**: `fix(sql): 修复cms_communication表定义并新增字典菜单配置` — sql/davis.sql, 相关配置文件
- **Wave 2**: `feat(backend): 任务派发讲价退回终止流程与通知提醒API` — Controller/Service/Mapper files
- **Wave 3**: `feat(frontend): 任务派发页面通知铃铛总账报表与权限控制` — Vue views/components
- **Wave 4**: `chore: 编译验证与集成检查` — No new files

---

## Success Criteria

### Verification Commands
```bash
cd /Users/shipeter/codes/davis/Davis && mvn clean package -Dmaven.test.skip=true  # Expected: BUILD SUCCESS
cd /Users/shipeter/codes/davis/Davis/ruoyi-ui && npm run build:prod  # Expected: Build complete
```

### Final Checklist
- [ ] 管理员可派发催收任务给指定会计
- [ ] 会计可接受、完成、退回(讲价)、发起终止
- [ ] 管理员可修改协商金额、重新派发、确认终止
- [ ] 导航栏铃铛显示未读到期提醒
- [ ] 地址租赁有省市区三级选择
- [ ] 会计/销售看不到金额字段
- [ ] 管理员可查看总账报表(月/年/按人/趋势图/导出)
- [ ] 到期提醒天数可在系统参数中配置
- [ ] 后端编译通过，前端构建通过
- [ ] 所有"Must NOT Have"项均未出现
