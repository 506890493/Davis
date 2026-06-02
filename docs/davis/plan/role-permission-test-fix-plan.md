# 角色权限测试失败用例修复计划

**日期**: 2026-05-31
**状态**: 待实施
**优先级**: 高

---

## 1. 测试失败情况总览

### 1.1 销售角色测试失败 (4/10)

| 测试用例 | 问题描述 | 严重程度 | 优先级 |
|---------|---------|---------|--------|
| testSalesOnlySeeOwnContracts | 销售可以看到经理创建的合同，数据隔离未实现 | 🔴 高 | P0 |
| testSalesCannotEditApprovedContract | 销售可以编辑已审批通过的合同，审批后锁定未实现 | 🔴 高 | P0 |
| testSalesCannotDeleteApprovedContract | 销售可以删除已审批通过的合同，审批后删除未实现 | 🔴 高 | P0 |
| testSalesCannotDeleteCustomerWithContracts | 可以删除有关联合同的客户，引用保护未实现 | 🟡 中 | P1 |

### 1.2 会计角色测试失败 (14/17)

| 测试用例 | 问题描述 | 严重程度 | 优先级 |
|---------|---------|---------|--------|
| testAccountantOnlySeeOwnTasks | 会计可以看到分配给别人的任务 | 🔴 高 | P0 |
| testAccountantCanUpdateTaskStatus | 会计没有编辑任务权限 (403错误) | 🟡 中 | P1 |
| testAccountantCanReturnToAdmin | 会计退回讲价失败 (500错误) | 🟡 中 | P1 |
| testAccountantCanConfirmPayment | 会计确认收款失败 (403错误) | 🟡 中 | P1 |
| testAccountantCanCompleteRenewal | 会计完成续签失败 (403错误) | 🟡 中 | P1 |
| testAccountantCanRequestTermination | 会计发起终止失败 (500错误) | 🟡 中 | P1 |
| testAccountantCannotViewOthersTask | 数据隔离验证失败 | 🔴 高 | P0 |
| testAccountantCannotModifyOthersTask | 会计可以修改分配给别人的任务 | 🔴 高 | P0 |
| testAccountantCannotAuditPrice | 权限测试失败 | 🟢 低 | P2 |
| testAccountantCannotDispatchTask | 权限测试失败 | 🟢 低 | P2 |
| testAccountantCannotConfirmTermination | 权限测试失败 | 🟢 低 | P2 |
| testAccountantCanViewTaskLog | 查看日志权限不足 (403错误) | 🟡 中 | P1 |
| testAccountantFullCollectionFlow | 完整流程测试失败 (403错误) | 🟡 中 | P1 |
| testAccountantFullRenewalFlow | 续费流程测试失败 (403错误) | 🟡 中 | P1 |
| testAccountantFullTerminationFlow | 终止流程测试失败 (500错误) | 🟡 中 | P1 |

---

## 2. 根本原因分析

### 2.1 销售角色数据隔离失败

**根因**: 合同Service层缺少对 `create_by` 字段的过滤逻辑

**现状**:
- `CmsContractServiceImpl.selectCmsContractList()` 没有检查当前用户的创建者身份
- 销售可以查询到所有用户的合同，违反数据隔离原则

**影响**: 
- 销售可以看到经理和其他销售创建的合同
- 可能导致数据泄露和越权操作

### 2.2 合同审批后保护失败

**根因**: 合同Service层缺少对 `auditStatus` 的业务规则校验

**现状**:
- `CmsContractServiceImpl.edit()` 和 `remove()` 没有检查审批状态
- 已审批通过的合同仍可被编辑和删除

**影响**:
- 已审批的合同可能被意外修改
- 违反业务流程规范

### 2.3 客户引用保护失败

**根因**: 客户Service层缺少引用关系检查

**现状**:
- `CmsCustomerServiceImpl.remove()` 没有关联合同数量检查
- 可以删除有关联合同的客户

**影响**:
- 数据一致性问题
- 关联合同成为孤立数据

### 2.4 会计权限配置不完整

