package com.njwd.basedata.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.njwd.basedata.cloudclient.*;
import com.njwd.basedata.mapper.*;
import com.njwd.basedata.mapper.CostItemMapper;
import com.njwd.basedata.service.AccountSubjectService;
import com.njwd.basedata.service.CashFlowItemService;
import com.njwd.basedata.service.SysInitDataService;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.*;
import com.njwd.entity.basedata.CommonAbstract;
import com.njwd.entity.basedata.Currency;
import com.njwd.entity.basedata.vo.CommonAbstractVo;
import com.njwd.entity.basedata.vo.SysInitDataVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.dto.ParameterSetDto;
import com.njwd.entity.ledger.vo.ParameterSetVo;
import com.njwd.entity.platform.*;
import com.njwd.entity.platform.dto.*;
import com.njwd.entity.platform.vo.*;
import com.njwd.financeback.mapper.*;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.RedisUtils;
import com.njwd.utils.UserUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/6 14:12
 */
@Service
public class SysInitDataServiceImpl implements SysInitDataService {

    private Logger logger = LoggerFactory.getLogger(SysInitDataServiceImpl.class);
    private Map<String,IDataInit> initMap = new LinkedHashMap();
    @Resource
    private SysInitDataMapper sysInitDataMapper;
    @Resource
    private PlatformTransactionManager transactionManager;
    @Resource
    private SysInitDataFeignClient sysInitDataFeignClient;
    @Resource
    private SysMenuOptionFeignClient sysMenuOptionFeignClient;
    @Resource
    private SysMenuOptionMapper sysMenuOptionMapper;
    @Resource
    private CommonAbstractFeignClient commonAbstractFeignClient;
    @Resource
    private CommonAbstractMapper commonAbstractMapper;
    @Resource
    private ParameterSetPlatformFeignClient parameterSetPlatformFeignClient;
    @Resource
    private ParameterSetLedgerFeignClient parameterSetLedgerFeignClient;
    @Resource
    private AccountSubjectService accountSubjectService;
    @Resource
    private AccountSubjectMapper accountSubjectMapper;
    @Resource
    private AccountSubjectFeignClient accountSubjectFeignClient;
    @Resource
    private CashFlowItemService cashFlowItemService;
    @Resource
    private CashFlowItemFeignClient cashFlowItemFeignClient;
    @Resource
    private MenuControlStrategyMapper menuControlStrategyMapper;
    @Resource
    private MenuCodingRulesMapper menuCodingRulesMapper;
    @Resource
    private MenuCodingRulesFeignClient menuCodingRulesFeignClient;
    @Resource
    private MenuControlStrategyFeignClient menuControlStrategyFeignClient;
    @Resource
    private SysSystemFeignClient sysSystemFeignClient;
    @Resource
    private CurrencyMapper currencyMapper;
    @Resource
    private CurrencyFeignClient currencyFeignClient;
    @Resource
    private TaxCategoryMapper taxCategoryMapper;
    @Resource
    private TaxCategoryFeignClient taxCategoryFeignClient;
    @Resource
    private CostItemMapper costItemMapper;
    @Resource
    private CostItemFeignClient costItemFeignClient;
    @Resource
    private UnitMapper unitMapper;
    @Resource
    private UnitFeignClient unitFeignClient;
    @Resource
    private AccountBookCategoryMapper accountBookCategoryMapper;
    @Resource
    private AccountStandardFeignClient accountStandardFeignClient;


    /**
     * @description: 需要初始化数据模块
     * @param: []
     * @return:
     * @author: xdy
     * @create: 2019-08-14 14-27
     */
    SysInitDataServiceImpl(){
        initMap.put(Constant.DataInit.MODULE_CURRENCY,initCurrency());
        initMap.put(Constant.DataInit.MODULE_TAX_CATEGORY,initTaxCategory());
        initMap.put(Constant.DataInit.MODULE_COMMON_ABSTRACT,initCommonAbstract());
        initMap.put(Constant.DataInit.MODULE_COST_ITEM,initCostItem());
        initMap.put(Constant.DataInit.MODULE_UNIT,initUnit());
        initMap.put(Constant.DataInit.MODULE_ACCOUNT_BOOK_CATEGORY,initAccountBookCategory());

        initMap.put(Constant.DataInit.MODULE_CASH_FLOW,initCashFlow());
        initMap.put(Constant.DataInit.MODULE_SUBJECT,initSubject());
        initMap.put(Constant.DataInit.MODULE_PARAMETER_SET,initParameterSet());
        initMap.put(Constant.DataInit.MODULE_MENU_OPTION,initMenuOption());
        initMap.put(Constant.DataInit.MODULE_CREATE_TABLE,initCreateTable());
        initMap.put(Constant.DataInit.MODULE_SYS_SYSTEM,initSysSystem());
    }

