package com.njwd.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.ExcelColumnConstant;
import com.njwd.common.ExcelDataConstant;
import com.njwd.common.LedgerConstant;
import com.njwd.entity.basedata.AccountBookSystem;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.AccountBookEntityDto;
import com.njwd.entity.basedata.dto.AccountBookSystemDto;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.basedata.vo.AccountBookSystemVo;
import com.njwd.entity.basedata.vo.AccountBookVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.*;
import com.njwd.entity.ledger.dto.BalanceCashFlowInitDto;
import com.njwd.entity.ledger.dto.BalanceInitRecordDto;
import com.njwd.entity.ledger.vo.BalanceCashFlowInitVo;
import com.njwd.entity.ledger.vo.BalanceInitRecordVo;
import com.njwd.entity.platform.dto.CashFlowItemDto;
import com.njwd.entity.platform.vo.CashFlowItemVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.ledger.cloudclient.AccountBookEntityFeignClient;
import com.njwd.ledger.cloudclient.AccountBookFeignClient;
import com.njwd.ledger.cloudclient.AccountBookSystemFeignClient;
import com.njwd.ledger.cloudclient.CashFlowReportClient;
import com.njwd.ledger.mapper.*;
import com.njwd.ledger.service.BalanceCashFlowInitService;
import com.njwd.service.FileService;
import com.njwd.utils.MergeUtil;
import com.njwd.utils.ShiroUtils;
import com.njwd.utils.UserUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author lj
 * @Description 现金流量期初接口
 * @Date: 2019/8/8 10:10
 **/
@Service
public class BalanceCashFlowInitServiceImpl implements BalanceCashFlowInitService {

    @Resource
    private BalanceCashFlowInitMapper balanceCashFlowInitMapper;

    @Resource
    private BalanceInitRecordMapper balanceInitRecordMapper;

    @Resource
    private BalanceSubjectInitMapper balanceSubjectInitMapper;

    @Resource
    private BalanceSubjectInitAuxiliaryMapper balanceSubInitAuxiliaryMapper;

    @Resource
    private BalanceSubjectInitAuxiliaryItemMapper balanceSubInitAuxItemMapper;

    @Resource
    private AccountBookFeignClient accountBookFeignClient;

    @Resource
    private AccountBookSystemFeignClient accountBookSystemFeignClient;

    @Resource
    private AccountBookEntityFeignClient accountBookEntityFeignClient;

    @Resource
    private CashFlowReportClient cashFlowReportClient;

    @Resource
    private FileService fileService;

