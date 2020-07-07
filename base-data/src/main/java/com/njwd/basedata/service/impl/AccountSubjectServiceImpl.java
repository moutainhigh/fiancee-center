package com.njwd.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.cloudclient.AuxiliaryFeignClient;
import com.njwd.basedata.mapper.*;
import com.njwd.basedata.service.AccountSubjectService;
import com.njwd.basedata.service.BaseCustomService;
import com.njwd.basedata.service.CashFlowItemService;
import com.njwd.common.Constant;
import com.njwd.common.MenuCodeConstant;
import com.njwd.entity.basedata.ReferenceContext;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.SubjectAuxiliary;
import com.njwd.entity.basedata.dto.SubjectAuxiliaryDto;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.basedata.vo.SubjectAuxiliaryVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.AccountSubject;
import com.njwd.entity.platform.AccountSubjectAuxiliary;
import com.njwd.entity.platform.AccountSubjectCurrency;
import com.njwd.entity.platform.Subject;
import com.njwd.entity.platform.dto.AccountSubjectAuxiliaryDto;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.dto.CashFlowItemDto;
import com.njwd.entity.platform.dto.SubjectDto;
import com.njwd.entity.platform.vo.*;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.service.FileService;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 会计科目实现类
 * @Author 周鹏
 * @Date 2019/6/12
 */
@Service
public class AccountSubjectServiceImpl implements AccountSubjectService {

    @Autowired
    private AccountSubjectMapper accountSubjectMapper;

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private AccountSubjectCurrencyMapper accountSubjectCurrencyMapper;

    @Autowired
    private AccountSubjectAuxiliaryMapper accountSubjectAuxiliaryMapper;

    @Autowired
    private SubjectAuxiliaryMapper subjectAuxiliaryMapper;

    @Autowired
    private AccountSubjectService accountSubjectService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ReferenceRelationService referenceRelationService;

    @Autowired
    BaseCustomService baseCustomService;

    @Autowired
    CashFlowItemService cashFlowItemService;

    @Autowired
    private AuxiliaryFeignClient auxiliaryFeignClient;

    /**
     * 新增会计科目模板数据
     *
     * @param list
     * @return int
     * @author: 周鹏
     * @create: 2019/6/12
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = Constant.RedisCache.ACCOUNT_SUBJECT, allEntries = true)
    public int addRoot(List<AccountSubjectDto> list) {
        int result = 0;
        if (CollectionUtils.isNotEmpty(list)) {
            SysUserVo operator = UserUtils.getUserVo();
            //设置平台表中现金流量id为基础资料库现金流量项目表对应的id
            setCashFlowIdInfo(list, operator);
            //根据subjectId分组
            Map<String, List<AccountSubjectDto>> collect = list.stream().collect(Collectors.groupingBy(e -> e.getSubjectId().toString()));
            List<AccountSubjectDto> subjectList;
            AccountSubjectDto info;
            Boolean flag;
            Subject subject;
            Subject subjectInfo;
            for (String key : collect.keySet()) {
                subjectList = collect.get(key);
                info = subjectList.get(0);
                info.setRootEnterpriseId(operator.getRootEnterpriseId());
                //判断当前所有会计科目数据是否有被引用，被引用，则抛出已被引用异常，没有被引用，则先删除当前基准科目的科目表信息以及会计科目所有数据，再引入新的一级科目
                flag = findIfIsCited(info);
                if (!flag) {
                    //已被引用
                    throw new ServiceException(ResultCode.IS_CITED);
                }
                subject = new Subject();
                FastUtils.copyProperties(info, subject);
                subject.setId(null);
                subject.setCreatorId(operator.getUserId());
                subject.setCreatorName(operator.getName());
                subject.setMaxLevel(Constant.AccountSubjectData.MAX_LEVEL);
                //step1:先查询科目表信息是否存在,不存在则新增科目表,存在则更新科目表并删除会计科目相关信息
                subjectInfo = subjectMapper.findInfoByParam(subject);
                if (subjectInfo != null) {
                    //更新科目表信息
                    subject.setId(subjectInfo.getId());
                    subjectMapper.updateById(subject);
                    //删除会计科目相关信息
                    info.setSubjectId(subjectInfo.getId());
                    deleteInfo(info);
                } else {
                    //初始化记录平台ID
                    subject.setPlatformId(info.getSubjectId());
                    //新增科目表信息
                    subjectMapper.insert(subject);
                }
                //step2:新增科目表辅助核算项信息
                insertSubjectAuxiliary(subject, operator);
                //step3:新增会计科目相关信息
                for (AccountSubjectDto accountSubjectDto : subjectList) {
                    //将科目表主键id作为会计科目的subjectId
                    accountSubjectDto.setSubjectId(subject.getId());
                    accountSubjectDto.setIsInit(Constant.Is.YES);
                    accountSubjectDto.setRootEnterpriseId(operator.getRootEnterpriseId());
                    accountSubjectDto.setCreatorId(operator.getUserId());
                    accountSubjectDto.setCreatorName(operator.getName());
                    result = addInfo(accountSubjectDto, Constant.AccountSubjectData.ADD_ROOT_TYPE);
                }
            }
        }
        return result;
    }

    /**
     * 新增科目表辅助核算项信息
     *
     * @param subject  科目表信息
     * @param operator 操作人信息
     */
    private void insertSubjectAuxiliary(Subject subject, SysUserVo operator) {
        List<SubjectAuxiliaryDto> list = new LinkedList<>();
        //新增一条核算主体辅助核算到科目表辅助核算项设置表
        SubjectAuxiliaryDto subjectAuxiliary = new SubjectAuxiliaryDto();
        subjectAuxiliary.setRootEnterpriseId(operator.getRootEnterpriseId());
        subjectAuxiliary.setSubjectId(subject.getId());
        subjectAuxiliary.setCode(Constant.AccountBookEntityInfo.CODE);
        subjectAuxiliary.setName(Constant.AccountBookEntityInfo.NAME);
        subjectAuxiliary.setSource(Constant.SubjectAuxiliarySource.PLATFORM);
        subjectAuxiliary.setSourceTable(Constant.AccountBookEntityInfo.SOURCE_TABLE);
        subjectAuxiliary.setCreatorId(operator.getUserId());
        subjectAuxiliary.setCreatorName(operator.getName());
        subjectAuxiliary.setIsInit(Constant.Is.YES);
        list.add(subjectAuxiliary);
        //新增平台的科目表辅助核算项信息
        SubjectDto subjectDto = new SubjectDto();
        subjectDto.setId(subject.getId());
        Result<List<AuxiliaryItemVo>> data = auxiliaryFeignClient.findBySubjectId(subjectDto);
        List<AuxiliaryItemVo> auxiliaryItemVoList = data.getData();
        if (CollectionUtils.isNotEmpty(auxiliaryItemVoList)) {
            SubjectAuxiliaryDto info;
            for (AuxiliaryItemVo item : auxiliaryItemVoList) {
                info = new SubjectAuxiliaryDto();
                FastUtils.copyProperties(item, info);
                list.add(info);
            }
        }
        subjectAuxiliaryMapper.addSubjectAuxiliary(list, subjectAuxiliary);
    }

