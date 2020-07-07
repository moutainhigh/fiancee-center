package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.ExcelDataConstant;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.ReferenceResult;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.platform.CashFlow;
import com.njwd.entity.platform.dto.CashFlowDto;
import com.njwd.entity.platform.vo.CashFlowVo;
import com.njwd.entity.platform.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.platform.mapper.BasePlatformMapper;
import com.njwd.platform.mapper.CashFlowMapper;
import com.njwd.platform.service.CashFlowService;
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
 * @Author lj
 * @Description 现金流量项目表
 * @Date:16:06 2019/6/13
 **/
@Service
public class CashFlowServiceImpl  implements CashFlowService{

    @Autowired
    private CashFlowMapper cashFlowMapper;

    @Autowired
    private SequenceService sequenceService;

    @Resource
    private BasePlatformMapper basePlatformMapper;

    @Resource
    private FileService fileService;

    @Resource
    private ReferenceRelationService referenceRelationService;

    @Resource
    private MessageService messageService;

    /**
     * 添加现金流量项目表
     *
     * @param cashFlowDto
     * @return int
     * @Author lj
     * @Date:11:55 2019/11/12
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"cashFlowList","cashListByStandIdAndTypeId"}, allEntries = true)
    public Long addCashFlow(CashFlowDto cashFlowDto) {
        //校验一个准则下面只有一个现金流量项目表
        checkAccStandard(cashFlowDto);
        //复制属性
        CashFlow cashFlow = new CashFlow();
        FastUtils.copyProperties(cashFlowDto, cashFlow);
        SysUserVo userVo = UserUtil.getUserVo();
        String code = cashFlowDto.getAccStandardCode().substring(cashFlowDto.getAccStandardCode().length()-PlatformConstant.CashFlow.CODE_LENGTH);
        String platFromCode = sequenceService.getCode(PlatformConstant.PlatformCodeRule.CASH_FLOW,PlatformConstant.CashFlow.CODE_LENGTH,code);
        StringBuilder maxLevelSb = new StringBuilder(PlatformConstant.CashFlow.MAX_LEVEL_PRE);
        for (Byte i = 1; i < cashFlowDto.getMaxLevelNum(); i++) {
            maxLevelSb.append(PlatformConstant.CashFlow.MAX_LEVEL_CONCAT);
        }
        cashFlow.setMaxLevel(maxLevelSb.toString());
        cashFlow.setCode(platFromCode);
        cashFlow.setCreatorId(userVo.getUserId());
        cashFlow.setCreatorName(userVo.getName());
        cashFlow.setCreateTime(new Date());
        cashFlow.setUpdateTime(null);
        cashFlowMapper.insert(cashFlow);
        return cashFlow.getId();
    }

    /**
     * 校验一个准则下面只有一个现金流量项目表
     * @Author lj
     * @Date:16:21 2019/11/26
     * @param cashFlowDto
     * @return void
     **/
    private void checkAccStandard(CashFlowDto cashFlowDto) {
        Integer row = cashFlowMapper.selectCount(new LambdaQueryWrapper<CashFlow>()
                .eq(CashFlow::getAccStandardId, cashFlowDto.getAccStandardId())
                .eq(CashFlow::getIsDel, Constant.Number.ZERO));
        if (row != 0) {
            throw new ServiceException(ResultCode.CASH_FLOW_EXIST);
        }
    }

