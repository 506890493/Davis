# 合同与客户关联管理 - 实现任务

## 前端实现

### 任务 1：合同编辑页面增加客户选择器

**文件**: `ruoyi-ui/src/views/system/contract/edit.vue`

**步骤**:
1. 在 data() 的 form 对象中增加 `customerId: null` 字段
2. 在 computed 中增加 `customerList` 用于存储客户下拉数据
3. 在 created() 或 beforeMount 中调用客户列表 API 获取客户数据
4. 在表单模板中公司名称（contractName）后增加客户选择器：
   ```html
   <el-form-item label="关联客户" prop="customerId">
     <el-select v-model="form.customerId" placeholder="请选择客户" clearable filterable>
       <el-option
         v-for="customer in customerList"
         :key="customer.customerId"
         :label="customer.customerName"
         :value="customer.customerId"
       />
     </el-select>
   </el-form-item>
   ```
5. 确保 submitForm() 提交时包含 customerId 字段

**验证**:
- 打开合同编辑页面，下拉选择客户，保存后刷新页面，客户应正确回显

---

### 任务 2：客户列表页面增加合同展开展示

**文件**: `ruoyi-ui/src/views/system/customer/index.vue`

**步骤**:

1. **导入 API**
   ```javascript
   import { listContract } from "@/api/system/contract";
   ```

2. **data() 扩展**
   ```javascript
   // 新增字段
   expandedRowKeys: [],       // 展开行的客户ID数组
   contractMap: {},           // customerId -> 合同列表映射
   ```

3. **表格添加展开列**
   ```html
   <el-table :data="customerList" @expand-change="handleExpandChange">
     <el-table-column type="expand">
       <template slot-scope="scope">
         <!-- 合同展示区域 -->
       </template>
     </el-table-column>
     <!-- 原有列... -->
   </el-table>
   ```

4. **展开行内容模板**
   ```html
   <template slot-scope="scope">
     <div v-if="contractMap[scope.row.customerId]">
       <!-- 代账合同 -->
       <div class="contract-section">
         <h4>代账合同</h4>
         <el-table :data="getContractsByType(scope.row.customerId, '0')">
           <el-table-column prop="contractCode" label="合同编号" />
           <el-table-column prop="contractName" label="公司名称" />
           <el-table-column prop="status" label="状态">
             <template slot-scope="s">
               <dict-tag :type="dict.type.cms_contract_status" :value="s.row.status" />
             </template>
           </el-table-column>
           <el-table-column prop="amount" label="金额" />
           <el-table-column label="期限">
             <template slot-scope="s">
               {{ s.row.startDate }} ~ {{ s.row.endDate }}
             </template>
           </el-table-column>
         </el-table>
       </div>
       <!-- 地址合同 -->
       <div class="contract-section">
         <h4>地址合同</h4>
         <el-table :data="getContractsByType(scope.row.customerId, '1')">
           <!-- 同上表格结构 -->
         </el-table>
       </div>
     </div>
     <div v-else class="empty-tip">暂无关联合同</div>
   </template>
   ```

5. **实现展开事件处理**
   ```javascript
   handleExpandChange(row, expanded) {
     if (expanded && !this.contractMap[row.customerId]) {
       // 加载该客户的合同
       listContract({ customerId: row.customerId }).then(res => {
         this.$set(this.contractMap, row.customerId, res.rows || []);
       });
     }
   }
   ```

6. **辅助方法**
   ```javascript
   getContractsByType(customerId, type) {
     const list = this.contractMap[customerId] || [];
     return list.filter(c => c.contractType === type);
   }
   ```

7. **样式调整**
   - 展开行内容添加适当的 padding 和分组标题样式
   - 区分代账合同和地址合同的视觉展示

**验证**:
- 客户列表点击行展开，显示该客户的代账合同和地址合同
- 关闭展开后再展开，数据保留（从缓存读取）

---

## 后端（如需确认）

### 任务 3：确认合同查询支持 customerId 参数

**文件**: `ruoyi-system/src/main/java/com/ruoyi/system/mapper/CmsContractMapper.xml`

检查 `selectCmsContractList` 方法的 SQL 是否已包含 customerId 查询条件：
```xml
<if test="customerId != null">
  AND customer_id = #{customerId}
</if>
```

如果不存在，需在 SQL 中添加此条件。

---

## 验收标准

1. ✅ 合同编辑页面可选择并保存关联客户
2. ✅ 合同编辑后重新打开，客户正确回显
3. ✅ 客户列表可展开显示代账合同和地址合同
4. ✅ 未关联客户的合同不显示在展开区域内
5. ✅ 页面功能符合现有代码风格和技术规范