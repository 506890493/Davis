package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.CmsCustomer;

/**
 * 客户管理Service接口
 * 
 * @author ruoyi
 */
public interface ICmsCustomerService
{
    /**
     * 查询客户管理
     * 
     * @param customerId 客户管理主键
     * @return 客户管理
     */
    public CmsCustomer selectCmsCustomerById(Long customerId);

    /**
     * 查询客户管理列表
     * 
     * @param cmsCustomer 客户管理
     * @return 客户管理集合
     */
    public List<CmsCustomer> selectCmsCustomerList(CmsCustomer cmsCustomer);

    /**
     * 新增客户管理
     * 
     * @param cmsCustomer 客户管理
     * @return 结果
     */
    public int insertCmsCustomer(CmsCustomer cmsCustomer);

    /**
     * 修改客户管理
     * 
     * @param cmsCustomer 客户管理
     * @return 结果
     */
    public int updateCmsCustomer(CmsCustomer cmsCustomer);

    /**
     * 删除客户管理
     * 
     * @param customerId 客户管理主键
     * @return 结果
     */
    public int deleteCmsCustomerById(Long customerId);

    /**
     * 批量删除客户管理
     * 
     * @param customerIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCmsCustomerByIds(Long[] customerIds);

    /**
     * 统计客户总数
     * 
     * @return 客户总数
     */
    public Long countCustomer();

    /**
     * 统计销售归属的客户数
     * 
     * @param ownerId 销售ID
     * @return 客户数
     */
    public Long countCustomerByOwner(Long ownerId);
}
