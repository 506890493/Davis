# 达维斯管理系统 - 功能扩展实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现客户管理模块、催缴任务优化、收款确认功能

**Architecture:** 基于 RuoYi-Vue 框架，新增客户管理模块，优化首页Dashboard和任务管理功能

**Tech Stack:** Java 8, Spring Boot 2.5, MyBatis, Vue.js 2.6, Element UI

---

## 文件结构

### 后端新增文件
- `ruoyi-system/src/main/java/com/ruoyi/system/domain/CmsCustomer.java` - 客户实体
- `ruoyi-system/src/main/java/com/ruoyi/system/mapper/CmsCustomerMapper.java` - 客户Mapper
- `ruoyi-system/src/main/resources/mapper/system/CmsCustomerMapper.xml` - 客户Mapper XML
- `ruoyi-system/src/main/java/com/ruoyi/system/service/ICmsCustomerService.java` - 客户服务接口
- `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsCustomerServiceImpl.java` - 客户服务实现
- `ruoyi-admin/src/main/java/com/ruoyi/web/controller/davis/CmsCustomerController.java` - 客户Controller

### 后端修改文件
- `ruoyi-system/src/main/java/com/ruoyi/system/domain/CmsContract.java` - 合同实体增加customer_id
- `ruoyi-system/src/main/java/com/ruoyi/system/mapper/CmsContractMapper.java` - 增加统计方法
- `ruoyi-system/src/main/resources/mapper/system/CmsContractMapper.xml` - 增加SQL
- `ruoyi-system/src/main/java/com/ruoyi/system/domain/CmsTask.java` - 任务实体增加字段
- `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsTaskServiceImpl.java` - 任务服务增加确认收款
- `ruoyi-system/src/main/java/com/ruoyi/system/domain/vo/DashboardStatsVo.java` - 增加客户统计字段
- `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsDashboardServiceImpl.java` - 增加客户统计

### 前端新增文件
- `ruoyi-ui/src/api/system/customer.js` - 客户API
- `ruoyi-ui/src/views/system/customer/index.vue` - 客户列表页
- `ruoyi-ui/src/views/system/customer/add.vue` - 新增客户页
- `ruoyi-ui/src/views/system/customer/edit.vue` - 编辑客户页

### 前端修改文件
- `ruoyi-ui/src/views/index.vue` - 首页增加创建催缴任务按钮
- `ruoyi-ui/src/views/system/task/index.vue` - 任务页增加确认收款功能
- `ruoyi-ui/src/api/system/task.js` - 增加确认收款API

---

## 实现任务

### 阶段一：数据库和后端基础

#### Task 1: 创建客户表

**Files:**
- Modify: `sql/davis-backend.sql` - 新增客户表和修改合同表SQL

- [ ] **Step 1: 添加客户表SQL**

```sql
-- 客户表
CREATE TABLE IF NOT EXISTS `cms_customer` (
    `customer_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '客户ID',
    `customer_name` VARCHAR(100) NOT NULL COMMENT '客户名称',
    `customer_type` VARCHAR(10) DEFAULT NULL COMMENT '客户类型（个人/企业）',
    `contact_person` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `contact_email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `owner_id` BIGINT(20) DEFAULT NULL COMMENT '归属销售',
    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- 修改合同表，增加customer_id和actual_amount字段
ALTER TABLE `cms_contract` ADD COLUMN `customer_id` BIGINT(20) COMMENT '关联客户ID' AFTER `parent_id`;
ALTER TABLE `cms_contract` ADD COLUMN `actual_amount` DECIMAL(12,2) COMMENT '实际收款金额' AFTER `amount`;

-- 修改任务表，增加字段
ALTER TABLE `cms_task` ADD COLUMN `task_type` VARCHAR(20) DEFAULT '催缴' COMMENT '任务类型' AFTER `task_status`;
ALTER TABLE `cms_task` ADD COLUMN `original_amount` DECIMAL(12,2) COMMENT '原合同金额' AFTER `current_amount`;
ALTER TABLE `cms_task` ADD COLUMN `actual_amount` DECIMAL(12,2) COMMENT '实际收款金额' AFTER `original_amount`;
ALTER TABLE `cms_task` ADD COLUMN `receive_remark` VARCHAR(500) COMMENT '收款备注' AFTER `actual_amount`;
```

