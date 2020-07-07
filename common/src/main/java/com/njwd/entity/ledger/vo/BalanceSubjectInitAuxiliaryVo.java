package com.njwd.entity.ledger.vo;

import com.njwd.entity.ledger.BalanceSubjectInitAuxiliary;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author lj
 * @Description
 * @Date:10:09 2019/8/14
 **/
@Getter
@Setter
public class BalanceSubjectInitAuxiliaryVo extends BalanceSubjectInitAuxiliary {
    /**
     * 余额方向 0：借方、1：贷方
     */
    private Byte balanceDirection;

    /**
     * 科目编码
     */
    private String accountSubjectCode;

    /**
     * 核算主体NAME
     */
    private String accountBookEntityName;

    /**
     * 核算主体CODE
     */
    private String accountBookEntityCode;
}
