package com.njwd.ledger.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.njwd.entity.ledger.VoucherEntryInterior;
import com.njwd.entity.ledger.dto.VoucherEntryDto;
import com.njwd.ledger.mapper.VoucherEntryInteriorMapper;
import com.njwd.ledger.service.VoucherEntryInteriorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/08/26
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class VoucherEntryInteriorServiceImpl extends ServiceImpl<VoucherEntryInteriorMapper, VoucherEntryInterior> implements VoucherEntryInteriorService {
    @Resource
    private VoucherEntryInteriorMapper voucherEntryInteriorMapper;

    @Override
    public int insertInteriorRelation(List<VoucherEntryDto> interiorGenerateEntryList, Long voucherId, Long interiorVoucherId) {
        return voucherEntryInteriorMapper.insertInteriorRelation(interiorGenerateEntryList, voucherId, interiorVoucherId);
    }
}
