package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.CmsKbCategory;
import com.ruoyi.system.domain.CmsKbDocument;

/**
 * 知识库-阅读端Service接口（聚合查询）
 *
 * @author ruoyi
 * @date 2026-06-11
 */
public interface ICmsKbPortalService {
    /**
     * 获取目录树（全量已启用目录）
     */
    List<CmsKbCategory> getTree();

    /**
     * 分页查询某目录下已发布文档
     */
    TableDataInfo listPublished(Long categoryId, Integer pageNum, Integer pageSize);

    /**
     * 获取文档详情（仅已发布）
     */
    CmsKbDocument getDetail(Long id);

    /**
     * 必读列表
     */
    List<CmsKbDocument> listRequired(Integer limit);

    /**
     * 搜索（按标题模糊匹配，仅已发布）
     */
    TableDataInfo search(String keyword, Integer pageNum, Integer pageSize);
}
