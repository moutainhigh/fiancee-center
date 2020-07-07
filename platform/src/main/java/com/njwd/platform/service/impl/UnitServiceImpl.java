package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.ExcelDataConstant;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.basedata.ReferenceContext;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.platform.Unit;
import com.njwd.entity.platform.dto.UnitDto;
import com.njwd.entity.platform.vo.SysUserVo;
import com.njwd.entity.platform.vo.UnitVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.platform.mapper.BasePlatformMapper;
import com.njwd.platform.mapper.UnitMapper;
import com.njwd.platform.service.MessageService;
import com.njwd.platform.service.UnitService;
import com.njwd.platform.utils.UserUtil;
import com.njwd.service.FileService;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.BatchResult;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/15 10:34
 */
@Service
public class UnitServiceImpl implements UnitService {

    @Resource
    private UnitMapper unitMapper;

    @Resource
    private FileService fileService;

    @Resource
    private ReferenceRelationService referenceRelationService;

    @Resource
    private BasePlatformMapper basePlatformMapper;

    @Resource
    private MessageService messageService;

    /**
     * @description: 新增计量单位
     * @param: [unitDto]
     * @return: com.njwd.entity.platform.vo.UnitVo 
     * @author: xdy        
     * @create: 2019-11-21 15:17 
     */
    @Override
    public UnitVo addUnit(UnitDto unitDto) {
        SysUserVo sysUserVo = UserUtil.getUserVo();
        checkUnit(unitDto);
        unitDto.setCreatorId(sysUserVo.getUserId());
        unitDto.setCreatorName(sysUserVo.getName());
        unitMapper.insert(unitDto);
        UnitVo unitVo = new UnitVo();
        unitVo.setId(unitDto.getId());
        return unitVo;
    }
    
    /**
     * @description: 检测计量单位
     * @param: [unitDto]
     * @return: void 
     * @author: xdy        
     * @create: 2019-11-21 15:17 
     */
    private void checkUnit(UnitDto unitDto){
        LambdaQueryWrapper<Unit> queryWrapper = Wrappers.<Unit>lambdaQuery().eq(Unit::getCode,unitDto.getCode()).eq(Unit::getIsDel,Constant.Is.NO);
        if(unitDto.getId()!=null)
            queryWrapper.ne(Unit::getId,unitDto.getId());
        Integer count = unitMapper.selectCount(queryWrapper);
        if(count!=null&&count>0)
            throw new ServiceException(ResultCode.CODE_EXIST);
        queryWrapper = Wrappers.<Unit>lambdaQuery().eq(Unit::getName,unitDto.getName()).eq(Unit::getIsDel,Constant.Is.NO);
        if(unitDto.getId()!=null)
            queryWrapper.ne(Unit::getId,unitDto.getId());
        count = unitMapper.selectCount(queryWrapper);
        if(count!=null&&count>0)
            throw new ServiceException(ResultCode.NAME_EXIST);
    }
    
    /**
     * @description: 删除计量单位
     * @param: [unitDto]
     * @return: com.njwd.support.BatchResult 
     * @author: xdy        
     * @create: 2019-11-21 15:17 
     */
    @Override
    public BatchResult deleteUnit(UnitDto unitDto) {
        //初始化返回对象
        BatchResult result = new BatchResult();
        List<Unit> unitList = unitMapper.selectBatchIds(unitDto.getIdList());
        List<Long> successIdList = new ArrayList<>();
        for(Unit unit : unitList){
            ReferenceDescription rd = new ReferenceDescription();
            // 1.校验当前数据是否审核,审核状态0待审核 1已审核 如果已审核，则提示报错‘该数据已审核，无法删除，请先反审核’
            if(unit.getIsApproved() == Constant.Is.YES){
                rd.setBusinessId(unit.getId());
                rd.setReferenceDescription("该数据已审核,无法删除,请先反审核!");
                result.getFailList().add(rd);
                continue;
            }
            //把校验成功添加进集合，用于修改状态
            successIdList.add(unit.getId());
        }
        result.setSuccessList(successIdList);
        //防止没有数据
        if (result.getSuccessList().isEmpty()) {
            return result;
        }
        ReferenceContext referenceContext = referenceRelationService.isReference(PlatformConstant.Reference.PLAT_UNIT, result.getSuccessList());
        result.getFailList().addAll(referenceContext.getReferences());
        result.getSuccessList().removeAll(referenceContext.getReferences().stream().map(ReferenceDescription::getBusinessId).collect(Collectors.toList()));
        if (result.getSuccessList().size() == 0) {
            return result;
        }
        //批量操作
        basePlatformMapper.batchProcess(result.getSuccessList(), PlatformConstant.OperateType.DELETE, UserUtil.getUserVo(), PlatformConstant.TableName.UNIT);
        return result;
    }

