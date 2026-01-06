
---

# RuoYi (Java/Spring Boot) 编程指令

## 1. 核心架构原则

* **继承基类**：所有的 Controller 必须继承 `BaseController`，所有的 Entity 必须根据字段情况继承 `BaseEntity`。
* **分层标准**：严格遵守 `Controller` -> `Service接口` -> `ServiceImpl` -> `Mapper接口` -> `Mapper XML` 的分层模式。
* **权限控制**：每个接口必须添加 `@PreAuthorize("@ss.hasPermi('模块名:子模块:动作')")`。

## 2. Controller 开发规范

* **注解选择**：统一使用 `@RestController`。
* **响应封装**：
* 分页查询：使用 `startPage()` 方法，返回 `TableDataInfo`（通过 `getDataTable(list)` 构造）。
* 增删改查：返回 `AjaxResult`。
* 操作成功/失败：使用 `toAjax(int)` 或 `success(Object)` 方法。


* **日志注解**：增/删/改操作必须添加 `@Log(title = "模块名称", businessType = BusinessType.INSERT/UPDATE/DELETE)`。
* **路径命名**：使用 `@RequestMapping("/system/功能名")` 风格。

## 3. Service 与 Mapper 规范

* **Service 命名**：接口以 `I` 开头（如 `ICmsContractService`），实现类以 `Impl` 结尾。
* **注入方式**：优先使用 `@Autowired` 进行字段注入。
* **方法命名习惯**：
* 列表查询：`select[EntityName]List`
* 主键查询：`select[EntityName]By[IdName]`
* 批量删除：`delete[EntityName]By[IdName]s` (接收 `Long[]`)。



## 4. 命名与注释风格

* **类注释**：必须包含 `@author ruoyi` 和 `@date yyyy-MM-dd` 格式。
* **方法注释**：首行简述功能，并包含标准的 Javadoc `@param` 和 `@return` 说明。
* **POJO 注解**：实体类必须包含 Excel 导出注解 `@Excel(name = "字段名称")`（如果该字段需要导出）。

## 5. 代码示例模板 (按此逻辑生成)

当要求生成 Controller 时，请遵循以下结构：

```java
/**
 * [模块说明]Controller
 */
@RestController
@RequestMapping("/system/[path]")
public class [Entity]Controller extends BaseController {
    @Autowired
    private I[Entity]Service [entity]Service;

    @PreAuthorize("@ss.hasPermi('system:[path]:list')")
    @GetMapping("/list")
    public TableDataInfo list([Entity] [entity]) {
        startPage();
        List<[Entity]> list = [entity]Service.select[Entity]List([entity]);
        return getDataTable(list);
    }
    
    // ... getInfo, add, edit, remove 等逻辑
}

```

## 6. 禁令

* **禁止**在 Controller 中直接写业务逻辑。
* **禁止**使用硬编码的错误提示，尽量使用 `AjaxResult.error("错误内容")`。
* **禁止**遗漏权限字符 `@PreAuthorize`。

---