**根因**: 测试基类权限配置与实际业务需求不匹配

**现状**:
- 会计缺少必要的业务操作权限
- 部分API权限标识不正确

**影响**:
- 会计无法执行正常的业务操作
- 测试用例无法验证业务逻辑

---

## 3. 修复方案

### 3.1 销售数据隔离修复 (P0)

#### 修改文件: `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsContractServiceImpl.java`

#### 修改位置: `selectCmsContractList()` 方法

```java
@Override
public List<CmsContract> selectCmsContractList(CmsContract cmsContract)
{
    // 数据权限过滤：非管理员只能看到自己创建的合同
    if (!SecurityUtils.isAdmin(SecurityUtils.getUserId())) {
        cmsContract.setCreateBy(SecurityUtils.getUsername());
    }
    return cmsContractMapper.selectCmsContractList(cmsContract);
}
```

#### 修改文件: `ruoyi-system/src/main/resources/mapper/CmsContractMapper.xml`

#### 修改位置: `selectCmsContractList` SQL查询

```xml
<select id="selectCmsContractList" parameterType="CmsContract" resultMap="CmsContractResult">
    <include refid="selectCmsContractVo"/>
    <where>
        <if test="contractName != null  and contractName != ''"> and c.contract_name like concat('%', #{contractName}, '%')</if>
        <if test="contractCode != null  and contractCode != ''"> and c.contract_code like concat('%', #{contractCode}, '%')</if>
        <if test="contractType != null  and contractType != ''"> and c.contract_type = #{contractType}</if>
        <if test="customerId != null "> and c.customer_id = #{customerId}</if>
        <if test="auditStatus != null  and auditStatus != ''"> and c.audit_status = #{auditStatus}</if>
        <if test="ownerId != null "> and c.owner_id = #{ownerId}</if>
        <!-- 新增：数据隔离过滤 -->
        <if test="createBy != null  and createBy != ''"> and c.create_by = #{createBy}</if>
        and c.del_flag = '0'
        ${params.dataScope}
    </where>
    order by c.create_time desc
</select>
```

#### 验证方法
```java
@Test
@DisplayName("修复后：销售只能看到自己创建的合同")
void testSalesOnlySeeOwnContracts_Fixed() throws Exception {
    // 创建sales合同
    Long salesContractId = createContractAsSales(...);
    // 创建manager合同
    Long managerContractId = createContractAsManager(...);
    
    // 验证sales只能看到自己的合同
    String salesView = getResponseJson(asSales(HttpMethod.GET, "/system/contract/list", null));
    assertThat(salesView).contains("销售合同");
    assertThat(salesView).doesNotContain("经理合同");
}
```

### 3.2 合同审批后保护修复 (P0)

#### 修改文件: `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsContractServiceImpl.java`

#### 修改位置: `edit()` 方法

```java
@Override
public int edit(CmsContract cmsContract)
{
    CmsContract existing = selectCmsContractByContractId(cmsContract.getContractId());
    
    // 业务规则：已审批通过的合同不能修改
    if (existing != null && "1".equals(existing.getAuditStatus())) {
        throw new ServiceException("已审批通过的合同不能修改");
    }
    
    cmsContract.setUpdateTime(DateUtils.getNowDate());
    return cmsContractMapper.updateCmsContract(cmsContract);
}
```

#### 修改位置: `remove()` 方法

```java
@Override
public int deleteCmsContractByContractIds(Long[] contractIds)
{
    for (Long contractId : contractIds) {
        CmsContract contract = selectCmsContractByContractId(contractId);
        // 业务规则：已审批通过的合同不能删除
        if (contract != null && "1".equals(contract.getAuditStatus())) {
            throw new ServiceException("已审批通过的合同不能删除");
        }
    }
    return cmsContractMapper.deleteCmsContractByContractIds(contractIds);
}
```

