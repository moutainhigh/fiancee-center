package com.njwd.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.Dept;
import com.njwd.entity.basedata.dto.DeptDto;
import com.njwd.entity.basedata.vo.DeptVo;
import com.njwd.entity.platform.dto.SysAuxDataDto;
import com.njwd.entity.platform.vo.SysAuxDataVo;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author jds
 * @Description 部门
 * @create 2019/7/7 15:40
 */
public interface DeptService{
    /**
     * 新增
     * @param deptDto
     * @return
     */
    Integer add(DeptDto deptDto);

    /**
     * 新增记录
     * @param deptDto
     * @return
     */
    Integer addUseCompany(DeptDto deptDto);

    /**
     * 批量新增
     * @param list
     * @return
     */
    Integer addDeptBatch(List<DeptDto> list);


    /**
     * 批量新增记录
     * @param list
     * @return
     */
    Integer addUseCompanyBatch(List<DeptDto> list);

    /**
     * 批量更改上级部门为非墨迹
     * @param list
     * @return
     */
    void updateParentNoEnd(List<Long> list,Byte type);

    /**
     * 更新使用公司
     * @param deptDto
     * @return
     */
    Integer update(DeptDto deptDto);

    /**
     * 保存
     * @param deptDto
     * @return
     */
    Integer saveDeptChange(DeptDto deptDto);

    /**
     * 删除
     * @param deptDto
     * @return
     */
    BatchResult deleteDeptBatch(DeptDto deptDto);


    /**
     * 分页查询
     * @param deptDto
     * @return
     */
    Page<DeptVo> findDeptPage(DeptDto deptDto);


    /**
     * 部门下拉列表
     * @param deptDto
     * @return
     */
    List<DeptVo> findDeptList(DeptDto deptDto);

    /**
     * 上级部门列表
     * @param deptDto
     * @return
     */
    Page<DeptVo> findParentDeptList( DeptDto deptDto);

    /**
     * 根据ID查询
     * @param deptDto
     * @return
     */
    DeptVo findDeptById(DeptDto deptDto);


    /**
     * 根据ID查询
     * @param deptDto
     * @return
     */
    DeptVo findDeptByIdForMapper(DeptDto deptDto);


    /**
     * 验证上级部门信息
     * @param deptDto
     * @return
     */
    Integer  findPre(DeptDto deptDto);


    /**
     * 批量验证权限
     * @param deptDto
     * @return
     */
    BatchResult batchVerifyPermission(DeptDto deptDto);


    /**
     * 验证上级部门信息
     * @param id
     * @return
     */
    void  findOldPre(Long id);

    /**
     * 根据编码查询
     * @param deptDto
     * @return
     */
    DeptVo findByCode(DeptDto deptDto);

    /**
     * 查询变更历史记录列表
     * @param deptDto
     * @return
     */
    DeptVo findCompanyList(DeptDto deptDto);


    /**
     * 查询变更历史记录数量
     * @param deptDto
     * @return
     */
    List<DeptVo> findChangeCount(DeptDto deptDto);

    /**
     * 验证名称是否存在
     * @param deptDto
     * @return
     */
    Integer checkName(DeptDto deptDto);


    /**
     * 批量更新状态
     *
     * @param deptDto
     * @return int
     * @title updateDeptStatusBatch
     */
    BatchResult updateDeptStatusBatch(DeptDto deptDto);


    /**
     * 导出
     * @param deptDto
     * @param response
     */
    void exportExcel(DeptDto deptDto, HttpServletResponse response);


    /**
     * 重新定義歷史表
     * @param deptDto
     * @param
     */
    void updateUseCompany(DeptDto deptDto);

    /**
     * 查询  部门属性  辅助资料列表 （分页）
     * @param sysAuxDataDto
     * @return
     */
    Result findDpetTypeList(SysAuxDataDto sysAuxDataDto);


    /**
     * 查询  部门属性  辅助资料表
     * @param platformSysAuxDataDto
     * @return
     */
    Result findDpetTypeByName(SysAuxDataDto platformSysAuxDataDto);

    /**
     * 查询  部门属性  辅助资料列表(不分页)
     * @param platformSysAuxDataDto
     * @return
     */
    List<SysAuxDataVo> findTypeList(SysAuxDataDto platformSysAuxDataDto);

    /**
     * 根据公司ID和部门名称查询部门ID
     * @return
     */
    String findIdByCompanyIdAndDeptName(DeptDto deptDto);

    /**
     * @Description 根据单位查询部门列表
     * @Author LuoY
     * @Date 2019/7/2 17:54
     * @Param [companyId]
     * @return java.util.List<com.njwd.entity.basedata.Dept>
     */
    List<Dept> findDeptListByCompanyId(Long companyId);

}
