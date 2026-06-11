package com.ruoyi.web.controller.davis;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.CmsKbDocument;
import com.ruoyi.system.domain.CmsKbDocumentVersion;
import com.ruoyi.system.service.ICmsKbDocumentService;
import com.ruoyi.system.service.ICmsKbDocumentVersionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 知识库-文档Service单元测试
 *
 * 验证业务规则：状态机 + 版本自动生成。
 */
@DisplayName("KB 文档Service单元测试")
public class CmsKbDocumentServiceTest extends BaseControllerTest {

    @Autowired
    private ICmsKbDocumentService documentService;

    @Autowired
    private ICmsKbDocumentVersionService versionService;

    @Test
    @DisplayName("新建文档自动产生 v1")
    void testInsertCreatesV1() {
        CmsKbDocument doc = new CmsKbDocument();
        doc.setCategoryId(1L);
        doc.setTitle("测试文档");
        doc.setDocType("2");
        doc.setNewContent("<p>内容</p>");
        Long id = documentService.insert(doc);

        CmsKbDocumentVersion v = versionService.selectCurrentByDoc(id);
        assertNotNull(v);
        assertEquals(1, v.getVersionNo().intValue());
        assertEquals(1, v.getIsCurrent().intValue());
        assertEquals("测试文档", v.getTitle());
        assertEquals("<p>内容</p>", v.getContent());
    }

    @Test
    @DisplayName("编辑文档产生 v2，旧版本 is_current=0")
    void testUpdateCreatesNewVersion() {
        CmsKbDocument doc = new CmsKbDocument();
        doc.setCategoryId(1L);
        doc.setTitle("原标题");
        doc.setDocType("2");
        doc.setNewContent("原内容");
        Long id = documentService.insert(doc);

        CmsKbDocument update = new CmsKbDocument();
        update.setId(id);
        update.setCategoryId(1L);
        update.setTitle("新标题");
        update.setDocType("2");
        update.setNewContent("新内容");
        documentService.update(update);

        List<CmsKbDocumentVersion> versions = versionService.selectByDocument(id);
        assertEquals(2, versions.size());
        CmsKbDocumentVersion current = versionService.selectCurrentByDoc(id);
        assertEquals(2, current.getVersionNo().intValue());
        assertEquals(1, current.getIsCurrent().intValue());
        assertEquals("新标题", current.getTitle());
        assertEquals("新内容", current.getContent());
    }

    @Test
    @DisplayName("回滚到 v1 产生 v3（内容同 v1）")
    void testRollbackProducesNewVersion() {
        CmsKbDocument doc = new CmsKbDocument();
        doc.setCategoryId(1L);
        doc.setTitle("A");
        doc.setDocType("2");
        doc.setNewContent("a");
        Long id = documentService.insert(doc);

        CmsKbDocument update = new CmsKbDocument();
        update.setId(id);
        update.setCategoryId(1L);
        update.setTitle("B");
        update.setDocType("2");
        update.setNewContent("b");
        documentService.update(update);

        versionService.rollback(id, 1);
        CmsKbDocumentVersion v3 = versionService.selectCurrentByDoc(id);
        assertEquals(3, v3.getVersionNo().intValue());
        assertEquals("A", v3.getTitle());
        assertEquals("a", v3.getContent());
    }

    @Test
    @DisplayName("回滚到当前版本抛异常")
    void testRollbackToCurrentRejected() {
        CmsKbDocument doc = new CmsKbDocument();
        doc.setCategoryId(1L);
        doc.setTitle("X");
        doc.setDocType("2");
        doc.setNewContent("x");
        Long id = documentService.insert(doc);
        assertThrows(ServiceException.class, () -> versionService.rollback(id, 1));
    }

    @Test
    @DisplayName("发布后状态变为 1")
    void testPublish() {
        CmsKbDocument doc = new CmsKbDocument();
        doc.setCategoryId(1L);
        doc.setTitle("待发布");
        doc.setDocType("2");
        doc.setNewContent("c");
        Long id = documentService.insert(doc);
        assertEquals(0, documentService.selectById(id).getStatus().intValue());

        documentService.publish(id);
        assertEquals(1, documentService.selectById(id).getStatus().intValue());
    }

    @Test
    @DisplayName("软删除后再查不到")
    void testSoftDelete() {
        CmsKbDocument doc = new CmsKbDocument();
        doc.setCategoryId(1L);
        doc.setTitle("待删除");
        doc.setDocType("2");
        doc.setNewContent("d");
        Long id = documentService.insert(doc);

        documentService.softDelete(new Long[]{id});
        CmsKbDocument after = documentService.selectById(id);
        assertNull(after);
    }
}
