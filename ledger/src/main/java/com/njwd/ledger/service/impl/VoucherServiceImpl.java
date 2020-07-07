package com.njwd.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.njwd.common.Constant;
import com.njwd.common.LedgerConstant;
import com.njwd.common.LogConstant;
import com.njwd.common.MenuCodeConstant;
import com.njwd.entity.base.ManagerInfo;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.dto.SequenceDto;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.*;
import com.njwd.entity.ledger.dto.*;
import com.njwd.entity.ledger.vo.*;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.exception.FeignClientErrorMsg;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.ledger.cloudclient.AccountSubjectFeignClient;
import com.njwd.ledger.cloudclient.PublicDataFeignClient;
import com.njwd.ledger.mapper.AccountBookPeriodMapper;
import com.njwd.ledger.mapper.VoucherEntryAuxiliaryMapper;
import com.njwd.ledger.mapper.VoucherEntryMapper;
import com.njwd.ledger.mapper.VoucherMapper;
import com.njwd.ledger.service.*;
import com.njwd.ledger.utils.LedgerUtils;
import com.njwd.service.FileService;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/07/25
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements VoucherService {
    private final static Logger LOGGER = LoggerFactory.getLogger(VoucherServiceImpl.class);
    @Resource
    private VoucherMapper voucherMapper;
    @Resource
    private VoucherEntryService voucherEntryService;
    @Resource
    private VoucherEntryAuxiliaryService voucherEntryAuxiliaryService;
    @Resource
    private VoucherEntryCashFlowService voucherEntryCashFlowService;
    @Resource
    private BalanceCashFlowService balanceCashFlowService;
    @Resource
    private BalanceSubjectService balanceSubjectService;
    @Resource
    private BalanceSubjectAuxiliaryService balanceSubjectAuxiliaryService;
    @Resource
    private BalanceSubjectAuxiliaryItemService balanceSubjectAuxiliaryItemService;
    @Resource
    private VoucherEntryInteriorService voucherEntryInteriorService;
    @Resource
    private AccountBookPeriodMapper accountBookPeriodMapper;
    @Resource
    private PublicDataFeignClient publicDataFeignClient;
    @Resource
    private AccountSubjectFeignClient accountSubjectFeignClient;
    @Resource
    private AuxiliaryAccountingService auxiliaryAccountingService;
    @Resource
    private BalanceCashFlowInitService balanceCashFlowInitService;
    @Resource
    private CommonService commonService;
    @Resource
    private FileService fileService;
    @Resource
    private VoucherEntryAuxiliaryMapper voucherEntryAuxiliaryMapper;
    @Resource
    private VoucherEntryMapper voucherEntryMapper;

    /**
     * 暂存
     *
     * @param voucherDto voucherDto
     * @return java.lang.Long
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:19
     **/
    @Override
    public Long draft(VoucherDto voucherDto) {
        SysUserVo operator = UserUtils.getUserVo();
        ParameterSetVo parameterSet = getParameterSet(operator);
        checkParamValid(voucherDto, parameterSet);
        voucherDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        Long voucherId = voucherDto.getId();
        if (voucherId != null) {
            // 修改
            Voucher existVoucher = voucherMapper.selectById(voucherId);
            FastUtils.checkNull(existVoucher);
            checkStatus(existVoucher, ResultCode.ONLY_DRAFT, LedgerConstant.VoucherStatus.DRAFT);
            checkEditOtherSet(operator, parameterSet, existVoucher);
            checkVersion(existVoucher, voucherDto);
            if (!voucherDto.getEditEntryList().isEmpty()) {
                LambdaQueryWrapper<VoucherEntry> entryWrapper = new LambdaQueryWrapper<VoucherEntry>().eq(VoucherEntry::getVoucherId, voucherId);
                LambdaQueryWrapper<VoucherEntryAuxiliary> auxiliaryWrapper = new LambdaQueryWrapper<VoucherEntryAuxiliary>().eq(VoucherEntryAuxiliary::getVoucherId, voucherId);
                LambdaQueryWrapper<VoucherEntryCashFlow> cashFlowWrapper = new LambdaQueryWrapper<VoucherEntryCashFlow>().eq(VoucherEntryCashFlow::getVoucherId, voucherId);
                if (voucherDto.getEditEntryList().get(0).getId() != null && 0 == voucherDto.getEditEntryList().get(0).getId()) {
                    // 首条记录id为0时删除所有分录+核算明细+现金流量分析明细
                    voucherEntryService.remove(entryWrapper);
                    voucherEntryAuxiliaryService.remove(auxiliaryWrapper);
                    voucherEntryCashFlowService.remove(cashFlowWrapper);
                } else {
                    // 根据序号查找分录对象
                    Map<Integer, VoucherEntry> rowNumEntryDict = new HashMap<>();
                    fillVoucherEntries(voucherDto, rowNumEntryDict);
                    List<Long> unchangedIds = new LinkedList<>();
                    voucherEntryService.insertOrUpdateBatch(voucherDto.getEditEntryList(), voucherId, unchangedIds, entryWrapper);
                    // 分录核算明细 删除再插入
                    updateEntryAuxiliaryAndCashFlow(null, voucherDto, voucherId, unchangedIds, auxiliaryWrapper, cashFlowWrapper, rowNumEntryDict, null);
                }
            }
            fillEditVoucherInfo(voucherDto, operator, existVoucher, null, null);
            voucherMapper.updateById(existVoucher);
        } else {
            // 新增
            FastUtils.checkParams(voucherDto.getAccountBookEntityId(), voucherDto.getVoucherDate());
            Voucher voucher = buildNewVoucherInfo(voucherDto, operator, LedgerConstant.VoucherStatus.DRAFT);
            voucherMapper.insert(voucher);
            voucherId = voucher.getId();
            if (!voucherDto.getEditEntryList().isEmpty()) {
                // 插入分录+辅助核算
                Map<Integer, VoucherEntry> rowNumEntryDict = insertEntryAndAuxiliary(voucherDto, voucherId);
                // 插入现金流量
                List<VoucherEntryCashFlow> saveCashFlowList = getVoucherEntryCashFlows(voucherDto, voucherId, rowNumEntryDict, null, null);
                if (!saveCashFlowList.isEmpty()) {
                    voucherEntryCashFlowService.saveBatch(saveCashFlowList);
                }
            }
        }
        return voucherId;
    }

    /**
     * 保存
     *
     * @param voucherDto voucherDto
     * @return java.lang.Long 主键
     * @author xyyxhcj@qq.com
     * @date 2019/7/26 11:24
     **/
    @Override
    public Long save(VoucherDto voucherDto) {
        SysUserVo operator = UserUtils.getUserVo();
        // 获取总账参数校验
        ParameterSetVo parameterSet = getParameterSet(operator);
        checkParamValid(voucherDto, parameterSet);
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        // 记录需要现金流量分析的分录
        List<VoucherEntryDto> needCashFlows = new LinkedList<>();
        // 存储内部往来准备生成的所有凭证
        final List<VoucherDto> interiorVoucherList = new LinkedList<>();
        Long voucherId = voucherDto.getId();
        if (voucherId == null) {
            voucherId = addVoucher(voucherDto, operator, parameterSet, accountSubjectDto, needCashFlows, interiorVoucherList);
        } else {
            voucherDto.setRootEnterpriseId(operator.getRootEnterpriseId());
            voucherDto.setStatus(LedgerConstant.VoucherStatus.PENDING);
            voucherDto.setCreatorId(operator.getUserId());
            voucherDto.setCreatorName(operator.getName());
            updateVoucher(voucherDto, operator, parameterSet, accountSubjectDto, needCashFlows, interiorVoucherList, voucherId);
        }
        return voucherId;
    }

    /**
     * 保存现金流量
     *
     * @param voucherDto voucherDto
     * @return java.lang.Long
     * @author xyyxhcj@qq.com
     * @date 2019/8/19 9:00
     **/
    @Override
    public Long saveCashFlow(VoucherDto voucherDto) {
        if (!getCashFlowEnableStatus(voucherDto)) {
            throw new ServiceException(ResultCode.CASH_FLOW_OFF);
        }
        SysUserVo operator = UserUtils.getUserVo();
        // 获取总账参数校验
        ParameterSetVo parameterSet = getParameterSet(operator);
        // 记录需要现金流量分析的分录
        List<VoucherEntryDto> needCashFlows = new LinkedList<>();
        Long voucherId = voucherDto.getId();
        Voucher existVoucher = voucherMapper.selectById(voucherId);
        FastUtils.checkNull(existVoucher);
        checkVersion(existVoucher, voucherDto);
        checkSaveCashFlowParamSet(voucherDto, parameterSet, existVoucher);
        // 现金分析明细构造字典
        Map<Integer, List<VoucherEntryCashFlowDto>> entryCashFlowDict = new LinkedHashMap<>();
        Map<Integer, VoucherEntry> rowNumEntryDict = new HashMap<>();
        Map<Long, Map<String, Object>> subjectDict = fillCashFlowDict(voucherDto, needCashFlows, voucherId, entryCashFlowDict, rowNumEntryDict);
        // 标记存在现金流量净发生额
        boolean hasCashAmount = false;
        if (!needCashFlows.isEmpty()) {
            // 检查借贷双方是否都是现金银行类科目且金额相等，如果是则不需进行现金流量分析
            hasCashAmount = isHasCashAmount(voucherDto, needCashFlows);
        }
        if (!hasCashAmount) {
            throw new ServiceException(ResultCode.CASH_FLOW_NEEDLESS);
        }
        // 保存现金流量
        // 是否校验现金流量： 参数控制“凭证保存时必须指定现金流量项目”为是时 如果有现金流量净发生额 则必须校验现金流量
        List<Long> unchangedIds = new LinkedList<>();
        List<VoucherEntryCashFlow> saveCashFlowList = getVoucherEntryCashFlows(voucherDto, voucherId, rowNumEntryDict, subjectDict, unchangedIds);
        if (voucherDto.getCashCheckType() == null) {
            voucherDto.setCashCheckType(existVoucher.getCashCheckType());
        }
        boolean specifiedCashFlow = checkCashFlowParamSet(voucherDto, parameterSet);
        // 先查出原数据 反向更新发生额表
        LambdaQueryWrapper<VoucherEntryCashFlow> cashFlowWrapper = new LambdaQueryWrapper<VoucherEntryCashFlow>().eq(VoucherEntryCashFlow::getVoucherId, voucherId);
        List<VoucherEntryCashFlow> beforeEntryCashFlows = voucherEntryCashFlowService.list(cashFlowWrapper);
        Map<Long, BalanceCashFlowDto> balanceCashFlowSubtractMap = new LinkedHashMap<>();
        countBalanceForCashFlow(balanceCashFlowSubtractMap, beforeEntryCashFlows, voucherDto.getPostingStatus());
        // 对变更数据进行删除/修改/新增
        collectStetCashFlowIds(unchangedIds, saveCashFlowList, cashFlowWrapper);
        boolean hasRemove = voucherEntryCashFlowService.remove(cashFlowWrapper);
        if (!saveCashFlowList.isEmpty()) {
            voucherEntryCashFlowService.saveOrUpdateBatch(saveCashFlowList);
        }
        if (!saveCashFlowList.isEmpty() || hasRemove) {
            updateVoucherEntryForSpecifiedCashFlow(voucherDto, voucherId, specifiedCashFlow);
            // 更新发生额表  如果过账则需要更新已过账余额
            Map<Long, BalanceCashFlowDto> balanceCashFlowAddMap = new LinkedHashMap<>();
            needCashFlows.forEach(entryDto -> countBalanceForCashFlow(balanceCashFlowAddMap, entryDto.getEditCashFlowList(), voucherDto.getPostingStatus()));
            if (!balanceCashFlowSubtractMap.isEmpty()) {
                LedgerUtils.lockCashFlow(voucherDto, () -> updateCashFlowBalance(balanceCashFlowSubtractMap, voucherDto, Constant.BalanceUpdateType.SUBTRACT));
            }
            if (!balanceCashFlowAddMap.isEmpty()) {
                LedgerUtils.lockCashFlow(voucherDto, () -> updateCashFlowBalance(balanceCashFlowAddMap, voucherDto, Constant.BalanceUpdateType.ADD));
            }
        }
        // 更新版本号
        Voucher updateVoucher = new Voucher();
        updateVoucher.setId(voucherId);
        updateVoucher.setCashCheckType(voucherDto.getCashCheckType());
        updateVoucher.setVersion(existVoucher.getVersion());
        voucherMapper.updateById(updateVoucher);
        return voucherId;
    }

    /**
     * 现金流量是否启用
     *
     * @param voucherDto voucherDto
     * @return boolean 启用为true
     * @author xyyxhcj@qq.com
     * @date 2019/9/4 10:12
     **/
    private boolean getCashFlowEnableStatus(VoucherDto voucherDto) {
        BalanceCashFlowInitDto balanceCashFlowInitDto = new BalanceCashFlowInitDto();
        balanceCashFlowInitDto.setAccountBookId(voucherDto.getAccountBookId());
        BalanceCashFlowInitVo accCashFlowStatus = balanceCashFlowInitService.findAccCashFlowStatus(balanceCashFlowInitDto);
        Byte cashFlowEnableStatus = accCashFlowStatus.getCashFlowEnableStatus();
        return Constant.Is.YES.equals(cashFlowEnableStatus);
    }

    /**
     * 生成冲销凭证
     *
     * @param voucherDto voucherDto
     * @return java.lang.Long
     * @author xyyxhcj@qq.com
     * @date 2019/8/19 9:04
     **/
    @Override
    public Long generateOffset(VoucherDto voucherDto) {
        SysUserVo operator = UserUtils.getUserVo();
        Voucher existVoucher = voucherMapper.selectById(voucherDto.getId());
        FastUtils.checkNull(existVoucher);
        checkVersion(existVoucher, voucherDto);
        checkGenerateOffsetValid(existVoucher);
        List<Long> generateIds = new LinkedList<>();
        generateIds.add(existVoucher.getId());
        VoucherVo datePeriodVoucher = generateVoucherDate(voucherDto, Constant.VoucherDateType.SYSTEM);
        if (datePeriodVoucher.getVoucherDate().before(existVoucher.getVoucherDate())) {
            throw new ServiceException(ResultCode.RUSH_DATE_EARLY);
        }
        List<VoucherDto> generateVoucherList = new LinkedList<>();
        addGenerateVoucher(operator, existVoucher, datePeriodVoucher, generateVoucherList);
        if (Constant.InteriorType.GENERATE == existVoucher.getInteriorType()) {
            // 查出生成的所有协同凭证
            List<Voucher> interiorVouchers = voucherMapper.selectList(new LambdaQueryWrapper<Voucher>()
                    .eq(Voucher::getSourceCode, existVoucher.getId().toString())
                    .in(Voucher::getSourceType, LedgerConstant.SourceType.COLLABORATE, LedgerConstant.SourceType.COMPANY_COLL)
                    .eq(Voucher::getIsDel, Constant.Is.NO));
            interiorVouchers.forEach(voucher -> {
                generateIds.add(voucher.getId());
                addGenerateVoucher(operator, voucher, datePeriodVoucher, generateVoucherList);
            });
        }
        // 取出所有涉及的分录+核算明细+现金流量
        LinkedList<VoucherEntryDto> entryList = voucherEntryService.findList(generateIds);
        LinkedList<VoucherEntryAuxiliaryDto> entryAuxiliaryList = voucherEntryAuxiliaryService.findList(generateIds);
        List<VoucherEntryCashFlowDto> entryCashFlowList = voucherEntryCashFlowService.findList(generateIds);
        checkDataForGenerateOffset(entryList, entryAuxiliaryList, entryCashFlowList);
        mergeVoucherListDetail(generateVoucherList, entryList, entryAuxiliaryList, entryCashFlowList);
        // 保存凭证的所有现金流量引用
        MergeUtil.mergeList(generateVoucherList, entryCashFlowList,
                Voucher::getId, VoucherEntryCashFlow::getVoucherId,
                (target, sourceList) -> target.getEditEntryCashFlowList().addAll(sourceList));
        // 插入并更新余额表
        voucherMapper.insertGenerateBatch(generateVoucherList);
        for (VoucherDto generateVoucher : generateVoucherList) {
            Long generateVoucherId = generateVoucher.getId();
            voucherEntryService.insertBatch(generateVoucher.getEditEntryList(), generateVoucherId);
            List<VoucherEntryAuxiliary> saveAuxiliaryList = getVoucherEntryAuxiliaries(generateVoucher, generateVoucherId, null);
            if (!saveAuxiliaryList.isEmpty()) {
                voucherEntryAuxiliaryService.saveBatch(saveAuxiliaryList);
            }
            List<VoucherEntryCashFlowDto> cashFlowList = generateVoucher.getEditEntryCashFlowList();
            if (cashFlowList != null && !cashFlowList.isEmpty()) {
                voucherEntryCashFlowService.insertBatch(cashFlowList, generateVoucherId);
            }
            // 更新余额
            updateBalance(generateVoucher, Constant.BalanceUpdateType.ADD);
        }
        // 原凭证改为已冲销 generateIds
        Voucher updateRushed = new Voucher();
        updateRushed.setIsOffset(Constant.Is.YES);
        voucherMapper.update(updateRushed, new LambdaQueryWrapper<Voucher>().in(Voucher::getId, generateIds));
        // 生成 主号子号
        List<Voucher> updateVouchers = new LinkedList<>();
        updateVoucherCodeBatch(updateVouchers, false, generateVoucherList);
        return generateVoucherList.get(0).getId();
    }

    /**
     * 生成冲销凭证明细数据，并校验数据是否可用
     *
     * @param entryList          entryList
     * @param entryAuxiliaryList entryAuxiliaryList
     * @param entryCashFlowList  entryCashFlowList
     * @author xyyxhcj@qq.com
     * @date 2019/9/11 13:43
     **/
    private void checkDataForGenerateOffset(LinkedList<VoucherEntryDto> entryList, LinkedList<VoucherEntryAuxiliaryDto> entryAuxiliaryList, List<VoucherEntryCashFlowDto> entryCashFlowList) {
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        // 获取科目/辅助核算/现金流量 数据字典
        Set<Long> ids = new LinkedHashSet<>();
        // 存储原id与对象的关联字典
        Map<Long, VoucherEntryDto> voucherEntryIdMap = new LinkedHashMap<>();
        if (!entryList.isEmpty()) {
            entryList.forEach(entry -> ids.add(entry.getAccountSubjectId()));
            Map<Long, Map<String, Object>> subjectDict = getSubjectDict(ids, accountSubjectDto);
            // 校验禁用的数据
            for (VoucherEntryDto entry : entryList) {
                checkValidForRushData(subjectDict, entry.getAccountSubjectId(), Constant.PropertyName.FULL_NAME, null);
                // 赋值
                entry.setDebitAmount(entry.getDebitAmount().negate());
                entry.setCreditAmount(entry.getCreditAmount().negate());
                entry.setOriginalDebitAmount(entry.getOriginalDebitAmount().negate());
                entry.setOriginalCreditAmount(entry.getOriginalCreditAmount().negate());
                entry.setInteriorType(Constant.InteriorType.NEEDLESS);
                voucherEntryIdMap.put(entry.getId(), entry);
            }
        }
        if (!entryAuxiliaryList.isEmpty()) {
            Map<String, Map<Long, Map<String, Object>>> auxiliaryDict = new LinkedHashMap<>();
            Map<String, Set<Long>> auxiliaryIdMap = new LinkedHashMap<>();
            entryAuxiliaryList.forEach(auxiliary -> auxiliaryIdMap.computeIfAbsent(auxiliary.getSourceTable(), k -> new LinkedHashSet<>()).add(auxiliary.getItemValueId()));
            fillAuxiliaryDict(auxiliaryDict, auxiliaryIdMap);
            // 校验
            for (VoucherEntryAuxiliaryDto auxiliary : entryAuxiliaryList) {
                String sourceTable = auxiliary.getSourceTable();
                if (Constant.TableName.ACCOUNT_BOOK_ENTITY.equals(sourceTable)) {
                    continue;
                }
                Map<Long, Map<String, Object>> auxiliaryData = auxiliaryDict.get(sourceTable);
                if (auxiliaryData == null) {
                    throw new ServiceException(ResultCode.DATA_ERROR);
                }
                checkValidForRushData(auxiliaryData, auxiliary.getItemValueId(), Constant.PropertyName.NAME, Constant.VoucherDataType.ARCHIVES);
            }
        }
        if (!entryCashFlowList.isEmpty()) {
            ids.clear();
            entryCashFlowList.forEach(cashFlow -> ids.add(cashFlow.getCashFlowItemId()));
            Map<Long, Map<String, Object>> cashFlowItemDict = getCashFlowItemDict(ids, accountSubjectDto);
            // 校验 取相反数
            for (VoucherEntryCashFlowDto cashFlow : entryCashFlowList) {
                checkValidForRushData(cashFlowItemDict, cashFlow.getCashFlowItemId(), Constant.PropertyName.FULL_NAME, Constant.VoucherDataType.CASH_FLOW_ITEM);
                cashFlow.setCurrencyAmount(cashFlow.getCurrencyAmount().negate());
                cashFlow.setEntry(voucherEntryIdMap.get(cashFlow.getEntryId()));
                cashFlow.setOppositeEntry(voucherEntryIdMap.get(cashFlow.getOppositeEntryId()));
            }
        }
    }

    /**
     * 获取流水号
     *
     * @param voucherDto voucherDto
     * @return com.njwd.entity.ledger.Voucher
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:31
     **/
    @Override
    public Voucher generateCode(Voucher voucherDto) {
        SequenceDto sequenceDto = new SequenceDto();
        sequenceDto.setCredWord(voucherDto.getCredentialWord());
        sequenceDto.setYear(voucherDto.getPostingPeriodYear());
        sequenceDto.setPeriodNo(voucherDto.getPostingPeriodNum());
        sequenceDto.setAccountId(voucherDto.getAccountBookId());
        Integer mainCode = publicDataFeignClient.getCredWord(sequenceDto).getData();
        sequenceDto.setEntityId(voucherDto.getAccountBookEntityId());
        Integer childCode = publicDataFeignClient.getCredWord(sequenceDto).getData();
        if (mainCode == null || childCode == null) {
            throw new ServiceException(String.format(ResultCode.FEIGN_CONNECT_ERROR.message, FeignClientErrorMsg.GET_VOUCHER_CODE_ERROR), ResultCode.FEIGN_CONNECT_ERROR);
        }
        voucherDto.setMainCode(mainCode);
        voucherDto.setChildCode(childCode);
        Voucher updateVoucher = new Voucher();
        FastUtils.copyProperties(voucherDto, updateVoucher);
        return updateVoucher;
    }

    /**
    * @description: 根据账簿id，项目来源表，项目值查询凭证是否引用当前辅助核算值
    * @Param [voucherDto]
    * @return java.lang.Integer
    * @author LuoY
    * @date 2019/9/29 11:55
    */
    @Override
    public Integer findVoucherEntryAuxiliary(VoucherDto voucherDto) {
        return voucherMapper.findVoucherByAccountBookId(voucherDto);
    }

    /**
     * @Description 查询范围内的账簿id存在凭证的账簿id
     * @Author 郑勇浩
     * @Data 2019/11/1 14:02
     * @Param [voucherDto]
     * @return java.util.List<java.lang.Long>
     */
    @Override
    public List<Long> findHasVoucherByAccountBookId(VoucherDto voucherDto) {
        return voucherMapper.findHasVoucherByAccountBookId(voucherDto);
    }

    @Override
    public List<VoucherVo> findVoucherByAccBookIdAndYear(VoucherDto voucherDto) {
//        return voucherMapper.selectOne(new LambdaQueryWrapper<Voucher>().eq(Voucher::getAccountBookId,voucherDto.getAccountBookId())
//                .eq(Voucher::getPostingPeriodYear,voucherDto.getPostingPeriodYear()).last(Constant.ConcatSql.LIMIT_1));
        return voucherMapper.findVoucherByAccBookIdAndYear(voucherDto);
    }

    /**
     * @return com.njwd.entity.ledger.vo.VoucherVo
     * @Author ZhuHC
     * @Date 2019/8/14 8:58
     * @Param [id]
     * @Description 凭证详情
     */
    @Override
    public VoucherVo findDetail(VoucherVo voucherVoInfo) {
        Long id = voucherVoInfo.getId();
        VoucherVo voucherVo = voucherMapper.findVoucherById(id);
        FastUtils.checkNull(voucherVo);
        List<Long> generateIds = new LinkedList<>();
        generateIds.add(voucherVo.getId());
        List<Voucher> interiorVouchers;
        if (Constant.InteriorType.GENERATE == voucherVo.getInteriorType()) {
            // 查出生成的所有协同凭证
            interiorVouchers = voucherMapper.selectList(new LambdaQueryWrapper<Voucher>()
                    .eq(Voucher::getSourceCode, voucherVo.getId().toString())
                    .in(Voucher::getSourceType, LedgerConstant.SourceType.COLLABORATE, LedgerConstant.SourceType.COMPANY_COLL)
                    .eq(Voucher::getIsDel, Constant.Is.NO));
            interiorVouchers.forEach(interiorVoucher -> {
                generateIds.add(interiorVoucher.getId());
            });
        }
        // 查分录 包括协同凭证分录
        List<VoucherEntryVo> voucherEntries = voucherEntryService.findVoucherEntryInteriorInfo(generateIds);
        // 查辅助核算明细
        List<VoucherEntryAuxiliary> auxiliaries = voucherEntryAuxiliaryService.list(new LambdaQueryWrapper<VoucherEntryAuxiliary>().in(VoucherEntryAuxiliary::getVoucherId, generateIds));
        // 查现金流量
        List<VoucherEntryCashFlow> cashFlows = voucherEntryCashFlowService.list(new LambdaQueryWrapper<VoucherEntryCashFlow>().in(VoucherEntryCashFlow::getVoucherId, generateIds).orderByAsc(VoucherEntryCashFlow::getRowNum));
        //查会计科目: 根据凭证分录 获得 需要查询的所有会计科目ID,获得会计科目详情
        List<AccountSubjectVo> accountSubjectVoList = new ArrayList<>();
        if(!FastUtils.checkNullOrEmpty(voucherEntries)){
            accountSubjectVoList = getSubjectList(voucherEntries);
        }
        //凭证分录 数据转换
        List<VoucherEntryVo> voucherEntryVos = new ArrayList<>();
        if(!FastUtils.checkNullOrEmpty(voucherEntries) && !FastUtils.checkNullOrEmpty(accountSubjectVoList)){
            voucherEntryVos = getVoucherEntryVos(voucherEntries, accountSubjectVoList);
        }
        //数据拼接 现金流量明细-现金流量
        Map<Long, List<VoucherEntryCashFlowVo>> cashFlowVoMap = getCashFlowVoMap(cashFlows);
        //数据拼接 辅助核算明细-核算项目
        Map<Long, List<VoucherEntryAuxiliaryVo>> auxiliaryVoMap = mergeAuxiliaryAndProject(auxiliaries);
        //将 辅助核算明细和现金流量明细数据加入 分录数据中
        List<VoucherEntryVo> finalVoucherEntryVos = new ArrayList<>();
        if(!FastUtils.checkNullOrEmpty(voucherEntryVos)){
            for (VoucherEntryVo voucherEntryVo : voucherEntryVos) {
                for (Map.Entry<Long, List<VoucherEntryAuxiliaryVo>> entry : auxiliaryVoMap.entrySet()) {
                    if (entry.getKey().equals(voucherEntryVo.getId())) {
                        voucherEntryVo.setVoucherEntryAuxiliaryVos(entry.getValue());
                    }
                }
                for (Map.Entry<Long, List<VoucherEntryCashFlowVo>> entry : cashFlowVoMap.entrySet()) {
                    if (entry.getKey().equals(voucherEntryVo.getId())) {
                        voucherEntryVo.setVoucherEntryCashFlowVos(entry.getValue());
                    }
                }
            }
            //区分 分录数据和 协同凭证的分录数据
            for(VoucherEntryVo vo : voucherEntryVos){
                if(vo.getVoucherId().equals(id)){
                    finalVoucherEntryVos.add(vo);
                }
            }
            //数据拼接 协同凭证分录数据放入分录数据对应字段中
            if(!FastUtils.checkNullOrEmpty(finalVoucherEntryVos)){
              /*  MergeUtil.merge(finalVoucherEntryVos, voucherEntryVos,
                        (vev, ev) ->  vev.getId().equals(ev.getInteriorEntryId()),
                        (vev, ev) -> {
                            vev.setInteriorEntryId(ev.getInteriorEntryId());
                            vev.setInteriorEntry(ev);
                        });*/
                //将上面的代码 优化成此代码
                  MergeUtil.merge(finalVoucherEntryVos,voucherEntryVos,
                          VoucherEntryVo::getId,VoucherEntryVo::getInteriorEntryId,
                          (vev, ev) -> {
                              vev.setInteriorEntryId(ev.getInteriorEntryId());
                              vev.setInteriorEntry(ev);
                          });
            }
        }
        //凭证分录根据 序号 科目编码排序
        finalVoucherEntryVos.sort((o1, o2) -> {
            int i = o1.getRowNum() - o2.getRowNum();
            if (0 == i ) {
                if(Long.valueOf(o1.getSubjectCode()) - Long.valueOf(o2.getSubjectCode()) == 0){
                    return Constant.Number.ZERO;
                }else {
                    return (Long.valueOf(o1.getSubjectCode()) - Long.valueOf(o2.getSubjectCode())) > 0 ? Constant.Number.ONE : Constant.Number.MINUS_ZERO;
                }
            }else {
                return i;
            }
        });
        voucherVo.setVoucherEntryVos(finalVoucherEntryVos);
        if(StringUtil.isNotEmpty(voucherVo.getSourceCode())){
            Voucher voucher = voucherMapper.selectOne(new LambdaQueryWrapper<Voucher>().eq(Voucher::getId, voucherVo.getSourceCode()));
            if(null != voucher){
                voucherVo.setOppositeMainCode(voucher.getMainCode());
                voucherVo.setOppositeCredentialWord(voucher.getCredentialWord());
            }
        }
        return voucherVo;
    }

    /**
     * @return void
     * @Author ZhuHC
     * @Date 2019/8/19 14:35
     * @Param [auxiliaries, auxiliaryVoMap]
     * @Description 归类辅助核算项目
     */
    private Map<Long, List<VoucherEntryAuxiliaryVo>> mergeAuxiliaryAndProject(List<VoucherEntryAuxiliary> auxiliaries) {
        Map<Long, List<VoucherEntryAuxiliaryVo>> auxiliaryVoMap = new LinkedHashMap<>();
        if (!FastUtils.checkNullOrEmpty(auxiliaries)) {
            //辅助核算项目
            List<CommonAuxiliary> allAuxiliaries = getAuxiliaryList(auxiliaries);
            if(!FastUtils.checkNullOrEmpty(allAuxiliaries)){
                List<VoucherEntryAuxiliaryVo> voucherEntryAuxiliaryVos = getVoucherEntryAuxiliaryVos(auxiliaries, allAuxiliaries);
                if(!FastUtils.checkNullOrEmpty(voucherEntryAuxiliaryVos)){
                    for (VoucherEntryAuxiliaryVo auxiliaryVos : voucherEntryAuxiliaryVos) {
                        auxiliaryVoMap.computeIfAbsent(auxiliaryVos.getEntryId(), k -> new LinkedList<>()).add(auxiliaryVos);
                    }
                }
            }
        }
        return auxiliaryVoMap;
    }

    /**
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.ledger.vo.VoucherVo>
     * @Author ZhuHC
     * @Date 2019/8/14 14:36
     * @Param [voucherDto]
     * @Description 凭证列表
     */
    @Override
    public Page<VoucherVo> findPage(VoucherDto voucherDto) {
        voucherDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        //查询条件包含辅助核算内容时，先过滤出包含选中辅助核算项的凭证分录，根据凭证分录删选出 凭证
        if(getInfoWithAuxiliaryByRules(voucherDto)) {
            return null;
        }
        Page<VoucherDto> page = voucherDto.getPage();
        //根据条件过滤分录数据  取得对应凭证数据
        List<VoucherEntryVo> voucherEntryVos = voucherEntryMapper.findListByRules(voucherDto);
        if(!FastUtils.checkNullOrEmpty(voucherEntryVos)){
            List<Long> voIds = voucherEntryVos.stream().map(VoucherEntryVo::getVoucherId).collect(Collectors.toList());
            voucherDto.setVoucherIds(voIds.stream().distinct().collect(Collectors.toList()));
        }
        Page<VoucherVo> voucherDtoPage = voucherMapper.findPage(page, voucherDto);
        if(!FastUtils.checkNullOrEmpty(voucherDtoPage.getRecords())){
            List<VoucherVo> voucherVos = voucherDtoPage.getRecords();
            //账簿期间信息-- 编码
            List<AccountBookPeriodVo> accountBookPeriodVoList = accountBookPeriodMapper.selectPeriodByAccountBookId(voucherDto);
            //账簿编码 添加入凭证列表中
            if(!FastUtils.checkNullOrEmpty(accountBookPeriodVoList)){
               /* MergeUtil.merge(voucherVos, accountBookPeriodVoList,
                        (vev, ev) ->  vev.getAccountBookId().equals(ev.getAccountBookId()),
                        (vev, ev) -> {
                            vev.setAccountBookCode(ev.getAccountBookCode());
                        });*/
                //将上面的代码 优化成此代码
               MergeUtil.merge(voucherVos,accountBookPeriodVoList,
                       VoucherVo::getAccountBookId,AccountBookPeriodVo::getAccountBookId,
                       (voucher,accountBookPeriod)->
                               voucher.setAccountBookCode(accountBookPeriod.getAccountBookCode()));
            }
            List<Long> sourceIds = new LinkedList<>();
            for(VoucherVo voucherVo : voucherVos){
              /*  if(StringUtil.isNotEmpty(voucherVo.getSourceCode())){
                    sourceIds.add(Long.valueOf(voucherVo.getSourceCode()));
                }*/
                if(!StringUtil.isEmpty(voucherVo.getSourceCode())){
                    sourceIds.add(Long.valueOf(voucherVo.getSourceCode()));
                }
            }
            if(!FastUtils.checkNullOrEmpty(sourceIds)){
                //对方凭证
                List<Voucher> voucherList = voucherMapper.selectList(new LambdaQueryWrapper<Voucher>().in(Voucher::getId, sourceIds));
                /*MergeUtil.merge(voucherVos, voucherList,
                        (vev, ev) ->  vev.getSourceCode().equals(String.valueOf(ev.getId())),
                        (vev, ev) -> {
                            vev.setOppositeCredentialWord(ev.getCredentialWord());
                            vev.setOppositeMainCode(ev.getMainCode());
                        });*/
                //将上面的代码 优化成此代码
                MergeUtil.merge(voucherVos,voucherList,
                        voucherVo ->voucherVo.getSourceCode(),
                        voucher -> String.valueOf(voucher.getId()),
                        (voucherVo,voucher) -> {
                            voucherVo.setOppositeCredentialWord(voucher.getCredentialWord());
                            voucherVo.setOppositeMainCode(voucher.getMainCode());
                        });
            }
            //根据 账簿  主体  凭证字 号 排序
            /*voucherVos.sort((o1, o2) -> {
                int i = (int)(o1.getAccountBookId() - o2.getAccountBookId());
                if (0 == i ) {
                    i = (int)(o1.getAccountBookEntityId() - o2.getAccountBookEntityId());
                }
                if (0 == i ) {
                    i = o1.getCredentialWord() - o2.getCredentialWord();
                }
                if (0 == i ) {
                    i = o1.getMainCode() - o2.getMainCode();
                }

                return i;
            });*/
        }
        return voucherDtoPage;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/28 14:57
     * @Param [voucherDto]
     * @return boolean
     * @Description 过滤凭证--根据辅助核算项
     */
    private boolean getInfoWithAuxiliaryByRules(VoucherDto voucherDto) {
        if(!FastUtils.checkNullOrEmpty(voucherDto.getSourceTableAndIdList())){
            VoucherEntryAuxiliaryDto voucherEntryAuxiliaryDto = new VoucherEntryAuxiliaryDto();
            voucherEntryAuxiliaryDto.setSourceTableAndIdList(voucherDto.getSourceTableAndIdList());
            List<VoucherEntryAuxiliaryVo> entryAuxiliaryVos = voucherEntryAuxiliaryMapper.findListByAuxiliary(voucherEntryAuxiliaryDto);
            if(!FastUtils.checkNullOrEmpty(entryAuxiliaryVos)){
                List<Long> valueIdList = new LinkedList<>();
                List<String> auxiliaryIds;
                List<Long> voIds;
                for(VoucherEntryAuxiliaryVo vo : entryAuxiliaryVos){
                    //核算项数量 与 根据分录中对应的辅助核算项个数一致时  通过过滤
                    if(voucherDto.getSourceTableAndIdList().size() == vo.getSourceTables().split(",").length){
                        //数组转list
                        auxiliaryIds = Arrays.asList(vo.getEntryIds().split(","));
                        //list 去除重复ID ; string 转 Long
                        voIds = auxiliaryIds.stream().map(id -> Long.parseLong(id)).collect(Collectors.toList());
                        voIds.stream().distinct().collect(Collectors.toList());
                        valueIdList.addAll(voIds);
                        voucherDto.setEntryIdList(valueIdList);
                    }
                }
            }else {
                return true;
            }
        }
        return false;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/23 10:53
     * @Param [voucherDto]
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.ledger.vo.VoucherEntryVo>
     * @Description 凭证列表 带明细
     */
    @Override
    public Page<VoucherEntryVo> findVoucherEntries(VoucherDto voucherDto) {
        Page<VoucherVo> voucherVoPage = findPage(voucherDto);
        Page<VoucherEntryVo> voucherEntryVoPage = new Page<>();
        if(!FastUtils.checkNullOrEmpty(voucherVoPage.getRecords())){
            List<VoucherEntryVo> voucherEntryVos = getVoucherEntriesByVoucher(voucherVoPage.getRecords(),voucherDto);
            voucherEntryVoPage.setRecords(voucherEntryVos);
        }
        voucherEntryVoPage.setTotal(voucherVoPage.getTotal());
        voucherEntryVoPage.setSize(voucherVoPage.getSize());
        voucherEntryVoPage.setCurrent(voucherVoPage.getCurrent());
        return voucherEntryVoPage;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/23 14:02
     * @Param [voucherDto, response]
     * @return void
     * @Description 凭证列表导出
     */
    @Override
    public void exportVoucherListExcel(VoucherDto voucherDto, HttpServletResponse response) {
        // 是否展示明细 1：展示，0：不展示
        if(Constant.Is.NO .equals(voucherDto.getIsDetailShow())){
            List<VoucherVo> voucherVos = findPage(voucherDto).getRecords();
            if(!FastUtils.checkNullOrEmpty(voucherVos)){
                //来源单号 凭证字号 等 数据转换
                StringBuffer sb;
                for(VoucherVo vo : voucherVos){
                    sb = new StringBuffer();
                    vo.setCredentialWordCode(auxiliaryAccountingService.convertData(vo.getCredentialWord())+"-"+vo.getMainCode());
                    if(StringUtil.isNotEmpty(vo.getSourceSystem())){
                        vo.setSourceSystem(LogConstant.sysName.LedgerSys);
                    }
                    if(StringUtil.isNotEmpty(vo.getSourceCode())) {
                        sb.append(vo.getAccountBookCode()).append("-").append(vo.getPeriodYearNum()).append("-")
                                .append(auxiliaryAccountingService.convertData(vo.getOppositeCredentialWord())).append(vo.getOppositeMainCode());
                    }
                    vo.setSourceCode(sb.toString());
                }
                fileService.exportExcel(response,voucherVos, MenuCodeConstant.VOUCHER_LEDGER);
            }
        }else {
            List<VoucherEntryVo> voucherEntryVoList = findVoucherEntries(voucherDto).getRecords();
            if(!FastUtils.checkNullOrEmpty(voucherEntryVoList)){
                //来源单号 凭证字号 等 数据转换
                StringBuffer sb;
                VoucherEntryVo entryVo ;
                VoucherEntryVo lastEntryVo ;
                for(int i = 0; i < voucherEntryVoList.size(); i++ ){
                    sb = new StringBuffer();
                    entryVo = voucherEntryVoList.get(i);
                    if(i == 0){
                        setSourceInfo(sb, entryVo);
                    }else {
                        lastEntryVo = voucherEntryVoList.get(i - 1);
                        if(!entryVo.getVoucherId().equals(lastEntryVo.getVoucherId())){
                            setSourceInfo(sb, entryVo);
                        }
                    }
                    entryVo.setSourceCode(sb.toString());
                    sb = new StringBuffer();
                    if(StringUtil.isNotEmpty(entryVo.getSubjectCode())){
                        sb.append(entryVo.getSubjectCode());
                    }
                    Byte flag = voucherDto.getFlag();
                    String name = null;
                    if(Constant.Number.INITIAL.equals(flag)){
                        name = entryVo.getSubjectFullName();
                    }else {
                        name = entryVo.getSubjectName();
                    }
                    if(StringUtil.isNotEmpty(name)){
                        sb.append(name);
                    }
                    entryVo.setAccountingSubjects(sb.toString());
                    sb = new StringBuffer();
                    List<VoucherEntryAuxiliaryVo> voucherEntryAuxiliaryVos = entryVo.getVoucherEntryAuxiliaryVos();
                    if(null != voucherEntryAuxiliaryVos && voucherEntryAuxiliaryVos.size() > 0){
                        for(VoucherEntryAuxiliaryVo voucherEntryAuxiliaryVo : voucherEntryAuxiliaryVos){
                            if(StringUtil.isNotEmpty(voucherEntryAuxiliaryVo.getProjectAuxiliaryName())){
                                sb.append(voucherEntryAuxiliaryVo.getProjectAuxiliaryName()).append(":");
                            }
                            if(StringUtil.isNotEmpty(voucherEntryAuxiliaryVo.getProjectName())){
                                sb.append(voucherEntryAuxiliaryVo.getProjectName()).append(" ");
                            }
                        }
                        entryVo.setAuxiliaryAccounting(sb.toString());
                    }
                }
                List<ExcelColumn> excelColumnList = fileService.findExcelColumn(MenuCodeConstant.VOUCHER_LEDGER);
                    for(int i=0;i<excelColumnList.size();i++){
                        ExcelColumn excelColumn = excelColumnList.get(i);
                        if(excelColumn.getField().equals("abstractContent")){
                            excelColumnList.add(i+1,new ExcelColumn("accountingSubjects","会计科目"));
                            excelColumnList.add(i+2,new ExcelColumn("auxiliaryAccounting","辅助核算"));
                            break;
                        }
                }
                fileService.exportExcel(response,voucherEntryVoList, excelColumnList);
            }
        }
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/30 13:01
     * @Param [sb, entryVo]
     * @return void
     * @Description
     */
    private void setSourceInfo(StringBuffer sb, VoucherEntryVo entryVo) {
        entryVo.setCredentialWordCode(auxiliaryAccountingService.convertData(entryVo.getCredentialWord()) + "-" + entryVo.getMainCode());
        if (StringUtil.isNotEmpty(entryVo.getSourceSystem())) {
            entryVo.setSourceSystem(LogConstant.sysName.LedgerSys);
        }
        if (StringUtil.isNotEmpty(entryVo.getSourceCode())) {
            sb.append(entryVo.getAccountBookCode()).append("-").append(entryVo.getPeriodYearNum()).append("-")
                    .append(auxiliaryAccountingService.convertData(entryVo.getOppositeCredentialWord())).append(entryVo.getOppositeMainCode());
        }
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/21 17:11
     * @Param [voucherList]
     * @return java.util.List<com.njwd.entity.ledger.vo.VoucherEntryVo>
     * @Description 凭证列表 对应 分录数据 处理
     */
    private List<VoucherEntryVo> getVoucherEntriesByVoucher(List<VoucherVo> voucherList,VoucherDto voucherDto) {
        List<Long> voucherIdList = new LinkedList<>();
        for(VoucherVo voucherVo : voucherList){
            voucherIdList.add(voucherVo.getId());
        }
        voucherDto.setVoucherIds(voucherIdList);
        // 查分录
        List<VoucherEntryVo> voucherEntries = voucherEntryService.findListWithVoucher(voucherDto);
        // 查辅助核算明细
        List<VoucherEntryAuxiliary> auxiliaries = voucherEntryAuxiliaryService.list(new LambdaQueryWrapper<VoucherEntryAuxiliary>().in(VoucherEntryAuxiliary::getVoucherId, voucherIdList));
        //查会计科目: 根据凭证分录 获得 需要查询的所有会计科目ID,获得会计科目详情
        List<AccountSubjectVo> accountSubjectVoList = new ArrayList<>();
        if(!FastUtils.checkNullOrEmpty(voucherEntries)){
            accountSubjectVoList = getSubjectList(voucherEntries);
        }
        //凭证分录 数据转换 并数据拼接 ：凭证分录-会计科目
        List<VoucherEntryVo> voucherEntryVos = new ArrayList<>();
        if(!FastUtils.checkNullOrEmpty(voucherEntries) && !FastUtils.checkNullOrEmpty(accountSubjectVoList)){
            voucherEntryVos = getVoucherEntryVos(voucherEntries, accountSubjectVoList);
        }
        //数据拼接 辅助核算明细-核算项目
        Map<Long, List<VoucherEntryAuxiliaryVo>> auxiliaryVoMap = mergeAuxiliaryAndProject(auxiliaries);
        //将 辅助核算明细数据加入 分录数据中
        if(!FastUtils.checkNullOrEmpty(voucherEntryVos) && auxiliaryVoMap.size() > 0){
            for (VoucherEntryVo voucherEntryVo : voucherEntryVos) {
                for (Map.Entry<Long, List<VoucherEntryAuxiliaryVo>> entry : auxiliaryVoMap.entrySet()) {
                    if (entry.getKey().equals(voucherEntryVo.getId())) {
                        voucherEntryVo.setVoucherEntryAuxiliaryVos(entry.getValue());
                    }
                }
            }
        }
        //处理数据，符合前端展示要求：每条凭证对应的非第一条分录数据，不展示账簿名称，主体名称，制单日期，凭证字号，制单审核复核过账人,来源单号，来源系统以及来源方式
        VoucherEntryVo entryVo ;
        VoucherEntryVo lastEntryVo ;
        for(int i = 0; i < voucherEntryVos.size(); i++ ){
            entryVo = voucherEntryVos.get(i);
            if(i == 0){
                entryVo.setCredentialWordCode(auxiliaryAccountingService.convertData(entryVo.getCredentialWord()) + "-" + entryVo.getMainCode());
            }else {
                lastEntryVo = voucherEntryVos.get(i - 1);
                if(entryVo.getVoucherId().equals(lastEntryVo.getVoucherId())){
                    clearEntryVoSomeInfo(entryVo);
                }
            }
        }
        return voucherEntryVos;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/23 10:25
     * @Param [entryVo]
     * @return void
     * @Description 不展示账簿名称，主体名称，制单日期，凭证字号，制单审核复核过账人,来源单号，来源系统以及来源方式
     */
    private void clearEntryVoSomeInfo(VoucherEntryVo entryVo) {
        entryVo.setAccountBookName(null);
        entryVo.setAccountBookEntityName(null);
        entryVo.setVoucherDate(null);
        entryVo.setCreatorName(null);
        entryVo.setApproverName(null);
        entryVo.setReviewerName(null);
        entryVo.setPostingUserName(null);
        entryVo.setSourceCode(null);
        entryVo.setSourceSystem(null);
        entryVo.setSourceType(null);
        entryVo.setSourceSystem(null);
        entryVo.setSourceCode(null);
        entryVo.setCredentialWordCode(null);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/21 17:13
     * @Param [finalVoucherEntryVos]
     * @return void
     * @Description 排序
     */
    private void sortVoucherEntries(List<VoucherEntryVo> finalVoucherEntryVos) {
        finalVoucherEntryVos.sort((o1, o2) -> {
            int i = (int)(o1.getAccountBookId() - o2.getAccountBookId());
            if(i == 0){
                i = (int)(o1.getAccountBookEntityId() - o2.getAccountBookEntityId());
            }
            if (i == 0) {
                i = o1.getCredentialWord() - o2.getCredentialWord();
            }
            if (i == 0) {
                i = o1.getMainCode() - o2.getMainCode();
            }
            if (i == 0) {
                i = o1.getRowNum() - o2.getRowNum();
            }
            return i;
        });
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/23 12:12
     * @Param [cashFlows]
     * @return java.util.Map<java.lang.Long,java.util.List<com.njwd.entity.ledger.vo.VoucherEntryCashFlowVo>>
     * @Description 拼接现金流量数据
     */
    private Map<Long, List<VoucherEntryCashFlowVo>> getCashFlowVoMap(List<VoucherEntryCashFlow> cashFlows) {
        Map<Long, List<VoucherEntryCashFlowVo>> cashFlowVoMap = new LinkedHashMap<>();
        if (!FastUtils.checkNullOrEmpty(cashFlows)) {
            //现金流量
            List<CommonAuxiliary> cashFlowDetailList = getCashFlowList(cashFlows);
            if (!FastUtils.checkNullOrEmpty(cashFlowDetailList)) {
                List<VoucherEntryCashFlowVo> voucherEntryCashFlowVos = getVoucherEntryCashFlowVos(cashFlows, cashFlowDetailList);
                if (!FastUtils.checkNullOrEmpty(voucherEntryCashFlowVos)) {
                    for (VoucherEntryCashFlowVo cashFlowVos : voucherEntryCashFlowVos) {
                        cashFlowVoMap.computeIfAbsent(cashFlowVos.getEntryId(), k -> new LinkedList<>()).add(cashFlowVos);
                    }
                }
            }
        }
        return cashFlowVoMap;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/17 14:21
     * @Param [voucherDto]
     * @return com.njwd.support.Result
     * @Description  凭证列表 根据凭证ID查询(暂时弃用)
     */
    @Override
    public List<VoucherVo> findVouchersByIds(VoucherDto voucherDto) {
        List<VoucherVo> details = new ArrayList<>();
        VoucherVo voucherVo;
        VoucherVo vo = new VoucherVo();
        for (Long id : voucherDto.getVoucherIds()) {
            vo.setId(id);
            voucherVo = findDetail(vo);
            voucherVo.setAbstractContent(voucherVo.getFirstAbstract());
            voucherVo.setCredentialWordCode(auxiliaryAccountingService.convertData(voucherVo.getCredentialWord()) + "-" + voucherVo.getMainCode());
            details.add(voucherVo);
        }
        return details;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/5 11:13
     * @Param [voucherDto]
     * @return java.util.List<com.njwd.entity.ledger.vo.VoucherVo>
     * @Description 凭证列表 打印查询
     */
    @Override
    public VoucherVo findVoucherForPrint(VoucherDto voucherDto) {
        List<VoucherVo> voucherVos = voucherMapper.findPage(voucherDto);
        if(!FastUtils.checkNullOrEmpty(voucherVos)){
            List<VoucherVo> details = new ArrayList<>();
            VoucherVo voucherVo;
            for (VoucherVo vo : voucherVos) {
                voucherVo = findDetail(vo);
                voucherVo.setAbstractContent(vo.getAbstractContent());
                voucherVo.setCredentialWordCode(auxiliaryAccountingService.convertData(voucherVo.getCredentialWord()) + "-" + voucherVo.getMainCode());
                details.add(voucherVo);
            }
            //获取总账参数 根据其中凭证打印参数 将数据分割
            VoucherVo voucherListAfterSpilt = new VoucherVo();
            if(!FastUtils.checkNullOrEmpty(details)){
                voucherListAfterSpilt = getVoucherVosAfterSpilt(details);
            }
            return voucherListAfterSpilt;
        }
        return null;
    }

    /**
     * @return com.njwd.support.Result
     * @Author ZhuHC
     * @Date 2019/8/19 17:15
     * @Param [voucherDto]
     * @Description 凭证 打印 查询
     */
    @Override
    public VoucherVo findVoucherInfoAndDetail(VoucherDto voucherDto) {
        List<VoucherVo> voucherList = new ArrayList<>();
        //根据 总账账簿 核算主体 会计期间 查询 凭证
        getVoucherVos(voucherDto, voucherList);
        List<Long> voucherIdList = new ArrayList<>();
        if(!FastUtils.checkNullOrEmpty(voucherList)){
            for (VoucherVo voucherVo : voucherList) {
                voucherIdList.add(voucherVo.getId());
            }
        }
        if (!voucherIdList.isEmpty()) {
            //查分录
            List<VoucherEntry> voucherEntries = voucherEntryService.list(new LambdaQueryWrapper<VoucherEntry>().in(VoucherEntry::getVoucherId, voucherIdList).orderByAsc(VoucherEntry::getRowNum));
            // 查辅助核算明细
            List<VoucherEntryAuxiliary> auxiliaries = voucherEntryAuxiliaryService.list(new LambdaQueryWrapper<VoucherEntryAuxiliary>().in(VoucherEntryAuxiliary::getVoucherId, voucherIdList));
            //查会计科目: 根据凭证分录 获得 需要查询的所有会计科目ID,获得会计科目详情
            List<AccountSubjectVo> accountSubjectVoList = new ArrayList<>();
            if(!FastUtils.checkNullOrEmpty(voucherEntries)){
                accountSubjectVoList = getSubjectListByVoucherEntry(voucherEntries);
            }
            //凭证分录 数据转换
            List<VoucherEntryVo> voucherEntryVos = new ArrayList<>();
            if(!FastUtils.checkNullOrEmpty(voucherEntries) && !FastUtils.checkNullOrEmpty(accountSubjectVoList)){
                voucherEntryVos = getVoucherEntryVosByVoucherEntry(voucherEntries, accountSubjectVoList);
            }
            //数据拼接 辅助核算明细-核算项目
            Map<Long, List<VoucherEntryAuxiliaryVo>> auxiliaryVoMap = mergeAuxiliaryAndProject(auxiliaries);
            if(!FastUtils.checkNullOrEmpty(voucherEntryVos)){
                //将 辅助核算明细数据加入 分录数据中
                for (VoucherEntryVo voucherEntryVo : voucherEntryVos) {
                    for (Map.Entry<Long, List<VoucherEntryAuxiliaryVo>> entry : auxiliaryVoMap.entrySet()) {
                        if (entry.getKey().equals(voucherEntryVo.getId())) {
                            voucherEntryVo.setVoucherEntryAuxiliaryVos(entry.getValue());
                        }
                    }
                }
                //同一凭证下的分录数据归类
                Map<Long, List<VoucherEntryVo>> voucherEntryVoMap = new LinkedHashMap<>();
                //辅助核算项目
                for (VoucherEntryVo voucherEntryVo : voucherEntryVos) {
                    voucherEntryVoMap.computeIfAbsent(voucherEntryVo.getVoucherId(), k -> new LinkedList<>()).add(voucherEntryVo);
                }
                //将 分录数据加入 凭证数据中
                if(!FastUtils.checkNullOrEmpty(voucherList)){
                    for (VoucherVo voucherVo : voucherList) {
                        for (Map.Entry<Long, List<VoucherEntryVo>> entry : voucherEntryVoMap.entrySet()) {
                            if (entry.getKey().equals(voucherVo.getId())) {
                                voucherVo.setVoucherEntryVos(entry.getValue());
                            }
                        }
                    }
                }
            }
        }
        //获取总账参数 根据其中凭证打印参数 将数据分割
        VoucherVo voucherListAfterSpilt = new VoucherVo();
        if(!FastUtils.checkNullOrEmpty(voucherList)){
            voucherListAfterSpilt = getVoucherVosAfterSpilt(voucherList);
        }
        return voucherListAfterSpilt;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/30 17:00
     * @Param [voucherList]
     * @return java.util.List<com.njwd.entity.ledger.vo.VoucherVo>
     * @Description fenge 分割 打印数据
     */
    private VoucherVo getVoucherVosAfterSpilt(List<VoucherVo> voucherList) {
        SysUserVo user = UserUtils.getUserVo();
        ParameterSetVo parameterSetVo = getParameterSet(user);
        VoucherVo resultVo = new VoucherVo();
        if(null != parameterSetVo){
            Byte printModel = FastUtils.getParamSetSub(parameterSetVo, Constant.IsCompany.GROUP_ID, Constant.ParameterSetKey.PRINT_MODEL).getValue().byteValue();
            resultVo.setPrintModel(printModel);
            //每版数据数
            int num;
            if (LedgerConstant.PrintModel.A41.equals(printModel)) {
                num = LedgerConstant.PrintModelParameter.A41_NUM;
            } else if (LedgerConstant.PrintModel.A42.equals(printModel)) {
                num = LedgerConstant.PrintModelParameter.A42_NUM;
            } else if (LedgerConstant.PrintModel.A43.equals(printModel)){
                num = LedgerConstant.PrintModelParameter.A43_NUM;
            } else {
                resultVo.setVoucherVoList(voucherList);
                return resultVo;
            }

            List<VoucherVo> voucherListAfterSpilt = new ArrayList<>();
            List<VoucherEntryVo> voucherEntryVos;
            VoucherVo newVoucherVo;
            boolean flag;
            BigDecimal debitAmount ;
            BigDecimal creditAmount ;
            List<VoucherEntryVo> newVos;
            VoucherEntryVo vo ;
            for (VoucherVo voucherVo : voucherList) {
                //分录数据
                voucherEntryVos = voucherVo.getVoucherEntryVos();
                flag = true;
               if(!FastUtils.checkNullOrEmpty(voucherEntryVos)){
                   while (flag) {
                       newVoucherVo = new VoucherVo();
                       FastUtils.copyProperties(voucherVo, newVoucherVo);
                       debitAmount = BigDecimal.ZERO;
                       creditAmount = BigDecimal.ZERO;
                       //分录条数 大于 所能展示的条数时  进行数据分割
                       if (voucherEntryVos.size() > num) {
                           //按最大值分出数据
                           newVos = new ArrayList<>();
                           for (int i = 0; i < num; i++) {
                               vo = voucherEntryVos.get(i);
                               newVos.add(vo);
                               debitAmount = debitAmount.add(vo.getCreditAmount());
                               creditAmount = creditAmount.add(vo.getCreditAmount());
                           }
                           newVoucherVo.setVoucherEntryVos(newVos);
                           newVoucherVo.setDebitAmount(debitAmount);
                           newVoucherVo.setCreditAmount(creditAmount);
                           voucherListAfterSpilt.add(newVoucherVo);
                           //移除 分割出来的数据
                           voucherEntryVos.removeAll(newVos);
                       } else {
                           //小于 时  结束分割循环
                           for (VoucherEntryVo entryVo : voucherEntryVos) {
                               debitAmount = debitAmount.add(entryVo.getCreditAmount());
                               creditAmount = creditAmount.add(entryVo.getCreditAmount());
                           }
                           newVoucherVo.setDebitAmount(debitAmount);
                           newVoucherVo.setCreditAmount(creditAmount);
                           //切割数据 最后一段
                           newVoucherVo.setIsEndFlag(Constant.Number.INITIAL);
                           newVoucherVo.setVoucherEntryVos(voucherEntryVos);
                           voucherListAfterSpilt.add(newVoucherVo);
                           flag = false;
                       }
                   }
               }
            }
            resultVo.setVoucherVoList(voucherListAfterSpilt);
        }else{
            resultVo.setVoucherVoList(voucherList);
        }
        return resultVo;
    }

    /**
     * @return void
     * @Author ZhuHC
     * @Date 2019/8/19 17:16
     * @Param [voucherDto, voucherList]
     * @Description 设置 凭证 查询条件
     */
    private void getVoucherVos(VoucherDto voucherDto, List<VoucherVo> voucherList) {
        List<List<Long>> accountBookIdLists = voucherDto.getAccountBookIdLists();
        List<List<Long>> accountBookEntityIdLists = voucherDto.getAccountBookEntityIdLists();
        List<Integer> periodYearNumLists = voucherDto.getPeriodYearNumLists();
        List<Long> accountBookIdList;
        List<Long> accountBookEntityIdList;
        Integer periodYearNum;
        List<VoucherVo> voucherVos;
        VoucherDto dto;
        AccountBookPeriod accountBookPeriod = new AccountBookPeriod();
        ManagerInfo manageInfo = new ManagerInfo();
        List<Object> list;
        Long rootId;
        for (int i = 0; i < accountBookIdLists.size(); i++) {
            accountBookIdList = accountBookIdLists.get(i);
            accountBookEntityIdList = accountBookEntityIdLists.get(i);
            dto = new VoucherDto();
            dto.setAccountBookIds(accountBookIdList);
            dto.setAccountBookEntityIds(accountBookEntityIdList);
            if (null != periodYearNumLists && !periodYearNumLists.isEmpty()) {
                periodYearNum = periodYearNumLists.get(i);
                dto.setPeriodYearNum(periodYearNum);
            } else {
                dto.setVoucherDate(voucherDto.getVoucherDate());
                dto.setCredentialWord(voucherDto.getCredentialWord());
                dto.setMainCodes(voucherDto.getMainCodes());
                dto.setChildCode(voucherDto.getChildCode());
            }
            voucherVos = voucherMapper.findByEntriesAndPeriod(dto);
            //更新账簿期间表的最后打印时间
            if(!FastUtils.checkNullOrEmpty(voucherVos)){
                rootId = UserUtils.getUserVo().getRootEnterpriseId();
                manageInfo.setLastPrintTime(new Date());
                list = FastUtils.getManagerList(manageInfo);
                accountBookPeriod.setManageInfos(manageInfo);
                accountBookPeriod.setRootEnterpriseId(rootId);
                accountBookPeriod.setAccountBookId(accountBookIdList.get(0));
                accountBookPeriod.setPeriodYearNum(dto.getPeriodYearNum());
                accountBookPeriod.setSystemSign(Constant.SourceSystem.LEDGER);
                accountBookPeriodMapper.updatePeriodPrintTime(accountBookPeriod,list);
                voucherList.addAll(voucherVos);
            }
        }
    }

    /**
     * 刘遵通
     * 检查审核
     *
     * @param voucherDto
     * @return
     */
    @Override
    public BatchResult checkApprove(VoucherDto voucherDto, BatchResult result) {
        //登录用户
        SysUserVo user = UserUtils.getUserVo();
        //获取总账
        ParameterSetVo parameterSet = getParameterSet(user);
        List<VoucherVo> vouchers = new ArrayList<VoucherVo>();
        List<Long> ids = new LinkedList<>();
        List<String> sourceCodes = new LinkedList<>();
        //初始化返回对象
        //result = initData();
        //获取凭证列表
        List<VoucherVo> voucherList = voucherMapper.findVoucherList(voucherDto);
        for (VoucherVo voucher : voucherList) {
            ReferenceDescription rd = new ReferenceDescription();
            //1.列表下审核需校验凭证是否被查看(不做并发锁定判断)
            //把对象list转换成map
            Map<Long, VoucherDto> map = listToMap(voucherDto.getEditVoucherList());
            Integer version = map.get(voucher.getId()).getVersion();
            //获取系统参数值
            Byte isCashierReview = FastUtils.getParamSetSub(parameterSet, voucher.getAccountBookId(), Constant.ParameterSetKey.IS_CASHIER_REVIEW).getValue().byteValue();
            Byte isReviewBeforeApprove = FastUtils.getParamSetSub(parameterSet, voucher.getAccountBookId(), Constant.ParameterSetKey.IS_REVIEW_BEFORE_APPROVE).getValue().byteValue();
            Byte isAddApproveSame = FastUtils.getParamSetSub(parameterSet, voucher.getAccountBookId(), Constant.ParameterSetKey.IS_ADD_APPROVE_SAME).getValue().byteValue();
            if (!voucher.getVersion().equals(version)) {
                //获取 凭证字类型，主账号，子账号
                String mainCodeOrChildCode = getMainCodeOrChildCode(voucher);
                rd.setBusinessId(voucher.getId());
                rd.setReferenceDescription(String.format(LedgerConstant.MessAge.USER_BEING_OPERATION, mainCodeOrChildCode));
                result.getFailList().add(rd);
                continue;
            }
            //4.检查凭证状态是否是已过账
            else if (voucher.getStatus() == LedgerConstant.VoucherStatus.POST && voucher.getPostingStatus() == Constant.Is.YES) {
                //获取 凭证字类型，主账号，子账号
                String mainCodeOrChildCode = getMainCodeOrChildCode(voucher);
                rd.setBusinessId(voucher.getId());
                rd.setReferenceDescription(String.format(LedgerConstant.MessAge.VOUCHER_POST, mainCodeOrChildCode));
                result.getFailList().add(rd);
                continue;
            }
            // 2.列表下检查凭证是否已审核
            //获取当前对象的凭证状态：-1草稿，0未审核，1待过账，2.已过账。审核状态:0未审核，1以审核
            else if (voucher.getStatus() == LedgerConstant.VoucherStatus.POSTING && voucher.getApproveStatus() == Constant.Is.YES) {
                //获取 凭证字类型，主账号，子账号
                String mainCodeOrChildCode = getMainCodeOrChildCode(voucher);
                rd.setBusinessId(voucher.getId());
                rd.setReferenceDescription(String.format(LedgerConstant.MessAge.VOUCHER_POSTING, mainCodeOrChildCode));
                result.getFailList().add(rd);
                continue;
            }
            //5.检查是否协同凭证
            else if (voucher.getSourceType() == LedgerConstant.SourceType.COLLABORATE) {
                if (voucherList.size() == Constant.Number.ONE) {
                    rd.setBusinessId(voucher.getId());
                    rd.setReferenceDescription(LedgerConstant.MessAge.COORDINATION_VOUCHER_NOTAPPROVE);
                    result.getFailList().add(rd);
                    continue;
                } else {
                    ids.add(voucher.getId());
                    continue;
                }
            }
            //3.检查【参数选项】 是否需要出纳复核=是 勾选，【出纳复核是否在审核之前】=是，勾选，先复核
            else if (isCashierReview == Constant.Is.YES && isReviewBeforeApprove == Constant.Is.YES) {
                //获取 凭证字类型，主账号，子账号
                String mainCodeOrChildCode = getMainCodeOrChildCode(voucher);
                //来源是协同的凭证，凭证未复核
                if (voucher.getSourceType() == LedgerConstant.SourceType.COLLABORATE && voucher.getReviewStatus() == Constant.Is.NO) {
                    rd.setReferenceDescription(String.format(LedgerConstant.MessAge.COORDINATION_VOUCHER, mainCodeOrChildCode));
                    rd.setBusinessId(voucher.getId());
                    result.getFailList().add(rd);
                    continue;
                    //凭证需复核但未复核
                } else if (voucher.getReviewStatus() == Constant.Is.NO && voucher.getCashCheckType() != Constant.CashFlowCheckType.NEEDLESS) {
                    rd.setReferenceDescription(String.format(LedgerConstant.MessAge.VOUCHER_PLEASE_REVIEW, mainCodeOrChildCode));
                    rd.setBusinessId(voucher.getId());
                    result.getFailList().add(rd);
                    continue;
                }
            }
            //6.检查账簿期间是否锁定，锁定期间的凭证不允许审核
            //7.检查【参数信息】【允许凭证制单人和审核人是同一人】没选
            else if (isAddApproveSame == Constant.Is.NO) {
                if (voucher.getCreatorId().equals(user.getUserId())) {
                    rd.setBusinessId(voucher.getId());
                    rd.setReferenceDescription(LedgerConstant.MessAge.APPROVEUSER_MAKINGUSER_DIFFERENCE);
                    result.getFailList().add(rd);
                    continue;
                }
            }
            //添加审核成功的
            sourceCodes.add(voucher.getId().toString());
            addSuccess(vouchers, rd, voucher);
            //获取主账号，子账号
            String mainCodeOrChildCode = getMainCodeOrChildCode(voucher);
            rd.setReferenceDescription(String.format(LedgerConstant.MessAge.APPROVE_SUCCESS, mainCodeOrChildCode));
            result.getSuccessDetailsList().add(rd);
        }
        //防止没有数据
        if (result.getSuccessDetailsList().size() == 0) {
            return result;
        }
        List<VoucherVo> voucherListBySourceCode = voucherMapper.findVoucherListBySourceCode(sourceCodes);
        vouchers.addAll(voucherListBySourceCode);
        //更新 审核人，审核人ID，审核状态，凭证状态，审核时间
        //  Voucher v = new Voucher();
        voucherDto.setApproverName(user.getName());
        voucherDto.setApproverId(user.getUserId());
        voucherDto.setApproveStatus(Constant.Is.YES);
        voucherDto.setStatus(LedgerConstant.VoucherStatus.POSTING);
        Date date = new Date();
        voucherDto.setApproveTime(date);
        //   FastUtils.copyProperties(voucherDto,v);
        // int i =voucherMapper.update(v,new QueryWrapper<Voucher>().in("id",result.getSuccessList()));
        int i = voucherMapper.updateVoucher(voucherDto, vouchers);
        System.out.println(i);
        return result;
    }

    /**
     * 刘遵通 添加成功的
     *
     * @param vouchers
     * @param rd
     * @param
     * @param voucher
     */
    private void addSuccess(List<VoucherVo> vouchers, ReferenceDescription rd, VoucherVo voucher) {
        vouchers.add(voucher);
        rd.setBusinessId(voucher.getId());
    }

    /**
     * 获取参数设置
     *
     * @param operator operator
     * @return com.njwd.entity.ledger.vo.ParameterSetVo
     * @author xyyxhcj@qq.com
     * @date 2019/10/22 9:20
     **/
    private ParameterSetVo getParameterSet(SysUserVo operator) {
        return commonService.getParameterSet(operator);
    }

    /**
     * 刘遵通
     * 反审核
     *
     * @param voucherDto
     * @return
     */
    @Override
    public BatchResult reversalApprove(VoucherDto voucherDto, BatchResult result) {
        //登录用户
        SysUserVo user = UserUtils.getUserVo();
        //获取总账
        ParameterSetVo parameterSet = getParameterSet(user);
         //允许审核人和反审核人不是同一人
        List<VoucherVo> vouchers = new ArrayList<VoucherVo>();
        List<Long> ids = new LinkedList<>();
        List<String> sourceCodes = new LinkedList<>();
        //初始化返回对象
        //result = initData();
        //获取凭证列表
        List<VoucherVo> voucherList = voucherMapper.findVoucherList(voucherDto);
        for (VoucherVo voucher : voucherList) {
            ReferenceDescription rd = new ReferenceDescription();
            // 1.列表下反审核需校验凭证是否被查看(不做并发锁定判断)
            //把list对象转换成map
            Map<Long, VoucherDto> map = listToMap(voucherDto.getEditVoucherList());
            Integer version = map.get(voucher.getId()).getVersion();
            //获取系统参数值
            Byte isApproveNotSame = FastUtils.getParamSetSub(parameterSet, voucher.getAccountBookId(), Constant.ParameterSetKey.IS_APPROVE_NOT_SAME).getValue().byteValue();
            if (!voucher.getVersion().equals(version)) {
                //获取 凭证字类型，主账号，子账号
                String mainCodeOrChildCode = getMainCodeOrChildCode(voucher);
                rd.setBusinessId(voucher.getId());
                rd.setReferenceDescription(String.format(LedgerConstant.MessAge.USER_BEING_OPERATION, mainCodeOrChildCode));
                result.getFailList().add(rd);
                continue;
            }
            // 2.列表下检查凭证是否已审核
            //获取当前对象的凭证状态：-1草稿，0未审核，1待过账，2.已过账。审核状态:0未审核，1已审核
            else if (voucher.getStatus() == LedgerConstant.VoucherStatus.PENDING && voucher.getApproveStatus() == Constant.Is.NO) {
                //获取 凭证字类型，主账号，子账号
                String mainCodeOrChildCode = getMainCodeOrChildCode(voucher);
                rd.setBusinessId(voucher.getId());
                rd.setReferenceDescription(String.format(LedgerConstant.MessAge.VOUCHER_PLEASE_APPROVE, mainCodeOrChildCode));
                result.getFailList().add(rd);
                continue;
            }
            //3.列表下检查凭证状态是否是已过账
            else if (voucher.getStatus() == LedgerConstant.VoucherStatus.POST && voucher.getPostingStatus() == Constant.Is.YES) {
                //获取 凭证字类型，主账号，子账号
                String mainCodeOrChildCode = getMainCodeOrChildCode(voucher);
                rd.setBusinessId(voucher.getId());
                rd.setReferenceDescription(String.format(LedgerConstant.MessAge.VOUCHER_ALREADY_POSTING, mainCodeOrChildCode));
                result.getFailList().add(rd);
                continue;
            }
            //4.检查是否协同凭证
            else if (voucher.getSourceType() == LedgerConstant.SourceType.COLLABORATE) {
                if (voucherList.size() == Constant.Number.ONE) {
                    rd.setBusinessId(voucher.getId());
                    rd.setReferenceDescription(LedgerConstant.MessAge.COORDINATION_VOUCHER_NOT_BACKAPPROVE);
                    result.getFailList().add(rd);
                    continue;
                } else {
                    ids.add(voucher.getId());
                    continue;
                }
            }
            //5.检查账簿期间是否锁定，锁定期间的凭证不允许反审核
            //6.检查【参数信息】【允许审核和反审核不是同一人】 没选
            else if (isApproveNotSame == Constant.Is.NO) {
                if (!voucher.getApproverId().equals(user.getUserId())) {
                    //获取 凭证字类型，主账号，子账号
                    String mainCodeOrChildCode = getMainCodeOrChildCode(voucher);
                    rd.setBusinessId(voucher.getId());
                    rd.setReferenceDescription(String.format(LedgerConstant.MessAge.APPROVEUSER_BACKAPPROVEUSER_MUST_IDENTICAL, mainCodeOrChildCode));
                    result.getFailList().add(rd);
                    continue;
                }
            }
            //添加反审核成功的
            sourceCodes.add(voucher.getId().toString());
            addSuccess(vouchers, rd, voucher);
            String mainCodeOrChildCode = getMainCodeOrChildCode(voucher);
            rd.setReferenceDescription(String.format(LedgerConstant.MessAge.BACKAPPROVE_SUCCESS, mainCodeOrChildCode));
            result.getSuccessDetailsList().add(rd);
        }
        //防止没有数据
        if (result.getSuccessDetailsList().size() == 0) {
            return result;
        }
        List<VoucherVo> voucherListBySourceCode = voucherMapper.findVoucherListBySourceCode(sourceCodes);
        vouchers.addAll(voucherListBySourceCode);
        //更新 审核人，审核人ID，审核状态，凭证状态，审核时间
        //  Voucher v = new Voucher();
        voucherDto.setApproverName("");
        voucherDto.setApproverId(null);
        voucherDto.setApproveStatus(Constant.Is.NO);
        voucherDto.setStatus(LedgerConstant.VoucherStatus.PENDING);
        voucherDto.setApproveTime(null);
        int i = voucherMapper.updateVoucher(voucherDto, vouchers);
        System.out.println(i);
        return result;
    }

    /**
     * 刘遵通
     * 复核
     *
     * @param voucherDto
     * @return
     */
    @Override
    public BatchResult checkReview(VoucherDto voucherDto, BatchResult result) {
        //登录用户
        SysUserVo user = UserUtils.getUserVo();
        //获取总账
        ParameterSetVo parameterSet = getParameterSet(user);
        List<VoucherVo> vouchers = new ArrayList<VoucherVo>();
        //初始化返回对象
        //result = initData();
        //获取凭证列表
        List<VoucherVo> voucherList = voucherMapper.findVoucherList(voucherDto);
        for (VoucherVo voucher : voucherList) {
            ReferenceDescription rd = new ReferenceDescription();
            //1.列表下复核需校验凭证是否被查看(不做并发锁定判断)
            //把list对象转换成map
            Map<Long, VoucherDto> map = listToMap(voucherDto.getEditVoucherList());
            Integer version = map.get(voucher.getId()).getVersion();
            //获取系统参数值
            Byte isCashierReview =  FastUtils.getParamSetSub(parameterSet, voucher.getAccountBookId(), Constant.ParameterSetKey.IS_CASHIER_REVIEW).getValue().byteValue();
            if (!voucher.getVersion().equals(version)) {
                //获取 凭证字类型，主账号，子账号
                String mainCodeOrChildCode = getMainCodeOrChildCode(voucher);
                rd.setBusinessId(voucher.getId());
                rd.setReferenceDescription(String.format(LedgerConstant.MessAge.USER_BEING_OPERATION, mainCodeOrChildCode));
                result.getFailList().add(rd);
                continue;
            }
            //2.列表下检查凭证是否需要复核 未勾选 0否 1是  ||  非现金类凭证 为 -1
            else if (isCashierReview == Constant.Is.NO || voucher.getCashCheckType() == Constant.CashFlowCheckType.NEEDLESS) {
                //获取 凭证字类型，主账号，子账号
                String mainCodeOrChildCode = getMainCodeOrChildCode(voucher);
                rd.setBusinessId(voucher.getId());
                rd.setReferenceDescription(String.format(LedgerConstant.MessAge.NONEED_REVIEW, mainCodeOrChildCode));
                result.getFailList().add(rd);
                continue;
            }
            //4.列表下检查凭证状态是否是已过账
            else if (voucher.getStatus() == LedgerConstant.VoucherStatus.POST && voucher.getPostingStatus() == Constant.Is.YES) {
                //获取 凭证字类型，主账号，子账号
                String mainCodeOrChildCode = getMainCodeOrChildCode(voucher);
                rd.setBusinessId(voucher.getId());
                rd.setReferenceDescription(String.format(LedgerConstant.MessAge.VOUCHER_ALREADY_POSTING_REVIEW, mainCodeOrChildCode));
                result.getFailList().add(rd);
                continue;
            }
            //3.列表下检查凭证复核状态是否是已复核
            else if (voucher.getReviewStatus() == Constant.Is.YES) {
                //获取 凭证字类型，主账号，子账号
                String mainCodeOrChildCode = getMainCodeOrChildCode(voucher);
                rd.setBusinessId(voucher.getId());
                rd.setReferenceDescription(String.format(LedgerConstant.MessAge.VOUCHER_ALREADY_REVIEW, mainCodeOrChildCode));
                result.getFailList().add(rd);
                continue;
            }

            //添加复核成功的
            addSuccess(vouchers, rd, voucher);
            String mainCodeOrChildCode = getMainCodeOrChildCode(voucher);
            rd.setReferenceDescription(String.format(LedgerConstant.MessAge.REVIEW_SUCCESS, mainCodeOrChildCode));
            result.getSuccessDetailsList().add(rd);
        }
        //防止没有数据
        if (result.getSuccessDetailsList().size() == 0) {
            return result;
        }
        //更新复核人，复核人ID，复核状态，复核时间
        voucherDto.setReviewerName(user.getName());
        voucherDto.setReviewerId(user.getUserId());
        voucherDto.setReviewStatus(Constant.Is.YES);
        Date date = new Date();
        voucherDto.setReviewTime(date);
        int i = voucherMapper.updateVoucher(voucherDto, vouchers);
        System.out.println(i);
        return result;
    }

    /**
     * 刘遵通
     * 反复核
     *
     * @param voucherDto
     * @return
     */
    @Override
    public BatchResult reversalReview(VoucherDto voucherDto, BatchResult result) {
        //登录用户
        SysUserVo user = UserUtils.getUserVo();
        //获取总账
        ParameterSetVo parameterSet = getParameterSet(user);
        List<VoucherVo> vouchers = new ArrayList<VoucherVo>();
        //初始化返回对象
        //result = initData();
        //获取凭证列表
        List<VoucherVo> voucherList = voucherMapper.findVoucherList(voucherDto);
        for (VoucherVo voucher : voucherList) {
            ReferenceDescription rd = new ReferenceDescription();
            //1.列表下反复核需校验凭证是否被查看(不做并发锁定判断)
            //把list对象转换成map
            Map<Long, VoucherDto> map = listToMap(voucherDto.getEditVoucherList());
            Integer version = map.get(voucher.getId()).getVersion();
            //获取系统参数值
            Byte isReviewNotSame =  FastUtils.getParamSetSub(parameterSet, voucher.getAccountBookId(), Constant.ParameterSetKey.IS_REVIEW_NOT_SAME).getValue().byteValue();
            Byte isReviewBeforeApprove = FastUtils.getParamSetSub(parameterSet, voucher.getAccountBookId(), Constant.ParameterSetKey.IS_REVIEW_BEFORE_APPROVE).getValue().byteValue();
            if (!voucher.getVersion().equals(version)) {
                //获取 凭证字类型 ，主账号，子账号
                String mainCodeOrChildCode = getMainCodeOrChildCode(voucher);
                rd.setBusinessId(voucher.getId());
                rd.setReferenceDescription(String.format(LedgerConstant.MessAge.USER_BEING_OPERATION, mainCodeOrChildCode));
                result.getFailList().add(rd);
                continue;
            }
            //2.列表下检查凭证复核状态是否待复核
            else if (voucher.getReviewStatus() == Constant.Is.NO) {
                //获取 凭证字类型 ，主账号，子账号
                String mainCodeOrChildCode = getMainCodeOrChildCode(voucher);
                rd.setBusinessId(voucher.getId());
                rd.setReferenceDescription(String.format(LedgerConstant.MessAge.VOUCHER_PLEASE_REVIEW, mainCodeOrChildCode));
                result.getFailList().add(rd);
                continue;
            }
            //3.列表下检查凭证状态是否已过账
            else if (voucher.getStatus() == LedgerConstant.VoucherStatus.POST && voucher.getPostingStatus() == Constant.Is.YES) {
                //获取 凭证字类型 ，主账号，子账号
                String mainCodeOrChildCode = getMainCodeOrChildCode(voucher);
                rd.setBusinessId(voucher.getId());
                rd.setReferenceDescription(String.format(LedgerConstant.MessAge.VOUCHER_ALREADY_POSTING_REVIEW, mainCodeOrChildCode));
                result.getFailList().add(rd);
                continue;
            }
            //5.检查参数选项（出纳复核是否在审核之前）选中
            else if (isReviewBeforeApprove == Constant.Is.YES) {
                //判断凭证状态=已审核
                if (voucher.getStatus() == LedgerConstant.VoucherStatus.POSTING && voucher.getApproveStatus() == Constant.Is.YES) {
                    //获取 凭证字类型 ，主账号，子账号
                    String mainCodeOrChildCode = getMainCodeOrChildCode(voucher);
                    rd.setBusinessId(voucher.getId());
                    rd.setReferenceDescription(String.format(LedgerConstant.MessAge.BACKREVIEW_IN_BACKAPPROVE_AFTER, mainCodeOrChildCode));
                    result.getFailList().add(rd);
                    continue;
                }
            }
            //4.检查反复核操作人和复核人（出纳字段）是否为同一人  没选
            else if (isReviewNotSame == Constant.Is.NO && !user.getName().equals(voucher.getReviewerName())) {
                    //获取 凭证字类型 ，主账号，子账号
                    String mainCodeOrChildCode = getMainCodeOrChildCode(voucher);
                    rd.setBusinessId(voucher.getId());
                    rd.setReferenceDescription(String.format(LedgerConstant.MessAge.BACKREVIEWUSER_REVIEWUSER_MUST_IDENTICAL, mainCodeOrChildCode));
                    result.getFailList().add(rd);
                    continue;
            }

            //添加反复核成功的
            addSuccess(vouchers, rd, voucher);
            String mainCodeOrChildCode = getMainCodeOrChildCode(voucher);
            rd.setReferenceDescription(String.format(LedgerConstant.MessAge.BACKREVIEW_SUCCESS, mainCodeOrChildCode));
            result.getSuccessDetailsList().add(rd);
        }
        //防止没有数据
        if (result.getSuccessDetailsList().size() == 0) {
            return result;
        }
        //更新复核人，复核人ID，复核状态，复核时间
        voucherDto.setReviewerName("");
        voucherDto.setReviewerId(null);
        voucherDto.setReviewStatus(Constant.Is.NO);
        voucherDto.setReviewTime(null);
        int i = voucherMapper.updateVoucher(voucherDto, vouchers);
        System.out.println(i);
        return result;
    }

    /**
     * 刘遵通
     * List转map
     *
     * @param editVoucherList
     * @return
     */
    private Map<Long, VoucherDto> listToMap(List<VoucherDto> editVoucherList) {
        Map<Long, List<VoucherDto>> collect = editVoucherList.stream().collect(Collectors.groupingBy(Voucher::getId));
        Map<Long, VoucherDto> map = editVoucherList.stream().collect(Collectors.toMap(VoucherDto::getId, a -> a, (k1, k2) -> k1));
        return map;
    }

    /**
     * @description: 期间和状态查询凭证列表
     * @param: [accountBookPeriod, voucherStatus]
     * @return: java.util.List<com.njwd.entity.ledger.Voucher>
     * @author: xdy
     * @create: 2019-08-12 10-18
     */
    @Override
    public List<Voucher> findVouchersByPeriod(AccountBookPeriodVo accountBookPeriod, List<Byte> voucherStatus) {
        return voucherMapper.findVouchersByPeriod(accountBookPeriod, voucherStatus);
    }

    /**
     * 刘遵通
     * 获取主账号 子账号
     *
     * @param voucher
     * @return
     */
    private String getMainCodeOrChildCode(VoucherVo voucher) {
        StringBuffer sb = new StringBuffer();
        //凭证字类型 1：记 、2：收、3：付、4：转
        Byte credentialWord = voucher.getCredentialWord();
        if (null != credentialWord) {
            if (StringUtil.isNotEmpty(credentialWord.toString())) {
                switch (credentialWord) {
                    case 1:
                        sb.append(LedgerConstant.CredentialWordName.RECORD);
                        break;
                    case 2:
                        sb.append(LedgerConstant.CredentialWordName.RECEIVE);
                        break;
                    case 3:
                        sb.append(LedgerConstant.CredentialWordName.PAY);
                        break;
                    default:
                        sb.append(LedgerConstant.CredentialWordName.TRANSFER);
                        break;
                }
            }
        }
        //凭证主号
        Integer mainCode = voucher.getMainCode();
        if (null != mainCode) {
            if (StringUtil.isNotEmpty(mainCode.toString())) {
                sb.append(mainCode.toString());
            }
        }
        //凭证字号
        Integer childCode = voucher.getChildCode();
        if (null != childCode) {
            if (StringUtil.isNotEmpty(childCode.toString())) {
                sb.append("-").append(childCode.toString());
            }
        }
        return sb.toString();
    }

    /**
     * 刘遵通
     * 初始化返回对象
     *
     * @return
     */
    private BatchResult initData() {
        BatchResult result = new BatchResult();
        result.setFailList(new ArrayList<>());
        result.setSuccessList(new ArrayList<>());
        result.setSuccessDetailsList(new ArrayList<>());
        return result;
    }

    /**
     * @description: 批量更新凭证号
     * @param: [vouchers]
     * @return: int
     * @author: xdy
     * @create: 2019-08-12 10-19
     */
    @Override
    public int updateVoucherCode(List<Voucher> vouchers) {
        return voucherMapper.updateVoucherCode(vouchers);
    }

    /**
     * @param
     * @return
     * @description 过账时修改凭证状态 包括过账状态
     * @author fancl
     * @date 2019/8/19
     */
    @Override
    public int updateVoucherStatusForPeriod(AccountBookPeriod accountBookPeriod, Voucher voucher) {
        return voucherMapper.updateVoucherStatusForPeriod(accountBookPeriod, voucher);
    }

    /**
     * @param
     * @return
     * @description 根据条件查凭证
     * @author fancl
     * @date 2019/8/15
     */
    @Override
    public List<VoucherVo> findByCondition(Voucher voucher, List<Byte> voucherStatus) {
        return voucherMapper.findByCondition(voucher, voucherStatus);
    }

    /**
     * 批量删除
     *
     * @param voucherDto  voucherDto
     * @param batchResult batchResult
     * @return com.njwd.support.BatchResult
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:36
     **/
    @Override
    public BatchResult deleteBatch(VoucherDto voucherDto, BatchResult batchResult) {
        List<Long> successList = new LinkedList<>();
        batchResult.setSuccessList(successList);
        // 来源方式是协同的自动忽略
        List<Long> ids = new LinkedList<>();
        List<VoucherDto> editVoucherList = voucherDto.getEditVoucherList();
        Map<Long, VoucherDto> voucherDtoMap = new LinkedHashMap<>();
        for (VoucherDto dto : editVoucherList) {
            ids.add(dto.getId());
            voucherDtoMap.put(dto.getId(), dto);
        }
        // 查出所有凭证，再过滤
        List<VoucherDto> existVouchers = getExistVouchersByIds(ids);
        Map<Long, VoucherDto> existVoucherMap = new LinkedHashMap<>();
        existVouchers.forEach(voucher -> existVoucherMap.put(voucher.getId(), voucher));
        Iterator<Map.Entry<Long, VoucherDto>> iterator = voucherDtoMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, VoucherDto> entry = iterator.next();
            Long voucherId = entry.getKey();
            if (existVoucherMap.get(voucherId) == null) {
                // 过滤已删除数据
                iterator.remove();
                successList.add(voucherId);
            }
        }
        // 记录需更新余额/发生额的凭证ID
        final List<Long> needUpdateBalances = new LinkedList<>();
        checkForDelete(editVoucherList, voucherDtoMap, existVouchers, batchResult.getFailList(), needUpdateBalances);
        if (!voucherDtoMap.isEmpty()) {
            // 查出生成的协同凭证,合并所有待删除凭证
            List<String> sourceCodes = new LinkedList<>();
            for (Long id : voucherDtoMap.keySet()) {
                sourceCodes.add(id.toString());
            }
            List<VoucherDto> removeVouchers = new LinkedList<>();
            List<Voucher> vouchers = voucherMapper.selectList(new LambdaQueryWrapper<Voucher>()
                    .in(Voucher::getSourceType, LedgerConstant.SourceType.COLLABORATE, LedgerConstant.SourceType.COMPANY_COLL)
                    .in(Voucher::getSourceCode, sourceCodes)
                    // 过滤已删除的协同凭证
                    .eq(Voucher::getIsDel, Constant.Is.NO)
                    .select(Voucher::getId,
                            Voucher::getVersion,
                            Voucher::getAccountBookId,
                            Voucher::getAccountBookEntityId,
                            Voucher::getStatus,
                            Voucher::getPostingPeriodYear,
                            Voucher::getPostingPeriodNum));
            for (Voucher voucher : vouchers) {
                needUpdateBalances.add(voucher.getId());
                VoucherDto voucherTemp = new VoucherDto();
                FastUtils.copyProperties(voucher, voucherTemp);
                removeVouchers.add(voucherTemp);
            }
            if (!needUpdateBalances.isEmpty()) {
                removeVouchers.addAll(voucherDtoMap.values());
                updateBalanceForRemoveVouchers(needUpdateBalances, removeVouchers);
            }
            if (!removeVouchers.isEmpty()) {
                // 逻辑删除
                voucherMapper.deleteBatch(removeVouchers);
                removeVouchers.forEach(voucher -> successList.add(voucher.getId()));
            }
        }
        return batchResult;
    }

    /**
     * 删除凭证前.更新余额表
     *
     * @param voucherIds     voucherIds
     * @param removeVouchers removeVouchers
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:37
     **/
    @Override
    public void updateBalanceForRemoveVouchers(Collection<Long> voucherIds, List<VoucherDto> removeVouchers) {
        // 取出所有涉及的分录+核算明细+现金流量
        LinkedList<VoucherEntryDto> entryList = voucherEntryService.findList(voucherIds);
        LinkedList<VoucherEntryAuxiliaryDto> entryAuxiliaryList = voucherEntryAuxiliaryService.findList(voucherIds);
        List<VoucherEntryCashFlowDto> entryCashFlowList = voucherEntryCashFlowService.findList(voucherIds);
        mergeVoucherListDetail(removeVouchers, entryList, entryAuxiliaryList, entryCashFlowList);
        // 更新发生额
        for (VoucherDto voucher : removeVouchers) {
            updateBalance(voucher, Constant.BalanceUpdateType.SUBTRACT);
        }
    }

    /**
     * 获取存在的凭证
     *
     * @param voucherIds voucherIds
     * @return java.util.List<com.njwd.entity.ledger.dto.VoucherDto>
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:38
     **/
    @Override
    public List<VoucherDto> getExistVouchersByIds(Collection<Long> voucherIds) {
        return voucherMapper.findExistListByIds(voucherIds);
    }

    /**
     * 设置批量操作的失败信息
     *
     * @param voucherDtoMap voucherDtoMap
     * @param failList      failList
     * @param voucherId     voucherId
     * @param errorCode     errorCode
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:38
     **/
    private void setFailDescription(Map<Long, VoucherDto> voucherDtoMap, List<ReferenceDescription> failList, Long voucherId, ResultCode errorCode) {
        ReferenceDescription failDescription = new ReferenceDescription();
        failDescription.setBusinessId(voucherId);
        failDescription.setReferenceDescription(errorCode.message);
        failList.add(failDescription);
        voucherDtoMap.remove(voucherId);
    }

    /**
     * 获取制单日期
     *
     * @param voucherDto voucherDto
     * @return com.njwd.entity.ledger.vo.VoucherVo
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:38
     **/
    @Override
    public VoucherVo generateVoucherDate(VoucherDto voucherDto) {
        @NotNull SysUserVo operator = UserUtils.getUserVo();
        FastUtils.checkParams(voucherDto.getVoucherDateType());
        return generateVoucherDate(voucherDto, voucherDto.getVoucherDateType());
    }

    /**
     * 生成系统日期
     *
     * @param voucherDto   voucherDto
     * @param generateType Constant.VoucherDateType 系统日期/上一张凭证日期
     * @return 返回制单日期及对应的期间
     * @author xyyxhcj@qq.com
     * @date 2019/8/22 10:51
     **/
    private VoucherVo generateVoucherDate(VoucherDto voucherDto, Byte generateType) {
        Date voucherDate = null;
        Date now = DateUtils.beginOfDate(new Date());
        // 当期
        AccountBookPeriod currentAccountBookPeriod = null;
        // 获取所有未结账期间
        List<AccountBookPeriod> accountBookPeriods = accountBookPeriodMapper.selectList(new LambdaQueryWrapper<AccountBookPeriod>()
                .eq(AccountBookPeriod::getAccountBookId, voucherDto.getAccountBookId())
                .eq(AccountBookPeriod::getStatus, Constant.Status.ON)
                .eq(AccountBookPeriod::getSystemSign, Constant.SystemSignValue.LEDGER)
                .eq(AccountBookPeriod::getIsSettle, Constant.Is.NO)
                .orderByAsc(AccountBookPeriod::getPeriodYear)
                .orderByAsc(AccountBookPeriod::getPeriodNum));
        if (accountBookPeriods.isEmpty()) {
            throw new ServiceException(ResultCode.ACCOUNT_PERIOD_OFF);
        }
        Date start;
        Date end;
        for (AccountBookPeriod accountBookPeriod : accountBookPeriods) {
            start = accountBookPeriod.getStartDate();
            end = accountBookPeriod.getEndDate();
            if (start == null || end == null || start.after(end)) {
                throw new ServiceException(ResultCode.DATA_ERROR, accountBookPeriod);
            }
            if (DateUtils.isBetween(now, start, end)) {
                currentAccountBookPeriod = accountBookPeriod;
                break;
            }
        }
        if (currentAccountBookPeriod == null) {
            currentAccountBookPeriod = accountBookPeriods.get(accountBookPeriods.size() - 1);
        }
        if (generateType == null) {
            // 管理员无配置数据
            throw new ServiceException(ResultCode.PERMISSION_NOT);
        }
        /* 2019/8/26去除序时控制   if (Constant.Is.YES.equals(parameterSet.getIsAddOrderTime())) {
            // 序时控制时取当期最晚的制单日期
            Voucher lastVoucher = voucherMapper.selectOne(new LambdaQueryWrapper<Voucher>()
                    .eq(Voucher::getAccountBookId, voucherDto.getAccountBookId())
                    .eq(Voucher::getAccountBookEntityId, voucherDto.getAccountBookEntityId())
                    .eq(Voucher::getPostingPeriodYear, currentAccountBookPeriod.getPeriodYear())
                    .eq(Voucher::getPostingPeriodNum, currentAccountBookPeriod.getPeriodNum())
                    .orderByDesc(Voucher::getCreateTime).last(Constant.ConcatSql.LIMIT_1));
            if (lastVoucher == null) {
                voucherDate = currentAccountBookPeriod.getStartDate();
            } else {
                voucherDate = lastVoucher.getVoucherDate();
            }
        } else */
        if (Constant.VoucherDateType.SYSTEM == generateType) {
            // 取当前最小未结账月份
            AccountBookPeriod firstAccountBookPeriod = accountBookPeriods.get(0);
            if (DateUtils.isBetween(now, firstAccountBookPeriod.getStartDate(), firstAccountBookPeriod.getEndDate())) {
                // 当最小未结账期间等于当前期间则制单日期取当前日期
                voucherDate = now;
            } else if (DateUtils.compareDate(firstAccountBookPeriod.getEndDate(), now) < 1) {
                // 当最小未结账期间小于当前期间则制单日期取最小未结账期间的月末最后一天
                voucherDate = firstAccountBookPeriod.getEndDate();
            } else {
                // 当最小未结账期间大于当前日期则制单日期取最小未结账期间的月初第一天
                voucherDate = firstAccountBookPeriod.getStartDate();
            }
        } else if (Constant.VoucherDateType.LAST_VOUCHER == generateType) {
            // 从打开的期间中取上一张凭证日期
            Voucher lastVoucher = voucherMapper.selectOne(new LambdaQueryWrapper<Voucher>()
                    .eq(Voucher::getAccountBookId, voucherDto.getAccountBookId())
                    .eq(Voucher::getAccountBookEntityId, voucherDto.getAccountBookEntityId())
                    .ge(Voucher::getVoucherDate, accountBookPeriods.get(0).getStartDate())
                    .le(Voucher::getVoucherDate, accountBookPeriods.get(accountBookPeriods.size() - 1).getEndDate())
                    .orderByDesc(Voucher::getCreateTime).last(Constant.ConcatSql.LIMIT_1));
            if (lastVoucher == null) {
                voucherDate = currentAccountBookPeriod.getStartDate();
            } else {
                voucherDate = lastVoucher.getVoucherDate();
            }
        }
        VoucherVo datePeriodVoucher = new VoucherVo();
        datePeriodVoucher.setVoucherDate(voucherDate);
        for (AccountBookPeriod accountBookPeriod : accountBookPeriods) {
            if (DateUtils.isBetween(voucherDate, accountBookPeriod.getStartDate(), accountBookPeriod.getEndDate())) {
                datePeriodVoucher.setPostingPeriodYear(accountBookPeriod.getPeriodYear());
                datePeriodVoucher.setPostingPeriodNum(accountBookPeriod.getPeriodNum());
                datePeriodVoucher.setPeriodYearNum(accountBookPeriod.getPeriodYear() * 100 + accountBookPeriod.getPeriodNum());
            }
        }
        datePeriodVoucher.setAccountBookPeriods(accountBookPeriods);
        return datePeriodVoucher;
    }

    /**
     * @return java.util.List<com.njwd.entity.ledger.vo.VoucherEntryAuxiliaryVo>
     * @Author ZhuHC
     * @Date 2019/8/14 9:02
     * @Param [auxiliaries, allAuxiliaries]
     * @Description 辅助核算明细
     */
    private List<VoucherEntryAuxiliaryVo> getVoucherEntryAuxiliaryVos(List<VoucherEntryAuxiliary> auxiliaries, List<CommonAuxiliary> allAuxiliaries) {
        List<VoucherEntryAuxiliaryVo> voucherEntryAuxiliaryVos = new ArrayList<>();
        VoucherEntryAuxiliaryVo auxiliaryVo;
        for (VoucherEntryAuxiliary auxiliary : auxiliaries) {
            auxiliaryVo = new VoucherEntryAuxiliaryVo();
            FastUtils.copyProperties(auxiliary, auxiliaryVo);
            voucherEntryAuxiliaryVos.add(auxiliaryVo);
        }
        /*MergeUtil.merge(voucherEntryAuxiliaryVos, allAuxiliaries,
                (eav, aux) -> eav.getSourceTable().equals(aux.getSourceTable()) && eav.getItemValueId().equals(aux.getId()),
                (eav, aux) -> {
                    eav.setProjectCode(aux.getCode());
                    eav.setProjectName(aux.getName());
                    eav.setProjectFullName(aux.getFullName());
                    eav.setProjectAuxiliaryName(aux.getAuxiliaryName());
                });*/
           //将上面的代码 优化成此代码
           MergeUtil.merge(voucherEntryAuxiliaryVos,allAuxiliaries,
                   eav->eav.getSourceTable()+eav.getItemValueId(),aux->aux.getSourceTable()+aux.getId(),
                   (eav, aux) -> {
                       eav.setProjectCode(aux.getCode());
                       eav.setProjectName(aux.getName());
                       eav.setProjectFullName(aux.getFullName());
                       eav.setProjectAuxiliaryName(aux.getAuxiliaryName());
                   }
                   );
        return voucherEntryAuxiliaryVos;
    }

    /**
     * @return java.util.List<com.njwd.entity.ledger.vo.VoucherEntryCashFlowVo>
     * @Author ZhuHC
     * @Date 2019/8/14 9:01
     * @Param [cashFlows, cashFlowDetailList]
     * @Description 现金流量明细
     */
    private List<VoucherEntryCashFlowVo> getVoucherEntryCashFlowVos(List<VoucherEntryCashFlow> cashFlows, List<CommonAuxiliary> cashFlowDetailList) {
        List<VoucherEntryCashFlowVo> voucherEntryCashFlowVos = new ArrayList<>();
        VoucherEntryCashFlowVo cashFlowVo;
        for (VoucherEntryCashFlow cashFlow : cashFlows) {
            cashFlowVo = new VoucherEntryCashFlowVo();
            FastUtils.copyProperties(cashFlow, cashFlowVo);
            voucherEntryCashFlowVos.add(cashFlowVo);
        }
      /*  MergeUtil.merge(voucherEntryCashFlowVos, cashFlowDetailList,
                (cf, cfd) -> cf.getCashFlowItemId().equals(cfd.getId()),
                (cf, cfd) -> {
                    cf.setCashFlowCode(cfd.getCode());
                    cf.setCashFlowName(cfd.getName());
                    cf.setCashFlowFullName(cfd.getFullName());
                    cf.setCashFlowDirection(cfd.getCashFlowDirection());
                });*/
        //将上面的代码 优化成此代码
          MergeUtil.merge(voucherEntryCashFlowVos,cashFlowDetailList,
                  VoucherEntryCashFlowVo::getCashFlowItemId,CommonAuxiliary::getId,
                  (voucherEntryCashFlowVo,commonAuxiliary) ->{
                      voucherEntryCashFlowVo.setCashFlowCode(commonAuxiliary.getCode());
                      voucherEntryCashFlowVo.setCashFlowName(commonAuxiliary.getName());
                      voucherEntryCashFlowVo.setCashFlowFullName(commonAuxiliary.getFullName());
                      voucherEntryCashFlowVo.setCashFlowDirection(commonAuxiliary.getCashFlowDirection());
                  }
          );
        return voucherEntryCashFlowVos;
    }

    /**
     * @return java.util.List<com.njwd.entity.ledger.vo.VoucherEntryVo>
     * @Author ZhuHC
     * @Date 2019/8/14 9:01
     * @Param [voucherEntries, accountSubjectVoList]
     * @Description 凭证分录
     */
    private List<VoucherEntryVo> getVoucherEntryVos(List<VoucherEntryVo> voucherEntries, List<AccountSubjectVo> accountSubjectVoList) {
        List<VoucherEntryVo> voucherEntryVos = new ArrayList<>();
        VoucherEntryVo voucherEntryVo;
        for (VoucherEntryVo voucherEntry : voucherEntries) {
            voucherEntryVo = new VoucherEntryVo();
            FastUtils.copyProperties(voucherEntry, voucherEntryVo);
            voucherEntryVos.add(voucherEntryVo);
        }
        //数据拼接 凭证分录-会计科目
        if(!FastUtils.checkNullOrEmpty(voucherEntryVos)){
            mergeVoucherEntryAndSubject(accountSubjectVoList, voucherEntryVos);
        }
        return voucherEntryVos;
    }

    /**
     * @return java.util.List<com.njwd.entity.ledger.vo.VoucherEntryVo>
     * @Author ZhuHC
     * @Date 2019/8/14 9:01
     * @Param List<VoucherEntry> voucherEntries, List<AccountSubjectVo> accountSubjectVoList
     * @Description 凭证分录
     */
    private List<VoucherEntryVo> getVoucherEntryVosByVoucherEntry(List<VoucherEntry> voucherEntries, List<AccountSubjectVo> accountSubjectVoList) {
        List<VoucherEntryVo> voucherEntryVos = new ArrayList<>();
        VoucherEntryVo voucherEntryVo;
        for (VoucherEntry voucherEntry : voucherEntries) {
            voucherEntryVo = new VoucherEntryVo();
            FastUtils.copyProperties(voucherEntry, voucherEntryVo);
            voucherEntryVos.add(voucherEntryVo);
        }
        //数据拼接 凭证分录-会计科目
        if(!FastUtils.checkNullOrEmpty(voucherEntryVos)){
            mergeVoucherEntryAndSubject(accountSubjectVoList, voucherEntryVos);
        }
        return voucherEntryVos;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/30 17:22
     * @Param [accountSubjectVoList, voucherEntryVos]
     * @return void
     * @Description 分录和会计科目数据拼接
     */
    private void mergeVoucherEntryAndSubject(List<AccountSubjectVo> accountSubjectVoList, List<VoucherEntryVo> voucherEntryVos) {
       /* MergeUtil.merge(voucherEntryVos, accountSubjectVoList,
                (ve, asv) -> ve.getAccountSubjectId().equals(asv.getId()),
                (ve, asv) -> {
                    ve.setSubjectCode(asv.getCode());
                    ve.setSubjectName(asv.getName());
                    ve.setSubjectFullName(asv.getFullName());
                    ve.setAccountCategory(asv.getAccountCategory());
                });*/
        //将上面的代码 优化成此代码
          MergeUtil.merge(voucherEntryVos, accountSubjectVoList,
                  VoucherEntryVo::getAccountSubjectId,AccountSubjectVo::getId,
                  (ve, asv) -> {
                      ve.setSubjectCode(asv.getCode());
                      ve.setSubjectName(asv.getName());
                      ve.setSubjectFullName(asv.getFullName());
                      ve.setAccountCategory(asv.getAccountCategory());
                  }
          );
    }

    /**
     * @return java.util.List<com.njwd.entity.ledger.CommonAuxiliary>
     * @Author ZhuHC
     * @Date 2019/8/14 9:00
     * @Param [cashFlows]
     * @Description 现金流量
     */
    private List<CommonAuxiliary> getCashFlowList(List<VoucherEntryCashFlow> cashFlows) {
        List<Long> cashFlowList = new ArrayList<>();
        for (VoucherEntryCashFlow cashFlow : cashFlows) {
            cashFlowList.add(cashFlow.getCashFlowItemId());
        }
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        accountSubjectDto.setIds(new ArrayList<>(cashFlowList));
        accountSubjectDto.setSourceTable(Constant.TableName.CASH_FLOW_ITEM);
        Map<Long, Map<String, Object>> cashFlowItem = accountSubjectFeignClient.findSourceTableInfo(accountSubjectDto).getData();
        return getCommonAuxiliaries(cashFlowItem);
    }

    /**
     * @return java.util.List<com.njwd.entity.ledger.CommonAuxiliary>
     * @Author ZhuHC
     * @Date 2019/8/30 11:02
     * @Param [auxiliaryProject]
     * @Description 现金流量 明细
     */
    private List<CommonAuxiliary> getCommonAuxiliaries(Map<Long, Map<String, Object>> auxiliaryProject) {
        List<CommonAuxiliary> commonAuxiliaryList = new ArrayList<>();
        if(null != auxiliaryProject){
            CommonAuxiliary commonAuxiliary;
            for (Map.Entry<Long, Map<String, Object>> entry : auxiliaryProject.entrySet()) {
                commonAuxiliary = new CommonAuxiliary();
                Map<String, Object> item = entry.getValue();
                if (null != item.get("code")) {
                    commonAuxiliary.setCode(String.valueOf(item.get("code")));
                }
                if (null != item.get("name")) {
                    commonAuxiliary.setName(String.valueOf(item.get("name")));
                }
                if (null != item.get("fullName")) {
                    commonAuxiliary.setFullName(String.valueOf(item.get("fullName")));
                }
                if (null != item.get("cashFlowDirection")) {
                    commonAuxiliary.setCashFlowDirection(Byte.valueOf(item.get("cashFlowDirection").toString()));
                }
                commonAuxiliary.setId(entry.getKey());
                commonAuxiliaryList.add(commonAuxiliary);
            }
        }
        return commonAuxiliaryList;
    }

    /**
     * @return java.util.List<com.njwd.entity.ledger.CommonAuxiliary>
     * @Author ZhuHC
     * @Date 2019/8/14 8:59
     * @Param [auxiliaries]
     * @Description 辅助核算项目
     */
    private List<CommonAuxiliary> getAuxiliaryList(List<VoucherEntryAuxiliary> auxiliaries) {
        Map<String, List<Long>> sourceMap = new LinkedHashMap<>();
        //分类 辅助核算项目来源表及其项目值
        List<Long> list;
        for (VoucherEntryAuxiliary auxiliary : auxiliaries) {
            if (sourceMap.get(auxiliary.getSourceTable()) == null) {
                list = new ArrayList<>();
                list.add(auxiliary.getItemValueId());
                sourceMap.put(auxiliary.getSourceTable(), list);
            } else {
                list = sourceMap.get(auxiliary.getSourceTable());
                list.add(auxiliary.getItemValueId());
            }
        }
        //遍历 查询辅助核算项目
        AccountSubjectDto param = new AccountSubjectDto();
        List<CommonAuxiliary> allAuxiliaries = new ArrayList<>();
        List<String> sourceTableList = new ArrayList<>();
        List<List<List<Long>>> idLists = new ArrayList<>();
        List<List<Long>> idList = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        for (Map.Entry<String,List<Long>> entry : sourceMap.entrySet()) {
            sourceTableList.add(entry.getKey());
            ids.addAll(entry.getValue());
            idList.add(ids);
            idLists.add(idList);
        }
        param.setSourceTableList(sourceTableList);
        param.setIdLists(idLists);
        List<List<Map<String, Object>>> allSourceTableInfo = accountSubjectFeignClient.findAllSourceTableInfo(param).getData();
        // 辅助核算项目值 转换
        List<CommonAuxiliary> commonAuxiliaryList;
        if(null != allSourceTableInfo && !allSourceTableInfo.isEmpty()){
            for(int i = 0; i < allSourceTableInfo.size(); i++){
                if(!FastUtils.checkNullOrEmpty(allSourceTableInfo.get(i)) && null != sourceTableList.get(i)){
                    commonAuxiliaryList = getCommonAuxiliaries(allSourceTableInfo.get(i),sourceTableList.get(i));
                    allAuxiliaries.addAll(commonAuxiliaryList);
                }
            }
        }
        //根据来源表 及 租户ID 查询 辅助核算名称
        return allAuxiliaries;
    }

    /**
     * @return java.util.List<com.njwd.entity.ledger.CommonAuxiliary>
     * @Author ZhuHC
     * @Date 2019/8/14 8:59
     * @Param [auxiliaryProject, sourceTable]
     * @Description 来源表 数据
     */
    private List<CommonAuxiliary> getCommonAuxiliaries(List<Map<String, Object>> auxiliaryProject, String sourceTable) {
        List<CommonAuxiliary> commonAuxiliaryList = new ArrayList<>();
        CommonAuxiliary commonAuxiliary;
        for (Map<String, Object> item : auxiliaryProject) {
            commonAuxiliary = new CommonAuxiliary();
            if (null != sourceTable) {
                if (null != item.get("code")) {
                    if("wd_accounting_item_value".equals(sourceTable)){
                        commonAuxiliary.setCode(String.valueOf(item.get("auxiliaryCode")));
                    }else {
                        commonAuxiliary.setCode(String.valueOf(item.get("code")));
                    }
                }
                if (null != item.get("name")) {
                    commonAuxiliary.setName(String.valueOf(item.get("name")));
                }
                if (null != item.get("fullName")) {
                    commonAuxiliary.setFullName(String.valueOf(item.get("fullName")));
                }
                if (null != item.get("auxiliaryName")) {
                    commonAuxiliary.setAuxiliaryName(String.valueOf(item.get("auxiliaryName")));
                }
                commonAuxiliary.setSourceTable(sourceTable);
            }
            commonAuxiliary.setId(Long.valueOf(item.get("id").toString()));
            commonAuxiliaryList.add(commonAuxiliary);
        }
        return commonAuxiliaryList;
    }

    /**
     * @return java.util.List<com.njwd.entity.platform.vo.AccountSubjectVo>
     * @Author ZhuHC
     * @Date 2019/8/14 8:58
     * @Param [voucherEntries]
     * @Description 根据code查询会计科目
     */
    private List<AccountSubjectVo> getSubjectList(List<VoucherEntryVo> voucherEntries) {
        List<Long> subjectIdList = new LinkedList<>();
        for (VoucherEntryVo voucherEntry : voucherEntries) {
            subjectIdList.add(voucherEntry.getAccountSubjectId());
        }
        return getAccountSubjectVos(subjectIdList);
    }

    /**
     * @return java.util.List<com.njwd.entity.platform.vo.AccountSubjectVo>
     * @Author ZhuHC
     * @Date 2019/8/14 8:58
     * @Param List<VoucherEntry>
     * @Description 根据code查询会计科目
     */
    private List<AccountSubjectVo> getSubjectListByVoucherEntry(List<VoucherEntry> voucherEntries) {
        List<Long> subjectIdList = new ArrayList<>();
        for (VoucherEntry voucherEntry : voucherEntries) {
            subjectIdList.add(voucherEntry.getAccountSubjectId());
        }
        return getAccountSubjectVos(subjectIdList);
    }

    /**
     * @return java.util.List<com.njwd.entity.platform.vo.AccountSubjectVo>
     * @Author ZhuHC
     * @Date 2019/8/19 13:53
     * @Param [subjectIdList]
     * @Description 会计科目查询
     */
    private List<AccountSubjectVo> getAccountSubjectVos(List<Long> subjectIdList) {
        List<AccountSubjectVo> accountSubjectVoList = new ArrayList<>();
        if(!FastUtils.checkNullOrEmpty(subjectIdList)){
            AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
            accountSubjectDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
            accountSubjectDto.setIds(subjectIdList);
            //等于
            accountSubjectDto.setSubjectCodeOperator(LedgerConstant.QueryScheme.OPERATOR_EQUAL);
            accountSubjectVoList = accountSubjectFeignClient.findInfoForLedger(accountSubjectDto).getData();
        }
        return accountSubjectVoList;
    }

    /**
     * 补充凭证编辑信息 生成凭证号
     *
     * @param voucherDto          voucherDto
     * @param operator            operator
     * @param existVoucher        existVoucher
     * @param interiorVoucherList interiorVoucherList
     * @param unchangedEntryIds
     * @return 准备修改的凭证数据
     * @author xyyxhcj@qq.com
     * @date 2019/8/9 13:33
     */
    private List<Voucher> fillEditVoucherInfo(VoucherDto voucherDto, SysUserVo operator, Voucher existVoucher, @Nullable List<VoucherDto> interiorVoucherList, @Nullable List<Long> unchangedEntryIds) {
        boolean isCollectInteriorVoucher = interiorVoucherList != null && unchangedEntryIds != null;
        Date now = new Date();
        existVoucher.setUpdatorId(operator.getUserId());
        existVoucher.setUpdatorName(operator.getName());
        existVoucher.setUpdateTime(now);
        existVoucher.setBillNum(voucherDto.getBillNum());
        existVoucher.setFirstAbstract(voucherDto.getFirstAbstract());
        existVoucher.setDebitAmount(voucherDto.getDebitAmount());
        existVoucher.setCreditAmount(voucherDto.getCreditAmount());
        Date beforeDate = existVoucher.getVoucherDate();
        Date newDate = voucherDto.getVoucherDate();
        Byte newWord = voucherDto.getCredentialWord();
        Byte oldWord = existVoucher.getCredentialWord();
        Integer newPeriod = voucherDto.getPeriodYearNum();
        Integer oldPeriod = existVoucher.getPeriodYearNum();
        boolean changeDate = false;
        boolean changedWord = false;
        boolean changedPeriod = false;
        boolean changedStatus = false;
        if (!StringUtils.isEmpty(newWord) && !oldWord.equals(newWord)) {
            changedWord = true;
            existVoucher.setCredentialWord(voucherDto.getCredentialWord());
        }
        boolean unchangeEntryEmpty = unchangedEntryIds.isEmpty();
        if (newPeriod != null && !newPeriod.equals(oldPeriod)) {
            changedPeriod = true;
            existVoucher.setPostingPeriodYear(voucherDto.getPostingPeriodYear());
            existVoucher.setPostingPeriodNum(voucherDto.getPostingPeriodNum());
            existVoucher.setPeriodYearNum(newPeriod);
            // 变更期间 对原生成未变更的协同凭证发生额 减旧期间 加到 新期间 并准备生成新的凭证号
            if (isCollectInteriorVoucher && !unchangeEntryEmpty) {
                List<VoucherEntryInterior> interiorRecordList = voucherEntryInteriorService.list(new LambdaQueryWrapper<VoucherEntryInterior>()
                        .in(VoucherEntryInterior::getEntryId, unchangedEntryIds));
                if (!interiorRecordList.isEmpty()) {
                    Set<Long> needRefreshBalanceVoutherIds = new HashSet<>();
                    interiorRecordList.forEach(record -> needRefreshBalanceVoutherIds.add(record.getInteriorVoucherId()));
                    // 减协同凭证原余额
                    List<VoucherDto> needRefreshVouchers = getExistVouchersByIds(needRefreshBalanceVoutherIds);
                    updateBalanceForRemoveVouchers(needRefreshBalanceVoutherIds, needRefreshVouchers);
                    for (VoucherDto voucher : needRefreshVouchers) {
                        interiorVoucherList.add(voucher);
                        voucher.setUpdatorId(operator.getUserId());
                        voucher.setUpdatorName(operator.getName());
                        voucher.setUpdateTime(now);
                        voucher.setVoucherDate(voucherDto.getVoucherDate());
                        voucher.setPostingPeriodYear(voucherDto.getPostingPeriodYear());
                        voucher.setPostingPeriodNum(voucherDto.getPostingPeriodNum());
                        // 更新发生额
                        updateBalance(voucher, Constant.BalanceUpdateType.ADD);
                    }
                }
            }
        }
        if (newDate != null && DateUtils.compareDate(newDate, beforeDate) != 0) {
            changeDate = true;
            existVoucher.setVoucherDate(newDate);
            if (isCollectInteriorVoucher && !unchangeEntryEmpty) {
                // 只要变更了时间 就需要刷新原生成的协同凭证数据
                voucherMapper.refreshInteriorVouchers(voucherDto, operator, unchangedEntryIds, now);
            }
        }
        if (LedgerConstant.VoucherStatus.DRAFT == existVoucher.getStatus()) {
            changedStatus = true;
            existVoucher.setStatus(LedgerConstant.VoucherStatus.PENDING);
        }
        if (changedWord || changedPeriod || changedStatus) {
            // 如果变更期间/凭证字/原来状态为草稿 生成凭证号
            generateCode(existVoucher);
        }
        existVoucher.setCashFlowAmount(voucherDto.getCashFlowAmount());
        existVoucher.setCashCheckType(voucherDto.getCashCheckType());
        existVoucher.setInteriorType(voucherDto.getInteriorType());
        List<Voucher> updateVouchers = new LinkedList<>();
        updateVouchers.add(existVoucher);
        return updateVouchers;
    }

    /**
     * 构造待存储现金流量明细,提供subjectDict时进行校验
     *
     * @param voucherDto      voucherDto
     * @param voucherId       voucherId
     * @param rowNumEntryDict rowNumEntryDict 序号-分录 字典
     * @param subjectDict     科目信息字典,为null时不校验现金流量
     * @param unchangedIds    修改时收集无变更ids
     * @return java.util.List<com.njwd.entity.ledger.VoucherEntryCashFlow> 待存储现金流量明细数据
     * @author xyyxhcj@qq.com
     * @date 2019/8/9 12:47
     **/
    private List<VoucherEntryCashFlow> getVoucherEntryCashFlows(VoucherDto voucherDto, Long voucherId, Map<Integer, VoucherEntry> rowNumEntryDict, @Nullable Map<Long, Map<String, Object>> subjectDict, List<Long> unchangedIds) {
        // 构造分录ID->分录字典
        Map<Long, VoucherEntry> idEntryDict = rowNumEntryDict.values().stream().filter(entry -> entry.getId() != null).collect(Collectors.toMap(VoucherEntry::getId, entry -> entry));
        boolean checkCashFlow = subjectDict != null;
        Set<Long> cashFlowIds = new HashSet<>();
        List<VoucherEntryCashFlow> saveCashFlowList = new LinkedList<>();
        VoucherEntryCashFlow tempCashFlow;
        boolean isCollectUnchangedIds = unchangedIds != null;
        // 判断现金流量id不可重复
        Map<Long, Boolean> cashFlowIdDict = new LinkedHashMap<>();
        for (VoucherEntryDto entryDto : voucherDto.getEditEntryList()) {
            for (VoucherEntryCashFlowDto cashFlowDto : entryDto.getEditCashFlowList()) {
                String cashFlowRowNum = entryDto.getRowNum() + Constant.Character.UNDER_LINE + cashFlowDto.getRowNum();
                ServiceException oppositeEntryNotExist = getCashFlowOppositeNotExistException(cashFlowRowNum);
                if (checkCashFlow) {
                    cashFlowIds.add(cashFlowDto.getCashFlowItemId());
                }
                Long cashFlowId = cashFlowDto.getId();
                boolean noCashFlowId = cashFlowId == null;
                if (noCashFlowId || Constant.Is.YES.equals(cashFlowDto.getIsModify())) {
                    // 新增或修改时校验是否变更,
                    tempCashFlow = new VoucherEntryCashFlow();
                    FastUtils.copyProperties(cashFlowDto, tempCashFlow);
                    tempCashFlow.setVoucherId(voucherId);
                    tempCashFlow.setEntryId(entryDto.getId());
                    VoucherEntry oppositeEntry = getOppositeEntry(rowNumEntryDict, cashFlowDto);
                    if (oppositeEntry == null) {
                        throw oppositeEntryNotExist;
                    }
                    tempCashFlow.setOppositeEntryId(oppositeEntry.getId());
                    saveCashFlowList.add(tempCashFlow);
                } else if (isCollectUnchangedIds) {
                    unchangedIds.add(cashFlowId);
                }
                // 判断分录id是否重复
                if (!noCashFlowId) {
                    if (cashFlowIdDict.containsKey(cashFlowId)) {
                        // 分录id不可重复
                        LOGGER.warn("传参的现金流量分析行id重复:{}", cashFlowId);
                        throw new ServiceException(ResultCode.PARAMS_NOT_RIGHT);
                    } else {
                        cashFlowIdDict.put(cashFlowId, true);
                    }
                }
            }
        }
        if (checkCashFlow && !cashFlowIds.isEmpty()) {
            BigDecimal cashInflow = countCashInFlow(voucherDto, rowNumEntryDict, subjectDict, idEntryDict, cashFlowIds);
            setCashFlowCheckTypeIsExamined(voucherDto, cashInflow.compareTo(voucherDto.getCashFlowAmount()) == 0);
        }
        return saveCashFlowList;
    }

    /**
     * 计算现金流量净流入额
     *
     * @param voucherDto      voucherDto
     * @param rowNumEntryDict rowNumEntryDict
     * @param subjectDict     subjectDict
     * @param idEntryDict     idEntryDict
     * @param cashFlowIds     cashFlowIds
     * @return java.math.BigDecimal
     * @author xyyxhcj@qq.com
     * @date 2019/10/8 17:00
     **/
    private BigDecimal countCashInFlow(VoucherDto voucherDto, Map<Integer, VoucherEntry> rowNumEntryDict, @Nullable Map<Long, Map<String, Object>> subjectDict, Map<Long, VoucherEntry> idEntryDict, Set<Long> cashFlowIds) {
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        Map<Long, Map<String, Object>> cashFlowItemDict = getCashFlowItemDict(cashFlowIds, accountSubjectDto);
        // 计算净流入额
        BigDecimal cashInflow = BigDecimal.ZERO;
        for (VoucherEntryDto entryDto : voucherDto.getEditEntryList()) {
            for (VoucherEntryCashFlowDto cashFlowDto : entryDto.getEditCashFlowList()) {
                String cashFlowRowNum = entryDto.getRowNum() + Constant.Character.UNDER_LINE + cashFlowDto.getRowNum();
                ServiceException oppositeEntryNotExist = getCashFlowOppositeNotExistException(cashFlowRowNum);
                if (cashFlowDto.getOppositeRowNum() == null) {
                    // 如果前端未放入oppositeRowNum,补充参数
                    if (cashFlowDto.getOppositeEntryId() == null) {
                        // 未提供原始关联分录ID
                        throw oppositeEntryNotExist;
                    }
                    VoucherEntry oppositeVoucherEntry = idEntryDict.get(cashFlowDto.getOppositeEntryId());
                    if (oppositeVoucherEntry == null) {
                        // 查不到对方分录
                        throw oppositeEntryNotExist;
                    }
                    cashFlowDto.setOppositeRowNum(oppositeVoucherEntry.getRowNum());
                }
                Map<String, Object> cashFlowItemMap = checkCashFlowValid(rowNumEntryDict, subjectDict, cashFlowItemDict, entryDto, cashFlowDto, cashFlowRowNum, oppositeEntryNotExist);
                if (FastUtils.isStatus(cashFlowItemMap, Constant.PropertyName.CASH_FLOW_DIRECTION)) {
                    cashInflow = cashInflow.add(cashFlowDto.getCurrencyAmount());
                } else {
                    cashInflow = cashInflow.subtract(cashFlowDto.getCurrencyAmount());
                }
            }
        }
        return cashInflow;
    }

    /**
     * 校验现金流量分析传参是否正确
     *
     * @param rowNumEntryDict       rowNumEntryDict
     * @param subjectDict           subjectDict
     * @param cashFlowItemDict      cashFlowItemDict
     * @param entryDto              entryDto
     * @param cashFlowDto           cashFlowDto
     * @param cashFlowRowNum        cashFlowRowNum
     * @param oppositeEntryNotExist oppositeEntryNotExist
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author xyyxhcj@qq.com
     * @date 2019/10/8 15:47
     **/
    private Map<String, Object> checkCashFlowValid(Map<Integer, VoucherEntry> rowNumEntryDict, @NotNull Map<Long, Map<String, Object>> subjectDict, Map<Long, Map<String, Object>> cashFlowItemDict, VoucherEntryDto entryDto, VoucherEntryCashFlowDto cashFlowDto, String cashFlowRowNum, ServiceException oppositeEntryNotExist) {
        VoucherEntry oppositeVoucherEntry = rowNumEntryDict.get(cashFlowDto.getOppositeRowNum());
        if (oppositeVoucherEntry == null) {
            throw oppositeEntryNotExist;
        }
        Map<String, Object> subjectMap = subjectDict.get(entryDto.getAccountSubjectId());
        Map<String, Object> oppositeSubjectMap = subjectDict.get(oppositeVoucherEntry.getAccountSubjectId());
        if (subjectMap == null || oppositeSubjectMap == null) {
            // 表示前端未保存凭证直接调用现金流量分析
            throw new ServiceException(ResultCode.VOUCHER_DATA_NOT_ACCORD_CASH_FLOW, cashFlowRowNum);
        }
        if (!subjectMap.get(Constant.PropertyName.ACCOUNT_CATEGORY).toString().startsWith(Constant.AccountCategory.A) || oppositeSubjectMap.get(Constant.PropertyName.ACCOUNT_CATEGORY).toString().startsWith(Constant.AccountCategory.A)) {
            // 对方分录不可为现金科目
            throw new ServiceException(ResultCode.CASH_FLOW_PARAM_ERROR, cashFlowRowNum);
        }
        Map<String, Object> cashFlowItemMap = cashFlowItemDict.get(cashFlowDto.getCashFlowItemId());
        if (cashFlowItemMap == null || FastUtils.isStatus(cashFlowItemMap, Constant.PropertyName.IS_DEL)) {
            throw new ServiceException(ResultCode.CASH_FLOW_ITEM_NOT_EXIST, cashFlowRowNum);
        } else if (!FastUtils.isStatus(cashFlowItemMap, Constant.PropertyName.IS_FINAL)) {
            throw new ServiceException(ResultCode.CASH_FLOW_ITEM_NOT_FINAL, cashFlowRowNum);
        }
        return cashFlowItemMap;
    }

    /**
     * 创建现金流量分析参数错误的异常
     *
     * @param cashFlowRowNum cashFlowRowNum
     * @return com.njwd.exception.ServiceException
     * @author xyyxhcj@qq.com
     * @date 2019/10/8 15:44
     **/
    private ServiceException getCashFlowOppositeNotExistException(String cashFlowRowNum) {
        return new ServiceException(ResultCode.CASH_FLOW_OPPOSITE_NOT_EXIST, cashFlowRowNum);
    }

    /**
     * 获取现金流量分析的对方分录
     *
     * @param rowNumEntryDict rowNumEntryDict
     * @param cashFlowDto     cashFlowDto
     * @return com.njwd.entity.ledger.VoucherEntry
     * @author xyyxhcj@qq.com
     * @date 2019/10/8 15:37
     **/
    private VoucherEntry getOppositeEntry(Map<Integer, VoucherEntry> rowNumEntryDict, VoucherEntryCashFlowDto cashFlowDto) {
        Integer oppositeRowNum = cashFlowDto.getOppositeRowNum();
        if (oppositeRowNum == null) {
            return null;
        }
        return rowNumEntryDict.get(oppositeRowNum);
    }

    /**
     * 如果现金流量检查通过,设置凭证现金流量检查类型为已检查
     *
     * @param voucherDto voucherDto
     * @param isExamined isExamined
     * @author xyyxhcj@qq.com
     * @date 2019/8/23 15:32
     **/
    private void setCashFlowCheckTypeIsExamined(VoucherDto voucherDto, boolean isExamined) {
        if (isExamined) {
            // 现金流量的现金科目的净发生额与对方科目中的现金流入减去现金流出的差额相等
            voucherDto.setCashCheckType(Constant.CashFlowCheckType.EXAMINED);
        } else {
            voucherDto.setCashCheckType(Constant.CashFlowCheckType.UNEXAMINED);
        }
    }

    /**
     * 获取现金流量项字典
     *
     * @param cashFlowIds       cashFlowIds
     * @param accountSubjectDto accountSubjectDto
     * @return 现金流量项字典 key为id
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:20
     **/
    private Map<Long, Map<String, Object>> getCashFlowItemDict(Set<Long> cashFlowIds, AccountSubjectDto accountSubjectDto) {
        accountSubjectDto.setIds(new ArrayList<>(cashFlowIds));
        accountSubjectDto.setSourceTable(Constant.TableName.CASH_FLOW_ITEM);
        Result<Map<Long, Map<String, Object>>> cashFlowItemResult = accountSubjectFeignClient.findSourceTableInfo(accountSubjectDto);
        Map<Long, Map<String, Object>> cashFlowItemDict = cashFlowItemResult.getData();
        if (cashFlowItemDict == null) {
            throw new ServiceException(ResultCode.CASH_FLOW_ITEM_NOT_EXIST);
        }
        return cashFlowItemDict;
    }

    /**
     * 构造待存储核算明细
     *
     * @param voucherDto        voucherDto
     * @param voucherId         voucherId
     * @param unchangedEntryIds 未变更的分录ID
     * @return java.util.List<com.njwd.entity.ledger.VoucherEntryAuxiliary> 待存储核算明细数据
     * @author xyyxhcj@qq.com
     * @date 2019/8/9 11:52
     **/
    @Override
    public  @NotNull List<VoucherEntryAuxiliary> getVoucherEntryAuxiliaries(VoucherDto voucherDto, Long voucherId, List<Long> unchangedEntryIds) {
        List<VoucherEntryAuxiliary> saveAuxiliaryList = new LinkedList<>();
        VoucherEntryAuxiliary tempAuxiliary;
        boolean isFilterEntry = unchangedEntryIds != null;
        for (VoucherEntryDto entryDto : voucherDto.getEditEntryList()) {
            if (isFilterEntry && unchangedEntryIds.contains(entryDto.getId())) {
                continue;
            }
            for (VoucherEntryAuxiliaryDto auxiliaryDto : entryDto.getEditAuxiliaryList()) {
                auxiliaryDto.setId(null);
                tempAuxiliary = new VoucherEntryAuxiliary();
                FastUtils.copyProperties(auxiliaryDto, tempAuxiliary);
                tempAuxiliary.setVoucherId(voucherId);
                tempAuxiliary.setEntryId(entryDto.getId());
                saveAuxiliaryList.add(tempAuxiliary);
            }
        }
        return saveAuxiliaryList;
    }

    /**
     * 构造待存储分录
     *
     * @param voucherDto      voucherDto
     * @param rowNumEntryDict rowNumEntryDict 序号-分录 字典
     * @author xyyxhcj@qq.com
     * @date 2019/8/9 10:42
     **/
    private void fillVoucherEntries(VoucherDto voucherDto, Map<Integer, VoucherEntry> rowNumEntryDict) {
        for (VoucherEntryDto entryDto : voucherDto.getEditEntryList()) {
            // 构造字典
            rowNumEntryDict.put(entryDto.getRowNum(), entryDto);
        }
    }

    /**
     * 补充计算分录中的本位币换算
     *
     * @param entryDto entryDto
     * @author xyyxhcj@qq.com
     * @date 2019/8/15 14:46
     **/
    private void countStandard(VoucherEntryDto entryDto) {
        entryDto.setDebitAmount(entryDto.getOriginalDebitAmount().multiply(entryDto.getExchangeRate()));
        entryDto.setCreditAmount(entryDto.getOriginalCreditAmount().multiply(entryDto.getExchangeRate()));
    }

    /**
     * 校验是否可修改他人凭证
     *
     * @param operator     operator
     * @param parameterSet parameterSet
     * @param existVoucher existVoucher
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:21
     **/
    private void checkEditOtherSet(SysUserVo operator, ParameterSetVo parameterSet, Voucher existVoucher) {
        ParameterSetSub isCanUpdateOther = FastUtils.getParamSetSub(parameterSet, existVoucher.getAccountBookId(), Constant.ParameterSetKey.IS_CAN_UPDATE_OTHER);
        if (Constant.Is.NO.equals(isCanUpdateOther.getValue().byteValue())) {
            // 不可修改他人凭证
            if (!operator.getUserId().equals(existVoucher.getCreatorId())) {
                throw new ServiceException(ResultCode.NOT_ALLOW_EDIT_OTHER_VOUCHER);
            }
        }
    }

    /**
     * 修改凭证
     *
     * @param voucherDto          voucherDto
     * @param operator            operator
     * @param parameterSet        parameterSet
     * @param accountSubjectDto   accountSubjectDto
     * @param needCashFlows       needCashFlows
     * @param interiorVoucherList 自动生成的内部往来对方凭证集合
     * @param voucherId           voucherId
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:22
     **/
    private void updateVoucher(VoucherDto voucherDto, SysUserVo operator, ParameterSetVo parameterSet, AccountSubjectDto accountSubjectDto, List<VoucherEntryDto> needCashFlows, List<VoucherDto> interiorVoucherList, Long voucherId) {
        Voucher existVoucher = voucherMapper.selectById(voucherId);
        FastUtils.checkNull(existVoucher);
        checkStatus(existVoucher, ResultCode.NOT_ALLOW_SAVE, LedgerConstant.VoucherStatus.DRAFT, LedgerConstant.VoucherStatus.PENDING);
        checkChronological(voucherDto, parameterSet, existVoucher);
        checkEditOtherSet(operator, parameterSet, existVoucher);
        checkVersion(existVoucher, voucherDto);
        // 先查出原凭证明细 分录+核算明细+现金流量
        VoucherDto beforeVoucher = getBeforeVoucher(voucherId, existVoucher);
        // 存放未变更的分录（不包括序号）
        final List<Long> unchangedEntryIds = new LinkedList<>();
        updateVoucherInfo(voucherDto, parameterSet, accountSubjectDto, needCashFlows, interiorVoucherList, voucherId, unchangedEntryIds);
        // 新增协同凭证 更新协同发生额
        generateInteriorVoucherList(voucherDto, interiorVoucherList, voucherId);
        // 更新当前凭证发生额 减去当前凭证原明细数据 重新加上新的
        updateBalance(beforeVoucher, Constant.BalanceUpdateType.SUBTRACT);
        updateBalance(voucherDto, Constant.BalanceUpdateType.ADD);
        // 变更可修改字段 如果变更期间 原凭证需要减原期间发生额 + 新期间发生额
        List<Voucher> updateVouchers = fillEditVoucherInfo(voucherDto, operator, existVoucher, interiorVoucherList, unchangedEntryIds);
        updateVoucherCodeBatch(updateVouchers, interiorVoucherList.isEmpty(), interiorVoucherList);
    }

    /**
     * 更新凭证关联的数据
     *  如果删除生成了协同的分录 需要一并删除生成的凭证,并更新对应凭证的余额
     *
     * @param voucherDto          voucherDto
     * @param parameterSet        parameterSet
     * @param accountSubjectDto   accountSubjectDto
     * @param needCashFlows       needCashFlows
     * @param interiorVoucherList 自动生成的内部往来对方凭证集合
     * @param voucherId           voucherId
     * @param unchangedEntryIds   unchangedEntryIds
     * @author xyyxhcj@qq.com
     * @date 2019/9/21 20:22
     **/
    private void updateVoucherInfo(VoucherDto voucherDto, ParameterSetVo parameterSet, AccountSubjectDto accountSubjectDto, List<VoucherEntryDto> needCashFlows, final List<VoucherDto> interiorVoucherList, Long voucherId, List<Long> unchangedEntryIds) {
        if (!voucherDto.getEditEntryList().isEmpty()) {
            LambdaQueryWrapper<VoucherEntry> entryWrapper = new LambdaQueryWrapper<VoucherEntry>().eq(VoucherEntry::getVoucherId, voucherId);
            LambdaQueryWrapper<VoucherEntryAuxiliary> auxiliaryWrapper = new LambdaQueryWrapper<VoucherEntryAuxiliary>().eq(VoucherEntryAuxiliary::getVoucherId, voucherId);
            LambdaQueryWrapper<VoucherEntryCashFlow> cashFlowWrapper = new LambdaQueryWrapper<VoucherEntryCashFlow>().eq(VoucherEntryCashFlow::getVoucherId, voucherId);
            if (voucherDto.getEditEntryList().get(0).getId() != null && 0 == voucherDto.getEditEntryList().get(0).getId()) {
                // 首条记录id为0时删除所有分录+核算明细+现金流量分析明细
                voucherEntryService.remove(entryWrapper);
                voucherEntryAuxiliaryService.remove(auxiliaryWrapper);
                voucherEntryCashFlowService.remove(cashFlowWrapper);
            } else {
                // 获取分录使用的科目信息,辅助核算数据字典
                Map<String, Map<Long, Map<String, Object>>> auxiliaryDict = new LinkedHashMap<>();
                // 并校验分录传参+校验辅助核算+内部往来明细
                Map<Long, Map<String, Object>> subjectDict = getSubjectDict(voucherDto, voucherDto, accountSubjectDto, auxiliaryDict);
                // 根据序号查找分录对象
                Map<Integer, VoucherEntry> rowNumEntryDict = new HashMap<>();
                // 标记内部往来在借方/贷方 [0]-借 [1]-贷
                boolean[] interiorUse = new boolean[2];
                // 默认为不需要生成
                voucherDto.setInteriorType(Constant.InteriorType.NEEDLESS);
                // 判断分录id不可重复
                Map<Long, Boolean> entryIdDict = new LinkedHashMap<>();
                for (VoucherEntryDto entryDto : voucherDto.getEditEntryList()) {
                    Integer rowNum = entryDto.getRowNum();
                    // 构造字典
                    rowNumEntryDict.put(rowNum, entryDto);
                    countStandard(entryDto);
                    Long entryId = entryDto.getId();
                    boolean noEntryId = entryId == null;
                    if (noEntryId || Constant.Is.YES.equals(entryDto.getIsModify())) {
                        // 新增或修改 校验科目数据是否合法
                        checkEntryValid(voucherDto, subjectDict, auxiliaryDict, needCashFlows, interiorVoucherList, voucherDto, interiorUse, entryDto, rowNum);
                    } else {
                        // 未修改的数据 仍需要校验现金流量
                        Map<String, Object> subjectMap = subjectDict.get(entryDto.getAccountSubjectId());
                        if (subjectMap.get(Constant.PropertyName.ACCOUNT_CATEGORY).toString().startsWith(Constant.AccountCategory.A)) {
                            needCashFlows.add(entryDto);
                        }
                        // 如果未修改的数据有内部往来科目,则不修改内部往来类型
                        if (FastUtils.isStatus(subjectMap, Constant.PropertyName.IS_INTERIOR)) {
                            voucherDto.setInteriorType(null);
                        }
                    }
                    // 判断分录id是否重复
                    if (!noEntryId) {
                        if (entryIdDict.containsKey(entryId)) {
                            // 分录id不可重复
                            LOGGER.warn("传参的分录id重复:{}", entryId);
                            throw new ServiceException(ResultCode.PARAMS_NOT_RIGHT);
                        } else {
                            entryIdDict.put(entryId, true);
                        }
                    }
                }
                // 标记存在现金流量净发生额
                boolean hasCashAmount = setCashCheckType(voucherDto, needCashFlows);
                voucherEntryService.insertOrUpdateBatch(voucherDto.getEditEntryList(), voucherId, unchangedEntryIds, entryWrapper);
                // 如果删除生成了协同的分录 需要一并删除生成的凭证
                LambdaQueryWrapper<VoucherEntryInterior> queryWrapper = new LambdaQueryWrapper<VoucherEntryInterior>()
                        .eq(VoucherEntryInterior::getVoucherId, voucherId);
                if (!unchangedEntryIds.isEmpty()) {
                    queryWrapper.notIn(VoucherEntryInterior::getEntryId, unchangedEntryIds);
                }
                List<VoucherEntryInterior> interiorRecordList = voucherEntryInteriorService.list(queryWrapper);
                if (!interiorRecordList.isEmpty()) {
                    Set<Long> needRemoveVoucherIds = new HashSet<>();
                    interiorRecordList.forEach(record -> needRemoveVoucherIds.add(record.getInteriorVoucherId()));
                    // 删除协同凭证
                    List<VoucherDto> needRemoveVouchers = getExistVouchersByIds(needRemoveVoucherIds);
                    updateBalanceForRemoveVouchers(needRemoveVoucherIds, needRemoveVouchers);
                    if (!needRemoveVouchers.isEmpty()) {
                        // 逻辑删除并维护关联表
                        voucherMapper.deleteBatch(needRemoveVouchers);
                        voucherEntryInteriorService.remove(new LambdaQueryWrapper<VoucherEntryInterior>().in(VoucherEntryInterior::getInteriorVoucherId, needRemoveVoucherIds));
                    }
                }
                updateEntryAuxiliaryAndCashFlow(parameterSet, voucherDto, voucherId, unchangedEntryIds, auxiliaryWrapper, cashFlowWrapper, rowNumEntryDict, hasCashAmount ? subjectDict : null);
            }
        }
    }

    /**
     * 获取原凭证数据
     *
     * @param voucherId    voucherId
     * @param existVoucher existVoucher
     * @return com.njwd.entity.ledger.dto.VoucherDto
     * @author xyyxhcj@qq.com
     * @date 2019/9/21 20:20
     **/
    private VoucherDto getBeforeVoucher(Long voucherId, Voucher existVoucher) {
        List<Long> singletonId = Collections.singletonList(voucherId);
        LinkedList<VoucherEntryDto> beforeEntryList = voucherEntryService.findList(singletonId);
        LinkedList<VoucherEntryAuxiliaryDto> beforeEntryAuxiliaryList = voucherEntryAuxiliaryService.findList(singletonId);
        List<VoucherEntryCashFlowDto> beforeEntryCashFlowList = voucherEntryCashFlowService.findList(singletonId);
        VoucherDto beforeVoucher = new VoucherDto();
        FastUtils.copyProperties(existVoucher, beforeVoucher);
        mergeVoucherListDetail(Collections.singletonList(beforeVoucher), beforeEntryList, beforeEntryAuxiliaryList, beforeEntryCashFlowList);
        return beforeVoucher;
    }

    /**
     * 修改凭证的分录核算明细 删除再插入
     *
     * @param parameterSet      parameterSet
     * @param voucherDto        voucherDto
     * @param voucherId         voucherId
     * @param unchangedEntryIds unchangedEntryIds
     * @param auxiliaryWrapper  auxiliaryWrapper
     * @param cashFlowWrapper   cashFlowWrapper
     * @param rowNumEntryDict   rowNumEntryDict
     * @param subjectDict       subjectDict
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:22
     **/
    private void updateEntryAuxiliaryAndCashFlow(ParameterSetVo parameterSet, VoucherDto voucherDto, Long voucherId, List<Long> unchangedEntryIds, LambdaQueryWrapper<VoucherEntryAuxiliary> auxiliaryWrapper, LambdaQueryWrapper<VoucherEntryCashFlow> cashFlowWrapper, Map<Integer, VoucherEntry> rowNumEntryDict, @Nullable Map<Long, Map<String, Object>> subjectDict) {
        // 分录核算明细 删除再插入
        if (unchangedEntryIds != null && !unchangedEntryIds.isEmpty()) {
            auxiliaryWrapper.notIn(VoucherEntryAuxiliary::getEntryId, unchangedEntryIds);
        }
        voucherEntryAuxiliaryService.remove(auxiliaryWrapper);
        List<VoucherEntryAuxiliary> saveAuxiliaryList = getVoucherEntryAuxiliaries(voucherDto, voucherId, unchangedEntryIds);
        if (!saveAuxiliaryList.isEmpty()) {
            voucherEntryAuxiliaryService.saveBatch(saveAuxiliaryList);
        }
        List<Long> unchangedCashFlowIds = new LinkedList<>();
        List<VoucherEntryCashFlow> saveCashFlowList = getVoucherEntryCashFlows(voucherDto, voucherId, rowNumEntryDict, subjectDict, unchangedCashFlowIds);
        collectStetCashFlowIds(unchangedCashFlowIds, saveCashFlowList, cashFlowWrapper);
        boolean hasRemove = voucherEntryCashFlowService.remove(cashFlowWrapper);
        boolean hasCashFlows = !saveCashFlowList.isEmpty();
        if (hasCashFlows || hasRemove) {
            if (parameterSet != null) {
                boolean specifiedCashFlow = checkCashFlowParamSet(voucherDto, parameterSet);
                updateVoucherEntryForSpecifiedCashFlow(voucherDto, voucherId, specifiedCashFlow);
            }
            if (hasCashFlows) {
                voucherEntryCashFlowService.saveOrUpdateBatch(saveCashFlowList);
            }
        }
    }

    /**
     * 添加凭证
     *
     * @param voucherDto          voucherDto
     * @param operator            operator
     * @param parameterSet        parameterSet
     * @param accountSubjectDto   accountSubjectDto
     * @param needCashFlows       needCashFlows
     * @param interiorVoucherList 自动生成的内部往来对方凭证集合
     * @return java.lang.Long
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:22
     **/
    private Long addVoucher(VoucherDto voucherDto, SysUserVo operator, ParameterSetVo parameterSet, AccountSubjectDto accountSubjectDto, List<VoucherEntryDto> needCashFlows, List<VoucherDto> interiorVoucherList) {
        Long voucherId;
        checkChronological(voucherDto, parameterSet, null);
        Voucher createVoucher = buildNewVoucherInfo(voucherDto, operator, LedgerConstant.VoucherStatus.PENDING);
        Map<Long, Map<String, Object>> subjectDict = null;
        if (!voucherDto.getEditEntryList().isEmpty()) {
            // 获取分录使用的科目信息,辅助核算数据字典 并校验分录传参+校验辅助核算+内部往来明细
            Map<String, Map<Long, Map<String, Object>>> auxiliaryDict = new LinkedHashMap<>();
            // 存储科目/辅助核算数据字典
            subjectDict = getSubjectDict(voucherDto, createVoucher, accountSubjectDto, auxiliaryDict);
            // 标记内部往来在借方/贷方 [0]-借 [1]-贷
            boolean[] interiorUse = new boolean[2];
            // 默认为不需要生成
            createVoucher.setInteriorType(Constant.InteriorType.NEEDLESS);
            for (VoucherEntryDto entryDto : voucherDto.getEditEntryList()) {
                countStandard(entryDto);
                Integer rowNum = entryDto.getRowNum();
                checkEntryValid(voucherDto, subjectDict, auxiliaryDict, needCashFlows, interiorVoucherList, createVoucher, interiorUse, entryDto, rowNum);
            }
        }
        voucherMapper.insert(createVoucher);
        voucherId = createVoucher.getId();
        // 插入分录+辅助核算
        Map<Integer, VoucherEntry> rowNumEntryDict = insertEntryAndAuxiliary(voucherDto, voucherId);
        boolean hasCashAmount = setCashCheckType(voucherDto, needCashFlows);
        // 插入现金流量
        // 是否校验现金流量： 参数控制“凭证保存时必须指定现金流量项目”为是时 如果有现金流量净发生额 则必须校验现金流量
        List<VoucherEntryCashFlow> saveCashFlowList = getVoucherEntryCashFlows(voucherDto, voucherId, rowNumEntryDict, hasCashAmount ? subjectDict : null, null);
        boolean specifiedCashFlow = checkCashFlowParamSet(voucherDto, parameterSet);
        if (!saveCashFlowList.isEmpty()) {
            voucherEntryCashFlowService.saveBatch(saveCashFlowList);
        }
        if (specifiedCashFlow) {
            // 已指定现金流量,批量更新分录字段
            VoucherEntry voucherEntry = new VoucherEntry();
            voucherEntry.setCashFlowType(Constant.CashFlowType.SPECIFIED);
            voucherEntryService.update(voucherEntry, new LambdaQueryWrapper<VoucherEntry>()
                    .eq(VoucherEntry::getVoucherId, voucherId)
                    .eq(VoucherEntry::getCashFlowType, Constant.CashFlowType.UNSPECIFIED));
        }
        updateBalance(voucherDto, Constant.BalanceUpdateType.ADD);
        generateInteriorVoucherList(voucherDto, interiorVoucherList, voucherId);
        // 获取凭证号
        Voucher updateVoucher = generateCode(voucherDto);
        updateVoucher.setId(voucherId);
        updateVoucher.setCashFlowAmount(voucherDto.getCashFlowAmount());
        updateVoucher.setCashCheckType(voucherDto.getCashCheckType());
        updateVoucher.setInteriorType(voucherDto.getInteriorType());
        List<Voucher> updateVouchers = new LinkedList<>();
        updateVouchers.add(updateVoucher);
        // 获取协同凭证号 更新
        updateVoucherCodeBatch(updateVouchers, interiorVoucherList.isEmpty(), interiorVoucherList);
        return voucherId;
    }

    /**
     * 校验现金流量分析参数设置
     *
     * @param voucherDto   voucherDto
     * @param parameterSet parameterSet
     * @return boolean
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:22
     **/
    private boolean checkCashFlowParamSet(VoucherDto voucherDto, ParameterSetVo parameterSet) {
        boolean specifiedCashFlow = Constant.CashFlowCheckType.EXAMINED == voucherDto.getCashCheckType();
        if (!getCashFlowEnableStatus(voucherDto)) {
            // 未启用现金流量系统时不继续校验
            return specifiedCashFlow;
        }
        boolean passCheckCashFlow = Constant.CashFlowCheckType.NEEDLESS == voucherDto.getCashCheckType() || specifiedCashFlow;
        if (Constant.Is.YES.equals(FastUtils.getParamSetSub(parameterSet, voucherDto.getAccountBookId(), Constant.ParameterSetKey.IS_MUST_SET_CASH_FLOW).getValue().byteValue()) && !passCheckCashFlow) {
            throw new ServiceException(ResultCode.CASH_FLOW_ANALYZE_IMBALANCE);
        }
        return specifiedCashFlow;
    }

    /**
     * 获取现金流量校验类型
     *
     * @param voucherDto    voucherDto
     * @param needCashFlows needCashFlows
     * @return boolean
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:23
     **/
    private boolean setCashCheckType(VoucherDto voucherDto, List<VoucherEntryDto> needCashFlows) {
        // 标记存在现金流量净发生额
        boolean hasCashAmount = false;
        if (!needCashFlows.isEmpty()) {
            hasCashAmount = isHasCashAmount(voucherDto, needCashFlows);
            setCashFlowCheckTypeIsExamined(voucherDto, !hasCashAmount);
        } else {
            voucherDto.setCashCheckType(Constant.CashFlowCheckType.NEEDLESS);
        }
        return hasCashAmount;
    }

    /**
     * 新增协同凭证
     *
     * @param voucherDto          voucherDto
     * @param interiorVoucherList interiorVoucherList
     * @param voucherId           voucherId
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:25
     **/
    private void generateInteriorVoucherList(VoucherDto voucherDto, List<VoucherDto> interiorVoucherList, Long voucherId) {
        if (!interiorVoucherList.isEmpty()) {
            interiorVoucherList.forEach(interiorVoucher -> interiorVoucher.setFirstAbstract(interiorVoucher.getEditEntryList().get(0).getAbstractContent()));
            // 插入协同凭证 分录 核算明细 现金流量分析明细 协同信息表
            voucherMapper.insertGenerateBatch(interiorVoucherList);
            for (VoucherDto interiorVoucher : interiorVoucherList) {
                Long interiorVoucherId = interiorVoucher.getId();
                voucherEntryService.insertBatch(interiorVoucher.getEditEntryList(), interiorVoucherId);
                List<VoucherEntryAuxiliary> saveAuxiliaryList = getVoucherEntryAuxiliaries(interiorVoucher, interiorVoucherId, null);
                if (!saveAuxiliaryList.isEmpty()) {
                    voucherEntryAuxiliaryService.saveBatch(saveAuxiliaryList);
                }
                List<VoucherEntryCashFlowDto> entryCashFlowList = interiorVoucher.getEditEntryCashFlowList();
                if (entryCashFlowList != null && !entryCashFlowList.isEmpty()) {
                    voucherEntryCashFlowService.insertBatch(entryCashFlowList, interiorVoucherId);
                }
                // 维护协同信息表
                voucherEntryInteriorService.insertInteriorRelation(Collections.singletonList(interiorVoucher.getEditEntryList().get(1)), voucherId, interiorVoucherId);
                // 更新余额
                updateBalance(interiorVoucher, Constant.BalanceUpdateType.ADD);
            }
            voucherDto.setInteriorType(Constant.InteriorType.GENERATE);
            VoucherEntry updateEntry = new VoucherEntry();
            updateEntry.setInteriorType(Constant.InteriorType.GENERATE);
            voucherEntryService.update(updateEntry, new LambdaQueryWrapper<VoucherEntry>()
                    .eq(VoucherEntry::getVoucherId, voucherId)
                    .eq(VoucherEntry::getInteriorType, Constant.InteriorType.NOT_GENERATE));
        }
    }

    /**
     * 校验分录数据是否合法
     *
     * @param voucherDto          voucherDto
     * @param subjectDict         subjectDict
     * @param auxiliaryDict       auxiliaryDict
     * @param needCashFlows       needCashFlows
     * @param interiorVoucherList 自动生成的内部往来对方凭证集合
     * @param createVoucher       createVoucher
     * @param interiorUse         interiorUse
     * @param entryDto            entryDto
     * @param rowNum              rowNum
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:25
     **/
    private void checkEntryValid(VoucherDto voucherDto, Map<Long, Map<String, Object>> subjectDict, Map<String, Map<Long, Map<String, Object>>> auxiliaryDict, List<VoucherEntryDto> needCashFlows, final List<VoucherDto> interiorVoucherList, Voucher createVoucher, boolean[] interiorUse, VoucherEntryDto entryDto, Integer rowNum) {
        // 校验科目数据是否合法
        Map<String, Object> subjectMap = checkSubjectValid(subjectDict, entryDto, rowNum);
        // 内部往来不可选其他辅助核算
        boolean isInterior = FastUtils.isStatus(subjectMap, Constant.PropertyName.IS_INTERIOR);
        // 如果有辅助核算,校验
        Object accountSubjectAuxiliaryListObj = subjectMap.get(Constant.PropertyName.ACCOUNT_SUBJECT_AUXILIARY_LIST);
        List<VoucherEntryAuxiliaryDto> auxiliaryDtoList = entryDto.getEditAuxiliaryList();
        if (!isInterior && accountSubjectAuxiliaryListObj instanceof List) {
            checkAuxiliaryItemValid(auxiliaryDict, rowNum, auxiliaryDtoList, (List) accountSubjectAuxiliaryListObj);
            entryDto.setInteriorType(Constant.InteriorType.NEEDLESS);
        } else if (isInterior) {
            // 如果有内部往来,校验
            collectInteriorVouchers(voucherDto, subjectDict, auxiliaryDict, createVoucher, interiorVoucherList, interiorUse, entryDto, rowNum, auxiliaryDtoList);
        } else {
            entryDto.setEditAuxiliaryList(Collections.emptyList());
            entryDto.setInteriorType(Constant.InteriorType.NEEDLESS);
        }
        if (subjectMap.get(Constant.PropertyName.ACCOUNT_CATEGORY).toString().startsWith(Constant.AccountCategory.A)) {
            if (isInterior) {
                throw new ServiceException(ResultCode.DATA_ERROR);
            }
            // 如果有现金流量,先标记未分析
            needCashFlows.add(entryDto);
            entryDto.setCashFlowType(Constant.CashFlowType.UNSPECIFIED);
        } else {
            entryDto.setEditCashFlowList(Collections.emptyList());
            entryDto.setCashFlowType(Constant.CashFlowType.NEEDLESS);
        }
    }

    /**
     * 校验并收集生成的内部往来凭证
     *
     * @param voucherDto          voucherDto
     * @param subjectDict         subjectDict
     * @param auxiliaryDict       auxiliaryDict
     * @param createVoucher       createVoucher
     * @param interiorVoucherList 自动生成的内部往来对方凭证集合
     * @param interiorUse         interiorUse
     * @param entryDto            entryDto
     * @param rowNum              rowNum
     * @param auxiliaryDtoList    auxiliaryDtoList
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:26
     **/
    private void collectInteriorVouchers(VoucherDto voucherDto, Map<Long, Map<String, Object>> subjectDict, Map<String, Map<Long, Map<String, Object>>> auxiliaryDict, Voucher createVoucher, final List<VoucherDto> interiorVoucherList, boolean[] interiorUse, VoucherEntryDto entryDto, Integer rowNum, List<VoucherEntryAuxiliaryDto> auxiliaryDtoList) {
        if (entryDto.getDebitAmount() != null && entryDto.getDebitAmount().compareTo(BigDecimal.ZERO) != 0) {
            interiorUse[0] = true;
        } else {
            interiorUse[1] = true;
        }
        if (interiorUse[0] == interiorUse[1]) {
            throw new ServiceException(ResultCode.INTERIOR_ONLY_ONE_SIDE);
        }
        entryDto.setInteriorType(Constant.InteriorType.NOT_GENERATE);
        if (auxiliaryDtoList == null || auxiliaryDtoList.isEmpty()) {
            throw new ServiceException(ResultCode.INTERIOR_ERROR);
        }
        // 一对一指定协同方分录
        VoucherEntryAuxiliaryDto auxiliaryDto = auxiliaryDtoList.get(0);
        checkAuxiliaryValid(auxiliaryDict, rowNum, auxiliaryDto);
        auxiliaryDto.setSourceTable(Constant.TableName.ACCOUNT_BOOK_ENTITY);
        Long accountEntityId = auxiliaryDto.getItemValueId();
        // 生成对方的协同凭证 分录逐行生成对方凭证
        VoucherDto interiorVoucher = new VoucherDto();
        interiorVoucherList.add(interiorVoucher);
        interiorVoucher.setDebitAmount(BigDecimal.ZERO);
        interiorVoucher.setCreditAmount(BigDecimal.ZERO);
        interiorVoucher.setCashFlowAmount(BigDecimal.ZERO);
        // 校验录入的协同分录
        VoucherEntryDto inputInteriorEntry = entryDto.getEditInteriorEntry();
        // 此处拿到的是输入的subjectMap
        Map<String, Object> inputSubjectMap = checkSubjectValid(subjectDict, inputInteriorEntry, rowNum);
        // 双方不能都是内部往来科目
        if (FastUtils.isStatus(inputSubjectMap, Constant.PropertyName.IS_INTERIOR)) {
            throw new ServiceException(ResultCode.INTERIOR_ONLY_ONE_SIDE, rowNum);
        }
        inputInteriorEntry.setInteriorType(Constant.InteriorType.NEEDLESS);
        countStandard(inputInteriorEntry);
        // 自动生成的分录及辅助核算
        VoucherEntryDto interiorEntry = generateInteriorEntry(voucherDto, entryDto, inputInteriorEntry);
        // 内部往来科目map
        Map<String, Object> interiorSubjectMap = subjectDict.get(interiorEntry.getAccountSubjectId());
        fillInteriorVoucherInfo(voucherDto, createVoucher, accountEntityId, interiorVoucher, inputSubjectMap, inputInteriorEntry, interiorSubjectMap, interiorEntry);
        // 汇总自动生成数据
        interiorVoucher.getEditEntryList().add(interiorEntry);
        // 汇总手动录入数据
        interiorVoucher.getEditEntryList().add(inputInteriorEntry);
    }

    /**
     * 补充协同凭证明细信息
     *
     * @param voucherDto         voucherDto
     * @param createVoucher      createVoucher
     * @param accountEntityId    accountEntityId
     * @param interiorVoucher    interiorVoucher
     * @param inputSubjectMap    录入的科目map
     * @param inputInteriorEntry inputInteriorEntry
     * @param interiorSubjectMap interiorSubjectMap内部往来科目map
     * @param interiorEntry      interiorEntry
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:27
     **/
    private void fillInteriorVoucherInfo(VoucherDto voucherDto, Voucher createVoucher, Long accountEntityId, VoucherDto interiorVoucher, Map<String, Object> inputSubjectMap, VoucherEntryDto inputInteriorEntry, Map<String, Object> interiorSubjectMap, VoucherEntryDto interiorEntry) {
        // 计算借贷方合计
        BigDecimal inputInteriorEntryDebit = inputInteriorEntry.getDebitAmount();
        boolean inputInteriorUseDebit = inputInteriorEntryDebit != null && BigDecimal.ZERO.compareTo(inputInteriorEntryDebit) != 0;
        if (inputInteriorUseDebit) {
            // 累计录入借方及自动生成的贷方
            interiorVoucher.setDebitAmount(interiorVoucher.getDebitAmount().add(inputInteriorEntry.getDebitAmount()));
            interiorVoucher.setCreditAmount(interiorVoucher.getCreditAmount().add(interiorEntry.getCreditAmount()));
        } else {
            interiorVoucher.setDebitAmount(interiorVoucher.getDebitAmount().add(interiorEntry.getDebitAmount()));
            interiorVoucher.setCreditAmount(interiorVoucher.getCreditAmount().add(inputInteriorEntry.getCreditAmount()));
        }
        if (inputSubjectMap.get(Constant.PropertyName.ACCOUNT_CATEGORY).toString().startsWith(Constant.AccountCategory.A)) {
            if (getCashFlowEnableStatus(voucherDto)) {
                // 如果有现金流量,自动分析 标记现金流量检查类型
                interiorVoucher.setCashCheckType(Constant.CashFlowCheckType.EXAMINED);
                VoucherEntryCashFlowDto interiorCashFlow = new VoucherEntryCashFlowDto();
                Object cashFlowItemId;
                if (inputInteriorUseDebit) {
                    // 使用借方,流入
                    cashFlowItemId = interiorSubjectMap.get(Constant.PropertyName.CASH_INFLOW_ID);
                    if (cashFlowItemId == null) {
                        throw new ServiceException(ResultCode.INTERIOR_SUBJECT_MISSING_IN_CONFIG, interiorSubjectMap.get(Constant.PropertyName.ID));
                    }
                    interiorCashFlow.setCurrencyAmount(inputInteriorEntryDebit);
                    // 净发生额计算
                    interiorVoucher.setCashFlowAmount(interiorVoucher.getCashFlowAmount().add(inputInteriorEntryDebit));
                } else {
                    cashFlowItemId = interiorSubjectMap.get(Constant.PropertyName.CASH_OUTFLOW_ID);
                    if (cashFlowItemId == null) {
                        throw new ServiceException(ResultCode.INTERIOR_SUBJECT_MISSING_OUT_CONFIG, interiorSubjectMap.get(Constant.PropertyName.ID));
                    }
                    interiorCashFlow.setCurrencyAmount(inputInteriorEntry.getCreditAmount());
                    // 净发生额计算
                    interiorVoucher.setCashFlowAmount(interiorVoucher.getCashFlowAmount().subtract(inputInteriorEntry.getCreditAmount()));
                }
                interiorCashFlow.setCashFlowItemId(Long.valueOf(cashFlowItemId.toString()));
                // 存储关联分录对象,用于获取关联ID
                interiorCashFlow.setEntry(inputInteriorEntry);
                interiorCashFlow.setOppositeEntry(interiorEntry);
                inputInteriorEntry.setEditCashFlowList(Collections.singletonList(interiorCashFlow));
                inputInteriorEntry.setCashFlowType(Constant.CashFlowType.SPECIFIED);
                interiorVoucher.getEditEntryCashFlowList().add(interiorCashFlow);
            } else {
                interiorVoucher.setCashCheckType(Constant.CashFlowCheckType.UNEXAMINED);
            }
        } else {
            interiorVoucher.setCashCheckType(Constant.CashFlowCheckType.NEEDLESS);
            inputInteriorEntry.setEditCashFlowList(Collections.emptyList());
            inputInteriorEntry.setCashFlowType(Constant.CashFlowType.NEEDLESS);
        }
        // 补充数据
        interiorVoucher.setAccountBookEntityId(accountEntityId);
        interiorVoucher.setSourceType(LedgerConstant.SourceType.COLLABORATE);
        interiorVoucher.setSourceSystem(Constant.SourceSystem.LEDGER);
        interiorVoucher.setSourceVoucher(createVoucher);
        interiorVoucher.setAbstractContent(interiorEntry.getAbstractContent());
        // 前端传核算主体名
        interiorVoucher.setAccountBookEntityName(inputInteriorEntry.getAccountBookEntityName());
        interiorVoucher.setRootEnterpriseId(voucherDto.getRootEnterpriseId());
        interiorVoucher.setAccountBookId(voucherDto.getAccountBookId());
        interiorVoucher.setAccountBookName(voucherDto.getAccountBookName());
        interiorVoucher.setVoucherDate(voucherDto.getVoucherDate());
        interiorVoucher.setPostingPeriodYear(voucherDto.getPostingPeriodYear());
        interiorVoucher.setPostingPeriodNum(voucherDto.getPostingPeriodNum());
        interiorVoucher.setPeriodYearNum(voucherDto.getPeriodYearNum());
        interiorVoucher.setStatus(voucherDto.getStatus());
        interiorVoucher.setInteriorType(Constant.InteriorType.NEEDLESS);
        interiorVoucher.setCreatorId(voucherDto.getCreatorId());
        interiorVoucher.setCreatorName(voucherDto.getCreatorName());
        // 计算收付转
        if (voucherDto.getCredentialWord() == Constant.CredentialWordType.RECORD) {
            interiorVoucher.setCredentialWord(Constant.CredentialWordType.RECORD);
        } else if (Constant.CashFlowCheckType.NEEDLESS == interiorVoucher.getCashCheckType()) {
            interiorVoucher.setCredentialWord(Constant.CredentialWordType.TRANSFER);
        } else if (interiorVoucher.getCashFlowAmount().compareTo(BigDecimal.ZERO) > 0) {
            interiorVoucher.setCredentialWord(Constant.CredentialWordType.RECEIVE);
        } else {
            interiorVoucher.setCredentialWord(Constant.CredentialWordType.PAY);
        }
    }

    /**
     * 生成协同凭证分录
     *
     * @param voucherDto         voucherDto
     * @param entryDto           entryDto
     * @param inputInteriorEntry inputInteriorEntry
     * @return com.njwd.entity.ledger.dto.VoucherEntryDto
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:27
     **/
    private VoucherEntryDto generateInteriorEntry(VoucherDto voucherDto, VoucherEntryDto entryDto, VoucherEntryDto inputInteriorEntry) {
        VoucherEntryDto interiorEntry = new VoucherEntryDto();
        VoucherEntryAuxiliaryDto generateEntryAuxiliary = new VoucherEntryAuxiliaryDto();
        generateEntryAuxiliary.setSourceTable(Constant.TableName.ACCOUNT_BOOK_ENTITY);
        generateEntryAuxiliary.setItemValueId(voucherDto.getAccountBookEntityId());
        interiorEntry.getEditAuxiliaryList().add(generateEntryAuxiliary);
        interiorEntry.setAbstractContent(inputInteriorEntry.getAbstractContent());
        interiorEntry.setAccountSubjectId(entryDto.getAccountSubjectId());
        interiorEntry.setOriginalDebitAmount(entryDto.getOriginalCreditAmount());
        interiorEntry.setOriginalCreditAmount(entryDto.getOriginalDebitAmount());
        interiorEntry.setOriginalCoin(entryDto.getOriginalCoin());
        interiorEntry.setExchangeRate(entryDto.getExchangeRate());
        // 内部往来科目 应为非现金类科目
        interiorEntry.setCashFlowType(Constant.CashFlowType.NEEDLESS);
        interiorEntry.setInteriorType(Constant.InteriorType.NEEDLESS);
        countStandard(interiorEntry);
        // 输入的分录存储关联数据，用于获取分录ID->维护关联表
        inputInteriorEntry.setInteriorEntry(entryDto);
        return interiorEntry;
    }

    /**
     * 检查借贷双方是否都是现金银行类科目且金额相等，如果是则不需进行现金流量分析
     *
     * @param voucherDto    voucherDto
     * @param needCashFlows needCashFlows
     * @return boolean
     * @author xyyxhcj@qq.com
     * @date 2019/8/21 14:17
     **/
    private boolean isHasCashAmount(VoucherDto voucherDto, List<VoucherEntryDto> needCashFlows) {
        BigDecimal debitCashCount = BigDecimal.ZERO;
        BigDecimal creditCashCount = BigDecimal.ZERO;
        for (VoucherEntryDto needCashFlowEntry : needCashFlows) {
            if (BigDecimal.ZERO.compareTo(needCashFlowEntry.getOriginalDebitAmount()) != 0) {
                debitCashCount = debitCashCount.add(needCashFlowEntry.getDebitAmount());
            } else {
                creditCashCount = creditCashCount.add(needCashFlowEntry.getCreditAmount());
            }
        }
        voucherDto.setCashFlowAmount(debitCashCount.subtract(creditCashCount));
        return BigDecimal.ZERO.compareTo(voucherDto.getCashFlowAmount()) != 0;
    }

    /**
     * 更新分录的现金流量字段为已指定/未指定
     *
     * @param voucherDto        voucherDto
     * @param voucherId         voucherId
     * @param specifiedCashFlow specifiedCashFlow
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:28
     **/
    private void updateVoucherEntryForSpecifiedCashFlow(VoucherDto voucherDto, Long voucherId, boolean specifiedCashFlow) {
        VoucherEntry voucherEntry = new VoucherEntry();
        byte cashFlowType;
        if (specifiedCashFlow) {
            // 已指定现金流量,批量更新分录字段为已指定
            voucherDto.setCashCheckType(Constant.CashFlowCheckType.EXAMINED);
            cashFlowType = Constant.CashFlowType.SPECIFIED;
        } else {
            // 未指定现金流量,批量更新分录字段为未指定
            voucherDto.setCashCheckType(Constant.CashFlowCheckType.UNEXAMINED);
            cashFlowType = Constant.CashFlowType.UNSPECIFIED;
        }
        voucherEntry.setCashFlowType(cashFlowType);
        voucherEntryService.update(voucherEntry, new LambdaQueryWrapper<VoucherEntry>()
                .eq(VoucherEntry::getVoucherId, voucherId)
                .ne(VoucherEntry::getCashFlowType, Constant.CashFlowType.NEEDLESS));
    }

    /**
     * 收集不删除的现金流量分析ID
     *
     * @param unchangedIds     unchangedIds
     * @param saveCashFlowList saveCashFlowList
     * @param cashFlowWrapper  cashFlowWrapper
     * @author xyyxhcj@qq.com
     * @date 2019/8/21 15:12
     **/
    private void collectStetCashFlowIds(List<Long> unchangedIds, List<VoucherEntryCashFlow> saveCashFlowList, LambdaQueryWrapper<VoucherEntryCashFlow> cashFlowWrapper) {
        List<Long> existIds = new LinkedList<>();
        for (VoucherEntryCashFlow cashFlow : saveCashFlowList) {
            if (cashFlow.getId() != null) {
                existIds.add(cashFlow.getId());
            }
        }
        existIds.addAll(unchangedIds);
        if (!existIds.isEmpty()) {
            cashFlowWrapper.notIn(VoucherEntryCashFlow::getId, existIds);
        }
    }

    /**
     * 构造字典 现金流量项目 + 科目 + 序号->分录
     *
     * @param voucherDto        voucherDto
     * @param needCashFlows     needCashFlows
     * @param voucherId         voucherId
     * @param entryCashFlowDict entryCashFlowDict
     * @param rowNumEntryDict   rowNumEntryDict
     * @return java.util.Map<java.lang.Long, java.util.Map < java.lang.String, java.lang.Object>>
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:29
     **/
    private Map<Long, Map<String, Object>> fillCashFlowDict(VoucherDto voucherDto, List<VoucherEntryDto> needCashFlows, Long voucherId, Map<Integer, List<VoucherEntryCashFlowDto>> entryCashFlowDict, Map<Integer, VoucherEntry> rowNumEntryDict) {
        for (VoucherEntryDto entryDto : voucherDto.getEditEntryList()) {
            Integer rowNum = entryDto.getRowNum();
            for (VoucherEntryCashFlowDto entryCashFlowDto : entryDto.getEditCashFlowList()) {
                entryCashFlowDict.computeIfAbsent(rowNum, k -> new LinkedList<>()).add(entryCashFlowDto);
            }
            // 构造字典
            rowNumEntryDict.put(rowNum, entryDto);
        }
        List<VoucherEntry> existEntryList = voucherEntryService.list(new LambdaQueryWrapper<VoucherEntry>().eq(VoucherEntry::getVoucherId, voucherId));
        // 取出所有使用科目
        Set<Long> subjectIds = new HashSet<>();
        for (VoucherEntry entry : existEntryList) {
            Integer rowNum = entry.getRowNum();
            Long accountSubjectId = entry.getAccountSubjectId();
            subjectIds.add(accountSubjectId);
            VoucherEntry rowNumEntry = rowNumEntryDict.get(rowNum);
            rowNumEntry.setAccountSubjectId(accountSubjectId);
            rowNumEntry.setId(entry.getId());
        }
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        Map<Long, Map<String, Object>> subjectDict = getSubjectDict(subjectIds, accountSubjectDto);
        for (VoucherEntry entry : existEntryList) {
            Map<String, Object> subjectMap = subjectDict.get(entry.getAccountSubjectId());
            if (subjectMap == null) {
                throw new ServiceException(ResultCode.ACCOUNT_SUBJECT_NOT_EXIST, entry.getRowNum());
            }
            if (subjectMap.get(Constant.PropertyName.ACCOUNT_CATEGORY).toString().startsWith(Constant.AccountCategory.A)) {
                // 如果有现金流量,先标记未分析
                VoucherEntryDto needCashFlowEntry = new VoucherEntryDto();
                FastUtils.copyProperties(entry, needCashFlowEntry);
                needCashFlowEntry.setEditCashFlowList(entryCashFlowDict.get(entry.getRowNum()));
                needCashFlowEntry.setCashFlowType(Constant.CashFlowType.UNSPECIFIED);
                needCashFlows.add(needCashFlowEntry);
            }
        }
        return subjectDict;
    }

    /**
     * 获取科目字典
     *
     * @param subjectIds        subjectIds
     * @param accountSubjectDto accountSubjectDto
     * @return java.util.Map<java.lang.Long, java.util.Map < java.lang.String, java.lang.Object>>
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:30
     **/
    private Map<Long, Map<String, Object>> getSubjectDict(Set<Long> subjectIds, AccountSubjectDto accountSubjectDto) {
        Map<Long, Map<String, Object>> subjectDict = LedgerUtils.getSubjectDict(accountSubjectFeignClient, new ArrayList<>(subjectIds), accountSubjectDto);
        if (subjectDict == null) {
            throw new ServiceException(ResultCode.ACCOUNT_SUBJECT_NOT_EXIST);
        }
        return subjectDict;
    }

    /**
     * 校验现金流量分析的参数设置
     *
     * @param voucherDto   voucherDto
     * @param parameterSet parameterSet
     * @param existVoucher existVoucher
     * @author xyyxhcj@qq.com
     * @date 2019/8/21 13:55
     **/
    private void checkSaveCashFlowParamSet(VoucherDto voucherDto, ParameterSetVo parameterSet, Voucher existVoucher) {
        if (Constant.CashFlowCheckType.NEEDLESS == existVoucher.getCashCheckType()) {
            // 在凭证当凭证不需要现金流量分析的时候，点击凭证的“现金流量”按钮后，提示“当前凭证无需进行现金流量分析”
            throw new ServiceException(ResultCode.CASH_FLOW_NEEDLESS);
        }
        switch (existVoucher.getStatus()) {
            case LedgerConstant.VoucherStatus.DRAFT:
                throw new ServiceException(ResultCode.DATA_ERROR);
            case LedgerConstant.VoucherStatus.PENDING:
                break;
            case LedgerConstant.VoucherStatus.POSTING:
                // 已审核待过账
                // 校验 总账参数“已审核、已过账凭证允许修改现金流量”为“是” 该参数已移除2019/8/27
                //if (Constant.Is.NO.equals(parameterSet.getIsCanUpdateCash())) {
                //    throw new ServiceException(ResultCode.POST_CASH_FLOW_CANT_EDIT);
                //}
                break;
            case LedgerConstant.VoucherStatus.POST:
                // 已过账
                //if (Constant.Is.NO.equals(parameterSet.getIsCanUpdateCash())) {
                //    throw new ServiceException(ResultCode.POST_CASH_FLOW_CANT_EDIT);
                //}
                // 更新余额时需要同时更新已过账数据
                voucherDto.setPostingStatus(existVoucher.getPostingStatus());
                break;
            default:
        }
        // 已冲销及冲销凭证不可修改现金流量 不限制?
        //if (Constant.Is.YES.equals(existVoucher.getIsOffset()) || LedgerConstant.SourceType.RUSH == existVoucher.getSourceType()) {
        //    throw new ServiceException(ResultCode.RUSH_CANT_SAVE_CASH_FLOW);
        //}
        AccountBookPeriod accountPeriod = accountBookPeriodMapper.selectOne(new LambdaQueryWrapper<AccountBookPeriod>()
                .eq(AccountBookPeriod::getAccountBookId, voucherDto.getAccountBookId())
                .eq(AccountBookPeriod::getPeriodYear, voucherDto.getPostingPeriodYear())
                .eq(AccountBookPeriod::getPeriodNum, voucherDto.getPostingPeriodNum())
                .eq(AccountBookPeriod::getSystemSign, Constant.SystemSignValue.LEDGER)
                .last(Constant.ConcatSql.LIMIT_1));
        if (accountPeriod == null) {
            throw new ServiceException(ResultCode.DATA_ERROR);
        }
        if (Constant.Is.YES.equals(accountPeriod.getIsSettle())) {
            // 已结账时，校验总账参数“结账后允许修改现金流量
            if (Constant.Is.NO.equals(FastUtils.getParamSetSub(parameterSet, voucherDto.getAccountBookId(), Constant.ParameterSetKey.IS_UPDATE_CASH).getValue().byteValue())) {
                throw new ServiceException(ResultCode.SETTLE_CASH_FLOW_CANT_EDIT);
            }
        }
    }

    /**
     * 批量更新凭证号
     *
     * @param updateVouchers      updateVouchers
     * @param isEmpty             isEmpty 集合是否为空
     * @param generateVoucherList generateVoucherList
     * @author xyyxhcj@qq.com
     * @date 2019/8/22 16:16
     **/
    @Override
    public void updateVoucherCodeBatch(List<Voucher> updateVouchers, boolean isEmpty, Collection<VoucherDto> generateVoucherList) {
        if (!isEmpty) {
            for (VoucherDto generateVoucher : generateVoucherList) {
                Voucher updateGenerateVoucher = generateCode(generateVoucher);
                updateVouchers.add(updateGenerateVoucher);
            }
        }
        updateBatchById(updateVouchers);
    }

    /**
     * 校验被冲销的数据是否可用
     *
     * @param remoteDataDict subjectDict等远程接口请求数据
     * @param id             id
     * @param name           name
     * @author xyyxhcj@qq.com
     * @date 2019/8/22 14:11
     **/
    private void checkValidForRushData(Map<Long, Map<String, Object>> remoteDataDict, Long id, String name, String dataType) {
        Map<String, Object> subjectMap = remoteDataDict.get(id);
        if (subjectMap == null || FastUtils.isStatus(subjectMap, Constant.PropertyName.IS_DEL)) {
            throw new ServiceException(ResultCode.DATA_ERROR);
        }
        if (subjectMap.get(Constant.PropertyName.IS_ENABLE) != null && !FastUtils.isStatus(subjectMap, Constant.PropertyName.IS_ENABLE)) {
            throw new ServiceException(String.format(ResultCode.RUSH_FAIL_REASON_DISABLE.message, subjectMap.get(Constant.PropertyName.CODE), subjectMap.get(name) + (dataType == null ? "" : dataType)), ResultCode.RUSH_FAIL_REASON_DISABLE);
        }
    }

    /**
     * 合并凭证明细
     *
     * @param existVoucherList   existVoucherList
     * @param entryList          entryList
     * @param entryAuxiliaryList entryAuxiliaryList
     * @param entryCashFlowList  entryCashFlowList
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:31
     **/
    private void mergeVoucherListDetail(List<VoucherDto> existVoucherList, List<VoucherEntryDto> entryList, List<VoucherEntryAuxiliaryDto> entryAuxiliaryList, List<VoucherEntryCashFlowDto> entryCashFlowList) {
        // 分类
        MergeUtil.mergeList(entryList, entryAuxiliaryList, VoucherEntry::getId, VoucherEntryAuxiliary::getEntryId,
                (target, sourceList) -> target.getEditAuxiliaryList().addAll(sourceList));
        MergeUtil.mergeList(entryList, entryCashFlowList, VoucherEntry::getId, VoucherEntryCashFlow::getEntryId,
                (target, sourceList) -> target.getEditCashFlowList().addAll(sourceList));
        MergeUtil.mergeList(existVoucherList, entryList, Voucher::getId, VoucherEntry::getVoucherId,
                (target, sourceList) -> target.getEditEntryList().addAll(sourceList));
    }

    /**
     * 生成冲销表头
     *
     * @param operator            operator
     * @param existVoucher        existVoucher
     * @param datePeriodVoucher   datePeriodVoucher
     * @param generateVoucherList generateVoucherList
     * @author xyyxhcj@qq.com
     * @date 2019/8/22 11:28
     **/
    private void addGenerateVoucher(SysUserVo operator, Voucher existVoucher, Voucher datePeriodVoucher, List<VoucherDto> generateVoucherList) {
        VoucherDto generateVoucher = new VoucherDto();
        generateVoucher.setId(existVoucher.getId());
        generateVoucher.setRootEnterpriseId(existVoucher.getRootEnterpriseId());
        generateVoucher.setAccountBookId(existVoucher.getAccountBookId());
        generateVoucher.setAccountBookName(existVoucher.getAccountBookName());
        generateVoucher.setAccountBookEntityId(existVoucher.getAccountBookEntityId());
        generateVoucher.setAccountBookEntityName(existVoucher.getAccountBookEntityName());
        generateVoucher.setVoucherDate(datePeriodVoucher.getVoucherDate());
        generateVoucher.setPostingPeriodYear(datePeriodVoucher.getPostingPeriodYear());
        generateVoucher.setPostingPeriodNum(datePeriodVoucher.getPostingPeriodNum());
        generateVoucher.setPeriodYearNum(datePeriodVoucher.getPeriodYearNum());
        generateVoucher.setBillNum(existVoucher.getBillNum());
        generateVoucher.setCredentialWord(existVoucher.getCredentialWord());
        generateVoucher.setSourceType(LedgerConstant.SourceType.RUSH);
        generateVoucher.setSourceSystem(Constant.SourceSystem.LEDGER);
        generateVoucher.setSourceCode(existVoucher.getId().toString());
        generateVoucher.setFirstAbstract(existVoucher.getFirstAbstract());
        generateVoucher.setDebitAmount(existVoucher.getDebitAmount().negate());
        generateVoucher.setCreditAmount(existVoucher.getCreditAmount().negate());
        generateVoucher.setCashCheckType(existVoucher.getCashCheckType());
        generateVoucher.setCashFlowAmount(existVoucher.getCashFlowAmount().negate());
        generateVoucher.setStatus(LedgerConstant.VoucherStatus.PENDING);
        generateVoucher.setInteriorType(Constant.InteriorType.NEEDLESS);
        generateVoucher.setCreatorId(operator.getUserId());
        generateVoucher.setCreatorName(operator.getName());
        generateVoucherList.add(generateVoucher);
    }

    /**
     * 校验状态是已过账 不可为已冲销 来源方式-手工
     *
     * @param existVoucher existVoucher
     * @author xyyxhcj@qq.com
     * @date 2019/8/22 9:45
     **/
    private void checkGenerateOffsetValid(Voucher existVoucher) {
        if (Constant.Is.NO.equals(existVoucher.getPostingStatus())) {
            throw new ServiceException(ResultCode.VOUCHER_NOT_POST);
        }
        if (Constant.Is.YES.equals(existVoucher.getIsOffset())) {
            throw new ServiceException(ResultCode.VOUCHER_RUSHED);
        }
        if (LedgerConstant.SourceType.MANUAL != existVoucher.getSourceType()) {
            throw new ServiceException(ResultCode.VOUCHER_NOT_MANUAL);
        }
    }

    /**
     * 更新凭证发生额
     *
     * @param voucherDto voucherDto
     * @param updateType updateType
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:32
     **/
    private void updateBalance(VoucherDto voucherDto, byte updateType) {
        Map<Long, BalanceCashFlowDto> balanceCashFlowMap = new LinkedHashMap<>();
        Map<Long, BalanceSubjectDto> balanceSubjectMap = new LinkedHashMap<>();
        Map<Long, Map<String, BalanceSubjectAuxiliaryDto>> balanceSubjectAuxiliaryIdMap = new LinkedHashMap<>();
        // 计算准备更新的发生额
        countBalance(voucherDto, balanceCashFlowMap, balanceSubjectMap, balanceSubjectAuxiliaryIdMap);
        // 将分录统计的变更余额写入科目余额/现金流量发生额表
        if (!balanceCashFlowMap.isEmpty()) {
            LedgerUtils.lockCashFlow(voucherDto, () -> updateCashFlowBalance(balanceCashFlowMap, voucherDto, updateType));
        }
        if (!balanceSubjectMap.isEmpty()) {
            LedgerUtils.lockSubject(voucherDto, () -> {
                updateSubjectBalance(balanceSubjectMap, voucherDto, updateType);
                if (!balanceSubjectAuxiliaryIdMap.isEmpty()) {
                    updateSubjectAuxiliaryBalance(balanceSubjectAuxiliaryIdMap, voucherDto, updateType);
                }
                return null;
            });
        }
    }

    /**
     * 校验辅助核算项与科目配置是否一致
     *
     * @param auxiliaryDict               auxiliaryDict
     * @param rowNum                      rowNum
     * @param auxiliaryDtoList            前端传的数据auxiliaryDtoList
     * @param accountSubjectAuxiliaryList 科目接口返回的实时数据accountSubjectAuxiliaryList
     * @author xyyxhcj@qq.com
     * @date 2019/8/15 16:15
     **/
    private void checkAuxiliaryItemValid(Map<String, Map<Long, Map<String, Object>>> auxiliaryDict, Integer rowNum, List<VoucherEntryAuxiliaryDto> auxiliaryDtoList, List accountSubjectAuxiliaryList) {
        Set<String> dtoSet = new HashSet<>();
        for (VoucherEntryAuxiliaryDto auxiliaryDto : auxiliaryDtoList) {
            dtoSet.add(auxiliaryDto.getSourceTable());
            checkAuxiliaryValid(auxiliaryDict, rowNum, auxiliaryDto);
        }
        Set<String> subjectConfig = new HashSet<>();
        for (Object map : accountSubjectAuxiliaryList) {
            subjectConfig.add((String) ((Map) map).get(Constant.PropertyName.AUXILIARY_SOURCE_TABLE));
        }
        if (!dtoSet.toString().equals(subjectConfig.toString())) {
            LOGGER.warn("科目数据与前端传参不符，前端传的{}，科目接口返回{}", dtoSet, subjectConfig);
            throw new ServiceException(ResultCode.AUXILIARY_ERROR, rowNum);
        }
    }

    /**
     * 校验科目数据是否可用
     *
     * @param subjectDict subjectDict
     * @param entryDto    entryDto
     * @param rowNum      rowNum
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @author xyyxhcj@qq.com
     * @date 2019/8/15 16:13
     **/
    private Map<String, Object> checkSubjectValid(Map<Long, Map<String, Object>> subjectDict, VoucherEntryDto entryDto, @Nullable Integer rowNum) {
        Map<String, Object> subjectMap = subjectDict.get(entryDto.getAccountSubjectId());
        LedgerUtils.checkSubjectValid(subjectMap, rowNum, ResultCode.ACCOUNT_SUBJECT_NOT_EXIST, Constant.PropertyName.IS_DEL, true);
        LedgerUtils.checkSubjectValid(subjectMap, rowNum, ResultCode.ACCOUNT_SUBJECT_DISABLE, Constant.PropertyName.IS_ENABLE, false);
        LedgerUtils.checkSubjectValid(subjectMap, rowNum, ResultCode.ACCOUNT_SUBJECT_NOT_FINAL, Constant.PropertyName.IS_FINAL, false);
        return subjectMap;
    }

    /**
     * 更新现金流量发生额
     *
     * @param balanceCashFlowMap balanceCashFlowMap
     * @param voucherDto         voucherDto
     * @param updateType         updateType
     * @return java.lang.Object
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:32
     **/
    private Object updateCashFlowBalance(Map<Long, BalanceCashFlowDto> balanceCashFlowMap, VoucherDto voucherDto, byte updateType) {
        Set<Long> cashFlowIds = new LinkedHashSet<>(balanceCashFlowMap.keySet());
        if (cashFlowIds.isEmpty()) {
            return null;
        }
        List<BalanceCashFlow> balanceCashFlowList = balanceCashFlowService.list(new LambdaQueryWrapper<BalanceCashFlow>()
                .eq(BalanceCashFlow::getAccountBookId, voucherDto.getAccountBookId())
                .eq(BalanceCashFlow::getAccountBookEntityId, voucherDto.getAccountBookEntityId())
                .eq(BalanceCashFlow::getPeriodYear, voucherDto.getPostingPeriodYear())
                .eq(BalanceCashFlow::getPeriodNum, voucherDto.getPostingPeriodNum())
                .in(BalanceCashFlow::getItemId, cashFlowIds)
                .select(BalanceCashFlow::getItemId));
        // 查询未初始化的数据
        for (BalanceCashFlow balanceCashFlow : balanceCashFlowList) {
            cashFlowIds.remove(balanceCashFlow.getItemId());
        }
        if (Constant.BalanceUpdateType.ADD == updateType) {
            // 插入
            List<BalanceCashFlow> initBalanceCashFlow = new LinkedList<>();
            for (Long cashFlowId : cashFlowIds) {
                BalanceCashFlow balanceCashFlow = new BalanceCashFlow();
                initBalanceCashFlow.add(balanceCashFlow);
                balanceCashFlow.setAccountBookId(voucherDto.getAccountBookId());
                balanceCashFlow.setAccountBookEntityId(voucherDto.getAccountBookEntityId());
                balanceCashFlow.setItemId(cashFlowId);
                balanceCashFlow.setPeriodYear(voucherDto.getPostingPeriodYear());
                balanceCashFlow.setPeriodNum(voucherDto.getPostingPeriodNum());
                balanceCashFlow.setPeriodYearNum(voucherDto.getPeriodYearNum());
            }
            if (!initBalanceCashFlow.isEmpty()) {
                balanceCashFlowService.saveBatch(initBalanceCashFlow);
            }
        } else if (!cashFlowIds.isEmpty()) {
            throw new ServiceException(ResultCode.DATA_ERROR);
        }
        // 更新
        if (!balanceCashFlowMap.isEmpty()) {
            balanceCashFlowService.updateBatch(balanceCashFlowMap.values(), voucherDto, updateType);
        }
        return null;
    }

    /**
     * 更新科目余额
     *
     * @param balanceSubjectMap balanceSubjectMap
     * @param voucherDto        voucherDto
     * @param updateType        updateType
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:33
     **/
    private void updateSubjectBalance(Map<Long, BalanceSubjectDto> balanceSubjectMap, VoucherDto voucherDto, byte updateType) {
        // 查询未初始化的数据
        Set<Long> ids = new LinkedHashSet<>(balanceSubjectMap.keySet());
        List<BalanceSubject> balanceList = balanceSubjectService.list(new LambdaQueryWrapper<BalanceSubject>()
                .eq(BalanceSubject::getAccountBookId, voucherDto.getAccountBookId())
                .eq(BalanceSubject::getAccountBookEntityId, voucherDto.getAccountBookEntityId())
                .eq(BalanceSubject::getPeriodYear, voucherDto.getPostingPeriodYear())
                .eq(BalanceSubject::getPeriodNum, voucherDto.getPostingPeriodNum())
                .in(BalanceSubject::getAccountSubjectId, ids)
                .select(BalanceSubject::getAccountSubjectId));
        for (BalanceSubject balance : balanceList) {
            ids.remove(balance.getAccountSubjectId());
        }
        if (Constant.BalanceUpdateType.ADD == updateType) {
            // 插入
            List<BalanceSubject> initBalance = new LinkedList<>();
            for (Long id : ids) {
                BalanceSubject balance = new BalanceSubject();
                initBalance.add(balance);
                balance.setAccountBookId(voucherDto.getAccountBookId());
                balance.setAccountBookEntityId(voucherDto.getAccountBookEntityId());
                balance.setAccountSubjectId(id);
                balance.setPeriodYear(voucherDto.getPostingPeriodYear());
                balance.setPeriodNum(voucherDto.getPostingPeriodNum());
                balance.setPeriodYearNum(voucherDto.getPeriodYearNum());
            }
            if (!initBalance.isEmpty()) {
                balanceSubjectService.saveBatch(initBalance);
            }
        } else if (!ids.isEmpty()) {
            throw new ServiceException(ResultCode.DATA_ERROR);
        }
        // 更新
        if (!balanceSubjectMap.isEmpty()) {
            balanceSubjectService.updateBatch(balanceSubjectMap.values(), voucherDto, updateType);
        }
    }

    /**
     * 更新辅助核算科目余额
     *
     * @param balanceSubjectAuxiliaryIdMap balanceSubjectAuxiliaryIdMap
     * @param voucherDto                   voucherDto
     * @param updateType                   updateType
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:33
     **/
    private void updateSubjectAuxiliaryBalance(Map<Long, Map<String, BalanceSubjectAuxiliaryDto>> balanceSubjectAuxiliaryIdMap, VoucherDto voucherDto, byte updateType) {
        // 查询未初始化的数据
        List<BalanceSubjectAuxiliaryItem> existBalanceItemList = balanceSubjectAuxiliaryItemService.list(new LambdaQueryWrapper<BalanceSubjectAuxiliaryItem>()
                .eq(BalanceSubjectAuxiliaryItem::getAccountBookId, voucherDto.getAccountBookId())
                .eq(BalanceSubjectAuxiliaryItem::getAccountBookEntityId, voucherDto.getAccountBookEntityId())
                .eq(BalanceSubjectAuxiliaryItem::getPeriodYear, voucherDto.getPostingPeriodYear())
                .eq(BalanceSubjectAuxiliaryItem::getPeriodNum, voucherDto.getPostingPeriodNum())
                .in(BalanceSubjectAuxiliaryItem::getAccountSubjectId, balanceSubjectAuxiliaryIdMap.keySet())
                .select(BalanceSubjectAuxiliaryItem::getBalanceAuxiliaryId,
                        BalanceSubjectAuxiliaryItem::getAccountSubjectId,
                        BalanceSubjectAuxiliaryItem::getSourceTable,
                        BalanceSubjectAuxiliaryItem::getItemValueId));
        Map<Long, BalanceSubjectAuxiliaryDto> existBalanceSubjectAuxiliaryMap = new LinkedHashMap<>();
        for (BalanceSubjectAuxiliaryItem subjectAuxiliaryItem : existBalanceItemList) {
            BalanceSubjectAuxiliaryDto balanceSubjectAuxiliaryDto = existBalanceSubjectAuxiliaryMap.computeIfAbsent(subjectAuxiliaryItem.getBalanceAuxiliaryId(), k -> new BalanceSubjectAuxiliaryDto());
            balanceSubjectAuxiliaryDto.getBalanceSubjectAuxiliaryItems().add(subjectAuxiliaryItem);
            if (balanceSubjectAuxiliaryDto.getId() == null) {
                balanceSubjectAuxiliaryDto.setId(subjectAuxiliaryItem.getBalanceAuxiliaryId());
                balanceSubjectAuxiliaryDto.setAccountSubjectId(subjectAuxiliaryItem.getAccountSubjectId());
            }
        }
        Collection<BalanceSubjectAuxiliaryDto> existBalanceSubjectAuxiliaryList = existBalanceSubjectAuxiliaryMap.values();
        for (BalanceSubjectAuxiliaryDto balance : existBalanceSubjectAuxiliaryList) {
            balance.setKeySign(new StringBuilder());
            balance.getKeySign().append(balance.getAccountSubjectId()).append(Constant.Character.UNDER_LINE);
            Set<BalanceSubjectAuxiliaryItem> temps = balance.getBalanceSubjectAuxiliaryItems();
            for (BalanceSubjectAuxiliaryItem balanceSubjectAuxiliaryItem : temps) {
                balance.getKeySign().append(balanceSubjectAuxiliaryItem.getSign()).append(Constant.Character.UNDER_LINE);
            }
        }
        List<BalanceSubjectAuxiliaryDto> initBalanceSubjectAuxiliaries = new LinkedList<>();
        Map<String, BalanceSubjectAuxiliaryDto> allBalanceSubjectAuxiliaryMap = new LinkedHashMap<>();
        for (Map<String, BalanceSubjectAuxiliaryDto> balanceSubjectAuxiliaryMap : balanceSubjectAuxiliaryIdMap.values()) {
            allBalanceSubjectAuxiliaryMap.putAll(balanceSubjectAuxiliaryMap);
            initBalanceSubjectAuxiliaries.addAll(balanceSubjectAuxiliaryMap.values());
        }
        initBalanceSubjectAuxiliaries.removeAll(existBalanceSubjectAuxiliaryList);
        if (Constant.BalanceUpdateType.ADD == updateType) {
            // 插入
            if (!initBalanceSubjectAuxiliaries.isEmpty()) {
                balanceSubjectAuxiliaryService.initBatch(initBalanceSubjectAuxiliaries, voucherDto);
            }
        } else if (!initBalanceSubjectAuxiliaries.isEmpty()) {
            throw new ServiceException(ResultCode.DATA_ERROR);
        }
        // 已存在数据设主键
        for (BalanceSubjectAuxiliaryDto balanceSubjectAuxiliaryDto : existBalanceSubjectAuxiliaryList) {
            BalanceSubjectAuxiliaryDto balance = allBalanceSubjectAuxiliaryMap.get(balanceSubjectAuxiliaryDto.getKeySign().toString());
            if (balance != null) {
                balance.setId(balanceSubjectAuxiliaryDto.getId());
            }
        }
        // 更新
        balanceSubjectAuxiliaryService.updateBatch(allBalanceSubjectAuxiliaryMap.values(), voucherDto, updateType);
    }

    /**
     * 计算余额/发生额
     **/
    private void countBalance(VoucherDto voucherDto, Map<Long, BalanceCashFlowDto> balanceCashFlowMap, Map<Long, BalanceSubjectDto> balanceSubjectMap, Map<Long, Map<String, BalanceSubjectAuxiliaryDto>> balanceSubjectAuxiliaryIdMap) {
        if (voucherDto.getStatus() != null && LedgerConstant.VoucherStatus.DRAFT == voucherDto.getStatus()) {
            return;
        }
        BigDecimal debitAmount;
        BigDecimal creditAmount;
        BigDecimal standardDebitAmount;
        BigDecimal standardCreditAmount;
        for (VoucherEntryDto entryDto : voucherDto.getEditEntryList()) {
            Long subjectId = entryDto.getAccountSubjectId();
            BalanceSubjectDto balanceSubject = balanceSubjectMap.computeIfAbsent(subjectId, k -> new BalanceSubjectDto());
            balanceSubject.setSubjectId(subjectId);
            standardDebitAmount = entryDto.getDebitAmount();
            standardCreditAmount = entryDto.getCreditAmount();
            // 取出已累计的值,追加发生额
            debitAmount = balanceSubject.getDebitAmount();
            if (debitAmount == null) {
                debitAmount = BigDecimal.ZERO;
            }
            creditAmount = balanceSubject.getCreditAmount();
            if (creditAmount == null) {
                creditAmount = BigDecimal.ZERO;
            }
            balanceSubject.setDebitAmount(standardDebitAmount.add(debitAmount));
            balanceSubject.setCreditAmount(standardCreditAmount.add(creditAmount));
            if (!entryDto.getEditAuxiliaryList().isEmpty()) {
                Map<String, BalanceSubjectAuxiliaryDto> balanceSubjectAuxiliaryMap = balanceSubjectAuxiliaryIdMap.computeIfAbsent(subjectId, k -> new LinkedHashMap<>());
                Set<VoucherEntryAuxiliaryDto> balanceSubjectAuxiliarySet = new HashSet<>(entryDto.getEditAuxiliaryList());
                StringBuilder keySign = new StringBuilder();
                keySign.append(entryDto.getAccountSubjectId()).append(Constant.Character.UNDER_LINE);
                for (VoucherEntryAuxiliaryDto auxiliaryDto : balanceSubjectAuxiliarySet) {
                    keySign.append(auxiliaryDto.getSign()).append(Constant.Character.UNDER_LINE);
                }
                BalanceSubjectAuxiliaryDto balanceSubjectAuxiliary = balanceSubjectAuxiliaryMap.computeIfAbsent(keySign.toString(), k -> new BalanceSubjectAuxiliaryDto());
                // 取出辅助核算项已累计的值,追加发生额
                debitAmount = balanceSubjectAuxiliary.getDebitAmount();
                if (debitAmount == null) {
                    debitAmount = BigDecimal.ZERO;
                }
                creditAmount = balanceSubjectAuxiliary.getCreditAmount();
                if (creditAmount == null) {
                    creditAmount = BigDecimal.ZERO;
                }
                balanceSubjectAuxiliary.setAccountSubjectId(subjectId);
                balanceSubjectAuxiliary.setDebitAmount(standardDebitAmount.add(debitAmount));
                balanceSubjectAuxiliary.setCreditAmount(standardCreditAmount.add(creditAmount));
                balanceSubjectAuxiliary.setVoucherEntryAuxiliaryList(entryDto.getEditAuxiliaryList());
                // 存储核算明细,用于更新科目余额时判断
                balanceSubjectAuxiliary.setKeySign(keySign);
                // 仅当未放入明细时再放入
                if (balanceSubjectAuxiliary.getBalanceSubjectAuxiliaryItems().isEmpty()) {
                    for (VoucherEntryAuxiliaryDto auxiliaryDto : entryDto.getEditAuxiliaryList()) {
                        BalanceSubjectAuxiliaryItem item = new BalanceSubjectAuxiliaryItem();
                        item.setAccountBookId(voucherDto.getAccountBookId());
                        item.setAccountBookEntityId(voucherDto.getAccountBookEntityId());
                        item.setAccountSubjectId(balanceSubjectAuxiliary.getAccountSubjectId());
                        item.setPeriodYear(voucherDto.getPostingPeriodYear());
                        item.setPeriodNum(voucherDto.getPostingPeriodNum());
                        item.setPeriodYearNum(voucherDto.getPeriodYearNum());
                        item.setSourceTable(auxiliaryDto.getSourceTable());
                        item.setItemValueId(auxiliaryDto.getItemValueId());
                        balanceSubjectAuxiliary.getBalanceSubjectAuxiliaryItems().add(item);
                    }
                }
            }
            countBalanceForCashFlow(balanceCashFlowMap, entryDto.getEditCashFlowList(), voucherDto.getPostingStatus());
        }
    }

    /**
     * 计算现金流量发生额
     *
     * @param balanceCashFlowMap balanceCashFlowMap
     * @param entryCashFlowList  entryCashFlowList
     * @param postingStatus      postingStatus
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:33
     **/
    private void countBalanceForCashFlow(Map<Long, BalanceCashFlowDto> balanceCashFlowMap, List<? extends VoucherEntryCashFlow> entryCashFlowList, Byte postingStatus) {
        if (entryCashFlowList == null) {
            return;
        }
        for (VoucherEntryCashFlow cashFlow : entryCashFlowList) {
            BalanceCashFlowDto balanceCashFlow = balanceCashFlowMap.computeIfAbsent(cashFlow.getCashFlowItemId(), k -> new BalanceCashFlowDto());
            balanceCashFlow.setItemId(cashFlow.getCashFlowItemId());
            BigDecimal occurAmount = balanceCashFlow.getOccurAmount();
            if (occurAmount == null) {
                occurAmount = BigDecimal.ZERO;
            }
            BigDecimal countAmount = cashFlow.getCurrencyAmount().add(occurAmount);
            balanceCashFlow.setOccurAmount(countAmount);
            if (Constant.Is.YES.equals(postingStatus)) {
                // 如果为已过账 则记录已过账余额
                balanceCashFlow.setPostOccurAmount(countAmount);
            }
        }
    }

    /**
     * 校验辅助核算/内部往来 数据有效性
     **/
    private void checkAuxiliaryValid(Map<String, Map<Long, Map<String, Object>>> auxiliaryDict, Integer rowNum, VoucherEntryAuxiliaryDto auxiliaryDto) {
        Map<Long, Map<String, Object>> auxiliaryData = auxiliaryDict.get(auxiliaryDto.getSourceTable());
        if (auxiliaryData == null) {
            throw new ServiceException(ResultCode.AUXILIARY_ITEM_NOT_EXIST, rowNum);
        }
        Map<String, Object> auxiliaryMap = auxiliaryData.get(auxiliaryDto.getItemValueId());
        if (auxiliaryMap == null || FastUtils.isStatus(auxiliaryMap, Constant.PropertyName.IS_DEL)) {
            throw new ServiceException(ResultCode.AUXILIARY_ITEM_NOT_EXIST, rowNum);
        }
        if (auxiliaryMap.get(Constant.PropertyName.IS_ENABLE) != null && !FastUtils.isStatus(auxiliaryMap, Constant.PropertyName.IS_ENABLE)) {
            throw new ServiceException(ResultCode.AUXILIARY_ITEM_NOT_ENABLE, rowNum);
        }
    }

    /**
     * 校验序时控制 2019/8/26去掉序时控制
     **/
    private void checkChronological(VoucherDto voucherDto, ParameterSetVo parameterSet, Voucher existVoucher) {
        /*if (Constant.Is.YES.equals(parameterSet.getIsAddOrderTime())) {
            Date voucherDate = DateUtils.beginOfDate(voucherDto.getVoucherDate());
            // 表示变更凭证字
            boolean changeWord = !existVoucher.getCredentialWord().equals(voucherDto.getCredentialWord());
            // 由草稿保存
            boolean draft2Save = existVoucher != null && LedgerConstant.VoucherStatus.DRAFT == existVoucher.getStatus();
            if (voucherDto.getId() == null || draft2Save || changeWord) {
                // 新增凭证：如果制单日期小于记账期间的最大制单日期，提示“序时控制，制单日期必须大于等于2019-6-25日！”
                // 获取最后一张主号的制单时间
                Voucher limitDateVoucher = voucherMapper.selectOne(new LambdaQueryWrapper<Voucher>()
                        .eq(Voucher::getCredentialWord, voucherDto.getCredentialWord())
                        .eq(Voucher::getPostingPeriodYear, voucherDto.getPostingPeriodYear())
                        .eq(Voucher::getPostingPeriodNum, voucherDto.getPostingPeriodNum())
                        .eq(Voucher::getAccountBookId, voucherDto.getAccountBookId())
                        .orderByDesc(Voucher::getMainCode).select(Voucher::getVoucherDate)
                        .last(Constant.ConcatSql.LIMIT_1));
                Date limitDate = limitDateVoucher.getVoucherDate();
                if (voucherDate.before(limitDate)) {
                    throw new ServiceException(String.format(ResultCode.CHRONOLOGICAL_CONTROL_NEW.message, DateUtils.format(limitDate, DateUtils.PATTERN_DAY)), ResultCode.CHRONOLOGICAL_CONTROL_NEW);
                }
            } else {
                // 编辑凭证：修改的日期只能大于等于上一个凭证号的日期 小于等于下一张凭证号的日期，否则提示“序时控制，制单日期必须大于等于2019-6-2日且小于等于2019-6-4！”（V1.2.2）
                // 根据凭证号查上一张凭证和下一张凭证
                Voucher ltVoucher = voucherMapper.selectOne(new LambdaQueryWrapper<Voucher>()
                        .eq(Voucher::getCredentialWord, voucherDto.getCredentialWord())
                        .eq(Voucher::getPostingPeriodYear, voucherDto.getPostingPeriodYear())
                        .eq(Voucher::getPostingPeriodNum, voucherDto.getPostingPeriodNum())
                        .eq(Voucher::getAccountBookId, voucherDto.getAccountBookId())
                        .lt(Voucher::getMainCode, existVoucher.getMainCode())
                        .orderByDesc(Voucher::getMainCode).select(Voucher::getVoucherDate)
                        .last(Constant.ConcatSql.LIMIT_1));
                Voucher gtVoucher = voucherMapper.selectOne(new LambdaQueryWrapper<Voucher>()
                        .eq(Voucher::getCredentialWord, voucherDto.getCredentialWord())
                        .eq(Voucher::getPostingPeriodYear, voucherDto.getPostingPeriodYear())
                        .eq(Voucher::getPostingPeriodNum, voucherDto.getPostingPeriodNum())
                        .eq(Voucher::getAccountBookId, voucherDto.getAccountBookId())
                        .gt(Voucher::getMainCode, existVoucher.getMainCode())
                        .orderByAsc(Voucher::getMainCode).select(Voucher::getVoucherDate)
                        .last(Constant.ConcatSql.LIMIT_1));
                if (!DateUtils.isBetween(voucherDate, ltVoucher.getVoucherDate(), gtVoucher.getVoucherDate())) {
                    String ltDate = DateUtils.format(ltVoucher.getVoucherDate(), DateUtils.PATTERN_DAY);
                    String gtDate = DateUtils.format(gtVoucher.getVoucherDate(), DateUtils.PATTERN_DAY);
                    throw new ServiceException(String.format(ResultCode.CHRONOLOGICAL_CONTROL_UPDATE.message, ltDate, gtDate), ResultCode.CHRONOLOGICAL_CONTROL_UPDATE);
                }
            }
        }*/
    }

    /**
     * 插入分录+辅助核算
     **/
    private Map<Integer, VoucherEntry> insertEntryAndAuxiliary(VoucherDto voucherDto, Long voucherId) {
        Map<Integer, VoucherEntry> rowNumEntryDict = new HashMap<>();
        fillVoucherEntries(voucherDto, rowNumEntryDict);
        if (!voucherDto.getEditEntryList().isEmpty()) {
            voucherEntryService.insertBatch(voucherDto.getEditEntryList(), voucherId);
        }
        List<VoucherEntryAuxiliary> saveAuxiliaryList = getVoucherEntryAuxiliaries(voucherDto, voucherId, null);
        if (!saveAuxiliaryList.isEmpty()) {
            voucherEntryAuxiliaryService.saveBatch(saveAuxiliaryList);
        }
        return rowNumEntryDict;
    }

    /**
     * 获取分录使用的科目信息,并校验分录传参
     *
     * @param voucherDto        voucherDto
     * @param createVoucher     voucher
     * @param accountSubjectDto accountSubjectDto
     * @param auxiliaryDict     补全辅助核算字典
     * @return java.util.Map<java.lang.Long, java.util.Map < java.lang.String, java.lang.Object>> 科目信息字典
     * @author xyyxhcj@qq.com
     * @date 2019/8/9 13:46
     **/
    private Map<Long, Map<String, Object>> getSubjectDict(VoucherDto voucherDto, Voucher createVoucher, AccountSubjectDto accountSubjectDto, @NotNull Map<String, Map<Long, Map<String, Object>>> auxiliaryDict) {
        Set<Long> subjectIds = new HashSet<>();
        BigDecimal debitAmount = BigDecimal.ZERO;
        BigDecimal creditAmount = BigDecimal.ZERO;
        Map<String, Set<Long>> auxiliaryIdMap = new LinkedHashMap<>();
        for (VoucherEntryDto entryDto : voucherDto.getEditEntryList()) {
            FastUtils.checkParams(entryDto.getOriginalCoin(), entryDto.getOriginalCreditAmount(), entryDto.getOriginalDebitAmount(), entryDto.getExchangeRate());
            Integer rowNum = entryDto.getRowNum();
            // 校验借贷平衡
            BigDecimal entryDebit = entryDto.getOriginalDebitAmount();
            BigDecimal entryCredit;
            if (entryDebit != null && BigDecimal.ZERO.compareTo(entryDebit) != 0) {
                // 使用借方
                entryCredit = BigDecimal.ZERO;
                debitAmount = debitAmount.add(entryDebit);
            } else {
                entryDebit = BigDecimal.ZERO;
                entryCredit = entryDto.getOriginalCreditAmount();
                creditAmount = creditAmount.add(entryCredit);
            }
            fillSubjectAndAuxiliaryData(subjectIds, auxiliaryIdMap, entryDto, voucherDto);
            // 取内部往来使用的科目及辅助核算项id
            VoucherEntryDto editInteriorEntry = entryDto.getEditInteriorEntry();
            if (editInteriorEntry != null) {
                // 校验协同凭证与该分录借贷金额是否一致
                BigDecimal interiorEntryDebit = editInteriorEntry.getOriginalDebitAmount();
                BigDecimal interiorEntryCredit;
                if (interiorEntryDebit != null && BigDecimal.ZERO.compareTo(interiorEntryDebit) != 0) {
                    // 使用借方
                    interiorEntryCredit = BigDecimal.ZERO;
                } else {
                    interiorEntryDebit = BigDecimal.ZERO;
                    interiorEntryCredit = editInteriorEntry.getOriginalCreditAmount();
                }
                boolean passInteriorAmount = interiorEntryDebit.compareTo(entryDebit) == 0 && interiorEntryCredit.compareTo(entryCredit) == 0;
                boolean passInteriorNegate = interiorEntryDebit.compareTo(entryDebit.negate()) == 0 && interiorEntryCredit.compareTo(entryCredit.negate()) == 0;
                if (!passInteriorAmount && !passInteriorNegate) {
                    throw new ServiceException(ResultCode.INTERIOR_IMBALANCE, rowNum);
                }
                fillSubjectAndAuxiliaryData(subjectIds, auxiliaryIdMap, editInteriorEntry, voucherDto);
            }
        }
        if (debitAmount.compareTo(creditAmount) != 0) {
            throw new ServiceException(ResultCode.IMBALANCE);
        }
        createVoucher.setDebitAmount(debitAmount);
        createVoucher.setCreditAmount(creditAmount);
        // 获取数据
        Map<Long, Map<String, Object>> subjectDict = getSubjectDict(subjectIds, accountSubjectDto);
        fillAuxiliaryDict(auxiliaryDict, auxiliaryIdMap);
        return subjectDict;
    }

    /**
     * 获取辅助核算明细数据字典
     *
     * @param auxiliaryDict  auxiliaryDict
     * @param auxiliaryIdMap auxiliaryIdMap
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:35
     **/
    private void fillAuxiliaryDict(@NotNull Map<String, Map<Long, Map<String, Object>>> auxiliaryDict, Map<String, Set<Long>> auxiliaryIdMap) {
        if (auxiliaryIdMap.isEmpty()) {
            return;
        }
        List<String> sourceTableList = new LinkedList<>();
        List<List<List<Long>>> idLists = new LinkedList<>();
        ArrayList<List<Long>> idList;
        for (Map.Entry<String, Set<Long>> entry : auxiliaryIdMap.entrySet()) {
            sourceTableList.add(entry.getKey());
            idList = new ArrayList<>();
            idLists.add(idList);
            idList.add(new ArrayList<>(entry.getValue()));
        }
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        accountSubjectDto.setSourceTableList(sourceTableList);
        accountSubjectDto.setIdLists(idLists);
        Result<List<List<Map<String, Object>>>> allSourceTableInfoResult = accountSubjectFeignClient.findAllSourceTableInfo(accountSubjectDto);
        if (allSourceTableInfoResult.getData() == null || allSourceTableInfoResult.getData().size() != accountSubjectDto.getSourceTableList().size()) {
            throw new ServiceException(String.format(ResultCode.FEIGN_CONNECT_ERROR.message, FeignClientErrorMsg.GET_AUXILIARY_DATA_ERROR), ResultCode.FEIGN_CONNECT_ERROR);
        }
        int index = 0;
        Map<Long, Map<String, Object>> auxiliaryMapDict;
        for (Map.Entry<String, Set<Long>> entry : auxiliaryIdMap.entrySet()) {
            List<Map<String, Object>> auxiliaryDictData = allSourceTableInfoResult.getData().get(index++);
            if (auxiliaryDictData == null || auxiliaryDictData.isEmpty()) {
                continue;
            }
            auxiliaryMapDict = new LinkedHashMap<>();
            for (Map<String, Object> auxiliaryMap : auxiliaryDictData) {
                Object idObj = auxiliaryMap.get(Constant.PropertyName.ID);
                if (idObj == null) {
                    continue;
                }
                auxiliaryMapDict.put(Long.valueOf(idObj.toString()), auxiliaryMap);
            }
            auxiliaryDict.put(entry.getKey(), auxiliaryMapDict);
        }
    }

    /**
     * 汇总凭证使用的科目及辅助核算项id
     *
     * @param subjectIds     subjectIds
     * @param auxiliaryIdMap auxiliaryIdMap
     * @param entryDto       entryDto
     * @param voucherDto voucherDto
     * @author xyyxhcj@qq.com
     * @date 2019/8/15 15:18
     **/
    private void fillSubjectAndAuxiliaryData(Set<Long> subjectIds, Map<String, Set<Long>> auxiliaryIdMap, VoucherEntryDto entryDto, VoucherDto voucherDto) {
        FastUtils.checkParams(entryDto.getAccountSubjectId());
        subjectIds.add(entryDto.getAccountSubjectId());
        // 取使用的辅助核算数据
        for (VoucherEntryAuxiliaryDto auxiliaryDto : entryDto.getEditAuxiliaryList()) {
            FastUtils.checkParams(auxiliaryDto.getItemValueId());
            if (StringUtils.isEmpty(auxiliaryDto.getSourceTable())) {
                auxiliaryDto.setSourceTable(Constant.TableName.ACCOUNT_BOOK_ENTITY);
            }
            if (Constant.TableName.ACCOUNT_BOOK_ENTITY.equals(auxiliaryDto.getSourceTable())) {
                if (auxiliaryDto.getItemValueId().equals(voucherDto.getAccountBookEntityId())) {
                    throw new ServiceException(ResultCode.ENTRY_ENTITY_SAME_VOUCHER, entryDto.getRowNum());
                }
            }
            auxiliaryIdMap.computeIfAbsent(auxiliaryDto.getSourceTable(), k -> new LinkedHashSet<>()).add(auxiliaryDto.getItemValueId());
        }
    }

    /**
     * 构造凭证,补充新增信息
     *
     * @param voucherDto voucherDto
     * @param operator   operator
     * @param status     凭证状态
     * @return com.njwd.entity.ledger.Voucher
     * @author xyyxhcj@qq.com
     * @date 2019/8/9 13:07
     **/
    private Voucher buildNewVoucherInfo(VoucherDto voucherDto, SysUserVo operator, byte status) {
        Voucher voucher = new Voucher();
        voucherDto.setStatus(status);
        voucherDto.setCreatorId(operator.getUserId());
        voucherDto.setCreatorName(operator.getName());
        voucherDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        voucherDto.setSourceSystem(null);
        voucherDto.setSourceCode(null);
        FastUtils.copyProperties(voucherDto, voucher);
        return voucher;
    }

    /**
     * 校验状态
     *
     * @param existVoucher   existVoucher
     * @param errorCode      errorCode 自定义抛出的异常
     * @param allowStatusArr 允许操作的状态,从小到大排列
     * @author xyyxhcj@qq.com
     * @date 2019/8/9 9:43
     **/
    private void checkStatus(Voucher existVoucher, ResultCode errorCode, byte... allowStatusArr) {
        if (Arrays.binarySearch(allowStatusArr, existVoucher.getStatus()) < 0) {
            throw new ServiceException(errorCode);
        }
        // 校验是否为已复核 协同凭证
        if (Constant.Is.YES.equals(existVoucher.getReviewStatus())
                || LedgerConstant.SourceType.COLLABORATE == existVoucher.getSourceType()) {
            throw new ServiceException(errorCode);
        }
    }

    /**
     * 校验参数是否可用
     *
     * @param voucherDto voucherDto
     * @author xyyxhcj@qq.com
     * @date 2019/7/26 15:34
     **/
    private void checkParamValid(VoucherDto voucherDto, ParameterSetVo parameterSet) {
        FastUtils.checkParams(voucherDto.getAccountBookEntityId(), voucherDto.getVoucherDate());
        if (parameterSet != null) {
            ParameterSetSub credentialWordType = FastUtils.getParamSetSub(parameterSet, voucherDto.getAccountBookId(), Constant.ParameterSetKey.CREDENTIAL_WORD_TYPE);
            // 校验凭证字
            switch (credentialWordType.getValue().byteValue()) {
                case Constant.CredentialWordSet.RECORD:
                    if (voucherDto.getCredentialWord() != Constant.CredentialWordType.RECORD) {
                        throw new ServiceException(ResultCode.CREDENTIAL_WORD_ERROR);
                    }
                    break;
                case Constant.CredentialWordSet.CASH_TRANSFER:
                    switch (voucherDto.getCredentialWord()) {
                        case Constant.CredentialWordType.RECEIVE:
                        case Constant.CredentialWordType.PAY:
                        case Constant.CredentialWordType.TRANSFER:
                            break;
                        default:
                            throw new ServiceException(ResultCode.CREDENTIAL_WORD_ERROR);
                    }
                    break;
                default:
            }
        }
        Integer periodYear = voucherDto.getPostingPeriodYear();
        Byte periodNum = voucherDto.getPostingPeriodNum();
        // 校验期间
        AccountBookPeriod accountPeriod = checkAccountBookPeriod(voucherDto, periodYear, periodNum);
        periodYear = voucherDto.getBeforePeriodYear();
        periodNum = voucherDto.getBeforePeriodNum();
        if (periodYear != null && periodNum != null) {
            // 如果变更了期间，校验原期间
            checkAccountBookPeriod(voucherDto, periodYear, periodNum);
        }
        // 校验制单日期与期间是否一致
        if (!DateUtils.isBetween(voucherDto.getVoucherDate(), accountPeriod.getStartDate(), accountPeriod.getEndDate())) {
            throw new ServiceException(ResultCode.VOUCHER_DATE_ERROR);
        }
    }

    /**
     * 校验期间
     *
     * @param voucherDto voucherDto
     * @param periodYear periodYear
     * @param periodNum  periodNum
     * @return com.njwd.entity.ledger.AccountBookPeriod
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:36
     **/
    private AccountBookPeriod checkAccountBookPeriod(VoucherDto voucherDto, Integer periodYear, Byte periodNum) {
        AccountBookPeriod accountPeriod = accountBookPeriodMapper.selectOne(new LambdaQueryWrapper<AccountBookPeriod>()
                .eq(AccountBookPeriod::getAccountBookId, voucherDto.getAccountBookId())
                .eq(AccountBookPeriod::getPeriodYear, periodYear)
                .eq(AccountBookPeriod::getPeriodNum, periodNum)
                .eq(AccountBookPeriod::getSystemSign, Constant.SystemSignValue.LEDGER)
                .eq(AccountBookPeriod::getStatus, Constant.Status.ON)
                .last(Constant.ConcatSql.LIMIT_1));
        if (accountPeriod == null) {
            // 校验期间是否打开
            throw new ServiceException(ResultCode.ACCOUNT_PERIOD_OFF);
        }
        if (Constant.Is.YES.equals(accountPeriod.getIsSettle())) {
            // 校验是否已结账
            throw new ServiceException(ResultCode.ACCOUNT_PERIOD_SETTLE);
        }
        return accountPeriod;
    }

    /**
     * 校验版本号并进行自增
     */
    private void checkVersion(Voucher existVoucher, VoucherDto voucherDto) {
        if (!existVoucher.getVersion().equals(voucherDto.getVersion())) {
            throw new ServiceException(ResultCode.VERSION_ERROR);
        }
        int newVersion = existVoucher.getVersion() + 1;
        voucherDto.setVersion(newVersion);
        existVoucher.setVersion(newVersion);
    }

    /**
     * 校验是否可删除
     *
     * @param editVoucherList    editVoucherList
     * @param voucherDtoMap      voucherDtoMap
     * @param existVouchers      existVouchers
     * @param failList           failList
     * @param needUpdateBalances 收集待审核的凭证ids
     * @author xyyxhcj@qq.com
     * @date 2019/8/30 15:37
     **/
    private void checkForDelete(List<VoucherDto> editVoucherList, Map<Long, VoucherDto> voucherDtoMap, List<VoucherDto> existVouchers, List<ReferenceDescription> failList, final List<Long> needUpdateBalances) {
        for (Voucher existVoucher : existVouchers) {
            Long voucherId = existVoucher.getId();
            if (LedgerConstant.SourceType.COLLABORATE == existVoucher.getSourceType()) {
                if (editVoucherList.size() == 1) {
                    // 单张协同凭证不允许删除 来源方式为协同，提示“协同凭证不能直接被删除！”
                    throw new ServiceException(ResultCode.INTERIOR_NOT_DELETE);
                }
                voucherDtoMap.remove(voucherId);
            } else if (Constant.Is.YES.equals(existVoucher.getApproveStatus())) {
                // 已审核
                setFailDescription(voucherDtoMap, failList, voucherId, ResultCode.APPROVE_EXIST_CANT_DELETE);
            } else if (Constant.Is.YES.equals(existVoucher.getReviewStatus())) {
                // 已出纳审核
                setFailDescription(voucherDtoMap, failList, voucherId, ResultCode.REVIEW_EXIST_CANT_DELETE);
            } else if (LedgerConstant.SourceType.FORWARD == existVoucher.getSourceType()) {
                // 损益结转凭证不能直接被删除！
                setFailDescription(voucherDtoMap, failList, voucherId, ResultCode.FORWARD_CANT_DELETE);
            } else if (LedgerConstant.SourceType.RUSH == existVoucher.getSourceType()) {
                // 冲销凭证不能删除
                setFailDescription(voucherDtoMap, failList, voucherId, ResultCode.RUSH_CANT_DELETE);
            } else if (LedgerConstant.SourceType.BUSINESS_SYSTEM == existVoucher.getSourceType()) {
                // 业务系统凭证不能直接被删除
                setFailDescription(voucherDtoMap, failList, voucherId, ResultCode.BUSINESS_SYSTEM_CANT_DELETE);
            } else {
                VoucherDto voucherTemp = voucherDtoMap.get(voucherId);
                if (!existVoucher.getVersion().equals(voucherTemp.getVersion())) {
                    // 校验版本号
                    setFailDescription(voucherDtoMap, failList, voucherId, ResultCode.VERSION_ERROR);
                } else {
                    // 版本号一致
                    voucherTemp.setStatus(existVoucher.getStatus());
                    if (LedgerConstant.VoucherStatus.PENDING == existVoucher.getStatus()) {
                        needUpdateBalances.add(voucherId);
                        voucherTemp.setAccountBookId(existVoucher.getAccountBookId());
                        voucherTemp.setAccountBookEntityId(existVoucher.getAccountBookEntityId());
                        voucherTemp.setPostingPeriodYear(existVoucher.getPostingPeriodYear());
                        voucherTemp.setPostingPeriodNum(existVoucher.getPostingPeriodNum());
                    }
                }
            }
        }
    }

    /**
     *根据租户id和科目id去查询凭证  用于公司间协同 租户端 启用功能
     * 刘遵通
     * @param voucherDto
     * @return
     */
    @Override
    public List<VoucherVo> findVoucherByRootIdAndSubjectid(VoucherDto voucherDto) {
        return voucherMapper.findVoucherByRootIdAndSubjectid(voucherDto);
    }
}
