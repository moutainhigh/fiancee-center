package com.njwd.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.njwd.common.Constant;
import com.njwd.common.ExcelColumnConstant;
import com.njwd.common.LedgerConstant;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.SequenceDto;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.basedata.vo.AccountBookVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.Balance;
import com.njwd.entity.ledger.MultiColumnScheme;
import com.njwd.entity.ledger.MultiColumnSchemeItem;
import com.njwd.entity.ledger.VoucherEntryAuxiliary;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.ledger.dto.MultiColumnReportDto;
import com.njwd.entity.ledger.dto.MultiColumnSchemeDto;
import com.njwd.entity.ledger.vo.*;
import com.njwd.entity.platform.AccountSubjectAuxiliary;
import com.njwd.entity.platform.dto.AccountSubjectAuxiliaryDto;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.exception.ResultCode;
import com.njwd.ledger.cloudclient.AccountBookFeignClient;
import com.njwd.ledger.cloudclient.AccountSubjectFeignClient;
import com.njwd.ledger.cloudclient.PublicDataFeignClient;
import com.njwd.ledger.mapper.MultiColumnSchemeItemMapper;
import com.njwd.ledger.mapper.MultiColumnSchemeMapper;
import com.njwd.ledger.mapper.VoucherEntryAuxiliaryMapper;
import com.njwd.ledger.service.AccountBookPeriodService;
import com.njwd.ledger.service.MultiColumnSchemeService;
import com.njwd.service.FileService;
import com.njwd.support.BatchResult;
import com.njwd.utils.FastUtils;
import com.njwd.utils.UserUtils;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author liuxiang
 * @Description 多栏账方案
 * @Date:14:26 2019/7/29
 **/
@Service
public class MultiColumnSchemeServiceImpl extends ServiceImpl<MultiColumnSchemeMapper,MultiColumnScheme> implements MultiColumnSchemeService {

    @Resource
    private AccountSubjectFeignClient accountSubjectFeignClient;

    @Resource
    private MultiColumnSchemeMapper multiColumnSchemeMapper;

    @Resource
    private MultiColumnSchemeItemMapper multiColumnSchemeItemMapper;

    @Resource
    private AccountBookFeignClient accountBookFeignClient;

    @Resource
    private PublicDataFeignClient publicDataFeignClient;

    @Resource
    private VoucherEntryAuxiliaryMapper voucherEntryAuxiliaryMapper;

    private Logger logger = LoggerFactory.getLogger(MultiColumnSchemeServiceImpl.class);

    @Resource
    private FileService fileService;

    @Resource
    private AccountBookPeriodService accountBookPeriodService;

    /**
     * @return int
     * @Description 新增多栏账方案设置
     * @Author liuxiang
     * @Date:17:15 2019/7/30
     * @Param [multiColumnSchemeDto]
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MultiColumnSchemeVo addMultiColumnScheme(MultiColumnSchemeDto dto) {

        // 拷贝前端参数
        MultiColumnScheme multiColumnScheme = new MultiColumnScheme();
        FastUtils.copyProperties(dto, multiColumnScheme);

        // 根据账簿ID查询账簿编码
        AccountBookDto accountBookDto = new AccountBookDto();
        accountBookDto.setId(dto.getAccountBookId());
        AccountBookVo accountBookVo = accountBookFeignClient.selectById(accountBookDto).getData();

        // 流水号实体
        SequenceDto sequenceDto=new SequenceDto();
        // 设置前缀
        sequenceDto.setPreCode(accountBookVo.getCode());
        // 设置流水号位数
        sequenceDto.setLength(Constant.BaseCodeRule.LENGTH_THREE);
        // 设置ID
        sequenceDto.setRootEnterPriseId(dto.getRootEnterpriseId());
        // 设置主体类型
        sequenceDto.setType(Constant.BaseCodeRule.ENTERPRISE);
        // 设置多栏账方案编码
        multiColumnScheme.setCode(publicDataFeignClient.getCode(sequenceDto).getData());
        multiColumnSchemeMapper.addMultiColumnScheme(multiColumnScheme);
        if(dto.getItemList()!=null&&!dto.getItemList().isEmpty())
            multiColumnSchemeItemMapper.insertBatch(multiColumnScheme.getId(),dto.getItemList());

        dto.setId(multiColumnScheme.getId());
        return findMultiColumnSchemeById(dto);
    }

    /**
     * @return com.njwd.system.support.BatchResult
     * @Description 批量删除多栏账方案
     * @Author liuxiang
     * @Date:17:16 2019/7/30
     * @Param [dto]
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = Constant.RedisCache.MULTI_COLUMN_SCHEME, allEntries = true,beforeInvocation = true)
    public BatchResult deleteMultiColumnSchemeBatch(MultiColumnSchemeDto dto) {
        //初始化
        BatchResult result = new BatchResult();
        result.setFailList(new ArrayList<>());
        result.setSuccessList(new ArrayList<>());

        //查询idList 的状态
        List<MultiColumnSchemeVo> multiColumnSchemeList = multiColumnSchemeMapper.findStatusByIdList(dto);

        //循环添加错误
        for (MultiColumnSchemeVo multiColumnSchemeVo : multiColumnSchemeList) {
            //判断删除状态
            if (multiColumnSchemeVo.getIsDel().equals(Constant.Is.YES)) {
                ReferenceDescription fd = new ReferenceDescription();
                fd.setBusinessId(multiColumnSchemeVo.getId());
                //返回已删除
                fd.setReferenceDescription(ResultCode.IS_DEL.message);
                result.getFailList().add(fd);
                continue;
            }
            //添加成功的
            result.getSuccessList().add(multiColumnSchemeVo.getId());
        }
        //防止没有数据
        if (result.getSuccessList().size() == 0) {
            return result;
        }

        //SQL PARAM
        MultiColumnScheme sqlParam = new MultiColumnScheme();
        sqlParam.setRootEnterpriseId(dto.getRootEnterpriseId());
        //生成更新条件
        dto.setIsDel(Constant.Is.YES);
        dto.setRootEnterpriseId(null);
        multiColumnSchemeMapper.update(dto, new QueryWrapper<>(sqlParam).in("id", result.getSuccessList()));
        return result;
    }

    /**
     * @return int
     * @Description 修改多栏账方案设置
     * @Author liuxiang
     * @Date:13:53 2019/7/31
     * @Param [multiColumnSchemeDto]
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = Constant.RedisCache.MULTI_COLUMN_SCHEME, key = "#multiColumnSchemeDto.id")
    public int updateMultiColumnScheme(MultiColumnSchemeDto multiColumnSchemeDto) {
        int result;
        MultiColumnScheme multiColumnScheme=new MultiColumnScheme();
        // 拷贝前端参数
        FastUtils.copyProperties(multiColumnSchemeDto,multiColumnScheme);
        // 执行更新操作
        result=multiColumnSchemeMapper.updateMultiColumnScheme(multiColumnScheme);
        multiColumnSchemeItemMapper.delete(Wrappers.<MultiColumnSchemeItem>lambdaQuery().eq(MultiColumnSchemeItem::getSchemeId,multiColumnSchemeDto.getId()));
        if(multiColumnSchemeDto.getItemList()!=null&&!multiColumnSchemeDto.getItemList().isEmpty())
            multiColumnSchemeItemMapper.insertBatch(multiColumnScheme.getId(),multiColumnSchemeDto.getItemList());
        return result;
    }

    /**
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.ledger.entity.vo.MultiColumnSchemeVo>
     * @Description 根据CodeOrName查询多栏账方案分页
     * @Author liuxiang
     * @Date:16:11 2019/7/30
     * @Param [page, multiColumnSchemeDto]
     **/
    @Override
    public Page<MultiColumnSchemeVo> findMultiColumnSchemePageByCodeOrName(MultiColumnSchemeDto multiColumnSchemeDto) {
        Page<MultiColumnSchemeVo> page = multiColumnSchemeDto.getPage();
        SysUserVo userVo = UserUtils.getUserVo();
        multiColumnSchemeDto.setRootEnterpriseId(userVo.getRootEnterpriseId());
        page = multiColumnSchemeMapper.findMultiColumnSchemePage(page, multiColumnSchemeDto);
        return page;
    }

