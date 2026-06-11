package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.CmsKbCategory;
import com.ruoyi.system.mapper.CmsKbCategoryMapper;
import com.ruoyi.system.service.ICmsKbCategoryService;

/**
 * 知识库-目录Service业务层
 *
 * <p>关键业务规则：</p>
 * <ul>
 *   <li>循环引用：不能将目录移至自身或自身后代下</li>
 *   <li>删除保护：有子目录或有文档的目录不可删除</li>
 * </ul>
 *
 * @author ruoyi
 * @date 2026-06-11
 */
@Service
public class CmsKbCategoryServiceImpl implements ICmsKbCategoryService {

    @Autowired
    private CmsKbCategoryMapper categoryMapper;

    @Override
    public CmsKbCategory selectById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public List<CmsKbCategory> selectList(CmsKbCategory query) {
        return categoryMapper.selectList(query);
    }

    @Override
    public List<CmsKbCategory> selectChildren(Long parentId) {
        return categoryMapper.selectChildren(parentId);
    }

    @Override
    public List<CmsKbCategory> selectAll() {
        return categoryMapper.selectAll();
    }

    @Override
    @Transactional
    public int insert(CmsKbCategory category) {
        if (category.getOrderNum() == null) {
            category.setOrderNum(0);
        }
        return categoryMapper.insert(category);
    }

    @Override
    @Transactional
    public int update(CmsKbCategory category) {
        if (category.getParentId() != null && category.getId() != null) {
            if (category.getParentId().equals(category.getId())) {
                throw new ServiceException("不能把目录移动到自身下[code=KB_CATEGORY_LOOP]");
            }
            if (isDescendant(category.getParentId(), category.getId())) {
                throw new ServiceException("不能把目录移动到自己的子目录下[code=KB_CATEGORY_LOOP]");
            }
        }
        return categoryMapper.update(category);
    }

    /**
     * 判断 candidate 是否为 ancestor 的后代（沿 parent_id 链向上追溯）。
     */
    private boolean isDescendant(Long candidate, Long ancestor) {
        Long pid = candidate;
        while (pid != null && pid != 0L) {
            CmsKbCategory c = categoryMapper.selectById(pid);
            if (c == null) {
                return false;
            }
            if (c.getId().equals(ancestor)) {
                return true;
            }
            pid = c.getParentId();
        }
        return false;
    }

    @Override
    @Transactional
    public int updateOrder(Long id, Long parentId, Integer orderNum) {
        return categoryMapper.updateOrderNum(id, parentId, orderNum);
    }

    @Override
    @Transactional
    public int deleteByIds(Long[] ids) {
        for (Long id : ids) {
            if (categoryMapper.countChildren(id) > 0) {
                throw new ServiceException("目录[id=" + id + "]下还有子目录，请先清空[code=KB_CATEGORY_NOT_EMPTY]");
            }
            if (categoryMapper.countDocuments(id) > 0) {
                throw new ServiceException("目录[id=" + id + "]下还有文档，请先清空[code=KB_CATEGORY_NOT_EMPTY]");
            }
        }
        return categoryMapper.deleteByIds(ids);
    }

    @Override
    public List<CmsKbCategory> selectRequired() {
        return categoryMapper.selectRequired();
    }
}
