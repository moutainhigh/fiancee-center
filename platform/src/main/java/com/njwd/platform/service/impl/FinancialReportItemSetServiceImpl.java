package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.ExcelDataConstant;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.ReferenceResult;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.platform.FinancialReport;
import com.njwd.entity.platform.FinancialReportItem;
import com.njwd.entity.platform.FinancialReportItemFormula;
import com.njwd.entity.platform.FinancialReportItemSet;
import com.njwd.entity.platform.dto.FinancialReportItemDto;
import com.njwd.entity.platform.dto.FinancialReportItemSetDto;
import com.njwd.entity.platform.vo.FinancialReportItemSetVo;
import com.njwd.entity.platform.vo.FinancialReportItemVo;
import com.njwd.entity.platform.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.platform.mapper.BasePlatformMapper;
import com.njwd.platform.mapper.FinancialReportItemMapper;
import com.njwd.platform.mapper.FinancialReportItemSetMapper;
import com.njwd.platform.mapper.FinancialReportMapper;
import com.njwd.platform.service.FinancialReportItemSetService;
import com.njwd.platform.service.SequenceService;
import com.njwd.platform.utils.UserUtil;
import com.njwd.service.FileService;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.BatchResult;
import com.njwd.utils.FastUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author liuxiang
 * @Description 财务报告项目明细设置列表
 * @Date:9:56 2019/6/25
 **/
@Service
public class FinancialReportItemSetServiceImpl implements FinancialReportItemSetService {

    @Autowired
    private FinancialReportItemSetMapper financialReportItemSetMapper;

    @Autowired
    private FinancialReportItemMapper financialReportItemMapper;

    @Autowired
    private FinancialReportMapper financialReportMapper;

    @Resource
    private BasePlatformMapper basePlatformMapper;

    @Resource
    private ReferenceRelationService referenceRelationService;

    @Resource
    private FileService fileService;

    @Autowired
    private SequenceService sequenceService;

