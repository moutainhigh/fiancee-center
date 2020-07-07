package com.njwd.basedata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.service.StaffService;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.basedata.dto.StaffDto;
import com.njwd.entity.basedata.vo.StaffVo;
import com.njwd.logger.SenderService;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.HttpUtils;
import com.njwd.utils.UserUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * 基础资料的员工
 * @Author: Zhuhc
 * @Date: 2019/6/12 9:02
 */
@RestController
@RequestMapping("staff")
public class StaffController extends BaseController {

    @Resource
    private StaffService staffService;

    @Resource
    private SenderService senderService;

    /**
     * @Author ZhuHC
     * @Date  2019/6/20 15:56
     * @Param [staffDto]
     * @return java.lang.String
     * @Description 新增员工
     */
    @PostMapping("addStaff")
    public Result<Long> addStaff(@RequestBody StaffDto staffDto)
    {
        Long flag = staffService.addStaff(staffDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,LogConstant.menuName.employee,
                LogConstant.operation.add,LogConstant.operation.add_type,flag.toString()));
        return ok(flag);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/6/20 15:56
     * @Param [staffDto]
     * @return java.lang.String
     * @Description 根据ID删除员工
     */
    @PostMapping("deleteStaff")
    public Result<BatchResult> deleteStaff(@RequestBody StaffDto staffDto)
    {
        FastUtils.checkParams(staffDto.getId(),staffDto.getVersion(),staffDto.getIsEnterpriseAdmin());
        staffDto.setIsDel(Constant.Is.YES);
        BatchResult batchResult = staffService.deleteStaffById(staffDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,LogConstant.menuName.employee,
                LogConstant.operation.delete,LogConstant.operation.delete_type,staffDto.getId().toString()));
        return ok(batchResult);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/6/20 15:57
     * @Param [staffDto]
     * @return java.lang.String
     * @Description 批量删除
     */
    @PostMapping("deleteBatch")
    public Result<BatchResult> deleteBatch(@RequestBody StaffDto staffDto)
    {
        FastUtils.checkParams(staffDto.getIds(),staffDto.getVersions(),staffDto.getIsEnterpriseAdmin());
        staffDto.setIsDel(Constant.Is.YES);
        BatchResult batchResult = staffService.deleteStaff(staffDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,LogConstant.menuName.employee,
                LogConstant.operation.deleteBatch,LogConstant.operation.deleteBatch_type,staffDto.getIds().toString()));
        return ok(batchResult);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/6/20 15:57
     * @Param [staffDto]
     * @return java.lang.String
     * @Description 禁用
     */
    @PostMapping("disableBatch")
    public Result<BatchResult> disableBatch(@RequestBody StaffDto staffDto)
    {
        FastUtils.checkParams(staffDto.getIds(),staffDto.getVersions(),staffDto.getIsEnterpriseAdmin());
        staffDto.setIsEnable(Constant.Is.NO);
        BatchResult batchResult;
        batchResult = staffService.disableBatch(staffDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,LogConstant.menuName.employee,
                LogConstant.operation.forbiddenBatch,LogConstant.operation.forbiddenBatch_type,staffDto.getIds().toString()));
        return ok(batchResult);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/6/20 15:58
     * @Param [staffDto]
     * @return java.lang.String
     * @Description 反禁用
     */
    @PostMapping("enableBatch")
    public Result<BatchResult> enableBatch(@RequestBody StaffDto staffDto)
    {
        FastUtils.checkParams(staffDto.getIds(),staffDto.getVersions(),staffDto.getIsEnterpriseAdmin());
        staffDto.setIsEnable(Constant.Is.YES);
        BatchResult batchResult;
        batchResult = staffService.enableBatch(staffDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,LogConstant.menuName.employee,
                LogConstant. operation.antiForbiddenBatch,LogConstant. operation.antiForbiddenBatch_type,staffDto.getIds().toString()));
        return ok(batchResult);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/6/28 11:07
     * @Param [staffDto]
     * @return java.lang.String
     * @Description 修改员工信息
     */
    @PostMapping("updateStaffInfo")
    public Result<BatchResult> updateStaffInfo(@RequestBody StaffDto staffDto)
    {
        FastUtils.checkParams(staffDto.getId(),staffDto.getVersion(),staffDto.getIsEnterpriseAdmin());
        BatchResult flag = staffService.updateStaffInfo(staffDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys,LogConstant.menuName.employee,
                LogConstant.operation.update,LogConstant.operation.update_type,staffDto.getId().toString()));
        return ok(flag);
    }

    /**
     * @Author ZhuHC
     * @Date  2019/6/20 15:58
     * @Param [staffDto]
     * @return java.lang.String
     * @Description 查询员工列表 分页
     */
    @PostMapping("findStaffPage")
    public  Result<Page<StaffVo>> findStaffPage(@RequestBody StaffDto staffDto)
    {
        return ok(staffService.findStaffPage(staffDto));
    }

    /**
     * @Author ZhuHC
     * @Date  2019/6/20 15:59
     * @Param [staffDto]
     * @return java.lang.String
     * @Description 根据ID查询员工信息
     */
    @PostMapping("findStaffById")
    public Result<StaffVo> findStaffById(@RequestBody StaffDto staffDto)
    {
        FastUtils.checkParams(staffDto.getId());
        return ok(staffService.findStaffById(staffDto));
    }

    /**
     * @Author ZhuHC
     * @Date  2019/6/20 16:00
     * @Param [staffDto, response]
     * @return void
     * @Description 数据导出
     */
    @RequestMapping("exportExcel")
    public void exportExcel(@RequestBody StaffDto staffDto, HttpServletResponse response){
        staffService.exportExcel(staffDto,response);
    }
}
