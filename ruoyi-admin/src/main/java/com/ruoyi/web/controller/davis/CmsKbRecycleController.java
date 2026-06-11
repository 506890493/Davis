package com.ruoyi.web.controller.davis;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.CmsKbDocument;
import com.ruoyi.system.service.ICmsKbDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识库-回收站Controller
 *
 * @author ruoyi
 * @date 2026-06-11
 */
@RestController
@RequestMapping("/kb/recycle")
public class CmsKbRecycleController extends BaseController {

    @Autowired
    private ICmsKbDocumentService documentService;

    /**
     * 回收站文档列表（仅 del_flag=1）
     */
    @PreAuthorize("@ss.hasPermi('kb:recycle:list')")
    @GetMapping("/list")
    public TableDataInfo list(CmsKbDocument query) {
        startPage();
        return getDataTable(documentService.selectRecycleList(query));
    }

    /**
     * 恢复软删文档
     */
    @PreAuthorize("@ss.hasPermi('kb:recycle:restore')")
    @PostMapping("/restore")
    public AjaxResult restore(@RequestBody Long[] ids) {
        return toAjax(documentService.restore(ids));
    }

    /**
     * 物理删除（仅 admin）
     */
    @PreAuthorize("@ss.hasPermi('kb:recycle:purge')")
    @DeleteMapping("/purge")
    public AjaxResult purge(@RequestBody Long[] ids) {
        return toAjax(documentService.hardDelete(ids));
    }
}
