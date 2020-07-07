package com.njwd.entity.ledger.dto;

import com.njwd.entity.base.query.BaseLedgerQueryDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author 周鹏
 * @Description 财务报表-科目余额表
 * @Date:14:03 2019/8/2
 **/
@Getter
@Setter
public class BalanceSubjectQueryDto extends BaseLedgerQueryDto {
    /**
     * 总账账簿id
     */
    private Long accountBookId;

    /**
     * 核算主体id
     */
    private Long accountBookEntityId;

    /**
     * 会计科目id
     */
    private Long accountSubjectId;

    /**
     * 核算主体ID List
     */
    private List<Long> accountBookEntityIdList;

    /**
     * 制单日期查询类型
     */
    private Byte voucherDateOperator;

    /**
     * 制单日期区间
     */
    private List<String> voucherDates;

    /**
     * 期间年度
     */
    private Integer periodYear;

    /**
     * 期间号
     */
    private Byte periodNum;

    /**
     * 会计科目编码查询类型
     */
    private Byte subjectCodeOperator;

    /**
     * 科目表id
     */
    private Long subjectId;

    /**
     * 会计科目id集合
     */
    private List<Long> subjectIds;

    /**
     * 会计科目编码区间
     */
    private List<Long> subjectCodes;

    /**
     * 会计科目级次查询类型
     */
    private Byte subjectLevelOperator;

    /**
     * 科目级次区间
     */
    private List<Byte> subjectLevels;

    /**
     * 是否仅显示末级科目(0:否 1:是)
     */
    private Byte isFinal;

    /**
     * 是否显示科目全名(0:不显示 1:显示)
     */
    private Byte isShowFullName;

    /**
     * 是否包含禁用科目(0:不包含 1:包含)
     */
    private Byte isIncludeEnable;

    /**
     * 最近结账期间年度
     */
    private Integer lastPeriodYear;

    /**
     * 最近结账期间号
     */
    private Byte lastPeriodNum;

    /**
     * id拼接
     */
    private String ids;
}
