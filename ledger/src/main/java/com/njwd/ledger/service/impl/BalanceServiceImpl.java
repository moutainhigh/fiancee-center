package com.njwd.ledger.service.impl;

import com.njwd.common.Constant;
import com.njwd.common.LedgerConstant;
import com.njwd.entity.basedata.dto.AccountBookEntityDto;
import com.njwd.entity.basedata.dto.CompanyDto;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.basedata.vo.CompanyVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.ledger.FinancialReport;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.ledger.dto.BalanceDto;
import com.njwd.entity.ledger.dto.BalanceSubjectQueryDto;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;
import com.njwd.entity.ledger.vo.BalanceSubjectVo;
import com.njwd.entity.ledger.vo.BalanceVo;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.dto.FinancialReportItemSetDto;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.entity.platform.vo.FinancialReportItemSetVo;
import com.njwd.ledger.cloudclient.AccountSubjectFeignClient;
import com.njwd.ledger.cloudclient.CompanyFeignClient;
import com.njwd.ledger.cloudclient.FinancialReportItemSetFeignClient;
import com.njwd.ledger.service.AccountBookPeriodService;
import com.njwd.ledger.service.BalanceService;
import com.njwd.ledger.service.BalanceSubjectService;
import com.njwd.ledger.service.FinancialReportService;
import com.njwd.ledger.utils.ExportUtils;
import com.njwd.utils.FastUtils;
import com.njwd.utils.StringUtil;
import com.njwd.utils.UserUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * 资产负债表——账簿
 *
 * @author zhuzs
 * @date 2019-08-01 17:39
 */
@Service
public class BalanceServiceImpl implements BalanceService {
    @Autowired
    private FinancialReportItemSetFeignClient financialReportItemSetFeignClient;
    @Autowired
    private AccountSubjectFeignClient accountSubjectFeignClient;
    @Autowired
    private CompanyFeignClient companyFeignClient;
    @Autowired
    private BalanceSubjectService balanceSubjectService;
    @Autowired
    private FinancialReportService financialReportService;
    @Autowired
    private AccountBookPeriodService accountBookPeriodService;

    /**
     * 资产负债表
     *
     * @param balanceDtos
     * @return
     */
    @Override
    public List<BalanceVo> getBalanceReport(List<BalanceDto> balanceDtos) {
        // 返回值
        List<BalanceVo> balanceVoList = new ArrayList<>();

        // 遍历账簿
        for (BalanceDto balanceDto : balanceDtos) {

            // 设置 账簿/本位币 信息
            BalanceVo balanceVo = new BalanceVo();
            balanceVo.setAccountBookId(balanceDto.getAccountBookId());
            balanceVo.setAccountBookName(balanceDto.getAccountBookName());
            CompanyDto companyDto = new CompanyDto();
            companyDto.setId(balanceDto.getCompanyId());
            CompanyVo companyVo = companyFeignClient.findCompanyById(companyDto).getData();
            balanceVo.setAccountingCurrencyId(companyVo.getAccountingCurrencyId());
            balanceVo.setAccountingCurrencyName(companyVo.getAccountingCurrencyName());
            // 设置报告项目信息
            List<FinancialReportItemSetVo> balanceItemList = getBalanceItemList(balanceDto);
            balanceVo.setBalanceItemList(balanceItemList);
            // 获取科目编码信息 —— 基础资料
            List<AccountSubjectVo> accountSubjectVos = getAccountingSubjectList(balanceDto);

            // 核算主体各报告项目 期末余额/年初余额/合计
            List<BalanceVo> entityBalanceVoList = new ArrayList<>();
            balanceVo.setEntityBalanceVoList(entityBalanceVoList);
            // 报告粒度 账簿/核算主体
            List<AccountBookEntityDto> accountBookEntityDtoList = balanceDto.getAccountBookEntityDtoList();

            // 账簿
            if (accountBookEntityDtoList == null || Constant.Number.ZERO.equals(accountBookEntityDtoList.size())) {
                BalanceVo entityBalanceVo = new BalanceVo();
                //设置报告各项目 期末/年初余额
                // 生成 查询科目余额信息的入参
                BalanceSubjectQueryDto balanceSubjectQueryDto = generateQueryParam(balanceDto);
                setClosingAndInitialBalance(balanceSubjectQueryDto, balanceItemList, accountSubjectVos, balanceDto, entityBalanceVo);

                entityBalanceVoList.add(entityBalanceVo);
            } else {
                // 核算主体
                for (AccountBookEntityDto accountBookEntityDto : accountBookEntityDtoList) {
                    // 设置核算主体信息
                    BalanceVo entityBalanceVo = new BalanceVo();
                    entityBalanceVo.setAccountBookEntityId(accountBookEntityDto.getId());
                    entityBalanceVo.setAccountBookEntityName(accountBookEntityDto.getEntityName());

                    // 设置报告个项目 期末/年初余额
                    // 生成 查询科目余额信息的入参
                    BalanceSubjectQueryDto balanceSubjectQueryDto = generateQueryParam(balanceDto);
                    balanceSubjectQueryDto.setAccountBookEntityId(accountBookEntityDto.getId());
                    setClosingAndInitialBalance(balanceSubjectQueryDto, balanceItemList, accountSubjectVos, balanceDto, entityBalanceVo);

                    entityBalanceVoList.add(entityBalanceVo);
                }

                // 多核算主体 设置报告个项目 期末余额/年初余额 合计
                if (entityBalanceVoList.size() > Constant.Number.ONE) {

                    BalanceVo entityBalanceTotal = new BalanceVo();
                    entityBalanceTotal.setAccountBookEntityId(LedgerConstant.Ledger.TOTAL);
                    entityBalanceTotal.setAccountBookEntityName(LedgerConstant.Ledger.TOTAL_NAME);
                    List<FinancialReportItemSetVo> balanceItemListWithTotalData = getBalanceItemList(balanceDto);
                    entityBalanceTotal.setBalanceItemList(balanceItemListWithTotalData);

                    Map<String, BigDecimal> totalClosingBalance = new HashMap<>();
                    Map<String, BigDecimal> totalInitialBalance = new HashMap<>();
                    entityBalanceTotal.setClosingBalanceReport(totalClosingBalance);
                    entityBalanceTotal.setInitialBalanceReport(totalInitialBalance);

                    for (int i = 0; i < balanceItemListWithTotalData.size(); i++) {
                        FinancialReportItemSetVo financialReportItemSetVo = balanceItemListWithTotalData.get(i);
                        financialReportItemSetVo.setTotalClosingBalance(BigDecimal.ZERO);
                        financialReportItemSetVo.setTotalInitialBalance(BigDecimal.ZERO);
                        totalClosingBalance.put(financialReportItemSetVo.getCode(), BigDecimal.ZERO);
                        totalInitialBalance.put(financialReportItemSetVo.getCode(), BigDecimal.ZERO);
                        for (BalanceVo perEntity : entityBalanceVoList) {
                            Map<String, BigDecimal> currClosingBalance = perEntity.getClosingBalanceReport();
                            Map<String, BigDecimal> currInitialBalance = perEntity.getInitialBalanceReport();
                            financialReportItemSetVo.setTotalClosingBalance(currClosingBalance.get(financialReportItemSetVo.getCode()));
                            financialReportItemSetVo.setTotalInitialBalance(currInitialBalance.get(financialReportItemSetVo.getCode()));

                            totalClosingBalance.put(financialReportItemSetVo.getCode(), totalClosingBalance.get(financialReportItemSetVo.getCode()).add(currClosingBalance.get(financialReportItemSetVo.getCode())));
                            totalInitialBalance.put(financialReportItemSetVo.getCode(), totalInitialBalance.get(financialReportItemSetVo.getCode()).add(currInitialBalance.get(financialReportItemSetVo.getCode())));
                        }
                    }
                    entityBalanceVoList.add(Constant.Number.ZERO, entityBalanceTotal);
                }

            }

            balanceVoList.add(balanceVo);
        }

        return balanceVoList;
    }

