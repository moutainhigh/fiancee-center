package com.njwd.financeback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.cloudclient.AccountingPeriodFeignClient;
import com.njwd.basedata.cloudclient.ParameterSetLedgerFeignClient;
import com.njwd.basedata.cloudclient.SysSystemFeignClient;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.AccountBookSystem;
import com.njwd.entity.basedata.BatchOperationDetails;
import com.njwd.entity.basedata.BatchOperationMessage;
import com.njwd.entity.basedata.ReferenceResult;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.AccountBookSystemDto;
import com.njwd.entity.basedata.vo.AccountBookSystemVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.dto.ParameterSetDto;
import com.njwd.entity.ledger.vo.ParameterSetVo;
import com.njwd.entity.platform.dto.AccountingPeriodDto;
import com.njwd.entity.platform.dto.SysSystemDto;
import com.njwd.entity.platform.vo.AccountingPeriodVo;
import com.njwd.entity.platform.vo.SysSystemVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.financeback.cloudclient.AccountBookPeriodFeignClient;
import com.njwd.financeback.mapper.AccountBookSystemMapper;
import com.njwd.financeback.service.AccountBookService;
import com.njwd.financeback.service.AccountBookSystemService;
import com.njwd.service.ReferenceRelationService;
import com.njwd.utils.FastUtils;
import com.njwd.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 账簿启用子系统记录
 *
 * @Author: Zhuzs
 * @Date: 2019-06-25 16:53
 */
@Service
public class AccountBookSystemServiceImpl implements AccountBookSystemService {
    @Resource
    private AccountBookSystemMapper accountBookSystemMapper;
    @Autowired
    private SysSystemFeignClient sysSystemFeignClient;
    @Autowired
    private AccountBookPeriodFeignClient accountBookPeriodFeignClient;
    @Autowired
    private AccountingPeriodFeignClient accountingPeriodFeignClient;
    @Autowired
    private AccountBookService accountBookService;
    @Autowired
    private ReferenceRelationService referenceRelationService;
    @Resource
    private AccountBookSystemService accountBookSystemService;
    @Resource
    private ParameterSetLedgerFeignClient parameterSetLedgerFeignClient;

    /**
     * 启用子系统
     *
     * @param: [accountBookDto]
     * @return: int
     * @author: zhuzs
     * @date: 2019-09-16 17:47
     */
    @Override
    @Transactional
    public int enableAccountBookSystem(AccountBookDto accountBookDto) {
        SysUserVo operator = UserUtils.getUserVo();
        List<AccountBookSystemDto> accountBookSystemDtos = accountBookDto.getAccountBookSystemDtoList();
        for(AccountBookSystemDto accountBookSystemDto:accountBookSystemDtos){
            // 启用子系统
            addAccountBookSystem(operator,accountBookSystemDto,accountBookDto);
            // 新增账簿期间
            addAccountBookPeriodByParam(operator,accountBookSystemDto,accountBookDto);

            // 更新账簿 操作日志信息
            accountBookService.updateAccountBook(accountBookDto);

        }
        return 1;
    }

