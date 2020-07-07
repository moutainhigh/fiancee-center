package com.njwd.basedata.service;

import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.Customer;
import com.njwd.entity.basedata.dto.CustomerSupplierDto;
import com.njwd.entity.basedata.vo.CustomerSupplierVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @Description 客户供应商模块Service层实现类
 * @Author 朱小明
 * @Date 2019/6/18 16:08
 * @Param
 * @return
 **/
public interface CustomerSupplierService{

    /**
     * @Description 添加客户供应商
     * @Author 朱小明
     * @Date 2019/6/18 16:12
     * @Param [customerSupplierDto]
     * @return java.lang.Long
     */
    Long addCustomerSupplier(CustomerSupplierDto customerSupplierDto);

    /**
     * @Description 批量添加客户供应商
     * @Author wuweiming
     * @Date 2019/8/30
     * @Param [customerSupplierDto]
     * @return java.lang.Long
     */
    Long batchAddCustomerSupplier(CustomerSupplierDto customerSupplierDto);

    /**
     * @Description 根据id集合批量删除
     * @Author 朱小明
     * @Date 2019/7/4 21:25
     * @Param [customerSupplierDto]
     * @return com.njwd.support.BatchResult
     **/
    BatchResult updateBatchDelete(CustomerSupplierDto customerSupplierDto);

    /**
     * 升级
     * wuweiming
     * @param customerSupplierDto
     * @return
     */
    Integer upGradeById(CustomerSupplierDto customerSupplierDto);

    /**
     * @Description 根据id集合批量启用禁用
     * @Author 朱小明
     * @Date 2019/7/4 21:25
     * @Param [customerSupplierDto]
     * @return com.njwd.support.BatchResult
     **/
    BatchResult updateBatchEnable(CustomerSupplierDto customerSupplierDto);

    /**
     * @Description 根据ID更新客户供应商(admin)
     * ADMIN:以归属公司维度更新客户供应商
     * @Author 朱小明
     * @Date 2019/6/19 9:36
     * @Param [customerSupplierDto]
     * @return int
     */
    int updateCustomerSupplierById(CustomerSupplierDto customerSupplierDto);

    /**
     * @Description 根据ID更新数据
     * USER:以使用公司维度更新客户供应商
     * @Author 朱小明
     * @Date 2019/6/28 16:02
     * @Param [customerSupplierDto]
     * @return int
     **/
    int updateCustomerSupplierCoById(CustomerSupplierDto customerSupplierDto);

    /**
     * @Description 根据ID查询客户供应商数据
     *  ADMIN:以归属公司维度查询客户供应商
     * @Author 朱小明
     * @Date 2019/6/26 14:55
     * @Param [customerSupplierDto]
     * @return com.njwd.entity.basedata.CustomerSupplier
     **/
    CustomerSupplierVo findCustomerById(Long id);

    /**
     * @Description 根据ID查询客户供应商数据
     * USER:以使用公司维度查询客户供应商
     * @Author 朱小明
     * @Date 2019/6/27 13:23
     * @Param [customerSupplierDto]
     * @return com.njwd.entity.basedata.vo.CustomerSupplierVo
     **/
    CustomerSupplierVo findCustomerCoById(Long id);

    /**
     * @Description 根据ID查询客户供应商数据
     *  ADMIN:以归属公司维度查询客户供应商
     * @Author 朱小明
     * @Date 2019/6/26 14:55
     * @Param [customerSupplierDto]
     * @return com.njwd.entity.basedata.CustomerSupplier
     **/
    CustomerSupplierVo findSupplierById(Long id);

    /**
     * @Description 根据ID查询客户供应商数据
     * USER:以使用公司维度查询客户供应商
     * @Author 朱小明
     * @Date 2019/6/27 13:23
     * @Param [customerSupplierDto]
     * @return com.njwd.entity.basedata.vo.CustomerSupplierVo
     **/
    CustomerSupplierVo findSupplierCoById(Long id);

    /**
     * @Description 查询客户供应商列表（分页）
     * ADMIN:以归属公司维度查询客户供应商
     * @Author 朱小明
     * @Date 2019/6/18 16:14
     * @Param [customerSupplierDto]
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.njwd.entity.basedata.CustomerSupplier>
     */
    Page<CustomerSupplierVo> findCustomerSupplierPage(CustomerSupplierDto customerSupplierDto);

    /**
     * @Description 查询客户供应商列表（分页）
     * USER:以使用公司维度查询客户供应商
     * @Author 朱小明
     * @Date 2019/6/18 16:14
     * @Param [customerSupplierDto]
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.njwd.entity.basedata.CustomerSupplier>
     */
    Page<CustomerSupplierVo> findCustomerSupplierCoPage(CustomerSupplierDto customerSupplierDto);

    /**
     * @Description 校验指定字段数据唯一性
     * @Author 朱小明
     * @Date 2019/6/18 13:42
     * @Param [customerSupplierDto]
     * @return java.lang.Boolean
     */
    Boolean checkDuplicate(CustomerSupplierDto customerSupplierDto);

    /**
     * @Description 管理员(admin画面)导出数据
     * @Author 朱小明
     * @Date 2019/6/28 10:30
     * @Param [customerSupplierDto, response]
     * @return void
     **/
    void exportExcelForAdmin(CustomerSupplierDto customerSupplierDto, HttpServletResponse response);

    /**
     * @Description 管理员(user画面)导出数据
     * @Author 朱小明
     * @Date 2019/6/28 10:30
     * @Param [customerSupplierDto, response]
     * @return void
     **/
    void exportExcelForUser(CustomerSupplierDto customerSupplierDto, HttpServletResponse response);

    /**
     * @description: 批量分配客户
     * @Param [CustomerSupplierDto]
     * @return com.njwd.support.BatchResult
     * @author wuweiming
     * @date 2019/8/24 15:42
     */
    BatchResult updateBatchCustomerUseCompany(CustomerSupplierDto dto);

    /**
     * @description: 批量分配供应商
     * @Param [CustomerSupplierDto]
     * @return com.njwd.support.BatchResult
     * @author wuweiming
     * @date 2019/8/24 15:42
     */
    BatchResult updateBatchSupplierUseCompany(CustomerSupplierDto dto);

    /**
     * @description: 批量取消分配客户
     * @Param [CustomerSupplierDto]
     * @return com.njwd.support.BatchResult
     * @author wuweiming
     * @date 2019/8/24 15:42
     */
    BatchResult cancelBatchCustomerUseCompany(CustomerSupplierDto dto);

    /**
     * @description: 批量取消分配供应商
     * @Param [CustomerSupplierDto]
     * @return com.njwd.support.BatchResult
     * @author wuweiming
     * @date 2019/8/24 15:42
     */
    BatchResult cancelBatchSupplierUseCompany(CustomerSupplierDto dto);
}
