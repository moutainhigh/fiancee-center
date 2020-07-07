package com.njwd.ledger.service.impl;

import com.alibaba.excel.util.CollectionUtils;
import com.njwd.common.Constant;
import com.njwd.common.LedgerConstant;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.CompanyDto;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.basedata.vo.AccountBookVo;
import com.njwd.entity.basedata.vo.CompanyVo;
import com.njwd.entity.ledger.BalanceSubject;
import com.njwd.entity.ledger.FinancialReport;
import com.njwd.entity.ledger.dto.*;
import com.njwd.entity.ledger.vo.*;
import com.njwd.entity.platform.FinancialReportItemFormula;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.dto.CashFlowItemDto;
import com.njwd.entity.platform.dto.FinancialReportItemSetDto;
import com.njwd.entity.platform.vo.*;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.ledger.cloudclient.*;
import com.njwd.ledger.service.AccountBookPeriodService;
import com.njwd.ledger.service.BalanceCashFlowService;
import com.njwd.ledger.service.BalanceSubjectService;
import com.njwd.ledger.service.FinancialReportService;
import com.njwd.ledger.utils.ExportUtils;
import com.njwd.utils.FastUtils;
import com.njwd.utils.StringUtil;
import com.njwd.utils.UserUtils;
import lombok.extern.java.Log;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @author LuoY
 * @description: 财务总账-财务报告-利润表,现金流量表
 * @date 2019/7/25 16:15
 */
@Service
@Log
public class FinancialReportServiceImpl implements FinancialReportService {

    @Resource
    private AccountBookFeignClient accountBookFeignClient;

    @Resource
    private AccountSubjectFeignClient accountSubjectFeignClient;

    @Resource
    private FinancialReportItemSetFeignClient financialReportItemSetFeignClient;

    @Resource
    private CashFlowReportClient cashFlowReportClient;

    @Resource
    private CompanyFeignClient companyFeignClient;

    @Resource
    private BalanceSubjectService balanceSubjectService;

    @Resource
    private BalanceCashFlowService balanceCashFlowService;

    @Resource
    private AccountBookPeriodService accountBookPeriodService;