    /**
     * @return com.njwd.ledger.entity.vo.MultiColumnSchemeVo
     * @Description 根据ID查询多栏账方案设置
     * @Author liuxiang
     * @Date:10:50 2019/7/31
     * @Param [multiColumnSchemeDto]
     **/
    @Override
    public MultiColumnSchemeVo findMultiColumnSchemeById(MultiColumnSchemeDto multiColumnSchemeDto) {
        MultiColumnSchemeVo multiColumnSchemeVo = multiColumnSchemeMapper.findMultiColumnSchemeById(multiColumnSchemeDto);
        multiColumnSchemeVo.setItemList(multiColumnSchemeItemMapper.selectList(Wrappers.<MultiColumnSchemeItem>lambdaQuery().eq(MultiColumnSchemeItem::getSchemeId,multiColumnSchemeDto.getId())));
        return multiColumnSchemeVo;
    }

    /**
     * @return java.util.List<com.njwd.ledger.entity.vo.MultiColumnSchemeVo>
     * @Description 查询未删除状态的多栏账方案
     * @Author liuxiang
     * @Date:11:04 2019/8/2
     * @Param []
     **/
    @Override
    public List<MultiColumnSchemeVo> findMultiColumnSchemeList() {
        return multiColumnSchemeMapper.findMultiColumnSchemeList();
    }

    /**
     * @description: 获取分栏账报表
     * @param: [multiColumnReportDto]
     * @return: com.njwd.entity.ledger.vo.MultiColumnReportVo
     * @author: xdy
     * @create: 2019-08-31 15:49
     */
    @Override
    public MultiColumnReportVo findMultiColumnReport(MultiColumnReportDto multiColumnReportDto){
        MultiColumnReportVo multiColumnReportVo = new MultiColumnReportVo();
        MultiColumnContext multiColumnContext = new MultiColumnContext();
        multiColumnContext.setMultiColumnReportDto(multiColumnReportDto);
        //获取报表相关数据
        boolean res = findReportData(multiColumnContext);
        if(!res)
            return multiColumnReportVo;
        //凭证
        List<VoucherEntryVo> voucherEntryVoList = multiColumnContext.getVoucherEntryVoList();
        //科目余额
        Map<String,BalanceSubjectVo> balanceSubjectMap = multiColumnContext.getBalanceSubjectMap();
        Long prevEntityId=-1L;
        for(int i=0;i<voucherEntryVoList.size();i++){
            VoucherEntryVo voucherEntryVo=voucherEntryVoList.get(i);
            multiColumnContext.setVoucherEntryVo(voucherEntryVo);
            BalanceSubjectVo balanceSubjectVo = balanceSubjectMap.get(generateEntityPeriodKey(voucherEntryVo.getAccountBookEntityId(),voucherEntryVo.getPeriodYearNum()));
            multiColumnContext.setBalanceSubjectVo(balanceSubjectVo);
            //期初余额
            if(!prevEntityId.equals(voucherEntryVo.getAccountBookEntityId())){
                openingBalance(multiColumnContext);
                prevEntityId = voucherEntryVo.getAccountBookEntityId();
            }
            //分录余额
            voucherEntryBalance(multiColumnContext);
            //合计累计
            if(i+1>=voucherEntryVoList.size()
                    ||(!voucherEntryVo.getAccountBookEntityId().equals(voucherEntryVoList.get(i+1).getAccountBookEntityId())
                        ||voucherEntryVo.getPeriodYearNum()!= voucherEntryVoList.get(i+1).getPeriodYearNum())){
                //期间统计
                periodTotal(multiColumnContext);
            }
        }
        //去除无数据项目
        delNoDataCol(multiColumnContext);
        //返回报表
        multiColumnReportVo.setItemList(multiColumnContext.getItemList());
        multiColumnReportVo.setSubjectList(multiColumnContext.getMultiColumnSchemeItems());
        return multiColumnReportVo;
    }

    /**
     * @description: 导出多栏账报表
     * @param: [multiColumnReportDto, response]
     * @return: void
     * @author: xdy
     * @create: 2019-09-05 09:36
     */
    @Override
    public void exportMultiColumnReport(MultiColumnReportDto multiColumnReportDto, HttpServletResponse response) {
        MultiColumnReportVo multiColumnReportVo = findMultiColumnReport(multiColumnReportDto);
        List<ExcelColumn> excelColumnList = new ArrayList<>();
        excelColumnList.add(ExcelColumnConstant.MultiColumn.ACCOUNT_BOOK_ENTITY_NAME);
        excelColumnList.add(ExcelColumnConstant.MultiColumn.VOUCHER_DATE);
        excelColumnList.add(ExcelColumnConstant.MultiColumn.CREDENTIAL_WORD_CODE);
        excelColumnList.add(ExcelColumnConstant.MultiColumn.ABSTRACT_CONTENT);
        excelColumnList.add(ExcelColumnConstant.MultiColumn.TOTAL_DEBIT_AMOUNT);
        excelColumnList.add(ExcelColumnConstant.MultiColumn.TOTAL_CREDIT_AMOUNT);
        excelColumnList.add(ExcelColumnConstant.MultiColumn.BALANCE_DIRECTION_NAME);
        excelColumnList.add(ExcelColumnConstant.MultiColumn.TOTAL_BALANCE);
        List<MultiColumnSchemeItem> subjectList= multiColumnReportVo.getSubjectList();
        List<List<String>> excelHead = excelColumnList.stream().map(i->{
            List<String> excelCelTitle = new ArrayList();
            excelCelTitle.add("");
            return excelCelTitle;
        }).collect(Collectors.toList());
        if(subjectList!=null&&!subjectList.isEmpty()){
            for(int i=0;i<subjectList.size();i++){
                MultiColumnSchemeItem multiColumnSchemeItem = subjectList.get(i);
                excelColumnList.add(new ExcelColumn("itemList["+i+"]",multiColumnSchemeItem.getItemName()));
                List<String> excelCelTitle = new ArrayList<>();
                if(Constant.BalanceDirection.DEBIT.equals(multiColumnSchemeItem.getDirection())){
                    excelCelTitle.add(Constant.BalanceDirectionOwner.DEBIT);
                }else if(Constant.BalanceDirection.CREDIT.equals(multiColumnSchemeItem.getDirection())){
                    excelCelTitle.add(Constant.BalanceDirectionOwner.CREDIT);
                }else {
                    excelCelTitle.add(multiColumnSchemeItem.getDirection()==null?"":String.valueOf(multiColumnSchemeItem.getDirection()));
                }
                excelHead.add(excelCelTitle);
            }
        }
        ExcelColumn[]  excelColumnArr =  excelColumnList.toArray(new ExcelColumn[]{});
        fileService.exportExcel(response,multiColumnReportVo.getItemList(), LedgerConstant.LedgerExportName.LEDGER_MULTI_COLUMN_REPORT,excelHead,excelColumnArr);
    }


