package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.CmsKbDocument;

/**
 * 知识库-文档Service接口
 *
 * @author ruoyi
 * @date 2026-06-11
 */
public interface ICmsKbDocumentService {
    /**
     * 根据ID查询
     */
    CmsKbDocument selectById(Long id);

    /**
     * 条件查询列表
     */
    List<CmsKbDocument> selectList(CmsKbDocument query);

    /**
     * 新增文档（自动创建 v1）
     */
    Long insert(CmsKbDocument doc);

    /**
     * 更新文档（自动创建新版本）
     */
    int update(CmsKbDocument doc);

    /**
     * 发布文档
     */
    int publish(Long id);

    /**
     * 下线文档
     */
    int offline(Long id);

    /**
     * 批量软删除（写入回收站）
     */
    int softDelete(Long[] ids);
}