- [ ] **Step 2: 执行SQL**

```bash
# 使用davis-docker_execute_sql工具执行上述SQL
```

- [ ] **Step 3: Commit**

```bash
git add sql/davis-backend.sql
git commit -m "feat: add customer table and modify contract/task tables"
```

---

#### Task 2: 创建客户实体类

**Files:**
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/domain/CmsCustomer.java`

- [ ] **Step 1: 创建CmsCustomer.java**

```java
package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * 客户表 cms_customer
 * 
 * @author ruoyi
 */
public class CmsCustomer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 客户ID */
    private Long customerId;

    /** 客户名称 */
    private String customerName;

    /** 客户类型（个人/企业） */
    private String customerType;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String contactPhone;

    /** 邮箱 */
    private String contactEmail;

    /** 地址 */
    private String address;

    /** 备注 */
    private String remark;

    /** 归属销售 */
    private Long ownerId;

    /** 归属销售名称 */
    private String ownerName;

    /** 删除标志 */
    private String delFlag;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date updateTime;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "CmsCustomer{" +
                "customerId=" + customerId +
                ", customerName='" + customerName + '\'' +
                ", customerType='" + customerType + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                ", address='" + address + '\'' +
                ", remark='" + remark + '\'' +
                ", ownerId=" + ownerId +
                '}';
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add ruoyi-system/src/main/java/com/ruoyi/system/domain/CmsCustomer.java
git commit -m "feat: add CmsCustomer entity class"
```

---

#### Task 3: 创建客户Mapper接口和XML

**Files:**
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/mapper/CmsCustomerMapper.java`
- Create: `ruoyi-system/src/main/resources/mapper/system/CmsCustomerMapper.xml`

- [ ] **Step 1: 创建CmsCustomerMapper.java**

```java
package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.CmsCustomer;
import org.apache.ibatis.annotations.Param;

/**
 * 客户管理Mapper接口
 * 
 * @author ruoyi
 */
public interface CmsCustomerMapper 
{
    /**
     * 查询客户管理
     * 
     * @param customerId 客户管理主键
     * @return 客户管理
     */
    public CmsCustomer selectCmsCustomerById(Long customerId);

    /**
     * 查询客户管理列表
     * 
     * @param cmsCustomer 客户管理
     * @return 客户管理集合
     */
    public List<CmsCustomer> selectCmsCustomerList(CmsCustomer cmsCustomer);

    /**
     * 新增客户管理
     * 
     * @param cmsCustomer 客户管理
     * @return 结果
     */
    public int insertCmsCustomer(CmsCustomer cmsCustomer);

    /**
     * 修改客户管理
     * 
     * @param cmsCustomer 客户管理
     * @return 结果
     */
    public int updateCmsCustomer(CmsCustomer cmsCustomer);

    /**
     * 删除客户管理
     * 
     * @param customerId 客户管理主键
     * @return 结果
     */
    public int deleteCmsCustomerById(Long customerId);

    /**
     * 批量删除客户管理
     * 
     * @param customerIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCmsCustomerByIds(Long[] customerIds);

    /**
     * 统计客户总数
     * 
     * @return 客户总数
     */
    public Long countCustomer();

    /**
     * 统计销售归属的客户数
     * 
     * @param ownerId 销售ID
     * @return 客户数
     */
    public Long countCustomerByOwner(@Param("ownerId") Long ownerId);
}
```