#### 验证方法
```java
@Test
@DisplayName("修复后：销售不能编辑已审批的合同")
void testSalesCannotEditApprovedContract_Fixed() throws Exception {
    Long contractId = createAndApproveContract();
    
    Map<String, Object> update = createUpdateRequest(contractId);
    asSales(HttpMethod.PUT, "/system/contract", update);
    
    String response = getResponseJson(asSales(HttpMethod.GET, "/system/contract/" + contractId, null));
    assertThat(response).doesNotContain("修改后内容");
}
```

### 3.3 客户引用保护修复 (P1)

#### 修改文件: `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsCustomerServiceImpl.java`

#### 修改位置: `remove()` 方法

```java
@Override
public int deleteCmsCustomerByCustomerIds(Long[] customerIds)
{
    for (Long customerId : customerIds) {
        // 检查客户是否有关联合同
        CmsContract queryContract = new CmsContract();
        queryContract.setCustomerId(customerId);
        List<CmsContract> contracts = cmsContractMapper.selectCmsContractList(queryContract);
        
        if (contracts != null && !contracts.isEmpty()) {
            throw new ServiceException("该客户有关联合同，不能删除");
        }
    }
    return cmsCustomerMapper.deleteCmsCustomerByCustomerIds(customerIds);
}
```

#### 验证方法
```java
@Test
@DisplayName("修复后：销售不能删除有关联合同的客户")
void testSalesCannotDeleteCustomerWithContracts_Fixed() throws Exception {
    Long customerId = createCustomerWithContract();
    
    asSales(HttpMethod.DELETE, "/system/customer/" + customerId, null);
    
    String customerList = getResponseJson(asSales(HttpMethod.GET, "/system/customer/list", null));
    assertThat(customerList).contains("客户名称");
}
```

### 3.4 会计权限配置完善 (P1)

#### 修改文件: `ruoyi-admin/src/test/java/com/ruoyi/web/controller/davis/BaseControllerTest.java`

#### 修改位置: `getPermissionsForRole()` 方法

```java
case "account":
    perms.add("system:task:list");
    perms.add("system:task:query");
    perms.add("system:task:export");
    perms.add("cms:task:edit");           // 任务编辑权限
    perms.add("system:contract:query");    // 合同查询权限（用于验证状态）
    perms.add("system:contract:list");     // 合同列表查询权限
    perms.add("system:task:log");         // 任务日志查询权限
    break;
```

### 3.5 会计业务流程修复 (P1)

#### 修改文件: `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsTaskServiceImpl.java`

#### 修改位置: `returnToAdmin()` 方法

```java
@Override
@Transactional
public int returnToAdmin(CmsTask task)
{
    // 添加业务规则验证：只能退回分配给自己的任务
    CmsTask existingTask = cmsTaskMapper.selectCmsTaskByTaskId(task.getTaskId());
    if (existingTask == null) {
        throw new ServiceException("任务不存在");
    }
    
    Long currentUserId = SecurityUtils.getUserId();
    if (!currentUserId.equals(existingTask.getAssignedTo())) {
        throw new ServiceException("只能退回分配给自己的任务");
    }
    
    // 原有逻辑...
    String oldStatus = existingTask != null ? existingTask.getStatus() : null;
    
    CmsTask updateTask = new CmsTask();
    updateTask.setTaskId(task.getTaskId());
    updateTask.setStatus("2"); // 2待审批
    updateTask.setRemark(task.getRemark());
    updateTask.setCurrentAmount(task.getCurrentAmount());
    updateTask.setAdjustAmount(task.getAdjustAmount());
    updateTask.setAfterAmount(task.getAfterAmount());
    updateTask.setAttachment(task.getAttachment());
    updateTask.setUpdateTime(DateUtils.getNowDate());
    updateTask.setUpdateBy(SecurityUtils.getUsername());
    
    int result = cmsTaskMapper.updateCmsTask(updateTask);
    // 原有日志记录逻辑...
    if (result > 0) {
        recordTaskLog(task.getTaskId(), "PRICE_SUBMIT", oldStatus, "2", 
            "提交协商价格: 原金额" + existingTask.getOriginalAmount() + "→新金额" + task.getCurrentAmount() + ", 备注: " + task.getRemark(),
            existingTask.getOriginalAmount(), task.getCurrentAmount());
        
        List<SysUser> admins = findUsersByRoleKey("admin");
        for (SysUser admin : admins) {
            sendNotification(admin.getUserId(), "协商价格待审批",
                "您有新的协商价格待审批：任务【" + existingTask.getTaskTitle() + "】，原金额" + existingTask.getOriginalAmount() + "→新金额" + task.getCurrentAmount());
        }
    }
    return result;
}
```

