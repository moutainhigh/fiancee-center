package com.njwd.ledger.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.LogConstant;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;
import com.njwd.exception.ResultCode;
import com.njwd.ledger.service.AccountBookPeriodService;
import com.njwd.ledger.service.SettleServiceBak;
import com.njwd.ledger.utils.LedgerUtils;
import com.njwd.logger.SenderService;
import com.njwd.support.BaseController;
import com.njwd.support.CheckVoucherResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.HttpUtils;
import com.njwd.utils.UserUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;

/**
 * @Description 期末处理
 * @Author 朱小明
 * @since 2019/7/29 14:09
 */
//@RestController
//@RequestMapping("settle")
public class SettleControllerBak extends BaseController {
    @Resource
    private AccountBookPeriodService accountBookPeriodService;
    @Resource
    private SettleServiceBak settleServiceBak;
    @Resource
    private SenderService senderService;


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
     * @Description 结账
     * @Author 朱小明
     * @Date 2019/7/29 14:13
     * @Param accountBookPeriod.id
     * @return void
     **/
    @PostMapping("settle")
    public Result<CheckVoucherResult> settle (@RequestBody AccountBookPeriodDto accountBookPeriod) {
        FastUtils.checkParams(accountBookPeriod.getId());
        AccountBookPeriodDto accountBookPeriodDto = new AccountBookPeriodDto();
        accountBookPeriodDto.setId(accountBookPeriod.getId());
        FastUtils.copyProperties(accountBookPeriodService
                .findPeriodByAccBookIdAndSystemSign(accountBookPeriodDto),accountBookPeriod);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.LedgerSys, LogConstant.menuName.balanceSettle,
                LogConstant.operation.settle, LogConstant.operation.settle_type, accountBookPeriod.getId().toString()));
        CheckVoucherResult result = LedgerUtils.lockAccountBook(()-> settleServiceBak.settle(accountBookPeriod) ,accountBookPeriod);
        if (result.getLossProfitList().size()==0) {
            ok(ResultCode.NOT_SETTLE_CONDITION, result);
        }
        return ok(result);
    }

    /**
     * @Description 反结账
     * @Author 朱小明
     * @Date 2019/7/29 14:13
     * @Param []
     * @return void
     **/
    @PostMapping("cancelSettle")
    public Result<AccountBookPeriod> cancelSettle(@RequestBody AccountBookPeriodDto accountBookPeriod) {
        FastUtils.checkParams(accountBookPeriod.getId());
        AccountBookPeriodDto accountBookPeriodDto = new AccountBookPeriodDto();
        accountBookPeriodDto.setId(accountBookPeriod.getId());
        FastUtils.copyProperties(
                accountBookPeriodService.findPeriodByAccBookIdAndSystemSign(accountBookPeriodDto), accountBookPeriod);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.LedgerSys, LogConstant.menuName.balanceSettle,
                LogConstant.operation.cancelSettle, LogConstant.operation.cancleSettleType, accountBookPeriod.getId().toString()));
        return ok(LedgerUtils.lockAccountBook(()-> settleServiceBak.cancelSettle(accountBookPeriod),accountBookPeriod));
    }
}
