package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.basedata.CustomerCompany;
import com.njwd.entity.basedata.CustomerSupplierCompany;
import com.njwd.entity.basedata.dto.CustomerSupplierCompanyDto;
import com.njwd.entity.basedata.dto.CustomerSupplierDto;
import com.njwd.entity.basedata.vo.CustomerCompanyVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description 客户供应商使用公司处理
 * @Author 朱小明
 * @Date 2019/6/27 9:16
 * @Param
 * @return
 **/
public interface CustomerCompanyMapper
        extends BaseMapper<CustomerCompany> {

    /**
     * @return java.lang.Long
     * @Description 以使用公司维度校验是否存在已存在数据
     * @Author 朱小明
     * @Date 2019/7/2 14:55
     * @Param [customerSupplierCompany]
     **/
    Long selectCustomerIdForCheckUseCompany(CustomerSupplierCompany customerSupplierCompany);

    int updateCustomerFromMain(@Param("idList") List<Long> idList, @Param("flag") int flag);

    List<CustomerCompany> selectAllCompaniesByCustomerIds(@Param("dto") CustomerSupplierCompanyDto dto);

    List<CustomerCompany> checkCustomerUseCompanys(@Param("dto") CustomerSupplierDto dto);

    int deleteByCustomerIdAndCompanyId(@Param("dto") CustomerSupplierCompanyDto dto);

    List<CustomerCompanyVo> findCustomerUseCompanysByCustomerIds(@Param("dto") CustomerSupplierCompanyDto dto);

    List<Long> findCustomerCompanyInfos(@Param("dto") CustomerSupplierDto dto);

    Integer findCustomerCompanyInfosWithOutSelf(@Param("dto") CustomerSupplierCompanyDto dto);

    List<CustomerCompanyVo> selectCustomerCompanyByParams(@Param("dto") CustomerSupplierCompanyDto dto);

    /**
     * @param idList
     * @return java.util.List<java.lang.Long>
     * @Description 查询已被分配数据
     * @Author 朱小明
     * @Date 2019/10/9
     **/
    List<Long> selectForFilterFp(List<Long> idList);

    /**
     * 根据条件查询客户使用公司信息
     *
     * @param dto
     * @return
     */
    List<CustomerCompanyVo> findCustomerCompanyList(@Param("dto") CustomerSupplierDto dto);
}