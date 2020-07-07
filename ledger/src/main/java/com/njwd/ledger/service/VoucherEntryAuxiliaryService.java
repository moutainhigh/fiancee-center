package com.njwd.ledger.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.njwd.entity.ledger.VoucherEntryAuxiliary;
import com.njwd.entity.ledger.dto.VoucherEntryAuxiliaryDto;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author xyyxhcj@qq.com
 * @since 2019-08-09
 */
public interface VoucherEntryAuxiliaryService extends IService<VoucherEntryAuxiliary> {

    /**
     * 查询核算明细
     *
     * @param voucherIds voucherIds
     * @return java.util.List<com.njwd.entity.ledger.dto.VoucherEntryAuxiliaryDto>
     * @author xyyxhcj@qq.com
     * @date 2019/8/20 9:46
     **/
    LinkedList<VoucherEntryAuxiliaryDto> findList(Collection<Long> voucherIds);

}