    /**
     * 新增会计科目相关信息
     *
     * @param accountSubjectDto
     * @return int
     * @author: 周鹏
     * @create: 2019/6/12
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int add(AccountSubjectDto accountSubjectDto) {
        int result;
        accountSubjectDto.setIsFinal(Constant.Is.YES);
        result = addInfo(accountSubjectDto, Constant.AccountSubjectData.ADD_TYPE);
        return result;
    }

    /**
     * 新增会计科目相关信息(公共方法)
     *
     * @param accountSubjectDto
     * @param type
     * @return int
     * @author: 周鹏
     * @create: 2019/6/12
     */
    @Override
    public int addInfo(AccountSubjectDto accountSubjectDto, String type) {
        int result;
        //校验科目编码是否以00结尾
        String digitCode = accountSubjectDto.getCode().substring(accountSubjectDto.getUpCode().length());
        if (digitCode.equals(Constant.Character.ZERO)) {
            throw new ServiceException(ResultCode.WRONG_CODE);
        }
        //校验科目编码及名称是否重复
        Boolean flag = checkDuplicateInfo(accountSubjectDto);
        if (!flag) {
            //校验不通过,返回校验结果（覆盖校验重复提示并返回重复字段）
            ResultCode.COLUMN_EXIST.message = accountSubjectDto.getMessage();
            throw new ServiceException(ResultCode.COLUMN_EXIST, accountSubjectDto.getColumnList());
        }
        AccountSubjectVo upInfo = new AccountSubjectVo();
        //只有在新增下级和导入的时候,判断上级科目是否可以新增下级(过滤引入模板操作)
        if (Constant.AccountSubjectData.ADD_TYPE.equals(type) && StringUtil.isNotEmpty(accountSubjectDto.getUpCode())) {
            //页面新增下级的时候,验证上级科目的版本号
            if (null != accountSubjectDto.getUpId() && null != accountSubjectDto.getUpVersion()) {
                AccountSubject parentInfo = accountSubjectMapper.selectById(accountSubjectDto.getUpId());
                if (!accountSubjectDto.getUpVersion().equals(parentInfo.getVersion())) {
                    throw new ServiceException(ResultCode.PARENT_VERSION_ERROR);
                }
                FastUtils.copyProperties(parentInfo, upInfo);
            } else {
                //查询上级科目
                List<AccountSubjectVo> list = accountSubjectMapper.findByParam(accountSubjectDto);
                if (list.size() == 0) {
                    throw new ServiceException(ResultCode.UP_ACCOUNT_SUBJECT_NOT_EXIST);
                } else if (list.size() > 1) {
                    //存在多条编码相同的数据
                    throw new ServiceException(ResultCode.UP_CODE_ONE_TO_MANY);
                } else {
                    upInfo = list.get(0);
                }
            }
            if (upInfo.getIsEnable().equals(Constant.Is.NO)) {
                throw new ServiceException(ResultCode.UP_ACCOUNT_SUBJECT_DISABLE);
            }
            //如果上级科目是预置数据,判断该上级科目下是否已存在下级预置数据
            if (upInfo.getIsInit().equals(Constant.Is.YES)) {
                flag = checkNextInitInfo(accountSubjectDto);
                if (!flag) {
                    throw new ServiceException(ResultCode.ADD_UNDER_INIT_DATA);
                }
            }
            //校验上级科目是否被引用
            List<Long> ids = new LinkedList<>();
            ids.add(upInfo.getId());
            ReferenceContext referenceContext = referenceRelationService.isReference(Constant.Reference.ACCOUNT_SUBJECT, ids);
            if (!referenceContext.getReferences().isEmpty()) {
                ResultCode.IS_CITED.message = referenceContext.getReferences().get(0).getReferenceDescription();
                throw new ServiceException(ResultCode.IS_CITED);
            }
        }
        //step1:新增会计科目信息
        //TODO 目前按照集团创建,集团共享的控制策略，如后面有新的策略，则按新的策略设置值
        accountSubjectDto.setCreateCompanyId(Constant.AccountSubjectData.GROUP_ID);
        accountSubjectDto.setCompanyId(Constant.AccountSubjectData.GROUP_ID);
        accountSubjectDto.setUseCompanyId(Constant.AccountSubjectData.GROUP_ID);
        AccountSubject accountSubject = new AccountSubject();
        FastUtils.copyProperties(accountSubjectDto, accountSubject);
        result = accountSubjectMapper.insert(accountSubject);
        //记录日志用
        accountSubjectDto.setId(accountSubject.getId());
        if (result > 0) {
            //step2:币种信息不为空，新增会计科目与币种关系信息
            if (StringUtil.isNotEmpty(accountSubjectDto.getCurrencyIds()) && StringUtil.isNotEmpty(accountSubjectDto.getCurrencyNames())) {
                addAccountSubjectCurrency(accountSubject, accountSubjectDto);
            }
            //step3:辅助核算信息不为空，新增会计科目与辅助核算关系信息
            if (StringUtil.isNotEmpty(accountSubjectDto.getAuxiliaryCodes())) {
                addAccountSubjectAuxiliary(accountSubject, accountSubjectDto, type);
            }
            //step4:如果存在上级科目,并且上级科目为末级,更新上级科目为非末级
            if (upInfo.getIsFinal() != null && upInfo.getIsFinal().equals(Constant.Is.YES)) {
                upInfo.setIsFinal(Constant.Is.NO);
                upInfo.setVersion(accountSubjectDto.getUpVersion());
                accountSubjectMapper.updateById(upInfo);
                //清除上级科目信息的缓存
                accountSubjectDto.setUpId(upInfo.getId());
            }
            //清除缓存
            RedisUtils.remove(Constant.RedisCache.ACCOUNT_SUBJECT, accountSubjectDto.getUpId());

            //TODO 这一版如果会计科目被引用,则无法新增下级,所以以下代码暂不使用
            /*//step4:如果存在上级科目,并且上级科目为末级,交换此科目数据与上级科目数据信息
            if (upInfo.getIsFinal() != null && upInfo.getIsFinal().equals(Constant.Is.YES)) {
                //返回给前端上级科目id
                accountSubjectDto.setId(upInfo.getId());
                AccountSubject upAccountSubject = new AccountSubject();
                FastUtils.copyProperties(accountSubjectDto, upAccountSubject);
                //特殊处理:前端去除现金流量预设选项的时候更新为空
                upAccountSubject.setCreateTime(new Date());
                upAccountSubject.setUpdateTime(new Date());
                upAccountSubject.setCashFlowId(upAccountSubject.getCashFlowId() == null ? 0L : upAccountSubject.getCashFlowId());
                upAccountSubject.setCashInflowId(accountSubject.getCashInflowId() == null ? 0L : upAccountSubject.getCashInflowId());
                upAccountSubject.setCashOutflowId(accountSubject.getCashOutflowId() == null ? 0L : upAccountSubject.getCashOutflowId());
                upAccountSubject.setCashInflowCode(upAccountSubject.getCashInflowCode() == null ? "" : upAccountSubject.getCashInflowCode());
                upAccountSubject.setCashOutflowCode(upAccountSubject.getCashOutflowCode() == null ? "" : upAccountSubject.getCashOutflowCode());
                upAccountSubject.setCashInflowName(upAccountSubject.getCashInflowName() == null ? "" : upAccountSubject.getCashInflowName());
                upAccountSubject.setCashOutflowName(upAccountSubject.getCashOutflowName() == null ? "" : upAccountSubject.getCashOutflowName());
                //更新上级科目为非预置
                upAccountSubject.setIsInit(Constant.Is.NO);
                //更新上级科目信息
                accountSubjectMapper.updateById(upAccountSubject);

                //替换上级辅助核算对应会计科目id为新增科目的id
                AccountSubjectAuxiliaryDto accountSubjectAuxiliaryDto = new AccountSubjectAuxiliaryDto();
                accountSubjectAuxiliaryDto.setAccountSubjectId(upInfo.getId());
                accountSubjectAuxiliaryDto.setChildAccountSubjectId(accountSubject.getId());
                accountSubjectAuxiliaryMapper.updateAccountSubjectId(accountSubjectAuxiliaryDto);

                //辅助核算信息不为空，新增上级会计科目与辅助核算关系信息
                if (StringUtil.isNotEmpty(accountSubjectDto.getAuxiliaryCodes())) {
                    addAccountSubjectAuxiliary(upAccountSubject, accountSubjectDto);
                }

                //替换上级科目id为新增科目的id
                upInfo.setId(accountSubject.getId());
                FastUtils.copyProperties(upInfo, accountSubject);
                accountSubject.setIsFinal(Constant.Is.NO);
                //更新新增科目的信息
                accountSubjectMapper.updateById(accountSubject);
            }*/
        }
        return result;
    }

