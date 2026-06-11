package com.ruoyi.web.controller.davis;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.CmsKbDocument;
import com.ruoyi.system.service.ICmsKbPortalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识库-阅读端Controller
 *
 * <p>所有读端接口强制仅 status=1（已发布）的文档可见，避免草稿/下线内容泄露。</p>
 *
 * @author ruoyi
 * @date 2026-06-11
 */
@RestController
@RequestMapping("/kb/portal")
public class CmsKbPortalController extends BaseController {

    @Autowired
    private ICmsKbPortalService portalService;

    /**
     * 目录树
     */
    @PreAuthorize("@ss.hasPermi('kb:portal:view')")
    @GetMapping("/tree")
    public AjaxResult tree() {
        return success(portalService.getTree());
    }

    /**
     * 某目录下已发布文档分页
     */
    @PreAuthorize("@ss.hasPermi('kb:portal:view')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(required = false) Long categoryId,
                               @RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return portalService.listPublished(categoryId, pageNum, pageSize);
    }

    /**
     * 文档详情（仅已发布）
     */
    @PreAuthorize("@ss.hasPermi('kb:portal:view')")
    @GetMapping("/detail/{id}")
    public AjaxResult detail(@PathVariable("id") Long id) {
        CmsKbDocument doc = portalService.getDetail(id);
        if (doc == null) {
            return AjaxResult.error("文档不存在或未发布");
        }
        return success(doc);
    }

    /**
     * 必读列表
     */
    @PreAuthorize("@ss.hasPermi('kb:portal:required')")
    @GetMapping("/required")
    public AjaxResult required(@RequestParam(defaultValue = "10") Integer limit) {
        return success(portalService.listRequired(limit));
    }

    /**
     * 标题模糊搜索（仅已发布）
     */
    @PreAuthorize("@ss.hasPermi('kb:portal:view')")
    @GetMapping("/search")
    public TableDataInfo search(@RequestParam String keyword,
                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        return portalService.search(keyword, pageNum, pageSize);
    }
}
