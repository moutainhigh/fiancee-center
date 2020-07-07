package com.njwd.ledger.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.LogConstant;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.dto.PostPeriodDto;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;
import com.njwd.ledger.service.AccountBookPeriodService;
import com.njwd.ledger.service.TransferItemsService;
import com.njwd.ledger.utils.LedgerUtils;
import com.njwd.logger.SenderService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.HttpUtils;
import com.njwd.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


/**
 * 过账
 *
 * @author fancl
 * @date 2019-08-05 15:06
 */
@RequestMapping("transferItems")
@RestController
public class TransferItemsController extends BaseController {
    @Autowired
    private TransferItemsService transferItemsService;

    @Autowired
    private AccountBookPeriodService accountBookPeriodService;

    @Autowired
    private SenderService senderService;



    /**
     * 过账
     *
     * @param postPeriodDto 过账Dto
     * @return
     */
    @RequestMapping("postPeriod")
    public Result<Page<AccountBookPeriodVo>> postPeriod(@RequestBody PostPeriodDto postPeriodDto) {
        //校验id必须非空
        FastUtils.checkParams(postPeriodDto,postPeriodDto.getPeriodList());
        postPeriodDto.getPeriodList().stream().forEach(period -> {
            FastUtils.checkParams(period.getId(),period.getAccountBookId(),period.getPeriodYear(),period.getPeriodNum());
        });
        AccountBookPeriod[] accountBookPeriods = postPeriodDto.getPeriodList().toArray(new AccountBookPeriod[0]);

        //锁定所包含的账簿期间
        Page<AccountBookPeriodVo> transferItemVos = LedgerUtils.lockAccountBook(() ->
                        transferItemsService.doTransferItems(postPeriodDto)
                , postPeriodDto.getPeriodList().toArray(new AccountBookPeriod[0]));

        //测试, 先不锁
        //Page<AccountBookPeriodVo> transferItemVos = transferItemsService.doTransferItems( accountBookPeriodDto,accountBookPeriodDto.getAccountBookIds());
        //日志
        List<Long> ids = new ArrayList<>();
        postPeriodDto.getPeriodList().stream().forEach(p-> ids.add(p.getId()));
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.LedgerSys, LogConstant.menuName.postPeriod,
                LogConstant.operation.postPeriod, LogConstant.operation.postPeriod_type, ids.toString()));
        return ok(transferItemVos);
    }


}

