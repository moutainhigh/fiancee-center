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
import com.njwd.entity.platform.FinancialReportItemSet;
import com.njwd.entity.platform.dto.FinancialReportDto;
import com.njwd.entity.platform.vo.FinancialReportVo;
import com.njwd.entity.platform.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.platform.mapper.BasePlatformMapper;
import com.njwd.platform.mapper.FinancialReportItemSetMapper;
import com.njwd.platform.mapper.FinancialReportMapper;
import com.njwd.platform.service.FinancialReportService;
import com.njwd.platform.service.MessageService;
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
import java.util.stream.Collectors;

/**
 * @Author liuxiang
 * @Description 财务报告
 * @Date:9:56 2019/6/25
 **/
@Service
public class FinancialReportServiceImpl implements FinancialReportService {

    @Autowired
    private FinancialReportMapper financialReportMapper;

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private FinancialReportItemSetMapper financialReportItemSetMapper;

    @Resource
    private BasePlatformMapper basePlatformMapper;

    @Resource
    private FileService fileService;

    @Resource
    private ReferenceRelationService referenceRelationService;

    @Resource
    private MessageService messageService;

    /**
     * 新增财务报表
     *
     * @param financialReportDto
     * @return int
     * @Author lj
     * @Date:10:29 2019/11/18
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"assetListByAccStandardId","profitListByAccStandardId","cashFlowListByAccStandardId"}, allEntries = true)
    public Long addFinancialReport(FinancialReportDto financialReportDto) {
        //校验名称不重复
        checkName(financialReportDto);
        //复制属性
        FinancialReport financialReport = new FinancialReport();
        FastUtils.copyProperties(financialReportDto, financialReport);
        SysUserVo userVo = UserUtil.getUserVo();
        financialReport.setCreatorId(userVo.getUserId());
        financialReport.setCreatorName(userVo.getName());
        String code = financialReportDto.getAccStandardCode().substring(financialReportDto.getAccStandardCode().length()-PlatformConstant.CashFlow.CODE_LENGTH)+financialReportDto.getReportTypeCode();
        String platFromCode = sequenceService.getCode(PlatformConstant.PlatformCodeRule.FINANCIAL_REPORT,PlatformConstant.CashFlow.CODE_LENGTH,code);
        financialReport.setCode(platFromCode);

        financialReport.setCreateTime(new Date());
        financialReport.setUpdateTime(null);
        financialReportMapper.insert(financialReport);
        return financialReport.getId();
    }

    /**
     * 校验名称不重复
     * @Author lj
     * @Date:11:35 2019/11/18
     * @param financialReportDto
     * @return void
     **/
    private void checkName(FinancialReportDto financialReportDto) {
        int row = 0;
        row = financialReportMapper.selectCount(new LambdaQueryWrapper<FinancialReport>()
                .eq(FinancialReport::getName, financialReportDto.getName())
                .eq(FinancialReport::getIsDel, Constant.Number.ZERO));
        if (row != 0) {
            throw new ServiceException(ResultCode.NAME_EXIST);
        }
    }

