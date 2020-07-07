package com.njwd.entity.ledger.dto;

import com.njwd.entity.ledger.BalanceSubjectAuxiliary;
import com.njwd.entity.ledger.BalanceSubjectAuxiliaryItem;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 科目辅助核算项目余额
 *
 * @author zhuzs
 * @date 2019-08-09 14:29
 */
@Getter
@Setter
@ToString(callSuper = true)
public class BalanceSubjectAuxiliaryDto extends BalanceSubjectAuxiliary {
    /**
     * 核算主体ID List
     */
    private List<Long> accountBookEntityIdList;
    /**
     * 凭证分录对应的辅助核算项数据
     **/
    private List<VoucherEntryAuxiliaryDto> voucherEntryAuxiliaryList;
    /**
     * 辅助核算项明细
     **/
    private Set<BalanceSubjectAuxiliaryItem> balanceSubjectAuxiliaryItems = new HashSet<>();
    /**
     * 辅助核算项拼接标识
     **/
    private StringBuilder keySign;

    /**
     * 辅助核算项拼接标识
     **/
    private String keySigns;

    /**
     * 是否更新已过账数据 1是0否
     **/
    private byte isPost;

    /**
     * 是否更新损益数据 1是0否
     **/
    private byte isSy;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BalanceSubjectAuxiliaryDto that = (BalanceSubjectAuxiliaryDto) o;
        return keySign.toString().equals(that.keySign.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(keySign.toString());
    }


}

