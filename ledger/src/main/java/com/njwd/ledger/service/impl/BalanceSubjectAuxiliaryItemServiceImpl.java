package com.njwd.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.njwd.common.Constant;
import com.njwd.entity.ledger.BalanceSubject;
import com.njwd.entity.ledger.BalanceSubjectAuxiliaryItem;
import com.njwd.entity.ledger.dto.BalanceSubjectAuxiliaryItemQueryDto;
import com.njwd.entity.ledger.dto.BalanceSubjectQueryDto;
import com.njwd.entity.ledger.vo.BalanceSubjectAuxiliaryItemVo;
import com.njwd.entity.ledger.vo.BalanceSubjectVo;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.ledger.mapper.BalanceSubjectAuxiliaryItemMapper;
import com.njwd.ledger.mapper.BalanceSubjectMapper;
import com.njwd.ledger.service.BalanceSubjectAuxiliaryItemService;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.MergeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/08/13
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BalanceSubjectAuxiliaryItemServiceImpl extends ServiceImpl<BalanceSubjectAuxiliaryItemMapper, BalanceSubjectAuxiliaryItem> implements BalanceSubjectAuxiliaryItemService {
    @Resource
    private BalanceSubjectAuxiliaryItemMapper balanceSubjectAuxiliaryItemMapper;
    /**
     * @param balanceSubjectAuxiliaryItemQueryDto
     * @return com.njwd.entity.ledger.BalanceSubjectAuxiliaryItem
     * @description: 根据核算账簿id, 项目id和来源表查询科目期初辅助核算明细
     * @Param [balanceSubjectAuxiliaryItemQueryDto]
     * @author LuoY
     * @date 2019/8/23 16:25
     */
    @Override
    public BalanceSubjectAuxiliaryItem findByAccountBookIdAndItemId(BalanceSubjectAuxiliaryItemQueryDto balanceSubjectAuxiliaryItemQueryDto) {
        return balanceSubjectAuxiliaryItemMapper.selectOne(new LambdaQueryWrapper<BalanceSubjectAuxiliaryItem>().
                eq(BalanceSubjectAuxiliaryItem::getAccountBookId,balanceSubjectAuxiliaryItemQueryDto.getAccountBookId()).
                eq(BalanceSubjectAuxiliaryItem::getSourceTable,balanceSubjectAuxiliaryItemQueryDto.getSourceTables()).
                eq(BalanceSubjectAuxiliaryItem::getItemValueId,balanceSubjectAuxiliaryItemQueryDto.getItemValueId()));
    }
}
