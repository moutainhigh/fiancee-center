package com.njwd.ledger.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.Voucher;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;
import com.njwd.entity.ledger.vo.SettleResult;
import com.njwd.ledger.cloudclient.CompanyFeignClient;
import com.njwd.ledger.service.AccountBookPeriodService;
import com.njwd.ledger.service.SettleService;
import com.njwd.ledger.service.VoucherService;
import com.njwd.ledger.utils.LedgerUtils;
import com.njwd.logger.SenderService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.HttpUtils;
import com.njwd.utils.ShiroUtils;
import com.njwd.utils.UserUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 备用
 *
 * @author xyyxhcj@qq.com
 * @since 2019/09/20
 */
@RestController
@RequestMapping("settle")
public class SettleController extends BaseController {
	@Resource
	private AccountBookPeriodService accountBookPeriodService;
	@Resource
	private SettleService backupService;
	@Resource
	private SenderService senderService;
	@Resource
	private VoucherService voucherService;
	@Resource
	private CompanyFeignClient companyFeignClient;

	/**
	 * @Description 根据条件获取账簿列表
	 * @Author 朱小明
	 * @Date 2019/8/7 9:28
	 * @Param []
	 * @return com.njwd.support.Result<Page<AccountBookPeriod>>
	 **/
	@PostMapping("findPageByCondition")
	public Result<Page<AccountBookPeriodVo>> findPageByCondition(@RequestBody AccountBookPeriodDto accountBookPeriodDto) {
		FastUtils.checkParams(accountBookPeriodDto.getPage(),accountBookPeriodDto.getMenuCode(),accountBookPeriodDto.getCompanyId());
		return ok(accountBookPeriodService.findPageByCondition(accountBookPeriodDto));
	}

	/**
	 * 查询可 结账/反结账 的账簿分页
	 **/
	@PostMapping("findPage")
	public Result<Page<AccountBookPeriodVo>> findPage(@RequestBody AccountBookPeriodDto accountBookPeriodDto) {
		FastUtils.checkParams(accountBookPeriodDto.getPage(), accountBookPeriodDto.getMenuCode(), accountBookPeriodDto.getCompanyId(), accountBookPeriodDto.getIsSettle());
		return ok(accountBookPeriodService.findPageForSettle(accountBookPeriodDto));
	}
	/**
	 * 结账
	 **/
	@PostMapping("settle")
	public Result<SettleResult> settle(@RequestBody AccountBookPeriodDto accountBookPeriodDto) {
		FastUtils.checkParams(accountBookPeriodDto.getAccountBookId(), accountBookPeriodDto.getPeriodYear(), accountBookPeriodDto.getPeriodNum());
		ShiroUtils.checkPerm(Constant.MenuDefine.SETTLE, LedgerUtils.getCompanyId(companyFeignClient, accountBookPeriodDto.getAccountBookId()));
		// 取下期期间 同时锁两期，防止写入下期数据时冲突,如果无下一期则结果为null,不锁下期
		AccountBookPeriod nextPeriod = getNextPeriod(accountBookPeriodDto);
		SettleResult result = LedgerUtils.lockAccountBook(() -> backupService.settle(accountBookPeriodDto, nextPeriod), accountBookPeriodDto, nextPeriod);
		if (result.getLossProfitListVoucherIds() != null && !result.getLossProfitListVoucherIds().isEmpty()) {
			// 再查出结转凭证 防止凭证号改变
			result.setLossProfitList(voucherService.list(new LambdaQueryWrapper<Voucher>()
					.in(Voucher::getId, result.getLossProfitListVoucherIds())
					.select(Voucher::getId, Voucher::getCredentialWord,
							Voucher::getMainCode, Voucher::getChildCode)));
		}
		senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.LedgerSys, LogConstant.menuName.balanceSettle, LogConstant.operation.settle, LogConstant.operation.settle_type, result.getAccountBookPeriod().getId().toString()));
		return ok(result);
	}

	/**
	 * 反结账
	 **/
	@PostMapping("cancelSettle")
	public Result<AccountBookPeriod> cancelSettle(@RequestBody AccountBookPeriodDto accountBookPeriodDto) {
		FastUtils.checkParams(accountBookPeriodDto.getAccountBookId(), accountBookPeriodDto.getPeriodYear(), accountBookPeriodDto.getPeriodNum());
		ShiroUtils.checkPerm(Constant.MenuDefine.UNSETTLE, LedgerUtils.getCompanyId(companyFeignClient, accountBookPeriodDto.getAccountBookId()));
		// 取下期期间 同时锁两期，防止下期点击结账,如果无下一期则结果为null,不锁下期
		AccountBookPeriod nextPeriod = getNextPeriod(accountBookPeriodDto);
		AccountBookPeriod accountBookPeriod = LedgerUtils.lockAccountBook(() -> backupService.cancelSettle(accountBookPeriodDto), accountBookPeriodDto, nextPeriod);
		senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.LedgerSys, LogConstant.menuName.balanceSettle, LogConstant.operation.cancelSettle, LogConstant.operation.cancleSettleType, accountBookPeriod.getId().toString()));
		return ok(accountBookPeriod);
	}

	/**
	 * 取下一期
	 *
	 * @param existAccountBookPeriod existAccountBookPeriod
	 * @return com.njwd.entity.ledger.AccountBookPeriod
	 * @author xyyxhcj@qq.com
	 * @date 2019/9/27 20:10
	 **/
	private AccountBookPeriod getNextPeriod(AccountBookPeriod existAccountBookPeriod) {
		return accountBookPeriodService.getOne(new LambdaQueryWrapper<AccountBookPeriod>()
				.eq(AccountBookPeriod::getAccountBookId, existAccountBookPeriod.getAccountBookId())
				.eq(AccountBookPeriod::getSystemSign, Constant.SystemSign.LEDGER)
				.gt(AccountBookPeriod::getPeriodYearNum, existAccountBookPeriod.getPeriodYearNum())
				.last(Constant.ConcatSql.LIMIT_1));
	}
}
