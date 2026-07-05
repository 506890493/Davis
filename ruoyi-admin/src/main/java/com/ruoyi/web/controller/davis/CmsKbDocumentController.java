package com.ruoyi.web.controller.davis;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.CmsKbDocument;
import com.ruoyi.system.domain.CmsKbDocumentVersion;
import com.ruoyi.system.mapper.CmsKbDocumentVersionMapper;
import com.ruoyi.system.service.ICmsKbDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识库-文档Controller
 *
 * @author ruoyi
 * @date 2026-06-11
 */
@RestController
@RequestMapping("/kb/document")
public class CmsKbDocumentController extends BaseController {

    @Autowired
    private ICmsKbDocumentService documentService;

    @Autowired
    private CmsKbDocumentVersionMapper versionMapper;

    /**
     * 查询文档列表
     */
    @PreAuthorize("@ss.hasPermi('kb:document:list')")
    @GetMapping("/list")
    public TableDataInfo list(CmsKbDocument query) {
        startPage();
        return getDataTable(documentService.selectList(query));
    }

    /**
     * 获取文档详细信息（含当前版本正文，供编辑弹窗渲染）
     */
    @PreAuthorize("@ss.hasPermi('kb:document:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        CmsKbDocument doc = documentService.selectById(id);
        if (doc != null) {
            CmsKbDocumentVersion ver = versionMapper.selectCurrentByDoc(id);
            if (ver != null && ver.getContent() != null) {
                doc.setNewContent(ver.getContent());
            }
        }
        return success(doc);
    }

    /**
     * 新增文档（自动生成 v1 版本，状态=草稿）
     */
    @PreAuthorize("@ss.hasPermi('kb:document:add')")
    @Log(title = "知识库文档", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CmsKbDocument doc) {
        doc.setCreateBy(getUsername());
        doc.setUpdateBy(getUsername());
        Long id = documentService.insert(doc);
        return success(id);
    }

    /**
     * 修改文档（自动生成新版本）
     */
    @PreAuthorize("@ss.hasPermi('kb:document:edit')")
    @Log(title = "知识库文档", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CmsKbDocument doc) {
        doc.setUpdateBy(getUsername());
        return toAjax(documentService.update(doc));
    }

    /**
     * 软删除文档（进入回收站）
     */
    @PreAuthorize("@ss.hasPermi('kb:document:remove')")
    @Log(title = "知识库文档", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(documentService.softDelete(ids));
    }

    /**
     * 发布文档
     */
    @PreAuthorize("@ss.hasPermi('kb:document:publish')")
    @Log(title = "知识库文档", businessType = BusinessType.UPDATE)
    @PutMapping("/publish")
    public AjaxResult publish(@RequestBody CmsKbDocument doc) {
        return toAjax(documentService.publish(doc.getId()));
    }

    /**
     * 下线文档
     */
    @PreAuthorize("@ss.hasPermi('kb:document:publish')")
    @Log(title = "知识库文档", businessType = BusinessType.UPDATE)
    @PutMapping("/offline")
    public AjaxResult offline(@RequestBody CmsKbDocument doc) {
        return toAjax(documentService.offline(doc.getId()));
    }
}
