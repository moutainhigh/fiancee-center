package com.njwd.basedata.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.BusinessUnit;
import com.njwd.entity.basedata.dto.BusinessUnitDto;
import com.njwd.entity.basedata.vo.BusinessUnitVo;
import com.njwd.entity.platform.SysAuxData;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * @Description 业务单元
 * @Author 朱小明
 * @Date 2019/8/1 9:04
 **/
@RequestMapping("financeback/businessUnit")
public interface BusinessUnitApi {

    /**
     * 新增业务单元
     * @param businessUnitDto
     * @return
     */
    @RequestMapping("addBusinessUnit")
    Result<BusinessUnit> addBusinessUnit(@RequestBody BusinessUnitDto businessUnitDto);

    /**
     * 批量删除
     * @param businessUnitDto
     * @return
     */
    @RequestMapping("deleteBatch")
    Result<BatchResult> deleteBatch(@RequestBody BusinessUnitDto businessUnitDto);

    /**
     * 修改业务单元
     * @param businessUnitDto
     * @return
     */
    @RequestMapping("updateBusinessUnit")
    Result<BusinessUnit> updateBusinessUnit(@RequestBody BusinessUnitDto businessUnitDto);

    /**
     * 根据ID查找
     * @param businessUnitDto
     * @return
     */
    @RequestMapping("findBusinessUnitById")
    Result<BusinessUnit> findBusinessUnitById(@RequestBody BusinessUnitDto businessUnitDto);

    /**
     * 根据名称查找
     * @param businessUnitDto
     * @return data 为 1 表示名称重复
     */
    @RequestMapping("findBusinessUnitByName")
    Result<BusinessUnit> findBusinessUnitByName(@RequestBody BusinessUnitDto businessUnitDto);

    /**
     * 业务单元列表
     * @param businessUnitDto
     * @return
     */
    @RequestMapping("findBusinessUnitPage")
    Result<Page<BusinessUnitVo>> findBusinessUnitPage(@RequestBody BusinessUnitDto businessUnitDto);

    /**
     * 获取 业务单元形态 列表
     *
     * @return
     */
    @RequestMapping("findFormList")
    Result<List<SysAuxData>> findFormList();


}
