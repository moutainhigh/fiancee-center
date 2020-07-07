package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.LogConstant;
import com.njwd.entity.platform.dto.CashFlowDto;
import com.njwd.entity.platform.vo.CashFlowVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.logger.SenderService;
import com.njwd.platform.service.CashFlowService;
import com.njwd.platform.utils.UserUtil;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.HttpUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @Description 现金流量项目表
 * @Date:16:08 2019/6/13
 **/
@RestController
@RequestMapping("cashFlow")
public class CashFlowController extends BaseController {
    @Autowired
    private CashFlowService cashFlowService;
    @Resource
    private SenderService senderService;

    /**
     * 添加现金流量项目表
     * @Author lj
     * @Date:17:25 2019/11/11
     * @param cashFlowDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("addCashFlow")
    public Result<Long> addCashFlow(@RequestBody CashFlowDto cashFlowDto){
        FastUtils.checkParams(cashFlowDto.getName(),cashFlowDto.getAccStandardId(),
                cashFlowDto.getMaxLevelNum(),cashFlowDto.getAccStandardCode());
        Long id = cashFlowService.addCashFlow(cashFlowDto);
        if(id>0){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.cashFlow,
                    LogConstant.operation.add, LogConstant.operation.add_type, id.toString()));
        }
        return ok(id);
    }

    /**
     * 修改现金流量项目表
     * @Author lj
     * @Date:17:25 2019/11/11
     * @param cashFlowDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("updateCashFlow")
    public Result<Long> updateCashFlow(@RequestBody CashFlowDto cashFlowDto){
        FastUtils.checkParams(cashFlowDto.getName(),cashFlowDto.getAccStandardId(),cashFlowDto.getOldAccStandardId(),
                cashFlowDto.getMaxLevelNum(),cashFlowDto.getId(),cashFlowDto.getVersion());
        //记录日志
        Long id = cashFlowService.updateCashFlow(cashFlowDto);
        if(id>0){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.cashFlow,
                    LogConstant.operation.update, LogConstant.operation.update_type, id.toString()));
        }
        return ok(id);
    }

    /**
     * 删除现金流量项目表
     * @Author lj
     * @Date:17:25 2019/11/11
     * @param cashFlowDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("delCashFlowBatch")
    public Result<BatchResult> delCashFlowBatch(@RequestBody CashFlowDto cashFlowDto){
        //获取参数集合
        List<CashFlowVo> changeList = cashFlowDto.getChangeList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(changeList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for(CashFlowVo vo : changeList){
            FastUtils.checkParams(vo.getId(),vo.getVersion());
        }
        //记录日志
        BatchResult batchResult = cashFlowService.delCashFlowBatch(cashFlowDto);
        if(CollectionUtils.isNotEmpty(batchResult.getSuccessList())){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.cashFlow,
                    LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, batchResult.getSuccessList().toString()));
        }
        return ok(batchResult);
    }

    /**
     * 审核现金流量项目表
     * @Author lj
     * @Date:17:25 2019/11/11
     * @param cashFlowDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("approveCashFlowBatch")
    public Result<BatchResult> approveCashFlowBatch(@RequestBody CashFlowDto cashFlowDto){
        //获取参数集合
        List<CashFlowVo> changeList = cashFlowDto.getChangeList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(changeList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for(CashFlowVo vo : changeList){
            FastUtils.checkParams(vo.getId(),vo.getVersion());
        }
        //记录日志
        BatchResult batchResult = cashFlowService.approveCashFlowBatch(cashFlowDto);
        if(CollectionUtils.isNotEmpty(batchResult.getSuccessList())){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.cashFlow,
                    LogConstant.operation.approve, LogConstant.operation.approve_type, batchResult.getSuccessList().toString()));
        }
        return ok(batchResult);
    }

    /**
     * 反审核现金流量项目表
     * @Author lj
     * @Date:17:25 2019/11/11
     * @param cashFlowDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("disApproveCashFlowBatch")
    public Result<BatchResult> disApproveCashFlowBatch(@RequestBody CashFlowDto cashFlowDto){
        //获取参数集合
        List<CashFlowVo> changeList = cashFlowDto.getChangeList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(changeList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for(CashFlowVo vo : changeList){
            FastUtils.checkParams(vo.getId(),vo.getVersion());
        }
        //记录日志
        BatchResult batchResult = cashFlowService.disApproveCashFlowBatch(cashFlowDto);
        if(CollectionUtils.isNotEmpty(batchResult.getSuccessList())){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.cashFlow,
                    LogConstant.operation.reversalApprove, LogConstant.operation.reversalApprove_type, batchResult.getSuccessList().toString()));
        }
        return ok(batchResult);
    }

    /**
     * 发布现金流量项目表
     * @Author lj
     * @Date:17:25 2019/11/11
     * @param cashFlowDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("releaseCashFlowBatch")
    public Result<BatchResult> releaseCashFlowBatch(@RequestBody CashFlowDto cashFlowDto){
        //获取参数集合
        List<CashFlowVo> changeList = cashFlowDto.getChangeList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(changeList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for(CashFlowVo vo : changeList){
            FastUtils.checkParams(vo.getId(),vo.getVersion());
        }
        //记录日志
        BatchResult batchResult = cashFlowService.releaseCashFlowBatch(cashFlowDto);
        if(CollectionUtils.isNotEmpty(batchResult.getSuccessList())){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.cashFlow,
                    LogConstant.operation.release, LogConstant.operation.release_type, batchResult.getSuccessList().toString()));
        }
        return ok(batchResult);
    }

    /**
     * @Description 查询现金流量项目表列表
     * @Author liuxiang
     * @Date:15:09 2019/7/2
     * @Param [cashFlowDto]
     * @return java.lang.String
     **/
    @PostMapping("findCashFlowList")
    public Result<List<CashFlowVo>> findCashFlowList(@RequestBody CashFlowDto cashFlowDto){
        return ok(cashFlowService.findCashFlowList(cashFlowDto));
    }

