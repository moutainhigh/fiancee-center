package com.njwd.ledger.service.impl;

import com.njwd.common.Constant;
import com.njwd.common.LedgerConstant;
import com.njwd.entity.basedata.dto.AccountBookEntityDto;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.ledger.dto.ItemJournalQueryDto;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;
import com.njwd.entity.ledger.vo.GeneralReturnItemJournalVo;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.ledger.cloudclient.AccountSubjectFeignClient;
import com.njwd.ledger.mapper.ItemJournalMapper;
import com.njwd.ledger.service.AccountBookPeriodService;
import com.njwd.ledger.service.AuxiliaryAccountingService;
import com.njwd.ledger.service.ItemJournalService;
import com.njwd.service.FileService;
import com.njwd.utils.FastUtils;
import com.njwd.utils.MergeUtil;
import com.njwd.utils.StringUtil;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Description
 * @Author: ZhuHC
 * @Date: 2019/8/6 17:17
 */
@Service
public class ItemJournalServiceImpl implements ItemJournalService {

    @Resource
    private ItemJournalMapper itemJournalMapper;

    @Resource
    private AccountSubjectFeignClient accountSubjectFeignClient;

    @Resource
    private AccountBookPeriodService accountBookPeriodService;

    @Resource
    private FileService fileService;

    @Resource
    private AuxiliaryAccountingService auxiliaryAccountingService;