    /**
     * 批量 启用子系统
     *
     * @param: [accountBookDtos]
     * @return: com.njwd.entity.basedata.BatchOperationDetails
     * @author: zhuzs
     * @date: 2019-09-16 17:47
     */
    @Override
    @Transactional
    public BatchOperationDetails enableAccountBookSystemBatch(List<AccountBookDto> accountBookDtos) {
        SysUserVo operator = UserUtils.getUserVo();

        // 操作详情
        BatchOperationDetails result = new BatchOperationDetails();
        List<BatchOperationMessage> batchOperationMessageArrayList = new ArrayList<>();
        List<Long> success = new ArrayList<>();

        for(AccountBookDto accountBookDto:accountBookDtos){
            // 待启用子系统
            List<AccountBookSystemDto> accountBookSystemsToStart = accountBookDto.getAccountBookSystemDtoList();
            // 已启用子系统
            AccountBookSystemDto accountBookSystemDto = new AccountBookSystemDto();
            accountBookSystemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
            accountBookSystemDto.setAccountBookId(accountBookDto.getId());
            List<AccountBookSystemVo> accountBookSystemEnable = findEnableList(accountBookSystemDto);

            // 暂未启用任何子系统
            if(null==accountBookSystemEnable || Constant.Number.ZERO.equals(accountBookSystemEnable.size())){
                for(AccountBookSystemDto accToStart:accountBookSystemsToStart){
                    // 新增 账簿启用子系统记录
                    addAccountBookSystem(operator,accToStart,accountBookDto);
                    // 新增账簿期间
                    addAccountBookPeriodByParam(operator,accToStart,accountBookDto);

                    BatchOperationMessage batchOperationMessage = new BatchOperationMessage();
                    batchOperationMessage.setId(accountBookDto.getId());
                    batchOperationMessage.setStatus(Constant.Character.SUCCESS);
                    batchOperationMessage.setDetails("启用:"+'"'+accountBookDto.getCode()+'"'+accountBookDto.getName()+accToStart.getSystemName());
                    batchOperationMessage.setSign(accToStart.getSystemSign());
                    batchOperationMessageArrayList.add(batchOperationMessage);
                }
            }else{
                // 已启用子系统
                boolean flag;
                for(AccountBookSystemDto accToStart:accountBookSystemsToStart){
                    BatchOperationMessage batchOperationMessage = new BatchOperationMessage();
                    flag = true;
                    for(AccountBookSystemVo accEnable:accountBookSystemEnable){
                        if((accEnable.getSystemSign().equals(accToStart.getSystemSign()))){
                            flag = false;
                            break;
                        }
                    }

                    if(flag){
                        // 新增 账簿启用子系统记录
                        addAccountBookSystem(operator,accToStart,accountBookDto);
                        // 新增 账簿期间
                        addAccountBookPeriodByParam(operator,accToStart,accountBookDto);

                        batchOperationMessage.setStatus(Constant.Character.SUCCESS);
                    }else{
                        batchOperationMessage.setStatus(Constant.Character.FAIL);
                    }
                    success.add(accountBookDto.getId());
                    batchOperationMessage.setSign(accToStart.getSystemSign());
                    batchOperationMessage.setDetails("启用:"+'"'+accountBookDto.getCode()+'"'+accountBookDto.getName()+accToStart.getSystemName());
                    batchOperationMessage.setId(accountBookDto.getId());
                    batchOperationMessageArrayList.add(batchOperationMessage);
                 }
            }

            // 更新账簿 操作日志信息
            accountBookService.updateAccountBook(accountBookDto);

            result.setSuccessIds(success);
            result.setBatchOperationMessageList(batchOperationMessageArrayList);
        }
        return result;
    }

    /**
     * 反启用子系统
     *
     * @param: [accountBookDto]
     * @return: com.njwd.entity.basedata.BatchOperationDetails
     * @author: zhuzs
     * @date: 2019-09-16 17:47
     */
    @Override
    @Transactional
    public BatchOperationDetails antiEnableAccountBookSystem(AccountBookDto accountBookDto) {
        // 操作详情
        BatchOperationDetails result = new BatchOperationDetails();
        List<BatchOperationMessage> batchOperationMessageArrayList = new ArrayList<>();

        List<AccountBookSystemDto> accountBookSystemDtos = accountBookDto.getAccountBookSystemDtoList();
        for(AccountBookSystemDto accountBookSystemDto:accountBookSystemDtos){

            //
            ReferenceResult referenceResult = referenceRelationService.isReference(Constant.Reference.ACCOUNT_BOOK_SYSTEM,accountBookSystemDto.getId());
            // 被引用
            BatchOperationMessage batchOperationMessage = new BatchOperationMessage();
            if(referenceResult.isReference()){
                batchOperationMessage.setStatus(Constant.Character.IS_REFERENCE);
                batchOperationMessage.setDetails(String.format(referenceResult.getReferenceDescription(),accountBookDto.getName(),accountBookSystemDto.getSystemName()));

            }else{
                // 未被引用
                accountBookSystemMapper.delete(new LambdaQueryWrapper<AccountBookSystem>()
                        .eq(AccountBookSystem::getAccountBookId,accountBookDto.getId())
                        .eq(AccountBookSystem::getSystemSign,accountBookSystemDto.getSystemSign()));

                //删除 该子系统关联的所有账簿期间
                AccountBookPeriod accountBookPeriod = new AccountBookPeriod();
                accountBookPeriod.setAccountBookId(accountBookDto.getId());
                accountBookPeriod.setSystemSign(accountBookSystemDto.getSystemSign());
                Integer row = accountBookPeriodFeignClient.deleteByAccountBookIdAndSystemSign(accountBookPeriod).getData();
                if(row <= Constant.Number.ZERO){
                    throw new ServiceException(ResultCode.OPERATION_FAILURE);
                }

                batchOperationMessage.setStatus(Constant.Character.SUCCESS);
            }
            batchOperationMessage.setId(accountBookSystemDto.getId());
            batchOperationMessage.setSign(accountBookSystemDto.getSystemSign());
            batchOperationMessageArrayList.add(batchOperationMessage);
        }
        result.setBatchOperationMessageList(batchOperationMessageArrayList);

        // 更新账簿 操作日志信息
        accountBookService.updateAccountBook(accountBookDto);
        return result;
    }