    /**
     * 添加核算主体
     *
     * @param balanceInitRecordDto
     * @return int
     * @Author lj
     * @Date:10:10 2019/8/2
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addBalanceInitRecordBatch(BalanceInitRecordDto balanceInitRecordDto) {
        SysUserVo user = UserUtils.getUserVo();
        List<BalanceInitRecordDto> balanceInitRecordDtos = balanceInitRecordDto.getBalanceInitRecordDtos();
        int result = Constant.Number.ONE;
        //校验核算主体是否存在
        if (CollectionUtils.isNotEmpty(balanceInitRecordDtos)) {
            BalanceInitRecordDto record = balanceInitRecordDtos.get(Constant.Number.ZERO);
            Byte cashFlowStatus = getCashFlowStatus(record.getAccountBookId());
            List<BalanceInitRecord> balanceInitRecordList = balanceInitRecordMapper.selectList(new LambdaQueryWrapper<BalanceInitRecord>()
                    .eq(BalanceInitRecord::getAccountBookId, record.getAccountBookId()));
            if(CollectionUtils.isNotEmpty(balanceInitRecordList) &&LedgerConstant.SubjectStatus.INIT_ED.equals(balanceInitRecordList.get(Constant.Number.ZERO).getSubjectStatus())) {
                throw new ServiceException(ResultCode.BALANCE_INIT_RECORD_INIT);
            }
            Map<Long, List<BalanceInitRecord>> recordMap = balanceInitRecordList.stream().collect(Collectors.groupingBy(BalanceInitRecord::getEntityId));
            for (BalanceInitRecordDto br : balanceInitRecordDtos) {
                //核算主体已存在
                if (recordMap.containsKey(br.getEntityId())) {
                    throw new ServiceException(ResultCode.BALANCE_INIT_RECORD_EXIST);
                }
                //簿启用子系统记录表-现金流量启用状态=是
                if (!Constant.Is.YES.equals(cashFlowStatus)) {
                    br.setCashStatus(LedgerConstant.CashStatus.UNUSED);
                } else {
                    br.setCashStatus(LedgerConstant.CashStatus.UN_RECORD);
                }
                br.setRootEnterpriseId(user.getRootEnterpriseId());
            }
            //批量添加数据
            result = balanceInitRecordMapper.addBalanceInitRecordBatch(balanceInitRecordDtos);
        }
        return result;
}

    /**
     * 获取现金流量状态
     *
     * @param accountBookId
     * @return java.lang.Byte
     * @Author lj
     * @Date:14:10 2019/8/29
     **/
    private Byte getCashFlowStatus(Long accountBookId) {
        AccountBookSystemDto accountBookSystemDto = new AccountBookSystemDto();
        accountBookSystemDto.setAccountBookId(accountBookId);
        accountBookSystemDto.setSystemSign(Constant.SystemSignValue.LEDGER);
        List<AccountBookSystemVo> accountBookSystemVos = accountBookSystemFeignClient.findEnableList(accountBookSystemDto).getData();
        Byte cashFlowStatus = null;
        if (!org.springframework.util.CollectionUtils.isEmpty(accountBookSystemVos)) {
            AccountBookSystemVo accountBookSystemVo = accountBookSystemVos.get(Constant.Number.ZERO);
            cashFlowStatus = accountBookSystemVo.getCashFlowEnableStatus();
        }
        return cashFlowStatus;
    }

    /**
     * 获取账簿状态
     *
     * @param accountBookId
     * @return
     * @Author lj
     * @Date:14:10 2019/8/29
     **/
    private void checkAccountBookStatus(Long accountBookId) {
        //查询数据是否已经被删除
        List<BalanceInitRecord> balanceInitRecordList = balanceInitRecordMapper.selectList(new LambdaQueryWrapper<BalanceInitRecord>()
                .eq(BalanceInitRecord::getAccountBookId, accountBookId));
        if(CollectionUtils.isEmpty(balanceInitRecordList)){
            throw new ServiceException(ResultCode.BALANCE_INIT_RECORD_DELETED);
        }else if(LedgerConstant.SubjectStatus.INIT_ED.equals(balanceInitRecordList.get(Constant.Number.ZERO).getSubjectStatus())) {
            throw new ServiceException(ResultCode.BALANCE_INIT_RECORD_INIT);
        }
    }