    /**
     * 新增报表项目库
     *
     * @param financialReportItemDto
     * @return int
     * @Author lj
     * @Date:16:23 2019/11/15
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value ="financialReportItemSetList", allEntries = true)
    public Long addFinancialReportItem(FinancialReportItemDto financialReportItemDto) {
        //校验名称不能重复
        checkData(financialReportItemDto);
        //复制属性
        FinancialReportItem financialReportItem = new FinancialReportItem();
        copyProperties(financialReportItemDto, financialReportItem);
        financialReportItemMapper.insert(financialReportItem);
        return financialReportItem.getId();
    }

    /**
     * 删除报表项目库
     *
     * @param financialReportItemDto
     * @return com.njwd.support.BatchResult
     * @Author lj
     * @Date:17:26 2019/11/15
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value ="financialReportItemSetList", allEntries = true)
    public BatchResult delFinancialReportItemBatch(FinancialReportItemDto financialReportItemDto) {
        return updateStatusBatch(financialReportItemDto,PlatformConstant.OperateType.DELETE);
    }

    /**
     * 批量校验
     * @Author lj
     * @Date:10:13 2019/11/13
     * @param financialReportItemDto， type
     * @return com.njwd.support.BatchResult
     **/
    private BatchResult updateStatusBatch(FinancialReportItemDto financialReportItemDto, int type) {
        //初始化
        BatchResult result = new BatchResult();
        result.setFailList(new LinkedList<>());
        result.setSuccessList(new ArrayList<>());

        //查询待查询的所有数据的状态
        List<FinancialReportItemVo> statusList = financialReportItemMapper.findReportItemListStatus(financialReportItemDto);
        //如果查询结果没有数据
        if (CollectionUtils.isEmpty(statusList)) {
            for (FinancialReportItemVo id : financialReportItemDto.getChangeList()) {
                addFailResult(result, id.getId(), ResultCode.RECORD_NOT_EXIST.message);
            }
        }
        //转化为id为key的状态map
        Map<Long, FinancialReportItemVo> statusMap = statusList.stream().collect(Collectors.toMap(FinancialReportItemVo::getId, o -> o));

        //循环判断当前数据是否能添加
        FinancialReportItemVo statusData;
        for (FinancialReportItemVo change : financialReportItemDto.getChangeList()) {
            Long changeId = change.getId();
            //获取当前数据对应的状态数据
            statusData = statusMap.get(changeId);
            if (statusData == null) {
                addFailResult(result, changeId, ResultCode.RECORD_NOT_EXIST.message);
                continue;
            }
            if(type==PlatformConstant.OperateType.DELETE){
                //判断 删除状态
                if (Constant.Is.YES.equals(statusData.getIsDel())) {
                    addFailResult(result, changeId, ResultCode.IS_DEL.message);
                    continue;
                }
                //判断 审核状态
                if (Constant.Is.YES.equals(statusData.getIsApproved())) {
                    addFailResult(result, changeId, ResultCode.DEL_CHECK_APPROVED.message);
                    continue;
                }
                // 校验 是否被引用
                ReferenceResult referenceResult = referenceRelationService.isReference(PlatformConstant.Reference.PLAT_FINANCIAL_REPORT_ITEM, changeId);
                if (referenceResult.isReference()) {
                    addFailResult(result, changeId, referenceResult.getReferenceDescription());
                    continue;
                }
            }

            if(type==PlatformConstant.OperateType.APPROVED){
                //判断 审核状态
                if (Constant.Is.YES.equals(statusData.getIsApproved())) {
                    addFailResult(result, changeId, ResultCode.APPROVE_CHECK_APPROVED.message);
                    continue;
                }
            }

            if(type==PlatformConstant.OperateType.DISAPPROVED){
                //判断 审核状态
                if (Constant.Is.NO.equals(statusData.getIsApproved())) {
                    addFailResult(result, changeId, ResultCode.DISAPPROVE_CHECK_DISAPPROVED.message);
                    continue;
                }
            }

            //判断版本号
            if (!statusData.getVersion().equals(change.getVersion())) {
                addFailResult(result, statusData.getId(), ResultCode.VERSION_ERROR.message);
                continue;
            }
            //通过验证的数据
            result.getSuccessList().add(statusData.getId());
        }

        // 获取用户
        SysUserVo userVo = UserUtil.getUserVo();
        List<Long> successList = result.getSuccessList();
        if(CollectionUtils.isNotEmpty(successList)){
            basePlatformMapper.batchProcess(successList,type,userVo,PlatformConstant.TableName.FINANCIAL_REPORT_ITEM);
        }
        return result;
    }

    /**
     * 添加失败结果
     * @Author lj
     * @Date:13:58 2019/11/13
     * @param result, id, failMessage
     * @return void
     **/
    private void addFailResult(BatchResult result, Long id, String failMessage) {
        ReferenceDescription fd = new ReferenceDescription();
        fd.setBusinessId(id);
        fd.setReferenceDescription(failMessage);
        result.getFailList().add(fd);
    }

