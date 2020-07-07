package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.LogConstant;
import com.njwd.entity.platform.dto.FinancialReportDto;
import com.njwd.entity.platform.vo.FinancialReportVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.logger.SenderService;
import com.njwd.platform.service.FinancialReportService;
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
 * @Description 财务报告
 * @Date:11:13 2019/6/25
 **/
@RestController
@RequestMapping("financialReport")
public class FinancialReportController extends BaseController {

    @Autowired
    private FinancialReportService financialReportService;

    @Resource
    private SenderService senderService;

    /**
     * 新增财务报表
     * @Author lj
     * @Date:9:33 2019/11/12
     * @param financialReportDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("addFinancialReport")
    public Result<Long> addFinancialReport(@RequestBody FinancialReportDto financialReportDto) {
        FastUtils.checkParams(financialReportDto.getAccStandardId(),financialReportDto.getReportTypeId(),
                financialReportDto.getAccStandardCode(),financialReportDto.getReportTypeCode(),financialReportDto.getName(),
                financialReportDto.getYear());
        Long id = financialReportService.addFinancialReport(financialReportDto);
        if(id>0){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.financialReport,
                    LogConstant.operation.add, LogConstant.operation.add_type, id.toString()));
        }
        return ok(id);
    }

    /**
     * 删除财务报表
     * @Author lj
     * @Date:9:33 2019/11/12
     * @param financialReportDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("delFinancialReportBatch")
    public Result<BatchResult> delFinancialReportBatch(@RequestBody FinancialReportDto financialReportDto) {
        //获取参数集合
        List<FinancialReportVo> changeList = financialReportDto.getChangeList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(changeList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for(FinancialReportVo vo : changeList){
            FastUtils.checkParams(vo.getId(),vo.getVersion());
        }
        //记录日志
        BatchResult batchResult = financialReportService.delFinancialReportBatch(financialReportDto);
        if(CollectionUtils.isNotEmpty(batchResult.getSuccessList())){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.financialReport,
                    LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, batchResult.getSuccessList().toString()));
        }
        return ok(batchResult);
    }

    /**
     * 修改财务报表
     * @Author lj
     * @Date:9:33 2019/11/12
     * @param financialReportDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("updateFinancialReport")
    public Result<Long> updateFinancialReport(@RequestBody FinancialReportDto financialReportDto) {
        FastUtils.checkParams(financialReportDto.getAccStandardId(),financialReportDto.getReportTypeId(),
                financialReportDto.getOldName(),financialReportDto.getId(),financialReportDto.getName(),
                financialReportDto.getYear(),financialReportDto.getVersion());
        Long id = financialReportService.updateFinancialReport(financialReportDto);
        if(id>0){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.financialReport,
                    LogConstant.operation.update, LogConstant.operation.update_type, id.toString()));
        }
        return ok(id);
    }

    /**
     * 审核财务报表
     * @Author lj
     * @Date:9:33 2019/11/12
     * @param financialReportDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("approveFinancialReportBatch")
    public Result<BatchResult> approveFinancialReportBatch(@RequestBody FinancialReportDto financialReportDto) {
        //获取参数集合
        List<FinancialReportVo> changeList = financialReportDto.getChangeList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(changeList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for(FinancialReportVo vo : changeList){
            FastUtils.checkParams(vo.getId(),vo.getVersion());
        }
        //记录日志
        BatchResult batchResult = financialReportService.approveFinancialReportBatch(financialReportDto);
        if(CollectionUtils.isNotEmpty(batchResult.getSuccessList())){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.financialReport,
                    LogConstant.operation.approve, LogConstant.operation.approve_type, batchResult.getSuccessList().toString()));
        }
        return ok(batchResult);
    }

    /**
     * 反审核财务报表
     * @Author lj
     * @Date:9:33 2019/11/12
     * @param financialReportDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("disApproveFinancialReportBatch")
    public Result<BatchResult> disApproveFinancialReportBatch(@RequestBody FinancialReportDto financialReportDto) {
        //获取参数集合
        List<FinancialReportVo> changeList = financialReportDto.getChangeList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(changeList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for(FinancialReportVo vo : changeList){
            FastUtils.checkParams(vo.getId(),vo.getVersion());
        }
        //记录日志
        BatchResult batchResult = financialReportService.disApproveFinancialReportBatch(financialReportDto);
        if(CollectionUtils.isNotEmpty(batchResult.getSuccessList())){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.financialReport,
                    LogConstant.operation.reversalApprove, LogConstant.operation.reversalApprove_type, batchResult.getSuccessList().toString()));
        }
        return ok(batchResult);
    }

    /**
     * 发布财务报表
     * @Author lj
     * @Date:9:33 2019/11/12
     * @param financialReportDto
     * @return com.njwd.support.Result<java.lang.Long>
     **/
    @PostMapping("releaseFinancialReportBatch")
    public Result<BatchResult> releaseFinancialReportBatch(@RequestBody FinancialReportDto financialReportDto) {
        //获取参数集合
        List<FinancialReportVo> changeList = financialReportDto.getChangeList();
        //editList 为空直接返回
        if(CollectionUtils.isEmpty(changeList)){
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //循环遍历参数集合，校验参数是否有值
        for(FinancialReportVo vo : changeList){
            FastUtils.checkParams(vo.getId(),vo.getVersion());
        }
        //记录日志
        BatchResult batchResult = financialReportService.releaseFinancialReportBatch(financialReportDto);
        if(CollectionUtils.isNotEmpty(batchResult.getSuccessList())){
            senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.PlatformSys, LogConstant.menuName.financialReport,
                    LogConstant.operation.release, LogConstant.operation.release_type, batchResult.getSuccessList().toString()));
        }
        return ok(batchResult);
    }

    /**
     * 根据ID查询财务报表
     * @Author lj
     * @Date:16:48 2019/11/18
     * @param financialReportDto
     * @return com.njwd.support.Result<com.njwd.entity.platform.vo.FinancialReportVo>
     **/
    @PostMapping("findFinancialReportById")
    public Result<FinancialReportVo> findFinancialReportById(@RequestBody FinancialReportDto financialReportDto){
        return ok(financialReportService.findFinancialReportById(financialReportDto));
    }

    /**
     * 查询财务报表分页
     * @Author lj
     * @Date:9:57 2019/11/18
     * @param financialReportDto
     * @return com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.FinancialReportVo>>
     **/
    @PostMapping("findFinancialReportListPage")
    public Result<Page<FinancialReportVo>> findFinancialReportListPage(@RequestBody FinancialReportDto financialReportDto){
        return ok(financialReportService.findFinancialReportListPage(financialReportDto));
    }
    /**
     * 导出
     * @param financialReportDto
     * @param response
     */
    @RequestMapping("exportExcel")
    public void exportExcel(@RequestBody FinancialReportDto financialReportDto, HttpServletResponse response){
        financialReportService.exportExcel(financialReportDto,response);
    }

    /**
     * @Description 根据会计准则ID查询利润表
     * @Author liuxiang
     * @Date:15:12 2019/7/2
     * @Param [financialReportVo]
     * @return java.lang.String
     **/
    @PostMapping("findProfitListByAccStandardId")
    public Result<List<FinancialReportVo>> findProfitListByAccStandardId(@RequestBody FinancialReportDto financialReportDto) {
        List<FinancialReportVo> financialReportVoList=financialReportService.findProfitListByAccStandardId(financialReportDto);
        return ok(financialReportVoList);
    }

    /**
     * @Description 查询利润表
     * @Author liuxiang
     * @Date:15:12 2019/7/2
     * @Param [financialReportVo]
     * @return java.lang.String
     **/
    @PostMapping("findProfitList")
    public Result<List<FinancialReportVo>> findProfitList() {
        List<FinancialReportVo> financialReportVoList=financialReportService.findProfitListByAccStandardId(new FinancialReportDto());
        return ok(financialReportVoList);
    }

    /**
     * @Description 根据会计准则ID查询资产负债表
     * @Author liuxiang
     * @Date:15:13 2019/7/2
     * @Param [financialReportVo]
     * @return java.lang.String
     **/
    @PostMapping("findAssetListByAccStandardId")
    public Result<List<FinancialReportVo>> findAssetListByAccStandardId(@RequestBody FinancialReportDto financialReportDto) {
        List<FinancialReportVo> financialReportVoList=financialReportService.findAssetListByAccStandardId(financialReportDto);
        return ok(financialReportVoList);
    }

    /**
     * @Description 查询资产负债表
     * @Author liuxiang
     * @Date:15:13 2019/7/2
     * @Param [financialReportVo]
     * @return java.lang.String
     **/
    @PostMapping("findAssetList")
    public Result<List<FinancialReportVo>> findAssetList() {
        List<FinancialReportVo> financialReportVoList=financialReportService.findAssetListByAccStandardId(new FinancialReportDto());
        return ok(financialReportVoList);
    }

    /**
     * @Description 查询现金流量表下拉框
     * @Author liuxiang
     * @Date:15:13 2019/7/2
     * @Param [financialReportDto]
     * @return java.lang.String
     **/
    @PostMapping("findCashFlowListByAccStandardId")
    public Result<List<FinancialReportVo>> findCashFlowListByAccStandardId(@RequestBody FinancialReportDto financialReportDto) {
        return ok(financialReportService.findCashFlowListByAccStandardId(financialReportDto));
    }

    /**
     * @Description 查询现金流量表下拉框
     * @Author liuxiang
     * @Date:15:13 2019/7/2
     * @Param [financialReportDto]
     * @return java.lang.String
     **/
    @PostMapping("findCashFlowList")
    public Result<List<FinancialReportVo>> findCashFlowList() {
        return ok(financialReportService.findCashFlowListByAccStandardId(new FinancialReportDto()));
    }


    /**
     * @Description 查询财务报告利润表
     * @Author liuxiang
     * @Date:17:48 2019/7/26
     * @Param []
     * @return java.lang.String
     **/
    @PostMapping("findPrfitList")
    public Result<List<FinancialReportVo>> findPrfitList() {
        return ok(financialReportService.findProfitList());
    }

    /**
     * @Description 查询财务报告资产负债表
     * @Author liuxiang
     * @Date:17:48 2019/7/26
     * @Param []
     * @return java.lang.String
     **/
    @PostMapping("findAsetList")
    public Result<List<FinancialReportVo>> findAsetList() {
        return ok(financialReportService.findAssetList());
    }

    /**
     * @Description 查询财务报告现金流量表
     * @Author liuxiang
     * @Date:17:48 2019/7/26
     * @Param []
     * @return java.lang.String
     **/
    @PostMapping("findCashFlwList")
    public Result<List<FinancialReportVo>> findCashFlwList() {
        return ok(financialReportService.findCashFlowList());
    }

}