    /**
     * 现金流量期初录入
     *
     * @param balanceCashFlowInitDto
     * @return int
     * @Author lj
     * @Date:10:10 2019/8/2
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addCashFlowInitBatch(BalanceCashFlowInitDto balanceCashFlowInitDto) {
        SysUserVo user = UserUtils.getUserVo();
        List<BalanceCashFlowInitDto> balanceCashFlowInits = balanceCashFlowInitDto.getBalanceCashFlowInits();

        //批量移除openingBalance为NULL的数据
        BigDecimal zore = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
        List<BalanceCashFlowInitDto> balanceCashFlowInitList = new ArrayList<BalanceCashFlowInitDto>();
        //插入前先清空
        if (CollectionUtils.isNotEmpty(balanceCashFlowInits)) {
            checkAccountBookStatus(balanceCashFlowInits.get(Constant.Number.ZERO).getAccountBookId());
            balanceCashFlowInitMapper.delete(new LambdaQueryWrapper<BalanceCashFlowInit>()
                    .eq(BalanceCashFlowInit::getAccountBookEntityId, balanceCashFlowInits.get(Constant.Number.ZERO).getAccountBookEntityId())
                    .eq(BalanceCashFlowInit::getPeriodYear, balanceCashFlowInits.get(Constant.Number.ZERO).getPeriodYear())
                    .eq(BalanceCashFlowInit::getPeriodNum, balanceCashFlowInits.get(Constant.Number.ZERO).getPeriodNum()));
            balanceCashFlowInitList = balanceCashFlowInits.stream().filter(t -> t.getOpeningBalance() != null && zore.compareTo(t.getOpeningBalance()) != Constant.Number.ZERO && Constant.Is.YES.equals(t.getIsFinal())).collect(Collectors.toList());
        }
        int result = Constant.Number.ONE;
        if (CollectionUtils.isNotEmpty(balanceCashFlowInitList)) {
            result = balanceCashFlowInitMapper.addCashFlowInitBatch(balanceCashFlowInitList);
        }
        //更新核算主体记录表现金流量状态
        BalanceInitRecord balanceInitRecord = new BalanceInitRecord();
        balanceInitRecord.setCashStatus(LedgerConstant.CashStatus.RECORDED);
        balanceInitRecordMapper.update(balanceInitRecord,new LambdaQueryWrapper<BalanceInitRecord>()
                .eq(BalanceInitRecord::getEntityId,balanceCashFlowInits.get(Constant.Number.ZERO).getAccountBookEntityId())
                .eq(BalanceInitRecord::getPeriodYear, balanceCashFlowInits.get(Constant.Number.ZERO).getPeriodYear())
                .eq(BalanceInitRecord::getRootEnterpriseId, user.getRootEnterpriseId())
                .eq(BalanceInitRecord::getPeriodNum, balanceCashFlowInits.get(Constant.Number.ZERO).getPeriodNum()));
        return result;
    }

    /**
     * 获取现金流量分组
     *
     * @param cashFlowCode
     * @return java.lang.Boolean
     * @Author lj
     * @Date:14:12 2019/8/29
     **/
    private Boolean isCashFlowGroup(String cashFlowCode) {
        Boolean flag = false;
        for (String code : LedgerConstant.CashFlowGroup.CASH_FLOW_GROUPS.split(Constant.Character.COMMA)) {
            if (code.equals(cashFlowCode)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * 自动层层汇总至上级现金流量项目
     *
     * @param balCashFlowMap,code
     * @return java.math.BigDecimal
     * @Author lj
     * @Date:10:13 2019/8/9
     **/
    private BigDecimal getCashFlowMoney(Map<String, BigDecimal> balCashFlowMap, String code) {
        BigDecimal result = null;
        for (Map.Entry<String, BigDecimal> entry : balCashFlowMap.entrySet()) {
            //根据末级科目code去计算所有包含当前code的本年已发生额
            if (entry.getKey().startsWith(code)) {
                if (result == null) {
                    result = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
                }
                result = result.add(entry.getValue());
            }
        }
        return result;
    }

    /**
     * 根据核算主体ID清空期初数据
     *
     * @param balanceCashFlowInitDto
     * @return int
     * @Author lj
     * @Date:15:42 2019/7/25
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBalCashFlowBatch(BalanceCashFlowInitDto balanceCashFlowInitDto) {
        checkAccoutBookStatusTwo(balanceCashFlowInitDto);
        SysUserVo user = UserUtils.getUserVo();
        int flag;
        //清空现金流量期初数据
        flag = balanceCashFlowInitMapper.delete(new LambdaQueryWrapper<BalanceCashFlowInit>()
                .eq(BalanceCashFlowInit::getAccountBookEntityId, balanceCashFlowInitDto.getAccountBookEntityId())
                .eq(BalanceCashFlowInit::getPeriodYear, balanceCashFlowInitDto.getPeriodYear())
                .eq(BalanceCashFlowInit::getPeriodNum, balanceCashFlowInitDto.getPeriodNum()));
        //更新核算主体记录表现金流量状态
        BalanceInitRecord balanceInitRecord = new BalanceInitRecord();
        balanceInitRecord.setCashStatus(LedgerConstant.CashStatus.UN_RECORD);
        balanceInitRecordMapper.update(balanceInitRecord,new LambdaQueryWrapper<BalanceInitRecord>()
                .eq(BalanceInitRecord::getEntityId,balanceCashFlowInitDto.getAccountBookEntityId())
                .eq(BalanceInitRecord::getPeriodYear, balanceCashFlowInitDto.getPeriodYear())
                .eq(BalanceInitRecord::getRootEnterpriseId, user.getRootEnterpriseId())
                .eq(BalanceInitRecord::getPeriodNum, balanceCashFlowInitDto.getPeriodNum()));
        return flag;
    }

    private void checkAccoutBookStatusTwo(BalanceCashFlowInitDto balanceCashFlowInitDto) {
        //查询数据是否已经被删除
        List<BalanceInitRecord> balanceInitRecordList = balanceInitRecordMapper.selectList(new LambdaQueryWrapper<BalanceInitRecord>()
                .eq(BalanceInitRecord::getEntityId, balanceCashFlowInitDto.getAccountBookEntityId())
                .eq(BalanceInitRecord::getPeriodYear, balanceCashFlowInitDto.getPeriodYear())
                .eq(BalanceInitRecord::getPeriodNum, balanceCashFlowInitDto.getPeriodNum()));
        if(CollectionUtils.isEmpty(balanceInitRecordList)){
            throw new ServiceException(ResultCode.BALANCE_INIT_RECORD_DELETED);
        }else if(LedgerConstant.SubjectStatus.INIT_ED.equals(balanceInitRecordList.get(Constant.Number.ZERO).getSubjectStatus())) {
            throw new ServiceException(ResultCode.BALANCE_INIT_RECORD_INIT);
        }
    }

    /**
     * 删除核算主体
     *
     * @param balanceInitRecordDto
     * @return int
     * @Author lj
     * @Date:15:42 2019/7/25
     **/
    @Override
    public int deleteBalanceInitRecord(BalanceInitRecordDto balanceInitRecordDto) {
        SysUserVo user = UserUtils.getUserVo();
        //查询数据是否已经被删除
        BalanceInitRecord balanceInitRecord = balanceInitRecordMapper.selectOne(new LambdaQueryWrapper<BalanceInitRecord>()
                .eq(BalanceInitRecord::getEntityId, balanceInitRecordDto.getEntityId())
                .eq(BalanceInitRecord::getPeriodYear, balanceInitRecordDto.getPeriodYear())
                .eq(BalanceInitRecord::getRootEnterpriseId, user.getRootEnterpriseId())
                .eq(BalanceInitRecord::getPeriodNum, balanceInitRecordDto.getPeriodNum()));
        if(balanceInitRecord==null){
            throw new ServiceException(ResultCode.BALANCE_INIT_RECORD_DELETED);
        }else if(LedgerConstant.SubjectStatus.INIT_ED.equals(balanceInitRecord.getSubjectStatus())) {
            throw new ServiceException(ResultCode.BALANCE_INIT_RECORD_INIT);
        }
        int flag;
        //删除核算主体
        flag = balanceInitRecordMapper.delete(new LambdaQueryWrapper<BalanceInitRecord>()
                .eq(BalanceInitRecord::getEntityId, balanceInitRecordDto.getEntityId())
                .eq(BalanceInitRecord::getPeriodYear, balanceInitRecordDto.getPeriodYear())
                .eq(BalanceInitRecord::getPeriodNum, balanceInitRecordDto.getPeriodNum()));
        //清空现金流量期初数据
        balanceCashFlowInitMapper.delete(new LambdaQueryWrapper<BalanceCashFlowInit>()
                .eq(BalanceCashFlowInit::getAccountBookEntityId, balanceInitRecordDto.getEntityId())
                .eq(BalanceCashFlowInit::getPeriodYear, balanceInitRecordDto.getPeriodYear())
                .eq(BalanceCashFlowInit::getPeriodNum, balanceInitRecordDto.getPeriodNum()));
        //清空科目期初数据
        balanceSubjectInitMapper.delete(new LambdaQueryWrapper<BalanceSubjectInit>()
                .eq(BalanceSubjectInit::getAccountBookEntityId, balanceInitRecordDto.getEntityId())
                .eq(BalanceSubjectInit::getPeriodYear, balanceInitRecordDto.getPeriodYear())
                .eq(BalanceSubjectInit::getPeriodNum, balanceInitRecordDto.getPeriodNum()));
        //清空科目期初辅助核算余额数据
        balanceSubInitAuxiliaryMapper.delete(new LambdaQueryWrapper<BalanceSubjectInitAuxiliary>()
                .eq(BalanceSubjectInitAuxiliary::getAccountBookEntityId, balanceInitRecordDto.getEntityId())
                .eq(BalanceSubjectInitAuxiliary::getPeriodYear, balanceInitRecordDto.getPeriodYear())
                .eq(BalanceSubjectInitAuxiliary::getPeriodNum, balanceInitRecordDto.getPeriodNum()));
        //清空科目期初辅助核算初始化项目数据
        balanceSubInitAuxItemMapper.delete(new LambdaQueryWrapper<BalanceSubjectInitAuxiliaryItem>()
                .eq(BalanceSubjectInitAuxiliaryItem::getAccountBookEntityId, balanceInitRecordDto.getEntityId())
                .eq(BalanceSubjectInitAuxiliaryItem::getPeriodYear, balanceInitRecordDto.getPeriodYear())
                .eq(BalanceSubjectInitAuxiliaryItem::getPeriodNum, balanceInitRecordDto.getPeriodNum()));
        return flag;
    }

    /**
     * 拉取当前所在公司账簿，自动带出账簿中有数据的核算主体及当前所在核算主体，账簿对应现金流量信息,账簿启用期间
     *
     * @param balanceCashFlowInitDto
     * @return com.njwd.ledger.entity.vo.BalanceCashFlowInitVo
     * @Author lj
     * @Date:11:00 2019/7/31
     **/
    @Override
    public BalanceCashFlowInitVo findAccountBookByComId(BalanceCashFlowInitDto balanceCashFlowInitDto) {
        //查询当前用户所在的账簿，以及对应的账簿启用期间
        BalanceCashFlowInitVo balanceCashFlowInitVo = new BalanceCashFlowInitVo();
        SysUserVo userVo = UserUtils.getUserVo();
        AccountBookDto accountBookDto = new AccountBookDto();
        //accountBookDto.setCompanyId(balanceCashFlowInitDto.getCompanyId());
        accountBookDto.setId(balanceCashFlowInitDto.getAccountBookId());
        AccountBookVo accountBookVo = accountBookFeignClient.findAccountBookById(accountBookDto).getData();
        if (accountBookVo != null) {
            //权限校验
            ShiroUtils.checkPerm(Constant.MenuDefine.CASH_EDIT,accountBookVo.getCompanyId());
            balanceCashFlowInitVo.setAccountBookId(accountBookVo.getId());
            balanceCashFlowInitVo.setAccountBookName(accountBookVo.getName());
            balanceCashFlowInitVo.setCashFlowId(accountBookVo.getCashFlowItemId());
            balanceCashFlowInitVo.setCashFlowName(accountBookVo.getAccountCashFlow().getCashFlowName());
            balanceCashFlowInitVo.setHasSubAccount(accountBookVo.getHasSubAccount());
            AccountBookSystemDto accountBookSystemDto = new AccountBookSystemDto();
            accountBookSystemDto.setAccountBookId(accountBookVo.getId());
            accountBookSystemDto.setRootEnterpriseId(userVo.getRootEnterpriseId());
            Page<AccountBookSystemVo> accountBookSystemVoPage = accountBookSystemFeignClient.findLedgerList(accountBookSystemDto).getData();
            List<AccountBookSystemVo> accountBookSystemList = accountBookSystemVoPage.getRecords();
            for (AccountBookSystem accountBookSystem : accountBookSystemList) {
                if (Constant.SystemSignValue.LEDGER.equals(accountBookSystem.getSystemSign())) {
                    balanceCashFlowInitVo.setPeriodYear(accountBookSystem.getPeriodYear());
                    balanceCashFlowInitVo.setPeriodNum(accountBookSystem.getPeriodNum());
                    balanceCashFlowInitVo.setAccountBookSystemId(accountBookSystem.getId());
                }
            }

            //查询账簿的下的核算主体
            List<Long> accountBookIdList = new ArrayList<Long>();
            accountBookIdList.add(accountBookVo.getId());
            AccountBookEntityDto accountBookEntityDto = new AccountBookEntityDto();
            accountBookEntityDto.setAccountBookIdList(accountBookIdList);
            List<AccountBookEntityVo> accountBookEntityList = accountBookEntityFeignClient.findAuthOperationalEntityList(accountBookEntityDto).getData().getRecords();
            balanceCashFlowInitVo.setEntityList(accountBookEntityList);
        }
        return balanceCashFlowInitVo;
    }

    /**
     * a)	当前账簿启用二级核算，核算主体默认选中公司本部，可选择到其他核算主体，流量项目中显示属性为内部往来现金流量；
     * b)	当前账簿未启用二级核算，核算主体默认选中公司本部且置灰不可选择，项目列表中不显示属性为内部往来的现金流量
     *
     * @param balanceCashFlowInitDto
     * @return List<AccountSubjectVo>
     * @Author lj
     * @Date:10:12 2019/7/29
     **/
    @Override
    public List<CashFlowItemVo> findCashFlow(BalanceCashFlowInitDto balanceCashFlowInitDto) {
        //调用系统后台接口
        List<CashFlowItemVo> cashFlowItemVoList;
        //未启用二级核算
        if (Constant.Is.NO.equals(balanceCashFlowInitDto.getHasSubAccount())) {
            CashFlowItemDto cashFlowItemDto = new CashFlowItemDto();
            cashFlowItemDto.setCashFlowId(balanceCashFlowInitDto.getCashFlowId());
            cashFlowItemDto.setIsInteriorContact(Constant.Is.NO);
            cashFlowItemVoList = cashFlowReportClient.findCashFlowItemForReport(cashFlowItemDto).getData();
        } else {
            CashFlowItemDto cashFlowItemDto = new CashFlowItemDto();
            cashFlowItemDto.setCashFlowId(balanceCashFlowInitDto.getCashFlowId());
            cashFlowItemVoList = cashFlowReportClient.findCashFlowItemForReport(cashFlowItemDto).getData();
        }
        //获取现金流量期初数据
        List<BalanceCashFlowInit> balanceCashFlowInitList = balanceCashFlowInitMapper.selectList(new LambdaQueryWrapper<BalanceCashFlowInit>()
                .eq(BalanceCashFlowInit::getAccountBookEntityId, balanceCashFlowInitDto.getAccountBookEntityId())
                .eq(BalanceCashFlowInit::getPeriodNum, balanceCashFlowInitDto.getPeriodNum())
                .eq(BalanceCashFlowInit::getPeriodYear, balanceCashFlowInitDto.getPeriodYear()));
        if (CollectionUtils.isNotEmpty(cashFlowItemVoList) && CollectionUtils.isNotEmpty(balanceCashFlowInitList)) {
            MergeUtil.merge(cashFlowItemVoList, balanceCashFlowInitList,
                    cashFlowItemVo -> cashFlowItemVo.getId(),balanceCashFlowInit->balanceCashFlowInit.getItemId(),
                    (cashFlowItemVo, balanceCashFlowInit) -> {
                        cashFlowItemVo.setOpeningBalance(balanceCashFlowInit.getOpeningBalance());
                    });

            //现金流量code与本年已发生额Map
            Map<String, BigDecimal> openingBalanceMap = cashFlowItemVoList.stream().filter(t -> (t.getOpeningBalance() != null)).collect(Collectors.toMap(b -> b.getCode(),
                    s -> s.getOpeningBalance() == null ? new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT) : s.getOpeningBalance()));
            //末级科目数据自动汇总到上级，用于前端展示
            for (CashFlowItemVo cashFlowItemVo : cashFlowItemVoList) {
                if (!isCashFlowGroup(cashFlowItemVo.getCode()) && Constant.Is.NO.equals(cashFlowItemVo.getIsFinal())) {
                    BigDecimal openingBalance = getCashFlowMoney(openingBalanceMap, cashFlowItemVo.getCode());
                    if (openingBalance != null) {
                        cashFlowItemVo.setOpeningBalance(openingBalance);
                    }
                }
            }

            //计算一级分组的值
            Map<String, BigDecimal> cashFlowMap = cashFlowItemVoList.stream().collect(Collectors.toMap(b -> b.getCode(),
                    s -> s.getOpeningBalance() == null ? new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT) : s.getOpeningBalance()));
            for (CashFlowItemVo cashFlowItemVo : cashFlowItemVoList) {
                //101-102
                if (LedgerConstant.CashFlowGroup.CASH_FLOW_GROUP_ONE.equals(cashFlowItemVo.getCode())) {
                    cashFlowItemVo.setOpeningBalance(cashFlowMap.get(LedgerConstant.CashFlowGroup.CASH_FLOW_ONE_Z_ONE).subtract(cashFlowMap.get(LedgerConstant.CashFlowGroup.CASH_FLOW_ONE_Z_TWO)));
                }
                //201-202
                if (LedgerConstant.CashFlowGroup.CASH_FLOW_GROUP_TWO.equals(cashFlowItemVo.getCode())) {
                    cashFlowItemVo.setOpeningBalance(cashFlowMap.get(LedgerConstant.CashFlowGroup.CASH_FLOW_TWO_Z_ONE).subtract(cashFlowMap.get(LedgerConstant.CashFlowGroup.CASH_FLOW_TWO_Z_TWO)));
                }
                //301-302
                if (LedgerConstant.CashFlowGroup.CASH_FLOW_GROUP_THR.equals(cashFlowItemVo.getCode())) {
                    cashFlowItemVo.setOpeningBalance(cashFlowMap.get(LedgerConstant.CashFlowGroup.CASH_FLOW_THR_Z_ONE).subtract(cashFlowMap.get(LedgerConstant.CashFlowGroup.CASH_FLOW_THR_Z_TWO)));
                }
                //401-402
                if (LedgerConstant.CashFlowGroup.CASH_FLOW_GROUP_FOU.equals(cashFlowItemVo.getCode())) {
                    cashFlowItemVo.setOpeningBalance(cashFlowMap.get(LedgerConstant.CashFlowGroup.CASH_FLOW_FOU_Z_ONE).subtract(cashFlowMap.get(LedgerConstant.CashFlowGroup.CASH_FLOW_FOU_Z_TWO)));
                }
            }
        }
        return cashFlowItemVoList;
    }

    /**
     * @param balanceCashFlowInitDto
     * @return BalanceCashFlowInitVo
     * @Description 查询帐簿总账现金流量启用状态
     * @Author lj
     * @Date:10:12 2019/7/29
     **/
    @Override
    public BalanceCashFlowInitVo findAccCashFlowStatus(BalanceCashFlowInitDto balanceCashFlowInitDto) {
        BalanceCashFlowInitVo balanceCashFlowInitVo = new BalanceCashFlowInitVo();
        AccountBookSystemDto accountBookSystemDto = new AccountBookSystemDto();
        accountBookSystemDto.setAccountBookId(balanceCashFlowInitDto.getAccountBookId());
        accountBookSystemDto.setSystemSign(Constant.SystemSignValue.LEDGER);
        List<AccountBookSystemVo> accountBookSystemVos = accountBookSystemFeignClient.findEnableList(accountBookSystemDto).getData();
        if (CollectionUtils.isNotEmpty(accountBookSystemVos)) {
            AccountBookSystemVo accountBookSystemVo = accountBookSystemVos.get(Constant.Number.ZERO);
            balanceCashFlowInitVo.setCashFlowEnableStatus(accountBookSystemVo.getCashFlowEnableStatus());
        }
        return balanceCashFlowInitVo;
    }

    /**
     * 查询期初录入记录列表
     *
     * @param balanceInitRecordDto
     * @return
     */
    @Override
    public List<BalanceInitRecordVo> findListByParam(BalanceInitRecordDto balanceInitRecordDto) {
        //查询有权限的核算主体
        List<Long> accountBookIdList= balanceInitRecordMapper.findAccoutBookIdListByParam(new BalanceInitRecordDto());
        AccountBookEntityDto accountBookEntityDto = new AccountBookEntityDto();
        accountBookEntityDto.setAccountBookIdList(accountBookIdList);
        Page<AccountBookEntityVo> accountBookEntityVoPage = accountBookEntityFeignClient.findAuthOperationalEntityList(accountBookEntityDto).getData();
        List<AccountBookEntityVo> accountBookEntityVos =accountBookEntityVoPage.getRecords();
        List<Long> entityIdList = new ArrayList<>();
        //如果一个权限都没有直接返回空
        if(CollectionUtils.isNotEmpty(accountBookEntityVos)){
            for(AccountBookEntityVo accountBookEntityVo:accountBookEntityVos){
                entityIdList.add(accountBookEntityVo.getId());
            }
        }else{
            return new ArrayList<>();
        }
        balanceInitRecordDto.setEntityIdList(entityIdList);
        return balanceInitRecordMapper.findListByParam(balanceInitRecordDto);
    }

    /**
     * 查询期初录入表账簿id列表
     *
     * @param balanceInitRecordDto
     * @return
     */
    @Override
    public List<Long> findAccoutBookIdListByParam(BalanceInitRecordDto balanceInitRecordDto) {
        return balanceInitRecordMapper.findAccoutBookIdListByParam(balanceInitRecordDto);
    }

    /**
     * 查询期初录入表核算主体id列表
     *
     * @param balanceInitRecordDto
     * @return
     */
    @Override
    public List<Long> findEntityIdListByParam(BalanceInitRecordDto balanceInitRecordDto) {
        return balanceInitRecordMapper.findEntityIdListByParam(balanceInitRecordDto);
    }
    
    /**
     * @description: 导出excel
     * @param: [balanceCashFlowInitDto, response]
     * @return: void 
     * @author: xdy        
     * @create: 2019-10-22 15:30 
     */
    @Override
    public void exportExcel(BalanceCashFlowInitDto balanceCashFlowInitDto, HttpServletResponse response) {
        List<CashFlowItemVo> cashFlowItemVoList = findCashFlow(balanceCashFlowInitDto);
        checkAccoutBookStatusTwo(balanceCashFlowInitDto);
        fileService.exportExcel(response,cashFlowItemVoList, ExcelColumnConstant.CashFlowInit.CODE
                ,ExcelColumnConstant.CashFlowInit.NAME
                ,ExcelColumnConstant.CashFlowInit.CASH_FLOW_DIRECTION
                ,ExcelColumnConstant.CashFlowInit.OPENING_BALANCE);
    }
}
