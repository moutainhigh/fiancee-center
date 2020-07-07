package com.njwd.entity.ledger.dto;

import com.njwd.entity.ledger.vo.VoucherEntryAuxiliaryVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/07/30
 */
@Getter
@Setter
public class VoucherEntryAuxiliaryDto extends VoucherEntryAuxiliaryVo {
    private static final long serialVersionUID = 7408093773068096048L;

    private List<VoucherEntryAuxiliaryDto> sourceTableAndIdList;

    /**
     * 辅助核算项 list
     */
    private List<Long> itemValueIdList;

    public StringBuilder getSign() {
        StringBuilder sign = new StringBuilder();
        return sign.append(getSourceTable()).append(getItemValueId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSign().toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VoucherEntryAuxiliaryDto that = (VoucherEntryAuxiliaryDto) o;
        return getSign().toString().equals(that.getSign().toString());
    }
}
