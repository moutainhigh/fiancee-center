package com.njwd.utils;


import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: Excel工具类
 * @author: fancl
 * @create: 2019-05-09
 */
public class ExcelUtil {
    /**
     * 日志记录
     */
    private static final Logger log = LoggerFactory.getLogger(ExcelUtil.class);
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 方法描述：生成表头 <br/>
     *
     * @param isContainsHeadTitle 是否包含一级标题
     * @param headTile            一级标题 标签名称
     * @param secondTitles        二级标题列表
     * @return
     */
    private static XSSFWorkbook makeTitle(boolean isContainsHeadTitle, String headTile, String[] secondTitles) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFCellStyle styleTitle = null; // 一级标题样式

        XSSFSheet sheet = null; // 标签
        sheet = workbook.createSheet(headTile);
        sheet.setDefaultColumnWidth(25);
        XSSFRow rowField = null; // 行
        // 包含一级标题
        if (isContainsHeadTitle) {
            CellRangeAddress cellRangeAddress = new CellRangeAddress(0, 0, 0, secondTitles.length - 1);// 单元格范围
            sheet.addMergedRegion(cellRangeAddress);
            styleTitle = createStyle(workbook, (short) 16);
            XSSFRow rowTitle = sheet.createRow(0);
            XSSFCell cellTitle = rowTitle.createCell(0);
            // 为标题设置背景颜色
            //styleTitle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            styleTitle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
            cellTitle.setCellValue(headTile);
            cellTitle.setCellStyle(styleTitle);
            rowField = sheet.createRow(1);
        }
        // 仅包含二级标题
        else {
            rowField = sheet.createRow(0);
        }
        XSSFCellStyle styleField = createStyle(workbook, (short) 13);
        ; // 二级标题样式
        // 填充二级标题
        for (int i = 0; i < secondTitles.length; i++) {
            XSSFCell cell = rowField.createCell(i);
            cell.setCellValue(secondTitles[i]);
            cell.setCellStyle(styleField);
        }
        return workbook;
    }

    /**
     * 插入数据
     *
     * @param isContainsHeadTitle
     * @param workbook
     * @param dataList
     * @param beanPropertys
     * @return
     */