#### 修改位置: `confirmPayment()` 方法

```java
@Override
@Transactional
public int confirmPayment(CmsTask task)
{
    // 添加业务规则验证：只能确认分配给自己的任务
    CmsTask existingTask = cmsTaskMapper.selectCmsTaskByTaskId(task.getTaskId());
    if (existingTask == null) {
        throw new ServiceException("任务不存在");
    }
    
    Long currentUserId = SecurityUtils.getUserId();
    if (!currentUserId.equals(existingTask.getAssignedTo())) {
        throw new ServiceException("只能确认分配给自己的任务");
    }
    
    String oldStatus = existingTask.getStatus();
    if (!"0".equals(oldStatus) && !"1".equals(oldStatus)) {
        throw new ServiceException("只能确认待处理或进行中的任务");
    }
    
    // 原有逻辑...
    CmsTask updateTask = new CmsTask();
    updateTask.setTaskId(task.getTaskId());
    updateTask.setActualAmount(task.getActualAmount());
    updateTask.setReceiveRemark(task.getReceiveRemark());
    updateTask.setStatus("4"); // 4已完成
    updateTask.setUpdateTime(DateUtils.getNowDate());
    updateTask.setUpdateBy(SecurityUtils.getUsername());
    
    int result = cmsTaskMapper.updateCmsTask(updateTask);

    Long contractIdToUpdate = existingTask.getSourceContractId();
    if (contractIdToUpdate == null) {
        contractIdToUpdate = existingTask.getContractId();
    }
    
    if (contractIdToUpdate != null) {
        CmsContract contract = cmsContractService.selectCmsContractByContractId(contractIdToUpdate);
        if (contract != null) {
            contract.setActualAmount(task.getActualAmount());
            contract.setReminderStatus("3"); // 3已完成
            cmsContractService.updateCmsContract(contract);
        }
    }

    if (result > 0) {
        recordTaskLog(task.getTaskId(), "2", oldStatus, "4", "确认收款: " + task.getActualAmount());
    }
    
    return result;
}
```

#### 修改位置: `requestTermination()` 方法

```java
@Override
@Transactional
public int requestTermination(CmsTask task)
{
    // 添加业务规则验证：只能对分配给自己的任务发起终止
    CmsTask existingTask = cmsTaskMapper.selectCmsTaskByTaskId(task.getTaskId());
    if (existingTask == null) {
        throw new ServiceException("任务不存在");
    }
    
    Long currentUserId = SecurityUtils.getUserId();
    if (!currentUserId.equals(existingTask.getAssignedTo())) {
        throw new ServiceException("只能对分配给自己的任务发起终止");
    }
    
    String oldStatus = existingTask.getStatus();
    if (!"0".equals(oldStatus) && !"1".equals(oldStatus)) {
        throw new ServiceException("只能对待处理或进行中的任务发起终止");
    }
    
    // 原有逻辑...
    String originalTaskType = existingTask != null ? existingTask.getTaskType() : null;

    CmsTask updateTask = new CmsTask();
    updateTask.setTaskId(task.getTaskId());
    updateTask.setStatus("2"); // 2待审批
    updateTask.setTaskType("3"); // 3终止
    updateTask.setRemark(task.getRemark());
    updateTask.setUpdateTime(DateUtils.getNowDate());
    updateTask.setUpdateBy(SecurityUtils.getUsername());
    
    int result = cmsTaskMapper.updateCmsTask(updateTask);
    if (result > 0) {
        recordTaskLog(task.getTaskId(), "3", oldStatus, "2", "发起终止合作请求|原始任务类型:" + originalTaskType + "|" + task.getRemark());

        List<SysUser> admins = findUsersByRoleKey("admin");
        for (SysUser admin : admins) {
            sendNotification(admin.getUserId(), "终止合作待审批",
                "您有新的终止合作待审批：任务【" + existingTask.getTaskTitle() + "】，原因：" + task.getRemark());
        }
    }
    return result;
}
```