    /**
     * @description: 初始化币种
     * @param: []
     * @return: com.njwd.basedata.service.impl.SysInitDataServiceImpl.IDataInit
     * @author: xdy
     * @create: 2019-11-12 17:03
     */
    private IDataInit initCurrency() {
        return context -> {
            context.setSystemSign(Constant.Context.BASE_DATA_FEIGN);
            SysUserVo userVo = UserUtils.getUserVo();
            Integer count = currencyMapper.selectCount(Wrappers.<com.njwd.entity.basedata.Currency>lambdaQuery().eq(com.njwd.entity.basedata.Currency::getRootEnterpriseId, userVo.getRootEnterpriseId()));
            if (count != null && count > 0) {
                logger.debug("租户{}模块{}数据已初始化", userVo.getRootEnterpriseId(), context.getDataSign());
                return true;
            }
            CurrencyDto currencyDto = new CurrencyDto();
            Result<List<CurrencyVo>> result = currencyFeignClient.findCurrencyList(currencyDto);
            List<CurrencyVo> platformCurrencyList = result.getData();
            if (platformCurrencyList == null) {
                logger.debug("模块{}无初始化数据", context.getDataSign());
                return false;
            }

            List<com.njwd.entity.basedata.vo.CurrencyVo> currencyList = new ArrayList<>();
            platformCurrencyList.forEach(platformCurrency -> {
                com.njwd.entity.basedata.Currency currency = new Currency();
                currency.setCode(platformCurrency.getCode());
                currency.setIsoCode(platformCurrency.getIsoCode());
                currency.setName(platformCurrency.getName());
                currency.setSymbol(platformCurrency.getSymbol());
                currency.setPrecision(platformCurrency.getPrecision());
                currency.setUnitPrecision(platformCurrency.getUnitPrecision());
                currency.setRoundingType(platformCurrency.getRoundingType());
                currency.setIsApproved(platformCurrency.getIsApproved());
                currency.setIsReleased(platformCurrency.getIsReleased());
                currency.setCreatorId(userVo.getCreatorId());
                currency.setCreatorName(userVo.getCreatorName());
                currency.setRootEnterpriseId(userVo.getRootEnterpriseId());
                currency.setPlatformId(currency.getId());
                currency.setVersion(Constant.Number.ZERO);
            });
            currencyMapper.addBatch(currencyList);
            return true;
        };
    }

    /**
     * @description: 初始化税种
     * @param: []
     * @return: com.njwd.basedata.service.impl.SysInitDataServiceImpl.IDataInit
     * @author: xdy
     * @create: 2019-11-12 17:04
     */
    private IDataInit initTaxCategory() {
        return context -> {
            context.setSystemSign(Constant.Context.BASE_DATA_FEIGN);
            SysUserVo userVo = UserUtils.getUserVo();
            Integer count = taxCategoryMapper.selectCount(Wrappers.<TaxCategory>lambdaQuery().eq(TaxCategory::getRootEnterpriseId,userVo.getRootEnterpriseId()));
            if(count!=null&&count>0){
                logger.debug("租户{}模块{}数据已初始化",userVo.getRootEnterpriseId(),context.getDataSign());
                return true;
            }
            TaxCategoryDto taxCategoryDto = new TaxCategoryDto();
            taxCategoryDto.setIsApproved(Constant.Is.YES);
            taxCategoryDto.setIsReleased(Constant.Is.YES);
            Result<List<TaxCategoryVo>> result = taxCategoryFeignClient.findTaxCategoryList(taxCategoryDto);
            List<TaxCategoryVo> taxCategoryList = result.getData();
            if(taxCategoryList==null){
                logger.debug("模块{}无初始化数据",context.getDataSign());
                return false;
            }
            taxCategoryList.forEach(taxCategory -> {
                taxCategory.setCreatorId(userVo.getUserId());
                taxCategory.setCreatorName(userVo.getName());
                taxCategory.setRootEnterpriseId(userVo.getRootEnterpriseId());
                taxCategory.setPlatformId(taxCategory.getId());
            });
            taxCategoryMapper.addBatch(taxCategoryList);
            return true;
        };
    }