    /**
     * 新增会计科目与币种关系信息
     *
     * @param accountSubject
     * @param accountSubjectDto
     * @return int
     * @author: 周鹏
     * @create: 2019/6/12
     */
    private int addAccountSubjectCurrency(AccountSubject accountSubject, AccountSubjectDto accountSubjectDto) {
        int result = 0;
        String[] currencyId = accountSubjectDto.getCurrencyIds().split(",", -1);
        String[] currencyName = accountSubjectDto.getCurrencyNames().split(",", -1);
        AccountSubjectCurrency accountSubjectCurrency = new AccountSubjectCurrency();
        for (int i = 0; i < currencyId.length; i++) {
            accountSubjectCurrency.setAccountSubjectId(accountSubject.getId());
            accountSubjectCurrency.setCurrencyId(Long.parseLong(currencyId[i]));
            accountSubjectCurrency.setCurrencyName(currencyName[i]);
            result = accountSubjectCurrencyMapper.insert(accountSubjectCurrency);
        }
        return result;
    }

    /**
     * 新增会计科目与辅助核算关系信息
     *
     * @param accountSubject
     * @param accountSubjectDto
     * @return int
     * @author: 周鹏
     * @create: 2019/6/12
     */
    private int addAccountSubjectAuxiliary(AccountSubject accountSubject, AccountSubjectDto accountSubjectDto, String type) {
        int result = 0;
        String[] auxiliaryCode = accountSubjectDto.getAuxiliaryCodes().split(",", -1);
        String[] auxiliaryName = accountSubjectDto.getAuxiliaryNames().split(",", -1);
        //TODO 这版本不加值来源和值依赖,后期迭代添加
//        String[] auxiliarySource = accountSubjectDto.getAuxiliarySources().split(",", -1);
        String[] auxiliarySourceTable = accountSubjectDto.getAuxiliarySourceTables().split(",", -1);
//        String[] auxiliaryDependent = accountSubjectDto.getAuxiliaryDependents().split(",", -1);
        AccountSubjectAuxiliary accountSubjectAuxiliary = new AccountSubjectAuxiliary();
        if (Constant.AccountSubjectData.ADD_ROOT_TYPE.equals(type)) {
            accountSubjectAuxiliary.setIsInit(Constant.Is.YES);
        }
        for (int i = 0; i < auxiliaryCode.length; i++) {
            accountSubjectAuxiliary.setAccountSubjectId(accountSubject.getId());
            accountSubjectAuxiliary.setAuxiliaryCode(auxiliaryCode[i]);
            accountSubjectAuxiliary.setAuxiliaryName(auxiliaryName[i]);
//            accountSubjectAuxiliary.setAuxiliarySource(auxiliarySource[i]);
            accountSubjectAuxiliary.setAuxiliarySourceTable(auxiliarySourceTable[i]);
//            accountSubjectAuxiliary.setAuxiliaryDependent(auxiliaryDependent[i]);
            result = accountSubjectAuxiliaryMapper.insert(accountSubjectAuxiliary);
        }
        return result;
    }

    /**
     * 新增科目表辅助核算项
     *
     * @param subjectAuxiliaryDto
     * @return int
     * @author: 周鹏
     * @create: 2019/8/24
     */
    @Override
    @CacheEvict(value = Constant.RedisCache.ACCOUNT_SUBJECT, allEntries = true)
    public int addSubjectAuxiliary(SubjectAuxiliaryDto subjectAuxiliaryDto) {
        int result;
        //启用的科目表辅助核算项集合
        List<SubjectAuxiliaryDto> enableList = subjectAuxiliaryDto.getEnableList();
        //取消之前已启用的科目表辅助核算项集合
        List<SubjectAuxiliaryDto> disableList = subjectAuxiliaryDto.getDisableList();
        Long subjectId = subjectAuxiliaryDto.getSubjectId();
        //校验取消之前已启用的辅助核算项是否已被会计科目使用
        if (CollectionUtils.isNotEmpty(disableList)) {
            AccountSubjectAuxiliary accountSubjectAuxiliary = accountSubjectAuxiliaryMapper.findUsedAuxiliaryName(disableList, subjectId);
            if (accountSubjectAuxiliary != null) {
                //存在已被使用的辅助核算项,则返回给前端哪些辅助核算项不能被取消启用
                ResultCode.COULD_NOT_CANCEL_ENABLE.message = accountSubjectAuxiliary.getAuxiliaryName() + ResultCode.COULD_NOT_CANCEL_ENABLE.message;
                throw new ServiceException(ResultCode.COULD_NOT_CANCEL_ENABLE);
            }
        }

        //删除此科目下非预置辅助核算项
        result = subjectAuxiliaryMapper.deleteBySubjectId(subjectId, Constant.Is.NO);
        if (CollectionUtils.isNotEmpty(enableList)) {
            //新增科目表辅助核算项
            SysUserVo operator = UserUtils.getUserVo();
            subjectAuxiliaryDto.setRootEnterpriseId(operator.getRootEnterpriseId());
            subjectAuxiliaryDto.setCreatorId(operator.getUserId());
            subjectAuxiliaryDto.setCreatorName(operator.getName());
            subjectAuxiliaryDto.setIsInit(Constant.Is.NO);
            result = subjectAuxiliaryMapper.addSubjectAuxiliary(enableList, subjectAuxiliaryDto);
        }
        return result;
    }

    /**
     * 删除会计科目相关信息
     *
     * @param accountSubjectDto
     * @return java.lang.String
     * @author 周鹏
     * @date 2019/6/26
     */
    private void deleteInfo(AccountSubjectDto accountSubjectDto) {
        //step1:删除会计科目与辅助核算关系信息
        accountSubjectAuxiliaryMapper.delete(accountSubjectDto);
        //step2:删除科目表辅助核算项信息
        subjectAuxiliaryMapper.deleteBySubjectId(accountSubjectDto.getSubjectId(), null);
        //step3:删除会计科目与币种关系信息
        accountSubjectCurrencyMapper.delete(accountSubjectDto);
        //step4:删除会计科目信息
        accountSubjectMapper.delete(accountSubjectDto);
    }

