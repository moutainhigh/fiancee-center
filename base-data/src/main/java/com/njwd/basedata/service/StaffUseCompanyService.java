package com.njwd.basedata.service;

import com.njwd.entity.basedata.dto.StaffUseCompanyDto;

import java.util.List;

/**
 * @Author ZhuHC
 * @Date  2019/8/20 11:39
 *
 */
public interface StaffUseCompanyService {

    /**
     * @Author ZhuHC
     * @Date  2019/8/22 9:34
     * @Param staffUseCompanyDto
     * @return Integer
     * @Description 插入员工使用公司变更记录
     */
    Integer insertStaffUseCompany(StaffUseCompanyDto staffUseCompanyDto);


    /**
     * @Author ZhuHC
     * @Date  2019/8/22 9:34
     * @Param List<StaffUseCompanyDto>
     * @return Integer
     * @Description 批量插入员工使用公司变更记录
     */
    Integer insertStaffUseCompanyList(List<StaffUseCompanyDto> staffUseCompanyDtoList);


    /**
     * @Author ZhuHC
     * @Date  2019/8/22 9:34
     * @Param staffUseCompanyDto
     * @return Integer
     * @Description 记录不存在时插入员工使用公司变更记录
     */
    Integer insertStaffUseCompanyNotExist(StaffUseCompanyDto staffUseCompanyDto);
}
