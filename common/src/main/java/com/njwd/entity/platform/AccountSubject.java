package com.njwd.entity.platform;

import com.njwd.annotation.ExcelCell;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.base.ManagerInfo;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author liuxiang
 * @Description 会计科目
 * @Date:15:52 2019/6/25
 **/
@Getter
@Setter
public class AccountSubject extends BaseModel implements Serializable {
    private static final long serialVersionUID = -3288204783642982826L;

    /**
     * 企业ID
     */
    private Long rootEnterpriseId;

    /**
     * 上级编码
     */
    @ExcelCell(index = 2)
    private String upCode;

    /**
     * 上级科目ID
     **/
    private Long upId;

    /**
     * 科目编码
     */
    @ExcelCell(index = 0)
    private String code;

    /**
     * 科目名称
     */
    @ExcelCell(index = 1)
    private String name;

    /**
     * 科目名称全称
     */
    private String fullName;

    /**
     * 科目级次
     */
    private Byte level;

    /**
     * 创建公司 【公司】表ID
     */
    private Long createCompanyId;

    /**
     * 归属公司 【公司】表ID
     */
    private Long companyId;

    /**
     * 使用公司 【公司】表ID
     */
    private Long useCompanyId;

    /**
     * 科目表ID 对应【科目】表ID
     */
    private Long subjectId;

    /**
     * 余额方向 0：借方、1：贷方
     */
    @ExcelCell(index = 3)
    private Byte balanceDirection;

    /**
     * 余额控制 0不控制 1控制
     **/
    private Byte balanceControl;

    /**
     * 会计要素项 【会计要素项】表ID
     */
    private Long accountElementItem;

    /**
     * 会计要素项名称 【会计要素项】表NAME
     */
    private String accountElementItemName;

    /**
     * 是否是损益科目 0：否 1：是
     */
    private Byte isProfitAndLoss;

    /**
     * 是否是表外科目 0：否 1：是
     */
    private Byte isOffBalance;

    /**
     * 科目类别 【科目类别】表ID
     */
    private Long subjectCategory;

    /**
     * 科目类别名称 【科目类别】表NAME
     */
    private String subjectCategoryName;

    /**
     * 分类code
     */
    private String accountCategory;

    /**
     * 内部往来 0：否、1：是
     */
    private Byte isInterior;

    /**
     * 现金流量表id 【现金流量表】表ID
     */
    private Long cashFlowId;

    /**
     * 现金流入 【现金流量项目】表CODE
     */
    @ExcelCell(index = 4)
    private String cashInflowCode;

    /**
     * 现金流入名称 【现金流量项目】表NAME
     */
    @ExcelCell(index = 5)
    private String cashInflowName;

    /**
     * 现金流出 【现金流量项目】表CODE
     */
    @ExcelCell(index = 6)
    private String cashOutflowCode;

    /**
     * 现金流出名称 【现金流量项目】表NAME
     */
    @ExcelCell(index = 7)
    private String cashOutflowName;

    /**
     * 源基准表科目ID [wd_account_subject]
     **/
    private Long baseAccSubjectId;

    /**
     * 是否预置 0：否、1：是
     */
    private Byte isInit;

    /**
     * 是否为末级科目 0：否、1：是
     */
    private Byte isFinal;

    /**
     * 启用标识 0：禁用、1：启用
     */
    private Byte isEnable;

    /**
     * 现金流入 【现金流量项目】表ID
     */
    private Long cashInflowId;

    /**
     * 现金流出 【现金流量项目】表ID
     */
    private Long cashOutflowId;

    /**
     * 审核状态 0：未审核、1：已审核
     */
    private Byte isApproved;

    /**
     * 发布状态 0：未发布、1：已发布
     */
    private Byte isReleased;

    /**
     * 操作信息:禁用人,禁用时间等
     */
    private ManagerInfo manageInfo;

    /**
     * 平台ID 初始化
     */
    private Long platformId;

    /**
     * 平台名称 初始化
     */
    private String platformName;

}
