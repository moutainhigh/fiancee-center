package com.njwd.platform.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.platform.SysRoleMenu;
import com.njwd.entity.platform.dto.SysMenuDto;
import com.njwd.entity.platform.dto.SysUserDto;
import com.njwd.entity.platform.vo.SysMenuVo;
import com.njwd.entity.platform.vo.SysRoleMenuVo;
import com.njwd.entity.platform.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.platform.mapper.SysMenuMapper;
import com.njwd.platform.mapper.SysUserMapper;
import com.njwd.platform.service.SysMenuService;
import com.njwd.platform.service.SysUserService;
import com.njwd.service.FileService;
import com.njwd.utils.FastUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 菜单/权限
 *
 * @author: zhuzs
 * @date: 2019-11-12
 */
@Service
public class SysMenuServiceImpl implements SysMenuService {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysMenuMapper sysMenuMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private FileService fileService;

    /**
     * 获取用户所有权限信息
     *
     * @param: [sysUserDto]
     * @return: java.util.Set<java.lang.String>
     * @author: zhuzs
     * @date: 2019-11-12
     */
    @Override
    public Set<String> findPermissionDefinitionsByUserId(SysUserDto sysUserDto) {
        //校验用户ID是否未空
        FastUtils.checkParams(sysUserDto.getUserId());
        return sysMenuMapper.findPermissionDefinitionsByUserId(sysUserDto);
    }

    /**
     * 获取 权限树
     *
     * @param: []
     * @return: java.util.List<com.njwd.entity.platform.vo.SysMenuVo>
     * @author: zhuzs
     * @date: 2019-11-14
     */
    @Override
    public List<SysMenuVo> findList() {
        // 返回权限树:先查所有,在java中通过栈遍历分层
        LinkedList<SysMenuVo> allList = sysMenuMapper.findList(null);

        return rebuildData(allList);
    }

    /**
     * 根据类型 获取菜单列表
     *
     * @param: [sysMenuDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.SysMenuVo>
     * @author: zhuzs
     * @date: 2019-11-20
     */
    @Override
    public Page<SysMenuVo> findMenuPageByType(SysMenuDto sysMenuDto) {
        Page<SysMenuVo> page = sysMenuDto.getPage();
        return sysMenuMapper.findPage(page, sysMenuDto);
    }

    /**
     * 返回 实施、运营、产品、管理员 拥有的角色权限列表
     *
     * @param: []
     * @return: java.util.List<com.njwd.entity.platform.vo.SysRoleVo>
     * @author: zhuzs
     * @date: 2019-11-13
     */
    @Override
    public SysRoleMenuVo findUserMenuList() {
        List<SysRoleMenuVo> sysRoleMenuVos = sysUserMapper.findRoleMenuList();
        SysRoleMenuVo sysRoleMenuVo = new SysRoleMenuVo();

        // 实施人员 拥有的默认权限
        List<SysRoleMenu> implementerMenus = new ArrayList<>();
        // 运营人员 拥有的默认权限
        List<SysRoleMenu> operatorMenus = new ArrayList<>();
        // 产品人员 拥有的默认权限
        List<SysRoleMenu> productorMenus = new ArrayList<>();
        // 产品人员 拥有的默认权限
        List<SysRoleMenu> sysManagerMenus = new ArrayList<>();
        if (sysRoleMenuVos != null && sysRoleMenuVos.size() != PlatformConstant.Character.ZERO_I) {
            for (SysRoleMenuVo sysRoleMenu : sysRoleMenuVos) {
                switch (sysRoleMenu.getRoleId().intValue()) {
                    case 1:
                        implementerMenus.add(sysRoleMenu);
                        break;
                    case 2:
                        operatorMenus.add(sysRoleMenu);
                        break;
                    case 3:
                        productorMenus.add(sysRoleMenu);
                        break;
                    case 4:
                        sysManagerMenus.add(sysRoleMenu);
                        break;
                    default:
                }

            }
        }

        sysRoleMenuVo.setImplementerMenus(implementerMenus);
        sysRoleMenuVo.setOperatorMenus(operatorMenus);
        sysRoleMenuVo.setProductorMenus(productorMenus);
        sysRoleMenuVo.setSysManagerMenus(sysManagerMenus);

        return sysRoleMenuVo;
    }