    /**
     * @description: 获取报表相关数据
     * @param: [multiColumnReportDto, multiColumnContext]
     * @return: void
     * @author: xdy
     * @create: 2019-09-02 17:29
     */
    private boolean findReportData(MultiColumnContext multiColumnContext){
        MultiColumnReportDto multiColumnReportDto = multiColumnContext.getMultiColumnReportDto();
        //分栏帐方案
        MultiColumnScheme multiColumnScheme = multiColumnSchemeMapper.selectById(multiColumnReportDto.getSchemeId());
        multiColumnContext.setMultiColumnScheme(multiColumnScheme);
        //会计科目
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        accountSubjectDto.setId(multiColumnScheme.getAccountSubjectId());
        AccountSubjectVo accountSubjectVo = accountSubjectFeignClient.findInfoById(accountSubjectDto).getData();
        multiColumnContext.setAccountSubjectVo(accountSubjectVo);
        //会计科目的末级科目
        accountSubjectDto = new AccountSubjectDto();
        accountSubjectDto.setSubjectId(accountSubjectVo.getSubjectId());
        accountSubjectDto.setCode(accountSubjectVo.getCode());
        //accountSubjectDto.setIsFinal(C);
        List<AccountSubjectVo> accountSubjectVoList =  accountSubjectFeignClient.findAllChildInfo(accountSubjectDto).getData();
        if(accountSubjectVoList==null||accountSubjectVoList.isEmpty())
            return false;
        //会计科目辅助核算
        AccountSubjectAuxiliary accountSubjectAuxiliary=null;
        if(Constant.MultiColumnScheme.SCHEME_TYPE_AUXILIAY.equals(multiColumnScheme.getSchemeType())&&multiColumnScheme.getAuxiliaryItemId()!=null){
            AccountSubjectAuxiliaryDto accountSubjectAuxiliaryDto = new AccountSubjectAuxiliaryDto();
            accountSubjectAuxiliaryDto.setId(multiColumnScheme.getAuxiliaryItemId());
            accountSubjectAuxiliary = accountSubjectFeignClient.findAccountSubjectAuxiliary(accountSubjectAuxiliaryDto).getData();
            multiColumnContext.setAccountSubjectAuxiliary(accountSubjectAuxiliary);
        }
        //分栏帐方案明细
        List<MultiColumnSchemeItem> multiColumnSchemeItems = multiColumnSchemeItemMapper.selectList(Wrappers.<MultiColumnSchemeItem>lambdaQuery().eq(MultiColumnSchemeItem::getSchemeId,multiColumnReportDto.getSchemeId())
                                                            .orderByAsc(MultiColumnSchemeItem::getDirection).orderByAsc(MultiColumnSchemeItem::getId));
        if(multiColumnSchemeItems==null||multiColumnSchemeItems.isEmpty()){
            multiColumnContext.setHasSubjectItem(false);
            //辅助核算项目
            if(Constant.MultiColumnScheme.SCHEME_TYPE_AUXILIAY.equals(multiColumnScheme.getSchemeType())&&accountSubjectAuxiliary!=null){
                accountSubjectDto = new AccountSubjectDto();
                accountSubjectDto.getPage().setSize(10000);
                accountSubjectDto.setSourceTable(accountSubjectAuxiliary.getAuxiliarySourceTable());
                accountSubjectDto.setCode(String.valueOf(accountSubjectAuxiliary.getAuxiliaryCode()));
                accountSubjectDto.setIsEnable(Constant.Is.YES);
                Page<Map<String,Object>> auxiliaryList = accountSubjectFeignClient.findSourceTableList(accountSubjectDto).getData();
                multiColumnSchemeItems = convertToSchemeItem(auxiliaryList.getRecords(),accountSubjectVo);
            }else if(Constant.MultiColumnScheme.SCHEME_TYPE_SUBJECT.equals(multiColumnScheme.getSchemeType())){
                multiColumnSchemeItems = convertToSchemeItem(accountSubjectVoList);
            }
        }
        multiColumnContext.setMultiColumnSchemeItems(multiColumnSchemeItems);
        List<Long> accountSubjectIds = accountSubjectVoList.stream().map(i->i.getId()).collect(Collectors.toList());
        multiColumnReportDto.setAccountSubjectIds(accountSubjectIds);
        //凭证状态
        List<Byte> voucherStatusList = new ArrayList<>();
        Collections.addAll(voucherStatusList,LedgerConstant.VoucherStatus.POST);
        multiColumnReportDto.setVoucherStatusList(voucherStatusList);//默认已过账
        if(Constant.Is.YES.equals(multiColumnReportDto.getIsIncludeUnbooked())){
            multiColumnReportDto.addVoucherStatus(LedgerConstant.VoucherStatus.PENDING);//待审核 测试
            multiColumnReportDto.addVoucherStatus(LedgerConstant.VoucherStatus.POSTING);
        }
        //凭证来源
        List<Byte> voucherSourceTypes = new ArrayList<>();
        Collections.addAll(voucherSourceTypes,LedgerConstant.SourceType.MANUAL,LedgerConstant.SourceType.COLLABORATE,
                LedgerConstant.SourceType.RUSH,LedgerConstant.SourceType.BUSINESS_SYSTEM,LedgerConstant.SourceType.COMPANY_COLL);//默认不包含损益结转
        multiColumnReportDto.setVoucherSourceTypes(voucherSourceTypes);
        if(Constant.Is.YES.equals(multiColumnReportDto.getIsIncludeProfitAndLoss())){
            multiColumnReportDto.addVoucherSourceType(LedgerConstant.SourceType.FORWARD);
        }
        //科目余额
        MultiColumnReportDto balanceMultiColumnReportDto = new MultiColumnReportDto();
        FastUtils.copyProperties(multiColumnReportDto,balanceMultiColumnReportDto);

        //最小未结账期间
        AccountBookPeriodDto accountBookPeriodDto = new AccountBookPeriodDto();
        accountBookPeriodDto.setSystemSign(Constant.SystemSign.LEDGER);
        accountBookPeriodDto.setAccountBookId(multiColumnReportDto.getAccountBookId());
        // 已开启
        accountBookPeriodDto.setStatus(Constant.Status.ON);
        // 未结账
        accountBookPeriodDto.setIsSettle(LedgerConstant.Ledger.ZERO);
        // 是否查询最小期间
        accountBookPeriodDto.setIsLeast(Constant.Is.YES);
        AccountBookPeriodVo accountBookPeriodVo = accountBookPeriodService.findPeriodByAccBookIdAndSystemSign(accountBookPeriodDto);
        if(accountBookPeriodVo!=null){
            Integer maxPeriodYearNum = multiColumnReportDto.getPeriodYears().get(1)*100+multiColumnReportDto.getPeriodNumbers().get(1);
            if(maxPeriodYearNum>=accountBookPeriodVo.getPeriodYearNum()){
                balanceMultiColumnReportDto.getPeriodNumbers().set(0,(byte)0);
                balanceMultiColumnReportDto.getPeriodYears().set(0,accountBookPeriodVo.getPeriodYear());
                multiColumnContext.setMinPeriodYearNum(accountBookPeriodVo.getPeriodYearNum());
            }else{
                multiColumnContext.setMinPeriodYearNum(accountBookPeriodVo.getPeriodYearNum());
            }
        }
        List<BalanceSubjectVo> balanceSubjectVoList = multiColumnSchemeMapper.findBalanceSubject(balanceMultiColumnReportDto);
        balanceSubjectListToMap(multiColumnContext,balanceSubjectVoList);
        //非末级的所有末级
        listToTree(multiColumnContext,accountSubjectVoList);
        //凭证
        List<VoucherEntryVo> voucherEntryVoList = multiColumnSchemeMapper.findVoucherEntry(multiColumnReportDto);
        if(voucherEntryVoList==null||voucherEntryVoList.isEmpty())
            return false;
        multiColumnContext.setVoucherEntryVoList(voucherEntryVoList);
        //获取凭证辅助核算
        if(Constant.MultiColumnScheme.SCHEME_TYPE_AUXILIAY.equals(multiColumnScheme.getSchemeType())
                &&accountSubjectAuxiliary!=null&&multiColumnSchemeItems!=null&&!multiColumnSchemeItems.isEmpty()){
            final AccountSubjectAuxiliary subjectAuxiliary =  accountSubjectAuxiliary;
            //凭证辅助核算
            List<Long> voucherEntryIds = voucherEntryVoList.stream().map(i->i.getId()).collect(Collectors.toList());
            List<VoucherEntryAuxiliary> voucherEntryAuxiliaries = voucherEntryAuxiliaryMapper.findListByEntryId(voucherEntryIds);
            Map<Long,List<Long>> voucherEntryAuxiliaryMap = new HashMap<>();
            voucherEntryAuxiliaries.stream().filter(i->i.getSourceTable().equals(subjectAuxiliary.getAuxiliarySourceTable())).forEach(i->{
                List<Long> auxiliaryIds = voucherEntryAuxiliaryMap.get(i.getEntryId());
                if(auxiliaryIds==null){
                    auxiliaryIds = new ArrayList<>();
                    voucherEntryAuxiliaryMap.put(i.getEntryId(),auxiliaryIds);
                }
                auxiliaryIds.add(i.getItemValueId());
            });
            multiColumnContext.setVoucherEntryAuxiliaryMap(voucherEntryAuxiliaryMap);
            //科目余额辅助核算
            List<Long> itemIds = multiColumnSchemeItems.stream().map(i->i.getItemId()).collect(Collectors.toList());
            balanceMultiColumnReportDto.setItemValueIds(itemIds);
            balanceMultiColumnReportDto.setSourceTable(subjectAuxiliary.getAuxiliarySourceTable());
            List<BalanceSubjectAuxiliaryVo> balanceSubjectAuxiliaryList = multiColumnSchemeMapper.findBalanceSubjectAuxiliary(balanceMultiColumnReportDto);
            Map<String,BalanceSubjectAuxiliaryVo> balanceSubjectAuxiliaryMap = balanceSubjectAuxiliaryList.stream().collect(Collectors.toMap(i->generateEntityPeriodSubjectAuxiliaryKey(i.getAccountBookEntityId(),i.getPeriodYearNum(),i.getAccountSubjectId(),i.getItemValueId()),i->i));
            multiColumnContext.setBalanceSubjectAuxiliaryMap(balanceSubjectAuxiliaryMap);
        }
        //项目是否有数据
        multiColumnContext.setSubjectHasData(new boolean[multiColumnSchemeItems.size()]);
        //报表明细
        multiColumnContext.setItemList(new ArrayList<>());
        return true;
    }

