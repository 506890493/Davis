# 会计完成任务后生成待审批合同 - 技术设计

## 系统架构

沿用现有 RuoYi-Vue 三层架构：
- **Controller 层**：处理 HTTP 请求（已有 CmsTaskController）
- **Service 层**：业务逻辑处理（CmsTaskServiceImpl）
- **Mapper 层**：数据访问（CmsContractMapper）

## 数据模型

### 现有字段（已存在）
- `CmsContract.contractId`：合同主键
- `CmsContract.contractName`：公司名称
- `CmsContract.contractType`：合同类型 ('1'=代账, '2'=地址)
- `CmsContract.amount`：金额
- `CmsContract.startDate`：开始日期
- `CmsContract.endDate`：结束日期
- `CmsContract.ownerId`：会计ID
- `CmsContract.parentId`：父合同ID（关联原合同）
- `CmsContract.auditStatus`：审批状态 ('0'=待审批, '1'=已审批, '2'=已拒绝)
- `CmsTask.sourceContractId`：源合同ID（用于任务关联）
- `CmsTask.newAmount`：新金额（续签）
- `CmsTask.startDate`：新开始日期
- `CmsTask.endDate`：新结束日期

无需新增数据表或字段。

## 功能模块设计

### 后端实现：completeRenewal 方法增强

修改 `CmsTaskServiceImpl.completeRenewal()` 方法：

```java
@Override
@Transactional
public int completeRenewal(CmsTask task)
{
    // 1. 获取任务和原合同信息
    CmsTask existingTask = cmsTaskMapper.selectCmsTaskByTaskId(task.getTaskId());
    Long sourceContractId = existingTask.getSourceContractId();
    if (sourceContractId == null) {
        sourceContractId = existingTask.getContractId();
    }
    CmsContract sourceContract = cmsContractMapper.selectCmsContractByContractId(sourceContractId);
    
    // 2. 创建续费合同
    CmsContract newContract = new CmsContract();
    newContract.setContractName(sourceContract.getContractName() + "-续费");
    newContract.setContractType(sourceContract.getContractType());
    newContract.setAmount(task.getNewAmount());  // 新金额
    newContract.setStartDate(task.getStartDate());
    newContract.setEndDate(task.getEndDate());
    newContract.setOwnerId(sourceContract.getOwnerId());
    newContract.setAuditStatus("0");  // 待审批
    newContract.setParentId(sourceContractId);  // 关联原合同
    newContract.setCreateBy(SecurityUtils.getUsername());
    newContract.setCreateTime(DateUtils.getNowDate());
    cmsContractMapper.insertCmsContract(newContract);
    
    // 3. 更新任务状态
    CmsTask updateTask = new CmsTask();
    updateTask.setTaskId(task.getTaskId());
    updateTask.setStatus("4"); // 已完成
    updateTask.setRemark(task.getRemark());
    updateTask.setUpdateTime(DateUtils.getNowDate());
    updateTask.setUpdateBy(SecurityUtils.getUsername());
    int result = cmsTaskMapper.updateCmsTask(updateTask);
    
    // 4. 记录任务日志
    if (result > 0) {
        recordTaskLog(task.getTaskId(), "2", existingTask.getStatus(), "4", 
            "完成续签并创建合同: " + newContract.getContractName());
    }
    
    return result;
}
```

### 任务数据获取

前端调用 `completeRenewal` 时传递的数据：
```javascript
{
    taskId: 123,
    newAmount: 6000,           // 新金额
    startDate: "2027-01-01",   // 新开始日期
    endDate: "2027-12-31",     // 新结束日期
    remark: "续费一年"
}
```

## 接口设计

### 现有接口（无需改动）
- `POST /system/task/completeRenewal` - 完成续签任务

前端已传递 `newAmount`, `startDate`, `endDate`，后端直接使用。

## 数据流

```
[前端：会计点击"完成续签"]
    ↓ POST /system/task/completeRenewal
[后端：CmsTaskController.completeRenewal]
    ↓
[CmsTaskServiceImpl.completeRenewal]
    ↓ 1. 查询任务和原合同
    ↓ 2. 创建新续费合同（audit_status='0'）
    ↓ 3. 更新任务状态
    ↓ 4. 记录日志
[数据库：cms_contract 新增记录]
    ↓
[前端：刷新列表，提示成功]
    ↓
[经理：待审批页面看到新合同]
```

## 异常处理

- 原合同不存在：抛出 ServiceException
- 创建合同失败：事务回滚，返回错误
- 任务不存在：返回错误提示

## 通知提醒（可选）

创建新合同后，可额外添加通知提醒经理：
- 调用 NotificationService 创建通知
- 标题："有新续费合同待审批"
- 内容：合同名称 + 金额