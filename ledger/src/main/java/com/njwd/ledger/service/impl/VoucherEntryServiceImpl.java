package com.njwd.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.njwd.common.Constant;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.VoucherEntry;
import com.njwd.entity.ledger.dto.VoucherDto;
import com.njwd.entity.ledger.dto.VoucherEntryDto;
import com.njwd.entity.ledger.vo.PostPeriodBalanceVo;
import com.njwd.entity.ledger.vo.VoucherEntryVo;
import com.njwd.ledger.mapper.VoucherEntryMapper;
import com.njwd.ledger.service.VoucherEntryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/08/09
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class VoucherEntryServiceImpl extends ServiceImpl<VoucherEntryMapper, VoucherEntry> implements VoucherEntryService {

    @Resource
    private VoucherEntryMapper voucherEntryMapper;

    /**
     * 过账 修改科目过账余额
     * @param balanceList
     * @return
     */
    @Override
    public int updateVoucherBalanceForPostPeriod(List<PostPeriodBalanceVo> balanceList) {

        return voucherEntryMapper.updateVoucherBalanceForPostPeriod(balanceList);
    }

    @Override
    public int insertBatch(List<VoucherEntryDto> editEntryList, Long voucherId) {
        return voucherEntryMapper.insertBatch(editEntryList, voucherId);
    }

    @Override
    public void insertOrUpdateBatch(List<VoucherEntryDto> editEntryList, Long voucherId, List<Long> unchangedIds, LambdaQueryWrapper<VoucherEntry> entryWrapper) {
        List<VoucherEntryDto> modifyEntryList = new LinkedList<>();
        boolean isCollectUnchangedIds = unchangedIds != null;
        // 只修改序号
        List<VoucherEntry> editRowNumList = new LinkedList<>();
        for (VoucherEntryDto entryDto : editEntryList) {
            Long entryId = entryDto.getId();
            boolean isModify = Constant.Is.YES.equals(entryDto.getIsModify());
            boolean isNew = entryId == null;
            if (isNew || isModify) {
                // 新增/修改
                modifyEntryList.add(entryDto);
            } else if (isCollectUnchangedIds) {
                unchangedIds.add(entryId);
            }
            Integer newRowNum = entryDto.getNewRowNum();
            if (!isNew && !isModify && newRowNum != null) {
                // 仅变更序号
                VoucherEntry editRowNum = new VoucherEntry();
                editRowNum.setId(entryId);
                editRowNum.setRowNum(newRowNum);
                editRowNumList.add(editRowNum);
            }
        }
        if (isCollectUnchangedIds && !unchangedIds.isEmpty()) {
            entryWrapper.notIn(VoucherEntry::getId, unchangedIds);
        }
        voucherEntryMapper.delete(entryWrapper);
        if (!modifyEntryList.isEmpty()) {
            voucherEntryMapper.insertBatch(modifyEntryList, voucherId);
        }
        if (!editRowNumList.isEmpty()) {
            updateBatchById(editRowNumList);
        }
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/17 17:28
     * @Param
     * @return
     * @Description 根据凭证ID 查询 分录和协同分录
     */
    @Override
    public List<VoucherEntryVo> findVoucherEntryInteriorInfo(List<Long> voucherIds) {
        return voucherEntryMapper.findVoucherEntryInteriorInfo(voucherIds);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/17 17:28
     * @Param
     * @return
     * @Description 根据凭证ID 查询 凭证和分录信息
     */
    @Override
    public List<VoucherEntryVo> findListWithVoucher(VoucherDto voucherDto) {
        return voucherEntryMapper.findListWithVoucher(voucherDto);
    }
    /**
     * @description 过账 修改科目金额前 查询
     * @author fancl
     * @date 2019/8/22
     * @param
     * @return
     */
    @Override
    public List<PostPeriodBalanceVo> findBalanceBeforeUpdateForPostPeriod(AccountBookPeriod accountBookPeriod) {
        return voucherEntryMapper.findBalanceBeforeUpdateForPostPeriod(accountBookPeriod);
    }

    @Override
    public LinkedList<VoucherEntryDto> findList(Collection<Long> voucherIds) {
        return voucherEntryMapper.findList(voucherIds);
    }
}
