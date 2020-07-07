package com.njwd.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.mapper.CostItemMapper;
import com.njwd.basedata.service.CostItemService;
import com.njwd.basedata.service.SequenceService;
import com.njwd.common.Constant;
import com.njwd.common.ExcelDataConstant;
import com.njwd.entity.basedata.CostItemCompany;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.CostItem;
import com.njwd.entity.platform.dto.CostItemDto;
import com.njwd.entity.platform.vo.CostItemVo;

import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.service.FileService;
import com.njwd.support.BatchResult;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
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
        SysUserVo sysUserVo = UserUtils.getUserVo();
        checkCostItem(costItemDto);
        costItemDto.setCode(sequenceService.getCode(Constant.BaseCodeRule.COST_ITEM,3,sysUserVo.getRootEnterpriseId(),Constant.BaseCodeRule.ENTERPRISE));
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
        LambdaQueryWrapper<CostItem> queryWrapper = Wrappers.<CostItem>lambdaQuery().eq(CostItem::getName,costItemDto.getName())
                .eq(CostItem::getIsDel,Constant.Is.NO)
                .eq(CostItem::getRootEnterpriseId,UserUtils.getUserVo().getRootEnterpriseId());
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
        //todo 引用关系
        /*List<CostItem> costItemList = costItemMapper.selectBatchIds(costItemDto.getIdList());
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
        }*/
        //批量操作
        costItemMapper.deleteBatchIds(result.getSuccessList());
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
        SysUserVo sysUserVo = UserUtils.getUserVo();
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
                    ,new ExcelColumn("name","名称")
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

    /**
     * @description: 禁用费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.BatchResult
     * @author: xdy
     * @create: 2019-11-29 09:48
     */
    @Override
    public BatchResult forbiddenCostItem(CostItemDto costItemDto) {
        //初始化返回对象
        BatchResult result = new BatchResult();
        //todo 引用关系
        /*List<CostItem> costItemList = costItemMapper.selectBatchIds(costItemDto.getIdList());
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
        }*/
        //批量操作
        costItemDto.setIsEnable(Constant.Is.NO);
        costItemMapper.updateBatch(costItemDto);
        return result;
    }

    /**
     * @description: 反禁用费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.BatchResult
     * @author: xdy
     * @create: 2019-11-29 09:53
     */
    @Override
    public BatchResult antiForbiddenCostItem(CostItemDto costItemDto) {
        BatchResult result = new BatchResult();
        costItemDto.setIsEnable(Constant.Is.NO);
        costItemMapper.updateBatch(costItemDto);
        return result;
    }

    /**
     * @description: 分配费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.BatchResult
     * @author: xdy
     * @create: 2019-11-29 10:01
     */
    @Override
    public BatchResult allotCostItem(CostItemDto costItemDto) {
        SysUserVo sysUserVo = new SysUserVo();
        costItemMapper.deleteCostItemCompany(costItemDto);
        for(CostItemCompany costItemCompany:costItemDto.getCostItemCompanyList()){
            costItemCompany.setCostItemId(costItemDto.getId());
            costItemCompany.setCreatorId(sysUserVo.getUserId());
            costItemCompany.setCreatorName(sysUserVo.getName());
        }
        costItemMapper.addCostItemCompany(costItemDto);
        BatchResult batchResult = new BatchResult();
        return batchResult;
    }

    /**
     * @description: 取消分配费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.BatchResult
     * @author: xdy
     * @create: 2019-11-29 10:03
     */
    @Override
    public BatchResult cancelAllotCostItem(CostItemDto costItemDto) {
        costItemMapper.deleteCostItemCompany(costItemDto);
        BatchResult batchResult = new BatchResult();
        return batchResult;
        
    }

    /**
     * @description: 升级费用项目
     * @param: [costItemDto]
     * @return: com.njwd.entity.platform.vo.CostItemVo
     * @author: xdy
     * @create: 2019-11-29 10:07
     */
    @Override
    public CostItemVo upgradeAllotCostItem(CostItemDto costItemDto) {
        SysUserVo sysUserVo = UserUtils.getUserVo();
        costItemDto.setUpdatorId(sysUserVo.getUserId());
        costItemDto.setUpdatorName(sysUserVo.getName());
        costItemDto.setDataType(Constant.dataType.DISTRIBUTION);
        costItemMapper.updateById(costItemDto);
        CostItemVo costItemVo = new CostItemVo();
        costItemVo.setId(costItemDto.getId());
        return costItemVo;
    }

    /**
     * @description: 引入费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.BatchResult
     * @author: xdy
     * @create: 2019-11-29 10:14
     */
    @Override
    public BatchResult bringInCostItem(CostItemDto costItemDto) {

        return null;
    }


}

