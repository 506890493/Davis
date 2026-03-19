# AGENTS.md - Davis (RuoYi-Vue Fork) Development Guide

This document provides guidance for AI coding agents working in this codebase.

## Project Overview
**Davis** - a RuoYi-Vue fork for accounting/rental contract management. Tech stack is identical to RuoYi-Vue v3.9.0.

### Tech Stack
- **Backend**: Java 8, Spring Boot 2.5.15, Spring Security 5.7, MyBatis, Redis, JWT
- **Frontend**: Vue.js 2.6.12, Element UI 2.15, Vuex 3.6, Axios, Sass
- **Database**: MySQL with Druid connection pool

---

## Build & Run Commands

### Backend (Maven)
```bash
# Full build (skip tests - no test suite configured)
mvn clean package -Dmaven.test.skip=true

# Build specific module with dependencies
mvn clean package -Dmaven.test.skip=true -pl ruoyi-admin -am

# Run application
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

### Frontend (ruoyi-ui)
```bash
cd ruoyi-ui
npm install
npm run dev          # Dev server (port 80, proxies to localhost:8080)
npm run build:prod   # Production build
npm run build:stage  # Staging build
```

### Testing
**No test framework configured.** No JUnit, Mockito, Jest, or other testing dependencies exist.

---

## Project Structure
```
ruoyi-admin/       # Main entry point, controllers, configuration
ruoyi-system/      # Core business logic, entities, services, mappers
ruoyi-framework/   # Framework config (Security, Web, MyBatis)
ruoyi-common/      # Shared utilities, annotations, exceptions
ruoyi-quartz/      # Scheduled tasks module
ruoyi-generator/   # Code generator module
ruoyi-ui/          # Vue.js 2 frontend
```

---

## Backend Java Code Style

### Architecture
`Controller` -> `Service Interface` -> `ServiceImpl` -> `Mapper Interface` -> `Mapper XML`

### Class Inheritance
- **Controllers**: MUST extend `BaseController`
- **Entities**: MUST extend `BaseEntity`

### Controller Rules
```java
@RestController
@RequestMapping("/system/[feature]")
public class [Entity]Controller extends BaseController {
    @Autowired
    private I[Entity]Service [entity]Service;

    @PreAuthorize("@ss.hasPermi('system:[feature]:list')")
    @GetMapping("/list")
    public TableDataInfo list([Entity] entity) {
        startPage();
        List<[Entity]> list = [entity]Service.select[Entity]List(entity);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('system:[feature]:add')")
    @Log(title = "Module Name", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody [Entity] entity) {
        return toAjax([entity]Service.insert[Entity](entity));
    }
}
```

### Response Types
- **Paginated queries**: `TableDataInfo` via `startPage()` + `getDataTable(list)`
- **CRUD operations**: `AjaxResult` via `toAjax(int)`, `success(Object)`, or `error(String)`
- **No business logic in Controllers**

### Naming Conventions
| Type | Convention |
|------|------------|
| Service interface | `I[Entity]Service` (e.g., `ICmsContractService`) |
| Service impl | `[Entity]ServiceImpl` |
| List query | `select[Entity]List` |
| Get by ID | `select[Entity]By[IdName]` |
| Batch delete | `delete[Entity]By[IdName]s(Long[])` |
| Insert | `insert[Entity]` |
| Update | `update[Entity]` |

### Entity Class
```java
public class CmsContract extends BaseEntity {
    private static final long serialVersionUID = 1L;
    @Excel(name = "Field Name")
    private String fieldName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "Date Field", width = 30, dateFormat = "yyyy-MM-dd")
    private Date dateField;
}
```

### Javadoc (Required)
```java
/**
 * Module description
 * @author ruoyi
 * @date yyyy-MM-dd
 */
```

### Import Organization (in order)
1. `static` imports
2. `org.apache.*` / `org.springframework.*`
3. `com.ruoyi.*`
4. `javax.*` / `java.*`

### Prohibited
- No business logic in Controllers
- No hardcoded error messages (use `AjaxResult.error("message")`)
- Never omit `@PreAuthorize`
- Never use `AjaxResult.success()` with data for error cases

---

## Frontend Vue.js Code Style

### File Organization
- Views: `src/views/[module]/[feature]/index.vue`
- API modules: `src/api/[module]/[feature].js`
- Components: `src/views/[module]/[feature]/components/*.vue` or shared `src/components/`

### API Pattern
```javascript
import request from '@/utils/request'
export function listEntity(query) { return request({ url: '/system/feature/list', method: 'get', params: query }) }
export function getEntity(id) { return request({ url: '/system/feature/' + id, method: 'get' }) }
export function addEntity(data) { return request({ url: '/system/feature', method: 'post', data: data }) }
export function updateEntity(data) { return request({ url: '/system/feature', method: 'put', data: data }) }
export function delEntity(id) { return request({ url: '/system/feature/' + id, method: 'delete' }) }
```

### Component Options Order
```javascript
export default {
  name: "ComponentName",
  dicts: ["dict_type_1", "dict_type_2"],  // data dicts first
  components: {},                          // then components
  props: {},
  mixins: [],
  data() { return {} },
  computed: {},
  watch: {},
  created() {},
  mounted() {},
  methods: {}
}
```

### Permission Control
```html
<el-button v-hasPermi="['system:feature:add']">Add</el-button>
```

### Formatting
- **Encoding**: UTF-8
- **Indentation**: 2 spaces (Vue uses 2-space indentation, not 4)
- **Line endings**: LF
- **Trailing whitespace**: trimmed
- **Quotes**: single quotes for JS strings

### Vue Component Best Practices
- Use `dict-tag` component for displaying dict-type values
- Use `parseTime()` for date formatting
- Use `$modal.msgSuccess()` / `$modal.msgError()` for feedback
- Use `this.$refs['formRef'].validate()` for form validation
- Use `this.$router.push()` for navigation
- Use `v-hasPermi` on all action buttons

---

## Important Annotations (Backend)
| Annotation | Purpose |
|------------|---------|
| `@PreAuthorize("@ss.hasPermi('...')")` | Permission check |
| `@Log(title, businessType)` | Audit logging (INSERT/UPDATE/DELETE) |
| `@Excel(name = "...")` | Excel export/import |
| `@JsonFormat(pattern = "...")` | Date serialization |
| `@DataScope(deptAlias = "...")` | Data permission filtering |

---

## Key File Locations
| Purpose | Path |
|---------|------|
| Main config | `ruoyi-admin/src/main/resources/application.yml` |
| DB config | `ruoyi-admin/src/main/resources/application-druid.yml` |
| Logging | `ruoyi-admin/src/main/resources/logback.xml` |
| Frontend config | `ruoyi-ui/vue.config.js` |
| SQL scripts | `sql/` |
| System user entity | `ruoyi-system/src/main/java/com/ruoyi/system/domain/SysUser.java` |
| Base controller | `ruoyi-framework/src/main/java/com/ruoyi/framework/web/service/BaseController.java` |

---

## Custom Business Domain (Davis Extensions)
- **Contract types**: `Accounting` (dict: `1`) and `Rent` (dict: `2`)
- **Contract module**: `src/views/system/contract/`, `/system/contract/` API
- **Task module**: `src/views/system/task/`, `/system/task/` API
- **Ledger module**: `src/views/system/ledger/`
- **Approval workflow**: contracts have `auditStatus` (0=pending, 1=approved, 2=rejected)
