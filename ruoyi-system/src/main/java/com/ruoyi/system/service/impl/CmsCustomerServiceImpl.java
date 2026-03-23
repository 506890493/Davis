package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.CmsCustomerMapper;
import com.ruoyi.system.service.ICmsCustomerService;
import com.ruoyi.common.utils.SecurityUtils;
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
}
