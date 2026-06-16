package com.ruoyi.web.controller.davis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * KB 菜单 component 路径校验测试。
 *
 * <p>回归 bug：{@code data-init.sql} 与 {@code update_20260611_kb.sql} 中
 * 知识库相关菜单的 {@code component} 字段缺少 {@code system/} 前缀（如
 * {@code kb/portal/index}），导致 RuoYi 前端 {@code permission.js:118} 通过
 * {@code @/views/${route.component}} 找不到 webpack chunk，manager 点击
 * 「知识库学习」时 URL 留在 /index，体感为「加载缓慢」。</p>
 *
 * <p>正确路径必须为 {@code system/kb/portal/index} 等（与
 * {@code ruoyi-ui/src/views/system/kb/...} 实际目录结构一致）。</p>
 */
@DisplayName("KB 菜单 component 路由路径校验")
class KbMenuRoutingTest extends BaseControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 从 sys_menu 表读取所有与 KB 相关的菜单（C 类型的页面型菜单），
     * 并按 menu_name 关联的"KB 关键词"过滤。
     */
    private List<Map<String, Object>> loadKbMenus() {
        // 查所有 menu_type='C'（页面型）的 KB 相关菜单
        // KB 标识：component LIKE '%kb%' OR menu_name LIKE '%知识%' OR menu_name LIKE '%目录%' OR menu_name LIKE '%文档%' OR menu_name LIKE '%回收%'
        String sql = "SELECT menu_id, menu_name, parent_id, path, component, menu_type, perms "
                   + "FROM sys_menu "
                   + "WHERE menu_type = 'C' "
                   + "AND ("
                   + "  (component IS NOT NULL AND component LIKE '%kb%') "
                   + "  OR menu_name LIKE '%知识%' "
                   + "  OR menu_name LIKE '%目录%' "
                   + "  OR menu_name LIKE '%文档%' "
                   + "  OR menu_name LIKE '%回收%'"
                   + ") "
                   + "ORDER BY menu_id";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        assertNotNull(rows, "查询 sys_menu 失败");
        return rows;
    }

    @Test
    @DisplayName("KB 菜单的 component 路径必须以 'system/kb' 开头（否则 webpack 找不到 chunk）")
    void testKbMenuComponentPathPrefix() {
        List<Map<String, Object>> kbMenus = loadKbMenus();
        assertTrue(kbMenus.size() >= 1,
            "预期至少存在 1 条 KB 页面型菜单（C 类型），但实际为 0 条，请检查 data-init.sql");

        List<String> errors = new ArrayList<>();
        for (Map<String, Object> m : kbMenus) {
            String menuName = String.valueOf(m.get("menu_name"));
            String component = m.get("component") == null ? "" : String.valueOf(m.get("component"));
            // 排除 component 为空（如按钮型 / 父菜单 type=M 但这里已过滤 C，仍容错）
            if (component.isEmpty()) {
                continue;
            }
            // 必须以 system/kb 开头
            if (!component.startsWith("system/kb")) {
                errors.add("菜单 '" + menuName + "' 的 component='" + component
                    + "' 必须以 'system/kb' 开头（应改为 system/" + component + "）");
            }
        }

        if (!errors.isEmpty()) {
            fail("KB 菜单 component 路径缺少 'system/' 前缀，共 " + errors.size() + " 处错误：\n  - "
                + String.join("\n  - ", errors));
        }
    }

    @Test
    @DisplayName("KB 菜单 component 指向的 .vue 文件必须存在于 ruoyi-ui/src/views/ 下")
    void testKbMenuComponentFileExists() {
        List<Map<String, Object>> kbMenus = loadKbMenus();
        assertTrue(kbMenus.size() >= 1,
            "预期至少存在 1 条 KB 页面型菜单（C 类型），但实际为 0 条，请检查 data-init.sql");

        // 定位前端 views 根目录
        // 测试 cwd 是 D:/GitHub/ruoyi-davis/ruoyi-admin/，需要向上 1 级
        File viewsDir = new File("ruoyi-ui/src/views");
        if (!viewsDir.isDirectory()) {
            // 兜底：用绝对路径（部分 IDE cwd 不一致时）
            viewsDir = new File("D:/GitHub/ruoyi-davis/ruoyi-ui/src/views");
        }
        assertTrue(viewsDir.isDirectory(),
            "未找到前端 views 目录，期望路径 ruoyi-ui/src/views，实际 cwd=" + new File(".").getAbsolutePath());

        List<String> errors = new ArrayList<>();
        for (Map<String, Object> m : kbMenus) {
            String menuName = String.valueOf(m.get("menu_name"));
            String component = m.get("component") == null ? "" : String.valueOf(m.get("component"));
            if (component.isEmpty()) {
                continue;
            }
            File vueFile = new File(viewsDir, component + ".vue");
            if (!vueFile.isFile()) {
                errors.add("菜单 '" + menuName + "' component='" + component
                    + "' 对应的 .vue 文件不存在：" + vueFile.getAbsolutePath());
            }
        }

        if (!errors.isEmpty()) {
            fail("KB 菜单 component 路径对应的 .vue 文件缺失，共 " + errors.size() + " 处错误：\n  - "
                + String.join("\n  - ", errors));
        }
    }

    @Test
    @DisplayName("KB 菜单 component 路径示例（debug 输出）")
    void testDebugPrintKbMenuComponents() {
        List<Map<String, Object>> kbMenus = loadKbMenus();
        // 仅打印，帮助定位，不抛断言
        StringBuilder sb = new StringBuilder("\n=== KB 菜单 component 当前值 ===\n");
        for (Map<String, Object> m : kbMenus) {
            sb.append(String.format("  menu_id=%s, menu_name=%s, path=%s, component=%s, perms=%s%n",
                m.get("menu_id"), m.get("menu_name"), m.get("path"),
                m.get("component"), m.get("perms")));
        }
        System.out.println(sb);
        assertTrue(true, "调试输出完成");
    }
}
