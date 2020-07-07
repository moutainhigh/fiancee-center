package com.njwd.excel;

import com.njwd.annotation.ExcelExtend;
import com.njwd.basedata.mapper.AccountSubjectMapper;
import com.njwd.basedata.mapper.CashFlowItemMapper;
import com.njwd.basedata.mapper.SubjectMapper;
import com.njwd.basedata.service.AccountSubjectService;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.ReferenceContext;
import com.njwd.entity.basedata.dto.SubjectAuxiliaryDto;
import com.njwd.entity.basedata.vo.SubjectAuxiliaryVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.Subject;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.dto.CashFlowDto;
import com.njwd.entity.platform.dto.CashFlowItemDto;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.entity.platform.vo.CashFlowItemVo;
import com.njwd.entity.platform.vo.CashFlowVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.fileexcel.check.CheckContext;
import com.njwd.fileexcel.check.CheckResult;
import com.njwd.fileexcel.extend.AddExtend;
import com.njwd.fileexcel.extend.CheckExtend;
import com.njwd.fileexcel.extend.CheckHandler;
import com.njwd.service.ReferenceRelationService;
import com.njwd.utils.StringUtil;
import com.njwd.utils.UserUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @description: 校验会计科目导入模板参数并入库
 * @author: 周鹏
 * @create: 2019/6/18 11:54
 */
@Component
@ExcelExtend(type = "account_subject")
public class AccountSubjectExtend implements AddExtend<AccountSubjectDto>, CheckExtend {

    @Autowired
    private AccountSubjectService accountSubjectService;

    @Autowired
    private ReferenceRelationService referenceRelationService;

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private AccountSubjectMapper accountSubjectMapper;

    @Autowired
    private CashFlowItemMapper cashFlowItemMapper;

    public final Logger logger;

