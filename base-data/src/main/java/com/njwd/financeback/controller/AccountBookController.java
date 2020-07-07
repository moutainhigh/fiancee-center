package com.njwd.financeback.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.basedata.BatchOperationDetails;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.vo.AccountBookVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.platform.dto.AccountingPeriodDto;
import com.njwd.entity.platform.vo.AccountingPeriodVo;
import com.njwd.entity.platform.vo.FinancialReportItemSetVo;
import com.njwd.financeback.service.AccountBookService;
import com.njwd.financeback.service.AccountBookSystemService;
import com.njwd.financeback.service.AccountingPeriodService;
import com.njwd.logger.SenderService;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.HttpUtils;
import com.njwd.utils.ShiroUtils;
import com.njwd.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 核算账簿
 *
 * @Author: Zhuzs
 * @Date: 2019-06-18 17:02
 */
@RestController
@RequestMapping("accountBook")
public class AccountBookController extends BaseController {
    @Autowired
    private AccountBookService accountBookService;
    @Autowired
    private AccountBookSystemService accountBookSystemService;
    @Autowired
    private SenderService senderService;
    @Autowired
    private AccountingPeriodService accountingPeriodService;

    /**
     * 根据ID查询核算账簿
     *
     * @param: [accountBookDto]
     * @return: com.njwd.support.Result<com.njwd.entity.basedata.vo.AccountBookVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:21
     */
    @RequestMapping("selectById")
    public Result<AccountBookVo> selectById(@RequestBody AccountBookDto accountBookDto){
        return ok(accountBookService.selectById(accountBookDto));
    }