    /**
     * @description:
     * @param: [multiColumnContext]
     * @return: void
     * @author: xdy
     * @create: 2019-09-20 13:56
     */
    private void listToTree(MultiColumnContext multiColumnContext,List<AccountSubjectVo> accountSubjectVoList){
        //调整排序
        accountSubjectVoList.sort((a,b)->{
            if(a.getCode().length()!=b.getCode().length()){
                return b.getCode().length()-a.getCode().length();
            }else {
                return a.getCode().compareTo(b.getCode());
            }
        });
        //
        List<BalanceSubjectVo> entityPeriodBalanceList = multiColumnContext.getEntityPeriodBalanceList();
        Map<String, BalanceSubjectVo> balanceSubjectMap = multiColumnContext.getBalanceSubjectMap();

        Map<Long,List<Long>>  idTree = new HashMap<>();
        Map<String,List<Long>> codeTree = new HashMap<>();
        for(AccountSubjectVo p:accountSubjectVoList){
            List<Long> childIds = codeTree.get(p.getCode());
            if(childIds!=null){
                idTree.put(p.getId(),listToTree(idTree,childIds));
                for(BalanceSubjectVo entityPeriodBalance:entityPeriodBalanceList){
                    BalanceSubjectVo balanceSubjectVo =new BalanceSubjectVo();
                    balanceSubjectVo.setAccountBookId(entityPeriodBalance.getAccountBookId());
                    balanceSubjectVo.setAccountBookEntityId(entityPeriodBalance.getAccountBookEntityId());
                    balanceSubjectVo.setPeriodYear(entityPeriodBalance.getPeriodYear());
                    balanceSubjectVo.setPeriodNum(entityPeriodBalance.getPeriodNum());
                    balanceSubjectVo.setPeriodYearNum(entityPeriodBalance.getPeriodYearNum());
                    boolean isOk = false;
                    for(Long childId:childIds){
                        BalanceSubjectVo childBalanceSubject  =  balanceSubjectMap.get(generateEntityPeriodSubjectKey(entityPeriodBalance.getAccountBookEntityId(),entityPeriodBalance.getPeriodYearNum(),childId));
                        if(childBalanceSubject!=null){
                            isOk =true;
                            addBalanceSubject(balanceSubjectVo,childBalanceSubject);
                        }
                    }
                    if(isOk)
                        balanceSubjectMap.put(generateEntityPeriodSubjectKey(entityPeriodBalance.getAccountBookEntityId(),entityPeriodBalance.getPeriodYearNum(),p.getId()),balanceSubjectVo);
                }
            }
            List<Long> brotherIds = codeTree.get(p.getUpCode());
            if(brotherIds==null){
                brotherIds = new ArrayList<>();
                codeTree.put(p.getUpCode(),brotherIds);
            }
            brotherIds.add(p.getId());
        }
        multiColumnContext.setSubjectIdTree(idTree);
    }

    /**
     * @description:
     * @param: [idTree, childIds]
     * @return: void
     * @author: xdy
     * @create: 2019-09-20 14:36
     */
    private List<Long> listToTree(Map<Long,List<Long>> idTree,List<Long> ids){
        List list = new ArrayList();
        if(ids!=null){
            for(Long id:ids){
                List childIds = idTree.get(id);
                if(childIds==null){
                    list.add(id);
                }else{
                    list.addAll(listToTree(idTree,childIds));
                }
            }
        }
        return list;
    }

    /**
     * @description: 期初余额
     * @param: [multiColumnContext]
     * @return: void
     * @author: xdy
     * @create: 2019-09-02 16:41
     */
    private void openingBalance(MultiColumnContext multiColumnContext){
        MultiColumnReportItemVo multiColumnReportItemVo = new MultiColumnReportItemVo();
        multiColumnReportItemVo.setAccountBookEntityName(multiColumnContext.getVoucherEntryVo().getAccountBookEntityName());
        multiColumnReportItemVo.setAbstractContent("期初余额");
        multiColumnReportItemVo.setIsTotal(Constant.Is.NO);
        //期初余额
        multiColumnReportItemVo.setTotalBalance(findOpeningBalance(multiColumnContext));
        //方向
        direction(multiColumnReportItemVo,multiColumnContext);
        multiColumnReportItemVo.setMultiColumnAmount(new ArrayList<>());
        //多栏账
        List<MultiColumnSchemeItem> multiColumnSchemeItems = multiColumnContext.getMultiColumnSchemeItems();
        if(multiColumnSchemeItems!=null&&!multiColumnSchemeItems.isEmpty()){
            MultiColumnScheme multiColumnScheme = multiColumnContext.getMultiColumnScheme();
            for(int i=0;i< multiColumnSchemeItems.size();i++){
                MultiColumnSchemeItem multiColumnSchemeItem = multiColumnSchemeItems.get(i);
                BigDecimal openingBalance = findOpeningBalance(multiColumnContext,multiColumnSchemeItem.getItemId(),multiColumnScheme.getSchemeType());
                multiColumnReportItemVo.getMultiColumnAmount().add(openingBalance);
            }
        }
        multiColumnContext.getItemList().add(multiColumnReportItemVo);
        multiColumnContext.setPrevItem(multiColumnReportItemVo);
    }

    /**
     * @description: 获取期初
     * @param: [multiColumnContext]
     * @return: java.math.BigDecimal
     * @author: xdy
     * @create: 2019-09-20 10:43
     */
    private BigDecimal findOpeningBalance(MultiColumnContext multiColumnContext){
        return findOpeningBalance(multiColumnContext,null,Constant.MultiColumnScheme.SCHEME_TYPE_SUBJECT);
    }

    /**
     * @description: 获取期初
     * @param: [multiColumnContext, itemId, schemeType]
     * @return: java.math.BigDecimal
     * @author: xdy
     * @create: 2019-09-20 10:21
     */
    private BigDecimal findOpeningBalance(MultiColumnContext multiColumnContext,Long itemId,Byte schemeType){
        VoucherEntryVo voucherEntryVo =  multiColumnContext.getVoucherEntryVo();
        AccountSubjectVo accountSubjectVo = multiColumnContext.getAccountSubjectVo();
        Map<String,? extends Balance> balanceMap = null;
        String baseKey="";
        if(Constant.MultiColumnScheme.SCHEME_TYPE_AUXILIAY.equals(schemeType)){
            balanceMap = multiColumnContext.getBalanceSubjectAuxiliaryMap();
            baseKey = voucherEntryVo.getAccountBookEntityId()+":%d:"+voucherEntryVo.getAccountSubjectId()+":"+itemId;
        }else if(Constant.MultiColumnScheme.SCHEME_TYPE_SUBJECT.equals(schemeType)){
            balanceMap = multiColumnContext.getBalanceSubjectMap();
            if(itemId==null)
                baseKey = voucherEntryVo.getAccountBookEntityId()+":%d";
            else
                baseKey = voucherEntryVo.getAccountBookEntityId()+":%d:"+itemId;
        }
        if(balanceMap==null)
            return null;
        Balance balance = balanceMap.get(String.format(baseKey,voucherEntryVo.getPeriodYearNum()));

        BigDecimal openingBalance = null;
        if(balance!=null){
            Integer periodYearNum = balance.getPeriodYearNum();
            Integer minPeriodYearNum =  multiColumnContext.getMinPeriodYearNum();
            if(periodYearNum>=minPeriodYearNum){
                Integer initPeriodYearNum = balance.getPeriodYear()*100;
                Balance init = balanceMap.get(String.format(baseKey,initPeriodYearNum));
                if(init!=null)
                    openingBalance = init.getOpeningBalance();
                for(int i=initPeriodYearNum+1;i<periodYearNum;i++){
                    Balance p = balanceMap.get(String.format(baseKey,i));
                    BigDecimal amount=null;
                    if(p!=null){
                        if(Constant.BalanceDirection.DEBIT.equals(accountSubjectVo.getBalanceDirection())){
                            amount = p.getDebitAmount().subtract(p.getCreditAmount());
                        }else{
                            amount = p.getCreditAmount().subtract(p.getDebitAmount());
                        }
                    }
                    openingBalance = add(openingBalance,amount);
                }
            }else{
                openingBalance= balance.getOpeningBalance();
            }
        }
        return openingBalance;
    }

    /**
     * @description: 累计
     * @param: [multiColumnContext]
     * @return: java.math.BigDecimal[]
     * @author: xdy
     * @create: 2019-09-20 11:38
     */
    private BigDecimal[] findTotalAmount(MultiColumnContext multiColumnContext){
        return findTotalAmount(multiColumnContext,null,Constant.MultiColumnScheme.SCHEME_TYPE_SUBJECT);
    }

