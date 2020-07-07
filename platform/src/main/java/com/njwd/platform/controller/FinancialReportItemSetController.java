package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.LogConstant;
import com.njwd.entity.platform.dto.FinancialReportItemDto;
import com.njwd.entity.platform.dto.FinancialReportItemSetDto;
import com.njwd.entity.platform.vo.FinancialReportItemSetVo;
import com.njwd.entity.platform.vo.FinancialReportItemVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.logger.SenderService;
import com.njwd.platform.service.FinancialReportItemSetService;
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
import java.util.List;

/**
 * @Author liuxiang
 * @Description 财务报告项目明细设置
 * @Date:11:13 2019/6/25
 **/
@RestController
@RequestMapping("financialReportItemSet")
public class FinancialReportItemSetController extends BaseController {

    @Autowired
    private FinancialReportItemSetService financialReportItemSetService;

    @Resource
    private SenderService senderService;

    /**
     * 新增报表项目库
     * @Author lj
     * @Date:9:13 2019/11/12
     * @param financialReportItemDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("addFinancialReportItem")
    public Result<Long> addFinancialReportItem(@RequestBody FinancialReportItemDto financialReportItemDto){
        FastUtils.checkParams(financialReportItemDto.getReportTypeId(),financialReportItemDto.getReportTypeCode(),
                financialReportItemDto.getName());
        Long id = financialReportItemSetService.addFinancialReportItem(financialReportItemDto);
        if(id>0){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.financialReportItem,
                    LogConstant.operation.add, LogConstant.operation.add_type, id.toString()));
        }
        return ok(id);
    }

    /**
     * 删除报表项目库
     * @Author lj
     * @Date:9:13 2019/11/12
     * @param financialReportItemDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("delFinancialReportItemBatch")
    public Result<BatchResult> delFinancialReportItemBatch(@RequestBody FinancialReportItemDto financialReportItemDto){
        //获取参数集合
        List<FinancialReportItemVo> changeList = financialReportItemDto.getChangeList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(changeList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for(FinancialReportItemVo vo : changeList){
            FastUtils.checkParams(vo.getId(),vo.getVersion());
        }
        //记录日志
        BatchResult batchResult = financialReportItemSetService.delFinancialReportItemBatch(financialReportItemDto);
        if(CollectionUtils.isNotEmpty(batchResult.getSuccessList())){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.financialReportItem,
                    LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, batchResult.getSuccessList().toString()));
        }
        return ok(batchResult);
    }

    /**
     * 修改报表项目库
     * @Author lj
     * @Date:9:13 2019/11/12
     * @param financialReportItemDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("updateFinancialReportItem")
    public Result<Long> updateFinancialReportItem(@RequestBody FinancialReportItemDto financialReportItemDto){
        FastUtils.checkParams(financialReportItemDto.getReportTypeId(),financialReportItemDto.getId(),
                financialReportItemDto.getName(),financialReportItemDto.getOldName(),
                financialReportItemDto.getOldReportTypeId(),financialReportItemDto.getVersion());
        Long id = financialReportItemSetService.updateFinancialReportItem(financialReportItemDto);
        if(id>0){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.financialReportItem,
                    LogConstant.operation.update, LogConstant.operation.update_type, id.toString()));
        }
        return ok(id);
    }

    /**
     * 审核报表项目库
     * @Author lj
     * @Date:9:13 2019/11/12
     * @param financialReportItemDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("approveFinancialReportItemBatch")
    public Result<BatchResult> approveFinancialReportItemBatch(@RequestBody FinancialReportItemDto financialReportItemDto){
        //获取参数集合
        List<FinancialReportItemVo> changeList = financialReportItemDto.getChangeList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(changeList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for(FinancialReportItemVo vo : changeList){
            FastUtils.checkParams(vo.getId(),vo.getVersion());
        }
        //记录日志
        BatchResult batchResult = financialReportItemSetService.approveFinancialReportItemBatch(financialReportItemDto);
        if(CollectionUtils.isNotEmpty(batchResult.getSuccessList())){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.financialReportItem,
                    LogConstant.operation.approve, LogConstant.operation.approve_type, batchResult.getSuccessList().toString()));
        }
        return ok(batchResult);
    }

    /**
     * 反审核报表项目库
     * @Author lj
     * @Date:9:13 2019/11/12
     * @param financialReportItemDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("disApproveFinancialReportItemBatch")
    public Result<BatchResult> disApproveFinancialReportItemBatch(@RequestBody FinancialReportItemDto financialReportItemDto){
        //获取参数集合
        List<FinancialReportItemVo> changeList = financialReportItemDto.getChangeList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(changeList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for(FinancialReportItemVo vo : changeList){
            FastUtils.checkParams(vo.getId(),vo.getVersion());
        }
        //记录日志
        BatchResult batchResult = financialReportItemSetService.disApproveFinancialReportItemBatch(financialReportItemDto);
        if(CollectionUtils.isNotEmpty(batchResult.getSuccessList())){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.financialReportItem,
                    LogConstant.operation.reversalApprove, LogConstant.operation.reversalApprove_type, batchResult.getSuccessList().toString()));
        }
        return ok(batchResult);
    }

    /**
     * 添加财务报告项目明细
     * @Author lj
     * @Date:9:54 2019/11/12
     * @param financialReportItemSetDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("addFinancialReportItemSet")
    public Result<Long> addFinancialReportItemSet(@RequestBody FinancialReportItemSetDto financialReportItemSetDto){
        FastUtils.checkParams(financialReportItemSetDto.getReportId(),financialReportItemSetDto.getReportItemId(),
                financialReportItemSetDto.getName(),financialReportItemSetDto.getDirection(),
                financialReportItemSetDto.getLevel(),
                financialReportItemSetDto.getReportTypeId(),financialReportItemSetDto.getSort());
        Long id = financialReportItemSetService.addFinancialReportItemSet(financialReportItemSetDto);
        if(id>0){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.financialReportItemSet,
                    LogConstant.operation.add, LogConstant.operation.add_type, id.toString()));
        }
        return ok(id);
    }

    /**
     * 删除财务报告项目明细
     * @Author lj
     * @Date:9:54 2019/11/12
     * @param financialReportItemSetDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("delFinancialReportItemSetBatch")
    public Result<BatchResult> delFinancialReportItemSetBatch(@RequestBody FinancialReportItemSetDto financialReportItemSetDto){
        //获取参数集合
        List<FinancialReportItemSetVo> changeList = financialReportItemSetDto.getChangeList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(changeList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for(FinancialReportItemSetVo vo : changeList){
            FastUtils.checkParams(vo.getId(),vo.getReportId());
        }
        //记录日志
        BatchResult batchResult = financialReportItemSetService.delFinancialReportItemSetBatch(financialReportItemSetDto);
        if(CollectionUtils.isNotEmpty(batchResult.getSuccessList())){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.financialReportItemSet,
                    LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, batchResult.getSuccessList().toString()));
        }
        return ok(batchResult);
    }

    /**
     * 修改财务报告项目明细
     * @Author lj
     * @Date:9:54 2019/11/12
     * @param financialReportItemSetDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("updateFinancialReportItemSet")
    public Result<Long> updateFinancialReportItemSet(@RequestBody FinancialReportItemSetDto financialReportItemSetDto){
        FastUtils.checkParams(financialReportItemSetDto.getReportId(),financialReportItemSetDto.getReportItemId(),
                financialReportItemSetDto.getName(),financialReportItemSetDto.getDirection(),
                financialReportItemSetDto.getLevel(),
                financialReportItemSetDto.getReportTypeId(),financialReportItemSetDto.getId(),
                financialReportItemSetDto.getOldName(),financialReportItemSetDto.getVersion(),financialReportItemSetDto.getSort(),financialReportItemSetDto.getOldSort());
        Long id = financialReportItemSetService.updateFinancialReportItemSet(financialReportItemSetDto);
        if(id>0){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.financialReportItemSet,
                    LogConstant.operation.update, LogConstant.operation.update_type, id.toString()));
        }
        return ok(id);
    }

    /**
     * 清空表达式
     * @Author lj
     * @Date:9:54 2019/11/12
     * @param financialReportItemSetDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("clear")
    public Result<Integer> clear(@RequestBody FinancialReportItemSetDto financialReportItemSetDto){
        FastUtils.checkParams(financialReportItemSetDto.getId());
        return ok(financialReportItemSetService.clear(financialReportItemSetDto));
    }

    /**
     * 根据ID查询报表项目库
     * @Author lj
     * @Date:18:09 2019/11/15
     * @param dto
     * @return com.njwd.support.Result<com.njwd.entity.platform.vo.FinancialReportItemVo>
     **/
    @PostMapping("findReportItemById")
    public Result<FinancialReportItemVo> findReportItemById(@RequestBody FinancialReportItemDto dto){
        return ok(financialReportItemSetService.findReportItemById(dto));
    }

