package com.ruoyi.web.controller.davis;

import java.util.List;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.CmsKbCategory;
import com.ruoyi.system.service.ICmsKbCategoryService;
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
 * 知识库-目录Controller
 *
 * @author ruoyi
 * @date 2026-06-11
 */
@RestController
@RequestMapping("/kb/category")
public class CmsKbCategoryController extends BaseController {

    @Autowired
    private ICmsKbCategoryService categoryService;

    /**
     * 查询目录列表
     */
    @PreAuthorize("@ss.hasPermi('kb:category:list')")
    @GetMapping("/list")
    public TableDataInfo list(CmsKbCategory query) {
        startPage();
        return getDataTable(categoryService.selectList(query));
    }

    /**
     * 获取目录详细信息
     */
    @PreAuthorize("@ss.hasPermi('kb:category:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(categoryService.selectById(id));
    }

    /**
     * 新增目录
     */
    @PreAuthorize("@ss.hasPermi('kb:category:add')")
    @Log(title = "知识库目录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CmsKbCategory category) {
        category.setCreateBy(getUsername());
        category.setUpdateBy(getUsername());
        return toAjax(categoryService.insert(category));
    }

    /**
     * 修改目录
     */
    @PreAuthorize("@ss.hasPermi('kb:category:edit')")
    @Log(title = "知识库目录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CmsKbCategory category) {
        category.setUpdateBy(getUsername());
        return toAjax(categoryService.update(category));
    }

    /**
     * 拖拽排序（批量更新父节点与排序号）
     */
    @PreAuthorize("@ss.hasPermi('kb:category:edit')")
    @PutMapping("/order")
    public AjaxResult order(@RequestBody List<CmsKbCategory> list) {
        for (CmsKbCategory c : list) {
            categoryService.updateOrder(c.getId(), c.getParentId(), c.getOrderNum());
        }
        return success();
    }

    /**
     * 删除目录
     */
    @PreAuthorize("@ss.hasPermi('kb:category:remove')")
    @Log(title = "知识库目录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(categoryService.deleteByIds(ids));
    }
}
