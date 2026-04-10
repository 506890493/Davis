# 催缴任务管理模块优化实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 优化催缴任务管理流程，实现任务分配下拉选择、待审批任务页面、协商价格审批、生成新合同和完善审计日志功能

**Architecture:** 基于现有RuoYi-Vue前后分离架构，在现有任务管理模块基础上扩展：前端新增待审批页面和优化创建任务对话框，后端扩展Service方法和审计日志字段

**Tech Stack:** Spring Boot 2.5.15 + MyBatis + Vue 2.6 + Element UI 2.15

---

## 文件结构

```
ruoyi-ui/src/
├── api/system/task.js                    # 任务API（新增接口）
├── views/system/task/
│   ├── index.vue                         # 任务列表（优化创建任务对话框）
│   ├── pending.vue                       # 新增：待审批任务页面
│   └── components/
│       ├── TaskFormDialog.vue            # 新增：创建/编辑任务对话框
│       ├── PriceNegotiationDialog.vue    # 新增：协商价格对话框
│       └── CompleteTaskDialog.vue        # 新增：完成任务弹窗（生成合同）

ruoyi-admin/src/main/java/com/ruoyi/web/controller/davis/
├── CmsTaskController.java                # 扩展接口
├── CmsContractController.java            # 创建合同接口

ruoyi-system/src/main/java/com/ruoyi/system/
├── domain/CmsTaskLog.java                # 扩展字段
├── service/impl/CmsTaskServiceImpl.java  # 业务逻辑扩展
└── mapper/CmsTaskLogMapper.xml           # 新增SQL

数据库:
├── cms_task_log 表新增字段: amount_before, amount_after
└── sys_menu 表新增菜单（需手动在系统配置）
```

---

## 阶段一：后端扩展

### Task 1.1: 扩展审计日志表结构

**Files:**
- Modify: `ruoyi-system/src/main/java/com/ruoyi/system/domain/CmsTaskLog.java`
- Modify: `ruoyi-system/src/main/resources/mapper/system/CmsTaskLogMapper.xml`
- Create: `sql/cms_task_log_add_columns.sql`

- [ ] **Step 1: 编写SQL脚本扩展表结构**

```sql
-- sql/cms_task_log_add_columns.sql
ALTER TABLE cms_task_log 
ADD COLUMN amount_before DECIMAL(15,2) DEFAULT NULL COMMENT '变更前金额',
ADD COLUMN amount_after DECIMAL(15,2) DEFAULT NULL COMMENT '变更后金额';
```

- [ ] **Step 2: 修改 CmsTaskLog.java 实体类**

在 CmsTaskLog.java 中添加字段：
```java
/** 变更前金额 */
private BigDecimal amountBefore;

/** 变更后金额 */
private BigDecimal amountAfter;

public void setAmountBefore(BigDecimal amountBefore) { this.amountBefore = amountBefore; }
public BigDecimal getAmountBefore() { return amountBefore; }
public void setAmountAfter(BigDecimal amountAfter) { this.amountAfter = amountAfter; }
public BigDecimal getAmountAfter() { return amountAfter; }
```

- [ ] **Step 3: 修改 CmsTaskLogMapper.xml**

在 `<resultMap>` 中添加映射：
```xml
<result property="amountBefore" column="amount_before" />
<result property="amountAfter" column="amount_after" />
```

- [ ] **Step 4: 执行SQL脚本**

连接数据库执行 `sql/cms_task_log_add_columns.sql`

- [ ] **Step 5: 提交**

```bash
git add sql/ ruoyi-system/src/main/java/com/ruoyi/system/domain/CmsTaskLog.java ruoyi-system/src/main/resources/mapper/system/CmsTaskLogMapper.xml
git commit -m "feat(task): 扩展任务日志表结构，添加金额变更字段"
```

---

### Task 1.2: 完善审计日志记录方法

**Files:**
- Modify: `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsTaskServiceImpl.java`

- [ ] **Step 1: 修改 recordTaskLog 方法支持金额记录**

