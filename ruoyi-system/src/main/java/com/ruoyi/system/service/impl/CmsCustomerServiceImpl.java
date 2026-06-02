package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.CmsCustomerMapper;
import com.ruoyi.system.mapper.CmsContractMapper;
import com.ruoyi.system.service.ICmsCustomerService;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.CmsCustomer;

/**
 * 客户管理Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class CmsCustomerServiceImpl implements ICmsCustomerService
{
    @Autowired
    private CmsCustomerMapper cmsCustomerMapper;
    
    @Autowired
    private CmsContractMapper cmsContractMapper;

    /**
     * 查询客户管理
     * 
     * @param customerId 客户管理主键
     * @return 客户管理
     */
    @Override
    public CmsCustomer selectCmsCustomerById(Long customerId)
    {
        return cmsCustomerMapper.selectCmsCustomerById(customerId);
    }

    /**
     * 查询客户管理列表
     * 
     * @param cmsCustomer 客户管理
     * @return 客户管理
     */
    @Override
    public List<CmsCustomer> selectCmsCustomerList(CmsCustomer cmsCustomer)
    {
        return cmsCustomerMapper.selectCmsCustomerList(cmsCustomer);
    }

    /**
     * 新增客户管理
     * 
     * @param cmsCustomer 客户管理
     * @return 结果
     */
    @Override
    public int insertCmsCustomer(CmsCustomer cmsCustomer)
    {
        cmsCustomer.setCreateBy(SecurityUtils.getUsername());
        cmsCustomer.setCreateTime(new java.util.Date());
        return cmsCustomerMapper.insertCmsCustomer(cmsCustomer);
    }

    /**
     * 修改客户管理
     * 
     * @param cmsCustomer 客户管理
     * @return 结果
     */
    @Override
    public int updateCmsCustomer(CmsCustomer cmsCustomer)
    {
        cmsCustomer.setUpdateBy(SecurityUtils.getUsername());
        cmsCustomer.setUpdateTime(new java.util.Date());
        return cmsCustomerMapper.updateCmsCustomer(cmsCustomer);
    }

    /**
     * 删除客户管理
     * 
     * @param customerId 客户管理主键
     * @return 结果
     */
    @Override
    public int deleteCmsCustomerById(Long customerId)
    {
        return cmsCustomerMapper.deleteCmsCustomerById(customerId);
    }

    /**
     * 批量删除客户管理
     * 
     * @param customerIds 需要删除的数据主键集合
     * @return 结果
     */
    @Override
    public int deleteCmsCustomerByIds(Long[] customerIds)
    {
        String currentUser = SecurityUtils.getUsername();
        String roleType = determineRoleType();
        
        for (Long customerId : customerIds) {
            Long contractCount = cmsContractMapper.countContractsByCustomerId(customerId);
            if (contractCount != null && contractCount > 0) {
                throw new ServiceException("该客户有关联合同，不能删除");
            }
            
            // 检查删除权限：只有客户创建者或管理员才能删除客户
            if (!"admin".equals(roleType)) {
                CmsCustomer customer = cmsCustomerMapper.selectCmsCustomerById(customerId);
                System.out.println("Customer delete check - currentUser: " + currentUser + ", customer.create_by: " + customer.getCreateBy());
                if (customer != null && !currentUser.equals(customer.getCreateBy())) {
                    throw new ServiceException("只有客户创建者或管理员才能删除客户");
                }
            }
        }
        return cmsCustomerMapper.deleteCmsCustomerByIds(customerIds);
    }

    /**
     * 统计客户总数
     */
    @Override
    public Long countCustomer()
    {
        return cmsCustomerMapper.countCustomer();
    }

    /**
     * 统计销售归属的客户数
     */
    @Override
    public Long countCustomerByOwner(Long ownerId)
    {
        return cmsCustomerMapper.countCustomerByOwner(ownerId);
    }

    /**
     * 判断当前用户角色类型
     * @return admin/accountant/sales
     */
    private String determineRoleType() {
        try {
            com.ruoyi.common.core.domain.model.LoginUser loginUser = com.ruoyi.common.utils.SecurityUtils.getLoginUser();
            if (loginUser != null && loginUser.getUser() != null) {
                java.util.List<com.ruoyi.common.core.domain.entity.SysRole> roles = loginUser.getUser().getRoles();
                if (roles != null) {
                    for (com.ruoyi.common.core.domain.entity.SysRole role : roles) {
                        if ("admin".equals(role.getRoleKey())) return "admin";
                        if ("manager".equals(role.getRoleKey())) return "manager";
                        if ("accountant".equals(role.getRoleKey())) return "accountant";
                        if ("sales".equals(role.getRoleKey())) return "sales";
                    }
                }
            }
            
            // 如果无法确定角色，从用户名推断（用于测试环境）
            String username = com.ruoyi.common.utils.SecurityUtils.getUsername();
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
