# 测试设计文档模板 (Test Design Document Template)

所有生成的测试文件必须在文件开头包含一个多行注释块（`/* ... */`），作为测试设计文档。

## 文档结构

### 1. 概述 (Overview)
简要说明被测试函数/方法的功能、背景和测试的主要目的。

### 2. 核心规则 (Core Rules)
列出被测试逻辑的关键业务规则、优先级、约束条件或数据完整性要求。

### 3. 测试场景详解 (Test Scenarios)
使用 ASCII 边框装饰的块来描述每个场景，清晰展示：
- **Purpose**: 该场景验证什么
- **Initial State**: 测试开始前的数据状态
- **Expected Result**: 测试结束时应达到的最终状态或返回的错误

### 4. 场景映射表 (Scenario Mapping)
列出文档中的场景 ID/名称与代码中实际 `It` 或 `Test` 函数名的对应关系，方便定位。

---

## 完整示例

```go
/*
==============================================================================
TEST DESIGN DOCUMENT: VehicleRepository.AddVehicle
==============================================================================

1. OVERVIEW
-----------
验证 VehicleRepository 的 AddVehicle 方法能够正确地将新车辆记录插入
数据库，包括自动生成 ID、设置时间戳、验证唯一约束等。

测试的 API：
  - AddVehicle: 添加新车辆到数据库

2. CORE RULES
-------------
- License plate number (车牌号) 必须唯一
- Vehicle type 和 make 必须引用现有的 lookup 表数据
- Status 默认为 1 (active) 如果未指定
- Vehicle ID 必须自动生成
- CreatedAt 和 UpdatedAt 自动设置为当前时间

3. TEST SCENARIOS
------------------

┌─────────────────────────────────────────────────────────────────────────────┐
│ Scenario A: Successful vehicle creation                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│ Purpose:        Verify vehicle is created with auto-generated ID            │
│ Initial State:  Empty vehicles table                                        │
│ Expected:       VehicleId > 0, CreatedAt/UpdatedAt set, no error           │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│ Scenario B: Duplicate license plate rejection                               │
├─────────────────────────────────────────────────────────────────────────────┤
│ Purpose:        Verify unique constraint on license_plate_number            │
│ Initial State:  One vehicle with plate "B 1234 TEST"                       │
│ Expected:       Error containing "duplicate" or "unique"                    │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│ Scenario C: Invalid vehicle type                                            │
├─────────────────────────────────────────────────────────────────────────────┤
│ Purpose:        Verify foreign key constraint on vehicle_type_id            │
│ Initial State:  Empty vehicle_types lookup table                            │
│ Expected:       Error or nil result (type not found)                        │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│ Scenario D: Default status value                                            │
├─────────────────────────────────────────────────────────────────────────────┤
│ Purpose:        Verify status defaults to 1 (active) when not specified     │
│ Initial State:  Empty vehicles table                                        │
│ Expected:       Vehicle.Status == 1, no error                               │
└─────────────────────────────────────────────────────────────────────────────┘

4. SCENARIO MAPPING
-------------------
- Scenario A → It("should add a new vehicle successfully")
- Scenario B → It("should fail on duplicate license plate")
- Scenario C → It("should return error for invalid vehicle type")
- Scenario D → It("should set default status to 1")

==============================================================================
*/

package repository

import (
	// ... imports
)

var _ = ginkgo.Describe("VehicleRepository", func() {
	// ... test code
})
```

---

## 场景描述块格式

使用以下 ASCII 边框格式描述每个测试场景：

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│ Scenario X: [简短描述]                                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│ Purpose:        [验证什么功能/行为]                                          │
│ Initial State:  [测试开始前的数据状态]                                       │
│ Expected:       [期望的结果、返回值或错误]                                    │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 字段说明

- **Scenario X**: 场景编号（A, B, C...）+ 简短描述
- **Purpose**: 清晰说明该场景验证什么业务逻辑或边界条件
- **Initial State**: 描述测试开始前的数据库状态（如 "Empty users table"、"One user with status=pending"）
- **Expected**: 描述期望的结果（如 "User created with Id > 0"、"Error containing 'invalid email'"）

### 常见场景类型

| 场景类型 | 示例 Purpose |
|---------|-------------|
| 成功路径 | Verify vehicle is created with valid data |
| 验证失败 | Verify validation error when email is empty |
| 唯一约束 | Verify unique constraint on license plate |
| 外键约束 | Verify error when vehicle_type_id doesn't exist |
| 默认值 | Verify status defaults to 1 when not specified |
| 业务规则 | Verify error when trying to delete active vehicle |
| 错误传播 | Verify repository error propagates to service layer |

---

## 编写建议

### 1. 场景粒度
- ✅ 每个场景验证一个明确的业务规则或边界条件
- ❌ 避免一个场景混合多个不相关的验证

### 2. 描述清晰性
- ✅ Purpose 使用 "Verify that..." 或 "Verify...when..." 格式
- ✅ Initial State 明确数据状态（如 "3 users in DB"）
- ✅ Expected 使用具体断言（如 "Id > 0"、"Error contains 'duplicate'"）

### 3. 场景映射
- ✅ 场景映射表的代码名称要与实际测试函数名一致
- ✅ 使用一致的命名约定（如 "should..." 或 "when...then..."）

### 4. 文档维护
- ✅ 代码变更时同步更新测试文档
- ✅ 删除的测试场景也要从文档中移除