    /**
     * @description: 初始化费用项目
     * @param: []
     * @return: com.njwd.basedata.service.impl.SysInitDataServiceImpl.IDataInit
     * @author: xdy
     * @create: 2019-11-12 17:05
     */
    private IDataInit initCostItem() {
        return context -> {
            context.setSystemSign(Constant.Context.BASE_DATA_FEIGN);
            SysUserVo userVo = UserUtils.getUserVo();
            Integer count = costItemMapper.selectCount(Wrappers.<CostItem>lambdaQuery().eq(CostItem::getRootEnterpriseId,userVo.getRootEnterpriseId()));
            if(count!=null&&count>0){
                logger.debug("租户{}模块{}数据已初始化",userVo.getRootEnterpriseId(),context.getDataSign());
                return true;
            }
            CostItemDto costItemDto = new CostItemDto();
            costItemDto.setIsApproved(Constant.Is.YES);
            costItemDto.setIsReleased(Constant.Is.YES);
            Result<List<CostItemVo>> result = costItemFeignClient.findCostItemList(costItemDto);
            List<CostItemVo> costItemList = result.getData();
            if(costItemList==null){
                logger.debug("模块{}无初始化数据",context.getDataSign());
                return false;
            }
            costItemList.forEach(costItem -> {
                costItem.setCreatorId(userVo.getUserId());
                costItem.setCreatorName(userVo.getName());
                costItem.setRootEnterpriseId(userVo.getRootEnterpriseId());
                costItem.setPlatformId(costItem.getId());
            });
            costItemMapper.addBatch(costItemList);
            return true;
        };
    }

    /**
     * @description: 初始化计量单位
     * @param: []
     * @return: com.njwd.basedata.service.impl.SysInitDataServiceImpl.IDataInit
     * @author: xdy
     * @create: 2019-11-12 17:06
     */
    private IDataInit initUnit() {
        return context -> {
            context.setSystemSign(Constant.Context.BASE_DATA_FEIGN);
            SysUserVo userVo = UserUtils.getUserVo();
            Integer count = unitMapper.selectCount(Wrappers.<Unit>lambdaQuery().eq(Unit::getRootEnterpriseId,userVo.getRootEnterpriseId()));
            if(count!=null&&count>0){
                logger.debug("租户{}模块{}数据已初始化",userVo.getRootEnterpriseId(),context.getDataSign());
                return true;
            }
            UnitDto unitDto = new UnitDto();
            unitDto.setIsApproved(Constant.Is.YES);
            unitDto.setIsReleased(Constant.Is.YES);
            Result<List<UnitVo>> result = unitFeignClient.findUnitList(unitDto);
            List<UnitVo> unitList = result.getData();
            if(unitList==null){
                logger.debug("模块{}无初始化数据",context.getDataSign());
                return false;
            }
            unitList.forEach(unit -> {
                unit.setCreatorId(userVo.getUserId());
                unit.setCreatorName(userVo.getName());
                unit.setRootEnterpriseId(userVo.getRootEnterpriseId());
                unit.setPlatformId(unit.getId());
            });
            unitMapper.addBatch(unitList);
            return true;
        };
    }

    /**
     * @description: 初始化账簿分类
     * @param: []
     * @return: com.njwd.basedata.service.impl.SysInitDataServiceImpl.IDataInit
     * @author: xdy
     * @create: 2019-11-12 17:06
     */
    private IDataInit initAccountBookCategory() {
        return context -> {
            context.setSystemSign(Constant.Context.BASE_DATA_FEIGN);
            SysUserVo userVo = UserUtils.getUserVo();
            Integer count = accountBookCategoryMapper.selectCount(Wrappers.<AccountBookCategory>lambdaQuery().eq(AccountBookCategory::getRootEnterpriseId,userVo.getRootEnterpriseId()));
            if(count!=null&&count>0){
                logger.debug("租户{}模块{}数据已初始化",userVo.getRootEnterpriseId(),context.getDataSign());
                return true;
            }
            AccountBookCategoryDto accountBookCategoryDto = new AccountBookCategoryDto();
            accountBookCategoryDto.setIsReleased(Constant.Is.YES);
            accountBookCategoryDto.setIsApproved(Constant.Is.YES);
            Result<List<AccountBookCategoryVo>> result = accountStandardFeignClient.findAccountBookCategoryList(accountBookCategoryDto);
            List<AccountBookCategoryVo> accountBookCategoryList = result.getData();
            if(accountBookCategoryList==null){
                logger.debug("模块{}无初始化数据",context.getDataSign());
                return false;
            }
            addAccountBookCategory(accountBookCategoryList,userVo);
            return true;
        };
    }

    private void addAccountBookCategory(List<? extends AccountBookCategory> accountBookCategoryList,SysUserVo userVo){
        accountBookCategoryList.forEach(accountBookCategory -> {
            accountBookCategory.setCreatorId(userVo.getUserId());
            accountBookCategory.setCreatorName(userVo.getName());
            accountBookCategory.setRootEnterpriseId(userVo.getRootEnterpriseId());
            accountBookCategory.setPlatformId(accountBookCategory.getId());
        });
        accountBookCategoryMapper.addBatch(accountBookCategoryList);
    }

