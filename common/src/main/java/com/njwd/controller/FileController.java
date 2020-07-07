package com.njwd.controller;

import com.njwd.entity.basedata.excel.ExcelRequest;
import com.njwd.entity.basedata.excel.ExcelRule;
import com.njwd.entity.basedata.excel.ExcelTemplate;
import com.njwd.service.FileService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.JsonUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @description: 文件处理Contoller
 * @author: xdy
 * @create: 2019-05-09
 */
@RestController
@RequestMapping("file")
public class FileController extends BaseController {

    @Resource
    private FileService fileService;

    /**
     * @description: 下载excel模板
     * @param: [templateType]
     * @return: org.springframework.http.ResponseEntity
     * @author: xdy
     * @create: 2019-05-21 15-17
     */
    @RequestMapping("downloadExcelTemplate")
    public ResponseEntity downloadExcelTemplate(HttpServletResponse response, ExcelRequest excelRequest) throws IOException {
        return fileService.downloadExcelTemplate(response,excelRequest);
    }

    /**
     * @description: 上传并解析excel
     * @param: [file, templateType]
     * @return: java.lang.String
     * @author: xdy
     * @create: 2019-05-20 16-34
     */
    @RequestMapping("uploadAndCheckExcel")
    public Result uploadAndCheckExcel(@RequestParam(value = "file")MultipartFile file, String templateType, String customParams){
        Map<String,Object>  customParamsMap =   JsonUtils.json2Pojo(customParams, Map.class);
        return ok(fileService.uploadAndCheckExcel(file,templateType,customParamsMap));
    }

    /**
     * @description: 上传excel
     * @param: [file]
     * @return: java.lang.String
     * @author: xdy
     * @create: 2019-06-10 10-29
     */
    @RequestMapping("uploadExcel")
    public Result uploadExcel(@RequestParam(value = "file")MultipartFile file,String templateType){
        return ok(fileService.uploadExcel(file,templateType));
    }

    /**
     * @description: 校验excel
     * @param: [uuid, templateType]
     * @return: java.lang.String
     * @author: xdy
     * @create: 2019-06-10 10-30
     */
    @RequestMapping("checkExcel")
    public Result checkExcel(@RequestBody ExcelRequest excelRequest){
        return ok(fileService.checkExcel(excelRequest.getFileName(),excelRequest.getTemplateType(),excelRequest.getCustomParams()));
    }


    /**
     * @description: 导入excel
     * @param: [uuid]
     * @return: java.lang.String
     * @author: xdy
     * @create: 2019-05-21 14-17
     */
    @RequestMapping("importExcel")
    public Result importExcel(@RequestBody ExcelRequest excelRequest){
        return ok(fileService.importExcel(excelRequest.getUuid()));
    }


    /**
     * @description: 下载excel结果
     * @param: [uuid]
     * @return: org.springframework.http.ResponseEntity 
     * @author: xdy        
     * @create: 2019-05-22 16-44 
     */
    @RequestMapping("downloadExcelResult")
    public ResponseEntity downloadExcelResult(String uuid){
        return fileService.downloadExcelResult(uuid);
    }

    /**
     * @description: 获取模板信息
     * @param: [templateType]
     * @return: java.lang.String 
     * @author: xdy        
     * @create: 2019-08-01 17-18 
     */
    @RequestMapping("findExcelTemplate")
    public Result<ExcelTemplate> findExcelTemplate(String templateType){
        return ok(fileService.findExcelTemplate(templateType));
    }

    /**
     * @description: 获取规则
     * @param: [templateType]
     * @return: java.lang.String 
     * @author: xdy        
     * @create: 2019-08-01 17-18 
     */
    @RequestMapping("findExcelRule")
    public Result<List<ExcelRule>> findExcelRule(String templateType){
        return ok(fileService.findExcelRule(templateType));
    }


}
