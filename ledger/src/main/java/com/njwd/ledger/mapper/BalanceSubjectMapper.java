package com.njwd.ledger.mapper;

import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.BalanceSubject;
import com.njwd.entity.ledger.BalanceSubjectInit;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.ledger.dto.BalanceSubjectDto;
import com.njwd.entity.ledger.dto.BalanceSubjectQueryDto;
import com.njwd.entity.ledger.dto.VoucherDto;
import com.njwd.entity.ledger.vo.BalanceSubjectVo;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/7/24
 */
public interface BalanceSubjectMapper extends BalanceMapper<BalanceSubject> {
    int addSubject(@Param("balanceSubjectInit") BalanceSubjectInit balanceSubjectInit);

    int addSubjectOne(@Param("balanceSubjectInit") BalanceSubjectInit balanceSubjectInit);

    int addSubjectZero(@Param("balanceSubjectInit") BalanceSubjectInit balanceSubjectInit);

    int updateSubjectOne(@Param("balanceSubjectInit") BalanceSubjectInit balanceSubjectInit);

    int updateSubjectAdd(@Param("balanceSubjectInit") BalanceSubjectInit balanceSubjectInit);

    int updateSubjectDel(@Param("balanceSubjectInit") BalanceSubjectInit balanceSubjectInit);

    int updateSubjectDelOne(@Param("balanceSubjectInit") BalanceSubjectInit balanceSubjectInit);

    /**
     * 更新发生额
     *
     * @param balanceSubjects balanceSubjects
     * @param voucherDto      voucherDto
     * @param updateType      updateType
     * @return int
     * @author xyyxhcj@qq.com
     * @date 2019/8/13 20:29
     **/
    int updateBatch(@Param("balanceSubjects") Collection<BalanceSubjectDto> balanceSubjects, @Param("voucherDto") VoucherDto voucherDto, @Param("updateType") byte updateType);

    /**
     * 更新损益凭证发生额
     *
     * @param balanceSubjects   balanceSubjects
     * @param accountBookPeriod accountBookPeriod
     * @param accBookEntityId   账簿主体ID
     * @param updateType        updateType
     * @return int
     * @author xyyxhcj@qq.com
     * @date 2019/9/25 16:21
     **/
    int updateBatchForProfitLoss(@Param("balanceSubjects") Collection<BalanceSubjectDto> balanceSubjects, @Param("accountBookPeriod") AccountBookPeriod accountBookPeriod, @Param("accBookEntityId") Long accBookEntityId, @Param("updateType") byte updateType);

    /**
     * 根据条件统计科目余额表
     *
     * @param balanceSubjectQueryDto
     * @return BalanceSubjectVo
     * @author: 周鹏
     * @create: 2019/8/5
     */
    List<BalanceSubjectVo> findListByParam(@Param("balanceSubjectQueryDto") BalanceSubjectQueryDto balanceSubjectQueryDto);

    /**
     * 根据条件统计科目汇总表(科目余额表)
     *
     * @param balanceSubjectQueryDto
     * @return BalanceSubjectVo
     * @author: 周鹏
     * @create: 2019/8/5
     */
    List<BalanceSubjectVo> findCollectListByParam(@Param("balanceSubjectQueryDto") BalanceSubjectQueryDto balanceSubjectQueryDto);

    /**
     * 根据条件统计科目汇总表(凭证分录表)
     *
     * @param balanceSubjectQueryDto
     * @return BalanceSubjectVo
     * @author: 周鹏
     * @create: 2019/8/17
     */
    List<BalanceSubjectVo> findVoucherListByParam(@Param("balanceSubjectQueryDto") BalanceSubjectQueryDto balanceSubjectQueryDto);

    /**
     * 根据条件查询凭证号
     *
     * @param balanceSubjectQueryDto
     * @return BalanceSubjectVo
     * @author: 周鹏
     * @create: 2019/8/17
     */
    String findVoucherNumberByParam(@Param("balanceSubjectQueryDto") BalanceSubjectQueryDto balanceSubjectQueryDto);

