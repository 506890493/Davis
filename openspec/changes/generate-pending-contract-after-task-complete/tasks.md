# 会计完成任务后生成待审批合同 - 实现任务

## 后端实现

### 任务 1：修改 completeRenewal 方法创建续费合同

**文件**: `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsTaskServiceImpl.java`

**位置**: 约第 495-513 行，`completeRenewal` 方法

**步骤**:

1. **方法签名确认** - 确保 `completeRenewal` 方法参数包含：
   ```java
   public int completeRenewal(CmsTask task)
   ```
   
2. **在方法开头添加获取原合同逻辑**
   ```java
   CmsTask existingTask = cmsTaskMapper.selectCmsTaskByTaskId(task.getTaskId());
   if (existingTask == null) {
       throw new ServiceException("任务不存在");
   }
   
   // 获取源合同ID
   Long sourceContractId = existingTask.getSourceContractId();
   if (sourceContractId == null) {
       sourceContractId = existingTask.getContractId();
   }
   if (sourceContractId == null) {
       throw new ServiceException("无法获取关联合同");
   }
   
   // 获取原合同信息
   CmsContract sourceContract = cmsContractMapper.selectCmsContractByContractId(sourceContractId);
   if (sourceContract == null) {
       throw new ServiceException("关联合同不存在");
   }
   ```

3. **在更新任务状态前添加创建新合同逻辑**
   ```java
   // 创建续费合同
   CmsContract newContract = new CmsContract();
   newContract.setContractName(sourceContract.getContractName() + "-续费");
   newContract.setContractType(sourceContract.getContractType());
   newContract.setLegalPerson(sourceContract.getLegalPerson());
   newContract.setContactPerson(sourceContract.getContactPerson());
   newContract.setContactPhone(sourceContract.getContactPhone());
   newContract.setContactEmail(sourceContract.getContactEmail());
   newContract.setTaxType(sourceContract.getTaxType());
   newContract.setAmount(task.getNewAmount());  // 新金额
   newContract.setPaymentCycle(sourceContract.getPaymentCycle());
   newContract.setPaymentDate(sourceContract.getPaymentDate());
   newContract.setPaymentMethod(sourceContract.getPaymentMethod());
   newContract.setStartDate(task.getStartDate());  // 新开始日期
   newContract.setEndDate(task.getEndDate());    // 新结束日期
   newContract.setOwnerId(sourceContract.getOwnerId());
   newContract.setCustomerId(sourceContract.getCustomerId());
   newContract.setAuditStatus("0");  // 待审批
   newContract.setParentId(sourceContractId);  // 关联原合同
   newContract.setCreateBy(SecurityUtils.getUsername());
   newContract.setCreateTime(DateUtils.getNowDate());
   newContract.setReminderStatus("0");  // 初始催交状态
   cmsContractMapper.insertCmsContract(newContract);
   ```

4. **验证新增合同ID获取**
   - `insertCmsContract` 使用 `useGeneratedKeys="true" keyProperty="contractId"`
   - 插入后通过 `newContract.getContractId()` 获取新合同ID

5. **更新任务状态为已完成**
   ```java
   CmsTask updateTask = new CmsTask();
   updateTask.setTaskId(task.getTaskId());
   updateTask.setStatus("4"); // 已完成
   updateTask.setRemark(task.getRemark());
   updateTask.setUpdateTime(DateUtils.getNowDate());
   updateTask.setUpdateBy(SecurityUtils.getUsername());
   int result = cmsTaskMapper.updateCmsTask(updateTask);
   ```

6. **记录任务日志**
   ```java
   if (result > 0) {
       recordTaskLog(task.getTaskId(), "2", existingTask.getStatus(), "4", 
           "完成续签并创建合同: " + newContract.getContractName());
   }
   ```

**验证**:
- 调用 `/system/task/completeRenewal` 后，数��库 `cms_contract` 新增一条记录
- 新合同 `audit_status = '0'`，`parent_id` 关联原合同
- 经理在待审批页面能看到该合同

---

### 任务 2：添加通知提醒（可选）

**文件**: `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsTaskServiceImpl.java`

**依赖**: 已在 `CmsTaskServiceImpl` 中注入 `ICmsNotificationService`

**步骤**:
```java
@Autowired
private ICmsNotificationService notificationService;

// 在 completeRenewal 方法中，创建新合同后添加：
Long managerUserId = 1L; // 或通过角色查询获取经理ID
notificationService.createNotification(
    managerUserId,
    "新续费合同待审批: " + newContract.getContractName(),
    "合同金额: " + newContract.getAmount() + "元，期限: " + task.getStartDate() + " 至 " + task.getEndDate(),
    "contract",
    newContract.getContractId()
);
```

---

## 验收标准

1. ✅ 会计完成续签任务后，系统自动创建新续费合同
2. ✅ 新合同 `audit_status = '0'`（待审批）
3. ✅ 新合同 `parent_id` 正确关联原合同
4. ✅ 经理在待审批页面能看到该合同
5. ✅ 原合同保持不变
6. ✅ 任务状态更新为已完成
7. ✅ 前端提示成功

---

## 变更文件清单

| 文件 | 变更类型 | 说明 |
|------|----------|------|
| `CmsTaskServiceImpl.java` | 修改 | completeRenewal 方法添加创建合同逻辑 |