    /**
     * 根据ID查询现金流量项目表
     * @Author lj
     * @Date:16:57 2019/11/13
     * @param cashFlowDto
     * @return com.njwd.support.Result<com.njwd.entity.platform.vo.CashFlowVo>
     **/
    @PostMapping("findCashFlowById")
    public Result<CashFlowVo> findCashFlowById(@RequestBody CashFlowDto cashFlowDto){
        FastUtils.checkParams(cashFlowDto.getId());
        return ok(cashFlowService.findCashFlowById(cashFlowDto));
    }

    /**
     * 查询现金流量项目表列表分页
     * @Author lj
     * @Date:11:27 2019/11/12
     * @param cashFlowDto
     * @return com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.CashFlowVo>>
     **/
    @PostMapping("findCashFlowListPage")
    public Result<Page<CashFlowVo>> findCashFlowListPage(@RequestBody CashFlowDto cashFlowDto){
        return ok(cashFlowService.findCashFlowListPage(cashFlowDto));
    }

    /**
     * 导出
     * @param cashFlowDto
     * @param response
     */
    @RequestMapping("exportExcel")
    public void exportExcel(@RequestBody CashFlowDto cashFlowDto, HttpServletResponse response){
        cashFlowService.exportExcel(cashFlowDto,response);
    }

    /**
     * @Description 根据会计准则id、账簿类型id查询现金流量项目表列表
     * @Author lj
     * @Date:15:14 2019/6/25            
     * @Param [cashFlowDto]
     * @return java.lang.String
     **/
    @PostMapping("findCashListByStandIdAndTypeId")
    public Result<List<CashFlowVo>> findCashListByStandIdAndTypeId(@RequestBody CashFlowDto cashFlowDto){
        return ok(cashFlowService.findCashListByStandIdAndTypeId(cashFlowDto));
    }
}
