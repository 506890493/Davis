package com.ruoyi.system.service.impl;

import java.util.List;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ruoyi.common.core.page.TableDataInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.CmsKbCategory;
import com.ruoyi.system.domain.CmsKbDocument;
import com.ruoyi.system.domain.CmsKbDocumentVersion;
import com.ruoyi.system.mapper.CmsKbCategoryMapper;
import com.ruoyi.system.mapper.CmsKbDocumentMapper;
import com.ruoyi.system.mapper.CmsKbDocumentVersionMapper;
import com.ruoyi.system.service.ICmsKbDocumentVersionService;
import com.ruoyi.system.service.ICmsKbPortalService;

/**
 * 知识库-阅读端Service实现（聚合查询）
 *
 * <p>所有读端接口强制 status=1（仅已发布），
 * 避免在阅读端泄露草稿/下线内容。</p>
 *
 * @author ruoyi
 * @date 2026-06-11
 */
@Service
public class CmsKbPortalServiceImpl implements ICmsKbPortalService {

    @Autowired
    private CmsKbCategoryMapper categoryMapper;

    @Autowired
    private CmsKbDocumentMapper documentMapper;

    @Autowired
    private CmsKbDocumentVersionMapper versionMapper;

    @Autowired
    private ICmsKbDocumentVersionService versionService;

    @Override
    public List<CmsKbCategory> getTree() {
        return categoryMapper.selectAll();
    }

    @Override
    public TableDataInfo listPublished(Long categoryId, Integer pageNum, Integer pageSize) {
        CmsKbDocument q = new CmsKbDocument();
        q.setStatus(1);
        if (categoryId != null) {
            q.setCategoryId(categoryId);
        }
        int pn = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int ps = pageSize == null || pageSize < 1 ? 10 : pageSize;
        PageHelper.startPage(pn, ps);
        List<CmsKbDocument> list = documentMapper.selectList(q);
        long total = list instanceof Page ? ((Page<?>) list).getTotal() : list.size();
        return new TableDataInfo(list, total);
    }

    @Override
    public CmsKbDocument getDetail(Long id) {
        CmsKbDocument doc = documentMapper.selectById(id);
        if (doc == null || doc.getStatus() == null || doc.getStatus() != 1) {
            return null;
        }
        // 补充当前版本的正文（前端 v-html 直接渲染此字段）
        CmsKbDocumentVersion ver = versionMapper.selectCurrentByDoc(id);
        if (ver != null && ver.getContent() != null) {
            doc.setNewContent(ver.getContent());
        }
        return doc;
    }

    @Override
    public List<CmsKbDocument> listRequired(Integer limit) {
        CmsKbDocument q = new CmsKbDocument();
        q.setStatus(1);
        q.setIsRequired(1);
        int n = limit == null || limit < 1 ? 10 : limit;
        PageHelper.startPage(1, n);
        return documentMapper.selectList(q);
    }

    @Override
    public TableDataInfo search(String keyword, Integer pageNum, Integer pageSize) {
        CmsKbDocument q = new CmsKbDocument();
        q.setStatus(1);
        q.setTitle(keyword);
        int pn = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int ps = pageSize == null || pageSize < 1 ? 10 : pageSize;
        PageHelper.startPage(pn, ps);
        // 搜索结果不应用置顶排序（按 create_time DESC，避免置顶文档在搜索中插队）
        List<CmsKbDocument> list = documentMapper.selectListForSearch(q);
        long total = list instanceof Page ? ((Page<?>) list).getTotal() : list.size();
        return new TableDataInfo(list, total);
    }
}
