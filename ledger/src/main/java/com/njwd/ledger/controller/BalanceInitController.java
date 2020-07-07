package com.njwd.ledger.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.basedata.dto.AccountBookSystemDto;
import com.njwd.entity.basedata.vo.AccountBookSystemVo;
import com.njwd.entity.ledger.dto.BalanceInitDto;
import com.njwd.entity.ledger.vo.BalanceInitVo;
import com.njwd.ledger.cloudclient.AccountBookSystemFeignClient;
import com.njwd.ledger.service.BalanceInitService;
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
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author lj
 * @Description 总账初始化
 * @Date:15:37 2019/8/9
 **/
@RestController
@RequestMapping("balanceInit")
public class BalanceInitController extends BaseController {

    @Resource
    private AccountBookSystemFeignClient accountBookSystemFeignClient;

    @Resource
    private BalanceInitService balanceInitService;

    @Resource
    private SenderService senderService;
    
    /**
     * @Author lj
     * @Description 查询租户用户下总账账簿列表
     * @Date:15:49 2019/8/9
     **/
    @PostMapping("findAccountBookPage")
    public Result<Page<AccountBookSystemVo>> findAccountBookPage(@RequestBody AccountBookSystemDto accountBookSystemDto){
        return accountBookSystemFeignClient.findLedgerListByUserId(accountBookSystemDto);
    }

    /**
     * 账簿初始化
     * @Author lj
     * @Date:15:42 2019/8/9
     * @param
     * @return void
     **/
    @PostMapping("balanceInit")
    public Result<BalanceInitVo> balanceInit(@RequestBody BalanceInitDto balanceInitDto){
        //参数校验
        FastUtils.checkParams(balanceInitDto.getAccountBookId());
        FastUtils.checkParams(balanceInitDto.getCompanyId());
        //权限校验
        ShiroUtils.checkPerm(Constant.MenuDefine.LEDGER_INIT,balanceInitDto.getCompanyId());
        BalanceInitVo balanceInitVo = balanceInitService.balanceInit(balanceInitDto);
        //记录日志
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.LedgerSys, LogConstant.menuName.balanceInit, LogConstant.operation.init, LogConstant.operation.init_type, balanceInitDto.getAccountBookId().toString()));
        return ok(balanceInitVo);
    }

    /**
     * 账簿初始化批量校验
     * @Author lj
     * @Date:15:42 2019/8/9
     * @param
     * @return void
     **/
    @PostMapping("balanceInitBatchCheck")
    public Result<BalanceInitVo> balanceInitBatchCheck(@RequestBody BalanceInitDto balanceInitDto){
        //权限校验
        for(BalanceInitDto balanceInit:balanceInitDto.getBalanceInitList()){
            ShiroUtils.checkPerm(Constant.MenuDefine.LEDGER_INIT,balanceInit.getCompanyId());
        }
        return ok(balanceInitService.balanceInitBatchCheck(balanceInitDto));
    }

    /**
     * 账簿初始化批量更新数据
     * @Author lj
     * @Date:15:42 2019/8/9
     * @param
     * @return void
     **/
    @PostMapping("balanceInitBatchUpdate")
    public Result<BalanceInitVo> balanceInitBatchUpdate(@RequestBody BalanceInitDto balanceInitDto){
        //参数校验
        FastUtils.checkParams(balanceInitDto.getBalanceInitList());
        BalanceInitVo balanceInitVo = balanceInitService.balanceInitBatchUpdate(balanceInitDto);
        //记录日志
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.LedgerSys, LogConstant.menuName.balanceInit, LogConstant.operation.initBatch, LogConstant.operation.initBatch_type, balanceInitVo.getAccountBookIds().toString()));
        return ok(balanceInitVo);
    }

    /**
     * 账簿反初始化
     * @Author lj
     * @Date:15:42 2019/8/9
     * @param
     * @return void
     **/
    @PostMapping("balanceDisInit")
    public Result<BalanceInitVo> balanceDisInit(@RequestBody BalanceInitDto balanceInitDto){
        //参数校验
        FastUtils.checkParams(balanceInitDto.getAccountBookId());
        FastUtils.checkParams(balanceInitDto.getCompanyId());
        //权限校验
        ShiroUtils.checkPerm(Constant.MenuDefine.LEDGER_REVERSE,balanceInitDto.getCompanyId());
        BalanceInitVo balanceInitVo = balanceInitService.balanceDisInit(balanceInitDto);
        //记录日志
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.LedgerSys, LogConstant.menuName.balanceInit, LogConstant.operation.disInit, LogConstant.operation.disInit_type, balanceInitDto.getAccountBookId().toString()));
        return ok(balanceInitVo);
    }

    /**
     * 账簿反初始化批量校验
     * @Author lj
     * @Date:15:42 2019/8/9
     * @param
     * @return void
     **/
    @PostMapping("balanceDisInitBatchCheck")
    public Result<BalanceInitVo> balanceDisInitBatchCheck(@RequestBody BalanceInitDto balanceInitDto){
        //权限校验
        for(BalanceInitDto balanceInit:balanceInitDto.getBalanceInitList()){
            ShiroUtils.checkPerm(Constant.MenuDefine.LEDGER_REVERSE,balanceInit.getCompanyId());
        }
        return ok(balanceInitService.balanceDisInitBatchCheck(balanceInitDto));
    }

    /**
     * 账簿反初始化批量更新数据
     * @Author lj
     * @Date:15:42 2019/8/9
     * @param
     * @return void
     **/
    @PostMapping("balanceDisInitBatchUpdate")
    public Result<BalanceInitVo> balanceDisInitBatchUpdate(@RequestBody BalanceInitDto balanceInitDto){
        //参数校验
        FastUtils.checkParams(balanceInitDto.getBalanceInitList());
        BalanceInitVo balanceInitVo = balanceInitService.balanceDisInitBatchUpdate(balanceInitDto);
        //记录日志
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.LedgerSys, LogConstant.menuName.balanceInit, LogConstant.operation.disInitBatch, LogConstant.operation.disInitBatch_type, balanceInitVo.getAccountBookIds().toString()));
        return ok(balanceInitVo);
    }

    /**
     * 导出校验错误信息excel
     * @Author lj
     * @Date:16:15 2019/10/23
     * @param: [balanceInitVo, response]
     * @return void
     **/
    @RequestMapping("exportExcel")
    public void exportExcel(@RequestBody BalanceInitVo balanceInitVo, HttpServletResponse response){
        //参数校验
        FastUtils.checkParams(balanceInitVo.getAccountBookName());
        FastUtils.checkParams(balanceInitVo.getBalanceInitCheckVos());
        balanceInitService.exportExcel(balanceInitVo,response);
    }
}
