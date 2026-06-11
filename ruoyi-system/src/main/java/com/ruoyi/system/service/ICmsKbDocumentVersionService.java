package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.CmsKbDocumentVersion;

/**
 * 知识库-文档版本Service接口
 *
 * @author ruoyi
 * @date 2026-06-11
 */
public interface ICmsKbDocumentVersionService {
    /**
     * 查询某文档的当前版本
     */
    CmsKbDocumentVersion selectCurrentByDoc(Long documentId);

    /**
     * 查询某文档的全部版本
     */
    List<CmsKbDocumentVersion> selectByDocument(Long documentId);

    /**
     * 根据文档ID + 版本号查询
     */
    CmsKbDocumentVersion selectByDocAndVer(Long documentId, Integer versionNo);

    /**
     * 回滚：基于目标版本内容产生一个新版本
     */
    int rollback(Long documentId, Integer targetVersion);
}
