package com.njwd.service.impl;


import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.SysMenuTabColumn;
import com.njwd.entity.basedata.dto.query.TableConfigQueryDto;
import com.njwd.entity.basedata.excel.*;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.fileexcel.add.DataAddManager;
import com.njwd.fileexcel.export.DataGet;
import com.njwd.fileexcel.export.DataGetGroup;
import com.njwd.fileexcel.extend.CheckExtend;
import com.njwd.fileexcel.extend.DownloadExtend;
import com.njwd.fileexcel.extend.ExtendFactory;
import com.njwd.fileexcel.read.ExcelRead;
import com.njwd.fileexcel.read.ExcelReadFactory;
import com.njwd.mapper.FileMapper;
import com.njwd.service.FileService;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.JsonUtils;
import com.njwd.utils.RedisUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @description: 文件处理类
 * @author: xdy
 * @create: 2019-05-09
 */
@Service
public class FileServiceImpl implements FileService {

    @Value("${constant.file.excelRootPath}")
    private String excelRootPath;    //模版根路径
    @Resource
    private FileMapper fileMapper;

    @Resource(name="restTemplate0")
    private RestTemplate restTemplate;

    private String[] excelColumn = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};

    /**
     * @description: 下载模板文件
     * @param: [templateType]
     * @return: org.springframework.http.ResponseEntity<byte [ ]>
     * @author: xdy
     * @create: 2019-05-21 15-12
     */
    public ResponseEntity<byte[]> downloadExcelTemplate(HttpServletResponse response, ExcelRequest excelRequest) throws IOException {
        DownloadExtend downloadExtend = ExtendFactory.getDownloadExtend(excelRequest.getTemplateType());
        if(downloadExtend!=null){
            downloadExtend.download(response,excelRequest);
            return null;
        }
        return downloadExcelTemplate(excelRequest.getTemplateType());
    }
    
    /**
     * @description: 下载模板文件
     * @param: [templateType]
     * @return: org.springframework.http.ResponseEntity<byte[]> 
     * @author: xdy        
     * @create: 2019-10-18 09:07 
     */
    @Override
    public ResponseEntity<byte[]> downloadExcelTemplate(String templateType) throws IOException {
        //获取模板
        ExcelTemplate excelTemplate = findExcelTemplate0(templateType);
        if (excelTemplate == null)
            throw new ServiceException(ResultCode.EXCEL_TEMPLATE_NOT_EXISTS);
        File file = new File(excelTemplate.getTemplatePath());
        if (!file.exists() || !file.isFile())
            //抛出文件不存在信息
            throw new ServiceException(ResultCode.FILE_NOT_EXISTS);
        return ResponseEntity.ok()
                .header("Content-Disposition", String.format("attachment; filename=%s", file.getName()))
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("multipart/form-data"))
                .body(FileUtils.readFileToByteArray(file));
    }

    /**
     * @description: 上传并校验excel
     * @param: [file, templateType]
     * @return: com.njwd.financeback.entity.excel.ExcelResult
     * @author: xdy
     * @create: 2019-05-17 15-01
     */
    @Override
    public ExcelResult uploadAndCheckExcel(MultipartFile file, String templateType, Map<String,Object> customParams) {
        FastUtils.checkParams(templateType);
        //校验excel类型
        if (!file.getOriginalFilename().matches(".+\\.(xls|xlsx)")) {
            throw new ServiceException(ResultCode.FILE_MUST_IS_EXCEL);
        }
        ExcelResult excelResult;
        try {
            excelResult = checkExcel(file.getInputStream(),file.getOriginalFilename(),templateType,customParams);
        } catch (IOException e) {
            throw new ServiceException(ResultCode.EXCEL_NOT_CORRECT);
        }
        return excelResult;
    }

    @Override
    public ExcelResult uploadAndCheckExcel(MultipartFile file, String templateType) {
        return uploadAndCheckExcel(file,templateType,null);
    }

    /**
     * @description: 上传excel
     * @param: [file]
     * @return: java.lang.String 
     * @author: xdy        
     * @create: 2019-06-10 10-48 
     */
    @Override
    public String uploadExcel(MultipartFile file,String templateType) {
        //校验excel类型
        if (!file.getOriginalFilename().matches(".+\\.(xls|xlsx)")) {
            throw new ServiceException(ResultCode.FILE_MUST_IS_EXCEL);
        }
        if(templateType==null)
            templateType="";
        String uuid = UUID.randomUUID().toString();
        uuid = uuid.substring(uuid.length()-12);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String fileName;
        try {
            String sourceFileName = file.getOriginalFilename().substring(0,file.getOriginalFilename().lastIndexOf("."));
            String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            fileName = String.format("%s%s-%s%s%s",templateType,sdf.format(new Date()),uuid,sourceFileName,suffix);
            File dest = new File(new File(excelRootPath),fileName);
            if(!dest.getParentFile().exists()){
                dest.getParentFile().mkdirs();
            }
            file.transferTo(dest);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage(),ResultCode.UPLOAD_EXCEPTION);
        }
        return fileName;
    }
    
    /**
     * @description: 校验excel
     * @param: [fileName, templateType]
     * @return: com.njwd.financeback.entity.excel.ExcelResult
     * @author: xdy        
     * @create: 2019-06-10 11-06 
     */
    @Override
    public ExcelResult checkExcel(String fileName, String templateType,Map<String,Object> customParams) {
        File file = new File(new File(excelRootPath),fileName);
        if(!file.exists()){
            throw new ServiceException(ResultCode.FILE_NOT_EXISTS);
        }
        if (!file.getName().matches(".+\\.(xls|xlsx)")) {
            throw new ServiceException(ResultCode.FILE_MUST_IS_EXCEL);
        }
        ExcelResult excelResult;
        try {
            int startIndex = 0;
            if(fileName.startsWith(templateType))
                startIndex= templateType.length();
            fileName = fileName.substring(startIndex+30);
            excelResult = checkExcel(new FileInputStream(file),fileName,templateType,customParams);
        } catch (FileNotFoundException e) {
            throw new ServiceException(ResultCode.FILE_NOT_EXISTS);
        }
        try {
            file.delete();
        }catch (Exception e){

        }
        return excelResult;
    }

    /**
     * @description: 校验excel
     * @param: [workbook, templateType]
     * @return: com.njwd.financeback.entity.excel.ExcelResult
     * @author: xdy        
     * @create: 2019-06-10 11-07 
     */
    private ExcelResult checkExcel(InputStream inputStream,String fileName,String templateType,Map<String,Object> customParams){
        ExcelResult result = new ExcelResult();
        if(inputStream==null)
            throw new ServiceException(ResultCode.EXCEL_NOT_CORRECT);

        ExcelData excelData = new ExcelData();
        excelData.setFileName(fileName);
        //自定义请求参数
        excelData.setCustomParams(customParams);
        CheckExtend checkExtend = ExtendFactory.getCheckExtend(templateType);
        //忽略系统配置及校验
        if(checkExtend!=null&&!checkExtend.isSystemCheck()){
            ExcelTemplate excelTemplate = new ExcelTemplate();
            excelTemplate.setType(templateType);
            excelData.setExcelTemplate(excelTemplate);
            excelData.setSystemCheck(false);
        }else { //获取系统配置
            //获取模板
            ExcelTemplate excelTemplate = findExcelTemplate0(templateType);
            if (excelTemplate == null)
                throw new ServiceException(ResultCode.EXCEL_TEMPLATE_NOT_EXISTS);
            //获取该类型excel校验规则
            List<ExcelRule> rules = findExcelRule0(templateType);
            if (rules == null || rules.isEmpty())
                throw new ServiceException(ResultCode.EXCEL_RULE_NOT_EXISTS);
            //模板字段
            List<String> columns = new ArrayList<>();
            for(ExcelRule rule:rules){
                columns.add(rule.getBusinessColumn());
            }
            excelTemplate.setBusinessColumns(columns);
            excelData.setExcelTemplate(excelTemplate);
            excelData.setExcelRuleList(rules);
            excelData.setSystemCheck(true);
        }
        try {
            ExcelRead excelRead = ExcelReadFactory.getExcelRead(ExcelRead.EXCEL_READ_SAX);
            excelRead.read(inputStream,excelData);
            //缓存解析结果
            String uuid = UUID.randomUUID().toString();
            RedisUtils.set(excelCacheKey(uuid),excelData,10,TimeUnit.MINUTES);
            result.setUuid(uuid);
            if (!excelData.getExcelErrorList().isEmpty()) {
                result.setIsOk(Constant.Is.NO);
                result.setMessage(String.format("导入发生错误，其中可导入成功%d条，导入失败%d条",
                        excelData.getExcelRowDataList().size(), excelData.getExcelErrorList().size()));
            } else {
                result.setIsOk(Constant.Is.YES);
            }
        }catch (ServiceException e){
            throw e;
        }catch (ExcelAnalysisException e){
            if(e.getCause()!=null && e.getCause() instanceof ServiceException){
                throw (ServiceException)e.getCause();
            }
            RuntimeException exception = new ServiceException(ResultCode.EXCEL_PARSE_CORRECT);
            exception.initCause(e);
            throw exception;
        }catch (Exception e) {
            RuntimeException exception = new ServiceException(ResultCode.OPERATION_FAILURE);
            exception.initCause(e);
            throw exception;
        }
        return result;
    }


    /**
     * @description: 导入excel数据
     * @param: [uuid]
     * @return: com.njwd.financeback.entity.excel.ExcelResult
     * @author: xdy
     * @create: 2019-05-20 16-19
     */
    @Override
    public ExcelResult importExcel(String uuid) {
        FastUtils.checkParams(uuid);
        ExcelData excelData = (ExcelData)RedisUtils.getObj(excelCacheKey(uuid));//JsonUtils.json2Pojo(RedisUtils.get(uuid), ExcelData.class);
        if (excelData == null)
            throw new ServiceException(ResultCode.EXCEL_DATA_NOT_EXISTS);

        ExcelResult result = new ExcelResult();
        //导入数据
        DataAddManager dataAddManager = new DataAddManager(excelData);
        dataAddManager.boot0();
        //清空缓存
        RedisUtils.remove(excelCacheKey(uuid));
        //获取结果
        List<ExcelRowData> successList = dataAddManager.getSuccessList();
        List<ExcelError> errorList = dataAddManager.getErrorList();
        if(dataAddManager.isAddOk()){
            result.setIsOk(Constant.Is.YES);
            //result.setMessage();
        }else{
            String errorUuid = UUID.randomUUID().toString();
            ExcelData errorData = new ExcelData();
            errorData.setMultiSheet(excelData.isMultiSheet());
            errorData.setExcelErrorList(errorList);
            RedisUtils.set(excelCacheKey(errorUuid), errorData, 10, TimeUnit.MINUTES);
            result.setIsOk(Constant.Is.NO);
            result.setMessage(String.format("导入失败，其中可导入成功%d条，导入失败%d条", successList.size(), errorList.size()));
            result.setUuid(errorUuid);
        }

        return result;
    }


    /**
     * @description: 下载excel结果
     * @param: [uuid]
     * @return: org.springframework.http.ResponseEntity<byte [ ]>
     * @author: xdy
     * @create: 2019-05-22 16-40
     */
    @Override
    public ResponseEntity<byte[]> downloadExcelResult(String uuid) {
        FastUtils.checkParams(uuid);
        ExcelData excelData = (ExcelData)RedisUtils.getObj(excelCacheKey(uuid));
        StringBuffer sb = new StringBuffer();
        if (excelData != null && excelData.getExcelErrorList() != null && !excelData.getExcelErrorList().isEmpty()) {
            Collections.sort(excelData.getExcelErrorList(), Comparator.comparing(ExcelError::getRowNum));
            excelData.getExcelErrorList().forEach(excelError -> {
                if(excelData.isMultiSheet()){
                    sb.append("sheet:").append(excelError.getSheetName()).append(",");
                }
                sb.append("第").append(excelError.getRowNum()+1).append("行");
                if(excelError.getCellNum()>=0){
                    sb.append("第").append(excelError.getCellNum()+1).append("列[")
                            .append(toLetter(excelError.getCellNum())).append("]数据：")
                            .append(excelError.getData());
                }
                    sb.append(",错误原因：")
                        .append(excelError.getErrorMsg()).append("。\n");
            });
        } else {
            sb.append("未找到错误数据。");
        }
        byte[] data = sb.toString().getBytes();
        return ResponseEntity.ok()
                .header("Content-Disposition", String.format("attachment;filename=excel_result.txt"))
                .contentLength(data.length)
                .contentType(MediaType.parseMediaType("multipart/form-data"))
                .body(data);
    }

    private String toLetter(int colIndex){
        return colIndex<26?excelColumn[colIndex]:excelColumn[colIndex/26-1]+excelColumn[colIndex%26];
    }


    /**
     * @description: 获取模板校验规则
     * @param: [templateType]
     * @return: java.util.List<com.njwd.financeback.entity.excel.ExcelRule>
     * @author: xdy
     * @create: 2019-05-17 15-34
     */
    @Override
    public List<ExcelRule> findExcelRule(String templateType) {
        return fileMapper.findExcelRule(Wrappers.query().eq("type", templateType).orderByAsc("seri"));
    }

    private List<ExcelRule> findExcelRule0(String templateType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map= new LinkedMultiValueMap();
        map.add("templateType", templateType);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity(map,headers);
        ParameterizedTypeReference<Result<List<ExcelRule>>> responseBodyType = new ParameterizedTypeReference<Result<List<ExcelRule>>>(){};
        ResponseEntity<Result<List<ExcelRule>>> resp   =  restTemplate.exchange("http://"+Constant.Context.PLATFORM_FEIGN+"/platform/file/findExcelRule", HttpMethod.POST,request,responseBodyType);
        Result<List<ExcelRule>> result = resp.getBody();
        if(result.getCode()==Result.SUCCESS){
            List<ExcelRule> list = result.getData();
            for(int i=0;i<list.size();i++){
                ExcelRule excelRule = list.get(i);
                excelRule.setSeri(i);
            }
            return list;
        }
        return null;
    }

    /**
     * @description: 获取excel模板
     * @param: [templateType]
     * @return: com.njwd.financeback.entity.excel.ExcelTemplate
     * @author: xdy
     * @create: 2019-05-21 09-55
     */
    @Override
    public ExcelTemplate findExcelTemplate(String templateType) {
        return fileMapper.selectOne(new LambdaQueryWrapper<ExcelTemplate>()
                .eq(ExcelTemplate::getType, templateType));
    }

    private ExcelTemplate findExcelTemplate0(String templateType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map= new LinkedMultiValueMap();
        map.add("templateType", templateType);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity(map,headers);
        ParameterizedTypeReference<Result<ExcelTemplate>> responseBodyType = new ParameterizedTypeReference<Result<ExcelTemplate>>(){};
        ResponseEntity<Result<ExcelTemplate>> resp = restTemplate.exchange("http://"+Constant.Context.PLATFORM_FEIGN+"/platform/file/findExcelTemplate",HttpMethod.POST,request,responseBodyType);
        Result<ExcelTemplate> result = resp.getBody();
        if(result.getCode()==Result.SUCCESS){
            return result.getData();
        }
        return null;
    }

    @Override
    public void resetPage(Page page) {
        page.setCurrent(1);
        page.setSearchCount(false);
        page.setSize(DataGet.MAX_PAGE_SIZE);
    }

    /**
     * @description: 导出excel
     * @param: [response, datas, excelColumns]
     * @return: void 
     * @author: xdy        
     * @create: 2019-06-12 17-09 
     */
    @Override
    public <T> void exportExcel(HttpServletResponse response, List<T> datas,ExcelColumn... excelColumns){
        exportExcel(response,datas,String.valueOf(System.currentTimeMillis()),excelColumns);
    }

    @Override
    public <T> void exportExcel(HttpServletResponse response, List<T> datas, List<ExcelColumn> excelColumns){
        if(excelColumns==null)
            excelColumns = new ArrayList<>();
        ExcelColumn[] excelColumnArray = new ExcelColumn[excelColumns.size()];
        excelColumns.toArray(excelColumnArray);
        exportExcel(response,datas,excelColumnArray);
    }
    
    /**
     * @description: 导出excel
     * @param: [response, datas, fileName, excelColumns]
     * @return: void 
     * @author: xdy        
     * @create: 2019-09-05 17:30 
     */
    @Override
    public <T> void exportExcel(HttpServletResponse response, List<T> datas, String fileName,ExcelColumn... excelColumns){
        exportExcel(response,datas,fileName,null,excelColumns);
    }

    /**
     * @description: 导出excel
     * @param: [response, datas, excelColumns]
     * @return: void
     * @author: xdy
     * @create: 2019-06-12 17-09
     */
    @Override
    public <T> void exportExcel(HttpServletResponse response, List<T> datas, String fileName, List<List<String>> excelHead, ExcelColumn... excelColumns) {
        try (ServletOutputStream out= response.getOutputStream()) {
            if(excelHead==null)
                excelHead = new ArrayList<>();
            for(int i=0;i<excelColumns.length;i++){
                ExcelColumn column = excelColumns[i];
                List<String> list = null;
                if(i<excelHead.size())
                    list = excelHead.get(i);
                if(list==null)
                    excelHead.add(Arrays.asList(column.getTitle()));
                else{
                    list.add(column.getTitle());
                }
            }
            response.setContentType("multipart/form-data");
            response.setCharacterEncoding("utf-8");
            //解决下载附件中文名称变成下划线
            response.addHeader("Content-Disposition", "attachment;filename=" +
                    new String(fileName.getBytes("utf-8"),"iso-8859-1")+ ".xlsx");
            /**--该方法会导致excel文件名指定的是中文汉字导出的时候会变成下划线,所以给注掉了
            response.setHeader("Content-disposition", "attachment;filename="+fileName+".xlsx");**/
            ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX, true);
            com.alibaba.excel.metadata.Sheet sheet1 = new com.alibaba.excel.metadata.Sheet(1, 0);
            sheet1.setSheetName("Sheet1");
            sheet1.setHead(excelHead);
            sheet1.setAutoWidth(Boolean.TRUE);
            DataGetGroup dataGetGroup = new DataGetGroup();
            List<List<Object>> excelData = dataGetGroup.get(datas,excelColumns);
            writer.write1(excelData,sheet1);
            writer.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public <T> void exportExcel(HttpServletResponse response, List<T> datas, String menuCode) {
        exportExcel(response,datas,menuCode, Constant.Is.NO);
    }

    @Override
    public <T> void exportExcel(HttpServletResponse response, List<T> datas, String menuCode,Byte isEnterpriseAdmin) {
        List<ExcelColumn> excelColumns  = findExcelColumn(menuCode,isEnterpriseAdmin);
        exportExcel(response,datas,excelColumns);
    }

    @Override
    public List<ExcelColumn> findExcelColumn(String menuCode){
        return findExcelColumn(menuCode,Constant.Is.NO);
    }

    @Override
    public List<ExcelColumn> findExcelColumn(String menuCode, Byte isEnterpriseAdmin){
        //默认为用户
        if(!Constant.Is.YES.equals(isEnterpriseAdmin)){
            isEnterpriseAdmin = Constant.Is.NO;
        }
        //用户本地设置
        TableConfigQueryDto queryDto = new TableConfigQueryDto();
        queryDto.setMenuCode(menuCode);
        queryDto.setIsEnterpriseAdmin(isEnterpriseAdmin);
        List<SysMenuTabColumn> sysMenuTabColumns = findMenuTab(queryDto);
        List<ExcelColumn> excelColumns = new ArrayList<>();
        if(sysMenuTabColumns!=null&&!sysMenuTabColumns.isEmpty()){
            for(SysMenuTabColumn sysMenuTabColumn:sysMenuTabColumns){
                ExcelColumn excelColumn;
                String columnName;
                if(StringUtils.isBlank(sysMenuTabColumn.getColumnJsonName())){
                    columnName = sysMenuTabColumn.getColumnName();
                }else {
                    columnName = sysMenuTabColumn.getColumnJsonName()+"."+sysMenuTabColumn.getColumnName();
                }
                if (StringUtils.isBlank(sysMenuTabColumn.getConvertType())) {
                    excelColumn = new ExcelColumn(columnName, sysMenuTabColumn.getColumnRemark());
                } else {
                    excelColumn = new ExcelColumn(columnName, sysMenuTabColumn.getColumnRemark(), sysMenuTabColumn.getConvertType());
                }
                excelColumns.add(excelColumn);
            }
        }
        return excelColumns;
    }
    
    /**
     * @description: 获取表格设置
     * @param: [queryDto]
     * @return: java.util.List<com.njwd.entity.basedata.SysMenuTabColumn> 
     * @author: xdy        
     * @create: 2019-08-03 11-49 
     */
    public List<SysMenuTabColumn> findMenuTab(TableConfigQueryDto queryDto){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity(JsonUtils.object2Json(queryDto),headers);
        ParameterizedTypeReference<Result<List<SysMenuTabColumn>>> responseBodyType = new ParameterizedTypeReference<Result<List<SysMenuTabColumn>>>(){};
        ResponseEntity<Result<List<SysMenuTabColumn>>> resp = restTemplate.exchange("http://"+Constant.ServiceName.SERVICE_BASE_DATA+"/financeback/tableConfig/findUserList",HttpMethod.POST,request,responseBodyType);
        Result<List<SysMenuTabColumn>> result = resp.getBody();
        if(result.getCode()==Result.SUCCESS){
            return result.getData();
        }
        return null;
    }

    private String excelCacheKey(String uuid){
        return String.format(Constant.ExcelConfig.EXCEL_KEY_PREFIX,uuid);
    }


    /**
     * 下载excel模版
     *
     * @param request
     * @param response
     * @param fileName 文件名
     * @param suffix   后缀名
     * @throws ServiceException
     */
    @Override
    public void downExcelDemo(HttpServletRequest request, HttpServletResponse response, String fileName, String suffix) throws ServiceException {
        //文件全路径
        StringBuffer buffer = new StringBuffer();
        buffer.append(excelRootPath).append(File.separator).append(fileName).append(suffix);
        String fullPath = buffer.toString();
        File file = new File(fullPath);
        if (!file.exists() || !file.isFile())
            //抛出文件不存在信息
            throw new ServiceException(ResultCode.FILE_NOT_EXISTS);
        response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setContentLength((int) file.length());
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        byte[] buff = new byte[1024];
        BufferedInputStream bis = null;
        OutputStream os = null;
        try {
            os = response.getOutputStream();
            bis = new BufferedInputStream(new FileInputStream(file));
            int i = 0;
            while ((i = bis.read(buff)) != -1) {
                os.write(buff, 0, i);
                os.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 简化封装importExcel方法,因为需要对ExcelData单独处理,
     * 需要调用者调用结束后根据uuid清楚redis缓存
     * @author LuoY
     * @param excelData
     * @return
     */
    @Override
    public ExcelResult importExcelSecond(ExcelData excelData) {
        if (excelData == null)
            throw new ServiceException(ResultCode.EXCEL_DATA_NOT_EXISTS);

        ExcelResult result = new ExcelResult();
        //导入数据
        DataAddManager dataAddManager = new DataAddManager(excelData);
        dataAddManager.boot0();
        //获取结果
        List<ExcelRowData> successList = dataAddManager.getSuccessList();
        List<ExcelError> errorList = dataAddManager.getErrorList();
        if (!errorList.isEmpty()) {
            //缓存错误信息
            String errorUuid = UUID.randomUUID().toString();
            ExcelData errorData = new ExcelData();
            errorData.setExcelErrorList(errorList);
            RedisUtils.set(excelCacheKey(errorUuid), errorData, 10, TimeUnit.MINUTES);
            result.setIsOk(Constant.Is.NO);
            result.setMessage(String.format("导入已完成，其中导入成功%d条，导入失败%d条", successList.size(), errorList.size()));
            result.setUuid(errorUuid);
        } else {
            result.setIsOk(Constant.Is.YES);
        }
        return result;
    }

}

