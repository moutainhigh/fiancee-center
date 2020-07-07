package com.njwd.financeback.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.basedata.Company;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.CompanyDto;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.basedata.vo.CompanyVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.financeback.service.CompanyService;
import com.njwd.logger.SenderService;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.HttpUtils;
import com.njwd.utils.RedisUtils;
import com.njwd.utils.ShiroUtils;
import com.njwd.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


/**
 * 公司 controller
 * @Author: Zhuzs
 * @Date: 2019-05-15 11:06
 */
@RestController
@RequestMapping("company")
public class CompanyController extends BaseController {

    @Autowired
    private CompanyService  companyService;
    @Autowired
    private SenderService senderService;

    /**
     * 新增公司
     *
     * @param: [company]
     * @return: com.njwd.support.Result<com.njwd.entity.basedata.Company>
     * @author: zhuzs
     * @date: 2019-09-16 16:56
     */
    @RequestMapping("addCompany")
    public Result<Company> addCompany(@RequestBody Company company){
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        company.setRootEnterpriseId(operator.getRootEnterpriseId());
        Company result = companyService.addCompany(company);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.company,
                LogConstant.operation.add,
                LogConstant.operation.add_type,
                String.valueOf(result.getId())));
        return ok(result);
    }

    /**
     * 批量删除
     *
     * @param: [companyDto]
     * @return: com.njwd.support.Result<com.njwd.support.BatchResult>
     * @author: zhuzs
     * @date: 2019-09-16 16:56
     */
    @RequestMapping("deleteBatch")
    public Result<BatchResult> deleteBatch(@RequestBody CompanyDto companyDto){
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        BatchResult result;
        if(Constant.Number.ONE.equals(companyDto.getIdList().size())){
            result = companyService.delete(companyDto);
        }else{
            result = companyService.deleteBatch(companyDto);
            // 清除缓存
            RedisUtils.removeBatch(Constant.RedisCache.COMPANY,result.getSuccessList());
        }
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.company,
                LogConstant.operation.deleteBatch,
                LogConstant.operation.deleteBatch_type,
                String.valueOf(result.getSuccessList())));
        return ok(result);
    }

    /**
     * 修改公司信息
     *
     * @param: [company]
     * @return: com.njwd.support.Result<java.lang.Integer>
     * @author: zhuzs
     * @date: 2019-09-16 16:56
     */
    @RequestMapping("updateCompany")
    public Result<Integer> updateCompany(@RequestBody Company company){
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        company.setRootEnterpriseId(operator.getRootEnterpriseId());
        Integer result = companyService.updateCompany(company);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.company,
                LogConstant.operation.update,
                LogConstant.operation.update_type,
                String.valueOf(company.getId())));
        return  ok(result);
    }

    /**
     * 公司建账
     *
     * @param: [companyDto]
     * @return: com.njwd.support.Result<java.lang.Integer>
     * @author: zhuzs
     * @date: 2019-09-16 16:56
     */
    @RequestMapping("enableAccountBook")
    public Result<Integer> enableAccountBook(@RequestBody CompanyDto companyDto){
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        companyDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        Integer result = companyService.enableAccountBook(companyDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.company,
                LogConstant.operation.addAccounting,
                LogConstant.operation.addAccounting_type,
                String.valueOf(companyDto.getId())));
        return ok(result);
    }

    /**
     * 批量建账
     *
     * @param: [companyDtoList]
     * @return: com.njwd.support.Result<com.njwd.support.BatchResult>
     * @author: zhuzs
     * @date: 2019-09-16 16:56
     */
    @RequestMapping("enableAccountBookBatch")
    public Result<BatchResult> enableAccountBookBatch(@RequestBody List<CompanyDto> companyDtoList){
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        BatchResult result = companyService.enableAccountBookBatch(companyDtoList);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,
                LogConstant.menuName.company,
                LogConstant.operation.addAccountingBatch,
                LogConstant.operation.addAccountingBatch_type,
                String.valueOf(result.getSuccessList())));
        return ok(result);
    }

    /**
     * 获取 公司形态/公司类型/会计准则(含税制、本位币)/纳税人资质 列表
     *
     * @param: []
     * @return: com.njwd.support.Result<java.util.Map<java.lang.String,java.lang.Object>>
     * @author: zhuzs
     * @date: 2019-09-16 16:56
     */
    @RequestMapping("findSysAuxDataList")
    public Result<Map<String,Object>> findSysAuxDataList(){
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        return ok(companyService.findSysAuxDataList(operator));
    }

    /**
     * 根据 ID 查找公司
     *
     * @param: [companyDto]
     * @return: com.njwd.support.Result<com.njwd.entity.basedata.vo.CompanyVo>
     * @author: zhuzs
     * @date: 2019-09-16 16:57
     */
    @RequestMapping("findCompanyById")
    public Result<CompanyVo> findCompanyById(@RequestBody CompanyDto companyDto){
        return ok(companyService.findCompanyByIdOrCodeOrName(companyDto));
    }

    /**
     * 根据ID List 查询公司列表 （若是User 端，则查询已配置的公司列表）
     *
     * @param: [companyDto]
     * @return: com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.CompanyVo>>
     * @author: zhuzs
     * @date: 2019-09-16 16:57
     */
    @RequestMapping("findCompanyPage")
    public Result<Page<CompanyVo>> findCompanyPage(@RequestBody CompanyDto companyDto){
	    SysUserVo operator = UserUtils.getUserVo();
        companyDto.setRootEnterpriseId(operator.getRootEnterpriseId());
	    if (companyDto.getIsEnterpriseAdmin() != null && Constant.Is.NO.equals(companyDto.getIsEnterpriseAdmin())) {
		    // 非业务管理员时，添加userId，在查询时仅查询有对应公司权限的数据
		    companyDto.setUserId(operator.getUserId());
	    } else {
		    ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
	    }
	    return ok(companyService.findPage(companyDto));
    }


    /**
     * 根据ID List 查询公司列表 （若是User 端，则查询已配置的公司列表）
     *
     * @param: [companyDto]
     * @return: com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.CompanyVo>>
     * @author: zhuzs
     * @date: 2019-09-16 16:57
     */
    @RequestMapping("findCompanyPageOut")
    public Result<Page<CompanyVo>> findCompanyPageOut(@RequestBody CompanyDto companyDto){
        SysUserVo operator = UserUtils.getUserVo();
        companyDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        if (companyDto.getIsEnterpriseAdmin() != null && Constant.Is.NO.equals(companyDto.getIsEnterpriseAdmin())) {
            // 非业务管理员时，添加userId，在查询时仅查询有对应公司权限的数据
            companyDto.setUserId(operator.getUserId());
        } else {
            ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        }
        return ok(companyService.findCompanyPageOut(companyDto));
    }

    /**
     * 根据ID List 查询公司列表
     *
     * @param: [companyDto]
     * @return: com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.CompanyVo>>
     * @author: zhuzs
     * @date: 2019-09-16 16:57
     */
    @RequestMapping("findPageForConfigure")
    public Result<Page<CompanyVo>> findPageForConfigure(@RequestBody CompanyDto companyDto){
        return ok(companyService.findPageForConfigure(companyDto));
    }

    /**
     * 获取核算主体列表
     *
     * @param: [accountBookDtos]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.basedata.vo.AccountBookEntityVo>>
     * @author: zhuzs
     * @date: 2019-09-16 16:58
     */
    @RequestMapping("findAccountBookEntityList")
    public Result<List<AccountBookEntityVo>> findAccountBookEntityList(@RequestBody List<AccountBookDto> accountBookDtos){
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        return ok(companyService.findAccountBookEntityList(accountBookDtos));
    }

    /**
     * 导出excel
     *
     * @param: [companyDto, response]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 16:58
     */
    @RequestMapping("exportExcel")
    public void exportExcel(@RequestBody CompanyDto companyDto, HttpServletResponse response){
        SysUserVo operator = UserUtils.getUserVo();
        if (companyDto.getIsEnterpriseAdmin() != null && Constant.Is.NO.equals(companyDto.getIsEnterpriseAdmin())) {
            // 非业务管理员时，添加userId，在查询时仅查询有对应公司权限的数据
            companyDto.setUserId(operator.getUserId());
        } else {
            ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        }
        companyService.exportExcel(companyDto,response);
    }

    /**
     * 根据公司ID/企业ID 查询公司信息
     *
     * @param: [rootEnterpriseId]
     * @return: com.njwd.support.Result
     * @author: zhuzs
     * @date: 2019-09-16 16:58
     */
    @RequestMapping("findCompanyByEnterprise")
    public Result findCompanyByEnterprise(Long rootEnterpriseId){
        return ok(companyService.findCompanyByEnterprise(rootEnterpriseId));
    }

    /**
     * @Description 根据账簿ID查询使用公司信息
     * @Author 朱小明
     * @Date 2019/8/20 14:27
     * @Param [accountBookDto]
     * @return com.njwd.support.Result<com.njwd.entity.basedata.vo.CompanyVo>
     **/
    @PostMapping("checkHasSubAccount")
    public Result<CompanyVo> checkHasSubAccount(@RequestBody AccountBookDto accountBookDto){
        return ok(companyService.checkHasSubAccount(accountBookDto));
    }

}