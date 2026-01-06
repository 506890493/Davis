package com.ruoyi.system.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.system.domain.CmsApproval;
import com.ruoyi.system.mapper.CmsApprovalMapper;
import com.ruoyi.system.service.ICmsApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 业务审批Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-11-29
 */
@Service
public class CmsApprovalServiceImpl implements ICmsApprovalService 
{
    @Autowired
    private CmsApprovalMapper cmsApprovalMapper;

    /**
     * 查询业务审批
     * 
     * @param approvalId 业务审批主键
     * @return 业务审批
     */
    @Override
    public CmsApproval selectCmsApprovalByApprovalId(Long approvalId)
    {
        return cmsApprovalMapper.selectCmsApprovalByApprovalId(approvalId);
    }

    /**
     * 查询业务审批列表
     * 
     * @param cmsApproval 业务审批
     * @return 业务审批
     */
    @Override
    public List<CmsApproval> selectCmsApprovalList(CmsApproval cmsApproval)
    {
        return cmsApprovalMapper.selectCmsApprovalList(cmsApproval);
    }

    /**
     * 新增业务审批
     * 
     * @param cmsApproval 业务审批
     * @return 结果
     */
    @Override
    public int insertCmsApproval(CmsApproval cmsApproval)
    {
        cmsApproval.setCreateTime(DateUtils.getNowDate());
        return cmsApprovalMapper.insertCmsApproval(cmsApproval);
    }

    /**
     * 修改业务审批
     * 
     * @param cmsApproval 业务审批
     * @return 结果
     */
    @Override
    public int updateCmsApproval(CmsApproval cmsApproval)
    {
        cmsApproval.setUpdateTime(DateUtils.getNowDate());
        return cmsApprovalMapper.updateCmsApproval(cmsApproval);
    }

    /**
     * 批量删除业务审批
     * 
     * @param approvalIds 需要删除的业务审批主键
     * @return 结果
     */
    @Override
    public int deleteCmsApprovalByApprovalIds(Long[] approvalIds)
    {
        return cmsApprovalMapper.deleteCmsApprovalByApprovalIds(approvalIds);
    }

    /**
     * 删除业务审批信息
     * 
     * @param approvalId 业务审批主键
     * @return 结果
     */
    @Override
    public int deleteCmsApprovalByApprovalId(Long approvalId)
    {
        return cmsApprovalMapper.deleteCmsApprovalByApprovalId(approvalId);
    }
}
