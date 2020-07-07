package com.njwd.ledger.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.VoucherEntry;
import com.njwd.entity.ledger.dto.VoucherDto;
import com.njwd.entity.ledger.dto.VoucherEntryDto;
import com.njwd.entity.ledger.vo.PostPeriodBalanceVo;
import com.njwd.entity.ledger.vo.VoucherEntryVo;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * 分录
 *
 * @author xyyxhcj@qq.com
 * @since 2019-08-09
 */

public interface VoucherEntryService extends IService<VoucherEntry> {

    /**
     * 过账 修改科目过账余额
     * @param balanceList
     * @return
     */
    int updateVoucherBalanceForPostPeriod(List<PostPeriodBalanceVo> balanceList );

    /**
     * 批量插入分录
     *
     * @param editEntryList editEntryList
     * @param voucherId     voucherId
     * @return int
     * @author xyyxhcj@qq.com
     * @date 2019/8/14 16:54
     **/
    int insertBatch(List<VoucherEntryDto> editEntryList, Long voucherId);

    /**
     * 批量插入或修改分录
     *
     * @param editEntryList editEntryList
     * @param voucherId     voucherId
     * @param unchangedIds  修改时收集无变更ids
     * @param entryWrapper
     * @author xyyxhcj@qq.com
     * @date 2019/8/14 17:07
     **/
    void insertOrUpdateBatch(List<VoucherEntryDto> editEntryList, Long voucherId, List<Long> unchangedIds, LambdaQueryWrapper<VoucherEntry> entryWrapper);

    /**
     * @Author ZhuHC
     * @Date  2019/9/17 17:28
     * @Param
     * @return
     * @Description 根据凭证ID 查询 分录和协同分录
     */
    List<VoucherEntryVo> findVoucherEntryInteriorInfo(List<Long> voucherIds);

    /**
     * @Author ZhuHC
     * @Date  2019/9/17 17:28
     * @Param
     * @return
     * @Description 根据凭证ID 查询 凭证和分录信息
     */
    List<VoucherEntryVo> findListWithVoucher(VoucherDto voucherDto);

    /**
     * @description 过账 查询科目汇总金额
     * @author fancl
     * @date 2019/8/22
     * @param
     * @return
     */
    List<PostPeriodBalanceVo> findBalanceBeforeUpdateForPostPeriod(AccountBookPeriod accountBookPeriod);
    /**
     * 查询所有分录明细
     *
     * @param voucherIds voucherIds
     * @return java.util.LinkedList<com.njwd.entity.ledger.dto.VoucherEntryDto>
     * @author xyyxhcj@qq.com
     * @date 2019/8/20 9:39
     **/
    LinkedList<VoucherEntryDto> findList(Collection<Long> voucherIds);
}
