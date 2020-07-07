package com.njwd.basedata.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.dto.CustomerSupplierDto;
import com.njwd.entity.basedata.vo.CustomerSupplierVo;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Set;

/**
 * 基础资料的客户和供应商控制层.
 * @author 朱小明
 * @since 2019/6/11
 */
@FeignClient(name = Constant.Context.BASE_DATA_FEIGN, contextId = "CustomerSupplierApi")
@RequestMapping(Constant.Context.BASE_DATA + "customerSupplier")
public interface CustomerSupplierApi {

    /**
     * @Description 新增客户供应商.
     * @Author 朱小明
     * @Date 2019/6/12 18:01
     * @Param [customerSupplierDto]
     * @return java.lang.String
     **/
    @PostMapping("addCustomerSupplier")
    Result<Long> addCustomerSupplier(CustomerSupplierDto customerSupplierDto);

    /**
     * @Description 批量删除
     * @Author 朱小明
     * @Date 2019/6/18 9:38
     * @Param [customerSupplierDto]
     * @return java.lang.String
     **/
    @PostMapping("deleteCustomerSupplierByIds")
    Result<BatchResult> deleteCustomerSupplierByIds(CustomerSupplierDto customerSupplierDto);
    /**
     * @Description 根据ID更新客户供应商
     * @Author 朱小明
     * @Date 2019/6/19 15:54
     * @Param [customerSupplierDto]
     * @return java.lang.String
     **/
    @PostMapping("updateCustomerSupplierById")
    Result<Integer> updateCustomerSupplierById(CustomerSupplierDto customerSupplierDto);

    /**
     * @Description 批量启用
     * @Author 朱小明
     * @Date 2019/6/18 9:38
     * @Param [customerSupplierDto]
     * @return java.lang.String
     **/
    @PostMapping("enableCustomerSupplierByIds")
    Result<BatchResult> enableCustomerSupplierByIds(CustomerSupplierDto customerSupplierDto);

    /**
     * @Description 批量禁用
     * @Author 朱小明
     * @Date 2019/6/18 9:38
     * @Param [customerSupplierDto]
     * @return java.lang.String
     **/
    @PostMapping("disableCustomerSupplierByIds")
    Result<BatchResult> disableCustomerSupplierByIds(CustomerSupplierDto customerSupplierDto);

    /**
     * @Description 根据id查询客户供应商信息
     * @Author 朱小明
     * @Date 2019/6/18 9:38
     * @Param [customerSupplierDto]
     * @return java.lang.String
     **/
    @PostMapping("findCustomerSupplierById")
    Result<CustomerSupplierVo> findCustomerSupplierById(CustomerSupplierDto customerSupplierDto);

    /**
     * @Description 查询列表（分页）
     * @Author 朱小明
     * @Date 2019/6/18 9:38
     * @Param [customerSupplierDto]
     * @return java.lang.String
     **/
    @PostMapping("findCustomerSupplierPage")
    Result<Page<CustomerSupplierVo>> findCustomerSupplierPage(CustomerSupplierDto customerSupplierDto);

    /**
     * @Description 校验字段重复
     * @Author 朱小明
     * @Date 2019/6/12 18:01
     * @Param [customerSupplierDto]
     * @return java.lang.String
     **/
    @PostMapping("checkColumn")
    Result<Set<String>> checkColumn(CustomerSupplierDto customerSupplierDto);



}
