package com.ruoyi.web.controller.davis;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.service.ICmsKbDocumentVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识库-文档版本Controller
 *
 * @author ruoyi
 * @date 2026-06-11
 */
@RestController
@RequestMapping("/kb/version")
public class CmsKbVersionController extends BaseController {

    @Autowired
    private ICmsKbDocumentVersionService versionService;

    /**
     * 查询某文档的全部版本
     */
    @PreAuthorize("@ss.hasPermi('kb:version:list')")
    @GetMapping("/{docId}")
    public AjaxResult list(@PathVariable("docId") Long docId) {
        return success(versionService.selectByDocument(docId));
    }

    /**
     * 查询某文档的指定版本详情
     */
    @PreAuthorize("@ss.hasPermi('kb:version:list')")
    @GetMapping("/{docId}/{ver}")
    public AjaxResult detail(@PathVariable("docId") Long docId,
                              @PathVariable("ver") Integer ver) {
        return success(versionService.selectByDocAndVer(docId, ver));
    }

    /**
     * 回滚到指定版本（产生新版本，保留历史链路）
     */
    @PreAuthorize("@ss.hasPermi('kb:version:rollback')")
    @PostMapping("/{docId}/{ver}/rollback")
    public AjaxResult rollback(@PathVariable("docId") Long docId,
                                @PathVariable("ver") Integer ver) {
        return toAjax(versionService.rollback(docId, ver));
    }
}