    /**
     * 更新会计科目相关信息
     *
     * @param accountSubjectDto
     * @return int
     * @author: 周鹏
     * @create: 2019/6/12
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(AccountSubjectDto accountSubjectDto) {
        int result = 0;
        AccountSubject accountSubject = new AccountSubject();
        //需要清除缓存的id集合
        List<Long> clearCacheList = new LinkedList<>();
        //校验数据状态
        checkStatus(accountSubjectDto);
        //校验版本号
        checkVersion(accountSubjectDto);
        //查询此记录是否被引用
        List<Long> ids = new LinkedList<>();
        ids.add(accountSubjectDto.getId());
        ReferenceContext referenceContext = referenceRelationService.isReference(Constant.Reference.ACCOUNT_SUBJECT, ids);
        if (!referenceContext.getReferences().isEmpty()) {
            //已被引用,只能修改现金流量信息
            accountSubject.setId(accountSubjectDto.getId());
            //更新时如果现金流入流出为空,则更新现金流入流出为""(特殊处理)
            setCashFlowInfo(accountSubjectDto, accountSubject);
            result = accountSubjectMapper.updateById(accountSubject);
        } else if (!referenceContext.getNotReferences().isEmpty()) {
            //校验科目编码及名称是否重复
            Boolean flag = checkDuplicateInfo(accountSubjectDto);
            if (!flag) {
                //校验不通过,返回校验结果（覆盖校验重复提示并返回重复字段）
                ResultCode.COLUMN_EXIST.message = accountSubjectDto.getMessage();
                throw new ServiceException(ResultCode.COLUMN_EXIST, accountSubjectDto.getColumnList());
            }
            //step1:更新会计科目信息
            FastUtils.copyProperties(accountSubjectDto, accountSubject);
            //更新时如果现金流入流出为空,则更新现金流入流出为""(特殊处理)
            setCashFlowInfo(accountSubjectDto, accountSubject);
            result = accountSubjectMapper.updateById(accountSubject);
            if (result > 0) {
                //step2:删除会计科目与币种关系信息
                AccountSubjectCurrency accountSubjectCurrency = new AccountSubjectCurrency();
                accountSubjectCurrency.setAccountSubjectId(accountSubject.getId());
                //查询此会计科目已关联的币种关系数量
                int currencyNum = accountSubjectCurrencyMapper.findCountByParam(accountSubjectCurrency);
                int delResult = accountSubjectCurrencyMapper.deleteByParam(accountSubjectCurrency);
                if (delResult == currencyNum) {
                    //step3:删除会计科目与辅助核算关系信息
                    AccountSubjectAuxiliary accountSubjectAuxiliary = new AccountSubjectAuxiliary();
                    accountSubjectAuxiliary.setAccountSubjectId(accountSubject.getId());
                    //查询此会计科目已关联的辅助核算关系数量
                    int auxiliaryNum = accountSubjectAuxiliaryMapper.findCountByParam(accountSubjectAuxiliary);
                    delResult = accountSubjectAuxiliaryMapper.deleteByParam(accountSubjectAuxiliary);
                    if (delResult != auxiliaryNum) {
                        throw new ServiceException(ResultCode.AUXILIARY_DELETE);
                    }
                    //step4:币种信息不为空，新增会计科目与币种关系信息
                    if (StringUtil.isNotEmpty(accountSubjectDto.getCurrencyIds()) && StringUtil.isNotEmpty(accountSubjectDto.getCurrencyNames())) {
                        addAccountSubjectCurrency(accountSubject, accountSubjectDto);
                    }
                    //step5:辅助核算信息不为空，新增会计科目与辅助核算关系信息
                    if (StringUtil.isNotEmpty(accountSubjectDto.getAuxiliaryCodes())) {
                        addAccountSubjectAuxiliary(accountSubject, accountSubjectDto, Constant.AccountSubjectData.ADD_TYPE);
                    }
                    //step6:判断所选上级科目是否变化,如果变化,则根据条件更新原上级科目的是否末级和现上级科目的是否末级字段
                    result = updateParentIsFinal(accountSubjectDto, clearCacheList);
                } else {
                    throw new ServiceException(ResultCode.CURRENCY_DELETE);
                }
            }
        }
        clearCacheList.add(accountSubject.getId());
        //清除缓存
        RedisUtils.removeBatch(Constant.RedisCache.ACCOUNT_SUBJECT, clearCacheList);
        return result;
    }

    /**
     * 校验版本号
     *
     * @param accountSubjectDto 会计科目信息
     */
    private void checkVersion(AccountSubjectDto accountSubjectDto) {
        //验证上级科目的版本号
        if (null != accountSubjectDto.getUpId() && null != accountSubjectDto.getUpVersion()) {
            AccountSubject parentInfo = accountSubjectMapper.selectById(accountSubjectDto.getUpId());
            if (null != parentInfo.getVersion() && !accountSubjectDto.getUpVersion().equals(parentInfo.getVersion())) {
                throw new ServiceException(ResultCode.PARENT_VERSION_ERROR);
            }
        }
        //验证此科目的版本号
        if (null != accountSubjectDto.getId() && null != accountSubjectDto.getVersion()) {
            AccountSubject newInfo = accountSubjectMapper.selectById(accountSubjectDto.getId());
            if (null != newInfo.getVersion() && !accountSubjectDto.getVersion().equals(newInfo.getVersion())) {
                throw new ServiceException(ResultCode.VERSION_ERROR);
            }
        }
    }

    /**
     * 更新原上级科目的是否末级和现上级科目的是否末级字段
     *
     * @param accountSubjectDto
     * @param result
     * @param clearCacheList
     * @return
     */
    private int updateParentIsFinal(AccountSubjectDto accountSubjectDto, List<Long> clearCacheList) {
        int result = 1;
        if (!accountSubjectDto.getUpCode().equals(accountSubjectDto.getOriginalUpCode())) {
            //查询现上级科目信息
            AccountSubjectVo parentInfo = accountSubjectMapper.findParentInfoByParam(accountSubjectDto);
            //更新现上级科目为非末级
            accountSubjectDto.setId(parentInfo.getId());
            accountSubjectDto.setIsFinal(Constant.Is.NO);
            result = accountSubjectMapper.updateIsFinal(accountSubjectDto);
            clearCacheList.add(parentInfo.getId());
            //查询原上级科目是否存在其他下级科目
            accountSubjectDto.setUpCode(accountSubjectDto.getOriginalUpCode());
            parentInfo = accountSubjectMapper.findParentInfoByParam(accountSubjectDto);
            if (parentInfo.getChildNum() == 0) {
                //不存在,则更新原上级科目为末级
                accountSubjectDto.setId(parentInfo.getId());
                accountSubjectDto.setIsFinal(Constant.Is.YES);
                result = accountSubjectMapper.updateIsFinal(accountSubjectDto);
                clearCacheList.add(parentInfo.getId());
            }
        }
        return result;
    }

    /**
     * 删除
     *
     * @param accountSubjectDto
     * @return UpdateResult
     * @author: 周鹏
     * @create: 2019/7/9
     */
    @Override
    public BatchResult updateDelete(AccountSubjectDto accountSubjectDto) {
        BatchResult batchResult = new BatchResult();
        List<ReferenceDescription> failList = new ArrayList<>();
        List<Long> idList = accountSubjectDto.getIds();
        if (CollectionUtils.isNotEmpty(idList)) {
            //验证版本号
            FastUtils.filterVersionIds(accountSubjectMapper, new QueryWrapper<>(), Constant.ColumnName.ID, idList, accountSubjectDto.getVersionList(), failList);
            // 查询已删除的记录id,有则放入操作失败集合
            FastUtils.filterIds(ResultCode.IS_DEL, accountSubjectMapper, new QueryWrapper<AccountSubject>().eq(Constant.ColumnName.IS_DEL, Constant.Is.YES), Constant.ColumnName.ID, idList, failList);
            /*// 查询非末级的记录id,有则放入操作失败集合
            FastUtils.filterIds(ResultCode.IS_NOT_FINAL, accountSubjectMapper, new QueryWrapper<AccountSubject>().eq(Constant.ColumnName.IS_FINAL, Constant.Is.NO), Constant.ColumnName.ID, idList, failList);*/
            // 查询被引用的记录,有则放入操作失败集合
            ReferenceContext referenceContext = referenceRelationService.isReference(Constant.Reference.ACCOUNT_SUBJECT, idList);
            if (!referenceContext.getReferences().isEmpty()) {
                //查询被引用记录的详情
                List<Long> ids = referenceContext.getReferences().stream().map(ReferenceDescription::getBusinessId).collect(Collectors.toList());
                List<AccountSubject> citedList = accountSubjectMapper.selectBatchIds(ids);
                MergeUtil.merge(referenceContext.getReferences(), citedList,
                        ReferenceDescription::getBusinessId, AccountSubject::getId,
                        (referenceDescription, accountSubject) -> referenceDescription.setInfo(accountSubject));
                failList.addAll(referenceContext.getReferences());
            }
            batchResult.setFailList(failList);
            if (!referenceContext.getNotReferences().isEmpty()) {
                // 操作未被引用的数据
                AccountSubject accountSubject = new AccountSubject();
                accountSubject.setIsDel(Constant.Is.YES);
                FastUtils.updateBatch(accountSubjectMapper, accountSubject, Constant.ColumnName.ID, referenceContext.getNotReferences(), batchResult.getSuccessDetailsList());
                // 查询操作成功的数据对应上级科目可以被更新为末级科目的数据
                SysUserVo operator = UserUtils.getUserVo();
                List<Long> finalList = accountSubjectMapper.findToBeFinalList(referenceContext.getNotReferences(), operator.getRootEnterpriseId());
                if (CollectionUtils.isNotEmpty(finalList)) {
                    // 如果存在可更新的会计科目,更新科目信息为末级
                    accountSubjectDto.setIsFinal(Constant.Is.YES);
                    for (Long id : finalList) {
                        accountSubjectDto.setId(id);
                        accountSubjectMapper.updateIsFinal(accountSubjectDto);
                    }
                }
                //清除缓存
                finalList.addAll(referenceContext.getNotReferences());
                RedisUtils.removeBatch(Constant.RedisCache.ACCOUNT_SUBJECT, finalList);
            }
        }
        return batchResult;
    }