    /**
     * 批量 反启用
     *
     * @param: [accountBookDtos]
     * @return: com.njwd.entity.basedata.BatchOperationDetails
     * @author: zhuzs
     * @date: 2019-09-16 17:46
     */
    @Override
    @Transactional
    public  BatchOperationDetails antiEnableAccountBookSystemBatch(List<AccountBookDto> accountBookDtos) {
        SysUserVo operator = UserUtils.getUserVo();
        // 操作详情
        BatchOperationDetails result = new BatchOperationDetails();
        List<BatchOperationMessage> batchOperationMessageArrayList = new ArrayList<>();
        List<Long> success = new ArrayList<>();

        for(AccountBookDto accountBookDto:accountBookDtos){
            // 待反启用子系统
            List<AccountBookSystemDto> toAntiEnable = accountBookDto.getAccountBookSystemDtoList();
            AccountBookSystemDto accountBookSystemDto = new AccountBookSystemDto();
            accountBookSystemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
            accountBookSystemDto.setAccountBookId(accountBookDto.getId());
            // 已启用子系统
            List<AccountBookSystemVo> accountBookSystemEnable = findEnableList(accountBookSystemDto);

            // 暂未启用任何子系统
            if(null == accountBookSystemEnable || Constant.Number.ZERO.equals(accountBookSystemEnable.size())){
                for(AccountBookSystem accToAntiEnable : toAntiEnable){
                    BatchOperationMessage batchOperationMessage = new BatchOperationMessage();
                    batchOperationMessage.setId(accountBookDto.getId());
                    batchOperationMessage.setStatus(Constant.Character.FAIL);
                    batchOperationMessage.setDetails("反启用:"+'"'+accountBookDto.getCode()+'"'+accountBookDto.getName()+accToAntiEnable.getSystemName());
                    batchOperationMessageArrayList.add(batchOperationMessage);
                }
            }else{
                // 已启用
                boolean flag ;
                for(AccountBookSystem accToAntiEnable : toAntiEnable){
                    BatchOperationMessage batchOperationMessage = new BatchOperationMessage();
                    flag = false;
                    for(AccountBookSystemVo accEnable : accountBookSystemEnable){
                        if((accEnable.getSystemSign().equals(accToAntiEnable.getSystemSign()))){
                            //TODO 反启用子系统校验规则（引用状态）
                            ReferenceResult referenceResult = referenceRelationService.isReference(Constant.Reference.ACCOUNT_BOOK_SYSTEM,accEnable.getId());
                            if(!referenceResult.isReference()){
                                flag = true;
                            }else{
                                batchOperationMessage.setDetails(String.format(referenceResult.getReferenceDescription(),accountBookDto.getName(),accToAntiEnable.getSystemName()));
                            }
                            break;
                        }
                    }


                    if(flag){

                        // 删除 账簿启用子系统记录
                        accountBookSystemMapper.delete(new LambdaQueryWrapper<AccountBookSystem>()
                                .eq(AccountBookSystem::getAccountBookId,accountBookDto.getId())
                                .eq(AccountBookSystem::getSystemSign,accToAntiEnable.getSystemSign()));

                        //账簿ID和子系统标识 删除账簿期间数据
                        AccountBookPeriod accountBookPeriod = new AccountBookPeriod();
                        accountBookPeriod.setAccountBookId(accountBookDto.getId());
                        accountBookPeriod.setSystemSign(accToAntiEnable.getSystemSign());
                        Integer row = accountBookPeriodFeignClient.deleteByAccountBookIdAndSystemSign(accountBookPeriod).getData();
                        if(row <= Constant.Number.ZERO){
                            throw new ServiceException(ResultCode.OPERATION_FAILURE);
                        }
                        result.setSuccessIds(success);
                        batchOperationMessage.setStatus(Constant.Character.SUCCESS);

                    }else{
                        batchOperationMessage.setStatus(Constant.Character.IS_REFERENCE);

                    }

                    batchOperationMessage.setSign(accToAntiEnable.getSystemSign());
                    success.add(accountBookDto.getId());
                    batchOperationMessage.setId(accountBookDto.getId());
                    batchOperationMessage.setDetails("反启用:"+'"'+accountBookDto.getCode()+'"'+accountBookDto.getName()+accToAntiEnable.getSystemName());
                    batchOperationMessageArrayList.add(batchOperationMessage);
                }
            }

            // 更新账簿 操作日志信息
            accountBookService.updateAccountBook(accountBookDto);

            result.setBatchOperationMessageList(batchOperationMessageArrayList);
        }
        return result;
    }

