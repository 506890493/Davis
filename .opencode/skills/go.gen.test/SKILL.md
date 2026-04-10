---
name: go.gen.test
description: 为 Go 代码生成符合项目规范的测试文件。使用 GORM、Ginkgo、Gomega 和 testify，根据代码复杂度自动选择 SQLite 内存库或本地 PostgreSQL。当用户要求生成 Go 测试、编写单元测试、为 repository/service/handler 创建测试、或提到 gen-test 时使用此技能。支持输入：文件路径（repository/vehicle.go）、函数名（CreateVehicle）、或类名（VehicleRepository）。用户可调用：/go.gen.test <路径或函数名>。
user-invocable: true
allowed-tools: "Read, Write, Edit, Glob, Grep, Bash, Agent"
---

<system>
你是一位使用 GORM、Ginkgo 和 Gomega 的 Go 语言专家。
在生成涉及数据库交互的测试时，请严格遵循以下准则，以确保测试的可靠性和隔离性。
</system>

# Go 测试生成准则

## 0. 语言与交互规范
*   **交互语言**: AI 必须使用 **中文 (Simplified)** 与用户进行交流、解释代码逻辑和回答问题。
*   **代码语言**: 生成的代码（包括变量名、函数名、注释、字符串字面量）必须使用 **英文**。
*   **Commit Message**: 如果需要生成 Commit Message，必须使用 **英文** (Imperative mood)。

## 1.5 测试文件命名规范

| 层级 | 命名模式 | 示例 |
|------|---------|------|
| Repository（SQLite） | `{package}_test.go` | `vehicle_repository_test.go` |
| Repository（Ginkgo） | `{package}_ginkgo_test.go` | `vehicle_ginkgo_test.go` |
| Ginkgo Suite | `ginkgo_test.go` | `ginkgo_test.go`（每个包唯一） |
| Service | `{service}_test.go` | `user_service_test.go` |
| Handler | `{handler}_test.go` | `user_handler_test.go` |

---

## 1. 总则：依赖分类与 Mock 策略

不同类型的依赖采用不同的测试策略：

### 1.1 PostgreSQL — 必须使用真实数据库

**不使用** `sqlmock`、`gomock` 等方式模拟数据库连接。原因：
- SQL 语法正确性、类型转换（如 `bigint` ↔ `string`）只有真实 PG 才能验证
- GORM gen 生成的查询在 SQLite 下行为可能不一致
- 事务隔离级别、唯一约束、`ON CONFLICT` 等只有真实 PG 才能覆盖

**场景选择依据**：

| 条件 | 使用场景 |
|------|---------|
| 纯 CRUD，无 PG 特有语法，无外部依赖 | 场景 A（SQLite 内存库） |
| 使用 PG 特有功能（`jsonb`、行锁、`ON CONFLICT`） | 场景 B（本地 PG） |
| 有加密字段（PII）依赖 cipher 服务 | 场景 B（本地 PG）+ 本地 AES cipher（见第 6 节） |
| 复杂联表、分页、多条件过滤 | 场景 B（本地 PG） |

### 1.2 第三方服务 — 推荐使用 Mock

以下依赖**不落库**，可以（且推荐）使用 Mock 替代，以隔离外部系统、加快测试速度：

| 依赖类型 | Mock 方式 |
|---------|----------|
| Cipher 服务（`cipher.CipherService`） | 本地 in-memory AES 实现（见第 6 节） |
| Redis / 分布式锁 | `gomock`（见第 5 节） |
| 外部 HTTP API | `httptest.NewServer` 或 `gomock` |
| 消息队列（Kafka 等） | `gomock` |

**核心原则：数据落库的逻辑必须用真实数据库验证；不落库的 I/O 依赖可以 Mock。**

---

## 2. 场景选择与设置

### 场景 A：简单 CRUD / 低依赖
对于依赖较少的简单 Repository，使用 **内存 SQLite** 数据库。
*   **测试框架**: 标准库 `testing` 包。不要使用 ginkgo 库。
*   **设置**：见 `repository/fee_driver_binding_test.go:27` — SQLite 内存库 CRUD 示例

### 场景 B：复杂逻辑 / 集成测试
对于复杂的业务逻辑、事务或需要 PG 特有功能的场景，使用 **本地 Postgres 数据库**。
*   **测试框架**: `ginkgo` 和 `gomega`。
*   **参考文件**:
  - `repository/vehicle_ginkgo_test.go:1` — Ginkgo 集成测试示例
  - `client/gormclient/ginkgo_test.go:1` — Suite 初始化模式
  - `references/ginkgo_test.go.tmpl` — Suite 初始化模板