在 CmsTaskServiceImpl.java 中找到 `recordTaskLog` 方法，修改为：
```java
private void recordTaskLog(Long taskId, String actionType, String beforeStatus, String afterStatus, String remark) {
    recordTaskLog(taskId, actionType, beforeStatus, afterStatus, remark, null, null);
}

private void recordTaskLog(Long taskId, String actionType, String beforeStatus, String afterStatus, String remark, BigDecimal amountBefore, BigDecimal amountAfter) {
    CmsTaskLog log = new CmsTaskLog();
    log.setTaskId(taskId);
    log.setOperatorId(SecurityUtils.getUserId());
    log.setOperatorName(SecurityUtils.getUsername());
    log.setActionType(actionType);
    log.setBeforeStatus(beforeStatus);
    log.setAfterStatus(afterStatus);
    log.setRemark(remark);
    log.setAmountBefore(amountBefore);
    log.setAmountAfter(amountAfter);
    log.setCreateTime(DateUtils.getNowDate());
    cmsTaskLogService.insertCmsTaskLog(log);
}
```

- [ ] **Step 2: 修改 returnToAdmin 方法记录金额**

在 `returnToAdmin` 方法中，修改日志记录：
```java
// 原代码
recordTaskLog(task.getTaskId(), "3", oldStatus, "3", "任务退回: " + task.getRemark());
// 修改为
recordTaskLog(task.getTaskId(), "PRICE_SUBMIT", oldStatus, "3", 
    "提交协商价格: 原金额" + existingTask.getOriginalAmount() + "→新金额" + task.getCurrentAmount() + ", 备注: " + task.getRemark(),
    existingTask.getOriginalAmount(), task.getCurrentAmount());
```

- [ ] **Step 3: 修改 redispatch 方法记录金额**

在 `redispatch` 方法中，修改日志记录：
```java
// 原代码
recordTaskLog(task.getTaskId(), "5", oldStatus, "0", "重新派发任务");
// 修改为
recordTaskLog(task.getTaskId(), "PRICE_APPROVE", oldStatus, "0", 
    "同意协商价格: 新金额" + task.getCurrentAmount() + ", 备注: " + (task.getRemark() != null ? task.getRemark() : ""),
    existingTask.getCurrentAmount(), task.getCurrentAmount());
```

- [ ] **Step 4: 添加拒绝协商价格方法**

在 CmsTaskController.java 中添加：
```java
/**
 * 经理拒绝协商价格
 */
@PreAuthorize("@ss.hasPermi('cms:task:audit')")
@Log(title = "任务管理", businessType = BusinessType.UPDATE)
@PostMapping("/rejectPrice")
public AjaxResult rejectPrice(@RequestBody CmsTask task) {
    return toAjax(cmsTaskService.rejectPrice(task));
}
```

在 ICmsTaskService.java 添加接口：
```java
int rejectPrice(CmsTask task);
```

在 CmsTaskServiceImpl.java 实现：
```java
@Override
@Transactional
public int rejectPrice(CmsTask task) {
    CmsTask existingTask = cmsTaskMapper.selectCmsTaskByTaskId(task.getTaskId());
    String oldStatus = existingTask != null ? existingTask.getStatus() : null;
    
    CmsTask updateTask = new CmsTask();
    updateTask.setTaskId(task.getTaskId());
    updateTask.setStatus("0"); // 退回给会计，可重新协商
    updateTask.setRemark(task.getRemark());
    updateTask.setUpdateTime(DateUtils.getNowDate());
    
    int result = cmsTaskMapper.updateCmsTask(updateTask);
    if (result > 0) {
        recordTaskLog(task.getTaskId(), "PRICE_REJECT", oldStatus, "0", 
            "拒绝协商价格, 原因: " + task.getRemark());
        
        // 通知会计
        sendNotification(existingTask.getAssignedTo(), "协商价格已拒绝", 
            "您的协商价格已拒绝，原因：" + task.getRemark());
    }
    return result;
}

private void sendNotification(Long userId, String title, String content) {
    SysNotice notice = new SysNotice();
    notice.setNoticeTitle(title);
    notice.setNoticeType("2");
    notice.setNoticeContent(content);
    notice.setStatus("0");
    notice.setCreateBy(String.valueOf(userId));
    noticeService.insertNotice(notice);
}
```