    /**
     * 用户权限表
     *
     * @param: [sysUserDto]
     * @return: java.util.List
     * @author: zhuzs
     * @date: 2019-11-19
     */
    @Override
    public Page<SysMenuVo> findUserRoleMenuList(SysMenuDto sysMenuDto) {

        List<SysUserVo> userList = sysMenuDto.getUserList();
        if (userList == null || userList.size() == PlatformConstant.Character.ZERO_I) {
            SysUserDto sysUserDto = new SysUserDto();
            Page<SysUserVo> page = sysUserDto.getPage();
            fileService.resetPage(page);
            // http 请求
            Map<String, Object> result = sysUserService.doCoreHttpRequest(sysUserDto, PlatformConstant.EnterpriseAnduserManage.USER_ALL);
            if(! result.get("status").equals(PlatformConstant.Status.SUCCESS)){
                throw new ServiceException(ResultCode.FIND_ENTERPRISE_LIST_FAIL);
            }
            userList = JSONArray.parseArray(result.get("data").toString(), SysUserVo.class);
        }

        // 排序操作
        sysUserService.sortOperation(userList);

        sysMenuDto.setUserList(userList);
        Page<SysMenuVo> page = sysMenuDto.getPage();
        List<SysMenuVo> sysMenuVos = sysUserMapper.findUserRoleMenuList(page, sysMenuDto);

        for (SysMenuVo sysMenuVo : sysMenuVos) {
            for (SysUserVo userVo : userList) {
                if (sysMenuVo.getUserId().equals(userVo.getUser_id())) {
                    sysMenuVo.setUserName(userVo.getName());
                    sysMenuVo.setMobile(userVo.getMobile());
                }
            }
        }

        return page.setRecords(sysMenuVos);
    }


    /**
     * 用户权限表 导出
     *
     * @param: [sysMenuDto, response]
     * @return: void
     * @author: zhuzs
     * @date: 2019-11-26
     */
    @Override
    public void userRoleMenuListExport(SysMenuDto sysMenuDto, HttpServletResponse response) {
        Page<SysMenuVo> page = sysMenuDto.getPage();
        fileService.resetPage(page);

        fileService.exportExcel(response, findUserRoleMenuList(sysMenuDto).getRecords(),
                new ExcelColumn("mobile", "手机号码"),
                new ExcelColumn("userName", "员工姓名"),
                new ExcelColumn("fLevelName", "子系统"),
                new ExcelColumn("tLevelName", "功能菜单"),
                new ExcelColumn("childName", "功能按钮")
        );

    }

    /**
     * 数据重构
     *
     * @param: [sysUserMenuVos]
     * @return: java.util.List<com.njwd.entity.platform.vo.SysMenuVo>
     * @author: zhuzs
     * @date: 2019-11-20
     */
    @Override
    public List<SysMenuVo> rebuildData(List<SysMenuVo> sysUserMenuVos) {
        // 取出根级权限列表
        List<SysMenuVo> rootMenuList = new ArrayList<>();
        Iterator<SysMenuVo> iterator = sysUserMenuVos.iterator();

        while (iterator.hasNext()) {
            SysMenuVo sysMenuVo = iterator.next();
            if (sysMenuVo.getParentId() == null || 0 == sysMenuVo.getParentId()) {
                rootMenuList.add(sysMenuVo);
                iterator.remove();
            }
        }
        // 通过栈遍历分层
        LinkedList<SysMenuVo> stack = new LinkedList<>(rootMenuList);
        List<SysMenuVo> emptyList = Collections.emptyList();
        while (!stack.isEmpty()) {
            SysMenuVo vo = stack.pop();
            List<SysMenuVo> subList = new LinkedList<>();
            vo.setSysMenuList(subList);
            iterator = sysUserMenuVos.iterator();
            while (iterator.hasNext()) {
                SysMenuVo sysMenuVo = iterator.next();
                if (vo.getMenuId().equals(sysMenuVo.getParentId())) {
                    subList.add(sysMenuVo);
                    // 过滤 非末级菜单
                    if (Constant.Is.NO.equals(sysMenuVo.getIsChild())) {
                        stack.push(sysMenuVo);
                    } else {
                        sysMenuVo.setSysMenuList(emptyList);
                    }
                    // 从待分配list中删除该元素(一个子权限只会有一个父权限)
                    iterator.remove();
                }
            }
        }
        return rootMenuList;
    }

}