    /**
     * 根据ID 修改子系统初始化状态
     *
     * @param: [accountBookSystemDto]
     * @return: java.lang.Integer
     * @author: zhuzs
     * @date: 2019-09-16 17:46
     */
    @Override
    public Integer updateById(AccountBookSystemDto accountBookSystemDto) {
        return accountBookSystemMapper.update(accountBookSystemDto,new LambdaQueryWrapper<AccountBookSystem>().
                eq(AccountBookSystem::getId,accountBookSystemDto.getId()));
    }

    /**
     * 初始化子系统
     *
     * @param: [accountBookSystemDto]
     * @return: java.lang.Integer
     * @author: zhuzs
     * @date: 2019-09-16 17:46
     */
    @Override
    public Integer initSystemById(AccountBookSystemDto accountBookSystemDto) {
        AccountBookSystem accountBookSystem = accountBookSystemMapper.selectById(accountBookSystemDto.getId());
        // 校验是否被删除
        checkIsDel(accountBookSystem);
        // 检验是否已初始化
        if(accountBookSystem.getIsInitalized().equals(Constant.Number.INITLIZED)){
            throw new ServiceException(ResultCode.SYSTEM_ALREADY_INITLIZED);
        }

        accountBookSystemDto.setIsInitalized(Constant.Number.INITLIZED);
        return updateById(accountBookSystemDto);
    }

    /**
     * 反初始化子系统
     *
     * @param: [accountBookSystemDto]
     * @return: java.lang.Integer
     * @author: zhuzs
     * @date: 2019-09-16 17:46
     */
    @Override
    public Integer antiInitSystemById(AccountBookSystemDto accountBookSystemDto) {
        AccountBookSystem accountBookSystem = accountBookSystemMapper.selectById(accountBookSystemDto.getId());
        // 校验是否被删除
        checkIsDel(accountBookSystem);
        // 检验是否已反初始化
        if(accountBookSystem.getIsInitalized().equals(Constant.Number.ANTI_INITLIZED)){
            throw new ServiceException(ResultCode.SYSTEM_ALREADY_ANTIINITLIZED);
        }

        accountBookSystemDto.setIsInitalized(Constant.Number.ANTI_INITLIZED);
        return updateById(accountBookSystemDto);
    }

    /**
     * 根据 账簿ID/子系统标识 查询已启用子系统
     *
     * @param: [accountBookSystemDto]
     * @return: java.util.List<com.njwd.entity.basedata.vo.AccountBookSystemVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:46
     */
    @Override
    public List<AccountBookSystemVo> findEnableList(AccountBookSystemDto accountBookSystemDto) {
        return accountBookSystemMapper.findListByAccBookId(accountBookSystemDto);
    }

    /**
     * 查询 子系统及其状态 列表
     *
     * @param: [accountBookSystemDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.SysSystemVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:46
     */
    @Override
    public List<SysSystemVo> findList(AccountBookSystemDto accountBookSystemDto) {
        // 当前账簿已启用子系统
        List<AccountBookSystemVo> accountBookSystemListEnable = findEnableList(accountBookSystemDto);

        //整合系统启用状态
        SysSystemDto platformSysSystemDto = new SysSystemDto();
        platformSysSystemDto.setRootEnterpriseId(Constant.Number.ZEROL);
        List<SysSystemVo> sysSystemVos = accountBookSystemService.findAccountBookSystemAll(platformSysSystemDto);
        for(AccountBookSystemVo enable: accountBookSystemListEnable){
            for(SysSystemVo all:sysSystemVos){
                if(enable.getSystemSign().equals(all.getSystemSign())){
                    all.setId(enable.getId());
                    all.setStatus(Constant.Is.YES);
                    all.setPeriodYear(enable.getPeriodYear());
                    all.setPeriodNum(enable.getPeriodNum());
                    all.setOperatorId(enable.getOperatorId());
                    all.setOperatorName(enable.getOperatorName());
                    all.setOperateTime(enable.getOperateTime());
                    all.setCashFlowEnableStatus(enable.getCashFlowEnableStatus());
                }
            }
        }
        return sysSystemVos;
    }

