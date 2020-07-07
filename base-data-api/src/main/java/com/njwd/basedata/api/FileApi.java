package com.njwd.basedata.api;

import com.njwd.entity.basedata.excel.ExcelRequest;
import com.njwd.support.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @Description //TODO
 * @Author 朱小明
 * @Date 2019/8/1 9:12
 **/
@RequestMapping("file")
public interface FileApi {

    /**
     * @description: 下载excel模板
     * @param: [templateType]
     * @return: org.springframework.http.ResponseEntity
     * @author: 朱小明
     * @create: 2019-05-21 15-17
     */
    @RequestMapping("downloadExcelTemplate")
    ResponseEntity downloadExcelTemplate(String templateType) throws IOException;

    /**
     * @description: 上传并解析excel
     * @param: [file, templateType]
     * @return: java.lang.String
     * @author: 朱小明
     * @create: 2019-05-20 16-34
     */
    @RequestMapping("uploadAndCheckExcel")
    Result uploadAndCheckExcel(MultipartFile file, String templateType, String customParams);

    /**
     * @description: 上传excel
     * @param: [file]
     * @return: java.lang.String
     * @author: 朱小明
     * @create: 2019-06-10 10-29
     */
    @RequestMapping("uploadExcel")
    Result uploadExcel(MultipartFile file);

    /**
     * @description: 校验excel
     * @param: [uuid, templateType]
     * @return: java.lang.String
     * @author: 朱小明
     * @create: 2019-06-10 10-30
     */
    @RequestMapping("checkExcel")
    Result checkExcel(@RequestBody ExcelRequest excelRequest);


    /**
     * @description: 导入excel
     * @param: [uuid]
     * @return: java.lang.String
     * @author: 朱小明
     * @create: 2019-05-21 14-17
     */
    @RequestMapping("importExcel")
    Result importExcel(@RequestBody ExcelRequest excelRequest);


    /**
     * @description: 下载excel结果
     * @param: [uuid]
     * @return: org.springframework.http.ResponseEntity 
     * @author: 朱小明
     * @create: 2019-05-22 16-44 
     */
    @RequestMapping("downloadExcelResult")
    ResponseEntity downloadExcelResult(String uuid);

}
