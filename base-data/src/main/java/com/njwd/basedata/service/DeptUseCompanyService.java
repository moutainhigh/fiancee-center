package com.njwd.basedata.service;

import com.njwd.entity.basedata.DeptUseCompany;

import java.util.List;

/**
 * @author jds
 * @Description 使用公司表
 * @create 2019/7/7 15:37
 */
public interface DeptUseCompanyService {
    /**
     * 查询变更列表
     *
     * @param deptUseCompany
     * @return
     */
    List<DeptUseCompany> findUseCompanyList(DeptUseCompany deptUseCompany);



    /**
     * 删除记录
     *
     * @param list
     * @return
     */
    Integer delete(List<Long> list);

    /**
     * 删除记录
     *
     * @param list
     * @return
     */
    Integer deleteDept(List<Long> list);

    /**
     * 重定义记录
     *
     * @param deptUseCompany
     * @return
     */
    Integer update(DeptUseCompany deptUseCompany);

    /**
     * 新增
     *
     * @param deptUseCompany
     * @return
     */
    Integer addUseCompany(DeptUseCompany deptUseCompany);

    /**
     * 批量新增
     *
     * @param deptUseCompany
     * @return
     */
    Integer addUseCompanyBatch(List<DeptUseCompany> deptUseCompany);

}
