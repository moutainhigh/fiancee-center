package com.njwd.platform.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.BaseEntity;
import com.njwd.entity.platform.dto.SysUserDto;
import com.njwd.entity.platform.remore.resp.UserLogin;
import com.njwd.entity.platform.vo.SysUserVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 用户
 *
 * @author zhuzs
 * @date 2019-11-12 09:46
 */
public interface SysUserService {
    /**
     * 登录
     *
     * @param userLogin
     * @param id
     * @return
     */
    SysUserVo login(UserLogin userLogin, String id);

    /**
     * 新增用户及用户权限信息
     *
     * @param sysUserDto
     * @return
     */
    Integer addUserWithMenuInfo(SysUserDto sysUserDto);

    /**
     * 批量删除
     *
     * @param: [sysUserDto]
     * @return: com.njwd.support.BatchResult
     * @author: zhuzs
     * @date: 2019-11-12
     */
    BatchResult deleteBatch(SysUserDto sysUserDto);

    /**
     * 修改用户
     *
     * @param: [sysUserDto]
     * @return: java.lang.Integer
     * @author: zhuzs
     * @date: 2019-11-12
     */
    Integer updateUser(SysUserDto sysUserDto);

    /**
     * 分配租户
     *
     * @param: [sysUserDto]
     * @return: com.njwd.support.BatchResult
     * @author: zhuzs
     * @date: 2019-11-12
     */
    Integer assignEnterprises(SysUserDto sysUserDto);

    /**
     * 复制权限
     *
     * @param: [sysUserDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.SysMenuVo>
     * @author: zhuzs
     * @date: 2019-11-18
     */
    Integer copyAssignedMenusAndEnterprises(SysUserDto sysUserDto);

    /**
     * 查询具体用户信息 - core
     *
     * @param: [sysUserDto]
     * @return: com.njwd.entity.platform.vo.SysUserVo
     * @author: zhuzs
     * @date: 2019-11-18
     */
    SysUserVo toFindUser(SysUserDto sysUserDto);

    /**
     * 获取用户列表
     *
     * @param: [sysUserDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.SysUserVo>
     * @author: zhuzs
     * @date: 2019-11-18
     */
    Page<SysUserVo> findUserList(SysUserDto sysUserDto);

    /**
     * 查询 已分配的租户列表
     *
     * @param: [sysUserDto]
     * @return: com.njwd.entity.platform.vo.SysUserVo
     * @author: zhuzs
     * @date: 2019-11-20
     */
    SysUserVo findAssedEnterList(SysUserDto sysUserDto);

    /**
     * 获取用户信息 分页
     *
     * @param: [sysUserDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.SysUserVo>
     * @author: zhuzs
     * @date: 2019-11-20
     */
    Page<SysUserVo> findUserPage(SysUserDto sysUserDto);

    /**
     * 用户列表页 导出
     *
     * @param: [sysUserDto, response]
     * @return: void
     * @author: zhuzs
     * @date: 2019-11-26
     */
    void userListExport(SysUserDto sysUserDto, HttpServletResponse response);

    /**
     * 排序操作
     *
     * @param: [sysUserVoList]
     * @return: void
     * @author: zhuzs
     * @date: 2019-12-02
     */
    void sortOperation(List<SysUserVo> sysUserVoList);

    /**
     * Core http 请求
     *
     * @param: [param, childUrl]
     * @return: java.util.Map<java.lang.String,java.lang.Object>
     * @author: zhuzs
     * @date: 2019-12-02
     */
    Map<String, Object> doCoreHttpRequest(BaseEntity param, String childUrl);
}
