package com.njwd.fileexcel.extend;

import com.njwd.entity.basedata.excel.ExcelRequest;

import javax.servlet.http.HttpServletResponse;

/**
 * @description:
 * @author: xdy
 * @create: 2019/10/16 16:56
 */
public interface DownloadExtend {

    void download(HttpServletResponse response, ExcelRequest excelRequest);

}
