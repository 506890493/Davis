package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.CmsKbDocument;

/**
 * 知识库-文档Service接口
 *
 * @author ruoyi
 * @date 2026-06-11
 */
public interface ICmsKbDocumentService {
    /**
     * 根据ID查询
     */
    CmsKbDocument selectById(Long id);

    /**
     * 条件查询列表
     */
    List<CmsKbDocument> selectList(CmsKbDocument query);

    /**
     * 新增文档（自动创建 v1）
     */
    Long insert(CmsKbDocument doc);

    /**
     * 更新文档（自动创建新版本）
     */
    int update(CmsKbDocument doc);

    /**
     * 发布文档
     */
    int publish(Long id);

    /**
     * 下线文档
     */
    int offline(Long id);

    /**
     * 批量软删除（写入回收站）
     */
    int softDelete(Long[] ids);

    /**
     * 批量恢复软删文档
     */
    int restore(Long[] ids);

    /**
     * 批量物理删除
     */
    int hardDelete(Long[] ids);

    /**
     * 回收站列表（仅 del_flag=1 的文档）
     */
    List<CmsKbDocument> selectRecycleList(CmsKbDocument query);

    /**
     * 物理删除回收站中 delete_time < cutoff 的文档（连同 version、attachment）。
     * 由 Quartz 任务（KbRecycleCleanTask）每日 02:00 调用。
     *
     * @param cutoff 截止时间，早于该时间进入回收站的文档将被物理删除
     * @return 实际删除的文档数
     */
    int purgeExpired(java.util.Date cutoff);
}
