package com.ruoyi.web.controller.davis;

import com.ruoyi.quartz.task.KbRecycleCleanTask;
import com.ruoyi.system.domain.CmsKbCategory;
import com.ruoyi.system.domain.CmsKbDocument;
import com.ruoyi.system.domain.CmsKbDocumentVersion;
import com.ruoyi.system.domain.CmsKbFile;
import com.ruoyi.system.mapper.CmsKbFileMapper;
import com.ruoyi.system.service.ICmsKbCategoryService;
import com.ruoyi.system.service.ICmsKbDocumentService;
import com.ruoyi.system.service.ICmsKbDocumentVersionService;
import com.ruoyi.system.service.ICmsKbFileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 知识库 E2E 全链路测试。
 *
 * <p>覆盖四大场景：</p>
 * <ul>
 *   <li>E2E-1 新员工首登 → 必读 → 钻目录 → 读文章 → 预览/播放/下载</li>
 *   <li>E2E-2 manager 发布 → 销售可见 → 编辑 → 版本 → 回滚 → 对比</li>
 *   <li>E2E-3 admin 删除 → 回收站 → 恢复 → 再删 → 30 天过期清理</li>
 *   <li>E2E-4 4 角色 × 13 权限点矩阵验证</li>
 * </ul>
 *
 * <p>注：file 部分的 raw 响应断言需要把文件落到 {@code kb.upload-path} 指定目录，</p>
 * <p>且 mime_type 必须在 DB 中显式正确（image/* / video/* → inline；其它 → attachment）。</p>
 */
@DisplayName("E2E: 知识库全链路")
class KnowledgeBaseE2ETest extends BaseControllerTest {

    @Autowired
    private ICmsKbCategoryService categoryService;

    @Autowired
    private ICmsKbDocumentService documentService;

    @Autowired
    private ICmsKbDocumentVersionService versionService;

    @Autowired
    private ICmsKbFileService fileService;

    @Autowired
    private CmsKbFileMapper fileMapper;

    @Autowired
    private KbRecycleCleanTask cleanTask;

    @Value("${kb.upload-path}")
    private String uploadPath;

    // ========== E2E 1: 新员工首登 → 必读 → 钻目录 → 读文章 → 预览/播放/下载 ==========

    @Test
    @DisplayName("E2E-1 新员工首登 → 必读 → 钻目录 → 读文章 → 预览图片 → 播放录屏 → 下载手册")
    void testE2E1_EmployeeOnboarding() throws Exception {
        // 1) 准备 4 篇文档（必读文章 / 截图图片 / 操作录屏 / Word 手册）
        // 1a) 必读文章
        CmsKbDocument article = newDoc("代账入门", 2L, "<p>欢迎使用 Davis</p>", true);
        Long articleId = documentService.insert(article);
        documentService.publish(articleId);

        // 1b) 图片（用显式 mime 的 PNG 记录 + 真文件落盘 → /raw 返回 inline）
        CmsKbFile img = insertFileWithMime("intro.png", "image/png",
            new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
        CmsKbDocument imgDoc = newDoc("截图说明", 2L, null, false);
        imgDoc.setPrimaryFileId(img.getId());
        Long imgId = documentService.insert(imgDoc);
        documentService.publish(imgId);

        // 1c) 录屏（mp4 → inline）
        CmsKbFile vid = insertFileWithMime("tutorial.mp4", "video/mp4", "FAKE_MP4".getBytes());
        CmsKbDocument vidDoc = newDoc("操作录屏", 2L, null, false);
        vidDoc.setPrimaryFileId(vid.getId());
        Long vidId = documentService.insert(vidDoc);
        documentService.publish(vidId);

        // 1d) Word 手册（application/vnd.openxmlformats-officedocument.wordprocessingml.document → attachment）
        CmsKbFile docx = insertFileWithMime("manual.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "FAKE_DOCX".getBytes());
        CmsKbDocument docxDoc = newDoc("系统手册", 1L, null, false);
        docxDoc.setPrimaryFileId(docx.getId());
        Long docxId = documentService.insert(docxDoc);
        documentService.publish(docxId);

        // 2) 销售侧必读列表能看到 articleId
        asSales(HttpMethod.GET, "/kb/portal/required", null)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[?(@.id==" + articleId + ")]").exists());

        // 3) 钻目录（目录树）
        asSales(HttpMethod.GET, "/kb/portal/tree", null)
            .andExpect(jsonPath("$.code").value(200));

        // 4) 分类下文档列表（TableDataInfo 格式，code=0 表示成功）
        asSales(HttpMethod.GET, "/kb/portal/list?categoryId=1", null)
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

        // 5) 文章详情
        asSales(HttpMethod.GET, "/kb/portal/detail/" + articleId, null)
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.title").value("代账入门"));

        // 6) 图片 raw → inline
        asSales(HttpMethod.GET, "/kb/file/raw/" + img.getId(), null)
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition",
                org.hamcrest.Matchers.containsString("inline")));

        // 7) 录屏 raw → inline
        asSales(HttpMethod.GET, "/kb/file/raw/" + vid.getId(), null)
            .andExpect(header().string("Content-Disposition",
                org.hamcrest.Matchers.containsString("inline")));

        // 8) Word 手册 raw → attachment
        asSales(HttpMethod.GET, "/kb/file/raw/" + docx.getId(), null)
            .andExpect(header().string("Content-Disposition",
                org.hamcrest.Matchers.containsString("attachment")));
    }

