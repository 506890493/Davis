# 批量上传合同非法字符校验实现计划

## 背景
当前批量上传合同功能（`CmsContractServiceImpl.importCmsContract`）只校验了合同编码和名称是否为空，缺少对非法字符、SQL 注入、XSS 攻击等安全威胁的防护。为保障数据安全，需要添加全面的输入校验逻辑。

## 根因分析
1. **现有校验不足**：`CmsContractServiceImpl.java` 第264-309行只检查了空值
2. **安全风险**：用户可以上传包含恶意字符的 Excel 文件，可能导致：
   - SQL 注入攻击
   - XSS 跨站脚本攻击
   - 数据库存储异常字符影响展示
   - 控制字符破坏数据格式

## 实现方案

### 1. 创建校验工具类
**文件**：`ruoyi-common/src/main/java/com/ruoyi/common/utils/SecurityValidationUtil.java`

**功能**：
- `containsSqlInjection(String input)` - 检测 SQL 注入字符
- `containsXss(String input)` - 检测 XSS 脚本
- `containsControlCharacters(String input)` - 检测控制字符
- `validateContractData(CmsContract contract)` - 统一校验入口

**检测规则**：
```java
// SQL 注入关键字
Pattern SQL_PATTERN = Pattern.compile(
    ".*(;|--|/\\*|\\*/|xp_|sp_|exec|execute|drop|create|alter|insert|update|delete|truncate|declare|cast|convert).*",
    Pattern.CASE_INSENSITIVE
);

// XSS 脚本标签
Pattern XSS_PATTERN = Pattern.compile(
    ".*(<script|</script|<iframe|</iframe|javascript:|onerror=|onload=|onclick=|eval\\(|expression\\().*",
    Pattern.CASE_INSENSITIVE
);

// 控制字符（排除常规换行符）
Pattern CONTROL_CHAR_PATTERN = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]");
```

### 2. 修改 Service 层
**文件**：`ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsContractServiceImpl.java`

**修改位置**：`importCmsContract` 方法（第264-309行）

**变更内容**：
1. 在 for 循环中，字段非空校验之后添加安全校验
2. 调用 `SecurityValidationUtil.validateContractData(contract)`
3. 捕获校验异常，记录到 failureMsg 中并增加 failureNum

**伪代码**：
```java
for (CmsContract contract : contractList) {
    try {
        // 现有的非空校验
        if (StringUtils.isEmpty(contract.getContractCode()) || ...) {
            failureNum++;
            failureMsg.append("第" + index + "行: 合同编码或名称为空; ");
            continue;
        }
        
        // 新增：安全字符校验
        SecurityValidationUtil.validateContractData(contract);
        
        // 现有的业务逻辑（插入/更新）
        ...
    } catch (ServiceException e) {
        failureNum++;
        failureMsg.append("第" + index + "行: " + e.getMessage() + "; ");
    }
}
```

### 3. 优化错误提示
**改进点**：
- 当前错误信息只说"导入失败 X 条"，不知道具体哪一行
- **改进**：在循环中记录行号（从1开始），错误信息格式：`"第3行: 合同名称包含非法SQL字符; "`

### 4. 添加单元测试
**文件**：`ruoyi-system/src/test/java/com/ruoyi/system/service/CmsContractServiceTest.java`（新建）

**测试用例**：
- `testImportContract_Success_WithValidData()` - 正常数据导入成功
- `testImportContract_Fail_WithSqlInjection()` - SQL 注入字符被拦截
- `testImportContract_Fail_WithXssInjection()` - XSS 脚本被拦截
- `testImportContract_Fail_WithControlCharacters()` - 控制字符被拦截
- `testImportContract_PartialSuccess_WithMixedData()` - 部分成功部分失败

**使用 Mockito Mock**：
- Mock `cmsContractMapper`
- Mock `customerMapper`（如果需要）

## 影响范围

### 修改的文件
1. ✅ 新建：`ruoyi-common/.../SecurityValidationUtil.java`
2. ✅ 修改：`ruoyi-system/.../CmsContractServiceImpl.java`
3. ✅ 新建：`ruoyi-system/src/test/.../CmsContractServiceTest.java`

### 不影响
- 前端代码无需修改
- Controller 层无需修改
- Mapper 层无需修改
- 数据库表结构无需修改
- 现有正常数据导入流程不受影响

### 兼容性
- ✅ 向后兼容：只是增强了校验，不改变成功场景的行为
- ✅ 性能影响小：正则匹配在合理范围内
- ⚠️ 可能影响：如果历史数据中有非法字符，重新导入会失败（但这是期望行为）

## 验证方法

### 1. 单元测试验证
```bash
cd ruoyi-system
mvn test -Dtest=CmsContractServiceTest
```
预期：所有测试用例通过

### 2. 手动测试验证
准备测试 Excel 文件：
- `valid.xlsx` - 正常数据，预期成功
- `sql_injection.xlsx` - 包含 `'; DROP TABLE;`，预期失败并提示"包含非法SQL字符"
- `xss.xlsx` - 包含 `<script>alert('xss')</script>`，预期失败并提示"包含XSS脚本"
- `control_char.xlsx` - 包含 `\x00` 等控制字符，预期失败

步骤：
1. 启动后端服务
2. 登录系统（manager 或 admin 角色）
3. 进入合同管理页面
4. 点击"批量导入"
5. 分别上传测试文件
6. 验证成功/失败提示信息是否正确
7. 检查数据库中的数据是否符合预期

### 3. 集成测试验证
通过 MockMvc 测试完整的 Controller → Service 流程：
```java
@Test
void testImportData_Fail_WithSqlInjection() throws Exception {
    // 构造包含 SQL 注入的 Excel 文件
    MockMultipartFile file = createExcelWithSqlInjection();
    
    mockMvc.perform(multipart("/system/contract/importData")
        .file(file)
        .param("updateSupport", "false"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(500))
        .andExpect(jsonPath("$.msg").value(containsString("非法SQL字符")));
}
```

## 风险评估

### 低风险
- ✅ 只增强校验逻辑，不修改核心业务流程
- ✅ 有完整的单元测试覆盖
- ✅ 异常处理完善，不会导致系统崩溃

### 中风险
- ⚠️ 正则表达式可能误判：某些合法的特殊字符可能被拦截
  - **缓解措施**：正则规则经过仔细设计，避免过度严格
  - **应对方案**：如果用户反馈误判，可以调整规则

### 需要注意
- 校验规则需要与产品确认，是否允许某些特殊字符（如 `&`、`%` 等）
- 错误提示信息需要对用户友好，不要过于技术化

## 时间估计
- 实现 SecurityValidationUtil：30 分钟
- 修改 CmsContractServiceImpl：20 分钟
- 编写单元测试：40 分钟
- 手动测试验证：20 分钟
- **总计**：约 2 小时

## 后续优化
1. 考虑将校验规则配置化（存入数据库或配置文件）
2. 添加审计日志，记录所有非法字符拦截事件
3. 前端也可以添加一层预校验，提前拦截明显的非法字符