    /**
     * 修改报表项目库
     *
     * @param financialReportItemDto
     * @return int
     * @Author lj
     * @Date:16:56 2019/11/15
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value ="financialReportItemSetList", allEntries = true)
    public Long updateFinancialReportItem(FinancialReportItemDto financialReportItemDto) {

        if(!financialReportItemDto.getName().equals(financialReportItemDto.getOldName())||
                !financialReportItemDto.getReportTypeId().equals(financialReportItemDto.getOldReportTypeId())){
            //校验名称不能重复
            checkData(financialReportItemDto);
        }

        //校验数据是否已审核
        FinancialReportItem reportItem = financialReportItemMapper.selectOne(new LambdaQueryWrapper<FinancialReportItem>().eq(FinancialReportItem::getId,financialReportItemDto.getId()));
        if(reportItem!=null){
            if (Constant.Is.YES.equals(reportItem.getIsApproved())) {
                throw new ServiceException(ResultCode.IS_APPROVED);
            }
        }
        //校验版本
        if (!reportItem.getVersion().equals(financialReportItemDto.getVersion())) {
            throw new ServiceException(ResultCode.VERSION_ERROR);
        }
        if(!financialReportItemDto.getReportTypeId().equals(financialReportItemDto.getOldReportTypeId())){
            String code = financialReportItemDto.getReportTypeCode();
            String platFromCode = sequenceService.getCode(PlatformConstant.PlatformCodeRule.FINANCIAL_REPORT_ITEM,PlatformConstant.PlatformCodeRule.FINANCIAL_REPORT_ITEM_LENGTH,code);
            financialReportItemDto.setCode(platFromCode);
        }
        //复制属性
        FinancialReportItem financialReportItem = new FinancialReportItem();
        FastUtils.copyProperties(financialReportItemDto, financialReportItem);
        SysUserVo userVo = UserUtil.getUserVo();
        financialReportItem.setUpdatorId(userVo.getUserId());
        financialReportItem.setUpdatorName(userVo.getName());
        financialReportItem.setUpdateTime(new Date());
        financialReportItem.setVersion(financialReportItemDto.getVersion()+1);
        financialReportItemMapper.update(financialReportItem,new LambdaQueryWrapper<FinancialReportItem>().eq(FinancialReportItem::getId,financialReportItemDto.getId()));
        return financialReportItemDto.getId();
    }

    /**
     * 审核报表项目库
     *
     * @param financialReportItemDto
     * @return com.njwd.support.BatchResult
     * @Author lj
     * @Date:17:48 2019/11/15
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value ="financialReportItemSetList", allEntries = true)
    public BatchResult approveFinancialReportItemBatch(FinancialReportItemDto financialReportItemDto) {
        return updateStatusBatch(financialReportItemDto,PlatformConstant.OperateType.APPROVED);
    }

    /**
     * 反审核报表项目库
     *
     * @param financialReportItemDto
     * @return com.njwd.support.BatchResult
     * @Author lj
     * @Date:17:50 2019/11/15
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value ="financialReportItemSetList", allEntries = true)
    public BatchResult disApproveFinancialReportItemBatch(FinancialReportItemDto financialReportItemDto) {
        return updateStatusBatch(financialReportItemDto,PlatformConstant.OperateType.DISAPPROVED);
    }

    /**
     * 根据ID查询报表项目库
     *
     * @param dto
     * @return com.njwd.entity.platform.vo.FinancialReportItemVo
     * @Author lj
     * @Date:18:10 2019/11/15
     **/
    @Override
    public FinancialReportItemVo findReportItemById(FinancialReportItemDto dto) {
        return financialReportItemMapper.findReportItemById(dto);
    }

    /**
     * 校验名称不能重复
     * @Author lj
     * @Date:17:00 2019/11/15
     * @param financialReportItemDto
     * @return void
     **/
    private void checkData(FinancialReportItemDto financialReportItemDto) {
        Integer row = financialReportItemMapper.selectCount(new LambdaQueryWrapper<FinancialReportItem>()
                .eq(FinancialReportItem::getName, financialReportItemDto.getName())
                .eq(FinancialReportItem::getReportTypeId, financialReportItemDto.getReportTypeId())
                .eq(FinancialReportItem::getIsDel, Constant.Number.ZERO));
        if (row != 0) {
            throw new ServiceException(ResultCode.REPORT_ITEM_NAME_EXIST);
        }
    }

    /**
     * 复制属性
     * @Author lj
     * @Date:16:37 2019/11/15
     * @param financialReportItemDto, financialReportItem]
     * @return void
     **/
    private void copyProperties(FinancialReportItemDto financialReportItemDto, FinancialReportItem financialReportItem) {
        String code = financialReportItemDto.getReportTypeCode();
        String platFromCode = sequenceService.getCode(PlatformConstant.PlatformCodeRule.FINANCIAL_REPORT_ITEM,PlatformConstant.PlatformCodeRule.FINANCIAL_REPORT_ITEM_LENGTH,code);
        FastUtils.copyProperties(financialReportItemDto, financialReportItem);
        //获取用户
        SysUserVo userVo = UserUtil.getUserVo();
        financialReportItem.setCode(platFromCode);
        financialReportItem.setCreatorId(userVo.getUserId());
        financialReportItem.setCreatorName(userVo.getName());
        financialReportItem.setCreateTime(new Date());
        financialReportItem.setUpdateTime(null);
    }