- [ ] **Step 5: 提交**

```bash
git add ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsTaskServiceImpl.java ruoyi-admin/src/main/java/com/ruoyi/web/controller/davis/CmsTaskController.java ruoyi-system/src/main/java/com/ruoyi/system/service/ICmsTaskService.java
git commit -m "feat(task): 完善审计日志记录，支持金额变更和拒绝协商价格"
```

---

### Task 1.3: 修改生成新合同功能

**Files:**
- Modify: `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsTaskServiceImpl.java`
- Modify: `ruoyi-system/src/main/java/com/ruoyi/system/service/ICmsTaskService.java`

- [ ] **Step 1: 扩展 completeRenewal 方法**

修改 `completeRenewal` 方法签名和实现：
```java
@Override
@Transactional
public int completeRenewal(CmsTask task) {
    return completeRenewal(task, null, null, null, false);
}

@Override
@Transactional
public int completeRenewal(CmsTask task, BigDecimal newAmount, Date newStartDate, Date newEndDate, boolean generateContract) {
    CmsTask existingTask = cmsTaskMapper.selectCmsTaskByTaskId(task.getTaskId());
    String oldStatus = existingTask != null ? existingTask.getStatus() : null;
    
    CmsTask updateTask = new CmsTask();
    updateTask.setTaskId(task.getTaskId());
    updateTask.setStatus("4"); // 4已完成
    updateTask.setRemark(task.getRemark());
    updateTask.setUpdateTime(DateUtils.getNowDate());
    
    Long newContractId = null;
    
    // 如果需要生成新合同
    if (generateContract && newAmount != null) {
        CmsContract sourceContract = cmsContractService.selectCmsContractByContractId(
            existingTask.getSourceContractId() != null ? existingTask.getSourceContractId() : existingTask.getContractId());
        
        if (sourceContract != null) {
            // 复制原合同创建新合同
            CmsContract newContract = new CmsContract();
            BeanUtils.copyProperties(sourceContract, newContract, "contractId", "createTime", "createBy");
            newContract.setContractId(null);
            newContract.setParentId(sourceContract.getContractId());
            newContract.setAmount(newAmount);
            newContract.setStartDate(newStartDate);
            newContract.setEndDate(newEndDate);
            newContract.setAuditStatus("0"); // 待审批
            newContract.setStatus("0"); // 未开始
            newContract.setActualAmount(null);
            newContract.setCreateTime(DateUtils.getNowDate());
            newContract.setCreateBy(SecurityUtils.getUsername());
            
            cmsContractService.insertCmsContract(newContract);
            newContractId = newContract.getContractId();
            
            updateTask.setTargetContractId(newContractId);
        }
    }
    
    int result = cmsTaskMapper.updateCmsTask(updateTask);
    if (result > 0) {
        String logRemark = generateContract ? 
            "完成续签并生成新合同: 合同ID=" + newContractId + ", 价格=" + newAmount + ", 期限=" + newStartDate + "~" + newEndDate :
            "完成续签: " + task.getRemark();
        recordTaskLog(task.getTaskId(), "COMPLETE", oldStatus, "4", logRemark);
        
        // 更新原合同催收状态
        Long contractId = existingTask.getSourceContractId() != null ? existingTask.getSourceContractId() : existingTask.getContractId();
        if (contractId != null) {
            CmsContract contract = cmsContractService.selectCmsContractByContractId(contractId);
            if (contract != null) {
                contract.setReminderStatus("3"); // 3已完成
                cmsContractService.updateCmsContract(contract);
            }
        }
        
        // 通知经理
        if (generateContract) {
            sendNotification(existingTask.getCreateBy(), "新合同待审批", 
                "已完成催缴任务【" + existingTask.getTaskTitle() + "】，已生成待审批合同");
        }
    }
    
    return result;
}
```

- [ ] **Step 2: 修改 Controller 参数接收**

