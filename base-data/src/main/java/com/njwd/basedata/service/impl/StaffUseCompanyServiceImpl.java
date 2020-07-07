package com.njwd.basedata.service.impl;

import com.njwd.basedata.mapper.StaffUseCompanyMapper;
import com.njwd.basedata.service.StaffUseCompanyService;
import com.njwd.entity.basedata.dto.StaffUseCompanyDto;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description
 * @Author: ZhuHC
 * @Date: 2019/8/20 11:40
 */
@Service
public class StaffUseCompanyServiceImpl implements StaffUseCompanyService {
    @Resource
    private StaffUseCompanyMapper staffUseCompanyMapper;

    /**
     * @Author ZhuHC
     * @Date  2019/8/22 9:34
     * @Param staffUseCompanyDto
     * @return Integer
     * @Description 插入员工使用公司变更记录
     */
    @Override
    public Integer insertStaffUseCompany(StaffUseCompanyDto staffUseCompanyDto) {
        return staffUseCompanyMapper.insert(staffUseCompanyDto);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/22 9:34
     * @Param List<StaffUseCompanyDto>
     * @return Integer
     * @Description 批量插入员工使用公司变更记录
     */
    @Override
    public Integer insertStaffUseCompanyList(List<StaffUseCompanyDto> staffUseCompanyDtoList) {
        return staffUseCompanyMapper.addBatch(staffUseCompanyDtoList);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/8/22 9:34
     * @Param staffUseCompanyDto
     * @return Integer
     * @Description 记录不存在时插入员工使用公司变更记录
     */
    @Override
    public Integer insertStaffUseCompanyNotExist(@Param("staffUseCompanyDto") StaffUseCompanyDto staffUseCompanyDto) {
        return staffUseCompanyMapper.insertStaffUseCompanyNotExist(staffUseCompanyDto);
    }
}