    /**
     * 禁用/反禁用
     *
     * @param accountSubjectDto
     * @return UpdateResult
     * @author: 周鹏
     * @create: 2019/7/9
     */
    @Override
    public BatchResult updateEnable(AccountSubjectDto accountSubjectDto) {
        BatchResult batchResult = new BatchResult();
        List<ReferenceDescription> failList = new ArrayList<>();
        //最终需要处理的会计科目id集合
        List<Long> idList = accountSubjectMapper.findOperateIdsByParam(accountSubjectDto);
        if (CollectionUtils.isNotEmpty(idList)) {
            // 查询已删除的记录id,有则放入操作失败集合
            FastUtils.filterIds(ResultCode.IS_DEL, accountSubjectMapper, new QueryWrapper<AccountSubject>().eq(Constant.ColumnName.IS_DEL, Constant.Is.YES), Constant.ColumnName.ID, idList, failList);
            //查询启用状态已变更成功的记录,有则放入操作失败集合
            FastUtils.filterIds(Constant.Is.NO.equals(accountSubjectDto.getIsEnable()) ? ResultCode.IS_DISABLE : ResultCode.IS_ENABLE, accountSubjectMapper, new QueryWrapper<AccountSubject>().eq(Constant.ColumnName.IS_ENABLE, accountSubjectDto.getIsEnable()), Constant.ColumnName.ID, idList, failList);
            batchResult.setFailList(failList);
            if (idList.size() > 0) {
                //更新状态,并返回成功详情
                AccountSubject accountSubject = new AccountSubject();
                accountSubject.setBatchIds(idList);
                baseCustomService.batchEnable(accountSubject, accountSubjectDto.getIsEnable(), accountSubjectMapper, batchResult.getSuccessDetailsList());
                accountSubjectDto.setIds(idList);
                //清除缓存
                RedisUtils.removeBatch(Constant.RedisCache.ACCOUNT_SUBJECT, idList);
            }
        }
        return batchResult;
    }

    /**
     * 查询租户下是否存在会计科目信息
     *
     * @param rootEnterpriseId
     * @return int
     * @author: 周鹏
     * @create: 2019/7/15
     */
    @Override
    public int findCount(Long rootEnterpriseId) {
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        accountSubjectDto.setRootEnterpriseId(rootEnterpriseId);
        List<Long> ids = accountSubjectMapper.findIds(accountSubjectDto);
        return ids.size();
    }

    /**
     * 根据id查询会计科目信息
     *
     * @param accountSubjectDto
     * @return AccountSubjectVo
     * @author: 周鹏
     * @create: 2019/6/12
     */
    @Override
    public AccountSubjectVo findInfoById(AccountSubjectDto accountSubjectDto) {
        //查询会计科目基本信息及辅助核算项信息
        AccountSubjectVo accountSubjectVo = accountSubjectService.findAccountSubjectById(accountSubjectDto);
        //查询是否被引用
        List<Long> ids = new LinkedList<>();
        ids.add(accountSubjectDto.getId());
        ReferenceContext referenceContext = referenceRelationService.isReference(Constant.Reference.ACCOUNT_SUBJECT, ids);
        if (!referenceContext.getReferences().isEmpty()) {
            //已被引用
            accountSubjectVo.setIsCited(Constant.Is.YES);
        } else {
            accountSubjectVo.setIsCited(Constant.Is.NO);
        }
        return accountSubjectVo;
    }

    /**
     * 查询会计科目基本信息及辅助核算项信息
     *
     * @param accountSubjectDto
     * @return
     */
    @Override
    @Cacheable(value = Constant.RedisCache.ACCOUNT_SUBJECT, key = "#accountSubjectDto.id", unless = "#result == null")
    public AccountSubjectVo findAccountSubjectById(AccountSubjectDto accountSubjectDto) {
        AccountSubjectVo accountSubjectVo = accountSubjectMapper.findInfoById(accountSubjectDto);
        if (accountSubjectVo == null) {
            throw new ServiceException(ResultCode.RECORD_NOT_EXIST);
        }
        //获取已配置辅助核算项
        SubjectAuxiliaryDto subjectAuxiliaryDto = new SubjectAuxiliaryDto();
        subjectAuxiliaryDto.setSubjectId(accountSubjectVo.getSubjectId());
        subjectAuxiliaryDto.setAccountSubjectId(accountSubjectDto.getId());
        List<SubjectAuxiliaryVo> subjectAuxiliaryList = subjectAuxiliaryMapper.findSubjectAuxiliaryList(subjectAuxiliaryDto);
        accountSubjectVo.setSubjectAuxiliaryList(subjectAuxiliaryList);
        return accountSubjectVo;
    }

    /**
     * 分页查询会计科目列表信息
     *
     * @param accountSubjectDto
     * @return page
     * @author: 周鹏
     * @create: 2019/6/12
     */
    @Override
    public Page<AccountSubjectVo> findPage(AccountSubjectDto accountSubjectDto) {
        Page<AccountSubjectVo> page = accountSubjectDto.getPage();
        page = accountSubjectMapper.findPage(page, accountSubjectDto);
        if (accountSubjectDto.getIfFindAuxiliary() != null && accountSubjectDto.getIfFindAuxiliary().equals(Constant.Is.YES)) {
            List<AccountSubjectVo> list = page.getRecords();
            if (CollectionUtils.isNotEmpty(list)) {
                //封装查询辅助核算信息的参数
                List<Long> ids = list.stream().map(AccountSubjectVo::getId).collect(Collectors.toList());
                accountSubjectDto.setIds(ids);
                List<AccountSubjectAuxiliary> auxiliaryList = accountSubjectAuxiliaryMapper.findListById(accountSubjectDto);
                if (CollectionUtils.isNotEmpty(auxiliaryList)) {
                    MergeUtil.mergeList(list, auxiliaryList,
                            AccountSubjectVo::getId, AccountSubjectAuxiliary::getAccountSubjectId,
                            (accountSubjectVo, auxiliaryLists) -> accountSubjectVo.setAccountSubjectAuxiliaryList(auxiliaryLists));
                }
            }
        }
        return page;
    }

    /**
     * 查询会计科目信息id
     *
     * @param accountSubjectDto
     * @return list
     * @author: 周鹏
     * @create: 2019/12/4
     */
    @Override
    public List<Long> findIds(AccountSubjectDto accountSubjectDto){
        return accountSubjectMapper.findIds(accountSubjectDto);
    }

    /**
     * 查询会计科目信息是否被引用
     *
     * @param accountSubjectDto
     * @return boolean
     * @author: 周鹏
     * @create: 2019/7/2
     */
    @Override
    public Boolean findIfIsCited(AccountSubjectDto accountSubjectDto) {
        Boolean flag = true;
        accountSubjectDto.setIsFinal(Constant.Is.YES);
        List<Long> ids = accountSubjectMapper.findIds(accountSubjectDto);
        if (ids.size() > 0) {
            ReferenceContext referenceContext = referenceRelationService.isReference(Constant.Reference.ACCOUNT_SUBJECT, ids);
            if (!referenceContext.getReferences().isEmpty()) {
                //已被引用
                flag = false;
            }
        }
        return flag;
    }

    /**
     * 查询当前科目表是否已经增加新的科目
     *
     * @param accountSubjectDto
     * @return boolean
     * @author: 周鹏
     * @create: 2019/8/22
     */
    @Override
    public Boolean findIfExistNewInfo(AccountSubjectDto accountSubjectDto) {
        Boolean flag = true;
        List<Long> ids = accountSubjectMapper.findIds(accountSubjectDto);
        if (ids.size() > 0) {
            flag = false;
        }
        return flag;
    }

    /**
     * 根据id查询所有上级科目信息
     *
     * @param accountSubjectDto
     * @return AccountSubjectVo
     * @author: 周鹏
     * @create: 2019/8/2
     */
    @Override
    public List<AccountSubjectVo> findAllParentInfo(AccountSubjectDto accountSubjectDto) {
        //查询此末级会计科目信息
        List<AccountSubjectVo> list = accountSubjectMapper.findByParam(accountSubjectDto);
        //去除null记录
        list.removeAll(Collections.singleton(null));
        if (list.size() == 1) {
            findAllParentInfo(list.get(0), list);
        }
        return list;
    }

