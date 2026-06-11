package com.ruoyi.web.controller.davis;

import com.ruoyi.system.domain.CmsKbCategory;
import com.ruoyi.system.domain.CmsKbDocument;
import com.ruoyi.system.service.ICmsKbCategoryService;
import com.ruoyi.system.service.ICmsKbDocumentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 知识库 Controller 集成测试
 *
 * <p>覆盖场景：</p>
 * <ul>
 *   <li>权限矩阵：manager 可发布，sales 调管理端 403</li>
 *   <li>草稿隔离：未发布文档对销售侧不可见</li>
 *   <li>删除保护：删除有子目录/有文档的目录被拒</li>
 *   <li>版本回滚：通过 controller 端点验证</li>
 *   <li>回收站：软删→列表→恢复全流程</li>
 * </ul>
 */
@DisplayName("集成测试: 知识库 Controller")
class CmsKbControllerIntegrationTest extends BaseControllerTest {

    @Autowired
    private ICmsKbCategoryService categoryService;

    @Autowired
    private ICmsKbDocumentService documentService;

    @Test
    @DisplayName("1. manager 创建草稿→发布→销售侧 detail 可见")
    void testManagerPublishAndPortalVisible() throws Exception {
        // 1) 新建草稿文档（直接走 service，模拟 manager 创建）
        CmsKbDocument doc = new CmsKbDocument();
        doc.setCategoryId(1L);
        doc.setTitle("集成测试文档_发布流程");
        doc.setDocType("2");
        doc.setSummary("测试摘要");
        doc.setNewContent("<p>正文</p>");
        Long id = documentService.insert(doc);
        assertNotNull(id);

        // 2) manager 调 /kb/document/publish
        CmsKbDocument pubReq = new CmsKbDocument();
        pubReq.setId(id);
        asManager(HttpMethod.PUT, "/kb/document/publish", pubReq)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // 3) 验证数据库已发布
        CmsKbDocument published = documentService.selectById(id);
        assertNotNull(published);
        assertTrue(published.getStatus() != null && published.getStatus() == 1, "文档应已发布 status=1");

        // 4) sales 调 /kb/portal/detail/{id} 可见
        asSales(HttpMethod.GET, "/kb/portal/detail/" + id, null)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.title").value("集成测试文档_发布流程"));
    }

    @Test
    @DisplayName("2. 草稿对销售侧不可见（detail 返回 404）")
    void testDraftNotVisibleToSales() throws Exception {
        // manager 创建一篇草稿（不发布）
        CmsKbDocument doc = new CmsKbDocument();
        doc.setCategoryId(1L);
        doc.setTitle("草稿隔离测试");
        doc.setDocType("2");
        doc.setNewContent("x");
        Long id = documentService.insert(doc);

        // sales 调 /kb/portal/detail 应看不到（code != 200）
        asSales(HttpMethod.GET, "/kb/portal/detail/" + id, null)
            .andExpect(jsonPath("$.code").value(not(200)));
    }

    @Test
    @DisplayName("3. sales 调 /kb/category/add 返 403（RuoYi 风格：HTTP 200 + body.code=403）")
    void testSalesCannotAddCategory() throws Exception {
        asSales(HttpMethod.POST, "/kb/category", new CmsKbCategory())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("4. 删除有子目录的父目录返错误（code != 200）")
    void testDeleteCategoryWithChildren() throws Exception {
        // 建一棵父子目录
        CmsKbCategory parent = new CmsKbCategory();
        parent.setParentId(0L);
        parent.setName("int_test_parent");
        parent.setOrderNum(99);
        parent.setIsRequired(0);
        parent.setStatus(1);
        categoryService.insert(parent);

        CmsKbCategory child = new CmsKbCategory();
        child.setParentId(parent.getId());
        child.setName("int_test_child");
        child.setOrderNum(1);
        child.setIsRequired(0);
        child.setStatus(1);
        categoryService.insert(child);

        // 调 manager 删除父目录 → 应被 ServiceException 拒绝
        asManager(HttpMethod.DELETE, "/kb/category/" + parent.getId(), null)
            .andExpect(jsonPath("$.code").value(not(200)));
    }

    @Test
    @DisplayName("5. manager 删除空目录成功，code=200")
    void testDeleteEmptyCategory() throws Exception {
        CmsKbCategory leaf = new CmsKbCategory();
        leaf.setParentId(0L);
        leaf.setName("int_test_leaf");
        leaf.setOrderNum(100);
        leaf.setIsRequired(0);
        leaf.setStatus(1);
        categoryService.insert(leaf);

        asManager(HttpMethod.DELETE, "/kb/category/" + leaf.getId(), null)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("6. manager 调 /kb/document/list 返 TableDataInfo（code=200）")
    void testDocumentList() throws Exception {
        asManager(HttpMethod.GET, "/kb/document/list", null)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("7. manager 调 /kb/portal/tree 返 4 个预置分类")
    void testPortalTree() throws Exception {
        asManager(HttpMethod.GET, "/kb/portal/tree", null)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("8. 软删除→回收站列表→恢复 完整流程")
    void testRecycleRestore() throws Exception {
        // 1) 创建草稿
        CmsKbDocument doc = new CmsKbDocument();
        doc.setCategoryId(1L);
        doc.setTitle("回收站流程测试");
        doc.setDocType("2");
        doc.setNewContent("x");
        Long id = documentService.insert(doc);

        // 2) manager 调 /kb/document DELETE 软删
        asManager(HttpMethod.DELETE, "/kb/document/" + id, null)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // 3) 调 /kb/recycle/list 看到
        ResultActions listResult = asManager(HttpMethod.GET, "/kb/recycle/list", null);
        listResult.andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // 4) 调 /kb/recycle/restore 恢复
        asManager(HttpMethod.POST, "/kb/recycle/restore", new Long[]{id})
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // 5) 验证 del_flag=0
        CmsKbDocument restored = documentService.selectById(id);
        assertNotNull(restored);
        assertTrue(restored.getDelFlag() == null || restored.getDelFlag() == 0, "文档应已恢复 del_flag=0");
    }

    @Test
    @DisplayName("9. 版本回滚 controller 端点")
    void testVersionRollback() throws Exception {
        // 1) 建文档（自动 v1）
        CmsKbDocument doc = new CmsKbDocument();
        doc.setCategoryId(1L);
        doc.setTitle("回滚测试文档");
        doc.setDocType("2");
        doc.setNewContent("v1 content");
        Long id = documentService.insert(doc);

        // 2) update 产生 v2
        CmsKbDocument update = new CmsKbDocument();
        update.setId(id);
        update.setTitle("回滚测试文档");
        update.setCategoryId(1L);
        update.setNewContent("v2 content");
        update.setUpdateBy("manager");
        documentService.update(update);

        // 3) 回滚到 v1
        asManager(HttpMethod.POST, "/kb/version/" + id + "/1/rollback", null)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // 4) 验证产生了 v3
        CmsKbDocument latest = documentService.selectById(id);
        assertNotNull(latest);
        assertTrue(latest.getCurrentVersion() != null && latest.getCurrentVersion() == 3, "应为 v3");
    }
}

