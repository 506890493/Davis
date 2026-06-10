package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.CmsKbDocumentVersion;
import org.apache.ibatis.annotations.Param;

/**
 * 知识库-文档版本Mapper接口
 *
 * @author ruoyi
 * @date 2026-06-11
 */
public interface CmsKbDocumentVersionMapper {

    /**
     * 根据文档ID + 版本号查询
     */
    CmsKbDocumentVersion selectByDocAndVer(@Param("documentId") Long documentId,
                                           @Param("versionNo") Integer versionNo);

    /**
     * 查询文档的当前版本
     */
    CmsKbDocumentVersion selectCurrentByDoc(@Param("documentId") Long documentId);

    /**
     * 查询某文档的全部版本
     */
    List<CmsKbDocumentVersion> selectByDocument(@Param("documentId") Long documentId);

    /**
     * 查询某文档当前最大版本号
     */
    int selectMaxVersionNo(@Param("documentId") Long documentId);

    /**
     * 新增版本
     */
    int insert(CmsKbDocumentVersion v);

    /**
     * 清除某文档的当前版本标记
     */
    int clearCurrent(@Param("documentId") Long documentId);

    /**
     * 物理删除某文档的全部版本
     */
    int hardDeleteByDocument(@Param("documentId") Long documentId);
}