修改 CmsTaskController.java 中的 `completeRenewal` 方法：
```java
@PreAuthorize("@ss.hasPermi('cms:task:edit')")
@Log(title = "完成任务", businessType = BusinessType.UPDATE)
@PostMapping("/completeRenewal")
public AjaxResult completeRenewal(@RequestBody CmsTask task) {
    // 新参数：generateContract, newAmount, newStartDate, newEndDate
    return toAjax(cmsTaskService.completeRenewal(task, 
        task.getActualAmount(), // 复用字段传新金额
        task.getStartDate(),    // 新合同开始日期
        task.getEndDate(),      // 新合同结束日期
        "true".equals(task.getRemark()))); // 用remark字段传是否生成合同
}
```

- [ ] **Step 3: 提交**

```bash
git add ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsTaskServiceImpl.java ruoyi-admin/src/main/java/com/ruoyi/web/controller/davis/CmsTaskController.java ruoyi-system/src/main/java/com/ruoyi/system/service/ICmsTaskService.java
git commit -m "feat(task): 扩展完成任务逻辑，支持生成新合同"
```

---

### Task 1.4: 添加通知服务方法

**Files:**
- Modify: `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsTaskServiceImpl.java`

- [ ] **Step 1: 添加通知私有方法**

在 CmsTaskServiceImpl.java 中添加：
```java
/**
 * 发送站内通知
 */
private void sendNotification(Long receiverId, String title, String content) {
    if (receiverId == null) return;
    SysNotice notice = new SysNotice();
    notice.setNoticeTitle(title);
    notice.setNoticeType("2"); // 公告
    notice.setNoticeContent(content);
    notice.setStatus("0"); // 正常
    notice.setCreateBy(String.valueOf(receiverId));
    noticeService.insertNotice(notice);
}
```

- [ ] **Step 2: 在 redispatch 方法中添加通知**

修改 `redispatch` 方法，添加：
```java
// 通知会计
sendNotification(task.getAssignedTo(), "任务重新派发", 
    "您有新任务待处理：【" + existingTask.getTaskTitle() + "】，新金额：" + task.getCurrentAmount());
```

- [ ] **Step 3: 在 requestTermination 方法中添加通知**

修改 `requestTermination` 方法：
```java
// 通知经理
sendNotification(Long.parseLong(existingTask.getCreateBy()), "终止合作待审批", 
    "您有终止合作待审批：任务【" + existingTask.getTaskTitle() + "】");
```

- [ ] **Step 4: 在 confirmTermination 方法中添加通知**

在 ICmsTaskService 中添加 `confirmTermination` 返回新状态，然后在 ServiceImpl 中添加通知：
```java
// 通知会计
String notifyMsg = approved ? "终止合作已确认" : "终止合作已拒绝，原因：" + params.get("rejectReason");
sendNotification(existingTask.getAssignedTo(), approved ? "终止合作已确认" : "终止合作已拒绝", notifyMsg);
```

- [ ] **Step 5: 提交**

```bash
git add ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsTaskServiceImpl.java
git commit -m "feat(task): 添加任务相关站内通知功能"
```

---

## 阶段二：前端API

### Task 2.1: 扩展前端任务API

**Files:**
- Modify: `ruoyi-ui/src/api/system/task.js`

- [ ] **Step 1: 添加新接口**

```javascript
// 提交协商价格
export function submitPriceNegotiation(data) {
  return request({
    url: '/system/task/returnToAdmin',
    method: 'post',
    data: data
  })
}

// 同意协商价格
export function approvePriceNegotiation(data) {
  return request({
    url: '/system/task/redispatch',
    method: 'post',
    data: data
  })
}

// 拒绝协商价格
export function rejectPriceNegotiation(data) {
  return request({
    url: '/system/task/rejectPrice',
    method: 'post',
    data: data
  })
}

// 同意终止合作
export function approveTermination(params) {
  return request({
    url: '/system/task/confirmTermination',
    method: 'post',
    params: { taskId: params.taskId, approved: true }
  })
}

// 拒绝终止合作
export function rejectTermination(params) {
  return request({
    url: '/system/task/confirmTermination',
    method: 'post',
    params: { taskId: params.taskId, approved: false, rejectReason: params.rejectReason }
  })
}

// 完成任务（包含生成合同）
export function completeTaskWithContract(data) {
  return request({
    url: '/system/task/completeRenewal',
    method: 'post',
    data: data
  })
}

// 获取待审批任务列表
export function listPendingTask(query) {
  return request({
    url: '/system/task/pendingList',
    method: 'get',
    params: query
  })
}
```

