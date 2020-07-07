package com.njwd.ledger.utils;

import com.njwd.common.Constant;
import com.njwd.common.LedgerConstant;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.ledger.FinancialReport;
import com.njwd.utils.FastUtils;
import com.njwd.utils.StringUtil;
import lombok.extern.java.Log;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;

/**
 * @author LuoY
 * @description: excel导出工具类
 * @date 2019/8/30 11:47
 */
@Log
public class ExportUtils {
    /**
     * 创建合并单元格
     *
     * @param sheet    工作簿对象
     * @param indexArr {{"a","b","c","d"},...}
     *                 a: 起始行号
     *                 b: 终止行号
     *                 c: 起始列号
     *                 d: 终止列号
     * @return
     */
    public static void generateMergeCells(HSSFSheet sheet, int[] indexArr) {

        int startR = indexArr[0];
        int endR = indexArr[1];
        int startC = indexArr[2];
        int endC = indexArr[3];
        CellRangeAddress region = new CellRangeAddress(startR, endR, startC, endC);
        sheet.addMergedRegion(region);
    }

    /**
     * 创建字体 & 单元格样式
     *
     * @param workbook
     * @param styleName 样式名称
     *                  boldCenterGray：加粗、居中、灰色背景
     *                  boldCenter：加粗、居中、无背景色
     *                  center：不加粗、居中、无背景色
     *                  bold：加粗、不居中、无背景色
     *                  indentation: 缩进
     * @return
     */
    public static HSSFCellStyle getCellStyle(HSSFWorkbook workbook, String styleName) {
        // 创建字体 微软雅黑，12号
        HSSFFont fontStyle = workbook.createFont();
        fontStyle.setFontName("微软雅黑");
        fontStyle.setFontHeightInPoints((short) 12);
        // 单元格样式
        HSSFCellStyle cellStyle = workbook.createCellStyle();
        // 设置字体
        cellStyle.setFont(fontStyle);
        //自动换行
        cellStyle.setWrapText(false);
        // 上下居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        switch (styleName) {
            case LedgerConstant.ExportConstant.BOLD_CENTER_GRAY:
                //加粗
                fontStyle.setBold(true);
                //灰色背景
                cellStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                //居中
                cellStyle.setAlignment(HorizontalAlignment.CENTER);
                break;
            case LedgerConstant.ExportConstant.BOLD_CENTER:
                fontStyle.setBold(true);
                cellStyle.setAlignment(HorizontalAlignment.CENTER);
                break;
            case LedgerConstant.ExportConstant.CENTER:
                cellStyle.setAlignment(HorizontalAlignment.CENTER);
                break;
            case LedgerConstant.ExportConstant.INDENTATION:
                cellStyle.setIndention((short)2);
                break;
            case LedgerConstant.ExportConstant.BOLD:
                // 字体加粗
                fontStyle.setBold(true);
            default:

        }
        return cellStyle;
    }

    /**
     * @return void
     * @description: 字体设置
     * @Param [headerFont 字体, fontSize 字体大小, isBold 是否加粗]
     * @author LuoY
     * @date 2019/8/30 16:37
     */
    public static void baseFont(Font headerFont, int fontSize, Boolean isBold) {
        headerFont.setFontName("微软雅黑");
        headerFont.setFontHeightInPoints((short) fontSize);
        headerFont.setBold(isBold);
        headerFont.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
    }

    /**
     * @return void
     * @description: 设置边框
     * @Param [headCellStyle, isTitle 是否标题(标题默认为灰色)]
     * @author LuoY
     * @date 2019/9/2 14:44
     */
    public static void baseBorder(HSSFCellStyle headCellStyle, Boolean isTitle) {
        //下边框
        headCellStyle.setBorderBottom(BorderStyle.THIN);
        //左边框
        headCellStyle.setBorderLeft(BorderStyle.THIN);
        //上边框
        headCellStyle.setBorderTop(BorderStyle.THIN);
        //右边框
        headCellStyle.setBorderRight(BorderStyle.THIN);
        if (isTitle) {
            //设置标题颜色为灰色
            headCellStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
            headCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
    }

    /**
     * @return void
     * @description: 简单的统一样式(带边框)
     * @Param [headCellStyle 单元格, headerFont 字体]
     * Param alignType center 居中，left 左对齐，right 右对齐,isWrapText 是否允许换行(默认不换行)
     * @author LuoY
     * @date 2019/8/30 16:20
     */
    public static void baseStyle(HSSFCellStyle headCellStyle, Font headerFont, String isCenter, Boolean isWrapText) {
        baseBorder(headCellStyle, true);
        // 设置字体
        headCellStyle.setFont(headerFont);
        switch (isCenter) {
            case "left":
                // 左右居中
                headCellStyle.setAlignment(HorizontalAlignment.LEFT);
                // 上下居中
                headCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                break;
            case "right":
                // 左右居中
                headCellStyle.setAlignment(HorizontalAlignment.RIGHT);
                // 上下居中
                headCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                break;
            default:
                // 左右居中
                headCellStyle.setAlignment(HorizontalAlignment.CENTER);
                // 上下居中
                headCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        }
        if (isWrapText) {
            headCellStyle.setWrapText(true);
        } else {
            //是否换行
            headCellStyle.setWrapText(false);
        }
    }


    /**
     * @return org.apache.poi.hssf.usermodel.HSSFSheet
     * @description: 单元格斜线(财务报告)
     * @Param [sheet, start 斜线下方文字, end 斜线上方文字, indexArr{{"a","b","c","d"},...}
     * a: 起始行号
     * b: 终止行号
     * c: 起始列号
     * d: 终止列号]
     * @author LuoY
     * @date 2019/8/30 10:02
     */
    public static int obliqueLine(HSSFSheet sheet, String start, String end, HSSFCellStyle startStyle, HSSFCellStyle endStyle, HSSFCellStyle accountStyle, int[] indexArr, List<FinancialReport> financialReports) {
        int headRows = 0;
        //斜线参数
        int startR = indexArr[0];
        int endR = indexArr[1];
        int startC = indexArr[2];
        int endC = indexArr[3];
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
        HSSFClientAnchor a = new HSSFClientAnchor(0, 0, 1023, 255, (short) startC, startR, (short) endC, endR);
        //创建单元格
        if (startR == endR) {
            //画一行两列斜线
            HSSFRow accountBookRow = sheet.createRow(startR);
            HSSFRow accountBookEntityRow = accountBookRow;
            HSSFCell startCell = accountBookRow.createCell(startC);
            startCell.setCellValue(start);
            startCell.setCellStyle(startStyle);
            HSSFCell endCell = accountBookRow.createCell(endC);
            endCell.setCellValue(end);
            endCell.setCellStyle(endStyle);
            //处理核算账簿,主体数据
                                                headRows = reportDataHandle(sheet, accountBookRow, accountBookEntityRow, accountStyle, 2, financialReports);

        } else {
            //画两行两列斜线
            HSSFRow accountBookRow = sheet.createRow(startR);
            HSSFCell startCell = accountBookRow.createCell(endC);
            startCell.setCellValue(end);
            startCell.setCellStyle(endStyle);
            HSSFRow accountBookEntityRow = sheet.createRow(endR);
            HSSFCell endCell = accountBookEntityRow.createCell(startC);
            endCell.setCellValue(start);
            endCell.setCellStyle(startStyle);
            //处理核算账簿,主体数据
            headRows = reportDataHandle(sheet, accountBookRow, accountBookEntityRow, accountStyle, 2, financialReports);
        }
        //划线
        HSSFSimpleShape shape1 = patriarch.createSimpleShape(a);
        shape1.setShapeType(HSSFSimpleShape.OBJECT_TYPE_LINE);
        shape1.setLineStyle(HSSFSimpleShape.LINESTYLE_SOLID);
        return headRows;
    }

    /**
     * @return void
     * @description: 财务报告表头模板
     * @Param [workbook, sheet, row 合并行数, cell 合并列数, title 标题,font 字体大小,period 期间,currency 币种,financialReports 财务报告数据]
     * @author LuoY
     * @date 2019/8/30 11:35
     */
    public static int headTitleStyleSingleAccountBook(HSSFWorkbook workbook, HSSFSheet sheet, int cell, String title,
                                                      String period, String currency, List<FinancialReport> financialReports) {
        //获取标题头行数
        int headRows;
        //标题字体
        Font headerFont = workbook.createFont();
        //标题备注字体
        Font headRemark = workbook.createFont();
        //账簿字体
        Font accountFont = workbook.createFont();
        baseFont(headerFont, LedgerConstant.ExportConstant.SIZE_SIXTEEN, true);
        baseFont(headRemark, LedgerConstant.ExportConstant.SIZE_TEN, false);
        baseFont(accountFont, LedgerConstant.ExportConstant.SIZE_TEN, true);
        //表头样式
        HSSFCellStyle headCellStyle = workbook.createCellStyle();
        HSSFCellStyle headRemarkCellStyle = workbook.createCellStyle();
        HSSFCellStyle headRemarkCellStyle2 = workbook.createCellStyle();
        HSSFCellStyle accountStyle = workbook.createCellStyle();
        //设置边框
        baseStyle(headCellStyle, headerFont, "center", false);
        baseStyle(headRemarkCellStyle, headRemark, "left", false);
        baseStyle(headRemarkCellStyle2, headRemark, "right", false);
        baseStyle(accountStyle, accountFont, "center", false);
        //是否锁单元格
        headCellStyle.setLocked(true);
        headRemarkCellStyle.setLocked(true);
        accountStyle.setLocked(true);
        //加标题单元格(默认占两行)
        Row titleRow = sheet.createRow(Constant.Number.ZERO);
        Cell titleCell = titleRow.createCell(Constant.Number.ZERO);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(headCellStyle);
        //标题备注
        Row titleRemarkRow = sheet.createRow(Constant.Number.ONE);
        Cell titleRemarkCell = titleRemarkRow.createCell(Constant.Number.ZERO);
        titleRemarkCell.setCellStyle(headRemarkCellStyle);
        Row titleRemarkRow1 = sheet.getRow(Constant.Number.ONE);
        Cell titleRemarkCell2 = titleRemarkRow1.createCell(cell);
        titleRemarkCell2.setCellStyle(headRemarkCellStyle2);
        //期间
        titleRemarkCell.setCellValue(LedgerConstant.ExportConstant.PERIOD+period);
        //币种
        titleRemarkCell2.setCellValue(LedgerConstant.ExportConstant.CURRENCY+currency);
        //合并表头单元格
        sheet.addMergedRegion(new CellRangeAddress(Constant.Number.ZERO, Constant.Number.ZERO, Constant.Number.ZERO, cell));
        //斜线
        if (financialReports.size() > Constant.Number.ONE) {
            //多核算账簿需要划斜线
            if (FastUtils.checkNullOrEmpty(financialReports.get(Constant.Number.ZERO).getAccountBookEntityVoList())) {
                //多账簿没有核算主体划一行斜线
                int[] arr = {2, 2, 0, 1};
                headRows = obliqueLine(sheet, LedgerConstant.ExportConstant.ITEM, LedgerConstant.ExportConstant.ACCOUNTBOOK, headRemarkCellStyle, headRemarkCellStyle2, accountStyle, arr, financialReports);
            } else {
                //至少一个核算主体划两行斜线
                int[] arr = {2, 3, 0, 1};
                headRows = obliqueLine(sheet, LedgerConstant.ExportConstant.ITEM, LedgerConstant.ExportConstant.ACCOUNTBOOKENTITY, headRemarkCellStyle, headRemarkCellStyle2, accountStyle, arr, financialReports);
            }
        } else {
            if (financialReports.get(Constant.Number.ZERO).getAccountBookEntityVoList().size() > Constant.Number.ONE) {
                //单账簿多核算主体
                int[] arr = {2, 3, 0, 1};
                headRows = obliqueLine(sheet, LedgerConstant.ExportConstant.ITEM, LedgerConstant.ExportConstant.ACCOUNTBOOKENTITY, headRemarkCellStyle, headRemarkCellStyle2, accountStyle, arr, financialReports);
            } else if (financialReports.get(Constant.Number.ZERO).getAccountBookEntityVoList().size() == Constant.Number.ONE) {
                //单账簿一个核算主体
                HSSFRow accountBookRow = sheet.createRow(Constant.Number.TWO);
                HSSFRow accountBookEntityRow = sheet.createRow(Constant.Number.THREE);
                //多核算账簿,不带核算主体
                headRows = reportDataHandle(sheet, accountBookRow, accountBookEntityRow, accountStyle, 0, financialReports);
            } else {
                //单账簿没有核算主体
                HSSFRow accountBookRow = sheet.createRow(Constant.Number.TWO);
                //多核算账簿,不带核算主体
                headRows = reportDataHandle(sheet, accountBookRow, accountBookRow, accountStyle, 0, financialReports);
            }
        }
        return headRows;
    }


    /**
     * @return int 行数
     * @description: 处理表头核算账簿和核算主体
     * @Param [sheet, accountBookRow, accountBookEntityRow, financialReports]
     * @author LuoY
     * @date 2019/8/31 16:05
     */
    public static int reportDataHandle(HSSFSheet sheet, HSSFRow accountBookRow, HSSFRow accountBookEntityRow, HSSFCellStyle accountStyle, int startCellNum, List<FinancialReport> financialReports) {
        //设置核算账簿,主体的字体
        int headRows = accountBookEntityRow.getRowNum();
        //处理核算账簿核算账簿
        int initBookCell = startCellNum;
        int initEntityCell = startCellNum;
        for (FinancialReport financialReport : financialReports) {
            //处理核算账簿
            //创建账簿单元格
            HSSFCell accountBookCell = accountBookRow.createCell(initBookCell);
            accountBookCell.setCellValue(financialReport.getAccountBookName());

            //设置合并单元格列
            int[] arry = {accountBookRow.getRowNum(), accountBookRow.getRowNum(), initBookCell, initBookCell + financialReport.getColumnNum() - Constant.Number.ONE};
            //合并单元格
            if (!Constant.Number.ONE.equals(financialReport.getColumnNum())) {
                generateMergeCells(sheet, arry);
            }
            accountBookCell.setCellStyle(accountStyle);
            initBookCell = initBookCell + financialReport.getColumnNum();
            //处理核算主体
            if (!FastUtils.checkNullOrEmpty(financialReport.getAccountBookEntityVoList())) {
                //如果有核算主体就处理核算主体
                for (AccountBookEntityVo accountBookEntityVo : financialReport.getAccountBookEntityVoList()) {
                    //创建主体单元格
                    HSSFCell accountEntityCell = accountBookEntityRow.createCell(initEntityCell);
                    accountEntityCell.setCellValue(accountBookEntityVo.getEntityName());
                    //设置合并单元格列
                    int[] arryEntity = {accountBookEntityRow.getRowNum(), accountBookEntityRow.getRowNum(), initEntityCell, initEntityCell + accountBookEntityVo.getColumnNum() - Constant.Number.ONE};
                    //合并单元格
                    if (!Constant.Number.ONE.equals(accountBookEntityVo.getColumnNum())) {
                        generateMergeCells(sheet, arryEntity);
                    }
                    initEntityCell = initEntityCell + accountBookEntityVo.getColumnNum();
                    accountEntityCell.setCellStyle(accountStyle);
                }
            }
        }
        return headRows;
    }

    /**
     * @description: 报告明细数据行 创建
     * @param:
     * @return:
     * @author: Zhuzs
     * @create: 2019-08-31 15:27
     * @Remark: 先获取写入行和获取写入单元格是为了避免覆盖之前表格样式设置, 如果先设置单元格样式, 再创建单元格会导致设置的样式失效
     */
    public static void generateExportDetailInfo(HSSFWorkbook workbook, HSSFSheet sheet, List<String[]> detailInfoList, Integer startRowNum) {
        Integer currRow = startRowNum + Constant.Number.ONE;
        HSSFCellStyle cellStyle;
        HSSFFont font;
        for (int i = 0; i < detailInfoList.size(); i++) {
            //每个单元格设置样式,设置一个样式会导致一个样式调整，其他单元格同步调整，所以每个单元格的样式都初始化一下
            cellStyle = workbook.createCellStyle();
            font = workbook.createFont();
            font.setFontName("微软雅黑");
            font.setFontHeightInPoints((short) 10);
            cellStyle.setFont(font);

            //写入导出数据
            String[] rowValues = detailInfoList.get(i);
            //先获取写入行
            HSSFRow row = sheet.getRow(currRow + i);
            if (StringUtil.isBlank(row)) {
                //如果未获取到写入行,表示当前行未创建,创建当前写入行
                row = sheet.createRow(currRow);
            }
            for (int j = 0; j < rowValues.length; j++) {
                //先获取写入单元格,不存在则创建写入单元格
                HSSFCell cell = row.getCell(j);
                if (StringUtil.isBlank(cell)) {
                    cell = row.createCell(j);
                }
                cell.setCellValue(rowValues[j]);
                cell.setCellStyle(cellStyle);
            }
            currRow++;
        }
    }

    /**
     * @description: 报告明细数据行 创建 带样式
     * @param:
     * @return:
     * @author: Zhuzs
     * @create: 2019-08-31 15:27
     */
    public static void generateExportDetailInfoWithStyle( Integer startRowNum,List<String[]> rebuildDataInfoList, List<HSSFCellStyle[]> styleArrList,HSSFSheet sheet) {
        Integer currRow = startRowNum + Constant.Number.ONE;
        for (int i = 0; i < rebuildDataInfoList.size(); i++) {
            String[] rowValues = rebuildDataInfoList.get(i);
            HSSFCellStyle[] rowStyle = styleArrList.get(i);
            HSSFRow row = sheet.createRow(currRow);
            for (int j = 0; j < rowValues.length; j++) {
                HSSFCell cell = row.createCell(j);
                cell.setCellValue(rowValues[j]);
                cell.setCellStyle(rowStyle[j]);
            }
            currRow++;
        }
    }

    /**
     * @return void
     * @description: 初始化单元格边框
     * @Param [workbook, sheet, startRow 起始行,startCell 起始列, rows 行数, cells单元格列数]
     * @author LuoY
     * @date 2019/9/2 14:07
     */
    public static void  initCellBorder(HSSFWorkbook workbook, HSSFSheet sheet, int startRow, int startCell, int rows, int cells, Boolean title) {
        //初始化一个边框样式
        HSSFCellStyle cellStyle;
        cellStyle = workbook.createCellStyle();
        //设置单元格斜线
        for (int i = startRow; i <= rows; i++) {
            //循环行,获取已经创建的行
            HSSFRow row = sheet.getRow(i);
            if (row == null) {
                //如果行不存在,则创建一行
                row = sheet.createRow(i);
            }
            for (int j = startCell; j <= cells; j++) {
                baseBorder(cellStyle, title);
                //循环获取单元格
                HSSFCell cell = row.getCell(j);

                if (cell == null) {
                    //如果当前单元格未创建,创建单元格赋值样式
                    cell = row.createCell(j);
                    cell.setCellStyle(cellStyle);
                } else {
                    String value =cell.getStringCellValue();
                    //如果当前单元格已创建，则获取当前单元格样式
                    HSSFCellStyle cellStyle1 = cell.getCellStyle();
                    if (cellStyle1 != null&&!StringUtil.isBlank(value)) {
                        //如果样式已经存在，添加边框样式
                        baseBorder(cellStyle1, title);
                        //将样式赋值给单元格
                        cell.setCellStyle(cellStyle1);
                    } else {
                        //如果不存在样式直接赋值单元格边框样式
                        cell.setCellStyle(cellStyle);
                    }
                }
            }
        }
    }

    /**
     * @return int
     * @description: 计算总列数
     * @Param [financialReports]
     * @author LuoY
     * @date 2019/9/2 14:06
     */
    public static int calculationCellCount(List<FinancialReport> financialReports) {
        int cellCount = 0;
        for (FinancialReport financialReport : financialReports) {
            cellCount = cellCount + financialReport.getColumnNum();
        }
        if (financialReports.size() > Constant.Number.ONE) {
            //多账簿需要划斜线
            cellCount = cellCount - Constant.Number.ONE + Constant.Number.TWO;
        } else {
            if (financialReports.get(Constant.Number.ZERO).getAccountBookEntityVoList().size() > Constant.Number.ONE) {
                //单账簿分账的情况下,也需要划斜线
                cellCount = cellCount - Constant.Number.ONE + Constant.Number.TWO;
            } else {
                //单账簿或单账簿单个核算主体,不画斜线
                cellCount = cellCount - Constant.Number.ONE;
            }
        }
        return cellCount;
    }

}
