package com.njwd.entity.ledger.dto;

import com.njwd.entity.base.query.BaseLedgerQueryDto;
import com.njwd.entity.platform.vo.AccountSubjectAuxiliaryVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * @Author wuweiming
 * @Description 现金日记账
 * @Date:10:03 2019/08/07
 **/
@Getter
@Setter
public class BalanceSubjectCashJournalQueryDto extends BaseLedgerQueryDto {

    /**
     * 总账账簿id
     */
    private Long accountBookId;

    /**
     * 核算主体id
     */
    private Long accountBookEntityId;

    /**
     * 末级科目
     */
    private List<Long> lastSubjectList;

    /**
     * 期间年度
     */
    private Integer periodYear;

    /**
     * 期间号
     */
    private Byte periodNumber;

    /**
     * 制单日期集合
     */
    private List<String> voucherDate;

    /**
     * 制单日期
     */
    private String vocherTime;

    /**
     * 会计科目id
     */
    private Long accountSubjectId;

    /**
     * 科目id
     */
    private Long subjectId;

    /**
     * 科目名称
     */
    private String subjectName;

    /**
     * 会计科目查询类型
     */
    private Byte subjectOperator;

    /**
     * 会计科目
     */
    private List<Long> subjectList;

    /**
     * 会计科目code集合
     */
    private List<String> codeList;

    /**
     * 科目编码
     */
    private String subjectCode;

    /**
     * 会计科目级次
     */
    private List<Byte> subjectLevels;

    /**
     * 仅显示末级科目
     */
    private Byte lastSubjectLevel;

    /**
     * 显示科目全程
     */
    private Byte showSubjectFullName;

    /**
     * 显示对方科目
     */
    private Byte showOppositeSubjectName;

    /**
     * 来源表名
     */
    private String sourceTable;

    /**
     * 辅助核算值id
     */
    private Long itemValueId;

    /**
     * 辅助核算值name
     */
    private String itemValueName;

    /**
     * 余额方向(0:借,1:贷,2:平)
     */
    private Byte balanceDirection;

    /**
     * 余额方向(0:借,1:贷,2:平)
     */
    private String balanceDirectionName;

    /**
     * 期初余额表
     */
    private String initBalanceSourceTable;

    /**
     * 是否跨年(0:否，1：是)
     */
    private Byte isManyYears;

    /**
     * 来源表名集合
     */
    private List<String> sourceTables;

    /**
     * 辅助核算值id区间
     */
    private List<Long> itemValueIds;

    /**
     * 辅助核算信息
     */
    private List<AccountSubjectAuxiliaryVo> itemValueInfos;

    /**
     * 搜友辅助核算信息
     */
    private List<BalanceSubjectCashJournalQueryDto> itemValueList;

    /**
     * 是否仅显示末级科目(0:否 1:是)
     */
    private Byte isFinal;

    /**
     * 是否包含禁用科目（0：不包含，1：包含）
     */
    private Byte isConfigEnable;

    /**
     * 凭证分录ID集合
     */
    private Set<Long> ids;

}