    /**
     * 查询报表项目库分页
     *
     * @param financialReportItemDto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.FinancialReportItemVo>
     * @Author lj
     * @Date:15:18 2019/11/15
     **/
    @Override
    public Page<FinancialReportItemVo> findReportItemListPage(FinancialReportItemDto financialReportItemDto) {
        Page<FinancialReportItemVo> page= financialReportItemDto.getPage();
        page=financialReportItemMapper.findReportItemListPage(page,financialReportItemDto);
        return page;
    }

    /**
     * 导出
     *
     * @param financialReportItemDto
     * @param response
     */
    @Override
    public void exportExcel(FinancialReportItemDto financialReportItemDto, HttpServletResponse response) {
        Page<FinancialReportItemVo> page = financialReportItemDto.getPage();
        fileService.resetPage(page);
        Page<FinancialReportItemVo> financialReportListPage = financialReportItemMapper.findReportItemListPage(page,financialReportItemDto);
        fileService.exportExcel(response,financialReportListPage.getRecords(),
                new ExcelColumn("code","编码"),
                new ExcelColumn("name","名称"),
                new ExcelColumn("reportTypeName","报表类型"),
                new ExcelColumn("isApproved","审核状态", ExcelDataConstant.SYSTEM_DATA_IS_APPROVED)
        );
    }

    /**
     * 添加财务报告项目明细
     *
     * @param financialReportItemSetDto
     * @return int
     * @Author lj
     * @Date:15:20 2019/11/19
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value ="financialReportItemSetList", allEntries = true)
    public Long addFinancialReportItemSet(FinancialReportItemSetDto financialReportItemSetDto) {
        //校验名称不能重复
        checkName(financialReportItemSetDto);
        //获取编码
        FinancialReportItem reportItem = getFinancialReportItem(financialReportItemSetDto);
        //排序
        String code=String.valueOf(financialReportItemSetDto.getSort());
        //校验编码 当前财务报表下不允许重复
        checkCode(financialReportItemSetDto, code);
        //复制属性
        FinancialReportItemSet financialReportItemSet = new FinancialReportItemSet();
        FastUtils.copyProperties(financialReportItemSetDto, financialReportItemSet);
        SysUserVo userVo = UserUtil.getUserVo();
        financialReportItemSet.setCreatorId(userVo.getUserId());
        financialReportItemSet.setCreatorName(userVo.getName());
        financialReportItemSet.setCode(code);
        financialReportItemSet.setCreateTime(new Date());
        financialReportItemSet.setUpdateTime(null);
        financialReportItemSetMapper.insert(financialReportItemSet);
        //更新项目库表的字段属性
        if(financialReportItemSetDto.getItemType()!=null){
            updateItem(financialReportItemSetDto, reportItem);
        }
        Long itemSetId = financialReportItemSet.getId();
        if(financialReportItemSetDto.getFormula()!=null){
            //组装公式数据
            List<FinancialReportItemFormula> financialReportItemFormulaList = getFinancialReportItemFormulas(financialReportItemSetDto, itemSetId);
            //插入公式
            financialReportItemSetMapper.insertItemFormulaBatch(financialReportItemFormulaList);
        }
        return itemSetId;
    }

    /**
     * 校验编码 当前财务报表下不允许重复
     * @Author lj
     * @Date:16:51 2019/11/25
     * @param financialReportItemSetDto, code
     * @return void
     **/
    private void checkCode(FinancialReportItemSetDto financialReportItemSetDto, String code) {
        Integer row = financialReportItemSetMapper.selectCount(new LambdaQueryWrapper<FinancialReportItemSet>()
                .eq(FinancialReportItemSet::getCode, code)
                .eq(FinancialReportItemSet::getReportId, financialReportItemSetDto.getReportId())
                .eq(FinancialReportItemSet::getIsDel, Constant.Number.ZERO));
        if (row != 0) {
            throw new ServiceException(ResultCode.REPORT_ITEM_CODE_EXIST);
        }
    }

