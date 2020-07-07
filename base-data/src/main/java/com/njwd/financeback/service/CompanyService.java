package com.njwd.financeback.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.AccountBook;
import com.njwd.entity.basedata.Company;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.CompanyDto;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.basedata.vo.CompanyVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.dto.FinancialReportDto;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @Author: Zhuzs
 * @Date: 2019-05-15 11:08
 */
public interface CompanyService {

    /**
     * 新增公司
     *
     * @param: [company]
     * @return: com.njwd.entity.basedata.Company
     * @author: zhuzs
     * @date: 2019-09-16 16:58
     */
    Company addCompany(Company company);

    /**
     * 删除
     *
     * @param: [companyDto]
     * @return: com.njwd.support.BatchResult
     * @author: zhuzs
     * @date: 2019-09-16 16:58
     */
    BatchResult delete(CompanyDto companyDto);

    /**
     * 批量删除
     *
     * @param: [companyDto]
     * @return: com.njwd.support.BatchResult
     * @author: zhuzs
     * @date: 2019-09-16 16:59
     */
    BatchResult deleteBatch(CompanyDto companyDto);

    /**
     * 修改公司信息
     *
     * @param: [company]
     * @return: int
     * @author: zhuzs
     * @date: 2019-09-16 16:59
     */
    int updateCompany(Company company);

    /**
     * 删除账簿后 修改公司建账状态
     *
     * @param: [idList]
     * @return: int
     * @author: zhuzs
     * @date: 2019-09-16 16:59
     */
    int updateBatch(List<Long> idList);

    /**
     * 公司建账
     *
     * @param: [companyDto]
     * @return: int
     * @author: zhuzs
     * @date: 2019-09-16 16:59
     */
    int enableAccountBook(CompanyDto companyDto);

    /**
     * 批量建账
     *
     * @param: [companyDtoList]
     * @return: com.njwd.support.BatchResult
     * @author: zhuzs
     * @date: 2019-09-16 16:59
     */
    BatchResult enableAccountBookBatch(List<CompanyDto> companyDtoList);

    /**
     * 根据 ID 查询公司
     *
     * @param: [companyDto]
     * @return: com.njwd.entity.basedata.vo.CompanyVo
     * @author: zhuzs
     * @date: 2019-09-16 16:59
     */
    CompanyVo findCompanyByIdOrCodeOrName(CompanyDto companyDto);

    /**
     * 获取公司列表 分页
     *
     * @param: [companyDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.CompanyVo>
     * @author: zhuzs
     * @date: 2019-09-16 16:59
     */
    Page<CompanyVo> findPage(CompanyDto companyDto);


    /**
     * 获取公司列表 分页  排除当前公司
     *
     * @param: [companyDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.CompanyVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:00
     */
    Page<CompanyVo> findCompanyPageOut(CompanyDto companyDto);


    /**
     * 查询公司列表
     *
     * @param: [companyDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.CompanyVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:00
     */
    Page<CompanyVo> findPageForConfigure(CompanyDto companyDto);

    /**
     * 获取核算账簿列表
     *
     * @param: [accountBookDtos]
     * @return: java.util.List<com.njwd.entity.basedata.vo.AccountBookEntityVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:00
     */
    List<AccountBookEntityVo> findAccountBookEntityList(List<AccountBookDto> accountBookDtos);

    /**
     * 导出
     *
     * @param: [companyDto, response]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 17:00
     */
    void exportExcel(CompanyDto companyDto, HttpServletResponse response);

    /**
     * 获取 公司形态/公司类型/会计准则/纳税人资质 列表
     *
     * @param: [operator]
     * @return: java.util.Map<java.lang.String,java.lang.Object>
     * @author: zhuzs
     * @date: 2019-09-16 17:00
     */
    Map<String,Object>  findSysAuxDataList(SysUserVo operator);

    /**
     * 根据公司ID/企业ID 查询公司信息
     *
     * @param: [rootEnterpriseId]
     * @return: java.util.List<com.njwd.entity.basedata.Company>
     * @author: zhuzs
     * @date: 2019-09-16 17:00
     */
    List<Company> findCompanyByEnterprise(Long rootEnterpriseId);

    /**
     * @Description 校验簿是否启用分账核算
     * @Author 朱小明
     * @Date 2019/8/19 15:54
     * @Param [companyDto]
     * @return CompanyVo
     **/
    CompanyVo checkHasSubAccount(AccountBookDto accountBookDto);

    /**
     * 获取 资产负债表/现金流量表/利润表 ID
     * 
     * @param: [accountBook, platformFinancialReportDto]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-17 15:44
     */
    void getFinancialReportIds(AccountBook accountBook, FinancialReportDto platformFinancialReportDto);


}
