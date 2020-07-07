package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.Staff;
import com.njwd.entity.basedata.dto.StaffDto;
import com.njwd.entity.basedata.vo.StaffVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author zhuhc
 */
public interface StaffMapper extends BaseMapper<Staff> {

    /**
     * @Author ZhuHC
     * @Date  2019/7/3 15:56
     * @Param Staff
     * @return Integer
     * @Description 新增员工
     */
    Integer insertStaff(@Param("staff") Staff staff);

    /**
     * @Author ZhuHC
     * @Date  2019/7/4 11:57
     * @Param
     * @return
     * @Description 查询已删除数据
     */
    List<StaffVo> findDeletedIds(@Param("staffDto") StaffDto staffDto);

    /**
     * @Author ZhuHC
     * @Date  2019/7/3 15:57
     * @Param List<StaffDto>
     * @return int
     * @Description 批量插入
     */
    int addBatchStaff(List<StaffDto> staffDtoList);

    /**
     * @Author ZhuHC
     * @Date  2019/9/10 16:10
     * @Param
     * @return
     * @Description 批量删除 并更新version
     */
    int deleteStaffByIds(@Param("staffDto") StaffDto staffDto);

    /**
     * @Author ZhuHC
     * @Date  2019/7/3 15:57
     * @Param StaffDto
     * @return Integer
     * @Description 根据员工编码及企业查询员工是否存在
     */
    Integer findStaffByCode(@Param("staffDto") StaffDto staffDto);

    /**
    * @description: 根据员工编码及企业查询员工id
    * @param staffDto
    * @return java.lang.Integer
    * @author LuoY
    * @date 2019/7/19 15:13
    */
    Integer findStaffInfoByCode(@Param("staffDto") StaffDto staffDto);

    /**
     * @Author ZhuHC
     * @Date  2019/8/22 9:26
     * @Param staffDto
     * @return Integer
     * @Description 判断手机号 银行号是否唯一
     */
    Integer findStaffInfoByNumberOrCard(@Param("staffDto") StaffDto staffDto);

    /**
     * @Author ZhuHC
     * @Date  2019/7/3 15:57
     * @Param StaffDto
     * @return Integer
     * @Description 查询员工 是否重复
     */
    Integer findStaffByInfo(@Param("staffDto") StaffDto staffDto);

    /**
     * @Author ZhuHC
     * @Date  2019/7/3 15:57
     * @Param StaffDto
     * @Param Page<StaffVo>
     * @return List<StaffVo>
     * @Description 查询员工列表
     */
    Page<StaffVo> findPage(@Param("page") Page<StaffVo> page, @Param("staffDto") StaffDto staffDto);

   /**
    * @Author ZhuHC
    * @Date  2019/7/3 15:58
    * @Param StaffDto
    * @return StaffVo
    * @Description 根据ID查询员工
    */
    StaffVo findStaffById(@Param("staffDto") StaffDto staffDto);

    /**
     * @Author ZhuHC
     * @Date  2019/9/18 17:12
     * @Param
     * @return
     * @Description 根据ID查询员工列表
     */
    List<StaffVo> findStaffListById(@Param("staffDto") StaffDto staffDto);


    /**
     * @Description //根据部门id查询关联的员工
     * @Author jds
     * @Date 2019/8/20 13:39
     * @Param
     * @return
     **/
    List<Long> findStaffByDeptId(@Param("staffDto") StaffDto staffDto);

}