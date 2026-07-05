package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.ruoyi.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.CmsKbDocument;
import com.ruoyi.system.domain.CmsKbDocumentVersion;
import com.ruoyi.system.mapper.CmsKbAttachmentMapper;
import com.ruoyi.system.mapper.CmsKbDocumentMapper;
import com.ruoyi.system.mapper.CmsKbDocumentVersionMapper;
import com.ruoyi.system.service.ICmsKbDocumentService;

/**
 * 知识库-文档Service业务层
 *
 * <p>关键业务规则：</p>
 * <ul>
 *   <li>新建文档自动生成 v1 版本，且文档状态=草稿(0)</li>
 *   <li>更新文档自动生成新版本（老版本 is_current=0）</li>
 *   <li>状态机：草稿(0) → 发布(1) → 下线(2)</li>
 * </ul>
 *
 * <p>说明：cms_kb_document 不保存 content（富文本存于 cms_kb_document_version）。
 * 当调用方在 CmsKbDocument 上额外设置 newContent 字段时，会作为新版本正文；
 * 否则继承当前版本的 content。</p>
 *
 * @author ruoyi
 * @date 2026-06-11
 */
@Service
public class CmsKbDocumentServiceImpl implements ICmsKbDocumentService {

    @Autowired
    private CmsKbDocumentMapper documentMapper;

    @Autowired
    private CmsKbDocumentVersionMapper versionMapper;

    @Autowired
    private CmsKbAttachmentMapper attachmentMapper;

    @Override
    public CmsKbDocument selectById(Long id) {
        return documentMapper.selectById(id);
    }

    @Override
    public List<CmsKbDocument> selectList(CmsKbDocument query) {
        return documentMapper.selectList(query);
    }

    @Override
    @Transactional
    public Long insert(CmsKbDocument doc) {
        doc.setStatus(0);
        doc.setDelFlag(0);
        doc.setViewCount(0L);
        doc.setCurrentVersion(1);
        documentMapper.insert(doc);

        CmsKbDocumentVersion v = new CmsKbDocumentVersion();
        v.setDocumentId(doc.getId());
        v.setVersionNo(1);
        v.setTitle(doc.getTitle());
        v.setContent(doc.getNewContent());
        v.setPrimaryFileId(doc.getPrimaryFileId());
        v.setSummary(doc.getSummary());
        v.setTags(doc.getTags());
        v.setSaveReason("自动");
        v.setIsCurrent(1);
        versionMapper.insert(v);
        return doc.getId();
    }

    @Override
    @Transactional
    public int update(CmsKbDocument doc) {
        CmsKbDocumentVersion current = versionMapper.selectCurrentByDoc(doc.getId());
        String carryContent = doc.getNewContent() != null
            ? doc.getNewContent()
            : (current != null ? current.getContent() : null);
        // 标记旧版本 is_current=0
        versionMapper.clearCurrent(doc.getId());
        int newVerNo = versionMapper.selectMaxVersionNo(doc.getId()) + 1;
        CmsKbDocumentVersion v = new CmsKbDocumentVersion();
        v.setDocumentId(doc.getId());
        v.setVersionNo(newVerNo);
        v.setTitle(doc.getTitle());
        v.setContent(carryContent);
        v.setPrimaryFileId(doc.getPrimaryFileId());
        v.setSummary(doc.getSummary());
        v.setTags(doc.getTags());
        v.setSaveReason("自动");
        v.setIsCurrent(1);
        versionMapper.insert(v);
        doc.setCurrentVersion(newVerNo);
        return documentMapper.update(doc);
    }

    @Override
    @Transactional
    public int publish(Long id) {
        CmsKbDocument doc = documentMapper.selectById(id);
        if (doc == null) {
            throw new ServiceException("文档不存在[code=KB_DOC_NOT_FOUND]");
        }
        return documentMapper.updateStatus(id, 1, new Date());
    }

    @Override
    public int setPinned(Long id, boolean pinned, String updateBy) {
        CmsKbDocument doc = documentMapper.selectById(id);
        if (doc == null) {
            throw new ServiceException("文档不存在[code=KB_DOC_NOT_FOUND]");
        }
        return documentMapper.updatePin(id, pinned, updateBy);
    }

    @Override
    @Transactional
    public int offline(Long id) {
        return documentMapper.updateStatus(id, 2, null);
    }

    @Override
    @Transactional
    public int softDelete(Long[] ids) {
        return documentMapper.softDelete(ids, new Date());
    }

    @Override
    @Transactional
    public int restore(Long[] ids) {
        return documentMapper.restoreByIds(ids);
    }

    @Override
    @Transactional
    public int hardDelete(Long[] ids) {
        return documentMapper.hardDelete(ids);
    }

    @Override
    public List<CmsKbDocument> selectRecycleList(CmsKbDocument query) {
        return documentMapper.selectRecycleList(query);
    }

    @Override
    @Transactional
    public int purgeExpired(Date cutoff) {
        // 1) 找回收站里的全部文档
        List<CmsKbDocument> expired = documentMapper.selectRecycleList(new CmsKbDocument());
        if (expired == null || expired.isEmpty()) {
            return 0;
        }
        // 2) 过滤 delete_time < cutoff
        List<Long> toDelete = new ArrayList<>();
        for (CmsKbDocument d : expired) {
            if (d.getDeleteTime() != null && d.getDeleteTime().before(cutoff)) {
                toDelete.add(d.getId());
            }
        }
        if (toDelete.isEmpty()) {
            return 0;
        }
        // 3) 物理删关联：version / attachment
        Long[] ids = toDelete.toArray(new Long[0]);
        for (Long id : ids) {
            versionMapper.hardDeleteByDocument(id);
            attachmentMapper.hardDeleteByDocument(id);
        }
        // 4) 物理删文档
        return documentMapper.hardDelete(ids);
    }
}