    /**
     * 根据账簿ID/核算主体ID 查询科目信息
     *
     * @param balanceSubjectQueryDto
     * @return
     */
    List<BalanceSubjectVo> findListByAccountBookIdAndEntityId(BalanceSubjectQueryDto balanceSubjectQueryDto);

    /**
     * 查询单个期间的余额信息
     *
     * @param list
     * @return
     */
    List<BalanceSubjectVo> findInfoByPeriod(@Param("list") List<BalanceSubjectQueryDto> list);

    /**
     * 查询多个期间的余额信息
     *
     * @param balanceSubjectQueryDto
     * @return
     */
    List<BalanceSubjectVo> findInfoByPeriods(@Param("balanceSubjectQueryDto") BalanceSubjectQueryDto balanceSubjectQueryDto);

    /**
     * 查询账簿会计科目的启用期间及金额信息
     *
     * @param list
     * @return
     */
    List<BalanceSubjectVo> findStartPeriodBalance(@Param("list") List<BalanceSubjectVo> list);

    /**
     * 根据账簿id/核算主体id/期间 查询科目发生额累计
     *
     * @param balanceSubjectQueryDto
     * @return
     */
    List<BalanceSubjectVo> getAccumulateBalanceByPeriodNum(BalanceSubjectQueryDto balanceSubjectQueryDto);

    /**
     * @param balanceSubjectQueryDto
     * @return
     * @Author wuweiming
     * 查询当前账簿下每个核算主体的第一期数据
     */
    BalanceSubjectVo findFirstBalanceSubjectByParams(@Param("dto") BalanceSubjectQueryDto balanceSubjectQueryDto);

    /**
     * @param balanceSubjectQueryDto
     * @return
     * @Author wuweiming
     * 查询最后一次结账的科目余额信息
     */
    List<BalanceSubjectVo> findLastPostingBalanceSubjectInfo(@Param("dto") BalanceSubjectQueryDto balanceSubjectQueryDto);

    /**
     * @param balanceSubjectQueryDto
     * @return
     * @Author wuweiming
     * 查询科目余额信息
     */
    List<BalanceSubjectVo> findBalanceSubjectByParams(@Param("dto") BalanceSubjectQueryDto balanceSubjectQueryDto);

    /**
     * @param accountBookPeriod
     * @return java.util.List<com.njwd.entity.ledger.BalanceSubject>
     * @Description 查询指定期间的损益科目余额
     * @Author 朱小明
     * @Date 2019/8/29
     **/
    List<BalanceSubject> selectSubjctBalanceAsList(@Param("accountBookPeriod") AccountBookPeriodDto accountBookPeriod);

    /**
     * @param accountBookPeriod
     * @return java.util.List<com.njwd.entity.ledger.BalanceSubject>
     * @Description 查询指定期间的科目余额
     * @Author 朱小明
     * @Date 2019/8/29
     **/
    List<BalanceSubject> selectSubjctBalanceList(@Param("accountBookPeriod") AccountBookPeriodDto accountBookPeriod);

    /**
     * @return com.njwd.entity.ledger.BalanceSubject
     * @description: 根据账簿核算主体和期间查询指定科目余额信息
     * @Param [balanceSubjectQueryDto]
     * @author LuoY
     * @date 2019/9/18 10:23
     */
    List<BalanceSubject> selectBalanceSubjectBySubjectId(@Param("dto") BalanceSubjectQueryDto balanceSubjectQueryDto);

    /**
    * @description: 根据账簿信息查询利润表数据
    * @Param [balanceSubjectQueryDto]
    * @return java.util.List<com.njwd.entity.ledger.BalanceSubject>
    * @author LuoY
    * @date 2019/9/27 16:35
    */
    List<BalanceSubjectVo> selectBalanceSubjectInfoByAccount(BalanceSubjectQueryDto balanceSubjectQueryDto);

}
