package com.njwd.entity.base.query;

import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.AccountBookEntityDto;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author 周鹏
 * @Description 总账报表查询通用Dto
 * @Date:14:03 2019/8/7
 **/
@Data
public class BaseLedgerQueryDto {
    /**
     * 总账账簿id查询类型
     */
    private Byte accountBookIdOperator;

    /**
     * 总账账簿id区间
     */
    private List<Long> accountBookIds;

    /**
     * 核算主体id查询类型
     */
    private Byte accountBookEntityIdOperator;

    /**
     * 核算主体id区间
     */
    private List<Long> accountBookEntityIds;

    /**
     * 核算主体相关信息集合
     */
    private List<AccountBookEntityDto> accountBookEntityList;

    /**
     * 核算账簿相关信息集合
     */
    private List<AccountBookDto> accountBookList;

    /**
     * 会计期间查询类型
     */
    private Byte periodOperator;

    /**
     * 期间年度区间
     */
    private List<Integer> periodYears;

    /**
     * 期间号区间
     */
    private List<Byte> periodNumbers;

    /**
     * 是否包含未记账凭证(0:不包含 1:包含)
     */
    private Byte isIncludeUnbooked;

    /**
     * 是否包含损益结转凭证(0:不包含 1:包含)
     */
    private Byte isIncludeProfitAndLoss;

    /**
     * 是否显示辅助核算明细(0:不显示 1:显示)
     */
    private Byte isShowAuxiliaryDetail;

    /**
     * 显示条件(0:本期无发生不显示 1:余额为零不显示 2:余额为零且本期无发生不显示)
     */
    private Byte showCondition;

    /**
     * 租户id
     */
    private Long rootEnterpriseId;

}
