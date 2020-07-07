package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.ExcelDataConstant;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.platform.CostItem;
import com.njwd.entity.platform.dto.CostItemDto;
import com.njwd.entity.platform.vo.CostItemVo;
import com.njwd.entity.platform.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.platform.mapper.BasePlatformMapper;
import com.njwd.platform.mapper.CostItemMapper;
import com.njwd.platform.service.CostItemService;
import com.njwd.platform.service.MessageService;
import com.njwd.platform.service.SequenceService;
import com.njwd.platform.utils.UserUtil;
import com.njwd.service.FileService;
import com.njwd.support.BatchResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: 费用项目
 * @author: xdy
 * @create: 2019-11-19 16:39
 */
@Service
public class CostItemServiceImpl implements CostItemService {

    @Resource
    private CostItemMapper costItemMapper;

    @Resource
    private FileService fileService;

    @Resource
    private BasePlatformMapper basePlatformMapper;

    @Resource
    private MessageService messageService;

    @Resource
    private SequenceService sequenceService;

    /**
     * @description: 新增费用项目
     * @param: [costItemDto]
     * @return: com.njwd.entity.platform.vo.CostItemVo
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    @Override
    public CostItemVo addCostItem(CostItemDto costItemDto) {
        SysUserVo sysUserVo = UserUtil.getUserVo();
        checkCostItem(costItemDto);
        costItemDto.setCode(sequenceService.getCode(PlatformConstant.PlatformCodeRule.COST_ITEM,3));
        costItemDto.setCreatorId(sysUserVo.getUserId());
        costItemDto.setCreatorName(sysUserVo.getName());
        costItemMapper.insert(costItemDto);
        CostItemVo costItemVo = new CostItemVo();
        costItemVo.setId(costItemDto.getId());
        return costItemVo;
    }
    
    /**
     * @description: 
     * @param: []
     * @return: void 
     * @author: xdy        
     * @create: 2019-11-22 09:15 
     */
    private void checkCostItem(CostItemDto costItemDto){
        LambdaQueryWrapper<CostItem> queryWrapper = Wrappers.<CostItem>lambdaQuery().eq(CostItem::getName,costItemDto.getName()).eq(CostItem::getIsDel,Constant.Is.NO);
        if(costItemDto.getId()!=null)
            queryWrapper.ne(CostItem::getId,costItemDto.getId());
        Integer count = costItemMapper.selectCount(queryWrapper);
        if(count!=null&&count>0)
            throw new ServiceException(ResultCode.NAME_EXIST);
    }

