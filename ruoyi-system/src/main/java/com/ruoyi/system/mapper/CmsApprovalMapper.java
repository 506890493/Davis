package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.CmsApproval;

import java.util.List;

/**
 * 业务审批Mapper接口
 * 
 * @author ruoyi
 * @date 2025-11-29
 */
public interface CmsApprovalMapper 
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
     * 删除业务审批
     * 
     * @param approvalId 业务审批主键
     * @return 结果
     */
    public int deleteCmsApprovalByApprovalId(Long approvalId);

    /**
     * 批量删除业务审批
     * 
     * @param approvalIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCmsApprovalByApprovalIds(Long[] approvalIds);
}