    /**
     * 修改财务报表
     *
     * @param financialReportDto
     * @return int
     * @Author lj
     * @Date:14:29 2019/11/18
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"assetListByAccStandardId","profitListByAccStandardId","cashFlowListByAccStandardId"}, allEntries = true)
    public Long updateFinancialReport(FinancialReportDto financialReportDto) {

        //校验数据是否已审核
        FinancialReport report = financialReportMapper.selectOne(new LambdaQueryWrapper<FinancialReport>().eq(FinancialReport::getId,financialReportDto.getId()));
        if(report!=null){
            if (Constant.Is.YES.equals(report.getIsApproved())) {
                throw new ServiceException(ResultCode.IS_APPROVED);
            }
        }
        //校验版本
        if (!report.getVersion().equals(financialReportDto.getVersion())) {
            throw new ServiceException(ResultCode.VERSION_ERROR);
        }
        //校验名称不重复
        if(!financialReportDto.getName().equals(financialReportDto.getOldName())){
            checkName(financialReportDto);
        }
        if(!financialReportDto.getReportTypeCode().equals(financialReportDto.getOldReportTypeCode())||!financialReportDto.getAccStandardCode().equals(financialReportDto.getOldAccStandardCode())){
            String code = financialReportDto.getAccStandardCode().substring(financialReportDto.getAccStandardCode().length()-PlatformConstant.CashFlow.CODE_LENGTH)+financialReportDto.getReportTypeCode();
            String platFromCode = sequenceService.getCode(PlatformConstant.PlatformCodeRule.FINANCIAL_REPORT,PlatformConstant.CashFlow.CODE_LENGTH,code);
            financialReportDto.setCode(platFromCode);
        }
        //复制属性
        FinancialReport financialReport = new FinancialReport();
        FastUtils.copyProperties(financialReportDto, financialReport);
        SysUserVo userVo = UserUtil.getUserVo();
        financialReport.setUpdatorId(userVo.getUserId());
        financialReport.setUpdatorName(userVo.getName());
        financialReport.setUpdateTime(new Date());
        financialReport.setVersion(financialReportDto.getVersion()+1);
        financialReportMapper.update(financialReport,new LambdaQueryWrapper<FinancialReport>().eq(FinancialReport::getId,financialReportDto.getId()));
        return financialReportDto.getId();
    }

    /**
     * 删除财务报表
     *
     * @param financialReportDto
     * @return com.njwd.support.BatchResult
     * @Author lj
     * @Date:14:57 2019/11/18
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"assetListByAccStandardId","profitListByAccStandardId","cashFlowListByAccStandardId"}, allEntries = true)
    public BatchResult delFinancialReportBatch(FinancialReportDto financialReportDto) {
        return updateStatusBatch(financialReportDto,PlatformConstant.OperateType.DELETE);
    }

    /**
     * 审核财务报表
     *
     * @param financialReportDto
     * @return com.njwd.support.BatchResult
     * @Author lj
     * @Date:16:00 2019/11/18
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"assetListByAccStandardId","profitListByAccStandardId","cashFlowListByAccStandardId"}, allEntries = true)
    public BatchResult approveFinancialReportBatch(FinancialReportDto financialReportDto) {
        return updateStatusBatch(financialReportDto,PlatformConstant.OperateType.APPROVED);
    }

    /**
     * 反审核财务报表
     *
     * @param financialReportDto
     * @return com.njwd.support.BatchResult
     * @Author lj
     * @Date:16:00 2019/11/18
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"assetListByAccStandardId","profitListByAccStandardId","cashFlowListByAccStandardId"}, allEntries = true)
    public BatchResult disApproveFinancialReportBatch(FinancialReportDto financialReportDto) {
        return updateStatusBatch(financialReportDto,PlatformConstant.OperateType.DISAPPROVED);
    }

    /**
     * 发布财务报表
     *
     * @param financialReportDto
     * @return com.njwd.support.BatchResult
     * @Author lj
     * @Date:16:02 2019/11/18
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"assetListByAccStandardId","profitListByAccStandardId","cashFlowListByAccStandardId"}, allEntries = true)
    public BatchResult releaseFinancialReportBatch(FinancialReportDto financialReportDto) {
        return updateStatusBatch(financialReportDto,PlatformConstant.OperateType.RELEASED);
    }

    /**
     * 根据ID查询财务报表
     *
     * @param financialReportDto
     * @return com.njwd.entity.platform.vo.FinancialReportVo
     * @Author lj
     * @Date:16:45 2019/11/18
     **/
    @Override
    public FinancialReportVo findFinancialReportById(FinancialReportDto financialReportDto) {
        return financialReportMapper.findFinancialReportById(financialReportDto);
    }

