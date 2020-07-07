package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.basedata.CustomerSupplierCompany;
import com.njwd.entity.basedata.SupplierCompany;
import com.njwd.entity.basedata.dto.CustomerSupplierCompanyDto;
import com.njwd.entity.basedata.dto.CustomerSupplierDto;
import com.njwd.entity.basedata.vo.SupplierCompanyVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description 客户供应商使用公司处理
 * @Author 朱小明
 * @Date 2019/6/27 9:16
 * @Param
 * @return
 **/
public interface SupplierCompanyMapper
        extends BaseMapper<SupplierCompany> {

    /**
     * @Description 以使用公司维度校验是否存在已存在数据
     * @Author 朱小明
     * @Date 2019/7/2 14:55
     * @Param [customerSupplierCompany]
     * @return java.lang.Long
     **/
    Long selectSupplierIdForCheckUseCompany(CustomerSupplierCompany customerSupplierCompany);

    int updateSupplierFromMain(@Param("idList") List<Long> idList, @Param("flag") int flag);

    List<SupplierCompany> checkSupplierUseCompanys(@Param("dto") CustomerSupplierDto dto);

    int deleteBySupplierIdAndCompanyId(@Param("dto") CustomerSupplierCompanyDto dto);

    List<SupplierCompany> selectAllCompaniesBySupplierIds(@Param("dto") CustomerSupplierCompanyDto dto);

    List<SupplierCompanyVo> findSupplierUseCompanysBySupplierIds(@Param("dto") CustomerSupplierCompanyDto dto);

    List<Long> findSupplierCompanyInfos(@Param("dto") CustomerSupplierDto dto);

    Integer findSupplierCompanyInfosWithOutSelf(@Param("dto")CustomerSupplierCompanyDto dto);

    List<SupplierCompanyVo> selectSupplierCompanyByParams(@Param("dto")CustomerSupplierCompanyDto dto);

    /**
     * @Description 查询已被分配数据
     * @Author 朱小明
     * @Date 2019/10/9
     * @param idList
     * @return java.util.List<java.lang.Long>
     **/
    List<Long> selectForFilterFp(List<Long> idList);

    /**
     * 根据条件查询供应商使用公司信息
     *
     * @param dto
     * @return
     */
    List<SupplierCompanyVo> findSupplierCompanyList(@Param("dto") CustomerSupplierDto dto);
}