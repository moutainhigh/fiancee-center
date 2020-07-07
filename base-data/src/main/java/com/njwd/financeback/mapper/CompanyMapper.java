package com.njwd.financeback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.Company;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.CompanyDto;
import com.njwd.entity.basedata.vo.CompanyVo;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Author: Zhuzs
 * @Date: 2019-05-21 15:19
 */
public interface CompanyMapper extends BaseMapper<Company> {

    /**
     * 根据 id/name/code/enterpriseId 查询公司信息
     *
     * @param: [companyDto]
     * @return: com.njwd.entity.basedata.vo.CompanyVo
     * @author: zhuzs
     * @date: 2019-09-16 17:05
     */
    CompanyVo selectByIdOrCodeOrName(@Param("companyDto") CompanyDto companyDto);

    /**
     * 根据ID List 查询公司列表 （若是User 端，则查询已配置的公司列表）
     *
     * @param: [page, companyDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.CompanyVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:05
     */
    Page<CompanyVo> findPage(@Param("page") Page<CompanyVo> page, @Param("companyDto") CompanyDto companyDto);


    /**
     * 根据ID List 查询公司列表 （若是User 端，则查询已配置的公司列表）
     * 排除当前公司和集团
     *
     * @param: [page, companyDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.CompanyVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:05
     */
    Page<CompanyVo> findCompanyPageOut(@Param("page") Page<CompanyVo> page, @Param("companyDto") CompanyDto companyDto);


    /**
     * 根据ID List 查询公司列表
     *
     * @param: [page, companyDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.CompanyVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:05
     */
    Page<CompanyVo> findPageForConfigure(@Param("page") Page<CompanyVo> page, @Param("companyDto") CompanyDto companyDto);

    /**
     * 查询用户有关联权限的公司
     *
     * @param: [userId, rootEnterpriseId]
     * @return: java.util.Map<java.lang.Long, com.njwd.entity.basedata.vo.CompanyVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:05
     */
    @MapKey("id")
    Map<Long, CompanyVo> findCompanyMap(@Param("userId") Long userId, @Param("rootEnterpriseId") Long rootEnterpriseId);

    /**
     * 根据公司名称及租户查询公司ID
     *
     * @param: [name, rootEnterpriseId]
     * @return: com.njwd.entity.basedata.vo.CompanyVo
     * @author: zhuzs
     * @date: 2019-09-16 17:06
     */
    CompanyVo findCompanyByName(@Param("name") String name, @Param("rootEnterpriseId") Long rootEnterpriseId);

    /**
     * 查询租户下的所有公司
     *
     * @param: [rootEnterpriseId]
     * @return: java.util.List<com.njwd.entity.basedata.vo.CompanyVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:06
     */
    List<CompanyVo> findList(@Param("rootEnterpriseId") Long rootEnterpriseId);

    /**
     * @return com.njwd.entity.basedata.vo.CompanyVo
     * @Description 通过编码查询公司
     * @Author 郑勇浩
     * @Data 2019/8/22 15:42
     * @Param [rootEnterpriseId, code]
     */
    CompanyVo findCompanyByCode(@Param("rootEnterpriseId") Long rootEnterpriseId, @Param("code") String code);

    /**
     * 批量新增
     *
     * @param: [companyDtoList]
     * @return: int
     * @author: zhuzs
     * @date: 2019-09-16 17:06
     */
    int addBatch(List<CompanyDto> companyDtoList);

    List<CompanyVo> checkHasSubAccount(@Param("accountBookDto") AccountBookDto accountBookDto);
}
