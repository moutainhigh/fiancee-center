package com.njwd.entity.ledger.dto;

import com.njwd.entity.ledger.BalanceSubjectInitAuxiliaryItem;
import com.njwd.entity.ledger.vo.BalanceSubjectInitAuxiliaryVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author lj
 * @Description
 * @Date:10:11 2019/8/14
 **/
@Getter
@Setter
public class BalanceSubjectInitAuxiliaryDto extends BalanceSubjectInitAuxiliaryVo {

    /**
     *批量插入科目期初辅助核算初始化项目
     **/
    private List<BalanceSubjectInitAuxiliaryItem> balanceSubjectInitAuxItemList;

    /**
     * 子系统启用记录ID
     */
    private Long accountBookSystemId;
}
