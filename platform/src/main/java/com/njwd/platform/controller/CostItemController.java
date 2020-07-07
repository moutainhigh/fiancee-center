package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.LogConstant;
import com.njwd.entity.platform.dto.CostItemDto;
import com.njwd.entity.platform.vo.CostItemVo;
import com.njwd.logger.SenderService;
import com.njwd.platform.service.CostItemService;
import com.njwd.platform.utils.UserUtil;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.HttpUtils;
import com.njwd.utils.UserUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.njwd.support.BaseController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @description: 费用项目
 * @author: xdy
 * @create: 2019-11-19 16:39
 */
@RestController
@RequestMapping("costItem")
public class CostItemController extends BaseController {

    @Resource
    private CostItemService costItemService;
    @Resource
    private SenderService senderService;

    /**
     * @description: 新增费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.Result<com.njwd.entity.platform.vo.CostItemVo>
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    @RequestMapping("addCostItem")
    public Result<CostItemVo> addCostItem(@RequestBody CostItemDto costItemDto){
        FastUtils.checkParams(costItemDto.getName());
        CostItemVo costItemVo = costItemService.addCostItem(costItemDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.costItem,
                LogConstant.operation.add, LogConstant.operation.add_type, null));
        return ok(costItemVo);
    }

    /**
     * @description: 删除费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.Result<com.njwd.support.BatchResult>
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    @RequestMapping("deleteCostItem")
    public Result<BatchResult> deleteCostItem(@RequestBody CostItemDto costItemDto){
        FastUtils.checkParams(costItemDto.getIdList());
        BatchResult batchResult = costItemService.deleteCostItem(costItemDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.costItem,
                LogConstant.operation.delete, LogConstant.operation.delete_type, null));
        return ok(batchResult);
    }

    /**
     * @description: 修改费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.Result<com.njwd.entity.platform.vo.CostItemVo>
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    @RequestMapping("updateCostItem")
    public Result<CostItemVo> updateCostItem(@RequestBody CostItemDto costItemDto){
        FastUtils.checkParams(costItemDto.getId(),costItemDto.getName());
        CostItemVo costItemVo = costItemService.updateCostItem(costItemDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.costItem,
                LogConstant.operation.update, LogConstant.operation.update_type, null));
        return ok(costItemVo);
    }

    /**
     * @description: 根据主键查询费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.Result<com.njwd.entity.platform.vo.CostItemVo>
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    @RequestMapping("findCostItemById")
    public Result<CostItemVo> findCostItemById(@RequestBody CostItemDto costItemDto){
        FastUtils.checkParams(costItemDto.getId());
        return ok(costItemService.findCostItemById(costItemDto));
    }

    /**
     * @description: 费用项目分页
     * @param: [costItemDto]
     * @return: com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.CostItemVo>>
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    @RequestMapping("findCostItemPage")
    public Result<Page<CostItemVo>> findCostItemPage(@RequestBody CostItemDto costItemDto){
        return ok(costItemService.findCostItemPage(costItemDto));
    }

    /**
     * @description: 审核费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.Result<com.njwd.support.BatchResult>
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    @RequestMapping("approveCostItem")
    public Result<BatchResult> approveCostItem(@RequestBody CostItemDto costItemDto){
        FastUtils.checkParams(costItemDto.getIdList());
        BatchResult batchResult = costItemService.approveCostItem(costItemDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.costItem,
                LogConstant.operation.approve, LogConstant.operation.approve_type, null));
        return ok(batchResult);
    }

    /**
     * @description: 反审核费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.Result<com.njwd.support.BatchResult>
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    @RequestMapping("reverseApproveCostItem")
    public Result<BatchResult> reverseApproveCostItem(@RequestBody CostItemDto costItemDto){
        FastUtils.checkParams(costItemDto.getIdList());
        BatchResult batchResult = costItemService.reverseApproveCostItem(costItemDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.costItem,
                LogConstant.operation.reversalApprove, LogConstant.operation.reversalApprove_type, null));
        return ok(batchResult);
    }

    /**
     * @description: 发布费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.Result<com.njwd.support.BatchResult>
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    @RequestMapping("releaseCostItem")
    public Result<BatchResult> releaseCostItem(@RequestBody CostItemDto costItemDto){
        FastUtils.checkParams(costItemDto.getIdList());
        BatchResult batchResult = costItemService.releaseCostItem(costItemDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys, LogConstant.menuName.costItem,
                LogConstant.operation.release, LogConstant.operation.release_type, null));
        return ok(batchResult);
    }

    /**
     * @description: 导出费用项目
     * @param: [costItemDto, response]
     * @return: void
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    @RequestMapping("exportExcel")
    public void exportExcel(@RequestBody CostItemDto costItemDto, HttpServletResponse response){
        costItemService.exportExcel(costItemDto,response);
    }

    /**
     * @description: 费用项目列表
     * @param: [costItemDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.platform.vo.CostItemVo>>
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    @RequestMapping("findCostItemList")
    public Result<List<CostItemVo>> findCostItemList(@RequestBody CostItemDto costItemDto){
        return ok(costItemService.findCostItemList(costItemDto));
    }

}