    /**
     * @description:
     * @param: []
     * @return: com.njwd.entity.basedata.SysInitData
     * @author: xdy
     * @create: 2019-08-06 14-56
     */
    @Override
    public SysInitDataVo findSysInitData() {
        SysInitDataVo sysInitData = new SysInitDataVo();
        sysInitData.setStatus(Constant.DataInit.STATUS_FAIL);
        //增量数据
        List<AccountBookCategoryVo> accountBookCategoryList = findIncrementAccountBookCategory();
        if(accountBookCategoryList!=null&&!accountBookCategoryList.isEmpty()){
            logger.debug("{}增量数据需同步",Constant.DataInit.MODULE_ACCOUNT_BOOK_CATEGORY);
            return sysInitData;
        }
        //初始化数据
        SysUserVo sysUserVo = getUserInfo();
        String res =  RedisUtils.get(generateKey(sysUserVo.getRootEnterpriseId()));
        if(res!=null){
            sysInitData.setStatus(Constant.DataInit.STATUS_OK);
            return sysInitData;
        }
        List<SysInitData> sysInitDataList =  sysInitDataMapper.selectList(Wrappers.<SysInitData>lambdaQuery().eq(SysInitData::getRootEnterpriseId,sysUserVo.getRootEnterpriseId()));
        boolean isOk=true;
        for(String dataSign:initMap.keySet()){
            //待初始化
            if(isToInit(sysInitDataList,dataSign)){
                isOk = false;
                break;
            }
        }
        if(isOk){
            RedisUtils.set(generateKey(sysUserVo.getRootEnterpriseId()),Constant.DataInit.MODULE_ALL);
            sysInitData.setStatus(Constant.DataInit.STATUS_OK);
            return sysInitData;
        }
        return sysInitData;
    }

    /**
     * @description: 初始化数据
     * @param: []
     * @return: int
     * @author: xdy
     * @create: 2019-08-06 17-27
     */
    @Override
    public SysInitDataVo initData(){
        SysUserVo sysUserVo = getUserInfo();
        List<SysInitData> sysInitDataList = sysInitDataMapper.selectList(Wrappers.<SysInitData>lambdaQuery()
                .eq(SysInitData::getRootEnterpriseId,sysUserVo.getRootEnterpriseId()));
        int failCount=0,okCount=0;
        DataInitContext context = new DataInitContext();
        for(String key:initMap.keySet()){
            SysUserVo userVo = getUserInfo();
            boolean hasInit;
            if(isToInit(sysInitDataList,key)) {
                hasInit = RedisUtils.lock(generateLock(userVo.getRootEnterpriseId(), key), Constant.DataInit.LOCK_TIMEOUT, () -> {
                    TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
                    boolean isOk;
                    try {
                        isOk = initData(key, initMap.get(key), context);
                        if (isOk) {
                            context.putOk(key);
                            transactionManager.commit(status);
                        } else {
                            transactionManager.rollback(status);
                        }
                    } catch (Exception e) {
                        //e.printStackTrace();
                        transactionManager.rollback(status);
                        isOk = false;
                    }
                    return isOk;
                });
            }else {
                context.putOk(key);
                logger.debug("{}数据已初始化",key);
                hasInit = true;
            }
            if(hasInit){
                okCount++;
            }else{
                failCount++;
            }
        }
        logger.debug("初始化结束,成功{}条，失败{}条。",okCount,failCount);
        //增量数据
        List<AccountBookCategoryVo> accountBookCategoryList = findIncrementAccountBookCategory();
        boolean incrementOk = true;
        if(accountBookCategoryList!=null&&!accountBookCategoryList.isEmpty()){
            try {
                addAccountBookCategory(accountBookCategoryList,sysUserVo);
                logger.debug("{}增量数据同步成功",Constant.DataInit.MODULE_ACCOUNT_BOOK_CATEGORY);
            }catch (Exception e){
                logger.debug("{}增量数据同步失败",Constant.DataInit.MODULE_ACCOUNT_BOOK_CATEGORY);
                incrementOk = false;
                e.printStackTrace();
            }
        }
        Byte status = Constant.DataInit.STATUS_FAIL;
        //全部成功
        if(okCount>0&&failCount==0&&incrementOk){
            status = Constant.DataInit.STATUS_OK;
        }
        SysInitDataVo sysInitData = new SysInitDataVo();
        sysInitData.setStatus(status);
        return sysInitData;
    }


