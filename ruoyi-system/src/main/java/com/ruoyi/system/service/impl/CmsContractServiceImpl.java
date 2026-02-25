package com.ruoyi.system.service.impl;

import java.util.List;
import java.util.ArrayList;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.CmsFile;
import com.ruoyi.system.domain.CmsContract;
import com.ruoyi.system.domain.CmsApproval;
import com.ruoyi.system.mapper.CmsContractMapper;
import com.ruoyi.system.service.ICmsApprovalService;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.service.ICmsContractService;

/**
 * 合同管理Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-14
 */
@Service
public class CmsContractServiceImpl implements ICmsContractService {
    @Autowired
    private CmsContractMapper cmsContractMapper;

    @Autowired
    private ICmsApprovalService cmsApprovalService;

    /**
     * 查询合同管理
     * 
     * @param contractId 合同管理主键
     * @return 合同管理
     */
    @Override
    public CmsContract selectCmsContractByContractId(Long contractId) {
        return cmsContractMapper.selectCmsContractByContractId(contractId);
    }

    /**
     * 查询合同管理列表
     * 
     * @param cmsContract 合同管理
     * @return 合同管理
     */
    @Override
    public List<CmsContract> selectCmsContractList(CmsContract cmsContract) {
        return cmsContractMapper.selectCmsContractList(cmsContract);
    }

    /**
     * 新增合同管理
     * 
     * @param cmsContract 合同管理
     * @return 结果
     */
    @Transactional
    @Override
    public int insertCmsContract(CmsContract cmsContract) {
        cmsContract.setCreateTime(DateUtils.getNowDate());
        int rows = cmsContractMapper.insertCmsContract(cmsContract);
        insertCmsFile(cmsContract);
        return rows;
    }

    /**
     * 修改合同管理
     * 
     * @param cmsContract 合同管理
     * @return 结果
     */
    @Transactional
    @Override
    public int updateCmsContract(CmsContract cmsContract) {
        cmsContract.setUpdateTime(DateUtils.getNowDate());
        if (cmsContract.getCmsFileList() != null) {
            cmsContractMapper.deleteCmsFileByContractId(cmsContract.getContractId());
            insertCmsFile(cmsContract);
        }
        return cmsContractMapper.updateCmsContract(cmsContract);
    }

    /**
     * 批量删除合同管理
     * 
     * @param contractIds 需要删除的合同管理主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteCmsContractByContractIds(Long[] contractIds) {
        cmsContractMapper.deleteCmsFileByContractIds(contractIds);
        return cmsContractMapper.deleteCmsContractByContractIds(contractIds);
    }

    /**
     * 删除合同管理信息
     * 
     * @param contractId 合同管理主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteCmsContractByContractId(Long contractId) {
        cmsContractMapper.deleteCmsFileByContractId(contractId);
        return cmsContractMapper.deleteCmsContractByContractId(contractId);
    }

    /**
     * 新增附件明细信息
     * 
     * @param cmsContract 合同管理对象
     */
    public void insertCmsFile(CmsContract cmsContract) {
        List<CmsFile> cmsFileList = cmsContract.getCmsFileList();
        Long contractId = cmsContract.getContractId();
        if (StringUtils.isNotNull(cmsFileList)) {
            List<CmsFile> list = new ArrayList<CmsFile>();
            for (CmsFile cmsFile : cmsFileList) {
                cmsFile.setContractId(contractId);
                list.add(cmsFile);
            }
            if (list.size() > 0) {
                cmsContractMapper.batchCmsFile(list);
            }
        }
    }

    /**
     * 导入合同管理数据
     *
     * @param contractList  合同管理数据列表
     * @param updateSupport 是否支持更新
     * @param operator      操作人
     * @return 结果
     */
    @Transactional
    @Override
    public String importCmsContract(List<CmsContract> contractList, boolean updateSupport, String operator) {
        if (contractList == null || contractList.isEmpty()) {
            throw new ServiceException("导入合同数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (CmsContract contract : contractList) {
            try {
                if (StringUtils.isEmpty(contract.getContractCode())
                        || StringUtils.isEmpty(contract.getContractName())) {
                    failureNum++;
                    failureMsg.append("合同编码或名称为空; ");
                    continue;
                }
                CmsContract exists = cmsContractMapper.selectCmsContractByContractCode(contract.getContractCode());
                contract.setCreateBy(operator);
                contract.setCreateTime(DateUtils.getNowDate());
                if (exists == null) {
                    cmsContractMapper.insertCmsContract(contract);
                    successNum++;
                } else if (updateSupport) {
                    contract.setContractId(exists.getContractId());
                    contract.setUpdateBy(operator);
                    contract.setUpdateTime(DateUtils.getNowDate());
                    cmsContractMapper.updateCmsContract(contract);
                    successNum++;
                } else {
                    failureNum++;
                    failureMsg.append("合同编码已存在且未开启更新; ");
                }
            } catch (Exception e) {
                failureNum++;
                failureMsg.append(e.getMessage()).append("; ");
            }
        }
        if (failureNum > 0) {
            failureMsg.insert(0, "导入失败 " + failureNum + " 条，详情：");
            throw new ServiceException(failureMsg.toString());
        }
        successMsg.append("导入成功 " + successNum + " 条");
        return successMsg.toString();
    }

    /**
     * 审批合同
     *
     * @param cmsContract 合同管理
     * @return 结果
     */
    @Override
    @Transactional
    public int auditContract(CmsContract cmsContract) {
        int rows = cmsContractMapper.updateCmsContract(cmsContract);
        if (rows > 0) {
            CmsContract fullContract = cmsContractMapper.selectCmsContractByContractId(cmsContract.getContractId());
            CmsApproval approval = new CmsApproval();
            approval.setContractId(cmsContract.getContractId());
            Long applicantId = fullContract.getOwnerId();
            if (applicantId == null) {
                applicantId = SecurityUtils.getUserId();
            }
            approval.setApplicantId(applicantId);
            approval.setApproverId(SecurityUtils.getUserId());
            approval.setStatus(cmsContract.getAuditStatus());
            approval.setApprovalMsg(cmsContract.getRemark());
            approval.setApprovalTime(DateUtils.getNowDate());
            approval.setApprovalType("3"); // 3=变更/审核
            cmsApprovalService.insertCmsApproval(approval);
        }
        return rows;
    }
}
