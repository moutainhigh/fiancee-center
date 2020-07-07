package com.njwd.entity.ledger.dto;

import com.njwd.entity.ledger.vo.BalanceSubjectCashJournalVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author wuweiming
 * @Description 现金日记账
 * @Date:10:03 2019/08/07
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class BalanceSubjectCashJournalDto extends BalanceSubjectCashJournalVo {

    //账簿ID
    private Long accountBookId;

    //核算主体
    private List<Long> entityList;

    //末级科目
    private List<Long> subjectList;

    //记账期间年
    private Long periodYear[];

    //记账期间号
    private Long periodNum[];

    //制单日期
    private String voucherDate;

    //会计科目
    private Long subjectIds[];

    //科目编码
    private String subjectCode;

    //会计科目比较符
    private Byte subjectIdsOperator;

    //科目级次
    private Long subjectLevel[];

    //仅显示末级科目
    private Byte lastSubjectLevel;

    //包含未记账凭证（0：不包含，1包含）
    private Byte isPostingStatus;

    //余额为零是否显示（0：不显示，1：显示）
    private Byte isShowBalanceZero;

    //隐藏（0：本期无发生不显示1：本期无发生显示）
    private Byte isShowOrHide;

    //显示科目全程
    private Byte showSubjectFullName;

    //显示对方科目
    private Byte showOppositeSubjectName;

    //来源表名
    private String sourceTable;

    //辅助核算值id
    private Long itemValueId;
}