    /**
     * @description: 初始化数据
     * @param: [sysInitDataList, dataSign, iDataInit]
     * @return: boolean
     * @author: xdy
     * @create: 2019-08-06 17-20
     */
    public boolean initData(String dataSign,IDataInit iDataInit,DataInitContext context){
        SysUserVo userVo = getUserInfo();
        //获取锁后再自测状态
        SysInitData sysInitData = sysInitDataMapper.selectOne(Wrappers.<SysInitData>lambdaQuery()
                .eq(SysInitData::getRootEnterpriseId,userVo.getRootEnterpriseId()).eq(SysInitData::getDataSign,dataSign));
        if(sysInitData!=null){
            logger.debug("{}数据已初始化",dataSign);
            return true;
        }
        logger.debug("{}数据初始化开始",dataSign);
        context.setDataSign(dataSign);
        boolean res = iDataInit.init(context);
        if(res){
            logger.debug("{}数据初始化成功",dataSign);
            sysInitData = new SysInitData();
            sysInitData.setRootEnterpriseId(userVo.getRootEnterpriseId());
            sysInitData.setSystemSign(context.getSystemSign());
            sysInitData.setDataSign(dataSign);
            sysInitData.setCreatorId(userVo.getUserId());
            sysInitData.setCreatorName(userVo.getName());
            sysInitDataMapper.insert(sysInitData);
        }else {
            logger.debug("{}数据初始化失败",dataSign);
        }
        return res;
    }

    /**
     * @description: 是否需要初始化
     * @param: [sysInitDataList, dataSign]
     * @return: boolean
     * @author: xdy
     * @create: 2019-08-06 17-20
     */
    private boolean isToInit(List<SysInitData> sysInitDataList,String dataSign){
        if(sysInitDataList==null)
            return true;
        boolean isToInit = true;
        for(SysInitData sysInitData:sysInitDataList){
            if(dataSign.equals(sysInitData.getDataSign())){
                    isToInit=false;
                    break;
            }
        }
        return isToInit;
    }

    /**
     * @description: 初始化科目表
     * @param: []
     * @return: com.njwd.basedata.service.impl.SysInitDataServiceImpl.IDataInit
     * @author: xdy
     * @create: 2019-08-12 15-39
     */
    private IDataInit initSubject(){
        return (context)->{
            context.setSystemSign(Constant.Context.BASE_DATA_FEIGN);
            SysUserVo operator = getUserInfo();
            //查询租户下是否存在会计科目信息
            int count = accountSubjectService.findCount(operator.getRootEnterpriseId());
            if (count > 0){
                logger.debug("租户{}模块{}数据已初始化",operator.getRootEnterpriseId(),context.getDataSign());
                return true;
            }
            AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
            accountSubjectDto.setRootEnterpriseId(operator.getRootEnterpriseId());
            Result<List<FindAccountSubjectListVo>> rootInfo = accountSubjectFeignClient.findMoreSubjectTemplateList(accountSubjectDto);
            if(rootInfo.getCode()!=Result.SUCCESS||rootInfo.getData()==null){
                logger.debug("模块{}无初始化数据",context.getDataSign());
                return false;
            }
            List<AccountSubjectDto> list = new ArrayList<>();
            List<FindAccountSubjectListVo> templateList = rootInfo.getData();
            if (CollectionUtils.isNotEmpty(templateList)) {
                AccountSubjectDto info;
                for (FindAccountSubjectListVo item : templateList) {
                    info = new AccountSubjectDto();
                    FastUtils.copyProperties(item, info);
                    info.setPlatformId(info.getId());
                    list.add(info);
                }
            }
            int res = accountSubjectService.addRoot(list);
            if(res>0)
                return true;
            return false;
        };
    }

    /**
     * @description: 初始化现金流量
     * @param: []
     * @return: com.njwd.basedata.service.impl.SysInitDataServiceImpl.IDataInit
     * @author: xdy
     * @create: 2019-08-14 15-16
     */
    private IDataInit initCashFlow(){
        return context -> {
            context.setSystemSign(Constant.Context.BASE_DATA_FEIGN);
            SysUserVo operator = getUserInfo();
            int count = cashFlowItemService.findCount(operator.getRootEnterpriseId());
            if (count > 0) {
                logger.debug("租户{}模块{}数据已初始化",operator.getRootEnterpriseId(),context.getDataSign());
                return true;
            }
            CashFlowItemDto platformCashFlowItemDto = new CashFlowItemDto();
            platformCashFlowItemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
            Result<List<CashFlowItemVo>> cashFlowItemString = cashFlowItemFeignClient.findCashFlowItemList(platformCashFlowItemDto);
            List<CashFlowItemVo> list = cashFlowItemString.getData();
            if(list==null||list.isEmpty()){
                logger.debug("模块{}无数据初始化",context.getDataSign());
                return true;
            }
            List<CashFlowItemDto> cashFlowItemDtos = new ArrayList<>();
            for (CashFlowItemVo  cashFlowItemVo: list) {
                CashFlowItemDto cashFlowItemDto = new CashFlowItemDto();
                FastUtils.copyProperties(cashFlowItemVo,cashFlowItemDto);
                cashFlowItemDto.setPlatformId(cashFlowItemDto.getId());
                cashFlowItemDtos.add(cashFlowItemDto);
            }
            int flag = cashFlowItemService.addCashFlowItemBatch(cashFlowItemDtos);
            if(flag>0)
                return true;
            return false;
        };
    }

