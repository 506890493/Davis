# 审计功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为任务管理和合同审批添加完整的审计日志功能

**Architecture:** 
- 新增 cms_task_log 表记录任务操作历史
- 增强合同审批记录，添加审批快照
- 在任务状态变更时自动记录审计日志

**Tech Stack:** Java 8, Spring Boot, MyBatis, MySQL, Vue.js

---

## 文件结构

### 新增文件
- `ruoyi-system/src/main/java/com/ruoyi/system/domain/CmsTaskLog.java` - 任务日志实体
- `ruoyi-system/src/main/java/com/ruoyi/system/mapper/CmsTaskLogMapper.java` - 日志Mapper
- `ruoyi-system/src/main/resources/mapper/system/CmsTaskLogMapper.xml` - Mapper XML
- `ruoyi-system/src/main/java/com/ruoyi/system/service/ICmsTaskLogService.java` - 日志Service接口
- `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsTaskLogServiceImpl.java` - 日志Service实现

### 修改文件
- `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsTaskServiceImpl.java` - 添加审计日志记录
- `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsContractServiceImpl.java` - 增强审批记录
- `ruoyi-ui/src/views/system/task/index.vue` - 显示操作历史按钮

---

## Task 1: 创建数据库表和实体类

### Files:
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/domain/CmsTaskLog.java`

- [ ] **Step 1: 创建任务日志实体类**

```java
package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.web.domain.BaseEntity;

