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
import com.njwd.entity.platform.AuxiliaryItem;
import com.njwd.entity.platform.SourceOfValue;
import com.njwd.entity.platform.dto.AuxiliaryItemDto;
import com.njwd.entity.platform.dto.SourceOfValueDto;
import com.njwd.entity.platform.dto.SubjectDto;
import com.njwd.entity.platform.vo.AuxiliaryItemVo;
import com.njwd.entity.platform.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.fileexcel.export.DataGet;
import com.njwd.platform.mapper.AuxiliaryItemMapper;
import com.njwd.platform.mapper.BasePlatformMapper;
import com.njwd.platform.service.AuxiliaryItemService;
import com.njwd.platform.service.MessageService;
import com.njwd.platform.service.SequenceService;
import com.njwd.platform.utils.UserUtil;
import com.njwd.service.FileService;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.BatchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author liuxiang
 * @Description 辅助核算
 * @Date:16:27 2019/6/25
 **/
@Service
public class AuxiliaryItemServiceImpl implements AuxiliaryItemService {

    @Autowired
    private AuxiliaryItemMapper auxiliaryMapper;

    @Resource
    private SequenceService sequenceService;

    @Resource
    private FileService fileService;

    @Resource
    private ReferenceRelationService referenceRelationService;

    @Resource
    private BasePlatformMapper basePlatformMapper;

    @Resource
    private MessageService messageService;


    /**
     * @Description 根据ID查询辅助核算
     * @Author liuxiang
     * @Date:15:48 2019/7/2
     * @Param [auxiliaryVo]
     * @return com.njwd.platform.entity.vo.AuxiliaryItemVo
     **/
    @Override
    //@Cacheable(value = "auxiliaryItemById", key = "#auxiliaryDto.id+''",unless="#result == null")
    public AuxiliaryItemVo findAuxiliaryItemById(AuxiliaryItemDto auxiliaryDto) {
        return auxiliaryMapper.findAuxiliaryById(auxiliaryDto);
    }

    /**
     * @Description 查询辅助核算分页
     * @Author liuxiang
     * @Date:15:48 2019/7/2
     * @Param [auxiliaryDto]
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.platform.entity.vo.AuxiliaryItemVo>
     **/
    @Override
    //@Cacheable(value = "auxiliaryItemPage", key = "#auxiliaryDto.codeOrName+'-'+#auxiliaryDto.page.current+'-'+#auxiliaryDto.page.size")
    public Page<AuxiliaryItemVo> findAuxiliaryItemPage(AuxiliaryItemDto auxiliaryDto) {
        Page<AuxiliaryItemVo> page = auxiliaryDto.getPage();
        page=auxiliaryMapper.findAuxiliaryItemPage(page,auxiliaryDto);
        return page;
    }

    /**
     * @Description 查询辅助核算列表
     * @Author liuxiang
     * @Date:15:48 2019/7/2
     * @Param [auxiliaryDto]
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.platform.entity.vo.AuxiliaryItemVo>
     **/
    @Override
    //@Cacheable(value = "findAuxiliaryItemList", key = "#auxiliaryDto.codeOrName+''")
    public List<AuxiliaryItemVo> findAuxiliaryItemList(AuxiliaryItemDto auxiliaryDto) {
        Page<AuxiliaryItemVo> page = auxiliaryDto.getPage();
        page.setCurrent(1);
        page.setSearchCount(false);
        page.setSize(DataGet.MAX_PAGE_SIZE);
        page=auxiliaryMapper.findAuxiliaryItemPage(page,auxiliaryDto);
        return page.getRecords();
    }

    /**
     * @Description 根据名称字符串查询辅助核算
     * @Author liuxiang
     * @Date:15:49 2019/7/2
     * @Param [auxiliaryVo]
     * @return java.util.List<com.njwd.platform.entity.vo.AuxiliaryItemVo>
     **/
    @Override
    //@Cacheable(value = "auxiliaryItemListByNames", key = "#auxiliaryDto.names+''")
    public List<AuxiliaryItemVo> findAuxiliaryItemListByNames(AuxiliaryItemDto auxiliaryDto) {
        return auxiliaryMapper.findAuxiliaryListByNames(auxiliaryDto);
    }

    /**
     * @Description 查询所有未删除的辅助核算类型
     * @Author wuweiming
     * @Param []
     * @return List<AuxiliaryItemVo>
     **/
    @Override
    public List<AuxiliaryItemVo> findAllAuxiliaryItem() {
        return auxiliaryMapper.findAllAuxiliaryItem();
    }
    
