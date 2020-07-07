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
import com.njwd.entity.platform.dto.SubjectDto;
import com.njwd.entity.platform.vo.SubjectVo;
import com.njwd.entity.platform.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.platform.mapper.*;
import com.njwd.platform.service.MessageService;
import com.njwd.platform.service.SequenceService;
import com.njwd.platform.service.SubjectService;
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
import java.util.stream.Collectors;

/**
 * @Author lj
 * @Description 科目
 * @Date:14:18 2019/6/13
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class SubjectServiceImpl extends ServiceImpl<SubjectMapper, Subject> implements SubjectService {

    @Autowired
    private SubjectMapper subjectMapper;
    @Resource
    private SequenceService sequenceService;
    @Resource
    private AccountingStandardMapper accStandardMapper;
    @Resource
    private AccountElementMapper accElementMapper;
    @Resource
    private SubjectAuxiliaryMapper subjectAuxiliaryMapper;
    @Resource
    private AuxiliaryItemMapper auxiliaryItemMapper;
	@Resource
	private BasePlatformMapper basePlatformMapper;
	@Resource
    private ReferenceRelationService referenceRelationService;
    @Resource
    private FileService fileService;
    @Resource
    private MessageService messageService;

    /**
     * @return java.util.List<com.njwd.platform.entity.vo.SubjectVo>
     * @Description 查询科目列表
     * @Author liuxiang
     * @Date:16:49 2019/7/2
     * @Param [subjectDto]
     **/
    @Override
    @Cacheable(value = "subjectList", key = "#subjectDto.accountTypeId+'-'+#subjectDto.accStandardId+'-'+#subjectDto.isBase+'-'+#subjectDto.parentId")
    public List<SubjectVo> findSubjectList(SubjectDto subjectDto) {
        return subjectMapper.findSubjectList(subjectDto);
    }

    /**
     * 查询科目列表分页
     *
     * @param subjectDto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.SubjectVo>
     * @Author lj
     * @Date:14:18 2019/10/29
     **/
    @Override
    @Cacheable(value = "subjectListPage", key = "#subjectDto.name+'-'+#subjectDto.page.current+'-'+#subjectDto.page.size")
    public Page<SubjectVo> findSubjectListPage(SubjectDto subjectDto) {
        Page<SubjectVo> page = subjectDto.getPage();
        page = subjectMapper.findSubjectListPage(page, subjectDto);
        return page;
    }

    /**
     * 分页
     *
     * @param subjectDto subjectDto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.SubjectVo>
     * @author xyyxhcj@qq.com
     * @date 2019/11/12 15:45
     **/
    @Override
    public Page<SubjectVo> findPage(SubjectDto subjectDto) {
        Page<SubjectVo> page = subjectDto.getPage();
        return page.setRecords(subjectMapper.findPage(subjectDto, page));
    }

    /**
     * add
     *
     * @param subjectDto subjectDto
     * @return java.lang.Long
     * @author xyyxhcj@qq.com
     * @date 2019/11/14 11:04
     **/
    @Override
    public Long add(SubjectDto subjectDto) {
        Long accStandardId = subjectDto.getAccStandardId();
        FastUtils.checkParams(subjectDto.getName(), accStandardId, subjectDto.getElementId(), subjectDto.getMaximumLevel(), subjectDto.getIsBase());
        AccountingStandard accountingStandard = checkValid(subjectDto, accStandardId);
        boolean isBase = Constant.Is.YES.equals(subjectDto.getIsBase());
        Subject subject = getSubjectAndCheckBase(subjectDto, accStandardId, accountingStandard, isBase);
        subject.setCode(sequenceService.getCode(PlatformConstant.PlatformCodeRule.SUBJECT_TABLE, PlatformConstant.PlatformCodeRule.SUBJECT_TABLE_SERIAL_LENGTH, accountingStandard.getCode().substring(accountingStandard.getCode().length() - PlatformConstant.PlatformCodeRule.SUBJECT_TABLE_SERIAL_LENGTH)));
        SysUserVo operator = UserUtil.getUserVo();
        subject.setCreatorId(operator.getUserId());
        subject.setCreatorName(operator.getName());
        subject.setCreateTime(new Date());
        save(subject);
        Long id = subject.getId();
        saveSubjectAuxiliaries(subjectDto, id);
        return id;
    }

    private Subject getSubjectAndCheckBase(SubjectDto subjectDto, Long accStandardId, AccountingStandard accountingStandard, boolean isBase) {
        if (isBase) {
            // exist other baseTable?
            LambdaQueryWrapper<Subject> wrapper = new LambdaQueryWrapper<Subject>()
                    .eq(Subject::getAccStandardId, accStandardId)
                    .eq(Subject::getIsBase, Constant.Is.YES)
                    .eq(BaseModel::getIsDel, Constant.Is.NO);
            if (subjectDto.getId() != null) {
                wrapper.ne(BaseModel::getId, subjectDto.getId());
            }
            Subject subject = getOne(wrapper);
            if (subject != null) {
                throw new ServiceException(ResultCode.STANDARD_BASE_TABLE_EXIST);
            }
            subjectDto.setSubjectId(null);
            subjectDto.setSubjectName(null);
        } else {
            // confirm baseTable released
            FastUtils.checkParams(subjectDto.getSubjectId());
            Subject subject = getById(subjectDto.getSubjectId());
            if (subject == null || Constant.Is.YES.equals(subject.getIsDel()) || Constant.Is.NO.equals(subject.getIsReleased()) || Constant.Is.NO.equals(subject.getIsBase())) {
                throw new ServiceException(ResultCode.BASE_TABLE_NOT_RELEASED);
            }
            if (subject.getMaximumLevel() > subjectDto.getMaximumLevel()) {
                throw new ServiceException(ResultCode.MAXIMUM_LEVEL_LESS_BASE);
            }
        }
        return getSubject(subjectDto, accountingStandard);
    }

    /**
     * update
     *
     * @param subjectDto subjectDto
     * @return java.lang.Long
     * @author xyyxhcj@qq.com
     * @date 2019/11/14 16:19
     **/
    @Override
    public Long update(SubjectDto subjectDto) {
        Long id = subjectDto.getId();
        FastUtils.checkParams(id);
        Subject existSubject = getById(id);
        FastUtils.checkNull(existSubject);
        if (!existSubject.getVersion().equals(subjectDto.getVersion())) {
            throw new ServiceException(ResultCode.VERSION_ERROR);
        }
        if (Constant.Is.YES.equals(existSubject.getIsApproved())) {
            throw new ServiceException(ResultCode.IS_APPROVED);
        }
        Long accStandardId = subjectDto.getAccStandardId();
        FastUtils.checkParams(subjectDto.getName(), accStandardId, subjectDto.getElementId(), subjectDto.getMaximumLevel());
        AccountingStandard accountingStandard = checkValid(subjectDto, accStandardId);
        boolean isBase = Constant.Is.YES.equals(subjectDto.getIsBase());
        Subject subject = getSubjectAndCheckBase(subjectDto, accStandardId, accountingStandard, isBase);
        SysUserVo operator = UserUtil.getUserVo();
        subject.setUpdatorId(operator.getUserId());
        subject.setUpdatorName(operator.getName());
        subject.setUpdateTime(new Date());
        subject.setCode(null);
        updateById(subject);
        subjectAuxiliaryMapper.delete(new LambdaQueryWrapper<SubjectAuxiliary>().eq(SubjectAuxiliary::getSubjectId, id));
        saveSubjectAuxiliaries(subjectDto, id);
        return id;
    }

    /**
     * batch delete
     *
     * @param subjectDto subjectDto
     * @return com.njwd.support.BatchResult
     * @author xyyxhcj@qq.com
     * @date 2019/11/14 17:32
     **/
    @Override
    public BatchResult delete(SubjectDto subjectDto) {
        FastUtils.checkParams(subjectDto.getBatchEditSubjects());
        List<ReferenceDescription> failList = new LinkedList<>();
        BaseBatchFail<Subject, SubjectDto> baseFail = getSubjectBatchFail();
        LinkedHashSet<Long> checkIds = baseFail.getCheckIds(subjectDto.getBatchEditSubjects());
        Map<Long, Subject> existDataMap = list(new LambdaQueryWrapper<Subject>().in(BaseModel::getId, checkIds)).stream().collect(Collectors.toMap(BaseModel::getId, v -> v));
        addCheckDeletedAndVersion(baseFail);
        baseFail.checkFails.add(new BaseBatchFail.BaseEachFail<Subject, SubjectDto>(ResultCode.IS_APPROVED.message) {
            @Override
            public boolean isFail(Subject t, SubjectDto e) {
                return Constant.Is.YES.equals(t.getIsApproved());
            }
        });
        FastUtils.filterIds(subjectDto.getBatchEditSubjects(), failList, checkIds, existDataMap, baseFail);
        // check reference
        List<Long> successList = new ArrayList<>(checkIds);
        ReferenceContext referenceContext = referenceRelationService.isReference(PlatformConstant.Reference.PLAT_SUBJECT_TABLE, successList);
        failList.addAll(referenceContext.getReferences());
        successList.removeAll(referenceContext.getReferences().stream().map(ReferenceDescription::getBusinessId).collect(Collectors.toList()));
        // update
        if (!successList.isEmpty()) {
            basePlatformMapper.batchProcess(successList, PlatformConstant.OperateType.DELETE, UserUtil.getUserVo(), Constant.TableName.SUBJECT);
        }
	    BatchResult batchResult = new BatchResult();
	    batchResult.setFailList(failList);
	    batchResult.setSuccessList(new ArrayList<>(successList));
        return batchResult;
    }

    /**
     * 批量审核
     *
     * @param subjectDto subjectDto
     * @return com.njwd.support.BatchResult
     * @author xyyxhcj@qq.com
     * @date 2019/11/18 10:58
     **/
    @Override
    public BatchResult approve(SubjectDto subjectDto) {
        FastUtils.checkParams(subjectDto.getBatchEditSubjects());
        List<ReferenceDescription> failList = new LinkedList<>();
        BaseBatchFail<Subject, SubjectDto> baseFail = getSubjectBatchFail();
        LinkedHashSet<Long> checkIds = baseFail.getCheckIds(subjectDto.getBatchEditSubjects());
        Map<Long, Subject> existDataMap = list(new LambdaQueryWrapper<Subject>().in(BaseModel::getId, checkIds)).stream().collect(Collectors.toMap(BaseModel::getId, v -> v));
        addCheckVersion(baseFail);
        baseFail.checkFails.add(new BaseBatchFail.BaseEachFail<Subject, SubjectDto>(ResultCode.APPROVE_CHECK_APPROVED.message) {
            @Override
            public boolean isFail(Subject t, SubjectDto e) {
                return Constant.Is.YES.equals(t.getIsApproved());
            }
        });
        FastUtils.filterIds(subjectDto.getBatchEditSubjects(), failList, checkIds, existDataMap, baseFail);
        // update
        if (!checkIds.isEmpty()) {
            basePlatformMapper.batchProcess(checkIds, PlatformConstant.OperateType.APPROVED, UserUtil.getUserVo(), Constant.TableName.SUBJECT);
        }
        BatchResult batchResult = new BatchResult();
        batchResult.setFailList(failList);
        batchResult.setSuccessList(new ArrayList<>(checkIds));
        return batchResult;
    }

    /**
     * 批量反审
     *
     * @param subjectDto subjectDto
     * @return com.njwd.support.BatchResult
     * @author xyyxhcj@qq.com
     * @date 2019/11/18 11:21
     **/
    @Override
    public BatchResult reversalApprove(SubjectDto subjectDto) {
        FastUtils.checkParams(subjectDto.getBatchEditSubjects());
        List<ReferenceDescription> failList = new LinkedList<>();
        BaseBatchFail<Subject, SubjectDto> baseFail = getSubjectBatchFail();
        LinkedHashSet<Long> checkIds = baseFail.getCheckIds(subjectDto.getBatchEditSubjects());
        Map<Long, Subject> existDataMap = list(new LambdaQueryWrapper<Subject>().in(BaseModel::getId, checkIds)).stream().collect(Collectors.toMap(BaseModel::getId, v -> v));
        baseFail.checkFails.add(new BaseBatchFail.BaseEachFail<Subject, SubjectDto>(ResultCode.DISAPPROVE_CHECK_RELEASED.message) {
            @Override
            public boolean isFail(Subject t, SubjectDto e) {
                return Constant.Is.YES.equals(t.getIsReleased());
            }
        });
        baseFail.checkFails.add(new BaseBatchFail.BaseEachFail<Subject, SubjectDto>(ResultCode.DISAPPROVE_CHECK_DISAPPROVED.message) {
            @Override
            public boolean isFail(Subject t, SubjectDto e) {
                return Constant.Is.NO.equals(t.getIsApproved());
            }
        });
        addCheckVersion(baseFail);
        FastUtils.filterIds(subjectDto.getBatchEditSubjects(), failList, checkIds, existDataMap, baseFail);
        // update
        if (!checkIds.isEmpty()) {
            basePlatformMapper.batchProcess(checkIds, PlatformConstant.OperateType.DISAPPROVED, UserUtil.getUserVo(), Constant.TableName.SUBJECT);
        }
        BatchResult batchResult = new BatchResult();
        batchResult.setFailList(failList);
        batchResult.setSuccessList(new ArrayList<>(checkIds));
        return batchResult;
    }

    /**
     * 批量发布
     *
     * @param subjectDto subjectDto
     * @return com.njwd.support.BatchResult
     * @author xyyxhcj@qq.com
     * @date 2019/11/18 11:32
     **/
    @Override
    public BatchResult release(SubjectDto subjectDto) {
        FastUtils.checkParams(subjectDto.getBatchEditSubjects());
        List<ReferenceDescription> failList = new LinkedList<>();
        BaseBatchFail<Subject, SubjectDto> baseFail = getSubjectBatchFail();
        LinkedHashSet<Long> checkIds = baseFail.getCheckIds(subjectDto.getBatchEditSubjects());
        Map<Long, Subject> existDataMap = list(new LambdaQueryWrapper<Subject>().in(BaseModel::getId, checkIds)).stream().collect(Collectors.toMap(BaseModel::getId, v -> v));
        baseFail.checkFails.add(new BaseBatchFail.BaseEachFail<Subject, SubjectDto>(ResultCode.RELEASE_CHECK_NO_APPROVED.message) {
            @Override
            public boolean isFail(Subject t, SubjectDto e) {
                return Constant.Is.NO.equals(t.getIsApproved());
            }
        });
        baseFail.checkFails.add(new BaseBatchFail.BaseEachFail<Subject, SubjectDto>(ResultCode.RELEASE_CHECK_RELEASED.message) {
            @Override
            public boolean isFail(Subject t, SubjectDto e) {
                return Constant.Is.YES.equals(t.getIsReleased());
            }
        });
        addCheckVersion(baseFail);
        List<Long> baseSubjectIds = existDataMap.values().stream().filter(subject -> subject.getSubjectId() != null).map(Subject::getSubjectId).collect(Collectors.toList());
        Map<Long, Subject> baseSubjectDict;
        if (baseSubjectIds.isEmpty()) {
            baseSubjectDict = null;
        } else {
            baseSubjectDict = list(new LambdaQueryWrapper<Subject>().in(BaseModel::getId, baseSubjectIds)).stream().collect(Collectors.toMap(Subject::getSubjectId, v -> v));
        }
        baseFail.checkFails.add(new BaseBatchFail.BaseEachFail<Subject, SubjectDto>(ResultCode.BASE_SUBJECT_NOT_RELEASED.message) {
            @Override
            public boolean isFail(Subject t, SubjectDto e) {
                // 检查当前当前已选会计科目表的是否为基准表 ->	不是基准表，则继续检查其所属基准表是否已经发布 -> 未发布，则发布失败
                return Constant.Is.NO.equals(t.getIsBase()) && (baseSubjectDict == null || Constant.Is.NO.equals(baseSubjectDict.getOrDefault(t.getSubjectId(), t).getIsApproved()));
            }
        });
        FastUtils.filterIds(subjectDto.getBatchEditSubjects(), failList, checkIds, existDataMap, baseFail);
        // update
        if (!checkIds.isEmpty()) {
            basePlatformMapper.batchProcess(checkIds, PlatformConstant.OperateType.RELEASED, UserUtil.getUserVo(), Constant.TableName.SUBJECT);
            messageService.sendMessage(PlatformConstant.MessageType.DATA_UPDATE, subjectDto.getMessageDto());
        }
        BatchResult batchResult = new BatchResult();
        batchResult.setFailList(failList);
        batchResult.setSuccessList(new ArrayList<>(checkIds));
        return batchResult;
    }

    /**
     * 查详情
     *
     * @param subjectDto subjectDto
     * @return com.njwd.entity.platform.vo.SubjectVo
     * @author xyyxhcj@qq.com
     * @date 2019/11/18 17:48
     **/
    @Override
    public SubjectVo findDetail(SubjectDto subjectDto) {
        Long subjectId = subjectDto.getId();
        FastUtils.checkParams(subjectId);
        SubjectVo subjectVo= subjectMapper.findDetail(subjectId);
        FastUtils.checkNull(subjectVo);
        subjectVo.setAuxiliaryItemList(auxiliaryItemMapper.findBySubjectId(subjectId));
        return subjectVo;
    }

    /**
     * 导出
     *
     * @param subjectDto subjectDto
     * @param response   response
     * @author xyyxhcj@qq.com
     * @date 2019/11/24 10:02
     **/
    @Override
    public void exportExcel(SubjectDto subjectDto, HttpServletResponse response) {
        Page<SubjectVo> page = subjectDto.getPage();
        fileService.resetPage(page);
        List<SubjectVo> data = subjectMapper.findPage(subjectDto, page);
        fileService.exportExcel(response, data,
                new ExcelColumn("code", "编码"),
                new ExcelColumn("name", "名称"),
                new ExcelColumn("accStandardName", "会计准则"),
                new ExcelColumn("elementName", "会计要素表"),
                new ExcelColumn("isBaseStr", "基准表"),
                new ExcelColumn("subjectName", "所属基准表"),
                new ExcelColumn("isApprovedStr", "审核状态"),
                new ExcelColumn("isReleasedStr", "发布状态")
        );
    }

    private void addCheckDeletedAndVersion(BaseBatchFail<Subject, SubjectDto> baseFail) {
        baseFail.checkFails.add(new BaseBatchFail.BaseEachFail<Subject, SubjectDto>(ResultCode.IS_DEL.message) {
            @Override
            public boolean isFail(Subject t, SubjectDto e) {
                return t == null || Constant.Is.YES.equals(t.getIsDel());
            }
        });
        addCheckVersion(baseFail);
    }

    private void addCheckVersion(BaseBatchFail<Subject, SubjectDto> baseFail) {
        baseFail.checkFails.add(new BaseBatchFail.BaseEachFail<Subject, SubjectDto>(ResultCode.VERSION_ERROR.message) {
            @Override
            public boolean isFail(Subject t, SubjectDto e) {
                return !t.getVersion().equals(e.getVersion());
            }
        });
    }

    /**
     * 获取subject批量校验对象
     *
     * @return com.njwd.entity.base.BaseBatchFail<com.njwd.entity.platform.Subject,com.njwd.entity.platform.dto.SubjectDto>
     * @author xyyxhcj@qq.com
     * @date 2019/11/18 11:12
     **/
    private BaseBatchFail<Subject, SubjectDto> getSubjectBatchFail() {
        return new BaseBatchFail<Subject, SubjectDto>() {
            @Override
            public Long getId(SubjectDto subject) {
                return subject.getId();
            }
        };
    }

    /**
     * 校验数据有效性
     *
     * @param subjectDto    subjectDto
     * @param accStandardId accStandardId
     * @return com.njwd.entity.platform.AccountingStandard
     * @author xyyxhcj@qq.com
     * @date 2019/11/14 16:49
     **/
    private AccountingStandard checkValid(SubjectDto subjectDto, Long accStandardId) {
        AccountingStandard accountingStandard = accStandardMapper.selectById(accStandardId);
        if (accountingStandard == null || Constant.Is.YES.equals(accountingStandard.getIsDel()) || Constant.Is.NO.equals(accountingStandard.getIsApproved())) {
            throw new ServiceException(ResultCode.STANDARD_UNAPPROVED);
        }
        AccountElement accountElement = accElementMapper.selectById(subjectDto.getElementId());
        if (accountElement == null || Constant.Is.NO.equals(accountElement.getIsApproved())) {
            throw new ServiceException(ResultCode.ACC_ELEMENT_UNAPPROVED);
        }
        if (Arrays.binarySearch(PlatformConstant.ParamRule.ALLOW_SUBJECT_LEVEL, subjectDto.getMaximumLevel()) < 0) {
            throw new ServiceException(ResultCode.MAXIMUM_LEVEL_ERROR);
        }
        if (subjectDto.getAuxiliaryIds() != null && subjectDto.getAuxiliaryIds().length > PlatformConstant.ParamRule.MAX_AUXILIARIES) {
            throw new ServiceException(ResultCode.SUBJECT_AUXILIARY_SELECTED_OVERMUCH);
        }
        // 相同会计准则下会计科目表名称不可重复
        LambdaQueryWrapper<Subject> checkNameWrapper = new LambdaQueryWrapper<Subject>()
                .eq(Subject::getAccStandardId, accStandardId)
                .eq(BaseModel::getIsDel, Constant.Is.NO)
                .eq(Subject::getName, subjectDto.getName());
        if (subjectDto.getId() != null) {
            checkNameWrapper.ne(BaseModel::getId, subjectDto.getId());
        }
        List<Subject> existDuplicateName = list(checkNameWrapper);
        if (!existDuplicateName.isEmpty()) {
            throw new ServiceException(ResultCode.NAME_EXIST);
        }
        return accountingStandard;
    }

    /**
     * 获取subject
     *
     * @param subjectDto         subjectDto
     * @param accountingStandard accountingStandard
     * @return com.njwd.entity.platform.Subject
     * @author xyyxhcj@qq.com
     * @date 2019/11/14 16:41
     **/
    private Subject getSubject(SubjectDto subjectDto, AccountingStandard accountingStandard) {
        Subject subject = new Subject();
        FastUtils.copyProperties(subjectDto, subject);
        // generate maxLevel code
        StringBuilder maxLevelSb = new StringBuilder(PlatformConstant.Subject.MAX_LEVEL_PRE);
        for (Byte i = 1; i < subjectDto.getMaximumLevel(); i++) {
            maxLevelSb.append(PlatformConstant.Subject.MAX_LEVEL_CONCAT);
        }
        subject.setMaxLevel(maxLevelSb.toString());
        return subject;
    }

    /**
     * 同步关联表数据
     *
     * @param subjectDto subjectDto
     * @param id         id
     * @author xyyxhcj@qq.com
     * @date 2019/11/14 16:39
     **/
    private void saveSubjectAuxiliaries(SubjectDto subjectDto, Long id) {
        if (subjectDto.getAuxiliaryIds() != null && subjectDto.getAuxiliaryIds().length != 0) {
            List<AuxiliaryItem> auxiliaryItems = auxiliaryItemMapper.selectList(new LambdaQueryWrapper<AuxiliaryItem>()
                    .eq(AuxiliaryItem::getIsReleased, Constant.Is.YES)
                    .eq(AuxiliaryItem::getIsDel, Constant.Is.NO)
                    .in(AuxiliaryItem::getId, (Object[]) subjectDto.getAuxiliaryIds()));
            if (auxiliaryItems.size() != subjectDto.getAuxiliaryIds().length) {
                throw new ServiceException(ResultCode.ALLOW_SELECTED_RELEASED_AUXILIARY);
            }
            SubjectAuxiliary subjectAuxiliary;
            for (Long auxiliaryId : subjectDto.getAuxiliaryIds()) {
                subjectAuxiliary = new SubjectAuxiliary();
                subjectAuxiliary.setSubjectId(id);
                subjectAuxiliary.setAuxiliaryId(auxiliaryId);
                subjectAuxiliaryMapper.insert(subjectAuxiliary);
            }
        }
    }
}
