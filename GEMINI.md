# Project Context: RuoYi-Vue-master

## Project Overview
This project is based on **RuoYi v3.9.0**, a Java rapid development framework using **Spring Boot** for the backend and **Vue.js (v2)** for the frontend.

It has been customized to include a **Contract Management System (CMS)**, featuring contract tracking, task management (collection/renewal), and an approval workflow.

### Tech Stack
*   **Backend:** Java 8+, Spring Boot 2.5+, Spring Security, Redis, JWT, MyBatis, PageHelper.
*   **Frontend:** Vue.js 2.6, Element UI, Vuex, Axios.
*   **Database:** MySQL (implied).

## Architecture & Modules

The project follows a standard multi-module Maven structure:

*   **`ruoyi-admin`**: The main application entry point (`RuoYiApplication`). Contains controllers and configuration.
*   **`ruoyi-system`**: Core system business logic. **Crucially, the custom CMS logic (`CmsContract`, `CmsTask`, etc.) is implemented here.**
*   **`ruoyi-framework`**: Framework configuration (Security, Web, MyBatis).
*   **`ruoyi-common`**: Common utilities and annotations.
*   **`ruoyi-ui`**: The standalone frontend project.

## Key Custom Features (CMS)

Based on `sql/davis.sql` and recent modifications, the system includes:

1.  **Contract Management (`cms_contract`)**: Tracks contracts, including type (Accounting/Rent), payment details, and status.
2.  **Task Management (`cms_task`)**: Manages tasks linked to contracts, specifically for **Collection** (payment) and **Renewal** workflows.
3.  **Approval Workflow (`cms_approval`)**: Records approval history for contracts.
4.  **Communication Logs (`cms_communication`)**: Tracks interactions related to tasks.

## Building and Running

### Backend
The backend is a standard Maven project.

1.  **Build:**
    ```bash
    mvn clean package -Dmaven.test.skip=true
    ```
2.  **Run:**
    Run the `com.ruoyi.RuoYiApplication` class in `ruoyi-admin` or use the jar:
    ```bash
    java -jar ruoyi-admin/target/ruoyi-admin.jar
    ```
3.  **Configuration:** Check `ruoyi-admin/src/main/resources/application.yml` and `application-druid.yml` for database and Redis settings.

### Frontend (`ruoyi-ui`)

1.  **Install Dependencies:**
    ```bash
    cd ruoyi-ui
    npm install
    ```
2.  **Dev Server:**
    ```bash
    npm run dev
    ```
    (Typically runs on `http://localhost:80` and proxies API requests to `http://localhost:8080`).
3.  **Build for Production:**
    ```bash
    npm run build:prod
    ```

## Development Conventions

*   **Code Structure:** Follow the Controller -> Service -> Mapper -> SQL (XML) pattern.
*   **Authorization:** Use `@PreAuthorize("@ss.hasPermi('permission:string')")` on Controllers and `v-hasPermi="['permission:string']"` in Vue templates.
*   **Data Models:**
    *   Backend entities in `ruoyi-system/src/main/java/com/ruoyi/system/domain`.
    *   Frontend API calls in `ruoyi-ui/src/api/...`.
    *   Frontend Views in `ruoyi-ui/src/views/...`.

## Important Files & Paths

*   **SQL Scripts:** `sql/davis.sql` (Core CMS schema), `sql/ry_202*.sql` (Base RuoYi schema).
*   **Frontend Contract View:** `ruoyi-ui/src/views/system/contract/index.vue`
*   **Frontend Task View:** `ruoyi-ui/src/views/system/task/index.vue`
*   **Backend Contract Controller:** `ruoyi-admin/src/main/java/com/ruoyi/web/controller/davis/CmsContractController.java`
*   **Backend Contract Service:** `ruoyi-system/src/main/java/com/ruoyi/system/service/impl/CmsContractServiceImpl.java`
