package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.LogConstant;
import com.njwd.entity.platform.dto.CashFlowItemDto;
import com.njwd.entity.platform.vo.CashFlowItemVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.logger.SenderService;
import com.njwd.platform.service.CashFlowItemService;
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
 * @Description 现金流量项目
 * @Date:16:31 2019/6/13
 **/
@RestController
@RequestMapping("cashFlowItem")
public class CashFlowItemController extends BaseController {
    @Autowired
    private CashFlowItemService cashFlowItemService;

    @Resource
    private SenderService senderService;

    /**
     * 添加现金流量项目
     * @Author lj
     * @Date:17:25 2019/11/11
     * @param cashFlowItemDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("addCashFlowItem")
    public Result<Long> addCashFlowItem(@RequestBody CashFlowItemDto cashFlowItemDto){
        FastUtils.checkParams(cashFlowItemDto.getCashFlowId(),cashFlowItemDto.getCode(),
                cashFlowItemDto.getName(),cashFlowItemDto.getCashFlowDirection());
        Long id = cashFlowItemService.addCashFlowItem(cashFlowItemDto);
        if(id>0){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.cashFlowItems,
                    LogConstant.operation.add, LogConstant.operation.add_type, id.toString()));
        }
        return ok(id);
    }

    /**
     * 删除现金流量项目
     * @Author lj
     * @Date:17:25 2019/11/11
     * @param cashFlowItemDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("delCashFlowItemBatch")
    public Result<BatchResult> delCashFlowItemBatch(@RequestBody CashFlowItemDto cashFlowItemDto){
        //获取参数集合
        List<CashFlowItemVo> changeList = cashFlowItemDto.getChangeList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(changeList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for(CashFlowItemVo vo : changeList){
            FastUtils.checkParams(vo.getId(),vo.getVersion());
        }
        //记录日志
        BatchResult batchResult = cashFlowItemService.delCashFlowItemBatch(cashFlowItemDto);
        if(CollectionUtils.isNotEmpty(batchResult.getSuccessList())){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.cashFlowItems,
                    LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, batchResult.getSuccessList().toString()));
        }
        return ok(batchResult);
    }

    /**
     * 修改现金流量项目
     * @Author lj
     * @Date:17:25 2019/11/11
     * @param cashFlowItemDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("updateCashFlowItem")
    public Result<Long> updateCashFlowItem(@RequestBody CashFlowItemDto cashFlowItemDto){
        FastUtils.checkParams(cashFlowItemDto.getCashFlowId(),cashFlowItemDto.getCode(),
                cashFlowItemDto.getName(),cashFlowItemDto.getCashFlowDirection(),cashFlowItemDto.getId(),
                cashFlowItemDto.getOldName(),cashFlowItemDto.getOldCode(),cashFlowItemDto.getVersion());
        Long id = cashFlowItemService.updateCashFlowItem(cashFlowItemDto);
        if(id>0){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.cashFlowItems,
                    LogConstant.operation.update, LogConstant.operation.update_type, id.toString()));
        }
        return ok(id);
    }

    /**
     * 审核现金流量项目
     * @Author lj
     * @Date:17:25 2019/11/11
     * @param cashFlowItemDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("approveCashFlowItemBatch")
    public Result<BatchResult> approveCashFlowItemBatch(@RequestBody CashFlowItemDto cashFlowItemDto){
        //获取参数集合
        List<CashFlowItemVo> changeList = cashFlowItemDto.getChangeList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(changeList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for(CashFlowItemVo vo : changeList){
            FastUtils.checkParams(vo.getId(),vo.getVersion());
        }
        //记录日志
        BatchResult batchResult = cashFlowItemService.approveCashFlowItemBatch(cashFlowItemDto);
        if(CollectionUtils.isNotEmpty(batchResult.getSuccessList())){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.cashFlowItems,
                    LogConstant.operation.approve, LogConstant.operation.approve_type, batchResult.getSuccessList().toString()));
        }
        return ok(batchResult);
    }

    /**
     * 反审核现金流量项目
     * @Author lj
     * @Date:17:25 2019/11/11
     * @param cashFlowItemDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("disApproveCashFlowItemBatch")
    public Result<BatchResult> disApproveCashFlowItemBatch(@RequestBody CashFlowItemDto cashFlowItemDto){
        //获取参数集合
        List<CashFlowItemVo> changeList = cashFlowItemDto.getChangeList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(changeList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for(CashFlowItemVo vo : changeList){
            FastUtils.checkParams(vo.getId(),vo.getVersion());
        }
        //记录日志
        BatchResult batchResult = cashFlowItemService.disApproveCashFlowItemBatch(cashFlowItemDto);
        if(CollectionUtils.isNotEmpty(batchResult.getSuccessList())){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.cashFlowItems,
                    LogConstant.operation.reversalApprove, LogConstant.operation.reversalApprove_type, batchResult.getSuccessList().toString()));
        }
        return ok(batchResult);
    }

    /**
     * 发布现金流量项目
     * @Author lj
     * @Date:17:25 2019/11/11
     * @param cashFlowItemDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("releaseCashFlowItemBatch")
    public Result<BatchResult> releaseCashFlowItemBatch(@RequestBody CashFlowItemDto cashFlowItemDto){
        //获取参数集合
        List<CashFlowItemVo> changeList = cashFlowItemDto.getChangeList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(changeList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for(CashFlowItemVo vo : changeList){
            FastUtils.checkParams(vo.getId(),vo.getVersion());
        }
        //记录日志
        BatchResult batchResult = cashFlowItemService.releaseCashFlowItemBatch(cashFlowItemDto);
        if(CollectionUtils.isNotEmpty(batchResult.getSuccessList())){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.cashFlowItems,
                    LogConstant.operation.release, LogConstant.operation.release_type, batchResult.getSuccessList().toString()));
        }
        return ok(batchResult);
    }

    /**
     * 根据ID查询现金流量项目
     * @Author lj
     * @Date:16:57 2019/11/13
     * @param cashFlowItemDto
     * @return com.njwd.support.Result<com.njwd.entity.platform.vo.CashFlowVo>
     **/
    @PostMapping("findCashFlowItemById")
    public Result<CashFlowItemVo> findCashFlowItemById(@RequestBody CashFlowItemDto cashFlowItemDto){
        FastUtils.checkParams(cashFlowItemDto.getId());
        return ok(cashFlowItemService.findCashFlowItemById(cashFlowItemDto));
    }

