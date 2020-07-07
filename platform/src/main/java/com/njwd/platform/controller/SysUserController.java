package com.njwd.platform.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.LogConstant;
import com.njwd.entity.platform.dto.SysUserDto;
import com.njwd.entity.platform.remore.resp.UserLogin;
import com.njwd.entity.platform.vo.SysUserVo;
import com.njwd.logger.SenderService;
import com.njwd.platform.service.SysUserService;
import com.njwd.platform.utils.UserUtil;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户 前端控制器
 *
 * @author: zhuzs
 * @date: 2019-11-11
 */
@RestController
@RequestMapping("sysUser")
public class SysUserController extends BaseController {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SenderService senderService;

    /**
     * 登录
     */
    @PostMapping("login")
    public Result<SysUserVo> login(@RequestBody UserLogin userLogin, HttpServletRequest request) {
        return ok(sysUserService.login(userLogin, request.getSession().getId()));
    }

    /**
     * 新增用户
     *
     * @param: [sysUserDto]
     * @return: com.njwd.support.Result account 不填写 传空字符串
     * @author: zhuzs
     * @date: 2019-11-11
     */
    @PostMapping("addUserWithMenuInfo")
    public Result<Integer> addUserWithMenuInfo(@RequestBody SysUserDto sysUserDto) {
        Integer row = sysUserService.addUserWithMenuInfo(sysUserDto);
        senderService.sendLog(UserUtil.getUserLogInfo(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys,
                LogConstant.menuName.userManage,
                LogConstant.operation.add,
                LogConstant.operation.add_type));
        return ok(row);
    }

    /**
     * 修改用户信息
     *
     * @param: [sysUserDto]
     * @return: com.njwd.support.Result
     * @author: zhuzs
     * @date: 2019-11-18
     */
    @PostMapping("updateUser")
    public Result<Integer> updateUser(@RequestBody SysUserDto sysUserDto) {
        Integer row = sysUserService.updateUser(sysUserDto);
        senderService.sendLog(UserUtil.getUserLogInfo(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys,
                LogConstant.menuName.userManage,
                LogConstant.operation.update,
                LogConstant.operation.update_type));
        return ok(row);
    }

    /**
     * 删除
     *
     * @param: [sysUserDto]
     * @return: com.njwd.support.Result<com.njwd.support.BatchResult>
     * @author: zhuzs
     * @date: 2019-11-19
     */
    @PostMapping("deleteBatch")
    public Result<BatchResult> deleteBatch(@RequestBody SysUserDto sysUserDto) {
        BatchResult result = sysUserService.deleteBatch(sysUserDto);
        senderService.sendLog(UserUtil.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys,
                LogConstant.menuName.userManage,
                LogConstant.operation.deleteBatch,
                LogConstant.operation.deleteBatch_type,
                String.valueOf(result.getSuccessList())));
        return ok(result);
    }

    /**
     * 分配租户
     *
     * @param: [sysUserDto]
     * @return: com.njwd.support.Result<java.lang.Integer>
     * @author: zhuzs
     * @date: 2019-11-18
     */
    @PostMapping("assignEnterprises")
    public Result<Integer> assignEnterprises(@RequestBody SysUserDto sysUserDto) {
        Integer row = sysUserService.assignEnterprises(sysUserDto);
        senderService.sendLog(UserUtil.getUserLogInfo(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys,
                LogConstant.menuName.userManage,
                LogConstant.operation.add,
                LogConstant.operation.add_type));
        return ok(row);
    }

    /**
     * 复制权限
     *
     * @param: [sysUserDto]
     * @return: com.njwd.support.Result<java.lang.Integer>
     * @author: zhuzs
     * @date: 2019-11-19
     */
    @PostMapping("copyAssignedMenusAndEnterprises")
    public Result<Integer> copyAssignedMenusAndEnterprises(@RequestBody SysUserDto sysUserDto) {
        Integer row = sysUserService.copyAssignedMenusAndEnterprises(sysUserDto);
        senderService.sendLog(UserUtil.getUserLogInfo(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.PlatformSys,
                LogConstant.menuName.userManage,
                LogConstant.operation.add,
                LogConstant.operation.add_type));
        return ok(row);
    }

    /**
     * 查询用户列表
     *
     * @param: [sysUserDto]
     * @return: com.njwd.support.Result<java.util.List < com.njwd.entity.platform.vo.SysUserVo>>
     * @author: zhuzs
     * @date: 2019-11-18
     */
    @PostMapping("findUserList")
    public Result<Page<SysUserVo>> findUserList(@RequestBody SysUserDto sysUserDto) {
        return ok(sysUserService.findUserList(sysUserDto));
    }

    /**
     * 查询具体用户信息 user_id
     *
     * @param: [sysUserDto]
     * @return: com.njwd.support.Result
     * @author: zhuzs
     * @date: 2019-11-18
     */
    @PostMapping("toFindUser")
    public Result<SysUserVo> toFindUser(@RequestBody SysUserDto sysUserDto) {
        return ok(sysUserService.toFindUser(sysUserDto));
    }

    /**
     * 获取用户信息 分页
     *
     * @param: [sysMenuDto]
     * @return: com.njwd.support.Result<java.util.List < com.njwd.entity.platform.vo.SysUserVo>>
     * @author: zhuzs
     * @date: 2019-11-20
     */
    @PostMapping("findUserPage")
    public Result<Page<SysUserVo>> findUserPage(@RequestBody SysUserDto sysUserDto) {
        return ok(sysUserService.findUserPage(sysUserDto));
    }

    /**
     * 查询 已分配的租户列表
     *
     * @param: [sysUserDto]
     * @return: com.njwd.support.Result<com.njwd.entity.platform.vo.SysUserVo>
     * @author: zhuzs
     * @date: 2019-11-20
     */
    @PostMapping("findAssedEnterList")
    public Result<SysUserVo> findAssedEnterList(@RequestBody SysUserDto sysUserDto) {
        return ok(sysUserService.findAssedEnterList(sysUserDto));
    }

    /**
     * 用户列表页 导出
     *
     * @param: [sysUserDto, response]
     * @return: void
     * @author: zhuzs
     * @date: 2019-11-26
     */
    @RequestMapping("userListExport")
    public void userListExport(@RequestBody SysUserDto sysUserDto, HttpServletResponse response) {
        sysUserService.userListExport(sysUserDto, response);
    }

}