//    private static <T> XSSFWorkbook geneExcelData(boolean isContainsHeadTitle, XSSFWorkbook workbook, List<T> dataList,
//                                                  String[] beanPropertys) {
//        XSSFSheet sheet = workbook.getSheetAt(0);
//        // 填充数据
//        XSSFCellStyle styleData = workbook.createCellStyle();
//        //styleData.setAlignment(XSSFCellStyle.ALIGN_CENTER);
//        //styleData.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
//        int jumpRow = 0; // 跳过的行数
//        if (isContainsHeadTitle) {
//            jumpRow = 2;
//        } else {
//            jumpRow = 1;
//        }
//        for (int j = 0; j < dataList.size(); j++) {
//            XSSFRow rowData = sheet.createRow(j + jumpRow);
//            T t = dataList.get(j);
//            for (int k = 0; k < beanPropertys.length; k++) {
//                Object value = BeanUtils.getBeanProperty(t, beanPropertys[k]);
//                XSSFCell cellData = rowData.createCell(k);
//                //对日期格式的进行转日期字符串处理
//                if (value instanceof java.util.Date) {
//                    value = DateUtils.format((Date) value, DATE_FORMAT);
//                }
//                cellData.setCellValue(value == null ? "" : value.toString());
//                cellData.setCellType(Cell.CELL_TYPE_STRING);
//                cellData.setCellStyle(styleData);
//
//
//            }
//        }
//        return workbook;
//    }

    /**
     * 使用批量导入方法时，请注意需要导入的Bean的字段和excel的列一一对应
     *
     * @param clazz
     * @param file
     * @param beanProperty
     * @return
     */
    private static <T> List<T> parserExcel(Class<T> clazz, File file, String[] beanProperty) {
        // 得到workbook
        List<T> list = new ArrayList<T>();
        try {
            Workbook workbook = WorkbookFactory.create(file);
            Sheet sheet = workbook.getSheetAt(0);
            // 直接从第三行开始获取数据
            int rowSize = sheet.getPhysicalNumberOfRows();
            if (rowSize > 2) {
                for (int i = 2; i < rowSize; i++) {
                    T t = clazz.newInstance();
                    Row row = sheet.getRow(i);
                    int cellSize = row.getPhysicalNumberOfCells();
                    for (int j = 0; j < cellSize; j++) {

                        Object cellValue = getCellValue(row.getCell(j));
                        org.apache.commons.beanutils.BeanUtils.copyProperty(t, beanProperty[j], cellValue);
                    }

                    list.add(t);

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;

    }

    /**
     * 通用的读取excel单元格的处理方法
     *
     * @param cell
     * @return
     */
    public static Object getCellValue(Cell cell) {
        Object result = null;
        if (cell != null) {
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_STRING:
                    result = cell.getStringCellValue();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        result = cell.getDateCellValue();
                    } else {
                        Double value = cell.getNumericCellValue();
                        if (value % 1 == 0) {
                            result = value.longValue();
                        } else {
                            result = value;
                        }
                    }
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    result = cell.getBooleanCellValue();
                    break;
                case Cell.CELL_TYPE_FORMULA:
                    //cell执行过公式，不会进入
                    break;
                case Cell.CELL_TYPE_ERROR:
                    break;
                case Cell.CELL_TYPE_BLANK:
                    break;
                default:
                    break;

            }
        }
        return result;
    }

    /**
     * 提取公共的样式
     *
     * @param workbook
     * @param fontSize
     * @return
     */
    private static XSSFCellStyle createStyle(XSSFWorkbook workbook, short fontSize) {
        XSSFCellStyle style = workbook.createCellStyle();
        //style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        //style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        // 创建一个字体样式
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints(fontSize);
        //font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
        style.setFont(font);
        return style;
    }

    /**
     * 方法描述：导出方法
     *
     * #author fanchunlei
     *
     * @param <T>
     * @param isContainsHeadTitle 是否包含一级标题
     * @param headTile            一级标题 及标签名称
     * @param secondTitles        二级标题 即展示的列标题
     * @param dataList            数据列表
     * @param beanProperty        数据列表中的属性
     * @param fileName            生成的文件名
     * @param response            返回的输出流
     * @param response            返回的输出流
     */
//    public static <T> void exportExcel(boolean isContainsHeadTitle, String headTile, String[] secondTitles,
//                                       List<T> dataList, String[] beanProperty, String fileName, HttpServletRequest request,
//                                       HttpServletResponse response) throws ServiceException {
//        // 参数有效性校验
//        FastUtils.checkParams(headTile, secondTitles, dataList, beanProperty, fileName);
//        // 文件后缀名必须为xlsx或xls
//        if (!(fileName.toLowerCase().endsWith(".xlsx") || (fileName.toLowerCase().endsWith(".xls")))) {
//
//        }
//        log.info("[Excel导出] headTile：{}, secondTitles:{}, dataList:{}, beanProperty:{}, fileName:{}", headTile,
//                secondTitles.toString(), dataList.toString(), beanProperty.toString(), fileName);
//        try {
//            // 创建标题
//            XSSFWorkbook workbookTitle = makeTitle(isContainsHeadTitle, headTile, secondTitles);
//            // 生成数据
//            XSSFWorkbook workBook = geneExcelData(isContainsHeadTitle, workbookTitle, dataList, beanProperty);
//            // 输出文件并关闭输出流
//            OutputStream outStream = null;
//            try {
//                // 设置http头信息
//                response.setHeader("Pragma", "No-cache");
//                response.setHeader("Cache-Control", "no-store");
//                response.setDateHeader("Expires", 0);
//                response.setContentType("application/octet-stream");
//                // 判断浏览器内核
//                boolean isMSIE = HttpUtils.isMSBrowser(request);
//                if (isMSIE) {
//                    fileName = URLEncoder.encode(fileName, "UTF-8");
//                } else {
//                    fileName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
//                }
//                // 将文件流输出到response
//                response.setHeader("Content-disposition", "attachment;filename=\"" + fileName + "\"");
//                outStream = response.getOutputStream();
//                workBook.write(outStream);
//                outStream.flush();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                if (outStream != null) {
//                    try {
//                        outStream.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 方法描述：获取Excel数据公共方法
     * <p>
     * #author fanchunlei
     *
     * @param isContainsHeadTitle 是否包含一级表头
     * @param clazz               数据实体的class对象
     * @param inputStream         Excel文件的输入流
     * @param beanProperty        数据列属性
     */
    public static <T> List<T> getExcelData(boolean isContainsHeadTitle, Class<T> clazz, InputStream inputStream,
                                           String[] beanProperty) {

        // 得到workbook
        List<T> list = new ArrayList<T>();
        int startRow = 0; // 开始读取数据的行数
        if (isContainsHeadTitle) {
            startRow = 2;
        } else {
            startRow = 1;
        }
        try {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            // 从表头后开始获取数据
            int rowSize = sheet.getPhysicalNumberOfRows();
            if (rowSize > startRow) {
                for (int i = startRow; i < rowSize; i++) {
                    T t = clazz.newInstance();
                    Row row = sheet.getRow(i);
                    int cellSize = row.getPhysicalNumberOfCells();
                    for (int j = 0; j < cellSize; j++) {

                        Object cellValue = getCellValue(row.getCell(j));
                        org.apache.commons.beanutils.BeanUtils.copyProperty(t, beanProperty[j], cellValue);
                    }

                    list.add(t);

                }
            }

        } catch (Exception e) {
            log.error("=======================getExcelData方法读取Excel数据异常=======================");
        }
        return list;

    }

    public static void main(String[] args) {

    }

}

