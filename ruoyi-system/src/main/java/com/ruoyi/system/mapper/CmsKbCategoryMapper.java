package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.CmsKbCategory;
import org.apache.ibatis.annotations.Param;

/**
 * 知识库-目录Mapper接口
 *
 * @author ruoyi
 * @date 2026-06-11
 */
public interface CmsKbCategoryMapper {

    /**
     * 根据ID查询
     */
    CmsKbCategory selectById(@Param("id") Long id);

    /**
     * 条件查询列表
     */
    List<CmsKbCategory> selectList(CmsKbCategory query);

    /**
     * 查询某父节点下的子目录
     */
    List<CmsKbCategory> selectChildren(@Param("parentId") Long parentId);

    /**
     * 查询所有有效目录（懒加载禁用时返回全树）
     */
    List<CmsKbCategory> selectAll();

    /**
     * 新增目录
     */
    int insert(CmsKbCategory category);

    /**
     * 更新目录
     */
    int update(CmsKbCategory category);

    /**
     * 调整排序号（支持父节点变更）
     */
    int updateOrderNum(@Param("id") Long id,
                       @Param("parentId") Long parentId,
                       @Param("orderNum") Integer orderNum);

    /**
     * 逻辑删除（批量）
     */
    int deleteByIds(@Param("ids") Long[] ids);

    /**
     * 统计子目录数
     */
    int countChildren(@Param("parentId") Long parentId);

    /**
     * 统计目录下文档数
     */
    int countDocuments(@Param("categoryId") Long categoryId);

    /**
     * 查询必学目录
     */
    List<CmsKbCategory> selectRequired();
}
