package com.njwd.basedata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.cloudclient.*;
import com.njwd.basedata.service.AccountSubjectService;
import com.njwd.basedata.service.AccountingItemService;
import com.njwd.basedata.service.SubjectService;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.basedata.dto.SubjectAuxiliaryDto;
import com.njwd.entity.basedata.vo.AccountingItemVo;
import com.njwd.entity.basedata.vo.SubjectAuxiliaryVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.AccountSubjectAuxiliary;
import com.njwd.entity.platform.Subject;
import com.njwd.entity.platform.dto.*;
import com.njwd.entity.platform.vo.*;
import com.njwd.exception.ResultCode;
import com.njwd.logger.SenderService;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 会计科目
 *
 * @author 周鹏
 * @date 2019/6/12
 */
@RestController
@RequestMapping("accountSubject")
public class AccountSubjectController extends BaseController {
    @Autowired
    private SubjectService subjectService;

    @Autowired
    private AccountSubjectService accountSubjectService;

    @Autowired
    private AccountingItemService accountingItemService;

    @Autowired
    private SenderService senderService;

    @Autowired
    private AccountSubjectFeignClient accountSubjectFeignClient;

    @Autowired
    private SubjectFeignClient subjectFeignClient;

    @Autowired
    private AuxiliaryFeignClient auxiliaryFeignClient;

    @Autowired
    private AccountElementItemFeignClient accountElementItemFeignClient;

    @Autowired
    private SubjectCategoryFeignClient subjectCategoryFeignClient;

    @Autowired
    private AccountBookTypeFeignClient accountBookTypeFeignClient;

    @Autowired
    private AccountingStandardFeignClient accountingStandardFeignClient;

    /**
     * 初始化会计科目基准数据.
     *
     * @return Result
     * @author 周鹏
     * @date 2019/7/12
     */
    @PostMapping("addInitInfo")
    public Result<Long> addInitInfo() {
        SysUserVo operator = UserUtils.getUserVo();
        FastUtils.checkParams(operator.getRootEnterpriseId());
        //查询租户下是否存在会计科目信息
        int count = accountSubjectService.findCount(operator.getRootEnterpriseId());
        if (count > 0) {
            return ok();
        }
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        accountSubjectDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        return addRootInfo(accountSubjectDto);
    }

    /**
     * 引入会计科目模板数据.
     *
     * @param accountSubjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/6/12
     */
    @PostMapping("addRoot")
    public Result<Long> addRoot(@RequestBody AccountSubjectDto accountSubjectDto) {
        FastUtils.checkParams(accountSubjectDto.getSubjectId());
        return addRootInfo(accountSubjectDto);
    }

