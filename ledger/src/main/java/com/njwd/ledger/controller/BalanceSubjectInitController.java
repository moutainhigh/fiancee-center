package com.njwd.ledger.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.basedata.vo.AccountBookVo;
import com.njwd.entity.ledger.dto.BalanceSubjectInitAuxiliaryDto;
import com.njwd.entity.ledger.dto.BalanceSubjectInitDto;
import com.njwd.entity.ledger.vo.BalanceSubjectInitVo;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.ledger.cloudclient.AccountBookFeignClient;
import com.njwd.ledger.service.BalanceSubjectInitService;
import com.njwd.logger.SenderService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author lj
 * @Description 期初余额-科目期初
 * @Date:17:58 2019/7/25
 **/
@RestController
@RequestMapping("balanceSubjectInit")
public class BalanceSubjectInitController extends BaseController {
    @Resource
    private BalanceSubjectInitService balanceSubjectInitService;

    @Resource
    private AccountBookFeignClient accountBookFeignClient;

    @Resource
    private SenderService senderService;

    /**
     * 科目期初录入
     *
     * @param balanceSubjectInitDto
     * @return java.lang.String
     * @Author lj
     * @Date:14:17 2019/7/29
     **/
    @PostMapping("addSubjectInitBatch")
    public Result<Boolean> addSubjectInitBatch(@RequestBody BalanceSubjectInitDto balanceSubjectInitDto) {
        List<BalanceSubjectInitDto> balanceSubjectInitList = balanceSubjectInitDto.getBalanceSubjectInitList();
        List<BalanceSubjectInitAuxiliaryDto> balanceSubInitAuxiliaryList = balanceSubjectInitDto.getBalanceSubInitAuxiliaryList();
        //参数校验
        if(CollectionUtils.isEmpty(balanceSubjectInitList)&& CollectionUtils.isEmpty(balanceSubInitAuxiliaryList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
//        FastUtils.checkParams(balanceSubjectInitDto.getCompanyId());
//        //权限校验
//        ShiroUtils.checkPerm(Constant.MenuDefine.SUBJECT_EDIT,balanceSubjectInitDto.getCompanyId());
        Long accountBookEntityId = null;
        if(CollectionUtils.isEmpty(balanceSubjectInitList)){
            accountBookEntityId = balanceSubInitAuxiliaryList.get(Constant.Number.ZERO).getAccountBookEntityId();
        }else {
            accountBookEntityId = balanceSubjectInitList.get(Constant.Number.ZERO).getAccountBookEntityId();
        }
        int result = RedisUtils.lock(String.format(Constant.LockKey.ACCOUNT_SUBJECT_INIT, accountBookEntityId), Constant.SysConfig.REDIS_LOCK_TIMEOUT, () -> balanceSubjectInitService.addSubjectInitBatch(balanceSubjectInitDto));

        if (result > Constant.Number.ZERO) {
            //记录日志
            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.LedgerSys, LogConstant.menuName.subjectInit, LogConstant.operation.save, LogConstant.operation.save_type, accountBookEntityId.toString()));
        }
        return confirm(result);
    }

