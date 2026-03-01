package com.ruoyi.system.domain.vo;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;

/**
 * 总账报表按人员统计VO
 *
 * @author ruoyi
 * @date 2026-03-01
 */
public class LedgerByPersonVo
{
    /** 人员登录名 */
    @Excel(name = "登录名")
    private String userName;

    /** 显示名 */
    @Excel(name = "姓名")
    private String nickName;

    /** 角色名(会计/销售) */
    @Excel(name = "角色")
    private String roleName;

    /** 合同数 */
    @Excel(name = "合同数")
    private Integer contractCount;

    /** 收入总额 */
    @Excel(name = "收入总额")
    private BigDecimal totalAmount;

    /** 完成任务数 */
    @Excel(name = "完成任务数")
    private Integer taskCount;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Integer getContractCount() {
        return contractCount;
    }

    public void setContractCount(Integer contractCount) {
        this.contractCount = contractCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Integer taskCount) {
        this.taskCount = taskCount;
    }
}