    /**
     * @description: 累计
     * @param: [multiColumnContext, itemId, schemeType]
     * @return: java.math.BigDecimal[]
     * @author: xdy
     * @create: 2019-09-20 11:38
     */
    private BigDecimal[] findTotalAmount(MultiColumnContext multiColumnContext,Long itemId,Byte schemeType){
        VoucherEntryVo voucherEntryVo =  multiColumnContext.getVoucherEntryVo();
        Map<String,? extends Balance> balanceMap = null;
        String baseKey="";
        if(Constant.MultiColumnScheme.SCHEME_TYPE_AUXILIAY.equals(schemeType)){
            balanceMap = multiColumnContext.getBalanceSubjectAuxiliaryMap();
            baseKey = voucherEntryVo.getAccountBookEntityId()+":%d:"+voucherEntryVo.getAccountSubjectId()+":"+itemId;
        }else if(Constant.MultiColumnScheme.SCHEME_TYPE_SUBJECT.equals(schemeType)){
            balanceMap = multiColumnContext.getBalanceSubjectMap();
            if(itemId==null)
                baseKey = voucherEntryVo.getAccountBookEntityId()+":%d";
            else
                baseKey = voucherEntryVo.getAccountBookEntityId()+":%d:"+itemId;
        }
        if(balanceMap==null)
            return null;
        Balance balance = balanceMap.get(String.format(baseKey,voucherEntryVo.getPeriodYearNum()));

        BigDecimal[] totalAmount = null;
        if(balance!=null){
            Integer periodYearNum = balance.getPeriodYearNum();
            Integer minPeriodYearNum = multiColumnContext.getMinPeriodYearNum();
            if(periodYearNum>=minPeriodYearNum){
                Integer initPeriodYearNum = balance.getPeriodYear()*100;
                Balance init = balanceMap.get(String.format(baseKey,initPeriodYearNum));
                totalAmount =  findTotalAmount(init,multiColumnContext);
                for(int i=initPeriodYearNum+1;i<=periodYearNum;i++){
                    Balance p = balanceMap.get(String.format(baseKey,i));
                    BigDecimal[] tempAmount = findPeriodAmount(p,multiColumnContext);
                    for(int j=0;j<totalAmount.length;j++){
                        totalAmount[j] = add(totalAmount[j],tempAmount[j]);
                    }
                }
            }else{
                totalAmount = findTotalAmount(balance,multiColumnContext);
            }
        }
        return totalAmount;
    }

    /**
     * @description: 凭证分录余额
     * @param: [multiColumnContext]
     * @return: void
     * @author: xdy
     * @create: 2019-09-03 09:06
     */
    private void voucherEntryBalance(MultiColumnContext multiColumnContext){
        VoucherEntryVo voucherEntryVo = multiColumnContext.getVoucherEntryVo();
        List<MultiColumnReportItemVo> itemList = multiColumnContext.getItemList();
        MultiColumnScheme multiColumnScheme = multiColumnContext.getMultiColumnScheme();
        List<MultiColumnSchemeItem> multiColumnSchemeItems = multiColumnContext.getMultiColumnSchemeItems();
        boolean[] subjectHasData = multiColumnContext.getSubjectHasData();
        //凭证分录
        MultiColumnReportItemVo multiColumnReportItemVo = new MultiColumnReportItemVo();
        multiColumnReportItemVo.setAccountBookEntityName(voucherEntryVo.getAccountBookEntityName());
        multiColumnReportItemVo.setVoucherDate(voucherEntryVo.getVoucherDate());
        multiColumnReportItemVo.setVoucherId(voucherEntryVo.getVoucherId());
        multiColumnReportItemVo.setCredentialWord(voucherEntryVo.getCredentialWord());
        multiColumnReportItemVo.setMainCode(voucherEntryVo.getMainCode());
        multiColumnReportItemVo.setCredentialWordCode(wordCode(voucherEntryVo));
        multiColumnReportItemVo.setAbstractContent(voucherEntryVo.getAbstractContent());
        multiColumnReportItemVo.setTotalDebitAmount(voucherEntryVo.getDebitAmount());
        multiColumnReportItemVo.setTotalCreditAmount(voucherEntryVo.getCreditAmount());
        multiColumnReportItemVo.setIsTotal(Constant.Is.NO);
        //计算合计余额  方向借  借加贷减  方向贷 贷加借减
        totalBalance(multiColumnReportItemVo,multiColumnContext);
        //方向
        direction(multiColumnReportItemVo,multiColumnContext);
        //分栏明细账
        List<BigDecimal> multiColumnAmount = new ArrayList<>();
        if(multiColumnSchemeItems!=null&&!multiColumnSchemeItems.isEmpty()){

            Map<Long,List<Long>> subjectIdTree = multiColumnContext.getSubjectIdTree();
            Map<Long,List<Long>> voucherEntryAuxiliaryMap = multiColumnContext.getVoucherEntryAuxiliaryMap();


            List<BigDecimal> periodMultiColumnAmount = multiColumnContext.getMultiColumnAmount();
            for(int i=0;i<multiColumnSchemeItems.size();i++){
                MultiColumnSchemeItem multiColumnSchemeItem = multiColumnSchemeItems.get(i);
                BigDecimal columnAmount=null;
                List<Long> subjectIds = new ArrayList<>();
                if(Constant.MultiColumnScheme.SCHEME_TYPE_SUBJECT.equals(multiColumnScheme.getSchemeType())){//会计科目
                    //本科目及下级所有末级科目
                    subjectIds.add(multiColumnSchemeItem.getItemId());
                    List<Long> childIds = subjectIdTree.get(multiColumnSchemeItem.getItemId());
                    if(childIds!=null){
                        subjectIds.addAll(childIds);
                    }
                    if(subjectIds!=null&&subjectIds.contains(voucherEntryVo.getAccountSubjectId())){
                        columnAmount = findVoucherAmount(multiColumnSchemeItem.getDirection(),multiColumnContext);
                        subjectHasData[i] = true;
                    }
                }else if(Constant.MultiColumnScheme.SCHEME_TYPE_AUXILIAY.equals(multiColumnScheme.getSchemeType())){//辅助核算
                    subjectIds  = voucherEntryAuxiliaryMap.get(voucherEntryVo.getId());
                    if(subjectIds!=null&&subjectIds.contains(multiColumnSchemeItem.getItemId())){
                        columnAmount = findVoucherAmount(multiColumnSchemeItem.getDirection(),multiColumnContext);
                        subjectHasData[i] = true;
                    }
                }

                multiColumnAmount.add(columnAmount);
                //右侧合计
                BigDecimal periodColumnAmount;
                if(i>=periodMultiColumnAmount.size()){
                    periodColumnAmount = new BigDecimal(0);
                    periodMultiColumnAmount.add(periodColumnAmount);
                }else {
                    periodColumnAmount = periodMultiColumnAmount.get(i);
                }
                periodMultiColumnAmount.set(i,add(columnAmount,periodColumnAmount));
            }

        }
        multiColumnReportItemVo.setMultiColumnAmount(multiColumnAmount);
        itemList.add(multiColumnReportItemVo);
        multiColumnContext.setPrevItem(multiColumnReportItemVo);
    }

