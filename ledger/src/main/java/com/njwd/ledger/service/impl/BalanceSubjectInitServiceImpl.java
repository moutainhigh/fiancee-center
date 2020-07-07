package com.njwd.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.ExcelColumnConstant;
import com.njwd.common.LedgerConstant;
import com.njwd.entity.basedata.AccountBookSystem;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.AccountBookEntityDto;
import com.njwd.entity.basedata.dto.AccountBookSystemDto;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.basedata.vo.AccountBookSystemVo;
import com.njwd.entity.basedata.vo.AccountBookVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.BalanceInitRecord;
import com.njwd.entity.ledger.BalanceSubjectInit;
import com.njwd.entity.ledger.BalanceSubjectInitAuxiliary;
import com.njwd.entity.ledger.BalanceSubjectInitAuxiliaryItem;
import com.njwd.entity.ledger.dto.BalanceCashFlowInitDto;
import com.njwd.entity.ledger.dto.BalanceInitDto;
import com.njwd.entity.ledger.dto.BalanceSubjectInitAuxiliaryDto;
import com.njwd.entity.ledger.dto.BalanceSubjectInitDto;
import com.njwd.entity.ledger.vo.BalanceSubjectInitExtVo;
import com.njwd.entity.ledger.vo.BalanceSubjectInitVo;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.ledger.cloudclient.AccountBookEntityFeignClient;
import com.njwd.ledger.cloudclient.AccountBookFeignClient;
import com.njwd.ledger.cloudclient.AccountBookSystemFeignClient;
import com.njwd.ledger.cloudclient.AccountSubjectFeignClient;
import com.njwd.ledger.mapper.BalanceInitRecordMapper;
import com.njwd.ledger.mapper.BalanceSubjectInitAuxiliaryItemMapper;
import com.njwd.ledger.mapper.BalanceSubjectInitAuxiliaryMapper;
import com.njwd.ledger.mapper.BalanceSubjectInitMapper;
import com.njwd.ledger.service.BalanceSubjectInitService;
import com.njwd.service.FileService;
import com.njwd.utils.FastUtils;
import com.njwd.utils.MergeUtil;
import com.njwd.utils.ShiroUtils;
import com.njwd.utils.UserUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author lj
 * @Description 期初余额-科目期初
 * @Date:9:50 2019/7/29
 **/
@Service
public class BalanceSubjectInitServiceImpl implements BalanceSubjectInitService {

    @Resource
    private BalanceSubjectInitMapper balanceSubjectInitMapper;

    @Resource
    private BalanceInitRecordMapper balanceInitRecordMapper;

    @Resource
    private BalanceSubjectInitAuxiliaryMapper balanceSubInitAuxiliaryMapper;

    @Resource
    private BalanceSubjectInitAuxiliaryItemMapper balanceSubInitAuxItemMapper;

    @Resource
    private AccountBookFeignClient accountBookFeignClient;

    @Resource
    private AccountBookSystemFeignClient accountBookSystemFeignClient;

    @Resource
    private AccountSubjectFeignClient accountSubjectFeignClient;

    @Resource
    private AccountBookEntityFeignClient accountBookEntityFeignClient;

    @Resource
    private FileService fileService;

