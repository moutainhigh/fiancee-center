package com.njwd.entity.ledger.dto;

import com.njwd.entity.base.query.BaseLedgerQueryDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author 周鹏
 * @since 2019/8/15
 */
@Getter
@Setter
public class BalanceSubjectAuxiliaryItemQueryDto extends BaseLedgerQueryDto {
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
     * 期间年度
     */
    private Integer periodYear;

    /**
     * 期间号
     */
    private Byte periodNum;

    /**
     * 核算值id
     */
    private Long itemValueId;

    /**
     * 核算值id拼接
     */
    private String itemValueIds;

    /**
     * 辅助核算余额明细ID拼接
     */
    private String balanceAuxiliaryIds;

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
    private List<Long> subjectLevels;

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
     * 是否显示辅助核算项小计(0:不显示 1:显示)
     */
    private Byte isShowAuxiliaryCount;

    /**
     * 辅助核算项表名
     */
    private String sourceTable;

    /**
     * 辅助核算项表名拼接
     */
    private String sourceTables;

    /**
     * 辅助核算项id
     */
    private List<Long> itemValueIdList;

    /**
     * 辅助核算项表名与id对应关系集合
     */
    private List<BalanceSubjectAuxiliaryItemQueryDto> sourceTableAndIdList;
}