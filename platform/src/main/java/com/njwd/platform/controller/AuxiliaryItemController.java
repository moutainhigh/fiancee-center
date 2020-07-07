package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.LogConstant;
import com.njwd.entity.platform.SourceOfValue;
import com.njwd.entity.platform.dto.AuxiliaryItemDto;
import com.njwd.entity.platform.dto.SourceOfValueDto;
import com.njwd.entity.platform.dto.SubjectDto;
import com.njwd.entity.platform.vo.AuxiliaryItemVo;
import com.njwd.logger.SenderService;
import com.njwd.platform.service.AuxiliaryItemService;
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
 * @Author liuxiang
 * @Description 辅助核算
 * @Date:17:57 2019/6/17
 **/
@RestController
@RequestMapping("auxiliaryItem")
public class AuxiliaryItemController extends BaseController {

    @Autowired
    private AuxiliaryItemService auxiliaryItemService;
    @Resource
    private SenderService senderService;
    
    /**
     * @description: 新增核算项目
     * @param: [auxiliaryItemDto]
     * @return: com.njwd.support.Result<com.njwd.entity.platform.vo.AuxiliaryItemVo> 
     * @author: xdy        
     * @create: 2019-11-21 15:49 
     */
    @RequestMapping("addAuxiliaryItem")
    public Result<AuxiliaryItemVo> addAuxiliaryItem(@RequestBody AuxiliaryItemDto auxiliaryItemDto){
        FastUtils.checkParams(auxiliaryItemDto.getName(),auxiliaryItemDto.getSourceId());
        AuxiliaryItemVo auxiliaryItemVo = auxiliaryItemService.addAuxiliaryItem(auxiliaryItemDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.auxiliaryItem,
                LogConstant.operation.add, LogConstant.operation.add_type, null));
        return ok(auxiliaryItemVo);
    }
    
    /**
     * @description: 删除核算项目
     * @param: [auxiliaryItemDto]
     * @return: com.njwd.support.Result<com.njwd.support.BatchResult> 
     * @author: xdy        
     * @create: 2019-11-21 15:49 
     */
    @RequestMapping("deleteAuxiliaryItem")
    public Result<BatchResult> deleteAuxiliaryItem(@RequestBody AuxiliaryItemDto auxiliaryItemDto){
        FastUtils.checkParams(auxiliaryItemDto.getIdList());
        BatchResult batchResult = auxiliaryItemService.deleteAuxiliaryItem(auxiliaryItemDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.auxiliaryItem,
                LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, null));
        return ok(batchResult);
    }
    
    /**
     * @description: 修改核算项目
     * @param: [auxiliaryItemDto]
     * @return: com.njwd.support.Result<com.njwd.entity.platform.vo.AuxiliaryItemVo> 
     * @author: xdy        
     * @create: 2019-11-21 15:49 
     */
    @RequestMapping("updateAuxiliaryItem")
    public Result<AuxiliaryItemVo> updateAuxiliaryItem(@RequestBody AuxiliaryItemDto auxiliaryItemDto){
        FastUtils.checkParams(auxiliaryItemDto.getId(),auxiliaryItemDto.getName(),auxiliaryItemDto.getSourceId());
        AuxiliaryItemVo auxiliaryItemVo = auxiliaryItemService.updateAuxiliaryItem(auxiliaryItemDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.auxiliaryItem,
                LogConstant.operation.update, LogConstant.operation.update_type, null));
        return ok(auxiliaryItemVo);
    }
    
    /**
     * @description: 审核核算项目
     * @param: [auxiliaryItemDto]
     * @return: com.njwd.support.Result<com.njwd.support.BatchResult> 
     * @author: xdy        
     * @create: 2019-11-21 15:49 
     */
    @RequestMapping("approvedAuxiliaryItem")
    public Result<BatchResult> approveAuxiliaryItem(@RequestBody AuxiliaryItemDto auxiliaryItemDto){
        FastUtils.checkParams(auxiliaryItemDto.getIdList());
        BatchResult batchResult = auxiliaryItemService.approveAuxiliaryItem(auxiliaryItemDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.auxiliaryItem,
                LogConstant.operation.approve, LogConstant.operation.approve_type, null));
        return ok(batchResult);
    }
    
    /**
     * @description: 反审核核算项目
     * @param: [auxiliaryItemDto]
     * @return: com.njwd.support.Result<com.njwd.support.BatchResult> 
     * @author: xdy        
     * @create: 2019-11-21 15:50 
     */
    @RequestMapping("reversalApproveAuxiliaryItem")
    public Result<BatchResult> reversalApproveAuxiliaryItem(@RequestBody AuxiliaryItemDto auxiliaryItemDto){
        FastUtils.checkParams(auxiliaryItemDto.getIdList());
        BatchResult batchResult = auxiliaryItemService.reversalApproveAuxiliaryItem(auxiliaryItemDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.auxiliaryItem,
                LogConstant.operation.reversalApprove, LogConstant.operation.reversalApprove_type, null));
        return ok(batchResult);
    }
    
    /**
     * @description: 发布核算项目
     * @param: [auxiliaryItemDto]
     * @return: com.njwd.support.Result<com.njwd.support.BatchResult> 
     * @author: xdy        
     * @create: 2019-11-21 15:50 
     */
    @RequestMapping("releaseAuxiliaryItem")
    public Result<BatchResult> releaseAuxiliaryItem(@RequestBody AuxiliaryItemDto auxiliaryItemDto){
        FastUtils.checkParams(auxiliaryItemDto.getIdList());
        BatchResult batchResult = auxiliaryItemService.releaseAuxiliaryItem(auxiliaryItemDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.auxiliaryItem,
                LogConstant.operation.release, LogConstant.operation.release_type, null));
        return ok(batchResult);
    }
    
    /**
     * @description: 导出核算项目
     * @param: [auxiliaryItemDto, response]
     * @return: void 
     * @author: xdy        
     * @create: 2019-11-21 15:50 
     */
    @RequestMapping("exportExcel")
    public void exportExcel(@RequestBody AuxiliaryItemDto auxiliaryItemDto, HttpServletResponse response) {
        auxiliaryItemService.exportExcel(auxiliaryItemDto,response);
    }
    /**
     * @Description 根据ID查询辅助核算
     * @Author liuxiang
     * @Date:15:07 2019/7/2
     * @Param [auxiliaryItemVo]
     * @return java.lang.String
     **/
    @PostMapping("findAuxiliaryItemById")
    public Result<AuxiliaryItemVo> findAuxiliaryItemById(@RequestBody AuxiliaryItemDto auxiliaryDto){
        return ok(auxiliaryItemService.findAuxiliaryItemById(auxiliaryDto));
    }



    /**
     * @Description 查询辅助核算分页
     * @Author liuxiang
     * @Date:15:07 2019/7/2
     * @Param [auxiliaryDto]
     * @return java.lang.String
     **/
    @PostMapping("findAuxiliaryItemPage")
    public Result<Page<AuxiliaryItemVo>> findAuxiliaryItemPage(@RequestBody AuxiliaryItemDto auxiliaryDto){
        return ok(auxiliaryItemService.findAuxiliaryItemPage(auxiliaryDto));
    }

    /**
     * @Description 查询辅助核算列表
     * @Author liuxiang
     * @Date:15:07 2019/7/2
     * @Param [auxiliaryDto]
     * @return java.lang.String
     **/
    @PostMapping("findAuxiliaryItemList")
    public Result<List<AuxiliaryItemVo>> findAuxiliaryItemList(@RequestBody AuxiliaryItemDto auxiliaryDto){
        return ok(auxiliaryItemService.findAuxiliaryItemList(auxiliaryDto));
    }

    /**
     * @Description 根据名称字符串查询辅助核算
     * @Author liuxiang
     * @Date:15:07 2019/7/2
     * @Param [auxiliaryVo]
     * @return java.lang.String
     **/
    @PostMapping("findAuxiliaryItemListByNames")
    public Result<List<AuxiliaryItemVo>> findAuxiliaryItemListByNames(@RequestBody AuxiliaryItemDto auxiliaryDto){
        return ok(auxiliaryItemService.findAuxiliaryItemListByNames(auxiliaryDto));
    }

    /**
     * @Description 查询所有未删除的辅助核算类型
     * @Author wuweiming
     * @Param []
     * @return Result<List<AuxiliaryItemVo>>
     **/
    @PostMapping("findAllAuxiliaryItem")
    public Result<List<AuxiliaryItemVo>> findAllAuxiliaryItem(){
        return ok(auxiliaryItemService.findAllAuxiliaryItem());
    }

    /**
     * @description: 获取值来源
     * @param: [sourceOfValueDto]
     * @return: com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.SourceOfValue>> 
     * @author: xdy        
     * @create: 2019-11-14 14:36 
     */
    @RequestMapping("findSourceOfValuePage")
    public Result<Page<SourceOfValue>> findSourceOfValuePage(@RequestBody SourceOfValueDto sourceOfValueDto){
        return ok(auxiliaryItemService.findSourceOfValuePage(sourceOfValueDto));
    }

    /**
     * 查询科目表配置的辅助核算
     *
     * @param subjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/12/3
     */
    @RequestMapping("findBySubjectId")
    public Result<List<AuxiliaryItemVo>> findBySubjectId(@RequestBody SubjectDto subjectDto){
        return ok(auxiliaryItemService.findBySubjectId(subjectDto));
    }

}
