package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.CmsTask;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 任务管理Mapper接口
 *
 * @author ruoyi
 * @date 2026-01-17
 */
public interface CmsTaskMapper
{
    /**
     * 查询任务管理
     *
     * @param taskId 任务管理主键
     * @return 任务管理
     */
    public CmsTask selectCmsTaskByTaskId(Long taskId);

    /**
     * 查询任务管理列表
     *
     * @param cmsTask 任务管理
     * @return 任务管理集合
     */
    public List<CmsTask> selectCmsTaskList(CmsTask cmsTask);

    /**
     * 新增任务管理
     *
     * @param cmsTask 任务管理
     * @return 结果
     */
    public int insertCmsTask(CmsTask cmsTask);

    /**
     * 修改任务管理
     *
     * @param cmsTask 任务管理
     * @return 结果
     */
    public int updateCmsTask(CmsTask cmsTask);

    /**
     * 删除任务管理
     *
     * @param taskId 任务管理主键
     * @return 结果
     */
    public int deleteCmsTaskByTaskId(Long taskId);

    /**
     * 批量删除任务管理
     *
     * @param taskIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCmsTaskByTaskIds(Long[] taskIds);

    /**
     * 统计已完成任务的金额
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param assignedTo 执行人ID（可选）
     * @return 金额
     */
    public BigDecimal sumCompletedTaskAmount(@Param("startDate") Date startDate, 
                                              @Param("endDate") Date endDate, 
                                              @Param("assignedTo") Long assignedTo);

    /**
     * 统计已完成代账类型任务数量
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param assignedTo 执行人ID（可选）
     * @return 数量
     */
    public Long countCompletedBookkeepingTasks(@Param("startDate") Date startDate, 
                                                @Param("endDate") Date endDate, 
                                                @Param("assignedTo") Long assignedTo);
}