    /**
     * @param incomeStatementsTableDto
     * @return java.util.List<com.njwd.ledger.entity.vo.IncomeStatementsAccountEntityVo>
     * @description: 根据条件查询指定利润表
     * @author LuoY
     * @date 2019/7/26 13:52
     */
    @Override
    public List<IncomeStatementsAccountBookVo> findCurrentIssueProfit(@NotNull IncomeStatementsReportDto incomeStatementsTableDto) {
        //校验核算账簿信息
        if (FastUtils.checkNullOrEmpty(incomeStatementsTableDto.getIncomeStatementsAccountBookVoList())) {
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //获取查询的核算账簿信息
        List<IncomeStatementsAccountBookVo> incomeStatementsAccountBookVos = incomeStatementsTableDto.getIncomeStatementsAccountBookVoList();
        //初始化占收入比计算项
        Map<String, Boolean> incomeRation = initializeIncomeRation(incomeStatementsTableDto);
        //循环核算账簿
        for (IncomeStatementsAccountBookVo incomeStatementsAccountBookVo : incomeStatementsAccountBookVos) {
            //初始化核算账簿币种信息
            initializeAccountBookCurrencyName(incomeStatementsAccountBookVo);
            //是否计算合计
            Boolean isCount = false;
            //获取上一年最大期间数
            Byte lastYearMaxPeriodNum = initLastPeriodNum(incomeStatementsTableDto, incomeStatementsAccountBookVo);
            //根据当前核算账簿查询配置的财务报告
            List<FinancialReportItemSetVo> financialReportItemSetVos = findFinancialReport(incomeStatementsAccountBookVo.getAccountBookId(), LedgerConstant.FinancialReportName.PROFITREPORT);
            //只有核算账簿没有核算主体时候,初始化一个同名核算账簿的核算主体
            initAccountBookEntityInfo(incomeStatementsAccountBookVo);
            //初始化核算主体财务报告
            financialReportProfit(incomeStatementsAccountBookVo.getIncomeStatementsAccountEntityVos(), financialReportItemSetVos);
            //获取当前核算账簿的科目信息
            List<AccountSubjectVo> accountSubjectVoList = getAccountSubjectVo(incomeStatementsAccountBookVo);
            //初始化合计本期
            Map<String, BigDecimal> countCurrentMoney = new HashMap<>();
            //初始化合计本年累计
            Map<String, BigDecimal> countYearCulmulativeMoney = new HashMap<>();
            //初始化合计同比
            Map<String, BigDecimal> countYearCompareMoney = new HashMap<>();
            //初始化合计环比
            Map<String, BigDecimal> countMonthCompareMoney = new HashMap<>();
            //如果核算主体数量大于1，则需要计算合计
            if (incomeStatementsAccountBookVo.getIncomeStatementsAccountEntityVos().size() > Constant.Number.ONE) {
                isCount = true;
            }
            //循环核算主体
            for (IncomeStatementsAccountEntityVo incomeStatementsAccountEntityVo : incomeStatementsAccountBookVo.getIncomeStatementsAccountEntityVos()) {
                //计算各核算主体(除合计)
                if (!LedgerConstant.FinancialString.ACCOUNTBOOKID.equals(incomeStatementsAccountEntityVo.getAccountBookEntityId())) {
                    //设置查询当前核算主体科目余额的条件
                    BalanceSubjectQueryDto balanceSubjectQueryDto = new BalanceSubjectQueryDto();
                    balanceSubjectQueryDto.setAccountBookId(incomeStatementsAccountBookVo.getAccountBookId());
                    balanceSubjectQueryDto.setAccountBookEntityId(incomeStatementsAccountEntityVo.getAccountBookEntityId());
                    balanceSubjectQueryDto.setPeriodYear(incomeStatementsTableDto.getPeriodYear());
                    balanceSubjectQueryDto.setPeriodNum(incomeStatementsTableDto.getPeriodNum());
                    //获取当前核算主体,当前期间的科目余额信息
                    List<BalanceSubjectVo> currentSubjectList = findBalance(balanceSubjectQueryDto);
                    //整合当前科余额信息
                    List<IncomeStatementsVo> currentIncomeStatementsVoList = subjectDataHandle(currentSubjectList, accountSubjectVoList);
                    //计算本期财务报告MAP
                    Map<String, BigDecimal> currentFinancialReportMap = financialProfitReportMap(LedgerConstant.IncomeRationType.CURRENTISSUE,
                            incomeStatementsTableDto.getPosting(), financialReportItemSetVos, currentIncomeStatementsVoList);
                    //计算本年累计财务报告MAP
                    Map<String, BigDecimal> yearCumulativeFinancialReportMap = new HashMap<>();
                    //计算同比期间财务报告MAP
                    Map<String, BigDecimal> yearCompareFinancialReportMap = new HashMap<>();
                    //计算环比期间财务报告MAP
                    Map<String, BigDecimal> monthCompareFinancialReportMap = new HashMap<>();
                    //计算各个金额map
                    balanceSubjectQueryDto.setPeriodNum(incomeStatementsTableDto.getPeriodNum());
                    initializeMoneyFinancialMap(incomeStatementsTableDto, balanceSubjectQueryDto, yearCumulativeFinancialReportMap, financialReportItemSetVos, yearCompareFinancialReportMap,
                            accountSubjectVoList, lastYearMaxPeriodNum, monthCompareFinancialReportMap, incomeStatementsAccountEntityVo, currentIncomeStatementsVoList, incomeStatementsAccountBookVo, currentFinancialReportMap);
                    //计算合计Map
                    if (isCount) {
                        //本期合计
                        countCurrentMoney = calculationCountReportMap(currentFinancialReportMap, countCurrentMoney);
                        if (Constant.Is.YES.equals(incomeStatementsTableDto.getYearCumulative())) {
                            //本年累计合计
                            countYearCulmulativeMoney = calculationCountReportMap(yearCumulativeFinancialReportMap, countYearCulmulativeMoney);
                        }
                        if (Constant.Is.YES.equals(incomeStatementsTableDto.getYearOnYear())) {
                            //同比合计
                            countYearCompareMoney = calculationCountReportMap(yearCompareFinancialReportMap, countYearCompareMoney);
                        }
                        if (Constant.Is.YES.equals(incomeStatementsTableDto.getMonthOnMonth())) {
                            //环比合计
                            countMonthCompareMoney = calculationCountReportMap(monthCompareFinancialReportMap, countMonthCompareMoney);
                        }
                    }
                    //循环核算主体利润表
                    for (IncomeStatementsTableVo incomeStatementsTableVo : incomeStatementsAccountEntityVo.getIncomeStatementsTablesListVos()) {
                        //金额计算
                        calculationMoney(incomeStatementsTableVo, currentFinancialReportMap, yearCumulativeFinancialReportMap,
                                yearCompareFinancialReportMap, monthCompareFinancialReportMap, incomeStatementsTableDto);
                    }
                    //计算占收入比
                    if (Constant.Is.YES.equals(incomeStatementsTableDto.getIncomeRatio())) {
                        currentIncomeRationCal(incomeRation, incomeStatementsAccountEntityVo.getIncomeStatementsTablesListVos());
                    }
                }
            }
            //计算合计
            if (isCount) {
                calculationCount(incomeStatementsAccountBookVo, incomeStatementsTableDto, countCurrentMoney, countYearCulmulativeMoney,
                        countYearCompareMoney, countMonthCompareMoney, incomeRation);
            }
        }
        //
        if (Constant.Is.YES.equals(incomeStatementsTableDto.getSubjectDetail())) {
            incomeStatementsAccountBookVos = dataSortHandle(incomeStatementsAccountBookVos);
        }
        return incomeStatementsAccountBookVos;
    }

    /**
     * @param cashFlowReportDto
     * @return java.util.List<com.njwd.entity.ledger.vo.IncomeStatementsAccountBookVo>
     * @description: 根据指定条件查询现金流量表
     * @author LuoY
     * @date 2019/8/9 11:35
     */
    @Override
    public List<IncomeStatementsAccountBookVo> findCashFlowReport(@NotNull CashFlowReportDto cashFlowReportDto) {
        //校验核算账簿信息
        if (FastUtils.checkNullOrEmpty(cashFlowReportDto.getIncomeStatementsAccountBookVoList())) {
            //没有核算账簿信息直接抛出异常
            throw new ServiceException(ResultCode.PARAMS_NOT);
        }
        //初始化核算账簿主体信息
        List<IncomeStatementsAccountBookVo> incomeStatementsAccountBookVoList = cashFlowReportDto.getIncomeStatementsAccountBookVoList();
        //查询现金流量项目信息
        CashFlowItemDto cashFlowItemDto = new CashFlowItemDto();
        cashFlowItemDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        List<CashFlowItemVo> cashFlowItemVo = cashFlowReportClient.findCashFlowItemForReport(cashFlowItemDto).getData();
        //将科目明细项转化为MAP<code,direction> 方便获取报告code的方向
        Map<String, Byte> cashFlowItemMap = dataToCashFlowItemMap(cashFlowItemVo);
        //循环核算账簿
        for (IncomeStatementsAccountBookVo incomeStatementsAccountBookVo : incomeStatementsAccountBookVoList) {
            //初始化核算账簿的币种
            initializeAccountBookCurrencyName(incomeStatementsAccountBookVo);
            //是否需要计算合计项
            Boolean isCount = false;
            //定义合计项财务报告本期金额MAP
            Map<String, BigDecimal> countCurrentFinancialReportMap = new HashMap<>();
            //定义合计项财务报告本年累计金额MAP
            Map<String, BigDecimal> countYearFinancialReportMap = new HashMap<>();
            //初始化核算主体
            initAccountBookEntityInfo(incomeStatementsAccountBookVo);
            //根据当前核算账簿查询配置的财务报告
            List<FinancialReportItemSetVo> financialReportItemSetVos = findFinancialReport(incomeStatementsAccountBookVo.getAccountBookId(), LedgerConstant.FinancialReportName.CASHFLOWREPORT);
            //根据财务报告获取对应公式的方向

            //获取现金流量特殊项
            FinancialReportItemSetVo financialReportItemSetVo = cashFlowSpecial(financialReportItemSetVos);
            //初始化核算主体财务报告
            financialReportCashFlow(incomeStatementsAccountBookVo.getIncomeStatementsAccountEntityVos(), financialReportItemSetVos);
            //判断是否需要计算合计
            if (incomeStatementsAccountBookVo.getIncomeStatementsAccountEntityVos().size() > Constant.Number.ONE) {
                isCount = true;
            }
            //循环核算主体
            for (IncomeStatementsAccountEntityVo incomeStatementsAccountEntityVo : incomeStatementsAccountBookVo.getIncomeStatementsAccountEntityVos()) {
                //合计项的accountBookEntityId = -1
                if (!LedgerConstant.FinancialString.ACCOUNTBOOKID.equals(incomeStatementsAccountEntityVo.getAccountBookEntityId())) {
                    //设置查询核算主体现金流量余额的条件
                    BalanceCashFlowDto balanceCashFlowDto = new BalanceCashFlowDto();
                    balanceCashFlowDto.setAccountBookId(incomeStatementsAccountBookVo.getAccountBookId());
                    balanceCashFlowDto.setAccountBookEntityId(incomeStatementsAccountEntityVo.getAccountBookEntityId());
                    balanceCashFlowDto.setPeriodYear(cashFlowReportDto.getPeriodYear());
                    balanceCashFlowDto.setPeriodNum(cashFlowReportDto.getPeriodNum());
                    //根据核算主体查询当前期间现金流量余额
                    List<BalanceCashFlowVo> balanceCashFlowVos = calculationCashFlowVo(balanceCashFlowDto);

                    //整合现金流量项目余额信息
                    List<CashFlowReportVo> cashFlowReportVos = cashFlowDataHandle(balanceCashFlowVos, cashFlowItemVo,
                            incomeStatementsAccountBookVo, incomeStatementsAccountEntityVo, cashFlowReportDto, financialReportItemSetVo);
                    //计算本期现金流量财务报告MAP
                    Map<String, BigDecimal> currentCashFlowFinancialReportMap = financialCashFlowReportMap(LedgerConstant.IncomeRationType.CURRENTISSUE,
                            cashFlowReportDto.getPosting(), financialReportItemSetVos, cashFlowReportVos, cashFlowItemMap);
                    //计算本年累计现金流量财务报告MAP
                    Map<String, BigDecimal> yearCumulativeFinancialReportMap = financialCashFlowReportMap(LedgerConstant.IncomeRationType.YEARCUMULATIVE,
                            cashFlowReportDto.getPosting(), financialReportItemSetVos, cashFlowReportVos, cashFlowItemMap);
                    //计算合计项
                    if (isCount) {
                        countCurrentFinancialReportMap = calculationCountReportMap(currentCashFlowFinancialReportMap, countCurrentFinancialReportMap);
                        countYearFinancialReportMap = calculationCountReportMap(yearCumulativeFinancialReportMap, countYearFinancialReportMap);
                    }
                    //循环现金流量表
                    for (CashFlowReportTabletVo cashFlowReportTabletVo : incomeStatementsAccountEntityVo.getCashFlowReportTabletVos()) {
                        //计算本期金额
                        calculationCashFlow(LedgerConstant.IncomeRationType.CURRENTISSUE, cashFlowReportTabletVo, currentCashFlowFinancialReportMap);

                        //计算本年累计
                        calculationCashFlow(LedgerConstant.IncomeRationType.YEARCUMULATIVE, cashFlowReportTabletVo, yearCumulativeFinancialReportMap);
                    }
                }
            }
            //计算合计
            if (isCount) {
                for (IncomeStatementsAccountEntityVo incomeStatementsAccountEntityVo : incomeStatementsAccountBookVo.getIncomeStatementsAccountEntityVos()) {
                    //计算合计
                    if (LedgerConstant.FinancialString.ACCOUNTBOOKID.equals(incomeStatementsAccountEntityVo.getAccountBookEntityId())) {
                        //循环现金流量表
                        for (CashFlowReportTabletVo cashFlowReportTabletVo : incomeStatementsAccountEntityVo.getCashFlowReportTabletVos()) {
                            //计算本期金额
                            calculationCashFlow(LedgerConstant.IncomeRationType.CURRENTISSUE, cashFlowReportTabletVo, countCurrentFinancialReportMap);
                            //计算本年累计
                            calculationCashFlow(LedgerConstant.IncomeRationType.YEARCUMULATIVE, cashFlowReportTabletVo, countYearFinancialReportMap);
                        }
                    }
                }
            }
        }
        return incomeStatementsAccountBookVoList;
    }

    /**
     * @param platformFinancialReportItemSetVos 财务报告
     * @param balanceSubject                    科目余额信息
     * @return java.util.Map<java.lang.String, java.math.BigDecimal>  map<财务报告行号,财务报告计算金额>
     * @description: 财务报告计算公式
     * @author LuoY
     * @date 2019/8/6 10:42
     */
    @Override
    public Map<String, BigDecimal> financialReportCalculationResult(@NotNull List<FinancialReportItemSetVo> platformFinancialReportItemSetVos, @NotNull Map<String, String> balanceSubject, BalanceDto balanceDto, Map<String, Byte> cashFlowItemMap) {
        Map<String, BigDecimal> financialReportResult = new HashMap<>();
        //循环财务报告
        for (FinancialReportItemSetVo platformFinancialReportItemSetVo : platformFinancialReportItemSetVos) {
            BigDecimal result = BigDecimal.ZERO;
            if (!FastUtils.checkNullOrEmpty(platformFinancialReportItemSetVo.getFinancialReportItemFormulaVoList())) {
                //如果报告公司列表不为空,循环计算公式
                for (FinancialReportItemFormulaVo financialReportItemFormulaVo : platformFinancialReportItemSetVo.getFinancialReportItemFormulaVoList()) {
                    if (!StringUtil.isBlank(financialReportItemFormulaVo.getId())) {
                        if (LedgerConstant.FormulaType.SUBECTORITEM.equals(financialReportItemFormulaVo.getFormulaType())) {
                            //按code计算,从科目余额取
                            if (LedgerConstant.Operator.ADD.equals(financialReportItemFormulaVo.getOperator())) {
                                //加(现金流量一级code没有方向,默认流入)
                                result = result.add(FastUtils.Null2Zero(getSubjectMoney(balanceSubject, financialReportItemFormulaVo.getFormulaItemCode(),
                                        cashFlowItemMap != null ? cashFlowItemMap.get(financialReportItemFormulaVo.getFormulaItemCode()) : financialReportItemFormulaVo.getFormulaItemDirection())));
                            } else if (LedgerConstant.Operator.SUBTRACT.equals(financialReportItemFormulaVo.getOperator())) {
                                //减(现金流量一级code没有方向,默认流入)
                                result = result.subtract(FastUtils.Null2Zero(getSubjectMoney(balanceSubject, financialReportItemFormulaVo.getFormulaItemCode(),
                                        cashFlowItemMap != null ? cashFlowItemMap.get(financialReportItemFormulaVo.getFormulaItemCode()) : financialReportItemFormulaVo.getFormulaItemDirection())));
                            }
                        } else if (LedgerConstant.FormulaType.ITEMLINE.equals(financialReportItemFormulaVo.getFormulaType())) {
                            //按行计算,从计算结果集取
                            if (LedgerConstant.Operator.ADD.equals(financialReportItemFormulaVo.getOperator())) {
                                //加
                                result = result.add(FastUtils.Null2Zero(financialReportResult.get(financialReportItemFormulaVo.getFormulaItemCode())));
                            } else if (LedgerConstant.Operator.SUBTRACT.equals(financialReportItemFormulaVo.getOperator())) {
                                //减
                                result = result.subtract(FastUtils.Null2Zero(financialReportResult.get(financialReportItemFormulaVo.getFormulaItemCode())));
                            }
                        } else if (LedgerConstant.FormulaType.SPECIALCASHFLOW.equals(financialReportItemFormulaVo.getFormulaType())) {
                            //现金流量特殊项
                            //按code计算,从科目余额取
                            if (LedgerConstant.Operator.ADD.equals(financialReportItemFormulaVo.getOperator())) {
                                //加
                                result = result.add(FastUtils.Null2Zero(getSubjectMoney(balanceSubject, financialReportItemFormulaVo.getFormulaItemCode(), financialReportItemFormulaVo.getFormulaItemDirection())));
                            } else if (LedgerConstant.Operator.SUBTRACT.equals(financialReportItemFormulaVo.getOperator())) {
                                //减
                                result = result.subtract(FastUtils.Null2Zero(getSubjectMoney(balanceSubject, financialReportItemFormulaVo.getFormulaItemCode(), financialReportItemFormulaVo.getFormulaItemDirection())));
                            }
                        }
                    }
                }
            }

            // 判断是否计算重分类 && 非空判断
            if (balanceDto != null && Constant.Is.YES.equals(balanceDto.getRearrange()) && !FastUtils.checkNullOrEmpty(platformFinancialReportItemSetVo.getFinancialReportRearrangeFormulaVoList())) {
                result = BigDecimal.ZERO;
                // 循环重分类计算公式
                for (FinancialReportRearrangeFormulaVo mula : platformFinancialReportItemSetVo.getFinancialReportRearrangeFormulaVoList()) {
                    // 循环末级科目
                    for (Map.Entry<String, String> entry : balanceSubject.entrySet()) {
                        // 末级科目方向
                        Byte lastDirection = Byte.valueOf(entry.getValue().split(LedgerConstant.ExportConstant.SPLIT)[0]);
                        // 末级科目余额
                        BigDecimal lastBalance = new BigDecimal(entry.getValue().split(LedgerConstant.ExportConstant.SPLIT)[1]);

                        // 对方科目
                        if (Constant.Is.YES.equals(mula.getIsOther()) && entry.getKey().startsWith(mula.getOthersideFormulaItemCode())) {
                            if (lastDirection.equals(mula.getCFormulaItemDirection()) && lastBalance.compareTo(BigDecimal.ZERO) == 1) {
                                result = result.add(lastBalance);
                            }

                            if ((!lastDirection.equals(mula.getCFormulaItemDirection())) && lastBalance.compareTo(BigDecimal.ZERO) == -1) {
                                result = result.subtract(lastBalance);
                            }
                        }

                        // 非对方科目
                        if (Constant.Is.NO.equals(mula.getIsOther()) && entry.getKey().startsWith(mula.getFormulaItemCode())) {
                            // 坏账准备 仅存在一个级次的科目 且 是余额
                            if (mula.getFormulaItemCode().startsWith(LedgerConstant.Ledger.BAD_DEBT_CODE)) {
                                // 减
                                result = result.subtract(lastBalance);

                            } else {
                                if (lastDirection.equals(mula.getCFormulaItemDirection()) && lastBalance.compareTo(BigDecimal.ZERO) == 1) {
                                    result = result.add(lastBalance);
                                }

                                if ((!lastDirection.equals(mula.getCFormulaItemDirection())) && lastBalance.compareTo(BigDecimal.ZERO) == -1) {
                                    result = result.subtract(lastBalance);
                                }
                            }
                        }
                    }

                }
            }
            financialReportResult.put(platformFinancialReportItemSetVo.getCode(), result);
        }
        return financialReportResult;
    }

    /**
     * @return void
     * @description: 导出利润表
     * @Param [response, incomeStatementsTableDto]
     * @author LuoY
     * @date 2019/8/30 9:20
     */
    @Override
    public void exportIssueProfit(HttpServletResponse response, IncomeStatementsReportDto incomeStatementsTableDto) {
        //查询出利润表
        List<IncomeStatementsAccountBookVo> incomeStatementsAccountBookVoList = findCurrentIssueProfit(incomeStatementsTableDto);
        //获取单个核算主体列数
        int singleEntityCells = singleEntityCells(incomeStatementsTableDto);
        //抽取标题头
        List<FinancialReport> financialReports = financialReportsHandle(incomeStatementsAccountBookVoList, singleEntityCells);
        //执行导出
        String fileName = LedgerConstant.FinancialReportName.PROFITREPORT;
        List<String[]> financialDatas = financialDataHandle(incomeStatementsAccountBookVoList, incomeStatementsTableDto);
        //期间
        String period = incomeStatementsTableDto.getPeriodYear() + "-" + incomeStatementsTableDto.getPeriodNum();
        //币种(第一个核算账簿币种信息)
        String currency = incomeStatementsAccountBookVoList.get(Constant.Number.ZERO).getCurrencyName();
        try {
            exportDataToExcel(response, fileName, LedgerConstant.ExportConstant.SHEETPROFITNAME, financialReports, financialDatas, period, currency);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new ServiceException(ResultCode.EXPORTDATATOEXCEL_FAIL);
        }
    }

    /**
     * @param response
     * @param cashFlowReportDto
     * @return void
     * @description: 导出现金流量表
     * @Param [response, incomeStatementsTableDto]
     * @author LuoY
     * @date 2019/8/30 9:20
     */
    @Override
    public void exportCashFlowReport(HttpServletResponse response, CashFlowReportDto cashFlowReportDto) {
        //查询出现金流量表结果
        List<IncomeStatementsAccountBookVo> incomeStatementsAccountBookVoList = findCashFlowReport(cashFlowReportDto);
        //获取单个核算主体列数
        int singleEntityCells = LedgerConstant.ExportConstant.SINGLECASHFLOWCELLNUM;
        //抽取标题头
        List<FinancialReport> financialReports = financialReportsHandle(incomeStatementsAccountBookVoList, singleEntityCells);
        //定义标题
        String fileName = LedgerConstant.FinancialReportName.CASHFLOWREPORT;
        //期间
        String period = cashFlowReportDto.getPeriodYear() + "-" + cashFlowReportDto.getPeriodNum();
        //币种(第一个核算账簿币种信息)
        String currency = incomeStatementsAccountBookVoList.get(Constant.Number.ZERO).getCurrencyName();
        //获取封装数据
        List<String[]> financialDatas = financialCashFlowDataHandle(incomeStatementsAccountBookVoList);
        try {
            //导出
            exportDataToExcel(response, fileName, LedgerConstant.ExportConstant.SHEETCASHFLOWNAME, financialReports, financialDatas, period, currency);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new ServiceException(ResultCode.EXPORTDATATOEXCEL_FAIL);
        }

    }

    /*******************设置基础信息**********************/
    /**
     * @return void
     * @description: 初始化核算账簿币种
     * @Param [incomeStatementsAccountBookVo]
     * @author LuoY
     * @date 2019/8/22 10:05
     */
    private void initializeAccountBookCurrencyName(IncomeStatementsAccountBookVo incomeStatementsAccountBookVo) {
        FastUtils.checkNull(incomeStatementsAccountBookVo.getCompanyId());
        //根据核算账簿对应公司id查询公司记账币种信息
        CompanyDto companyDto = new CompanyDto();
        companyDto.setId(incomeStatementsAccountBookVo.getCompanyId());
        CompanyVo companyVo = companyFeignClient.findCompanyById(companyDto).getData();
        if (!StringUtil.isBlank(companyVo)) {
            incomeStatementsAccountBookVo.setCurrencyId(companyVo.getAccountingCurrencyId());
            incomeStatementsAccountBookVo.setCurrencyName(companyVo.getAccountingCurrencyName());
        } else {
            incomeStatementsAccountBookVo.setCurrencyId(LedgerConstant.Ledger.DEFAULT_CURRENCY);
            incomeStatementsAccountBookVo.setCurrencyName(LedgerConstant.Ledger.DEFAULT_CURRENCYNAME);
        }

    }

    /**
     * @return void
     * @description: 初始化金额计算Map
     * @Param
     * @author LuoY
     * @date 2019/8/21 11:23
     */
    private void initializeMoneyFinancialMap(IncomeStatementsReportDto incomeStatementsTableDto, BalanceSubjectQueryDto balanceSubjectQueryDto,
                                             Map<String, BigDecimal> yearCumulativeFinancialReportMap, List<FinancialReportItemSetVo> financialReportItemSetVos,
                                             Map<String, BigDecimal> yearCompareFinancialReportMap, List<AccountSubjectVo> accountSubjectVoList,
                                             Byte lastYearMaxPeriodNum, Map<String, BigDecimal> monthCompareFinancialReportMap,
                                             IncomeStatementsAccountEntityVo incomeStatementsAccountEntityVo, List<IncomeStatementsVo> currentIncomeStatementsVoList,
                                             IncomeStatementsAccountBookVo incomeStatementsAccountBookVo, Map<String, BigDecimal> currentMoney) {
        //本年累计
        if (Constant.Is.YES.equals(incomeStatementsTableDto.getYearCumulative())) {
            yearCumulativeFinancialReportMap.putAll(financialProfitReportMap(LedgerConstant.IncomeRationType.YEARCUMULATIVE, incomeStatementsTableDto.getPosting(), financialReportItemSetVos, currentIncomeStatementsVoList));
        }
        //同比
        if (Constant.Is.YES.equals(incomeStatementsTableDto.getYearOnYear())) {
            //设置同比期间科目余额查询条件
            BalanceSubjectQueryDto balanceYearCompareDto = new BalanceSubjectQueryDto();
            //年度期间-1
            balanceYearCompareDto.setPeriodYear(balanceSubjectQueryDto.getPeriodYear() - Constant.Number.ONE);
            balanceYearCompareDto.setPeriodNum(balanceSubjectQueryDto.getPeriodNum());
            balanceYearCompareDto.setAccountBookId(balanceSubjectQueryDto.getAccountBookId());
            balanceYearCompareDto.setAccountBookEntityId(balanceSubjectQueryDto.getAccountBookEntityId());

            //获取当前核算主体,同比期间的科目余额信息
            List<BalanceSubjectVo> yearCompareSubjectList = findBalance(balanceYearCompareDto);
            //当前核算主体同比期间科目余额信息处理
            List<IncomeStatementsVo> yearCompareIncomeStatementsVoList = subjectDataHandle(yearCompareSubjectList, accountSubjectVoList);
            yearCompareFinancialReportMap.putAll(financialProfitReportMap(LedgerConstant.IncomeRationType.YEARCOMPARE, incomeStatementsTableDto.getPosting(), financialReportItemSetVos, yearCompareIncomeStatementsVoList));
        }
        //环比
        if (Constant.Is.YES.equals(incomeStatementsTableDto.getMonthOnMonth())) {
            //设置环比期间科目余额查询条件
            BalanceSubjectQueryDto balanceMonthCompareDto = new BalanceSubjectQueryDto();
            balanceMonthCompareDto.setAccountBookEntityId(balanceSubjectQueryDto.getAccountBookEntityId());
            balanceMonthCompareDto.setAccountBookId(balanceSubjectQueryDto.getAccountBookId());
            if (Constant.PeriodNum.January.equals(balanceSubjectQueryDto.getPeriodNum())) {
                //如果当前期间为1,则查询上一年度最后一个期间
                balanceMonthCompareDto.setPeriodYear(balanceSubjectQueryDto.getPeriodYear() - Constant.Number.ONE);
                //设置期间为上一年度最大期间数
                balanceMonthCompareDto.setPeriodNum(lastYearMaxPeriodNum);
            } else {
                balanceMonthCompareDto.setPeriodYear(balanceSubjectQueryDto.getPeriodYear());
                balanceMonthCompareDto.setPeriodNum((byte) (balanceSubjectQueryDto.getPeriodNum() - Constant.Number.ONE));
            }

            //获取当前核算主体,同比期间的科目余额信息
            List<BalanceSubjectVo> monthCompareSubjectList = findBalance(balanceMonthCompareDto);
            //当前核算主体同比期间科目余额信息处理
            List<IncomeStatementsVo> monthIncomeStatementsVoList = subjectDataHandle(monthCompareSubjectList, accountSubjectVoList);
            monthCompareFinancialReportMap.putAll(financialProfitReportMap(LedgerConstant.IncomeRationType.MONTHCOMPARE,
                    incomeStatementsTableDto.getPosting(), financialReportItemSetVos, monthIncomeStatementsVoList));
        }
        //科目明细
        if (Constant.Is.YES.equals(incomeStatementsTableDto.getSubjectDetail())) {
            //如果需要计算科目明细.先处理同比和环比值
            financialReportSubject(incomeStatementsTableDto.getPosting(), incomeStatementsAccountEntityVo,
                    incomeStatementsAccountEntityVo.getIncomeStatementsTablesListVos(), financialReportItemSetVos,
                    currentIncomeStatementsVoList, incomeStatementsAccountBookVo, currentMoney, yearCumulativeFinancialReportMap);
        }
    }

    /**
     * @return java.lang.Byte
     * @description: 获取上一年的最大期间数
     * @Param [incomeStatementsTableDto, incomeStatementsAccountBookVo]
     * @author LuoY
     * @date 2019/8/21 10:42
     */
    private Byte initLastPeriodNum(IncomeStatementsReportDto incomeStatementsTableDto, IncomeStatementsAccountBookVo incomeStatementsAccountBookVo) {
        byte lastYearMaxPeriodNum = 0;
        //如果当前查询期间为1,且需要查询环比数据,查询当前账簿的上一年最大期间数
        if (Constant.Is.YES.equals(incomeStatementsTableDto.getMonthOnMonth()) && incomeStatementsTableDto.getPeriodNum().equals(Constant.Number.INITIAL)) {
            //查询当前核算主体的上一年最大期间数
            AccountBookPeriodDto accountBookPeriodDto = new AccountBookPeriodDto();
            accountBookPeriodDto.setAccountBookId(incomeStatementsAccountBookVo.getAccountBookId());
            //期间年度为当前查询年度期间上一年度
            accountBookPeriodDto.setPeriodYear(incomeStatementsTableDto.getPeriodYear() - Constant.Number.ONE);
            lastYearMaxPeriodNum = accountBookPeriodService.findMaxPeriodNumByYearAndAccountBookId(accountBookPeriodDto);
        }
        return lastYearMaxPeriodNum;
    }

    /**
     * @return java.util.List<com.njwd.entity.platform.vo.AccountSubjectVo>
     * @description: 获取末级会计科目
     * @Param [incomeStatementsAccountBookVo]
     * @author LuoY
     * @date 2019/8/21 10:28
     */
    private List<AccountSubjectVo> getAccountSubjectVo(IncomeStatementsAccountBookVo incomeStatementsAccountBookVo) {
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        accountSubjectDto.setSubjectId(incomeStatementsAccountBookVo.getSubjectId());
        accountSubjectDto.setIsFinal(Constant.Is.YES);
        List<AccountSubjectVo> accountSubjectVos = accountSubjectFeignClient.findInfoForLedger(accountSubjectDto).getData();
        return accountSubjectVos;
    }

    /**
     * @return void
     * @description: 初始化参与占收入比计算的列
     * @Param [incomeRation, incomeStatementsTableDto]
     * @author LuoY
     * @date 2019/8/21 10:04
     */
    private Map<String, Boolean> initializeIncomeRation(IncomeStatementsReportDto incomeStatementsTableDto) {
        Map<String, Boolean> incomeRation = new HashMap<>();
        //本期默认为true
        incomeRation.put(LedgerConstant.IncomeRationType.CURRENTISSUE, true);
        //本年累计
        if (Constant.Is.YES.equals(incomeStatementsTableDto.getYearCumulative())) {
            incomeRation.put(LedgerConstant.IncomeRationType.YEARCUMULATIVE, true);
        } else {
            incomeRation.put(LedgerConstant.IncomeRationType.YEARCUMULATIVE, false);
        }
        //同比
        if (Constant.Is.YES.equals(incomeStatementsTableDto.getYearOnYear())) {
            incomeRation.put(LedgerConstant.IncomeRationType.YEARCOMPARE, true);
        } else {
            incomeRation.put(LedgerConstant.IncomeRationType.YEARCOMPARE, false);
        }
        //环比
        if (Constant.Is.YES.equals(incomeStatementsTableDto.getMonthOnMonth())) {
            incomeRation.put(LedgerConstant.IncomeRationType.MONTHCOMPARE, true);
        } else {
            incomeRation.put(LedgerConstant.IncomeRationType.MONTHCOMPARE, false);
        }
        return incomeRation;
    }

    /**
     * @param bookId 核算账簿id
     * @return java.util.List<com.njwd.entity.platform.vo.FinancialReportItemSetVo>
     * @description: 根据核算账簿id账簿对应的财务报告
     * @author LuoY
     * @date 2019/8/9 13:31
     */
    private List<FinancialReportItemSetVo> findFinancialReport(Long bookId, String Type) {
        //根据核算主体获取对应的报告id
        AccountBookDto accountBookDto = new AccountBookDto();
        accountBookDto.setId(bookId);
        AccountBookVo accountBookVo = accountBookFeignClient.selectById(accountBookDto).getData();
        //校验查询的账簿信息
        FastUtils.checkNull(accountBookVo);
        FinancialReportItemSetDto financialReportItemSetDto = new FinancialReportItemSetDto();
        if (Type.equals(LedgerConstant.FinancialReportName.PROFITREPORT)) {
            financialReportItemSetDto.setReportId(accountBookVo.getIncomeStatementId());
        } else {
            financialReportItemSetDto.setReportId(accountBookVo.getCashFlowId());
        }

        financialReportItemSetDto.setRearrange(Constant.Number.ANTI_INITLIZED);
        //根据报告id获取财务报告
        List<FinancialReportItemSetVo> financialReportItemSetVos = financialReportItemSetFeignClient.findFinancialReportItemSetList(financialReportItemSetDto).getData();
        return financialReportItemSetVos;
    }

    /**
     * @param incomeStatementsAccountBookVo 初始化核算主体
     * @return void
     * @description:
     * @author LuoY
     * @date 2019/8/12 14:31
     */
    private void initAccountBookEntityInfo(IncomeStatementsAccountBookVo incomeStatementsAccountBookVo) {
        //没有核算主体信息,初始化一个同名核算账簿的核算主体(账簿查询的情况)
        if (FastUtils.checkNullOrEmpty(incomeStatementsAccountBookVo.getIncomeStatementsAccountEntityVos())) {
            List<IncomeStatementsAccountEntityVo> incomeStatementsAccountEntityVos = new LinkedList<>();
            IncomeStatementsAccountEntityVo incomeStatementsAccountEntityVo = new IncomeStatementsAccountEntityVo();
            //同账簿名核算主体,id为null
            incomeStatementsAccountEntityVo.setAccountBookEntityName(incomeStatementsAccountBookVo.getAccountBookName());
            incomeStatementsAccountEntityVos.add(incomeStatementsAccountEntityVo);
            incomeStatementsAccountBookVo.setIncomeStatementsAccountEntityVos(incomeStatementsAccountEntityVos);
        } else if (!FastUtils.checkNullOrEmpty(incomeStatementsAccountBookVo.getIncomeStatementsAccountEntityVos()) &&
                incomeStatementsAccountBookVo.getIncomeStatementsAccountEntityVos().size() > 1) {
            //如果核算主体个数大于2,添加核算主体name为"合计",id-1
            IncomeStatementsAccountEntityVo incomeStatementsAccountEntityVo = new IncomeStatementsAccountEntityVo();
            incomeStatementsAccountEntityVo.setAccountBookEntityName(LedgerConstant.FinancialString.TOTAL);
            incomeStatementsAccountEntityVo.setAccountBookEntityId(LedgerConstant.FinancialString.ACCOUNTBOOKID);
            //合计放list第一位
            incomeStatementsAccountBookVo.getIncomeStatementsAccountEntityVos().add(Constant.Number.ZERO, incomeStatementsAccountEntityVo);
        }
    }

    /**
     * @param financialReportItemSetDtos 财务报告,incomeStatementsTableList 核算主体
     * @return java.util.List<com.njwd.ledger.entity.vo.IncomeStatementsAccountEntityVo>
     * @description: 设置利润表每个核算主体的财务报告
     * @author LuoY
     * @date 2019/7/29 11:10
     */
    private void financialReportProfit(@NotNull List<IncomeStatementsAccountEntityVo> incomeStatementsAccountEntityVoList, @NotNull List<FinancialReportItemSetVo> financialReportItemSetDtos) {
        for (IncomeStatementsAccountEntityVo incomeStatementsAccountEntityVo : incomeStatementsAccountEntityVoList) {
            if (!LedgerConstant.FinancialString.ACCOUNTBOOKID.equals(incomeStatementsAccountEntityVo.getAccountBookEntityId())) {
                List<IncomeStatementsTableVo> incomeStatementsTableVoList = new LinkedList<>();
                for (FinancialReportItemSetVo financialReportItemSetDto : financialReportItemSetDtos) {
                    IncomeStatementsTableVo incomeStatementsTableVo = new IncomeStatementsTableVo();
                    //报告表名
                    incomeStatementsTableVo.setTableCode(LedgerConstant.FinancialReportCode.INCOMESTAEMENTCODE);
                    //报告名称
                    incomeStatementsTableVo.setProjectName(financialReportItemSetDto.getName());
                    //行号
                    incomeStatementsTableVo.setProjectCode(financialReportItemSetDto.getCode());
                    //是否科目明细
                    incomeStatementsTableVo.setIsSubject(Constant.Is.NO);
                    //级次
                    incomeStatementsTableVo.setLevel(financialReportItemSetDto.getLevel());
                    //类型
                    incomeStatementsTableVo.setItemType(financialReportItemSetDto.getItemType());
                    //增减标识
                    incomeStatementsTableVo.setIsAdd(financialReportItemSetDto.getIsAdd());
                    //流向标识
                    incomeStatementsTableVo.setIsFlow(financialReportItemSetDto.getIsFlow());
                    //流向标识
                    incomeStatementsTableVo.setIsContain(financialReportItemSetDto.getIsContain());
                    //添加利润表
                    incomeStatementsTableVoList.add(incomeStatementsTableVo);
                }
                incomeStatementsAccountEntityVo.setIncomeStatementsTablesListVos(incomeStatementsTableVoList);
            }
        }
    }

    /**
     * @param incomeStatementsAccountEntityVoList, financialReportItemSetDtos
     * @return void
     * @description: 设置现金流量表每个核算主体的财务报告
     * @author LuoY
     * @date 2019/8/14 13:45
     */
    private void financialReportCashFlow(@NotNull List<IncomeStatementsAccountEntityVo> incomeStatementsAccountEntityVoList, @NotNull List<FinancialReportItemSetVo> financialReportItemSetDtos) {
        for (IncomeStatementsAccountEntityVo incomeStatementsAccountEntityVo : incomeStatementsAccountEntityVoList) {
            List<CashFlowReportTabletVo> cashFlowReportTabletVos = new LinkedList<>();
            for (FinancialReportItemSetVo financialReportItemSetDto : financialReportItemSetDtos) {
                CashFlowReportTabletVo cashFlowReportTabletVo = new CashFlowReportTabletVo();
                //报告表名
                cashFlowReportTabletVo.setTableCode(LedgerConstant.FinancialReportCode.CASHFLOWTABLECODE);
                //报告名称
                cashFlowReportTabletVo.setProjectName(financialReportItemSetDto.getName());
                //行号
                cashFlowReportTabletVo.setProjectCode(financialReportItemSetDto.getCode());
                //级次
                cashFlowReportTabletVo.setLevel(financialReportItemSetDto.getLevel());
                //类型
                cashFlowReportTabletVo.setItemType(financialReportItemSetDto.getItemType());
                //增减标识
                cashFlowReportTabletVo.setIsAdd(financialReportItemSetDto.getIsAdd());
                //流向标识
                cashFlowReportTabletVo.setIsFlow(financialReportItemSetDto.getIsFlow());
                //添加现金流量表
                cashFlowReportTabletVos.add(cashFlowReportTabletVo);
            }
            incomeStatementsAccountEntityVo.setCashFlowReportTabletVos(cashFlowReportTabletVos);
        }
    }

    /**
     * @param incomeStatementsAccountEntityVo, financialReportItemSetDtos, incomeStatementsTableDto, accountSubjectVoList
     * @return void
     * @description: 科目余额明细项
     * @author LuoY
     * @date 2019/8/7 16:58
     */
    private void financialReportSubject(Byte posting, IncomeStatementsAccountEntityVo incomeStatementsAccountEntityVo,
                                        List<IncomeStatementsTableVo> incomeStatementsTableVo, List<FinancialReportItemSetVo> financialReportItemSetDtos,
                                        List<IncomeStatementsVo> currentIncomeStatementsVoList, IncomeStatementsAccountBookVo incomeStatementsAccountBookVo,
                                        Map<String, BigDecimal> currentMoney, Map<String, BigDecimal> yearCul) {
        List<IncomeStatementsTableVo> incomeStatementsTableVos = new LinkedList<>();
        //获取当前报告公式配置的所有科目的科目层级
        Map<String, List<AccountSubjectVo>> subjectDetail = findCodeAllLevel(financialReportItemSetDtos, incomeStatementsAccountBookVo);
        //整合科目表和科目余额信息
        Map<String, List<IncomeStatementsTableVo>> subjectDetailMoneyInfo = subjectAllinfoHandle(subjectDetail, currentIncomeStatementsVoList, posting);
        //获取本期科目余额map
        Map<String, String> currentBalanceMap = incomeDataToMap(currentIncomeStatementsVoList, true, posting);
        //获取本年累计科目余额map
        Map<String, String> yearBalanceMap = incomeDataToMap(currentIncomeStatementsVoList, false, posting);
        //计算每个code所有非末级的金额
        subjectDataMoneyCaulation(subjectDetailMoneyInfo, currentBalanceMap, yearBalanceMap);

        //循环利润表
        for (IncomeStatementsTableVo incomeStatementsTableVo1 : incomeStatementsTableVo) {
            //循环财务报告
            for (FinancialReportItemSetVo financialReportItemSetDto : financialReportItemSetDtos) {
                //如果利润表和财务报告code相等且有计算公式
                if (incomeStatementsTableVo1.getProjectCode().equals(financialReportItemSetDto.getCode()) && !FastUtils.checkNullOrEmpty(financialReportItemSetDto.getFinancialReportItemFormulaVoList())) {
                    //循环计算公式
                    for (FinancialReportItemFormula financialReportItemFormula : financialReportItemSetDto.getFinancialReportItemFormulaVoList()) {
                        //过滤掉公式类型不是编码的
                        if (!StringUtil.isBlank(financialReportItemFormula.getFormulaItemCode())) {
                            if (LedgerConstant.FormulaType.SUBECTORITEM.equals(financialReportItemFormula.getFormulaType())) {
                                //循环处理过的科目余额,根据公司code获取查询出来的科目信息
                                if (!FastUtils.checkNullOrEmpty(subjectDetailMoneyInfo.get(financialReportItemFormula.getFormulaItemCode()))) {
                                    //如果存在对应code科目信息,添加到利润表中
                                    subjectDetailMoneyInfo.get(financialReportItemFormula.getFormulaItemCode()).forEach(data -> {
                                        IncomeStatementsTableVo incomeStatementsTableVo2 = new IncomeStatementsTableVo();
                                        incomeStatementsTableVo2.setCurrentMoney(data.getCurrentMoney());
                                        //计算明细项本期合计
                                        if (StringUtil.isBlank(currentMoney.get(data.getProjectCode()))) {
                                            currentMoney.put(data.getProjectCode(), data.getCurrentMoney());
                                        } else {
                                            currentMoney.put(data.getProjectCode(), currentMoney.get(data.getProjectCode()).add(data.getCurrentMoney()));
                                        }
                                        incomeStatementsTableVo2.setYearCumulative(data.getYearCumulative());
                                        //计算明细项本年合计
                                        if (StringUtil.isBlank(yearCul.get(data.getProjectCode()))) {
                                            yearCul.put(data.getProjectCode(), data.getCurrentMoney());
                                        } else {
                                            yearCul.put(data.getProjectCode(), yearCul.get(data.getProjectCode()).add(data.getYearCumulative()));
                                        }
                                        incomeStatementsTableVo2.setProjectCode(data.getProjectCode());
                                        incomeStatementsTableVo2.setReportCode(financialReportItemSetDto.getCode());
                                        incomeStatementsTableVo2.setIsSubject(Constant.Is.YES);
                                        incomeStatementsTableVo2.setProjectName(data.getProjectCode().
                                                concat(LedgerConstant.ExportConstant.BLANK + data.getProjectName()));
                                        incomeStatementsTableVo2.setLevel(new BigDecimal(financialReportItemSetDto.getLevel().toString()).
                                                add(new BigDecimal(data.getLevel().toString())).byteValueExact());
                                        if (StringUtil.isBlank(data.getUpProjectCode())) {
                                            incomeStatementsTableVo2.setUpProjectCode(financialReportItemSetDto.getCode());
                                        } else {
                                            if (financialReportItemFormula.getFormulaItemCode().equals(data.getProjectCode())) {
                                                incomeStatementsTableVo2.setUpProjectCode(financialReportItemSetDto.getCode());
                                            } else {
                                                incomeStatementsTableVo2.setUpProjectCode(data.getUpProjectCode());
                                            }
                                        }

                                        // 遍历待返回项目，无重复项
                                        boolean flag  = false;
                                        for(IncomeStatementsTableVo income:incomeStatementsTableVos){
                                            if(income.getProjectCode().equals(incomeStatementsTableVo2.getProjectCode())){
                                                flag = true;
                                            }
                                        }
                                        if(!flag){
                                            incomeStatementsTableVos.add(incomeStatementsTableVo2);
                                        }
                                    });
                                }
                            }
                        }

                    }
                }
            }
        }
        incomeStatementsAccountEntityVo.getIncomeStatementsTablesListVos().addAll(incomeStatementsTableVos);
    }

    /*******************计算方法体**********************/
    /*******************利润表**********************/
    /**
     * @return void
     * @description: 利润表金额计算
     * @Param incomeStatementsTableVo, currentFinancialReportMap, yearCumulativeFinancialReportMap
     * @Param yearCompareFinancialReportMap, monthCompareFinancialReportMap, incomeStatementsTableDto
     * @author LuoY
     * @date 2019/8/21 10:51
     */
    private void calculationMoney(IncomeStatementsTableVo incomeStatementsTableVo, Map<String, BigDecimal> currentFinancialReportMap,
                                  Map<String, BigDecimal> yearCumulativeFinancialReportMap, Map<String, BigDecimal> yearCompareFinancialReportMap,
                                  Map<String, BigDecimal> monthCompareFinancialReportMap, IncomeStatementsReportDto incomeStatementsTableDto) {
        //计算本期金额
        calculation(LedgerConstant.IncomeRationType.CURRENTISSUE, incomeStatementsTableVo, currentFinancialReportMap, Constant.Is.NO);

        //计算本年累计
        if (Constant.Is.YES.equals(incomeStatementsTableDto.getYearCumulative())) {
            calculation(LedgerConstant.IncomeRationType.YEARCUMULATIVE, incomeStatementsTableVo, yearCumulativeFinancialReportMap, Constant.Is.NO);
        }

        //计算同比
        if (Constant.Is.YES.equals(incomeStatementsTableDto.getYearOnYear())) {
            calculation(LedgerConstant.IncomeRationType.YEARCOMPARE, incomeStatementsTableVo, yearCompareFinancialReportMap, Constant.Is.NO);
        }


        //计算环比
        if (Constant.Is.YES.equals(incomeStatementsTableDto.getMonthOnMonth())) {
            calculation(LedgerConstant.IncomeRationType.MONTHCOMPARE, incomeStatementsTableVo, monthCompareFinancialReportMap, Constant.Is.NO);
        }

        //计算增长率
        if (Constant.Is.YES.equals(incomeStatementsTableDto.getGrowRate())) {
            if (Constant.Is.YES.equals(incomeStatementsTableDto.getYearOnYear())) {
                //同比
                growRateCalculation(LedgerConstant.YearOrMonth.YEAR, incomeStatementsTableVo);
            } else if (Constant.Is.YES.equals(incomeStatementsTableDto.getMonthOnMonth())) {
                //环比
                growRateCalculation(LedgerConstant.YearOrMonth.MONTH, incomeStatementsTableVo);
            } else {
                //如果都不包含直接抛出异常
                throw new ServiceException(ResultCode.PROFITSTATEMENT_GROWDATE_ERROR);
            }
        }
    }

    /**
     * @return void
     * @description: 计算利润表合计
     * @Param incomeStatementsAccountBookVo, incomeStatementsTableDto, countCurrentMoney, countYearCulmulativeMoney
     * @Param countYearCompareMoney, countMonthCompareMoney, incomeRation
     * @author LuoY
     * @date 2019/8/21 10:24
     */
    private void calculationCount(IncomeStatementsAccountBookVo incomeStatementsAccountBookVo, IncomeStatementsReportDto incomeStatementsTableDto,
                                  Map<String, BigDecimal> countCurrentMoney, Map<String, BigDecimal> countYearCulmulativeMoney,
                                  Map<String, BigDecimal> countYearCompareMoney, Map<String, BigDecimal> countMonthCompareMoney, Map<String, Boolean> incomeRation) {
        //先初始化合计核算主体财务报告,取当前核算账簿第二个核算主体财务报告(所有核算主体财务报告应该一致)
        List<IncomeStatementsTableVo> incomeStatementsTableVos = new LinkedList<>();
        incomeStatementsAccountBookVo.getIncomeStatementsAccountEntityVos().get(Constant.Number.TWO).getIncomeStatementsTablesListVos().forEach(data -> {
                    IncomeStatementsTableVo incomeStatementsTableVo = new IncomeStatementsTableVo();
                    //金额初始化
                    incomeStatementsTableVo.setReportCode(data.getReportCode());
                    incomeStatementsTableVo.setDirection(data.getDirection());
                    incomeStatementsTableVo.setIsContain(data.getIsContain());
                    incomeStatementsTableVo.setLevel(data.getLevel());
                    incomeStatementsTableVo.setProjectName(data.getProjectName());
                    incomeStatementsTableVo.setProjectCode(data.getProjectCode());
                    incomeStatementsTableVo.setUpProjectCode(data.getUpProjectCode());
                    incomeStatementsTableVo.setTableCode(data.getTableCode());
                    incomeStatementsTableVo.setIsFlow(data.getIsFlow());
                    incomeStatementsTableVo.setIsAdd(data.getIsAdd());
                    incomeStatementsTableVo.setItemType(data.getItemType());
                    incomeStatementsTableVo.setIsSubject(data.getIsSubject());
                    incomeStatementsTableVos.add(incomeStatementsTableVo);
                }
        );
        incomeStatementsAccountBookVo.getIncomeStatementsAccountEntityVos().forEach(data -> {
                    if (LedgerConstant.FinancialString.ACCOUNTBOOKID.equals(data.getAccountBookEntityId())) {
                        data.setIncomeStatementsTablesListVos(incomeStatementsTableVos);
                    }
                }
        );
        for (IncomeStatementsAccountEntityVo incomeStatementsAccountEntityVo : incomeStatementsAccountBookVo.getIncomeStatementsAccountEntityVos()) {
            if (LedgerConstant.FinancialString.ACCOUNTBOOKID.equals(incomeStatementsAccountEntityVo.getAccountBookEntityId())) {
                //循环核算主体利润表
                for (IncomeStatementsTableVo incomeStatementsTableVo : incomeStatementsAccountEntityVo.getIncomeStatementsTablesListVos()) {
                    //计算本期金额
                    calculation(LedgerConstant.IncomeRationType.CURRENTISSUE, incomeStatementsTableVo, countCurrentMoney, Constant.Is.YES);

                    //计算本年累计
                    if (Constant.Is.YES.equals(incomeStatementsTableDto.getYearCumulative())) {
                        calculation(LedgerConstant.IncomeRationType.YEARCUMULATIVE, incomeStatementsTableVo, countYearCulmulativeMoney, Constant.Is.YES);
                    }

                    //计算同比
                    if (Constant.Is.YES.equals(incomeStatementsTableDto.getYearOnYear())) {
                        calculation(LedgerConstant.IncomeRationType.YEARCOMPARE, incomeStatementsTableVo, countYearCompareMoney, Constant.Is.YES);
                    }

                    //计算环比
                    if (Constant.Is.YES.equals(incomeStatementsTableDto.getMonthOnMonth())) {
                        //计算当前核算主体同比金额
                        calculation(LedgerConstant.IncomeRationType.MONTHCOMPARE, incomeStatementsTableVo, countMonthCompareMoney, Constant.Is.YES);
                    }
                    //计算增长率
                    if (Constant.Is.YES.equals(incomeStatementsTableDto.getGrowRate())) {
                        if (Constant.Is.YES.equals(incomeStatementsTableDto.getYearOnYear())) {
                            //同比
                            growRateCalculation(LedgerConstant.YearOrMonth.YEAR, incomeStatementsTableVo);
                        } else if (Constant.Is.YES.equals(incomeStatementsTableDto.getYearOnYear())) {
                            //环比
                            growRateCalculation(LedgerConstant.YearOrMonth.MONTH, incomeStatementsTableVo);
                        }
                    }
                }
                //计算占收入比
                if (Constant.Is.YES.equals(incomeStatementsTableDto.getIncomeRatio())) {
                    currentIncomeRationCal(incomeRation, incomeStatementsAccountEntityVo.getIncomeStatementsTablesListVos());
                }
            }
        }
    }

    /**
     * @param type, posting, financialReportItemSetVoList, incomeStatementsVoList
     * @return java.util.Map<java.lang.String, java.math.BigDecimal>
     * @description: 计算利润表财务报告金额
     * @author LuoY
     * @date 2019/8/9 13:34
     */
    private Map<String, BigDecimal> financialProfitReportMap(String type, Byte posting, @NotNull List<FinancialReportItemSetVo> financialReportItemSetVoList, @NotNull List<IncomeStatementsVo> incomeStatementsVoList) {
        //获取当前核算金额map
        Map<String, String> currentMoneyMap;
        if (LedgerConstant.IncomeRationType.YEARCUMULATIVE.equals(type)) {
            //本年累计
            currentMoneyMap = subjectDataToMap(Constant.Is.YES, posting, incomeStatementsVoList);
        } else {
            //其他
            currentMoneyMap = subjectDataToMap(Constant.Is.NO, posting, incomeStatementsVoList);
        }
        //先计算财务报告金额
        Map<String, BigDecimal> financialProfit = financialReportCalculationResult(financialReportItemSetVoList, currentMoneyMap, null, null);
        return financialProfit;
    }

    /**
     * @param type 计算类型, incomeStatementsTableVo 利润表,financialReport 财务报告map
     * @return void
     * @description: 计算本期, 本年累计, 同比, 环比
     * @author LuoY
     * @date 2019/8/6 16:25
     */
    private void calculation(@NotNull String type, @NotNull IncomeStatementsTableVo incomeStatementsTableVo, @NotNull Map<String, BigDecimal> financialReport, Byte isSubject) {
        //给财务报告对应的利润表赋值
        if (Constant.Is.YES.equals(isSubject)) {
            //需要计算科目明细
            if (LedgerConstant.IncomeRationType.CURRENTISSUE.equals(type)) {
                //本期
                incomeStatementsTableVo.setCurrentMoney(FastUtils.Null2Zero(financialReport.get(incomeStatementsTableVo.getProjectCode())));
            } else if (LedgerConstant.IncomeRationType.YEARCUMULATIVE.equals(type)) {
                //本年累计
                incomeStatementsTableVo.setYearCumulative(FastUtils.Null2Zero(financialReport.get(incomeStatementsTableVo.getProjectCode())));
            } else if (LedgerConstant.IncomeRationType.YEARCOMPARE.equals(type)) {
                //同比
                incomeStatementsTableVo.setYearOnYear(FastUtils.Null2Zero(financialReport.get(incomeStatementsTableVo.getProjectCode())));
            } else if (LedgerConstant.IncomeRationType.MONTHCOMPARE.equals(type)) {
                //环比
                incomeStatementsTableVo.setMonthOnMonth(FastUtils.Null2Zero(financialReport.get(incomeStatementsTableVo.getProjectCode())));
            }
        } else {
            if (isSubject.equals(incomeStatementsTableVo.getIsSubject())) {
                //不计算科目明细
                if (LedgerConstant.IncomeRationType.CURRENTISSUE.equals(type)) {
                    //本期
                    incomeStatementsTableVo.setCurrentMoney(FastUtils.Null2Zero(financialReport.get(incomeStatementsTableVo.getProjectCode())));
                } else if (LedgerConstant.IncomeRationType.YEARCUMULATIVE.equals(type)) {
                    //本年累计
                    incomeStatementsTableVo.setYearCumulative(FastUtils.Null2Zero(financialReport.get(incomeStatementsTableVo.getProjectCode())));
                } else if (LedgerConstant.IncomeRationType.YEARCOMPARE.equals(type)) {
                    //同比
                    incomeStatementsTableVo.setYearOnYear(FastUtils.Null2Zero(financialReport.get(incomeStatementsTableVo.getProjectCode())));
                } else if (LedgerConstant.IncomeRationType.MONTHCOMPARE.equals(type)) {
                    //环比
                    incomeStatementsTableVo.setMonthOnMonth(FastUtils.Null2Zero(financialReport.get(incomeStatementsTableVo.getProjectCode())));
                }
            }
        }
    }

    /**
     * @param yearOrMonth 同比或环比,IncomeStatementsTableVo
     * @return void
     * @description: 计算增长率
     * @author LuoY
     * @date 2019/7/31 11:16
     */
    private void growRateCalculation(Byte yearOrMonth, @NotNull IncomeStatementsTableVo incomeStatementsTableVos) {
        //给财务报告项计算同比或环比
        if (Constant.Is.NO.equals(incomeStatementsTableVos.getIsSubject())) {
            if (LedgerConstant.YearOrMonth.MONTH.equals(yearOrMonth)) {
                //环比增长率=（本期-环比)/环比,保留两位小数,四舍五入,除数为0值为0
                incomeStatementsTableVos.setGrowRate(FastUtils.Null2Zero(incomeStatementsTableVos.getMonthOnMonth()).compareTo(BigDecimal.ZERO) == Constant.Number.ZERO ?
                        new BigDecimal(Constant.Number.ZERO) :
                        ((incomeStatementsTableVos.getCurrentMoney().
                                subtract(incomeStatementsTableVos.getMonthOnMonth())).
                                divide(FastUtils.Null2Zero(incomeStatementsTableVos.getMonthOnMonth()), LedgerConstant.FRPDecimalDigits.FINANCIALDECIMALPERCENT, RoundingMode.HALF_UP).
                                multiply(new BigDecimal(Constant.Number.ONEHUNDRED))).
                                setScale(LedgerConstant.FRPDecimalDigits.FINANCIALDECIMAL, RoundingMode.HALF_UP));
            } else {
                //同比增长率=（本期-同比）/同比,保留两位小数,四舍五入，除数为0值为0
                incomeStatementsTableVos.setGrowRate(FastUtils.Null2Zero(incomeStatementsTableVos.getYearOnYear()).compareTo(BigDecimal.ZERO) == Constant.Number.ZERO ?
                        new BigDecimal(Constant.Number.ZERO) :
                        ((incomeStatementsTableVos.getCurrentMoney().
                                subtract(incomeStatementsTableVos.getYearOnYear())).
                                divide(FastUtils.Null2Zero(incomeStatementsTableVos.getYearOnYear()), LedgerConstant.FRPDecimalDigits.FINANCIALDECIMALPERCENT, RoundingMode.HALF_UP).
                                multiply(new BigDecimal(Constant.Number.ONEHUNDRED))).
                                setScale(LedgerConstant.FRPDecimalDigits.FINANCIALDECIMAL, RoundingMode.HALF_UP));
            }
        }
    }

    /**
     * @param incomeStatementsTableVos
     * @return void
     * @description: 计算收入占比
     * @author LuoY
     * @date 2019/7/31 14:58
     */
    private void currentIncomeRationCal(Map<String, Boolean> incomeRation, @NotNull List<IncomeStatementsTableVo> incomeStatementsTableVos) {
        //本期营业收入
        BigDecimal currentBusinessIncome = null;
        //本年累计营业收入
        BigDecimal yearBusinessIncome = null;
        //同比营业收入
        BigDecimal yearCompareBusinessIncome = null;
        //环比营业收入
        BigDecimal monthCompareBusinessIncome = null;
        for (int j = 0; j < incomeStatementsTableVos.size(); j++) {
            //先获取主营业务收入金额
            if (incomeStatementsTableVos.get(j).getProjectName().contains(LedgerConstant.FinancialString.MainBusinessIncome)) {
                //本期是肯定有值的，本期为null，其余皆为null
                if (currentBusinessIncome == null) {
                    //获取对应营业收入金额
                    if (incomeRation.get(LedgerConstant.IncomeRationType.CURRENTISSUE)) {
                        //本期
                        currentBusinessIncome = incomeStatementsTableVos.get(j).getCurrentMoney();
                    }
                    if (incomeRation.get(LedgerConstant.IncomeRationType.YEARCUMULATIVE)) {
                        //本年累计
                        yearBusinessIncome = incomeStatementsTableVos.get(j).getYearCumulative();
                    }
                    if (incomeRation.get(LedgerConstant.IncomeRationType.YEARCOMPARE)) {
                        //同比
                        yearCompareBusinessIncome = incomeStatementsTableVos.get(j).getYearOnYear();
                    }
                    if (incomeRation.get(LedgerConstant.IncomeRationType.MONTHCOMPARE)) {
                        //环比
                        monthCompareBusinessIncome = incomeStatementsTableVos.get(j).getMonthOnMonth();
                    }
                    //获取到对应的占收入比之后，重新循环开始计算占收入比
                    j = 0;
                } else {
                    //营业收入不计算收入占比
                    continue;
                }
            }
            //本期营业收入不为null,计算本期收入占比（bigDecimal判断是否相等的时候要求小数位相等，会出现0!=0.0的情况,
            // 所以使用bigDecimal.compareTo(BigDecimal.Zero)==0判断）
            if (currentBusinessIncome != null) {
                //本期金额占收入比=本期金额/本期营业收入
                incomeStatementsTableVos.get(j).setCurrentIncomeRatio(currentBusinessIncome.compareTo(BigDecimal.ZERO) == Constant.Number.ZERO ?
                        new BigDecimal(Constant.Number.ZERO) :
                        (FastUtils.Null2Zero(incomeStatementsTableVos.get(j).getCurrentMoney()).
                                divide(currentBusinessIncome, LedgerConstant.FRPDecimalDigits.FINANCIALDECIMALPERCENT, RoundingMode.HALF_UP).
                                multiply(new BigDecimal(Constant.Number.ONEHUNDRED))).
                                setScale(LedgerConstant.FRPDecimalDigits.FINANCIALDECIMAL, RoundingMode.HALF_UP));
            }
            if (yearBusinessIncome != null) {
                //本年累计金额占收入比=本年累计金额/本年累计营业收入
                incomeStatementsTableVos.get(j).setYearIncomeRatio(yearBusinessIncome.compareTo(BigDecimal.ZERO) == Constant.Number.ZERO ?
                        new BigDecimal(Constant.Number.ZERO) :
                        (FastUtils.Null2Zero(incomeStatementsTableVos.get(j).getYearCumulative()).
                                divide(yearBusinessIncome, LedgerConstant.FRPDecimalDigits.FINANCIALDECIMALPERCENT, RoundingMode.HALF_UP).
                                multiply(new BigDecimal(Constant.Number.ONEHUNDRED))).
                                setScale(LedgerConstant.FRPDecimalDigits.FINANCIALDECIMAL, RoundingMode.HALF_UP));
            }
            if (yearCompareBusinessIncome != null) {
                //同比金额占收入比=同比金额/同比营业收入
                incomeStatementsTableVos.get(j).setYearCompareIncomeRatio(yearCompareBusinessIncome.compareTo(BigDecimal.ZERO) == Constant.Number.ZERO ?
                        new BigDecimal(Constant.Number.ZERO) :
                        (FastUtils.Null2Zero(incomeStatementsTableVos.get(j).getYearOnYear()).
                                divide(yearCompareBusinessIncome, LedgerConstant.FRPDecimalDigits.FINANCIALDECIMALPERCENT, RoundingMode.HALF_UP).
                                multiply(new BigDecimal(Constant.Number.ONEHUNDRED))).
                                setScale(LedgerConstant.FRPDecimalDigits.FINANCIALDECIMAL, RoundingMode.HALF_UP));
            }
            if (monthCompareBusinessIncome != null) {
                //环比金额占收入比=环比金额/环比营业收入
                incomeStatementsTableVos.get(j).setMonthCompareIncomeRatio(monthCompareBusinessIncome.compareTo(BigDecimal.ZERO) == Constant.Number.ZERO ?
                        new BigDecimal(Constant.Number.ZERO) :
                        (FastUtils.Null2Zero(incomeStatementsTableVos.get(j).getMonthOnMonth()).
                                divide(monthCompareBusinessIncome, LedgerConstant.FRPDecimalDigits.FINANCIALDECIMALPERCENT, RoundingMode.HALF_UP).
                                multiply(new BigDecimal(Constant.Number.ONEHUNDRED))).
                                setScale(LedgerConstant.FRPDecimalDigits.FINANCIALDECIMAL, RoundingMode.HALF_UP));
            }
        }
    }

    /**
     * @param balanceSubject 科目余额信息
     * @param code           科目余额code
     * @return java.math.BigDecimal
     * @description: 计算科目余额
     * @author LuoY
     * @date 2019/8/6 11:41
     */
    private BigDecimal getSubjectMoney(Map<String, String> balanceSubject, String code, Byte direction) {
        BigDecimal result = new BigDecimal(Constant.Number.ZERO);
        if(balanceSubject.size()>Constant.Number.ZERO){
            for (Map.Entry<String, String> entry : balanceSubject.entrySet()) {
                //根据末级科目code去计算所有以当前code开头的科目余额金额
                if (entry.getKey().startsWith(code)) {
                    // 末级科目方向
                    Byte lastDirection = Byte.valueOf(entry.getValue().split(LedgerConstant.ExportConstant.SPLIT)[0]);
                    // 余额
                    BigDecimal balance = new BigDecimal(entry.getValue().split(LedgerConstant.ExportConstant.SPLIT)[1]);
                    if (direction.equals(lastDirection.byteValue())) {
                        result = result.add(balance);
                    } else {
                        result = result.subtract(balance);
                    }

                }
            }
        }
        return result;
    }

    /*******************现金流量表**********************/
    /**
     * @param type, posting, financialReportItemSetVoList, incomeStatementsVoList
     * @return java.util.Map<java.lang.String, java.math.BigDecimal>
     * @description: 计算现金流量表财务报告金额
     * @author LuoY
     * @date 2019/8/9 13:34
     */
    private Map<String, BigDecimal> financialCashFlowReportMap(String type, Byte posting, List<FinancialReportItemSetVo> financialReportItemSetVoList, List<CashFlowReportVo> cashFlowReportVos, Map<String, Byte> cashFlowItemMap) {
        //获取当前核算金额map
        Map<String, String> currentMoneyMap;
        if (LedgerConstant.IncomeRationType.YEARCUMULATIVE.equals(type)) {
            //本年累计
            currentMoneyMap = cashFlowDataToMap(Constant.Is.YES, posting, cashFlowReportVos);
        } else {
            //其他
            currentMoneyMap = cashFlowDataToMap(Constant.Is.NO, posting, cashFlowReportVos);
        }
        //先计算财务报告金额
        Map<String, BigDecimal> financialCashFlow = financialReportCalculationResult(financialReportItemSetVoList, currentMoneyMap, null, cashFlowItemMap);
        return financialCashFlow;
    }

    /**
     * @param currentMap 当前核算主体map, countMap 合计财务报告map
     * @return java.util.Map<java.lang.String, java.math.BigDecimal>
     * @description: 计算当前核算主体合计map
     * @author LuoY
     * @date 2019/8/9 16:36
     */
    private Map<String, BigDecimal> calculationCountReportMap(Map<String, BigDecimal> currentMap, Map<String, BigDecimal> countMap) {
        Map<String, BigDecimal> map = new HashMap<>();
        if (Constant.Number.ZERO.equals(countMap.size())) {
            map = currentMap;
        } else {
            //每个code累加
            for (String key : countMap.keySet()) {
                map.put(key, FastUtils.Null2Zero(countMap.get(key).add(FastUtils.Null2Zero(currentMap.get(key)))));
            }
        }
        return map;
    }

    /**
     * @param type 计算类型, incomeStatementsTableVo 利润表,financialReport 财务报告map
     * @return void
     * @description: 计算本期, 本年累计
     * @author LuoY
     * @date 2019/8/6 16:25
     */
    private void calculationCashFlow(@NotNull String type, @NotNull CashFlowReportTabletVo cashFlowReportTabletVo, @NotNull Map<String, BigDecimal> financialReport) {
        //给财务报告对应的利润表赋值
        if (LedgerConstant.IncomeRationType.CURRENTISSUE.equals(type)) {
            //本期
            cashFlowReportTabletVo.setCurrentMoney(FastUtils.Null2Zero(financialReport.get(cashFlowReportTabletVo.getProjectCode())));
        } else if (LedgerConstant.IncomeRationType.YEARCUMULATIVE.equals(type)) {
            //本年累计
            cashFlowReportTabletVo.setYearCumulative(FastUtils.Null2Zero(financialReport.get(cashFlowReportTabletVo.getProjectCode())));
        }
    }

    /*******************数据操作体**********************/
    /*******************利润表**********************/
    /**
     * @param currentSubjectList 科目余额表, accountSubjectVoList 基础科目表
     * @return java.util.List<com.njwd.entity.ledger.vo.IncomeStatementsTableVo>
     * @description: 科目余额数据整合
     * @author LuoY
     * @date 2019/8/7 10:47
     */
    private List<IncomeStatementsVo> subjectDataHandle(List<BalanceSubjectVo> currentSubjectList, List<AccountSubjectVo> accountSubjectVoList) {
        List<IncomeStatementsVo> incomeStatementsVoList = new LinkedList<>();
        if (!FastUtils.checkNullOrEmpty(currentSubjectList)) {
            for (BalanceSubjectVo balanceSubject : currentSubjectList) {
                //本期
                BigDecimal currentResult = new BigDecimal(Constant.Number.ZERO);
                //已过账本期
                BigDecimal postCurrentResult = new BigDecimal(Constant.Number.ZERO);
                //本年累计
                BigDecimal currentYearResult = new BigDecimal(Constant.Number.ZERO);
                //已过账本年累计
                BigDecimal postCurrentYearResult = new BigDecimal(Constant.Number.ZERO);
                IncomeStatementsVo incomeStatementsVo = null;
                if (!FastUtils.checkNullOrEmpty(accountSubjectVoList)) {
                    for (AccountSubjectVo accountSubjectVo : accountSubjectVoList) {
                        //根据科目id处理
                        if (balanceSubject.getAccountSubjectId().equals(accountSubjectVo.getId())) {
                            incomeStatementsVo = new IncomeStatementsVo();
                            //根据科目方向计算对应科目金额(科目金额计算规则： 贷方, + 贷方 - 借方 ; 借方, + 借方 - 贷方)
                            if (Constant.BalanceDirection.CREDIT.equals(accountSubjectVo.getBalanceDirection())) {
                                //贷方(剔除损益发生额)
                                currentResult = currentResult.add(FastUtils.Null2Zero(balanceSubject.getCreditAmount()).subtract(FastUtils.Null2Zero(balanceSubject.getSyCreditAmount()))).
                                        subtract(FastUtils.Null2Zero(balanceSubject.getDebitAmount()).subtract(FastUtils.Null2Zero(balanceSubject.getSyDebitAmount())));
                                postCurrentResult = postCurrentResult.add(FastUtils.Null2Zero(balanceSubject.getPostCreditAmount()).subtract(FastUtils.Null2Zero((balanceSubject.getPostSyCreditAmount()))).
                                        subtract(FastUtils.Null2Zero(balanceSubject.getPostDebitAmount()).subtract(FastUtils.Null2Zero(balanceSubject.getPostSyDebitAmount()))));
                                currentYearResult = currentYearResult.add(FastUtils.Null2Zero(balanceSubject.getTotalCreditAmount()).subtract(FastUtils.Null2Zero(balanceSubject.getSyTotalCreditAmount()))).
                                        subtract(FastUtils.Null2Zero(balanceSubject.getTotalDebitAmount()).subtract(FastUtils.Null2Zero(balanceSubject.getSyTotalDebitAmount())));
                                postCurrentYearResult = postCurrentYearResult.add(FastUtils.Null2Zero(balanceSubject.getPostTotalCreditAmount()).subtract(FastUtils.Null2Zero(balanceSubject.getPostSyTotalCreditAmount()))).
                                        subtract(FastUtils.Null2Zero(balanceSubject.getPostTotalDebitAmount()).subtract(FastUtils.Null2Zero(balanceSubject.getPostSyTotalDebitAmount())));
                            } else {
                                //借方(剔除损益发生额)
                                currentResult = currentResult.add(FastUtils.Null2Zero(balanceSubject.getDebitAmount()).subtract(FastUtils.Null2Zero(balanceSubject.getSyDebitAmount()))).
                                        subtract(FastUtils.Null2Zero(balanceSubject.getCreditAmount()).subtract(FastUtils.Null2Zero(balanceSubject.getSyCreditAmount())));
                                postCurrentResult = postCurrentResult.add(FastUtils.Null2Zero(balanceSubject.getPostDebitAmount().subtract(balanceSubject.getPostSyDebitAmount()))).
                                        subtract(FastUtils.Null2Zero(balanceSubject.getPostCreditAmount()).subtract(FastUtils.Null2Zero(balanceSubject.getPostSyCreditAmount())));
                                currentYearResult = currentYearResult.add(balanceSubject.getTotalDebitAmount().subtract(balanceSubject.getSyTotalDebitAmount())).
                                        subtract(FastUtils.Null2Zero(balanceSubject.getTotalCreditAmount()).subtract(FastUtils.Null2Zero(balanceSubject.getSyTotalCreditAmount())));
                                postCurrentYearResult = postCurrentYearResult.add(FastUtils.Null2Zero(balanceSubject.getPostTotalDebitAmount()).subtract(FastUtils.Null2Zero(balanceSubject.getPostSyTotalDebitAmount()))).
                                        subtract(FastUtils.Null2Zero(balanceSubject.getPostTotalCreditAmount()).subtract(FastUtils.Null2Zero(balanceSubject.getPostSyTotalCreditAmount())));
                            }
                            //科目id
                            incomeStatementsVo.setAccountSubjectId(balanceSubject.getAccountSubjectId());
                            //科目code
                            incomeStatementsVo.setAccountSubjectCode(accountSubjectVo.getCode());
                            //科目name
                            incomeStatementsVo.setAccountSubjectName(accountSubjectVo.getName());
                            //科目full-name
                            incomeStatementsVo.setAccountSubjectFullName(accountSubjectVo.getFullName());
                            //科目余额方向
                            incomeStatementsVo.setBalanceDirection(accountSubjectVo.getBalanceDirection());
                            //本期金额
                            incomeStatementsVo.setCurrentMoney(currentResult);
                            //本期累计
                            incomeStatementsVo.setTotalCurrentMoney(currentYearResult);
                            //已过账本期金额
                            incomeStatementsVo.setPostCurrentMoney(postCurrentResult);
                            //已过账本期累计
                            incomeStatementsVo.setPostTotalCurrentMoney(postCurrentYearResult);
                        }
                    }
                }
                if (incomeStatementsVo != null) {
                    incomeStatementsVoList.add(incomeStatementsVo);
                }
            }
        }

        return incomeStatementsVoList;
    }

    /**
     * @param mapType map转换类型,posting 是否包含未过账, incomeStatementsVoList 科目余额数据
     * @return java.util.Map<java.lang.String, java.math.BigDecimal>
     * @description: 科目余额金额计算
     * @author LuoY
     * @date 2019/8/6 15:53
     */
    private Map<String, String> subjectDataToMap(Byte mapType, Byte posting, List<IncomeStatementsVo> incomeStatementsVoList) {
        Map<String, String> balanceSubject = new HashMap<>();
        for (IncomeStatementsVo incomeStatementsVo : incomeStatementsVoList) {
            if (Constant.Is.YES.equals(mapType)) {
                //按本年累计取数
                if (Constant.Is.YES.equals(posting)) {
                    //包含未过账,直接取本年累计
                    balanceSubject.put(incomeStatementsVo.getAccountSubjectCode(), incomeStatementsVo.getBalanceDirection() + LedgerConstant.ExportConstant.SPLIT + FastUtils.Null2Zero(incomeStatementsVo.getTotalCurrentMoney()).toString());
                } else {
                    //不包含未过账,直接取已过账本年累计
                    balanceSubject.put(incomeStatementsVo.getAccountSubjectCode(), incomeStatementsVo.getBalanceDirection() + LedgerConstant.ExportConstant.SPLIT + FastUtils.Null2Zero(incomeStatementsVo.getPostTotalCurrentMoney()).toString());
                }
            } else {
                //按本期取数
                if (Constant.Is.YES.equals(posting)) {
                    //包含未过账,直接取本期
                    balanceSubject.put(incomeStatementsVo.getAccountSubjectCode(), incomeStatementsVo.getBalanceDirection() + LedgerConstant.ExportConstant.SPLIT + FastUtils.Null2Zero(incomeStatementsVo.getCurrentMoney()).toString());
                } else {
                    //不包含未过账,直接取已过账本期
                    balanceSubject.put(incomeStatementsVo.getAccountSubjectCode(), incomeStatementsVo.getBalanceDirection() + LedgerConstant.ExportConstant.SPLIT + incomeStatementsVo.getPostCurrentMoney().toString());
                }
            }
        }
        return balanceSubject;
    }

    /*******************现金流量表**********************/

    /**
     * @param balanceCashFlowVos 现金流量项目余额, cashFlowItemVo 现金流量项目,financialReportItemSetVo 现金流量特殊项
     * @return java.util.Map<java.lang.String, java.math.BigDecimal>
     * @description: 现金流量项目余额整合
     * @author LuoY
     * @date 2019/8/9 15:05
     */
    private List<CashFlowReportVo> cashFlowDataHandle(@NotNull List<BalanceCashFlowVo> balanceCashFlowVos, @NotNull List<CashFlowItemVo> cashFlowItemVos, IncomeStatementsAccountBookVo incomeStatementsAccountBookVo, IncomeStatementsAccountEntityVo incomeStatementsAccountEntityVo, CashFlowReportDto cashFlowReportDto, FinancialReportItemSetVo financialReportItemSetVo) {
        List<CashFlowReportVo> cashFlowReportVos = new LinkedList<>();
        Map<String, List<AccountSubjectVo>> map = new HashMap<>();
        for (BalanceCashFlowVo balanceCashFlowVo : balanceCashFlowVos) {
            for (CashFlowItemVo cashFlowItemVo : cashFlowItemVos) {
                if (cashFlowItemVo.getId().equals(balanceCashFlowVo.getItemId())) {
                    CashFlowReportVo cashFlowReportVo = new CashFlowReportVo();
                    //项目编码
                    cashFlowReportVo.setCode(cashFlowItemVo.getCode());
                    //本期发生额
                    cashFlowReportVo.setOccurAmount(FastUtils.Null2Zero(balanceCashFlowVo.getOccurAmount()));
                    //已过账本期发生额
                    cashFlowReportVo.setPostOccurAmount(FastUtils.Null2Zero(balanceCashFlowVo.getPostOccurAmount()));
                    //本年累计
                    cashFlowReportVo.setTotalAmount(FastUtils.Null2Zero(balanceCashFlowVo.getTotalAmount()));
                    //已过账本年累计
                    cashFlowReportVo.setPostTotalAmount(FastUtils.Null2Zero(balanceCashFlowVo.getPostTotalAmount()));
                    //现金流量方向
                    cashFlowReportVo.setCashFlowDirection(cashFlowItemVo.getCashFlowDirection());
                    cashFlowReportVos.add(cashFlowReportVo);
                }
            }
        }
        //处理现金流量特殊项
        if (!StringUtil.isBlank(financialReportItemSetVo)) {
            //获取指科目code的末级科目id
            AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
            // 公式中所有科目的Code
            List<String> codes = new ArrayList<>();
            for (FinancialReportItemFormulaVo financialReportItemFormulaVo : financialReportItemSetVo.getFinancialReportItemFormulaVoList()) {
                codes.add(financialReportItemFormulaVo.getFormulaItemCode());
            }
            accountSubjectDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
            accountSubjectDto.setIsFinal(Constant.Is.YES);
            accountSubjectDto.setCodes(codes);
            // 查询所有科目的末级科目ID、Code
            List<AccountSubjectVo> accountSubjectVos = accountSubjectFeignClient.findAllChildInfo(accountSubjectDto).getData();
            List<AccountSubjectVo> accountSubjectVoList;
            for (FinancialReportItemFormulaVo financialReportItemFormulaVo : financialReportItemSetVo.getFinancialReportItemFormulaVoList()) {
                accountSubjectVoList = new ArrayList<>();
                for(AccountSubjectVo accountSubjectVo: accountSubjectVos){
                    if(accountSubjectVo.getCode().startsWith(financialReportItemFormulaVo.getFormulaItemCode())){
                        accountSubjectVoList.add(accountSubjectVo);
                    }
                }
                map.put(financialReportItemFormulaVo.getFormulaItemCode(), accountSubjectVoList);
            }

            //设置科目余额查询条件
            BalanceSubjectQueryDto balanceSubjectQueryDto = new BalanceSubjectQueryDto();
            balanceSubjectQueryDto.setAccountBookId(incomeStatementsAccountBookVo.getAccountBookId());
            if (!LedgerConstant.FinancialString.ACCOUNTBOOKID.equals(incomeStatementsAccountEntityVo.getAccountBookEntityId())) {
                balanceSubjectQueryDto.setAccountBookEntityId(incomeStatementsAccountEntityVo.getAccountBookEntityId());
            }
            balanceSubjectQueryDto.setPeriodYear(cashFlowReportDto.getPeriodYear());

            for (FinancialReportItemFormulaVo financialReportItemFormulaVo : financialReportItemSetVo.getFinancialReportItemFormulaVoList()) {
                if (!StringUtil.isBlank(financialReportItemFormulaVo.getId())) {
                    //循环现金流量特殊项公式
                    List<Long> subjectIds = new LinkedList<>();
                    CashFlowReportVo cashFlowReportVo;
                    Map<Long, Byte> accountDirection = new HashMap<>();
                    //获取指定code的所有末级科目idList
                    for (AccountSubjectVo accountSubjectVo : map.get(financialReportItemFormulaVo.getFormulaItemCode())) {
                        subjectIds.add(accountSubjectVo.getId());
                        accountDirection.put(accountSubjectVo.getId(), accountSubjectVo.getBalanceDirection());
                    }
                    if (!FastUtils.checkNullOrEmpty(subjectIds)) {
                        //如果有末级科目ids就查询指定科目的期初年初
                        //查询本期期初
                        balanceSubjectQueryDto.setSubjectIds(subjectIds);
                        balanceSubjectQueryDto.setPeriodNum(cashFlowReportDto.getPeriodNum());
                        List<BalanceSubject> openBalance = balanceSubjectService.findBalanceSubjectInfoBySubjectId(balanceSubjectQueryDto);
                        //查询本年年初（第一期间期初为年初）
                        balanceSubjectQueryDto.setPeriodNum(Constant.PeriodNum.ZERO);
                        balanceSubjectQueryDto.setSubjectIds(subjectIds);
                        List<BalanceSubject> yearOpenBalance = balanceSubjectService.findBalanceSubjectInfoBySubjectId(balanceSubjectQueryDto);
                        if (CollectionUtils.isEmpty(yearOpenBalance)){
                            //如果0期没有值，则取1期
                            balanceSubjectQueryDto.setPeriodNum(Constant.PeriodNum.January);
                            yearOpenBalance = balanceSubjectService.findBalanceSubjectInfoBySubjectId(balanceSubjectQueryDto);
                        }
                        //期初MAP
                        Map<String, String> openBalanceMap = openBalanceMap(openBalance, accountDirection, financialReportItemFormulaVo.getFormulaItemCode());
                        //年初MAP
                        Map<String, String> yearOpenBalanceMap = openBalanceMap(yearOpenBalance, accountDirection, financialReportItemFormulaVo.getFormulaItemCode());
                        cashFlowReportVo = new CashFlowReportVo();
                        cashFlowReportVo.setCode(financialReportItemFormulaVo.getFormulaItemCode());
                        cashFlowReportVo.setOccurAmount(FastUtils.Null2Zero(getSubjectMoney(openBalanceMap, financialReportItemFormulaVo.getFormulaItemCode(), financialReportItemFormulaVo.getFormulaItemDirection())));
                        cashFlowReportVo.setPostOccurAmount(FastUtils.Null2Zero(getSubjectMoney(openBalanceMap, financialReportItemFormulaVo.getFormulaItemCode(), financialReportItemFormulaVo.getFormulaItemDirection())));
                        cashFlowReportVo.setTotalAmount(FastUtils.Null2Zero(getSubjectMoney(yearOpenBalanceMap, financialReportItemFormulaVo.getFormulaItemCode(), financialReportItemFormulaVo.getFormulaItemDirection())));
                        cashFlowReportVo.setPostTotalAmount(FastUtils.Null2Zero(getSubjectMoney(yearOpenBalanceMap, financialReportItemFormulaVo.getFormulaItemCode(), financialReportItemFormulaVo.getFormulaItemDirection())));
                        cashFlowReportVos.add(cashFlowReportVo);
                    }
                }
            }
        }

        return cashFlowReportVos;
    }

    /**
     * @param mapType map转换类型,posting 是否包含未过账, incomeStatementsVoList 科目余额数据
     * @return java.util.Map<java.lang.String, java.math.BigDecimal>
     * @description: 科目余额金额计算
     * @author LuoY
     * @date 2019/8/6 15:53
     */
    private Map<String, String> cashFlowDataToMap(Byte mapType, Byte posting, List<CashFlowReportVo> cashFlowReportVos) {
        Map<String, String> balanceSubject = new HashMap<>();
        for (CashFlowReportVo cashFlowReportVo : cashFlowReportVos) {
            if (Constant.Is.YES.equals(mapType)) {
                //按本年累计取数
                if (Constant.Is.YES.equals(posting)) {
                    //包含未过账,直接去本年累计(现金流量),现金流量项目如果一级没有方向,默认为流入
                    balanceSubject.put(cashFlowReportVo.getCode(), (Constant.BalanceDirection.FLAT.equals(cashFlowReportVo.getCashFlowDirection()) ? Constant.BalanceDirection.CREDIT.toString() : cashFlowReportVo.getCashFlowDirection())
                            + LedgerConstant.ExportConstant.SPLIT + FastUtils.Null2Zero(cashFlowReportVo.getTotalAmount()).toString());
                } else {
                    //不包含未过账,直接去已过账本年累计
                    balanceSubject.put(cashFlowReportVo.getCode(), (Constant.BalanceDirection.FLAT.equals(cashFlowReportVo.getCashFlowDirection()) ? Constant.BalanceDirection.CREDIT.toString() : cashFlowReportVo.getCashFlowDirection())
                            + LedgerConstant.ExportConstant.SPLIT + FastUtils.Null2Zero(cashFlowReportVo.getPostTotalAmount()).toString());
                }
            } else {
                //按本期取数
                if (Constant.Is.YES.equals(posting)) {
                    //包含未过账,直接取本期
                    balanceSubject.put(cashFlowReportVo.getCode(), (Constant.BalanceDirection.FLAT.equals(cashFlowReportVo.getCashFlowDirection()) ? Constant.BalanceDirection.CREDIT.toString() : cashFlowReportVo.getCashFlowDirection())
                            + LedgerConstant.ExportConstant.SPLIT + FastUtils.Null2Zero(cashFlowReportVo.getOccurAmount()));
                } else {
                    //不包含未过账,直接取已过账本期
                    balanceSubject.put(cashFlowReportVo.getCode(), (Constant.BalanceDirection.FLAT.equals(cashFlowReportVo.getCashFlowDirection()) ? Constant.BalanceDirection.CREDIT.toString() : cashFlowReportVo.getCashFlowDirection())
                            + LedgerConstant.ExportConstant.SPLIT + FastUtils.Null2Zero(cashFlowReportVo.getPostOccurAmount()));
                }
            }
        }
        return balanceSubject;
    }

    /**
     * @return java.util.List<com.njwd.entity.ledger.vo.BalanceCashFlowVo> 现金流量项目余额
     * @description: 计算当前期间现金流量项目余额
     * @Param [incomeStatementsAccountBookVo, balanceCashFlowDto]
     * @author LuoY
     * @date 2019/8/16 15:20
     */
    private List<BalanceCashFlowVo> calculationCashFlowVo(BalanceCashFlowDto balanceCashFlowDto) {
        balanceCashFlowDto.setLastPeriod(balanceCashFlowDto.getPeriodNum());
        //查询本期
        List<BalanceCashFlowVo> current = balanceCashFlowService.findBalanceCashFlowByCondition(balanceCashFlowDto);
        balanceCashFlowDto.setLastPeriod(Constant.PeriodNum.January);
        //查询本年合计
        List<BalanceCashFlowVo> currentYear = balanceCashFlowService.findBalanceCashFlowByCondition(balanceCashFlowDto);
        for (BalanceCashFlowVo balanceCashFlowVo : currentYear) {
            boolean isHas = false;
            for (BalanceCashFlowVo BalanceCashFlowVo1 : current) {
                if (balanceCashFlowVo.getItemId().equals(BalanceCashFlowVo1.getItemId())) {
                    balanceCashFlowVo.setOccurAmount(BalanceCashFlowVo1.getOccurAmount());
                    balanceCashFlowVo.setPostOccurAmount(BalanceCashFlowVo1.getPostOccurAmount());
                    isHas = true;
                }
            }
            if (!isHas) {
                balanceCashFlowVo.setOccurAmount(BigDecimal.ZERO);
                balanceCashFlowVo.setPostOccurAmount(BigDecimal.ZERO);
            }
        }
        return currentYear;
    }

    /*******************导出*******************/
    /**
     * @return java.lang.String[]
     * @description: 处理财务报告(利润表)
     * @Param [incomeStatementsAccountBookVoList]
     * @author LuoY
     * @date 2019/9/2 17:53
     */
    private List<String[]> financialDataHandle(@NotNull List<IncomeStatementsAccountBookVo> incomeStatementsAccountBookVoList, IncomeStatementsReportDto incomeStatementsReportDto) {
        List<IncomeStatementsAccountBookVo> accountBookVos = incomeStatementsAccountBookVoList;
        List<String[]> financialReportData = new LinkedList<>();
        List<String> financialReport = new LinkedList<>();
        List<IncomeStatementsAccountEntityVo> incomeStatementsAccountEntityVos;
        List<IncomeStatementsTableVo> incomeStatementsTableVos;
        int reportNum = Constant.Number.ONE;
        String data;
        String[] head = financialDataHead(accountBookVos, incomeStatementsReportDto);
        financialReportData.add(Constant.Number.ZERO, head);
        for (int i = 0; i < accountBookVos.size(); i++) {
            incomeStatementsAccountEntityVos = accountBookVos.get(i).getIncomeStatementsAccountEntityVos();
            for (int j = 0; j < incomeStatementsAccountEntityVos.size(); j++) {
                incomeStatementsTableVos = incomeStatementsAccountEntityVos.get(j).getIncomeStatementsTablesListVos();
                //定义财务报告项行号
                int itemNum = Constant.Number.ONE;
                int subjectItemNum = Constant.Number.ONE;
                Map<String, String> upCodeLineNum = new HashMap<>();
                for (int k = 0; k < incomeStatementsTableVos.size(); k++) {
                    if (Constant.Number.ZERO.equals(i) && Constant.Number.ZERO.equals(j)) {
                        //财务报告不带科目明细
                        data = "";
                        //第一个核算账簿,核算主体,创建字符串
                        //项目名称
                        data += projectNameHandle(incomeStatementsTableVos.get(k), reportNum) + LedgerConstant.ExportConstant.SPLIT;
                        //行号
                        if (Constant.Is.YES.equals(incomeStatementsReportDto.getSubjectDetail())) {
                            //如果查询了科目明细项,处理科目明细
                            if (Constant.Is.YES.equals(incomeStatementsTableVos.get(k).getIsSubject())) {
                                //获取上级code
                                String Upcode = upCodeLineNum.get(String.valueOf(incomeStatementsTableVos.get(k).getLevel().intValue() - Constant.Number.ONE));
                                //获取当前code
                                String currentCode = upCodeLineNum.get(String.valueOf(incomeStatementsTableVos.get(k).getLevel().intValue()));
                                String line;
                                //如果上级code为空
                                if (StringUtil.isEmpty(Upcode)) {
                                    Upcode = String.valueOf(itemNum - Constant.Number.ONE);
                                    if (StringUtil.isEmpty(currentCode)) {
                                        //如果当前code为空，从1开始
                                        subjectItemNum = Constant.Number.ONE;
                                    } else {
                                        //如果当前code不为空，获取当前级次code+1
                                        String currentLineNum = currentCode.substring(currentCode.length() - Constant.Number.TWO, currentCode.length());
                                        subjectItemNum = Integer.valueOf(currentLineNum) + Constant.Number.ONE;
                                    }
                                } else {
                                    //如果上级code不为空
                                    if (StringUtil.isEmpty(currentCode)) {
                                        //当前级次为空
                                        subjectItemNum = Constant.Number.ONE;
                                    } else {
                                        //当前级次不为空
                                        String currentLineNum = currentCode.substring(currentCode.length() - Constant.Number.TWO, currentCode.length());
                                        subjectItemNum = Integer.valueOf(currentLineNum) + Constant.Number.ONE;
                                        if (!currentCode.substring(Constant.Number.ZERO, currentCode.length() - 2).equals(Upcode)) {
                                            //如果当前级次不是上级code开头
                                            subjectItemNum = Constant.Number.ONE;
                                        }
                                    }
                                }
                                //拼行号
                                if (subjectItemNum > Constant.Number.NINE) {
                                    line = Upcode + subjectItemNum;
                                } else {
                                    line = Upcode + Constant.Number.ZERO + subjectItemNum;
                                }
                                data += line + LedgerConstant.ExportConstant.SPLIT;
                                upCodeLineNum.put(incomeStatementsTableVos.get(k).getLevel().toString(), line);
                                subjectItemNum++;
                            } else {
                                //不是报科目明细项,行号叠加
                                data += itemNum + LedgerConstant.ExportConstant.SPLIT;
                                subjectItemNum = Constant.Number.ONE;
                                upCodeLineNum = new HashMap<>();
                                itemNum++;
                            }
                        } else {
                            //如果没查科目明细,正常处理
                            data += itemNum + LedgerConstant.ExportConstant.SPLIT;
                            itemNum++;
                        }

                        //拼接核算主体金额信息
                        data += appendFinancialReportData(incomeStatementsTableVos.get(k), incomeStatementsReportDto);
                        if (LedgerConstant.FinancialReportItemLevel.LEVEL_ONE.equals(incomeStatementsTableVos.get(k).getLevel())) {
                            reportNum++;
                        }
                        financialReport.add(data);
                    } else {
                        //不是第一个核算账簿直接拼金额字符串
                        data = financialReport.get(k);
                        data += appendFinancialReportData(incomeStatementsTableVos.get(k), incomeStatementsReportDto);
                        financialReport.remove(k);
                        financialReport.add(k, data);
                    }
                }
            }
        }
        financialReportData.addAll(stringToArray(financialReport));
        return financialReportData;
    }

    /**
     * @return java.util.List<java.lang.String [ ]>
     * @description: 处理财务报告(现金流量表)
     * @Param [incomeStatementsAccountBookVoList, cashFlowReportDto]
     * @author LuoY
     * @date 2019/9/4 14:00
     */
    private List<String[]> financialCashFlowDataHandle(@NotNull List<IncomeStatementsAccountBookVo> incomeStatementsAccountBookVoList) {
        List<String[]> financialReportData = new LinkedList<>();
        List<String> financialReport = new LinkedList<>();
        List<IncomeStatementsAccountEntityVo> incomeStatementsAccountEntityVos;
        List<CashFlowReportTabletVo> cashFlowReportTabletVos;
        int reportNum = Constant.Number.ONE;
        String data;
        //处理标题
        String[] head = financialCashFloDataHead(incomeStatementsAccountBookVoList);
        financialReportData.add(Constant.Number.ZERO, head);
        for (int i = 0; i < incomeStatementsAccountBookVoList.size(); i++) {
            incomeStatementsAccountEntityVos = incomeStatementsAccountBookVoList.get(i).getIncomeStatementsAccountEntityVos();
            for (int j = 0; j < incomeStatementsAccountEntityVos.size(); j++) {
                cashFlowReportTabletVos = incomeStatementsAccountEntityVos.get(j).getCashFlowReportTabletVos();
                int itemNum = 0;
                for (int k = 0; k < cashFlowReportTabletVos.size(); k++) {
                    if (Constant.Number.ZERO.equals(i) && Constant.Number.ZERO.equals(j)) {
                        data = "";
                        //第一个核算账簿,核算主体,创建字符串
                        //项目名称
                        data += projectNameCashFlowHandle(cashFlowReportTabletVos.get(k), reportNum) + LedgerConstant.ExportConstant.SPLIT;
                        //行号
                        data += (itemNum + 1) + LedgerConstant.ExportConstant.SPLIT;
                        itemNum++;
                        //拼接核算主体信息
                        data += appendFinancialCashFlowReportData(cashFlowReportTabletVos.get(k));
                        if (LedgerConstant.FinancialReportItemLevel.LEVEL_ONE.equals(cashFlowReportTabletVos.get(k).getLevel())) {
                            reportNum++;
                        }
                        financialReport.add(data);
                    } else {
                        //不是第一个核算账簿直接拼金额字符串
                        data = financialReport.get(k);
                        data += appendFinancialCashFlowReportData(cashFlowReportTabletVos.get(k));
                        financialReport.remove(k);
                        financialReport.add(k, data);
                    }
                }
            }
        }
        financialReportData.addAll(stringToArray(financialReport));
        return financialReportData;
    }

    /**
     * @return java.lang.String
     * @description: 报告名称处理（利润表）
     * @Param [incomeStatementsTableVo]
     * @author LuoY
     * @date 2019/9/3 17:52
     */
    private String projectNameHandle(IncomeStatementsTableVo incomeStatementsTableVo, int reportNum) {
        String projectName;
        String blank = LedgerConstant.ExportConstant.BLANK;

        //拼增减标识
        if (Constant.Is.YES.equals(incomeStatementsTableVo.getIsAdd())) {
            projectName = LedgerConstant.ExportConstant.ADD + incomeStatementsTableVo.getProjectName();
        } else if (Constant.Is.NO.equals(incomeStatementsTableVo.getIsAdd())) {
            projectName = LedgerConstant.ExportConstant.SUB + incomeStatementsTableVo.getProjectName();
        } else {
            projectName = incomeStatementsTableVo.getProjectName();
        }

        //拼中文汉字
        if (LedgerConstant.FinancialReportItemLevel.LEVEL_ONE.equals(incomeStatementsTableVo.getLevel())) {
            String num = FastUtils.int2chineseNum(reportNum);
            projectName = num + LedgerConstant.ExportConstant.APPEND + projectName;
        }

        //拼缩进
        for (int i = 0; i < incomeStatementsTableVo.getLevel().intValue(); i++) {
            blank += blank;
        }
        projectName = blank + projectName;
        return projectName;
    }

    /**
     * @return java.lang.String
     * @description: 报告名称处理(现金流量表)
     * @Param [cashFlowReportTabletVo, reportNum]
     * @author LuoY
     * @date 2019/9/4 14:00
     */
    private String projectNameCashFlowHandle(CashFlowReportTabletVo cashFlowReportTabletVo, int reportNum) {
        String projectName;
        String blank = LedgerConstant.ExportConstant.BLANK;
        //拼增减标识
        if (Constant.Is.YES.equals(cashFlowReportTabletVo.getIsAdd())) {
            projectName = LedgerConstant.ExportConstant.ADD + cashFlowReportTabletVo.getProjectName();
        } else if (Constant.Is.NO.equals(cashFlowReportTabletVo.getIsAdd())) {
            projectName = LedgerConstant.ExportConstant.SUB + cashFlowReportTabletVo.getProjectName();
        } else {
            projectName = cashFlowReportTabletVo.getProjectName();
        }

        //拼中文汉字
        if (LedgerConstant.FinancialReportItemLevel.LEVEL_ONE.equals(cashFlowReportTabletVo.getLevel())) {
            String num = FastUtils.int2chineseNum(reportNum);
            projectName = num + LedgerConstant.ExportConstant.APPEND + projectName;
        }

        //拼缩进
        for (int i = 1; i < cashFlowReportTabletVo.getLevel().intValue(); i++) {
            blank += blank;
        }
        projectName = blank + projectName;
        return projectName;
    }

    /**
     * @return java.lang.String[]
     * @description: 数据处理(利润表)
     * @Param [incomeStatementsAccountBookVoList, incomeStatementsReportDto]
     * @author LuoY
     * @date 2019/9/3 15:49
     */
    private String[] financialDataHead(@NotNull List<IncomeStatementsAccountBookVo> incomeStatementsAccountBookVoList, IncomeStatementsReportDto incomeStatementsReportDto) {
        String head = LedgerConstant.ExportConstant.ITEM + LedgerConstant.ExportConstant.SPLIT + LedgerConstant.ExportConstant.LINE + LedgerConstant.ExportConstant.SPLIT;
        for (IncomeStatementsAccountBookVo incomeStatementsAccountBookVo : incomeStatementsAccountBookVoList) {
            for (IncomeStatementsAccountEntityVo incomeStatementsAccountEntityVo : incomeStatementsAccountBookVo.getIncomeStatementsAccountEntityVos()) {
                //本期
                head += LedgerConstant.ExportConstant.CURRENTMONEY + LedgerConstant.ExportConstant.SPLIT;
                //本期占收入比
                if (Constant.Is.YES.equals(incomeStatementsReportDto.getIncomeRatio())) {
                    head += LedgerConstant.ExportConstant.INCOMERATION + LedgerConstant.ExportConstant.SPLIT;
                }
                //本年累计
                if (Constant.Is.YES.equals(incomeStatementsReportDto.getYearCumulative())) {
                    head += LedgerConstant.ExportConstant.YEARCUMULATIVE + LedgerConstant.ExportConstant.SPLIT;
                    //本年累计占收入比
                    if (Constant.Is.YES.equals(incomeStatementsReportDto.getIncomeRatio())) {
                        head += LedgerConstant.ExportConstant.INCOMERATION + LedgerConstant.ExportConstant.SPLIT;
                    }
                }
                //同比
                if (Constant.Is.YES.equals(incomeStatementsReportDto.getYearOnYear())) {
                    head += LedgerConstant.ExportConstant.YEARCOMPARE + LedgerConstant.ExportConstant.SPLIT;
                    //同比占收入比
                    if (Constant.Is.YES.equals(incomeStatementsReportDto.getIncomeRatio())) {
                        head += LedgerConstant.ExportConstant.INCOMERATION + LedgerConstant.ExportConstant.SPLIT;
                    }
                }
                //环比
                if (Constant.Is.YES.equals(incomeStatementsReportDto.getMonthOnMonth())) {
                    head += LedgerConstant.ExportConstant.MONTHCOMPARE + LedgerConstant.ExportConstant.SPLIT;
                    //环比占收入比
                    if (Constant.Is.YES.equals(incomeStatementsReportDto.getIncomeRatio())) {
                        head += LedgerConstant.ExportConstant.INCOMERATION + LedgerConstant.ExportConstant.SPLIT;
                    }
                }
                //增长率
                if (Constant.Is.YES.equals(incomeStatementsReportDto.getGrowRate())) {
                    head += LedgerConstant.ExportConstant.GROWRATE + LedgerConstant.ExportConstant.SPLIT;
                }
            }
        }
        head = head.substring(Constant.Number.ZERO, head.length() - Constant.Number.ONE);
        String[] heads = head.split(LedgerConstant.ExportConstant.SPLIT);
        return heads;
    }

    /**
     * @return java.lang.String[]
     * @description: 数据处理(现金流量表)
     * @Param [incomeStatementsAccountBookVoList]
     * @author LuoY
     * @date 2019/9/4 14:01
     */
    private String[] financialCashFloDataHead(@NotNull List<IncomeStatementsAccountBookVo> incomeStatementsAccountBookVoList) {
        String head = LedgerConstant.ExportConstant.ITEM + LedgerConstant.ExportConstant.SPLIT + LedgerConstant.ExportConstant.LINE + LedgerConstant.ExportConstant.SPLIT;
        for (IncomeStatementsAccountBookVo incomeStatementsAccountBookVo : incomeStatementsAccountBookVoList) {
            for (IncomeStatementsAccountEntityVo incomeStatementsAccountEntityVo : incomeStatementsAccountBookVo.getIncomeStatementsAccountEntityVos()) {
                //本期
                head += LedgerConstant.ExportConstant.CURRENTMONEY + LedgerConstant.ExportConstant.SPLIT;
                //本年累计
                head += LedgerConstant.ExportConstant.YEARCUMULATIVE + LedgerConstant.ExportConstant.SPLIT;
            }
        }
        //去掉末尾分隔符
        head = head.substring(Constant.Number.ZERO, head.length() - Constant.Number.ONE);
        String[] heads = head.split(LedgerConstant.ExportConstant.SPLIT);
        return heads;
    }

    /**
     * @return java.util.List<com.njwd.entity.ledger.FinancialReport>
     * @description: 财务报告处理
     * @Param [incomeStatementsAccountBookVoList, incomeStatementsReportDto]
     * @author LuoY
     * @date 2019/9/3 15:40
     */
    private List<FinancialReport> financialReportsHandle(@NotNull List<IncomeStatementsAccountBookVo> incomeStatementsAccountBookVoList, int singleEntityCells) {
        List<FinancialReport> financialReports = new LinkedList<>();
        List<AccountBookEntityVo> accountBookEntityVos;
        FinancialReport financialReport;
        //是否多账簿
        Boolean isSingleAccountBook = false;
        for (IncomeStatementsAccountBookVo incomeStatementsAccountBookVo : incomeStatementsAccountBookVoList) {
            financialReport = new FinancialReport();
            financialReport.setAccountBookName(incomeStatementsAccountBookVo.getAccountBookName());
            int entitys = Constant.Number.ZERO;
            accountBookEntityVos = new LinkedList<>();
            for (IncomeStatementsAccountEntityVo incomeStatementsAccountEntityVo : incomeStatementsAccountBookVo.getIncomeStatementsAccountEntityVos()) {
                if (Constant.Number.ONE.equals(incomeStatementsAccountBookVoList.size()) && Constant.Number.ONE.equals(incomeStatementsAccountBookVo.getIncomeStatementsAccountEntityVos().size())) {
                    //单账簿最多一个核算主体
                    if (!StringUtil.isBlank(incomeStatementsAccountEntityVo.getAccountBookEntityId())) {
                        //单账簿单核算主体
                        AccountBookEntityVo accountBookEntityVo = new AccountBookEntityVo();
                        accountBookEntityVo.setEntityName(incomeStatementsAccountEntityVo.getAccountBookEntityName());
                        //单核算主体的情况下,列数+2(项目+行次)
                        accountBookEntityVo.setColumnNum(singleEntityCells + Constant.Number.TWO);
                        accountBookEntityVos.add(accountBookEntityVo);
                        entitys = Constant.Number.ONE;
                        isSingleAccountBook = true;
                    } else if (StringUtil.isBlank(incomeStatementsAccountEntityVo.getAccountBookEntityId())) {
                        //单账簿不带核算主体
                        entitys = Constant.Number.ONE;
                        isSingleAccountBook = true;
                    }
                } else {
                    if (!StringUtil.isBlank(incomeStatementsAccountEntityVo.getAccountBookEntityId())) {
                        //多账簿多核算主体或单账簿多核算主体
                        AccountBookEntityVo accountBookEntityVo = new AccountBookEntityVo();
                        accountBookEntityVo.setEntityName(incomeStatementsAccountEntityVo.getAccountBookEntityName());
                        accountBookEntityVo.setColumnNum(singleEntityCells);
                        accountBookEntityVos.add(accountBookEntityVo);
                        entitys++;
                    } else if (StringUtil.isBlank(incomeStatementsAccountEntityVo.getAccountBookEntityId()) && Constant.Number.ONE.equals(incomeStatementsAccountBookVo.getIncomeStatementsAccountEntityVos().size())) {
                        //多账簿不带核算主体
                        entitys++;
                    }
                }

            }
            financialReport.setAccountBookEntityVoList(accountBookEntityVos);
            if (isSingleAccountBook) {
                //单账簿加两列(项目和行次)
                financialReport.setColumnNum(singleEntityCells * entitys + Constant.Number.TWO);
            } else {
                //多账簿(单个核算主体列数*核算主体个数)
                financialReport.setColumnNum(singleEntityCells * entitys);
            }
            financialReports.add(financialReport);
        }
        return financialReports;
    }

    /**
     * @return void
     * @description: 利润表字符串处理
     * @Param [data, incomeStatementsTableVo, incomeStatementsReportDto]
     * @author LuoY
     * @date 2019/9/3 10:54
     */
    private String appendFinancialReportData(IncomeStatementsTableVo incomeStatementsTableVo, IncomeStatementsReportDto incomeStatementsReportDto) {
        String data = "";
        //本期金额
        data += FastUtils.Null2Zero(incomeStatementsTableVo.getCurrentMoney()).compareTo(BigDecimal.ZERO) == Constant.Number.ZERO ? BigDecimal.ZERO + LedgerConstant.ExportConstant.SPLIT :
                incomeStatementsTableVo.getCurrentMoney() + LedgerConstant.ExportConstant.SPLIT;
        if (Constant.Is.YES.equals(incomeStatementsReportDto.getIncomeRatio())) {
            //如果需要计算占收入比
            data += FastUtils.Null2Zero(incomeStatementsTableVo.getCurrentIncomeRatio()).compareTo(BigDecimal.ZERO) == Constant.Number.ZERO ? BigDecimal.ZERO + "%," : incomeStatementsTableVo.getCurrentIncomeRatio() + "%,";
        }
        //本年累计
        if (Constant.Is.YES.equals(incomeStatementsReportDto.getYearCumulative())) {
            data += FastUtils.Null2Zero(incomeStatementsTableVo.getYearCumulative()).compareTo(BigDecimal.ZERO) == Constant.Number.ZERO ? BigDecimal.ZERO + LedgerConstant.ExportConstant.SPLIT :
                    incomeStatementsTableVo.getYearCumulative() + LedgerConstant.ExportConstant.SPLIT;
            if (Constant.Is.YES.equals(incomeStatementsReportDto.getIncomeRatio())) {
                //如果需要计算占收入比
                data += FastUtils.Null2Zero(incomeStatementsTableVo.getYearIncomeRatio()).compareTo(BigDecimal.ZERO) == Constant.Number.ZERO ? BigDecimal.ZERO + "%," : incomeStatementsTableVo.getYearIncomeRatio() + "%,";
            }
        }
        //同比
        if (Constant.Is.YES.equals(incomeStatementsReportDto.getYearOnYear())) {
            data += FastUtils.Null2Zero(incomeStatementsTableVo.getYearOnYear()).compareTo(BigDecimal.ZERO) == Constant.Number.ZERO ? BigDecimal.ZERO + LedgerConstant.ExportConstant.SPLIT :
                    incomeStatementsTableVo.getYearOnYear() + LedgerConstant.ExportConstant.SPLIT;
            if (Constant.Is.YES.equals(incomeStatementsReportDto.getIncomeRatio())) {
                //如果需要计算占收入比
                data += FastUtils.Null2Zero(incomeStatementsTableVo.getYearCompareIncomeRatio()).compareTo(BigDecimal.ZERO) == Constant.Number.ZERO ? BigDecimal.ZERO + "%," : incomeStatementsTableVo.getYearCompareIncomeRatio() + "%,";
            }
        }
        //环比
        if (Constant.Is.YES.equals(incomeStatementsReportDto.getMonthOnMonth())) {
            data += FastUtils.Null2Zero(incomeStatementsTableVo.getMonthOnMonth()).compareTo(BigDecimal.ZERO) == Constant.Number.ZERO ? BigDecimal.ZERO + LedgerConstant.ExportConstant.SPLIT :
                    incomeStatementsTableVo.getMonthOnMonth() + LedgerConstant.ExportConstant.SPLIT;
            if (Constant.Is.YES.equals(incomeStatementsReportDto.getIncomeRatio())) {
                //如果需要计算占收入比
                data += FastUtils.Null2Zero(incomeStatementsTableVo.getMonthCompareIncomeRatio()).compareTo(BigDecimal.ZERO) == Constant.Number.ZERO ? BigDecimal.ZERO + "%," : incomeStatementsTableVo.getMonthCompareIncomeRatio() + "%,";
            }
        }
        //增长率
        if (Constant.Is.YES.equals(incomeStatementsReportDto.getGrowRate())) {
            data += FastUtils.Null2Zero(incomeStatementsTableVo.getGrowRate()).compareTo(BigDecimal.ZERO) == Constant.Number.ZERO ? BigDecimal.ZERO + "%," : incomeStatementsTableVo.getGrowRate() + "%,";
        }
        return data;
    }

    /**
     * @return java.lang.String
     * @description: 现金流量表字符串处理
     * @Param [cashFlowReportTabletVo]
     * @author LuoY
     * @date 2019/9/4 14:02
     */
    private String appendFinancialCashFlowReportData(CashFlowReportTabletVo cashFlowReportTabletVo) {
        String data = "";
        //本期金额
        data += cashFlowReportTabletVo.getCurrentMoney().compareTo(BigDecimal.ZERO) == Constant.Number.ZERO ? BigDecimal.ZERO + LedgerConstant.ExportConstant.SPLIT : cashFlowReportTabletVo.getCurrentMoney() + LedgerConstant.ExportConstant.SPLIT;
        //本年累计
        data += cashFlowReportTabletVo.getYearCumulative().compareTo(BigDecimal.ZERO) == Constant.Number.ZERO ? BigDecimal.ZERO + LedgerConstant.ExportConstant.SPLIT : cashFlowReportTabletVo.getYearCumulative() + LedgerConstant.ExportConstant.SPLIT;
        return data;
    }

    /**
     * @return java.util.List<java.lang.String [ ]>
     * @description: 财务报告字符串转数组
     * @Param [financialReport]
     * @author LuoY
     * @date 2019/9/3 11:02
     */
    private List<String[]> stringToArray(List<String> financialReport) {
        List<String[]> financialReportData = new LinkedList<>();
        for (String data : financialReport) {
            //去掉末尾拼接符
            data = data.substring(Constant.Number.ZERO, data.length() - Constant.Number.ONE);
            //按拼接符拆数组
            String[] financialData = data.split(LedgerConstant.ExportConstant.SPLIT);
            financialReportData.add(financialData);
        }
        return financialReportData;
    }

    /**
     * @return int
     * @description: 计算单个核算主体所占列数
     * @Param [incomeStatementsReportDto]
     * @author LuoY
     * @date 2019/9/2 16:51
     */
    private int singleEntityCells(IncomeStatementsReportDto incomeStatementsReportDto) {
        //默认计算本期,默认一列
        int cells = Constant.Number.ONE;
        //本年累计
        if (Constant.Is.YES.equals(incomeStatementsReportDto.getYearCumulative())) {
            cells++;
        }
        //同比
        if (Constant.Is.YES.equals(incomeStatementsReportDto.getYearOnYear())) {
            cells++;
        }
        //环比
        if (Constant.Is.YES.equals(incomeStatementsReportDto.getMonthOnMonth())) {
            cells++;
        }
        //占收入比
        if (Constant.Is.YES.equals(incomeStatementsReportDto.getIncomeRatio())) {
            cells = cells * 2;
        }
        //增长率
        if (Constant.Is.YES.equals(incomeStatementsReportDto.getGrowRate())) {
            cells++;
        }
        return cells;
    }

    /**
     * @return com.njwd.entity.platform.vo.FinancialReportItemSetVo
     * @description: 获取现金流量特殊项
     * @Param [financialReportItemSetVos]
     * @author LuoY
     * @date 2019/9/17 16:16
     */
    private FinancialReportItemSetVo cashFlowSpecial(List<FinancialReportItemSetVo> financialReportItemSetVos) {
        FinancialReportItemSetVo financialReportItemSetVo = null;
        for (FinancialReportItemSetVo financialReportItemSetVo1 : financialReportItemSetVos) {
            for (FinancialReportItemFormulaVo financialReportItemFormulaVo : financialReportItemSetVo1.getFinancialReportItemFormulaVoList()) {
                if (LedgerConstant.FormulaType.SPECIALCASHFLOW.equals(financialReportItemFormulaVo.getFormulaType())) {
                    financialReportItemSetVo = financialReportItemSetVo1;
                    break;
                }
            }
        }
        return financialReportItemSetVo;
    }

    /**
     * @return java.util.Map<java.lang.String, java.util.List < com.njwd.entity.platform.vo.AccountSubjectVo>>
     * @description: 查询报告公式所有参与计算的科目code对应的所有层级科目
     * @Param [financialReportItemSetDtos, incomeStatementsAccountBookVo]
     * @author LuoY
     * @date 2019/9/21 9:49
     */
    private Map<String, List<AccountSubjectVo>> findCodeAllLevel(List<FinancialReportItemSetVo> financialReportItemSetDtos, IncomeStatementsAccountBookVo incomeStatementsAccountBookVo) {
        Map<String, List<AccountSubjectVo>> subjectDetail = new HashMap<>();
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        // 存放报告项目公式中所有的Code
        List<String> codes = new ArrayList<>();
        for (FinancialReportItemSetVo financialReportItemSetDto : financialReportItemSetDtos) {
            for (FinancialReportItemFormula financialReportItemFormula : financialReportItemSetDto.getFinancialReportItemFormulaVoList()) {
                if (!StringUtil.isBlank(financialReportItemFormula.getId())) {
                    if (LedgerConstant.FormulaType.SUBECTORITEM.equals(financialReportItemFormula.getFormulaType())) {
                        //获取报告项目公式中所有的Code
                        accountSubjectDto.setSubjectId(incomeStatementsAccountBookVo.getSubjectId());
                        codes.add(financialReportItemFormula.getFormulaItemCode());
                    }
                }
            }
        }
        accountSubjectDto.setCodes(codes);
        // 获取项目公式中所有科目的末级科目 ID 、code
        List<AccountSubjectVo> accountSubjects = accountSubjectFeignClient.findAllChildInfo(accountSubjectDto).getData();

        List<AccountSubjectVo> accountSubjectList ;
        // 遍历报告项目
        for (FinancialReportItemSetVo financialReportItemSetDto : financialReportItemSetDtos) {
            accountSubjectList = new ArrayList<>();
            // 遍历项目公式
            for (FinancialReportItemFormula financialReportItemFormula : financialReportItemSetDto.getFinancialReportItemFormulaVoList()) {
                if (!StringUtil.isBlank(financialReportItemFormula.getId())) {
                    if (LedgerConstant.FormulaType.SUBECTORITEM.equals(financialReportItemFormula.getFormulaType())) {
                        // 遍历末级科目
                        for(AccountSubjectVo accountSubjectVo: accountSubjects){
                            if(accountSubjectVo.getCode().startsWith(financialReportItemFormula.getFormulaItemCode())){
                                accountSubjectList.add(accountSubjectVo);
                            }
                        }
                    }
                }
            subjectDetail.put(financialReportItemFormula.getFormulaItemCode(), accountSubjectList);
            }
        }

        return subjectDetail;
    }

    /**
     * @return java.util.Map<java.lang.String, java.util.List < com.njwd.entity.ledger.vo.IncomeStatementsTableVo>>
     * @description: 将科目余额表金额和科目表数据整合
     * @Param [subjectDetail, currentIncomeStatementsVoList, posting]
     * @author LuoY
     * @date 2019/9/21 10:58
     */
    private Map<String, List<IncomeStatementsTableVo>> subjectAllinfoHandle(Map<String, List<AccountSubjectVo>> subjectDetail, List<IncomeStatementsVo> currentIncomeStatementsVoList, Byte posting) {
        Map<String, List<IncomeStatementsTableVo>> subjectDetailMoneyInfo = new HashMap<>();
        //循环code所有级次表
        subjectDetail.forEach((key, value) -> {
            List<IncomeStatementsTableVo> incomeStatementsTableVos1 = new LinkedList<>();
            if (!FastUtils.checkNullOrEmpty(value)) {
                value.forEach(subject -> {
                    //将subjectVo转换为IncomeStatementsTableVo
                    IncomeStatementsTableVo incomeStatementsTableVo1 = new IncomeStatementsTableVo();
                    incomeStatementsTableVo1.setProjectCode(subject.getCode());
                    incomeStatementsTableVo1.setLevel(subject.getLevel());
                    incomeStatementsTableVo1.setProjectName(subject.getName());
                    incomeStatementsTableVo1.setUpProjectCode(subject.getUpCode());
                    incomeStatementsTableVo1.setIsSubject(Constant.Is.YES);
                    incomeStatementsTableVo1.setDirection(subject.getBalanceDirection());
                    //查找对应code的科目余额金额信息
                    currentIncomeStatementsVoList.forEach(data -> {
                        if (data.getAccountSubjectCode().equals(subject.getCode())) {
                            if (Constant.Is.YES.equals(posting)) {
                                //包含未过账,直接取本期
                                incomeStatementsTableVo1.setCurrentMoney(data.getCurrentMoney());
                                incomeStatementsTableVo1.setYearCumulative(data.getTotalCurrentMoney());
                            } else {
                                //不包含未过账,直接取已过账
                                incomeStatementsTableVo1.setCurrentMoney(data.getPostCurrentMoney());
                                incomeStatementsTableVo1.setYearCumulative(data.getPostTotalCurrentMoney());
                            }
                        }
                    });
                    incomeStatementsTableVos1.add(incomeStatementsTableVo1);
                });
            }
            subjectDetailMoneyInfo.put(key, incomeStatementsTableVos1);
        });
        return subjectDetailMoneyInfo;
    }

    /**
     * @return java.util.Map<java.lang.String, java.math.BigDecimal>
     * @description: 将科目余额信息转换为Map
     * @Param [incomeStatementsTableVos]
     * @author LuoY
     * @date 2019/9/21 11:31
     */
    private Map<String, String> incomeDataToMap(List<IncomeStatementsVo> incomeStatementsTableVos, Boolean currentMoney, Byte posting) {
        Map<String, String> incomeData = new HashMap<>();
        incomeStatementsTableVos.forEach(data -> {
            if (currentMoney) {
                //本期金额
                if (Constant.Is.YES.equals(posting)) {
                    //包含未过账
                    incomeData.put(data.getAccountSubjectCode(), data.getBalanceDirection() + LedgerConstant.ExportConstant.SPLIT + data.getCurrentMoney());
                } else {
                    //不包含未过账
                    incomeData.put(data.getAccountSubjectCode(), data.getBalanceDirection() + LedgerConstant.ExportConstant.SPLIT + data.getPostCurrentMoney());
                }
            } else {
                //本年累计
                if (Constant.Is.YES.equals(posting)) {
                    //包含未过账
                    incomeData.put(data.getAccountSubjectCode(), data.getBalanceDirection() + LedgerConstant.ExportConstant.SPLIT + data.getTotalCurrentMoney());
                } else {
                    //不包含未过账
                    incomeData.put(data.getAccountSubjectCode(), data.getBalanceDirection() + LedgerConstant.ExportConstant.SPLIT + data.getPostTotalCurrentMoney());
                }
            }
        });
        return incomeData;
    }

    /**
     * @return java.util.Map<java.lang.String, java.util.List < com.njwd.entity.ledger.vo.IncomeStatementsTableVo>>
     * @description: 计算科目余额每一级次金额
     * @Param [subjectInfo]
     * @author LuoY
     * @date 2019/9/21 11:21
     */
    private void subjectDataMoneyCaulation(Map<String, List<IncomeStatementsTableVo>> subjectInfo, Map<String, String> currentBalanceMap, Map<String, String> yearBalanceMap) {
        //计算金额
        subjectInfo.forEach((key, value) -> {
            if (!FastUtils.checkNullOrEmpty(value)) {
                for (IncomeStatementsTableVo incomeStatementsTableVo : value) {
                    //计算本期
                    incomeStatementsTableVo.setCurrentMoney(getSubjectMoney(currentBalanceMap, incomeStatementsTableVo.getProjectCode(), incomeStatementsTableVo.getDirection()));
                    //计算本年累计
                    incomeStatementsTableVo.setYearCumulative(getSubjectMoney(yearBalanceMap, incomeStatementsTableVo.getProjectCode(), incomeStatementsTableVo.getDirection()));
                }
            }
        });
    }

    /**
     * @return java.util.List<com.njwd.entity.ledger.vo.IncomeStatementsAccountBookVo>
     * @description: 利润表财务报告整理(科目明细)
     * @Param [incomeStatementsAccountBookVoList]
     * @author LuoY
     * @date 2019/9/20 11:42
     */
    private List<IncomeStatementsAccountBookVo> dataSortHandle(List<IncomeStatementsAccountBookVo> incomeStatementsAccountBookVoList) {
        List<IncomeStatementsAccountBookVo> incomeStatementsAccountBookVoList1 = new LinkedList<>();
        for (IncomeStatementsAccountBookVo incomeStatementsAccountBookVo : incomeStatementsAccountBookVoList) {
            //循环核算账簿
            List<IncomeStatementsAccountEntityVo> incomeStatementsAccountEntityVos = new LinkedList<>();
            for (IncomeStatementsAccountEntityVo incomeStatementsAccountEntityVo : incomeStatementsAccountBookVo.getIncomeStatementsAccountEntityVos()) {
                //循环核算主体
                List<IncomeStatementsTableVo> incomeStatementsTableVos = new LinkedList<>();
                for (IncomeStatementsTableVo incomeStatementsTableVo : incomeStatementsAccountEntityVo.getIncomeStatementsTablesListVos()) {
                    //循环财务报告
                    if (Constant.Is.NO.equals(incomeStatementsTableVo.getIsSubject())) {
                        //如果财务报告科目明细为否表示是财务报告,直接添加
                        incomeStatementsTableVos.add(incomeStatementsTableVo);
                        for (IncomeStatementsTableVo incomeStatementsTableVo1 : incomeStatementsAccountEntityVo.getIncomeStatementsTablesListVos()) {
                            //循环财务报告
                            if (Constant.Is.YES.equals(incomeStatementsTableVo1.getIsSubject())) {
                                if (incomeStatementsTableVo1.getReportCode().equals(incomeStatementsTableVo.getProjectCode())) {
                                    //如果科目明细的上级code等于财务报告的code,表示是当前财务报告的科目明细项,添加到当前财务报告项后面
                                    incomeStatementsTableVos.add(incomeStatementsTableVo1);
                                }
                            }
                        }
                    }
                }
                //添加核算主体
                IncomeStatementsAccountEntityVo incomeStatementsAccountEntityVo1 = incomeStatementsAccountEntityVo;
                incomeStatementsAccountEntityVo1.setIncomeStatementsTablesListVos(incomeStatementsTableVos);
                incomeStatementsAccountEntityVos.add(incomeStatementsAccountEntityVo1);
            }
            //添加核算账簿
            IncomeStatementsAccountBookVo incomeStatementsAccountBookVo1 = incomeStatementsAccountBookVo;
            incomeStatementsAccountBookVo1.setIncomeStatementsAccountEntityVos(incomeStatementsAccountEntityVos);
            incomeStatementsAccountBookVoList1.add(incomeStatementsAccountBookVo1);
        }
        return incomeStatementsAccountBookVoList1;
    }

    /**
     * @return java.util.Map<java.lang.String, java.lang.Byte>
     * @description: 将财务报告转换为MAP
     * @Param [cashFlowItemVo]
     * @author LuoY
     * @date 2019/9/26 11:28
     */
    private Map<String, Byte> dataToCashFlowItemMap(List<CashFlowItemVo> cashFlowItemVo) {
        Map<String, Byte> cashFlowItemMap = new HashMap<>();
        if (!FastUtils.checkNullOrEmpty(cashFlowItemVo)) {
            cashFlowItemVo.forEach(data -> {
                //一级现金流量科目code没有方向默认为流入 = 1
                cashFlowItemMap.put(data.getCode(), data.getCashFlowDirection() == null ?
                        Constant.BalanceDirection.CREDIT : Constant.BalanceDirection.FLAT.equals(data.getCashFlowDirection()) ?
                        Constant.BalanceDirection.CREDIT : data.getCashFlowDirection());
            });
        }
        return cashFlowItemMap;
    }

    /**
     * @return java.util.Map<java.lang.String, java.lang.String>
     * @description: 现金流量特殊项期初map
     * @Param [balanceSubjectVos]
     * @author LuoY
     * @date 2019/9/26 14:20
     */
    private Map<String, String> openBalanceMap(List<BalanceSubject> balanceSubjectVos, Map<Long, Byte> accountMap, String code) {
        Map<String, String> openBalance = new HashMap<>();
        balanceSubjectVos.forEach(data -> {
            openBalance.put(code, accountMap.get(data.getAccountSubjectId()) + LedgerConstant.ExportConstant.SPLIT + data.getOpeningBalance());
        });
        return openBalance;
    }

    private List<BalanceSubjectVo> findBalance(BalanceSubjectQueryDto balanceSubjectQueryDto) {
        balanceSubjectQueryDto.setLastPeriodNum(balanceSubjectQueryDto.getPeriodNum());
        //查询本期
        List<BalanceSubjectVo> current = balanceSubjectService.findBalanceSubjectInfoByAccountInfo(balanceSubjectQueryDto);
        //查询本期合计
        balanceSubjectQueryDto.setLastPeriodNum(Constant.PeriodNum.January);
        List<BalanceSubjectVo> balanceSubjectVos = balanceSubjectService.findBalanceSubjectInfoByAccountInfo(balanceSubjectQueryDto);
        // 查询 O 期
        balanceSubjectQueryDto.setLastPeriodNum(Constant.Number.ANTI_INITLIZED);
        balanceSubjectQueryDto.setPeriodNum(Constant.Number.ANTI_INITLIZED);
        List<BalanceSubjectVo> initial = balanceSubjectService.findBalanceSubjectInfoByAccountInfo(balanceSubjectQueryDto);
        boolean isHas = false;
        for (BalanceSubjectVo balanceSubjectVo : balanceSubjectVos) {
            isHas = false;
            for (BalanceSubjectVo balanceSubjectVo1 : current) {
                //如果有对应id的本期数据
                if (balanceSubjectVo.getAccountSubjectId().equals(balanceSubjectVo1.getAccountSubjectId())) {
                    balanceSubjectVo.setCreditAmount(balanceSubjectVo1.getCreditAmount());
                    balanceSubjectVo.setDebitAmount(balanceSubjectVo1.getDebitAmount());
                    balanceSubjectVo.setPostCreditAmount(balanceSubjectVo1.getPostCreditAmount());
                    balanceSubjectVo.setPostDebitAmount(balanceSubjectVo1.getPostDebitAmount());
                    balanceSubjectVo.setSyCreditAmount(balanceSubjectVo1.getSyCreditAmount());
                    balanceSubjectVo.setSyDebitAmount(balanceSubjectVo1.getSyDebitAmount());
                    balanceSubjectVo.setPostSyCreditAmount(balanceSubjectVo1.getPostSyCreditAmount());
                    balanceSubjectVo.setPostSyDebitAmount(balanceSubjectVo1.getPostSyDebitAmount());
                    isHas = true;
                }
            }
            if (!isHas) {
                balanceSubjectVo.setCreditAmount(BigDecimal.ZERO);
                balanceSubjectVo.setDebitAmount(BigDecimal.ZERO);
                balanceSubjectVo.setPostCreditAmount(BigDecimal.ZERO);
                balanceSubjectVo.setPostDebitAmount(BigDecimal.ZERO);
                balanceSubjectVo.setSyCreditAmount(BigDecimal.ZERO);
                balanceSubjectVo.setSyDebitAmount(BigDecimal.ZERO);
                balanceSubjectVo.setPostSyCreditAmount(BigDecimal.ZERO);
                balanceSubjectVo.setPostSyDebitAmount(BigDecimal.ZERO);
            }
        }

        // 累加 0期 初始值
        if(balanceSubjectVos.size() > initial.size()){
            for (BalanceSubjectVo balanceSubjectVo : balanceSubjectVos) {
                for (BalanceSubjectVo initialVo : initial) {
                    if(initialVo.getAccountSubjectId().equals(balanceSubjectVo.getAccountSubjectId())){
                        balanceSubjectVo.setTotalDebitAmount(balanceSubjectVo.getTotalDebitAmount().add(initialVo.getTotalDebitAmount()));
                        balanceSubjectVo.setTotalCreditAmount(balanceSubjectVo.getTotalCreditAmount().add(initialVo.getTotalCreditAmount()));
                        balanceSubjectVo.setPostTotalDebitAmount(balanceSubjectVo.getPostTotalDebitAmount().add(initialVo.getPostTotalDebitAmount()));
                        balanceSubjectVo.setPostTotalCreditAmount(balanceSubjectVo.getPostTotalCreditAmount().add(initialVo.getPostTotalCreditAmount()));
                        balanceSubjectVo.setSyTotalDebitAmount(balanceSubjectVo.getSyTotalDebitAmount().add(initialVo.getSyTotalDebitAmount()));
                        balanceSubjectVo.setSyTotalCreditAmount(balanceSubjectVo.getSyTotalCreditAmount().add(initialVo.getSyTotalCreditAmount()));
                        balanceSubjectVo.setPostSyTotalDebitAmount(balanceSubjectVo.getPostSyTotalDebitAmount().add(initialVo.getPostSyTotalDebitAmount()));
                        balanceSubjectVo.setPostSyTotalCreditAmount(balanceSubjectVo.getPostSyTotalCreditAmount().add(initialVo.getPostSyTotalCreditAmount()));
                    }
                }
            }
        }else{
            for (BalanceSubjectVo initialVo : initial) {
                isHas = true;
                for (BalanceSubjectVo balanceSubjectVo : balanceSubjectVos) {
                    if(initialVo.getAccountSubjectId().equals(balanceSubjectVo.getAccountSubjectId())){
                        balanceSubjectVo.setTotalDebitAmount(balanceSubjectVo.getTotalDebitAmount().add(initialVo.getTotalDebitAmount()));
                        balanceSubjectVo.setTotalCreditAmount(balanceSubjectVo.getTotalCreditAmount().add(initialVo.getTotalCreditAmount()));
                        balanceSubjectVo.setPostTotalDebitAmount(balanceSubjectVo.getPostTotalDebitAmount().add(initialVo.getPostTotalDebitAmount()));
                        balanceSubjectVo.setPostTotalCreditAmount(balanceSubjectVo.getPostTotalCreditAmount().add(initialVo.getPostTotalCreditAmount()));
                        balanceSubjectVo.setSyTotalDebitAmount(balanceSubjectVo.getSyTotalDebitAmount().add(initialVo.getSyTotalDebitAmount()));
                        balanceSubjectVo.setSyTotalCreditAmount(balanceSubjectVo.getSyTotalCreditAmount().add(initialVo.getSyTotalCreditAmount()));
                        balanceSubjectVo.setPostSyTotalDebitAmount(balanceSubjectVo.getPostSyTotalDebitAmount().add(initialVo.getPostSyTotalDebitAmount()));
                        balanceSubjectVo.setPostSyTotalCreditAmount(balanceSubjectVo.getPostSyTotalCreditAmount().add(initialVo.getPostSyTotalCreditAmount()));
                        isHas = false;
                        break;
                    }
                }

                if(isHas){
                    BalanceSubjectVo balanceSubjectVo = new BalanceSubjectVo();
                    // 本期
                    balanceSubjectVo.setCreditAmount(BigDecimal.ZERO);
                    balanceSubjectVo.setDebitAmount(BigDecimal.ZERO);
                    balanceSubjectVo.setPostCreditAmount(BigDecimal.ZERO);
                    balanceSubjectVo.setPostDebitAmount(BigDecimal.ZERO);
                    balanceSubjectVo.setSyCreditAmount(BigDecimal.ZERO);
                    balanceSubjectVo.setSyDebitAmount(BigDecimal.ZERO);
                    balanceSubjectVo.setPostSyCreditAmount(BigDecimal.ZERO);
                    balanceSubjectVo.setPostSyDebitAmount(BigDecimal.ZERO);
                    // 累计
                    balanceSubjectVo.setTotalDebitAmount(initialVo.getTotalDebitAmount());
                    balanceSubjectVo.setTotalCreditAmount(initialVo.getTotalCreditAmount());
                    balanceSubjectVo.setPostTotalDebitAmount(initialVo.getPostTotalDebitAmount());
                    balanceSubjectVo.setPostTotalCreditAmount(initialVo.getPostTotalCreditAmount());
                    balanceSubjectVo.setSyTotalDebitAmount(initialVo.getSyTotalDebitAmount());
                    balanceSubjectVo.setSyTotalCreditAmount(initialVo.getSyTotalCreditAmount());
                    balanceSubjectVo.setPostSyTotalDebitAmount(initialVo.getPostSyTotalDebitAmount());
                    balanceSubjectVo.setPostSyTotalCreditAmount(initialVo.getPostSyTotalCreditAmount());
                }
            }
        }
        return balanceSubjectVos;
    }

    /**
     * @return void
     * @description: 财务报告导出
     * @Param [response, fileName 标题(文件名), financialReports 报表头, datas 报表数据, period 期间, currency 币种]
     * @author LuoY
     * @date 2019/9/3 17:59
     */
    private void exportDataToExcel(HttpServletResponse response, @NotNull String fileName, String sheetName, @NotNull List<FinancialReport> financialReports, @NotNull List<String[]> datas, String period, String currency) throws Exception {
        response.setContentType("multipart/form-data");
        response.setCharacterEncoding("utf-8");
        //解决下载附件中文名称变成下划线
        response.addHeader("Content-Disposition", "attachment;filename=" +
                new String(fileName.getBytes("utf-8"), "iso-8859-1") + ".xls");
        OutputStream stream = response.getOutputStream();
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 生成一个表格
        HSSFSheet sheet = workbook.createSheet(sheetName);
        //获取生成的总列数
        int cellCount = ExportUtils.calculationCellCount(financialReports);
        // 设置表格默认列宽度为20个字节
        sheet.setDefaultColumnWidth(20);
        // 设置表格默认行高为24个字节
        sheet.setDefaultRowHeightInPoints(24);
        //设置表头,返回表头所占行数
        int headRows = ExportUtils.headTitleStyleSingleAccountBook(workbook, sheet, cellCount, fileName, period, currency, financialReports);
        //操作财务报告数据
        ExportUtils.generateExportDetailInfo(workbook, sheet, datas, headRows);
        //设置标题头样式
        ExportUtils.initCellBorder(workbook, sheet, Constant.Number.ZERO, Constant.Number.ZERO, headRows + Constant.Number.ONE, cellCount, true);
        //设置统一边框样式
        ExportUtils.initCellBorder(workbook, sheet, headRows + Constant.Number.ONE, Constant.Number.ZERO, headRows + datas.size(), cellCount, false);
        //导出
        if (null != workbook && null != stream) {
            workbook.write(stream);
            stream.close();
        }
    }
}
