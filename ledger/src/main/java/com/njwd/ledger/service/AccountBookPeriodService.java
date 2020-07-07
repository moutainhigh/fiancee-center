package com.njwd.ledger.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.njwd.entity.basedata.vo.AccountBookVo;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;
import com.njwd.entity.ledger.vo.BalanceSubjectVo;
import com.njwd.support.Result;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 账簿期间表
 *
 * @author zhuzs
 * @date 2019-07-02 19:16
 */
public interface AccountBookPeriodService extends IService<AccountBookPeriod> {
	/**
	 * 新增账簿期间
	 *
	 * @param: [accountBookPeriod]
	 * @return: java.lang.Integer
	 * @author: zhuzs
	 * @date: 2019-10-16
	 */
	Integer addAccountBookPeriod(AccountBookPeriod accountBookPeriod);

	/**
	 * 账簿ID和子系统标识 删除账簿期间数据
	 *
	 * @param: [accountBookPeriod]
	 * @return: java.lang.Integer
	 * @author: zhuzs
	 * @date: 2019-10-16
	 */
	Integer deleteByAccountBookIdAndSystenSign(AccountBookPeriod accountBookPeriod);

	/**
	 * 根据 ID 、账簿ID、子系统标识、期间年、期间号、制单日期、是否查询最小期间、是否查询最大期间 查询账簿期间
	 *
	 * @param: [accountBookPeriodDto]
	 * @return: com.njwd.entity.ledger.vo.AccountBookPeriodVo
	 * @author: zhuzs
	 * @date: 2019-10-15 09:37
	 */
	AccountBookPeriodVo findPeriodByAccBookIdAndSystemSign(AccountBookPeriodDto accountBookPeriodDto);

	/**
	 * 根据 账簿DI/账簿IDs、子系统标识 获取账簿期间列表
	 *
	 * @param: [accountBookPeriodDto]
	 * @return: java.util.List<com.njwd.entity.ledger.vo.AccountBookPeriodVo>
	 * @author: zhuzs
	 * @date: 2019-10-15 09:36
	 */
	List<AccountBookPeriodVo> findPeriodRangeByAccBookIdsAndSystemSign(AccountBookPeriodDto accountBookPeriodDto);

	/**
	 * 根据账簿id和年度查询期间范围
	 *
	 * @param: [balanceSubjectList]
	 * @return: com.njwd.entity.ledger.vo.AccountBookPeriodVo
	 * @author: 周鹏
	 * @date: 2019-10-15
	 */
	List<AccountBookPeriodVo> findPeriodAreaByYear(List<BalanceSubjectVo> balanceSubjectList);

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
	 * @description: 根据年份查询指定年份最大期间数
	 * @Param [accountBookPeriodDto]
	 * @return java.lang.Byte
	 * @author LuoY
	 * @date 2019/8/16 11:50
	 */
	Byte findMaxPeriodNumByYearAndAccountBookId(AccountBookPeriodDto accountBookPeriodDto);

	/**
	 * @description: 获取待整理账簿期间
	 * @param: [accountBookPeriodDto]
	 * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.ledger.vo.AccountBookPeriodVo>
	 * @author: xdy
	 * @create: 2019-08-15 09-29
	 */
	Page<AccountBookPeriodVo> findToAdjustPage(AccountBookPeriodDto accountBookPeriodDto);

	/**
	 * @Description 根据条件获取账簿列表
	 * @Author 朱小明
	 * @Date 2019/8/7 16:42
	 * @Param [accountBookPeriod]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.ledger.vo.AccountBookPeriodVo>
	 **/
	Page<AccountBookPeriodVo> findPageByCondition(AccountBookPeriodDto accountBookPeriod);

	/**
	 * 查询可 结账/反结账 的账簿列表
	 *
	 * @param accountBookPeriodDto accountBookPeriodDto
	 * @return page
	 * @author xyyxhcj@qq.com
	 * @date 2019/10/18 15:53
	 **/
	Page<AccountBookPeriodVo> findPageForSettle(AccountBookPeriodDto accountBookPeriodDto);

	/**
	 * 获取有权的所有账簿
	 *
	 * @param menuCode menuCode
	 * @param accountBookIds 账簿id列表
	 * @return com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountBookVo>>
	 * @author xyyxhcj@qq.com
	 * @date 2019/10/31 9:47
	 **/
	Result<Page<AccountBookVo>> findHasPermAccBooks(@NotNull String menuCode, @Nullable List<Long> accountBookIds);
}
