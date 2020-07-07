package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.ledger.Balance;
import com.njwd.entity.ledger.BalanceSubjectAuxiliaryItem;
import com.njwd.entity.ledger.BalanceSubjectInitAuxiliaryItem;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.ledger.dto.BalanceSubjectAuxiliaryDto;
import com.njwd.entity.ledger.dto.BalanceSubjectAuxiliaryItemQueryDto;
import com.njwd.entity.ledger.dto.VoucherDto;
import com.njwd.entity.ledger.vo.BalanceSubjectAuxiliaryItemVo;
import com.njwd.entity.ledger.vo.BalanceSubjectAuxiliaryVo;
import com.njwd.entity.ledger.vo.PostPeriodBalanceVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/7/24
 */
public interface BalanceSubjectAuxiliaryItemMapper extends BaseMapper<BalanceSubjectAuxiliaryItem> {
    int addBalanceSubAuxItem(@Param("item") BalanceSubjectInitAuxiliaryItem item);

    int addBalanceSubAuxItemOne(@Param("item") BalanceSubjectInitAuxiliaryItem item);

    int addBalanceSubAuxItemZero(@Param("item") BalanceSubjectInitAuxiliaryItem item);

    /**
     * 批量插入
     *
     * @param balanceSubjectAuxiliaryItems balanceSubjectAuxiliaryItems
     * @param balanceAuxiliaryId           balanceAuxiliaryId
     * @param voucherDto                   voucherDto
     * @return id
     * @author xyyxhcj@qq.com
     * @date 2019/8/14 13:40
     **/
    int insertBatch(@Param("balanceSubjectAuxiliaryItems") List<BalanceSubjectAuxiliaryItem> balanceSubjectAuxiliaryItems, @Param("balanceAuxiliaryId") Long balanceAuxiliaryId, @Param("voucherDto") VoucherDto voucherDto);

    int insertBatch(@Param("balanceSubjectAuxiliaryItems") Set<BalanceSubjectAuxiliaryItem> balanceSubjectAuxiliaryItems, @Param("balanceAuxiliaryId") Long balanceAuxiliaryId, @Param("voucherDto") VoucherDto voucherDto);

    /**
     * 根据条件查询辅助核算项列表
     *
     * @param auxiliaryItemQueryDto
     * @return
     */
    List<BalanceSubjectAuxiliaryItemVo> findListByParam(@Param("auxiliaryItemQueryDto") BalanceSubjectAuxiliaryItemQueryDto auxiliaryItemQueryDto);

    /**
     * 根据条件查询辅助核算项启用期间的balance_auxiliary_id
     *
     * @param list
     * @return
     */
    List<BalanceSubjectAuxiliaryItemVo> findStartIdListByParam(@Param("list") List<BalanceSubjectAuxiliaryVo> list);

    /**
     * 根据条件查询辅助核算项列表
     *
     * @param auxiliaryItemQueryDto
     * @return
     */
    List<BalanceSubjectAuxiliaryItemVo> findListByAuxiliaries(@Param("auxiliaryItemQueryDto") BalanceSubjectAuxiliaryItemQueryDto auxiliaryItemQueryDto);

    /**
     * 根据条件查询辅助核算项列表
     *
     * @param auxiliaryItemQueryDtoe
     * @return
     */
    List<BalanceSubjectAuxiliaryItemVo> findListByAuxiliariesAndPeriod(@Param("auxiliaryItemQueryDto") BalanceSubjectAuxiliaryItemQueryDto auxiliaryItemQueryDtoe);
    /**
    * @Description 查询带辅助核算项目的科目
    * @Author 朱小明
    * @Date 2019/8/29
    * @param accountBookPeriod
    * @return java.util.List<com.njwd.entity.ledger.BalanceSubjectAuxiliaryItem>
    **/
    List<BalanceSubjectAuxiliaryItem> selectAuxiliaryIteamList(@Param("accountBookPeriod") AccountBookPeriodDto accountBookPeriod);

    /**
    * @Description 根据期间查询辅助核算
    * @Author 朱小明
    * @Date 2019/8/29
    * @param balanceSubjectAuxiliaryDto
    * @return java.util.List<com.njwd.entity.ledger.vo.BalanceSubjectAuxiliaryItemVo>
    **/
    List<BalanceSubjectAuxiliaryItemVo> selectAuxiliaryBalanceAsList(@Param("balanceSubjectAuxiliaryDto") BalanceSubjectAuxiliaryDto balanceSubjectAuxiliaryDto);

    /**
    * @Description 查询指定期间的辅助核算项目
    * @Author 朱小明
    * @Date 2019/9/25
    * @param accountBookPeriodDto
    * @return java.util.List<com.njwd.entity.ledger.BalanceSubjectAuxiliaryItem>
    **/
    List<BalanceSubjectAuxiliaryItem> selectSubjctAuxiliaryItemList(@Param("accountBookPeriod") AccountBookPeriodDto accountBookPeriodDto);


    /**
     * @description 查询辅助核算项明细
     * @author fancl
     * @date 2019/10/6
     * @param
     * @return
     */
    List<PostPeriodBalanceVo> findAuxItemForPostPeriod(@Param("balance") Balance balance);
}