    /**
     * 查询现金流量项目分页
     * @Author lj
     * @Date:17:44 2019/11/13
     * @param cashFlowItemDto
     * @return com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.CashFlowItemVo>>
     **/
    @PostMapping("findCashFlowItemPageNew")
    public Result<Page<CashFlowItemVo>> findCashFlowItemPageNew(@RequestBody CashFlowItemDto cashFlowItemDto){
        return ok(cashFlowItemService.findCashFlowItemPageNew(cashFlowItemDto));
    }

    /**
     * 查询现金流量项目列表-初始化基础资料
     * @Author lj
     * @Date:17:29 2019/12/2
     * @param cashFlowItemDto
     * @return java.util.List<com.njwd.entity.platform.vo.CashFlowItemVo>
     **/
    @PostMapping("findCashFlowItemListNew")
    public Result<List<CashFlowItemVo>> findCashFlowItemNew(@RequestBody CashFlowItemDto cashFlowItemDto){
        return ok(cashFlowItemService.findCashFlowItemNew(cashFlowItemDto));
    }

    /**
     * 导出
     * @param cashFlowItemDto
     * @param response
     */
    @RequestMapping("exportExcel")
    public void exportExcel(@RequestBody CashFlowItemDto cashFlowItemDto, HttpServletResponse response){
        cashFlowItemService.exportExcel(cashFlowItemDto,response);
    }

    /**
     * @Description 查询现金流量项目列表
     * @Author liuxiang
     * @Date:15:10 2019/7/2
     * @Param [cashFlowItemDto]
     * @return java.lang.String
     **/
    @PostMapping("findCashFlowItemList")
    public Result<List<CashFlowItemVo>> findCashFlowItemList(@RequestBody CashFlowItemDto cashFlowItemDto) {
        return ok(cashFlowItemService.findCashFlowItemList(cashFlowItemDto));
    }

    /**
     * @Description 查询现金流量项目分页
     * @Author liuxiang
     * @Date:15:10 2019/7/2
     * @Param [cashFlowItemDto]
     * @return java.lang.String
     **/
    @PostMapping("findCashFlowItemPage")
    public Result<Page<CashFlowItemVo>> findCashFlowItemPage(@RequestBody CashFlowItemDto cashFlowItemDto){
        return ok(cashFlowItemService.findCashFlowItemPage(cashFlowItemDto));
    }
}
