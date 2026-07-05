package com.ruoyi.system.mapper;

import java.util.Date;
import java.util.List;
import com.ruoyi.system.domain.CmsKbDocument;
import org.apache.ibatis.annotations.Param;

/**
 * 知识库-文档Mapper接口
 *
 * @author ruoyi
 * @date 2026-06-11
 */
public interface CmsKbDocumentMapper {

    /**
     * 根据ID查询
     */
    CmsKbDocument selectById(@Param("id") Long id);

    /**
     * 条件查询列表（应用置顶排序规则：is_pinned DESC, pinned_at ASC, title ASC）
     */
    List<CmsKbDocument> selectList(CmsKbDocument query);

    /**
     * 搜索专用：与 selectList 条件相同，但排序不应用置顶规则
     * （按 create_time DESC，避免置顶文档在搜索结果中插队）
     */
    List<CmsKbDocument> selectListForSearch(CmsKbDocument query);

    /**
     * 新增文档
     */
    int insert(CmsKbDocument doc);

    /**
     * 更新文档
     */
    int update(CmsKbDocument doc);

    /**
     * 更新文档状态（含发布时间）
     */
    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status,
                     @Param("publishedTime") Date publishedTime);

    /**
     * 更新当前版本号
     */
    int updateCurrentVersion(@Param("id") Long id,
                             @Param("currentVersion") Integer currentVersion);

    /**
     * 置顶 / 取消置顶。
     * pinned=true  → is_pinned=1, pinned_at=NOW()
     * pinned=false → is_pinned=0, pinned_at=NULL
     */
    int updatePin(@Param("id") Long id,
                  @Param("pinned") boolean pinned,
                  @Param("updateBy") String updateBy);

    /**
     * 逻辑删除（写入删除时间，进入回收站）
     */
    int softDelete(@Param("ids") Long[] ids,
                   @Param("deleteTime") Date deleteTime);

    /**
     * 恢复软删文档（del_flag=0, delete_time=NULL）
     */
    int restoreByIds(@Param("ids") Long[] ids);

    /**
     * 物理删除（批量）
     */
    int hardDelete(@Param("ids") Long[] ids);

    /**
     * 回收站列表（del_flag=1）
     */
    List<CmsKbDocument> selectRecycleList(CmsKbDocument query);

    /**
     * 统计某目录下文档数
     */
    int countByCategory(@Param("categoryId") Long categoryId);

    /**
     * 统计回收站中超过 cutoff 时间仍未恢复的文档数
     */
    int countExpiredInRecycle(@Param("cutoff") Date cutoff);
}
