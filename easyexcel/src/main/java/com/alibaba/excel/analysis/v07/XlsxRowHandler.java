package com.alibaba.excel.analysis.v07;

import com.alibaba.excel.annotation.FieldType;
import com.alibaba.excel.constant.ExcelXmlConstants;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventRegisterCenter;
import com.alibaba.excel.event.OneRowAnalysisFinishEvent;
import com.alibaba.excel.util.PositionUtils;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Arrays;

import static com.alibaba.excel.constant.ExcelXmlConstants.*;

/**
 *
 * @author jipengfei
 */
public class XlsxRowHandler extends DefaultHandler {

    private String currentCellIndex;

    private FieldType currentCellType;

    private int curRow;

    private int curCol=-1;

    private int maxCol;

    private Object[] curRowContent = new Object[20];

    private String currentCellValue;

    private SharedStringsTable sst;

    private StylesTable stylesTable;

    private AnalysisContext analysisContext;

    private AnalysisEventRegisterCenter registerCenter;

    // 单元格存储格式的索引，对应style.xml中的numFmts元素的子元素索引
    private int numFmtIndex;
    // 单元格存储的格式化字符串，nmtFmt的formateCode属性的值
    private String numFmtString;

    public XlsxRowHandler(AnalysisEventRegisterCenter registerCenter, SharedStringsTable sst,StylesTable stylesTable,
                          AnalysisContext analysisContext) {
        this.registerCenter = registerCenter;
        this.analysisContext = analysisContext;
        this.sst = sst;
        this.stylesTable = stylesTable;
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {

        setTotalRowCount(name, attributes);

        startCell(name, attributes);

        startCellValue(name);

    }

    private void startCellValue(String name) {
        if (name.equals(CELL_VALUE_TAG) || name.equals(CELL_VALUE_TAG_1)) {
            // initialize current cell value
            currentCellValue = "";
        }
    }

    private void startCell(String name, Attributes attributes) {
        if (ExcelXmlConstants.CELL_TAG.equals(name)) {
            currentCellIndex = attributes.getValue(ExcelXmlConstants.POSITION);
            int nextRow = PositionUtils.getRow(currentCellIndex);
            if (nextRow > curRow) {
                curRow = nextRow;
                // endRow(ROW_TAG);
            }
            analysisContext.setCurrentRowNum(curRow);
            curCol = PositionUtils.getCol(currentCellIndex);

            String cellType = attributes.getValue("t");
            currentCellType = FieldType.NUMBER;

            if (cellType != null && cellType.equals("s")) {
                currentCellType = FieldType.STRING;
            }
            if(currentCellType.equals(FieldType.NUMBER)){
                String xfIndexStr = attributes.getValue("s");
                if(xfIndexStr!=null){
                    int xfIndex = Integer.parseInt(xfIndexStr);
                    XSSFCellStyle xssfCellStyle = stylesTable.getStyleAt(xfIndex);
                    numFmtIndex = xssfCellStyle.getDataFormat();
                    numFmtString = xssfCellStyle.getDataFormatString();
                    if(numFmtString!=null){
                        if (DateUtil.isADateFormat(numFmtIndex, numFmtString)) {
                            currentCellType = FieldType.DATE;
                        }
                    }
                }
            }
        }
    }

    private void endCellValue(String name) throws SAXException {
        // ensure size
        if(curCol<0)
            return;
        if (curCol >= curRowContent.length) {
            curRowContent = Arrays.copyOf(curRowContent, (int)(curCol * 1.5));
        }
        Object cellValue = currentCellValue;
        if (CELL_VALUE_TAG.equals(name)) {

            switch (currentCellType) {
                case STRING:
                    int idx = Integer.parseInt(currentCellValue);
                    cellValue = new XSSFRichTextString(sst.getEntryAt(idx)).toString();
                    currentCellType = FieldType.EMPTY;
                    break;
                case NUMBER:
                   if(currentCellValue.contains("."))
                       cellValue = Double.valueOf(currentCellValue);
                   else
                       cellValue = Long.valueOf(currentCellValue);
                   break;
                case DATE:
                    cellValue = DateUtil.getJavaDate(Double.parseDouble(currentCellValue), analysisContext.use1904WindowDate());
                    break;
            }
            curRowContent[curCol] = cellValue;
        } else if (CELL_VALUE_TAG_1.equals(name)) {
            curRowContent[curCol] = cellValue;
        }
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        endRow(name);
        endCellValue(name);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        currentCellValue += new String(ch, start, length);
    }


    private void setTotalRowCount(String name, Attributes attributes) {
        if (DIMENSION.equals(name)) {
            String d = attributes.getValue(DIMENSION_REF);
            String totalStr = d.substring(d.indexOf(":") + 1, d.length());
            String c = totalStr.toUpperCase().replaceAll("[A-Z]", "");
            analysisContext.setTotalCount(Integer.parseInt(c));
        }

    }

    private void endRow(String name) {
        if (name.equals(ROW_TAG)) {
            if(curRow==0)
                maxCol = curCol;
            registerCenter.notifyListeners(new OneRowAnalysisFinishEvent(curRowContent,maxCol));
            curRowContent = new Object[maxCol+1];
        }
    }

}