> **⚠️ 重要**：每个包只能有一个 `ginkgo_test.go` 文件用于 Suite 初始化。
> 如果目标包已存在该文件，新生成的测试应放在其他文件中（如 `xxx_ginkgo_test.go`），
> 且只包含 `Describe`/`It` 块，不要重复 `BeforeSuite`/`TestXxx` 函数。

*   **Suite 初始化**: `BeforeSuite` 和 `TestXxx` 函数必须放在包内专用的 `ginkgo_test.go` 文件中。
    见 `references/ginkgo_test.go.tmpl` — Suite 初始化模板（使用 `migrations.RepoRoot()`）

#### 测试隔离（场景 B）
每个 `It` 块包裹在事务中，`AfterEach` 回滚，确保测试间数据完全隔离。

**两种隔离模式**：
- 模式一：Repository 直接注入 `*gorm.DB`
- 模式二：Repository 通过 `*query.Query` 参数接收 DB（q 参数传入式）

完整代码示例：`references/test_isolation_patterns.go.tmpl`

**关键注意事项**：
- **`UnderlyingDB()` 事务穿透陷阱**：如果实现内部调用了 `q.SomeModel.WithContext(ctx).UnderlyingDB()`，返回的是全局连接而非当前事务，事务回滚对其写入无效，且查询会看到数据库中所有已有数据（包括其他 cipher key 加密的旧记录）
- **应对策略**：通过唯一前缀 + 查询过滤条件（如 `CompanyName`、`FleetId`）将查询结果限定在本次测试创建的数据范围内，而不依赖事务隔离
- **唯一约束字段**：模型中有唯一约束的字段（如 `fleet_id`）在测试辅助函数中必须生成唯一值，否则多条测试数据插入时会因重复值报错。推荐：`FleetId: fmt.Sprintf("fleet-%d", time.Now().UnixNano())`

#### Delta Testing Pattern（差值测试法）

**核心原则**：测试应该验证"我做的操作产生的效果"，而不是"数据库的绝对状态"。

**问题**：当数据库中已有生产数据（如 migration 插入的初始数据）时，使用绝对数量断言会导致测试失败。

**解决方案**：使用差值验证（`afterCount - beforeCount`）

**代码示例**：`references/common_mistakes_examples.go.tmpl` - DeltaTesting_WrongWay() vs DeltaTesting_CorrectWay()

**适用场景**：

| 场景 | 使用差值法 | 使用绝对值 |
|------|----------|----------|
| 创建操作 | ✅ 创建 3 个 users，验证 count +3 | ❌ |
| 删除操作 | ✅ 删除 2 个 items，验证 count -2 | ❌ |
| 列表查询 | ✅ 添加特定类型 items，验证该类型 count +N | ❌ |
| 业务规则 | ❌ | ✅ "系统必须恰好有 5 个管理员角色" |

**参考文档**：`service/DELTA_TESTING_PATTERN.md`

#### Sequence Isolation（序列隔离）

**问题**：Migration 脚本通常插入生产数据（id=1, 2, 3...），测试创建新数据时会因主键冲突失败。

**解决方案**：在 migration 脚本中设置序列起始值为 1000，将生产数据（1-999）与测试数据（1000+）隔离。

**SQL 示例**：
```sql
-- 在 migration 的最后添加
ALTER SEQUENCE roles_id_seq RESTART WITH 1000;
ALTER SEQUENCE resources_id_seq RESTART WITH 1000;
ALTER SEQUENCE user_role_rels_id_seq RESTART WITH 1000;
```

**原则**：
- 生产数据：id 1-999
- 测试数据：id 1000+（由测试动态生成）
- 避免硬编码 ID（如 `RoleId: []int64{1}`）

**代码示例**：`references/common_mistakes_examples.go.tmpl` - HardcodedID_WrongWay() vs HardcodedID_CorrectWay()

**参考文档**：`migrations/SEQUENCE_ISOLATION_FIX.md`

---

## 3. 测试设计文档规范 (Test Design Requirements)

所有生成的测试文件必须在文件开头包含一个多行注释块（`/* ... */`），作为测试设计文档。

文档应包含以下章节：
- **概述 (Overview)**：简要说明被测试函数/方法的功能、背景和测试的主要目的
- **核心规则 (Core Rules)**：列出被测试逻辑的关键业务规则、优先级、约束条件或数据完整性要求
- **测试场景详解 (Test Scenarios)**：使用 ASCII 边框装饰的块来描述每个场景（Purpose、Initial State、Expected Result）
- **场景映射表 (Scenario Mapping)**：列出文档中的场景 ID/名称与代码中实际 `It` 或 `Test` 函数名的对应关系

