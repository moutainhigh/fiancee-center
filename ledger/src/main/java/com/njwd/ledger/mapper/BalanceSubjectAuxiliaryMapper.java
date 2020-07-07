package com.njwd.ledger.mapper;

import com.njwd.entity.ledger.BalanceSubjectAuxiliary;
import com.njwd.entity.ledger.BalanceSubjectInitAuxiliary;
import com.njwd.entity.ledger.dto.*;
import com.njwd.entity.ledger.vo.BalanceSubjectAuxiliaryVo;
import com.njwd.entity.ledger.vo.BalanceSubjectVo;
import com.njwd.entity.ledger.vo.PostPeriodBalanceVo;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 科目辅助核算项目余额
 *
 * @author xyyxhcj@qq.com
 * @since 2019/7/24
 */
public interface BalanceSubjectAuxiliaryMapper extends BalanceMapper<BalanceSubjectAuxiliary> {
    int addSubjectInitAuxiliary(@Param("balSubjectInitAux") BalanceSubjectInitAuxiliary balSubjectInitAux);

    int addSubjectInitAuxiliaryOne(@Param("balSubjectInitAux") BalanceSubjectInitAuxiliary balSubjectInitAux);

    int addSubjectInitAuxiliaryZero(@Param("balSubjectInitAux") BalanceSubjectInitAuxiliary balSubjectInitAux);

    /**
     * 批量插入
     *
     * @param balanceSubjectAuxiliaries balanceSubjectAuxiliaries
     * @param voucherDto                voucherDto
     * @return id
     * @author xyyxhcj@qq.com
     * @date 2019/8/14 12:22
     **/
    int insertBatch(@Param("balanceSubjectAuxiliaries") List<BalanceSubjectAuxiliaryDto> balanceSubjectAuxiliaries, @Param("voucherDto") VoucherDto voucherDto);

    /**
     * 更新发生额
     *
     * @param balanceSubjectAuxiliaries balanceSubjectAuxiliaries
     * @param voucherDto                voucherDto
     * @param updateType                updateType
     * @return int
     * @author xyyxhcj@qq.com
     * @date 2019/8/14 13:50
     **/
    int updateBatch(@Param("balanceSubjectAuxiliaries") Collection<BalanceSubjectAuxiliaryDto> balanceSubjectAuxiliaries, @Param("voucherDto") VoucherDto voucherDto, @Param("updateType") byte updateType);

    /**
     * 更新损益凭证发生额
     *
     * @param balanceSubjectAuxiliaries balanceSubjectAuxiliaries
     * @param updateType                updateType
     * @return int
     * @author xyyxhcj@qq.com
     * @date 2019/8/14 13:50
     **/
    int updateBatchForProfitLoss(@Param("balanceSubjectAuxiliaries") Collection<BalanceSubjectAuxiliaryDto> balanceSubjectAuxiliaries, @Param("updateType") byte updateType);

    int updateSubjectInitAuxiliaryOne(@Param("balSubjectInitAux") BalanceSubjectInitAuxiliary balSubjectInitAux);

    int updateSubjectInitAuxiliaryAdd(@Param("balSubjectInitAux") BalanceSubjectInitAuxiliary balSubjectInitAux);

    int updateSubjectInitAuxiliaryDel(@Param("balSubjectInitAux") BalanceSubjectInitAuxiliary balSubjectInitAux);

    int updateSubjectInitAuxiliaryDelOne(@Param("balSubjectInitAux") BalanceSubjectInitAuxiliary balSubjectInitAux);

    /**
     * 重分类 包含未过账
     *
     * @param accountSubjectVoList
     * @return
     */
    BalanceSubjectVo findBySubjectIdList(@Param("accountSubjectVoList") List<AccountSubjectVo> accountSubjectVoList, @Param("balanceDto") BalanceDto balanceDto);

    /**
     * 重分类 不包含未过账
     *
     * @param accountSubjectVoList
     * @return
     */
    BalanceSubjectVo findPostingBySubjectIdList(@Param("accountSubjectVoList") List<AccountSubjectVo> accountSubjectVoList, @Param("balanceDto") BalanceDto balanceDto);

    /**
     * 根据辅助核算项查询辅助核算余额信息
     *
     * @param auxiliaryItemQueryDto
     * @return
     */
    List<BalanceSubjectAuxiliaryVo> findInfoByAuxiliaryItem(@Param("auxiliaryItemQueryDto") BalanceSubjectAuxiliaryItemQueryDto auxiliaryItemQueryDto);

    /**
     * 查询单个期间的余额信息
     *
     * @param auxiliaryItemQueryDto
     * @return
     */
    BalanceSubjectAuxiliaryVo findInfoByPeriod(@Param("auxiliaryItemQueryDto") BalanceSubjectAuxiliaryItemQueryDto auxiliaryItemQueryDto);

    /**
     * 查询多个期间的余额信息
     *
     * @param auxiliaryItemQueryDto
     * @return
     */
    List<BalanceSubjectAuxiliaryVo> findInfoByPeriods(@Param("auxiliaryItemQueryDto") BalanceSubjectAuxiliaryItemQueryDto auxiliaryItemQueryDto);

    /**
     * 查询账簿辅助核算的启用期间及金额信息
     *
     * @param list
     * @return
     */
    List<BalanceSubjectAuxiliaryVo> findStartPeriodBalance(@Param("list") List<BalanceSubjectAuxiliaryVo> list);

    /**
     * 查询当前账簿下每个核算主体的第一期数据
     *
     * @param dto
     * @return
     * @Author wuweiming
     */
    BalanceSubjectAuxiliaryVo findFirstBalanceSubjectAuxiliaryByParams(@Param("dto") BalanceSubjectAuxiliaryItemQueryDto dto);

    /**
     * 查询每个核算主体期间区间的科目余额信息
     *
     * @param dto
     * @return
     * @Author wuweiming
     */
    List<BalanceSubjectAuxiliaryVo> findBalanceSubjectAuxiliaryByParams(@Param("dto") BalanceSubjectAuxiliaryItemQueryDto dto);

    /**
     * @param accountBookPeriod
     * @return java.util.List<com.njwd.entity.ledger.BalanceSubject>
     * @Description 查询指定期间和其上一个期间的辅助核算余额
     * @Author 朱小明
     * @Date 2019/8/29
     **/
    List<BalanceSubjectAuxiliaryDto> selectSubjctAuxiliaryBalanceList(@Param("accountBookPeriod") AccountBookPeriodDto accountBookPeriod);

    /**
     * @param accountBookPeriod
     * @return java.util.List<com.njwd.entity.ledger.BalanceSubjectAuxiliary>
     * @Description 根据subjectList查询辅助核算余额表信息
     * @Author 朱小明
     * @Date 2019/9/6
     **/

    List<BalanceSubjectAuxiliaryDto> selectAuxiliaryList(@Param("accountBookPeriod") AccountBookPeriodDto accountBookPeriod);

    /**
     * 插入下期期初数据
     *
     * @param nextWithAuxiliaryBalanceSubjectList nextWithAuxiliaryBalanceSubjectList
     * @return int
     * @author xyyxhcj@qq.com
     * @date 2019/9/27 9:16
     **/
    int insertBatchForNextPeriod(@Param("nextWithAuxiliaryBalanceSubjectList") List<BalanceSubjectAuxiliaryVo> nextWithAuxiliaryBalanceSubjectList);


    int updateAuxBalanceForPostPeriod(@Param("balanceList")List<PostPeriodBalanceVo> balanceList);

}