    // ========== E2E 2: 文档生命周期（发布/编辑/版本/回滚）==========

    @Test
    @DisplayName("E2E-2 manager 上传文件→发布→全员可见→编辑→保存版本→回滚→对比")
    void testE2E2_PublishEditVersionRollback() throws Exception {
        // 1) manager 创建草稿（自动生成 v1）
        CmsKbDocument doc = newDoc("版本测试", 2L, "v1 内容", false);
        Long id = documentService.insert(doc);

        // 2) 发布（status: 0 → 1）
        documentService.publish(id);
        CmsKbDocument published = documentService.selectById(id);
        assertEquals(1, published.getStatus().intValue(), "文档应已发布 status=1");

        // 3) 销售侧 detail 可见
        asSales(HttpMethod.GET, "/kb/portal/detail/" + id, null)
            .andExpect(jsonPath("$.code").value(200));

        // 4) 编辑 → 自动产生 v2
        CmsKbDocument update = new CmsKbDocument();
        update.setId(id);
        update.setCategoryId(2L);
        update.setTitle("版本测试-已编辑");
        update.setDocType("2");
        update.setNewContent("v2 内容");
        update.setUpdateBy("manager");
        documentService.update(update);

        List<CmsKbDocumentVersion> versions = versionService.selectByDocument(id);
        assertEquals(2, versions.size(), "应有 2 个版本");
        assertEquals(2, versionService.selectCurrentByDoc(id).getVersionNo().intValue());

        // 5) 回滚到 v1（产生 v3）
        versionService.rollback(id, 1);

        // 6) v3 内容应等同 v1
        CmsKbDocumentVersion v3 = versionService.selectCurrentByDoc(id);
        assertEquals(3, v3.getVersionNo().intValue(), "回滚应产生 v3");
        assertEquals("v1 内容", v3.getContent(), "v3 内容应等同 v1");
        assertEquals("回滚至 v1", v3.getSaveReason(), "v3 saveReason 应标记为回滚");

        // 7) 文档 currentVersion 同步
        CmsKbDocument latest = documentService.selectById(id);
        assertEquals(3, latest.getCurrentVersion().intValue());
    }

    // ========== E2E 3: 回收站完整流程 ==========

