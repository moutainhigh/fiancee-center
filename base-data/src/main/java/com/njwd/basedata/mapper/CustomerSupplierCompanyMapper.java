package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.basedata.CustomerSupplierCompany;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description 客户供应商使用公司处理
 * @Author 朱小明
 * @Date 2019/6/27 9:16
 * @Param
 * @return
 **/
public interface CustomerSupplierCompanyMapper
        extends BaseMapper<CustomerSupplierCompany> {

    /**
     * @Description 以使用公司维度校验是否存在已存在数据
     * @Author 朱小明
     * @Date 2019/7/2 14:55
     * @Param [customerSupplierCompany]
     * @return java.lang.Long
     **/
    Long selectIdForCheckUseCompany(CustomerSupplierCompany customerSupplierCompany);

    int updateFromMain(@Param("idList") List<Long> idList, @Param("flag") int flag);
}