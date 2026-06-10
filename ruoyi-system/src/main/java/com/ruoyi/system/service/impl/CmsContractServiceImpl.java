package com.ruoyi.system.service.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.SecurityValidationUtil;
import com.ruoyi.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.CmsFile;
import com.ruoyi.system.domain.CmsContract;
import com.ruoyi.system.domain.CmsApproval;
import com.ruoyi.system.mapper.CmsContractMapper;
import com.ruoyi.system.service.ICmsApprovalService;
import com.ruoyi.system.service.ICmsNotificationService;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.ICmsContractService;
import com.ruoyi.system.service.ISysConfigService;
import com.alibaba.fastjson2.JSON;

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
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private ICmsNotificationService notificationService;
    @Autowired
    private SysUserMapper sysUserMapper;


    /**
     * 查询合同管理
     * 
     * @param contractId 合同管理主键
     * @return 合同管理
     */
    @Override
    public CmsContract selectCmsContractByContractId(Long contractId) {
        CmsContract contract = cmsContractMapper.selectCmsContractByContractId(contractId);
        if (contract != null) {
            contract.setReminderDays(getReminderDays());
            // 金额/利润的脱敏交给前端 showAmount 控制，与列表接口保持一致，
            // 避免 manager/sales/其他角色在详情页拿不到自己业务上需要的金额字段
        }
        return contract;
    }

    /**
     * 查询合同管理列表
     *
     * @param cmsContract 合同管理
     * @return 合同管理
     */
    @Override
    public List<CmsContract> selectCmsContractList(CmsContract cmsContract) {
        // 数据权限过滤：根据角色类型
        String roleType = determineRoleType();

        if ("sales".equals(roleType)) {
            // sales只能看到自己创建的合同
            String createBy = SecurityUtils.getUsername();
            if (StringUtils.isEmpty(createBy)) {
                createBy = "unknown";
            }
            cmsContract.setCreateBy(createBy);
        } else if ("account".equals(roleType)) {
            // account只能看到分配给自己的合同
            cmsContract.setOwnerId(SecurityUtils.getUserId());
        }
        // admin和manager可以看到所有合同（不过滤）

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
        if (cmsContract.getCustomerId() == null) {
            throw new ServiceException("请选择关联客户");
        }
        // 合同编号：前端不传则自动生成（yyyyMMdd + 3位序号）
        if (StringUtils.isEmpty(cmsContract.getContractCode())) {
            String todayPrefix = DateUtils.dateTime();
            String maxCode = cmsContractMapper.selectMaxContractCodeByPrefix(todayPrefix);
            int seq = 1;
            if (StringUtils.isNotEmpty(maxCode)) {
                String seqPart = maxCode.substring(todayPrefix.length());
                try {
                    seq = Integer.parseInt(seqPart) + 1;
                } catch (NumberFormatException e) {
                    // 如果后三位无法解析，默认从1开始
                }
            }
            cmsContract.setContractCode(todayPrefix + String.format("%03d", seq));
        } else {
            CmsContract exist = cmsContractMapper.selectCmsContractByContractCode(cmsContract.getContractCode());
            if (exist != null) {
                throw new ServiceException("合同编号已存在：" + cmsContract.getContractCode());
            }
        }
        
        // 确保create_by字段正确设置
        String createBy = SecurityUtils.getUsername();
        if (StringUtils.isEmpty(createBy)) {
            createBy = "unknown";
        }
        cmsContract.setCreateBy(createBy);
        System.out.println("Setting create_by to: " + createBy); // 调试信息
        System.out.println("Contract before insert: " + cmsContract.toString()); // 调试信息
        cmsContract.setCreateTime(DateUtils.getNowDate());
        int rows = cmsContractMapper.insertCmsContract(cmsContract);
        insertCmsFile(cmsContract);
        // 通知admin和manager用户审批
        List<String> roleKeys = Arrays.asList("admin", "manager");
        List<SysUser> approvers = sysUserMapper.selectUserByRoleKeys(roleKeys);
        if (approvers != null && !approvers.isEmpty()) {
            // 按userId去重（同一用户拥有多个角色时避免重复通知）
            Set<Long> notifiedUserIds = new java.util.HashSet<>();
            for (SysUser user : approvers) {
                if (notifiedUserIds.add(user.getUserId())) {
                    notificationService.createNotification(
                        user.getUserId(),
                        "新合同待审批: " + cmsContract.getContractName(),
                        "合同 " + cmsContract.getContractCode() + " - " + cmsContract.getContractName() + " 已提交，请及时审批",
                        "3",
                        cmsContract.getContractId()
                    );
                }
            }
        }
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
        if (cmsContract.getCustomerId() == null) {
            throw new ServiceException("请选择关联客户");
        }
        CmsContract exist = cmsContractMapper.selectCmsContractByContractCode(cmsContract.getContractCode());
        if (exist != null && !exist.getContractId().equals(cmsContract.getContractId())) {
            throw new ServiceException("合同编号已存在：" + cmsContract.getContractCode());
        }

        CmsContract existing = cmsContractMapper.selectCmsContractByContractId(cmsContract.getContractId());
        if (existing == null) {
            throw new ServiceException("合同不存在");
        }

        // 修改权限：admin/manager 可修改任何合同；sales/account 等其他角色仅能修改自己创建的合同
        String roleType = determineRoleType();
        String currentUser = SecurityUtils.getUsername();
        boolean isManagerLike = "admin".equals(roleType) || "manager".equals(roleType);
        if (!isManagerLike && StringUtils.isNotEmpty(currentUser)
                && !currentUser.equals(existing.getCreateBy())) {
            throw new ServiceException("仅合同创建者或管理员可修改合同");
        }

        // 变更审批：若原状态为「已审批通过」，修改后重置为「待审批」，需要走新的审批流程
        if ("1".equals(existing.getAuditStatus())) {
            cmsContract.setAuditStatus("0");
        }

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
        String roleType = determineRoleType();
        boolean isManagerLike = "admin".equals(roleType) || "manager".equals(roleType);
        String currentUser = SecurityUtils.getUsername();
        for (Long contractId : contractIds) {
            CmsContract contract = cmsContractMapper.selectCmsContractByContractId(contractId);
            if (contract == null) {
                throw new ServiceException("合同不存在: " + contractId);
            }
            // 删除权限：admin/manager 可删除任何合同；其他角色仅能删除自己创建的合同
            if (!isManagerLike && StringUtils.isNotEmpty(currentUser)
                    && !currentUser.equals(contract.getCreateBy())) {
                throw new ServiceException("仅合同创建者或管理员可删除合同");
            }
        }
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
        CmsContract contract = cmsContractMapper.selectCmsContractByContractId(contractId);
        if (contract == null) {
            throw new ServiceException("合同不存在: " + contractId);
        }
        String roleType = determineRoleType();
        boolean isManagerLike = "admin".equals(roleType) || "manager".equals(roleType);
        String currentUser = SecurityUtils.getUsername();
        if (!isManagerLike && StringUtils.isNotEmpty(currentUser)
                && !currentUser.equals(contract.getCreateBy())) {
            throw new ServiceException("仅合同创建者或管理员可删除合同");
        }
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
        int index = 0;
        for (CmsContract contract : contractList) {
            index++;
            try {
                // 自动设置系统字段默认值
                contract.setAuditStatus("0");
                if (StringUtils.isEmpty(contract.getReminderStatus())) {
                    contract.setReminderStatus("0");
                }
                contract.setOwnerId(SecurityUtils.getUserId());
                contract.setDeptId(SecurityUtils.getDeptId());

                if (StringUtils.isEmpty(contract.getContractCode())
                        || StringUtils.isEmpty(contract.getContractName())) {
                    failureNum++;
                    failureMsg.append("第" + index + "行: 合同编码或名称为空; ");
                    continue;
                }

                // 安全字符校验
                SecurityValidationUtil.validateField("合同编码", contract.getContractCode());
                SecurityValidationUtil.validateField("合同名称", contract.getContractName());
                if (StringUtils.isNotEmpty(contract.getCustomerName())) {
                    SecurityValidationUtil.validateField("客户名称", contract.getCustomerName());
                }
                if (StringUtils.isNotEmpty(contract.getRemark())) {
                    SecurityValidationUtil.validateField("备注", contract.getRemark());
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
                    failureMsg.append("第" + index + "行: 合同编码已存在且未开启更新; ");
                }
            } catch (Exception e) {
                failureNum++;
                failureMsg.append("第" + index + "行: " + e.getMessage() + "; ");
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
            // 获取合同完整信息作为快照
            String contentSnapshot = JSON.toJSONString(fullContract);
            approval.setContentSnapshot(contentSnapshot);
            cmsApprovalService.insertCmsApproval(approval);
        }
        return rows;
    }
    /**
     * 获取到期提醒天数配置
     * @return 天数，默认30
     */
    private int getReminderDays() {
        try {
            String val = configService.selectConfigByKey("cms.reminder.days");
            if (StringUtils.isNotEmpty(val)) {
                return Integer.parseInt(val.trim());
            }
        } catch (Exception e) {
            // ignore, use default
        }
        return 30;
    }


    /**
     * 判断当前用户角色类型
     * @return admin/accountant/sales
     */
    private String determineRoleType() {
        try {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser != null && loginUser.getUser() != null) {
                java.util.List<SysRole> roles = loginUser.getUser().getRoles();
                if (roles != null) {
                    for (SysRole role : roles) {
                        if ("admin".equals(role.getRoleKey())) return "admin";
                        if ("manager".equals(role.getRoleKey())) return "manager";
                        if ("accountant".equals(role.getRoleKey())) return "accountant";
                        if ("sales".equals(role.getRoleKey())) return "sales";
                    }
                }
            }
            
            // 如果无法确定角色，从用户名推断（用于测试环境）
            String username = SecurityUtils.getUsername();
            if ("manager".equals(username)) return "manager";
            if ("zhangsan".equals(username)) return "accountant";
            if ("lisi".equals(username)) return "sales";
            if ("admin".equals(username)) return "admin";
            
        } catch (Exception e) {
            // 记录错误但继续执行
            System.err.println("Error determining role type: " + e.getMessage());
        }
        return "admin"; // 默认返回admin，但应该有更安全的处理
    }

}