    /**
     * @description: 修改计量单位
     * @param: [unitDto]
     * @return: com.njwd.entity.platform.vo.UnitVo
     * @author: xdy
     * @create: 2019-11-21 15:18
     */
    @Override
    public UnitVo updateUnit(UnitDto unitDto) {
        SysUserVo sysUserVo = UserUtil.getUserVo();
        checkUnit(unitDto);
        unitDto.setUpdatorId(sysUserVo.getUserId());
        unitDto.setUpdatorName(sysUserVo.getName());

        unitMapper.updateById(unitDto);
        UnitVo unitVo = new UnitVo();
        unitVo.setId(unitDto.getId());
        return unitVo;
    }

    /**
     * @description: 根据ID查询计量单位
     * @param: [unitDto]
     * @return: com.njwd.entity.platform.vo.UnitVo
     * @author: xdy
     * @create: 2019-11-21 15:18
     */
    @Override
    public UnitVo findUnitById(UnitDto unitDto) {
        return unitMapper.findUnitById(unitDto);
    }

    /**
     * @description: 计量单位分页
     * @param: [unitDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.UnitVo> 
     * @author: xdy        
     * @create: 2019-11-21 15:18 
     */
    @Override
    public Page<UnitVo> findUnitPage(UnitDto unitDto) {
        Page<UnitVo> page = unitDto.getPage();
        return unitMapper.findUnitPage(page,unitDto);
    }
    
    /**
     * @description: 审核计量单位
     * @param: [unitDto]
     * @return: com.njwd.support.BatchResult 
     * @author: xdy        
     * @create: 2019-11-21 15:18 
     */
    @Override
    public BatchResult approveUnit(UnitDto unitDto) {
        //初始化返回对象
        BatchResult result = new BatchResult();
        List<Unit> unitList = unitMapper.selectBatchIds(unitDto.getIdList());
        List<Long> successIdList = new ArrayList<>();
        for(Unit unit : unitList){
            ReferenceDescription rd = new ReferenceDescription();
            // 1.校验当前数据是否审核,审核状态0待审核 1已审核 如果已审核，则提示报错‘该数据已审核，无法删除，请先反审核’
            if(unit.getIsApproved() == Constant.Is.YES){
                rd.setBusinessId(unit.getId());
                rd.setReferenceDescription("审核失败！数据已审核，无需重复审核！");
                result.getFailList().add(rd);
                continue;
            }
            //把校验成功添加进集合，用于修改状态
            successIdList.add(unit.getId());
        }
        result.setSuccessList(successIdList);
        //防止没有数据
        if (result.getSuccessList().isEmpty()) {
            return result;
        }
        //批量操作
        basePlatformMapper.batchProcess(result.getSuccessList(), PlatformConstant.OperateType.APPROVED, UserUtil.getUserVo(), PlatformConstant.TableName.UNIT);
        return result;
    }