    /**
     * @description: 删除费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.BatchResult
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    @Override
    public BatchResult deleteCostItem(CostItemDto costItemDto) {
        //初始化返回对象
        BatchResult result = new BatchResult();
        List<CostItem> costItemList = costItemMapper.selectBatchIds(costItemDto.getIdList());
        List<Long> successIdList = new ArrayList<>();
        for(CostItem costItem : costItemList){
            ReferenceDescription rd = new ReferenceDescription();
            // 1.校验当前数据是否审核,审核状态0待审核 1已审核 如果已审核，则提示报错‘该数据已审核，无法删除，请先反审核’
            if(costItem.getIsApproved() == Constant.Is.YES){
                rd.setBusinessId(costItem.getId());
                rd.setReferenceDescription("该数据已审核,无法删除,请先反审核!");
                result.getFailList().add(rd);
                continue;
            }
            //把校验成功添加进集合，用于修改状态
            successIdList.add(costItem.getId());
        }
        result.setSuccessList(successIdList);
        //防止没有数据
        if (result.getSuccessList().isEmpty()) {
            return result;
        }
        //批量操作
        basePlatformMapper.batchProcess(result.getSuccessList(), PlatformConstant.OperateType.DELETE, UserUtil.getUserVo(), PlatformConstant.TableName.COST_ITEM);
        return result;
    }

    /**
     * @description: 修改费用项目
     * @param: [costItemDto]
     * @return: com.njwd.entity.platform.vo.CostItemVo
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    @Override
    public CostItemVo updateCostItem(CostItemDto costItemDto) {
        checkCostItem(costItemDto);
        SysUserVo sysUserVo = UserUtil.getUserVo();
        costItemDto.setUpdatorId(sysUserVo.getUserId());
        costItemDto.setUpdatorName(sysUserVo.getName());
        costItemMapper.updateById(costItemDto);
        CostItemVo costItemVo = new CostItemVo();
        costItemVo.setId(costItemDto.getId());
        return costItemVo;
    }

    /**
     * @description: 根据主键查询费用项目
     * @param: [costItemDto]
     * @return: com.njwd.entity.platform.vo.CostItemVo
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    @Override
    public CostItemVo findCostItemById(CostItemDto costItemDto) {
        return costItemMapper.findCostItemById(costItemDto);
    }

    /**
     * @description: 费用项目分页
     * @param: [costItemDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.CostItemVo>
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    @Override
    public Page<CostItemVo> findCostItemPage(CostItemDto costItemDto) {
        Page<CostItemVo> page = costItemDto.getPage();
        return costItemMapper.findCostItemPage(page,costItemDto);
    }

    /**
     * @description: 审核费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.BatchResult
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    @Override
    public BatchResult approveCostItem(CostItemDto costItemDto) {
        //初始化返回对象
        BatchResult result = new BatchResult();
        List<CostItem> costItemList = costItemMapper.selectBatchIds(costItemDto.getIdList());
        List<Long> successIdList = new ArrayList<>();
        for(CostItem costItem : costItemList){
            ReferenceDescription rd = new ReferenceDescription();
            // 1.校验当前数据是否审核,审核状态0待审核 1已审核 如果已审核，则提示报错‘该数据已审核，无法删除，请先反审核’
            if(costItem.getIsApproved() == Constant.Is.YES){
                rd.setBusinessId(costItem.getId());
                rd.setReferenceDescription("审核失败！数据已审核，无需重复审核！");
                result.getFailList().add(rd);
                continue;
            }
            //把校验成功添加进集合，用于修改状态
            successIdList.add(costItem.getId());
        }
        result.setSuccessList(successIdList);
        //防止没有数据
        if (result.getSuccessList().isEmpty()) {
            return result;
        }
        //批量操作
        basePlatformMapper.batchProcess(result.getSuccessList(), PlatformConstant.OperateType.APPROVED, UserUtil.getUserVo(), PlatformConstant.TableName.COST_ITEM);
        return result;
    }

    /**
     * @description: 反审核费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.BatchResult
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    @Override
    public BatchResult reverseApproveCostItem(CostItemDto costItemDto) {
        //初始化返回对象
        BatchResult result = new BatchResult();
        List<CostItem> costItemList = costItemMapper.selectBatchIds(costItemDto.getIdList());
        List<Long> successIdList = new ArrayList<>();
        for(CostItem costItem : costItemList){
            ReferenceDescription rd = new ReferenceDescription();
            // 1.校验当前数据是否审核,审核状态0待审核 1已审核 如果已审核，则提示报错‘该数据已审核，无法删除，请先反审核’
            if(costItem.getIsReleased() == Constant.Is.YES){
                rd.setBusinessId(costItem.getId());
                rd.setReferenceDescription("反审核失败！数据已发布，无法反审核！");
                result.getFailList().add(rd);
                continue;
            }
            if(costItem.getIsApproved() == Constant.Is.NO){
                rd.setBusinessId(costItem.getId());
                rd.setReferenceDescription("反审核失败！数据未审核，无需反审核！");
                result.getFailList().add(rd);
                continue;
            }
            //把校验成功添加进集合，用于修改状态
            successIdList.add(costItem.getId());
        }
        result.setSuccessList(successIdList);
        //防止没有数据
        if (result.getSuccessList().isEmpty()) {
            return result;
        }
        //批量操作
        basePlatformMapper.batchProcess(result.getSuccessList(), PlatformConstant.OperateType.DISAPPROVED, UserUtil.getUserVo(), PlatformConstant.TableName.COST_ITEM);
        return result;
    }

    /**
     * @description: 发布费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.BatchResult
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    @Override
    public BatchResult releaseCostItem(CostItemDto costItemDto) {
        //初始化返回对象
        BatchResult result = new BatchResult();
        List<CostItem> costItemList = costItemMapper.selectBatchIds(costItemDto.getIdList());
        List<Long> successIdList = new ArrayList<>();
        for(CostItem costItem : costItemList){
            ReferenceDescription rd = new ReferenceDescription();
            // 1.校验当前数据是否审核,审核状态0待审核 1已审核 如果已审核，则提示报错‘该数据已审核，无法删除，请先反审核’
            if(costItem.getIsApproved() == Constant.Is.NO){
                rd.setBusinessId(costItem.getId());
                rd.setReferenceDescription("发布失败！只能发布已审核的数据！");
                result.getFailList().add(rd);
                continue;
            }
            if(costItem.getIsReleased() == Constant.Is.YES){
                rd.setBusinessId(costItem.getId());
                rd.setReferenceDescription("发布失败！数据已发布，无需重复发布！");
                result.getFailList().add(rd);
                continue;
            }
            //把校验成功添加进集合，用于修改状态
            successIdList.add(costItem.getId());
        }
        result.setSuccessList(successIdList);
        //防止没有数据
        if (result.getSuccessList().isEmpty()) {
            return result;
        }
        messageService.sendMessage(PlatformConstant.MessageType.SYSTEM_NOTICE,costItemDto.getMessageDto());
        //批量操作
        basePlatformMapper.batchProcess(result.getSuccessList(), PlatformConstant.OperateType.RELEASED, UserUtil.getUserVo(), PlatformConstant.TableName.COST_ITEM);
        return result;
    }

    /**
     * @description: 导出费用项目
     * @param: [costItemDto, response]
     * @return: void
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    @Override
    public void exportExcel(CostItemDto costItemDto, HttpServletResponse response) {
        Page<CostItemVo> page = costItemDto.getPage();
        fileService.resetPage(page);
        page=costItemMapper.findCostItemPage(page,costItemDto);
        fileService.exportExcel(response,page.getRecords()
                    ,new ExcelColumn("code","编码")
                    ,new ExcelColumn("name","名称")
                    ,new ExcelColumn("desc","描述")
                    ,new ExcelColumn("isApproved","审核状态", ExcelDataConstant.SYSTEM_DATA_IS_APPROVED)
                    ,new ExcelColumn("isApproved","发布状态",ExcelDataConstant.SYSTEM_DATA_IS_RELEASED));
    }

    /**
     * @description: 费用项目列表
     * @param: [costItemDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.CostItemVo>
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    @Override
    public List<CostItemVo> findCostItemList(CostItemDto costItemDto) {
        return costItemMapper.findCostItemList(costItemDto);
    }



}

