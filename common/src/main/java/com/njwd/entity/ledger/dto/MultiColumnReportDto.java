package com.njwd.entity.ledger.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/31 11:52
 */
@Getter
@Setter
public class MultiColumnReportDto implements Serializable {

    private static final long serialVersionUID = -2138260312713644909L;

    /**
     * 多栏账方案ID
     **/
    private Long schemeId;

    /**
     * 企业ID
     **/
    private Long rootEnterpriseId;

    /**
     * 总账账簿id
     */
    private Long accountBookId;

    /**
     * 核算主体id区间
     */
    private List<Long> accountBookEntityIds;

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
     * 会计科目ID
     */
    private List<Long> accountSubjectIds;

    /**
     * 凭证状态
     */
    private List<Byte> voucherStatusList;

    /**
     * 来源方式
     */
    private List<Byte> voucherSourceTypes;

    /**
     * 来源表
     */
    private String sourceTable;

    /**
     * 辅助核算主键
     */
    private List<Long> itemValueIds;


    public void addVoucherStatus(Byte voucherStatus){
        if(voucherStatusList==null)
            voucherStatusList = new ArrayList<>();
        voucherStatusList.add(voucherStatus);
    }
    public void addVoucherSourceType(Byte sourceType){
        if(voucherSourceTypes==null)
            voucherSourceTypes = new ArrayList<>();
        voucherSourceTypes.add(sourceType);
    }


}
