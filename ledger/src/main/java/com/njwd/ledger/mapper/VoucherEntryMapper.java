package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.VoucherEntry;
import com.njwd.entity.ledger.dto.QueryVoucherEntryDto;
import com.njwd.entity.ledger.dto.VoucherDto;
import com.njwd.entity.ledger.dto.VoucherEntryDto;
import com.njwd.entity.ledger.vo.PostPeriodBalanceVo;
import com.njwd.entity.ledger.vo.VoucherEntryVo;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/7/24
 */
public interface VoucherEntryMapper extends BaseMapper<VoucherEntry> {
    /**
     * 批量插入分录
     *
     * @param editEntryList editEntryList
     * @param voucherId     voucherId
     * @author xyyxhcj@qq.com
     * @date 2019/8/5 15:18
     **/
    int insertBatch(@Param("editEntryList") List<VoucherEntryDto> editEntryList, @Param("voucherId") Long voucherId);

    /**
     * 批量修改
     *
     * @param updateEntryList updateEntryList
     * @return int
     * @author xyyxhcj@qq.com
     * @date 2019/8/14 17:12
     **/
    int updateBatch(@Param("updateEntryList") List<VoucherEntryDto> updateEntryList);



    /**
     * 过账 修改科目过账余额
     *
     * @param balanceList 科目汇总金额
     * @return
     */

    int updateVoucherBalanceForPostPeriod(@Param("balanceList") List<PostPeriodBalanceVo> balanceList);

    /**
     * @description 过账 修改科目金额前汇总金额
     * @author fancl
     * @date 2019/8/22
     * @param
     * @return
     */
    List<PostPeriodBalanceVo> findBalanceBeforeUpdateForPostPeriod(@Param("accountBookPeriod")AccountBookPeriod accountBookPeriod);

    /**
     * @Author ZhuHC
     * @Date  2019/9/17 17:28
     * @Param
     * @return
     * @Description 根据凭证ID 查询 分录和协同分录
     */
    List<VoucherEntryVo> findVoucherEntryInteriorInfo(@Param("voucherIds")List<Long> voucherIds);

    /**
     * @Author ZhuHC
     * @Date  2019/9/17 17:28
     * @Param
     * @return
     * @Description 根据凭证ID 查询 凭证和分录信息
     */
    List<VoucherEntryVo> findListWithVoucher(@Param("voucherDto") VoucherDto voucherDto);

    /**
     * 查询分录明细
     *
     * @param voucherIds voucherIds
     * @return java.util.LinkedList<com.njwd.entity.ledger.dto.VoucherEntryDto>
     * @author xyyxhcj@qq.com
     * @date 2019/8/20 9:43
     **/
    LinkedList<VoucherEntryDto> findList(@Param("voucherIds") Collection<Long> voucherIds);

    /**
     * @Author Libao
     * @Description 查询现金流量分录信息
     * @Date  2019/9/2 9:57
     * @Param [queryVoucherEntryDto]
     * @return java.util.List<com.njwd.entity.ledger.vo.VoucherEntryVo>
     */
    List<VoucherEntryVo> findCashFlowItemCashReport(@Param("queryVoucherEntryDto") QueryVoucherEntryDto queryVoucherEntryDto);

    /**
     * @Author Libao
     * @Description 查询非现金流量分录信息
     * @Date  2019/9/2 9:57
     * @Param [queryVoucherEntryDto]
     * @return java.util.List<com.njwd.entity.ledger.vo.VoucherEntryVo>
     */
    List<VoucherEntryVo> findCashFlowItemUnCashReport(@Param("queryVoucherEntryDto") QueryVoucherEntryDto queryVoucherEntryDto);

    /**
     * @Author Libao
     * @Description 查询现金流量明细报表
     * @Date  2019/9/2 9:57
     * @Param [queryVoucherEntryDto]
     * @return java.util.List<com.njwd.entity.ledger.vo.VoucherEntryVo>
     */
    List<VoucherEntryVo> findCashFlowItemDetailReport(@Param("queryVoucherEntryDto") QueryVoucherEntryDto queryVoucherEntryDto);

    /**
     * @Author ZhuHC
     * @Date  2019/9/29 18:31
     * @Param
     * @return
     * @Description 根据条件查询分录
     */
    List<VoucherEntryVo> findListByRules(@Param("voucherDto") VoucherDto voucherDto);
}