    {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public int addBatch(List<AccountSubjectDto> list) {
        int result = 0;
        if (CollectionUtils.isNotEmpty(list)) {
            for (AccountSubjectDto accountSubjectDto : list) {
                result = accountSubjectService.add(accountSubjectDto);
            }
        }
        return result;
    }

    @Override
    public int add(AccountSubjectDto accountSubjectDto) {
        return accountSubjectService.add(accountSubjectDto);
    }

    @Override
    public void check(CheckContext checkContext) {
        Long subjectId = checkContext.getLongValue("subjectId");
        checkContext.addSheetHandler("code", getCodeCheckHandler(subjectId))
                .addSheetHandler("name", getNameCheckHandler())
                .addSheetHandler("cashInflowCode", getCashInflowCodeCheckHandler())
                .addSheetHandler("cashOutflowCode", getCashOutflowCodeCheckHandler())
                .addSheetHandler("auxiliaryCodes", getAuxiliaryCodesCheckHandler());
    }

    /**
     * 扩展code规则校验
     *
     * @return
     */
    private CheckHandler<AccountSubjectDto> getCodeCheckHandler(Long subjectId) {
        return (data) -> {
            data.setSubjectId(subjectId);
            String message = this.checkCode(data);
            /*try {
                message = RedisUtils.lock(String.format(Constant.LockKey.ACCOUNT_SUBJECT_CODE, data.getCode()),
                        Constant.SysConfig.REDIS_LOCK_TIMEOUT, () -> this.checkCode(data));
            } catch (Exception e) {
                logger.error(e.toString());
            }*/
            if (StringUtil.isNotEmpty(message)) {
                return CheckResult.error(message);
            }
            return CheckResult.ok();
        };
    }

    /**
     * 校验科目编码和上级科目编码
     *
     * @param accountSubjectDto
     * @return 校验结果
     * @author: 周鹏
     * @create: 2019/6/24
     */
    public String checkCode(AccountSubjectDto accountSubjectDto) {
        String message = "";
        int count;
        String code = accountSubjectDto.getCode();
        String upCode = accountSubjectDto.getUpCode();
        SysUserVo operator = UserUtils.getUserVo();
        //查询科目表信息
        Subject subject = new Subject();
        subject.setRootEnterpriseId(operator.getRootEnterpriseId());
        subject.setSubjectId(accountSubjectDto.getSubjectId());
        Subject subjectInfo = subjectMapper.findInfoByParam(subject);
        if (subjectInfo == null) {
            //科目表不存在
            message = ResultCode.SUBJECT_NOT_EXIST.message;
            return message;
        }
        accountSubjectDto.setSubjectId(subjectInfo.getId());
        //查询科目编码是否存在
        accountSubjectDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        count = accountSubjectMapper.checkDuplicateCode(accountSubjectDto);
        if (count > 0) {
            message = ResultCode.SUBJECT_CODE_EXIST.message;
            return message;
        }
        //查询上级科目是否存在
        List<AccountSubjectVo> list = accountSubjectMapper.findByParam(accountSubjectDto);
        //去除null记录
        list.removeAll(Collections.singleton(null));
        AccountSubjectVo upInfo;
        if (list.size() == 0) {
            message = ResultCode.UP_ACCOUNT_SUBJECT_NOT_EXIST.message;
            return message;
        } else if (list.size() > 1) {
            //存在多条编码相同的数据
            message = ResultCode.UP_CODE_ONE_TO_MANY.message;
            return message;
        } else {
            upInfo = list.get(0);
            if (Constant.Is.NO.equals(upInfo.getIsEnable())) {
                throw new ServiceException(ResultCode.UP_ACCOUNT_SUBJECT_DISABLE);
            }
        }
        //如果上级科目是预置数据,判断该上级科目下是否已存在下级预置数据
        if (upInfo.getIsInit().equals(Constant.Is.YES)) {
            Boolean flag = accountSubjectService.checkNextInitInfo(accountSubjectDto);
            if (!flag) {
                throw new ServiceException(ResultCode.ADD_UNDER_INIT_DATA);
            }
        }
        //校验科目编码最大长度
        String maxLevel = upInfo.getMaxLevel();
        String[] levels = maxLevel.split("-");
        int maxLength = 0;
        for (String level : levels) {
            maxLength += Integer.valueOf(level);
        }
        if (code.length() > maxLength) {
            message = "科目编码不能超过" + maxLevel + "个字符";
            return message;
        }
        //获取与上级科目编码的位数差
        Integer digit = Integer.valueOf(levels[upInfo.getLevel()]);
        //校验科目编码和上级科目编码间的关系
        Boolean flag = code.length() - upCode.length() != digit || !upCode.equals(code.substring(0, upCode.length()));
        if (flag) {
            message = ResultCode.CODE_AND_UP_CODE_RULE_WRONG.message;
            return message;
        }
        //校验科目编码是否以00结尾
        String digitCode = code.substring(upCode.length());
        if (digitCode.equals(Constant.Character.ZERO)) {
            message = ResultCode.WRONG_CODE.message;
            return message;
        }
        //校验上级科目是否被引用
        List<Long> ids = new ArrayList<>();
        ids.add(upInfo.getId());
        ReferenceContext referenceContext = referenceRelationService.isReference(Constant.Reference.ACCOUNT_SUBJECT, ids);
        if (!referenceContext.getReferences().isEmpty()) {
            message = referenceContext.getReferences().get(0).getReferenceDescription();
            return message;
        }
        //查询此会计准测下的现金流量表信息
        CashFlowDto cashFlowDto = new CashFlowDto();
        cashFlowDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        cashFlowDto.setAccStandardId(subjectInfo.getAccStandardId());
        CashFlowVo cashFlowVo = cashFlowItemMapper.findCashFlowInfo(cashFlowDto);
        if (cashFlowVo != null) {
            accountSubjectDto.setCashFlowId(cashFlowVo.getId());
        }
        //校验通过,组装科目信息
        initAccountSubjectInfo(accountSubjectDto, operator, upInfo);
        return message;
    }

    /**
     * 组装会计科目信息
     *
     * @param accountSubjectDto 会计科目信息
     * @param operator          登陆人信息
     * @param upInfo            上级科目信息
     */
    private void initAccountSubjectInfo(AccountSubjectDto accountSubjectDto, SysUserVo operator, AccountSubjectVo upInfo) {
        //TODO 目前按照集团创建,集团共享的控制策略，如后面有新的策略，则按新的策略设置值
        accountSubjectDto.setCreateCompanyId(Constant.AccountSubjectData.GROUP_ID);
        accountSubjectDto.setCompanyId(Constant.AccountSubjectData.GROUP_ID);
        accountSubjectDto.setUseCompanyId(Constant.AccountSubjectData.GROUP_ID);
        //设置登陆人信息
        accountSubjectDto.setCreatorId(operator.getUserId());
        accountSubjectDto.setCreatorName(operator.getName());
        //从上级会计科目取其他信息
        Byte level = (byte) (upInfo.getLevel() + 1);
        accountSubjectDto.setFullName(upInfo.getFullName() + Constant.Symbol.UNDERLINE + accountSubjectDto.getName());
        accountSubjectDto.setLevel(level);
        accountSubjectDto.setAccountElementItem(upInfo.getAccountElementItem());
        accountSubjectDto.setAccountElementItemName(upInfo.getAccountElementItemName());
        accountSubjectDto.setIsProfitAndLoss(upInfo.getIsProfitAndLoss());
        accountSubjectDto.setIsOffBalance(upInfo.getIsOffBalance());
        accountSubjectDto.setSubjectCategory(upInfo.getSubjectCategory());
        accountSubjectDto.setSubjectCategoryName(upInfo.getSubjectCategoryName());
        accountSubjectDto.setAccountCategory(upInfo.getAccountCategory());
        accountSubjectDto.setIsInterior(upInfo.getIsInterior());
        accountSubjectDto.setCurrencyIds(upInfo.getCurrencyIds());
        accountSubjectDto.setCurrencyNames(upInfo.getCurrencyNames());
    }

    /**
     * 扩展name规则校验
     *
     * @return
     */
    private CheckHandler<AccountSubjectDto> getNameCheckHandler() {
        return (data) -> {
            /*String message = RedisUtils.lock(String.format(Constant.LockKey.ACCOUNT_SUBJECT_NAME, data.getName()),
                    Constant.SysConfig.REDIS_LOCK_TIMEOUT, () -> this.checkName(data));*/
            String message = this.checkName(data);
            if (StringUtil.isNotEmpty(message)) {
                return CheckResult.error(message);
            }
            return CheckResult.ok();
        };
    }

    /**
     * 校验科目名称
     *
     * @param accountSubjectDto
     * @return 校验结果
     * @author: 周鹏
     * @create: 2019/6/24
     */
    public String checkName(AccountSubjectDto accountSubjectDto) {
        String message = "";
        int count = accountSubjectMapper.checkDuplicateName(accountSubjectDto);
        if (count > 0) {
            message = ResultCode.SUBJECT_NAME_EXIST.message;
        }
        return message;
    }

    /**
     * 扩展cashInflowCode规则校验
     *
     * @return
     */
    private CheckHandler<AccountSubjectDto> getCashInflowCodeCheckHandler() {
        return (data) -> {
            if (StringUtil.isNotEmpty(data.getCashInflowCode())) {
                if (StringUtil.isEmpty(data.getCashFlowId().toString())) {
                    return CheckResult.error(ResultCode.CASH_FLOW_NOT_EXIST.message);
                }
                CashFlowItemDto cashFlowItemDto = new CashFlowItemDto();
                cashFlowItemDto.setCashFlowId(data.getCashFlowId());
                cashFlowItemDto.setCode(data.getCashInflowCode());
                //查询现金流入信息
                List<CashFlowItemVo> inList = cashFlowItemMapper.findListByParam(cashFlowItemDto);
                //去除null记录
                inList.removeAll(Collections.singleton(null));
                if (inList.size() == 0) {
                    return CheckResult.error(ResultCode.CASH_INFLOW_NOT_EXIST.message);
                }
                if (inList.size() > 1) {
                    return CheckResult.error(ResultCode.CASH_INFLOW_ONE_TO_MANY.message);
                }
                CashFlowItemVo info = inList.get(0);
                if (!Constant.CashFlowItemData.INFLOW_DIRECTION.equals((info.getCashFlowDirection()))) {
                    return CheckResult.error(ResultCode.CASH_INFLOW_WRONG_DIRECTION.message);
                }
                if (!Constant.Is.YES.equals((info.getIsEnable()))) {
                    return CheckResult.error(ResultCode.IS_DISABLE.message);
                }
                if (!Constant.Is.YES.equals((info.getIsFinal()))) {
                    return CheckResult.error(ResultCode.IS_NOT_FINAL.message);
                }
                if (StringUtil.isNotEmpty(data.getCashInflowName()) && !data.getCashInflowName().equals(info.getName())) {
                    return CheckResult.error(ResultCode.CASH_INFLOW_CODE_NAME_NOT_MATCH.message);
                }
            }
            return CheckResult.ok();
        };
    }

    /**
     * 扩展cashOutflowCode规则校验
     *
     * @return
     */
    private CheckHandler<AccountSubjectDto> getCashOutflowCodeCheckHandler() {
        return (data) -> {
            if (StringUtil.isNotEmpty(data.getCashOutflowCode())) {
                if (StringUtil.isEmpty(data.getCashFlowId().toString())) {
                    return CheckResult.error(ResultCode.CASH_FLOW_NOT_EXIST.message);
                }
                CashFlowItemDto cashFlowItemDto = new CashFlowItemDto();
                cashFlowItemDto.setCashFlowId(data.getCashFlowId());
                cashFlowItemDto.setCode(data.getCashOutflowCode());
                //查询现金流入信息
                List<CashFlowItemVo> outList = cashFlowItemMapper.findListByParam(cashFlowItemDto);
                //去除null记录
                outList.removeAll(Collections.singleton(null));
                if (outList.size() == 0) {
                    return CheckResult.error(ResultCode.CASH_OUTFLOW_NOT_EXIST.message);
                }
                if (outList.size() > 1) {
                    return CheckResult.error(ResultCode.CASH_OUTFLOW_ONE_TO_MANY.message);
                }
                CashFlowItemVo info = outList.get(0);
                if (!Constant.CashFlowItemData.OUTFLOW_DIRECTION.equals((info.getCashFlowDirection()))) {
                    return CheckResult.error(ResultCode.CASH_OUTFLOW_WRONG_DIRECTION.message);
                }
                if (!Constant.Is.YES.equals((info.getIsEnable()))) {
                    return CheckResult.error(ResultCode.IS_DISABLE.message);
                }
                if (!Constant.Is.YES.equals((info.getIsFinal()))) {
                    return CheckResult.error(ResultCode.IS_NOT_FINAL.message);
                }
                if (StringUtil.isNotEmpty(data.getCashOutflowName()) && !data.getCashOutflowName().equals(info.getName())) {
                    return CheckResult.error(ResultCode.CASH_OUTFLOW_CODE_NAME_NOT_MATCH.message);
                }
            }
            return CheckResult.ok();
        };
    }

    /**
     * 扩展auxiliaryNames规则校验
     *
     * @return
     */
    private CheckHandler<AccountSubjectDto> getAuxiliaryCodesCheckHandler() {
        return (data) -> {
            if (StringUtil.isNotEmpty(data.getAuxiliaryCodes())) {
                //获取已配置辅助核算项
                SubjectAuxiliaryDto subjectAuxiliaryDto = new SubjectAuxiliaryDto();
                subjectAuxiliaryDto.setSubjectId(data.getSubjectId());
                subjectAuxiliaryDto.setCodes(data.getAuxiliaryCodes());
                List<SubjectAuxiliaryVo> list = accountSubjectService.findSubjectAuxiliaryList(subjectAuxiliaryDto);
                String[] auxiliaryCodes = data.getAuxiliaryCodes().split(",", -1);
                if (list.size() < auxiliaryCodes.length) {
                    return CheckResult.error(ResultCode.AUXILIARY_NOT_EXIST.message);
                }
                if (list.size() > auxiliaryCodes.length) {
                    return CheckResult.error(ResultCode.AUXILIARY_ONE_TO_MANY.message);
                }
                //校验通过,组装辅助核算信息
                StringBuilder auxiliaryNames = new StringBuilder();
                StringBuilder auxiliarySourceTables = new StringBuilder();
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) {
                        auxiliaryNames.append(",");
                        auxiliarySourceTables.append(",");
                    }
                    auxiliaryNames.append(list.get(i).getName());
                    auxiliarySourceTables.append(list.get(i).getSourceTable());
                }
                data.setAuxiliaryNames(auxiliaryNames.toString());
                data.setAuxiliarySourceTables(auxiliarySourceTables.toString());
            }
            return CheckResult.ok();
        };
    }

}