    /**
     * @description: 期间统计
     * @param: [multiColumnContext]
     * @return: void
     * @author: xdy
     * @create: 2019-09-02 17:49
     */
    private void periodTotal(MultiColumnContext multiColumnContext){
        VoucherEntryVo voucherEntryVo = multiColumnContext.getVoucherEntryVo();
        List<MultiColumnReportItemVo> itemList = multiColumnContext.getItemList();
        List<BigDecimal> multiColumnAmount = multiColumnContext.getMultiColumnAmount();
        //本月合计
        MultiColumnReportItemVo[] multiColumnReportItemVos = new MultiColumnReportItemVo[2];
        MultiColumnReportItemVo multiColumnReportItemVo = new MultiColumnReportItemVo();
        multiColumnReportItemVo.setAbstractContent("本月合计");
        multiColumnReportItemVo.setTotalDebitAmount(multiColumnContext.getDebitAmount());
        multiColumnReportItemVo.setTotalCreditAmount(multiColumnContext.getCreditAmount());
        multiColumnReportItemVo.setTotalBalance(multiColumnContext.getBalanceAmount());
        //右侧多栏合计
        multiColumnReportItemVo.setMultiColumnAmount(multiColumnAmount);
        multiColumnReportItemVos[0] = multiColumnReportItemVo;

        //本年累计
        multiColumnReportItemVo = new MultiColumnReportItemVo();
        multiColumnReportItemVo.setAbstractContent("本年累计");
        BigDecimal[] totalAmount = findTotalAmount(multiColumnContext);
        multiColumnReportItemVo.setTotalDebitAmount(totalAmount[0]);
        multiColumnReportItemVo.setTotalCreditAmount(totalAmount[1]);
        multiColumnReportItemVo.setTotalBalance(totalAmount[2]);
        multiColumnReportItemVos[1] = multiColumnReportItemVo;
        multiColumnContext.setPrevItem(multiColumnReportItemVo);
        for(int i=0;i<multiColumnReportItemVos.length;i++){
            multiColumnReportItemVo = multiColumnReportItemVos[i];
            multiColumnReportItemVo.setAccountBookEntityName(voucherEntryVo.getAccountBookEntityName());
            direction(multiColumnReportItemVo,multiColumnContext);
            multiColumnReportItemVo.setIsTotal(Constant.Is.YES);
            itemList.add(multiColumnReportItemVo);
        }

        //右侧多栏
        List<MultiColumnSchemeItem> multiColumnSchemeItems = multiColumnContext.getMultiColumnSchemeItems();
        List<BigDecimal> totalMultiColumnAmount = new ArrayList<>();
        if(multiColumnSchemeItems!=null&&!multiColumnSchemeItems.isEmpty()){
            MultiColumnScheme multiColumnScheme = multiColumnContext.getMultiColumnScheme();

            for(int i=0;i< multiColumnSchemeItems.size();i++){
                MultiColumnSchemeItem multiColumnSchemeItem = multiColumnSchemeItems.get(i);
                BigDecimal[] amounts =  findTotalAmount(multiColumnContext,multiColumnSchemeItem.getItemId(),multiColumnScheme.getSchemeType());
                BigDecimal amount=null;
                if(amounts!=null&&amounts.length==3){
                    amount = amounts[2];
                }
                totalMultiColumnAmount.add(amount);
            }
            multiColumnReportItemVos[1].setMultiColumnAmount(totalMultiColumnAmount);
        }

        //重置本期合计
        multiColumnContext.setDebitAmount(new BigDecimal(0));
        multiColumnContext.setCreditAmount(new BigDecimal(0));
        multiColumnContext.setBalanceAmount(new BigDecimal(0));
        multiColumnContext.setMultiColumnAmount(new ArrayList<>());

    }

    /**
     * @description: 获取本期合计 本年累计
     * @param: [balance, multiColumnContext]
     * @return: java.math.BigDecimal[]
     * @author: xdy
     * @create: 2019-09-04 15:59
     */
    private BigDecimal[] findPeriodAmount(Balance balance, MultiColumnContext multiColumnContext){
        BigDecimal periodDebitAmount=null,periodCreditAmount=null,periodAmount=null;
        MultiColumnReportDto multiColumnReportDto = multiColumnContext.getMultiColumnReportDto();
        AccountSubjectVo accountSubjectVo = multiColumnContext.getAccountSubjectVo();
        if(balance!=null){
            periodDebitAmount = balance.getDebitAmount();
            periodCreditAmount = balance.getCreditAmount();
            //不包含未过账
            if(Constant.Is.NO.equals(multiColumnReportDto.getIsIncludeUnbooked())){
                periodDebitAmount = balance.getPostDebitAmount();
                periodCreditAmount = balance.getPostCreditAmount();
            }
            //不包含损益结转
            if(Constant.Is.NO.equals(multiColumnReportDto.getIsIncludeProfitAndLoss())){
                if(Constant.Is.NO.equals(multiColumnReportDto.getIsIncludeUnbooked())){
                    periodDebitAmount = periodDebitAmount.subtract(balance.getPostSyDebitAmount());
                    periodCreditAmount = periodCreditAmount.subtract(balance.getPostSyCreditAmount());
                }else if(Constant.Is.YES.equals(multiColumnReportDto.getIsIncludeUnbooked())){
                    periodDebitAmount = periodDebitAmount.subtract(balance.getSyDebitAmount());
                    periodCreditAmount = periodCreditAmount.subtract(balance.getSyCreditAmount());
                }
            }
            if(Constant.BalanceDirection.DEBIT.equals(accountSubjectVo.getBalanceDirection())){
                periodAmount = periodDebitAmount.subtract(periodCreditAmount);
            }else if(Constant.BalanceDirection.CREDIT.equals(accountSubjectVo.getBalanceDirection())){
                periodAmount = periodCreditAmount.subtract(periodDebitAmount);
            }
        }
        BigDecimal[] amount = new BigDecimal[]{periodDebitAmount,periodCreditAmount,periodAmount};
        return amount;
    }

    /**
     * @description: 发生额累计
     * @param: [balance, multiColumnContext]
     * @return: java.math.BigDecimal[]
     * @author: xdy
     * @create: 2019-09-20 11:26
     */
    private BigDecimal[] findTotalAmount(Balance balance, MultiColumnContext multiColumnContext){
        BigDecimal totalDebitAmount=null,totalCreditAmount=null,totalAmount=null;
        MultiColumnReportDto multiColumnReportDto = multiColumnContext.getMultiColumnReportDto();
        AccountSubjectVo accountSubjectVo = multiColumnContext.getAccountSubjectVo();
        if(balance!=null){
            totalDebitAmount = balance.getTotalDebitAmount();
            totalCreditAmount = balance.getTotalCreditAmount();
            //不包含未过账
            if(Constant.Is.NO.equals(multiColumnReportDto.getIsIncludeUnbooked())){
                totalDebitAmount = balance.getPostTotalDebitAmount();
                totalCreditAmount = balance.getPostTotalCreditAmount();
            }
            //不包含损益结转
            if(Constant.Is.NO.equals(multiColumnReportDto.getIsIncludeProfitAndLoss())){
                if(Constant.Is.NO.equals(multiColumnReportDto.getIsIncludeUnbooked())){
                    totalDebitAmount = totalDebitAmount.subtract(balance.getPostSyTotalDebitAmount());
                    totalCreditAmount = totalCreditAmount.subtract(balance.getPostSyTotalCreditAmount());
                }else if(Constant.Is.YES.equals(multiColumnReportDto.getIsIncludeUnbooked())){
                    totalDebitAmount = totalDebitAmount.subtract(balance.getSyTotalDebitAmount());
                    totalCreditAmount = totalCreditAmount.subtract(balance.getSyTotalCreditAmount());
                }
            }
            if(Constant.BalanceDirection.DEBIT.equals(accountSubjectVo.getBalanceDirection())){
                totalAmount = totalDebitAmount.subtract(totalCreditAmount);
            }else if(Constant.BalanceDirection.CREDIT.equals(accountSubjectVo.getBalanceDirection())){
                totalAmount = totalCreditAmount.subtract(totalDebitAmount);
            }
        }
        BigDecimal[] amount = new BigDecimal[]{totalDebitAmount,totalCreditAmount,totalAmount};
        return amount;
    }

    /**
     * @description: 删除无数据项目
     * @param: [multiColumnContext]
     * @return: void
     * @author: xdy
     * @create: 2019-09-02 16:06
     */
    private void delNoDataCol(MultiColumnContext multiColumnContext){
        if(multiColumnContext.isHasSubjectItem())
            return;
        List<Integer> noDataIndex = new ArrayList<>();
        for(int i=0;i<multiColumnContext.getSubjectHasData().length;i++){
            if(!multiColumnContext.getSubjectHasData()[i]){
                noDataIndex.add(i);
            }
        }
        if(noDataIndex.isEmpty())
            return;
        //正序排序
        Collections.sort(noDataIndex);
        //反转之后变成倒序 排序
        Collections.reverse(noDataIndex);
        //分录余额分栏
        for(MultiColumnReportItemVo itemVo:multiColumnContext.getItemList()){
            if(itemVo.getMultiColumnAmount()==null||itemVo.getMultiColumnAmount().isEmpty())
                continue;
            for(int index:noDataIndex){
                if(index<itemVo.getMultiColumnAmount().size())
                    itemVo.getMultiColumnAmount().remove(index);
            }
        }
        //项目标题分栏
        List<MultiColumnSchemeItem> multiColumnSchemeItems = multiColumnContext.getMultiColumnSchemeItems();
        if(multiColumnSchemeItems!=null&&!multiColumnSchemeItems.isEmpty()){
            for(int index:noDataIndex){
                if(index<multiColumnSchemeItems.size()){
                    multiColumnSchemeItems.remove(index);
                }
            }
        }
    }

