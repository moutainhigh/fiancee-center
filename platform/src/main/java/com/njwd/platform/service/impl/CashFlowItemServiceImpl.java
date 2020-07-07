package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.njwd.common.Constant;
import com.njwd.common.ExcelDataConstant;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.ReferenceResult;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.platform.CashFlow;
import com.njwd.entity.platform.CashFlowItem;
import com.njwd.entity.platform.dto.CashFlowItemDto;
import com.njwd.entity.platform.vo.CashFlowItemVo;
import com.njwd.entity.platform.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.fileexcel.export.DataGet;
import com.njwd.platform.mapper.BasePlatformMapper;
import com.njwd.platform.mapper.CashFlowItemMapper;
import com.njwd.platform.mapper.CashFlowMapper;
import com.njwd.platform.service.CashFlowItemService;
import com.njwd.platform.service.MessageService;
import com.njwd.platform.utils.UserUtil;
import com.njwd.service.FileService;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.BatchResult;
import com.njwd.utils.FastUtils;
import com.njwd.utils.StringUtil;
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
 * @Description 现金流量项目
 * @Date:16:30 2019/6/13
 **/
@Service
public class CashFlowItemServiceImpl extends ServiceImpl<CashFlowItemMapper, CashFlowItem> implements CashFlowItemService{

    @Autowired
    private CashFlowItemMapper cashFlowItemMapper;

    @Autowired
    private CashFlowMapper cashFlowMapper;

    @Resource
    private BasePlatformMapper basePlatformMapper;

    @Resource
    private FileService fileService;

    @Resource
    private ReferenceRelationService referenceRelationService;

    @Resource
    private MessageService messageService;