    /**
     * @description: 查询值来源
     * @param: [sourceOfValueDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.SourceOfValue> 
     * @author: xdy        
     * @create: 2019-11-14 14:33 
     */
    @Override
    public Page<SourceOfValue> findSourceOfValuePage(SourceOfValueDto sourceOfValueDto) {
        Page<SourceOfValue> page = sourceOfValueDto.getPage();
        return auxiliaryMapper.findSourceOfValuePage(page,sourceOfValueDto);
    }
    
    /**
     * @description: 新增核算项目
     * @param: [auxiliaryItemDto]
     * @return: com.njwd.entity.platform.vo.AuxiliaryItemVo 
     * @author: xdy        
     * @create: 2019-11-21 15:51 
     */
    @Override
    public AuxiliaryItemVo addAuxiliaryItem(AuxiliaryItemDto auxiliaryItemDto) {
        checkAuxiliaryItem(auxiliaryItemDto);
        SysUserVo userVo = UserUtil.getUserVo();
        auxiliaryItemDto.setCode(sequenceService.getCode(PlatformConstant.PlatformCodeRule.ACCOUNTING_ITEMS,3));
        auxiliaryItemDto.setCreatorId(userVo.getUserId());
        auxiliaryItemDto.setCreatorName(userVo.getName());
        SourceOfValue sourceOfValue = auxiliaryMapper.findSourceOfValuePageById(auxiliaryItemDto.getSourceId());
        auxiliaryItemDto.setSourceModel(sourceOfValue.getModel());
        auxiliaryItemDto.setSourceName(sourceOfValue.getName());
        auxiliaryItemDto.setSourceTable(sourceOfValue.getSourceTable());
        auxiliaryMapper.insert(auxiliaryItemDto);
        AuxiliaryItemVo auxiliaryItemVo = new AuxiliaryItemVo();
        auxiliaryItemVo.setId(auxiliaryItemDto.getId());
        return auxiliaryItemVo;
    }

    private void checkAuxiliaryItem(AuxiliaryItemDto auxiliaryItemDto){
        LambdaQueryWrapper<AuxiliaryItem> queryWrapper = Wrappers.<AuxiliaryItem>lambdaQuery().eq(AuxiliaryItem::getName,auxiliaryItemDto.getName())
                .eq(AuxiliaryItem::getIsDel,Constant.Is.NO);
        if(auxiliaryItemDto.getId()!=null)
            queryWrapper.ne(AuxiliaryItem::getId,auxiliaryItemDto.getId());
        Integer count = auxiliaryMapper.selectCount(queryWrapper);
        if(count!=null&&count>0)
            throw new ServiceException(ResultCode.NAME_EXIST);

        //值来源是否重复
        queryWrapper = Wrappers.<AuxiliaryItem>lambdaQuery().eq(AuxiliaryItem::getSourceId,auxiliaryItemDto.getSourceId())
                .eq(AuxiliaryItem::getIsDel,Constant.Is.NO);
        if(auxiliaryItemDto.getId()!=null)
            queryWrapper.ne(AuxiliaryItem::getId,auxiliaryItemDto.getId());
        count = auxiliaryMapper.selectCount(queryWrapper);
        if(count!=null&&count>0)
            throw new ServiceException(ResultCode.SOURCE_EXIST);

    }
    
    /**
     * @description: 修改核算项目
     * @param: [auxiliaryItemDto]
     * @return: com.njwd.entity.platform.vo.AuxiliaryItemVo 
     * @author: xdy        
     * @create: 2019-11-21 15:51 
     */
    @Override
    public AuxiliaryItemVo updateAuxiliaryItem(AuxiliaryItemDto auxiliaryItemDto) {
        checkAuxiliaryItem(auxiliaryItemDto);
        SysUserVo userVo = UserUtil.getUserVo();
        auxiliaryItemDto.setUpdatorId(userVo.getUserId());
        auxiliaryItemDto.setUpdatorName(userVo.getName());
        SourceOfValue sourceOfValue = auxiliaryMapper.findSourceOfValuePageById(auxiliaryItemDto.getSourceId());
        auxiliaryItemDto.setSourceModel(sourceOfValue.getModel());
        auxiliaryItemDto.setSourceName(sourceOfValue.getName());
        auxiliaryItemDto.setSourceTable(sourceOfValue.getSourceTable());
        auxiliaryMapper.updateById(auxiliaryItemDto);
        AuxiliaryItemVo auxiliaryItemVo = new AuxiliaryItemVo();
        auxiliaryItemVo.setId(auxiliaryItemDto.getId());
        return auxiliaryItemVo;
    }