    /**
     * 新增运营平台会计科目信息.
     *
     * @param accountSubjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/7/12
     */
    private Result<Long> addRootInfo(AccountSubjectDto accountSubjectDto) {
        //获取运营平台会计科目模板数据
        Result<List<FindAccountSubjectListVo>> rootInfo = accountSubjectFeignClient.findMoreSubjectTemplateList(accountSubjectDto);
        List<AccountSubjectDto> list = new ArrayList<>();
        List<FindAccountSubjectListVo> templateList = rootInfo.getData();
        if (CollectionUtils.isNotEmpty(templateList)) {
            AccountSubjectDto info;
            for (FindAccountSubjectListVo item : templateList) {
                info = new AccountSubjectDto();
                FastUtils.copyProperties(item, info);
                list.add(info);
            }
        }
        Result<Long> result = addInfo(new AccountSubjectDto(), () -> accountSubjectService.addRoot(list));
        //日志记录id
        List<Long> idList = new ArrayList<>();
        for (AccountSubjectDto info : list) {
            idList.add(info.getId());
        }
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys, LogConstant.menuName.accoutSubject,
                LogConstant.operation.addBatch, LogConstant.operation.addBatch_type, idList.toString()));
        return result;
    }

    /**
     * 新增会计科目相关信息.
     *
     * @param accountSubjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/6/12
     */
    @PostMapping("add")
    public Result<Long> add(@RequestBody AccountSubjectDto accountSubjectDto) {
        SysUserVo operator = UserUtils.getUserVo();
        accountSubjectDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        accountSubjectDto.setCreatorId(operator.getUserId());
        accountSubjectDto.setCreatorName(operator.getName());
        Result<Long> result = addInfo(accountSubjectDto, () -> accountSubjectService.add(accountSubjectDto));
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys, LogConstant.menuName.accoutSubject,
                LogConstant.operation.add, LogConstant.operation.add_type, accountSubjectDto.getId().toString()));
        return result;
    }

    /**
     * 新增/修改会计科目信息
     *
     * @param accountSubjectDto,lockProcess
     * @return Result
     * @author 周鹏
     * @date 2019/6/12
     */
    private Result<Long> addInfo(AccountSubjectDto accountSubjectDto, RedisUtils.LockProcess<Integer> lockProcess) {
        lockProcess.execute();
        return ok(accountSubjectDto.getId());
    }

    /**
     * 引入会计科目.
     *
     * @param accountSubjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/12/4
     */
    @PostMapping("addNewPlatformInfo")
    public Result<Integer> addNewPlatformInfo(AccountSubjectDto accountSubjectDto) {
        SysUserVo operator = UserUtils.getUserVo();
        SubjectDto subjectDto = new SubjectDto();
        subjectDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        subjectDto.setSubjectId(accountSubjectDto.getBaseSubjectId());
        //查询科目表信息
        Subject subjectInfo = subjectService.findSubject(subjectDto);
        accountSubjectDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        accountSubjectDto.setIsInit(Constant.Is.YES);
        accountSubjectDto.setIfFindPlatformId(Constant.Is.YES);
        accountSubjectDto.setSubjectId(subjectInfo.getId());
        //查询当前科目表的会计科目信息id
        List<Long> platformIds = accountSubjectService.findIds(accountSubjectDto);
        //获取运营平台会计科目模板数据
        AccountSubjectDto param = new AccountSubjectDto();
        param.setSubjectId(subjectInfo.getTemplateSubjectId());
        param.setPlatformIds(platformIds);
        Result<List<FindAccountSubjectListVo>> rootInfo = accountSubjectFeignClient.findMoreSubjectTemplateList(param);
        List<AccountSubjectDto> list = new ArrayList<>();
        List<FindAccountSubjectListVo> templateList = rootInfo.getData();
        if (CollectionUtils.isNotEmpty(templateList)) {
            AccountSubjectDto info;
            for (FindAccountSubjectListVo item : templateList) {
                info = new AccountSubjectDto();
                FastUtils.copyProperties(item, info);
                info.setIsInit(Constant.Is.YES);
                list.add(info);
            }
        }
        Integer result = 0;
        for (AccountSubjectDto item : list) {
            try {
                result += accountSubjectService.addInfo(item, Constant.AccountSubjectData.ADD_ROOT_TYPE);
            } catch (Exception e) {
                logger.error("编码[" + item.getCode() + "]引入失败");
            }
        }
        //日志记录id
        List<Long> idList = new ArrayList<>();
        for (AccountSubjectDto info : list) {
            idList.add(info.getId());
        }
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys, LogConstant.menuName.accoutSubject,
                LogConstant.operation.addBatch, LogConstant.operation.addBatch_type, idList.toString()));
        return ok(result);
    }

    /**
     * 新增科目表辅助核算项.
     *
     * @param subjectAuxiliaryDto
     * @return Result
     * @author 周鹏
     * @date 2019/8/24
     */
    @PostMapping("addSubjectAuxiliary")
    public Result<ResultCode> addSubjectAuxiliary(@RequestBody SubjectAuxiliaryDto subjectAuxiliaryDto) {
        int result = accountSubjectService.addSubjectAuxiliary(subjectAuxiliaryDto);
        List<SubjectAuxiliaryDto> enableList = subjectAuxiliaryDto.getEnableList();
        List<SubjectAuxiliaryDto> disableList = subjectAuxiliaryDto.getDisableList();
        //日志记录id
        List<Long> idList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(enableList)) {
            for (SubjectAuxiliaryDto info : enableList) {
                idList.add(info.getId());
            }
            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.FinanceBackSys, LogConstant.menuName.subjectAuxiliary,
                    LogConstant.operation.addBatch, LogConstant.operation.addBatch_type, idList.toString()));
        }
        if (CollectionUtils.isNotEmpty(disableList)) {
            for (SubjectAuxiliaryDto info : disableList) {
                idList.add(info.getId());
            }
            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.FinanceBackSys, LogConstant.menuName.subjectAuxiliary,
                    LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, idList.toString()));
        }

        if (result > 0) {
            return ok();
        }
        return error(ResultCode.OPERATION_FAILURE);
    }

    /**
     * 更新会计科目相关信息.
     *
     * @param accountSubjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/6/12
     */
    @PostMapping("update")
    public Result<Long> update(@RequestBody AccountSubjectDto accountSubjectDto) {
        SysUserVo operator = UserUtils.getUserVo();
        accountSubjectDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        accountSubjectDto.setUpdateTime(new Date());
        accountSubjectDto.setUpdatorId(operator.getUserId());
        accountSubjectDto.setUpdatorName(operator.getName());
        Result<Long> result = addInfo(accountSubjectDto, () -> accountSubjectService.update(accountSubjectDto));
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys, LogConstant.menuName.accoutSubject,
                LogConstant.operation.update, LogConstant.operation.update_type, accountSubjectDto.getId().toString()));
        return result;
    }

    /**
     * 根据id集合批量删除会计科目信息
     *
     * @param accountSubjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/6/12
     */
    @PostMapping("updateBatchDelete")
    public Result<BatchResult> updateBatchDelete(@RequestBody AccountSubjectDto accountSubjectDto) {
        FastUtils.checkParams(accountSubjectDto.getIds());
        accountSubjectDto.setIsDel(Constant.Is.YES);
        SysUserVo operator = UserUtils.getUserVo();
        accountSubjectDto.setUpdatorId(operator.getUserId());
        accountSubjectDto.setUpdatorName(operator.getName());
        BatchResult batchResult = accountSubjectService.updateDelete(accountSubjectDto);
        if (batchResult.getSuccessDetailsList().size() > 0) {
            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.FinanceBackSys, LogConstant.menuName.accoutSubject,
                    LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, accountSubjectDto.getIds().toString()));
        }
        return ok(batchResult);
    }

    /**
     * 根据code集合禁用会计科目信息
     *
     * @param accountSubjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/6/12
     */
    @PostMapping("updateBatchDisable")
    public Result<BatchResult> updateBatchDisable(@RequestBody AccountSubjectDto accountSubjectDto) {
        BatchResult batchResult = getBatchResult(accountSubjectDto, Constant.Is.NO);
        return ok(batchResult);
    }

    /**
     * 根据code集合反禁用会计科目信息
     *
     * @param accountSubjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/6/12
     */
    @PostMapping("updateBatchEnable")
    public Result<BatchResult> updateBatchEnable(@RequestBody AccountSubjectDto accountSubjectDto) {
        BatchResult batchResult = getBatchResult(accountSubjectDto, Constant.Is.YES);
        return ok(batchResult);
    }

    /**
     * 获取禁用/反禁用结果
     *
     * @param accountSubjectDto
     * @param type
     * @return BatchResult
     * @author 周鹏
     * @date 2019/7/4
     */
    private BatchResult getBatchResult(AccountSubjectDto accountSubjectDto, byte type) {
        FastUtils.checkParams(accountSubjectDto.getSubjectId(), accountSubjectDto.getCodes(), type);
        accountSubjectDto.setIsEnable(type);
        SysUserVo operator = UserUtils.getUserVo();
        accountSubjectDto.setUpdatorId(operator.getUserId());
        accountSubjectDto.setUpdatorName(operator.getName());
        String operation = "";
        String operationType = "";
        if (type == Constant.Is.YES) {
            operation = LogConstant.operation.antiForbiddenBatch;
            operationType = LogConstant.operation.antiForbiddenBatch_type;
        } else if (type == Constant.Is.NO) {
            operation = LogConstant.operation.forbiddenBatch;
            operationType = LogConstant.operation.forbiddenBatch_type;
        }
        BatchResult batchResult = accountSubjectService.updateEnable(accountSubjectDto);
        if (batchResult.getSuccessDetailsList().size() > 0) {
            senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                    LogConstant.sysName.FinanceBackSys, LogConstant.menuName.accoutSubject,
                    operation, operationType, accountSubjectDto.getIds().toString()));
        }
        return batchResult;
    }

    /**
     * 分页查询会计科目模板数据列表
     *
     * @param accountSubjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/6/12
     */
    @PostMapping("findRootPage")
    public Result<Page<FindAccountSubjectListVo>> findRootPage(@RequestBody AccountSubjectDto accountSubjectDto) {
        FastUtils.checkParams(accountSubjectDto.getSubjectId());
        //获取运营平台会计科目模板数据
        return accountSubjectFeignClient.findMoreSubjectTemplatePage(accountSubjectDto);
    }

    /**
     * 账簿类型下拉框
     *
     * @return Result
     * @author 周鹏
     * @date 2019/6/25
     */
    @PostMapping("findAccountBookTypeSelection")
    public Result<List<AccountBookTypeVo>> findAccountBookTypeSelection() {
        //获取运营平台账簿类型选项
        return accountBookTypeFeignClient.findAccountBookTypeList();
    }

    /**
     * 会计准则下拉框
     *
     * @param
     * @return Result
     * @author 周鹏
     * @date 2019/6/25
     */
    @PostMapping("findAccountingStandardSelection")
    public Result<List<AccountingStandardVo>> findAccountingStandardSelection() {
        //获取运营平台会计准则选项
        return accountingStandardFeignClient.findAccountingList();
    }

    /**
     * 科目表下拉框
     *
     * @param platformSubjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/6/12
     */
    @PostMapping("findSubjectSelection")
    public Result<List<SubjectVo>> findSubjectSelection(@RequestBody SubjectDto platformSubjectDto) {
        FastUtils.checkParams(platformSubjectDto.getIsBase());
        //获取运营平台科目表选项
        return subjectFeignClient.findSubjectList(platformSubjectDto);
    }

    /**
     * 会计要素下拉框,科目类别下拉框,辅助核算项下拉框
     *
     * @param subjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/8/21
     */
    @PostMapping("findSelectionList")
    public Result<Map<String, Object>> findSelectionList(@RequestBody SubjectDto subjectDto) {
        Map<String, Object> resultMap = new HashMap<>();
        //根据基准表id查询科目表id
        Subject subjectInfo = accountSubjectService.findSubjectByParam(subjectDto);
        if (subjectInfo == null) {
            return ok(resultMap);
        }
        //获取运营平台-会计要素选项
        AccountElementItemDto accountElementItemDto = new AccountElementItemDto();
        accountElementItemDto.setElementId(subjectInfo.getElementId());
        Result<List<AccountElementItemVo>> elementItemVoResult = accountElementItemFeignClient.findListByEleId(accountElementItemDto);
        List<AccountElementItemVo> elementItemVoList = elementItemVoResult.getData();
        //获取运营平台-科目类别选项
        SubjectCategoryDto subjectCategoryDto = new SubjectCategoryDto();
        subjectCategoryDto.setElementId(subjectInfo.getElementId());
        Result<List<SubjectCategoryVo>> categoryVoResult = subjectCategoryFeignClient.findListByElemId(subjectCategoryDto);
        List<SubjectCategoryVo> categoryVoList = categoryVoResult.getData();
        //获取已配置辅助核算项
        SubjectAuxiliaryDto subjectAuxiliaryDto = new SubjectAuxiliaryDto();
        subjectAuxiliaryDto.setSubjectId(subjectInfo.getId());
        List<SubjectAuxiliaryVo> subjectAuxiliaryList = accountSubjectService.findSubjectAuxiliaryList(subjectAuxiliaryDto);
        resultMap.put("accountElementItemList", elementItemVoList);
        resultMap.put("subjectCategoryList", categoryVoList);
        resultMap.put("subjectAuxiliaryList", subjectAuxiliaryList);
        return ok(resultMap);
    }

    /**
     * 查询辅助核算设置列表
     *
     * @return Result
     * @author 周鹏
     */
    @PostMapping("findAuxiliaryItemList")
    public Result<Map<String, Object>> findAuxiliaryItemList(@RequestBody SubjectDto subjectDto) {
        Map<String, Object> resultMap = new HashMap<>();
        //根据基准表id查询科目表id
        Subject subjectInfo = accountSubjectService.findSubjectByParam(subjectDto);
        //获取平台辅助核算项
        AuxiliaryItemDto platformAuxiliaryItemDto = new AuxiliaryItemDto();
        platformAuxiliaryItemDto.setIsReleased(Constant.Is.YES);
        platformAuxiliaryItemDto.setIsApproved(Constant.Is.YES);
        Result<List<AuxiliaryItemVo>> auxiliaryItemVoResult = auxiliaryFeignClient.findAuxiliaryItemList(platformAuxiliaryItemDto);
        List<AuxiliaryItemVo> auxiliaryItemList = auxiliaryItemVoResult.getData();
        //获取自定义辅助核算项(包含是否已启用标识)
        List<AccountingItemVo> accountingItemList = accountingItemService.findAllAccountItem(subjectInfo);
        //获取已配置辅助核算项
        SubjectAuxiliaryDto subjectAuxiliaryDto = new SubjectAuxiliaryDto();
        subjectAuxiliaryDto.setSubjectId(subjectInfo.getId());
        List<SubjectAuxiliaryVo> subjectAuxiliaryList = accountSubjectService.findSubjectAuxiliaryList(subjectAuxiliaryDto);
        //根据已配置辅助核算项设置平台辅助核算项的是否启用属性
        MergeUtil.merge(auxiliaryItemList, subjectAuxiliaryList,
                (auxiliaryItem, subjectAuxiliary) -> auxiliaryItem.getCode().equals(subjectAuxiliary.getCode()),
                (auxiliaryItem, subjectAuxiliary) -> {
                    auxiliaryItem.setIsInit(subjectAuxiliary.getIsInit());
                    auxiliaryItem.setIfUsed(Integer.valueOf(Constant.Is.YES));
                });
        resultMap.put("subjectId", subjectInfo.getId());
        resultMap.put("auxiliaryItemList", auxiliaryItemList);
        resultMap.put("accountingItemList", accountingItemList);
        return ok(resultMap);
    }

    /**
     * 查询已配置辅助核算项目列表.
     *
     * @param subjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/8/27
     */
    @PostMapping("findSubjectAuxiliaryList")
    public Result<List<SubjectAuxiliaryVo>> findSubjectAuxiliaryList(@RequestBody SubjectDto subjectDto) {
        //根据基准表id查询科目表id
        Subject subjectInfo = accountSubjectService.findSubjectByParam(subjectDto);
        //获取已配置辅助核算项
        SubjectAuxiliaryDto subjectAuxiliaryDto = new SubjectAuxiliaryDto();
        subjectAuxiliaryDto.setSubjectId(subjectInfo.getId());
        List<SubjectAuxiliaryVo> subjectAuxiliaryList = accountSubjectService.findSubjectAuxiliaryList(subjectAuxiliaryDto);
        return ok(subjectAuxiliaryList);
    }

    /**
     * 根据id查询会计科目信息.
     *
     * @param accountSubjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/6/12
     */
    @PostMapping("findInfoById")
    public Result<AccountSubjectVo> findInfoById(@RequestBody AccountSubjectDto accountSubjectDto) {
        FastUtils.checkParams(accountSubjectDto.getId());
        return ok(accountSubjectService.findInfoById(accountSubjectDto));
    }

    /**
     * 根据id查询会计科目信息.
     *
     * @param accountSubjectDto
     * @return Result
     * @author --
     * @date 2019/6/12
     */
    @PostMapping("findNamesByIds")
    public Result<List<AccountSubjectVo>> findNamesByIds(@RequestBody AccountSubjectDto accountSubjectDto) {
        FastUtils.checkParams(accountSubjectDto.getIds());
        return ok(accountSubjectService.findSubjectInfoByParam(accountSubjectDto));
    }

    /**
     * 分页查询会计科目列表信息
     *
     * @param accountSubjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/6/12
     */
    @PostMapping("findPage")
    public Result<Page<AccountSubjectVo>> findPage(@RequestBody AccountSubjectDto accountSubjectDto) {
        //user端校验查询权限
        if (accountSubjectDto.getIsEnterpriseAdmin() != null && accountSubjectDto.getIsEnterpriseAdmin().equals(Constant.Is.NO)
                && accountSubjectDto.getCompanyId() != null && !accountSubjectDto.getCompanyId().equals(Constant.AccountSubjectData.GROUP_ID)) {
            Boolean flag = ShiroUtils.hasPerm(Constant.MenuDefine.ACCOUNT_SUBJECT_FIND, accountSubjectDto.getCompanyId());
            if (!flag) {
                return ok();
            }
        }
        SysUserVo operator = UserUtils.getUserVo();
        accountSubjectDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        return ok(accountSubjectService.findPage(accountSubjectDto));
    }

    /**
     * 查询会计科目信息是否末级
     *
     * @param accountSubjectDto
     * @return Result
     * @author: 周鹏
     * @create: 2019/7/25
     */
    @PostMapping("findIfIsFinal")
    public Result<Boolean> findIfIsFinal(@RequestBody AccountSubjectDto accountSubjectDto) {
        FastUtils.checkParams(accountSubjectDto.getId());
        List<Long> ids = new ArrayList<>();
        ids.add(accountSubjectDto.getId());
        accountSubjectDto.setIds(ids);
        List<AccountSubjectVo> list = accountSubjectService.findSubjectInfoByParam(accountSubjectDto);
        Boolean flag = true;
        if (list.size() == 1 && Constant.Is.NO.equals(list.get(0).getIsFinal())) {
            flag = false;
        }
        return ok(flag);
    }

    /**
     * 查询会计科目信息是否被引用
     *
     * @param accountSubjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/7/2
     */
    @PostMapping("findIfIsCited")
    public Result<Boolean> findIfIsCited(@RequestBody AccountSubjectDto accountSubjectDto) {
        FastUtils.checkParams(accountSubjectDto.getSubjectId());
        SysUserVo operator = UserUtils.getUserVo();
        accountSubjectDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        Boolean flag = accountSubjectService.findIfIsCited(accountSubjectDto);
        return ok(flag);
    }

    /**
     * 查询当前科目表是否已经增加新的科目
     *
     * @param accountSubjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/8/22
     */
    @PostMapping("findIfExistNewInfo")
    public Result<Boolean> findIfExistNewInfo(@RequestBody AccountSubjectDto accountSubjectDto) {
        FastUtils.checkParams(accountSubjectDto.getSubjectId());
        SysUserVo operator = UserUtils.getUserVo();
        accountSubjectDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        accountSubjectDto.setIsInit(Constant.Is.NO);
        Boolean flag = accountSubjectService.findIfExistNewInfo(accountSubjectDto);
        return ok(flag);
    }

    /**
     * 根据id查询所有上级科目信息
     *
     * @param accountSubjectDto
     * @return Result
     * @author: 周鹏
     * @create: 2019/8/2
     */
    @PostMapping("findAllParentInfo")
    public Result<List<AccountSubjectVo>> findAllParentInfo(@RequestBody AccountSubjectDto accountSubjectDto) {
        return ok(accountSubjectService.findAllParentInfo(accountSubjectDto));
    }

    /**
     * 根据条件查询会计科目信息(支持总账需求)
     *
     * @param accountSubjectDto
     * @return Result
     * @author: 周鹏
     * @create: 2019/8/6
     */
    @PostMapping("findInfoForLedger")
    public Result<List<AccountSubjectVo>> findInfoForLedger(@RequestBody AccountSubjectDto accountSubjectDto) {
        return ok(accountSubjectService.findInfoForLedger(accountSubjectDto));
    }

    /**
     * 根据条件查询单个辅助核算来源表信息(支持总账需求,后端用)
     *
     * @param accountSubjectDto
     * @return Result
     * @author: 周鹏
     * @create: 2019/8/6
     */
    @PostMapping("findSourceTableInfo")
    public Result<Map<Long, Map<String, Object>>> findSourceTableInfo(@RequestBody AccountSubjectDto accountSubjectDto) {
        return ok(accountSubjectService.findSourceTableInfo(accountSubjectDto));
    }

    /**
     * 根据条件查询所有辅助核算来源表信息(支持总账需求,后端用)
     *
     * @param accountSubjectDto
     * @return Result
     * @author: 周鹏
     * @create: 2019/8/31
     */
    @PostMapping("findAllSourceTableInfo")
    public Result<List<List<Map<String, Object>>>> findAllSourceTableInfo(@RequestBody AccountSubjectDto accountSubjectDto) {
        FastUtils.checkParams(accountSubjectDto.getSourceTableList());
        return ok(accountSubjectService.findAllSourceTableInfo(accountSubjectDto));
    }

    /**
     * 根据条件查询相应辅助核算来源表信息列表(支持总账需求,前端用)
     *
     * @param accountSubjectDto
     * @return Result
     * @author: 周鹏
     * @create: 2019/8/28
     */
    @PostMapping("findSourceTableList")
    public Result<Page<Map<String, Object>>> findSourceTableList(@RequestBody AccountSubjectDto accountSubjectDto) {
        FastUtils.checkParams(accountSubjectDto.getCompanyIds(), accountSubjectDto.getSourceTable(), accountSubjectDto.getIfFindUseCompanyDataOnly());
        return ok(accountSubjectService.findSourceTableList(accountSubjectDto));
    }

    /**
     * 根据条件查询会计科目关联的辅助核算项组合(支持总账需求)
     *
     * @param accountSubjectDto
     * @return Result
     * @author: 周鹏
     * @create: 2019/8/21
     */
    @PostMapping("findAuxiliaryGroup")
    public Result<List<AccountSubjectAuxiliaryVo>> findAuxiliaryGroup(@RequestBody AccountSubjectDto accountSubjectDto) {
        FastUtils.checkParams(accountSubjectDto.getSubjectId());
        return ok(accountSubjectService.findAuxiliaryGroup(accountSubjectDto));
    }

    /**
     * 根据会计科目查询辅助核算信息列表(支持总账需求)
     *
     * @param accountSubjectAuxiliaryDto
     * @return Result
     * @author: 周鹏
     * @create: 2019/8/28
     */
    @PostMapping("findAuxiliaryPage")
    public Result<Page<AccountSubjectAuxiliaryVo>> findAuxiliaryPage(@RequestBody AccountSubjectAuxiliaryDto accountSubjectAuxiliaryDto) {
        FastUtils.checkParams(accountSubjectAuxiliaryDto.getAccountSubjectId());
        return ok(accountSubjectService.findAuxiliaryPage(accountSubjectAuxiliaryDto));
    }

    /**
     * 根据条件查询科目信息
     *
     * @param dto
     * @return Result<AccountSubjectVo>
     * @author: wuweiming
     * @create: 2019/08/09
     */
    @PostMapping("findSubjectInfoByParam")
    public Result<AccountSubjectVo> findSubjectInfoByParam(@RequestBody AccountSubjectDto dto) {
        //根据基准表id查询科目表id
        SubjectDto subjectDto = new SubjectDto();
        subjectDto.setId(dto.getSubjectId());
        subjectDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        Subject subjectInfo = accountSubjectService.findSubjectByParam(subjectDto);

        Result<List<AccountElementItemVo>> elementItemVoResult = new Result<>();
        if (subjectInfo != null) {
            //获取运营平台-会计要素选项
            AccountElementItemDto accountElementItemDto = new AccountElementItemDto();
            accountElementItemDto.setElementId(subjectInfo.getElementId());
            elementItemVoResult = accountElementItemFeignClient.findListByEleId(accountElementItemDto);
        }

        //查询 科目信息
        dto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        List<AccountSubjectVo> accountSubjectList = accountSubjectService.findSubjectInfoByParam(dto);

        AccountSubjectVo vo = new AccountSubjectVo();
        vo.setSubject(subjectInfo);
        vo.setElementItemVoResult(elementItemVoResult);
        vo.setAccountSubjectList(accountSubjectList);

        return ok(vo);
    }

    /**
     * 根据code和subjectId查询所有下级科目信息
     *
     * @param accountSubjectDto
     * @return Result<List<AccountSubjectVo>>
     * @author: wuweiming
     * @create: 2019/08/06
     */
    @PostMapping("findAllChildInfo")
    public Result<List<AccountSubjectVo>> findAllChildInfo(@RequestBody AccountSubjectDto accountSubjectDto) {
        return ok(accountSubjectService.findAllChildInfo(accountSubjectDto));
    }

    /**
     * 查询会计科目信息
     *
     * @param accountSubjectDto
     * @return Result<List<AccountSubjectVo>>
     * @author: 朱小明
     * @create: 2019/08/23
     */
    @PostMapping("findAccountSubjectByElement")
    public Result<List<AccountSubjectVo>> findAccountSubjectByElement(@RequestBody AccountSubjectDto accountSubjectDto) {
        return ok(accountSubjectService.findSubjectInfoByParam(accountSubjectDto));
    }

    /**
     * @description: 获取会计科目辅助核算
     * @param: [accountSubjectAuxiliaryDto]
     * @return: com.njwd.support.Result<com.njwd.entity.platform.AccountSubjectAuxiliary>
     * @author: xdy
     * @create: 2019-09-03 15:34
     */
    @PostMapping("findAccountSubjectAuxiliary")
    public Result<AccountSubjectAuxiliary> findAccountSubjectAuxiliary(@RequestBody AccountSubjectAuxiliaryDto accountSubjectAuxiliaryDto) {
        return ok(accountSubjectService.findAccountSubjectAuxiliary(accountSubjectAuxiliaryDto));
    }

    /**
     * 根据条件查询科目信息
     *
     * @param dto
     * @return Result<AccountSubjectVo>
     * @author: wuweiming
     * @create: 2019/09/19
     */
    @PostMapping("findSubjectInfoByParamWithCodes")
    public Result<AccountSubjectVo> findSubjectInfoByParamWithCodes(@RequestBody AccountSubjectDto dto) {
        //校验必传字段是否为空
        FastUtils.checkParams(dto.getSubjectId(), dto.getIds(), dto.getIsFinal(), dto.getIsIncludeEnable());
        dto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        dto.setIsEnable(Constant.Is.YES);
        //根据基准表id查询科目表id
        SubjectDto subjectDto = new SubjectDto();
        subjectDto.setId(dto.getSubjectId());
        Subject subjectInfo = accountSubjectService.findSubjectByParam(subjectDto);

        Result<List<AccountElementItemVo>> elementItemVoResult = new Result<>();
        if (subjectInfo != null) {
            //获取运营平台-会计要素选项
            AccountElementItemDto accountElementItemDto = new AccountElementItemDto();
            accountElementItemDto.setElementId(subjectInfo.getElementId());
            elementItemVoResult = accountElementItemFeignClient.findListByEleId(accountElementItemDto);
        }

        //根据会计科目id查询会计科目信息
        List<AccountSubjectVo> list = accountSubjectService.findSubjectInfoByParam(dto);
        List<String> codeList = list.stream().map(AccountSubjectVo::getCode).collect(Collectors.toList());
        dto.setCodes(codeList);
        dto.setIds(null);
        dto.setIsEnable(null);
        //根据所有的末级id查询所有的上级科目信息
        List<AccountSubjectVo> accountSubjectList = accountSubjectService.findSubjectInfoByParam(dto);
        AccountSubjectVo vo = new AccountSubjectVo();
        vo.setSubject(subjectInfo);
        vo.setElementItemVoResult(elementItemVoResult);
        vo.setAccountSubjectList(accountSubjectList);

        return ok(vo);
    }

    /**
     * 根据条件查询科目表信息
     *
     * @param subjectDto
     * @return Result
     * @author: 周鹏
     * @create: 2019/11/12
     */
    @PostMapping("findSubjectInfo")
    public Result<Subject> findSubjectInfo(@RequestBody SubjectDto subjectDto) {
        FastUtils.checkParams(subjectDto.getId());
        return ok(accountSubjectService.findSubjectInfo(subjectDto));
    }

    /**
     * Excel 导出
     *
     * @param accountSubjectDto
     * @param response
     * @return
     * @author 周鹏
     * @date 2019/6/12
     */
    @RequestMapping("exportExcel")
    public Result<Boolean> exportExcel(@RequestBody AccountSubjectDto accountSubjectDto, HttpServletResponse response) {
        Boolean flag = true;
        //user端校验导出权限
        if (accountSubjectDto.getIsEnterpriseAdmin() != null && accountSubjectDto.getIsEnterpriseAdmin().equals(Constant.Is.NO)
                && accountSubjectDto.getCompanyId() != null && !accountSubjectDto.getCompanyId().equals(Constant.AccountSubjectData.GROUP_ID)) {
            flag = ShiroUtils.hasPerm(Constant.MenuDefine.ACCOUNT_SUBJECT_FIND, accountSubjectDto.getCompanyId());
        }
        if (flag) {
            accountSubjectService.exportExcel(accountSubjectDto, response);
        }
        return ok(flag);
    }

    /**
     * 校验会计科目编码和名称是否重复.
     *
     * @param accountSubjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/6/12
     */
    @PostMapping("checkDuplicateInfo")
    public Result<ResultCode> checkDuplicateInfo(@RequestBody AccountSubjectDto accountSubjectDto) {
        Boolean flag = true;
        if (StringUtil.isNotEmpty(accountSubjectDto.getCode()) || StringUtil.isNotEmpty(accountSubjectDto.getFullName())) {
            try {
                /*flag = RedisUtils.lock(String.format(Constant.LockKey.ACCOUNT_SUBJECT, accountSubjectDto.getCode(), accountSubjectDto.getName()),
                        Constant.SysConfig.REDIS_LOCK_TIMEOUT, () -> accountSubjectService.checkDuplicateInfo(accountSubjectDto));*/
                flag = accountSubjectService.checkDuplicateInfo(accountSubjectDto);
            } catch (Exception e) {
                logger.error(e.toString());
            }
        }
        if (flag) {
            //校验通过
            return ok();
        }
        //校验不通过,返回校验结果（覆盖校验重复提示并返回重复字段）
        ResultCode.COLUMN_EXIST.message = accountSubjectDto.getMessage();
        return ok(ResultCode.COLUMN_EXIST, accountSubjectDto.getColumnList());
    }

    /**
     * 校验预置科目下是否存在下级预置科目.
     *
     * @param accountSubjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/7/25
     */
    @PostMapping("checkNextInitInfo")
    public Result<ResultCode> checkNextInitInfo(@RequestBody AccountSubjectDto accountSubjectDto) {
        FastUtils.checkParams(accountSubjectDto.getUpCode());
        SysUserVo operator = UserUtils.getUserVo();
        accountSubjectDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        Boolean flag = accountSubjectService.checkNextInitInfo(accountSubjectDto);
        if (flag) {
            //校验通过
            return ok();
        }
        //校验不通过,返回校验结果
        return ok(ResultCode.ADD_UNDER_INIT_DATA, null);
    }

    /**
     * 根据code集合校验批量禁用与反禁用
     *
     * @param accountSubjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/9/3
     */
    @PostMapping("checkUpdateBatch")
    public Result<Boolean> checkUpdateBatch(@RequestBody AccountSubjectDto accountSubjectDto) {
        FastUtils.checkParams(accountSubjectDto.getSubjectId(), accountSubjectDto.getCodes(), accountSubjectDto.getIsEnable());
        Boolean result = accountSubjectService.checkUpdateBatch(accountSubjectDto);
        return ok(result);
    }

}
