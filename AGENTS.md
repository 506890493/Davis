# AGENTS.md - RuoYi-Vue Development Guide

This document provides guidance for AI coding agents working in this codebase.

## Project Overview

**RuoYi v3.9.0** - A Java/Spring Boot + Vue.js 2 enterprise rapid development framework with separated frontend/backend architecture.

### Tech Stack
- **Backend**: Java 8, Spring Boot 2.5.15, Spring Security 5.7, MyBatis, Redis, JWT
- **Frontend**: Vue.js 2.6.12, Element UI 2.15, Vuex 3.6, Axios
- **Database**: MySQL with Druid connection pool
- **Build Tools**: Maven (backend), npm/Vue CLI (frontend)

## Build & Run Commands

### Backend (Maven)

```bash
# Build (skip tests - no test suite configured)
mvn clean package -Dmaven.test.skip=true

# Run application
java -jar ruoyi-admin/target/ruoyi-admin.jar

# Or use startup scripts
./ry.sh start|stop|restart|status    # Linux
ry.bat start                          # Windows
```

### Frontend (ruoyi-ui)

```bash
cd ruoyi-ui

# Install dependencies
npm install
# Or with Chinese mirror
npm install --registry=https://registry.npmmirror.com

# Development server (port 80, proxies to localhost:8080)
npm run dev

# Production build
npm run build:prod

# Staging build
npm run build:stage
```

### Testing

This project does not have a standard test suite. No JUnit, Mockito, Jest, or other testing frameworks are configured.

## Project Structure

```
ruoyi-admin/       # Main entry point, controllers, configuration
ruoyi-system/      # Core business logic, domain entities, services, mappers
ruoyi-framework/   # Framework config (Security, Web, MyBatis)
ruoyi-common/      # Shared utilities, annotations, exceptions
ruoyi-quartz/      # Scheduled tasks module
ruoyi-generator/   # Code generator module
ruoyi-ui/          # Vue.js frontend application
sql/               # Database scripts
```

## Code Style Guidelines

### Backend Java

#### Architecture Pattern
Strictly follow: `Controller` -> `Service Interface` -> `ServiceImpl` -> `Mapper Interface` -> `Mapper XML`

#### Class Inheritance
- Controllers MUST extend `BaseController`
- Entity classes MUST extend `BaseEntity`

#### Controller Rules
```java
@RestController
@RequestMapping("/system/[feature]")
public class [Entity]Controller extends BaseController {
    @Autowired
    private I[Entity]Service [entity]Service;

    // Every endpoint MUST have @PreAuthorize
    @PreAuthorize("@ss.hasPermi('system:[feature]:list')")
    @GetMapping("/list")
    public TableDataInfo list([Entity] entity) {
        startPage();  // Required for pagination
        List<[Entity]> list = [entity]Service.select[Entity]List(entity);
        return getDataTable(list);
    }

    // CUD operations MUST have @Log annotation
    @PreAuthorize("@ss.hasPermi('system:[feature]:add')")
    @Log(title = "Module Name", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody [Entity] entity) {
        return toAjax([entity]Service.insert[Entity](entity));
    }
}
```

#### Response Types
- Paginated list queries: Return `TableDataInfo` using `startPage()` + `getDataTable(list)`
- Single operations (CRUD): Return `AjaxResult`
- Success/failure: Use `toAjax(int)` or `success(Object)`

#### Naming Conventions
- Service interface: `I[Entity]Service` (e.g., `ICmsContractService`)
- Service implementation: `[Entity]ServiceImpl`
- Methods:
  - List query: `select[Entity]List`
  - Get by ID: `select[Entity]By[IdName]`
  - Batch delete: `delete[Entity]By[IdName]s` (accepts `Long[]`)

#### Entity Class Requirements
```java
public class CmsContract extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Excel(name = "Field Name")                    // For exportable fields
    private String fieldName;

    @JsonFormat(pattern = "yyyy-MM-dd")            // For date fields
    @Excel(name = "Date Field", width = 30, dateFormat = "yyyy-MM-dd")
    private Date dateField;

    // Standard getters/setters
    // Override toString() using ToStringBuilder
}
```

#### Javadoc Requirements
```java
/**
 * Module description
 *
 * @author ruoyi
 * @date yyyy-MM-dd
 */
```

#### Prohibited Practices
- Never write business logic directly in Controllers
- Never use hardcoded error messages; use `AjaxResult.error("message")`
- Never omit `@PreAuthorize` permission annotations

### Frontend Vue.js

#### File Organization
- Views: `src/views/[module]/[feature]/index.vue`
- API calls: `src/api/[module]/[feature].js`
- Components: `src/components/`
- Store: `src/store/`

#### API Pattern
```javascript
import request from '@/utils/request'

export function listEntity(query) {
  return request({ url: '/system/feature/list', method: 'get', params: query })
}

export function getEntity(id) {
  return request({ url: '/system/feature/' + id, method: 'get' })
}

export function addEntity(data) {
  return request({ url: '/system/feature', method: 'post', data: data })
}

export function updateEntity(data) {
  return request({ url: '/system/feature', method: 'put', data: data })
}

export function delEntity(id) {
  return request({ url: '/system/feature/' + id, method: 'delete' })
}
```

#### Permission Control
Use `v-hasPermi` directive on buttons:
```html
<el-button v-hasPermi="['system:feature:add']">Add</el-button>
```

#### Formatting (from .editorconfig)
- Charset: UTF-8
- Indentation: 2 spaces
- Line endings: LF
- Trim trailing whitespace: Yes
- Final newline: Yes

## Key File Locations

| Purpose | Path |
|---------|------|
| Main config | `ruoyi-admin/src/main/resources/application.yml` |
| DB config | `ruoyi-admin/src/main/resources/application-druid.yml` |
| Logging | `ruoyi-admin/src/main/resources/logback.xml` |
| MyBatis config | `ruoyi-admin/src/main/resources/mybatis/mybatis-config.xml` |
| Frontend config | `ruoyi-ui/vue.config.js` |
| SQL scripts | `sql/` |

## Important Annotations

| Annotation | Purpose |
|------------|---------|
| `@PreAuthorize("@ss.hasPermi('...')")` | Permission check on endpoints |
| `@Log(title, businessType)` | Audit logging for CUD operations |
| `@Excel(name = "...")` | Excel export/import field mapping |
| `@JsonFormat(pattern = "...")` | Date serialization format |
| `@DataScope(deptAlias = "...")` | Data permission filtering |
