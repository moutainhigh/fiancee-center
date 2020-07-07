package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.ledger.Voucher;
import com.njwd.entity.ledger.dto.BalanceSubjectCashJournalQueryDto;
import com.njwd.entity.ledger.vo.BalanceSubjectCashJournalVo;
import com.njwd.entity.ledger.vo.VoucherEntryAuxiliaryVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author wuweiming
 * @since 2019/08/13
 */
public interface CashJournalMapper extends BaseMapper<Voucher> {

    /**
     * @Description 查询现金日记账
     * @Author wuweiming
     * @Data 2019/08/07 17:02
     * @Param BalanceSubjectCashJournalQueryDto
     * @return com.njwd.ledger.entity.vo.BalanceSubjectCashJournalVo
     */
    List<BalanceSubjectCashJournalVo> findCashJournalList(@Param("dto") BalanceSubjectCashJournalQueryDto dto);

    /**
     * @Description 查询辅助核算现金日记账
     * @Author wuweiming
     * @Data 2019/08/07 17:02
     * @Param BalanceSubjectCashJournalQueryDto
     * @return com.njwd.ledger.entity.vo.BalanceSubjectCashJournalVo
     */
    List<BalanceSubjectCashJournalVo> findCashJournalAuxiliaryList(@Param("dto") BalanceSubjectCashJournalQueryDto dto);

    /**
     * @Description 查询凭证信息
     * @Author wuweiming
     * @Data 2019/09/19 17:44
     * @Param BalanceSubjectCashJournalQueryDto
     * @return com.njwd.ledger.entity.vo.BalanceSubjectCashJournalVo
     */
    List<Long> findVocherListByParams(@Param("dto") BalanceSubjectCashJournalQueryDto dto);

    /**
     * @Description 查询辅助核算凭证分录信息
     * @Author wuweiming
     * @Data 2019/09/29 17:44
     * @Param BalanceSubjectCashJournalQueryDto
     * @return com.njwd.ledger.entity.vo.VoucherEntryAuxiliaryVo
     */
    List<VoucherEntryAuxiliaryVo> findVoucherEntryForAuxiary(@Param("dto") BalanceSubjectCashJournalQueryDto dto);

    /**
     * @Description 查询辅助核算信息
     * @Author wuweiming
     * @Data 2019/09/29 17:44
     * @Param Set<Long> voucherEntryAuxiliaryIds
     * @return com.njwd.ledger.entity.vo.VoucherEntryAuxiliaryVo
     */
    List<VoucherEntryAuxiliaryVo> selectAuxiliaryIteam(Set<Long> voucherEntryAuxiliaryIds);
}