    /**
     * 删除财务报告项目明细
     *
     * @param financialReportItemSetDto
     * @return int
     * @Author lj
     * @Date:9:41 2019/11/20
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value ="financialReportItemSetList", allEntries = true)
    public BatchResult delFinancialReportItemSetBatch(FinancialReportItemSetDto financialReportItemSetDto) {
        //初始化
        BatchResult result = new BatchResult();
        result.setFailList(new LinkedList<>());
        result.setSuccessList(new ArrayList<>());
        for(FinancialReportItemSetVo financialReportItemSetVo:financialReportItemSetDto.getChangeList()){
            Long changeId = financialReportItemSetVo.getId();
            FinancialReportItemSet financialReportItemSet= financialReportItemSetMapper.selectOne(new LambdaQueryWrapper<FinancialReportItemSet>()
                    .eq(FinancialReportItemSet::getId,changeId));
            FinancialReport financialReport= financialReportMapper.selectOne(new LambdaQueryWrapper<FinancialReport>()
                    .eq(FinancialReport::getId,financialReportItemSetVo.getReportId()));
            //判断 删除状态
            if (Constant.Is.YES.equals(financialReportItemSet.getIsDel())) {
                addFailResult(result, changeId, ResultCode.IS_DEL.message);
                continue;
            }
            //判断 删除状态
            if (Constant.Is.YES.equals(financialReport.getIsDel())) {
                addFailResult(result, changeId, ResultCode.IS_DEL.message);
                continue;
            }
            //判断 审核状态
            if (Constant.Is.YES.equals(financialReport.getIsApproved())) {
                addFailResult(result, changeId, ResultCode.DEL_CHECK_APPROVED.message);
                continue;
            }
            //判断引用
            ReferenceResult referenceResult = referenceRelationService.isReference(PlatformConstant.Reference.PLAT_FINANCIAL_REPORT_ITEM_SET, changeId);
            if (referenceResult.isReference()) {
                addFailResult(result, changeId, ResultCode.IS_REFERENCE.message);
                continue;
            }
            //通过验证的数据
            result.getSuccessList().add(changeId);
        }
        //修改财务报告项目明细数据状态为删除状态
        //获取用户
        SysUserVo userVo = UserUtil.getUserVo();
        List<Long> successList = result.getSuccessList();

        if(CollectionUtils.isNotEmpty(successList)){
            basePlatformMapper.batchProcess(successList,PlatformConstant.OperateType.DELETE,userVo,PlatformConstant.TableName.FINANCIAL_REPORT_ITEM_SET);
        }
        return result;
    }

    /**
     * 修改财务报告项目明细
     *
     * @param financialReportItemSetDto
     * @return int
     * @Author lj
     * @Date:17:58 2019/11/19
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value ="financialReportItemSetList", allEntries = true)
    public Long updateFinancialReportItemSet(FinancialReportItemSetDto financialReportItemSetDto) {
        //校验名称不能重复
        if(!financialReportItemSetDto.getOldName().equals(financialReportItemSetDto.getName())){
            checkName(financialReportItemSetDto);
        }

        //校验数据是否已审核
        FinancialReport report = financialReportMapper.selectOne(new LambdaQueryWrapper<FinancialReport>().eq(FinancialReport::getId,financialReportItemSetDto.getReportId()));
        if(report!=null){
            if (Constant.Is.YES.equals(report.getIsApproved())) {
                throw new ServiceException(ResultCode.IS_APPROVED);
            }
        }

        //校验数据是否已审核
        FinancialReportItemSet itemSet = financialReportItemSetMapper.selectOne(new LambdaQueryWrapper<FinancialReportItemSet>().eq(FinancialReportItemSet::getId,financialReportItemSetDto.getId()));
        //版本校验
        if (!itemSet.getVersion().equals(financialReportItemSetDto.getVersion())) {
            throw new ServiceException(ResultCode.VERSION_ERROR);
        }

        //获取编码
        FinancialReportItem reportItem = getFinancialReportItem(financialReportItemSetDto);
        //排序
        String code=String.valueOf(financialReportItemSetDto.getSort());
        if(!financialReportItemSetDto.getOldSort().equals(financialReportItemSetDto.getSort())){
            //校验编码 当前财务报表下不允许重复
            checkCode(financialReportItemSetDto, code);
        }
        //复制属性
        FinancialReportItemSet financialReportItemSet = new FinancialReportItemSet();
        FastUtils.copyProperties(financialReportItemSetDto, financialReportItemSet);
        SysUserVo userVo = UserUtil.getUserVo();
        financialReportItemSet.setUpdatorId(userVo.getUserId());
        financialReportItemSet.setUpdatorName(userVo.getName());
        financialReportItemSet.setCode(code);
        financialReportItemSet.setUpdateTime(new Date());
        financialReportItemSet.setVersion(financialReportItemSetDto.getVersion());
        financialReportItemSetMapper.update(financialReportItemSet,new LambdaQueryWrapper<FinancialReportItemSet>()
                .eq(FinancialReportItemSet::getId,financialReportItemSetDto.getId()));
        //更新项目库表的字段属性
        if(financialReportItemSetDto.getItemType()!=null){
            updateItem(financialReportItemSetDto, reportItem);
        }
        Long itemSetId = financialReportItemSet.getId();
        if(financialReportItemSetDto.getFormula()!=null){
            //组装公式数据
            List<FinancialReportItemFormula> financialReportItemFormulaList = getFinancialReportItemFormulas(financialReportItemSetDto, itemSetId);
            //清空公式
            financialReportItemSetMapper.deleteItemFormulaByItemId(itemSetId);
            //插入公式
            financialReportItemSetMapper.insertItemFormulaBatch(financialReportItemFormulaList);
        }
        return itemSetId;
    }

    /**
     * 清空表达式
     *
     * @param financialReportItemSetDto
     * @return int
     * @Author lj
     * @Date:17:58 2019/11/19
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value ="financialReportItemSetList", allEntries = true)
    public int clear(FinancialReportItemSetDto financialReportItemSetDto) {
        Long itemSetId = financialReportItemSetDto.getId();
        //清空公式
        return financialReportItemSetMapper.deleteItemFormulaByItemId(itemSetId);
    }

    /**
     * 更新项目库字段
     * @Author lj
     * @Date:18:08 2019/11/19
     * @param financialReportItemSetDto, reportItem]
     * @return void
     **/
    private void updateItem(FinancialReportItemSetDto financialReportItemSetDto, FinancialReportItem reportItem) {
        FinancialReportItem financialReportItem = new FinancialReportItem();
        financialReportItem.setItemType(financialReportItemSetDto.getItemType());
        financialReportItemMapper.update(financialReportItem, new LambdaQueryWrapper<FinancialReportItem>().eq(FinancialReportItem::getId, reportItem.getId()));
    }