- [ ] **Step 2: 提交**

```bash
git add ruoyi-ui/src/api/system/task.js
git commit -m "feat(task): 扩展前端任务API接口"
```

---

## 阶段三：前端页面

### Task 3.1: 创建待审批任务页面

**Files:**
- Create: `ruoyi-ui/src/views/system/task/pending.vue`
- Create: `ruoyi-ui/src/views/system/task/pending.vue`

- [ ] **Step 1: 创建待审批任务页面**

创建 `ruoyi-ui/src/views/system/task/pending.vue`：
```vue
<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="任务类型" prop="taskType">
        <el-select v-model="queryParams.taskType" placeholder="请选择任务类型" clearable>
          <el-option label="协商价格" value="PRICE" />
          <el-option label="终止合作" value="TERMINATE" />
        </el-select>
      </el-form-item>
      <el-form-item label="任务标题" prop="taskTitle">
        <el-input v-model="queryParams.taskTitle" placeholder="请输入任务标题" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <span style="font-weight: bold; color: #409EFF;">待审批任务列表</span>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="taskList">
      <el-table-column label="任务标题" align="center" prop="taskTitle" min-width="150" show-overflow-tooltip />
      <el-table-column label="任务类型" align="center" width="120">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.taskType === 'PRICE'" type="warning">协商价格</el-tag>
          <el-tag v-else-if="scope.row.taskType === 'TERMINATE'" type="danger">终止合作</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="原金额" align="center" prop="originalAmount" width="100">
        <template slot-scope="scope">
          {{ scope.row.originalAmount || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="新金额" align="center" prop="currentAmount" width="100">
        <template slot-scope="scope">
          <span v-if="scope.row.currentAmount" style="color: #E6A23C; font-weight: bold;">
            {{ scope.row.currentAmount }}
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="执行人" align="center" prop="assignedToName" width="100" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          {{ parseTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="handleView(scope.row)">查看</el-button>
          <el-button v-if="scope.row.taskType === 'PRICE'" size="mini" type="text" @click="handleApprovePrice(scope.row)">同意</el-button>
          <el-button v-if="scope.row.taskType === 'PRICE'" size="mini" type="text" style="color: #F56C6C;" @click="handleRejectPrice(scope.row)">拒绝</el-button>
          <el-button v-if="scope.row.taskType === 'TERMINATE'" size="mini" type="text" @click="handleApproveTerminate(scope.row)">同意终止</el-button>
          <el-button v-if="scope.row.taskType === 'TERMINATE'" size="mini" type="text" style="color: #F56C6C;" @click="handleRejectTerminate(scope.row)">拒绝终止</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 拒绝原因对话框 -->
    <el-dialog title="请输入拒绝原因" :visible.sync="rejectDialogVisible" width="500px" append-to-body>
      <el-form ref="rejectForm" :model="rejectForm" :rules="rejectRules" label-width="100px">
        <el-form-item label="拒绝原因" prop="reason">
          <el-input v-model="rejectForm.reason" type="textarea" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitReject">确 定</el-button>
        <el-button @click="cancelReject">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPendingTask, approvePriceNegotiation, rejectPriceNegotiation, approveTermination, rejectTermination } from "@/api/system/task";

export default {
  name: "PendingTask",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      taskList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        taskType: undefined,
        taskTitle: undefined,
        status: '2'
      },
      rejectDialogVisible: false,
      rejectForm: {
        taskId: undefined,
        taskType: undefined,
        reason: ''
      },
      rejectRules: {
        reason: [{ required: true, message: "请输入拒绝原因", trigger: "blur" }]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listPendingTask(this.queryParams).then(response => {
        this.taskList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleView(row) {
      this.$router.push({ path: '/system/task/detail/' + row.taskId });
    },
    handleApprovePrice(row) {
      this.$confirm('确认同意此协商价格吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        approvePriceNegotiation({ taskId: row.taskId, currentAmount: row.currentAmount }).then(() => {
          this.$modal.msgSuccess("已同意协商价格");
          this.getList();
        });
      });
    },
    handleRejectPrice(row) {
      this.rejectForm = { taskId: row.taskId, taskType: 'PRICE', reason: '' };
      this.rejectDialogVisible = true;
    },
    handleApproveTerminate(row) {
      this.$confirm('确认同意终止合作吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        approveTermination({ taskId: row.taskId }).then(() => {
          this.$modal.msgSuccess("已同意终止合作");
          this.getList();
        });
      });
    },
    handleRejectTerminate(row) {
      this.rejectForm = { taskId: row.taskId, taskType: 'TERMINATE', reason: '' };
      this.rejectDialogVisible = true;
    },
    submitReject() {
      this.$refs.rejectForm.validate(valid => {
        if (valid) {
          if (this.rejectForm.taskType === 'PRICE') {
            rejectPriceNegotiation({ taskId: this.rejectForm.taskId, remark: this.rejectForm.reason }).then(() => {
              this.$modal.msgSuccess("已拒绝协商价格");
              this.rejectDialogVisible = false;
              this.getList();
            });
          } else {
            rejectTermination({ taskId: this.rejectForm.taskId, rejectReason: this.rejectForm.reason }).then(() => {
              this.$modal.msgSuccess("已拒绝终止合作");
              this.rejectDialogVisible = false;
              this.getList();
            });
          }
        }
      });
    },
    cancelReject() {
      this.rejectDialogVisible = false;
      this.rejectForm = { taskId: undefined, taskType: undefined, reason: '' };
    }
  }
};
</script>
```

