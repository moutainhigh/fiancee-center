package com.njwd.financeback.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.basedata.SysRole;
import com.njwd.entity.basedata.dto.SysRoleDto;
import com.njwd.entity.basedata.dto.SysUserDto;
import com.njwd.entity.basedata.dto.UserRoleDto;
import com.njwd.entity.basedata.vo.SysRoleVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.basedata.vo.UserRoleVo;
import com.njwd.exception.ResultCode;
import com.njwd.financeback.service.SysMenuService;
import com.njwd.financeback.service.SysRoleService;
import com.njwd.logger.SenderService;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 岗位 前端控制器
 *
 * @author xyyxhcj@qq.com
 * @since 2019/05/21
 */
@RestController
@RequestMapping("sysRole")
public class SysRoleController extends BaseController {
    @Resource
    private SysRoleService sysRoleService;
    @Resource
    private SysMenuService sysMenuService;
    @Resource
    private SenderService senderService;

    /**
     * 平台管理员创建通用角色/岗位
     */
    @PostMapping("add")
    public Result<Long> add(@RequestBody SysRole sysRole) {
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.SYS_ADMIN, null);
        FastUtils.checkParams(sysRole.getName());
        SysUserVo operator = UserUtils.getUserVo();
        sysRole.setCreatorId(operator.getUserId());
        sysRole.setCreatorName(operator.getName());
        return ok(sysRoleService.add(sysRole));
    }

    @PostMapping("addEnterprise")
    public Result<Long> addEnterprise(@RequestBody SysRoleDto sysRoleDto) {
        return addEnterpriseWithPerm(sysRoleDto);
    }

    /**
     * 创建租户岗位同时分配权限
     */
    @PostMapping("addEnterpriseWithPerm")
    public Result<Long> addEnterpriseWithPerm(@RequestBody SysRoleDto sysRoleDto) {
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        FastUtils.checkParams(sysRoleDto.getName());
        sysRoleDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        Long result = RedisUtils.lock(String.format(Constant.LockKey.SYS_ROLE_CODE, operator.getRootEnterpriseId(), sysRoleDto.getCode()), Constant.SysConfig.REDIS_LOCK_TIMEOUT, () -> {
            sysRoleService.checkCode(sysRoleDto);
            return sysRoleService.addEnterpriseWithPerm(sysRoleDto, operator);
        });
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.postAuth, LogConstant.operation.add, LogConstant.operation.add, result.toString()));
        return ok(result);
    }

    @PostMapping("update")
    public Result<Long> update(@RequestBody SysRole sysRole) {
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.SYS_ADMIN, null);
        SysUserVo operator = UserUtils.getUserVo();
        sysRole.setUpdatorId(operator.getUserId());
        sysRole.setUpdatorName(operator.getName());
        sysRole.setIsEnable(null);
        sysRole.setIsDel(null);
        sysRole.setRootEnterpriseId(0L);
        return ok(sysRoleService.update(sysRole));
    }

    @PostMapping("updateEnterprise")
    public Result<Long> updateEnterprise(@RequestBody SysRole sysRole) {
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        sysRole.setUpdatorId(operator.getUserId());
        sysRole.setUpdatorName(operator.getName());
        sysRole.setIsEnable(null);
        sysRole.setIsDel(null);
        sysRole.setRootEnterpriseId(operator.getRootEnterpriseId());
        Long id = RedisUtils.lock(String.format(Constant.LockKey.SYS_ROLE_CODE, operator.getRootEnterpriseId(), sysRole.getCode()), Constant.SysConfig.REDIS_LOCK_TIMEOUT, () -> {
            sysRoleService.checkCode(sysRole);
            return sysRoleService.update(sysRole);
        });
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.postAuth, LogConstant.operation.update, LogConstant.operation.update_type, sysRole.getRoleId().toString()));
        return ok(id);
    }

    /**
     * 禁用
     */
    @PostMapping("disableEnterpriseBatch")
    public Result<BatchResult> disableBatch(@RequestBody SysRoleDto sysRoleDto) {
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        sysRoleDto.setIsEnable(Constant.Is.NO);
        BatchResult batchResult = sysRoleService.updateBatch(sysRoleDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.postAuth, LogConstant.operation.forbidden, LogConstant.operation.forbidden_type, batchResult.getSuccessList().toString()));
        return ok(batchResult);
    }

    /**
     * 反禁用
     */
    @PostMapping("enableEnterpriseBatch")
    public Result<BatchResult> enableBatch(@RequestBody SysRoleDto sysRoleDto) {
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        sysRoleDto.setIsEnable(Constant.Is.YES);
        BatchResult batchResult = sysRoleService.updateBatch(sysRoleDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.postAuth, LogConstant.operation.antiForbidden, LogConstant.operation.antiForbidden_type, batchResult.getSuccessList().toString()));
        return ok(batchResult);
    }

    @PostMapping("deleteEnterpriseBatch")
    public Result<BatchResult> deleteEnterpriseBatch(@RequestBody SysRoleDto sysRoleDto) {
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        BatchResult batchResult = sysRoleService.updateBatchDelete(sysRoleDto);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()),
                LogConstant.sysName.FinanceBackSys, LogConstant.menuName.postAuth,
                LogConstant.operation.deleteBatch, LogConstant.operation.deleteBatch_type, batchResult.getSuccessList().toString()));
        return ok(batchResult);
    }

    @PostMapping("deleteBatch")
    public Result deleteBatch(@RequestBody SysRoleDto sysRoleDto) {
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.SYS_ADMIN, null);
        sysRoleDto.setIsDel(Constant.Is.YES);
        SysUserVo operator = UserUtils.getUserVo();
        // 把管理员的租户id设为0,防止误操作租户数据
        operator.setRootEnterpriseId(0L);
        return confirm(sysRoleService.updateBatch(sysRoleDto, operator));
    }

    @PostMapping("findById")
    public Result<SysRoleVo> findById(@RequestBody SysRoleDto sysRoleDto) {
        return ok(sysRoleService.findById(sysRoleDto));
    }

    @PostMapping("findPage")
    public Result<Page<SysRoleVo>> findPage(@RequestBody SysRoleDto sysRoleDto) {
        if (sysRoleDto.getRootEnterpriseId() == null) {
            sysRoleDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        }
        return ok(sysRoleService.findPage(sysRoleDto));
    }

    /**
     * rootEnterpriseId必有,有userId则查询对应的角色,无userId则查询租户下的所有岗位
     */
    @PostMapping("findList")
    public Result<List<SysRoleVo>> findList(@RequestBody SysUserDto sysUserDto) {
        if (sysUserDto.getRootEnterpriseId() == null) {
            sysUserDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        }
        return ok(sysRoleService.findList(sysUserDto));
    }

    /**
     * 配置通用岗位的权限
     */
    @PostMapping("assign")
    public Result assign(@RequestBody SysRoleDto sysRoleDto) {
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.SYS_ADMIN, null);
        FastUtils.checkParams(sysRoleDto.getRoleId());
        sysRoleDto.setRootEnterpriseId(0L);
        sysMenuService.assign(sysRoleDto, UserUtils.getUserVo());
        return ok(true);
    }

    @PostMapping("assignEnterprise")
    public Result assignEnterprise(@RequestBody SysRoleDto sysRoleDto) {
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        FastUtils.checkParams(sysRoleDto.getRoleId());
        sysRoleDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        sysMenuService.assign(sysRoleDto, operator);
        senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.postAuth, LogConstant.operation.auth, LogConstant.operation.auth_type, sysRoleDto.getRoleId().toString()));
        return ok(true);
    }

    /**
     * 查询编码是否重复
     */
    @PostMapping("checkCode")
    public Result checkCode(@RequestBody SysRoleDto sysRoleDto) {
        FastUtils.checkParams(sysRoleDto.getCode());
        sysRoleDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        sysRoleService.checkCode(sysRoleDto);
        return ok(true);
    }


    /**
     * 用户权限列表 分页
     */
    @PostMapping("findUserRolePage")
    public Result<Page<UserRoleVo>> findUserRolePage(@RequestBody UserRoleDto userRoleDto) {
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        userRoleDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        return ok(sysRoleService.findUserRolePage(userRoleDto));
    }

    /**
     * 查询当前企业可用岗位列表 分页
     */
    @RequestMapping("findEnableRoleList")
    public Result<Page<SysRoleVo>> findEnableRoleList(@RequestBody SysRoleDto sysRoleDto) {
        SysUserVo operator = UserUtils.getUserVo();
        ShiroUtils.checkRole(Constant.ShiroAdminDefi.BUSINESS_ADMIN, operator.getRootEnterpriseId());
        sysRoleDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        return ok(sysRoleService.findEnableList(sysRoleDto));
    }

    /**
     * 校验数据是否被引用,被引用则不允许修改
     */
    @PostMapping("checkRefer")
    public Result checkRefer(@RequestBody SysRoleDto sysRoleDto) {
        if (sysRoleService.checkRefer(sysRoleDto.getRoleId())) {
            return ok(true);
        } else {
            return ok(ResultCode.ROLE_REFER_USER, null);
        }
    }
}