    /**
     * 添加现金流量项目
     *
     * @param cashFlowItemDto
     * @return int
     * @Author lj
     * @Date:10:52 2019/11/14
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"cashFlowItemList","cashFlowItemPage"}, allEntries = true)
    public Long addCashFlowItem(CashFlowItemDto cashFlowItemDto) {
        //获取上级项目和级次
        String upCode =cashFlowItemDto.getUpCode();
        Byte level;
        Byte add = Constant.Number.INITLIZED;
        String fullName;
        Long upId = null;
        Integer upVersion = null;
        if(StringUtil.isEmpty(upCode)){
            level=Constant.Number.INITLIZED;
            //校验编码不能以00结尾
            String code = cashFlowItemDto.getCode();
            fullName = cashFlowItemDto.getName();
            if (code.length()!=Constant.Number.ONE||!FastUtils.match(PlatformConstant.CashFlowItem.PATTERN,code)){
                throw new ServiceException(ResultCode.CASH_FLOW_ITEM_CHECK_CODE_FIRST);
            }
        }else {
            //查询上级项目的级次
            CashFlowItem cashFlowItem = cashFlowItemMapper.selectOne(new LambdaQueryWrapper<CashFlowItem>()
                    .eq(CashFlowItem::getCashFlowId,cashFlowItemDto.getCashFlowId())
                    .eq(CashFlowItem::getIsDel,Constant.Number.ZERO)
                    .eq(CashFlowItem::getCode,upCode));
            level=(byte)(cashFlowItem.getLevel()+add);
            fullName = cashFlowItem.getFullName()+Constant.Character.UNDER_LINE+cashFlowItemDto.getName();
            upId = cashFlowItem.getId();
            upVersion = cashFlowItem.getVersion();
            //校验上级数据的数据状态
            if(cashFlowItem!=null){
                if (Constant.Is.NO.equals(cashFlowItem.getIsApproved())) {
                    throw new ServiceException(ResultCode.DATA_UN_APPROVED);
                }
            }
            //校验编码格式校验
            String code = cashFlowItemDto.getCode();
            if(code.length()<2){
                throw new ServiceException(ResultCode.CODING_FORMAT_NO_RIGHT);
            }
            String newCode = code.substring(code.length() -2);
            if (!code.equals(upCode+newCode)||Constant.Character.ZERO.equals(newCode)||!FastUtils.match(PlatformConstant.CashFlowItem.PATTERN_TWO,newCode)){
                throw new ServiceException(ResultCode.CODING_FORMAT_NO_RIGHT);
            }
        }

        checkMaxLevel(cashFlowItemDto, level, fullName);

        //校验唯一性
        checkCashFlowItemUniqueness(cashFlowItemDto);
        //更新上级的isFinal字段
        if(StringUtil.isNotEmpty(upCode)){
            //将上级末级字段设置为0（非末级）
            CashFlowItem item = new CashFlowItem();
            item.setIsFinal(Constant.Is.NO);
            //版本号加1
            item.setVersion(upVersion);
            //特殊处理
            cashFlowItemMapper.update(item, new LambdaQueryWrapper<CashFlowItem>().eq(CashFlowItem::getId, upId));
        }
        //复制属性
        CashFlowItem cashFlowItem = new CashFlowItem();
        FastUtils.copyProperties(cashFlowItemDto, cashFlowItem);
        SysUserVo userVo = UserUtil.getUserVo();
        cashFlowItem.setCreatorId(userVo.getUserId());
        cashFlowItem.setCreatorName(userVo.getName());
        cashFlowItem.setCreateTime(new Date());
        cashFlowItem.setUpdateTime(null);
        cashFlowItemMapper.insert(cashFlowItem);
        return cashFlowItem.getId();
    }

    /**
     * 删除现金流量项目
     *
     * @param cashFlowItemDto
     * @return com.njwd.support.BatchResult
     * @Author lj
     * @Date:16:55 2019/11/14
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"cashFlowItemList","cashFlowItemPage"}, allEntries = true)
    public BatchResult delCashFlowItemBatch(CashFlowItemDto cashFlowItemDto) {
        return updateStatusBatch(cashFlowItemDto, PlatformConstant.OperateType.DELETE);
    }

    /**
     * 批量校验
     * @Author lj
     * @Date:10:13 2019/11/13
     * @param cashFlowItemDto， type
     * @return com.njwd.support.BatchResult
     **/
    private BatchResult updateStatusBatch(CashFlowItemDto cashFlowItemDto, int type) {
        //初始化
        BatchResult result = new BatchResult();
        result.setFailList(new LinkedList<>());
        result.setSuccessList(new ArrayList<>());

        //查询待查询的所有数据的状态
        List<CashFlowItemVo> statusList = cashFlowItemMapper.findCashFlowItemListStatus(cashFlowItemDto);
        //如果查询结果没有数据
        if (CollectionUtils.isEmpty(statusList)) {
            for (CashFlowItemVo id : cashFlowItemDto.getChangeList()) {
                addFailResult(result, id.getId(), ResultCode.RECORD_NOT_EXIST.message);
            }
        }
        //转化为id为key的状态map
        Map<Long, CashFlowItemVo> statusMap = statusList.stream().collect(Collectors.toMap(CashFlowItemVo::getId, o -> o));

        //循环判断当前数据是否能添加
        CashFlowItemVo statusData;
        //创建对象updateUp用于修改上级为末级
        CashFlowItemDto updateUp;
        Set<Long> needReleaseIds = new LinkedHashSet<>();
        Map<Long,CashFlowItemVo> cashFlowItemVoMap= new LinkedHashMap<>();
        for (CashFlowItemVo change : cashFlowItemDto.getChangeList()) {
            Long changeId=change.getId();
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
                //判断 发布状态
                if (Constant.Is.YES.equals(statusData.getIsReleased())) {
                    addFailResult(result, changeId, ResultCode.IS_RELEASED.message);
                    continue;
                }
                //判断当前数据是否有下级现金流量项目
                if (Constant.Is.NO.equals(statusData.getIsFinal())) {
                    addFailResult(result, changeId, ResultCode.CASH_FLOW_ITEM_CHECK_FINAL.message);
                    continue;
                }
                //判断 审核状态
                if (Constant.Is.YES.equals(statusData.getIsApproved())) {
                    addFailResult(result, changeId, ResultCode.DEL_CHECK_APPROVED.message);
                    continue;
                }
                // 校验 是否被引用
                ReferenceResult referenceResult = referenceRelationService.isReference(PlatformConstant.Reference.PLAT_CASH_FLOW_ITEM, changeId);
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
                //判断当前数据是否有下级现金流量项目
                if (Constant.Is.NO.equals(statusData.getIsFinal())) {
                    addFailResult(result, changeId, ResultCode.CASH_FLOW_ITEM_CHECK_FINAL_DISAPPROVE.message);
                    continue;
                }
            }

            Boolean flag=true;
            List<Long> relationIds = new LinkedList<>();
            if(type==PlatformConstant.OperateType.RELEASED){
                //判断 发布状态
                if (Constant.Is.YES.equals(statusData.getIsReleased())) {
                    addFailResult(result, changeId, ResultCode.RELEASE_CHECK_RELEASED.message);
                    continue;
                }
                if(Constant.Is.NO.equals(statusData.getIsApproved())){
                    addFailResult(result, changeId, ResultCode.CASH_FLOW_ITEM_CHECK_DISAPPROVED.message);
                    continue;
                }
                //根据code和cashFlowId查询所有下级数据
                CashFlowItemDto itemDto = new CashFlowItemDto();
                itemDto.setIsReleased(Constant.Number.ANTI_INITLIZED);
                itemDto.setCashFlowId(statusData.getCashFlowId());
                List<CashFlowItemVo> cashFlowItemVoList =cashFlowItemMapper.findAllCashFlowItemByCode(itemDto);
                //检查所选的现金流量项目的所有的上下级项目中是否有未审核的现金流量项目
                for(CashFlowItemVo cashFlowItemVo:cashFlowItemVoList){
                    if(changeId.equals(cashFlowItemVo.getId())){
                        relationIds.add(cashFlowItemVo.getId());
                        cashFlowItemVoMap.put(cashFlowItemVo.getId(),cashFlowItemVo);
                        continue;
                    }
                    if (cashFlowItemVo.getCode().startsWith(statusData.getCode()) || statusData.getCode().startsWith(cashFlowItemVo.getCode())) {
                        if(Constant.Is.NO.equals(cashFlowItemVo.getIsApproved())){
                            flag=false;
                        }
                        cashFlowItemVoMap.put(cashFlowItemVo.getId(),cashFlowItemVo);
                        relationIds.add(cashFlowItemVo.getId());
                    }
                }
                if (flag) {
                    needReleaseIds.addAll(relationIds);
                }else {
                    for(Map.Entry<Long,CashFlowItemVo> entry:cashFlowItemVoMap.entrySet()){
                        CashFlowItemVo cashFlowItemVo =entry.getValue();
                        ReferenceDescription fd = new ReferenceDescription();
                        fd.setBusinessId(cashFlowItemVo.getId());
                        fd.setReferenceDescription(ResultCode.CASH_FLOW_ITEM_CHECK_DISAPPROVED.message);
                        result.getFailList().add(fd);
                    }
                }
            }

            //判断版本号
            if (!statusData.getVersion().equals(change.getVersion())) {
                addFailResult(result, statusData.getId(), ResultCode.VERSION_ERROR.message);
                continue;
            }
            //删除成功判断将上级设置为末级
            updateUp = new CashFlowItemDto();
            if(type==PlatformConstant.OperateType.DELETE){
                if (statusData.getUpCode() != null && !Constant.Character.STRING_ZERO.equals(statusData.getUpCode())){
                    //查询上级
                    CashFlowItemDto cashFlowItemDto1 = new CashFlowItemDto();
                    cashFlowItemDto1.setCashFlowId(statusData.getCashFlowId());
                    cashFlowItemDto1.setUpCode(statusData.getUpCode());
                    CashFlowItem cashFlowItem = cashFlowItemMapper.selectOne(new LambdaQueryWrapper<CashFlowItem>()
                            .eq(CashFlowItem::getCashFlowId,cashFlowItemDto1.getCashFlowId())
                            .eq(CashFlowItem::getCode,cashFlowItemDto1.getUpCode())
                            .eq(CashFlowItem::getIsDel,Constant.Number.ZERO)
                    );
                    updateUp.setCashFlowId(statusData.getCashFlowId());
                    updateUp.setUpCode(statusData.getUpCode());
                    updateUp.setVersion(cashFlowItem.getVersion());
                    updateUp.setId(statusData.getId());
                    checkIsNeedUpdateFinal(updateUp);
                }
            }

            //通过验证的数据
            if(flag){
                result.getSuccessList().add(statusData.getId());
            }
        }
        SysUserVo userVo = UserUtil.getUserVo();
        List<Long> successList = result.getSuccessList();
        if(CollectionUtils.isNotEmpty(successList)){
            if(type!=PlatformConstant.OperateType.RELEASED){
                basePlatformMapper.batchProcess(successList,type,userVo,PlatformConstant.TableName.CASH_FLOW_ITEM);
            }
        }
        //发布
        if(type==PlatformConstant.OperateType.RELEASED){
            if(CollectionUtils.isNotEmpty(needReleaseIds)){
                basePlatformMapper.batchProcess(needReleaseIds,type,userVo,PlatformConstant.TableName.CASH_FLOW_ITEM);
                messageService.sendMessage(PlatformConstant.MessageType.DATA_UPDATE,cashFlowItemDto.getMessageDto());
                ReferenceDescription description;
                LinkedList<ReferenceDescription> successDetailsList = new LinkedList<>();
                for (Long needReleaseId : needReleaseIds) {
                    CashFlowItemVo cashFlowItemVo = cashFlowItemVoMap.get(needReleaseId);
                    description = new ReferenceDescription<>();
                    description.setBusinessId(needReleaseId);
                    description.setInfo(cashFlowItemVo);
                    successDetailsList.add(description);
                }
                result.setSuccessDetailsList(successDetailsList);
            }
            List<ReferenceDescription> failList =result.getFailList();
            if(CollectionUtils.isNotEmpty(failList)){
                //去重复
                failList = failList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(()->new TreeSet<>(Comparator.comparing(ReferenceDescription::getBusinessId))), ArrayList::new));
                Map<Long, CashFlowItem> failDict = list(new LambdaQueryWrapper<CashFlowItem>()
                        .in(BaseModel::getId, failList.stream().map(ReferenceDescription::getBusinessId).collect(Collectors.toList())))
                        .stream().collect(Collectors.toMap(BaseModel::getId, v -> v));
                result.setFailList(failList);
                result.setFailDict(failDict);
            }
        }
        return result;
    }

    /**
     * @Author Libao
     * @Description 校验是否需要跟新末级
     * @Date  2019/9/6 9:43
     * @Param [cashFlowItemDto]
     * @return void
     */
    public void checkIsNeedUpdateFinal(CashFlowItemDto cashFlowItemDto) {
        List<CashFlowItem> cashFlowItemList = cashFlowItemMapper.selectList(new LambdaQueryWrapper<CashFlowItem>()
                .eq(CashFlowItem::getCashFlowId,cashFlowItemDto.getCashFlowId())
                .eq(CashFlowItem::getUpCode,cashFlowItemDto.getUpCode())
                .eq(CashFlowItem::getIsDel,Constant.Number.ZERO)
                .ne(CashFlowItem::getId,cashFlowItemDto.getId())
        );
        //判断上级是否存在下级
        if (cashFlowItemList == null || cashFlowItemList.isEmpty()) {
            //不存在下级则跟新上级为末级
            CashFlowItem cashFlowItemUp = new CashFlowItem();
            cashFlowItemUp.setIsFinal(Constant.Is.YES);
            cashFlowItemUp.setVersion(cashFlowItemDto.getVersion());
            cashFlowItemMapper.update(cashFlowItemUp, new LambdaQueryWrapper<CashFlowItem>()
                    .eq(CashFlowItem::getCode, cashFlowItemDto.getUpCode())
                    .eq(CashFlowItem::getCashFlowId, cashFlowItemDto.getCashFlowId())
                    .eq(CashFlowItem::getIsDel,Constant.Number.ZERO)
            );
        }
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
     * 修改现金流量项目
     *
     * @param cashFlowItemDto
     * @return int
     * @Author lj
     * @Date:15:02 2019/11/14
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"cashFlowItemList","cashFlowItemPage"}, allEntries = true)
    public Long updateCashFlowItem(CashFlowItemDto cashFlowItemDto) {
        //校验数据是否已审核
        CashFlowItem cash = cashFlowItemMapper.selectOne(new LambdaQueryWrapper<CashFlowItem>().eq(CashFlowItem::getId,cashFlowItemDto.getId()));
        if(cash!=null){
            if (Constant.Is.YES.equals(cash.getIsApproved())) {
                throw new ServiceException(ResultCode.IS_APPROVED);
            }
        }
        //版本校验
        checkVersion(cashFlowItemDto);
        //获取上级项目和级次
        String upCode =cashFlowItemDto.getUpCode();
        Byte level;
        Byte add = Constant.Number.INITLIZED;
        String fullName;
        if(StringUtil.isEmpty(upCode)){
            level=Constant.Number.INITLIZED;
            //校验编码不能以00结尾
            String code = cashFlowItemDto.getCode();
            fullName = cashFlowItemDto.getName();
            if (code.length()!=Constant.Number.ONE||!FastUtils.match(PlatformConstant.CashFlowItem.PATTERN,code)){
                throw new ServiceException(ResultCode.CASH_FLOW_ITEM_CHECK_CODE_FIRST);
            }
        }else {
            //查询上级项目的级次
            CashFlowItem cashFlowItem = cashFlowItemMapper.selectOne(new LambdaQueryWrapper<CashFlowItem>()
                    .eq(CashFlowItem::getCashFlowId,cashFlowItemDto.getCashFlowId())
                    .eq(CashFlowItem::getIsDel, Constant.Number.ZERO)
                    .eq(CashFlowItem::getCode,upCode));
            level=(byte)(cashFlowItem.getLevel()+add);
            fullName = cashFlowItem.getFullName()+Constant.Character.UNDER_LINE+cashFlowItemDto.getName();
            //校验编码不能以00结尾
            String code = cashFlowItemDto.getCode();
            String newCode = code.substring(code.length() -2);
            if (!code.equals(upCode+newCode)||!FastUtils.match(PlatformConstant.CashFlowItem.PATTERN_TWO,newCode)){
                throw new ServiceException(ResultCode.CASH_FLOW_ITEM_CHECK_CODE);
            }
        }
        //检验编码和名称是否重复
        checkCashFlowItemForUpdate(cashFlowItemDto);

        //校验最大级次
        checkMaxLevel(cashFlowItemDto, level, fullName);
        //复制属性
        CashFlowItem cashFlowItem = new CashFlowItem();
        FastUtils.copyProperties(cashFlowItemDto, cashFlowItem);
        SysUserVo userVo = UserUtil.getUserVo();
        cashFlowItem.setUpdatorId(userVo.getUserId());
        cashFlowItem.setUpdatorName(userVo.getName());
        cashFlowItem.setUpdateTime(new Date());
        cashFlowItem.setVersion(cashFlowItemDto.getVersion());
        //如果老的upCode等于新的upCode，则没有切换上级，否则为切换上级，走新增流程
        if (cashFlowItemDto.getUpCode()!=null&&!cashFlowItemDto.getUpCode().equals(cashFlowItemDto.getOldUpCode())){
            //查询上级项目根据upCode
            CashFlowItem cashFlowItemVoUp = cashFlowItemMapper.selectOne(new LambdaQueryWrapper<CashFlowItem>()
                    .eq(CashFlowItem::getCashFlowId,cashFlowItemDto.getCashFlowId())
                    .eq(CashFlowItem::getIsDel,Constant.Number.ZERO)
                    .eq(CashFlowItem::getCode,upCode));
            //开始更新
            cashFlowItemMapper.update(cashFlowItem, new LambdaQueryWrapper<CashFlowItem>().eq(CashFlowItem::getId, cashFlowItemDto.getId()));
            //跟新上级为非末级
            cashFlowItemVoUp.setIsFinal(Constant.Is.NO);
            cashFlowItemVoUp.setVersion(cashFlowItemDto.getVersion());
            cashFlowItemMapper.update(cashFlowItemVoUp, new LambdaQueryWrapper<CashFlowItem>().eq(CashFlowItem::getId, cashFlowItemVoUp.getId()));
            //新建老的Dto用于校验老的上级项目是否存在下级
            CashFlowItemDto cashFlowItemDtoOld = new CashFlowItemDto();
            cashFlowItemDtoOld.setCashFlowId(cashFlowItemDto.getCashFlowId());
            cashFlowItemDtoOld.setUpCode(cashFlowItemDto.getOldUpCode());
            //判断原上级项目是否存在其他下级，不存在则跟新末级为是
            int downCount = cashFlowItemMapper.findIsExistNextCashFlowItem(cashFlowItemDtoOld);
            //不存在下级则跟新老的上级为末级
            if (downCount == 0) {
                CashFlowItem cashFlowItemOld = new CashFlowItem();
                cashFlowItemOld.setIsFinal(Constant.Is.YES);
                cashFlowItemOld.setCode(cashFlowItemDto.getOldUpCode());
                cashFlowItemOld.setCashFlowId(cashFlowItemDto.getCashFlowId());
                cashFlowItemOld.setVersion(cashFlowItemDto.getVersion());
                cashFlowItemMapper.update(cashFlowItemOld,new LambdaQueryWrapper<CashFlowItem>()
                        .eq(CashFlowItem::getCode, cashFlowItemOld.getCode())
                        .eq(CashFlowItem::getCashFlowId,cashFlowItemOld.getCashFlowId())
                        .eq(CashFlowItem::getIsDel,Constant.Number.ZERO));
            }
        }else {
            //没有更换上级，直接更新
            cashFlowItemMapper.update(cashFlowItem, new LambdaQueryWrapper<CashFlowItem>().eq(CashFlowItem::getId, cashFlowItemDto.getId()));
        }
        return cashFlowItemDto.getId();
    }

    /**
     * 版本号校验
     * @Author lj
     * @Date:16:26 2019/11/14
     * @param cashFlowItemDto
     * @return void
     **/
    @Override
    public void checkVersion(CashFlowItemDto cashFlowItemDto){
        Long id = cashFlowItemMapper.checkVersion(cashFlowItemDto);
        if (id != null){
            throw new ServiceException(ResultCode.CASH_FLOW_ITEM_CHECK_VERSION);
        }
    }

    /**
     * 审核现金流量项目
     *
     * @param cashFlowItemDto
     * @return com.njwd.support.BatchResult
     * @Author lj
     * @Date:9:07 2019/11/15
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResult approveCashFlowItemBatch(CashFlowItemDto cashFlowItemDto) {
        return updateStatusBatch(cashFlowItemDto, PlatformConstant.OperateType.APPROVED);
    }

    /**
     * 反审核现金流量项目
     *
     * @param cashFlowItemDto
     * @return com.njwd.support.BatchResult
     * @Author lj
     * @Date:9:08 2019/11/15
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"cashFlowItemList","cashFlowItemPage"}, allEntries = true)
    public BatchResult disApproveCashFlowItemBatch(CashFlowItemDto cashFlowItemDto) {
        return updateStatusBatch(cashFlowItemDto, PlatformConstant.OperateType.DISAPPROVED);
    }

    /**
     * 发布现金流量项目
     *
     * @param cashFlowItemDto
     * @return com.njwd.support.BatchResult
     * @Author lj
     * @Date:9:09 2019/11/15
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"cashFlowItemList","cashFlowItemPage"}, allEntries = true)
    public BatchResult releaseCashFlowItemBatch(CashFlowItemDto cashFlowItemDto) {
        return updateStatusBatch(cashFlowItemDto, PlatformConstant.OperateType.RELEASED);
    }

    /**
     * 根据ID查询现金流量项目
     *
     * @param cashFlowItemDto
     * @return com.njwd.entity.platform.vo.CashFlowVo
     * @Author lj
     * @Date:16:57 2019/11/13
     **/
    @Override
    public CashFlowItemVo findCashFlowItemById(CashFlowItemDto cashFlowItemDto) {
        return cashFlowItemMapper.findCashFlowItemById(cashFlowItemDto);
    }

    /**
     * 校验最大级次
     * @Author lj
     * @Date:15:42 2019/11/14
     * @param cashFlowItemDto, level, fullName
     * @return void
     **/
    private void checkMaxLevel(CashFlowItemDto cashFlowItemDto, Byte level, String fullName) {
        cashFlowItemDto.setLevel(level);
        cashFlowItemDto.setFullName(fullName);
        //查询项目表里的最大级次
        CashFlow cashFlow = cashFlowMapper.selectOne(new LambdaQueryWrapper<CashFlow>()
                .eq(CashFlow::getId, cashFlowItemDto.getCashFlowId())
                .eq(CashFlow::getIsDel, Constant.Number.ZERO)
        );
        Byte maxLevelNum = cashFlow.getMaxLevelNum();
        //校验最大级次
        if (cashFlowItemDto.getLevel() > maxLevelNum) {
            throw new ServiceException(ResultCode.CASH_FLOW_ITEM_MAX_LEVEL);
        }
    }

    private void checkCashFlowItemForUpdate(CashFlowItemDto cashFlowItemDto) {
        int row = 0;
        if (!cashFlowItemDto.getOldName().equals(cashFlowItemDto.getName())) {
            row = cashFlowItemMapper.selectCount(new LambdaQueryWrapper<CashFlowItem>()
                    .eq(CashFlowItem::getName, cashFlowItemDto.getName())
                    .eq(CashFlowItem::getCashFlowId, cashFlowItemDto.getCashFlowId())
                    .eq(CashFlowItem::getLevel, cashFlowItemDto.getLevel())
                    .eq(CashFlowItem::getIsDel, Constant.Number.ZERO));
            if (row != 0) {
                throw new ServiceException(ResultCode.NAME_EXIST);
            }
        }
        if (!cashFlowItemDto.getOldCode().equals(cashFlowItemDto.getCode())) {
            row = cashFlowItemMapper.selectCount(new LambdaQueryWrapper<CashFlowItem>()
                    .eq(CashFlowItem::getCode, cashFlowItemDto.getCode())
                    .eq(CashFlowItem::getCashFlowId, cashFlowItemDto.getCashFlowId())
                    .eq(CashFlowItem::getIsDel,Constant.Number.ZERO));
            if (row != 0) {
                throw new ServiceException(ResultCode.CODE_EXIST);
            }
        }
    }

    /**
     * @Author Libao
     * @Description 校验现金流量项目编码/名称是否重复
     * @Date 2019/6/12 14:10
     * @Param [cashFlowItemDto]
     */
    private void checkCashFlowItemUniqueness(CashFlowItemDto cashFlowItemDto) {
        Integer row = cashFlowItemMapper.selectCount(new LambdaQueryWrapper<CashFlowItem>()
                .eq(CashFlowItem::getCode, cashFlowItemDto.getCode())
                .eq(CashFlowItem::getCashFlowId, cashFlowItemDto.getCashFlowId())
                .eq(CashFlowItem::getIsDel, Constant.Number.ZERO));
        if (row != 0) {
            throw new ServiceException(ResultCode.CASH_FLOW_ITEM_CODE_EXIST);
        }
        row = cashFlowItemMapper.selectCount(new LambdaQueryWrapper<CashFlowItem>()
                .eq(CashFlowItem::getName, cashFlowItemDto.getName())
                .eq(CashFlowItem::getCashFlowId, cashFlowItemDto.getCashFlowId())
                .eq(CashFlowItem::getLevel, cashFlowItemDto.getLevel())
                .eq(CashFlowItem::getIsDel,Constant.Number.ZERO));
        if (row != 0) {
            throw new ServiceException(ResultCode.CASH_FLOW_ITEM_NAME_EXIST);
        }
    }
    /**
     * @Description 查询现金流量项目列表
     * @Author liuxiang
     * @Date:16:47 2019/7/2
     * @Param [cashFlowItemDto]
     * @return java.util.List<com.njwd.platform.entity.vo.CashFlowItemVo>
     **/
    @Override
    @Cacheable(value = "cashFlowItemList", key = "#cashFlowItemDto.cashFlowId+'-'+#cashFlowItemDto.rootEnterpriseId+'-'+#cashFlowItemDto.cashFlowDirection+'-'+#cashFlowItemDto.codeOrName")
    public List<CashFlowItemVo> findCashFlowItemList(CashFlowItemDto cashFlowItemDto) {
        return cashFlowItemMapper.findCashFlowItemList(cashFlowItemDto);
    }

    /**
     * @Description 查询现金流量项目分页
     * @Author liuxiang
     * @Date:16:47 2019/7/2
     * @Param [cashFlowItemDto]
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.platform.entity.vo.CashFlowItemVo>
     **/
    @Override
    @Cacheable(value = "cashFlowItemPage", key = "#cashFlowItemDto.cashFlowId+'-'+#cashFlowItemDto.rootEnterpriseId+'-'+#cashFlowItemDto.page.current+'-'+#cashFlowItemDto.page.size")
    public Page<CashFlowItemVo> findCashFlowItemPage(CashFlowItemDto cashFlowItemDto) {
        Page<CashFlowItemVo> page= cashFlowItemDto.getPage();
        page=cashFlowItemMapper.findCashFlowItemPage(page,cashFlowItemDto);
        return page;
    }

    /**
     * 查询现金流量项目分页
     *
     * @param cashFlowItemDto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.CashFlowItemVo>
     * @Author lj
     * @Date:17:45 2019/11/13
     **/
    @Override
    public Page<CashFlowItemVo> findCashFlowItemPageNew(CashFlowItemDto cashFlowItemDto) {
        Page<CashFlowItemVo> page= cashFlowItemDto.getPage();
        page=cashFlowItemMapper.findCashFlowItemPageNew(page,cashFlowItemDto);
        return page;
    }

    /**
     * 查询现金流量项目列表-初始化基础资料
     * @Author lj
     * @Date:17:29 2019/12/2
     * @param cashFlowItemDto
     * @return java.util.List<com.njwd.entity.platform.vo.CashFlowItemVo>
     **/
    @Override
    public List<CashFlowItemVo> findCashFlowItemNew(CashFlowItemDto cashFlowItemDto){
        Page<CashFlowItemVo> page= cashFlowItemDto.getPage();
        page.setCurrent(1);
        page.setSearchCount(false);
        page.setSize(DataGet.MAX_PAGE_SIZE);
        page=cashFlowItemMapper.findCashFlowItemPageNew(page,cashFlowItemDto);
        return page.getRecords();
    }

    /**
     * 导出
     *
     * @param cashFlowItemDto
     * @param response
     */
    @Override
    public void exportExcel(CashFlowItemDto cashFlowItemDto, HttpServletResponse response) {
        Page<CashFlowItemVo> page = cashFlowItemDto.getPage();
        fileService.resetPage(page);
        Page<CashFlowItemVo> cashFlowItemPageNew = cashFlowItemMapper.findCashFlowItemPageNew(page,cashFlowItemDto);
        fileService.exportExcel(response,cashFlowItemPageNew.getRecords(),
                new ExcelColumn("code","编码"),
                new ExcelColumn("name","名称"),
                new ExcelColumn("cashFlowDirection","方向",ExcelDataConstant.SYSTEM_DATA_CASH_FLOW_DIRECTION),
                new ExcelColumn("upName","上级项目"),
                new ExcelColumn("cashFlowName","现金流量项目表"),
                new ExcelColumn("isApproved","审核状态", ExcelDataConstant.SYSTEM_DATA_IS_APPROVED),
                new ExcelColumn("isReleased","发布状态",ExcelDataConstant.SYSTEM_DATA_IS_RELEASED)
        );
    }

}