    /**
     * 根据会计科目信息查询上级科目信息
     *
     * @param accountSubjectVo,list
     * @return AccountSubjectVo
     * @author: 周鹏
     * @create: 2019/8/2
     */
    public void findAllParentInfo(AccountSubjectVo accountSubjectVo, List<AccountSubjectVo> list) {
        if (StringUtil.isNotEmpty(accountSubjectVo.getUpCode())) {
            //查询上级科目信息
            AccountSubjectDto param = new AccountSubjectDto();
            param.setUpCode(accountSubjectVo.getUpCode());
            param.setSubjectId(accountSubjectVo.getSubjectId());
            List<AccountSubjectVo> parentList = accountSubjectMapper.findByParam(param);
            //去除null记录
            list.removeAll(Collections.singleton(null));
            if (parentList.size() == 1) {
                list.add(parentList.get(0));
                //递归查询上级科目信息
                findAllParentInfo(parentList.get(0), list);
            }
        }
    }

    /**
     * 根据条件查询会计科目信息(支持总账需求)
     *
     * @param accountSubjectDto
     * @return AccountSubjectVo
     * @author: 周鹏
     * @create: 2019/8/2
     */
    @Override
    public List<AccountSubjectVo> findInfoForLedger(AccountSubjectDto accountSubjectDto) {
        SysUserVo operator = UserUtils.getUserVo();
        accountSubjectDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        if (accountSubjectDto.getBaseSubjectId() != null) {
            //根据基准表id查询科目表id
            Subject subject = new Subject();
            subject.setRootEnterpriseId(operator.getRootEnterpriseId());
            subject.setSubjectId(accountSubjectDto.getBaseSubjectId());
            Subject subjectInfo = subjectMapper.findInfoByParam(subject);
            accountSubjectDto.setSubjectId(subjectInfo.getId());
        }
        //查询会计科目相关信息
        List<AccountSubjectVo> list = accountSubjectMapper.findInfoForLedger(accountSubjectDto);
        if (accountSubjectDto.getIfFindAuxiliary() != null && accountSubjectDto.getIfFindAuxiliary().equals(Constant.Is.YES)) {
            if (CollectionUtils.isNotEmpty(list)) {
                //封装查询辅助核算信息的参数
                List<Long> ids = list.stream().map(AccountSubjectVo::getId).collect(Collectors.toList());
                accountSubjectDto.setIds(ids);
                List<AccountSubjectAuxiliary> auxiliaryList = accountSubjectAuxiliaryMapper.findListById(accountSubjectDto);
                if (CollectionUtils.isNotEmpty(auxiliaryList)) {
                    MergeUtil.mergeList(list, auxiliaryList,
                            AccountSubjectVo::getId, AccountSubjectAuxiliary::getAccountSubjectId,
                            (accountSubjectVo, auxiliaryLists) -> accountSubjectVo.setAccountSubjectAuxiliaryList(auxiliaryLists));
                }
            }
        }
        return list;
    }

    /**
     * 根据条件查询单个辅助核算来源表信息(支持总账需求)
     *
     * @param accountSubjectDto
     * @return AccountSubjectVo
     * @author: 周鹏
     * @create: 2019/8/6
     */
    @Override
    public Map<Long, Map<String, Object>> findSourceTableInfo(AccountSubjectDto accountSubjectDto) {
        SysUserVo operator = UserUtils.getUserVo();
        accountSubjectDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        Boolean flag = StringUtil.isEmpty(accountSubjectDto.getSourceTable());
        if (flag) {
            accountSubjectDto.setSourceTable(Constant.TableName.ACCOUNT_SUBJECT);
        }
        Map<Long, Map<String, Object>> resultMap = accountSubjectMapper.findSourceTableInfo(accountSubjectDto);

        if (flag) {
            //查询辅助核算信息
            List<AccountSubjectAuxiliary> auxiliaryList = accountSubjectAuxiliaryMapper.findListById(accountSubjectDto);
            if (CollectionUtils.isNotEmpty(auxiliaryList)) {
                //根据会计科目id分组
                Map<Long, List<AccountSubjectAuxiliary>> collectMap = auxiliaryList.stream().collect
                        (Collectors.groupingBy(e -> e.getAccountSubjectId()));
                for (Map.Entry<Long, Map<String, Object>> entry : resultMap.entrySet()) {
                    for (Long key : collectMap.keySet()) {
                        if (entry.getKey().equals(key)) {
                            entry.getValue().put("accountSubjectAuxiliaryList", collectMap.get(key));
                        }
                    }
                }
            }
        }
        return resultMap;
    }

    /**
     * 根据条件查询所有辅助核算来源表信息(支持总账需求)
     *
     * @param accountSubjectDto
     * @return AccountSubjectVo
     * @author: 周鹏
     * @create: 2019/8/31
     */
    @Override
    public List<List<Map<String, Object>>> findAllSourceTableInfo(AccountSubjectDto accountSubjectDto) {
        SysUserVo operator = UserUtils.getUserVo();
        //最终结果集
        List<List<Map<String, Object>>> finalList = new LinkedList<>();
        //辅助核算来源表查询结果集
        List<Map<String, Object>> resultList;
        List<String> sourceTableList = accountSubjectDto.getSourceTableList();
        List<List<List<Long>>> idLists = accountSubjectDto.getIdLists();
        List<List<List<String>>> codeLists = accountSubjectDto.getCodeLists();
        String sourceTables;
        List<List<Long>> idList = new LinkedList<>();
        List<List<String>> codeList = new LinkedList<>();
        String[] sourceTable;
        Page<Map<String, Object>> page;
        AccountSubjectDto param;
        for (int i = 0; i < sourceTableList.size(); i++) {
            resultList = new LinkedList<>();
            sourceTables = sourceTableList.get(i);
            if (idLists != null && idLists.size() > i) {
                idList = idLists.get(i);
            }
            if (codeLists != null && codeLists.size() > i) {
                codeList = codeLists.get(i);
            }
            sourceTable = sourceTables.split(",");
            for (int j = 0; j < sourceTable.length; j++) {
                param = new AccountSubjectDto();
                param.setRootEnterpriseId(operator.getRootEnterpriseId());
                param.setIfFindUseCompanyDataOnly(accountSubjectDto.getIfFindUseCompanyDataOnly());
                param.setCompanyIds(accountSubjectDto.getCompanyIds());
                param.setBusinessUnitIds(accountSubjectDto.getBusinessUnitIds());
                param.setSourceTable(sourceTable[j]);
                if (idList.size() > j) {
                    param.setIds(idList.get(j));
                }
                if (codeList.size() > j) {
                    param.setCodes(codeList.get(j));
                }
                if (Constant.TableName.STAFF.equals(param.getSourceTable())) {
                    initEntityIdList(param);
                } else if (Constant.TableName.DEPT.equals(param.getSourceTable())) {
                    initEntityIdList(param);
                }
                //根据sourceTable查询列表数据
                page = param.getPage();
                fileService.resetPage(page);
                page = getSourceTablePage(param, page);
                resultList.addAll(page.getRecords());
            }
            finalList.add(resultList);
        }

        return finalList;
    }

    /**
     * 根据条件查询相应辅助核算来源表信息(支持总账需求)
     *
     * @param accountSubjectDto
     * @return AccountSubjectVo
     * @author: 周鹏
     * @create: 2019/8/6
     */
    @Override
    public Page<Map<String, Object>> findSourceTableList(AccountSubjectDto accountSubjectDto) {
        //根据sourceTable查询列表数据
        SysUserVo operator = UserUtils.getUserVo();
        accountSubjectDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        Page<Map<String, Object>> page = accountSubjectDto.getPage();
        if (Constant.TableName.STAFF.equals(accountSubjectDto.getSourceTable())) {
            initEntityIdList(accountSubjectDto);
        } else if (Constant.TableName.DEPT.equals(accountSubjectDto.getSourceTable())) {
            initEntityIdList(accountSubjectDto);
        }
        page = getSourceTablePage(accountSubjectDto, page);
        return page;
    }

