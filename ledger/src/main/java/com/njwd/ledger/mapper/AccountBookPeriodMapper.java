package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.ledger.dto.VoucherDto;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;
import com.njwd.entity.ledger.vo.BalanceSubjectVo;
import org.apache.ibatis.annotations.Param;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author liuxiang
 * @Description 账簿期间表
 * @Date:16:48 2019/7/26
 **/
public interface AccountBookPeriodMapper extends BaseMapper<AccountBookPeriod> {

    /**
     * 根据账簿ID,年度，期间 获取期间开始时间
     *
     * @param accountBookPeriodDto
     * @return
     */
    AccountBookPeriodVo findPeriodStartDate(AccountBookPeriodDto accountBookPeriodDto);

    /**
     * 根据账簿id查询该账簿最后一次（已结账已启用）账簿期间信息
     *
     * @param dto
     * @return
     */
    AccountBookPeriodVo findLastPostingByAccountBookId(@Param("dto") AccountBookPeriodDto dto);

    /**
     * 查询账簿的第一次期间信息
     *
     * @param dto
     * @return
     */
    AccountBookPeriodVo findFirstPostingByAccountBookId(@Param("dto") AccountBookPeriodDto dto);

    /**
     * 查询 区间内的期间集合
     *
     * @param dto
     * @return
     * @Author wuweiming
     */
    List<AccountBookPeriodVo> findAccountBookPeriodListByParams(@Param("dto") AccountBookPeriodDto dto);

    /**
     * @return
     * @Author ZhuHC
     * @Date 2019/9/25 17:51
     * @Param
     * @Description 设置账簿期间最近打印时间
     */
    int updatePeriodPrintTime(@Param("accountBookPeriod") AccountBookPeriod accountBookPeriod, @Param("manageList") List<Object> list);

    /**
     * @return
     * @Author ZhuHC
     * @Date 2019/9/29 18:10
     * @Param
     * @Description 根据账簿ID查询账簿编码
     */
    List<AccountBookPeriodVo> selectPeriodByAccountBookId(@Param("voucherDto") VoucherDto voucherDto);

    /**
     * 根据 ID 、账簿ID、子系统标识、期间年、期间号、制单日期、是否查询最小期间、是否查询最大期间 查询账簿期间
     *
     * @param accountBookPeriodDto
     * @return
     */
    AccountBookPeriodVo findPeriodByAccBookIdAndSystemSign(AccountBookPeriodDto accountBookPeriodDto);

    /**
     * 根据 账簿DI/账簿IDs、子系统标识 获取账簿期间列表
     *
     * @param: [accountBookPeriodDto]
     * @return: java.util.List<com.njwd.entity.ledger.vo.AccountBookPeriodVo>
     * @author: zhuzs
     * @date: 2019-10-16
     */
    List<AccountBookPeriodVo> findPeriodRangeByAccBookIdsAndSystemSign(AccountBookPeriodDto accountBookPeriodDto);

    /**
     * 根据账簿id和年度查询期间范围
     *
     * @param: [balanceSubjectList]
     * @return: com.njwd.entity.ledger.vo.AccountBookPeriodVo
     * @author: 周鹏
     * @date: 2019-10-16
     */
    List<AccountBookPeriodVo> findPeriodAreaByYear(@Param("list") List<BalanceSubjectVo> balanceSubjectList);

    /**
     * 根据账簿id查询启用期间
     *
     * @param: [accountBookPeriodDto]
     * @return: com.njwd.entity.ledger.vo.AccountBookPeriodVo
     * @author: 周鹏
     * @date: 2019-10-28
     */
    AccountBookPeriodVo findStartPeriodByAccountBook(AccountBookPeriodDto accountBookPeriodDto);

    /**
     * @return java.lang.Byte
     * @description: 根据年份查询当前年份最大期间数
     * @Param [accountBookPeriodDto]
     * @author LuoY
     * @date 2019/8/16 11:54
     */
    Byte findMaxPeriodNumByYearAndAccountBookId(AccountBookPeriodDto accountBookPeriodDto);

    /**
     * 获取待整理账簿期间
     *
     * @param: [page, accountBookPeriodDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.ledger.vo.AccountBookPeriodVo>
     * @author: xdy
     * @date: 2019-10-16
     */
    Page<AccountBookPeriodVo> findToAdjustPage(@Param("page") Page<AccountBookPeriodVo> page, @Param("accountBookPeriodDto") AccountBookPeriodDto accountBookPeriodDto);

    /**
     * @param accountBookPeriodDto
     * @return java.util.List<java.lang.Long>
     * @Description 查询未来期间数
     * @Author 朱小明
     * @Date 2019/8/27
     **/
    List<AccountBookPeriod> selectFuturePeriodList(@Param("accountBookPeriodDto") AccountBookPeriodDto accountBookPeriodDto);

    /**
     * @param accountBookPeriod
     * @return com.njwd.entity.ledger.AccountBookPeriod
     * @Description 查询两个期间
     * @Author 朱小明
     * @Date 2019/8/29
     **/
    List<Integer> selectTwoPeriod(@Param("accountBookPeriod") AccountBookPeriodDto accountBookPeriod);

    /**
     * @param page, accountBookPeriodVos
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.ledger.vo.AccountBookPeriodVo>
     * @Description 根据账簿ID和最大期间查询账簿信息
     * @Author 朱小明
     * @Date 2019/9/19
     **/
    Page<AccountBookPeriodVo> selectPeriodPage(Page<AccountBookPeriodVo> page, @Param("accountBookPeriod") AccountBookPeriodDto accountBookPeriod);

    /**
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.ledger.vo.AccountBookPeriodVo>
     * @Description 查询已结账的账簿期间
     * @Author 朱小明
     * @Date 2019/8/14 14:57
     * @Param [page, accountBookPeriod]
     **/
    List<AccountBookPeriodVo> selectSettleYesPageByCondition(@Param("abpd") AccountBookPeriodDto accountBookPeriod);

    /**
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.ledger.vo.AccountBookPeriodVo>
     * @Description 查询未结账(排除不可结账)的账簿期间
     * @Author 朱小明
     * @Date 2019/8/14 14:57
     * @Param [page, accountBookPeriod]
     **/
    List<AccountBookPeriodVo> selectSettleNoPageByCondition(@Param("abpd") AccountBookPeriodDto accountBookPeriod);

    /**
     * @param
     * @return
     * @description 过账时 查询记录
     * @author fancl
     * @date 2019/8/21
     */
    Page<AccountBookPeriodVo> selectRecordsPageForPostPeriod(Page<AccountBookPeriodVo> page, @Param("abpd") AccountBookPeriodDto accountBookPeriod);

    /**
     * 查询可 结账/反结账 的账簿列表
     *
     * @param page                 page
     * @param accountBookPeriodDto accountBookPeriodDto
     * @param hasPermAccBookIds    hasPermAccBookIds 有权的账簿id
     * @param firstAccBookId       firstAccBookId 需要排在第一位的账簿id
     * @return java.util.List<com.njwd.entity.ledger.vo.AccountBookPeriodVo>
     * @author xyyxhcj@qq.com
     * @date 2019/10/18 16:37
     **/
    List<AccountBookPeriodVo> findPageForSettle(@Param("page") Page<AccountBookPeriodVo> page, @Param("accountBookPeriodDto") AccountBookPeriodDto accountBookPeriodDto, @NotNull @Param("hasPermAccBookIds") List<Long> hasPermAccBookIds, @Nullable @Param("firstAccBookId") Long firstAccBookId);
}