    private FinancialReportItem getFinancialReportItem(FinancialReportItemSetDto financialReportItemSetDto) {
        return financialReportItemMapper.selectOne(new LambdaQueryWrapper<FinancialReportItem>()
                .eq(FinancialReportItem::getName, financialReportItemSetDto.getName())
                .eq(FinancialReportItem::getReportTypeId, financialReportItemSetDto.getReportTypeId())
                .eq(FinancialReportItem::getIsDel, Constant.Number.ZERO));
    }

    /**
     * 组装公式数据
     * @Author lj
     * @Date:17:29 2019/11/19
     * @param financialReportItemSetDto, itemSetId
     * @return java.util.List<com.njwd.entity.platform.FinancialReportItemFormula>
     **/
    private List<FinancialReportItemFormula> getFinancialReportItemFormulas(FinancialReportItemSetDto financialReportItemSetDto, Long itemSetId) {
        //先做减的截取
        String[] item = financialReportItemSetDto.getFormula().split(Constant.Character.THROUGH_LINE);
        List<String> sList=new ArrayList<>();
        List<String> addList=new ArrayList<>();
        for(int i=0;i<item.length;i++){
            String[] temp =item[i].split("\\+");
            if(i==0){
                for(String t:temp){
                    addList.add(t);
                }
            }else {
                for(int j=0;j<temp.length;j++){
                    if(j==0){
                        sList.add(temp[j]);
                    }else {
                        addList.add(temp[j]);
                    }
                }
            }
        }
        List<FinancialReportItemFormula> financialReportItemFormulaList= new ArrayList<>();
        for(String add:addList){
            FinancialReportItemFormula financialReportItemFormula= new FinancialReportItemFormula();
            financialReportItemFormula.setItemSetId(itemSetId);
            financialReportItemFormula.setOperator(Constant.Number.ANTI_INITLIZED);
            if(Constant.Number.TWO.equals(financialReportItemSetDto.getDataType())){
                financialReportItemFormula.setFormulaType(Constant.Number.INITIAL);
            }else {
                financialReportItemFormula.setFormulaType(Constant.Number.ANTI_INITLIZED);
            }
            financialReportItemFormula.setFormulaItemCode(getNumbers(add));
            financialReportItemFormula.setCreateTime(new Date());
            financialReportItemFormulaList.add(financialReportItemFormula);
        }
        for(String s:sList){
            FinancialReportItemFormula formula= new FinancialReportItemFormula();
            formula.setItemSetId(itemSetId);
            formula.setOperator(Constant.Number.INITIAL);
            formula.setCreateTime(new Date());
            if(Constant.Number.TWO.equals(financialReportItemSetDto.getDataType())){
                formula.setFormulaType(Constant.Number.INITIAL);
            }else {
                formula.setFormulaType(Constant.Number.ANTI_INITLIZED);
            }
            formula.setFormulaItemCode(getNumbers(s));
            financialReportItemFormulaList.add(formula);
        }
        return financialReportItemFormulaList;
    }

