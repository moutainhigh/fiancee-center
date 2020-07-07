package com.njwd.entity.platform.vo;

import lombok.Data;
import lombok.ToString;

/**
 * @Author liuxiang
 * @Description 更多模板查询返回参数
 * @Date:14:18 2019/6/19
 **/
@Data
@ToString(callSuper = true)
public class FindAccountSubjectListVo {

    /**
     * 主键
     */
    private Long id;

    /**
     * 编码
     */
    private String code;

    /**
     * 上级科目编码
     */
    private String upCode;

    /**
     * 含上级完整名称
     */
    private String fullName;

    /**
     * 名称
     */
    private String name;

    /**
     * 极次
     */
    private String level;

    /**
     * 余额方向 0：借、1：贷
     */
    private Integer balanceDirection;

    /**
     * 数据类型 1：共享、2：分配、3：私有
     */
    private Byte dataType;

    /**
     * 【会计要素】表ID
     */
    private Long elementId;

    /**
     * 是否是损益科目 0：否 1：是
     */
    private Byte isProfitAndLoss;

    /**
     * 是否是表外科目 0：否 1：是
     */
    private Byte isOffBalance;

    /**
     * 分类ID
     */
    private String accountCategory;

    /**
     * 分类名称
     */
    private String accountCategoryName;

    /**
     * 会计要素项 【会计要素项】表ID
     */
    private Long accountElementItem;

    /**
     * 会计要素项名称 【会计要素项】表NAME
     */
    private String accountElementItemName;

    /**
     * 科目类别 【科目类别】表ID
     */
    private Long subjectCategory;

    /**
     * 科目类别名称 【科目类别】表NAME
     */
    private String subjectCategoryName;

    /**
     * 内部往来 0：否、1：是
     */
    private Integer isInterior;

    /**
     * 内部往来名称
     */
    private String isInteriorName;

    /**
     * 辅助核算CODE集合
     **/
    private String auxiliaryCodes;

    /**
     * 辅助核算名称集合
     **/
    private String auxiliaryNames;

    /**
     * 辅助核算来源名称集合
     **/
    private String auxiliarySources;

    /**
     * 现金流量流入ID
     **/
    private Long cashInflowId;

    /**
     * 现金流量流出ID
     **/
    private Long cashOutflowId;

    /**
     * 现金流量名称集合
     **/
    private String cashFlowNames;

    /**
     * 科目ID
     **/
    private String subjectId;

    /**
     * 科目名称
     **/
    private String subjectName;

    /**
     * 币种ID
     **/
    private String currencyIds;

    /**
     * 币种ID名称
     **/
    private String currencyNames;


    /**
     * 模板id
     **/
    private Long templateSubjectId;

    /**
     * 模板名称
     **/
    private String templateSubjectName;


    /**
     * 辅助核算值依赖名称集合
     **/
    private String auxiliaryDependents;


    /**
     * 模板最大级次
     **/
    private String maxLevel;

    /**
     * 账簿类型ID
     **/
    private String accountBookTypeId;
    /**
     * 账簿类型名称
     **/
    private String accountBookTypeName;
    /**
     * 会计准则ID
     **/
    private String accStandardId;
    /**
     * 会计准则名称
     **/
    private String accStandardName;

    /**
     * 现金量项目CODE
     **/
    private String cashInflowCode;

    /**
     * 现金量项目NAME
     **/
    private String cashInflowName;

    /**
     * 现金量项目CODE
     **/
    private String cashOutflowCode;

    /**
     * 现金量项目NAME
     **/
    private String cashOutflowName;

    /**
     * 是否末级 0：否、1：是
     **/
    private Byte isFinal;

    /**
     * 辅助核算值来源表
     **/
    private String auxiliarySourceTables;

    /**
     * 审核状态
     **/
    private String checkStatus;

    /**
     * 平台ID 初始化
     */
    private Long platformId;

    /**
     * 平台名称 初始化
     */
    private String platformName;

}