public class CmsTaskLog extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    private Long logId;
    private Long taskId;
    private Long operatorId;
    private String operatorName;
    private String actionType;
    private String beforeStatus;
    private String afterStatus;
    private String remark;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getBeforeStatus() { return beforeStatus; }
    public void setBeforeStatus(String beforeStatus) { this.beforeStatus = beforeStatus; }
    public String getAfterStatus() { return afterStatus; }
    public void setAfterStatus(String afterStatus) { this.afterStatus = afterStatus; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
```

- [ ] **Step 2: 执行SQL创建表**

```sql
CREATE TABLE cms_task_log (
  log_id bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  task_id bigint(20) DEFAULT NULL COMMENT '任务ID',
  operator_id bigint(20) DEFAULT NULL COMMENT '操作人ID',
  operator_name varchar(100) DEFAULT NULL COMMENT '操作人名称',
  action_type char(1) DEFAULT NULL COMMENT '操作类型: 0=创建, 1=开始, 2=完成, 3=终止, 4=分配, 5=重新分配',
  before_status char(1) DEFAULT NULL COMMENT '操作前状态',
  after_status char(1) DEFAULT NULL COMMENT '操作后状态',
  remark varchar(500) DEFAULT NULL COMMENT '操作备注',
  create_time datetime DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (log_id),
  KEY idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务操作日志表';
```

- [ ] **Step 3: Commit**

---

## Task 2: 创建Mapper层

### Files:
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/mapper/CmsTaskLogMapper.java`
- Create: `ruoyi-system/src/main/resources/mapper/system/CmsTaskLogMapper.xml`

- [ ] **Step 1: 创建Mapper接口**

```java
package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.CmsTaskLog;

public interface CmsTaskLogMapper {
    public List<CmsTaskLog> selectCmsTaskLogList(CmsTaskLog cmsTaskLog);
    public CmsTaskLog selectCmsTaskLogByLogId(Long logId);
    public int insertCmsTaskLog(CmsTaskLog cmsTaskLog);
    public int deleteCmsTaskLogByLogId(Long logId);
    public List<CmsTaskLog> selectCmsTaskLogByTaskId(Long taskId);
}
```

- [ ] **Step 2: 创建Mapper XML**

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
"http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.ruoyi.system.mapper.CmsTaskLogMapper">
    
    <resultMap type="CmsTaskLog" id="CmsTaskLogResult">
        <result property="logId" column="log_id"/>
        <result property="taskId" column="task_id"/>
        <result property="operatorId" column="operator_id"/>
        <result property="operatorName" column="operator_name"/>
        <result property="actionType" column="action_type"/>
        <result property="beforeStatus" column="before_status"/>
        <result property="afterStatus" column="after_status"/>
        <result property="remark" column="remark"/>
        <result property="createTime" column="create_time"/>
    </resultMap>
    
    <sql id="selectCmsTaskLogVo">
        select log_id, task_id, operator_id, operator_name, action_type, before_status, after_status, remark, create_time
        from cms_task_log
    </sql>
    
    <select id="selectCmsTaskLogList" parameterType="CmsTaskLog" resultMap="CmsTaskLogResult">
        <include refid="selectCmsTaskLogVo"/>
        <where>
            <if test="taskId != null">and task_id = #{taskId}</if>
            <if test="operatorId != null">and operator_id = #{operatorId}</if>
            <if test="actionType != null and actionType != ''">and action_type = #{actionType}</if>
        </where>
        order by create_time desc
    </select>
    
    <select id="selectCmsTaskLogByLogId" parameterType="Long" resultMap="CmsTaskLogResult">
        <include refid="selectCmsTaskLogVo"/>
        where log_id = #{logId}
    </select>
    
    <select id="selectCmsTaskLogByTaskId" parameterType="Long" resultMap="CmsTaskLogResult">
        <include refid="selectCmsTaskLogVo"/>
        where task_id = #{taskId}
        order by create_time desc
    </select>
    
    <insert id="insertCmsTaskLog" parameterType="CmsTaskLog" useGeneratedKeys="true" keyProperty="logId">
        insert into cms_task_log
        <trim prefix="(" suffix=")" suffixOverrides=",">
            <if test="taskId != null">task_id,</if>
            <if test="operatorId != null">operator_id,</if>
            <if test="operatorName != null">operator_name,</if>
            <if test="actionType != null">action_type,</if>
            <if test="beforeStatus != null">before_status,</if>
            <if test="afterStatus != null">after_status,</if>
            <if test="remark != null">remark,</if>
            create_time,
        </trim>
        <trim prefix="values (" suffix=")" suffixOverrides=",">
            <if test="taskId != null">#{taskId},</if>
            <if test="operatorId != null">#{operatorId},</if>
            <if test="operatorName != null">#{operatorName},</if>
            <if test="actionType != null">#{actionType},</if>
            <if test="beforeStatus != null">#{beforeStatus},</if>
            <if test="afterStatus != null">#{afterStatus},</if>
            <if test="remark != null">#{remark},</if>
            now(),
        </trim>
    </insert>
    
    <delete id="deleteCmsTaskLogByLogId" parameterType="Long">
        delete from cms_task_log where log_id = #{logId}
    </delete>
</mapper>
```

- [ ] **Step 3: Commit**

---

## Task 3: 创建Service层

### Files:
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/service/ICmsTaskLogService.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsTaskLogServiceImpl.java`

- [ ] **Step 1: 创建Service接口**

```java
package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.CmsTaskLog;

public interface ICmsTaskLogService {
    public List<CmsTaskLog> selectCmsTaskLogList(CmsTaskLog cmsTaskLog);
    public CmsTaskLog selectCmsTaskLogByLogId(Long logId);
    public int insertCmsTaskLog(CmsTaskLog cmsTaskLog);
    public int deleteCmsTaskLogByLogId(Long logId);
    public List<CmsTaskLog> selectCmsTaskLogByTaskId(Long taskId);
}
```

- [ ] **Step 2: 创建Service实现**

```java
package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.CmsTaskLogMapper;
import com.ruoyi.system.domain.CmsTaskLog;
import com.ruoyi.system.service.ICmsTaskLogService;

@Service
public class CmsTaskLogServiceImpl implements ICmsTaskLogService {
    
    @Autowired
    private CmsTaskLogMapper cmsTaskLogMapper;
    
    @Override
    public List<CmsTaskLog> selectCmsTaskLogList(CmsTaskLog cmsTaskLog) {
        return cmsTaskLogMapper.selectCmsTaskLogList(cmsTaskLog);
    }
    
    @Override
    public CmsTaskLog selectCmsTaskLogByLogId(Long logId) {
        return cmsTaskLogMapper.selectCmsTaskLogByLogId(logId);
    }
    
    @Override
    public int insertCmsTaskLog(CmsTaskLog cmsTaskLog) {
        return cmsTaskLogMapper.insertCmsTaskLog(cmsTaskLog);
    }
    
    @Override
    public int deleteCmsTaskLogByLogId(Long logId) {
        return cmsTaskLogMapper.deleteCmsTaskLogByLogId(logId);
    }
    
    @Override
    public List<CmsTaskLog> selectCmsTaskLogByTaskId(Long taskId) {
        return cmsTaskLogMapper.selectCmsTaskLogByTaskId(taskId);
    }
}
```

- [ ] **Step 3: Commit**

---

## Task 4: 修改CmsTaskServiceImpl添加审计日志记录

### Files:
- Modify: `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsTaskServiceImpl.java`

- [ ] **Step 1: 添加依赖和注入**

在文件顶部添加 import:
```java
import com.ruoyi.system.domain.CmsTaskLog;
import com.ruoyi.system.service.ICmsTaskLogService;
```

在类的成员变量区域添加:
```java
@Autowired
private ICmsTaskLogService cmsTaskLogService;
```

- [ ] **Step 2: 添加记录日志方法**

在 CmsTaskServiceImpl 类中添加:
```java
private void recordTaskLog(Long taskId, String actionType, String beforeStatus, String afterStatus, String remark) {
    CmsTaskLog log = new CmsTaskLog();
    log.setTaskId(taskId);
    log.setOperatorId(SecurityUtils.getUserId());
    log.setOperatorName(SecurityUtils.getUsername());
    log.setActionType(actionType);
    log.setBeforeStatus(beforeStatus);
    log.setAfterStatus(afterStatus);
    log.setRemark(remark);
    cmsTaskLogService.insertCmsTaskLog(log);
}
```

- [ ] **Step 3: 在任务创建时记录日志**

在 insertCmsTask 方法中，创建任务后添加:
```java
recordTaskLog(cmsTask.getTaskId(), "0", null, cmsTask.getStatus(), "创建任务");
```

- [ ] **Step 4: 在任务状态变更时记录日志**

找到 updateCmsTask 方法，在状态变更处添加记录:
- 开始处理时: recordTaskLog(taskId, "1", "0", "1", "开始处理任务")
- 完成时: recordTaskLog(taskId, "2", "1", "2", "完成任务")
- 终止时: recordTaskLog(taskId, "3", status, "3", "终止任务")
- 重新派发时: recordTaskLog(taskId, "5", null, status, "重新派发给: " + assignedToName)

- [ ] **Step 5: Commit**

---

## Task 5: 增强合同审批记录

### Files:
- Modify: `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsContractServiceImpl.java`

- [ ] **Step 1: 修改auditContract方法**

在 auditContract 方法中，保存审批记录时添加合同快照:
```java
// 获取合同完整信息作为快照
String contentSnapshot = JSON.toJSONString(fullContract);
approval.setContentSnapshot(contentSnapshot);
```

- [ ] **Step 2: Commit**

---

## Task 6: 前端显示任务操作历史

### Files:
- Modify: `ruoyi-ui/src/views/system/task/index.vue`

- [ ] **Step 1: 添加查看历史按钮**

在操作列中添加按钮:
```vue
<el-button
  size="mini"
  type="text"
  icon="el-icon-time"
  @click="handleViewHistory(scope.row)"
  v-hasPermi="['system:task:query']"
>操作历史</el-button>
```

- [ ] **Step 2: 添加查看历史的对话框和逻辑**

在 data 中添加:
```javascript
historyOpen: false,
historyList: []
```

添加方法:
```javascript
handleViewHistory(row) {
  historyTaskLog({ taskId: row.taskId }).then(res => {
    this.historyList = res.data || [];
    this.historyOpen = true;
  });
}
```

- [ ] **Step 3: 添加对话框组件**

在 template 中添加对话框:
```vue
<el-dialog title="操作历史" :visible.sync="historyOpen" width="700px">
  <el-table :data="historyList">
    <el-table-column label="操作时间" prop="createTime" width="160"/>
    <el-table-column label="操作人" prop="operatorName" width="100"/>
    <el-table-column label="操作类型" prop="actionType" width="100">
      <template slot-scope="scope">
        <span v-if="scope.row.actionType === '0'">创建</span>
        <span v-else-if="scope.row.actionType === '1'">开始</span>
        <span v-else-if="scope.row.actionType === '2'">完成</span>
        <span v-else-if="scope.row.actionType === '3'">终止</span>
        <span v-else-if="scope.row.actionType === '4'">分配</span>
        <span v-else-if="scope.row.actionType === '5'">重新分配</span>
      </template>
    </el-table-column>
    <el-table-column label="状态变更" width="120">
      <template slot-scope="scope">
        {{ scope.row.beforeStatus }} → {{ scope.row.afterStatus }}
      </template>
    </el-table-column>
    <el-table-column label="备注" prop="remark"/>
  </el-table>
</el-dialog>
```

- [ ] **Step 4: 添加API调用**

在 api 文件中添加:
```javascript
export function historyTaskLog(query) {
  return request({
    url: '/system/task/log',
    method: 'get',
    params: query
  })
}
```

- [ ] **Step 5: 添加Controller接口**

在 CmsTaskController 中添加:
```java
@GetMapping("/log")
public TableDataInfo logList(CmsTaskLog cmsTaskLog) {
    startPage();
    List<CmsTaskLog> list = cmsTaskLogService.selectCmsTaskLogList(cmsTaskLog);
    return getDataTable(list);
}
```

- [ ] **Step 6: Commit**

---

## Task 7: 构建和测试

- [ ] **Step 1: 编译后端**

```bash
mvn clean package -Dmaven.test.skip=true -pl ruoyi-admin -am
```

- [ ] **Step 2: 启动应用测试**

---

## 执行总结

完成以上任务后，系统将具备:
1. 任务操作完整审计日志
2. 合同审批快照记录
3. 前端可查看任务操作历史
