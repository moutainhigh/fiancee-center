package com.njwd.platform.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.AccountBookCategoryDto;
import com.njwd.entity.platform.dto.SysMenuDto;
import com.njwd.entity.platform.dto.SysUserDto;
import com.njwd.entity.platform.vo.SysMenuVo;
import com.njwd.entity.platform.vo.SysRoleMenuVo;
import com.njwd.platform.service.SysMenuService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 菜单/权限
 *
 * @author zhuzs
 * @date 2019-11-21 11:37
 */
@RestController
@RequestMapping("sysMenu")
public class SysMenuController extends BaseController {
    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 获取 权限组织树
     *
     * @param: []
     * @return: com.njwd.support.Result
     * @author: zhuzs
     * @date: 2019-11-12
     */
    @PostMapping("findMenuTree")
    public Result<List<SysMenuVo>> findMenuTree() {
        return ok(sysMenuService.findList());
    }

    /**
     * 根据类型 获取菜单列表 分页
     *
     * @param: [sysMenuDto]
     * @return: com.njwd.support.Result<java.util.List < com.njwd.entity.platform.vo.SysMenuVo>>
     * @author: zhuzs
     * @date: 2019-11-20
     */
    @PostMapping("findMenuPageByType")
    public Result<Page<SysMenuVo>> findMenuPageByType(@RequestBody SysMenuDto sysMenuDto) {
        return ok(sysMenuService.findMenuPageByType(sysMenuDto));
    }

    /**
     * 返回 实施、运营、产品、管理员 拥有的角色权限列表
     * role_id: 1:实施、2:运营、3:产品、4:管理员
     *
     * @param: []
     * @return: com.njwd.support.Result<java.util.List < com.njwd.entity.platform.SysRoleMenu>>
     * @author: zhuzs
     * @date: 2019-11-18
     */
    @PostMapping("findRoleMenuList")
    public Result<SysRoleMenuVo> findRoleMenuList() {
        return ok(sysMenuService.findUserMenuList());
    }

    /**
     * 用户权限表
     *
     * @param: [sysUserDto]
     * @return: com.njwd.support.Result<java.util.List < com.njwd.entity.platform.SysRoleMenu>>
     * @author: zhuzs
     * @date: 2019-11-18
     */
    @PostMapping("findUserRoleMenuList")
    public Result<Page<SysMenuVo>> findUserRoleMenuList(@RequestBody SysMenuDto sysMenuDto) {
        return ok(sysMenuService.findUserRoleMenuList(sysMenuDto));
    }

    /**
     * 用户权限表 导出
     *
     * @param: [sysMenuDto, response]
     * @return: void
     * @author: zhuzs
     * @date: 2019-11-26
     */
    @RequestMapping("userRoleMenuListExport")
    public void userRoleMenuListExport(@RequestBody SysMenuDto sysMenuDto, HttpServletResponse response){
        sysMenuService.userRoleMenuListExport(sysMenuDto,response);
    }
}