    /**
     * 查询报表项目库分页
     * @Author lj
     * @Date:15:16 2019/11/15
     * @param financialReportItemDto
     * @return com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.FinancialReportItemVo>>
     **/
    @PostMapping("findReportItemListPage")
    public Result<Page<FinancialReportItemVo>> findReportItemListPage(@RequestBody FinancialReportItemDto financialReportItemDto){
        return ok(financialReportItemSetService.findReportItemListPage(financialReportItemDto));
    }

    /**
     * 导出
     * @param financialReportItemDto
     * @param response
     */
    @RequestMapping("exportExcel")
    public void exportExcel(@RequestBody FinancialReportItemDto financialReportItemDto, HttpServletResponse response){
        financialReportItemSetService.exportExcel(financialReportItemDto,response);
    }

    /**
     * 根据报告ID查询财务报告项目明细列表
     * @Author lj
     * @Date:9:25 2019/11/19
     * @param financialReportItemSetDto
     * @return com.njwd.support.Result<java.util.List<com.njwd.entity.platform.vo.FinancialReportItemSetVo>>
     **/
    @PostMapping("findReportItemSetList")
    public Result<List<FinancialReportItemSetVo>> findReportItemSetList(@RequestBody FinancialReportItemSetDto financialReportItemSetDto) {
        return ok(financialReportItemSetService.findReportItemSetList(financialReportItemSetDto));
    }

