package com.njwd.entity.ledger.vo;

import com.njwd.entity.ledger.BalanceSubjectAuxiliary;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 科目辅助核算项目余额
 *
 * @author zhuzs
 * @date 2019-08-09 14:29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BalanceSubjectAuxiliaryVo extends BalanceSubjectAuxiliary {
    /**
     * 总账账簿名称
     */
    private String accountBookName;

    /**
     * 核算主体名称
     */
    private String accountBookEntityName;

    /**
     * 余额方向 0：借方、1：贷方
     */
    private Byte balanceDirection;

    /**
     * 期初余额方向
     */
    private String openingDirectionName;

    /**
     * 期末余额方向
     */
    private String closingDirectionName;

    /**
     * 启用标识 0：禁用、1：启用
     */
    private Byte isEnable;

    /**
     * 科目编码
     */
    private String code;

    /**
     * 科目名称
     */
    private String name;

    /**
     * 辅助核算表名
     */
    private String sourceTable;

    /**
     * 辅助核算表名拼接
     */
    private String sourceTables;

    /**
     * 辅助核算id拼接
     */
    private String itemValueIds;

    /**
     * 辅助核算编码
     */
    private String auxiliaryCode;

    /**
     * 辅助核算名称
     */
    private String auxiliaryName;

    /**
     * 辅助核算id
     */
    private Long itemValueId;

    /**
     * 辅助核算项唯一标识
     **/
    private String keySign;

    /**
     * 上期的balanceId,用于下期无数据时获取本期辅助核算列表
     **/
    private Long preBalanceId;

    /**
     * id拼接
     */
    private String ids;

    /**
     * 账簿启用期间
     */
    private Integer startPeriod;

    /**
     * 辅助核算余额明细ID集合
     */
    private List<String> auxiliaryIds;

    /**
     * 辅助核算来源表集合
     */
    private List<String> sourceTableList;

    /**
     * 辅助核算值来源集合
     */
    private List<String> itemValueIdList;

    /**
     * 账簿启用期间金额信息
     */
    private BalanceSubjectAuxiliaryVo startPeriodBalanceVo;

    /**
     * 科目余额信息
     */
    private BalanceSubjectVo balanceSubjectVo;

    /**
     * 开始期间号
     */
    private Byte beginNumber;

    /**
     * 结束期间号
     */
    private Byte endNumber;

    /**
     * 开始期间后最近的已结账期间
     */
    private AccountBookPeriodVo beginSettledPeriodVo;

    /**
     * 结束期间前最近的已结账期间
     */
    private AccountBookPeriodVo endSettledPeriodVo;

    /**
     * 开始期间
     */
    private Integer beginPeriod;

    /**
     * 结束期间
     */
    private Integer endPeriod;

    /**
     * 结束期间前最近的已结账期间
     */
    private Integer endSettledPeriod;
}

