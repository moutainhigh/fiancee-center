package com.njwd.basedata.mapper;



import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.Dept;
import com.njwd.entity.basedata.dto.DeptDto;
import com.njwd.entity.basedata.vo.DeptVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author jds
 * @Description 部门
 * @create 2019/7/7 15:40
 */
@Repository
public interface DeptMapper extends BaseMapper <Dept>{

    /**
     * 查询部门列表（分页）
     * @param deptDto
     * @return
     */
    Page<DeptVo> findPage(Page<DeptVo> page, @Param("deptDto") DeptDto deptDto);


    /**
     * 部门下拉列表（不分页）
     * @param deptDto
     * @return
     */
    List<DeptVo> findDeptList(@Param("deptDto") DeptDto deptDto);


    /**
     * 根据批量id查询数据
     * @param deptDto
     * @return
     */
    List<DeptVo> findDeptByIdList(@Param("deptDto") DeptDto deptDto);


    /**
     * 查询公司下的部门
     * @param deptDto
     * @return
     */
    List<DeptVo> findDeptListByCompany(@Param("deptDto") DeptDto deptDto);

    /**
     * 上级部门列表
     * @param deptDto
     * @return
     */
    Page<DeptVo> findParentDeptList(Page<DeptVo> page,@Param("deptDto") DeptDto deptDto);

    /**
     * 查询是否是墨迹
     * @param deptDto
     * @return
     */
    List<Long> findChild(@Param("deptDto") DeptDto deptDto);


    /**
     * 查询变更历史数量
     * @param deptDto
     * @return
     */
    List<DeptVo> findChangeCount(@Param("deptDto") DeptDto deptDto);

    /**
     * 查询已删除
     * @param deptDto
     * @return
     */
    List<Long> findIsDel(@Param("deptDto") DeptDto deptDto);

    /**
     * 查询已启用或者已禁用
     * @param deptDto
     * @return
     */
    List<Long> findIsEnable(@Param("deptDto") DeptDto deptDto);

    /**
     * 根据ID查询
     * @param deptDto
     * @return
     */
    DeptVo findById(@Param("deptDto") DeptDto deptDto);

    /**
     * 新增部门并返回id
     * @param deptDto
     * @return
     */
    int addDept(@Param("deptDto") DeptDto deptDto);

    /**
     * 批量新增部门并返回id
     * @param list
     * @return
     */
    int addDeptBatch(List<DeptDto> list);

    /**
     * 校验编码是否重复
     *
     * @param deptDto
     * @return count
     */
    int checkDuplicateCode(@Param("deptDto") DeptDto deptDto);

    /**
     * 校验部门名称是否重复
     *
     * @param deptDto
     * @return count
     */
    int checkDuplicateName(@Param("deptDto") DeptDto deptDto);


    /**
     * 根据公司ID和部门名称 查找部门ID
     * @param deptDto
     * @return
     */
    String findIdByCompanyIdAndDeptName(@Param("deptDto") DeptDto deptDto);

    /**
     * 根据编码查询部门
     * @param deptDto
     * @return
     */
    DeptVo findByCode(@Param("deptDto") DeptDto deptDto);

  /**
     * 查询上级部门
     * @param deptDto
     * @return
     */
    List<Long> findParentId(@Param("deptDto") DeptDto deptDto);

    /**
     * 查询下级部门
     * @param deptDto
     * @return
     */
    List<Long> findChidrenIds(@Param("deptDto") DeptDto deptDto);


    /**
     * 批量删除部门  并变更版本号
     * @param deptDto
     * @return
     */
   int deleteByIds(@Param("deptDto") DeptDto deptDto);

    /**
     * 变更使用公司
     * @param deptDto
     * @return
     */
   int updatUseCompanyId(@Param("deptDto") DeptDto deptDto);

}

