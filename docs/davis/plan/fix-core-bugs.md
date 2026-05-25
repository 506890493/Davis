# 修复 Davis 合同管理系统核心 Bug

## Context

基于 `docs/davis/bugs.md` 的 Review，Davis 合同管理系统的"催缴任务处理→收款确认→生成新合同"核心闭环存在 5 个严重逻辑缺陷，导致系统在关键业务路径上 **100% 崩溃或行为异常**。这些问题如果不修复，系统无法正常上线运行。

## 修改文件清单

| 文件 | 修改内容 |
|------|---------|
| `ruoyi-system/.../service/impl/CmsTaskServiceImpl.java` | 修复 Bug 1-5（核心修改） |
| `ruoyi-system/.../service/ICmsTaskService.java` | 修改 `completeRenewal` 方法签名 |
| `ruoyi-admin/.../controller/davis/CmsTaskController.java` | 修改 `completeRenewal` 接口适配新参数 |

---

## Bug 1 [致命]：completeCollectionTask 生成新合同必定崩溃

**根因**：
- 方法签名接收了 `CmsContract newContract` 参数，但方法体内重新 `new CmsContract()` 并仅手动拷贝了三个字段（contractName、contactPerson、contactPhone），**传入的 newContract 形参被完全忽略**
- **缺少 `customerId`**：`insertCmsContract` 第一行校验 `customerId == null → throw ServiceException("请选择关联客户")`，必抛异常
- **潜在 NPE**：`task.getSourceContractId()` 可能为 null（催收任务可能只有 contractId），随后 `sourceContract.getContractName()` 触发空指针

**修复方案**：

1. **增加 sourceContractId 空值保护**：当 `sourceContractId` 为 null 时，fallback 使用 `task.getContractId()`
2. **增加 sourceContract 空值校验**：查不到原合同时抛出明确的 ServiceException
3. **完整拷贝原合同字段**：从 sourceContract 拷贝所有业务字段，包括 `customerId`、`contractType`、`paymentCycle`、`paymentMethod`、`taxType`、`rentalAddress`、`ownerId`、`deptId`、`legalPerson`、`contactEmail` 等
4. **应用 newContract 覆盖值**：如果 `newContract != null`，用其 `amount`、`startDate`、`endDate`、`contractName` 覆盖对应字段
5. 设置 `parentId = sourceContract.getContractId()`，`auditStatus = "0"`（待审批）

**关键代码位置**：`CmsTaskServiceImpl.java` 第 221-245 行 `completeCollectionTask` 方法

---

## Bug 2 [严重]：讲价审批通知永远无法送达经理

**根因**：
- `returnToAdmin` 方法第 333 行：`Long managerId = Long.parseLong(existingTask.getCreateBy())`
- 但 `insertCmsTask` 第 110 行写入的是：`cmsTask.setCreateBy(SecurityUtils.getUsername())` —— 存的是用户名字符串（如 `"admin"`）
- `Long.parseLong("admin")` → `NumberFormatException`，被静默 catch 吞掉，通知代码完全跳过

**修复方案**：

1. 新增私有方法 `findUsersByRoleKey(String roleKey)`：通过 `sysRoleService.selectRoleList` 查找角色 → 获取 roleId → 通过 `sysUserService.selectAllocatedList` 获取该角色下所有用户
2. 修改 `returnToAdmin` 的通知逻辑：不再尝试从 `createBy` 解析 managerId，改为调用 `findUsersByRoleKey("admin")` 获取所有管理员用户，逐一发送通知
3. 同样修复 `requestTermination` 方法 —— 它也缺少提交终止审批后通知经理的逻辑（补全该通知）

---

## Bug 3 [严重]：经理拒绝终止合作后，任务类型无法恢复

**根因**：
- `requestTermination` 将 `taskType` 改为 `"3"`（终止）并持久化到数据库
- `confirmTermination` 拒绝时只把 `status` 改为 `"3"`（已退回），**未恢复 taskType**
- 后续如果会计重新走正常收款流程，taskType 依然是 `"3"` 而不是原来的 `"1"`（催收）或 `"2"`（续签），导致业务逻辑错乱

