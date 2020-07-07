package com.njwd.financeback.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.BusinessUnit;
import com.njwd.entity.basedata.dto.BusinessUnitDto;
import com.njwd.entity.basedata.vo.BusinessUnitVo;
import com.njwd.entity.platform.vo.SysAuxDataVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author: Zhuzs
 * @Date: 2019-05-15 11:09
 */
public interface BusinessUnitService {

    /**
     * 新增业务单元
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.entity.basedata.BusinessUnit
     * @author: zhuzs
     * @date: 2019-09-16 17:13
     */
    BusinessUnit addBusinessUnit(BusinessUnitDto businessUnitDto);

    /**
     * 删除
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.support.BatchResult
     * @author: zhuzs
     * @date: 2019-09-16 17:14
     */
    BatchResult delete(BusinessUnitDto businessUnitDto);

    /**
     * 批量删除
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.support.BatchResult
     * @author: zhuzs
     * @date: 2019-09-16 17:14
     */
    BatchResult deleteBatch(BusinessUnitDto businessUnitDto);

    /**
     * 修改业务单元
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.entity.basedata.BusinessUnit
     * @author: zhuzs
     * @date: 2019-09-16 17:14
     */
    BusinessUnit updateBusinessUnit(BusinessUnitDto businessUnitDto);

    /**
     * 根据 ID 查询业务单元（含引用关系）
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.entity.basedata.BusinessUnit
     * @author: zhuzs
     * @date: 2019-09-16 17:14
     */
    BusinessUnit findBusinessUnitById(BusinessUnitDto businessUnitDto);

    /**
     * 查询业务单元基础信息
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.entity.basedata.BusinessUnit
     * @author: zhuzs
     * @date: 2019-09-16 17:14
     */
    BusinessUnit findBusinessUnitBaseInfoById(BusinessUnitDto businessUnitDto);

    /**
     * 根据 名称 查询业务单元
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.entity.basedata.BusinessUnit
     * @author: zhuzs
     * @date: 2019-09-16 17:14
     */
    BusinessUnit findBusinessUnitByName(BusinessUnitDto businessUnitDto);

    /**
     * 根据 名称 查询业务单元
     *
     * @param: [businessUnitDto]
     * @return: com.njwd.entity.basedata.BusinessUnit
     * @author: zhuzs
     * @date: 2019-09-16 17:14
     */
    BusinessUnit findBusinessByCode(BusinessUnitDto businessUnitDto);

    /**
     * 根据 编码 查询业务单元
     *
     * @param: [businessUnit]
     * @return: java.lang.Integer
     * @author: zhuzs
     * @date: 2019-09-16 17:14
     */
    Integer findBusinessUnitByCode(BusinessUnit businessUnit);

    /**
     * 根据公司ID 查询业务单元
     *
     * @param: [rootEnterpriseId, companyId]
     * @return: java.util.List<com.njwd.entity.basedata.vo.BusinessUnitVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:15
     */
    List<BusinessUnitVo> findBusinessUnitByCompanyId(Long rootEnterpriseId, Long companyId);

    /**
     * 根据公司ID List 查询业务单元
     *
     * @param: [companyIds]
     * @return: java.util.List<java.lang.Long>
     * @author: zhuzs
     * @date: 2019-09-16 17:15
     */
    List<Long> findBusinessUnitByCompanyIds(List<Long> companyIds);

    /**
     * 查询业务单元列表 分页
     *
     * @param: [businessUnitDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.BusinessUnitVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:15
     */
    Page<BusinessUnitVo> findBusinessUnitPage(BusinessUnitDto businessUnitDto);

    /**
     * 查询业务单元列表
     *
     * @param: [businessUnitDto]
     * @return: java.util.List<com.njwd.entity.basedata.vo.BusinessUnitVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:15
     */
    List<BusinessUnitVo> findBusinessUnitList(BusinessUnitDto businessUnitDto);

    /**
     * 获取业务单元形态列表
     *
     * @param: []
     * @return: java.util.List<com.njwd.entity.platform.vo.SysAuxDataVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:15
     */
    List<SysAuxDataVo> findFormList();

    /**
     * 导出
     *
     * @param: [businessUnitDto, response]
     * @return: void
     * @author: zhuzs
     * @date: 2019-09-16 17:15
     */
    void exportExcel(BusinessUnitDto businessUnitDto, HttpServletResponse response);

    /**
     * 新增业务单元
     *
     * @param: [businessUnit, companyCode]
     * @return: com.njwd.entity.basedata.BusinessUnit
     * @author: zhuzs
     * @date: 2019-09-16 17:15
     */
    BusinessUnit generateCode(BusinessUnit businessUnit,String companyCode);

}
