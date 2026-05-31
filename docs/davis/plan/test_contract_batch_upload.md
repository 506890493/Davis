# 批量上传合同功能测试计划

## 测试目标
1. 验证批量上传合同功能正常工作，能够解析并导入合同数据
2. 验证当上传文件包含非法字符时，系统能够正确停止上传并提醒用户
3. 实现测试覆盖率目标：单元测试 90%、集成测试 90%、E2E 测试 90%

## 测试范围

### 1. 正常场景测试
- 1.1 上传有效格式的 Excel 文件（.xlsx, .xls）
- 1.2 上传包含多条合同数据的文件
- 1.3 验证合同数据成功导入数据库
- 1.4 验证导入成功的提示信息
- 1.5 验证 updateSupport 参数功能（更新已存在的合同）

### 2. 异常场景测试
- 2.1 上传非 Excel 文件（.txt, .pdf, .doc）
- 2.2 上传空文件
- 2.3 上传文件缺少必填字段（contractCode, contractName）
- 2.4 上传文件包含重复的合同编码（未开启 updateSupport）
- 2.5 上传文件包含非法字符
- 2.6 上传超大文件（超过限制）
- 2.7 上传格式错误的 Excel 文件

### 3. 非法字符场景测试
- 3.1 合同名称包含 SQL 注入字符（如 '; DROP TABLE;）
- 3.2 合同名称包含脚本注入字符（如 `<script>alert('xss')</script>`）
- 3.3 合同名称包含特殊控制字符（如 \n, \r, \t 等）
- 3.4 合同编码包含非法字符
- 3.5 金额字段包含非数字字符
- 3.6 日期字段格式不正确

## 测试环境
- 后端：Spring Boot 2.5.15, MySQL 8.0
- 前端：Vue 2.6, Element UI 2.15
- 测试框架：JUnit 5, Mockito, TestContainers
- 测试数据：准备有效的 Excel 模板文件

## 测试用例设计

### 单元测试（CmsContractServiceTest.java）
覆盖 `importCmsContract` 方法：
- `testImportContract_Success_WithValidData()`
- `testImportContract_Fail_WhenContractCodeIsNull()`
- `testImportContract_Fail_WhenContractNameIsNull()`
- `testImportContract_Fail_WhenContractCodeExists_WithoutUpdateSupport()`
- `testImportContract_Success_WhenContractCodeExists_WithUpdateSupport()`
- `testImportContract_Fail_WithEmptyList()`
- `testImportContract_Fail_WithSqlInjection()`
- `testImportContract_Fail_WithXssInjection()`
- `testImportContract_Fail_WithControlCharacters()`
- `testImportContract_Fail_WithInvalidAmountFormat()`
- `testImportContract_Fail_WithInvalidDateFormat()`

### 集成测试（CmsContractImportIntegrationTest.java）
覆盖 Controller 层到 Service 层的完整流程：
- `testImportData_Success_WithValidExcelFile()`
- `testImportData_Fail_WithEmptyFile()`
- `testImportData_Fail_WithInvalidFileType()`
- `testImportData_Fail_WithMalformedExcel()`
- `testImportData_Fail_WithIllegalCharacters()`
- `testImportTemplate_Success()`

### E2E 测试（ContractImportE2ETest.java）
使用 Playwright 或 Selenium 模拟用户操作：
- `testUploadContractFile_Success_Flow()`
- `testUploadContractFile_Fail_WithIllegalCharacters_Flow()`
- `testDownloadTemplate_Flow()`

## 测试数据准备

### 有效 Excel 文件（contracts_valid.xlsx）
| contractCode | contractName | customerId | amount | startDate | endDate | status |
|--------------|--------------|------------|--------|-----------|---------|--------|
| TEST001 | 测试合同A | 1 | 10000 | 2026-01-01 | 2026-12-31 | 0 |
| TEST002 | 测试合同B | 1 | 20000 | 2026-02-01 | 2027-01-31 | 0 |

### 包含非法字符的 Excel 文件（contracts_invalid.xlsx）
| contractCode | contractName | customerId | amount | startDate | endDate | status |
|--------------|--------------|------------|--------|-----------|---------|--------|
| TEST003 | 合同'; DROP TABLE cms_contract; | 1 | 10000 | 2026-01-01 | 2026-12-31 | 0 |
| TEST004 | <script>alert('xss')</script>合同 | 1 | 20000 | 2026-02-01 | 2027-01-31 | 0 |

### 其他测试文件
- `empty.xlsx` - 空文件
- `malformed.xlsx` - 格式错误的 Excel 文件
- `large.xlsx` - 超大文件（如果测试需要）
- `test.txt` - 非 Excel 文件

## 验证方法

### 正常场景验证
1. 检查数据库中是否正确插入了合同记录
2. 检查返回的成功消息是否正确
3. 检查合同编号、名称、金额、日期等字段是否正确

### 异常场景验证
1. 检查是否抛出正确的异常
2. 检查错误消息是否清晰明确
3. 检查数据库中未插入错误数据
4. 检查事务是否正确回滚

### 非法字符场景验证
1. 检查是否正确识别非法字符
2. 检查是否停止上传过程
3. 检查是否返回具体的错误提示信息（指出哪一行、哪个字段有问题）
4. 检查数据库中未注入恶意数据
5. 检查日志中是否记录了非法字符尝试

## 执行顺序
1. 先执行单元测试，验证业务逻辑正确性
2. 再执行集成测试，验证各层协作正常
3. 最后执行 E2E 测试，验证用户端到端流程
4. 确保每个测试用例独立运行，不依赖其他用例

## 风险评估
- 数据库事务回滚可能影响其他测试
- 测试文件路径在不同环境下可能不同
- Mock 数据可能与实际数据不一致

## 预期结果
- 所有单元测试通过，覆盖率 ≥ 90%
- 所有集成测试通过，覆盖率 ≥ 90%
- 所有 E2E 测试通过，覆盖率 ≥ 90%
- 非法字符被正确拦截，不会影响数据安全