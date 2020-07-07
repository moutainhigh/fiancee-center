package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.CashFlowItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author lj
 * @Description 现金流量项目
 * @Date:15:38 2019/6/12
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class CashFlowItemVo extends CashFlowItem {
    private static final long serialVersionUID = -6009070486966876652L;
    /**
     * 方向
     */
    private String cashFlowDirectionName;


    /**
     * 上级Id（新增互换Id）
     */
    private Long upId;


	/**
	 * 分组Id
	 */
	private Long cashFlowGroup;

    /**
     * 分组
     */
    private String cashFlowGroupName;
    /**
     * 归属现金流量项目表
     */
    private String cashFlowName;

    /**
     * 模板名称
     */
    private String templateCashFlowName;

    /**
     * 模板最大级次
     **/
    private String maxLevel;

    /**
     * 账簿类型Id
     **/
    private Long accountBookTypeId;
    /**
     * 账簿类型名称
     **/
    private String accountBookTypeName;
    /**
     * 会计准则Id
     **/
    private Long accStandardId;
    /**
     * 会计准则名称
     **/
    private String accStandardName;

    /**
     * 是否末级 0：否、1：是
     **/
    private Byte isFinal;

    /**
     * 归属公司名称
     */
    private String companyName;

    /**
     * 使用公司名称
     */
    private String useCompanyName;

    /**
     * 创建公司名称
     */
    private String createCompanyName;

    /**
     * 分组集合
     */
    private List<CashFlowItem> groupList;

    /**
     * 方向集合
     */
    private List<CashFlowItem> directionList;

    /**
     * 结果信息
     */
    private String resultMessage;

    /**
     * 上级名称
     */
    private String upName;

    /**
     * 金额
     */
    private BigDecimal totalAmount;

    /**
     * 本年已发生额
     */
    private BigDecimal openingBalance;

    /**
     * 启用人ID
     */
    private Long enabledUserId;
    /**
     * 启用用户名
     */
    private String  enabledUserName;
    /**
     * 启用时间
     */
    private String enabledTime;
    /**
     * 禁用人Id
     */
    private Long disabledUserId;
    /**
     * 禁用用户名
     */
    private String  disabledUserName;
    /**
     * 禁用时间
     */
    private String disabledTime;

}
