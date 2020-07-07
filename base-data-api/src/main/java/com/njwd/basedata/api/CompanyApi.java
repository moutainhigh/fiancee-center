package com.njwd.basedata.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.Company;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.CompanyDto;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.basedata.vo.CompanyVo;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


/**
 * @Description 公司Api
 * @Author 朱小明
 * @Date 2019/8/1 9:07
 **/
@RequestMapping("financeback/company")
public interface CompanyApi {

    /**
     * 新增公司
     * @param company
     * @return
     */
    @RequestMapping("addCompany")
    Result<Company> addCompany(@RequestBody Company company);

    /**
     * 批量删除
     * @param companyDto
     * @return
     */
    @RequestMapping("deleteBatch")
    Result<BatchResult> deleteBatch(@RequestBody CompanyDto companyDto);

    /**
     * 修改公司信息
     * @param company
     * @return
     */
    @RequestMapping("updateCompany")
    Result<Integer> updateCompany(@RequestBody Company company);

    /**
     * 公司建账
     * @param companyDto
     * @return
     */
    @RequestMapping("enableAccountBook")
    Result<Integer> enableAccountBook(@RequestBody CompanyDto companyDto);

    /**
     * 批量建账
     * @param companyDtoList
     * @return
     */
    @RequestMapping("enableAccountBookBatch")
    Result<BatchResult> enableAccountBookBatch(@RequestBody List<CompanyDto> companyDtoList);

    /**
     * 获取 公司形态/公司类型/会计准则(含税制、本位币)/纳税人资质 列表
     */
    @RequestMapping("findSysAuxDataList")
    Result<Map<String,Object>> findSysAuxDataList();

    /**
     * 根据 ID 查找公司
     * @param companyDto
     * @return
     */
    @RequestMapping("findCompanyById")
    Result<CompanyVo> findCompanyById(@RequestBody CompanyDto companyDto);

    /**
     * 获取公司列表，分页
     * @param companyDto
     * @return
     */
    @RequestMapping("findCompanyPage")
    Result<Page<CompanyVo>> findCompanyPage(@RequestBody CompanyDto companyDto);

    /**
     * 获取核算主体列表
     * @param accountBookDtos
     * @return
     */
    @RequestMapping("findAccountBookEntityList")
    Result<List<AccountBookEntityVo>> findAccountBookEntityList(@RequestBody List<AccountBookDto> accountBookDtos);

    /**
     * @Description 根据账簿ID查询使用公司信息
     * @Author 朱小明
     * @Date 2019/8/20 14:27
     * @Param [accountBookDto]
     * @return com.njwd.support.Result<com.njwd.entity.basedata.vo.CompanyVo>
     **/
    @PostMapping("checkHasSubAccount")
    Result<CompanyVo> checkHasSubAccount(@RequestBody AccountBookDto accountBookDto);


}