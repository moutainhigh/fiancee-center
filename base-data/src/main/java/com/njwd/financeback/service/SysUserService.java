package com.njwd.financeback.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.SysUserEnterprise;
import com.njwd.entity.basedata.dto.SysUserDto;
import com.njwd.entity.basedata.remote.resp.UserLogin;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.basedata.vo.UserAccountBookEntityVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author xyyxhcj@qq.com
 * @since 2019-05-22
 */

public interface SysUserService {

    /**
     * 自动登录
     *
     * @param userLogin userLogin
     * @param sessionId sessionId
     * @return sysUserVo
     */
    SysUserVo login(UserLogin userLogin, String sessionId);

    /**
     * 添加租户下的用户
     *
     * @param sysUserDto sysUserDto
     */
    void addUser(SysUserDto sysUserDto);

    /**
     * 分页
     *
     * @param sysUserDto sysUserDto
     * @return page
     */
    Page<SysUserVo> findPage(SysUserDto sysUserDto);

    /**
     * 查询当前租户下有权限的用户分页
     *
     * @param sysUserDto sysUserDto
     * @return page
     */
    Page<SysUserVo> findPermPage(SysUserDto sysUserDto);

    /**
     * 根据公司下的岗位反查用户
     *
     * @param sysUserDto sysUserDto
     * @return page
     */
    Page<SysUserVo> findRoleUserPage(SysUserDto sysUserDto);

    /**
     * 根据id查询用户
     *
     * @param sysUserDto sysUserDto
     * @return vo
     */
    SysUserVo findDetail(SysUserDto sysUserDto);

    /**
     * 查询用户Vo 总账内部调用
     *
     * @param sysUserDto sysUserDto
     * @return com.njwd.entity.basedata.vo.SysUserVo
     * @author xyyxhcj@qq.com
     * @date 2019/9/6 11:24
     **/
    SysUserVo findUserVo(SysUserDto sysUserDto);

    /**
     * 禁用/反禁用 租户的用户
     *
     * @param sysUserEnterprise sysUserEnterprise
     * @param operator          operator
     * @return int
     */
    int updateUserEnterprise(SysUserEnterprise sysUserEnterprise, SysUserVo operator);

    /**
     * 批量禁用/反禁用/删除
     *
     * @param sysUserDto sysUserDto
     * @param operator   operator
     * @return int
     */
    int updateEnterpriseUserBatch(SysUserDto sysUserDto, SysUserVo operator);

    /**
     * 查询未引入的用户分页
     *
     * @param sysUserDto sysUserDto
     * @return page
     */
    Page<SysUserVo> findNotImportPage(SysUserDto sysUserDto);

    /**
     * 查询当前企业可选用户列表 分页
     *
     * @param sysUserDto
     * @return
     */
    Page<SysUserVo> findEnableList(SysUserDto sysUserDto);

    void exportExcel(SysUserDto sysUserDto, HttpServletResponse response);

    /**
     * 修改个人信息
     *
     * @param sysUserDto sysUserDto
     * @param operator
     * @return int
     */
    int updateBySelf(SysUserDto sysUserDto, SysUserVo operator);

    /**
     * 批量添加用户
     *
     * @param sysUserDto sysUserDto
     * @param operator   操作人
     * @return userIds
     */
    List<Long> addUserBatch(SysUserDto sysUserDto, SysUserVo operator);

    /**
     * http 批量添加用户
     *
     * @param: [sysUserDto, operator]
     * @return: java.util.List<java.lang.Long>
     * @author: zhuzs
     * @date: 2019-11-21
     */
    List<Long> httpAddUserBatch(SysUserDto sysUserDto, SysUserVo operator);

    /**
     * 获取公司下的账簿主体 根据userId分类
     *
     * @param sysUserDto sysUserDto
     * @return map
     */
    Map<Long, List<UserAccountBookEntityVo>> findAccountBookEntityByCompany(SysUserDto sysUserDto);

    /**
     * 批量 禁用/反禁用
     *
     * @param sysUserDto sysUserDto
     * @param operator
     * @return BatchResult
     */
    BatchResult updateBatch(SysUserDto sysUserDto, SysUserVo operator);

    /**
     * 批量删除租户下的用户
     *
     * @param sysUserDto sysUserDto
     * @return BatchResult
     */
    BatchResult updateBatchDelete(SysUserDto sysUserDto);

    /**
     * 查个人的自定义数据
     *
     * @param operator operator
     * @return com.njwd.entity.basedata.SysUserEnterprise
     * @author xyyxhcj@qq.com
     * @date 2019/8/20 19:44
     **/
    SysUserEnterprise findConfig(SysUserVo operator);
}
