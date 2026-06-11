package com.ruoyi.web.controller.davis;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.CmsKbCategory;
import com.ruoyi.system.service.ICmsKbCategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 知识库-目录Service单元测试
 *
 * 验证业务规则：循环引用、有子目录/有文档时拒绝删除。
 */
@DisplayName("KB 目录Service单元测试")
public class CmsKbCategoryServiceTest extends BaseControllerTest {

    @Autowired
    private ICmsKbCategoryService categoryService;

    @Test
    @DisplayName("删除有子目录的父目录应抛 ServiceException")
    void testDeleteCategoryWithChildren() {
        CmsKbCategory parent = new CmsKbCategory();
        parent.setParentId(0L);
        parent.setName("parent_test");
        parent.setOrderNum(1);
        parent.setIsRequired(0);
        parent.setStatus(1);
        categoryService.insert(parent);

        CmsKbCategory child = new CmsKbCategory();
        child.setParentId(parent.getId());
        child.setName("child_test");
        child.setOrderNum(1);
        child.setIsRequired(0);
        child.setStatus(1);
        categoryService.insert(child);

        assertThrows(ServiceException.class,
            () -> categoryService.deleteByIds(new Long[]{parent.getId()}));
    }

    @Test
    @DisplayName("循环引用：把目录移到自身下应抛异常")
    void testCircularReference() {
        CmsKbCategory cat = new CmsKbCategory();
        cat.setParentId(0L);
        cat.setName("circular_test");
        cat.setOrderNum(1);
        cat.setIsRequired(0);
        cat.setStatus(1);
        categoryService.insert(cat);

        CmsKbCategory update = new CmsKbCategory();
        update.setId(cat.getId());
        update.setParentId(cat.getId());
        assertThrows(ServiceException.class, () -> categoryService.update(update));
    }

    @Test
    @DisplayName("将目录移到自己子目录下应抛异常")
    void testMoveToOwnDescendant() {
        CmsKbCategory a = new CmsKbCategory();
        a.setParentId(0L);
        a.setName("ancestor_test");
        a.setOrderNum(1);
        a.setIsRequired(0);
        a.setStatus(1);
        categoryService.insert(a);

        CmsKbCategory b = new CmsKbCategory();
        b.setParentId(a.getId());
        b.setName("mid_test");
        b.setOrderNum(1);
        b.setIsRequired(0);
        b.setStatus(1);
        categoryService.insert(b);

        CmsKbCategory c = new CmsKbCategory();
        c.setParentId(b.getId());
        c.setName("leaf_test");
        c.setOrderNum(1);
        c.setIsRequired(0);
        c.setStatus(1);
        categoryService.insert(c);

        // 试图把 a 移到 c 下（c 是 a 的后代）
        CmsKbCategory update = new CmsKbCategory();
        update.setId(a.getId());
        update.setParentId(c.getId());
        assertThrows(ServiceException.class, () -> categoryService.update(update));
    }

    @Test
    @DisplayName("正常更新（未发生循环引用）应成功")
    void testNormalUpdate() {
        CmsKbCategory cat = new CmsKbCategory();
        cat.setParentId(0L);
        cat.setName("normal_update_test");
        cat.setOrderNum(1);
        cat.setIsRequired(0);
        cat.setStatus(1);
        categoryService.insert(cat);

        CmsKbCategory update = new CmsKbCategory();
        update.setId(cat.getId());
        update.setName("renamed");
        int rows = categoryService.update(update);
        assertEquals(1, rows);

        CmsKbCategory loaded = categoryService.selectById(cat.getId());
        assertEquals("renamed", loaded.getName());
    }

    @Test
    @DisplayName("selectAll 应返回已启用目录")
    void testSelectAll() {
        java.util.List<CmsKbCategory> all = categoryService.selectAll();
        assertNotNull(all);
        assertTrue(all.size() >= 4, "至少有 4 条种子目录");
    }
}
