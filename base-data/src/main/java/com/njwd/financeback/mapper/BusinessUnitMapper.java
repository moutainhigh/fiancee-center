package com.njwd.financeback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.BusinessUnit;
import com.njwd.entity.basedata.dto.BusinessUnitDto;
import com.njwd.entity.basedata.vo.BusinessUnitVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 * @Author: Zhuzs
 * @Date: 2019-05-21 15:21
 */
public interface BusinessUnitMapper extends BaseMapper<BusinessUnit> {
    /**
     * 批量新增
     *
     * @param: [businessUnits]
     * @return: int
     * @author: zhuzs
     * @date: 2019-09-16 17:19
     */
    int addBatch(List<BusinessUnit> businessUnits);

    /**
     * 根据ID查询业务单元 （是否删除不校验）
     *
     * @param: [businessUnit]
     * @return: com.njwd.entity.basedata.vo.BusinessUnitVo
     * @author: zhuzs
     * @date: 2019-09-16 17:19
     */
    BusinessUnitVo selectById(BusinessUnit businessUnit);

    /**
     * 根据 公司ID 查询业务单元
     *
     * @param: [rootEnterpriseId, companyId]
     * @return: java.util.List<com.njwd.entity.basedata.vo.BusinessUnitVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:19
     */
    List<BusinessUnitVo> findPageByCompanyId(@Param("rootEnterpriseId") Long rootEnterpriseId, @Param("companyId") Long companyId);

    /**
     * 根据 公司ID List 查询业务单元列表
     *
     * @param: [companyIds]
     * @return: java.util.List<java.lang.Long>
     * @author: zhuzs
     * @date: 2019-09-16 17:19
     */
    List<Long> findListByCompanyIds(@Param("list") List<Long> companyIds);

    /**
     * 查询业务单元列表(含公司ID、公司名称） 分页
     *
     * @param: [page, businessUnitDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.BusinessUnitVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:20
     */
    Page<BusinessUnitVo> findPage(@Param("page") Page<BusinessUnitVo> page, @Param("businessUnitDto") BusinessUnitDto businessUnitDto);

    /**
     * 查询业务单元列表(含公司ID、公司名称）
     *
     * @param: [businessUnitDto]
     * @return: java.util.List<com.njwd.entity.basedata.vo.BusinessUnitVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:20
     */
    List<BusinessUnitVo> findBusinessUnitList(@Param("businessUnitDto") BusinessUnitDto businessUnitDto);

}