    /**
     * @description:资产负债表——导出
     * @author: Zhuzs
     * @create: 2019-09-02 14:48
     */
    @Override
    public void balanceReportExport(List<BalanceDto> balanceDtos, HttpServletResponse response) {
        // 1 获取数据
        List<BalanceVo> balanceVoList = getBalanceReport(balanceDtos);
        // 提取 会计期间 和 记账本位币信息
        String periodYearNum = balanceDtos.get(0).getPeriodYear().toString() +LedgerConstant.ExportConstant.DASH+ balanceDtos.get(0).getPeriodNum();
        String accountingCurrencyName = balanceVoList.get(0).getAccountingCurrencyName();
        List<FinancialReportItemSetVo> financialReportItemSetVoList = balanceVoList.get(0).getBalanceItemList();
        String fileName = "资产负债表";
        response.setContentType("application/ms-excel;");
        String inlineType = "attachment";
        try (OutputStream output = response.getOutputStream()) {
            response.setHeader("Content-Disposition", inlineType + ";filename=\"" + new String(fileName.getBytes("UTF-8"), "ISO-8859-1") + ".xlsx");

            // 2 在内存中创建一个Excel文件 & 创建工作簿
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("sheet");

            // 设置表格默认列宽度为20个字节
            sheet.setDefaultColumnWidth(20);
            sheet.autoSizeColumn(5);
            // 设置表格默认行高为24个字节
            sheet.setDefaultRowHeightInPoints(24);

            // 3 创建 表头
            List<FinancialReport> financialReports = new ArrayList<>();
            getAccBookAndEntityInfo(balanceVoList, financialReports);

            // 4 计算总列数 、获取表头
            Integer totalColumn = ExportUtils.calculationCellCount(financialReports);
            Integer startRowNum = ExportUtils.headTitleStyleSingleAccountBook(workbook, sheet, totalColumn, fileName, periodYearNum, accountingCurrencyName, financialReports);

            // 5 数据结构 重构（含标题行）/ 项目样式结合
            List<String[]> rebuildDataInfoList = new ArrayList<>();
            rebuildDataInfo(rebuildDataInfoList, balanceVoList);

            // 获取单元格样式
            List<HSSFCellStyle[]> styleArrList = new ArrayList<>();
            generateStyleList(financialReports, financialReportItemSetVoList, styleArrList, workbook);

            // 6 创建 报告项目明细（含标题行）
            ExportUtils.generateExportDetailInfoWithStyle(startRowNum, rebuildDataInfoList, styleArrList, sheet);

            // 设置边框背景色
            ExportUtils.initCellBorder(workbook, sheet, 0, 0, startRowNum, totalColumn, true);
            ExportUtils.initCellBorder(workbook, sheet, startRowNum+Constant.Number.ONE, 0, startRowNum + rebuildDataInfoList.size(), totalColumn, false);
            // 7 数据写出
            if (null != output)
                workbook.write(output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据账簿id 及 核算主体id ，查询各末级科目余额信息
     *
     * @param: [balanceSubjectQueryDto]
     * @return: java.util.List<com.njwd.entity.ledger.vo.BalanceSubjectVo>
     * @author: zhuzs
     * @date: 2019-09-16 14:19
     */
    private List<BalanceSubjectVo> findClosingListByAccountBookIdAndEntityId(BalanceSubjectQueryDto balanceSubjectQueryDto) {
        SysUserVo operator = UserUtils.getUserVo();

        List<BalanceSubjectVo> balanceSubjectList = new ArrayList<>();
        List<BalanceSubjectVo> lsatSettleList = new ArrayList<>();
        // 入参 期间年、期间号
        int queryPeriodYear = balanceSubjectQueryDto.getPeriodYear();
        byte queryPeriodNum = balanceSubjectQueryDto.getPeriodNum();
        // 是否存在反结账

        // 获取当前账簿最近结账 期间
        AccountBookPeriodDto accountBookPeriodDto = new AccountBookPeriodDto();
        accountBookPeriodDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        accountBookPeriodDto.setAccountBookId(balanceSubjectQueryDto.getAccountBookId());
        accountBookPeriodDto.setSystemSign(Constant.SystemSignValue.LEDGER);
        accountBookPeriodDto.setIsSettle(Constant.Is.YES);
        accountBookPeriodDto.setIsMax(Constant.Is.YES);
        AccountBookPeriodVo accountBookPeriodVo = accountBookPeriodService.findPeriodByAccBookIdAndSystemSign(accountBookPeriodDto);

        //默认等于当前期间数
        int latestPeriodYear = queryPeriodYear;
        byte latestPeriodNum = queryPeriodNum;
        // 最近结账 期间年、期间号
        if (accountBookPeriodVo != null) {
            //如果账簿期间存在该核算账簿数据
            latestPeriodYear = accountBookPeriodVo.getPeriodYear();
            latestPeriodNum = accountBookPeriodVo.getPeriodNum();

            // 查询期间已结账
            if (latestPeriodYear > queryPeriodYear || (latestPeriodYear == queryPeriodYear && latestPeriodNum >= queryPeriodNum)) {
                balanceSubjectList = balanceSubjectService.findListByAccountBookIdAndEntityId(balanceSubjectQueryDto);
                for (BalanceSubjectVo balanceSubjectVo:balanceSubjectList ){
                    balanceSubjectVo.setDebitAmountTotal(balanceSubjectVo.getDebitAmount());
                    balanceSubjectVo.setCreditAmountTotal(balanceSubjectVo.getCreditAmount());
                    balanceSubjectVo.setPostDebitAmountTotal(balanceSubjectVo.getPostDebitAmount());
                    balanceSubjectVo.setPostCreditAmountTotal(balanceSubjectVo.getPostCreditAmount());
                }
            } else {
                // 查询期间未结账
                // 获取 最大结账期间数据 作为初始化数据
                balanceSubjectQueryDto.setPeriodYear(latestPeriodYear);
                balanceSubjectQueryDto.setPeriodNum(latestPeriodNum);
                lsatSettleList = balanceSubjectService.findListByAccountBookIdAndEntityId(balanceSubjectQueryDto);

                // 最终返回结果
                balanceSubjectList = getFinalBalanceSubjectVos(balanceSubjectQueryDto, queryPeriodYear, queryPeriodNum);

                // 计算累计
                calculateTotal(balanceSubjectQueryDto, balanceSubjectList, latestPeriodYear, latestPeriodNum, lsatSettleList);
            }
        } else {
            // 获取 最小未结账期间数据 作为初始化数据
            accountBookPeriodDto.setIsSettle(Constant.Is.NO);
            accountBookPeriodDto.setIsLeast(Constant.Is.YES);
            accountBookPeriodVo = accountBookPeriodService.findPeriodByAccBookIdAndSystemSign(accountBookPeriodDto);
            balanceSubjectQueryDto.setLastPeriodYear(accountBookPeriodVo.getPeriodYear());
            balanceSubjectQueryDto.setLastPeriodNum(accountBookPeriodVo.getPeriodNum());
            lsatSettleList = balanceSubjectService.findListByAccountBookIdAndEntityId(balanceSubjectQueryDto);

            // 最终返回结果
            balanceSubjectList = getFinalBalanceSubjectVos(balanceSubjectQueryDto, queryPeriodYear, queryPeriodNum);

            // 计算累计
            calculateTotal(balanceSubjectQueryDto, balanceSubjectList, latestPeriodYear, latestPeriodNum, lsatSettleList);

        }

        return balanceSubjectList;
    }

    /**
     * 根据账簿id 及 核算主体id ，查询各末级科目余年初余额
     *
     * @param: [balanceSubjectQueryDto]
     * @return: java.util.List<com.njwd.entity.ledger.vo.BalanceSubjectVo>
     * @author: zhuzs
     * @date: 2019-10-09 21:26
     */
    private List<BalanceSubjectVo> findInitialListByAccountBookIdAndEntityId(BalanceSubjectQueryDto balanceSubjectQueryDto) {
        SysUserVo operator = UserUtils.getUserVo();

        List<BalanceSubjectVo> balanceSubjectList = new ArrayList<>();
        List<BalanceSubjectVo> lsatSettleList = new ArrayList<>();
        // 入参 期间年、期间号
        int queryPeriodYear = balanceSubjectQueryDto.getPeriodYear();
        byte queryPeriodNum = balanceSubjectQueryDto.getPeriodNum();
        // 是否存在反结账

        // 获取当前账簿最近结账 期间
        AccountBookPeriodDto accountBookPeriodDto = new AccountBookPeriodDto();
        accountBookPeriodDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        accountBookPeriodDto.setAccountBookId(balanceSubjectQueryDto.getAccountBookId());
        accountBookPeriodDto.setSystemSign(Constant.SystemSignValue.LEDGER);
        accountBookPeriodDto.setIsSettle(Constant.Is.YES);
        accountBookPeriodDto.setIsMax(Constant.Is.YES);
        AccountBookPeriodVo accountBookPeriodVo = accountBookPeriodService.findPeriodByAccBookIdAndSystemSign(accountBookPeriodDto);

        //默认等于当前期间数
        int latestPeriodYear = queryPeriodYear;
        byte latestPeriodNum = queryPeriodNum;
        // 最近结账 期间年、期间号
        if (accountBookPeriodVo != null) {
            //如果账簿期间存在该核算账簿数据
            latestPeriodYear = accountBookPeriodVo.getPeriodYear();
            latestPeriodNum = accountBookPeriodVo.getPeriodNum();

            // 查询期间已结账
            if (latestPeriodYear > queryPeriodYear || (latestPeriodYear == queryPeriodYear && latestPeriodNum >= queryPeriodNum)) {
                balanceSubjectList = balanceSubjectService.findListByAccountBookIdAndEntityId(balanceSubjectQueryDto);
                for (BalanceSubjectVo balanceSubjectVo:balanceSubjectList ){
                    balanceSubjectVo.setDebitAmountTotal(balanceSubjectVo.getDebitAmount());
                    balanceSubjectVo.setCreditAmountTotal(balanceSubjectVo.getCreditAmount());
                    balanceSubjectVo.setPostDebitAmountTotal(balanceSubjectVo.getPostDebitAmount());
                    balanceSubjectVo.setPostCreditAmountTotal(balanceSubjectVo.getPostCreditAmount());
                }
            } else {
                // 查询期间未结账
                // 获取 最大结账期间数据 作为初始化数据
                balanceSubjectQueryDto.setPeriodNum(latestPeriodNum);
                lsatSettleList = balanceSubjectService.findListByAccountBookIdAndEntityId(balanceSubjectQueryDto);

                // 最终返回结果
                balanceSubjectList = getFinalBalanceSubjectVos(balanceSubjectQueryDto, queryPeriodYear, queryPeriodNum);

                // 计算累计
                calculateTotal(balanceSubjectQueryDto, balanceSubjectList, latestPeriodYear, latestPeriodNum, lsatSettleList);
            }
        } else {
            // 获取 0 期数据 作为初始化数据
            balanceSubjectQueryDto.setPeriodYear(null);
            balanceSubjectQueryDto.setPeriodNum(Constant.Number.ANTI_INITLIZED);
            lsatSettleList = balanceSubjectService.findListByAccountBookIdAndEntityId(balanceSubjectQueryDto);

            // 获取最小未结账月份
            accountBookPeriodDto.setIsSettle(Constant.Is.NO);
            accountBookPeriodDto.setIsLeast(Constant.Is.YES);
            accountBookPeriodVo = accountBookPeriodService.findPeriodByAccBookIdAndSystemSign(accountBookPeriodDto);
            balanceSubjectQueryDto.setPeriodYear(queryPeriodYear);
            balanceSubjectQueryDto.setPeriodNum(queryPeriodNum);

            // 最终返回结果
            balanceSubjectList = getFinalBalanceSubjectVos(balanceSubjectQueryDto, queryPeriodYear, queryPeriodNum);

            // 计算累计
            calculateTotal(balanceSubjectQueryDto, balanceSubjectList, accountBookPeriodVo.getPeriodYear(), accountBookPeriodVo.getPeriodNum(), lsatSettleList);

            balanceSubjectQueryDto.setPeriodYear(null);
            balanceSubjectQueryDto.setPeriodNum(Constant.Number.ANTI_INITLIZED);
            List<BalanceSubjectVo> initialBalanceSubjectList = balanceSubjectService.findListByAccountBookIdAndEntityId(balanceSubjectQueryDto);

            for(BalanceSubjectVo result:balanceSubjectList){
                for(BalanceSubjectVo initial:initialBalanceSubjectList){
                }

            }

        }

        return balanceSubjectList;
    }

    /**
     * 生成 查询科目余额信息的入参
     *
     * @param: [balanceDto]
     * @return: com.njwd.entity.ledger.dto.BalanceSubjectQueryDto
     * @author: zhuzs
     * @date: 2019-09-29 17:20
     */
    private BalanceSubjectQueryDto generateQueryParam(BalanceDto balanceDto) {
        BalanceSubjectQueryDto balanceSubjectQueryDto = new BalanceSubjectQueryDto();
        balanceSubjectQueryDto.setAccountBookId(balanceDto.getAccountBookId());
        balanceSubjectQueryDto.setPeriodYear(balanceDto.getPeriodYear());
        balanceSubjectQueryDto.setPeriodNum(balanceDto.getPeriodNum());
        return balanceSubjectQueryDto;
    }

    /**
     * 查询期间未结账时，作为最终返回结果主体
     *
     * @param: [balanceSubjectQueryDto, queryPeriodYear, queryPeriodNum]
     * @return: java.util.List<com.njwd.entity.ledger.vo.BalanceSubjectVo>
     * @author: zhuzs
     * @date: 2019-09-24 17:21
     */
    private List<BalanceSubjectVo> getFinalBalanceSubjectVos(BalanceSubjectQueryDto balanceSubjectQueryDto, int queryPeriodYear, byte queryPeriodNum) {
        List<BalanceSubjectVo> balanceSubjectList;
        balanceSubjectQueryDto.setPeriodNum(queryPeriodNum);
        balanceSubjectQueryDto.setPeriodYear(queryPeriodYear);
        balanceSubjectList = balanceSubjectService.findListByAccountBookIdAndEntityId(balanceSubjectQueryDto);
        return balanceSubjectList;
    }

    /**
     * 计算 各期发生额累计
     *
     * @param: [balanceSubjectQueryDto, balanceSubjectList, latestPeriodYear, latestPeriodNum, lsatSettleList]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-19 20:18
     */
    private void calculateTotal(BalanceSubjectQueryDto balanceSubjectQueryDto, List<BalanceSubjectVo> balanceSubjectList, int latestPeriodYear, byte latestPeriodNum, List<BalanceSubjectVo> lsatSettleList) {
        boolean flag = true;
        // 初始化 期末余额/借方累计/贷方累计/已过账本期借方/已过账本期贷方/已过账借方累计/已过账贷方累计
        if(lsatSettleList.size() >= balanceSubjectList.size()){
            for (BalanceSubjectVo latestBalanceSubjectVo : lsatSettleList) {
                flag = true;
                for (BalanceSubjectVo finalBalanceSubjectVo : balanceSubjectList) {
                    if (latestBalanceSubjectVo.getAccountSubjectId().equals(finalBalanceSubjectVo.getAccountSubjectId())) {
                        // 整合 基础信息
                        integrateBaseParams(finalBalanceSubjectVo,latestBalanceSubjectVo.getOpeningBalance());
                        flag = false;
                        break;
                    }
                }
                if(flag){
                    BalanceSubjectVo balanceSubjectVo = new BalanceSubjectVo();
                    balanceSubjectVo.setAccountSubjectId(latestBalanceSubjectVo.getAccountSubjectId());
                    // 整合 基础信息
                    integrateBaseParams(balanceSubjectVo,latestBalanceSubjectVo.getOpeningBalance());

                    balanceSubjectList.add(balanceSubjectVo);
                }
            }
        }else{
            for (BalanceSubjectVo finalBalanceSubjectVo : balanceSubjectList) {
                flag = true;
                for (BalanceSubjectVo latestBalanceSubjectVo : lsatSettleList) {
                    if (latestBalanceSubjectVo.getAccountSubjectId().equals(finalBalanceSubjectVo.getAccountSubjectId())) {
                        // 整合 基础信息
                        integrateBaseParams(finalBalanceSubjectVo,latestBalanceSubjectVo.getOpeningBalance());
                        flag = false;
                        break;
                    }
                }
                if(flag){
                    // 整合 基础信息
                    integrateBaseParams(finalBalanceSubjectVo,BigDecimal.ZERO);

                }
            }
        }

        // 计算各期 发生额累计（资产负债表——计算科目余额）
        balanceSubjectQueryDto.setLastPeriodYear(latestPeriodYear);
        balanceSubjectQueryDto.setLastPeriodNum(latestPeriodNum);
        List<BalanceSubjectVo> accumulateBalanceList = balanceSubjectService.getAccumulateBalanceByPeriodNum(balanceSubjectQueryDto);

        if(accumulateBalanceList.size() >= balanceSubjectList.size()){
            for (BalanceSubjectVo currBalanceSubjectVo : accumulateBalanceList) {
                flag = true;
                for (BalanceSubjectVo finalBalanceSubjectVo : balanceSubjectList) {
                    if (currBalanceSubjectVo.getAccountSubjectId().equals(finalBalanceSubjectVo.getAccountSubjectId())) {
                        // 整合 发生额累计数据
                        integrateTotal(finalBalanceSubjectVo, currBalanceSubjectVo.getPostDebitAmount(), currBalanceSubjectVo.getPostCreditAmount(), currBalanceSubjectVo.getDebitAmount(), currBalanceSubjectVo.getCreditAmount());
                        flag = false;
                        break;
                    }
                }

                if(flag){
                    BalanceSubjectVo balanceSubjectVo = new BalanceSubjectVo();
                    balanceSubjectVo.setAccountSubjectId(currBalanceSubjectVo.getAccountSubjectId());
                    // 设置基础数据
                    balanceSubjectVo.setOpeningBalance(BigDecimal.ZERO);
                    // 整合 发生额累计数据
                    integrateTotal(balanceSubjectVo, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);


                    balanceSubjectList.add(balanceSubjectVo);
                }
            }
        }else{
            for (BalanceSubjectVo finalBalanceSubjectVo : balanceSubjectList) {
                flag = true;
                for (BalanceSubjectVo currBalanceSubjectVo : accumulateBalanceList) {
                    if (currBalanceSubjectVo.getAccountSubjectId().equals(finalBalanceSubjectVo.getAccountSubjectId())) {
                        // 整合 发生额累计数据
                        integrateTotal(finalBalanceSubjectVo, currBalanceSubjectVo.getPostDebitAmount(), currBalanceSubjectVo.getPostCreditAmount(), currBalanceSubjectVo.getDebitAmount(), currBalanceSubjectVo.getCreditAmount());
                        flag = false;
                        break;
                    }
                }

                if(flag){

                    // 设置基础数据
                    finalBalanceSubjectVo.setOpeningBalance(BigDecimal.ZERO);
                    // 整合 发生额累计数据
                    integrateTotal(finalBalanceSubjectVo, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
                }
            }
        }

    }

    /**
     * 整合 发生额累计 数据
     *
     * @param: [finalBalanceSubjectVo, postDebitAmount, postCreditAmount, debitAmount, creditAmount]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-29 16:02
     */
    private void integrateTotal(BalanceSubjectVo finalBalanceSubjectVo, BigDecimal postDebitAmount, BigDecimal postCreditAmount, BigDecimal debitAmount, BigDecimal creditAmount) {
        finalBalanceSubjectVo.setPostDebitAmountTotal(postDebitAmount);
        finalBalanceSubjectVo.setPostCreditAmountTotal(postCreditAmount);
        finalBalanceSubjectVo.setDebitAmountTotal(debitAmount);
        finalBalanceSubjectVo.setCreditAmountTotal(creditAmount);
    }

    /**
     * 整合 基础信息
     *
     * @param: [latestBalanceSubjectVo, finalBalanceSubjectVo]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-27 14:14
     */
    private void integrateBaseParams(BalanceSubjectVo finalBalanceSubjectVo,BigDecimal openingBalance) {
        finalBalanceSubjectVo.setOpeningBalance(openingBalance);
        finalBalanceSubjectVo.setTotalDebitAmount(BigDecimal.ZERO);
        finalBalanceSubjectVo.setTotalCreditAmount(BigDecimal.ZERO);
        finalBalanceSubjectVo.setPostTotalDebitAmount(BigDecimal.ZERO);
        finalBalanceSubjectVo.setPostTotalCreditAmount(BigDecimal.ZERO);
        finalBalanceSubjectVo.setSyTotalDebitAmount(BigDecimal.ZERO);
        finalBalanceSubjectVo.setSyTotalCreditAmount(BigDecimal.ZERO);
        finalBalanceSubjectVo.setPostSyTotalDebitAmount(BigDecimal.ZERO);
        finalBalanceSubjectVo.setPostSyTotalCreditAmount(BigDecimal.ZERO);
    }

    /**
     * 生成 报告项目样式列表
     *
     * @param: [financialReports, financialReportItemSetVoList, styleArrList, workbook]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 14:23
     */
    private void generateStyleList(List<FinancialReport> financialReports, List<FinancialReportItemSetVo> financialReportItemSetVoList, List<HSSFCellStyle[]> styleArrList, HSSFWorkbook workbook) {
        // 单账簿 && 无核算主体||单核算主体
        if (financialReports.size() == Constant.Number.ONE && (financialReports.get(0).getAccountBookEntityVoList().size() == Constant.Number.ZERO || financialReports.get(0).getAccountBookEntityVoList().size() == Constant.Number.ONE)) {
            // 若无核算主体或者单核算主体 ，报告明细项 拆分为： 01资产/02负债及所有者权益
            // 资产
            List<FinancialReportItemSetVo> assetsItemList = new ArrayList<>();
            // 负债及所有者权益
            List<FinancialReportItemSetVo> liabilityItemList = new ArrayList<>();
            //
            reportItemSplitTwoPart(financialReportItemSetVoList, assetsItemList, liabilityItemList);

            // 生成标题行 样式
            generateTitleRowStyle(styleArrList, workbook, Constant.Number.EIGHT);

            for (int i = 0; i < liabilityItemList.size(); i++) {
                HSSFCellStyle[] styleArr = new HSSFCellStyle[Constant.Number.EIGHT];
                if (i < assetsItemList.size() - Constant.Number.ONE || i == liabilityItemList.size() - Constant.Number.ONE) {
                    if (i == liabilityItemList.size() - Constant.Number.ONE) {
                        // 资产
                        itemLevelSelect(workbook, assetsItemList, assetsItemList.size() - Constant.Number.ONE, styleArr, Constant.Number.ZERO, Constant.Number.FOUR);
                    } else {
                        // 资产
                        itemLevelSelect(workbook, assetsItemList, i, styleArr, Constant.Number.ZERO, Constant.Number.FOUR);
                    }
                } else {
                    generateCellStyle(Constant.Number.ZERO, Constant.Number.FOUR, styleArr, workbook, LedgerConstant.ExportConstant.CENTER);
                }
                // 负债及所有者权益
                itemLevelSelect(workbook, liabilityItemList, i, styleArr, Constant.Number.FOUR, Constant.Number.EIGHT);
                styleArrList.add(styleArr);
            }

        } else {
            // 计算核算主体总数量
            Integer entityNum = Constant.Number.ZERO;
            for (FinancialReport financialReport : financialReports) {
                entityNum += financialReport.getAccountBookEntityVoList().size();
            }

            // 生成标题行 样式
            generateTitleRowStyle(styleArrList, workbook, entityNum * Constant.Number.TWO + Constant.Number.TWO);


            for (int i = 0; i < financialReportItemSetVoList.size(); i++) {
                FinancialReportItemSetVo financialReportItemSetVo = financialReportItemSetVoList.get(i);
                HSSFCellStyle[] styleArr = new HSSFCellStyle[entityNum * Constant.Number.TWO + Constant.Number.TWO];
                switch (financialReportItemSetVo.getLevel()) {
                    case 2:
                        generateCellStyle(Constant.Number.ZERO, entityNum * Constant.Number.TWO + Constant.Number.TWO, styleArr, workbook, LedgerConstant.ExportConstant.INDENTATION);
                        break;
                    case 0:
                    case 5:
                        generateCellStyle(Constant.Number.ZERO, entityNum * Constant.Number.TWO + Constant.Number.TWO, styleArr, workbook, LedgerConstant.ExportConstant.BOLD);
                        break;
                    case 6:
                        generateCellStyle(Constant.Number.ZERO, entityNum * Constant.Number.TWO + Constant.Number.TWO, styleArr, workbook, LedgerConstant.ExportConstant.BOLD_CENTER);
                        break;
                    default:
                        generateCellStyle(Constant.Number.ZERO, entityNum * Constant.Number.TWO + Constant.Number.TWO, styleArr, workbook, LedgerConstant.ExportConstant.DEFAULT);
                }

                styleArrList.add(styleArr);

                // 若当前项目为：资产总计，则下一行为标题行
                if (LedgerConstant.ExportConstant.ASSETS_TOTAL.equals(financialReportItemSetVo.getName())) {
                    // 生成标题行 样式
                    generateTitleRowStyle(styleArrList, workbook, entityNum * Constant.Number.TWO + Constant.Number.TWO);
                }
            }
        }
    }

    /**
     * 根据报告项目的级次 选择相应的样式
     *
     * @param: [workbook, itemList, i, styleArr, startColumn, endColumn]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 14:20
     */
    private void itemLevelSelect(HSSFWorkbook workbook, List<FinancialReportItemSetVo> itemList, int i, HSSFCellStyle[] styleArr, Integer startColumn, Integer endColumn) {
        switch (itemList.get(i).getLevel()) {
            case 2:
                generateCellStyle(startColumn, endColumn, styleArr, workbook, LedgerConstant.ExportConstant.INDENTATION);
                break;
            case 0:
            case 5:
                generateCellStyle(startColumn, endColumn, styleArr, workbook, LedgerConstant.ExportConstant.BOLD);
                break;
            case 6:
                generateCellStyle(startColumn, endColumn, styleArr, workbook, LedgerConstant.ExportConstant.BOLD_CENTER);
                break;
            default:
                generateCellStyle(startColumn, endColumn, styleArr, workbook, LedgerConstant.ExportConstant.DEFAULT);
        }
    }

    /**
     * 生成 标题行样式
     *
     * @param: [styleArrList, workbook, eight]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 14:20
     */
    private void generateTitleRowStyle(List<HSSFCellStyle[]> styleArrList, HSSFWorkbook workbook, Integer totalColumn) {
        // 标题行
        HSSFCellStyle[] titleStyle = new HSSFCellStyle[totalColumn];
        for (int i = 0; i < totalColumn; i++) {
            titleStyle[i] = ExportUtils.getCellStyle(workbook, LedgerConstant.ExportConstant.BOLD_CENTER_GRAY);
        }
        // add 标题行样式
        styleArrList.add(titleStyle);
    }

    /**
     * 生成单元格样式
     *
     * @param: [startIndex, endIndex, styleArr, workbook, cellStyle]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 14:21
     */
    private void generateCellStyle(Integer startIndex, Integer endIndex, HSSFCellStyle[] styleArr, HSSFWorkbook workbook, String cellStyle) {
        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                styleArr[i] = ExportUtils.getCellStyle(workbook, LedgerConstant.ExportConstant.CENTER);
            } else {
                styleArr[i] = ExportUtils.getCellStyle(workbook, cellStyle);
            }
        }
    }

    /**
     * 数据结构重构，报告项目信息整合成 字符串数组的集合
     *
     * @param: [rebuildDataInfolist, balanceVoList]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 14:21
     */
    private void rebuildDataInfo(List<String[]> rebuildDataInfolist, List<BalanceVo> balanceVoList) {

        // 1 单账簿 & 无核算主体/单核算主体
        boolean flag = false;
        if (balanceVoList.size() == Constant.Number.ONE && balanceVoList.get(0).getEntityBalanceVoList().size() <= 1) {
            flag = true;
        }
        if (flag) {
            // 报告明细项
            List<FinancialReportItemSetVo> balanceItemList = balanceVoList.get(0).getBalanceItemList();
            // 核算主体
            List<BalanceVo> entityBalanceVoList = balanceVoList.get(0).getEntityBalanceVoList();
            Map<String, BigDecimal> initialMap = entityBalanceVoList.get(0).getInitialBalanceReport();
            Map<String, BigDecimal> closingMap = entityBalanceVoList.get(0).getClosingBalanceReport();

            // 1.1 若无核算主体或者单核算主体 ，报告明细项 拆分为： 01资产/02负债及所有者权益
            // 资产
            List<FinancialReportItemSetVo> assetsItemList = new ArrayList<>();
            // 负债及所有者权益
            List<FinancialReportItemSetVo> liabilityItemList = new ArrayList<>();
            //
            reportItemSplitTwoPart(balanceItemList, assetsItemList, liabilityItemList);


            // 1.2 标题行信息
            String[] titleInfoArr = new String[Constant.Number.EIGHT];
            for (int j = 0; j < titleInfoArr.length; j++) {
                titleInfoArr[j] = LedgerConstant.ExportConstant.TITLE_ROW_OF_ONEBOOK_OR_ONEBOOK_WITH_ONEENTITY[j];
            }
            rebuildDataInfolist.add(titleInfoArr);

            // 1.3 项目信息
            for (int i = 0; i < liabilityItemList.size(); i++) {
                // 单账簿 生成行信息
                oneBookGenerateRowInfo(rebuildDataInfolist, initialMap, closingMap, assetsItemList, liabilityItemList, i);
            }
        } else {
            //2 多账簿
            // 计算核算主体总数量
            Integer entityNum = Constant.Number.ZERO;
            for (BalanceVo balanceVo : balanceVoList) {
                entityNum += balanceVo.getEntityBalanceVoList().size();
            }

            // 2.1 循环核算账簿
            List<BalanceVo> entityBalanceVoList = new ArrayList<>();
            for (BalanceVo balanceVo : balanceVoList) {
                entityBalanceVoList.addAll(balanceVo.getEntityBalanceVoList());
            }
            // 多账簿 生成行信息
            List<FinancialReportItemSetVo> itemList = balanceVoList.get(0).getBalanceItemList();
            FastUtils.checkNull(itemList);
            booksGenerateRowInfo(rebuildDataInfolist, entityNum, itemList, entityBalanceVoList);
        }

    }

    /**
     * 数据行信息重构 多账簿
     *
     * @param: [rebuildDataInfolist, entityNum, balanceItemList, entityBalanceVoList]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 14:27
     */
    private void booksGenerateRowInfo(List<String[]> rebuildDataInfolist, Integer entityNum, List<FinancialReportItemSetVo> balanceItemList, List<BalanceVo> entityBalanceVoList) {
        // 计数,用作下标
        Integer currIndex = Constant.Number.ZERO;
        // 生成 标题行信息
        booksGenerateTitleRow(entityNum, rebuildDataInfolist, LedgerConstant.ExportConstant.ASSETS_COLUMN);

        // 2.2 循环报告项目明细
        for (int i = 0; i < balanceItemList.size(); i++) {
            FinancialReportItemSetVo balanceItem = balanceItemList.get(i);
            String[] rowInfoArr = new String[Constant.Number.TWO * entityNum + Constant.Number.TWO];

            // 报告明细项名称 缩进/增减标识 处理
            String itemName = balanceItem.getName();
            itemName = indentationOperation(balanceItem, itemName);

            rowInfoArr[Constant.Number.ZERO] = itemName;
            rowInfoArr[Constant.Number.ONE] = String.valueOf(i + Constant.Number.ONE);
            currIndex = Constant.Number.TWO;
            if(LedgerConstant.Ledger.ZERO.equals(balanceItem.getLevel().byteValue())){
                for (int j = 0; j < entityBalanceVoList.size(); j++) {
                    rowInfoArr[currIndex] = Constant.Character.NULL_VALUE;
                    currIndex++;
                }
            }else{
                // 2.3 遍历核算主体
                for (int j = 0; j < entityBalanceVoList.size(); j++) {
                    Map<String, BigDecimal> initialMap = entityBalanceVoList.get(j).getInitialBalanceReport();
                    Map<String, BigDecimal> closingMap = entityBalanceVoList.get(j).getClosingBalanceReport();
                    rowInfoArr[currIndex] = String.valueOf(closingMap.get(balanceItem.getCode()));
                    ++currIndex;
                    rowInfoArr[currIndex] = String.valueOf(initialMap.get(balanceItem.getCode()));
                    ++currIndex;
                }
            }

            rebuildDataInfolist.add(rowInfoArr);

            if (LedgerConstant.ExportConstant.ASSETS_TOTAL.equals(balanceItem.getName())) {
                // 生成 标题行信息
                booksGenerateTitleRow(entityNum, rebuildDataInfolist, LedgerConstant.ExportConstant.LIABILITY_COLUMN);
            }

        }
    }

    /**
     * 数据行信息重构 单账簿
     *
     * @param: [rebuildDataInfolist, initialMap, closingMap, assetsItemList, liabilityItemList, i]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 14:28
     */
    private void oneBookGenerateRowInfo(List<String[]> rebuildDataInfolist, Map<String, BigDecimal> initialMap, Map<String, BigDecimal> closingMap, List<FinancialReportItemSetVo> assetsItemList, List<FinancialReportItemSetVo> liabilityItemList, int i) {
        FinancialReportItemSetVo liabilityItem = liabilityItemList.get(i);
        String[] dataInfoArr = new String[Constant.Number.EIGHT];
        // 判断 i == liabilityItemList.size() 目的：保证资产最后一行数据为资产总计
        if (i < assetsItemList.size() - Constant.Number.ONE || i == liabilityItemList.size() - Constant.Number.ONE) {
            FinancialReportItemSetVo assetsItem = null;
            // 资产
            if (i == liabilityItemList.size() - Constant.Number.ONE) {
                assetsItem = assetsItemList.get(assetsItemList.size() - Constant.Number.ONE);
                // 报告明细项名称 缩进/增减标识 处理
                String itemName = assetsItem.getName();
                itemName = indentationOperation(assetsItem, itemName);

                dataInfoArr[Constant.Number.ZERO] = itemName;
                dataInfoArr[Constant.Number.ONE] = String.valueOf(assetsItemList.size());
                dataInfoArr[Constant.Number.TWO] = String.valueOf(closingMap.get(assetsItem.getCode()));
                dataInfoArr[Constant.Number.THREE] = String.valueOf(initialMap.get(assetsItem.getCode()));
            } else {
                assetsItem = assetsItemList.get(i);
                // 报告明细项名称 缩进/增减标识 处理
                String itemName = assetsItem.getName();
                itemName = indentationOperation(assetsItem, itemName);

                dataInfoArr[Constant.Number.ZERO] = itemName;
                dataInfoArr[Constant.Number.ONE] = String.valueOf(i + Constant.Number.ONE);
                if(LedgerConstant.Ledger.ZERO.equals(assetsItem.getLevel().byteValue())){
                    dataInfoArr[Constant.Number.TWO] = Constant.Character.NULL_VALUE;
                    dataInfoArr[Constant.Number.THREE] = Constant.Character.NULL_VALUE;
                }else{
                    dataInfoArr[Constant.Number.TWO] = String.valueOf(closingMap.get(assetsItem.getCode()));
                    dataInfoArr[Constant.Number.THREE] = String.valueOf(initialMap.get(assetsItem.getCode()));
                }
            }

        } else {
            // 资产 无数据时，填写空字符
            dataInfoArr[Constant.Number.ZERO] = Constant.Character.NULL_VALUE;
            dataInfoArr[Constant.Number.ONE] = Constant.Character.NULL_VALUE;
            dataInfoArr[Constant.Number.TWO] = Constant.Character.NULL_VALUE;
            dataInfoArr[Constant.Number.THREE] = Constant.Character.NULL_VALUE;
        }

        // 负债及所有者权益
        // 报告明细项名称 缩进/增减标识 处理
        String itemName = liabilityItem.getName();
        itemName = indentationOperation(liabilityItem, itemName);

        dataInfoArr[Constant.Number.FOUR] = itemName;
        dataInfoArr[Constant.Number.FIVE] = String.valueOf(assetsItemList.size() + i + Constant.Number.ONE);

        if(LedgerConstant.Ledger.ZERO.equals(liabilityItem.getLevel().byteValue())){
            dataInfoArr[Constant.Number.TWO] = Constant.Character.NULL_VALUE;
            dataInfoArr[Constant.Number.THREE] = Constant.Character.NULL_VALUE;
        }else{
            dataInfoArr[Constant.Number.SIX] = String.valueOf(closingMap.get(liabilityItem.getCode()));
            dataInfoArr[Constant.Number.SEVEN] = String.valueOf(initialMap.get(liabilityItem.getCode()));
        }

        rebuildDataInfolist.add(dataInfoArr);
    }

    /**
     * @description: 多账簿 生成标题行信息（依据核算主体的数量）
     * @param:
     * @return:
     * @author: Zhuzs
     * @create: 2019-08-31 18:13
     */
    private void booksGenerateTitleRow(Integer entityNum, List<String[]> rebuildResult, String firstColumnName) {
        String[] titleInfoArr = new String[Constant.Number.TWO * entityNum + Constant.Number.TWO];
        // 两种情况 资产/负债和所有者权益
        titleInfoArr[Constant.Number.ZERO] = firstColumnName;
        titleInfoArr[Constant.Number.ONE] = LedgerConstant.ExportConstant.LINE;
        for (int i = 2; i < titleInfoArr.length; i++) {
            titleInfoArr[i] = LedgerConstant.ExportConstant.CLOSING;
            ++i;
            titleInfoArr[i] = LedgerConstant.ExportConstant.INITILAL;
        }
        rebuildResult.add(titleInfoArr);
    }

    /**
     * 提取 账簿及核算主体信息 供创建表头使用
     *
     * @param: [balanceVoList, financialReports]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 14:29
     */
    private void getAccBookAndEntityInfo(List<BalanceVo> balanceVoList, List<FinancialReport> financialReports) {
        // 单账簿 && 无核算主体||单核算主体
        if (balanceVoList.size() == Constant.Number.ONE && balanceVoList.get(0).getEntityBalanceVoList().size() == Constant.Number.ONE) {
            List<BalanceVo> entityBalanceVoList = balanceVoList.get(0).getEntityBalanceVoList();
            FinancialReport financialReport = new FinancialReport();
            List<AccountBookEntityVo> accountBookEntityVoList = new ArrayList<>();
            financialReport.setAccountBookEntityVoList(accountBookEntityVoList);
            financialReport.setAccountBookName(balanceVoList.get(0).getAccountBookName());
            financialReport.setColumnNum(Constant.Number.EIGHT);

            if (entityBalanceVoList.get(0).getAccountBookEntityId() != null) {
                AccountBookEntityVo accountBookEntityVo = new AccountBookEntityVo();
                accountBookEntityVo.setEntityName(entityBalanceVoList.get(0).getAccountBookEntityName());
                accountBookEntityVo.setColumnNum(Constant.Number.EIGHT);
                accountBookEntityVoList.add(accountBookEntityVo);
            }

            financialReports.add(financialReport);
        } else {
            //
            for (BalanceVo balanceVo : balanceVoList) {
                List<BalanceVo> entityBalanceVoList = balanceVo.getEntityBalanceVoList();
                FinancialReport financialReport = new FinancialReport();
                List<AccountBookEntityVo> accountBookEntityVoList = new ArrayList<>();
                financialReport.setAccountBookEntityVoList(accountBookEntityVoList);
                financialReport.setAccountBookName(balanceVo.getAccountBookName());
                financialReport.setColumnNum(entityBalanceVoList.size() * Constant.Number.TWO);

                if (null != entityBalanceVoList && entityBalanceVoList.size() != Constant.Number.ZERO) {
                    for (BalanceVo entityBalance : entityBalanceVoList) {
                        AccountBookEntityVo accountBookEntityVo = new AccountBookEntityVo();
                        accountBookEntityVo.setEntityName(entityBalance.getAccountBookEntityName());
                        accountBookEntityVo.setColumnNum(Constant.Number.TWO);
                        accountBookEntityVoList.add(accountBookEntityVo);
                    }
                }
                financialReports.add(financialReport);
            }
        }
    }

    /**
     * 财务报告明细项 拆分为： 01资产/02负债及所有者权益两部分（若无核算主体或者单核算主体益）
     *
     * @param: [balanceItemList, assetsItemList, liabilityItemList]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 14:29
     */
    private void reportItemSplitTwoPart(List<FinancialReportItemSetVo> balanceItemList, List<FinancialReportItemSetVo> assetsItemList, List<FinancialReportItemSetVo> liabilityItemList) {

        for (FinancialReportItemSetVo financialReportItemSetVo : balanceItemList) {
            // 资产
            if (LedgerConstant.ExportConstant.ASSETS.equals(financialReportItemSetVo.getItemType())) {
                assetsItemList.add(financialReportItemSetVo);
            } else {
                // 负债及所有者权益
                liabilityItemList.add(financialReportItemSetVo);
            }
        }
    }

    /**
     * 报告明细项名称 缩进/增减标识 处理
     *
     * @param: [item, itemName]
     * @return: java.lang.String
     * @author: zhuzs
     * @date: 2019-09-16 14:28
     */
    private String indentationOperation(FinancialReportItemSetVo item, String itemName) {
        String blank = "";
        //拼缩进
        if (item.getLevel().intValue() >= 1 && item.getLevel().intValue() < 5) {
            for (int i = 0; i < item.getLevel().intValue(); i++) {
                blank += "  ";
            }
        }

        // 是否包含 '其中: '
        if (Constant.Is.YES.equals(item.getIsContain())) {
            itemName = LedgerConstant.ExportConstant.AMONG + itemName;
        }

        // 增减标识
        if (LedgerConstant.Ledger.ZERO.equals(item.getIsAdd().byteValue())) {
            itemName = LedgerConstant.ExportConstant.SUB + itemName;
        } else if (LedgerConstant.Ledger.ONE.equals(item.getIsAdd().byteValue())) {
            itemName = LedgerConstant.ExportConstant.ADD + itemName;
        }

        return blank + itemName;
    }

    /**
     * 设置报告个项目 期末/年初余额
     *
     * @param: [balanceSubjectQueryDto, balanceItemList, accountSubjectVos, balanceDto, entityBalanceVo]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 14:29
     */
    private void setClosingAndInitialBalance(BalanceSubjectQueryDto balanceSubjectQueryDto, List<FinancialReportItemSetVo> balanceItemList, List<AccountSubjectVo> accountSubjectVos, BalanceDto balanceDto, BalanceVo entityBalanceVo) {
        SysUserVo operator = UserUtils.getUserVo();

        // 科目期末余额信息
        List<BalanceSubjectVo> closingBalanceList = findClosingListByAccountBookIdAndEntityId(balanceSubjectQueryDto);
        // 整合数据 科目编码/科目余额
        Map<String, BigDecimal> closingBalanceMap = integrateClosingBalanceAndInitialBalance(balanceItemList, closingBalanceList, accountSubjectVos, balanceDto);

        // 科目年初余额
        // 查询 最小期间
        AccountBookPeriodDto accountBookPeriodDto = new AccountBookPeriodDto();
        accountBookPeriodDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        accountBookPeriodDto.setAccountBookId(balanceSubjectQueryDto.getAccountBookId());
        accountBookPeriodDto.setSystemSign(Constant.SystemSignValue.LEDGER);
        accountBookPeriodDto.setIsLeast(Constant.Is.YES);
        AccountBookPeriodVo accountBookPeriodVo = accountBookPeriodService.findPeriodByAccBookIdAndSystemSign(accountBookPeriodDto);

        Map<String, BigDecimal> initialBalanceMap = new HashMap<>();
        Map<String, String> result = new Hashtable<>();
        // 最小期间号 大于 1
        if(accountBookPeriodVo.getPeriodYear().intValue() == balanceSubjectQueryDto.getPeriodYear().intValue() && accountBookPeriodVo.getPeriodNum().intValue() >= Constant.Number.ONE.intValue()){
            balanceSubjectQueryDto.setPeriodNum(Constant.Number.ANTI_INITLIZED);
            //整合数据 科目编码/科目余额 返回 报告项目 code，各项目余额
            initialBalanceMap = getStringBigDecimalMap(balanceSubjectQueryDto, balanceItemList, accountSubjectVos, balanceDto, result);

        }else{
            balanceSubjectQueryDto.setPeriodYear(balanceSubjectQueryDto.getPeriodYear().intValue()-Constant.Number.ONE.intValue());
            balanceSubjectQueryDto.setPeriodNum(LedgerConstant.Ledger.LAST_MONTH);
            List<BalanceSubjectVo> initialBalanceList = findInitialListByAccountBookIdAndEntityId(balanceSubjectQueryDto);
            // 整合数据 科目编码/科目余额 返回 报告项目 code，各项目余额
            initialBalanceMap = integrateClosingBalanceAndInitialBalance(balanceItemList, initialBalanceList, accountSubjectVos, balanceDto);

        }

        entityBalanceVo.setClosingBalanceReport(closingBalanceMap);
        entityBalanceVo.setInitialBalanceReport(initialBalanceMap);
    }

    /**
     * 获取 报告项目 code，各项目余额 Map
     *
     * @param: [balanceSubjectQueryDto, balanceItemList, accountSubjectVos, balanceDto, result]
     * @return: java.util.Map<java.lang.String,java.math.BigDecimal>
     * @author: zhuzs
     * @date: 2019-10-09 15:02
     */
    private Map<String, BigDecimal> getStringBigDecimalMap(BalanceSubjectQueryDto balanceSubjectQueryDto, List<FinancialReportItemSetVo> balanceItemList, List<AccountSubjectVo> accountSubjectVos, BalanceDto balanceDto, Map<String, String> result) {
        Map<String, BigDecimal> initialBalanceMap;
        // 查询余额数据
        List<BalanceSubjectVo> balanceSubjectList = balanceSubjectService.findListByAccountBookIdAndEntityId(balanceSubjectQueryDto);
        // 整合数据 key: 科目编码 value: 科目方向(0/1),科目余额
        for (BalanceSubjectVo balanceSubjectVo : balanceSubjectList) {
            for (AccountSubjectVo accountSubjectVo : accountSubjectVos) {
                if (accountSubjectVo.getId().equals(balanceSubjectVo.getAccountSubjectId())) {
                    result.put(accountSubjectVo.getCode(), accountSubjectVo.getBalanceDirection() + LedgerConstant.ExportConstant.SPLIT + balanceSubjectVo.getOpeningBalance());
                }
            }
        }
        // 返回 报告项目 code，各项目余额
        initialBalanceMap = financialReportService.financialReportCalculationResult(balanceItemList, result, balanceDto, null);
        return initialBalanceMap;
    }

    /**
     * 计算报告各项目余额 期末余额/年初余额
     *
     * @param: [balanceItemList, balanceSubjectList, accountSubjectVos, balanceDto]
     * @return: java.util.Map<java.lang.String, java.math.BigDecimal>
     * @author: zhuzs
     * @date: 2019-09-16 14:29
     */
    private Map<String, BigDecimal> integrateClosingBalanceAndInitialBalance(List<FinancialReportItemSetVo> balanceItemList, List<BalanceSubjectVo> balanceSubjectList, List<AccountSubjectVo> accountSubjectVos, BalanceDto balanceDto) {

        // 科目 编码/余额 （是否包含未过账）
        Map<String, String> finalBalanceMapByPeriodNum = integrateCodeAndBalance(balanceSubjectList, accountSubjectVos, balanceDto);
        // 返回 报告项目 code，各项目余额
        Map<String, BigDecimal> map = financialReportService.financialReportCalculationResult(balanceItemList, finalBalanceMapByPeriodNum, balanceDto,null);
        return map;
    }

    /**
     * 整合科目 编码/余额信息 （ code + closing_balance ）
     *
     * @param: [balanceSubjectVoList, accountSubjectVos, balanceDto]
     * @return: java.util.Map<java.lang.String, java.math.BigDecimal>
     * @author: zhuzs
     * @date: 2019-09-16 14:30
     */
    private Map<String, String> integrateCodeAndBalance(List<BalanceSubjectVo> balanceSubjectVoList, List<AccountSubjectVo> accountSubjectVos, BalanceDto balanceDto) {
        Map<String, String> result = new Hashtable<>();

        // 整合数据
        for (BalanceSubjectVo balanceSubjectVo : balanceSubjectVoList) {
            for (AccountSubjectVo accountSubjectVo : accountSubjectVos) {
                if (accountSubjectVo.getId().equals(balanceSubjectVo.getAccountSubjectId())) {
                    balanceSubjectVo.setCode(accountSubjectVo.getCode());
                    balanceSubjectVo.setBalanceDirection(accountSubjectVo.getBalanceDirection());
                }
            }
        }

        // 是否包含未过账
        if (Constant.Is.YES.equals(balanceDto.getPosting())) {
            for (BalanceSubjectVo balanceSubjectVo : balanceSubjectVoList) {
            	//如果code为空则表示已删除跳过不展示
				if (StringUtil.isEmpty(balanceSubjectVo.getCode())) {
					continue;
				}

				if (Constant.BalanceDirection.DEBIT.equals(balanceSubjectVo.getBalanceDirection())) {
                    // 科目方向 借方
                    result.put(balanceSubjectVo.getCode(), Constant.BalanceDirection.DEBIT + LedgerConstant.ExportConstant.SPLIT + balanceSubjectVo.getOpeningBalance().add(balanceSubjectVo.getDebitAmountTotal().subtract(balanceSubjectVo.getCreditAmountTotal())));
                } else {
                    // 科目方向 贷方
                    result.put(balanceSubjectVo.getCode(), Constant.BalanceDirection.CREDIT + LedgerConstant.ExportConstant.SPLIT +  balanceSubjectVo.getOpeningBalance().add(balanceSubjectVo.getCreditAmountTotal().subtract(balanceSubjectVo.getDebitAmountTotal())));
                }
            }
        } else {
            // 不包含未过账
            for (BalanceSubjectVo balanceSubjectVo : balanceSubjectVoList) {
				//如果code为空则表示已删除跳过不展示
				if (StringUtil.isEmpty(balanceSubjectVo.getCode())) {
					continue;
				}

				if (Constant.BalanceDirection.DEBIT.equals(balanceSubjectVo.getBalanceDirection())) {
                    // 科目方向 借方
                    result.put(balanceSubjectVo.getCode(), Constant.BalanceDirection.DEBIT + LedgerConstant.ExportConstant.SPLIT + balanceSubjectVo.getOpeningBalance().add(balanceSubjectVo.getPostDebitAmountTotal().subtract(balanceSubjectVo.getPostCreditAmountTotal())));
                } else {
                    // 科目方向 贷方
                    result.put(balanceSubjectVo.getCode(), Constant.BalanceDirection.CREDIT + LedgerConstant.ExportConstant.SPLIT +balanceSubjectVo.getOpeningBalance().add(balanceSubjectVo.getPostCreditAmountTotal().subtract(balanceSubjectVo.getPostDebitAmountTotal())));
                }
            }
        }
        return result;
    }

    /**
     * 资产负债表项目明细 —— 平台
     *
     * @param: [balanceDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.FinancialReportItemSetVo>
     * @author: zhuzs
     * @date: 2019-09-16 14:30
     */
    private List<FinancialReportItemSetVo> getBalanceItemList(BalanceDto balanceDto) {
        FinancialReportItemSetDto platformFinancialReportItemSetDto = new FinancialReportItemSetDto();
        platformFinancialReportItemSetDto.setReportId(balanceDto.getBalanceSheetId());
        List<FinancialReportItemSetVo> financialReportItemSetVos = financialReportItemSetFeignClient.findFinancialReportItemSetList(platformFinancialReportItemSetDto).getData();
        // 非空校验
        FastUtils.checkNull(financialReportItemSetVos);
        return financialReportItemSetVos;
    }

    /**
     * 获取末级科目id及编码 —— 基础资料
     *
     * @param: [balanceDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.AccountSubjectVo>
     * @author: zhuzs
     * @date: 2019-09-16 14:30
     */
    private List<AccountSubjectVo> getAccountingSubjectList(BalanceDto balanceDto) {
        AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
        accountSubjectDto.setSubjectId(balanceDto.getSubjectId());
        accountSubjectDto.setIsFinal(LedgerConstant.Ledger.ISFINAL);
        accountSubjectDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        List<AccountSubjectVo> accountSubjectVoList = accountSubjectFeignClient.findInfoForLedger(accountSubjectDto).getData();
        // 非空校验
        FastUtils.checkNull(accountSubjectVoList);
        return accountSubjectVoList;
    }

}