    /**
     * @description: 初始化租户表
     * @param: []
     * @return: com.njwd.basedata.service.impl.SysInitDataServiceImpl.IDataInit
     * @author: xdy
     * @create: 2019-08-12 10-50
     */
    private IDataInit initCreateTable(){
        return (context)->{
            context.setSystemSign(Constant.Context.LEDGER_FEIGN);
            Result<Boolean> result = sysInitDataFeignClient.createTable();
            if(result.getCode()==Result.SUCCESS)
                return result.getData();
            return false;
        };
    }

    /**
     * @description: 初始化子系统
     * @param: []
     * @return: com.njwd.basedata.service.impl.SysInitDataServiceImpl.IDataInit
     * @author: xdy
     * @create: 2019-09-05 16:03
     */
    private IDataInit initSysSystem(){
        return (context) ->{
            context.setSystemSign(Constant.Context.PLATFORM_FEIGN);
            SysUserVo userVo = UserUtils.getUserVo();
            SysSystemDto sysSystemDto = new SysSystemDto();
            sysSystemDto.setRootEnterpriseId(userVo.getRootEnterpriseId());
            sysSystemDto.setCreatorId(userVo.getUserId());
            sysSystemDto.setCreatorName(userVo.getName());
            Result<Boolean> result = sysSystemFeignClient.initData(sysSystemDto);
            if(result.getCode()==Result.SUCCESS){
                return true;
            }
            return false;
        };
    }

    /**
     * @description: 同步菜单选项
     * @param: []
     * @return: com.njwd.basedata.service.impl.SysInitDataServiceImpl.IDataInit
     * @author: xdy
     * @create: 2019-08-14 10-34
     */
    private IDataInit initMenuOption(){
        return (context)->{
            context.setSystemSign(Constant.Context.BASE_DATA_FEIGN);
            SysUserVo userVo = getUserInfo();

            //初始化编码规则
            Integer rulesCount = menuCodingRulesMapper.selectCount(Wrappers.<MenuCodingRules>lambdaQuery().eq(MenuCodingRules::getRootEnterpriseId,userVo.getRootEnterpriseId()));
            if(rulesCount==null||rulesCount==0){
                Result<List<MenuCodingRules>> rulesRes =  menuCodingRulesFeignClient.findMenuCodingRulesList();
                if(rulesRes.getCode()==Result.SUCCESS){
                    List<MenuCodingRules> rulesList = rulesRes.getData();
                    if(rulesList!=null&&!rulesList.isEmpty()){
                        for(MenuCodingRules rules:rulesList){
                            rules.setRootEnterpriseId(userVo.getRootEnterpriseId());
                        }
                        menuCodingRulesMapper.addBatch(rulesList);
                    }
                }
            }
            //初始化控制策略
            Integer strategyCount = menuControlStrategyMapper.selectCount(Wrappers.<MenuControlStrategy>lambdaQuery().eq(MenuControlStrategy::getRootEnterpriseId,userVo.getRootEnterpriseId()));
            if(strategyCount==null||strategyCount==0){
                Result<List<MenuControlStrategy>> strategyRes = menuControlStrategyFeignClient.findMenuControlStrategyList();
                if(strategyRes.getCode()==Result.SUCCESS){
                    List<MenuControlStrategy> strategyList = strategyRes.getData();
                    if(strategyList!=null&&!strategyList.isEmpty()){
                        for(MenuControlStrategy menuControlStrategy:strategyList){
                            menuControlStrategy.setRootEnterpriseId(userVo.getRootEnterpriseId());
                        }
                        menuControlStrategyMapper.addBatch(strategyList);
                    }
                }
            }
            //初始化菜单选项
            Integer count =  sysMenuOptionMapper.selectCount(Wrappers.<SysMenuOption>lambdaQuery().eq(SysMenuOption::getRootEnterpriseId,userVo.getRootEnterpriseId()));
            if(count!=null&&count>0)
                return true;
            Result<List<SysMenuOptionVo>> result =  sysMenuOptionFeignClient.findSysMenuOptionList(new SysMenuOptionDto());
            if(result.getCode()==Result.SUCCESS){
                List<SysMenuOptionVo> sysMenuOptionVoList = result.getData();
                if(sysMenuOptionVoList!=null&&!sysMenuOptionVoList.isEmpty()){
                    sysMenuOptionVoList = sysMenuOptionVoList.stream().filter(i->Constant.Is.YES.equals(i.getIsDefault())).map(i->{
                        i.setRootEnterpriseId(userVo.getRootEnterpriseId());
                        i.setCreatorId(userVo.getUserId());
                        i.setCreatorName(userVo.getName());
                        return i;
                    }).collect(Collectors.toList());
                    sysMenuOptionMapper.addBatch(sysMenuOptionVoList);
                }
                return true;
            }
            return false;
        };
    }

