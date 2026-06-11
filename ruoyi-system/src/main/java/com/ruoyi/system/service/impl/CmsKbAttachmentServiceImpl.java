package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.CmsKbAttachment;
import com.ruoyi.system.mapper.CmsKbAttachmentMapper;
import com.ruoyi.system.service.ICmsKbAttachmentService;

/**
 * 知识库-文档附件Service业务层
 *
 * @author ruoyi
 * @date 2026-06-11
 */
@Service
public class CmsKbAttachmentServiceImpl implements ICmsKbAttachmentService {

    @Autowired
    private CmsKbAttachmentMapper attachmentMapper;

    @Override
    public List<CmsKbAttachment> selectByDocument(Long documentId) {
        return attachmentMapper.selectByDocument(documentId);
    }

    @Override
    public int insert(CmsKbAttachment att) {
        return attachmentMapper.insert(att);
    }

    @Override
    @Transactional
    public int deleteById(Long id) {
        return attachmentMapper.deleteById(id);
    }

    @Override
    @Transactional
    public int hardDeleteByDocument(Long documentId) {
        return attachmentMapper.hardDeleteByDocument(documentId);
    }
}