    /**
     * @description: 删除核算项目
     * @param: [auxiliaryItemDto]
     * @return: com.njwd.support.BatchResult
     * @author: xdy
     * @create: 2019-11-21 15:51
     */
    @Override
    public BatchResult deleteAuxiliaryItem(AuxiliaryItemDto auxiliaryItemDto) {
        //初始化返回对象
        BatchResult result = new BatchResult();
        List<AuxiliaryItem> auxiliaryItemList = auxiliaryMapper.selectBatchIds(auxiliaryItemDto.getIdList());
        List<Long> successIdList = new ArrayList<>();
        for(AuxiliaryItem auxiliaryItem : auxiliaryItemList){
            ReferenceDescription rd = new ReferenceDescription();
            // 1.校验当前数据是否审核,审核状态0待审核 1已审核 如果已审核，则提示报错‘该数据已审核，无法删除，请先反审核’
            if(auxiliaryItem.getIsApproved() == Constant.Is.YES){
                rd.setBusinessId(auxiliaryItem.getId());
                rd.setReferenceDescription("该数据已审核,无法删除,请先反审核!");
                result.getFailList().add(rd);
                continue;
            }
            //把校验成功添加进集合，用于修改状态
            successIdList.add(auxiliaryItem.getId());
        }
        result.setSuccessList(successIdList);
        //防止没有数据
        if (result.getSuccessList().isEmpty()) {
            return result;
        }
        ReferenceContext referenceContext = referenceRelationService.isReference(PlatformConstant.Reference.PLAT_AUXILIARY_ITEM, result.getSuccessList());
        result.getFailList().addAll(referenceContext.getReferences());
        result.getSuccessList().removeAll(referenceContext.getReferences().stream().map(ReferenceDescription::getBusinessId).collect(Collectors.toList()));
        if (result.getSuccessList().size() == 0) {
            return result;
        }
        //批量操作
        basePlatformMapper.batchProcess(result.getSuccessList(), PlatformConstant.OperateType.DELETE, UserUtil.getUserVo(), PlatformConstant.TableName.AUXILIARY_ITEM);
        return result;
    }
    
    /**
     * @description: 审核核算项目
     * @param: [auxiliaryItemDto]
     * @return: com.njwd.support.BatchResult 
     * @author: xdy        
     * @create: 2019-11-21 15:51 
     */
    @Override
    public BatchResult approveAuxiliaryItem(AuxiliaryItemDto auxiliaryItemDto) {
        //初始化返回对象
        BatchResult result = new BatchResult();
        List<AuxiliaryItem> auxiliaryItemList = auxiliaryMapper.selectBatchIds(auxiliaryItemDto.getIdList());
        List<Long> successIdList = new ArrayList<>();
        for(AuxiliaryItem auxiliaryItem : auxiliaryItemList){
            ReferenceDescription rd = new ReferenceDescription();
            // 1.校验当前数据是否审核,审核状态0待审核 1已审核 如果已审核，则提示报错‘该数据已审核，无法删除，请先反审核’
            if(auxiliaryItem.getIsApproved() == Constant.Is.YES){
                rd.setBusinessId(auxiliaryItem.getId());
                rd.setReferenceDescription("审核失败！数据已审核，无需重复审核！");
                result.getFailList().add(rd);
                continue;
            }
            //把校验成功添加进集合，用于修改状态
            successIdList.add(auxiliaryItem.getId());
        }
        result.setSuccessList(successIdList);
        //防止没有数据
        if (result.getSuccessList().isEmpty()) {
            return result;
        }
        //批量操作
        basePlatformMapper.batchProcess(result.getSuccessList(), PlatformConstant.OperateType.APPROVED, UserUtil.getUserVo(), PlatformConstant.TableName.AUXILIARY_ITEM);
        return result;
    }