    /**
     * @author ZhuHC
     * @date  2019/8/7 9:09
     * @param itemJournalQueryDto
     * @return java.util.List
     * @description 科目日记账
     */
    @Override
    public List<GeneralReturnItemJournalVo> getItemJournalDetails(ItemJournalQueryDto itemJournalQueryDto){
        // 获得 明细账
        return getItemJournalInfo(itemJournalQueryDto);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/29 17:56
     * @Param [itemJournalQueryDto, response]
     * @return void
     * @Description  科目日记账  数据导出
     */
    @Override
    public void exportExcel(ItemJournalQueryDto itemJournalQueryDto, HttpServletResponse response) {
        List<GeneralReturnItemJournalVo> returnItemJournalVos = getItemJournalInfo(itemJournalQueryDto);
        if(null != returnItemJournalVos){
            for(GeneralReturnItemJournalVo vo : returnItemJournalVos){
                if(Constant.SortNum.DETAIL.equals(vo.getSortNum())){
                    vo.setCredentialWordCode(auxiliaryAccountingService.convertData(vo.getCredentialWord())+"-"+vo.getMainCode());
                }
                vo.setDirection(convertDirection(vo.getBalanceDirection()));
            }
            if (Constant.Is.YES.equals(itemJournalQueryDto.getIsShowFullName())){
                fileService.exportExcel(response, returnItemJournalVos,
                        new ExcelColumn("accountBookName","账簿名称"),
                        new ExcelColumn("subjectCode","科目编码"),
                        new ExcelColumn("subjectFullName","科目名称"),
                        new ExcelColumn("accountBookEntityName","业务单元"),
                        new ExcelColumn("voucherDate","日期"),
                        new ExcelColumn("periodYear","会计年度"),
                        new ExcelColumn("periodNum","期间"),
                        new ExcelColumn("credentialWordCode","凭证字号"),
                        new ExcelColumn("abstractContent","摘要"),
                        new ExcelColumn("debit","借方"),
                        new ExcelColumn("credit","贷方"),
                        new ExcelColumn("direction","方向"),
                        new ExcelColumn("balance","余额")
                );
            }else {
                fileService.exportExcel(response, returnItemJournalVos,
                        new ExcelColumn("accountBookName","账簿名称"),
                        new ExcelColumn("subjectCode","科目编码"),
                        new ExcelColumn("subjectName","科目名称"),
                        new ExcelColumn("accountBookEntityName","业务单元"),
                        new ExcelColumn("voucherDate","日期"),
                        new ExcelColumn("periodYear","会计年度"),
                        new ExcelColumn("periodNum","期间"),
                        new ExcelColumn("credentialWordCode","凭证字号"),
                        new ExcelColumn("abstractContent","摘要"),
                        new ExcelColumn("debit","借方"),
                        new ExcelColumn("credit","贷方"),
                        new ExcelColumn("direction","方向"),
                        new ExcelColumn("balance","余额")
                );
            }
        }
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/25 15:51
     * @Param [itemJournalQueryDto, response]
     * @return void
     * @Description 全部导出
     */
    @Override
    public void exportAllExcel(ItemJournalQueryDto itemJournalQueryDto, HttpServletResponse response) {
        List<GeneralReturnItemJournalVo> returnItemJournalVos = new ArrayList<>();
        List<GeneralReturnItemJournalVo> list;
        List<String> oldCodes = itemJournalQueryDto.getCodes();
        List<String> codes = new LinkedList<>();
        for(String code : oldCodes){
            codes.add(code);
            itemJournalQueryDto.setCodes(codes);
            list = getItemJournalInfo(itemJournalQueryDto);
            returnItemJournalVos.addAll(list);
            codes.clear();
        }
        if(null != returnItemJournalVos){
            for(GeneralReturnItemJournalVo vo : returnItemJournalVos){
                if(Constant.SortNum.DETAIL.equals(vo.getSortNum())){
                    vo.setCredentialWordCode(auxiliaryAccountingService.convertData(vo.getCredentialWord())+"-"+vo.getMainCode());
                }
                vo.setDirection(convertDirection(vo.getBalanceDirection()));
            }
            if (Constant.Is.YES.equals(itemJournalQueryDto.getIsShowFullName())){
                fileService.exportExcel(response, returnItemJournalVos,
                        new ExcelColumn("accountBookName","账簿名称"),
                        new ExcelColumn("subjectCode","科目编码"),
                        new ExcelColumn("subjectFullName","科目名称"),
                        new ExcelColumn("accountBookEntityName","业务单元"),
                        new ExcelColumn("voucherDate","日期"),
                        new ExcelColumn("periodYear","会计年度"),
                        new ExcelColumn("periodNum","期间"),
                        new ExcelColumn("credentialWordCode","凭证字号"),
                        new ExcelColumn("abstractContent","摘要"),
                        new ExcelColumn("debit","借方"),
                        new ExcelColumn("credit","贷方"),
                        new ExcelColumn("direction","方向"),
                        new ExcelColumn("balance","余额")
                );
            }else {
                fileService.exportExcel(response, returnItemJournalVos,
                        new ExcelColumn("accountBookName","账簿名称"),
                        new ExcelColumn("subjectCode","科目编码"),
                        new ExcelColumn("subjectName","科目名称"),
                        new ExcelColumn("accountBookEntityName","业务单元"),
                        new ExcelColumn("voucherDate","日期"),
                        new ExcelColumn("periodYear","会计年度"),
                        new ExcelColumn("periodNum","期间"),
                        new ExcelColumn("credentialWordCode","凭证字号"),
                        new ExcelColumn("abstractContent","摘要"),
                        new ExcelColumn("debit","借方"),
                        new ExcelColumn("credit","贷方"),
                        new ExcelColumn("direction","方向"),
                        new ExcelColumn("balance","余额")
                );
            }
        }
    }


    /**
     * @Author ZhuHC
     * @Date  2019/8/29 17:58
     * @Param [balanceDirection]
     * @return java.lang.String
     * @Description
     */
    private String convertDirection(int balanceDirection){
        //余额方向类型 0：借 、1：贷、2：平
        String wordStr = "";
        switch (balanceDirection){
            case Constant.BalanceDirectionType.DEBIT:
                wordStr = Constant.BalanceDirectionName.CREDIT ;
                break;
            case Constant.BalanceDirectionType.CREDIT:
                wordStr = Constant.BalanceDirectionName.DEBIT ;
                break;
            case Constant.BalanceDirectionType.FLAT:
                wordStr = Constant.BalanceDirectionName.FLAT ;
                break;
            default:
        }
        return wordStr;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/8 16:27
     * @Param [itemJournalQueryDto]
     * @return java.util.List<com.njwd.entity.ledger.vo.GeneralReturnItemJournalVo>
     * @Description 获取数据  处理数据
     */
    private List<GeneralReturnItemJournalVo> getItemJournalInfo(ItemJournalQueryDto itemJournalQueryDto){
        //从基础资料获取 会计科目数据
        List<AccountSubjectVo> subjectList = getAccountSubjectVos(itemJournalQueryDto);
        List<Long> subjectIdList = new LinkedList<>();
        // 去除多余会计科目
        if(!FastUtils.checkNullOrEmpty(subjectList)){
            for(AccountSubjectVo accountSubjectVo : subjectList){
                subjectIdList.add(accountSubjectVo.getId());
            }
        }
        itemJournalQueryDto.setSubjectIdList(subjectIdList);
        // 拼接 合计 明细数据，并拼接 核算项目，会计科目等数据
        return getWholeList(subjectList,itemJournalQueryDto);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/8 16:28
     * @Param [itemJournalQueryDto]
     * @return java.util.List<com.njwd.entity.platform.vo.AccountSubjectVo>
     * @Description  根据条件  获取 会计科目
     */
    private List<AccountSubjectVo>  getAccountSubjectVos(ItemJournalQueryDto itemJournalQueryDto) {
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        accountSubjectDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        Byte operator = itemJournalQueryDto.getAccountSubjectOperator();
        if(null != operator){
            accountSubjectDto.setSubjectCodeOperator(operator);
            if(operator.equals(LedgerConstant.QueryScheme.OPERATOR_EQUAL)){
                accountSubjectDto.setIds(itemJournalQueryDto.getSubjectCodes());
            }
            if(operator.equals(LedgerConstant.QueryScheme.OPERATOR_RANGE)){
                accountSubjectDto.setSubjectCodes(itemJournalQueryDto.getSubjectCodes());
            }
        }
        //仅显示末级科目 (1:仅显示)
        if(LedgerConstant.Ledger.ISFINAL.equals(itemJournalQueryDto.getIsOnlyShowFinal())){
            accountSubjectDto.setIsFinal(LedgerConstant.Ledger.ISFINAL);
        }
        accountSubjectDto.setSubjectLevels(itemJournalQueryDto.getLevels());
        //是否包含禁用科目（0不包含 1包含）
        accountSubjectDto.setIsIncludeEnable(itemJournalQueryDto.getIsIncludeEnable());
        accountSubjectDto.setCodes(itemJournalQueryDto.getCodes());
        return accountSubjectFeignClient.findInfoForLedger(accountSubjectDto).getData();
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/8 16:28
     * @Param [totalList, subjectList, itemJournalQueryDto]
     * @return void
     * @Description 数据整合
     */
    private List<GeneralReturnItemJournalVo> getWholeList(List<AccountSubjectVo> subjectList, ItemJournalQueryDto itemJournalQueryDto) {
        List<GeneralReturnItemJournalVo> totalList = new ArrayList<>();
        getTotalList(itemJournalQueryDto, totalList,subjectList);
        AccountSubjectVo newSubject = null;
        //取得前端传入的CODE 对应的会计科目详情，拼接到 list数据中
        for(AccountSubjectVo vo : subjectList){
            if(vo.getCode().equals(itemJournalQueryDto.getCodes().get(Constant.Number.ZERO))){
                newSubject = vo ;
                break;
            }
        }
        if(!FastUtils.checkNullOrEmpty(totalList) && null != newSubject){
            for(GeneralReturnItemJournalVo itemJournalVo : totalList){
                itemJournalVo.setSubjectCode(newSubject.getCode());
                itemJournalVo.setSubjectName(newSubject.getName());
                itemJournalVo.setSubjectFullName(newSubject.getFullName());
                itemJournalVo.setBalanceDirection(newSubject.getBalanceDirection());
            }
        }
        //计算 方向余额
        List<GeneralReturnItemJournalVo> countList = countBalance(totalList,itemJournalQueryDto);
        //去除 不符合条件的 数据
        if(!FastUtils.checkNullOrEmpty(countList)){
            deleteInfoByStatus(countList,itemJournalQueryDto);
        }
        //将账簿名称 和 主体名称 拼接到 科目数据上
        for(GeneralReturnItemJournalVo vo : countList){
            for(AccountBookEntityDto dto : itemJournalQueryDto.getAccountBookEntityList()){
                if(vo.getAccountBookId().equals(dto.getAccountBookId()) && vo.getAccountBookEntityId().equals(dto.getEntityId())){
                    vo.setAccountBookName(dto.getAccountBookName());
                    vo.setAccountBookEntityName(dto.getEntityName());
                    break;
                }
            }
        }
        return countList;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/18 15:58
     * @Param [subjectList, subjectVos, accountSubjectVo]
     * @return void
     * @Description  获取 末级CODE的所有上级CODE
     */
    private void getUpAccountSubject(List<AccountSubjectVo> subjectList, List<AccountSubjectVo> subjectVos, AccountSubjectVo accountSubjectVo) {
        if(StringUtil.isNotEmpty(accountSubjectVo.getUpCode())){
            for(AccountSubjectVo vo : subjectList){
                if(accountSubjectVo.getUpCode().equals(vo.getCode())){
                    subjectVos.add(vo);
                    getUpAccountSubject(subjectList,subjectVos,vo);
                    break;
                }
            }
        }
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/12 15:44
     * @Param [itemJournalQueryDto, totalList]
     * @return void
     * @Description 遍历 总账账簿 核算主体  会计科目，获取数据
     */
    private void getTotalList(ItemJournalQueryDto itemJournalQueryDto, List<GeneralReturnItemJournalVo> totalList,List<AccountSubjectVo> subjectList) {
        itemJournalQueryDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        List<GeneralReturnItemJournalVo> openingInfo = new ArrayList<>();
        //会计期间查询类型 0 制单日期 1 会计区间
        Byte periodOperator = itemJournalQueryDto.getPeriodOperator();
        Long tempNum = 0L;
        if(0 == periodOperator){
            //设置制单日期从第0期开始
            List<Date> voucherDates = itemJournalQueryDto.getVoucherDates();
            Date d = voucherDates.get(0);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String tempDate = formatter.format(d);
            String[] s = tempDate.split("-");
            tempNum = Long.valueOf(s[0]+"00");
        }else{
            List<Long> periodYearNums = itemJournalQueryDto.getPeriodYearNum();
            //设置区间为第0期
            tempNum = periodYearNums.get(0)/ 100 * 100;
        }
        itemJournalQueryDto.setPeriodNum(tempNum);
        //获取从第0期开始的数据
        List<GeneralReturnItemJournalVo>  zeroInfo = itemJournalMapper.getZeroOpeningInfo(itemJournalQueryDto);
        //取得启用期间的期初余额
        List<GeneralReturnItemJournalVo>  startOpeningInfo = itemJournalMapper.getStartOpeningInfo(itemJournalQueryDto);
        //拼接数据  条件相同 把第0期的数据赋值到最小期间数据上
        if(!FastUtils.checkNullOrEmpty(zeroInfo)){
            MergeUtil.merge(zeroInfo,startOpeningInfo,
                    (z, s) -> z.getAccountBookId().equals(s.getAccountBookId()) && z.getAccountBookEntityId() .equals(s.getAccountBookEntityId()),
                    (z, s) -> {
                        s.setDebit(z.getDebit());
                        s.setCredit(z.getCredit());
                    });
        }
        //取得不同 账簿 主体 科目 下，账簿开始期间到 选择期间前一期的 借贷方发生额；账簿和主体相同时 科目属于同一顶级科目，发生额按同一科目处理
        List<GeneralReturnItemJournalVo>  periodTotalBySubject = itemJournalMapper.getJournalOpening(itemJournalQueryDto);
        //合并list 将科目 余额方向赋给 发生额数据
        if(!FastUtils.checkNullOrEmpty(periodTotalBySubject)){
            MergeUtil.merge(periodTotalBySubject,subjectList,
                    (tol, sub) -> tol.getSubjectId().equals(sub.getId().toString()),
                    (tol, sub) -> {
                        tol.setBalanceDirection(sub.getBalanceDirection());
                    });
            openingInfo = getOpenInfo(periodTotalBySubject,startOpeningInfo,itemJournalQueryDto);
        }else {
            openingInfo.addAll(startOpeningInfo);
        }
        List<GeneralReturnItemJournalVo> detailsInfo = itemJournalMapper.getJournalInfo(itemJournalQueryDto);
        List<GeneralReturnItemJournalVo> numInfo = itemJournalMapper.getJournalPeriodNum(itemJournalQueryDto);
        List<GeneralReturnItemJournalVo> yearInfo = itemJournalMapper.getJournalPeriodYear(itemJournalQueryDto);
        List<GeneralReturnItemJournalVo> usedDetails;
        GeneralReturnItemJournalVo num;
        for(GeneralReturnItemJournalVo opening : openingInfo){
            totalList.add(opening);
            if(!FastUtils.checkNullOrEmpty(numInfo)){
                for(int i = 0;i < numInfo.size();i++){
                    num = numInfo.get(i);
                    //已经添加进 结果list 的 detail
                    usedDetails = new LinkedList<>();
                    if(opening.getAccountBookId().equals(num.getAccountBookId()) && opening.getAccountBookEntityId().equals(num.getAccountBookEntityId())){
                        if(!FastUtils.checkNullOrEmpty(detailsInfo)){
                            for(GeneralReturnItemJournalVo detail : detailsInfo){
                                if( detail.getAccountBookId().equals(num.getAccountBookId()) && detail.getAccountBookEntityId().equals(num.getAccountBookEntityId())
                                        && num.getPeriodYearNum().equals(detail.getPeriodYearNum())){
                                    totalList.add(detail);
                                    usedDetails.add(detail);
                                }
                            }
                            detailsInfo.removeAll(usedDetails);
                        }
                        totalList.add(num);
                        totalList.add(yearInfo.get(i));
                    }
                }
            }
        }
    }

    /**
     * @Author ZhuHC
     * @Date  2019/9/26 10:01
     * @Param [periodTotalBySubject, startOpeningInfo]
     * @return java.util.List<com.njwd.entity.ledger.vo.GeneralReturnItemJournalVo>
     * @Description 计算 借贷金额
     */
    private List<GeneralReturnItemJournalVo> getOpenInfo(List<GeneralReturnItemJournalVo> periodTotalBySubject,List<GeneralReturnItemJournalVo>  startOpeningInfo,ItemJournalQueryDto itemJournalQueryDto) {
        List<GeneralReturnItemJournalVo> voList = new LinkedList<>();
        for(GeneralReturnItemJournalVo vo : periodTotalBySubject){
            vo.setSignFlag(vo.getAccountBookId()+"_"+vo.getAccountBookEntityId());
        }
        Map<String, List<GeneralReturnItemJournalVo>> resultMap = new HashMap<>();
        for (GeneralReturnItemJournalVo vo : periodTotalBySubject) {
            if (resultMap.containsKey(vo.getSignFlag())) {
                //map中存在此flag，将数据存放当前key的map中
                resultMap.get(vo.getSignFlag()).add(vo);
            } else {
                //map中不存在，新建key，用来存放数据
                List<GeneralReturnItemJournalVo> tmpList = new ArrayList<>();
                tmpList.add(vo);
                resultMap.put(vo.getSignFlag(), tmpList);
            }
        }
        DecimalFormat format = new DecimalFormat("##0.00");
        // 借
        String debit ;
        // 贷
        String credit ;
        //余额
        String balance = format.format(0);
        int direction;
        GeneralReturnItemJournalVo journalVo = null;
        for (Map.Entry<String,List<GeneralReturnItemJournalVo>> map : resultMap.entrySet()) {
            for(int i = 0; i< map.getValue().size(); i++){
                GeneralReturnItemJournalVo vo = map.getValue().get(i);
                for(GeneralReturnItemJournalVo startVo: startOpeningInfo){
                    if(0 == i && vo.getAccountBookId().equals(startVo.getAccountBookId()) && vo.getAccountBookEntityId().equals(startVo.getAccountBookEntityId())){
                        balance = format.format(startVo.getBalance());
                    }
                }
                journalVo = vo;
                direction = vo.getBalanceDirection();
                debit = format.format(vo.getDebit());
                credit = format.format(vo.getCredit());
                Long periodYearNum = vo.getPeriodYearNum();
                List<Long> periodYearNumList = itemJournalQueryDto.getPeriodYearNum();
                if( null != periodYearNumList && periodYearNumList.size() > 0 ){
                    if(periodYearNumList.get(0) <= periodYearNum && periodYearNum <= periodYearNumList.get(1)){
                        if(null != startOpeningInfo && startOpeningInfo.size() > 0){
                            balance = format.format(new BigDecimal(balance));
                        }else {
                            balance = format.format(new BigDecimal(vo.getBalance()));
                        }
                    }
                }else{
                    //发生额 借方：加借减贷  贷方：加贷减借
                    if(Constant.BalanceDirection.DEBIT == direction){
                        balance = format.format(new BigDecimal(balance).add(new BigDecimal(debit)).subtract(new BigDecimal(credit)));
                    }
                    if(Constant.BalanceDirection.CREDIT == direction){
                        balance = format.format(new BigDecimal(balance).add(new BigDecimal(credit)).subtract(new BigDecimal(debit)));
                    }
                }
            }
          if(null != journalVo){
              journalVo.setDebit(0);
              journalVo.setCredit(0);
              journalVo.setBalance(new BigDecimal(balance).doubleValue());
              voList.add(journalVo);
              balance = format.format(0);
              debit = format.format(0);
              credit = format.format(0);
          }
        }
        return voList;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/12 15:45
     * @Param [itemJournalQueryDto, bookId, entityId, id]
     * @return void
     * @Description 总账账簿 核算主体  会计科目 查询条件插入
     */
    private void setQueryItem(ItemJournalQueryDto itemJournalQueryDto ,Long bookId, Long entityId) {
        List<Long> accountBookId = new LinkedList<>();
        List<Long> accountBookEntityId = new LinkedList<>();
        accountBookId.add(bookId);
        accountBookEntityId.add(entityId);
        itemJournalQueryDto.setAccountBookIds(accountBookId);
        itemJournalQueryDto.setAccountBookEntityIds(accountBookEntityId);
    }

    /**
     * @Author ZhuHC
     * @Date  16:29
     * @Param [returnList, itemJournalQueryDto]
     * @return void
     * @Description  删除 条件 过滤的数据
     */
    private void deleteInfoByStatus(List<GeneralReturnItemJournalVo> returnList,ItemJournalQueryDto itemJournalQueryDto) {
        if(!FastUtils.checkNullOrEmpty(returnList)){
            if(null != itemJournalQueryDto.getShowCondition()){
                GeneralReturnItemJournalVo generalReturnItemJournalVo;
                Byte condition = itemJournalQueryDto.getShowCondition();
                //0：本期无发生 1;余额为零  2； 本期无发生，且余额为零
                for(int i = returnList.size() - 1; i >= 0; i--){
                    generalReturnItemJournalVo = returnList.get(i);
                    if(Constant.SortNum.DETAIL.equals(generalReturnItemJournalVo.getSortNum())){
                        if(LedgerConstant.ReportShowCondition.HAPPEN_NO.equals(condition)){
                            if(Constant.Number.ZEROD.equals(generalReturnItemJournalVo.getCredit())
                                    && Constant.Number.ZEROD.equals(generalReturnItemJournalVo.getDebit())
                                    && Constant.Number.ZEROD.equals(generalReturnItemJournalVo.getBalance())){
                                returnList.remove(i);
                            }
                        }else if (LedgerConstant.ReportShowCondition.BALANCE_NO.equals(condition)){
                            if(Constant.Number.ZEROD.equals(generalReturnItemJournalVo.getBalance())){
                                returnList.remove(i);
                            }
                        }else if (LedgerConstant.ReportShowCondition.HAPPEN_BALANCE_NO.equals(condition)){
                            if(Constant.Number.ZEROD.equals(generalReturnItemJournalVo.getCredit()) && Constant.Number.ZEROD.equals(generalReturnItemJournalVo.getDebit())){
                                returnList.remove(i);
                            }
                        }
                    }
                }
            }
        }
    }
    /**
     * @Author ZhuHC
     * @Date  2019/8/8 16:29
     * @Param [totalList]
     * @return java.util.List<com.njwd.entity.ledger.vo.GeneralReturnItemJournalVo>
     * @Description 每日 月 期间 年 数据计算
     */
    private List<GeneralReturnItemJournalVo> countBalance(List<GeneralReturnItemJournalVo> totalList,ItemJournalQueryDto itemJournalQueryDto) {
        List<GeneralReturnItemJournalVo> newTotalList = new ArrayList<>();
        if(!FastUtils.checkNullOrEmpty(totalList)){
            DecimalFormat format = new DecimalFormat("##0.00");
            //余额
            String balance = format.format(0);
            //今日 借
            String todayDebit = format.format(0);
            //今日 贷
            String todayCredit = format.format(0);
            // 会计期间查询类型 periodOperator 0 制单日期 1 会计区间
            if(LedgerConstant.PeriodOperator.PERIOD_YEAR_AND_NUM.equals(itemJournalQueryDto.getPeriodOperator())){
                GeneralReturnItemJournalVo info;
                int direction;
                String debit;
                String credit;
                GeneralReturnItemJournalVo todayInfo;
                GeneralReturnItemJournalVo nextInfo;
                String periodYearDebit = format.format(0);
                String periodYearCrebit = format.format(0);
                //借方
                String tempDebit =  format.format(0);
                //贷方
                String tempCredit =  format.format(0);
                for(int i = 0; i< totalList.size();i++){
                    info = totalList.get(i);
                    direction = info.getBalanceDirection();
                    //余额方向  0：借、1：贷 2；平
                    if(Constant.SortNum.OPENING.equals(info.getSortNum())){
                        balance = format.format(info.getBalance());
                        debit = format.format(info.getDebit());
                        credit = format.format(info.getCredit());
                        tempDebit = debit;
                        tempCredit = credit;
                        addTotalLost(balance, newTotalList, info);
                        info.setDebit(0);
                        info.setCredit(0);
                    }else if(Constant.SortNum.DETAIL.equals(info.getSortNum())){
                        debit = format.format(info.getDebit());
                        credit = format.format(info.getCredit());
                        if(Constant.BalanceDirection.DEBIT == direction){
                            balance = format.format(new BigDecimal(balance).add(new BigDecimal(debit)).subtract(new BigDecimal(credit)));
                        }
                        if(Constant.BalanceDirection.CREDIT == direction){
                            balance = format.format(new BigDecimal(balance).add(new BigDecimal(credit)).subtract(new BigDecimal(debit)));
                        }
                        todayDebit = format.format(new BigDecimal(todayDebit).add(new BigDecimal(debit)));
                        todayCredit = format.format(new BigDecimal(todayCredit).add(new BigDecimal(credit)));
                        addTotalLost(balance, newTotalList, info);
                        nextInfo = totalList.get(i+1);
                        if(!Constant.SortNum.DETAIL.equals(nextInfo.getSortNum()) || !nextInfo.getVoucherDate().equals(info.getVoucherDate())){
                            //生成 今日累计 数据
                            todayInfo = getTodayGeneralReturnItemJournalVo(new BigDecimal(balance).doubleValue(), new BigDecimal(todayDebit).doubleValue(), new BigDecimal(todayCredit).doubleValue(), info);
                            setBalanceDirectionIfZero(new BigDecimal(balance).doubleValue(), todayInfo);
                            newTotalList.add(todayInfo);
                            todayDebit = format.format(0);
                            todayCredit = format.format(0);
                        }
                    }else if(Constant.SortNum.PERIOD_NUM.equals(info.getSortNum())){
                        periodYearDebit = format.format(new BigDecimal(periodYearDebit).add(new BigDecimal(info.getDebit())));
                        periodYearCrebit = format.format(new BigDecimal(periodYearCrebit).add(new BigDecimal(info.getCredit())));
                        addTotalLost(balance, newTotalList, info);
                    }else if(Constant.SortNum.PERIOD_YEAR.equals(info.getSortNum())){
                        periodYearDebit = format.format(new BigDecimal(periodYearDebit).add(new BigDecimal(tempDebit)));
                        periodYearCrebit = format.format(new BigDecimal(periodYearCrebit).add(new BigDecimal(tempCredit)));
                        tempDebit = format.format(0);
                        tempCredit = format.format(0);
                        info.setDebit(new BigDecimal(periodYearDebit).doubleValue());
                        info.setCredit(new BigDecimal(periodYearCrebit).doubleValue());
                        if(i+1 < totalList.size()){
                            GeneralReturnItemJournalVo finalTotal = totalList.get(i+1);
                            boolean flag = info.getPeriodYear() != finalTotal.getPeriodYear() || !info.getAccountBookId().equals(finalTotal.getAccountBookId())
                                    || !info.getAccountBookEntityId().equals(finalTotal.getAccountBookEntityId());
                            if(flag){
                                periodYearDebit = format.format(0);
                                periodYearCrebit = format.format(0);
                                tempDebit = format.format(0);
                                tempCredit = format.format(0);
                            }
                        }
                        addTotalLost(balance, newTotalList, info);
                    }else {
                        continue;
                    }
                }
            }else {
                getBalanceInfoByVoucher(itemJournalQueryDto, totalList, format, balance, todayDebit, todayCredit, newTotalList);
            }
        }
        return newTotalList;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/29 14:50
     * @Param [startDate, info]
     * @return com.njwd.entity.ledger.AccountBookPeriod
     * @Description 账簿会计区间 查询条件设置
     */
    private AccountBookPeriodVo getAccountBookPeriodByDate(Date startDate, GeneralReturnItemJournalVo info) {
        AccountBookPeriodDto param = new AccountBookPeriodDto();
        param.setAccountBookId(info.getAccountBookId());
        param.setSystemSign(Constant.SystemSignValue.LEDGER);
        param.setStatus(Constant.Status.ON);
        param.setVoucherDate(startDate);
        return accountBookPeriodService.findPeriodByAccBookIdAndSystemSign(param);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/12 15:47
     * @Param [itemJournalQueryDto, totalList, format, balance, todayDebit, todayCredit, newTotalList]
     * @return void
     * @Description 制单日期时  余额等数据计算
     */
    private void getBalanceInfoByVoucher(ItemJournalQueryDto itemJournalQueryDto, List<GeneralReturnItemJournalVo> totalList, DecimalFormat format, String balance, String todayDebit, String todayCredit, List<GeneralReturnItemJournalVo> newTotalList) {
        if(!FastUtils.checkNullOrEmpty(totalList)){
            Date startDate = itemJournalQueryDto.getVoucherDates().get(0);
            String periodNumDebit = format.format(0);
            String periodNumCrebit = format.format(0);
            String periodYearDebit = format.format(0);
            String periodYearCrebit = format.format(0);
            GeneralReturnItemJournalVo info;
            int direction;
            AccountBookPeriodVo period;
            String debit;
            String credit;
            GeneralReturnItemJournalVo nextInfo ;
            GeneralReturnItemJournalVo todayInfo ;
            //借方
            String tempDebit =  format.format(0);
            //贷方
            String tempCredit =  format.format(0);
            for(int i = 0; i< totalList.size();i++){
                info = totalList.get(i);
                direction = info.getBalanceDirection();
                //余额方向  0：借、1：贷 2；平
                if(Constant.SortNum.OPENING.equals(info.getSortNum())){
                    //根据 查询条件的 开始制单日期  获得 开始时间的会计区间
                    period = getAccountBookPeriodByDate(startDate, info);
                    balance = format.format(info.getBalance());
                    if(info.getPeriodYearNum().equals(Long.valueOf(period.getPeriodYearNum()))){
                        balance = getOpeningBalance(itemJournalQueryDto, format, startDate, info);
                    }
                    debit = format.format(info.getDebit());
                    credit = format.format(info.getCredit());
                    tempDebit = debit;
                    tempCredit = credit;
                    addTotalLost(balance, newTotalList, info);
                    info.setDebit(0);
                    info.setCredit(0);
                }else if(Constant.SortNum.DETAIL.equals(info.getSortNum())){
                    debit = format.format(info.getDebit());
                    credit = format.format(info.getCredit());
                    periodNumDebit = format.format(new BigDecimal(periodNumDebit).add(new BigDecimal(debit)));
                    periodNumCrebit = format.format(new BigDecimal(periodNumCrebit).add(new BigDecimal(credit)));
                    periodYearDebit = format.format(new BigDecimal(periodYearDebit).add(new BigDecimal(debit)));
                    periodYearCrebit = format.format(new BigDecimal(periodYearCrebit).add(new BigDecimal(credit)));
                    if(Constant.BalanceDirection.DEBIT == direction){
                        balance = format.format(new BigDecimal(balance).add(new BigDecimal(debit)).subtract(new BigDecimal(credit)));
                    }
                    if(Constant.BalanceDirection.CREDIT == direction){
                        balance = format.format(new BigDecimal(balance).add(new BigDecimal(credit)).subtract(new BigDecimal(debit)));
                    }
                    todayDebit = format.format(new BigDecimal(todayDebit).add(new BigDecimal(debit)));
                    todayCredit = format.format(new BigDecimal(todayCredit).add(new BigDecimal(credit)));
                    addTotalLost(balance, newTotalList, info);
                    nextInfo = totalList.get(i+1);
                    if(!Constant.SortNum.DETAIL.equals(nextInfo.getSortNum()) || !nextInfo.getVoucherDate().equals(info.getVoucherDate())){
                        //生成 今日累计 数据
                        todayInfo = getTodayGeneralReturnItemJournalVo(new BigDecimal(balance).doubleValue(), new BigDecimal(todayDebit).doubleValue(), new BigDecimal(todayCredit).doubleValue(), info);
                        setBalanceDirectionIfZero(new BigDecimal(balance).doubleValue(), todayInfo);
                        newTotalList.add(todayInfo);
                        todayDebit = format.format(0);
                        todayCredit = format.format(0);
                    }
                }else if(Constant.SortNum.PERIOD_NUM.equals(info.getSortNum())){
                    info.setDebit(new BigDecimal(periodNumDebit).doubleValue());
                    info.setCredit(new BigDecimal(periodNumCrebit).doubleValue());
                    periodNumDebit = format.format(0);
                    periodNumCrebit = format.format(0);
                    addTotalLost(balance, newTotalList, info);
                }else if(Constant.SortNum.PERIOD_YEAR.equals(info.getSortNum())){
                    periodYearDebit = format.format(new BigDecimal(periodYearDebit).add(new BigDecimal(tempDebit)));
                    periodYearCrebit = format.format(new BigDecimal(periodYearCrebit).add(new BigDecimal(tempCredit)));
                    tempDebit = format.format(0);
                    tempCredit = format.format(0);
                    info.setDebit(new BigDecimal(periodYearDebit).doubleValue());
                    info.setCredit(new BigDecimal(periodYearCrebit).doubleValue());
                    if(i+1 < totalList.size()){
                        GeneralReturnItemJournalVo finalTotal = totalList.get(i+1);
                        boolean flag = info.getPeriodYear() != finalTotal.getPeriodYear() || !info.getAccountBookId().equals(finalTotal.getAccountBookId())
                                       || !info.getAccountBookEntityId().equals(finalTotal.getAccountBookEntityId())
                                       || !info.getSubjectId().equals(finalTotal.getSubjectId());
                        if(flag){
                            periodYearDebit = format.format(0);
                            periodYearCrebit = format.format(0);
                            tempDebit = format.format(0);
                            tempCredit = format.format(0);
                        }
                    }
                    addTotalLost(balance, newTotalList, info);
                }else {
                    continue;
                }
            }
        }
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/29 14:27
     * @Param [balance, newTotalList, info]
     * @return void
     * @Description 数据 加入最终结果list中
     */
    private void addTotalLost(String balance, List<GeneralReturnItemJournalVo> newTotalList, GeneralReturnItemJournalVo info) {
        info.setBalance(new BigDecimal(balance).doubleValue());
        setBalanceDirectionIfZero(new BigDecimal(balance).doubleValue(), info);
        newTotalList.add(info);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/12 15:49
     * @Param [itemJournalQueryDto, format, startDate, info, direction]
     * @return java.lang.String
     * @Description  制单日期 期初 余额
     */
    private String getOpeningBalance(ItemJournalQueryDto itemJournalQueryDto, DecimalFormat format, Date startDate, GeneralReturnItemJournalVo info) {
        int direction = info.getBalanceDirection();
        String balance = format.format(info.getBalance());
        ItemJournalQueryDto dto = new ItemJournalQueryDto();
        dto.setSubjectIdList(itemJournalQueryDto.getSubjectIdList());
        setQueryItem(dto,info.getAccountBookId(),info.getAccountBookEntityId());
        dto.setIsIncludeUnbooked(itemJournalQueryDto.getIsIncludeUnbooked());
        dto.setIsIncludeProfitAndLoss(itemJournalQueryDto.getIsIncludeProfitAndLoss());
        dto.setShowCondition(itemJournalQueryDto.getShowCondition());
        dto.setPeriodOperator(itemJournalQueryDto.getPeriodOperator());
        List<Date> voucherDates = new LinkedList<>();
        AccountBookPeriodDto param = new AccountBookPeriodDto();
        param.setAccountBookId(info.getAccountBookId());
        param.setPeriodYear(info.getPeriodYear());
        param.setPeriodNum(info.getPeriodNum());
        param.setStatus(Constant.Status.ON);
        param.setSystemSign(Constant.SystemSignValue.LEDGER);
        AccountBookPeriodVo period = accountBookPeriodService.findPeriodByAccBookIdAndSystemSign(param);
        voucherDates.add(period.getStartDate());
        voucherDates.add(startDate);
        dto.setVoucherDates(voucherDates);
        GeneralReturnItemJournalVo openingLossInfo = itemJournalMapper.getOpeningLossInfo(dto);
        if(null != openingLossInfo){
            String debit = format.format(openingLossInfo.getDebit());
            String credit = format.format(openingLossInfo.getCredit());
            if(Constant.BalanceDirection.DEBIT == direction){
                balance = format.format(new BigDecimal(balance).add(new BigDecimal(debit)).subtract(new BigDecimal(credit)));
            }
            if(Constant.BalanceDirection.CREDIT == direction){
                balance = format.format(new BigDecimal(balance).add(new BigDecimal(credit)).subtract(new BigDecimal(debit)));
            }
        }
        return balance;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/8 16:30
     * @Param [balance, todayDebit, todayCredit, info]
     * @return com.njwd.entity.ledger.vo.GeneralReturnItemJournalVo
     * @Description 本日数据 生成
     */
    private GeneralReturnItemJournalVo getTodayGeneralReturnItemJournalVo(Double balance, Double todayDebit, Double todayCredit, GeneralReturnItemJournalVo info) {
        GeneralReturnItemJournalVo todayInfo = new GeneralReturnItemJournalVo();
        todayInfo.setSubjectCode(info.getSubjectCode());
        todayInfo.setAccountBookEntityName(info.getAccountBookEntityName());
        todayInfo.setAccountBookEntityId(info.getAccountBookEntityId());
        todayInfo.setAccountBookId(info.getAccountBookId());
        todayInfo.setAccountBookName(info.getAccountBookName());
        todayInfo.setPeriodYear(info.getPeriodYear());
        todayInfo.setPeriodNum(info.getPeriodNum());
        todayInfo.setAbstractContent("本日合计");
        todayInfo.setCredit(todayCredit);
        todayInfo.setDebit(todayDebit);
        todayInfo.setBalance(balance);
        todayInfo.setSubjectCode(info.getSubjectCode());
        todayInfo.setSubjectId(info.getSubjectId());
        todayInfo.setSubjectName(info.getSubjectName());
        todayInfo.setSubjectFullName(info.getSubjectFullName());
        todayInfo.setBalanceDirection(info.getBalanceDirection());
        return todayInfo;
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/8 16:30
     * @Param [balance, list]
     * @return void
     * @Description  设置余额方向
     */
    private void setBalanceDirectionIfZero(Double balance, GeneralReturnItemJournalVo vo) {
        if(Constant.Number.ZEROD.equals(balance)){
            vo.setBalanceDirection(Constant.BalanceDirectionType.FLAT);
        }
    }
}
