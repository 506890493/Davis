package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.CmsKbAttachment;

/**
 * 知识库-文档附件Service接口
 *
 * @author ruoyi
 * @date 2026-06-11
 */
public interface ICmsKbAttachmentService {
    /**
     * 查询某文档的全部附件
     */
    List<CmsKbAttachment> selectByDocument(Long documentId);

    /**
     * 新增附件
     */
    int insert(CmsKbAttachment att);

    /**
     * 根据ID删除附件
     */
    int deleteById(Long id);

    /**
     * 物理删除某文档的全部附件
     */
    int hardDeleteByDocument(Long documentId);
}