    /**
     * 根据子系统标识 查询当前企业已启用子系统列表
     *
     * @param: [accountBookSystemDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountBookSystemVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:46
     */
    @Override
    public Page<AccountBookSystemVo> findLedgerList(AccountBookSystemDto accountBookSystemDto) {
        Page<AccountBookSystemVo> page = accountBookSystemDto.getPage();
        return accountBookSystemMapper.findLedgerList(page,accountBookSystemDto);
    }

    //根据子系统标识 查询当前企业用户已启用子系统列表
    @Override
    public Page<AccountBookSystemVo> findLedgerListByUserId(AccountBookSystemDto accountBookSystemDto) {
        Page<AccountBookSystemVo> page = accountBookSystemDto.getPage();
        return accountBookSystemMapper.findLedgerListByUserId(page,accountBookSystemDto);
    }

    /**
     * 查询租户已购买的子系统
     *
     * @param: [platformSysSystemDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.SysSystemVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:45
     */
    @Override
    @Cacheable(value = Constant.RedisCache.ACCOUNT_BOOK_SYSTEM,key="#platformSysSystemDto.rootEnterpriseId",unless = "#result == null")
    public List<SysSystemVo> findAccountBookSystemAll(SysSystemDto platformSysSystemDto) {
        List<SysSystemVo> SysSystemVos = sysSystemFeignClient.findSysSytemList(platformSysSystemDto).getData();
        FastUtils.checkNull(SysSystemVos);
        return SysSystemVos;
    }

    /**
     * 根据账簿ID、子系统标识 查询账簿期间
     *
     * @param: [accountBookSystemDto, accountBookPeriod, accountBookDto]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 17:45
     */
    private void findPeriodByAccBookIdAndSystemSign(AccountBookSystemDto accountBookSystemDto,AccountBookPeriod accountBookPeriod,AccountBookDto accountBookDto){
        AccountingPeriodDto accountingPeriodDto = new AccountingPeriodDto();
        accountingPeriodDto.setPeriodYear(accountBookSystemDto.getPeriodYear());
        accountingPeriodDto.setPeriodNum(accountBookSystemDto.getPeriodNum());
        accountingPeriodDto.setIsAdjustment(Constant.Is.NO);
        accountingPeriodDto.setAccCalendarId(accountBookDto.getAccountingCalendarId());
        List<AccountingPeriodVo> accountingPeriodVos = accountingPeriodFeignClient.findAccPerByIsAdjAndAccCal(accountingPeriodDto).getData();
        // 非空校验
        FastUtils.checkNull(accountingPeriodVos);

        accountBookPeriod.setStartDate(accountingPeriodVos.get(0).getStartDate());
        accountBookPeriod.setEndDate(accountingPeriodVos.get(0).getEndDate());
    }

    /**
     * 依据配置 新增账簿期间
     *
     * @param: [operator, accToStart, accountBookDto]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 17:45
     */
    private void addAccountBookPeriodByParam(SysUserVo operator,AccountBookSystemDto accToStart,AccountBookDto accountBookDto){
        ParameterSetVo parameterSetVo = getParameterSetForLedger();
        if(parameterSetVo.getValue().longValue() == Constant.Number.TWOL.longValue()){
            addAccountBookPeriod(operator, accToStart, accountBookDto);
            if(accToStart.getPeriodNum().equals(Constant.PeriodNum.December)){
                accToStart.setPeriodYear(accToStart.getPeriodYear()+Constant.Number.ONE);
                accToStart.setPeriodNum(Constant.Number.ONEB);
            }else{
                accToStart.setPeriodNum((byte)(accToStart.getPeriodNum()+Constant.Number.ONE));
            }
            addAccountBookPeriod(operator, accToStart, accountBookDto);
        }else{
            addAccountBookPeriod(operator, accToStart, accountBookDto);
        }

    }

