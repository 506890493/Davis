package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.CmsKbDocument;
import com.ruoyi.system.domain.CmsKbDocumentVersion;
import com.ruoyi.system.mapper.CmsKbDocumentMapper;
import com.ruoyi.system.mapper.CmsKbDocumentVersionMapper;
import com.ruoyi.system.service.ICmsKbDocumentVersionService;

/**
 * 知识库-文档版本Service业务层
 *
 * <p>回滚采用"产生新版本"策略：基于目标版本内容创建一条新版本号，
 * 保留完整历史链路，不直接破坏当前版本记录。</p>
 *
 * @author ruoyi
 * @date 2026-06-11
 */
@Service
public class CmsKbDocumentVersionServiceImpl implements ICmsKbDocumentVersionService {

    @Autowired
    private CmsKbDocumentVersionMapper versionMapper;

    @Autowired
    private CmsKbDocumentMapper documentMapper;

    @Override
    public CmsKbDocumentVersion selectCurrentByDoc(Long documentId) {
        return versionMapper.selectCurrentByDoc(documentId);
    }

    @Override
    public List<CmsKbDocumentVersion> selectByDocument(Long documentId) {
        return versionMapper.selectByDocument(documentId);
    }

    @Override
    public CmsKbDocumentVersion selectByDocAndVer(Long documentId, Integer versionNo) {
        return versionMapper.selectByDocAndVer(documentId, versionNo);
    }

    @Override
    @Transactional
    public int rollback(Long documentId, Integer targetVersion) {
        CmsKbDocumentVersion target = versionMapper.selectByDocAndVer(documentId, targetVersion);
        if (target == null) {
            throw new ServiceException("目标版本不存在[code=KB_VERSION_NOT_FOUND]");
        }
        CmsKbDocument doc = documentMapper.selectById(documentId);
        if (doc == null) {
            throw new ServiceException("文档不存在[code=KB_DOC_NOT_FOUND]");
        }
        if (doc.getCurrentVersion() != null && doc.getCurrentVersion().equals(targetVersion)) {
            throw new ServiceException("已是当前版本，无需回滚[code=KB_VERSION_CURRENT]");
        }
        // 标记所有旧版本 is_current=0
        versionMapper.clearCurrent(documentId);
        int newVerNo = versionMapper.selectMaxVersionNo(documentId) + 1;
        CmsKbDocumentVersion v = new CmsKbDocumentVersion();
        v.setDocumentId(documentId);
        v.setVersionNo(newVerNo);
        v.setTitle(target.getTitle());
        v.setContent(target.getContent());
        v.setPrimaryFileId(target.getPrimaryFileId());
        v.setSummary(target.getSummary());
        v.setTags(target.getTags());
        v.setSaveReason("回滚至 v" + targetVersion);
        v.setIsCurrent(1);
        versionMapper.insert(v);
        documentMapper.updateCurrentVersion(documentId, newVerNo);
        return 1;
    }
}
