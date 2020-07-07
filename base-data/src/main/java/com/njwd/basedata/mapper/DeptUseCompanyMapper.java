package com.njwd.basedata.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.basedata.DeptUseCompany;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Description 部门使用公司变更记录
 * @Author jds
 * @Date 2019/6/21 10:10
 * @Param
 * @return
 **/
@Repository
public interface DeptUseCompanyMapper extends BaseMapper <DeptUseCompany>{

    /**
     * 查询变更列表
     * @param deptUseCompany
     * @return
     */
    List<DeptUseCompany> findUseCompanyList(@Param("deptUseCompany") DeptUseCompany deptUseCompany);

    /**
     * 新增
     * @param deptUseCompany
     * @return
     */
     int addUseCompany(@Param("deptUseCompany") DeptUseCompany deptUseCompany);

    /**
     * 批量新增
     * @param deptUseCompany
     * @return
     */
    int addUseCompanyBatch(List<DeptUseCompany> deptUseCompany);

    int deleteDept(List<Long> list);

}