    /**
     * 新增账簿期间
     *
     * @param: [operator, accToStart, accountBookDto]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-18 16:56
     */
    private void addAccountBookPeriod(SysUserVo operator, AccountBookSystemDto accToStart, AccountBookDto accountBookDto) {
        AccountBookPeriod accountBookPeriod = new AccountBookPeriod();
        accountBookPeriod.setRootEnterpriseId(operator.getRootEnterpriseId());
        accountBookPeriod.setAccountBookId(accountBookDto.getId());
        accountBookPeriod.setAccountBookName(accountBookDto.getName());
        accountBookPeriod.setAccountBookCode(accountBookDto.getCode());
        accountBookPeriod.setAccountBookSystemId(accToStart.getId());
        accountBookPeriod.setSystemName(accToStart.getSystemName());
        accountBookPeriod.setSystemSign(accToStart.getSystemSign());
        accountBookPeriod.setPeriodYear(accToStart.getPeriodYear());
        accountBookPeriod.setPeriodNum(accToStart.getPeriodNum());
        accountBookPeriod.setPeriodYearNum(accToStart.getPeriodYear() * Constant.Number.ONEHUNDRED + accToStart.getPeriodNum());
        accountBookPeriod.setUpdatorId(operator.getUserId());
        accountBookPeriod.setUpdatorName(operator.getName());
        accountBookPeriod.setStatus(Constant.Is.YES);
        accountBookPeriod.setIsRevisePeriod(Constant.Is.NO);
        // 获取账簿期间起止时间
        findPeriodByAccBookIdAndSystemSign(accToStart, accountBookPeriod, accountBookDto);

        Integer row = accountBookPeriodFeignClient.addAccountBookPeriod(accountBookPeriod).getData();
        if (row != 1) {
            throw new ServiceException(ResultCode.OPERATION_FAILURE);
        }
    }

    /**
     * 新增 账簿启用子系统记录
     *
     * @param: [operator, accToStart, accountBookDto]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 17:45
     */
    private void addAccountBookSystem(SysUserVo operator,AccountBookSystemDto accToStart,AccountBookDto accountBookDto){
        accToStart.setRootEnterpriseId(operator.getRootEnterpriseId());
        accToStart.setAccountBookId(accountBookDto.getId());
        accToStart.setAccountBookName(accountBookDto.getName());
        accToStart.setOperatorId(operator.getUserId());
        accToStart.setOperatorName(operator.getName());
        accToStart.setStatus(Constant.Is.YES);
        Integer row = accountBookSystemMapper.insert(accToStart);
        if(row != 1){
            throw new ServiceException(ResultCode.OPERATION_FAILURE);
        }
    }

    /**
     * 校验是否被删除
     *
     * @param: [accountBookSystem]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 17:45
     */
    private void checkIsDel(AccountBookSystem accountBookSystem){
        if(accountBookSystem == null ){
            throw new ServiceException(ResultCode.RECORD_NOT_EXIST);
        }
    }

    /**
     * @description: 获取账簿期间系统
     * @param: [accountBookSystemDto]
     * @return: com.njwd.entity.basedata.AccountBookSystem
     * @author: xdy
     * @create: 2019-08-14 16-40
     */
    @Override
    public AccountBookSystem findAccountBookSystem(AccountBookSystemDto accountBookSystemDto){
        return accountBookSystemMapper.selectById(accountBookSystemDto.getId());
    }

    /**
     * @param accountBookSystemDto
     * @return java.lang.Byte
     * @Description 根据系统标识+账簿ID+会计年度+会计期间查询是否已初始化
     * @Author 朱小明
     * @Date 2019/8/14 16:54
     * @Param [accountBookSystemDto]
     */
    @Override
    public AccountBookSystem findInitStatusByCondition(AccountBookSystemDto accountBookSystemDto) {
        return accountBookSystemMapper.selectInitStatusByCondition(accountBookSystemDto);
    }

    /**
     * 获取 总账参数设置
     *
     * @param: []
     * @return: com.njwd.entity.ledger.vo.ParameterSetVo
     * @author: zhuzs
     * @date: 2019-09-18 16:45
     */
    private ParameterSetVo getParameterSetForLedger(){
        ParameterSetDto parameterSetDto = new ParameterSetDto();
        // key 为 未来期间数
        parameterSetDto.setKey(Constant.ParameterSetKey.FUTURE_PERIOD_NUM);
        // 账簿ID 给0
        parameterSetDto.setAccountBookId(Constant.Number.ZEROL);
        ParameterSetVo parameterSetVo = parameterSetLedgerFeignClient.findParameterSetValue(parameterSetDto).getData();
        if(parameterSetVo == null || parameterSetVo.getValue() == null){
            // 暂未配置 未来期间数
            throw new ServiceException(ResultCode.NOT_CONFIG);
        }
        return parameterSetVo;
    }
}