- [ ] **Step 2: 提交**

```bash
git add ruoyi-ui/src/views/system/task/pending.vue
git commit -m "feat(task): 新增待审批任务页面"
```

---

### Task 3.2: 优化创建催缴任务对话框

**Files:**
- Modify: `ruoyi-ui/src/views/system/task/index.vue`

- [ ] **Step 1: 查看现有创建任务对话框结构**

阅读 `ruoyi-ui/src/views/system/task/index.vue` 中的表单部分，找到"创建催缴任务"的对话框代码

- [ ] **Step 2: 添加会计下拉选择**

在创建任务的对话框中添加：
```vue
<el-form-item label="执行会计" prop="assignedTo">
  <el-select v-model="form.assignedTo" placeholder="请选择执行会计" filterable @focus="loadAccountants">
    <el-option
      v-for="item in accountantList"
      :key="item.userId"
      :label="item.nickName"
      :value="item.userId"
    />
  </el-select>
</el-form-item>
```

在 data 中添加：
```javascript
accountantList: [],
```

添加加载会计列表方法：
```javascript
loadAccountants() {
  if (this.accountantList.length === 0) {
    getAssignableUsers().then(response => {
      this.accountantList = response.data || [];
    });
  }
},
```

- [ ] **Step 3: 如果有合同选择，添加联动带出默认会计**

如果创建任务时需要先选择合同，添加联动逻辑：
```javascript
handleContractChange(contractId) {
  if (contractId) {
    // 调用获取合同详情接口获取ownerId
    getContract(contractId).then(response => {
      if (response.data && response.data.ownerId) {
        this.form.assignedTo = response.data.ownerId;
      }
    });
  }
}
```

- [ ] **Step 4: 提交**

```bash
git add ruoyi-ui/src/views/system/task/index.vue
git commit -m "feat(task): 优化创建催缴任务，增加会计下拉选择"
```

---

### Task 3.3: 添加协商价格和完成任务弹窗

**Files:**
- Modify: `ruoyi-ui/src/views/system/task/index.vue`

- [ ] **Step 1: 添加协商价格按钮**

在任务行操作中添加"协商价格"按钮：
```vue
<el-button size="mini" type="text" @click="handlePriceNegotiation(row)">协商价格</el-button>
```

