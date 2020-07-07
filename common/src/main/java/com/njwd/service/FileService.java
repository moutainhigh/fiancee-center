package com.njwd.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.njwd.entity.basedata.excel.*;
import com.njwd.exception.ServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * excel文件上传、下载、导入导出
 */
public interface FileService {

    List<ExcelRule> findExcelRule(String templateType);

    ExcelTemplate findExcelTemplate(String templateType);

    <T> void exportExcel(HttpServletResponse response, List<T> datas, ExcelColumn... excelColumns);

    <T> void exportExcel(HttpServletResponse response, List<T> datas, List<ExcelColumn> excelColumns);

    <T> void exportExcel(HttpServletResponse response, List<T> datas, String fileName, ExcelColumn... excelColumns);

    <T> void exportExcel(HttpServletResponse response, List<T> datas, String fileName, List<List<String>> excelHead, ExcelColumn... excelColumns);

    <T> void exportExcel(HttpServletResponse response, List<T> datas, String menuCode);

    <T> void exportExcel(HttpServletResponse response, List<T> datas, String menuCode, Byte isEnterpriseAdmin);

    List<ExcelColumn> findExcelColumn(String menuCode);

    List<ExcelColumn> findExcelColumn(String menuCode, Byte isEnterpriseAdmin);

    /**
     * 下载excel模版
     *
     * @param request
     * @param response
     * @param fileName
     * @param suffix
     * @throws ServiceException
     */
    void downExcelDemo(HttpServletRequest request, HttpServletResponse response, String fileName, String suffix) throws ServiceException;

    /**
     * @description: 上传并校验excel
     * @param: [file, templateType]
     * @return: com.njwd.financeback.entity.excel.ExcelResult
     * @author: xdy
     * @create: 2019-05-17 17-46
     */
    ExcelResult uploadAndCheckExcel(MultipartFile file, String templateType, Map<String, Object> customParams);
    ExcelResult uploadAndCheckExcel(MultipartFile file, String templateType);
    /**
     * @description: 导入excel数据
     * @param: [uuid]
     * @return: com.njwd.financeback.entity.excel.ExcelResult
     * @author: xdy
     * @create: 2019-05-17 17-46
     */
    ExcelResult importExcel(String uuid);

    /**
     * @description: 下载模板文件
     * @param: [templateType]
     * @return: org.springframework.http.ResponseEntity<byte[]>
     * @author: xdy
     * @create: 2019-05-21 15-15
     */
    ResponseEntity<byte[]> downloadExcelTemplate(String templateType) throws IOException;
    
    /**
     * @description: 下载模板文件
     * @param: [response, excelRequest]
     * @return: org.springframework.http.ResponseEntity<byte[]> 
     * @author: xdy        
     * @create: 2019-10-18 09:07 
     */
    ResponseEntity<byte[]> downloadExcelTemplate(HttpServletResponse response, ExcelRequest excelRequest) throws IOException;

    /**
     * @description: 下载excel结果
     * @param: [uuid]
     * @return: org.springframework.http.ResponseEntity<byte[]>
     * @author: xdy
     * @create: 2019-05-22 09-10
     */
    ResponseEntity<byte[]> downloadExcelResult(String uuid);

    /**
     * @description: 上传excel
     * @param: [file]
     * @return: java.lang.String
     * @author: xdy
     * @create: 2019-06-10 10-36
     */
    String uploadExcel(MultipartFile file,String templateType);

    /**
     * @description:
     * @param: [uuid, templateType]
     * @return: com.njwd.financeback.entity.excel.ExcelResult
     * @author: xdy
     * @create: 2019-06-10 15-15
     */
    ExcelResult checkExcel(String uuid, String templateType, Map<String, Object> customParams);


    /**
     * 导入excel数据
     * @author LuoY
     * @param excelData
     * @return
     * @date 2019-06-19
     */
    ExcelResult importExcelSecond(ExcelData excelData);

     void resetPage(Page page);

}