    /**
     * 修改现金流量项目表
     *
     * @param cashFlowDto
     * @return int
     * @Author lj
     * @Date:11:55 2019/11/12
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"cashFlowList","cashListByStandIdAndTypeId"}, allEntries = true)
    public Long updateCashFlow(CashFlowDto cashFlowDto) {

        //校验数据是否已审核
        CashFlow cash = cashFlowMapper.selectOne(new LambdaQueryWrapper<CashFlow>().eq(CashFlow::getId,cashFlowDto.getId()));
        if(cash!=null){
            if (Constant.Is.YES.equals(cash.getIsApproved())) {
                throw new ServiceException(ResultCode.IS_APPROVED);
            }
        }
        //版本校验
        if (!cash.getVersion().equals(cashFlowDto.getVersion())) {
            throw new ServiceException(ResultCode.VERSION_ERROR);
        }
        //校验一个准则下面只有一个现金流量项目表
        if(!cashFlowDto.getOldAccStandardId().equals(cashFlowDto.getAccStandardId())){
            checkAccStandard(cashFlowDto);
            String code = cashFlowDto.getAccStandardCode().substring(cashFlowDto.getAccStandardCode().length()-PlatformConstant.CashFlow.CODE_LENGTH);
            String platFromCode = sequenceService.getCode(PlatformConstant.PlatformCodeRule.CASH_FLOW,PlatformConstant.CashFlow.CODE_LENGTH,code);
            cashFlowDto.setCode(platFromCode);
        }
        //复制属性
        CashFlow cashFlow = new CashFlow();
        FastUtils.copyProperties(cashFlowDto, cashFlow);
        SysUserVo userVo = UserUtil.getUserVo();
        cashFlow.setUpdatorId(userVo.getUserId());
        cashFlow.setUpdatorName(userVo.getName());
        cashFlow.setUpdateTime(new Date());
        cashFlow.setVersion(cashFlowDto.getVersion()+1);
        cashFlowMapper.update(cashFlow,new LambdaQueryWrapper<CashFlow>().eq(CashFlow::getId,cashFlowDto.getId()));
        return cashFlowDto.getId();
    }

    /**
     * 删除现金流量项目表
     *
     * @param cashFlowDto
     * @return int
     * @Author lj
     * @Date:17:25 2019/11/11
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"cashFlowList","cashListByStandIdAndTypeId"}, allEntries = true)
    public BatchResult delCashFlowBatch(CashFlowDto cashFlowDto) {
        return updateStatusBatch(cashFlowDto, PlatformConstant.OperateType.DELETE);
    }

    /**
     * 审核现金流量项目表
     *
     * @param cashFlowDto
     * @return com.njwd.support.BatchResult
     * @Author lj
     * @Date:13:45 2019/11/13
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"cashFlowList","cashListByStandIdAndTypeId"}, allEntries = true)
    public BatchResult approveCashFlowBatch(CashFlowDto cashFlowDto) {
        return updateStatusBatch(cashFlowDto, PlatformConstant.OperateType.APPROVED);
    }

    /**
     * 反审核现金流量项目表
     *
     * @param cashFlowDto
     * @return com.njwd.support.BatchResult
     * @Author lj
     * @Date:14:06 2019/11/13
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"cashFlowList","cashListByStandIdAndTypeId"}, allEntries = true)
    public BatchResult disApproveCashFlowBatch(CashFlowDto cashFlowDto) {
        return updateStatusBatch(cashFlowDto, PlatformConstant.OperateType.DISAPPROVED);
    }

    /**
     * 发布现金流量项目表
     *
     * @param cashFlowDto
     * @return com.njwd.support.BatchResult
     * @Author lj
     * @Date:14:40 2019/11/13
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"cashFlowList","cashListByStandIdAndTypeId"}, allEntries = true)
    public BatchResult releaseCashFlowBatch(CashFlowDto cashFlowDto) {
        return updateStatusBatch(cashFlowDto, PlatformConstant.OperateType.RELEASED);
    }

    /**
     * 批量校验
     * @Author lj
     * @Date:10:13 2019/11/13
     * @param cashFlowDto， type
     * @return com.njwd.support.BatchResult
     **/
    private BatchResult updateStatusBatch(CashFlowDto cashFlowDto, int type) {
        //初始化
        BatchResult result = new BatchResult();
        result.setFailList(new LinkedList<>());
        result.setSuccessList(new ArrayList<>());

        //查询待查询的所有数据的状态
        List<CashFlowVo> statusList = cashFlowMapper.findCashFlowListStatus(cashFlowDto);
        //如果查询结果没有数据
        if (CollectionUtils.isEmpty(statusList)) {
            for (CashFlowVo id : cashFlowDto.getChangeList()) {
                addFailResult(result, id.getId(), ResultCode.RECORD_NOT_EXIST.message);
            }
        }
        //转化为id为key的状态map
        Map<Long, CashFlowVo> statusMap = statusList.stream().collect(Collectors.toMap(CashFlowVo::getId, o -> o));

        //循环判断当前数据是否能添加
        CashFlowVo statusData;
        for (CashFlowVo changeId : cashFlowDto.getChangeList()) {
            //获取当前数据对应的状态数据
            statusData = statusMap.get(changeId.getId());
            if (statusData == null) {
                addFailResult(result, changeId.getId(), ResultCode.RECORD_NOT_EXIST.message);
                continue;
            }
            if(type==PlatformConstant.OperateType.DELETE){
                //判断 删除状态
                if (Constant.Is.YES.equals(statusData.getIsDel())) {
                    addFailResult(result, changeId.getId(), ResultCode.IS_DEL.message);
                    continue;
                }
                //判断 审核状态
                if (Constant.Is.YES.equals(statusData.getIsApproved())) {
                    addFailResult(result, changeId.getId(), ResultCode.DEL_CHECK_APPROVED.message);
                    continue;
                }
                //判断 发布状态
                if (Constant.Is.YES.equals(statusData.getIsReleased())) {
                    addFailResult(result, changeId.getId(), ResultCode.IS_RELEASED.message);
                    continue;
                }
                //校验 是否被引用
                ReferenceResult referenceResult = referenceRelationService.isReference(PlatformConstant.Reference.PLAT_CASH_FLOW, changeId.getId());
                if (referenceResult.isReference()) {
                    addFailResult(result, changeId.getId(), referenceResult.getReferenceDescription());
                    continue;
                }
            }

            if(type==PlatformConstant.OperateType.APPROVED){
                //判断 审核状态
                if (Constant.Is.YES.equals(statusData.getIsApproved())) {
                    addFailResult(result, changeId.getId(), ResultCode.APPROVE_CHECK_APPROVED.message);
                    continue;
                }
            }

            if(type==PlatformConstant.OperateType.DISAPPROVED){
                //判断 发布状态
                if (Constant.Is.YES.equals(statusData.getIsReleased())) {
                    addFailResult(result, changeId.getId(), ResultCode.DISAPPROVE_CHECK_RELEASED.message);
                    continue;
                }
                //判断 审核状态
                if (Constant.Is.NO.equals(statusData.getIsApproved())) {
                    addFailResult(result, changeId.getId(), ResultCode.DISAPPROVE_CHECK_DISAPPROVED.message);
                    continue;
                }
            }

            if(type==PlatformConstant.OperateType.RELEASED){
                //判断 审核状态
                if (Constant.Is.NO.equals(statusData.getIsApproved())) {
                    addFailResult(result, changeId.getId(), ResultCode.RELEASE_CHECK_NO_APPROVED.message);
                    continue;
                }
                //判断 发布状态
                if (Constant.Is.YES.equals(statusData.getIsReleased())) {
                    addFailResult(result, changeId.getId(), ResultCode.RELEASE_CHECK_RELEASED.message);
                    continue;
                }
            }

            //判断版本号
            if (!statusData.getVersion().equals(changeId.getVersion())) {
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
            basePlatformMapper.batchProcess(successList,type,userVo,PlatformConstant.TableName.CASH_FLOW);
            //发布
            if(type==PlatformConstant.OperateType.RELEASED){
                messageService.sendMessage(PlatformConstant.MessageType.DATA_UPDATE,cashFlowDto.getMessageDto());
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
     * @Description 查询现金流量项目表列表
     * @Author liuxiang
     * @Date:16:47 2019/7/2
     * @Param [cashFlowDto]
     * @return java.util.List<com.njwd.platform.entity.vo.CashFlowVo>
     **/
    @Override
    @Cacheable(value = "cashFlowList", key = "#cashFlowDto.accountTypeId+'-'+#cashFlowDto.accStandardId+'-'+#cashFlowDto.isBase")
    public List<CashFlowVo> findCashFlowList(CashFlowDto cashFlowDto) {
        return cashFlowMapper.findCashFlowList(cashFlowDto);
    }

    /**
     * 根据ID查询现金流量项目表
     *
     * @param cashFlowDto
     * @return com.njwd.entity.platform.vo.CashFlowVo
     * @Author lj
     * @Date:16:57 2019/11/13
     **/
    @Override
    public CashFlowVo findCashFlowById(CashFlowDto cashFlowDto) {
        return cashFlowMapper.findCashFlowById(cashFlowDto);
    }

    /**
     * 查询现金流量项目表列表分页
     * @Author lj
     * @Date:11:15 2019/11/12
     * @param cashFlowDto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.CashFlowVo>
     **/
    @Override
    //@Cacheable(value = "cashFlowListPage", key = "#cashFlowDto.accStandardId+'-'+#cashFlowDto.codeOrName+'-'+#cashFlowDto.page.current+'-'+#cashFlowDto.page.size")
    public Page<CashFlowVo> findCashFlowListPage(CashFlowDto cashFlowDto){
        Page<CashFlowVo> page =cashFlowDto.getPage();
        page = cashFlowMapper.findCashFlowListPage(page,cashFlowDto);
        return page;
    }

    /**
     * 导出
     *
     * @param cashFlowDto
     * @param response
     */
    @Override
    public void exportExcel(CashFlowDto cashFlowDto, HttpServletResponse response) {
        Page<CashFlowVo> page = cashFlowDto.getPage();
        fileService.resetPage(page);
        Page<CashFlowVo> cashFlowListPage = cashFlowMapper.findCashFlowListPage(page,cashFlowDto);
        fileService.exportExcel(response,cashFlowListPage.getRecords(),
                new ExcelColumn("code","编码"),
                new ExcelColumn("name","名称"),
                new ExcelColumn("accStandardName","会计准则"),
                new ExcelColumn("isApproved","审核状态", ExcelDataConstant.SYSTEM_DATA_IS_APPROVED),
                new ExcelColumn("isReleased","发布状态",ExcelDataConstant.SYSTEM_DATA_IS_RELEASED)
        );
    }

    /**
     * @Description 根据会计准则id和账簿类型id查询现金流量项目表列表
     * @Author lj
     * @Date:15:05 2019/6/25
     * @Param []
     * @return java.util.List<com.njwd.platform.entity.vo.CashFlowVo>
     **/
    @Override
    @Cacheable(value = "cashListByStandIdAndTypeId", key = "#cashFlowDto.accountTypeId+'-'+#cashFlowDto.accStandardId+'-'+#cashFlowDto.isBase")
    public List<CashFlowVo> findCashListByStandIdAndTypeId(CashFlowDto cashFlowDto) {
        return cashFlowMapper.findCashListByStandIdAndTypeId(cashFlowDto);
    }
}