    /**
     * 删除核算账簿
     *
     * @param: [accountBookDto]
     * @return: com.njwd.support.Result<com.njwd.support.BatchResult>
     * @author: zhuzs
     * @date: 2019-09-16 17:21
     */
    @RequestMapping("deleteBatchById")
    public Result<BatchResult> deleteBatchById(@RequestBody AccountBookDto accountBookDto){
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        BatchResult result ;
        if(Constant.Number.ONE.equals(accountBookDto.getAccountBookIdList().size())){
            result = accountBookService.delete(accountBookDto);
        }else{
            result = accountBookService.deleteBatch(accountBookDto);
        }
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.accountbook,
                LogConstant.operation.delete,
                LogConstant.operation.delete_type,
                String.valueOf(result.getSuccessList())));
        return ok(result);
    }

    /**
     * 启用子系统
     *
     * @param: [accountBookDto]
     * @return: com.njwd.support.Result<java.lang.Integer>
     * @author: zhuzs
     * @date: 2019-09-16 17:21
     */
    @RequestMapping("enableAccountBookSystem")
    public Result<Integer> enableAccountBookSystem (@RequestBody AccountBookDto accountBookDto){
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        accountBookDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        Integer result = accountBookSystemService.enableAccountBookSystem(accountBookDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.company,
                LogConstant.operation.addAccountingBatch,
                LogConstant.operation.addAccountingBatch_type,
                String.valueOf(accountBookDto.getId())));
        return ok(result);
    }

    /**
     * 批量 启用子系统
     *
     * @param: [accountBookDtos]
     * @return: com.njwd.support.Result<com.njwd.entity.basedata.BatchOperationDetails>
     * @author: zhuzs
     * @date: 2019-09-16 17:21
     */
    @RequestMapping("enableAccountBookSystemBatch")
    public Result<BatchOperationDetails> enableAccountBookSystemBatch (@RequestBody List<AccountBookDto> accountBookDtos){
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        BatchOperationDetails result = accountBookSystemService.enableAccountBookSystemBatch(accountBookDtos);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.company,
                LogConstant.operation.addAccountingBatch,
                LogConstant.operation.addAccountingBatch_type,
                String.valueOf(result.getSuccessIds())));
        return ok(result);
    }

    /**
     * 反启用子系统
     *
     * @param: [accountBookDto]
     * @return: com.njwd.support.Result<com.njwd.entity.basedata.BatchOperationDetails>
     * @author: zhuzs
     * @date: 2019-09-16 17:21
     */
    @RequestMapping("antiEnableAccountBookSystem")
    public Result<BatchOperationDetails> antiEnableAccountBookSystem (@RequestBody AccountBookDto accountBookDto){
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        accountBookDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        BatchOperationDetails result = accountBookSystemService.antiEnableAccountBookSystem(accountBookDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.company,
                LogConstant.operation.addAccountingBatch,
                LogConstant.operation.addAccountingBatch_type,
                String.valueOf(accountBookDto.getId())));
        return ok(result);
    }

    /**
     * 批量 反启用子系统
     *
     * @param: [accountBookDtos]
     * @return: com.njwd.support.Result<com.njwd.entity.basedata.BatchOperationDetails>
     * @author: zhuzs
     * @date: 2019-09-16 17:21
     */
    @RequestMapping("antiEnableAccountBookSystemBatch")
    public Result<BatchOperationDetails> antiEnableAccountBookSystemBatch (@RequestBody List<AccountBookDto> accountBookDtos){
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        BatchOperationDetails result = accountBookSystemService.antiEnableAccountBookSystemBatch(accountBookDtos);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.company,
                LogConstant.operation.addAccountingBatch,
                LogConstant.operation.addAccountingBatch_type,
                String.valueOf(result.getSuccessIds())));
        return ok(result);
    }

    /**
     * 获取账簿启用子系统 会计期间可选范围——平台
     *
     * @param: [platformAccountingPeriodDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.platform.vo.AccountingPeriodVo>>
     * @author: zhuzs
     * @date: 2019-09-16 17:22
     */
    @RequestMapping("findAccountingPeriod")
    public Result<List<AccountingPeriodVo>> findAccountingPeriod (@RequestBody AccountingPeriodDto platformAccountingPeriodDto){
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        return ok(accountingPeriodService.findAccountingPeriodByIsAdjustmentAndAccCalendarId(platformAccountingPeriodDto));
    }

    /**
     * 根据 ID 查询核算账簿
     *
     * @param: [accountBookDto]
     * @return: com.njwd.support.Result<com.njwd.entity.basedata.vo.AccountBookVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:22
     */
    @RequestMapping("findAccountBookById")
    public Result<AccountBookVo> findAccountBookById(@RequestBody AccountBookDto accountBookDto){
        SysUserVo operator = UserUtils.getUserVo();
        accountBookDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        return ok(accountBookService.findById(accountBookDto));
    }

    /**
     * 根据 公司ID 查询核算账簿
     *
     * @param: [accountBookDto]
     * @return: com.njwd.support.Result<com.njwd.entity.basedata.vo.AccountBookVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:22
     */
    @RequestMapping("findAccountBookByCompanyId")
    public Result<AccountBookVo> findAccountBookByCompanyId(@RequestBody AccountBookDto accountBookDto){
        SysUserVo operator = UserUtils.getUserVo();
        accountBookDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        return ok(accountBookService.findByCompanyId(accountBookDto));
    }

    /**
     * 根据 公司ID/账簿ID 查询核算账簿（默认核算主体，是否启用总帐模块，已打开的会计期间，会计准则，科目表）
     *
     * @param: [accountBookDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.basedata.vo.AccountBookVo>>
     * @author: zhuzs
     * @date: 2019-09-16 17:22
     */
    @RequestMapping("findAccBookDetailInfoByCompanyIdOrAccBookId")
    public Result<List<AccountBookVo>> findAccBookDetailInfoByCompanyIdOrAccBookId(@RequestBody AccountBookDto accountBookDto){
        return ok(accountBookService.findAccBookDetailInfoByCompanyIdOrAccBookId(accountBookDto));
    }

    /**
     * 查询核算账簿列表（含 子系统信息及子系统启用状态信息） 分页
     *
     * @param: [accountBookDto]
     * @return: com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountBookVo>>
     * @author: zhuzs
     * @date: 2019-09-16 17:22
     */
    @RequestMapping("findAccountBookPage")
    public Result<Page<AccountBookVo>> findAccountBookPage(@RequestBody AccountBookDto accountBookDto){
        SysUserVo operator = UserUtils.getUserVo();
        accountBookDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        if (accountBookDto.getIsEnterpriseAdmin() != null && Constant.Is.NO.equals(accountBookDto.getIsEnterpriseAdmin())) {
            // 非业务管理员时，添加userId，在查询时仅查询有对应公司权限的数据
            accountBookDto.setUserId(operator.getUserId());
        }else {
            ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        }
        return ok(accountBookService.findAccountBookPage(accountBookDto));
    }

    /**
     * 查询权限内核算账簿列表 （含核算主体、期间 信息）
     *
     * @param: [accountBookDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.basedata.vo.AccountBookVo>>
     * @author: zhuzs
     * @date: 2019-09-18 17:57
     */
    @RequestMapping("findAuthAllWithEntityInfo")
    public Result<List<AccountBookVo>>  findAuthAllWithEntityInfo(@RequestBody AccountBookDto accountBookDto){
        return ok(accountBookService.findAuthAllWithEntityInfo(accountBookDto));
    }

    /**
     * @Description 根据账簿idSet查询账簿列表信息
     * @Author 朱小明
     * @Date 2019/8/7 11:40
     * @Param
     * @return
     **/
    @PostMapping("findAccountBookListByIdSet")
    public Result<List<AccountBookVo>> findAccountBookListByIdSet(@RequestBody AccountBookDto accountBookDto){
        FastUtils.checkParams(accountBookDto.getIdSet());
        List<AccountBookVo> voList = accountBookService.findListByIdSet(accountBookDto.getIdSet());
        return ok(voList);
    }

    /**
     * @description: 获取权限内所有账簿
     * @param: []
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.basedata.vo.AccountBookVo>> 
     * @author: xdy        
     * @create: 2019-08-22 18-58 
     */
    @PostMapping("findAuthAll")
    public Result<List<AccountBookVo>> findAuthAll(@RequestBody AccountBookDto accountBookDto){
        return ok(accountBookService.findAuthAll(accountBookDto));
    }

    /**
     * 会计日历预览数据-平台
     *
     * @param: [accountingPeriodDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.platform.vo.AccountingPeriodVo>>
     * @author: zhuzs
     * @date: 2019-09-16 17:23
     */
    @PostMapping("findAccountBookPeriod")
    public Result<List<AccountingPeriodVo>> findAccountBookPeriod(@RequestBody AccountingPeriodDto accountingPeriodDto){
        FastUtils.checkParams(accountingPeriodDto.getAccCalendarId());
        return ok(accountBookService.findAccountBookPeriod(accountingPeriodDto));
    }

    /**
     * 资产负债表
     *
     * @param: [accountBookDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.platform.vo.FinancialReportItemSetVo>>
     * @author: zhuzs
     * @date: 2019-09-16 17:23
     */
    @PostMapping("findAssetReportList")
    public Result<List<FinancialReportItemSetVo>> findAssetReportList(@RequestBody AccountBookDto accountBookDto){
        return ok(accountBookService.findAssetReportList(accountBookDto));
    }

    /**
     * 现金流量表
     *
     * @param: [accountBookDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.platform.vo.FinancialReportItemSetVo>>
     * @author: zhuzs
     * @date: 2019-09-16 17:23
     */
    @PostMapping("findCashFlowReportList")
    public Result<List<FinancialReportItemSetVo>> findCashFlowReportList(@RequestBody AccountBookDto accountBookDto){
        return ok(accountBookService.findCashFlowReportList(accountBookDto));
    }

    /**
     * 利润表
     *
     * @param: [accountBookDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.platform.vo.FinancialReportItemSetVo>>
     * @author: zhuzs
     * @date: 2019-09-16 17:23
     */
    @PostMapping("findProfitReportList")
    public Result<List<FinancialReportItemSetVo>> findProfitReportList(@RequestBody AccountBookDto accountBookDto){
        return ok(accountBookService.findProfitReportList(accountBookDto));
    }

    /**
    * @Description
    * @Author 朱小明
    * @Date 2019/9/16
    * @param accountBookPeriodDto
    * @return com.njwd.support.Result<java.util.List<com.njwd.entity.platform.vo.AccountingPeriodVo>>
    **/
    @PostMapping("findAccountingPeriodForUpd")
    public Result<List<AccountingPeriodVo>> findAccountingPeriodForUpd(@RequestBody AccountBookPeriodDto accountBookPeriodDto){
        return ok(accountBookService.findAccountingPeriodForUpd(accountBookPeriodDto));
    }
}