- [ ] **Step 2: 添加协商价格对话框**

在模板中添加对话框：
```vue
<!-- 协商价格对话框 -->
<el-dialog title="提交协商价格" :visible.sync="priceDialogVisible" width="500px" append-to-body>
  <el-form ref="priceForm" :model="priceForm" label-width="100px">
    <el-form-item label="原金额">
      <span>{{ priceForm.originalAmount }}</span>
    </el-form-item>
    <el-form-item label="新金额" prop="currentAmount">
      <el-input v-model="priceForm.currentAmount" placeholder="请输入新金额" type="number" />
    </el-form-item>
    <el-form-item label="备注" prop="remark">
      <el-input v-model="priceForm.remark" type="textarea" placeholder="请输入备注" />
    </el-form-item>
  </el-form>
  <div slot="footer" class="dialog-footer">
    <el-button type="primary" @click="submitPriceNegotiation">提 交</el-button>
    <el-button @click="priceDialogVisible = false">取 消</el-button>
  </div>
</el-dialog>
```

在 data 中添加：
```javascript
priceDialogVisible: false,
priceForm: {
  taskId: undefined,
  originalAmount: undefined,
  currentAmount: undefined,
  remark: ''
},
```

添加处理方法：
```javascript
handlePriceNegotiation(row) {
  this.priceForm = {
    taskId: row.taskId,
    originalAmount: row.originalAmount,
    currentAmount: row.currentAmount || row.originalAmount,
    remark: ''
  };
  this.priceDialogVisible = true;
},
submitPriceNegotiation() {
  submitPriceNegotiation({
    taskId: this.priceForm.taskId,
    currentAmount: this.priceForm.currentAmount,
    remark: this.priceForm.remark
  }).then(() => {
    this.$modal.msgSuccess("已提交协商价格，待经理审批");
    this.priceDialogVisible = false;
    this.getList();
  });
},
```

- [ ] **Step 3: 添加完成任务弹窗（包含生成合同选项）**

添加完成任务对话框：
```vue
<!-- 完成任务对话框 -->
<el-dialog title="完成任务" :visible.sync="completeDialogVisible" width="600px" append-to-body>
  <el-form ref="completeForm" :model="completeForm" label-width="120px">
    <el-form-item label="是否生成新合同">
      <el-radio-group v-model="completeForm.generateContract">
        <el-radio :label="true">生成新合同</el-radio>
        <el-radio :label="false">不生成</el-radio>
      </el-radio-group>
    </el-form-item>
    <template v-if="completeForm.generateContract">
      <el-form-item label="新合同价格" prop="newAmount">
        <el-input v-model="completeForm.newAmount" type="number" placeholder="请输入新合同价格" />
      </el-form-item>
      <el-form-item label="新合同开始日期" prop="newStartDate">
        <el-date-picker v-model="completeForm.newStartDate" type="date" value-format="yyyy-MM-dd" placeholder="选择开始日期" />
      </el-form-item>
      <el-form-item label="新合同结束日期" prop="newEndDate">
        <el-date-picker v-model="completeForm.newEndDate" type="date" value-format="yyyy-MM-dd" placeholder="选择结束日期" />
      </el-form-item>
    </template>
    <el-form-item label="备注" prop="remark">
      <el-input v-model="completeForm.remark" type="textarea" placeholder="请输入备注" />
    </el-form-item>
  </el-form>
  <div slot="footer" class="dialog-footer">
    <el-button type="primary" @click="submitCompleteTask">确 定</el-button>
    <el-button @click="completeDialogVisible = false">取 消</el-button>
  </div>
</el-dialog>
```

在 data 中添加：
```javascript
completeDialogVisible: false,
completeForm: {
  taskId: undefined,
  generateContract: true,
  newAmount: undefined,
  newStartDate: undefined,
  newEndDate: undefined,
  remark: ''
},
```

