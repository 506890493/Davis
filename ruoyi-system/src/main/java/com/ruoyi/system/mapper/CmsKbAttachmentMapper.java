package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.CmsKbAttachment;
import org.apache.ibatis.annotations.Param;

/**
 * 知识库-文档附件Mapper接口
 *
 * @author ruoyi
 * @date 2026-06-11
 */
public interface CmsKbAttachmentMapper {

    /**
     * 查询某文档的全部附件
     */
    List<CmsKbAttachment> selectByDocument(@Param("documentId") Long documentId);

    /**
     * 新增附件
     */
    int insert(CmsKbAttachment att);

    /**
     * 根据ID删除附件
     */
    int deleteById(@Param("id") Long id);

    /**
     * 物理删除某文档的全部附件
     */
    int hardDeleteByDocument(@Param("documentId") Long documentId);
}
