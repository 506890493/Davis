package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.CmsContract;
import com.ruoyi.common.exception.ServiceException;

/**
 * 合同管理Service接口
 * 
 * @author ruoyi
 * @date 2025-12-14
 */
public interface ICmsContractService 
{
    /**
     * 查询合同管理
     * 
     * @param contractId 合同管理主键
     * @return 合同管理
     */
    public CmsContract selectCmsContractByContractId(Long contractId);

    /**
     * 查询合同管理列表
     * 
     * @param cmsContract 合同管理
     * @return 合同管理集合
     */
    public List<CmsContract> selectCmsContractList(CmsContract cmsContract);

    /**
     * 新增合同管理
     * 
     * @param cmsContract 合同管理
     * @return 结果
     */
    public int insertCmsContract(CmsContract cmsContract);

    /**
     * 修改合同管理
     * 
     * @param cmsContract 合同管理
     * @return 结果
     */
    public int updateCmsContract(CmsContract cmsContract);

    /**
     * 批量删除合同管理
     * 
     * @param contractIds 需要删除的合同管理主键集合
     * @return 结果
     */
    public int deleteCmsContractByContractIds(Long[] contractIds);

    /**
     * 删除合同管理信息
     * 
     * @param contractId 合同管理主键
     * @return 结果
     */
    public int deleteCmsContractByContractId(Long contractId);

    public String importCmsContract(List<CmsContract> contractList, boolean updateSupport, String operator);
}
