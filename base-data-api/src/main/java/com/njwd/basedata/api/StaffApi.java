package com.njwd.basedata.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.dto.StaffDto;
import com.njwd.entity.basedata.vo.StaffVo;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;

/**
 * 基础资料的员工
 * @Author: 朱小明
 * @Date: 2019/6/12 9:02
 */
@RequestMapping("staff")
public interface StaffApi {

    /**
     * @Author 朱小明
     * @Date  2019/6/20 15:56
     * @Param [staffDto]
     * @return java.lang.String
     * @Description 新增员工
     */
    @PostMapping("addStaff")
    Result<Long> addStaff(StaffDto staffDto);

    /**
     * @Author 朱小明
     * @Date  2019/6/20 15:56
     * @Param [staffDto]
     * @return java.lang.String
     * @Description 根据ID删除员工
     */
    @PostMapping("deleteStaff")
    Result<BatchResult> deleteStaff(StaffDto staffDto);

    /**
     * @Author 朱小明
     * @Date  2019/6/20 15:57
     * @Param [staffDto]
     * @return java.lang.String
     * @Description 批量删除
     */
    @PostMapping("deleteBatch")
    Result<BatchResult> deleteBatch(StaffDto staffDto);

    /**
     * @Author 朱小明
     * @Date  2019/6/20 15:57
     * @Param [staffDto]
     * @return java.lang.String
     * @Description 禁用
     */
    @PostMapping("disableBatch")
    Result<BatchResult> disableBatch(StaffDto staffDto);

    /**
     * @Author 朱小明
     * @Date  2019/6/20 15:58
     * @Param [staffDto]
     * @return java.lang.String
     * @Description 反禁用
     */
    @PostMapping("enableBatch")
    Result<BatchResult> enableBatch(StaffDto staffDto);

    /**
     * @Author 朱小明
     * @Date  2019/6/28 11:07
     * @Param [staffDto]
     * @return java.lang.String
     * @Description 修改员工信息
     */
    @PostMapping("updateStaffInfo")
    Result<Integer> updateStaffInfo(StaffDto staffDto);

    /**
     * @Author 朱小明
     * @Date  2019/6/20 15:58
     * @Param [staffDto]
     * @return java.lang.String
     * @Description 查询员工列表 分页
     */
    @PostMapping("findStaffPage")
     Result<Page<StaffVo>> findStaffPage(StaffDto staffDto);

    /**
     * @Author 朱小明
     * @Date  2019/6/20 15:59
     * @Param [staffDto]
     * @return java.lang.String
     * @Description 根据ID查询员工信息
     */
    @PostMapping("findStaffById")
     Result<StaffVo> findStaffById(StaffDto staffDto);

    /**
     * @Author 朱小明
     * @Date  2019/6/20 16:00
     * @Param [staffDto, response]
     * @return void
     * @Description 数据导出
     */
    @RequestMapping("exportExcel")
    void exportExcel(StaffDto staffDto, HttpServletResponse response);
}
