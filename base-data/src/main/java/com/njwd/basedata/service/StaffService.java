package com.njwd.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.dto.DeptDto;
import com.njwd.entity.basedata.dto.StaffDto;
import com.njwd.entity.basedata.vo.StaffVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author zhuhc
 */
public interface StaffService{
    /**
     * @Author ZhuHC
     * @Date  2019/7/3 14:45
     * @Param StaffDto
     * @return Long
     * @Description 新增员工
     */
    Long addStaff(StaffDto staffDto);

    /**
     * @Author ZhuHC
     * @Date  2019/7/3 14:48
     * @Param StaffDto
     * @return int
     * @Description 校验后的数据插入
     */
    int insertStaff(StaffDto staffDto);

   /**
    * @Author ZhuHC
    * @Date  2019/7/3 14:49
    * @Param List<StaffDto>
    * @return int
    * @Description 批量插入
    */
    int addBatchStaff(List<StaffDto> staffDtoList);

    /**
     * @Author ZhuHC
     * @Date  2019/7/3 14:49
     * @Param StaffDto
     * @return BatchResult
     * @Description 删除员工信息
     */
    BatchResult deleteStaff(StaffDto staffDto);

   /**
    * @Author ZhuHC
    * @Date  2019/7/3 14:49
    * @Param StaffDto
    * @return BatchResult
    * @Description 根据ID删除员工
    */
   BatchResult deleteStaffById(StaffDto staffDto);

    /**
     * @Author ZhuHC
     * @Date  2019/6/20 15:57
     * @Param [staffDto]
     * @return java.lang.String
     * @Description 禁用
     */
    BatchResult disableBatch(StaffDto staffDto);

    /**
     * @Author ZhuHC
     * @Date  2019/7/9 15:30
     * @Param [staffDto]
     * @return com.njwd.support.BatchResult
     * @Description 单个禁用
     */
    BatchResult disableStaff(StaffDto staffDto);

    /**
     * @Author ZhuHC
     * @Date  2019/6/20 15:57
     * @Param [staffDto]
     * @return java.lang.String
     * @Description 反禁用
     */
    BatchResult enableBatch(StaffDto staffDto);

    /**
     * @Author ZhuHC
     * @Date  2019/7/9 15:31
     * @Param [staffDto]
     * @return com.njwd.support.BatchResult
     * @Description 单个反禁用
     */
    BatchResult enableStaff(StaffDto staffDto);

    /**
     * @Author ZhuHC
     * @Date  2019/7/3 14:50
     * @Param StaffDto
     * @return int
     * @Description 修改员工信息(需校验员工唯一性)
     */
    BatchResult updateStaffInfo(StaffDto staffDto);



    /**
     * @Description //同步部门使用公司变更
     * @Author jds
     * @Date 2019/8/20 11:12
     * @Param DeptDto
     * @return
     **/
    int updateStaffInfoFromDept(DeptDto deptDto);


   /**
    * @Description //根据部门id查询关联员工
    * @Author jds
    * @Date 2019/8/20 13:55
    * @Param [deptDto]
    * @return java.util.List<java.lang.Long>
    **/
    List<Long>findStaffByDeptId(DeptDto deptDto);

    /**
     * @Author ZhuHC
     * @Date  2019/7/3 14:50
     * @Param StaffDto
     * @return StaffVo
     * @Description 根据 ID 查询员工
     */
    StaffVo findStaffById(StaffDto staffDto);

    /**
     * @Author ZhuHC
     * @Date  2019/9/9 10:58
     * @Param [staffDto]
     * @return com.njwd.entity.basedata.vo.StaffVo
     * @Description 根据ID查询员工 带缓存
     */
    StaffVo findStaffByIdWithCache(StaffDto staffDto);

   /**
    * @Author ZhuHC
    * @Date  2019/7/3 14:51
    * @Param StaffDto
    * @return Page<StaffVo>
    * @Description 查询员工列表 分页
    */
    Page<StaffVo> findStaffPage(StaffDto staffDto);

   /**
    * @Author ZhuHC
    * @Date  2019/7/3 14:51
    * @Param StaffDto
    * @return Integer
    * @Description 根据员工编码和企业ID查找员工是否存在
    */
    Integer findStaffByCode(StaffDto staffDto);

    /**
    * @description: 根据员工编码和企业id查询员工id
    * @param staffDto
    * @return com.njwd.entity.basedata.vo.StaffVo
    * @author LuoY
    * @date 2019/7/19 15:08
    */
    Integer findStaffInfoByCode(StaffDto staffDto);

    /**
     * @Author ZhuHC
     * @Date  2019/8/22 9:39
     * @Param staffDto
     * @return Integer
     * @Description 查询员工手机号银行卡号
     */
    Integer findStaffInfoByNumberOrCard(StaffDto staffDto);

   /**
    * @Author ZhuHC
    * @Date  2019/7/3 14:52
    * @Param StaffDto
    * @Param HttpServletResponse
    * @return void
    * @Description 导出excel
    */
    void exportExcel(StaffDto staffDto, HttpServletResponse response);

}
