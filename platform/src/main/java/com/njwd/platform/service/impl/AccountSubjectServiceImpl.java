package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.njwd.common.Constant;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.base.BaseBatchFail;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.basedata.ReferenceContext;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.platform.*;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.entity.platform.vo.FindAccountSubjectListVo;
import com.njwd.entity.platform.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.platform.mapper.*;
import com.njwd.platform.service.*;
import com.njwd.platform.utils.UserUtil;
import com.njwd.service.FileService;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.BatchResult;
import com.njwd.utils.FastUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author liuxiang
 * @Description 会计科目
 * @Date:15:47 2019/7/2
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class AccountSubjectServiceImpl extends ServiceImpl<AccountSubjectMapper, AccountSubject> implements AccountSubjectService {

    @Autowired
    private AccountSubjectMapper accountSubjectMapper;
    @Resource
    private SubjectService subjectService;
    @Resource
    private SubjectCategoryMapper subjectCategoryMapper;
    @Resource
    private AuxiliaryItemMapper auxiliaryItemMapper;
    @Resource
    private AccountSubjectAuxiliaryService accountSubjectAuxiliaryService;
    @Resource
    private BasePlatformMapper basePlatformMapper;
    @Resource
    private CurrencyMapper currencyMapper;
    @Resource
    private AccountSubjectCurrencyService accSubjectCurrencyService;
    @Resource
    private ReferenceRelationService referenceRelationService;
    @Resource
    private FileService fileService;
    @Resource
    private MessageService messageService;
	@Resource
	private AccountSubjectAuxiliaryMapper accSubjectAuxiliaryMapper;
    /**
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.platform.entity.vo.FindAccountSubjectListVo>
     * @Description 更多科目模板分页查询
     * @Author liuxiang
     * @Date:15:47 2019/7/2
     * @Param [accountSubjectDto]
     **/
    @Override
    @Cacheable(value = "accountSubjectPage", key = "#accountSubjectDto.subjectId+'-'+#accountSubjectDto.rootEnterpriseId+'-'+#accountSubjectDto.page.current+'-'+#accountSubjectDto.page.size")
    public Page<FindAccountSubjectListVo> findAccountSubjectPage(AccountSubjectDto accountSubjectDto) {
        Page<FindAccountSubjectListVo> page = accountSubjectDto.getPage();
        page = accountSubjectMapper.findAccountSubjectPage(page, accountSubjectDto);
        return page;
    }

    /**
     * @return java.util.List<com.njwd.platform.entity.vo.FindAccountSubjectListVo>
     * @Description 更多科目模板列表查询
     * @Author liuxiang
     * @Date:15:48 2019/7/2
     * @Param [accountSubjectDto]
     **/
    @Override
    @Cacheable(value = "accountSubjectList", key = "#accountSubjectDto.subjectId+'-'+#accountSubjectDto.rootEnterpriseId")
    public List<FindAccountSubjectListVo> findAccountSubjectList(AccountSubjectDto accountSubjectDto) {
        return accountSubjectMapper.findAccountSubjectList(accountSubjectDto);
    }

    /**
     * 分页
     *
     * @param accountSubjectDto accountSubjectDto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.AccountSubjectVo>
     * @author xyyxhcj@qq.com
     * @date 2019/11/13 15:23
     **/
    @Override
    public Page<AccountSubjectVo> findPage(AccountSubjectDto<AccountSubjectVo> accountSubjectDto) {
        Page<AccountSubjectVo> page = accountSubjectDto.getPage();
        if (accountSubjectDto.getNeedIntroductionId() != null) {
            Subject needIntroductionSubject = subjectService.getById(accountSubjectDto.getNeedIntroductionId());
            FastUtils.checkNull(needIntroductionSubject);
            if (Constant.Is.YES.equals(needIntroductionSubject.getIsBase())) {
                throw new ServiceException(ResultCode.NEEDLESS_INTRODUCTION);
            }
            accountSubjectDto.setSubjectIds(Collections.singletonList(needIntroductionSubject.getSubjectId()));
            accountSubjectDto.setIsReleased(Constant.Is.YES);
        }
        return page.setRecords(accountSubjectMapper.findPage(accountSubjectDto, page));
    }

    /**
     * add
     *
     * @param accSubjectDto accSubjectDto
     * @return java.lang.Long
     * @author xyyxhcj@qq.com
     * @date 2019/11/19 9:33
     **/
    @Override
    public Long add(AccountSubjectDto<AccountSubjectVo> accSubjectDto) {
        Long subjectId = accSubjectDto.getSubjectId();
        FastUtils.checkParams(subjectId, accSubjectDto.getSubjectCategory(), accSubjectDto.getCode(), accSubjectDto.getName(), accSubjectDto.getAccSubjectCurrencyList(), accSubjectDto.getBalanceDirection(), accSubjectDto.getAccountCategory());
        Subject subject = subjectService.getById(subjectId);
        FastUtils.checkNull(subject);
        if (accSubjectDto.getLevel() > subject.getMaximumLevel()) {
            throw new ServiceException(ResultCode.CASH_FLOW_ITEM_MAX_LEVEL);
        }
        Long upId = accSubjectDto.getUpId();
        AccountSubject upAccSubject = null;
	    boolean isBase = Constant.Is.YES.equals(subject.getIsBase());
        boolean hasUp = upId != null;
        if (isBase) {
            accSubjectDto.setBaseSubjectId(Constant.AccountSubjectData.BASE_ACC_SUBJECT_ID_SELF);
            if (hasUp) {
                // 当前会计科目表为基准表时，非必填，取自当前会计科目表中已审核状态未发布状态的会计科目
                upAccSubject = getById(upId);
                FastUtils.checkNull(upAccSubject);
                if (Constant.Is.NO.equals(upAccSubject.getIsApproved()) || Constant.Is.YES.equals(upAccSubject.getIsReleased())) {
                    throw new ServiceException(ResultCode.ACC_SUBJECT_UP_ERROR);
                }
            }
        } else {
            accSubjectDto.setBaseSubjectId(null);
            // 当前会计科目表不为基准表时，上级科目必填
            upAccSubject = checkUpAccSubjectForNotBase(upId);
        }
        // 有上级科目时，默认为上级科目的科目类别，不可修改
        fillCategoryData(accSubjectDto, upAccSubject);
        checkCode(accSubjectDto, subjectId, subject, upAccSubject);
        checkName(accSubjectDto, subjectId, upId);
        boolean hasAuxiliary = checkAuxiliary(accSubjectDto, upAccSubject);
        if (!isBase) {
            checkCurrency(accSubjectDto, upAccSubject);
        }
        Date now = new Date();
        AccountSubject accountSubject = new AccountSubject();
        FastUtils.copyProperties(accSubjectDto, accountSubject);
        SysUserVo operator = UserUtil.getUserVo();
        accountSubject.setCreatorId(operator.getUserId());
        accountSubject.setCreatorName(operator.getName());
        accountSubject.setCreateTime(now);
        saveOrUpdate(accountSubject);
        if (hasAuxiliary) {
            for (AccountSubjectAuxiliary accountSubjectAuxiliary : accSubjectDto.getAccountSubjectAuxiliaryList()) {
                accountSubjectAuxiliary.setAccountId(accountSubject.getId());
                accountSubjectAuxiliary.setCreateTime(now);
                accountSubjectAuxiliary.setAccountSubjectId(accountSubject.getId());
            }
            accountSubjectAuxiliaryService.saveBatch(accSubjectDto.getAccountSubjectAuxiliaryList());
        }
        for (AccountSubjectCurrency currency : accSubjectDto.getAccSubjectCurrencyList()) {
            currency.setAccountId(accountSubject.getId());
            currency.setCreateTime(now);
            currency.setAccountSubjectId(accountSubject.getId());
        }
        accSubjectCurrencyService.saveBatch(accSubjectDto.getAccSubjectCurrencyList());
        if (hasUp) {
            protectionIsFinal(Collections.singleton(upId), false);
        }
        return accountSubject.getId();
    }

    private void protectionIsFinal(Collection<Long> ids, boolean setFinal) {
        List<Long> hasChildIds = list(new LambdaQueryWrapper<AccountSubject>()
                .in(AccountSubject::getUpId, ids)
                .eq(BaseModel::getIsDel, Constant.Is.NO)
                .select(AccountSubject::getUpId, BaseModel::getId))
                .stream().filter(v -> v.getUpId() != null).map(AccountSubject::getUpId).distinct().collect(Collectors.toList());
        LinkedHashSet<Long> withoutChildIds = new LinkedHashSet<>(ids);
        withoutChildIds.removeAll(hasChildIds);
        if (!hasChildIds.isEmpty() && !setFinal) {
            accountSubjectMapper.updateIsFinalByIdIn(Constant.Is.NO, hasChildIds);
        }
        if (!withoutChildIds.isEmpty() && setFinal) {
            accountSubjectMapper.updateIsFinalByIdIn(Constant.Is.YES, withoutChildIds);
        }
    }

    private void checkCurrency(AccountSubjectDto accSubjectDto, AccountSubject upAccSubject) {
        List<AccountSubjectCurrency> upAccSubjectCurrencyList = accSubjectCurrencyService.list(new LambdaQueryWrapper<AccountSubjectCurrency>().eq(AccountSubjectCurrency::getAccountSubjectId, upAccSubject.getId()));
        if (upAccSubjectCurrencyList.size() > accSubjectDto.getAccSubjectCurrencyList().size()) {
            throw new ServiceException(ResultCode.CURRENCY_SELECT_ERROR);
        }
    }

    private boolean checkAuxiliary(AccountSubjectDto accSubjectDto, AccountSubject upAccSubject) {
        boolean hasAuxiliary = accSubjectDto.getAccountSubjectAuxiliaryList() != null && !accSubjectDto.getAccountSubjectAuxiliaryList().isEmpty();
        if (hasAuxiliary) {
            checkAuxiliaryList(accSubjectDto);
            if (upAccSubject != null) {
                List<AccountSubjectAuxiliary> upAccSubjectAuxiliaryList = accountSubjectAuxiliaryService.list(new LambdaQueryWrapper<AccountSubjectAuxiliary>().eq(AccountSubjectAuxiliary::getAccountSubjectId, upAccSubject.getId()));
                if (upAccSubjectAuxiliaryList.size() > accSubjectDto.getAccountSubjectAuxiliaryList().size()) {
                    throw new ServiceException(ResultCode.AUXILIARY_SELECT_ERROR);
                }
            }
        }
        return hasAuxiliary;
    }

    /**
     * update
     *
     * @param accSubjectDto accSubjectDto
     * @return java.lang.Long
     * @author xyyxhcj@qq.com
     * @date 2019/11/20 10:31
     **/
    @Override
    public Long update(AccountSubjectDto accSubjectDto) {
        Long id = accSubjectDto.getId();
        FastUtils.checkParams(id);
        AccountSubject existAccSubject = getById(id);
        FastUtils.checkNull(existAccSubject);
        if (!existAccSubject.getVersion().equals(accSubjectDto.getVersion())) {
            throw new ServiceException(ResultCode.VERSION_ERROR);
        }
        if (Constant.Is.YES.equals(existAccSubject.getIsApproved())) {
            throw new ServiceException(ResultCode.IS_APPROVED);
        }
        accSubjectDto.setSubjectId(null);
        Long subjectId = existAccSubject.getSubjectId();
        Subject subject = subjectService.getById(subjectId);
        if (accSubjectDto.getLevel() > subject.getMaximumLevel()) {
            throw new ServiceException(ResultCode.CASH_FLOW_ITEM_MAX_LEVEL);
        }
        Long upId = accSubjectDto.getUpId();
        AccountSubject upAccSubject = null;
        boolean isBase = Constant.Is.YES.equals(subject.getIsBase());
        if (isBase) {
            if (upId != null) {
                // 当前会计科目表为基准表时，非必填，取自当前会计科目表中已审核状态未发布状态的会计科目
                upAccSubject = getById(upId);
                FastUtils.checkNull(upAccSubject);
                if (Constant.Is.NO.equals(upAccSubject.getIsApproved()) || Constant.Is.YES.equals(upAccSubject.getIsReleased())) {
                    throw new ServiceException(ResultCode.ACC_SUBJECT_UP_ERROR);
                }
            }
        } else {
            // 当前会计科目表不为基准表时，上级科目必填
            upAccSubject = checkUpAccSubjectForNotBase(upId);
        }
        // 有上级科目时，默认为上级科目的科目类别，不可修改
        fillCategoryData(accSubjectDto, upAccSubject);
        checkCode(accSubjectDto, subjectId, subject, upAccSubject);
        checkName(accSubjectDto, subjectId, upId);
        boolean hasAuxiliary = checkAuxiliary(accSubjectDto, upAccSubject);
        accSubjectDto.setCreatorId(null);
        accSubjectDto.setCreatorName(null);
        accSubjectDto.setCreateTime(null);
        Date now = new Date();
        AccountSubject accountSubject = new AccountSubject();
        FastUtils.copyProperties(accSubjectDto, accountSubject);
        SysUserVo operator = UserUtil.getUserVo();
        accountSubject.setUpdatorId(operator.getUserId());
        accountSubject.setUpdatorName(operator.getName());
        accountSubject.setUpdateTime(now);
        LinkedList<Long> maybeIsFinal = new LinkedList<>();
        LinkedList<Long> setNotFinal = new LinkedList<>();
        Long oldUpId = existAccSubject.getUpId();
        if (!Objects.equals(oldUpId, upId)) {
            if (oldUpId != null) {
                maybeIsFinal.add(oldUpId);
            }
            if (upId != null) {
                setNotFinal.add(upId);
            }
        }
        saveOrUpdate(accountSubject);
        accountSubjectAuxiliaryService.remove(new LambdaQueryWrapper<AccountSubjectAuxiliary>().eq(AccountSubjectAuxiliary::getAccountSubjectId, id));
        if (hasAuxiliary) {
            for (AccountSubjectAuxiliary accountSubjectAuxiliary : accSubjectDto.getAccountSubjectAuxiliaryList()) {
                accountSubjectAuxiliary.setAccountId(accountSubject.getId());
                accountSubjectAuxiliary.setAccountSubjectId(accountSubject.getId());
            }
            accountSubjectAuxiliaryService.saveBatch(accSubjectDto.getAccountSubjectAuxiliaryList());
        }
        if (!maybeIsFinal.isEmpty()) {
            protectionIsFinal(maybeIsFinal, true);
        }
        if (!setNotFinal.isEmpty()) {
            protectionIsFinal(setNotFinal, false);
        }
        return id;
    }

    /**
     * delete
     *
     * @param accSubjectDto accSubjectDto
     * @return com.njwd.support.BatchResult
     * @author xyyxhcj@qq.com
     * @date 2019/11/20 11:19
     **/
    @Override
    public BatchResult delete(AccountSubjectDto<AccountSubjectVo> accSubjectDto) {
	    BatchOper batchOper = new BatchOper(accSubjectDto).invoke();
	    BaseBatchFail<AccountSubjectVo, AccountSubjectDto> baseFail = batchOper.getBaseFail();
	    LinkedHashSet<Long> checkIds = batchOper.getCheckIds();
	    Map<Long, AccountSubjectVo> existDataMap = batchOper.getExistDataMap();
        addCheckDeletedAndVersion(baseFail);
        baseFail.checkFails.add(new BaseBatchFail.BaseEachFail<AccountSubjectVo, AccountSubjectDto>(ResultCode.ACC_SUBJECT_IS_NOT_FINAL.message) {
            @Override
            public boolean isFail(AccountSubjectVo t, AccountSubjectDto e) {
                return Constant.Is.YES.equals(t.getHasChild());
            }
        });
        baseFail.checkFails.add(new BaseBatchFail.BaseEachFail<AccountSubjectVo, AccountSubjectDto>(ResultCode.IS_APPROVED.message) {
            @Override
            public boolean isFail(AccountSubjectVo t, AccountSubjectDto e) {
                return Constant.Is.YES.equals(t.getIsApproved());
            }
        });
        List<ReferenceDescription> failList = new LinkedList<>();
        FastUtils.filterIds(accSubjectDto.getBatchEditAccSubjects(), failList, checkIds, existDataMap, baseFail);
        // check reference
        List<Long> successList = new ArrayList<>(checkIds);
        ReferenceContext referenceContext = referenceRelationService.isReference(PlatformConstant.Reference.PLAT_ACCOUNT_SUBJECT, successList);
        failList.addAll(referenceContext.getReferences());
        successList.removeAll(referenceContext.getReferences().stream().map(ReferenceDescription::getBusinessId).collect(Collectors.toList()));
        // update
        if (!successList.isEmpty()) {
            basePlatformMapper.batchProcess(successList, PlatformConstant.OperateType.DELETE, UserUtil.getUserVo(), Constant.TableName.ACCOUNT_SUBJECT);
            accountSubjectAuxiliaryService.remove(new LambdaQueryWrapper<AccountSubjectAuxiliary>().in(AccountSubjectAuxiliary::getAccountSubjectId, successList));
            // 获取所有删除ID的上级
            List<AccountSubject> list = list(new LambdaQueryWrapper<AccountSubject>()
                    .in(AccountSubject::getId, successList)
                    .select(AccountSubject::getUpId, BaseModel::getId));
            List<Long> oldUpIds = list
                    .stream().filter(v -> v.getUpId() != null).map(AccountSubject::getUpId).distinct().collect(Collectors.toList());
            if (!oldUpIds.isEmpty()) {
                protectionIsFinal(oldUpIds, true);
            }
        }
        BatchResult batchResult = new BatchResult();
        batchResult.setFailList(failList);
        batchResult.setSuccessList(successList);
        return batchResult;
    }

    /**
     * approve
     *
     * @param accSubjectDto accSubjectDto
     * @return com.njwd.support.BatchResult
     * @author xyyxhcj@qq.com
     * @date 2019/11/20 14:03
     **/
    @Override
    public BatchResult approve(AccountSubjectDto<AccountSubjectVo> accSubjectDto) {
	    BatchOper batchOper = new BatchOper(accSubjectDto).invoke();
	    BaseBatchFail<AccountSubjectVo, AccountSubjectDto> baseFail = batchOper.getBaseFail();
	    LinkedHashSet<Long> checkIds = batchOper.getCheckIds();
	    Map<Long, AccountSubjectVo> existDataMap = batchOper.getExistDataMap();
        addCheckVersion(baseFail);
        List<ReferenceDescription> failList = new LinkedList<>();
        baseFail.checkFails.add(new BaseBatchFail.BaseEachFail<AccountSubjectVo, AccountSubjectDto>(ResultCode.APPROVE_CHECK_APPROVED.message) {
            @Override
            public boolean isFail(AccountSubjectVo t, AccountSubjectDto e) {
                return Constant.Is.YES.equals(t.getIsApproved());
            }
        });
        FastUtils.filterIds(accSubjectDto.getBatchEditAccSubjects(), failList, checkIds, existDataMap, baseFail);
        // update
        if (!checkIds.isEmpty()) {
            basePlatformMapper.batchProcess(checkIds, PlatformConstant.OperateType.APPROVED, UserUtil.getUserVo(), Constant.TableName.ACCOUNT_SUBJECT);
        }
        BatchResult batchResult = new BatchResult();
        batchResult.setFailList(failList);
        batchResult.setSuccessList(new ArrayList<>(checkIds));
        return batchResult;
    }

    /**
     * reversalApprove
     *
     * @param accSubjectDto accSubjectDto
     * @return com.njwd.support.BatchResult
     * @author xyyxhcj@qq.com
     * @date 2019/11/20 14:12
     **/
    @Override
    public BatchResult reversalApprove(AccountSubjectDto<AccountSubjectVo> accSubjectDto) {
	    BatchOper batchOper = new BatchOper(accSubjectDto).invoke();
	    BaseBatchFail<AccountSubjectVo, AccountSubjectDto> baseFail = batchOper.getBaseFail();
	    LinkedHashSet<Long> checkIds = batchOper.getCheckIds();
	    Map<Long, AccountSubjectVo> existDataMap = batchOper.getExistDataMap();
	    // 引入的基准表科目不能反审核
        baseFail.checkFails.add(new BaseBatchFail.BaseEachFail<AccountSubjectVo, AccountSubjectDto>(ResultCode.INTRODUCTION_ACC_SUBJECT_CANT_REVERSAL_APPROVE.message) {
            @Override
            public boolean isFail(AccountSubjectVo t, AccountSubjectDto e) {
                return t.getBaseAccSubjectId() != null && t.getBaseAccSubjectId() > Constant.AccountSubjectData.BASE_ACC_SUBJECT_ID_SELF;
            }
        });
        baseFail.checkFails.add(new BaseBatchFail.BaseEachFail<AccountSubjectVo, AccountSubjectDto>(ResultCode.REVERSAL_APPROVE_IS_NOT_FINAL.message) {
            @Override
            public boolean isFail(AccountSubjectVo t, AccountSubjectDto e) {
                return Constant.Is.YES.equals(t.getHasChild());
            }
        });
        baseFail.checkFails.add(new BaseBatchFail.BaseEachFail<AccountSubjectVo, AccountSubjectDto>(ResultCode.DISAPPROVE_CHECK_RELEASED.message) {
            @Override
            public boolean isFail(AccountSubjectVo t, AccountSubjectDto e) {
                return Constant.Is.YES.equals(t.getIsReleased());
            }
        });
        baseFail.checkFails.add(new BaseBatchFail.BaseEachFail<AccountSubjectVo, AccountSubjectDto>(ResultCode.DISAPPROVE_CHECK_DISAPPROVED.message) {
            @Override
            public boolean isFail(AccountSubjectVo t, AccountSubjectDto e) {
                return Constant.Is.NO.equals(t.getIsApproved());
            }
        });
        addCheckVersion(baseFail);
        List<ReferenceDescription> failList = new LinkedList<>();
        FastUtils.filterIds(accSubjectDto.getBatchEditAccSubjects(), failList, checkIds, existDataMap, baseFail);
        // update
        if (!checkIds.isEmpty()) {
            basePlatformMapper.batchProcess(checkIds, PlatformConstant.OperateType.DISAPPROVED, UserUtil.getUserVo(), Constant.TableName.ACCOUNT_SUBJECT);
        }
        BatchResult batchResult = new BatchResult();
        batchResult.setFailList(failList);
        batchResult.setSuccessList(new ArrayList<>(checkIds));
        return batchResult;
    }

	/**
     * findDetail
     *
     * @param accSubjectDto accSubjectDto
     * @return com.njwd.entity.platform.vo.AccountSubjectVo
     * @author xyyxhcj@qq.com
     * @date 2019/11/20 14:18
     **/
    @Override
    public AccountSubjectVo findDetail(AccountSubjectDto<AccountSubjectVo> accSubjectDto) {
        Long id = accSubjectDto.getId();
        FastUtils.checkParams(id);
        AccountSubjectVo accSubjectVo = accountSubjectMapper.findDetail(id);
        FastUtils.checkNull(accSubjectVo);
        accSubjectVo.setCurrencyVoList(currencyMapper.findAccSubjectCurrencyList(id));
	    accSubjectVo.setAccSubjectAuxiliaryList(accSubjectAuxiliaryMapper.findList(id));
        return accSubjectVo;
    }

    /**
     * release
     *
     * @param accSubjectDto accSubjectDto
     * @return com.njwd.support.BatchResult
     * @author xyyxhcj@qq.com
     * @date 2019/11/20 15:18
     **/
    @Override
    public BatchResult release(AccountSubjectDto<AccountSubjectVo> accSubjectDto) {
        BatchOper batchOper = new BatchOper(accSubjectDto).invoke();
        BaseBatchFail<AccountSubjectVo, AccountSubjectDto> baseFail = batchOper.getBaseFail();
        LinkedHashSet<Long> checkIds = batchOper.getCheckIds();
        Map<Long, AccountSubjectVo> existDataMap = batchOper.getExistDataMap();
        AccountSubjectDto tempDto = new AccountSubjectDto();
        tempDto.setIsReleased(Constant.Is.NO);
        List<AccountSubjectVo> unReleaseList = accountSubjectMapper.findList(tempDto);
        Map<Long, List<AccountSubjectVo>> unReleaseAccSubjectDict = unReleaseList.stream().collect(Collectors.groupingBy(AccountSubject::getSubjectId));
        Set<Long> needReleaseIds = new LinkedHashSet<>();
        LinkedList<ReferenceDescription> successDetailsList = new LinkedList<>();
        LinkedHashSet<ReferenceDescription> relationFailList = new LinkedHashSet<>();
        baseFail.checkFails.add(new BaseBatchFail.BaseEachFail<AccountSubjectVo, AccountSubjectDto>(ResultCode.ACC_SUBJECT_NO_APPROVED.message) {
            @Override
            public boolean isFail(AccountSubjectVo t, AccountSubjectDto e) {
                boolean isFail = t == null || Constant.Is.NO.equals(t.getIsApproved());
                if (!isFail && Constant.Is.NO.equals(t.getIsReleased())) {
                    List<AccountSubjectVo> accSubjects = unReleaseAccSubjectDict.getOrDefault(t.getSubjectId(), Collections.emptyList());
                    List<Long> relationIds = new LinkedList<>();
                    for (AccountSubjectVo accSubject : accSubjects) {
                        if (accSubject.getId().equals(t.getId())) {
	                        relationIds.add(accSubject.getId());
                            continue;
                        }
                        if (accSubject.getCode().startsWith(t.getCode()) || t.getCode().startsWith(accSubject.getCode())) {
                            if (Constant.Is.NO.equals(accSubject.getIsApproved())) {
                                isFail = true;
                            }
                            relationIds.add(accSubject.getId());
                        }
                    }
                    if (!isFail) {
                        needReleaseIds.addAll(relationIds);
                    } else {
                        ReferenceDescription desc;
                        for (Long relationId : relationIds) {
                            desc = new ReferenceDescription();
                            desc.setBusinessId(relationId);
                            desc.setReferenceDescription(ResultCode.ACC_SUBJECT_NO_APPROVED.message);
                            relationFailList.add(desc);
                        }
                    }
                }
                return isFail;
            }
        });
        baseFail.checkFails.add(new BaseBatchFail.BaseEachFail<AccountSubjectVo, AccountSubjectDto>(ResultCode.RELEASE_CHECK_RELEASED.message) {
            @Override
            public boolean isFail(AccountSubjectVo t, AccountSubjectDto e) {
                return Constant.Is.YES.equals(t.getIsReleased());
            }
        });
        addCheckVersion(baseFail);
        List<ReferenceDescription> failList = new LinkedList<>();
        FastUtils.filterIds(accSubjectDto.getBatchEditAccSubjects(), failList, checkIds, existDataMap, baseFail);
        relationFailList.addAll(failList);
        // update
	    if (!checkIds.isEmpty() && !needReleaseIds.isEmpty()) {
            basePlatformMapper.batchProcess(needReleaseIds, PlatformConstant.OperateType.RELEASED, UserUtil.getUserVo(), Constant.TableName.ACCOUNT_SUBJECT);
            messageService.sendMessage(PlatformConstant.MessageType.DATA_UPDATE, accSubjectDto.getMessageDto());
            Map<Long, AccountSubjectVo> accSubjectDict = unReleaseList.stream().collect(Collectors.toMap(BaseModel::getId, v -> v));
            ReferenceDescription<AccountSubjectVo> description;
            for (Long needReleaseId : needReleaseIds) {
                AccountSubjectVo accountSubjectVo = accSubjectDict.get(needReleaseId);
                description = new ReferenceDescription<>();
                description.setBusinessId(needReleaseId);
                description.setInfo(accountSubjectVo);
                successDetailsList.add(description);
            }
        }
        BatchResult batchResult = new BatchResult();
        batchResult.setFailList(new ArrayList<>(relationFailList));
        batchResult.setSuccessList(new ArrayList<>(checkIds));
        batchResult.setSuccessDetailsList(successDetailsList);
        if (!relationFailList.isEmpty()) {
            Map<Long, AccountSubject> failDict = list(new LambdaQueryWrapper<AccountSubject>()
                    .in(BaseModel::getId, relationFailList.stream().map(ReferenceDescription::getBusinessId).collect(Collectors.toList())))
                    .stream().collect(Collectors.toMap(BaseModel::getId, v -> v));
            batchResult.setFailDict(failDict);
        }
        return batchResult;
    }

    /**
     * introduction
     *
     * @param accSubjectDto accSubjectDto
     * @return com.njwd.support.BatchResult
     * @author xyyxhcj@qq.com
     * @date 2019/11/21 10:38
     **/
    @Override
    public BatchResult introduction(AccountSubjectDto<AccountSubjectVo> accSubjectDto) {
        Map<Long, AccountSubject> baseAccSubjectIdIntroducedMap = list(new LambdaQueryWrapper<AccountSubject>()
                .select(AccountSubject::getBaseAccSubjectId, BaseModel::getId)
                .eq(AccountSubject::getSubjectId, accSubjectDto.getNeedIntroductionId())
                .in(AccountSubject::getBaseAccSubjectId, accSubjectDto.getBatchIds()))
                .stream().collect(Collectors.toMap(AccountSubject::getBaseAccSubjectId, v -> v));
        BaseBatchFail<AccountSubjectVo, AccountSubjectDto> baseFail = getAccSubjectBatchFail();
        LinkedHashSet<Long> checkIds = new LinkedHashSet<>(accSubjectDto.getBatchIds());
        Map<Long, AccountSubjectVo> existDataMap = accountSubjectMapper.findByIdIn(checkIds).stream().collect(Collectors.toMap(BaseModel::getId, v -> v));
        baseFail.checkFails.add(new BaseBatchFail.BaseEachFail<AccountSubjectVo, AccountSubjectDto>(ResultCode.ACC_SUBJECT_UNRELEASED.message) {
            @Override
            public boolean isFail(AccountSubjectVo t, AccountSubjectDto e) {
                return Constant.Is.NO.equals(t.getIsReleased());
            }
        });
        baseFail.checkFails.add(new BaseBatchFail.BaseEachFail<AccountSubjectVo, AccountSubjectDto>(ResultCode.ACC_SUBJECT_EXIST.message) {
            @Override
            public boolean isFail(AccountSubjectVo t, AccountSubjectDto e) {
                return baseAccSubjectIdIntroducedMap.containsKey(e.getId());
            }
        });
        List<ReferenceDescription> failList = new LinkedList<>();
        FastUtils.filterIds(accSubjectDto.getBatchIds().stream().map(id -> {
            AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
            accountSubjectDto.setId(id);
            return accountSubjectDto;
        }).collect(Collectors.toList()), failList, checkIds, existDataMap, baseFail);
        // insert
        if (!checkIds.isEmpty()) {
            List<AccountSubject> sourceList = list(new LambdaQueryWrapper<AccountSubject>()
                    .in(BaseModel::getId, checkIds));
            Map<Long, List<AccountSubjectAuxiliary>> accSubjectAuxiliaryDict = accountSubjectAuxiliaryService.list(new LambdaQueryWrapper<AccountSubjectAuxiliary>().in(AccountSubjectAuxiliary::getAccountSubjectId, checkIds)).stream().collect(Collectors.groupingBy(AccountSubjectAuxiliary::getAccountSubjectId));
            Map<Long, AccountSubject> sourceSubjectDict = new LinkedHashMap<>();
            List<Long> sourceParentIds = new LinkedList<>();
            SysUserVo operator = UserUtil.getUserVo();
            Date now = new Date();
            Map<Byte, List<AccountSubject>> levelGroupAccSubjects = new HashMap<>();
            for (AccountSubject accSubject : sourceList) {
                if (accSubject.getUpId() != null) {
                    sourceParentIds.add(accSubject.getUpId());
                }
                sourceSubjectDict.put(accSubject.getId(), accSubject);
                accSubject.setBaseAccSubjectId(accSubject.getId());
                accSubject.setId(null);
                accSubject.setSubjectId(accSubjectDto.getNeedIntroductionId());
                accSubject.setIsReleased(Constant.Is.NO);
                accSubject.setUpdatorId(operator.getUserId());
                accSubject.setUpdatorName(operator.getName());
                accSubject.setUpdateTime(now);
                accSubject.setVersion(null);
                levelGroupAccSubjects.computeIfAbsent(accSubject.getLevel(), k -> new LinkedList<>()).add(accSubject);
            }
            Map<Long, AccountSubject> introducedParentIdDict;
            if (!sourceParentIds.isEmpty()) {
                introducedParentIdDict = list(new LambdaQueryWrapper<AccountSubject>()
                        .in(AccountSubject::getBaseAccSubjectId, sourceParentIds)
                        .eq(BaseModel::getIsDel, Constant.Is.NO)
                        .eq(AccountSubject::getSubjectId, accSubjectDto.getNeedIntroductionId())).stream().collect(Collectors.toMap(BaseModel::getId, v -> v));
            } else {
                introducedParentIdDict = Collections.emptyMap();
            }
            List<Map.Entry<Byte, List<AccountSubject>>> sortAccSubjects = levelGroupAccSubjects.entrySet()
                    .parallelStream().sorted((e1, e2) -> Byte.compare(e2.getKey(), e1.getKey())).collect(Collectors.toList());
            for (Map.Entry<Byte, List<AccountSubject>> entry : sortAccSubjects) {
                List<AccountSubject> accSubjects = entry.getValue();
                if (accSubjects.isEmpty()) {
                    continue;
                }
                for (AccountSubject accSubject : accSubjects) {
                    Long upId = accSubject.getUpId();
                    if (upId == null) {
                        continue;
                    }
                    AccountSubject upAccountSubject = sourceSubjectDict.getOrDefault(upId, introducedParentIdDict.get(upId));
                    accSubject.setUpId(upAccountSubject.getId());
                }
                saveBatch(accSubjects);
            }
            for (Map.Entry<Byte, List<AccountSubject>> entry : sortAccSubjects) {
                List<AccountSubject> accSubjects = entry.getValue();
                if (accSubjects.isEmpty()) {
                    continue;
                }
                for (AccountSubject accSubject : accSubjects) {
                    List<AccountSubjectAuxiliary> auxiliaryList = accSubjectAuxiliaryDict.get(accSubject.getBaseAccSubjectId());
                    if (auxiliaryList == null || auxiliaryList.isEmpty()) {
                        continue;
                    }
                    for (AccountSubjectAuxiliary accSubjectAuxiliary : auxiliaryList) {
                        Long accSubjectId = accSubject.getId();
                        accSubjectAuxiliary.setAccountId(accSubjectId);
                        accSubjectAuxiliary.setAccountSubjectId(accSubjectId);
                        accSubjectAuxiliary.setCreateTime(null);
                    }
                    accountSubjectAuxiliaryService.saveBatch(auxiliaryList);
                }
            }
        }
        BatchResult batchResult = new BatchResult();
        batchResult.setFailList(failList);
        batchResult.setSuccessList(new ArrayList<>(checkIds));
        return batchResult;
    }

    /**
     * export
     *
     * @param accountSubjectDto accountSubjectDto
     * @param response          response
     * @author xyyxhcj@qq.com
     * @date 2019/11/22 17:54
     **/
    @Override
    public void exportExcel(AccountSubjectDto<AccountSubjectVo> accountSubjectDto, HttpServletResponse response) {
        Page<AccountSubjectVo> page = accountSubjectDto.getPage();
        fileService.resetPage(page);
        List<AccountSubjectVo> data = accountSubjectMapper.findPage(accountSubjectDto, page);
        fileService.exportExcel(response, data,
                new ExcelColumn("code", "科目编码"),
                new ExcelColumn("name", "科目名称"),
                new ExcelColumn("subjectCategoryName", "科目类别"),
                new ExcelColumn("directionStr", "余额方向"),
                new ExcelColumn("accountCategoryStr", "科目属性"),
                new ExcelColumn("cashInflowName", "现金流入"),
                new ExcelColumn("cashOutflowName", "现金流出"),
                new ExcelColumn("subjectName", "科目表"),
                new ExcelColumn("isApprovedStr", "审核状态"),
                new ExcelColumn("isReleasedStr", "发布状态")
        );
    }

    /**
     * 获取accSubject批量校验对象
     *
     * @return com.njwd.entity.base.BaseBatchFail<com.njwd.entity.platform.Subject, com.njwd.entity.platform.dto.SubjectDto>
     * @author xyyxhcj@qq.com
     * @date 2019/11/18 11:12
     **/
    private BaseBatchFail<AccountSubjectVo, AccountSubjectDto> getAccSubjectBatchFail() {
        return new BaseBatchFail<AccountSubjectVo, AccountSubjectDto>() {
            @Override
            public Long getId(AccountSubjectDto accSubject) {
                return accSubject.getId();
            }
        };
    }

    private void addCheckDeletedAndVersion(BaseBatchFail<AccountSubjectVo, AccountSubjectDto> baseFail) {
        baseFail.checkFails.add(new BaseBatchFail.BaseEachFail<AccountSubjectVo, AccountSubjectDto>(ResultCode.IS_DEL.message) {
            @Override
            public boolean isFail(AccountSubjectVo t, AccountSubjectDto e) {
                return t == null || Constant.Is.YES.equals(t.getIsDel());
            }
        });
        addCheckVersion(baseFail);
    }

    private void addCheckVersion(BaseBatchFail<AccountSubjectVo, AccountSubjectDto> baseFail) {
        baseFail.checkFails.add(new BaseBatchFail.BaseEachFail<AccountSubjectVo, AccountSubjectDto>(ResultCode.VERSION_ERROR.message) {
            @Override
            public boolean isFail(AccountSubjectVo t, AccountSubjectDto e) {
                return !t.getVersion().equals(e.getVersion());
            }
        });
    }

    private AccountSubject checkUpAccSubjectForNotBase(Long upId) {
        FastUtils.checkParams(upId);
        AccountSubject upAccSubject = getById(upId);
        if (upAccSubject.getBaseAccSubjectId() == null) {
            // 取自当前会计科目表中是否基准表科目为否且已审核状态未发布的会计科目
            if (Constant.Is.NO.equals(upAccSubject.getIsApproved()) || Constant.Is.YES.equals(upAccSubject.getIsReleased())) {
                throw new ServiceException(ResultCode.ACC_SUBJECT_UP_ERROR);
            }
        } else {
            // 取基准表会计科目中的末级的会计科目
            AccountSubject baseAccSubject = getById(upAccSubject.getBaseAccSubjectId());
            FastUtils.checkNull(baseAccSubject);
            if (Constant.Is.NO.equals(baseAccSubject.getIsFinal())) {
                throw new ServiceException(ResultCode.BASE_ACC_SUBJECT_UP_ERROR);
            }
        }
        return upAccSubject;
    }

    private void fillCategoryData(AccountSubjectDto accSubjectDto, AccountSubject upAccSubject) {
        if (upAccSubject != null) {
            if (!upAccSubject.getVersion().equals(accSubjectDto.getUpVersion())) {
                throw new ServiceException(ResultCode.PARENT_VERSION_ERROR);
            }
            accSubjectDto.setSubjectCategory(upAccSubject.getSubjectCategory());
            accSubjectDto.setSubjectCategoryName(upAccSubject.getSubjectCategoryName());
            accSubjectDto.setFullName(upAccSubject.getFullName() + Constant.Character.UNDER_LINE + accSubjectDto.getName());
        } else {
            SubjectCategory subjectCategory = subjectCategoryMapper.selectById(accSubjectDto.getSubjectCategory());
            FastUtils.checkNull(subjectCategory);
            if (Constant.Is.NO.equals(subjectCategory.getIsApproved())) {
                throw new ServiceException(ResultCode.SUBJECT_CATEGORY_NOT_APPROVED);
            }
            accSubjectDto.setSubjectCategoryName(subjectCategory.getName());
        }
    }

    private void checkAuxiliaryList(AccountSubjectDto accSubjectDto) {
        if (accSubjectDto.getAccountSubjectAuxiliaryList().size() > PlatformConstant.ParamRule.MAX_AUXILIARY_USED) {
            throw new ServiceException(ResultCode.AUXILIARY_SIZE_ERROR);
        }
        List<Long> auxiliaryIds = accSubjectDto.getAccountSubjectAuxiliaryList().stream().map(AccountSubjectAuxiliary::getAuxiliaryId).collect(Collectors.toList());
        Map<Long, AuxiliaryItem> auxiliaryDict = auxiliaryItemMapper.selectList(new LambdaQueryWrapper<AuxiliaryItem>().in(AuxiliaryItem::getId, auxiliaryIds)).stream().collect(Collectors.toMap(AuxiliaryItem::getId, v -> v));
        boolean[] useCustomerOrSupplier = new boolean[2];
        for (AccountSubjectAuxiliary accountSubjectAuxiliary : accSubjectDto.getAccountSubjectAuxiliaryList()) {
            AuxiliaryItem auxiliary = auxiliaryDict.getOrDefault(accountSubjectAuxiliary.getAuxiliaryId(), new AuxiliaryItem());
            if (!Constant.Is.YES.equals(auxiliary.getIsReleased()) || Constant.Is.YES.equals(auxiliary.getIsDel())) {
                throw new ServiceException(ResultCode.AUXILIARY_DISABLE, accountSubjectAuxiliary.getAuxiliaryId());
            }
            if (Constant.TableName.CUSTOMER.equals(auxiliary.getSourceTable())) {
                useCustomerOrSupplier[0] = true;
            }else if (Constant.TableName.SUPPLIER.equals(auxiliary.getSourceTable())) {
                useCustomerOrSupplier[1] = true;
            }
            if (useCustomerOrSupplier[0] && useCustomerOrSupplier[1]) {
                throw new ServiceException(ResultCode.AUXILIARY_MUTUAL_EXCLUSION);
            }
            accountSubjectAuxiliary.setAuxiliaryName(auxiliary.getName());
            accountSubjectAuxiliary.setAuxiliarySource(auxiliary.getSourceName());
            accountSubjectAuxiliary.setAuxiliarySourceTable(auxiliary.getSourceTable());
        }
    }

    private void checkName(AccountSubjectDto accSubjectDto, Long subjectId, Long upId) {
        LambdaQueryWrapper<AccountSubject> checkNameWrapper = new LambdaQueryWrapper<AccountSubject>()
                .eq(AccountSubject::getSubjectId, subjectId)
                .eq(BaseModel::getIsDel, Constant.Is.NO)
                .eq(AccountSubject::getUpId, upId)
                .eq(AccountSubject::getName, accSubjectDto.getName());
        if (accSubjectDto.getId() != null) {
            checkNameWrapper.ne(BaseModel::getId, accSubjectDto.getId());
        }
        List<AccountSubject> existDuplicateName = list(checkNameWrapper);
        if (!existDuplicateName.isEmpty()) {
            throw new ServiceException(ResultCode.NAME_EXIST);
        }
    }

    private void checkCode(AccountSubjectDto accSubjectDto, Long subjectId, Subject subject, AccountSubject upAccSubject) {
        LinkedList<Integer> queue = new LinkedList<>();
        for (int i = 0; i < subject.getMaxLevel().length(); i++) {
            char c = subject.getMaxLevel().charAt(i);
            if (Character.isDigit(c)) {
                queue.add(c - Constant.Character.ZERO_CHAR);
            }
        }
        if (upAccSubject != null) {
            // 第二级乃至最后一级的编码规则为：长度为2位流水，从01到99自动增长，可改只能为01-99数值
            StringBuilder upCode = new StringBuilder(upAccSubject.getCode());
            while (!queue.isEmpty() && upCode.length() > 0) {
	            upCode.delete(0, queue.removeFirst());
            }
            if (queue.isEmpty()) {
                throw new ServiceException(ResultCode.ACC_SUBJECT_CODE_ERROR, accSubjectDto.getCode());
            }
            String lastCode = accSubjectDto.getCode().substring(upAccSubject.getCode().length());
            Integer lastCodeLength = queue.removeFirst();
            if (!Pattern.matches(String.format(Constant.AccountSubjectData.CHECK_ALL_NUM_REGEX, lastCodeLength), lastCode) || Pattern.matches(String.format(Constant.AccountSubjectData.CHECK_ALL_ZERO_REGEX, lastCodeLength), lastCode)) {
                throw new ServiceException(ResultCode.CODING_FORMAT_NO_RIGHT);
            }
        } else {
            // 校验顶级科目code 第一级编码为4位，且首位不能为0，且不能使用1999
            String lastCode = accSubjectDto.getCode();
            Integer lastCodeLength = queue.removeFirst();
            if (!Pattern.matches(String.format(Constant.AccountSubjectData.CHECK_ALL_NUM_REGEX, lastCodeLength), lastCode) || Pattern.matches(String.format(Constant.AccountSubjectData.CHECK_ALL_ZERO_REGEX, lastCodeLength), lastCode) || lastCode.startsWith(Constant.Character.STRING_ZERO)) {
                throw new ServiceException(ResultCode.CODING_FORMAT_NO_RIGHT);
            }
        }
        LambdaQueryWrapper<AccountSubject> checkCodeWrapper = new LambdaQueryWrapper<AccountSubject>()
                .eq(AccountSubject::getSubjectId, subjectId)
                .eq(BaseModel::getIsDel, Constant.Is.NO)
                .eq(AccountSubject::getCode, accSubjectDto.getCode());
        if (accSubjectDto.getId() != null) {
            checkCodeWrapper.ne(BaseModel::getId, accSubjectDto.getId());
        }
        List<AccountSubject> existDuplicateCode = list(checkCodeWrapper);
        if (!existDuplicateCode.isEmpty()) {
            throw new ServiceException(ResultCode.CODE_EXIST);
        }
        if (Arrays.binarySearch(Constant.AccountSubjectData.NOT_ALLOW_CODES, accSubjectDto.getCode()) > -1) {
            throw new ServiceException(ResultCode.ACC_SUBJECT_CODE_NOT_ALLOW);
        }
    }

	private class BatchOper {
		private AccountSubjectDto<AccountSubjectVo> accSubjectDto;
		private BaseBatchFail<AccountSubjectVo, AccountSubjectDto> baseFail;
		private LinkedHashSet<Long> checkIds;
		private Map<Long, AccountSubjectVo> existDataMap;

		public BatchOper(AccountSubjectDto<AccountSubjectVo> accSubjectDto) {
			this.accSubjectDto = accSubjectDto;
		}

		public BaseBatchFail<AccountSubjectVo, AccountSubjectDto> getBaseFail() {
			return baseFail;
		}

		public LinkedHashSet<Long> getCheckIds() {
			return checkIds;
		}

		public Map<Long, AccountSubjectVo> getExistDataMap() {
			return existDataMap;
		}

		public BatchOper invoke() {
			FastUtils.checkParams(accSubjectDto.getBatchEditAccSubjects());
			baseFail = getAccSubjectBatchFail();
			checkIds = baseFail.getCheckIds(accSubjectDto.getBatchEditAccSubjects());
			existDataMap = accountSubjectMapper.findByIdIn(checkIds).stream().collect(Collectors.toMap(BaseModel::getId, v -> v));
			return this;
		}
	}
}
