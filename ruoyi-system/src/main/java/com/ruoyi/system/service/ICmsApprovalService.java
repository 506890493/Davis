package com.ruoyi.system.service;

import com.ruoyi.system.domain.CmsApproval;

import java.util.List;

/**
 * 业务审批Service接口
 * 
 * @author ruoyi
 * @date 2025-11-29
 */
public interface ICmsApprovalService 
{
    /**
     * 查询业务审批
     * 
     * @param approvalId 业务审批主键
     * @return 业务审批
     */
    public CmsApproval selectCmsApprovalByApprovalId(Long approvalId);

    /**
     * 查询业务审批列表
     * 
     * @param cmsApproval 业务审批
     * @return 业务审批集合
     */
    public List<CmsApproval> selectCmsApprovalList(CmsApproval cmsApproval);

    /**
     * 新增业务审批
     * 
     * @param cmsApproval 业务审批
     * @return 结果
     */
    public int insertCmsApproval(CmsApproval cmsApproval);

    /**
     * 修改业务审批
     * 
     * @param cmsApproval 业务审批
     * @return 结果
     */
    public int updateCmsApproval(CmsApproval cmsApproval);

    /**
     * 批量删除业务审批
     * 
     * @param approvalIds 需要删除的业务审批主键集合
     * @return 结果
     */
    public int deleteCmsApprovalByApprovalIds(Long[] approvalIds);

    /**
     * 删除业务审批信息
     * 
     * @param approvalId 业务审批主键
     * @return 结果
     */
    public int deleteCmsApprovalByApprovalId(Long approvalId);
}
