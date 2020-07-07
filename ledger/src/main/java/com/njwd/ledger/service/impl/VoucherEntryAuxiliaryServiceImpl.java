package com.njwd.ledger.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.njwd.entity.ledger.VoucherEntryAuxiliary;
import com.njwd.entity.ledger.dto.VoucherEntryAuxiliaryDto;
import com.njwd.ledger.mapper.VoucherEntryAuxiliaryMapper;
import com.njwd.ledger.service.VoucherEntryAuxiliaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/08/09
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class VoucherEntryAuxiliaryServiceImpl extends ServiceImpl<VoucherEntryAuxiliaryMapper, VoucherEntryAuxiliary> implements VoucherEntryAuxiliaryService {

    @Autowired
    VoucherEntryAuxiliaryMapper voucherEntryAuxiliaryMapper;


    @Override
    public LinkedList<VoucherEntryAuxiliaryDto> findList(Collection<Long> voucherIds) {
        return voucherEntryAuxiliaryMapper.findList(voucherIds);
    }

}