    //截取数字
    private String getNumbers(String content) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }

    /**
     * 校验名称不能重复
     * @Author lj
     * @Date:16:01 2019/11/19
     * @param financialReportItemSetDto
     * @return void
     **/
    private void checkName(FinancialReportItemSetDto financialReportItemSetDto) {
        Integer row = financialReportItemSetMapper.selectCount(new LambdaQueryWrapper<FinancialReportItemSet>()
                .eq(FinancialReportItemSet::getName, financialReportItemSetDto.getName())
                .eq(FinancialReportItemSet::getReportId, financialReportItemSetDto.getReportId())
                .eq(FinancialReportItemSet::getIsDel, Constant.Number.ZERO));
        if (row != 0) {
            throw new ServiceException(ResultCode.REPORT_ITEM_NAME_EXIST);
        }
    }

    /**
     * 根据报告ID查询财务报告项目明细
     *
     * @param financialReportItemSetDto
     * @return java.util.List<com.njwd.entity.platform.vo.FinancialReportItemSetVo>
     * @Author lj
     * @Date:9:30 2019/11/19
     **/
    @Override
    public List<FinancialReportItemSetVo> findReportItemSetList(FinancialReportItemSetDto financialReportItemSetDto) {
        return financialReportItemSetMapper.findReportItemSetList(financialReportItemSetDto);
    }

    /**
     * 根据报告ID查询财务报告项目明细列表分页
     *
     * @param financialReportItemSetDto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.FinancialReportItemSetVo>
     * @Author lj
     * @Date:14:41 2019/11/19
     **/
    @Override
    public Page<FinancialReportItemSetVo> findReportItemSetListPage(FinancialReportItemSetDto financialReportItemSetDto) {
        Page<FinancialReportItemSetVo> page= financialReportItemSetDto.getPage();
        page=financialReportItemSetMapper.findReportItemSetListPage(page,financialReportItemSetDto);
        return page;
    }

    /**
     * 根据报告项目ID查询财务报告项目明细
     *
     * @param financialReportItemSetDto
     * @return com.njwd.entity.platform.vo.FinancialReportItemSetVo
     * @Author lj
     * @Date:10:49 2019/11/19
     **/
    @Override
    public FinancialReportItemSetVo findReportItemSetById(FinancialReportItemSetDto financialReportItemSetDto) {
        return financialReportItemSetMapper.findReportItemSetById(financialReportItemSetDto);
    }

    /**
     * @Description 根据报告库ID查询财务报告项目明细设置列表
     * @Author liuxiang
     * @Date:17:45 2019/8/1
     * @Param [dto]
     * @return java.util.List<com.njwd.platform.entity.vo.FinancialReportItemSetVo>
     **/
    @Override
    @Cacheable(value = "financialReportItemSetList", key = "#dto.reportId+''")
    public List<FinancialReportItemSetVo> findFinancialReportItemSetList(FinancialReportItemSetDto dto) {
        return financialReportItemSetMapper.findFinancialReportItemSetList(dto);
    }
}
