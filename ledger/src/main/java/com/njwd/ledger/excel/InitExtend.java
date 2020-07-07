package com.njwd.ledger.excel;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.BusinessUnit;
import com.njwd.entity.basedata.dto.AccountBookEntityDto;
import com.njwd.entity.basedata.dto.BusinessUnitDto;
import com.njwd.entity.basedata.dto.CompanyDto;
import com.njwd.entity.basedata.excel.ExcelRequest;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.basedata.vo.CompanyVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.fileexcel.extend.DownloadExtend;
import com.njwd.ledger.cloudclient.AccountBookEntityFeignClient;
import com.njwd.ledger.cloudclient.BusinessUnitFeignClient;
import com.njwd.ledger.cloudclient.CompanyFeignClient;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @description:
 * @author: xdy
 * @create: 2019/10/22 9:30
 */
public abstract class InitExtend implements DownloadExtend {

    @Resource
    AccountBookEntityFeignClient accountBookEntityFeignClient;
    @Resource
    BusinessUnitFeignClient businessUnitFeignClient;
    @Resource
    CompanyFeignClient companyFeignClient;

    protected abstract void writeSheet(ExcelWriter writer);

    /**
     * @description: 下载模板
     * @param: [response, excelRequest]
     * @return: void
     * @author: xdy
     * @create: 2019-10-17 14:39
     */
    @Override
    public void download(HttpServletResponse response, ExcelRequest excelRequest) {
        //获取excel文件名
        String excelName =  findExcelName(excelRequest.getAccountBookEntityId());
        if(excelName==null)
            throw new ServiceException(null);
        //生成
        try(ServletOutputStream out= response.getOutputStream()){
            response.setContentType("multipart/form-data");
            response.setCharacterEncoding("utf-8");
            response.addHeader("Content-Disposition", "attachment; filename=" +
                    new String(excelName.getBytes("utf-8"),"iso-8859-1")+ ".xlsx");
            ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX, true);
            writeSheet(writer);
            writer.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * @description: 期初模板名称
     * @param: [accountBookEntityId]
     * @return: java.lang.String
     * @author: xdy
     * @create: 2019-10-17 14:37
     */
    protected String findExcelName(Long accountBookEntityId){
        AccountBookEntityDto accountBookEntityDto = new AccountBookEntityDto();
        accountBookEntityDto.setId(accountBookEntityId);
        AccountBookEntityVo accountBookEntityVo = accountBookEntityFeignClient.findAccountBookEntityById(accountBookEntityDto).getData();
        String excelName = null;
        if(accountBookEntityVo!=null){
            String entityName=null;
            if(Constant.AccountBookEntityInfo.FORM_COMPANY.equals(accountBookEntityVo.getForm())){
                CompanyDto companyDto = new CompanyDto();
                companyDto.setId(accountBookEntityVo.getEntityId());
                CompanyVo companyVo = companyFeignClient.findCompanyById(companyDto).getData();
                entityName = companyVo.getName();
            }else if(Constant.AccountBookEntityInfo.FORM_UNIT.equals(accountBookEntityVo.getForm())){
                BusinessUnitDto businessUnitDto = new BusinessUnitDto();
                businessUnitDto.setId(accountBookEntityVo.getEntityId());
                businessUnitDto.setIsEnterpriseAdmin(Constant.Is.NO);
                BusinessUnit businessUnit = businessUnitFeignClient.findBusinessUnitById(businessUnitDto).getData();
                entityName = businessUnit.getName();
            }
            if(entityName!=null&&accountBookEntityVo.getAccountBookName()!=null){
                excelName = accountBookEntityVo.getAccountBookName()+"-"+entityName;
            }
        }
        return excelName;
    }

    protected void checkFileName(Long accountBookEntityId,String importFileName){
        String fileName = findExcelName(accountBookEntityId);
        importFileName = importFileName.substring(0,importFileName.indexOf("."));
        if(!fileName.equals(importFileName)){
            throw new ServiceException("文件名应为：“"+fileName+"“，请检查文件！", ResultCode.OPERATION_FAILURE);
        }
    }

}