    /**
     * 科目期初录入
     *
     * @param balanceSubjectInitDto
     * @return int
     * @Author lj
     * @Date:10:20 2019/8/2
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addSubjectInitBatch(BalanceSubjectInitDto balanceSubjectInitDto) {
        List<BalanceSubjectInitDto> balanceSubjectInitDtos = balanceSubjectInitDto.getBalanceSubjectInitList();
        //批量移除openingBalance为NULL的数据
        List<BalanceSubjectInitDto> balanceSubjectInitList = new ArrayList<BalanceSubjectInitDto>();
        //剔除空数据和非末级数据
        if (CollectionUtils.isNotEmpty(balanceSubjectInitDtos)) {
            balanceSubjectInitList = balanceSubjectInitDtos.stream().filter(t -> (t.getOpeningBalance() !=null || t.getThisYearCreditAmount() != null ||
                    t.getThisYearDebitAmount() != null || t.getYearOpeningBalance() != null)&&(Constant.Is.YES.equals(t.getIsFinal()))).collect(Collectors.toList());
        }

        for (BalanceSubjectInitDto bDto : balanceSubjectInitList) {
            setNotNull(bDto);
        }
        //剔除全部为0.00的数据
        BigDecimal zore = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
        if (CollectionUtils.isNotEmpty(balanceSubjectInitList)){
            balanceSubjectInitList = balanceSubjectInitList.stream().filter(t -> zore.compareTo(t.getOpeningBalance())!=Constant.Number.ZERO || zore.compareTo(t.getThisYearCreditAmount())!=Constant.Number.ZERO ||
                    zore.compareTo(t.getThisYearDebitAmount())!=Constant.Number.ZERO || zore.compareTo(t.getYearOpeningBalance())!=Constant.Number.ZERO).collect(Collectors.toList());
        }
        int flag = Constant.Number.ONE;
        //插入前先清空
        if (CollectionUtils.isNotEmpty(balanceSubjectInitList)) {
            checkAccountBookStatus(balanceSubjectInitDto.getBalanceSubjectInitList().get(Constant.Number.ZERO).getAccountBookId());
            //版本并发校验
            checkVesion(balanceSubjectInitDto,balanceSubjectInitDto.getBalanceSubjectInitList().get(Constant.Number.ZERO).getAccountBookEntityId());
            balanceSubjectInitMapper.delete(new LambdaQueryWrapper<BalanceSubjectInit>()
                    .eq(BalanceSubjectInit::getAccountBookEntityId, balanceSubjectInitDto.getBalanceSubjectInitList().get(Constant.Number.ZERO).getAccountBookEntityId())
                    .eq(BalanceSubjectInit::getPeriodYear, balanceSubjectInitDto.getBalanceSubjectInitList().get(Constant.Number.ZERO).getPeriodYear())
                    .eq(BalanceSubjectInit::getPeriodNum, balanceSubjectInitDto.getBalanceSubjectInitList().get(Constant.Number.ZERO).getPeriodNum()));
            //批量插入科目期初余额
            balanceSubjectInitMapper.addSubjectInitBatch(balanceSubjectInitList);
        }else {
            if (CollectionUtils.isNotEmpty(balanceSubjectInitDtos)) {
                checkAccountBookStatus(balanceSubjectInitDtos.get(Constant.Number.ZERO).getAccountBookId());
                //版本并发校验
                checkVesion(balanceSubjectInitDto,balanceSubjectInitDtos.get(Constant.Number.ZERO).getAccountBookEntityId());
                balanceSubjectInitMapper.delete(new LambdaQueryWrapper<BalanceSubjectInit>()
                        .eq(BalanceSubjectInit::getAccountBookEntityId, balanceSubjectInitDtos.get(Constant.Number.ZERO).getAccountBookEntityId())
                        .eq(BalanceSubjectInit::getPeriodYear, balanceSubjectInitDtos.get(Constant.Number.ZERO).getPeriodYear())
                        .eq(BalanceSubjectInit::getPeriodNum, balanceSubjectInitDtos.get(Constant.Number.ZERO).getPeriodNum()));
            }

        }

        List<BalanceSubjectInitAuxiliaryDto> balanceSubInitAuxiliaryList = balanceSubjectInitDto.getBalanceSubInitAuxiliaryList();
        Long accountBookEntityId = null;
        Long accountBookId = null;
        Long accountBookSystemId = null;
        Integer periodYear = null;
        Byte periodNum = null;
        //批量插入科目辅助核算项目余额
        if (CollectionUtils.isNotEmpty(balanceSubInitAuxiliaryList)) {
            accountBookId = balanceSubInitAuxiliaryList.get(Constant.Number.ZERO).getAccountBookId();
            accountBookEntityId = balanceSubInitAuxiliaryList.get(Constant.Number.ZERO).getAccountBookEntityId();
            accountBookSystemId = balanceSubInitAuxiliaryList.get(Constant.Number.ZERO).getAccountBookSystemId();
            periodYear = balanceSubInitAuxiliaryList.get(Constant.Number.ZERO).getPeriodYear();
            periodNum = balanceSubInitAuxiliaryList.get(Constant.Number.ZERO).getPeriodNum();
            checkAccountBookStatus(accountBookId);
            //版本并发校验
            checkVesion(balanceSubjectInitDto,accountBookEntityId);

            //辅助核算余额汇总到科目表
            Map<Long, List<BalanceSubjectInitAuxiliaryDto>> balAuxMap =balanceSubInitAuxiliaryList.stream().collect(Collectors.groupingBy(BalanceSubjectInitAuxiliaryDto::getAccountSubjectId));
            BalanceSubjectInit bSubjectInit = null;
            BigDecimal openingBalanceSum = null;
            BigDecimal yearOpeningBalanceSum = null;
            BigDecimal yearDebitAmountSum = null;
            BigDecimal yearCreditAmountSum = null;
            List<BalanceSubjectInitDto> deleteDtos= new ArrayList<>();
            BalanceSubjectInitDto deleteDto;
            List<BalanceSubjectInit> bSubjectInits = new ArrayList<>();
            for(Long subjectId : balAuxMap.keySet()){
                //清空科目期初辅助核算余额数据
                balanceSubInitAuxiliaryMapper.delete(new LambdaQueryWrapper<BalanceSubjectInitAuxiliary>()
                        .eq(BalanceSubjectInitAuxiliary::getAccountBookEntityId, accountBookEntityId)
                        .eq(BalanceSubjectInitAuxiliary::getAccountSubjectId, subjectId)
                        .eq(BalanceSubjectInitAuxiliary::getPeriodYear, periodYear)
                        .eq(BalanceSubjectInitAuxiliary::getPeriodNum, periodNum));
                //清空科目期初辅助核算初始化项目数据
                balanceSubInitAuxItemMapper.delete(new LambdaQueryWrapper<BalanceSubjectInitAuxiliaryItem>()
                        .eq(BalanceSubjectInitAuxiliaryItem::getAccountBookEntityId, accountBookEntityId)
                        .eq(BalanceSubjectInitAuxiliaryItem::getAccountSubjectId, subjectId)
                        .eq(BalanceSubjectInitAuxiliaryItem::getPeriodYear, periodYear)
                        .eq(BalanceSubjectInitAuxiliaryItem::getPeriodNum, periodNum));
                List<BalanceSubjectInitAuxiliaryDto> subjectInitAuxiliaryDtos = balAuxMap.get(subjectId);
                openingBalanceSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
                yearOpeningBalanceSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
                yearDebitAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
                yearCreditAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
                Boolean insertFlag=false;
                for(BalanceSubjectInitAuxiliaryDto balSubInitAuxiliary : subjectInitAuxiliaryDtos){
                    bSubjectInit = new BalanceSubjectInit();
                    bSubjectInit.setAccountBookEntityId(balSubInitAuxiliary.getAccountBookEntityId());
                    bSubjectInit.setAccountBookId(balSubInitAuxiliary.getAccountBookId());
                    bSubjectInit.setAccountSubjectId(balSubInitAuxiliary.getAccountSubjectId());
                    bSubjectInit.setPeriodYear(balSubInitAuxiliary.getPeriodYear());
                    bSubjectInit.setAccountBookSystemId(accountBookSystemId);
                    bSubjectInit.setPeriodNum(balSubInitAuxiliary.getPeriodNum());
                    deleteDto = new BalanceSubjectInitDto();
                    deleteDto.setAccountBookEntityId(bSubjectInit.getAccountBookEntityId());
                    deleteDto.setAccountSubjectId(bSubjectInit.getAccountSubjectId());
                    deleteDto.setPeriodYear(bSubjectInit.getPeriodYear());
                    deleteDto.setPeriodNum(bSubjectInit.getPeriodNum());
                    deleteDtos.add(deleteDto);
                    if (balSubInitAuxiliary.getOpeningBalance() != null || balSubInitAuxiliary.getThisYearCreditAmount() != null ||
                            balSubInitAuxiliary.getThisYearDebitAmount() != null || balSubInitAuxiliary.getYearOpeningBalance() != null) {
                        setBalanceNotNull(balSubInitAuxiliary);
                        //过滤全部为0.00的数据
                        if(zore.compareTo(balSubInitAuxiliary.getOpeningBalance())==Constant.Number.ZERO && zore.compareTo(balSubInitAuxiliary.getThisYearCreditAmount())==Constant.Number.ZERO &&
                                zore.compareTo(balSubInitAuxiliary.getThisYearDebitAmount())==Constant.Number.ZERO && zore.compareTo(balSubInitAuxiliary.getYearOpeningBalance())==Constant.Number.ZERO){
                            continue;
                        }
                        //辅助核算余额汇总到科目表
                        openingBalanceSum = openingBalanceSum.add(balSubInitAuxiliary.getOpeningBalance());
                        yearOpeningBalanceSum = yearOpeningBalanceSum.add(balSubInitAuxiliary.getYearOpeningBalance());
                        yearDebitAmountSum = yearDebitAmountSum.add(balSubInitAuxiliary.getThisYearDebitAmount());
                        yearCreditAmountSum = yearCreditAmountSum.add(balSubInitAuxiliary.getThisYearCreditAmount());
                        insertFlag=true;
                    }

                }
                bSubjectInit.setOpeningBalance(openingBalanceSum);
                bSubjectInit.setYearOpeningBalance(yearOpeningBalanceSum);
                bSubjectInit.setThisYearDebitAmount(yearDebitAmountSum);
                bSubjectInit.setThisYearCreditAmount(yearCreditAmountSum);
                if(insertFlag){
                    bSubjectInits.add(bSubjectInit);
                }
            }
            if(CollectionUtils.isNotEmpty(deleteDtos)){
                balanceSubjectInitMapper.deleteSubjectInitBatch(deleteDtos);
            }

            for(BalanceSubjectInit bSubjectIni:bSubjectInits){
                balanceSubjectInitMapper.insert(bSubjectIni);
            }

            //批量插入科目期初辅助核算
            insertSubAuxData(balanceSubjectInitDto);
        }

        //调用试算平衡更新表状态
        if(CollectionUtils.isNotEmpty(balanceSubjectInitDtos)){
            accountBookEntityId = balanceSubjectInitDtos.get(Constant.Number.ZERO).getAccountBookEntityId();
            accountBookSystemId = balanceSubjectInitDtos.get(Constant.Number.ZERO).getAccountBookSystemId();
            accountBookId = balanceSubjectInitDtos.get(Constant.Number.ZERO).getAccountBookId();
            periodYear = balanceSubjectInitDtos.get(Constant.Number.ZERO).getPeriodYear();
            periodNum = balanceSubjectInitDtos.get(Constant.Number.ZERO).getPeriodNum();
        }

        BalanceSubjectInitDto bal = new BalanceSubjectInitDto();
        bal.setAccountBookEntityId(accountBookEntityId);
        bal.setAccountBookSystemId(accountBookSystemId);
        bal.setAccountBookId(accountBookId);
        bal.setPeriodYear(periodYear);
        bal.setPeriodNum(periodNum);
        BalanceSubjectInitVo balance = trialBalance(bal);
        BalanceInitRecord balanceInitRecord = new BalanceInitRecord();
        if(zore.compareTo(balance.getDifferenceAmount())!=Constant.Number.ZERO ||zore.compareTo(balance.getDifferenceOpeningBalance())!=Constant.Number.ZERO){
            balanceInitRecord.setSubjectStatus(LedgerConstant.SubjectStatus.UN_BALANCE);
        }else {
            balanceInitRecord.setSubjectStatus(LedgerConstant.SubjectStatus.BALANCED);
        }
        SysUserVo user = UserUtils.getUserVo();
        balanceInitRecordMapper.update(balanceInitRecord,new LambdaQueryWrapper<BalanceInitRecord>()
                .eq(BalanceInitRecord::getEntityId,accountBookEntityId)
                .eq(BalanceInitRecord::getPeriodYear, periodYear)
                .eq(BalanceInitRecord::getRootEnterpriseId, user.getRootEnterpriseId())
                .eq(BalanceInitRecord::getPeriodNum, periodNum));

        return flag;
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

    private void checkVesion(BalanceSubjectInitDto balanceSubjectInitDto,Long accountBookEntityId) {
/*        Integer vsersion = balanceSubjectInitDto.getVersion();
        Integer existsVersion = (Integer) RedisUtils.getObj(accountBookEntityId.toString());
        if (!vsersion.equals(existsVersion)) {
//            throw new ServiceException(ResultCode.VERSION_ERROR);
        }
        existsVersion = existsVersion + 1;*/
//        RedisUtils.set(accountBookEntityId.toString(), existsVersion, 30, TimeUnit.MINUTES);
    }

    /**
     * 设置null为0.00
     * @Author lj
     * @Date:14:13 2019/8/29
     * @param balSubInitAuxiliary
     * @return void
     **/
    private void setBalanceNotNull(BalanceSubjectInitAuxiliaryDto balSubInitAuxiliary) {
        if (balSubInitAuxiliary.getOpeningBalance() == null) {
            balSubInitAuxiliary.setOpeningBalance(new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT));
        }
        if (balSubInitAuxiliary.getThisYearCreditAmount() == null) {
            balSubInitAuxiliary.setThisYearCreditAmount(new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT));
        }
        if (balSubInitAuxiliary.getThisYearDebitAmount() == null) {
            balSubInitAuxiliary.setThisYearDebitAmount(new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT));
        }
        if (balSubInitAuxiliary.getYearOpeningBalance() == null) {
            balSubInitAuxiliary.setYearOpeningBalance(new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT));
        }
    }

    /**
     * 插入辅助核算数据
     * @Author lj
     * @Date:14:14 2019/8/29
     * @param balanceSubjectInitDto
     * @return void
     **/
    private void insertSubAuxData(BalanceSubjectInitDto balanceSubjectInitDto) {
        for (BalanceSubjectInitAuxiliaryDto balSubInitAuxiliary : balanceSubjectInitDto.getBalanceSubInitAuxiliaryList()) {
            List<BalanceSubjectInitAuxiliaryItem> balanceSubjectInitAuxItemList = balSubInitAuxiliary.getBalanceSubjectInitAuxItemList();
            if (balSubInitAuxiliary.getOpeningBalance() != null || balSubInitAuxiliary.getThisYearCreditAmount() != null ||
                    balSubInitAuxiliary.getThisYearDebitAmount() != null || balSubInitAuxiliary.getYearOpeningBalance() != null) {
                setBalanceNotNull(balSubInitAuxiliary);
                //过滤全部为0.00的数据
                BigDecimal zore = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
                if(zore.compareTo(balSubInitAuxiliary.getOpeningBalance())==Constant.Number.ZERO && zore.compareTo(balSubInitAuxiliary.getThisYearCreditAmount())==Constant.Number.ZERO &&
                        zore.compareTo(balSubInitAuxiliary.getThisYearDebitAmount())==Constant.Number.ZERO && zore.compareTo(balSubInitAuxiliary.getYearOpeningBalance())==Constant.Number.ZERO){
                    continue;
                }
                balanceSubInitAuxiliaryMapper.addSubjectInitAuxiliary(balSubInitAuxiliary);
                for (BalanceSubjectInitAuxiliaryItem item : balanceSubjectInitAuxItemList) {
                    item.setAccountBookEntityId(balSubInitAuxiliary.getAccountBookEntityId());
                    item.setAccountBookId(balSubInitAuxiliary.getAccountBookId());
                    item.setAccountSubjectId(balSubInitAuxiliary.getAccountSubjectId());
                    item.setPeriodYear(balSubInitAuxiliary.getPeriodYear());
                    item.setPeriodNum(balSubInitAuxiliary.getPeriodNum());
                    item.setBalanceAuxiliaryId(balSubInitAuxiliary.getId());
                }
                //移除前端传来的空值
                Iterator<BalanceSubjectInitAuxiliaryItem> iterator = balanceSubjectInitAuxItemList.iterator();
                while (iterator.hasNext()) {
                    if(iterator.next().getItemValueId()==null){
                        iterator.remove();
                    }
                }
                //批量插入科目期初辅助核算初始化项目
                balanceSubInitAuxItemMapper.addBalanceSubInitAuxItemBatch(balanceSubjectInitAuxItemList);
            }
        }
    }

    /**
     * 设置null为0.00
     * @Author lj
     * @Date:14:13 2019/8/29
     * @param bDto
     * @return void
     **/
    private void setNotNull(BalanceSubjectInitDto bDto) {
        if (bDto.getOpeningBalance() == null) {
            bDto.setOpeningBalance(new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT));
        }
        if (bDto.getThisYearCreditAmount() == null) {
            bDto.setThisYearCreditAmount(new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT));
        }
        if (bDto.getThisYearDebitAmount() == null) {
            bDto.setThisYearDebitAmount(new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT));
        }
        if (bDto.getYearOpeningBalance() == null) {
            bDto.setYearOpeningBalance(new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT));
        }
    }

    /**
     * 校验账簿状态
     * @Author lj
     * @Date:11:32 2019/11/1
     * @param balanceSubjectInitDto
     * @return void
     **/
    private void checkAccoutBookStatusTwo(BalanceSubjectInitDto balanceSubjectInitDto) {
        //查询数据是否已经被删除
        List<BalanceInitRecord> balanceInitRecordList = balanceInitRecordMapper.selectList(new LambdaQueryWrapper<BalanceInitRecord>()
                .eq(BalanceInitRecord::getEntityId, balanceSubjectInitDto.getAccountBookEntityId())
                .eq(BalanceInitRecord::getPeriodYear, balanceSubjectInitDto.getPeriodYear())
                .eq(BalanceInitRecord::getPeriodNum, balanceSubjectInitDto.getPeriodNum()));
        if(CollectionUtils.isEmpty(balanceInitRecordList)){
            throw new ServiceException(ResultCode.BALANCE_INIT_RECORD_DELETED);
        }else if(LedgerConstant.SubjectStatus.INIT_ED.equals(balanceInitRecordList.get(Constant.Number.ZERO).getSubjectStatus())) {
            throw new ServiceException(ResultCode.BALANCE_INIT_RECORD_INIT);
        }
    }

    /**
     * 根据核算主体ID清空期初数据
     *
     * @param balanceSubjectInitDto
     * @return int
     * @Author lj
     * @Date:15:42 2019/7/25
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBalSubjectBatch(BalanceSubjectInitDto balanceSubjectInitDto) {
        int flag = Constant.Number.ONE;
        SysUserVo user = UserUtils.getUserVo();
        checkAccoutBookStatusTwo(balanceSubjectInitDto);
        try {
            //清空科目期初数据
            balanceSubjectInitMapper.delete(new LambdaQueryWrapper<BalanceSubjectInit>()
                    .eq(BalanceSubjectInit::getAccountBookEntityId, balanceSubjectInitDto.getAccountBookEntityId())
                    .eq(BalanceSubjectInit::getPeriodYear, balanceSubjectInitDto.getPeriodYear())
                    .eq(BalanceSubjectInit::getPeriodNum, balanceSubjectInitDto.getPeriodNum()));
            //清空科目期初辅助核算余额数据
            balanceSubInitAuxiliaryMapper.delete(new LambdaQueryWrapper<BalanceSubjectInitAuxiliary>()
                    .eq(BalanceSubjectInitAuxiliary::getAccountBookEntityId, balanceSubjectInitDto.getAccountBookEntityId())
                    .eq(BalanceSubjectInitAuxiliary::getPeriodYear, balanceSubjectInitDto.getPeriodYear())
                    .eq(BalanceSubjectInitAuxiliary::getPeriodNum, balanceSubjectInitDto.getPeriodNum()));
            //清空科目期初辅助核算初始化项目数据
            balanceSubInitAuxItemMapper.delete(new LambdaQueryWrapper<BalanceSubjectInitAuxiliaryItem>()
                    .eq(BalanceSubjectInitAuxiliaryItem::getAccountBookEntityId, balanceSubjectInitDto.getAccountBookEntityId())
                    .eq(BalanceSubjectInitAuxiliaryItem::getPeriodYear, balanceSubjectInitDto.getPeriodYear())
                    .eq(BalanceSubjectInitAuxiliaryItem::getPeriodNum, balanceSubjectInitDto.getPeriodNum()));
            //更新核算主体记录表科目状态
            BalanceInitRecord balanceInitRecord = new BalanceInitRecord();
            balanceInitRecord.setSubjectStatus(LedgerConstant.SubjectStatus.UN_RECORD);
            balanceInitRecordMapper.update(balanceInitRecord,new LambdaQueryWrapper<BalanceInitRecord>()
                    .eq(BalanceInitRecord::getEntityId,balanceSubjectInitDto.getAccountBookEntityId())
                    .eq(BalanceInitRecord::getPeriodYear, balanceSubjectInitDto.getPeriodYear())
                    .eq(BalanceInitRecord::getRootEnterpriseId, user.getRootEnterpriseId())
                    .eq(BalanceInitRecord::getPeriodNum, balanceSubjectInitDto.getPeriodNum()));
        } catch (Exception e) {
            flag = Constant.Number.ZERO;
            new ServiceException(ResultCode.OPERATION_FAILURE);
        }
        return flag;
    }

    /**
     * 试算平衡
     * @Author lj
     * @Date:11:43 2019/9/9
     * @param balanceSubjectInitDto
     * @return com.njwd.entity.ledger.vo.BalanceSubjectInitVo
     **/
    @Override
    public BalanceSubjectInitVo trialBalance(BalanceSubjectInitDto balanceSubjectInitDto){
        BalanceSubjectInitVo balanceSubjectInitVo = new BalanceSubjectInitVo();
        AccountBookVo accountBookVo = getAccountBook(balanceSubjectInitDto);
        if(accountBookVo!=null){
            AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
            accountSubjectDto.setSubjectId(accountBookVo.getSubjectId());
            List<AccountSubjectVo> accountSubjectVos = accountSubjectFeignClient.findInfoForLedger(accountSubjectDto).getData();
            List<BalanceSubjectInitDto> balanceSubjectInitDtos = new ArrayList<BalanceSubjectInitDto>();
            //查询科目期初数据
            List<BalanceSubjectInit> balanceSubjectInits = balanceSubjectInitMapper.selectList(new LambdaQueryWrapper<BalanceSubjectInit>()
                    .eq(BalanceSubjectInit::getAccountBookId, balanceSubjectInitDto.getAccountBookId())
                    .eq(BalanceSubjectInit::getAccountBookSystemId, balanceSubjectInitDto.getAccountBookSystemId())
                    .eq(BalanceSubjectInit::getPeriodYear, balanceSubjectInitDto.getPeriodYear())
                    .eq(BalanceSubjectInit::getPeriodNum, balanceSubjectInitDto.getPeriodNum())
                    .eq(BalanceSubjectInit::getAccountBookEntityId, balanceSubjectInitDto.getAccountBookEntityId()));
            BalanceSubjectInitDto balanceSubjectInitDtoTemp = null;
            for (BalanceSubjectInit balanceSubjectInit : balanceSubjectInits) {
                balanceSubjectInitDtoTemp = new BalanceSubjectInitDto();
                FastUtils.copyProperties(balanceSubjectInit, balanceSubjectInitDtoTemp);
                balanceSubjectInitDtos.add(balanceSubjectInitDtoTemp);
            }
            //复制科目属性
            MergeUtil.merge(balanceSubjectInitDtos, accountSubjectVos,
                    accSubjectDto->accSubjectDto.getAccountSubjectId(),accountSubjectVo->accountSubjectVo.getId(),
                    (accSubjectDto, accountSubjectVo) -> {
                        accSubjectDto.setBalanceDirection(accountSubjectVo.getBalanceDirection());
                        accSubjectDto.setAccountCategory(accountSubjectVo.getAccountCategory());
                        accSubjectDto.setAccountSubjectCode(accountSubjectVo.getCode());
                        accSubjectDto.setAccountElementItemName(accountSubjectVo.getAccountElementItemName());
                    });

            //试算平衡
            BigDecimal dOpeningBalanceSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            BigDecimal cOpeningBalanceSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            BigDecimal thisYearDebitAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            BigDecimal thisYearCreditAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            BigDecimal assetsOpeningBalanceSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            BigDecimal assetsThisYearDebitAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            BigDecimal assetsThisYearCreditAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            String assetsDirection = Constant.BalanceDirectionName.FLAT;
            BigDecimal costOpeningBalanceSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            BigDecimal costThisYearDebitAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            BigDecimal costThisYearCreditAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            String costDirection = Constant.BalanceDirectionName.FLAT;
            BigDecimal commonOpeningBalanceSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            BigDecimal commonThisYearDebitAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            BigDecimal commonThisYearCreditAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            String commonDirection = Constant.BalanceDirectionName.FLAT;
            BigDecimal dOpeningBalanceTotal = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            BigDecimal dThisYearDebitAmountTotal = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            BigDecimal dThisYearCreditAmountTotal = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            String dTotalDirection = Constant.BalanceDirectionName.FLAT;
            BigDecimal debtOpeningBalanceSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            BigDecimal debtThisYearDebitAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            BigDecimal debtThisYearCreditAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            String debtDirection = Constant.BalanceDirectionName.FLAT;
            BigDecimal rightOpeningBalanceSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            BigDecimal rightThisYearDebitAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            BigDecimal rightThisYearCreditAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            String rightDirection = Constant.BalanceDirectionName.FLAT;
            BigDecimal profitOpeningBalanceSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            BigDecimal profitThisYearDebitAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            BigDecimal profitThisYearCreditAmountSum = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            String profitDirection = Constant.BalanceDirectionName.FLAT;
            BigDecimal cOpeningBalanceTotal = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            BigDecimal cThisYearDebitAmountTotal = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            BigDecimal cThisYearCreditAmountTotal = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
            String cTotalDirection = Constant.BalanceDirectionName.FLAT;
            BigDecimal zore = new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);

            BigDecimal differenceOpeningBalance;
            BigDecimal differenceAmount;
            for (BalanceSubjectInitDto balanceSubjectInit : balanceSubjectInitDtos) {
                //去除内部往来的科目
//                if (!LedgerConstant.BalanceInit.INTERIOR_SUBJECT_CODE.equals(balanceSubjectInit.getAccountSubjectCode())) {
                    //资产
                    if(Constant.AccountElementItemName.ASSETS.equals(balanceSubjectInit.getAccountElementItemName())){
                        if (Constant.Is.NO.equals(balanceSubjectInit.getBalanceDirection())) {
                            dOpeningBalanceSum = dOpeningBalanceSum.add(balanceSubjectInit.getOpeningBalance());
                            assetsOpeningBalanceSum = assetsOpeningBalanceSum.add(balanceSubjectInit.getOpeningBalance());
                            dOpeningBalanceTotal = dOpeningBalanceTotal.add(balanceSubjectInit.getOpeningBalance());
                        } else {
                            cOpeningBalanceSum = cOpeningBalanceSum.add(balanceSubjectInit.getOpeningBalance());
                            assetsOpeningBalanceSum = assetsOpeningBalanceSum.subtract(balanceSubjectInit.getOpeningBalance());
                            dOpeningBalanceTotal = dOpeningBalanceTotal.subtract(balanceSubjectInit.getOpeningBalance());
                        }
                        assetsThisYearDebitAmountSum = assetsThisYearDebitAmountSum.add(balanceSubjectInit.getThisYearDebitAmount());
                        assetsThisYearCreditAmountSum = assetsThisYearCreditAmountSum.add(balanceSubjectInit.getThisYearCreditAmount());
                        dThisYearDebitAmountTotal = dThisYearDebitAmountTotal.add(balanceSubjectInit.getThisYearDebitAmount());
                        dThisYearCreditAmountTotal = dThisYearCreditAmountTotal.add(balanceSubjectInit.getThisYearCreditAmount());
                    }
                    //成本
                    if(Constant.AccountElementItemName.COST.equals(balanceSubjectInit.getAccountElementItemName())){
                        if (Constant.Is.NO.equals(balanceSubjectInit.getBalanceDirection())) {
                            dOpeningBalanceSum = dOpeningBalanceSum.add(balanceSubjectInit.getOpeningBalance());
                            costOpeningBalanceSum = costOpeningBalanceSum.add(balanceSubjectInit.getOpeningBalance());
                            dOpeningBalanceTotal = dOpeningBalanceTotal.add(balanceSubjectInit.getOpeningBalance());
                        } else {
                            cOpeningBalanceSum = cOpeningBalanceSum.add(balanceSubjectInit.getOpeningBalance());
                            costOpeningBalanceSum = costOpeningBalanceSum.subtract(balanceSubjectInit.getOpeningBalance());
                            dOpeningBalanceTotal = dOpeningBalanceTotal.subtract(balanceSubjectInit.getOpeningBalance());
                        }
                        costThisYearDebitAmountSum = costThisYearDebitAmountSum.add(balanceSubjectInit.getThisYearDebitAmount());
                        costThisYearCreditAmountSum = costThisYearCreditAmountSum.add(balanceSubjectInit.getThisYearCreditAmount());
                        dThisYearDebitAmountTotal = dThisYearDebitAmountTotal.add(balanceSubjectInit.getThisYearDebitAmount());
                        dThisYearCreditAmountTotal = dThisYearCreditAmountTotal.add(balanceSubjectInit.getThisYearCreditAmount());
                    }
                    //共同
                    if(Constant.AccountElementItemName.COMMON.equals(balanceSubjectInit.getAccountElementItemName())){
                        if (Constant.Is.NO.equals(balanceSubjectInit.getBalanceDirection())) {
                            dOpeningBalanceSum = dOpeningBalanceSum.add(balanceSubjectInit.getOpeningBalance());
                            commonOpeningBalanceSum = commonOpeningBalanceSum.add(balanceSubjectInit.getOpeningBalance());
                            dOpeningBalanceTotal = dOpeningBalanceTotal.add(balanceSubjectInit.getOpeningBalance());
                        } else {
                            cOpeningBalanceSum = cOpeningBalanceSum.add(balanceSubjectInit.getOpeningBalance());
                            commonOpeningBalanceSum = commonOpeningBalanceSum.subtract(balanceSubjectInit.getOpeningBalance());
                            dOpeningBalanceTotal = dOpeningBalanceTotal.subtract(balanceSubjectInit.getOpeningBalance());
                        }
                        commonThisYearDebitAmountSum = commonThisYearDebitAmountSum.add(balanceSubjectInit.getThisYearDebitAmount());
                        commonThisYearCreditAmountSum = commonThisYearCreditAmountSum.add(balanceSubjectInit.getThisYearCreditAmount());
                        dThisYearDebitAmountTotal = dThisYearDebitAmountTotal.add(balanceSubjectInit.getThisYearDebitAmount());
                        dThisYearCreditAmountTotal = dThisYearCreditAmountTotal.add(balanceSubjectInit.getThisYearCreditAmount());
                    }
                    //负债
                    if(Constant.AccountElementItemName.DEBT.equals(balanceSubjectInit.getAccountElementItemName())){
                        if (Constant.Is.NO.equals(balanceSubjectInit.getBalanceDirection())) {
                            dOpeningBalanceSum = dOpeningBalanceSum.add(balanceSubjectInit.getOpeningBalance());
                            debtOpeningBalanceSum = debtOpeningBalanceSum.subtract(balanceSubjectInit.getOpeningBalance());
                            cOpeningBalanceTotal = cOpeningBalanceTotal.subtract(balanceSubjectInit.getOpeningBalance());
                        } else {
                            cOpeningBalanceSum = cOpeningBalanceSum.add(balanceSubjectInit.getOpeningBalance());
                            debtOpeningBalanceSum = debtOpeningBalanceSum.add(balanceSubjectInit.getOpeningBalance());
                            cOpeningBalanceTotal = cOpeningBalanceTotal.add(balanceSubjectInit.getOpeningBalance());
                        }
                        debtThisYearDebitAmountSum = debtThisYearDebitAmountSum.add(balanceSubjectInit.getThisYearDebitAmount());
                        debtThisYearCreditAmountSum = debtThisYearCreditAmountSum.add(balanceSubjectInit.getThisYearCreditAmount());
                        cThisYearDebitAmountTotal = cThisYearDebitAmountTotal.add(balanceSubjectInit.getThisYearDebitAmount());
                        cThisYearCreditAmountTotal = cThisYearCreditAmountTotal.add(balanceSubjectInit.getThisYearCreditAmount());
                    }
                    //权益
                    if(Constant.AccountElementItemName.RIGHT.equals(balanceSubjectInit.getAccountElementItemName())){
                        if (Constant.Is.NO.equals(balanceSubjectInit.getBalanceDirection())) {
                            dOpeningBalanceSum = dOpeningBalanceSum.add(balanceSubjectInit.getOpeningBalance());
                            rightOpeningBalanceSum = rightOpeningBalanceSum.subtract(balanceSubjectInit.getOpeningBalance());
                            cOpeningBalanceTotal = cOpeningBalanceTotal.subtract(balanceSubjectInit.getOpeningBalance());
                        } else {
                            cOpeningBalanceSum = cOpeningBalanceSum.add(balanceSubjectInit.getOpeningBalance());
                            rightOpeningBalanceSum = rightOpeningBalanceSum.add(balanceSubjectInit.getOpeningBalance());
                            cOpeningBalanceTotal = cOpeningBalanceTotal.add(balanceSubjectInit.getOpeningBalance());
                        }
                        rightThisYearDebitAmountSum = rightThisYearDebitAmountSum.add(balanceSubjectInit.getThisYearDebitAmount());
                        rightThisYearCreditAmountSum = rightThisYearCreditAmountSum.add(balanceSubjectInit.getThisYearCreditAmount());
                        cThisYearDebitAmountTotal = cThisYearDebitAmountTotal.add(balanceSubjectInit.getThisYearDebitAmount());
                        cThisYearCreditAmountTotal = cThisYearCreditAmountTotal.add(balanceSubjectInit.getThisYearCreditAmount());
                    }
                    //损益
                    if(Constant.AccountElementItemName.PROFIT.equals(balanceSubjectInit.getAccountElementItemName())){
                        if (Constant.Is.NO.equals(balanceSubjectInit.getBalanceDirection())) {
                            dOpeningBalanceSum = dOpeningBalanceSum.add(balanceSubjectInit.getOpeningBalance());
                            profitOpeningBalanceSum = profitOpeningBalanceSum.subtract(balanceSubjectInit.getOpeningBalance());
                            cOpeningBalanceTotal = cOpeningBalanceTotal.subtract(balanceSubjectInit.getOpeningBalance());
                        } else {
                            cOpeningBalanceSum = cOpeningBalanceSum.add(balanceSubjectInit.getOpeningBalance());
                            profitOpeningBalanceSum = profitOpeningBalanceSum.add(balanceSubjectInit.getOpeningBalance());
                            cOpeningBalanceTotal = cOpeningBalanceTotal.add(balanceSubjectInit.getOpeningBalance());
                        }
                        profitThisYearDebitAmountSum = profitThisYearDebitAmountSum.add(balanceSubjectInit.getThisYearDebitAmount());
                        profitThisYearCreditAmountSum = profitThisYearCreditAmountSum.add(balanceSubjectInit.getThisYearCreditAmount());
                        cThisYearDebitAmountTotal = cThisYearDebitAmountTotal.add(balanceSubjectInit.getThisYearDebitAmount());
                        cThisYearCreditAmountTotal = cThisYearCreditAmountTotal.add(balanceSubjectInit.getThisYearCreditAmount());
                    }
                    thisYearDebitAmountSum = thisYearDebitAmountSum.add(balanceSubjectInit.getThisYearDebitAmount());
                    thisYearCreditAmountSum = thisYearCreditAmountSum.add(balanceSubjectInit.getThisYearCreditAmount());
//                }
            }

            if(zore.compareTo(assetsOpeningBalanceSum)!=Constant.Number.ZERO){
                assetsDirection=Constant.BalanceDirectionName.DEBIT;
            }
            if(zore.compareTo(costOpeningBalanceSum)!=Constant.Number.ZERO){
                costDirection=Constant.BalanceDirectionName.DEBIT;
            }
            if(zore.compareTo(commonOpeningBalanceSum)!=Constant.Number.ZERO){
                commonDirection=Constant.BalanceDirectionName.DEBIT;
            }
            if(zore.compareTo(dOpeningBalanceTotal)!=Constant.Number.ZERO){
                dTotalDirection=Constant.BalanceDirectionName.DEBIT;
            }
            if(zore.compareTo(debtOpeningBalanceSum)!=Constant.Number.ZERO){
                debtDirection=Constant.BalanceDirectionName.CREDIT;
            }
            if(zore.compareTo(rightOpeningBalanceSum)!=Constant.Number.ZERO){
                rightDirection=Constant.BalanceDirectionName.CREDIT;
            }
            if(zore.compareTo(profitOpeningBalanceSum)!=Constant.Number.ZERO){
                profitDirection=Constant.BalanceDirectionName.CREDIT;
            }
            if(zore.compareTo(cOpeningBalanceTotal)!=Constant.Number.ZERO){
                cTotalDirection=Constant.BalanceDirectionName.CREDIT;
            }
            differenceOpeningBalance = dOpeningBalanceSum.subtract(cOpeningBalanceSum).abs();
            differenceAmount = thisYearDebitAmountSum.subtract(thisYearCreditAmountSum).abs();
            balanceSubjectInitVo.setDifferenceOpeningBalance(differenceOpeningBalance);
            balanceSubjectInitVo.setDifferenceAmount(differenceAmount);

            balanceSubjectInitVo.setAssetsOpeningBalanceSum(assetsOpeningBalanceSum);
            balanceSubjectInitVo.setAssetsThisYearDebitAmountSum(assetsThisYearDebitAmountSum);
            balanceSubjectInitVo.setAssetsThisYearCreditAmountSum(assetsThisYearCreditAmountSum);
            balanceSubjectInitVo.setAssetsDirection(assetsDirection);

            balanceSubjectInitVo.setCostOpeningBalanceSum(costOpeningBalanceSum);
            balanceSubjectInitVo.setCostThisYearDebitAmountSum(costThisYearDebitAmountSum);
            balanceSubjectInitVo.setCostThisYearCreditAmountSum(costThisYearCreditAmountSum);
            balanceSubjectInitVo.setCostDirection(costDirection);

            balanceSubjectInitVo.setCommonOpeningBalanceSum(commonOpeningBalanceSum);
            balanceSubjectInitVo.setCommonThisYearDebitAmountSum(commonThisYearDebitAmountSum);
            balanceSubjectInitVo.setCommonThisYearCreditAmountSum(commonThisYearCreditAmountSum);
            balanceSubjectInitVo.setCommonDirection(commonDirection);

            balanceSubjectInitVo.setDebitOpeningBalanceTotal(dOpeningBalanceTotal);
            balanceSubjectInitVo.setDebitThisYearDebitAmountTotal(dThisYearDebitAmountTotal);
            balanceSubjectInitVo.setDebitThisYearCreditAmountTotal(dThisYearCreditAmountTotal);
            balanceSubjectInitVo.setDebitTotalDirection(dTotalDirection);

            balanceSubjectInitVo.setDebtOpeningBalanceSum(debtOpeningBalanceSum);
            balanceSubjectInitVo.setDebtThisYearDebitAmountSum(debtThisYearDebitAmountSum);
            balanceSubjectInitVo.setDebtThisYearCreditAmountSum(debtThisYearCreditAmountSum);
            balanceSubjectInitVo.setDebtDirection(debtDirection);

            balanceSubjectInitVo.setRightOpeningBalanceSum(rightOpeningBalanceSum);
            balanceSubjectInitVo.setRightThisYearDebitAmountSum(rightThisYearDebitAmountSum);
            balanceSubjectInitVo.setRightThisYearCreditAmountSum(rightThisYearCreditAmountSum);
            balanceSubjectInitVo.setRightDirection(rightDirection);

            balanceSubjectInitVo.setProfitOpeningBalanceSum(profitOpeningBalanceSum);
            balanceSubjectInitVo.setProfitThisYearDebitAmountSum(profitThisYearDebitAmountSum);
            balanceSubjectInitVo.setProfitThisYearCreditAmountSum(profitThisYearCreditAmountSum);
            balanceSubjectInitVo.setProfitDirection(profitDirection);

            balanceSubjectInitVo.setCreditOpeningBalanceTotal(cOpeningBalanceTotal);
            balanceSubjectInitVo.setCreditThisYearDebitAmountTotal(cThisYearDebitAmountTotal);
            balanceSubjectInitVo.setCreditThisYearCreditAmountTotal(cThisYearCreditAmountTotal);
            balanceSubjectInitVo.setCreditTotalDirection(cTotalDirection);

        }
        return balanceSubjectInitVo;
    }

    /**
     * 获取帐簿信息
     * @Author lj
     * @Date:14:10 2019/8/29
     * @param initDto
     * @return com.njwd.entity.basedata.vo.AccountBookVo
     **/
    private AccountBookVo getAccountBook(BalanceSubjectInitDto initDto) {
        AccountBookDto accountBookDto = new AccountBookDto();
        accountBookDto.setId(initDto.getAccountBookId());
        return accountBookFeignClient.selectById(accountBookDto).getData();
    }

    /**
     * 拉取当前所在公司账簿，自动带出账簿中有数据的核算主体及当前所在核算主体，账簿对应科目信息,账簿启用期间
     *
     * @param balanceSubjectInitDto
     * @return com.njwd.ledger.entity.vo.BalanceSubjectInitVo
     * @Author lj
     * @Date:11:00 2019/7/31
     **/
    @Override
    public BalanceSubjectInitVo findAccountBookByComId(BalanceSubjectInitDto balanceSubjectInitDto) {
        //查询当前用户所在的账簿，以及对应的账簿启用期间
        BalanceSubjectInitVo balanceSubjectInitVo = new BalanceSubjectInitVo();
        SysUserVo userVo = UserUtils.getUserVo();
        AccountBookDto accountBookDto = new AccountBookDto();
        //accountBookDto.setCompanyId(balanceSubjectInitDto.getCompanyId());
        accountBookDto.setId(balanceSubjectInitDto.getAccountBookId());
        AccountBookVo accountBookVo = accountBookFeignClient.findAccountBookById(accountBookDto).getData();
        if (accountBookVo != null) {
            if(!Constant.Is.NO.equals(balanceSubjectInitDto.getRecordFlag())){
                //权限校验
                ShiroUtils.checkPerm(Constant.MenuDefine.SUBJECT_EDIT,accountBookVo.getCompanyId());
            }
            balanceSubjectInitVo.setAccountBookId(accountBookVo.getId());
            balanceSubjectInitVo.setAccountBookName(accountBookVo.getName());
            balanceSubjectInitVo.setSubjectId(accountBookVo.getSubjectId());
            balanceSubjectInitVo.setSubjectName(accountBookVo.getSubjectName());
            balanceSubjectInitVo.setHasSubAccount(accountBookVo.getHasSubAccount());
            AccountBookSystemDto accountBookSystemDto = new AccountBookSystemDto();
            accountBookSystemDto.setAccountBookId(accountBookVo.getId());
            accountBookSystemDto.setRootEnterpriseId(userVo.getRootEnterpriseId());
            Page<AccountBookSystemVo> accountBookSystemVoPage = accountBookSystemFeignClient.findLedgerList(accountBookSystemDto).getData();
            List<AccountBookSystemVo> accountBookSystemList = accountBookSystemVoPage.getRecords();
            for (AccountBookSystem accountBookSystem : accountBookSystemList) {
                if (Constant.SystemSignValue.LEDGER.equals(accountBookSystem.getSystemSign())) {
                    balanceSubjectInitVo.setPeriodYear(accountBookSystem.getPeriodYear());
                    balanceSubjectInitVo.setPeriodNum(accountBookSystem.getPeriodNum());
                    balanceSubjectInitVo.setAccountBookSystemId(accountBookSystem.getId());
                }
            }

            //查询账簿的下的核算主体
            List<Long> accountBookIdList= new ArrayList<Long>();
            accountBookIdList.add(accountBookVo.getId());
            AccountBookEntityDto accountBookEntityDto = new AccountBookEntityDto();
            accountBookEntityDto.setAccountBookIdList(accountBookIdList);
            List<AccountBookEntityVo> accountBookEntityList = accountBookEntityFeignClient.findAuthOperationalEntityList(accountBookEntityDto).getData().getRecords();
            balanceSubjectInitVo.setEntityList(accountBookEntityList);
        }
        return balanceSubjectInitVo;
    }

    /**
     * a)	当前账簿启用二级核算，核算主体默认选中当前所在核算主体，科目中显示属性为内部往来的科目；
     * b)	当前账簿未启用二级核算，核算主体默认选中当前唯一核算主体且置灰不可选择，科目中不显示属性为内部往来的科目
     *
     * @param balanceSubjectInitDto
     * @return List<AccountSubjectVo>
     * @Author lj
     * @Date:10:12 2019/7/29
     **/
    @Override
    public List<AccountSubjectVo> findSubject(BalanceSubjectInitDto balanceSubjectInitDto) {
//        //并发版本号
//        Integer version = (Integer) RedisUtils.getObj(balanceSubjectInitDto.getAccountBookEntityId().toString());
//        if(version==null){
//            version=Constant.Number.ONE;
//            RedisUtils.set(balanceSubjectInitDto.getAccountBookEntityId().toString(),version,30, TimeUnit.MINUTES);
//        }

        List<AccountSubjectVo> accountSubjectList;
        //未启用二级核算
        if (Constant.Is.NO.equals(balanceSubjectInitDto.getHasSubAccount())) {
            AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
            accountSubjectDto.setSubjectId(balanceSubjectInitDto.getSubjectId());
            accountSubjectDto.setIsInterior(Constant.Is.NO);
            accountSubjectList = accountSubjectFeignClient.findInfoForLedger(accountSubjectDto).getData();
        } else {
            AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
            accountSubjectDto.setSubjectId(balanceSubjectInitDto.getSubjectId());
            accountSubjectList = accountSubjectFeignClient.findInfoForLedger(accountSubjectDto).getData();
        }

        //获取科目期初数据
        List<BalanceSubjectInit> balanceSubjectInitList = balanceSubjectInitMapper.selectList(new LambdaQueryWrapper<BalanceSubjectInit>()
                .eq(BalanceSubjectInit::getAccountBookEntityId, balanceSubjectInitDto.getAccountBookEntityId())
                .eq(BalanceSubjectInit::getPeriodYear, balanceSubjectInitDto.getPeriodYear())
                .eq(BalanceSubjectInit::getPeriodNum, balanceSubjectInitDto.getPeriodNum()));
        if (CollectionUtils.isNotEmpty(accountSubjectList) && CollectionUtils.isNotEmpty(balanceSubjectInitList)) {
            MergeUtil.merge(accountSubjectList, balanceSubjectInitList,
                    accountSubjectVo -> accountSubjectVo.getId(),balanceSubjectInit->balanceSubjectInit.getAccountSubjectId(),
                    (accountSubjectVo, balanceSubjectInit) -> {
                        accountSubjectVo.setOpeningBalance(balanceSubjectInit.getOpeningBalance());
                        accountSubjectVo.setThisYearDebitAmount(balanceSubjectInit.getThisYearDebitAmount());
                        accountSubjectVo.setThisYearCreditAmount(balanceSubjectInit.getThisYearCreditAmount());
                        accountSubjectVo.setYearOpeningBalance(balanceSubjectInit.getYearOpeningBalance());
                    });
            //科目code与期初余额Map
            Map<String,BigDecimal> openingBalanceMap = accountSubjectList.stream().filter(t -> (t.getOpeningBalance() != null || t.getThisYearCreditAmount() != null ||
                    t.getThisYearDebitAmount() != null || t.getYearOpeningBalance() != null)).collect(Collectors.toMap(b->b.getCode()+Constant.Character.COMMA+b.getBalanceDirection(),
                    s->s.getOpeningBalance()==null?new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT):s.getOpeningBalance()));
            //科目code与本年贷方Map
            Map<String,BigDecimal> thisYearCreditAmountMap = accountSubjectList.stream().filter(t -> (t.getOpeningBalance() != null || t.getThisYearCreditAmount() != null ||
                    t.getThisYearDebitAmount() != null || t.getYearOpeningBalance() != null)).collect(Collectors.toMap(b->b.getCode()+Constant.Character.COMMA+b.getBalanceDirection(),
                    s->s.getThisYearCreditAmount()==null?new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT):s.getThisYearCreditAmount()));
            //科目code与本年借方Map
            Map<String,BigDecimal> thisYearDebitAmountMap = accountSubjectList.stream().filter(t -> (t.getOpeningBalance() != null || t.getThisYearCreditAmount() != null ||
                    t.getThisYearDebitAmount() != null || t.getYearOpeningBalance() != null)).collect(Collectors.toMap(b->b.getCode()+Constant.Character.COMMA+b.getBalanceDirection(),
                    s->s.getThisYearDebitAmount()==null?new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT):s.getThisYearDebitAmount()));
            //科目code与期初余额Map
            Map<String,BigDecimal> yearOpeningBalanceMap = accountSubjectList.stream().filter(t -> (t.getOpeningBalance() != null || t.getThisYearCreditAmount() != null ||
                    t.getThisYearDebitAmount() != null || t.getYearOpeningBalance() != null)).collect(Collectors.toMap(b->b.getCode()+Constant.Character.COMMA+b.getBalanceDirection(),
                    s->s.getYearOpeningBalance()==null?new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT):s.getYearOpeningBalance()));
            //末级科目数据自动汇总到上级，用于前端展示
            for(AccountSubjectVo accountSubjectVo : accountSubjectList){
//                accountSubjectVo.setVersion(version);
                if(Constant.Is.NO.equals(accountSubjectVo.getIsFinal())){
                    BigDecimal openingBalance = getSubjectMoney(openingBalanceMap,accountSubjectVo.getCode(),accountSubjectVo.getBalanceDirection(),true);
                    BigDecimal thisYearCreditAmount = getSubjectMoney(thisYearCreditAmountMap,accountSubjectVo.getCode(),accountSubjectVo.getBalanceDirection(),false);
                    BigDecimal thisYearDebitAmount = getSubjectMoney(thisYearDebitAmountMap,accountSubjectVo.getCode(),accountSubjectVo.getBalanceDirection(),false);
                    BigDecimal yearOpeningBalance = getSubjectMoney(yearOpeningBalanceMap,accountSubjectVo.getCode(),accountSubjectVo.getBalanceDirection(),true);
                    if(openingBalance!=null){
                        accountSubjectVo.setOpeningBalance(openingBalance);
                    }
                    if(thisYearCreditAmount!=null){
                        accountSubjectVo.setThisYearCreditAmount(thisYearCreditAmount);
                    }
                    if(thisYearDebitAmount!=null){
                        accountSubjectVo.setThisYearDebitAmount(thisYearDebitAmount);
                    }
                    if(yearOpeningBalance!=null){
                        accountSubjectVo.setYearOpeningBalance(yearOpeningBalance);
                    }
                }
            }
        }
        return accountSubjectList;
    }

    /**
     * 自动层层汇总至上级现科目金额
     *
     * @param subMap,code
     * @return java.math.BigDecimal
     * @Author lj
     * @Date:10:13 2019/8/9
     **/
    private BigDecimal getSubjectMoney(Map<String, BigDecimal> subMap, String code,Byte balanceDirection,Boolean flag) {
        BigDecimal result = null;
        for (Map.Entry<String, BigDecimal> entry : subMap.entrySet()) {
            //根据末级科目code去计算所有包含当前code的金额
            String entryCode = entry.getKey().split(Constant.Character.COMMA)[Constant.Number.ZERO];
            Byte entryBalanceDirection = Byte.valueOf(entry.getKey().split(Constant.Character.COMMA)[Constant.Number.ONE]);
            if (entryCode.startsWith(code)) {
                if(result==null){
                    result=new BigDecimal(LedgerConstant.BalanceInit.BIG_DECIMAL_INIT);
                }
                if(flag){
                    //方向一致加，不一致减
                    if(balanceDirection.equals(entryBalanceDirection)){
                        result = result.add(entry.getValue());
                    }else {
                        result = result.subtract(entry.getValue());
                    }
                }else {
                    result = result.add(entry.getValue());
                }
            }
        }
        return result;
    }

    /**
     * 查询期初辅助核算信息
     *
     * @param balanceSubjectInitDto
     * @return BalanceSubjectInitVo
     * @Author lj
     * @Date:17:25 2019/8/22
     **/
    @Override
    public BalanceSubjectInitVo findAuxInfo(BalanceSubjectInitDto balanceSubjectInitDto) {
        BalanceSubjectInitVo balanceSubjectInitVo = new BalanceSubjectInitVo();
        List<LinkedHashMap> auxList = new ArrayList<LinkedHashMap>();
        List<BalanceSubjectInitExtVo> titleInfo = new ArrayList<>();
        balanceSubjectInitVo.setAuxList(auxList);
        balanceSubjectInitVo.setTitleInfo(titleInfo);
        //查询辅助核算信息
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        List<Long> ids = new ArrayList<Long>();
        ids.add(balanceSubjectInitDto.getAccountSubjectId());
        accountSubjectDto.setIds(ids);
        Map<Long, Map<String, Object>> oMap = accountSubjectFeignClient.findSourceTableInfo(accountSubjectDto).getData();
        if(oMap!=null&&oMap.size()==0){
            throw new ServiceException(ResultCode.AUXILIARY_ITEM_LIST_NOT_EXIST);
        }
        Map<String, Object> subMap= oMap.get(balanceSubjectInitDto.getAccountSubjectId());
        List<Map<String,String>> accSubAuxList = (List<Map<String,String>>) subMap.get(Constant.PropertyName.ACCOUNT_SUBJECT_AUXILIARY_LIST);
        //根据sourceTable对辅助核算信息进行排序
        accSubAuxList.sort(Comparator.comparing(map -> map.get(LedgerConstant.AuxiliaryItem.AUXILIARY_SOURCE_TABLE)));
        //查询期初表里的辅助核算信息
        List<BalanceSubjectInitAuxiliary> balanceSubjectInitAuxiliaries = balanceSubInitAuxiliaryMapper.selectList(new LambdaQueryWrapper<BalanceSubjectInitAuxiliary>()
                .eq(BalanceSubjectInitAuxiliary::getAccountBookEntityId,balanceSubjectInitDto.getAccountBookEntityId())
                .eq(BalanceSubjectInitAuxiliary::getAccountSubjectId,balanceSubjectInitDto.getAccountSubjectId())
                .eq(BalanceSubjectInitAuxiliary::getPeriodYear,balanceSubjectInitDto.getPeriodYear())
                .eq(BalanceSubjectInitAuxiliary::getPeriodNum,balanceSubjectInitDto.getPeriodNum()));
        Map<String,String> sourceTableInfoMap = getSourceTableInfoMap();
        if(CollectionUtils.isEmpty(balanceSubjectInitAuxiliaries)){
            //拼接表头信息
            addTitile(titleInfo, accSubAuxList);
        }else {
            //拼接表头信息
            addTitile(titleInfo, accSubAuxList);
            //拼接行数据
            LinkedHashMap auxMap = null;
            for(BalanceSubjectInitAuxiliary subjectInitAuxiliary:balanceSubjectInitAuxiliaries){
                auxMap = new LinkedHashMap();
                //查询辅助核算项信息
                List<BalanceSubjectInitAuxiliaryItem> itemList = balanceSubInitAuxItemMapper.selectList(new LambdaQueryWrapper<BalanceSubjectInitAuxiliaryItem>()
                        .eq(BalanceSubjectInitAuxiliaryItem::getBalanceAuxiliaryId, subjectInitAuxiliary.getId())
                        .orderBy(true, true, BalanceSubjectInitAuxiliaryItem::getSourceTable, BalanceSubjectInitAuxiliaryItem::getItemValueId));
                for(BalanceSubjectInitAuxiliaryItem item:itemList){
                    if(item.getSourceTable().equals(Constant.TableName.ACCOUNTING_ITEM_VALUE)){
                        auxMap.put(sourceTableInfoMap.get(item.getSourceTable()+item.getItemValueId()),item.getItemValueId()+Constant.Character.VIRGULE+item.getItemValueName());
                    }else {
                        auxMap.put(item.getSourceTable(),item.getItemValueId()+Constant.Character.VIRGULE+item.getItemValueName());
                    }

                }
                auxMap.put(LedgerConstant.AuxiliaryItem.OPENING_BALANCE_KEY,subjectInitAuxiliary.getOpeningBalance());
                auxMap.put(LedgerConstant.AuxiliaryItem.THIS_YEAR_DEBIT_AMOUNT_KEY,subjectInitAuxiliary.getThisYearDebitAmount());
                auxMap.put(LedgerConstant.AuxiliaryItem.THIS_YEAR_CREDIT_AMOUNT_KEY,subjectInitAuxiliary.getThisYearCreditAmount());
                auxMap.put(LedgerConstant.AuxiliaryItem.YEAR_OPENING_BALANCE_KEY,subjectInitAuxiliary.getYearOpeningBalance());
                auxList.add(auxMap);
            }
        }
        return balanceSubjectInitVo;
    }

    private Map<String,String> getSourceTableInfoMap() {
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        List<String> sourceTableList = new ArrayList<String>();
        sourceTableList.add(Constant.TableName.ACCOUNTING_ITEM_VALUE);
        accountSubjectDto.setSourceTableList(sourceTableList);
        List<List<Map<String, Object>>> allSourceTableInfoList = accountSubjectFeignClient.findAllSourceTableInfo(accountSubjectDto).getData();
        //拼接SourceTable code map key 为SourceTable_code, value为id
        Map<String,String> sourceTableInfoMap = new HashMap<>();
        for(int i=0;i<allSourceTableInfoList.size();i++){
            List<Map<String, Object>> sourceTableInfoList = allSourceTableInfoList.get(i);
            String sourceTable=sourceTableList.get(i);
            for(Map<String, Object> sourceTableInfo : sourceTableInfoList){
                sourceTableInfoMap.put(sourceTable+sourceTableInfo.get(Constant.ColumnName.ID), sourceTable+sourceTableInfo.get(LedgerConstant.AuxiliaryItem.AUXILIARY_CODE));
            }
        }
        return sourceTableInfoMap;
    }

    /**
     * 添加表头信息
     * @Author lj
     * @Date:14:15 2019/8/29
     * @param titleInfo, accSubAuxList
     * @return void
     **/
    private void addTitile(List<BalanceSubjectInitExtVo> titleInfo, List<Map<String,String>> accSubAuxList) {
        LinkedHashMap auxMap = new LinkedHashMap();
        BalanceSubjectInitExtVo balanceSubjectInitExtVo = null;
        for(Map auxiliary:accSubAuxList){
            balanceSubjectInitExtVo = new BalanceSubjectInitExtVo();
            if(auxiliary.get(LedgerConstant.AuxiliaryItem.AUXILIARY_SOURCE_TABLE).equals(Constant.TableName.ACCOUNTING_ITEM_VALUE)){
                balanceSubjectInitExtVo.setKey(String.valueOf(auxiliary.get(LedgerConstant.AuxiliaryItem.AUXILIARY_SOURCE_TABLE))+auxiliary.get(LedgerConstant.AuxiliaryItem.AUXILIARY_CODE));
                balanceSubjectInitExtVo.setType(Constant.Is.YES);
                auxMap.put(String.valueOf(auxiliary.get(LedgerConstant.AuxiliaryItem.AUXILIARY_SOURCE_TABLE))+auxiliary.get(LedgerConstant.AuxiliaryItem.AUXILIARY_CODE),auxiliary.get(LedgerConstant.AuxiliaryItem.AUXILIARY_NAME));
            }else {
                balanceSubjectInitExtVo.setKey(String.valueOf(auxiliary.get(LedgerConstant.AuxiliaryItem.AUXILIARY_SOURCE_TABLE)));
                balanceSubjectInitExtVo.setType(Constant.Is.NO);
                auxMap.put(String.valueOf(auxiliary.get(LedgerConstant.AuxiliaryItem.AUXILIARY_SOURCE_TABLE)),auxiliary.get(LedgerConstant.AuxiliaryItem.AUXILIARY_NAME));
            }
            balanceSubjectInitExtVo.setName(String.valueOf(auxiliary.get(LedgerConstant.AuxiliaryItem.AUXILIARY_NAME)));
            titleInfo.add(balanceSubjectInitExtVo);
        }
    }

    /**
     * 获取账簿初始化状态
     * @Author lj
     * @Date:14:10 2019/8/29
     * @param balanceSubjectInitDto
     * @return java.lang.Byte
     **/
    private Byte getIsInitalized(BalanceSubjectInitDto balanceSubjectInitDto) {
        AccountBookSystemDto accountBookSystemDto = new AccountBookSystemDto();
        accountBookSystemDto.setAccountBookId(balanceSubjectInitDto.getAccountBookId());
        accountBookSystemDto.setSystemSign(Constant.SystemSignValue.LEDGER);
        List<AccountBookSystemVo> accountBookSystemVos = accountBookSystemFeignClient.findEnableList(accountBookSystemDto).getData();
        Byte isInitalized = null;
        if (!org.springframework.util.CollectionUtils.isEmpty(accountBookSystemVos)) {
            AccountBookSystemVo accountBookSystemVo = accountBookSystemVos.get(Constant.Number.ZERO);
            isInitalized = accountBookSystemVo.getIsInitalized();
        }
        return isInitalized;
    }

    /**
     * 查询帐簿下的核算主体
     *
     * @param balanceSubjectInitDto
     * @return Page<AccountBookEntityVo>
     * @Author lj
     * @Date:17:04 2019/8/1
     **/
    @Override
    public Page<AccountBookEntityVo> findBookEntityPageByComId(BalanceSubjectInitDto balanceSubjectInitDto) {
        List<Long> accountBookIdList= new ArrayList<Long>();
        //获取账簿初始化状态
        Byte isInitalized = getIsInitalized(balanceSubjectInitDto);
        //查询账簿状态是否初始化，已初始化的账簿不需要查询数据
        List<BalanceInitRecord> balanceInitRecordList = balanceInitRecordMapper.selectList(new LambdaQueryWrapper<BalanceInitRecord>()
                .eq(BalanceInitRecord::getAccountBookId,balanceSubjectInitDto.getAccountBookId()));
        if(CollectionUtils.isNotEmpty(balanceInitRecordList)){
            if(!LedgerConstant.SubjectStatus.INIT_ED.equals(balanceInitRecordList.get(Constant.Number.ZERO).getSubjectStatus())){
                accountBookIdList.add(balanceSubjectInitDto.getAccountBookId());
            }
        }else {
            if(Constant.Is.NO.equals(isInitalized)){
                accountBookIdList.add(balanceSubjectInitDto.getAccountBookId());
            }
        }
        AccountBookEntityDto accountBookEntityDto = new AccountBookEntityDto();
        accountBookEntityDto.setCodeOrName(balanceSubjectInitDto.getCodeOrName());
        accountBookEntityDto.setAccountBookIdList(accountBookIdList);
        accountBookEntityDto.setSelectedIdList(balanceSubjectInitDto.getSelectedIdList());
        accountBookEntityDto.setPage(balanceSubjectInitDto.getPage());
        return accountBookEntityFeignClient.findAuthOperationalEntityPage(accountBookEntityDto).getData();
    }
    
    /**
     * @description: 导出excel
     * @param: [balanceSubjectInitDto, response]
     * @return: void 
     * @author: xdy        
     * @create: 2019-10-22 15:08 
     */
    @Override
    public void exportExcel(BalanceSubjectInitDto balanceSubjectInitDto, HttpServletResponse response) {
        List<AccountSubjectVo> accountSubjectVoList = findSubject(balanceSubjectInitDto);
        checkAccoutBookStatusTwo(balanceSubjectInitDto);
        fileService.exportExcel(response,accountSubjectVoList, ExcelColumnConstant.SubjectInit.CODE
                ,ExcelColumnConstant.SubjectInit.NAME
                ,ExcelColumnConstant.SubjectInit.BALANCE_DIRECTION
                ,ExcelColumnConstant.SubjectInit.OPENING_BALANCE
                ,ExcelColumnConstant.SubjectInit.THIS_YEAR_DEBIT_AMOUNT
                ,ExcelColumnConstant.SubjectInit.THIS_YEAR_CREDIT_AMOUNT
                ,ExcelColumnConstant.SubjectInit.YEAR_OPENING_BALANCE);
    }
}
