package com.njwd.ledger.controller;

import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.vo.AccountBookVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.dto.BalanceCashFlowInitDto;
import com.njwd.entity.ledger.dto.BalanceInitRecordDto;
import com.njwd.entity.ledger.vo.BalanceCashFlowInitVo;
import com.njwd.entity.ledger.vo.BalanceInitRecordVo;
import com.njwd.entity.platform.vo.CashFlowItemVo;
import com.njwd.ledger.cloudclient.AccountBookFeignClient;
import com.njwd.ledger.service.BalanceCashFlowInitService;
import com.njwd.logger.SenderService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author lj
 * @Description 期初余额-现金流量期初
 * @Date:17:39 2019/8/7
 **/
@RestController
@RequestMapping("balanceCashFlowInit")
public class BalanceCashFlowInitController extends BaseController {

    @Resource
    private BalanceCashFlowInitService balanceCashFlowInitService;

    @Resource
    private SenderService senderService;

    @Resource
    private AccountBookFeignClient accountBookFeignClient;

    /**
     * 添加核算主体
     * @Author lj
     * @Date:13:42 2019/8/9
     * @param balanceInitRecordDto
     * @return com.njwd.support.Result<java.lang.Boolean>
     **/
    @PostMapping("addBalanceInitRecordBatch")
    public Result<Boolean> addBalanceInitRecordBatch(@RequestBody BalanceInitRecordDto balanceInitRecordDto) {
        List<BalanceInitRecordDto> balanceInitRecordDtos = balanceInitRecordDto.getBalanceInitRecordDtos();
        //参数校验
        FastUtils.checkParams(balanceInitRecordDtos);
        List<Long> entityIds = balanceInitRecordDtos.stream().map(BalanceInitRecordDto::getEntityId).collect(Collectors.toList());
        int result = RedisUtils.lock(String.format(Constant.LockKey.BALANCE_INIT_RECORD,entityIds.toString()), Constant.SysConfig.REDIS_LOCK_TIMEOUT, () -> balanceCashFlowInitService.addBalanceInitRecordBatch(balanceInitRecordDto));

        if (result > Constant.Number.ZERO) {
            //记录日志
            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.LedgerSys, LogConstant.menuName.balanceInitRecord, LogConstant.operation.addBatch, LogConstant.operation.addBatch_type, entityIds.toString()));
        }
        return confirm(result);
    }

    /**
     * 现金流量期初录入
     * @Author lj
     * @Date:13:42 2019/8/9
     * @param balanceCashFlowInitDto
     * @return com.njwd.support.Result<java.lang.Boolean>
     **/
    @PostMapping("addCashFlowInitBatch")
    public Result<Boolean> addCashFlowInitBatch(@RequestBody BalanceCashFlowInitDto balanceCashFlowInitDto) {
        List<BalanceCashFlowInitDto> balanceCashFlowInits = balanceCashFlowInitDto.getBalanceCashFlowInits();
        //参数校验
        FastUtils.checkParams(balanceCashFlowInits);
        Long accountBookEntityId = balanceCashFlowInits.get(Constant.Number.ZERO).getAccountBookEntityId();
        FastUtils.checkParams(balanceCashFlowInitDto.getCompanyId());
        //权限校验
        ShiroUtils.checkPerm(Constant.MenuDefine.CASH_EDIT,balanceCashFlowInitDto.getCompanyId());
        int result = RedisUtils.lock(String.format(Constant.LockKey.ACCOUNT_CASHFLOW_INIT,accountBookEntityId), Constant.SysConfig.REDIS_LOCK_TIMEOUT, () -> balanceCashFlowInitService.addCashFlowInitBatch(balanceCashFlowInitDto));

        if (result > Constant.Number.ZERO) {
            //记录日志
            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.LedgerSys, LogConstant.menuName.cashFlowInit, LogConstant.operation.save, LogConstant.operation.save_type, accountBookEntityId.toString()));
        }
        return confirm(result);
    }

    /**
     * 删除核算主体
     *
     * @param balanceInitRecordDto
     * @return java.lang.String
     * @Author lj
     * @Date:18:07 2019/7/25
     **/
    @PostMapping("deleteBalanceInitRecord")
    public Result<Integer> deleteBalanceInitRecord(@RequestBody BalanceInitRecordDto balanceInitRecordDto) {
        //参数校验
        FastUtils.checkParams(balanceInitRecordDto.getEntityId());
        Long accountBookEntityId = balanceInitRecordDto.getEntityId();
        AccountBookDto accountBookDto = new AccountBookDto();
        accountBookDto.setId(balanceInitRecordDto.getAccountBookId());
        AccountBookVo accountBookVo = accountBookFeignClient.findAccountBookById(accountBookDto).getData();
        if (accountBookVo != null) {
            //权限校验
            ShiroUtils.checkPerm(Constant.MenuDefine.CASH_DELETE,accountBookVo.getCompanyId());
        }

        int result = RedisUtils.lock(String.format(Constant.LockKey.BALANCE_INIT_RECORD, accountBookEntityId), Constant.SysConfig.REDIS_LOCK_TIMEOUT, () -> balanceCashFlowInitService.deleteBalanceInitRecord(balanceInitRecordDto));

        if (result > Constant.Number.ZERO) {
            //记录日志
            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.LedgerSys, LogConstant.menuName.balanceInitRecord, LogConstant.operation.delete, LogConstant.operation.delete_type, accountBookEntityId.toString()));
        }
        return ok(result);
    }

    /**
     * 根据核算主体ID清空期初数据
     *
     * @param balanceCashFlowInitDto
     * @return java.lang.String
     * @Author lj
     * @Date:18:07 2019/7/25
     **/
    @PostMapping("deleteBalCashFlowBatch")
    public Result<Integer> deleteBalCashFlowBatch(@RequestBody BalanceCashFlowInitDto balanceCashFlowInitDto) {
        //参数校验
        FastUtils.checkParams(balanceCashFlowInitDto.getAccountBookEntityId());
        Long accountBookEntityId = balanceCashFlowInitDto.getAccountBookEntityId();
//        //权限校验
//        ShiroUtils.checkPerm(Constant.MenuDefine.CASH_DELETE,balanceCashFlowInitDto.getCompanyId());
        int result = RedisUtils.lock(String.format(Constant.LockKey.ACCOUNT_CASHFLOW_INIT, accountBookEntityId), Constant.SysConfig.REDIS_LOCK_TIMEOUT, () -> balanceCashFlowInitService.deleteBalCashFlowBatch(balanceCashFlowInitDto));

        if (result > Constant.Number.ZERO) {
            //记录日志
            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.LedgerSys, LogConstant.menuName.cashFlowInit, LogConstant.operation.clear, LogConstant.operation.clear_type, accountBookEntityId.toString()));
        }
        return ok(result);
    }

    /**
     * 拉取当前公司账簿，账簿对应核算主体中责任人为当前用户的核算主体，账簿对应现金流量信息,账簿启用期间
     *
     * @param balanceCashFlowInitDto
     * @return java.lang.String
     * @Author lj
     * @Date:10:12 2019/7/29
     **/
    @PostMapping("findAccountBookByComId")
    public Result<BalanceCashFlowInitVo> findAccountBookByComId(@RequestBody BalanceCashFlowInitDto balanceCashFlowInitDto) {
        //参数校验
        FastUtils.checkParams(balanceCashFlowInitDto.getAccountBookId());
        return ok(balanceCashFlowInitService.findAccountBookByComId(balanceCashFlowInitDto));
    }

    /**
     * a)	当前账簿启用二级核算，核算主体默认选中公司本部，可选择到其他核算主体，流量项目中显示属性为内部往来现金流量；
     * b)	当前账簿未启用二级核算，核算主体默认选中公司本部且置灰不可选择，项目列表中不显示属性为内部往来的现金流量
     *
     * @param
     * @return java.lang.String
     * @Author lj
     * @Date:10:12 2019/7/29
     **/
    @PostMapping("findCashFlow")
    public Result<List<CashFlowItemVo>> findCashFlow(@RequestBody BalanceCashFlowInitDto balanceCashFlowInitDto) {
        return ok(balanceCashFlowInitService.findCashFlow(balanceCashFlowInitDto));
    }
    
    /**
     * @description: 导出excel
     * @param: [balanceCashFlowInitDto]
     * @return: void 
     * @author: xdy        
     * @create: 2019-10-22 15:18 
     */
    @RequestMapping("exportExcel")
    public void exportExcel(@RequestBody BalanceCashFlowInitDto balanceCashFlowInitDto, HttpServletResponse response){
        balanceCashFlowInitService.exportExcel(balanceCashFlowInitDto,response);
    }

    /**
     *  查询帐簿总账现金流量启用状态
     *
     * @param balanceCashFlowInitDto
     * @return java.lang.String
     * @Author lj
     * @Date:10:12 2019/7/29
     **/
    @PostMapping("findAccCashFlowStatus")
    public Result<BalanceCashFlowInitVo> findAccCashFlowStatus(@RequestBody BalanceCashFlowInitDto balanceCashFlowInitDto) {
        //参数校验
        FastUtils.checkParams(balanceCashFlowInitDto.getAccountBookId());
        return ok(balanceCashFlowInitService.findAccCashFlowStatus(balanceCashFlowInitDto));
    }

    /**
     * 查询期初录入表账簿id列表
     * @Author lj
     * @Date:15:08 2019/10/17
     * @param balanceInitRecordDto
     * @return com.njwd.support.Result<java.util.List<Long>>
     **/
    @PostMapping("findAccoutBookIdListByParam")
    public Result<List<Long>> findAccoutBookIdListByParam(@RequestBody BalanceInitRecordDto balanceInitRecordDto){
        return ok(balanceCashFlowInitService.findAccoutBookIdListByParam(balanceInitRecordDto));
    }

    /**
     * 查询期初录入表核算主体id列表
     * @Author lj
     * @Date:15:08 2019/10/17
     * @param balanceInitRecordDto
     * @return com.njwd.support.Result<java.util.List<Long>>
     **/
    @PostMapping("findEntityIdListByParam")
    public Result<List<Long>> findEntityIdListByParam(@RequestBody BalanceInitRecordDto balanceInitRecordDto){
        return ok(balanceCashFlowInitService.findEntityIdListByParam(balanceInitRecordDto));
    }

    /**
     * 查询期初录入记录列表
     * @Author lj
     * @Date:15:08 2019/10/17
     * @param balanceInitRecordDto
     * @return com.njwd.support.Result<java.util.List<com.njwd.entity.ledger.vo.BalanceInitRecordVo>>
     **/
    @PostMapping("findListByParam")
    public Result<List<BalanceInitRecordVo>> findListByParam(@RequestBody BalanceInitRecordDto balanceInitRecordDto){
        SysUserVo user = UserUtils.getUserVo();
        balanceInitRecordDto.setRootEnterpriseId(user.getRootEnterpriseId());
        return ok(balanceCashFlowInitService.findListByParam(balanceInitRecordDto));
    }

}