    /**
     * 批量校验
     * @Author lj
     * @Date:10:13 2019/11/13
     * @param financialReportDto， type
     * @return com.njwd.support.BatchResult
     **/
    private BatchResult updateStatusBatch(FinancialReportDto financialReportDto, int type) {
        //初始化
        BatchResult result = new BatchResult();
        result.setFailList(new LinkedList<>());
        result.setSuccessList(new ArrayList<>());

        //查询待查询的所有数据的状态
        List<FinancialReportVo> statusList = financialReportMapper.findFinancialReportListStatus(financialReportDto);
        //如果查询结果没有数据
        if (CollectionUtils.isEmpty(statusList)) {
            for (FinancialReportVo id : financialReportDto.getChangeList()) {
                addFailResult(result, id.getId(), ResultCode.RECORD_NOT_EXIST.message);
            }
        }
        //转化为id为key的状态map
        Map<Long, FinancialReportVo> statusMap = statusList.stream().collect(Collectors.toMap(FinancialReportVo::getId, o -> o));

        List<Long> ids = new ArrayList<>();
        for (FinancialReportVo id : financialReportDto.getChangeList()) {
            ids.add(id.getId());
        }
        //查询财务报表下的报表项目
        List<FinancialReportItemSet> itemSets =
                financialReportItemSetMapper.selectList(new LambdaQueryWrapper<FinancialReportItemSet>().in(FinancialReportItemSet::getReportId,ids));
        Map<Long, List<FinancialReportItemSet>> itemSetsMap =itemSets.stream().collect(Collectors.groupingBy(FinancialReportItemSet::getReportId));

        //循环判断当前数据是否能添加
        FinancialReportVo statusData;
        for (FinancialReportVo change : financialReportDto.getChangeList()) {
            Long changeId=change.getId();
            //获取当前数据对应的状态数据
            statusData = statusMap.get(changeId);
            if (statusData == null) {
                addFailResult(result, changeId, ResultCode.RECORD_NOT_EXIST.message);
                continue;
            }
            if(type==PlatformConstant.OperateType.DELETE){
                //判断 发布状态
                if (Constant.Is.YES.equals(statusData.getIsReleased())) {
                    addFailResult(result, changeId, ResultCode.IS_RELEASED.message);
                    continue;
                }
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
                ReferenceResult referenceResult = referenceRelationService.isReference(PlatformConstant.Reference.PLAT_FINANCIAL_REPORT, changeId);
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
                //检查当前已选数据有没有报表项目
                if (CollectionUtils.isEmpty(itemSetsMap.get(changeId))) {
                    addFailResult(result, changeId, ResultCode.REPROT_ITEM_SET_NOT_EXIST.message);
                    continue;
                }
            }

            if(type==PlatformConstant.OperateType.DISAPPROVED){
                //判断 发布状态
                if (Constant.Is.YES.equals(statusData.getIsReleased())) {
                    addFailResult(result, changeId, ResultCode.DISAPPROVE_CHECK_RELEASED.message);
                    continue;
                }
                //判断 审核状态
                if (Constant.Is.NO.equals(statusData.getIsApproved())) {
                    addFailResult(result, changeId, ResultCode.DISAPPROVE_CHECK_DISAPPROVED.message);
                    continue;
                }
            }

            if(type==PlatformConstant.OperateType.RELEASED){
                //判断 审核状态
                if (Constant.Is.NO.equals(statusData.getIsApproved())) {
                    addFailResult(result, changeId, ResultCode.RELEASE_CHECK_NO_APPROVED.message);
                    continue;
                }
                //判断 发布状态
                if (Constant.Is.YES.equals(statusData.getIsReleased())) {
                    addFailResult(result, changeId, ResultCode.RELEASE_CHECK_RELEASED.message);
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
        List<Long> successList = result.getSuccessList();
        //获取用户
        SysUserVo userVo = UserUtil.getUserVo();
        if(CollectionUtils.isNotEmpty(successList)){
            basePlatformMapper.batchProcess(successList,type,userVo,PlatformConstant.TableName.FINANCIAL_REPORT);
            //发布
            if(type==PlatformConstant.OperateType.RELEASED){
                messageService.sendMessage(PlatformConstant.MessageType.DATA_UPDATE,financialReportDto.getMessageDto());
            }
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
     * 查询财务报表分页
     *
     * @param financialReportDto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.FinancialReportVo>
     * @Author lj
     * @Date:9:58 2019/11/18
     **/
    @Override
    public Page<FinancialReportVo> findFinancialReportListPage(FinancialReportDto financialReportDto) {
        Page<FinancialReportVo> page =financialReportDto.getPage();
        page = financialReportMapper.findFinancialReportListPage(page,financialReportDto);
        return page;
    }

    /**
     * 导出
     *
     * @param financialReportDto
     * @param response
     */
    @Override
    public void exportExcel(FinancialReportDto financialReportDto, HttpServletResponse response) {
        Page<FinancialReportVo> page = financialReportDto.getPage();
        fileService.resetPage(page);
        Page<FinancialReportVo> financialReportListPage = financialReportMapper.findFinancialReportListPage(page,financialReportDto);
        fileService.exportExcel(response,financialReportListPage.getRecords(),
                new ExcelColumn("code","编码"),
                new ExcelColumn("name","名称"),
                new ExcelColumn("accStandardName","会计准则"),
                new ExcelColumn("reportTypeName","报表类型"),
                new ExcelColumn("year","年度版本"),
                new ExcelColumn("isApproved","审核状态", ExcelDataConstant.SYSTEM_DATA_IS_APPROVED),
                new ExcelColumn("isReleased","发布状态",ExcelDataConstant.SYSTEM_DATA_IS_RELEASED)
        );
    }

    /**
     * @Description 根据会计准则ID查询资产负债表
     * @Author liuxiang
     * @Date:16:48 2019/7/2
     * @Param [financialReportVo]
     * @return java.util.List<com.njwd.platform.entity.vo.FinancialReportVo>
     **/
    @Override
    @Cacheable(value = "assetListByAccStandardId", key = "#financialReportDto.accStandardId+''")
    public List<FinancialReportVo> findAssetListByAccStandardId(FinancialReportDto financialReportDto) {
        return financialReportMapper.findAssetListByAccStandardId(financialReportDto);
    }

    /**
     * @Description 根据会计准则ID查询利润表
     * @Author liuxiang
     * @Date:16:48 2019/7/2
     * @Param [financialReportVo]
     * @return java.util.List<com.njwd.platform.entity.vo.FinancialReportVo>
     **/
    @Override
    @Cacheable(value = "profitListByAccStandardId", key = "#financialReportDto.accStandardId+''")
    public List<FinancialReportVo> findProfitListByAccStandardId(FinancialReportDto financialReportDto) {
        return financialReportMapper.findProfitListByAccStandardId(financialReportDto);
    }

    /**
     * @Description 查询现金流量表下拉框
     * @Author liuxiang
     * @Date:16:49 2019/7/2
     * @Param [financialReportVo]
     * @return java.util.List<com.njwd.platform.entity.vo.FinancialReportVo>
     **/
    @Override
    @Cacheable(value = "cashFlowListByAccStandardId", key = "#financialReportDto.accStandardId+''")
    public List<FinancialReportVo> findCashFlowListByAccStandardId(FinancialReportDto financialReportDto) {
        return financialReportMapper.findCashFlowListByAccStandardId(financialReportDto);
    }

    /**
     * @Description 财务报告资产负债表查询
     * @Author liuxiang
     * @Date:17:38 2019/7/26
     * @Param []
     * @return java.util.List<com.njwd.platform.entity.vo.FinancialReportVo>
     **/
    @Override
    public List<FinancialReportVo> findAssetList() {
        return financialReportMapper.findAssetList();
    }

    /**
     * @Description 财务报告现金流量表查询
     * @Author liuxiang
     * @Date:17:38 2019/7/26
     * @Param []
     * @return java.util.List<com.njwd.platform.entity.vo.FinancialReportVo>
     **/
    @Override
    public List<FinancialReportVo> findCashFlowList() {
        return financialReportMapper.findCashFlowList();
    }

    /**
     * @Description 财务报告利润表查询
     *
     * @Author liuxiang
     * @Date:17:38 2019/7/26
     * @Param []
     * @return java.util.List<com.njwd.platform.entity.vo.FinancialReportVo>
     **/
    @Override
    public List<FinancialReportVo> findProfitList() {
        return financialReportMapper.findProfitList();
    }
}