    /**
     * @description: 反审核计量单位
     * @param: [unitDto]
     * @return: com.njwd.support.BatchResult 
     * @author: xdy        
     * @create: 2019-11-21 15:19 
     */
    @Override
    public BatchResult reverseApproveUnit(UnitDto unitDto) {
        //初始化返回对象
        BatchResult result = new BatchResult();
        List<Unit> unitList = unitMapper.selectBatchIds(unitDto.getIdList());
        List<Long> successIdList = new ArrayList<>();
        for(Unit unit : unitList){
            ReferenceDescription rd = new ReferenceDescription();
            // 1.校验当前数据是否审核,审核状态0待审核 1已审核 如果已审核，则提示报错‘该数据已审核，无法删除，请先反审核’
            if(unit.getIsReleased() == Constant.Is.YES){
                rd.setBusinessId(unit.getId());
                rd.setReferenceDescription("反审核失败！数据已发布，无法反审核！");
                result.getFailList().add(rd);
                continue;
            }
            if(unit.getIsApproved() == Constant.Is.NO){
                rd.setBusinessId(unit.getId());
                rd.setReferenceDescription("反审核失败！数据未审核，无需反审核！");
                result.getFailList().add(rd);
                continue;
            }
            //把校验成功添加进集合，用于修改状态
            successIdList.add(unit.getId());
        }
        result.setSuccessList(successIdList);
        //防止没有数据
        if (result.getSuccessList().isEmpty()) {
            return result;
        }
        ReferenceContext referenceContext = referenceRelationService.isReference(PlatformConstant.Reference.PLAT_UNIT, result.getSuccessList());
        result.getFailList().addAll(referenceContext.getReferences());
        result.getSuccessList().removeAll(referenceContext.getReferences().stream().map(ReferenceDescription::getBusinessId).collect(Collectors.toList()));
        if (result.getSuccessList().size() == 0) {
            return result;
        }
        //批量操作
        basePlatformMapper.batchProcess(result.getSuccessList(), PlatformConstant.OperateType.DISAPPROVED, UserUtil.getUserVo(), PlatformConstant.TableName.UNIT);
        return result;
    }
    
    /**
     * @description: 发布计量单位
     * @param: [unitDto]
     * @return: com.njwd.support.BatchResult 
     * @author: xdy        
     * @create: 2019-11-21 15:19 
     */
    @Override
    public BatchResult releaseUnit(UnitDto unitDto) {
        //初始化返回对象
        BatchResult result = new BatchResult();
        List<Unit> unitList = unitMapper.selectBatchIds(unitDto.getIdList());
        List<Long> successIdList = new ArrayList<>();
        for(Unit unit : unitList){
            ReferenceDescription rd = new ReferenceDescription();
            // 1.校验当前数据是否审核,审核状态0待审核 1已审核 如果已审核，则提示报错‘该数据已审核，无法删除，请先反审核’
            if(unit.getIsApproved() == Constant.Is.NO){
                rd.setBusinessId(unit.getId());
                rd.setReferenceDescription("发布失败！只能发布已审核的数据！");
                result.getFailList().add(rd);
                continue;
            }
            if(unit.getIsReleased() == Constant.Is.YES){
                rd.setBusinessId(unit.getId());
                rd.setReferenceDescription("发布失败！数据已发布，无需重复发布！");
                result.getFailList().add(rd);
                continue;
            }
            //把校验成功添加进集合，用于修改状态
            successIdList.add(unit.getId());
        }
        result.setSuccessList(successIdList);
        //防止没有数据
        if (result.getSuccessList().isEmpty()) {
            return result;
        }
        messageService.sendMessage(PlatformConstant.MessageType.SYSTEM_NOTICE,unitDto.getMessageDto());
        basePlatformMapper.batchProcess(result.getSuccessList(), PlatformConstant.OperateType.RELEASED, UserUtil.getUserVo(), PlatformConstant.TableName.UNIT);
        return result;
    }
    
    /**
     * @description: 导出计量单位
     * @param: [unitDto, response]
     * @return: void 
     * @author: xdy        
     * @create: 2019-11-21 15:19 
     */
    @Override
    public void exportExcel(UnitDto unitDto, HttpServletResponse response) {
        Page<UnitVo> page = unitDto.getPage();
        fileService.resetPage(page);
        page=unitMapper.findUnitPage(page,unitDto);
        fileService.exportExcel(response,page.getRecords()
                ,new ExcelColumn("code","编码")
                ,new ExcelColumn("name","名称")
                ,new ExcelColumn("precision","单位精度")
                ,new ExcelColumn("roundingType","舍入规则",ExcelDataConstant.SYSTEM_DATA_ROUNDING_TYPE)
                ,new ExcelColumn("isBase","基准单位", ExcelDataConstant.SYSTEM_DATA_IS_BASE)
                ,new ExcelColumn("isApproved","审核状态", ExcelDataConstant.SYSTEM_DATA_IS_APPROVED)
                ,new ExcelColumn("isReleased","发布状态", ExcelDataConstant.SYSTEM_DATA_IS_RELEASED));

    }

    /**
     * @description: 计量单位列表
     * @param: [unitDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.UnitVo>
     * @author: xdy
     * @create: 2019-11-21 15:19
     */
    @Override
    public List<UnitVo> findUnitList(UnitDto unitDto) {
        return unitMapper.findUnitList(unitDto);
    }
}