    /**
     * @description: 反审核核算项目
     * @param: [auxiliaryItemDto]
     * @return: com.njwd.support.BatchResult 
     * @author: xdy        
     * @create: 2019-11-21 15:52 
     */
    @Override
    public BatchResult reversalApproveAuxiliaryItem(AuxiliaryItemDto auxiliaryItemDto) {
        //初始化返回对象
        BatchResult result = new BatchResult();
        List<AuxiliaryItem> auxiliaryItemList = auxiliaryMapper.selectBatchIds(auxiliaryItemDto.getIdList());
        List<Long> successIdList = new ArrayList<>();
        for(AuxiliaryItem auxiliaryItem : auxiliaryItemList){
            ReferenceDescription rd = new ReferenceDescription();
            // 1.校验当前数据是否审核,审核状态0待审核 1已审核 如果已审核，则提示报错‘该数据已审核，无法删除，请先反审核’
            if(auxiliaryItem.getIsReleased() == Constant.Is.YES){
                rd.setBusinessId(auxiliaryItem.getId());
                rd.setReferenceDescription("反审核失败！数据已发布，无法反审核！");
                result.getFailList().add(rd);
                continue;
            }
            if(auxiliaryItem.getIsApproved() == Constant.Is.NO){
                rd.setBusinessId(auxiliaryItem.getId());
                rd.setReferenceDescription("反审核失败！数据未审核，无需反审核！");
                result.getFailList().add(rd);
                continue;
            }
            //把校验成功添加进集合，用于修改状态
            successIdList.add(auxiliaryItem.getId());
        }
        result.setSuccessList(successIdList);
        //防止没有数据
        if (result.getSuccessList().isEmpty()) {
            return result;
        }
        ReferenceContext referenceContext = referenceRelationService.isReference(PlatformConstant.Reference.PLAT_AUXILIARY_ITEM, result.getSuccessList());
        result.getFailList().addAll(referenceContext.getReferences());
        result.getSuccessList().removeAll(referenceContext.getReferences().stream().map(ReferenceDescription::getBusinessId).collect(Collectors.toList()));
        if (result.getSuccessList().size() == 0) {
            return result;
        }
        //批量操作
        basePlatformMapper.batchProcess(result.getSuccessList(), PlatformConstant.OperateType.DISAPPROVED, UserUtil.getUserVo(), PlatformConstant.TableName.AUXILIARY_ITEM);
        return result;
    }
    
    /**
     * @description: 导出核算项目
     * @param: [auxiliaryItemDto, response]
     * @return: void 
     * @author: xdy        
     * @create: 2019-11-21 15:52 
     */
    @Override
    public void exportExcel(AuxiliaryItemDto auxiliaryItemDto, HttpServletResponse response) {
        Page<AuxiliaryItemVo> page = auxiliaryItemDto.getPage();
        fileService.resetPage(page);
        page=auxiliaryMapper.findAuxiliaryItemPage(page,auxiliaryItemDto);
        fileService.exportExcel(response,page.getRecords()
                ,new ExcelColumn("code","编码")
                ,new ExcelColumn("name","名称")
                ,new ExcelColumn("sourceName","值来源")
                ,new ExcelColumn("isApproved","审核状态", ExcelDataConstant.SYSTEM_DATA_IS_APPROVED)
                ,new ExcelColumn("isReleased","发布状态", ExcelDataConstant.SYSTEM_DATA_IS_RELEASED));
    }

    /**
     * @description: 发布核算项目
     * @param: [auxiliaryItemDto]
     * @return: com.njwd.support.BatchResult
     * @author: xdy
     * @create: 2019-11-21 15:52
     */
    @Override
    public BatchResult releaseAuxiliaryItem(AuxiliaryItemDto auxiliaryItemDto) {
        //初始化返回对象
        BatchResult result = new BatchResult();
        List<AuxiliaryItem> auxiliaryItemList = auxiliaryMapper.selectBatchIds(auxiliaryItemDto.getIdList());
        List<Long> successIdList = new ArrayList<>();
        for(AuxiliaryItem auxiliaryItem : auxiliaryItemList){
            ReferenceDescription rd = new ReferenceDescription();
            // 1.校验当前数据是否审核,审核状态0待审核 1已审核 如果已审核，则提示报错‘该数据已审核，无法删除，请先反审核’
            if(auxiliaryItem.getIsApproved().equals(Constant.Is.NO)){
                rd.setBusinessId(auxiliaryItem.getId());
                rd.setReferenceDescription("发布失败！只能发布已审核的数据！");
                result.getFailList().add(rd);
                continue;
            }
            if(auxiliaryItem.getIsReleased().equals(Constant.Is.YES)){
                rd.setBusinessId(auxiliaryItem.getId());
                rd.setReferenceDescription("发布失败！数据已发布，无需重复发布！");
                result.getFailList().add(rd);
                continue;
            }
            //把校验成功添加进集合，用于修改状态
            successIdList.add(auxiliaryItem.getId());
        }
        result.setSuccessList(successIdList);
        //防止没有数据
        if (result.getSuccessList().isEmpty()) {
            return result;
        }
        messageService.sendMessage(PlatformConstant.MessageType.SYSTEM_NOTICE,auxiliaryItemDto.getMessageDto());
        basePlatformMapper.batchProcess(result.getSuccessList(), PlatformConstant.OperateType.RELEASED, UserUtil.getUserVo(), PlatformConstant.TableName.AUXILIARY_ITEM);
        return result;
    }

    /**
     * 查询科目表配置的辅助核算
     *
     * @param subjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/12/3
     */
    @Override
    public List<AuxiliaryItemVo> findBySubjectId(SubjectDto subjectDto){
        return auxiliaryMapper.findBySubjectId(subjectDto.getId());
    }

}