    /**
     * 查询辅助核算列表信息
     *
     * @param accountSubjectDto
     * @param page
     * @return
     */
    private Page<Map<String, Object>> getSourceTablePage(AccountSubjectDto accountSubjectDto, Page<Map<String, Object>> page) {
        if (Constant.TableName.STAFF.equals(accountSubjectDto.getSourceTable())) {
            page = accountSubjectMapper.findStaffInfo(page, accountSubjectDto);
        } else if (Constant.TableName.DEPT.equals(accountSubjectDto.getSourceTable())) {
            page = accountSubjectMapper.findDeptInfo(page, accountSubjectDto);
        } else if (Constant.TableName.ACCOUNT_BOOK_ENTITY.equals(accountSubjectDto.getSourceTable())) {
            page = accountSubjectMapper.findAccountBookEntityInfo(page, accountSubjectDto);
        } else if (Constant.TableName.ACCOUNTING_ITEM_VALUE.equals(accountSubjectDto.getSourceTable())) {
            page = accountSubjectMapper.findAccountingItemValueInfo(page, accountSubjectDto);
        } else if (Constant.TableName.PROJECT.equals(accountSubjectDto.getSourceTable())) {
            page = accountSubjectMapper.findProjectInfo(page, accountSubjectDto);
        } else if (Constant.TableName.CUSTOMER.equals(accountSubjectDto.getSourceTable())) {
            page = accountSubjectMapper.findCustomerInfo(page, accountSubjectDto);
        } else if (Constant.TableName.SUPPLIER.equals(accountSubjectDto.getSourceTable())) {
            page = accountSubjectMapper.findSupplierInfo(page, accountSubjectDto);
        } else if (Constant.TableName.BANK_ACCOUNT.equals(accountSubjectDto.getSourceTable())) {
            page = accountSubjectMapper.findBankAccountInfo(page, accountSubjectDto);
        }
        return page;
    }

    /**
     * 根据条件查询会计科目关联的辅助核算项组合(支持总账需求)
     *
     * @param accountSubjectDto
     * @return AccountSubjectVo
     * @author: 周鹏
     * @create: 2019/8/21
     */
    @Override
    public List<AccountSubjectAuxiliaryVo> findAuxiliaryGroup(AccountSubjectDto accountSubjectDto) {
        SysUserVo operator = UserUtils.getUserVo();
        accountSubjectDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        List<AccountSubjectAuxiliaryVo> list = accountSubjectAuxiliaryMapper.findAuxiliaryGroup(accountSubjectDto);
        return list;
    }

    /**
     * 根据会计科目查询辅助核算信息分页列表(支持总账需求)
     *
     * @param accountSubjectAuxiliaryDto
     * @return AccountSubjectAuxiliaryVo
     * @author: 周鹏
     * @create: 2019/8/28
     */
    @Override
    public Page<AccountSubjectAuxiliaryVo> findAuxiliaryPage(AccountSubjectAuxiliaryDto accountSubjectAuxiliaryDto) {
        SysUserVo operator = UserUtils.getUserVo();
        accountSubjectAuxiliaryDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        Page<AccountSubjectAuxiliaryVo> page = accountSubjectAuxiliaryDto.getPage();
        page = accountSubjectAuxiliaryMapper.findAuxiliaryPage(page, accountSubjectAuxiliaryDto);
        return page;
    }

    /**
     * 根据科目表id查询已配置辅助核算项
     *
     * @param subjectAuxiliaryDto
     * @return SubjectAuxiliaryVo
     * @author: 周鹏
     * @create: 2019/8/23
     */
    @Override
    public List<SubjectAuxiliaryVo> findSubjectAuxiliaryList(SubjectAuxiliaryDto subjectAuxiliaryDto) {
        List<SubjectAuxiliaryVo> list = subjectAuxiliaryMapper.findSubjectAuxiliaryList(subjectAuxiliaryDto);
        return list;
    }

    /**
     * 根据条件查询会计科目信息
     *
     * @param dto
     * @return List<AccountSubjectVo>
     * @author: wuweiming
     * @create: 2019/08/09
     */
    @Override
    public List<AccountSubjectVo> findSubjectInfoByParam(AccountSubjectDto dto) {
        return accountSubjectMapper.findSubjectInfoByParam(dto);
    }

    /**
     * 根据code和subjectId查询所有下级科目信息
     *
     * @param accountSubjectDto
     * @return List<AccountSubjectVo>
     * @author: wuweiming
     * @create: 2019/08/06
     */
    @Override
    public List<AccountSubjectVo> findAllChildInfo(AccountSubjectDto accountSubjectDto) {
        SysUserVo operator = UserUtils.getUserVo();
        accountSubjectDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        if (null != accountSubjectDto.getId()) {
            AccountSubject accountSubject = accountSubjectMapper.selectById(accountSubjectDto.getId());
            accountSubjectDto.setCode(accountSubject.getCode());
            accountSubjectDto.setSubjectId(accountSubject.getSubjectId());
            accountSubjectDto.setId(null);
        }
        //根据code和subjectId查询所有未删除的下级科目信息
        List<AccountSubjectVo> list = accountSubjectMapper.findInfoForLedger(accountSubjectDto);
        return list;
    }

    /**
     * 根据条件查询科目表信息
     *
     * @param subjectDto
     * @return Subject
     * @author: 周鹏
     * @create: 2019/8/21
     */
    @Override
    public Subject findSubjectByParam(SubjectDto subjectDto) {
        Subject subject = new Subject();
        SysUserVo operator = UserUtils.getUserVo();
        subject.setId(subjectDto.getId());
        subject.setRootEnterpriseId(operator.getRootEnterpriseId());
        subject.setSubjectId(subjectDto.getSubjectId());
        return subjectMapper.findInfoByParam(subject);
    }

    /**
     * @description: 获取会计科目辅助核算信息
     * @param: [accountSubjectAuxiliaryDto]
     * @return: com.njwd.entity.platform.AccountSubjectAuxiliary
     * @author: xdy
     * @create: 2019-09-03 11:27
     */
    @Override
    public AccountSubjectAuxiliary findAccountSubjectAuxiliary(AccountSubjectAuxiliaryDto accountSubjectAuxiliaryDto) {
        return accountSubjectAuxiliaryMapper.selectById(accountSubjectAuxiliaryDto.getId());
    }

    /**
     * 根据条件查询科目表信息
     *
     * @param subjectDto
     * @return Result
     * @author: 周鹏
     * @create: 2019/11/12
     */
    @Override
    public Subject findSubjectInfo(SubjectDto subjectDto) {
        return subjectMapper.selectById(subjectDto.getId());
    }

    /**
     * Excel 导出
     *
     * @param accountSubjectDto
     * @param response
     * @author: 周鹏
     * @create: 2019/6/12
     */
    @Override
    public void exportExcel(AccountSubjectDto accountSubjectDto, HttpServletResponse response) {
        SysUserVo operator = UserUtils.getUserVo();
        accountSubjectDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        Page<AccountSubjectVo> page = accountSubjectDto.getPage();
        fileService.resetPage(page);
        page = accountSubjectMapper.findPage(page, accountSubjectDto);
        fileService.exportExcel(response, page.getRecords(), MenuCodeConstant.ACCOUNT_SUBJECT, accountSubjectDto.getIsEnterpriseAdmin());
    }

    /**
     * 校验会计科目编码和名称是否重复
     *
     * @param accountSubjectDto
     * @return boolean
     * @author: 周鹏
     * @create: 2019/6/12
     */
    @Override
    public Boolean checkDuplicateInfo(AccountSubjectDto accountSubjectDto) {
        Boolean flag = true;
        List<String> columnList = new LinkedList<>();
        int count;
        StringBuilder message = new StringBuilder("");
        if (StringUtil.isNotEmpty(accountSubjectDto.getCode())) {
            count = accountSubjectMapper.checkDuplicateCode(accountSubjectDto);
            if (count > 0) {
                flag = false;
                columnList.add(Constant.EntityName.CODE);
                message.append(ResultCode.CODE_EXIST.message);
            }
        }
        if (StringUtil.isNotEmpty(accountSubjectDto.getFullName())) {
            count = accountSubjectMapper.checkDuplicateName(accountSubjectDto);
            if (count > 0) {
                flag = false;
                columnList.add(Constant.EntityName.NAME);
                message = StringUtil.isEmpty(message.toString()) ? message.append(ResultCode.NAME_EXIST.message) : message.append(",").append(ResultCode.NAME_EXIST.message);
            }
        }
        accountSubjectDto.setColumnList(columnList);
        accountSubjectDto.setMessage(message.toString());
        return flag;
    }