**修复方案**：

1. 在 `requestTermination` 中，更新前读取 `existingTask.getTaskType()` 作为 `originalTaskType`
2. 将 `originalTaskType` 记录到任务日志中（通过 `recordTaskLog` 的 remark 或扩展 log 内容）
3. 在 `confirmTermination` 拒绝分支中，从任务日志读取原始 taskType 并恢复：
   ```java
   updateTask.setStatus("3"); // 已退回
   updateTask.setTaskType(originalTaskType); // 恢复原类型
   ```
4. 实现方式：在 `requestTermination` 的日志 remark 中以约定格式附带 `originalTaskType`，`confirmTermination` 中通过 `cmsTaskLogService` 查询最近一条 actionType="3" 的日志，解析出原始 taskType

---

## Bug 4 [中等]：防重复派发逻辑失效

**根因**：
- `insertCmsTask` 幂等性检查（第 97-99 行）的查询条件包含了 `AssignedTo`
- 同一合同 + 同一任务类型，如果分配给不同会计，校验通过 → 生成多个重复任务

**修复方案**：

从幂等性检查的查询条件中**移除 `assignedTo`**：

```java
// 修改前
queryTask.setContractId(cmsTask.getContractId());
queryTask.setAssignedTo(cmsTask.getAssignedTo());  // ← 删除这行
queryTask.setTaskType(cmsTask.getTaskType());

// 修改后
queryTask.setContractId(cmsTask.getContractId());
queryTask.setTaskType(cmsTask.getTaskType());
```

同时更新错误提示信息为：`"该合同已有进行中的同类型任务，请勿重复派发"`

---

## Bug 5 [中等]：completeRenewal 未实现设计文档要求

**根因**：
- 设计文档要求 `completeRenewal` 支持 `newAmount`、`newStartDate`、`newEndDate`、`generateContract` 参数
- 当前实现仅接收 `CmsTask`，只做了 `setStatus("4")`，完全没有新合同生成逻辑

**修复方案**：

**接口层**（`ICmsTaskService.java`）：
- 修改 `completeRenewal` 方法签名，改为 `completeRenewal(Long taskId, CmsContract newContract, boolean generateContract)`

**实现层**（`CmsTaskServiceImpl.java`）：
- 将任务状态更新为已完成
- 若 `generateContract == true` 且 `newContract != null`：
  - 复用与 `completeCollectionTask` 相同的新合同生成逻辑（提取为私有方法 `createNewContractFromTask`）
  - 从原合同拷贝字段，用 `newContract` 的值覆盖金额/日期
- 若 `generateContract == false`：仅标记任务完成

**控制器层**（`CmsTaskController.java`）：
- 修改 `/completeRenewal` 接口，接收包含 `taskId`、`newContract`、`generateContract` 的请求体

**提取公共方法**：
- 将 `completeCollectionTask` 中"从原合同生成新合同"的逻辑提取为私有方法 `buildNewContractFromSource(CmsTask task, CmsContract newContract)`，供 `completeCollectionTask` 和 `completeRenewal` 复用

---

## 验证方案

1. **编译验证**：`mvn clean package -Dmaven.test.skip=true -pl ruoyi-admin -am` 确保编译通过
2. **启动系统**：`java -jar ruoyi-admin/target/ruoyi-admin.jar`，确保 Spring 上下文正常加载
3. **手工验证场景**：
   - 创建催收任务 → 确认收款 + 勾选"生成新合同" → 验证新合同已创建且 `customerId` 不为空
   - 会计提交讲价审批 → 检查管理员是否收到站内信
   - 会计提交终止合作 → 经理拒绝 → 检查任务类型是否恢复
   - 对同一合同重复派发任务 → 应收到"请勿重复派发"错误
   - 续签任务完成 + 生成新合同 → 验证新合同创建成功
