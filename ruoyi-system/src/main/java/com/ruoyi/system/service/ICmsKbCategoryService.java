package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.CmsKbCategory;

/**
 * 知识库-目录Service接口
 *
 * @author ruoyi
 * @date 2026-06-11
 */
public interface ICmsKbCategoryService {
    /**
     * 根据ID查询
     */
    CmsKbCategory selectById(Long id);

    /**
     * 条件查询列表
     */
    List<CmsKbCategory> selectList(CmsKbCategory query);

    /**
     * 查询某父节点下的子目录
     */
    List<CmsKbCategory> selectChildren(Long parentId);

    /**
     * 查询所有有效目录
     */
    List<CmsKbCategory> selectAll();

    /**
     * 新增目录
     */
    int insert(CmsKbCategory category);

    /**
     * 更新目录（含循环引用校验）
     */
    int update(CmsKbCategory category);

    /**
     * 调整排序号（支持父节点变更）
     */
    int updateOrder(Long id, Long parentId, Integer orderNum);

    /**
     * 批量逻辑删除（拒绝有子节点/有文档的目录）
     */
    int deleteByIds(Long[] ids);

    /**
     * 查询必学目录
     */
    List<CmsKbCategory> selectRequired();
}
