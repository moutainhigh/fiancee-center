package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.basedata.StaffUseCompany;
import com.njwd.entity.basedata.dto.StaffUseCompanyDto;

import java.util.List;

public interface StaffUseCompanyMapper extends BaseMapper<StaffUseCompany> {

    /**
     * @Author ZhuHC
     * @Date  2019/8/22 9:29
     * @Param staffUserCompanyDtoList
     * @return Integer
     * @Description 批量插入
     */
    Integer addBatch(List<StaffUseCompanyDto> staffUserCompanyDtoList);

    /**
     * @Author ZhuHC
     * @Date  2019/8/22 9:27
     * @Param staffUseCompanyDto
     * @return Integer
     * @Description 当记录不存在时 插入 员工变更信息
     */
    Integer insertStaffUseCompanyNotExist(StaffUseCompanyDto staffUseCompanyDto);
}
