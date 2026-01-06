package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.CmsContract;
import com.ruoyi.system.domain.CmsFile;

/**
 * 合同管理Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-14
 */
public interface CmsContractMapper 
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
     * 删除合同管理
     * 
     * @param contractId 合同管理主键
     * @return 结果
     */
    public int deleteCmsContractByContractId(Long contractId);

    /**
     * 批量删除合同管理
     * 
     * @param contractIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCmsContractByContractIds(Long[] contractIds);

    /**
     * 批量删除附件明细
     * 
     * @param contractIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCmsFileByContractIds(Long[] contractIds);
    
    /**
     * 批量新增附件明细
     * 
     * @param cmsFileList 附件明细列表
     * @return 结果
     */
    public int batchCmsFile(List<CmsFile> cmsFileList);
    

    /**
     * 通过合同管理主键删除附件明细信息
     * 
     * @param contractId 合同管理ID
     * @return 结果
     */
    public int deleteCmsFileByContractId(Long contractId);

    /**
     * 通过合同编码查询合同管理
     *
     * @param contractCode 合同编码
     * @return 合同管理
     */
    public CmsContract selectCmsContractByContractCode(String contractCode);
}