    /**
     * @description: 同步常用摘要
     * @param: []
     * @return: com.njwd.basedata.service.impl.SysInitDataServiceImpl.IDataInit
     * @author: xdy
     * @create: 2019-08-14 10-34
     */
    private IDataInit initCommonAbstract(){
        return (context)->{
            context.setSystemSign(Constant.Context.BASE_DATA_FEIGN);
            SysUserVo userVo = getUserInfo();
            Integer count =  commonAbstractMapper.selectCount(Wrappers.<CommonAbstract>lambdaQuery().eq(CommonAbstract::getRootEnterpriseId,userVo.getRootEnterpriseId()));
            if(count!=null&&count>0){
                logger.debug("租户{}模块{}数据已初始化",userVo.getRootEnterpriseId(),context.getDataSign());
                return true;
            }
            CommonAbstractDto commonAbstractDto = new CommonAbstractDto();
            Result<List<CommonAbstractVo>> result = commonAbstractFeignClient.findCommonAbstractList(commonAbstractDto);
            if(result.getCode()==Result.SUCCESS){
                List<CommonAbstractVo> list = result.getData();
                if(list==null||list.isEmpty()){
                    logger.debug("模块{}无数据初始化",context.getDataSign());
                    return true;
                }
                for(CommonAbstract commonAbstract:list){
                    commonAbstract.setRootEnterpriseId(userVo.getRootEnterpriseId());
                    commonAbstract.setCreateEnterpriseId(0L);
                    commonAbstract.setUseEnterpriseId(0L);
                    commonAbstract.setCreatorId(userVo.getUserId());
                    commonAbstract.setCreatorName(userVo.getName());
                    commonAbstract.setPlatformId(commonAbstract.getId());
                }
                commonAbstractMapper.addBatch(list);
                return true;
            }
            return false;
        };
    }

    /**
     * @description: 初始化参数设置
     * @param: []
     * @return: com.njwd.basedata.service.impl.SysInitDataServiceImpl.IDataInit
     * @author: xdy
     * @create: 2019-08-14 10-42
     */
    private IDataInit initParameterSet(){
        return context -> {
            context.setSystemSign(Constant.Context.LEDGER_FEIGN);
            //会计科目需要初始化成功
            if(!context.isOk(Constant.DataInit.MODULE_SUBJECT)){
                logger.debug("模块{}需先初始化成功",Constant.DataInit.MODULE_SUBJECT);
                return false;
            }
            List<ParameterSetVo> parameterSetVoList = parameterSetPlatformFeignClient.findParameterSet().getData();
            if(parameterSetVoList==null){
                logger.debug("模块{}获取数据失败",context.getDataSign());
                return false;
            }
            //获取平台会计科目数据
            ParameterSetVo fpAccSet=null,lrAcc=null,syAcc=null,subject=null;
            for(ParameterSetVo parameterSetVo:parameterSetVoList){
                if(Constant.ParameterSetKey.FP_ACC_SUBJECT_ID.equals(parameterSetVo.getKey())){
                    fpAccSet = parameterSetVo;
                }else if(Constant.ParameterSetKey.LR_ACC_SUBJECT_ID.equals(parameterSetVo.getKey())){
                    lrAcc = parameterSetVo;
                }else if(Constant.ParameterSetKey.SY_ACC_SUBJECT_ID.equals(parameterSetVo.getKey())){
                    syAcc = parameterSetVo;
                }else if(Constant.ParameterSetKey.ACC_SUBJECT_ID.equals(parameterSetVo.getKey())){
                    subject = parameterSetVo;
                }
            }
            Long[] newIds = convertAccountSubjectId(fpAccSet.getValue(), lrAcc.getValue(), syAcc.getValue());
            if(newIds==null) {
                logger.debug("模块{}获取会计科目数据失败",context.getDataSign());
                return false;
            }
            fpAccSet.setValue(newIds[0]);
            lrAcc.setValue(newIds[1]);
            syAcc.setValue(newIds[2]);
            subject.setValue(newIds[3]);
            //初始化数据
            ParameterSetDto parameterSetDto = new ParameterSetDto();
            parameterSetDto.setParameterSetVoList(parameterSetVoList);
            Result<Boolean> res = parameterSetLedgerFeignClient.initParameterSet(parameterSetDto);
            if(res.getCode()==Result.SUCCESS){
                return res.getData();
            }
            return false;

        };
    }