**完整模板和示例**：`references/test_design_doc_template.md`

---

## 4. 示例输出结构
当被要求生成测试时，请按以下结构组织：
1.  包含符合上述规范的 **测试设计文档**（`/* ... */` 注释块）。
2.  判断场景 A 还是场景 B（按第 2 节判断表）。
3.  相应地设置 DB 连接（场景 A 用 SQLite，场景 B 用本地 PG）。
4.  如有 cipher/redis 等第三方依赖，按第 1.2 节和第 6 节处理。
5.  执行操作，使用 `gomega`（Ginkgo）或 `testify`（标准库）断言结果。

---

## 5. 分布式锁测试规范

### 5.1 基本原则
当测试涉及分布式锁时，必须注意性能问题。避免在测试中触发长时间的重试等待（1s + 2s + 4s = 7秒）。

### 5.2 Mock 锁的正确方式
**完整代码示例**：`references/distributed_lock_mock_example.go.tmpl`

**关键点**：
- ❌ 错误：使用普通 error → 触发重试延迟 → 测试耗时 7 秒
- ✅ 正确：使用 `lock.ErrLockFailFast` → 立即失败 → 测试耗时 3ms
- 性能提升：**2333x**

**适用场景**：

| 场景 | 使用方法 | 原因 |
|------|---------|------|
| 测试锁获取成功 | `LockContext` 返回 `nil` | 模拟正常流程 |
| 测试锁获取失败 | `LockContext` 返回 `lock.ErrLockFailFast` | 避免等待重试（7秒→3ms） |
| 测试业务逻辑（不涉及锁） | 使用 mock 锁返回 `nil` | 隔离依赖 |

---

## 6. Cipher 服务本地构建模式

当被测代码依赖 `cipher.CipherService` 时，**不连接 Vault / 不需要 `KUBE_TOKEN`**，改用本地 in-memory AES 实现。

### 6.1 原理

```
buildLocalCipher()
  └── gocipher.NewAESKey()          生成随机 AES master key
  └── gocipher.NewAESWrapper()      用 master key 包装子 key
  └── inMemKeyStore                 子 key 存在内存 slice 中（非数据库）
  └── gocipher.NewCipher()          构建完整 *gocipher.Cipher
        └── localCipherSvc          包装为 cipher.CipherService 接口
```

### 6.2 完整模板

完整的 Go 代码模板请参见：[references/cipher_test_helper.go.tmpl](references/cipher_test_helper.go.tmpl)

将该文件内容复制到测试文件中即可使用。模板包含 `inMemKeyStore`、`aesKeyCryptographer`、`buildLocalCipher()` 和 `localCipherSvc` 四个组件。

### 6.3 使用方式
**完整模板**：`references/cipher_test_helper.go.tmpl`

**基本用法**：
```go
// Cipher scopes for users PII
var userCipherScopes = struct {
    crypto []string
    digest []string
}{
    crypto: []string{"users.email", "users.phone_number_enc", "users.address_enc"},
    digest: []string{"users.email", "users.phone_number_hash", "users.address_hash"},
}

// In BeforeEach:
ci, err := buildLocalCipher(userCipherScopes.crypto, userCipherScopes.digest)
Expect(err).NotTo(HaveOccurred())
cipherSvc := &localCipherSvc{ci: ci}
testRepo = NewUserCipherDomainRepository(cipherSvc)
```

### 6.4 关键约束

*   每个 `BeforeEach` 都应新建一个 `buildLocalCipher()` 实例，确保每个 `It` 的加密 key 完全隔离。
*   `localCipherSvc` 加密的数据**只能被同一实例解密**。如果测试中存在使用其他 cipher key 加密的旧数据库记录，`localCipherSvc` 无法解密，会返回 `decrypt: secret key not found`。
*   **应对**：通过唯一过滤条件（如 `CompanyName` 前缀）确保查询只命中本次测试写入的行（参见第 2 节的 `UnderlyingDB()` 陷阱说明）。

---

## 7. Service 层测试规范

Service 层不直接访问数据库，通过 Repository 接口完成数据访问，测试时用 Mock 替换 Repository。

### 7.1 核心原则

- **不使用真实数据库**：Repository 依赖全部通过 `go.uber.org/mock` Mock
- **测试框架**：标准库 `testing` + testify
- **结构**：表驱动测试