- [ ] **Step 2: 创建CmsCustomerMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
"http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.ruoyi.system.mapper.CmsCustomerMapper">
    
    <resultMap type="CmsCustomer" id="CmsCustomerResult">
        <result property="customerId"    column="customer_id"    />
        <result property="customerName"    column="customer_name"    />
        <result property="customerType"    column="customer_type"    />
        <result property="contactPerson"    column="contact_person"    />
        <result property="contactPhone"    column="contact_phone"    />
        <result property="contactEmail"    column="contact_email"    />
        <result property="address"    column="address"    />
        <result property="remark"    column="remark"    />
        <result property="ownerId"    column="owner_id"    />
        <result property="ownerName"  column="owner_name"  />
        <result property="delFlag"    column="del_flag"    />
        <result property="createBy"    column="create_by"    />
        <result property="createTime"    column="create_time"    />
        <result property="updateBy"    column="update_by"    />
        <result property="updateTime"    column="update_time"    />
    </resultMap>

    <sql id="selectCmsCustomerVo">
        select c.customer_id, c.customer_name, c.customer_type, c.contact_person, 
               c.contact_phone, c.contact_email, c.address, c.remark, c.owner_id, 
               u.nick_name as owner_name, c.del_flag, c.create_by, c.create_time, 
               c.update_by, c.update_time
        from cms_customer c
        left join sys_user u on c.owner_id = u.user_id
    </sql>

    <select id="selectCmsCustomerList" parameterType="CmsCustomer" resultMap="CmsCustomerResult">
        <include refid="selectCmsCustomerVo"/>
        <where>  
            <if test="customerName != null and customerName != ''"> and c.customer_name like concat('%', #{customerName}, '%')</if>
            <if test="customerType != null and customerType != ''"> and c.customer_type = #{customerType}</if>
            <if test="contactPerson != null and contactPerson != ''"> and c.contact_person like concat('%', #{contactPerson}, '%')</if>
            <if test="contactPhone != null and contactPhone != ''"> and c.contact_phone = #{contactPhone}</if>
            <if name="ownerId != null"> and c.owner_id = #{ownerId}</if>
            and c.del_flag = '0'
        </where>
    </select>

    <select id="selectCmsCustomerById" parameterType="Long" resultMap="CmsCustomerResult">
        <include refid="selectCmsCustomerVo"/>
        where c.customer_id = #{customerId}
    </select>

    <insert id="insertCmsCustomer" parameterType="CmsCustomer" useGeneratedKeys="true" keyProperty="customerId">
        insert into cms_customer
        <trim prefix="(" suffix=")" suffixOverrides=",">
            <if test="customerName != null and customerName != ''">customer_name,</if>
            <if test="customerType != null">customer_type,</if>
            <if test="contactPerson != null">contact_person,</if>
            <if test="contactPhone != null">contact_phone,</if>
            <if test="contactEmail != null">contact_email,</if>
            <if test="address != null">address,</if>
            <if test="remark != null">remark,</if>
            <if test="ownerId != null">owner_id,</if>
            <if test="createBy != null">create_by,</if>
            <if test="createTime != null">create_time,</if>
         </trim>
        <trim prefix="values (" suffix=")" suffixOverrides=",">
            <if test="customerName != null and customerName != ''">#{customerName},</if>
            <if test="customerType != null">#{customerType},</if>
            <if test="contactPerson != null">#{contactPerson},</if>
            <if test="contactPhone != null">#{contactPhone},</if>
            <if test="contactEmail != null">#{contactEmail},</if>
            <if test="address != null">#{address},</if>
            <if test="remark != null">#{remark},</if>
            <if test="ownerId != null">#{ownerId},</if>
            <if test="createBy != null">#{createBy},</if>
            <if test="createTime != null">#{createTime},</if>
         </trim>
    </insert>

    <update id="updateCmsCustomer" parameterType="CmsCustomer">
        update cms_customer
        <trim prefix="SET" suffixOverrides=",">
            <if test="customerName != null and customerName != ''">customer_name = #{customerName},</if>
            <if test="customerType != null">customer_type = #{customerType},</if>
            <if test="contactPerson != null">contact_person = #{contactPerson},</if>
            <if test="contactPhone != null">contact_phone = #{contactPhone},</if>
            <if test="contactEmail != null">contact_email = #{contactEmail},</if>
            <if test="address != null">address = #{address},</if>
            <if test="remark != null">remark = #{remark},</if>
            <if test="ownerId != null">owner_id = #{ownerId},</if>
            <if test="updateBy != null">update_by = #{updateBy},</if>
            <if test="updateTime != null">update_time = #{updateTime},</if>
        </trim>
        where customer_id = #{customerId}
    </update>

    <delete id="deleteCmsCustomerById" parameterType="Long">
        update cms_customer set del_flag = '1' where customer_id = #{customerId}
    </delete>

    <delete id="deleteCmsCustomerByIds" parameterType="Long">
        update cms_customer set del_flag = '1' where customer_id in 
        <foreach item="customerId" collection="array" open="(" separator="," close=")">
            #{customerId}
        </foreach>
    </delete>

    <select id="countCustomer" resultType="Long">
        select count(*) from cms_customer where del_flag = '0'
    </select>

    <select id="countCustomerByOwner" resultType="Long">
        select count(*) from cms_customer where del_flag = '0' and owner_id = #{ownerId}
    </select>

</mapper>
```

- [ ] **Step 3: Commit**

```bash
git add ruoyi-system/src/main/java/com/ruoyi/system/mapper/CmsCustomerMapper.java
git add ruoyi-system/src/main/resources/mapper/system/CmsCustomerMapper.xml
git commit -m "feat: add CmsCustomerMapper interface and XML"
```

---

#### Task 4: 创建客户服务层

**Files:**
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/service/ICmsCustomerService.java`
- Create: `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsCustomerServiceImpl.java`

- [ ] **Step 1: 创建ICmsCustomerService.java**

```java
package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.CmsCustomer;

/**
 * 客户管理Service接口
 * 
 * @author ruoyi
 */
public interface ICmsCustomerService
{
    /**
     * 查询客户管理
     * 
     * @param customerId 客户管理主键
     * @return 客户管理
     */
    public CmsCustomer selectCmsCustomerById(Long customerId);

    /**
     * 查询客户管理列表
     * 
     * @param cmsCustomer 客户管理
     * @return 客户管理集合
     */
    public List<CmsCustomer> selectCmsCustomerList(CmsCustomer cmsCustomer);

    /**
     * 新增客户管理
     * 
     * @param cmsCustomer 客户管理
     * @return 结果
     */
    public int insertCmsCustomer(CmsCustomer cmsCustomer);

    /**
     * 修改客户管理
     * 
     * @param cmsCustomer 客户管理
     * @return 结果
     */
    public int updateCmsCustomer(CmsCustomer cmsCustomer);

    /**
     * 删除客户管理
     * 
     * @param customerId 客户管理主键
     * @return 结果
     */
    public int deleteCmsCustomerById(Long customerId);

    /**
     * 批量删除客户管理
     * 
     * @param customerIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCmsCustomerByIds(Long[] customerIds);

    /**
     * 统计客户总数
     * 
     * @return 客户总数
     */
    public Long countCustomer();

    /**
     * 统计销售归属的客户数
     * 
     * @param ownerId 销售ID
     * @return 客户数
     */
    public Long countCustomerByOwner(Long ownerId);
}
```

- [ ] **Step 2: 创建CmsCustomerServiceImpl.java**

```java
package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.CmsCustomerMapper;
import com.ruoyi.system.service.ICmsCustomerService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.CmsCustomer;

/**
 * 客户管理Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class CmsCustomerServiceImpl implements ICmsCustomerService
{
    @Autowired
    private CmsCustomerMapper cmsCustomerMapper;

    /**
     * 查询客户管理
     * 
     * @param customerId 客户管理主键
     * @return 客户管理
     */
    @Override
    public CmsCustomer selectCmsCustomerById(Long customerId)
    {
        return cmsCustomerMapper.selectCmsCustomerById(customerId);
    }

    /**
     * 查询客户管理列表
     * 
     * @param cmsCustomer 客户管理
     * @return 客户管理
     */
    @Override
    public List<CmsCustomer> selectCmsCustomerList(CmsCustomer cmsCustomer)
    {
        return cmsCustomerMapper.selectCmsCustomerList(cmsCustomer);
    }

    /**
     * 新增客户管理
     * 
     * @param cmsCustomer 客户管理
     * @return 结果
     */
    @Override
    public int insertCmsCustomer(CmsCustomer cmsCustomer)
    {
        cmsCustomer.setCreateBy(SecurityUtils.getUsername());
        cmsCustomer.setCreateTime(new java.util.Date());
        return cmsCustomerMapper.insertCmsCustomer(cmsCustomer);
    }

    /**
     * 修改客户管理
     * 
     * @param cmsCustomer 客户管理
     * @return 结果
     */
    @Override
    public int updateCmsCustomer(CmsCustomer cmsCustomer)
    {
        cmsCustomer.setUpdateBy(SecurityUtils.getUsername());
        cmsCustomer.setUpdateTime(new java.util.Date());
        return cmsCustomerMapper.updateCmsCustomer(cmsCustomer);
    }

    /**
     * 删除客户管理
     * 
     * @param customerId 客户管理主键
     * @return 结果
     */
    @Override
    public int deleteCmsCustomerById(Long customerId)
    {
        return cmsCustomerMapper.deleteCmsCustomerById(customerId);
    }

    /**
     * 批量删除客户管理
     * 
     * @param customerIds 需要删除的数据主键集合
     * @return 结果
     */
    @Override
    public int deleteCmsCustomerByIds(Long[] customerIds)
    {
        return cmsCustomerMapper.deleteCmsCustomerByIds(customerIds);
    }

    /**
     * 统计客户总数
     */
    @Override
    public Long countCustomer()
    {
        return cmsCustomerMapper.countCustomer();
    }

    /**
     * 统计销售归属的客户数
     */
    @Override
    public Long countCustomerByOwner(Long ownerId)
    {
        return cmsCustomerMapper.countCustomerByOwner(ownerId);
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add ruoyi-system/src/main/java/com/ruoyi/system/service/ICmsCustomerService.java
git add ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsCustomerServiceImpl.java
git commit -m "feat: add CmsCustomerService interface and implementation"
```

---

#### Task 5: 创建客户Controller

**Files:**
- Create: `ruoyi-admin/src/main/java/com/ruoyi/web/controller/davis/CmsCustomerController.java`

- [ ] **Step 1: 创建CmsCustomerController.java**

```java
package com.ruoyi.web.controller.davis;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.CmsCustomer;
import com.ruoyi.system.service.ICmsCustomerService;

/**
 * 客户管理Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/customer")
public class CmsCustomerController extends BaseController
{
    @Autowired
    private ICmsCustomerService cmsCustomerService;

    /**
     * 获取客户列表
     */
    @PreAuthorize("@ss.hasPermi('system:customer:list')")
    @GetMapping("/list")
    public AjaxResult list(CmsCustomer cmsCustomer)
    {
        startPage();
        List<CmsCustomer> list = cmsCustomerService.selectCmsCustomerList(cmsCustomer);
        return getDataTable(list);
    }

    /**
     * 导出客户列表
     */
    @PreAuthorize("@ss.hasPermi('system:customer:export')")
    @Log(title = "客户管理", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(CmsCustomer cmsCustomer)
    {
        List<CmsCustomer> list = cmsCustomerService.selectCmsCustomerList(cmsCustomer);
        ExcelUtil<CmsCustomer> util = new ExcelUtil<CmsCustomer>(CmsCustomer.class);
        return util.exportExcel(list, "客户数据");
    }

    /**
     * 获取客户详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:customer:query')")
    @GetMapping("/{customerId}")
    public AjaxResult getInfo(@PathVariable("customerId") Long customerId)
    {
        return success(cmsCustomerService.selectCmsCustomerById(customerId));
    }

    /**
     * 新增客户
     */
    @PreAuthorize("@ss.hasPermi('system:customer:add')")
    @Log(title = "客户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CmsCustomer cmsCustomer)
    {
        return toAjax(cmsCustomerService.insertCmsCustomer(cmsCustomer));
    }

    /**
     * 修改客户
     */
    @PreAuthorize("@ss.hasPermi('system:customer:edit')")
    @Log(title = "客户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CmsCustomer cmsCustomer)
    {
        return toAjax(cmsCustomerService.updateCmsCustomer(cmsCustomer));
    }

    /**
     * 删除客户
     */
    @PreAuthorize("@ss.hasPermi('system:customer:remove')")
    @Log(title = "客户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{customerIds}")
    public AjaxResult remove(@PathVariable Long[] customerIds)
    {
        return toAjax(cmsCustomerService.deleteCmsCustomerByIds(customerIds));
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add ruoyi-admin/src/main/java/com/ruoyi/web/controller/davis/CmsCustomerController.java
git commit -m "feat: add CmsCustomerController for REST API"
```

---

### 阶段二：前端开发

#### Task 6: 创建客户管理前端API

**Files:**
- Create: `ruoyi-ui/src/api/system/customer.js`

- [ ] **Step 1: 创建customer.js**

```javascript
import request from '@/utils/request'

export function listCustomer(query) {
  return request({
    url: '/system/customer/list',
    method: 'get',
    params: query
  })
}

export function getCustomer(customerId) {
  return request({
    url: '/system/customer/' + customerId,
    method: 'get'
  })
}

export function addCustomer(data) {
  return request({
    url: '/system/customer',
    method: 'post',
    data: data
  })
}

export function updateCustomer(data) {
  return request({
    url: '/system/customer',
    method: 'put',
    data: data
  })
}

export function delCustomer(customerId) {
  return request({
    url: '/system/customer/' + customerId,
    method: 'delete'
  })
}

export function exportCustomer(query) {
  return request({
    url: '/system/customer/export',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
```

- [ ] **Step 2: Commit**

```bash
git add ruoyi-ui/src/api/system/customer.js
git commit -m "feat: add customer management API"
```

---

#### Task 7: 创建客户列表页面

**Files:**
- Create: `ruoyi-ui/src/views/system/customer/index.vue`

- [ ] **Step 1: 创建客户列表页面**

参考现有的 `ruoyi-ui/src/views/system/user/index.vue` 结构，创建客户管理列表页面，包含：
- 搜索栏：客户名称、联系电话、客户类型
- 按钮：新增、修改、删除、导出
- 表格：客户名称、客户类型、联系人、电话、邮箱、归属销售、操作
- 分页组件

```vue
<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true">
      <el-form-item label="客户名称" prop="customerName">
        <el-input v-model="queryParams.customerName" placeholder="请输入客户名称" clearable />
      </el-form-item>
      <el-form-item label="联系电话" prop="contactPhone">
        <el-input v-model="queryParams.contactPhone" placeholder="请输入联系电话" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 按钮栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" @click="handleAdd">新增</el-button>
      </el-col>
      <right-toolbar @queryTable="getList" />
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="customerList">
      <el-table-column label="客户ID" prop="customerId" width="80" />
      <el-table-column label="客户名称" prop="customerName" width="150" />
      <el-table-column label="客户类型" prop="customerType" width="100" />
      <el-table-column label="联系人" prop="contactPerson" width="100" />
      <el-table-column label="联系电话" prop="contactPhone" width="120" />
      <el-table-column label="邮箱" prop="contactEmail" width="180" />
      <el-table-column label="归属销售" prop="ownerName" width="100" />
      <el-table-column label="操作" width="180">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button size="mini" type="text" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination />
  </div>
</template>
```

- [ ] **Step 2: 完整实现客户列表页面（参考现有页面结构）**

- [ ] **Step 3: Commit**

```bash
git add ruoyi-ui/src/views/system/customer/index.vue
git add ruoyi-ui/src/views/system/customer/add.vue
git add ruoyi-ui/src/views/system/customer/edit.vue
git commit -m "feat: add customer management pages"
```

---

#### Task 8: 首页增加创建催缴任务功能

**Files:**
- Modify: `ruoyi-ui/src/views/index.vue`

- [ ] **Step 1: 增加创建催缴任务对话框**

在首页即将到期合同表格中，每行增加"创建催缴任务"按钮，点击后弹出对话框。

```vue
<!-- 创建催缴任务对话框 -->
<el-dialog title="创建催缴任务" :visible.sync="taskDialogVisible" width="500px">
  <el-form :model="taskForm" :rules="taskRules" ref="taskForm">
    <el-form-item label="合同名称">
      <el-input v-model="taskForm.contractName" disabled />
    </el-form-item>
    <el-form-item label="合同金额">
      <el-input v-model="taskForm.originalAmount" disabled />
    </el-form-item>
    <el-form-item label="分配给" prop="assignedTo">
      <el-select v-model="taskForm.assignedTo" placeholder="选择会计">
        <el-option label="张三" value="1" />
        <el-option label="李四" value="2" />
      </el-select>
    </el-form-item>
    <el-form-item label="截止时间" prop="deadline">
      <el-date-picker v-model="taskForm.deadline" type="date" placeholder="选择截止时间" />
    </el-form-item>
    <el-form-item label="备注" prop="remark">
      <el-input v-model="taskForm.remark" type="textarea" placeholder="请输入备注" />
    </el-form-item>
  </el-form>
  <div slot="footer">
    <el-button @click="taskDialogVisible = false">取消</el-button>
    <el-button type="primary" @click="submitTask">确定</el-button>
  </div>
</el-dialog>
```

- [ ] **Step 2: 添加对话框数据和方法**

- [ ] **Step 3: Commit**

```bash
git add ruoyi-ui/src/views/index.vue
git commit -m "feat: add create task dialog to index page"
```

---

### 阶段三：收款确认功能

#### Task 9: 任务增加确认收款功能

**Files:**
- Modify: `ruoyi-ui/src/views/system/task/index.vue`
- Modify: `ruoyi-ui/src/api/system/task.js`

- [ ] **Step 1: 增加确认收款对话框**

在任务列表中，增加"确认收款"按钮，点击后弹出对话框：
- 实际收款金额（可修改）
- 收款备注（记录折扣原因）

```vue
<!-- 确认收款对话框 -->
<el-dialog title="确认收款" :visible.sync="receiveDialogVisible" width="500px">
  <el-form :model="receiveForm" :rules="receiveRules" ref="receiveForm">
    <el-form-item label="合同名称">
      <el-input v-model="receiveForm.contractName" disabled />
    </el-form-item>
    <el-form-item label="原合同金额">
      <el-input v-model="receiveForm.originalAmount" disabled />
    </el-form-item>
    <el-form-item label="实际收款金额" prop="actualAmount">
      <el-input-number v-model="receiveForm.actualAmount" :min="0" />
    </el-form-item>
    <el-form-item label="收款备注" prop="receiveRemark">
      <el-input v-model="receiveForm.receiveRemark" type="textarea" placeholder="如有折扣或减免，请说明原因" />
    </el-form-item>
  </el-form>
  <div slot="footer">
    <el-button @click="receiveDialogVisible = false">取消</el-button>
    <el-button type="primary" @click="submitReceive">确认收款</el-button>
  </div>
</el-dialog>
```

- [ ] **Step 2: 修改任务API，增加确认收款接口**

```javascript
// 确认收款
export function confirmReceive(data) {
  return request({
    url: '/system/task/confirmReceive',
    method: 'put',
    data: data
  })
}
```

- [ ] **Step 3: Commit**

```bash
git add ruoyi-ui/src/views/system/task/index.vue
git add ruoyi-ui/src/api/system/task.js
git commit -m "feat: add confirm receive dialog to task page"
```

---

#### Task 10: 后端增加确认收款API

**Files:**
- Modify: `ruoyi-admin/src/main/java/com/ruoyi/web/controller/davis/CmsTaskController.java`
- Modify: `ruoyi-system/src/main/java/com/ruoyi/system/service/ICmsTaskService.java`
- Modify: `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsTaskServiceImpl.java`

- [ ] **Step 1: Controller增加确认收款方法**

```java
/**
 * 确认收款
 */
@PreAuthorize("@ss.hasPermi('system:task:edit')")
@Log(title = "任务管理", businessType = BusinessType.UPDATE)
@PutMapping("/confirmReceive")
public AjaxResult confirmReceive(@RequestBody CmsTask task)
{
    return toAjax(cmsTaskService.confirmReceive(task));
}
```

- [ ] **Step 2: Service接口增加方法**

```java
/**
 * 确认收款
 * 
 * @param task 任务信息
 * @return 结果
 */
public int confirmReceive(CmsTask task);
```

- [ ] **Step 3: Service实现**

```java
@Override
public int confirmReceive(CmsTask task)
{
    CmsTask dbTask = cmsTaskMapper.selectCmsTaskById(task.getTaskId());
    // 更新任务状态为已完成
    dbTask.setTaskStatus("已完成");
    dbTask.setActualAmount(task.getActualAmount());
    dbTask.setReceiveRemark(task.getReceiveRemark());
    dbTask.setUpdateBy(SecurityUtils.getUsername());
    dbTask.setUpdateTime(new Date());
    
    // 更新关联合同的实际收款金额
    CmsContract contract = contractMapper.selectCmsContractByContractId(dbTask.getContractId());
    if (contract != null) {
        contract.setActualAmount(task.getActualAmount());
        contract.setUpdateBy(SecurityUtils.getUsername());
        contract.setUpdateTime(new Date());
        contractMapper.updateCmsContract(contract);
    }
    
    return cmsTaskMapper.updateCmsTask(dbTask);
}
```

- [ ] **Step 4: Commit**

```bash
git add ruoyi-admin/src/main/java/com/ruoyi/web/controller/davis/CmsTaskController.java
git add ruoyi-system/src/main/java/com/ruoyi/system/service/ICmsTaskService.java
git add ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsTaskServiceImpl.java
git commit -m "feat: add confirmReceive API for task management"
```

---

### 阶段四：Dashboard统计优化

#### Task 11: Dashboard增加客户统计

**Files:**
- Modify: `ruoyi-system/src/main/java/com/ruoyi/system/domain/vo/DashboardStatsVo.java`
- Modify: `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsDashboardServiceImpl.java`

- [ ] **Step 1: DashboardStatsVo增加字段**

```java
// 客户统计
private Long totalCustomers;       // 客户总数
private Long myCustomerCount;       // 我的客户数（销售）
```

- [ ] **Step 2: Service实现中赋值**

```java
// 统计客户总数
Long totalCustomers = cmsCustomerMapper.countCustomer();
stats.setTotalCustomers(totalCustomers);

// 销售：统计自己的客户数
if ("sales".equals(roleType)) {
    Long myCustomers = cmsCustomerMapper.countCustomerByOwner(userId);
    stats.setMyCustomerCount(myCustomers);
}
```

- [ ] **Step 3: Commit**

```bash
git add ruoyi-system/src/main/java/com/ruoyi/system/domain/vo/DashboardStatsVo.java
git add ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsDashboardServiceImpl.java
git commit -m "feat: add customer statistics to dashboard"
```

---

### 阶段五：菜单权限配置

#### Task 12: 配置客户管理菜单

- [ ] **Step 1: 使用davis-docker执行SQL添加菜单**

```sql
-- 客户管理目录菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (2100, '客户管理', 1, 5, 'customer', 'system/customer/index', 1, 0, 'C', '0', '0', '', 'peoples', 'admin', NOW());

-- 客户列表菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (2101, '客户列表', 2100, 1, '', '', 1, 0, 'F', '0', '0', 'system:customer:list', '#', 'admin', NOW());

-- 新增客户权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (2102, '客户新增', 2100, 2, '', '', 1, 0, 'F', '0', '0', 'system:customer:add', '#', 'admin', NOW());

-- 修改客户权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (2103, '客户修改', 2100, 3, '', '', 1, 0, 'F', '0', '0', 'system:customer:edit', '#', 'admin', NOW());

-- 删除客户权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (2104, '客户删除', 2100, 4, '', '', 1, 0, 'F', '0', '0', 'system:customer:remove', '#', 'admin', NOW());

-- 导出客户权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (2105, '客户导出', 2100, 5, '', '', 1, 0, 'F', '0', '0', 'system:customer:export', '#', 'admin', NOW());

-- 查询客户权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
VALUES (2106, '客户查询', 2100, 6, '', '', 1, 0, 'F', '0', '0', 'system:customer:query', '#', 'admin', NOW());
```

- [ ] **Step 2: 分配菜单权限给所有角色**

```sql
INSERT INTO sys_role_menu (role_id, menu_id) VALUES 
(1, 2100), (1, 2101), (1, 2102), (1, 2103), (1, 2104), (1, 2105), (1, 2106),
(2, 2100), (2, 2101), (2, 2102), (2, 2103), (2, 2104), (2, 2105), (2, 2106),
(100, 2100), (100, 2101), (100, 2102), (100, 2103), (100, 2104), (100, 2105), (100, 2106),
(101, 2100), (101, 2101), (101, 2102), (101, 2103), (101, 2104), (101, 2105), (101, 2106);
```

- [ ] **Step 3: Commit**

```bash
git add sql/davis-backend.sql
git commit -m "feat: add customer management menu and permissions"
```

---

## 总结

完成以上12个任务后，将实现：
1. 客户管理模块（CRUD）
2. 合同与客户关联
3. 首页创建催缴任务功能
4. 任务确认收款功能
5. Dashboard客户统计

下一步可以扩展：
- 定时任务自动生成续费提醒
- 业绩报表
- 销售录入新客户优化