    /**
     * 校验数据状态
     *
     * @param accountSubjectDto
     * @author: 周鹏
     * @create: 2019/6/12
     */
    private void checkStatus(AccountSubjectDto accountSubjectDto) {
        AccountSubject accountSubject = accountSubjectMapper.selectById(accountSubjectDto.getId());
        if (accountSubject == null) {
            throw new ServiceException(ResultCode.RECORD_NOT_EXIST);
        }
        if (accountSubject.getIsDel().equals(Constant.Is.YES)) {
            throw new ServiceException(ResultCode.IS_DEL);
        }
        if (accountSubject.getIsEnable().equals(Constant.Is.NO)) {
            throw new ServiceException(ResultCode.IS_DISABLE);
        }
        if (accountSubject.getIsFinal().equals(Constant.Is.NO)) {
            throw new ServiceException(ResultCode.IS_NOT_FINAL);
        }
    }

    /**
     * 校验预置科目下是否存在下级预置科目
     *
     * @param accountSubjectDto
     * @author: 周鹏
     * @create: 2019/7/25
     */
    @Override
    public Boolean checkNextInitInfo(AccountSubjectDto accountSubjectDto) {
        Boolean flag = true;
        int count = accountSubjectMapper.checkNextInitInfo(accountSubjectDto);
        if (count > 0) {
            flag = false;
        }
        return flag;
    }

    /**
     * 根据code集合校验批量禁用与反禁用
     *
     * @param accountSubjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/9/3
     */
    @Override
    public Boolean checkUpdateBatch(AccountSubjectDto accountSubjectDto) {
        //最终需要处理的会计科目id集合
        List<Long> idList = accountSubjectMapper.findOperateIdsByParam(accountSubjectDto);
        if (idList.size() > accountSubjectDto.getCodes().size()) {
            return true;
        }
        return false;
    }

    /**
     * 组装核算主体id信息
     *
     * @param accountSubjectDto
     */
    private void initEntityIdList(AccountSubjectDto accountSubjectDto) {
        if (CollectionUtils.isNotEmpty(accountSubjectDto.getBusinessUnitIds())) {
            List<AccountBookEntityVo> entityList = accountSubjectMapper.findEntityIdList(accountSubjectDto);
            //设置公司id和核算主体id的关联关系
            List<Long> companyIds = accountSubjectDto.getCompanyIds();
            List<Map<String, Object>> companyAndEntityList = new LinkedList<>();
            Map<String, Object> map;
            for (int i = 0; i < companyIds.size(); i++) {
                if (CollectionUtils.isEmpty(entityList)) {
                    map = new HashMap<>();
                    map.put("companyId", companyIds.get(i));
                    map.put("entityId", "");
                    companyAndEntityList.add(map);
                } else {
                    for (int j = 0; j < entityList.size(); j++) {
                        if (companyIds.get(i).equals(entityList.get(j).getCompanyId())) {
                            map = new HashMap<>();
                            map.put("companyId", companyIds.get(i));
                            if (Constant.AccountBookEntityInfo.FORM_UNIT.equals(entityList.get(j).getForm())) {
                                map.put("entityId", entityList.get(j).getEntityId());
                            } else {
                                map.put("entityId", "");
                            }
                            companyAndEntityList.add(map);
                        }
                    }
                }
            }
            accountSubjectDto.setCompanyAndEntityList(companyAndEntityList);
        }
    }

    /**
     * 特殊处理现金流量相关信息
     *
     * @param accountSubjectDto
     * @param accountSubject
     */
    private void setCashFlowInfo(AccountSubjectDto accountSubjectDto, AccountSubject accountSubject) {
        accountSubject.setCashFlowId(accountSubjectDto.getCashFlowId() == null ? 0L : accountSubjectDto.getCashFlowId());
        accountSubject.setCashInflowId(accountSubjectDto.getCashInflowId() == null ? 0L : accountSubjectDto.getCashInflowId());
        accountSubject.setCashOutflowId(accountSubjectDto.getCashOutflowId() == null ? 0L : accountSubjectDto.getCashOutflowId());
        accountSubject.setCashInflowCode(accountSubjectDto.getCashInflowCode() == null ? "" : accountSubjectDto.getCashInflowCode());
        accountSubject.setCashOutflowCode(accountSubjectDto.getCashOutflowCode() == null ? "" : accountSubjectDto.getCashOutflowCode());
        accountSubject.setCashInflowName(accountSubjectDto.getCashInflowName() == null ? "" : accountSubjectDto.getCashInflowName());
        accountSubject.setCashOutflowName(accountSubjectDto.getCashOutflowName() == null ? "" : accountSubjectDto.getCashOutflowName());
        accountSubject.setVersion(accountSubjectDto.getVersion());
        accountSubject.setUpdateTime(accountSubjectDto.getUpdateTime());
        accountSubject.setUpdatorId(accountSubjectDto.getUpdatorId());
        accountSubject.setUpdatorName(accountSubjectDto.getUpdatorName());
    }

    /**
     * 设置平台表中现金流量id为基础资料库现金流量项目表对应的id
     *
     * @param list     会计科目信息集合
     * @param operator 登陆人信息
     */
    private void setCashFlowIdInfo(List<AccountSubjectDto> list, SysUserVo operator) {
        List<String> cashInflowCodes = list.stream().map(AccountSubjectDto::getCashInflowCode).collect(Collectors.toList());
        cashInflowCodes.removeAll(Collections.singleton(null));
        List<String> cashOutflowCodes = list.stream().map(AccountSubjectDto::getCashOutflowCode).collect(Collectors.toList());
        cashOutflowCodes.removeAll(Collections.singleton(null));
        CashFlowItemDto cashFlowItemDto = new CashFlowItemDto();
        cashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        cashFlowItemDto.setCodes(cashInflowCodes);
        List<CashFlowItemVo> cashInflowInfoList = cashFlowItemService.findCashFlowItemForReport(cashFlowItemDto);
        cashFlowItemDto.setCodes(cashOutflowCodes);
        List<CashFlowItemVo> cashOutflowInfoList = cashFlowItemService.findCashFlowItemForReport(cashFlowItemDto);
        //根据cashInflowCode设置cashInflowId,cashInflowCode和cashFlowId
        MergeUtil.mergeList(list, cashInflowInfoList,
                (accountSubjectDto, cashFlowItemVo) -> accountSubjectDto.getCashInflowCode() != null &&
                        accountSubjectDto.getCashInflowCode().equals(cashFlowItemVo.getCode()),
                (accountSubjectDto, CashFlowItemVoList) -> {
                    if (CashFlowItemVoList.size() > 0) {
                        //存在现金流量信息,设置cashInflowId和cashFlowId为基础资料库现金流量项目表的id
                        accountSubjectDto.setCashInflowId(CashFlowItemVoList.get(0).getId());
                        accountSubjectDto.setCashFlowId(CashFlowItemVoList.get(0).getCashFlowId());
                    } else {
                        //不存在现金流量信息,设置cashInflowId,cashInflowCode和cashFlowId为null
                        accountSubjectDto.setCashInflowId(null);
                        accountSubjectDto.setCashInflowCode(null);
                        accountSubjectDto.setCashFlowId(null);
                    }
                });
        //根据cashOutflowCode设置cashOutflowId,cashOutflowCode和cashFlowId
        MergeUtil.mergeList(list, cashOutflowInfoList,
                (accountSubjectDto, cashFlowItemVo) -> accountSubjectDto.getCashOutflowCode() != null &&
                        accountSubjectDto.getCashOutflowCode().equals(cashFlowItemVo.getCode()),
                (accountSubjectDto, CashFlowItemVoList) -> {
                    if (CashFlowItemVoList.size() > 0) {
                        //存在现金流量信息,设置cashOutflowId和cashFlowId为基础资料库现金流量项目表的id
                        accountSubjectDto.setCashOutflowId(CashFlowItemVoList.get(0).getId());
                        accountSubjectDto.setCashFlowId(CashFlowItemVoList.get(0).getCashFlowId());
                    } else {
                        //不存在现金流量信息,设置cashOutflowId,cashOutflowCode为null
                        accountSubjectDto.setCashOutflowId(null);
                        accountSubjectDto.setCashOutflowCode(null);
                    }
                });
    }

}
