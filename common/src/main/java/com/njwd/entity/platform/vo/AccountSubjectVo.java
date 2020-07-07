package com.njwd.entity.platform.vo;

import com.njwd.annotation.ExcelCell;
import com.njwd.common.Constant;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.basedata.vo.SubjectAuxiliaryVo;
import com.njwd.entity.platform.AccountSubject;
import com.njwd.entity.platform.AccountSubjectAuxiliary;
import com.njwd.entity.platform.Subject;
import com.njwd.support.Result;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author liuxiang
 * @Description 会计科目
 * @Date:14:16 2019/6/19
 **/
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AccountSubjectVo extends AccountSubject {
    private static final long serialVersionUID = -5088483319146756190L;

    /**
     * 数据类型 1：共享、2：分配、3：私有
     */
    private Byte dataType;

    /**
     * 【会计要素】表ID
     */
    private Long elementId;

    /**
     * 上级科目名称
     */
    private String upName;

    /**
     * 上级名称全称
     */
    private String upFullName;

    /**
     * 上级科目版本号
     */
    private Integer upVersion;

    /**
     * 直属下级科目数量
     */
    private Integer childNum;

    /**
     * 最大直属下级编码
     */
    private String maxChildCode;

    /**
     * 科目表名称
     */
    private String subjectName;

    /**
     * 核算币ID拼接 【币种】表ID
     */
    private String currencyIds;

    /**
     * 核算币种名称拼接 【币种】表NAME
     */
    private String currencyNames;

    /**
     * 创建公司名称 【公司】表NAME
     */
    private String createCompanyName;

    /**
     * 归属公司名称 【公司】表NAME
     */
    private String companyName;

    /**
     * 使用公司名称 【公司】表NAME
     */
    private String useCompanyName;

    /**
     * 现金流量项目预设流入流出拼接
     */
    private String cashFlowNames;

    /**
     * 科目模板表最大级次
     */
    private String maxLevel;

    /**
     * 会计科目与辅助核算关系信息列表
     */
    private List<AccountSubjectAuxiliary> accountSubjectAuxiliaryList = new ArrayList<>();

    /**
     * 科目与辅助核算配置关系信息列表
     */
    private List<SubjectAuxiliaryVo> subjectAuxiliaryList = new ArrayList<>();

    /**
     * 辅助核算数量
     */
    private Integer auxiliaryNum;

    /**
     * 辅助核算编码拼接
     */
    @ExcelCell(index = 8)
    private String auxiliaryCodes;

    /**
     * 辅助核算名称拼接
     */
    private String auxiliaryNames;

    /**
     * 辅助核算值来源拼接
     */
    private String auxiliarySources;

    /**
     * 辅助核算值来源表拼接
     */
    private String auxiliarySourceTables;

    /**
     * 辅助核算值依赖拼接
     */
    private String auxiliaryDependents;

    /**
     * 科目准则
     */
    private Subject subject;

    /**
     * 会计要素选项
     */
    private Result<List<AccountElementItemVo>> elementItemVoResult;

    /**
     * 科目信息
     */
    private List<AccountSubjectVo> accountSubjectList;

    /**
     * 期初余额
     */
    private BigDecimal openingBalance;

    /**
     * 本年借方
     */
    private BigDecimal thisYearDebitAmount;

    /**
     * 本年贷方
     */
    private BigDecimal thisYearCreditAmount;

    /**
     * 年初余额
     */
    private BigDecimal yearOpeningBalance;

    /**
     * 会计要素项ID
     */
    private Long accountElementItem;

    /**
     * 会计要素项名称
     */
    private String accountElementItemName;

    /**
     * 是否被引用 0:否 1:是
     */
    private Byte isCited;

    /**
     * 是否为列表中的第一级 0：否、1：是
     */
    private Byte isFirst;

    /**
     * 存在下级
     **/
    private Byte hasChild;

    private List<CurrencyVo> currencyVoList;

    private List<AccountSubjectAuxiliaryVo> accSubjectAuxiliaryList;

    public String getIsApprovedStr() {
        if (this.getIsApproved() == null) {
            return null;
        }
        //审核状态
        if (this.getIsApproved().equals(Constant.Is.YES)) {
            return PlatformConstant.ApprovedStatus.YES;
        } else {
            return PlatformConstant.ApprovedStatus.NO;
        }
    }

    public String getIsReleasedStr() {
        if (this.getIsReleased() == null) {
            return null;
        }
        //发布状态
        if (this.getIsReleased().equals(Constant.Is.YES)) {
            return PlatformConstant.ReleasedStatus.YES;
        } else {
            return PlatformConstant.ReleasedStatus.NO;
        }
    }

    public String getDirectionStr() {
        if (getBalanceDirection() != null) {
            switch (getBalanceDirection().intValue()) {
                case Constant.BalanceDirectionType.DEBIT:
                    return Constant.BalanceDirectionOwner.DEBIT;
                case Constant.BalanceDirectionType.CREDIT:
                    return Constant.BalanceDirectionOwner.CREDIT;
                default:
            }
        }
        return "";
    }

    public String getAccountCategoryStr() {
		if (getAccountCategory() == null) {
			return null;
		}
        switch (getAccountCategory()) {
            case Constant.AccountCategory.CASH:
                return "现金科目";
            case Constant.AccountCategory.BANK:
                return "银行科目";
            case Constant.AccountCategory.VIK:
                return "现金等价物";
            case Constant.AccountCategory.GENERAL:
                return "一般科目";
            default:
                return "";
        }
    }
}