    /**
     * 根据核算主体ID清空期初数据
     *
     * @param balanceSubjectInitDto
     * @return java.lang.String
     * @Author lj
     * @Date:18:07 2019/7/25
     **/
    @PostMapping("deleteBalSubjectBatch")
    public Result<Integer> deleteBalSubjectBatch(@RequestBody BalanceSubjectInitDto balanceSubjectInitDto) {
        //参数校验
        FastUtils.checkParams(balanceSubjectInitDto.getAccountBookEntityId());
        Long accountBookEntityId = balanceSubjectInitDto.getAccountBookEntityId();
        FastUtils.checkParams(balanceSubjectInitDto.getCompanyId());
        //权限校验
        ShiroUtils.checkPerm(Constant.MenuDefine.SUBJECT_EDIT,balanceSubjectInitDto.getCompanyId());
        int result = RedisUtils.lock(String.format(Constant.LockKey.ACCOUNT_SUBJECT_INIT, accountBookEntityId), Constant.SysConfig.REDIS_LOCK_TIMEOUT, () -> balanceSubjectInitService.deleteBalSubjectBatch(balanceSubjectInitDto));

        if (result > Constant.Number.ZERO) {
            //记录日志
            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.LedgerSys, LogConstant.menuName.subjectInit, LogConstant.operation.clear, LogConstant.operation.clear_type, accountBookEntityId.toString()));
        }
        return ok(result);
    }

    /**
     * 试算平衡
     * @Author lj
     * @Date:14:08 2019/9/9
     * @param balanceSubjectInitDto
     * @return com.njwd.support.Result<com.njwd.entity.ledger.vo.BalanceSubjectInitVo>
     **/
    @PostMapping("trialBalance")
    public Result<BalanceSubjectInitVo> trialBalance(@RequestBody BalanceSubjectInitDto balanceSubjectInitDto) {
        //参数校验
        FastUtils.checkParams(balanceSubjectInitDto.getAccountBookId());
        return ok(balanceSubjectInitService.trialBalance(balanceSubjectInitDto));
    }

    /**
     * 拉取当前所在公司账簿，自动带出账簿中有数据的核算主体及当前所在核算主体，账簿对应科目信息,账簿启用期间
     *
     * @param balanceSubjectInitDto
     * @return java.lang.String
     * @Author lj
     * @Date:10:12 2019/7/29
     **/
    @PostMapping("findAccountBookByComId")
    public Result<BalanceSubjectInitVo> findAccountBookByComId(@RequestBody BalanceSubjectInitDto balanceSubjectInitDto) {
        //参数校验
        FastUtils.checkParams(balanceSubjectInitDto.getAccountBookId());
        return ok(balanceSubjectInitService.findAccountBookByComId(balanceSubjectInitDto));
    }

    /**
     * a)	当前账簿启用二级核算，核算主体默认选中当前所在核算主体，科目中显示属性为内部往来的科目；
     * b)	当前账簿未启用二级核算，核算主体默认选中当前唯一核算主体且置灰不可选择，科目中不显示属性为内部往来的科目
     *
     * @param balanceSubjectInitDto
     * @return java.lang.String
     * @Author lj
     * @Date:10:12 2019/7/29
     **/
    @PostMapping("findSubject")
    public Result<List<AccountSubjectVo>> findSubject(@RequestBody BalanceSubjectInitDto balanceSubjectInitDto) {
        return ok(balanceSubjectInitService.findSubject(balanceSubjectInitDto));
    }

    /**
     * 导出excel
     * @Author lj
     * @Date:17:40 2019/10/23
     * @param balanceSubjectInitDto, response
     * @return void
     **/
    @PostMapping("exportExcel")
    public void exportExcel(@RequestBody BalanceSubjectInitDto balanceSubjectInitDto, HttpServletResponse response){
        balanceSubjectInitService.exportExcel(balanceSubjectInitDto,response);
    }

    /**
     * 查询期初辅助核算信息
     * @Author lj
     * @Date:17:25 2019/8/22
     * @param balanceSubjectInitDto
     * @return com.njwd.support.Result<com.njwd.entity.ledger.vo.BalanceSubjectInitVo>
     **/
    @PostMapping("findAuxInfo")
    public Result<BalanceSubjectInitVo> findAuxInfo(@RequestBody BalanceSubjectInitDto balanceSubjectInitDto) {
        return ok(balanceSubjectInitService.findAuxInfo(balanceSubjectInitDto));
    }

    /**
     * 查询前用户权限下的账簿集
     *
     * @param
     * @return java.lang.String
     * @Author lj
     * @Date:16:03 2019/7/30
     **/
    @PostMapping("findUserAccountBookPage")
    public Result<Page<AccountBookVo>> findUserAccountBookPage(@RequestBody AccountBookDto accountBookDto) {
        accountBookDto.setLedgerStatus(Constant.Number.INITIAL);
        accountBookDto.setIsEnterpriseAdmin(Constant.Number.ANTI_INITLIZED);
        return accountBookFeignClient.findAccountBookPage(accountBookDto);
    }

    /**
     * 查询帐簿下的核算主体
     *
     * @param balanceSubjectInitDto
     * @return java.lang.String
     * @Author lj
     * @Date:14:07 2019/7/31
     **/
    @PostMapping("findBookEntityPageByComId")
    public Result<Page<AccountBookEntityVo>> findBookEntityPageByComId(@RequestBody BalanceSubjectInitDto balanceSubjectInitDto) {
        return ok(balanceSubjectInitService.findBookEntityPageByComId(balanceSubjectInitDto));
    }

}
