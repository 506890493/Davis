# 合同与客户关联管理 - 技术设计

## 系统架构

沿用现有 RuoYi-Vue 三层架构：
- **Controller 层**：处理 HTTP 请求
- **Service 层**：业务逻辑处理
- **Mapper 层**：数据访问

## 数据模型

### 现有字段（已存在）
- `CmsContract.customerId`：Long 类型，关联 cms_customer 表的主键
- `CmsCustomer.customerId`：客户主键

无需新增数据表或字段。

## 功能模块设计

### 1. 合同端 - 客户选择器

**前端页面**：`ruoyi-ui/src/views/system/contract/edit.vue`

在合同编辑表单的公司名称（contractName）下方增加客户选择器：
- 组件：使用 `el-select` 配合 `el-option`
- 数据源：调用客户列表 API `/system/customer/list` 获取客户下拉选项
- 字段绑定：form.customerId → CmsContract.customerId
- 显示逻辑：选中客户后显示客户名称，提交时提交 customerId

**交互流程**：
1. 页面加载时获取客户列表（分页，pageSize=1000 获取全部）
2. 用户从下拉列表选择已有客户
3. 表单提交时 customerId 随其他字段一起提交

### 2. 客户端 - 合同展开展示

**前端页面**：`ruoyi-ui/src/views/system/customer/index.vue`

在客户列表增加行内展开功能：

#### 2.1 表格配置
- 在客户表格中增加 `expand` 列
- 使用 Element UI 的 `el-table` 展开行功能

#### 2.2 展开行内容
展开区域内显示两个区块：
- **代账合同列表**：contractType="0" 的合同
- **地址合同列表**：contractType="1" 的合同

每个列表展示字段：
- 合同编号（contractCode）
- 公司名称（contractName）
- 合同状态（status 动态计算）
- 合同金额（amount）
- 合同期限（startDate ~ endDate）

#### 2.3 数据获取
- 方式一：页面加载时获取所有客户的合同数据（按 customerId 分组）
- 方式二：点击展开时动态加载该客户的合同列表（推荐，减少初始请求量）

推荐实现：**动态加载方式**
- API：`/system/contract/list` 增加可选参数 `customerId`
- 后端 Service 层已有 selectCmsContractList 方法支持 customerId 查询

### 3. 后端接口（如需增强）

现有接口已满足需求，无需新增：
- `GET /system/customer/list` - 客户列表（用于下拉选择）
- `GET /system/contract/list` - 合同列表（支持 customerId 参数）
- `GET /system/contract/{id}` - 合同详情（含 customerId）
- `PUT /system/contract` - 更新合同（含 customerId）

## 接口设计

### 客户下拉数据获取
```javascript
// GET /system/customer/list?pageNum=1&pageSize=1000
// 返回客户列表，用于下拉选择
```

### 客户合同查询
```javascript
// GET /system/contract/list?customerId=123
// 返回该客户的所有合同
```

## 数据流

```
[客户列表页面]
    ↓ 点击展开
[调用 contract/list API，传入 customerId]
    ↓
[后端 Service 查询该客户的合同]
    ↓ 返回合同列表
[前端渲染代账合同和地址合同两个区块]
```

## 异常处理

- 客户被删除时，合同仍保留（历史数据），前端显示"已删除客户"或显示原 customerId
- 客户下拉列表获取失败时，显示降级提示，不阻塞表单提交

## 界面设计示意

### 合同编辑页面
```
合同类型: (●) 代账  ( ) 地址租赁
合同编号: [___________]
公司名称: [___________]
关联客户: [请选择客户 ▼]  ← 新增
```

### 客户列表页面
```
| 客户名称 | 客户类型 | 联系电话 | 操作   |
| 张三     | 企业     | 138xxxx  | 修改   | → 点击展开
                                                    ├─ 代账合同:
                                                    │  合同编号: HT001 | 公司: A公司 | 状态: 进行中 | 金额: 5000元
                                                    └─ 地址合同:
                                                       合同编号: HT002 | 公司: A公司 | 状态: 即将到期 | 金额: 20000元
```

## 技术要点

1. **客户数据量**：如客户数量大（>1000），考虑增加搜索过滤功能
2. **合同状态**：前端使用字典 `cms_contract_status` 转换显示
3. **数据权限**：合同列表查询需保持现有数据权限控制