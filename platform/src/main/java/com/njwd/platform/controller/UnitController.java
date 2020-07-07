package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.platform.dto.UnitDto;
import com.njwd.entity.platform.vo.UnitVo;
import com.njwd.logger.SenderService;
import com.njwd.platform.service.UnitService;
import com.njwd.platform.utils.UserUtil;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.HttpUtils;
import com.njwd.utils.UserUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @description: 计量单位
 * @author: xdy
 * @create: 2019/11/15 10:33
 */
@RestController
@RequestMapping("unit")
public class UnitController extends BaseController {

    @Resource
    private UnitService unitService;
    @Resource
    private SenderService senderService;
    
    /**
     * @description: 新增计量单位
     * @param: [unitDto]
     * @return: com.njwd.support.Result<com.njwd.entity.platform.vo.UnitVo> 
     * @author: xdy        
     * @create: 2019-11-19 11:16 
     */
    @RequestMapping("addUnit")
    public Result<UnitVo> addUnit(@RequestBody UnitDto unitDto){
        FastUtils.checkParams(unitDto.getCode(),unitDto.getName(),unitDto.getPrecision(),unitDto.getRoundingType());
        if(Constant.Is.NO.equals(unitDto.getIsBase())){
            FastUtils.checkParams(unitDto.getConversionId(),unitDto.getConversionValue());
        }
        UnitVo unitVo = unitService.addUnit(unitDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.unit,
                LogConstant.operation.add, LogConstant.operation.add_type, null));
        return ok(unitVo);
    }
    
    /**
     * @description: 删除计量单位
     * @param: [unitDto]
     * @return: com.njwd.support.Result<com.njwd.support.BatchResult> 
     * @author: xdy        
     * @create: 2019-11-19 11:16 
     */
    @RequestMapping("deleteUnit")
    public Result<BatchResult> deleteUnit(@RequestBody UnitDto unitDto){
        FastUtils.checkParams(unitDto.getIdList());
        BatchResult batchResult = unitService.deleteUnit(unitDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.unit,
                LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, null));
        return ok(batchResult);
    }
    
    /**
     * @description: 修改计量单位
     * @param: [unitDto]
     * @return: com.njwd.support.Result<com.njwd.entity.platform.vo.UnitVo> 
     * @author: xdy        
     * @create: 2019-11-19 11:16 
     */
    @RequestMapping("updateUnit")
    public Result<UnitVo> updateUnit(@RequestBody UnitDto unitDto){
        FastUtils.checkParams(unitDto.getId(),unitDto.getCode(),unitDto.getName(),unitDto.getPrecision(),unitDto.getRoundingType());
        if(Constant.Is.YES.equals(unitDto.getIsBase())){
            FastUtils.checkParams(unitDto.getConversionId(),unitDto.getConversionValue());
        }
        UnitVo unitVo = unitService.updateUnit(unitDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.unit,
                LogConstant.operation.update, LogConstant.operation.update_type, null));
        return ok(unitVo);
    }
    
    /**
     * @description: 根据ID查询计量单位
     * @param: [unitDto]
     * @return: com.njwd.support.Result<com.njwd.entity.platform.vo.UnitVo> 
     * @author: xdy        
     * @create: 2019-11-19 11:16 
     */
    @RequestMapping("findUnitById")
    public Result<UnitVo> findUnitById(@RequestBody UnitDto unitDto){
        FastUtils.checkParams(unitDto.getId());
        return ok(unitService.findUnitById(unitDto));
    }
    
    /**
     * @description: 计量单位分页
     * @param: [unitDto]
     * @return: com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.UnitVo>> 
     * @author: xdy        
     * @create: 2019-11-19 11:16 
     */
    @RequestMapping("findUnitPage")
    public Result<Page<UnitVo>> findUnitPage(@RequestBody UnitDto unitDto){
        return ok(unitService.findUnitPage(unitDto));
    }
    
    /**
     * @description: 审核计量单位
     * @param: [unitDto]
     * @return: com.njwd.support.Result<com.njwd.support.BatchResult> 
     * @author: xdy        
     * @create: 2019-11-19 11:16 
     */
    @RequestMapping("approveUnit")
    public Result<BatchResult> approveUnit(@RequestBody UnitDto unitDto){
        FastUtils.checkParams(unitDto.getIdList());
        BatchResult batchResult = unitService.approveUnit(unitDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.unit,
                LogConstant.operation.approve, LogConstant.operation.approve_type, null));
        return ok(batchResult);
    }
    
    /**
     * @description: 反审核计量单位
     * @param: [unitDto]
     * @return: com.njwd.support.Result<com.njwd.support.BatchResult> 
     * @author: xdy        
     * @create: 2019-11-19 11:16 
     */
    @RequestMapping("reverseApproveUnit")
    public Result<BatchResult> reverseApproveUnit(@RequestBody UnitDto unitDto){
        FastUtils.checkParams(unitDto.getIdList());
        BatchResult batchResult = unitService.reverseApproveUnit(unitDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.unit,
                LogConstant.operation.reversalApprove, LogConstant.operation.reversalApprove_type, null));
        return ok(batchResult);
    }

    /**
     * @description: 发布计量单位
     * @param: [unitDto]
     * @return: com.njwd.support.Result<com.njwd.support.BatchResult> 
     * @author: xdy        
     * @create: 2019-11-19 11:16 
     */
    @RequestMapping("releaseUnit")
    public Result<BatchResult> releaseUnit(@RequestBody UnitDto unitDto){
        FastUtils.checkParams(unitDto.getIdList());
        BatchResult batchResult = unitService.releaseUnit(unitDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.unit,
                LogConstant.operation.release, LogConstant.operation.release_type, null));
        return ok(batchResult);
    }
    
    /**
     * @description: 导出计量单位
     * @param: [unitDto, response]
     * @return: void 
     * @author: xdy        
     * @create: 2019-11-19 11:16 
     */
    @RequestMapping("exportExcel")
    public void exportExcel(@RequestBody UnitDto unitDto, HttpServletResponse response){
        unitService.exportExcel(unitDto,response);
    }
    
    /**
     * @description: 计量单位列表
     * @param: [unitDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.platform.vo.UnitVo>> 
     * @author: xdy        
     * @create: 2019-11-19 16:42
     */
    @RequestMapping("findUnitList")
    public Result<List<UnitVo>> findUnitList(@RequestBody UnitDto unitDto){
        return ok(unitService.findUnitList(unitDto));
    }

}