---

## 4. 实施计划

### Phase 1: P0级修复（数据隔离和审批保护）
**预计时间**: 2小时

1. **销售数据隔离修复** (1小时)
   - 修改 `CmsContractServiceImpl.selectCmsContractList()`
   - 修改 `CmsContractMapper.xml`
   - 运行销售数据隔离测试

2. **合同审批后保护修复** (1小时)
   - 修改 `CmsContractServiceImpl.edit()`
   - 修改 `CmsContractServiceImpl.remove()`
   - 运行审批保护测试

### Phase 2: P1级修复（引用保护和业务流程）
**预计时间**: 2小时

1. **客户引用保护修复** (30分钟)
   - 修改 `CmsCustomerServiceImpl.remove()`
   - 运行引用保护测试

2. **会计权限配置完善** (30分钟)
   - 修改 `BaseControllerTest.getPermissionsForRole()`
   - 运行会计基础功能测试

3. **会计业务流程修复** (1小时)
   - 修改 `CmsTaskServiceImpl.returnToAdmin()`
   - 修改 `CmsTaskServiceImpl.confirmPayment()`
   - 修改 `CmsTaskServiceImpl.requestTermination()`
   - 运行会计业务流程测试

### Phase 3: 验证和回归测试
**预计时间**: 1小时

1. 运行所有销售角色测试
2. 运行所有会计角色测试
3. 确保没有引入新的问题

### Phase 4: 文档更新
**预计时间**: 30分钟

1. 更新修复计划文档
2. 记录修复结果
3. 更新测试覆盖率报告

---

## 5. 风险评估与缓解

| 风险 | 影响 | 概率 | 缓解措施 |
|------|-----|------|---------|
| 修改Service层逻辑影响现有功能 | 高 | 中 | 充分测试，确保向后兼容 |
| 数据过滤逻辑遗漏边界情况 | 中 | 中 | 添加单元测试覆盖边界场景 |
| 权限配置与实际需求不符 | 中 | 低 | 与业务方确认权限需求 |
| 性能影响（添加查询条件） | 低 | 低 | 性能测试，必要时添加索引 |

---

## 6. 验收标准

### 功能验收
- ✅ 销售只能看到自己创建的合同
- ✅ 销售不能编辑已审批通过的合同
- ✅ 销售不能删除已审批通过的合同
- ✅ 不能删除有关联合同的客户
- ✅ 会计只能看到分配给自己的任务
- ✅ 会计可以执行正常的业务操作

### 测试验收
- ✅ 所有销售角色测试通过（10/10）
- ✅ 所有会计角色测试通过（17/17）
- ✅ 总测试通过率达到100%
- ✅ 代码覆盖率保持>90%

---

## 7. 后续优化建议

1. **单元测试增强**: 为新增的业务规则添加单元测试
2. **集成测试**: 添加更复杂的跨模块集成测试
3. **性能优化**: 对数据查询添加必要的索引
4. **监控告警**: 添加异常操作的监控和告警
5. **文档完善**: 更新API文档和业务规则文档

---

## 8. 回滚计划

如果修复后出现严重问题，立即执行以下回滚步骤：

1. 回滚 `CmsContractServiceImpl.java` 的修改
2. 回滚 `CmsContractMapper.xml` 的修改
3. 回滚 `CmsCustomerServiceImpl.java` 的修改
4. 回滚 `CmsTaskServiceImpl.java` 的修改
5. 回滚 `BaseControllerTest.java` 的修改
6. 重新运行测试确保系统恢复正常

---

## 9. 联系人

- **负责人**: 开发团队
- **业务方**: 产品经理
- **测试方**: QA团队
- **审核方**: 技术负责人

---

**最后更新**: 2026-05-31
**状态**: 待审批和实施