    /**
     * @description: 会计科目余额
     * @param: [balanceSubjectVoList]
     * @return: java.util.Map<java.lang.String,com.njwd.entity.ledger.vo.BalanceSubjectVo>
     * @author: xdy
     * @create: 2019-09-04 17:06
     */
    private void balanceSubjectListToMap(MultiColumnContext multiColumnContext, List<BalanceSubjectVo> balanceSubjectVoList){
        Map<String, BalanceSubjectVo> balanceSubjectMap = new HashMap<>();
        List<BalanceSubjectVo> entityPeriodBalanceList = new ArrayList<>();
        for(BalanceSubjectVo balanceSubjectVo:balanceSubjectVoList){
            BalanceSubjectVo peroidSubject = balanceSubjectMap.get(generateEntityPeriodKey(balanceSubjectVo.getAccountBookEntityId(),balanceSubjectVo.getPeriodYearNum()));
            if(peroidSubject==null){
                peroidSubject =new BalanceSubjectVo();
                peroidSubject.setAccountBookId(balanceSubjectVo.getAccountBookId());
                peroidSubject.setAccountBookEntityId(balanceSubjectVo.getAccountBookEntityId());
                peroidSubject.setPeriodYear(balanceSubjectVo.getPeriodYear());
                peroidSubject.setPeriodNum(balanceSubjectVo.getPeriodNum());
                peroidSubject.setPeriodYearNum(balanceSubjectVo.getPeriodYearNum());
                balanceSubjectMap.put(generateEntityPeriodKey(balanceSubjectVo.getAccountBookEntityId(),balanceSubjectVo.getPeriodYearNum()),peroidSubject);
                entityPeriodBalanceList.add(peroidSubject);
            }
            addBalanceSubject(peroidSubject,balanceSubjectVo);
            balanceSubjectMap.put(generateEntityPeriodSubjectKey(balanceSubjectVo.getAccountBookEntityId(),balanceSubjectVo.getPeriodYearNum(),balanceSubjectVo.getAccountSubjectId()),balanceSubjectVo);
        }
        multiColumnContext.setEntityPeriodBalanceList(entityPeriodBalanceList);
        multiColumnContext.setBalanceSubjectMap(balanceSubjectMap);
    }

    private void addBalanceSubject(BalanceSubjectVo peroidSubject,BalanceSubjectVo balanceSubjectVo){
        if(peroidSubject==null||balanceSubjectVo==null)
            return;
        peroidSubject.setOpeningBalance(add(peroidSubject.getOpeningBalance(),balanceSubjectVo.getOpeningBalance()));
        peroidSubject.setClosingBalance(add(peroidSubject.getClosingBalance(),balanceSubjectVo.getClosingBalance()));

        peroidSubject.setDebitAmount(add(peroidSubject.getDebitAmount(),balanceSubjectVo.getDebitAmount()));
        peroidSubject.setCreditAmount(add(peroidSubject.getCreditAmount(),balanceSubjectVo.getCreditAmount()));
        peroidSubject.setTotalDebitAmount(add(peroidSubject.getTotalDebitAmount(),balanceSubjectVo.getTotalDebitAmount()));
        peroidSubject.setTotalCreditAmount(add(peroidSubject.getTotalCreditAmount(),balanceSubjectVo.getTotalCreditAmount()));
        //已过账
        peroidSubject.setPostDebitAmount(add(peroidSubject.getPostDebitAmount(),balanceSubjectVo.getPostDebitAmount()));
        peroidSubject.setPostCreditAmount(add(peroidSubject.getPostCreditAmount(),balanceSubjectVo.getPostCreditAmount()));
        peroidSubject.setPostTotalDebitAmount(add(peroidSubject.getPostTotalDebitAmount(),balanceSubjectVo.getPostTotalDebitAmount()));
        peroidSubject.setPostTotalCreditAmount(add(peroidSubject.getPostTotalCreditAmount(),balanceSubjectVo.getPostTotalCreditAmount()));
        //损益
        peroidSubject.setSyDebitAmount(add(peroidSubject.getSyDebitAmount(),balanceSubjectVo.getSyDebitAmount()));
        peroidSubject.setSyCreditAmount(add(peroidSubject.getSyCreditAmount(),balanceSubjectVo.getSyCreditAmount()));
        peroidSubject.setSyTotalDebitAmount(add(peroidSubject.getSyTotalDebitAmount(),balanceSubjectVo.getSyTotalDebitAmount()));
        peroidSubject.setSyTotalCreditAmount(add(peroidSubject.getSyTotalCreditAmount(),balanceSubjectVo.getSyTotalCreditAmount()));
        //已过账损益
        peroidSubject.setPostSyDebitAmount(add(peroidSubject.getPostSyDebitAmount(),balanceSubjectVo.getPostSyDebitAmount()));
        peroidSubject.setPostSyCreditAmount(add(peroidSubject.getPostSyCreditAmount(),balanceSubjectVo.getPostSyCreditAmount()));
        peroidSubject.setPostSyTotalDebitAmount(add(peroidSubject.getPostSyTotalDebitAmount(),balanceSubjectVo.getPostSyTotalDebitAmount()));
        peroidSubject.setPostSyTotalCreditAmount(add(peroidSubject.getPostSyTotalCreditAmount(),balanceSubjectVo.getPostSyTotalCreditAmount()));
    }

    /**
     * @description:
     * @param: [a, b]
     * @return: java.math.BigDecimal
     * @author: xdy
     * @create: 2019-09-04 16:48
     */
    private BigDecimal add(BigDecimal a,BigDecimal b){
        if(a==null)
            a=new BigDecimal(0);
        if(b==null)
            b= new BigDecimal(0);
        return a.add(b);
    }

    /**
     * @description: 会计科目转为分栏帐项目
     * @param: [accountSubjectVoList]
     * @return: java.util.List<com.njwd.entity.ledger.MultiColumnSchemeItem>
     * @author: xdy
     * @create: 2019-09-02 17:06
     */
    private List<MultiColumnSchemeItem> convertToSchemeItem(List<AccountSubjectVo> accountSubjectVoList){
        List<MultiColumnSchemeItem> multiColumnSchemeItems = null;
        if(accountSubjectVoList!=null&&!accountSubjectVoList.isEmpty()){
            multiColumnSchemeItems = new ArrayList<>();
            for(AccountSubjectVo subjectVo:accountSubjectVoList){
                MultiColumnSchemeItem multiColumnSchemeItem = new MultiColumnSchemeItem();
                multiColumnSchemeItem.setItemId(subjectVo.getId());
                multiColumnSchemeItem.setDirection(subjectVo.getBalanceDirection());
                multiColumnSchemeItems.add(multiColumnSchemeItem);
            }
        }
        return multiColumnSchemeItems;
    }

    /**
     * @description: 会计科目转为分栏帐项目
     * @param: [auxiliaryList, accountSubjectVo]
     * @return: java.util.List<com.njwd.entity.ledger.MultiColumnSchemeItem>
     * @author: xdy
     * @create: 2019-09-03 11:08
     */
    private List<MultiColumnSchemeItem> convertToSchemeItem(List<Map<String,Object>> auxiliaryList,AccountSubjectVo accountSubjectVo){
        List<MultiColumnSchemeItem> multiColumnSchemeItems = null;
        if(auxiliaryList!=null&&!auxiliaryList.isEmpty()){
            multiColumnSchemeItems = new ArrayList<>();
            for(Map<String,Object> auxiliary:auxiliaryList){
                MultiColumnSchemeItem multiColumnSchemeItem = new MultiColumnSchemeItem();
                multiColumnSchemeItem.setItemId(Long.valueOf(auxiliary.get("id").toString()));
                multiColumnSchemeItem.setDirection(accountSubjectVo.getBalanceDirection());
                multiColumnSchemeItems.add(multiColumnSchemeItem);
            }
        }
        return multiColumnSchemeItems;
    }

    /**
     * @description: 获取凭证余额
     * @param: [multiColumnContext]
     * @return: java.math.BigDecimal
     * @author: xdy
     * @create: 2019-09-02 16:48
     */
    private BigDecimal findVoucherAmount(MultiColumnContext multiColumnContext){
        return findVoucherAmount(multiColumnContext.getAccountSubjectVo().getBalanceDirection(),multiColumnContext);
    }

