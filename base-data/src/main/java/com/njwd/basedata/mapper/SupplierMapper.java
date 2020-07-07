package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.CustomerSupplier;
import com.njwd.entity.basedata.Supplier;
import com.njwd.entity.basedata.dto.CustomerSupplierDto;
import com.njwd.entity.basedata.vo.CustomerSupplierVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description 客户供应商Mapper接口
 * @Author 朱小明
 * @Date 2019/6/21 14:06
 **/
public interface SupplierMapper extends BaseMapper<Supplier> {

    /**
     * @return java.util.List<com.njwd.entity.basedata.CustomerSupplier>
     * @Description 校验重复数据
     * @Author 朱小明
     * @Date 2019/6/21 17:47
     * @Param [customerSupplierDto]
     **/
    List<CustomerSupplier> selectSupplierListForCheckDuplicate(CustomerSupplierDto customerSupplierDto);

    /**
     * @return int
     * @Description 查询传入的code是否存在
     * @Author 朱小明
     * @Date 2019/6/25 13:56
     * @Param [code]
     **/
    Long selectSupplierIdForInsert(CustomerSupplierDto customerSupplierDto);

    /**
     * @Description 校验子表名称是否存在
     * @Author 朱小明
     * @Date 2019/6/28 9:05
     * @Param [name, companyId]
     * @return java.lang.Long
     **/
    Long selectSupplierOneByNameForCite(@Param("name") String name, @Param("companyId") Long companyId);

    /**
     * @Description 校验名称是否存在
     * @Author 朱小明
     * @Date 2019/6/28 9:06
     * @Param [customerSupplierDto]
     * @return int
     **/
    int selectSupplierCountByNameForCite(CustomerSupplierDto customerSupplierDto);
    /**
     * @Description 校验社会统一信用码是否存在
     * @Author 朱小明
     * @Date 2019/6/28 9:06
     * @Param [customerSupplierDto]
     * @return int
     **/
    int selectSupplierCountForCheckUscc(CustomerSupplierDto customerSupplierDto);
    /**
     * @Description 校验身份证是否存在
     * @Author 朱小明
     * @Date 2019/6/28 9:06
     * @Param [customerSupplierDto]
     * @return int
     **/
    int selectSupplierCountForCheckIdCard(CustomerSupplierDto customerSupplierDto);
    /**
     * @Description 校验身份证是否存在
     * @Author 朱小明
     * @Date 2019/6/28 9:06
     * @Param [customerSupplierDto]
     * @return int
     **/
    int selectSupplierCountForCheckName(CustomerSupplierDto customerSupplierDto);
    /**
     * @Description 校验编码是否存在
     * @Author 朱小明
     * @Date 2019/6/28 9:06
     * @Param [customerSupplierDto]
     * @return int
     **/
    int selectSupplierCountForCheckCode(CustomerSupplierDto customerSupplierDto);
    /**
     * @Description 根据ID以使用公司维度查询客户供应商（user)
     * @Author 朱小明
     * @Date 2019/6/28 9:06
     * @Param [id]
     * @return com.njwd.entity.basedata.vo.CustomerSupplierVo
     **/
    CustomerSupplierVo selectSupplierCoById(@Param("id") Long id);
    /**
     * @Description 根据ID以归属公司维度查询客户供应商(admin)
     * @Author 朱小明
     * @Date 2019/6/28 9:06
     * @Param [id]
     * @return com.njwd.entity.basedata.vo.CustomerSupplierVo
     **/
    CustomerSupplierVo selectSupplierById(@Param("id") Long id);
    /**
     * @Description 根据条件以使用公司维度查询客户供应商列表（user)
     * @Author 朱小明
     * @Date 2019/6/28 9:06
     * @Param [page, customerSupplierDto]
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page
     * <com.njwd.entity.basedata.vo.CustomerSupplierVo>
     **/
    Page<CustomerSupplierVo> selectSupplierCoPage(
            Page<CustomerSupplierVo> page, @Param("csd") CustomerSupplierDto customerSupplierDto);

    /**
     * @Description 根据条件以归属公司维度查询客户供应商列表（admin)
     * @Author 朱小明
     * @Date 2019/6/28 9:13
     * @Param [page, customerSupplierDto]
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page
     * <com.njwd.entity.basedata.vo.CustomerSupplierVo>
     **/
    Page<CustomerSupplierVo> selectSupplierVoPage(
            Page<CustomerSupplierVo> page, @Param("csd") CustomerSupplierDto customerSupplierDto);

    /**
     * @Description 根据以归属公司维度（admin)id集合批量禁用启用
     * @Author 朱小明
     * @Date 2019/7/1 9:09
     * @Param [customerSupplierDto]
     * @return int
     **/
    int updateSupplierStatusByIds(@Param("csd") CustomerSupplierDto customerSupplierDto);

    /**
     * @Description 根据以使用公司维度（user)id集合批量禁用启用
     * @Author 朱小明
     * @Date 2019/7/1 9:09
     * @Param [customerSupplierDto]
     * @return int
     **/
    int updateSupplierCoStatusByIds(@Param("csd") CustomerSupplierDto customerSupplierDto);

    /**
     * @Description 根据以归属公司维度（admin)id集合批量删除
     * @Author 朱小明
     * @Date 2019/7/1 9:09
     * @Param [customerSupplierDto]
     * @return int
     **/
    int deleteSupplierByIds(@Param("csd") CustomerSupplierDto customerSupplierDto);

    /**
     * @Description 根据以使用公司维度（user)id集合批量删除
     * @Author 朱小明
     * @Date 2019/7/1 9:09
     * @Param [customerSupplierDto]
     * @return int
     **/
    int deleteSupplierCoByIds(@Param("csd") CustomerSupplierDto customerSupplierDto);

    /**
     * @Description 根据归属公司获取ID
     * @Author 朱小明
     * @Date 2019/7/18 20:43
     * @Param [id]
     * @return java.lang.Long
     **/
    Long getSupplierIdByCompany(@Param("id") Long id);


    /**
     * @Description 根据归属公司集合获取ID集合
     * @Author 朱小明
     * @Date 2019/7/19 11:26
     * @Param [idList]
     * @return java.util.List<java.lang.Long>
     **/
    List<Long> selectSupplierIdsForUpdateMain(List<Long> idList);

    /**
     * @Description 根据主表id查询子表中使用公司和归属公司相同的id
     * @Author 朱小明
     * @Date 2019/7/24 9:49
     * @Param [notReferences]
     * @return java.util.List<java.lang.Long>
     **/
    List<Long> selectSupplierListForUpdate(List<Long> notReferences);

    List<CustomerSupplierVo> selectSupplierByParams(@Param("dto") CustomerSupplierDto dto);

    List<Long> findSupplierInfos(@Param("dto") CustomerSupplierDto dto);
}