### 7.2 典型结构
**完整模板**：`references/service_test.go.tmpl`

**核心要点**：
- 表驱动测试（table-driven tests）
- Mock 设置在 `setupMock` 函数中
- 使用 `errMatch` 验证错误消息内容

### 7.3 Mock 文件位置

Mock 文件由 `//go:generate mockgen` 指令生成，位于：
- `repository/mocks/*.go` — Repository 层 Mock
- `service/*_mock.go` — Service 层 Mock（同包）

若 Mock 文件不存在，提示用户运行 `go generate ./...`。

### 7.4 Mock 生成命令

在包含接口定义的文件顶部添加 go:generate 指令：

```go
//go:generate mockgen -source=user_service.go -destination=../mocks/user_service_mock.go UserService
```

然后运行：

```bash
go generate ./...
```

**参考示例**：`repository/user_repository.go:1` — Mock 生成指令示例

---

## 8. Handler 层测试规范

Handler 层处理 HTTP 请求，通过 Service 接口完成业务逻辑，测试时 Mock Service。

### 8.1 Handler 类型判断

Fleet 后端存在**两种** Handler 返回风格，生成前先读取目标文件确认：

| 类型 | 签名 | 测试方式 |
|------|------|---------|
| 返回值型 | `func (h *Handler) Method(r *http.Request, ...) (Result, error)` | 直接调用，断言返回值 |
| 标准 HTTP 型 | `func (h *Handler) Method(w http.ResponseWriter, r *http.Request)` | `httptest.NewRecorder()` 捕获响应 |

### 8.2-8.4 Handler 测试示例
**完整代码示例**：`references/handler_test_examples.go.tmpl`

**包含内容**：
- 返回值型 Handler 测试
- 标准 HTTP 型 Handler 测试（使用 `httptest.NewRecorder()`）
- JWT Claims 注入
- Gorilla Mux 路由参数设置（`mux.SetURLVars`）

---

## 9. 常见错误与解决方案

**完整代码示例**：`references/common_mistakes_examples.go.tmpl` - 包含所有错误/正确示例对比

### 9.1 UnderlyingDB() 事务穿透
**症状**: 测试数据相互影响，断言失败
**原因**: 调用了 `q.SomeModel.WithContext(ctx).UnderlyingDB()`，返回全局连接而非当前事务
**解决方案**: 使用 GORM gen 查询 + 唯一前缀过滤

### 9.2 Cipher 解密失败
**症状**: `decrypt: secret key not found`
**原因**: 试图用本地 cipher 解密旧数据库记录（不同 key 加密）
**解决方案**: 使用唯一前缀过滤，只命中本次测试创建的数据

### 9.3 Ginkgo Suite 冲突
**症状**: `multiple TestSuite functions` 或 `BeforeSuite already registered`
**原因**: 同一包内有多个 `ginkgo_test.go` 文件
**解决方案**: 只保留一个 `ginkgo_test.go`，新增测试放在 `xxx_ginkgo_test.go`（只包含 `Describe`/`It`）

### 9.4 锁测试超慢
**症状**: 单个测试耗时 7+ 秒
**原因**: 使用普通 error 而非 `lock.ErrLockFailFast`
**解决方案**: 见第 5.2 节和 `references/distributed_lock_mock_example.go.tmpl`

### 9.5 Mock 期望不匹配
**症状**: `not all expectations were met` 或 `unexpected call`
**原因**: Mock 设置与实际调用参数/次数不符
**解决方案**: 使用 `gomock.Any()`、`.Times(n)`、`gomock.Eq()`

### 9.6 SQLite 不支持 PG 特有语法
**症状**: `near "ON": syntax error` 或 `no such function: COALESCE`
**原因**: 使用了 PostgreSQL 特有语法（如 `ON CONFLICT`、`jsonb`）
**解决方案**: 改用场景 B（本地 PostgreSQL）

### 9.7 测试文件命名冲突
**症状**: `import cycle not allowed` 或测试无法找到某个类型
**原因**: 测试文件包名声明错误
**解决方案**: Repository 测试使用 `package repository`，Service/Handler 可使用 `package xxx_test`

### 9.8 不必要的 Cleanup 代码（反模式）
**症状**: BeforeEach 中有大量 `Unscoped().Delete()` 调用
**原因**: 误认为需要手动清理数据
**解决方案**: 只依赖事务 Rollback()，使用 Delta Testing 和 Sequence Isolation 处理特殊情况
**参考文档**: `service/CLEANUP_ANALYSIS.md`

---
