package com.njwd.ledger.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.njwd.entity.ledger.VoucherEntryInterior;
import com.njwd.entity.ledger.dto.VoucherEntryDto;

import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019-08-26
 */

public interface VoucherEntryInteriorService extends IService<VoucherEntryInterior> {
    /**
     * 批量插入协同分录关联信息
     *
     * @param interiorGenerateEntryList interiorGenerateEntryList
     * @param voucherId                 voucherId
     * @param interiorVoucherId         interiorVoucherId
     * @return int
     * @author xyyxhcj@qq.com
     * @date 2019/8/16 10:31
     **/
    int insertInteriorRelation(List<VoucherEntryDto> interiorGenerateEntryList, Long voucherId, Long interiorVoucherId);
}