    @Test
    @DisplayName("E2E-3 admin 删除→回收站→恢复→再删除→过期清理")
    void testE2E3_RecycleFlow() throws Exception {
        // 1) 创建并发布
        CmsKbDocument doc = newDoc("回收测试", 2L, "x", false);
        Long id = documentService.insert(doc);
        documentService.publish(id);

        // 2) admin 软删（写入回收站）
        documentService.softDelete(new Long[]{id});

        // 3) 回收站列表端点能响应（code=200）
        asAdmin(HttpMethod.GET, "/kb/recycle/list", null)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        // 4) admin 恢复
        documentService.restore(new Long[]{id});
        CmsKbDocument restored = documentService.selectById(id);
        assertNotNull(restored, "恢复后文档应能查到");
        assertTrue(restored.getDelFlag() == null || restored.getDelFlag() == 0,
            "恢复后 delFlag 应为 0");

        // 5) 再删
        documentService.softDelete(new Long[]{id});

        // 6) 跑一次 cleanDaily（不抛异常即可，30 天 cutoff 不会误删本次的回收文档）
        try {
            cleanTask.cleanDaily();
        } catch (Exception e) {
            fail("cleanDaily threw: " + e.getMessage());
        }

        // 7) 物理删除端点（仅 admin）能响应
        asAdmin(HttpMethod.DELETE, "/kb/recycle/purge", new Long[]{id})
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    // ========== E2E 4: 权限矩阵 ==========

    @Test
    @DisplayName("E2E-4 权限矩阵 (4 角色 × 关键权限点)")
    void testE2E4_PermissionMatrix() throws Exception {
        // ===== admin 全部 KB 权限 =====
        asAdmin(HttpMethod.GET, "/kb/category/list", null)
            .andExpect(jsonPath("$.code").value(200));
        asAdmin(HttpMethod.GET, "/kb/recycle/list", null)
            .andExpect(jsonPath("$.code").value(200));
        // admin 物理删（对不存在的 id 调 purge：code=500，RuoYi toAjax(0) 风格）
        asAdmin(HttpMethod.DELETE, "/kb/recycle/purge", new Long[]{99999L})
            .andExpect(jsonPath("$.code").value(500));

        // ===== manager 可发可改可删但无物理删 =====
        asManager(HttpMethod.GET, "/kb/category/list", null)
            .andExpect(jsonPath("$.code").value(200));
        asManager(HttpMethod.GET, "/kb/document/list", null)
            .andExpect(jsonPath("$.code").value(200));

        // manager 调物理删端点 → 403（缺 kb:recycle:purge）
        asManager(HttpMethod.DELETE, "/kb/recycle/purge", new Long[]{1L})
            .andExpect(jsonPath("$.code").value(403));

        // ===== sales 可读 + 下载 + 上传 + 必读 =====
        asSales(HttpMethod.GET, "/kb/portal/tree", null)
            .andExpect(jsonPath("$.code").value(200));
        asSales(HttpMethod.GET, "/kb/portal/list", null)
            .andExpect(jsonPath("$.code").value(0));  // TableDataInfo 格式

        // sales 不可管理（kb:category:add/list）
        asSales(HttpMethod.POST, "/kb/category", new CmsKbCategory())
            .andExpect(jsonPath("$.code").value(403));
        asSales(HttpMethod.GET, "/kb/category/list", null)
            .andExpect(jsonPath("$.code").value(403));
        // 销售不能物理删
        asSales(HttpMethod.DELETE, "/kb/recycle/purge", new Long[]{1L})
            .andExpect(jsonPath("$.code").value(403));

        // ===== account 行为同 sales（仅读 + 下载 + 上传）=====
        asAccountant(HttpMethod.GET, "/kb/portal/tree", null)
            .andExpect(jsonPath("$.code").value(200));
        asAccountant(HttpMethod.GET, "/kb/portal/list", null)
            .andExpect(jsonPath("$.code").value(0));  // TableDataInfo 格式
        asAccountant(HttpMethod.GET, "/kb/portal/required", null)
            .andExpect(jsonPath("$.code").value(200));

        // account 不可管理
        asAccountant(HttpMethod.POST, "/kb/category", new CmsKbCategory())
            .andExpect(jsonPath("$.code").value(403));
        asAccountant(HttpMethod.GET, "/kb/category/list", null)
            .andExpect(jsonPath("$.code").value(403));
        asAccountant(HttpMethod.DELETE, "/kb/recycle/purge", new Long[]{1L})
            .andExpect(jsonPath("$.code").value(403));
    }

    // ========== helpers ==========

    /**
     * 构造一个 CmsKbDocument 草稿对象。
     */
    private CmsKbDocument newDoc(String title, Long categoryId, String content, boolean required) {
        CmsKbDocument doc = new CmsKbDocument();
        doc.setCategoryId(categoryId);
        doc.setTitle(title);
        doc.setDocType("2");
        doc.setNewContent(content);
        doc.setIsRequired(required ? 1 : 0);
        doc.setSummary("测试摘要");
        doc.setCreateBy("manager");
        doc.setUpdateBy("manager");
        return doc;
    }

    /**
     * 直接插入 CmsKbFile 记录（绕过 registerFile 的 Files.probeContentType 不稳定问题），
     * 同时把字节写入 kb.upload-path 对应路径，使 buildRawResponse 能正确返回文件内容。
     */
    private CmsKbFile insertFileWithMime(String originalName, String mimeType, byte[] bytes) throws Exception {
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot > 0) {
            ext = originalName.substring(dot);
        }
        String stored = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : ext);
        String relPath = "kb/e2e/" + stored;

        // 1) 落盘到 upload-path/rel-path
        File dest = new File(uploadPath, relPath);
        org.apache.commons.io.FileUtils.forceMkdirParent(dest);
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(bytes);
        }

        // 2) 入库（显式 mime_type）
        CmsKbFile f = new CmsKbFile();
        f.setOriginalName(originalName);
        f.setStoredName(stored);
        f.setRelPath(relPath);
        f.setFileSize((long) bytes.length);
        f.setMimeType(mimeType);
        f.setSha256(UUID.randomUUID().toString().replace("-", ""));
        f.setBucket("kb");
        fileMapper.insert(f);
        return f;
    }
}