    /**
     * 根据报告ID查询财务报告项目明细列表分页
     * @Author lj
     * @Date:9:25 2019/11/19
     * @param financialReportItemSetDto
     * @return com.njwd.support.Result<java.util.List<com.njwd.entity.platform.vo.FinancialReportItemSetVo>>
     **/
    @PostMapping("findReportItemSetListPage")
    public Result<Page<FinancialReportItemSetVo>> findReportItemSetListPage(@RequestBody FinancialReportItemSetDto financialReportItemSetDto) {
        return ok(financialReportItemSetService.findReportItemSetListPage(financialReportItemSetDto));
    }

    /**
     * 根据报告项目ID查询财务报告项目明细
     * @Author lj
     * @Date:9:25 2019/11/19
     * @param financialReportItemSetDto
     * @return com.njwd.support.Result<java.util.List<com.njwd.entity.platform.vo.FinancialReportItemSetVo>>
     **/
    @PostMapping("findReportItemSetById")
    public Result<FinancialReportItemSetVo> findReportItemSetById(@RequestBody FinancialReportItemSetDto financialReportItemSetDto) {
        return ok(financialReportItemSetService.findReportItemSetById(financialReportItemSetDto));
    }

    /**
     * @Description 根据报告库ID查询财务报告项目明细设置列表
     * @Author liuxiang
     * @Date:17:47 2019/8/1
     * @Param [financialReportItemSetDto]
     * @return java.lang.String
     **/
    @PostMapping("findFinancialReportItemSetList")
    public Result<List<FinancialReportItemSetVo>> findFinancialReportItemSetList(@RequestBody FinancialReportItemSetDto financialReportItemSetDto) {
        List<FinancialReportItemSetVo> financialReportItemSetVoList=financialReportItemSetService.findFinancialReportItemSetList(financialReportItemSetDto);
        return ok(financialReportItemSetVoList);
    }

}