    /**
     * @description: 获取凭证余额
     * @param: [multiColumnContext, balanceDirection]
     * @return: java.math.BigDecimal
     * @author: xdy
     * @create: 2019-09-03 09:18
     */
    private BigDecimal findVoucherAmount(Byte balanceDirection,MultiColumnContext multiColumnContext){
        VoucherEntryVo voucherEntryVo = multiColumnContext.getVoucherEntryVo();
        BigDecimal amount = null;
        if(Constant.BalanceDirection.DEBIT.equals(balanceDirection)){
            if(voucherEntryVo.getDebitAmount()!=null&&voucherEntryVo.getDebitAmount().compareTo(BigDecimal.ZERO)!=0){
                amount = voucherEntryVo.getDebitAmount();
            }else if(voucherEntryVo.getCreditAmount()!=null&&voucherEntryVo.getCreditAmount().compareTo(BigDecimal.ZERO)!=0){
                amount = voucherEntryVo.getCreditAmount().negate();
            }
        }else if(Constant.BalanceDirection.CREDIT.equals(balanceDirection)){
            if(voucherEntryVo.getDebitAmount()!=null&&voucherEntryVo.getDebitAmount().compareTo(BigDecimal.ZERO)!=0){
                amount = voucherEntryVo.getDebitAmount().negate();
            }else if(voucherEntryVo.getCreditAmount()!=null&&voucherEntryVo.getCreditAmount().compareTo(BigDecimal.ZERO)!=0){
                amount = voucherEntryVo.getCreditAmount();
            }
        }
        return amount;
    }

    /**
     * @description: 合计余额
     * @param: []
     * @return: void
     * @author: xdy
     * @create: 2019-09-02 17:17
     */
    private void totalBalance(MultiColumnReportItemVo multiColumnReportItemVo,MultiColumnContext multiColumnContext){

        VoucherEntryVo voucherEntryVo = multiColumnContext.getVoucherEntryVo();
        multiColumnContext.setDebitAmount(add(multiColumnContext.getDebitAmount(),voucherEntryVo.getDebitAmount()));
        multiColumnContext.setCreditAmount(add(multiColumnContext.getCreditAmount(),voucherEntryVo.getCreditAmount()));
        multiColumnContext.setBalanceAmount(add(multiColumnContext.getBalanceAmount(),findVoucherAmount(multiColumnContext)));
        multiColumnReportItemVo.setTotalBalance(multiColumnContext.getBalanceAmount());
        /*MultiColumnReportItemVo prevItem = multiColumnContext.getPrevItem();
        BigDecimal prevBalance = new BigDecimal(0);
        if(prevItem!=null&&prevItem.getTotalBalance()!=null){
            prevBalance = prevBalance.add(prevItem.getTotalBalance());
        }
        multiColumnReportItemVo.setTotalBalance(prevBalance.add(findVoucherAmount(multiColumnContext)));*/
        //VoucherEntryVo voucherEntryVo = multiColumnContext.getVoucherEntryVo();
        //logger.debug("{}-{}-{}-{},preBalance:{},voucherAmount:{}",voucherEntryVo.getAccountBookEntityName(),voucherEntryVo.getCredentialWord()
        //        ,voucherEntryVo.getMainCode(),voucherEntryVo.getAbstractContent(),prevBalance.doubleValue(),findVoucherAmount(multiColumnContext).doubleValue());
    }

    /**
     * @description: 明细方向
     * @param: [multiColumnReportItemVo, multiColumnContext]
     * @return: void
     * @author: xdy
     * @create: 2019-09-02 17:41
     */
    private void direction(MultiColumnReportItemVo multiColumnReportItemVo,MultiColumnContext multiColumnContext){
        AccountSubjectVo accountSubjectVo = multiColumnContext.getAccountSubjectVo();
        if(multiColumnReportItemVo.getTotalBalance()!=null&&multiColumnReportItemVo.getTotalBalance().compareTo(BigDecimal.ZERO)!=0){
            multiColumnReportItemVo.setBalanceDirection(accountSubjectVo.getBalanceDirection());
        }else {
            multiColumnReportItemVo.setBalanceDirection(Constant.BalanceDirection.FLAT);
        }
        if(Constant.BalanceDirection.DEBIT.equals(multiColumnReportItemVo.getBalanceDirection())){
            multiColumnReportItemVo.setBalanceDirectionName(Constant.BalanceDirectionName.DEBIT);
        }else if(Constant.BalanceDirection.CREDIT.equals(multiColumnReportItemVo.getBalanceDirection())){
            multiColumnReportItemVo.setBalanceDirectionName(Constant.BalanceDirectionName.CREDIT);
        }else {
            multiColumnReportItemVo.setBalanceDirectionName(Constant.BalanceDirectionName.FLAT);
        }
    }

    /**
     * @description: 主体期间key
     * @param: [entityId, yearNum]
     * @return: java.lang.String
     * @author: xdy
     * @create: 2019-09-04 15:04
     */
    private String generateEntityPeriodKey(Long entityId,Integer yearNum){
        return String.format("%d:%d",entityId,yearNum);
    }

    /**
     * @description: 主体期间科目主键
     * @param: [entityId, yearNum, subjectId]
     * @return: java.lang.String
     * @author: xdy
     * @create: 2019-09-04 16:37
     */
    private String generateEntityPeriodSubjectKey(Long entityId,Integer yearNum,Long subjectId){
        return String.format("%d:%d:%d",entityId,yearNum,subjectId);
    }

    /**
     * @description: 主体期间辅助核算主键
     * @param: [entityId, yearNum, itemId]
     * @return: java.lang.String
     * @author: xdy
     * @create: 2019-09-04 15:04
     */
    private String generateEntityPeriodSubjectAuxiliaryKey(Long entityId,Integer yearNum,Long subjectId,Long itemId){
        return String.format("%d:%d:%d:%d",entityId,yearNum,subjectId,itemId);
    }

    /**
     * @description: 凭证字
     * @param: [voucherEntryVo]
     * @return: java.lang.String
     * @author: xdy
     * @create: 2019-09-05 14:19
     */
    private String wordCode(VoucherEntryVo voucherEntryVo){
        String wordName="";
        switch (voucherEntryVo.getCredentialWord()){
            case Constant.CredentialWordType.RECORD:
                wordName = LedgerConstant.CredentialWordName.RECORD;
                break;
            case Constant.CredentialWordType.RECEIVE:
                wordName = LedgerConstant.CredentialWordName.RECEIVE;
                break;
            case Constant.CredentialWordType.PAY:
                wordName = LedgerConstant.CredentialWordName.PAY;
                break;
            case Constant.CredentialWordType.TRANSFER:
                wordName = LedgerConstant.CredentialWordName.TRANSFER;
                break;
            default:
                if(voucherEntryVo.getCredentialWord()!=null)
                    wordName = String.valueOf(voucherEntryVo.getCredentialWord());
        }
        return wordName+"-"+voucherEntryVo.getMainCode();
    }

    @Getter
    @Setter
    class MultiColumnContext{

        //合计借方 DEBIT
        BigDecimal debitAmount;

        //合计贷方 CREDIT
        BigDecimal creditAmount;

        //合计余额
        BigDecimal balanceAmount;

        //合计
        List<BigDecimal> multiColumnAmount = new ArrayList<>();

        /**
         * 最小期间
         */
        Integer minPeriodYearNum;

        /**
         * 报表请求参数
         */
        MultiColumnReportDto multiColumnReportDto;

        /**
         * 多栏账方案
         */
        MultiColumnScheme multiColumnScheme;

        /**
         * 多栏账方案明细
         */
        List<MultiColumnSchemeItem> multiColumnSchemeItems;

        /**
         * 是否含有项目
         */
        boolean hasSubjectItem=true;

        /**
         * 项目是否有值
         */
        boolean[] subjectHasData;

        /**
         * 会计科目
         */
        AccountSubjectVo accountSubjectVo;

        /**
         *  科目余额
         */
        Map<String, BalanceSubjectVo> balanceSubjectMap;

        /**
         * 凭证分录
         */
        VoucherEntryVo voucherEntryVo;

        /**
         * 凭证分录列表
         */
        List<VoucherEntryVo> voucherEntryVoList;

        /**
         *  科目余额
         */
        BalanceSubjectVo balanceSubjectVo;

        /**
         * 分栏帐明细
         */
        List<MultiColumnReportItemVo> itemList;

        /**
         * 前一个分栏帐明细
         */
        MultiColumnReportItemVo prevItem;

        /**
         * 会计科目辅助核算
         */
        AccountSubjectAuxiliary accountSubjectAuxiliary;

        /**
         * 凭证
         */
        Map<Long,List<Long>> voucherEntryAuxiliaryMap;

        /**
         * 科目余额辅助核算
         */
        Map<String,BalanceSubjectAuxiliaryVo> balanceSubjectAuxiliaryMap;

        /**
         * 会计科目ID
         */
        Map<Long,List<Long>>  subjectIdTree;


        List<BalanceSubjectVo> entityPeriodBalanceList;

    }

}
