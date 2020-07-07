package com.njwd.platform.controller;

import com.alibaba.excel.util.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.LogConstant;
import com.njwd.entity.platform.AccountingCalendar;
import com.njwd.entity.platform.AccountingPeriod;
import com.njwd.entity.platform.dto.AccountingCalendarDto;
import com.njwd.entity.platform.vo.AccountingCalendarVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.logger.SenderService;
import com.njwd.platform.service.AccountingCalendarService;
import com.njwd.platform.utils.UserUtil;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author lj
 * @Description 会计日历
 * @Date:14:33 2019/6/26
 **/
@RestController
@RequestMapping("accountingCalendar")
public class AccountingCalendarController extends BaseController {

    @Autowired
    private AccountingCalendarService accountingCalendarService;
    @Resource
    private SenderService senderService;

    /**
     * @Description 根据会计准则id和账簿类型id查询会计日历列表
     * @Author lj
     * @Date:14:37 2019/6/26
     * @Param [accountingCalendarDto]
     * @return java.lang.String
     **/
    @PostMapping("findAccCaListByAccTypeAndStand")
    public Result<List<AccountingCalendarVo>> findAccCaListByAccTypeAndStand(@RequestBody AccountingCalendarDto accountingCalendarDto){
        return ok(accountingCalendarService.findAccCaListByAccTypeAndStand(accountingCalendarDto));
    }
    /**
     * 刘遵通
     * 增加会计日历
     * @param accountingCalendarDto
     * @return
     */
    @RequestMapping("addAccountingCalendar")
    public Result<Long> addAccountingCalendar(@RequestBody AccountingCalendarDto accountingCalendarDto){
        //会计期间
        List<AccountingPeriod> accountingPeriodList = accountingCalendarDto.getAccountingPeriodList();
        if(CollectionUtils.isEmpty(accountingPeriodList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //校验数据
        FastUtils.checkParams(accountingCalendarDto.getName(),accountingCalendarDto.getStartDate(),
                accountingCalendarDto.getTypeId(),accountingCalendarDto.getStartYear(),accountingCalendarDto.getAdjustNum());
        Long id = accountingCalendarService.addAccountingCalendar(accountingCalendarDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.accountingCalendar,
                LogConstant.operation.add, LogConstant.operation.add_type, null));
        return ok(id);
    }
    /**
     * 刘遵通
     * 生成会计期间
     * @param accountingCalendarDto
     * @return
     */
    @RequestMapping("generateAccountingPeriod")
    public Result<List<AccountingPeriod>> generateAccountingPeriod(@RequestBody AccountingCalendarDto accountingCalendarDto){
        FastUtils.checkParams(accountingCalendarDto.getName(),accountingCalendarDto.getStartDate(),
                accountingCalendarDto.getTypeId(),accountingCalendarDto.getStartYear(),accountingCalendarDto.getAdjustNum());
        List<AccountingPeriod> accountingPeriods = accountingCalendarService.generateAccountingPeriod(accountingCalendarDto);
        return ok(accountingPeriods);
    }

    /**
     * 刘遵通
     *  查询页面 （分页）
     * @param accountingCalendarDto
     * @return
     */
    @RequestMapping("findPage")
    public Result<Page<AccountingCalendarVo>> findPage(@RequestBody AccountingCalendarDto accountingCalendarDto){
        Page<AccountingCalendarVo> AccountingCalendarList = accountingCalendarService.findPage(accountingCalendarDto);
        return  ok(AccountingCalendarList);
    }

    /**
     * 删除
     * @param accountingCalendarDto
     * @return
     */
    @RequestMapping("deleteAccountingCalendar")
    public Result<BatchResult> deleteAccountingCalendar(@RequestBody AccountingCalendarDto accountingCalendarDto){
        //获取参数集合
        List<AccountingCalendarDto> editList = accountingCalendarDto.getEditList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(editList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for(AccountingCalendarDto accountingCalendar : editList){
            FastUtils.checkParams(accountingCalendar.getId(),accountingCalendar.getVersion());
        }
        BatchResult batchResult = accountingCalendarService.deleteAccountingCalendar(accountingCalendarDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.accountingCalendar,
                LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, null));
        return ok(batchResult);
    }
    /**
     * 刘遵通
     * 审核
     * @param accountingCalendarDto
     * @return
     */
    @RequestMapping("checkApprove")
    public Result<BatchResult> checkApprove(@RequestBody AccountingCalendarDto accountingCalendarDto){
        //获取参数集合
        List<AccountingCalendarDto> editList = accountingCalendarDto.getEditList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(editList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for(AccountingCalendarDto accountingCalendar : editList){
            FastUtils.checkParams(accountingCalendar.getId(),accountingCalendar.getVersion());
        }
        BatchResult batchResult = accountingCalendarService.checkApprove(accountingCalendarDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.accountingCalendar,
                LogConstant.operation.approve, LogConstant.operation.approve_type, null));
        return ok(batchResult);
    }
    /**
     * 刘遵通
     * 反审核
     * @param accountingCalendarDto
     * @return
     */
    @RequestMapping("reversalApprove")
    public Result<BatchResult> reversalApprove(@RequestBody AccountingCalendarDto accountingCalendarDto){
        //获取参数集合
        List<AccountingCalendarDto> editList = accountingCalendarDto.getEditList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(editList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for(AccountingCalendarDto accountingCalendar : editList){
            FastUtils.checkParams(accountingCalendar.getId(),accountingCalendar.getVersion());
        }
        BatchResult batchResult = accountingCalendarService.reversalApprove(accountingCalendarDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.accountingCalendar,
                LogConstant.operation.reversalApprove, LogConstant.operation.reversalApprove_type, null));
        return ok(batchResult);
    }
    /**
     * 刘遵通
     * 发布
     * @param accountingCalendarDto
     * @return
     */
    @RequestMapping("release")
    public Result<BatchResult> release(@RequestBody AccountingCalendarDto accountingCalendarDto){
        //获取参数集合
        List<AccountingCalendarDto> editList = accountingCalendarDto.getEditList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(editList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for(AccountingCalendarDto accountingCalendar : editList){
            FastUtils.checkParams(accountingCalendar.getId(),accountingCalendar.getVersion());
        }
        BatchResult batchResult = accountingCalendarService.release(accountingCalendarDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.accountingCalendar,
                LogConstant.operation.release, LogConstant.operation.release_type, null));
        return ok(batchResult);
    }
    /**
     * 编辑中的查看
     * @param accountingCalendarDto
     * @return
     */
    @RequestMapping("selectById")
    public Result<AccountingCalendarVo> selectById(@RequestBody AccountingCalendarDto accountingCalendarDto){
        AccountingCalendarVo accountingCalendarVo = accountingCalendarService.selectById(accountingCalendarDto);
        return  ok(accountingCalendarVo);
    }
    /**
     * 修改
     * @return
     */
    @RequestMapping("updateById")
    public Result<Long> updateById(@RequestBody AccountingCalendarDto accountingCalendarDto){
        //会计期间
        List<AccountingPeriod> accountingPeriodList = accountingCalendarDto.getAccountingPeriodList();
        if(CollectionUtils.isEmpty(accountingPeriodList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //校验数据
        FastUtils.checkParams(accountingCalendarDto.getId(),accountingCalendarDto.getCode(),accountingCalendarDto.getName(),accountingCalendarDto.getStartDate(),
                accountingCalendarDto.getTypeId(),accountingCalendarDto.getStartYear(),accountingCalendarDto.getAdjustNum());
        Long id = accountingCalendarService.updateById(accountingCalendarDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.accountingCalendar,
                LogConstant.operation.update, LogConstant.operation.update_type, null));
        return ok(id);
    }

    /**
     * 追加
     * @param accountingCalendarDto
     * @return
     */
    @RequestMapping("appendById")
    public Result<List<AccountingPeriod>> appendById(@RequestBody AccountingCalendarDto accountingCalendarDto){
        List<AccountingPeriod> accountingPeriodList = accountingCalendarService.appendById(accountingCalendarDto);
        return ok(accountingPeriodList);
    }
    /**
     * 导出
     * @param accountingCalendarDto
     * @param response
     */
    @RequestMapping("exportExcel")
    public void exportExcel(@RequestBody AccountingCalendarDto accountingCalendarDto, HttpServletResponse response){
        accountingCalendarService.exportExcel(accountingCalendarDto,response);
    }
}