添加处理方法：
```javascript
handleComplete(row) {
  this.completeForm = {
    taskId: row.taskId,
    generateContract: true,
    newAmount: row.currentAmount || row.originalAmount,
    newStartDate: undefined,
    newEndDate: undefined,
    remark: ''
  };
  this.completeDialogVisible = true;
},
submitCompleteTask() {
  const data = {
    taskId: this.completeForm.taskId,
    remark: this.completeForm.generateContract ? 'true' : 'false'
  };
  if (this.completeForm.generateContract) {
    data.actualAmount = this.completeForm.newAmount; // 用actualAmount传新金额
    data.startDate = this.completeForm.newStartDate;
    data.endDate = this.completeForm.newEndDate;
  }
  completeTaskWithContract(data).then(() => {
    this.$modal.msgSuccess("任务已完成");
    this.completeDialogVisible = false;
    this.getList();
  });
},
```

- [ ] **Step 4: 提交**

```bash
git add ruoyi-ui/src/views/system/task/index.vue
git commit -m "feat(task): 添加协商价格和完成任务弹窗功能"
```

---

### Task 3.4: 添加后端待审批列表接口

**Files:**
- Modify: `ruoyi-admin/src/main/java/com/ruoyi/web/controller/davis/CmsTaskController.java`

- [ ] **Step 1: 添加待审批任务列表接口**

在 CmsTaskController.java 中添加：
```java
/**
 * 获取待审批任务列表
 */
@PreAuthorize("@ss.hasPermi('system:task:pending')")
@GetMapping("/pendingList")
public TableDataInfo pendingList(CmsTask cmsTask)
{
    cmsTask.setStatus("2"); // 只查询待审批
    startPage();
    List<CmsTask> list = cmsTaskService.selectCmsTaskList(cmsTask);
    return getDataTable(list);
}
```

- [ ] **Step 2: 添加前端API调用**

在 `ruoyi-ui/src/api/system/task.js` 中 `listPendingTask` 已经添加

- [ ] **Step 3: 提交**

```bash
git add ruoyi-admin/src/main/java/com/ruoyi/web/controller/davis/CmsTaskController.java
git commit -m "feat(task): 添加待审批任务列表接口"
```

---

## 阶段四：菜单配置（手动）

### Task 4.1: 添加菜单和权限

**说明：** 以下配置需要在系统管理->菜单管理中手动添加，或者通过SQL插入

- [ ] **Step 1: 添加菜单SQL**

```sql
-- 菜单SQL（需要在系统管理-菜单管理中手动配置）
-- 父菜单：系统管理 -> 任务管理
-- 添加子菜单：待审批任务
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('待审批任务', (SELECT menu_id FROM sys_menu WHERE menu_name = '任务管理'), 2, 'pending', 'system/task/pending', 1, 'C', '0', '0', 'system:task:pending', '#', 'admin', NOW(), '', NULL, '');

-- 添加权限标识
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('任务审批', 0, 0, '', '', 1, 'F', '0', '0', 'system:task:approve', '#', 'admin', NOW(), '', NULL, '');
```

- [ ] **Step 2: 提交**

```bash
git add sql/
git commit -m "feat(task): 添加菜单SQL配置"
```

---

## 实施顺序

1. **Task 1.1** - 扩展审计日志表结构
2. **Task 1.2** - 完善审计日志记录方法
3. **Task 1.3** - 修改生成新合同功能
4. **Task 1.4** - 添加通知服务方法
5. **Task 2.1** - 扩展前端任务API
6. **Task 3.1** - 创建待审批任务页面
7. **Task 3.2** - 优化创建催缴任务对话框
8. **Task 3.3** - 添加协商价格和完成任务弹窗
9. **Task 3.4** - 添加后端待审批列表接口
10. **Task 4.1** - 菜单配置

---

## 验证清单

- [ ] 创建催缴任务时可选择会计
- [ ] 待审批任务页面正常显示待审批任务
- [ ] 协商价格：会计提交 -> 经理审批（同意/拒绝）流程正常
- [ ] 终止合作：会计提交 -> 经理审批（同意/拒绝）流程正常
- [ ] 完成任务时可选择生成新合同
- [ ] 生成新合同时填写价格和期限
- [ ] 任务操作有完整的审计日志记录
- [ ] 关键操作有站内通知