    /**
     * @description: 平台主键转换成基础资料主键
     * @param: [accountSubjectIds]
     * @return: java.lang.Long[]
     * @author: xdy
     * @create: 2019-08-22 11-51
     */
    private Long[] convertAccountSubjectId(Long... accountSubjectIds){
        Long[] result = new Long[accountSubjectIds.length+1];
        List<Long> ids = Stream.of(accountSubjectIds).filter(i->i!=null).collect(Collectors.toList());
        if(ids!=null&&!ids.isEmpty()){
            AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
            accountSubjectDto.setIds(ids);
            Result<List<FindAccountSubjectListVo>> subjectResult = accountSubjectFeignClient.findMoreSubjectTemplateList(accountSubjectDto);
            if(subjectResult.getCode()!=Result.SUCCESS||subjectResult.getData()==null) {
               return null;
            }
            SysUserVo userVo = getUserInfo();
            List<FindAccountSubjectListVo> platformList =  subjectResult.getData().stream().filter(i->ids.contains(i.getId())).collect(Collectors.toList());
            List<String> codes = platformList.stream().map(d->d.getCode()).collect(Collectors.toList());
            List<AccountSubject> accountSubjectList = accountSubjectMapper.selectList(Wrappers.<AccountSubject>lambdaQuery().eq(AccountSubject::getRootEnterpriseId,userVo.getRootEnterpriseId()).in(AccountSubject::getCode,codes));
            Map<String,Long> codeToIdMap = accountSubjectList.stream().collect(Collectors.toMap(i->i.getCode(),i->i.getId()));
            Map<Long,String> idToCodeMap = platformList.stream().collect(Collectors.toMap(i->i.getId(),i->i.getCode()));
            Long subjectId = 0L;
            if(!accountSubjectList.isEmpty()){
                AccountSubject accountSubject = accountSubjectList.get(0);
                subjectId = accountSubject.getSubjectId();
            }
            for(int i = 0;i<accountSubjectIds.length;i++){
                if(accountSubjectIds[i]!=null){
                    result[i] = codeToIdMap.get(idToCodeMap.get(accountSubjectIds[i]));
                }
            }
            result[accountSubjectIds.length]=subjectId;
        }
        return result;
    }

    private List<AccountBookCategoryVo> findIncrementAccountBookCategory(){
        SysUserVo userVo = UserUtils.getUserVo();
        List<AccountBookCategory> accountBookCategoryList = accountBookCategoryMapper.selectList(Wrappers.<AccountBookCategory>lambdaQuery()
                .eq(AccountBookCategory::getRootEnterpriseId,userVo.getRootEnterpriseId()).gt(AccountBookCategory::getPlatformId,0));
        List<Long> accountBookCategoryIds = accountBookCategoryList.stream().map(i->i.getPlatformId()).collect(Collectors.toList());
        AccountBookCategoryDto accountBookCategoryDto = new AccountBookCategoryDto();
        accountBookCategoryDto.setIsReleased(Constant.Is.YES);
        accountBookCategoryDto.setIsApproved(Constant.Is.YES);
        accountBookCategoryDto.setPlatformIds(accountBookCategoryIds);
        Result<List<AccountBookCategoryVo>> result = accountStandardFeignClient.findAccountBookCategoryList(accountBookCategoryDto);
        return result.getData();
    }

    /**
     * @description: 用户信息
     * @param: []
     * @return: com.njwd.entity.basedata.vo.SysUserVo
     * @author: xdy
     * @create: 2019-08-30 15-53
     */
    private SysUserVo getUserInfo(){
        /*SysUserVo userVo = new SysUserVo();
        userVo.setUserId(0L);
        userVo.setName("tt");
        userVo.setRootEnterpriseId(211L);*/
        SysUserVo userVo = UserUtils.getUserVo();
        return userVo;
    }

    /**
     * @description: 生成key
     * @param: [rootEnterpriseId]
     * @return: java.lang.String
     * @author: xdy
     * @create: 2019-08-06 17-21
     */
    private String generateKey(Long rootEnterpriseId){
        return String.format(Constant.DataInit.KEY,rootEnterpriseId);
    }

    /**
     * @description: 生成锁
     * @param: [rootEnterpriseId, dataSign]
     * @return: java.lang.String
     * @author: xdy
     * @create: 2019-08-06 17-26
     */
    private String generateLock(Long rootEnterpriseId,String dataSign){
        return String.format(Constant.DataInit.LOCK,rootEnterpriseId,dataSign);
    }

    /**
     * 初始化接口
     */
    public interface IDataInit{
        boolean init(DataInitContext context);
    }

    /**
     * @description: 初始化上下文
     * @param:
     * @return:
     * @author: xdy
     * @create: 2019-08-30 15-57
     */
    @Getter
    @Setter
    public class DataInitContext{
        private String systemSign;
        private String dataSign;
        private Map<String,Boolean> moduleOk = new HashMap<>();

        public void putOk(String dataSign){
            moduleOk.put(dataSign,true);
        }

        public Boolean isOk(String dataSign){
            Boolean flag = moduleOk.get(dataSign);
            return flag==null?false:flag;
        }

    }

}
