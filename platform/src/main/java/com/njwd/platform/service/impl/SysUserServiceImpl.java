package com.njwd.platform.service.impl;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.basedata.excel.ExcelColumn;
import com.njwd.entity.platform.BaseEntity;
import com.njwd.entity.platform.SysMenu;
import com.njwd.entity.platform.SysUserEnterprise;
import com.njwd.entity.platform.dto.SysUserDto;
import com.njwd.entity.platform.remore.resp.UserLogin;
import com.njwd.entity.platform.vo.RootEnterpriseVo;
import com.njwd.entity.platform.vo.SysMenuVo;
import com.njwd.entity.platform.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.platform.config.YmlProperties;
import com.njwd.platform.handler.UserRealm;
import com.njwd.platform.mapper.SysUserMapper;
import com.njwd.platform.service.SysMenuService;
import com.njwd.platform.service.SysUserService;
import com.njwd.platform.utils.UserUtil;
import com.njwd.service.FileService;
import com.njwd.support.BatchResult;
import com.njwd.utils.EncryptBase64Util;
import com.njwd.utils.FastUtils;
import com.njwd.utils.HttpUtils;
import com.njwd.utils.JsonUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户
 *
 * @author zhuzs
 * @date 2019-11-12 09:47
 */
@Service
public class SysUserServiceImpl implements SysUserService {
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysMenuService sysMenuService;
    @Autowired
    private YmlProperties ymlProperties;
    @Autowired
    private FileService fileService;

    /**
     * 登录
     *
     * @param: [userLogin]
     * @return: com.njwd.entity.basedata.vo.SysUserVo
     * {userId、name、mobile}、account、adminType
     * @author: zhuzsexportExcel
     * @date: 2019-11-12
     */
    @Override
    public SysUserVo login(UserLogin userLogin, String sessionId) {
        // 请求路径
        String url = ymlProperties.getNjwdCoreUrl() + PlatformConstant.EnterpriseAnduserManage.LOGIN_URL;

        // Base64 加密
        String encryptPassword = "";
        byte[] passwordBytes = userLogin.getPassword().getBytes();
        encryptPassword = EncryptBase64Util.encryptBASE64(passwordBytes);
        userLogin.setPassword(encryptPassword);

        // 根据请求参数 生成 json_str
        userLogin.setTimestamp(System.currentTimeMillis());
        userLogin.setSign(UserUtil.generateSign());
        String json_str = JsonUtils.object2JsonIgNull(userLogin);
        // Http请求 设置 url、参数
        JSONObject result = UserUtil.doPostRequest(url, json_str);

        //判断 返回状态
        if (!result.get("status").toString().equals(PlatformConstant.ResResult.SUCCESS)) {
            throw new ServiceException(ResultCode.ACCOUNT_OR_PASSWORD_NOEXIST);
        }

        // 处理返回结果
        Map<String, Object> resultMap = (Map<String, Object>) result.get("data");
        userLogin.setUser_id(Long.valueOf(resultMap.get("id").toString()));
        userLogin.setUser_name(resultMap.get("name").toString());
        userLogin.setAdmin_type(Integer.valueOf(resultMap.get("is_admin").toString()));
        userLogin.setMobile(resultMap.get("mobile").toString());
        userLogin.setAccount(resultMap.get("account").toString());

        // 返回用户信息
        Subject subject = SecurityUtils.getSubject();
        UserRealm.AutoLoginToken authenticationToken = new UserRealm.AutoLoginToken(userLogin);
        subject.login(authenticationToken);
        SysUserVo sysUserVo = UserUtil.getUserVo();
        // 用户 菜单/权限 信息
        fill(sysUserVo);
        sysUserVo.setAuthorization(sessionId);

        return sysUserVo;
    }

    /**
     * 新增用户及用户权限信息
     *
     * @param: [sysUserDto] add_type
     * @return: com.njwd.entity.platform.vo.SysUserVo
     * @author: zhuzs
     * @date: 2019-11-12
     */
    @Override
    @Transactional
    public Integer addUserWithMenuInfo(SysUserDto sysUserDto) {
        SysUserVo operator = UserUtil.getUserVo();
        sysUserDto.setAccount(sysUserDto.getAccount() == null ? "" : sysUserDto.getAccount());
        // http 请求
        Map<String, Object> result = doCoreHttpRequest(sysUserDto, PlatformConstant.EnterpriseAnduserManage.ADD_USER);
        // 获取 返回状态
        if (result.get("status").toString().equals(PlatformConstant.ResResult.FAIL)) {
            String statusCOde = result.get("statusCode").toString();
            // statusCode = 1,用户在本组织已存在，无需创建
            if (statusCOde.equals(PlatformConstant.StatusCode.EXIT_ONE)) {
                throw new ServiceException(ResultCode.USER_EXIT);
            }
            // statusCode = 2,用户账号已注册，请确认信息，是否将其添加至本企业？
            if (statusCOde.equals(PlatformConstant.StatusCode.EXIT_TWO)) {
                return PlatformConstant.Character.ZERO_I;
            } else {
                // 异常
                throw new ServiceException(ResultCode.ADD_USER_FAIL);
            }
        }

        // 获取 user_id
        sysUserDto.setUserId(Long.valueOf(result.get("user_id").toString()));
        // 本地存入用户权限权限信息
        if (sysUserDto.getToAssMenuIdList() != null && sysUserDto.getToAssMenuIdList().size() != PlatformConstant.Character.ZERO_I) {
            sysUserMapper.addSysUserMenu(sysUserDto, operator);
        }
        // 存入用户角色信息
        List<Long> roleIdList = sysUserDto.getRoleIdList();
        if(roleIdList != null && roleIdList.size() != PlatformConstant.Character.ZERO_I ){
            sysUserMapper.addSysUserRole(sysUserDto,operator);
        }

        return PlatformConstant.Character.ONE_I;
    }

    /**
     * 批量删除
     *
     * @param: [sysUserDto]
     * @return: com.njwd.support.BatchResult
     * @author: zhuzs
     * @date: 2019-11-12
     */
    @Override
    public BatchResult deleteBatch(SysUserDto sysUserDto) {
        // 操作详情
        BatchResult batchResult = new BatchResult();
        // 存在引用关系的用户
        List<Long> refUserIdList = null;
        // 批量删除 userId
        List<Long> toDelUserIdList = sysUserDto.getUserIdList();

        sysUserDto.setUserIdList(toDelUserIdList);
        refUserIdList = sysUserMapper.findUserIds(sysUserDto);
        toDelUserIdList.removeAll(refUserIdList);

        String user_ids = "";
        if (toDelUserIdList.size() != PlatformConstant.Character.ZERO_I) {
            for (Long userId : toDelUserIdList) {
                user_ids = user_ids + userId + ",";
            }
            user_ids = user_ids.substring(0, user_ids.lastIndexOf(","));
            sysUserDto.setUser_ids(user_ids);


            Map<String, Object> result = doCoreHttpRequest(sysUserDto, PlatformConstant.EnterpriseAnduserManage.DEL_USERS);
            if (result.get("status").toString().equals(PlatformConstant.Status.FAIL)) {
                if (result.get("not_del_user_ids") != null) {
                    List<Long> failIdList = JSONArray.parseArray(result.get("not_del_user_ids").toString(), Long.class);
                    refUserIdList.addAll(failIdList);
                    toDelUserIdList.removeAll(failIdList);
                } else {
                    throw new ServiceException(ResultCode.DEL_USERS_FAIL);
                }
            }
        }

        if(toDelUserIdList != null && toDelUserIdList.size() != PlatformConstant.Character.ZERO_I){
            sysUserDto.setUserIdList(toDelUserIdList);
        // 删除用户角色
            sysUserMapper.delUserRoleByUserIds(sysUserDto);
        // 删除用户权限
            sysUserMapper.deleteUserMenuByUserId(sysUserDto);
        }


        batchResult.setSuccessList(toDelUserIdList);
        batchResult.setFailIdList(refUserIdList);

        return batchResult;
    }

    /**
     * 修改用户
     *
     * @param: [sysUserDto] 用户名、用户—菜单/权限 列表
     * @return: java.lang.Integer
     * @author: zhuzs
     * @date: 2019-11-12
     */
    @Override
    @Transactional
    public Integer updateUser(SysUserDto sysUserDto) {
        SysUserVo operator = UserUtil.getUserVo();
        // 删除 并 新增 权限信息
        sysUserMapper.deleteUserMenuByUserId(sysUserDto);
        if (sysUserDto.getToAssMenuIdList() != null && sysUserDto.getToAssMenuIdList().size() != 0) {
            sysUserMapper.addSysUserMenu(sysUserDto, operator);
        }

        // 删除 并 新增 用户角色
        sysUserMapper.delUserRoleByUserIds(sysUserDto);
        if(sysUserDto.getRoleIdList() != null && sysUserDto.getRoleIdList().size() != PlatformConstant.Character.ZERO_I){
            sysUserMapper.addSysUserRole(sysUserDto,operator);
        }

        // 修改用户信息-Core
        sysUserDto.setUser_id(sysUserDto.getUserId());
        Map<String, Object> result = doCoreHttpRequest(sysUserDto, PlatformConstant.EnterpriseAnduserManage.UPDATE_USER);
        if (!result.get("status").toString().equals(PlatformConstant.ResResult.SUCCESS)) {
            throw new ServiceException(ResultCode.UPDATE_USER_FAIL);
        }

        return PlatformConstant.Character.ONE_I;
    }

    /**
     * 分配租户
     *
     * @param: [sysUserDto]
     * @return: com.njwd.support.BatchResult
     * @author: zhuzs
     * @date: 2019-11-12
     */
    @Override
    @Transactional
    public Integer assignEnterprises(SysUserDto sysUserDto) {
        SysUserVo operator = UserUtil.getUserVo();
        // 待返回 Core 已删除/待分配 租户IDs
        String del_ids = "";
        List<SysUserEnterprise> delList = new ArrayList<>();
        String add_ids = "";
        List<SysUserEnterprise> addList = new ArrayList<>();
        // 已分配租户信息
        List<SysUserEnterprise> assignedEnterpriseList = sysUserMapper.findAssnedEnterList(sysUserDto);
        // 待分配租户信息
        List<SysUserEnterprise> toAssignEnterpriseList = sysUserDto.getSelectedEnterList();

        // 暂无已分配租户，只做新增操作
        if (assignedEnterpriseList == null || assignedEnterpriseList.size() == PlatformConstant.Character.ZERO_I) {
            if (toAssignEnterpriseList != null && toAssignEnterpriseList.size() != PlatformConstant.Character.ZERO_I) {
                for (SysUserEnterprise toAss : toAssignEnterpriseList) {
                    addList.add(toAss);
                    add_ids +=  toAss.getRootEnterpriseId().toString() + ",";
                }
                add_ids = add_ids.substring(0, add_ids.lastIndexOf(","));
            }

            // 暂无待分配租户，做清空操作
        } else if (toAssignEnterpriseList == null || toAssignEnterpriseList.size() == PlatformConstant.Character.ZERO_I) {
            for (SysUserEnterprise assEd : assignedEnterpriseList) {
                delList.add(assEd);
                del_ids +=  assEd.getRootEnterpriseId().toString() + ",";
            }
            del_ids = del_ids.substring(0, del_ids.lastIndexOf(","));
        } else {
            boolean flag = false;
            // 已删除的
            for (SysUserEnterprise assEd : assignedEnterpriseList) {
                flag = false;
                for (SysUserEnterprise toAss : toAssignEnterpriseList) {
                    if (toAss.getRootEnterpriseId().equals(assEd.getRootEnterpriseId())) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    delList.add(assEd);
                    del_ids += assEd.getRootEnterpriseId().toString() + ",";
                }
            }
            if (del_ids.length() != PlatformConstant.Character.ZERO_I) {
                del_ids = del_ids.substring(0, del_ids.lastIndexOf(","));
            }

            // 新增的
            for (SysUserEnterprise toAss : toAssignEnterpriseList) {
                flag = false;
                for (SysUserEnterprise assEd : assignedEnterpriseList) {
                    if (toAss.getRootEnterpriseId().equals(assEd.getRootEnterpriseId())) {
                        flag = true;
                    }
                }
                if (!flag) {
                    addList.add(toAss);
                    add_ids +=  toAss.getRootEnterpriseId().toString() + ",";
                }
            }
            if (add_ids.length() != PlatformConstant.Character.ZERO_I) {
                add_ids = add_ids.substring(0, add_ids.lastIndexOf(","));
            }

        }
        sysUserDto.setAdd_ids(add_ids);
        sysUserDto.setDel_ids(del_ids);
        sysUserDto.setDelList(delList);
        sysUserDto.setToAssEnterList(addList);

        // 分配租户，调用 Core 接口
        Map<String, Object> result = new HashMap<>();
        if (addList.size() != PlatformConstant.Character.ZERO_I) {
            // http 请求
            result = doCoreHttpRequest(sysUserDto, PlatformConstant.EnterpriseAnduserManage.ASSIGN_ENTERPRISE);
            if (!result.get("status").toString().equals(PlatformConstant.ResResult.SUCCESS)) {
                throw new ServiceException(ResultCode.UPDATE_USER_FAIL);
            }
        }

        // 分配租户，本地
        if (delList.size() != PlatformConstant.Character.ZERO_I) {
            sysUserMapper.delAssignedEnterpriseByEnterpriseIds(sysUserDto);
        }
        if (addList.size() != PlatformConstant.Character.ZERO_I) {
            sysUserMapper.assEnterprises(sysUserDto, operator);
        }

        // 分配租户，调用基础资料接口
        if (addList.size() != PlatformConstant.Character.ZERO_I) {
            doDataBaseHttpReq(sysUserDto, operator, addList, result);
        }

        return PlatformConstant.Character.ONE_I;
    }

    /**
     * 复制权限
     *
     * @param: [sysUserDto] 已选择 用户IDs
     * @return: java.util.List<com.njwd.entity.platform.vo.SysMenuVo>
     * @author: zhuzs
     * @date: 2019-11-18
     */
    @Override
    @Transactional
    public Integer copyAssignedMenusAndEnterprises(SysUserDto sysUserDto) {
        SysUserVo operator = UserUtil.getUserVo();
        // 获取待配置权限和待分配租户
        sysUserDto.setUser_id(sysUserDto.getToAssUserId());
        List<SysMenuVo> toAssMenuList = sysUserMapper.findAssedMenuList(sysUserDto);
        List<SysUserEnterprise> toAssEnterList = sysUserMapper.findAssnedEnterList(sysUserDto);

        // 目标用户
        List<SysUserVo> sysUserVoList = sysUserDto.getSysUserVoList();
        if (sysUserVoList.size() != PlatformConstant.Character.ZERO_I) {
            for (SysUserVo user : sysUserVoList) {
                sysUserDto.setUser_id(user.getUserId());
                sysUserDto.setMobile(user.getMobile());
                if (toAssMenuList != null && toAssMenuList.size() != PlatformConstant.Character.ZERO_I.intValue()) {
                    // 复制权限信息
                    // 已配置
                    List<SysMenuVo> assedMenuVos = sysUserMapper.findAssedMenuList(sysUserDto);
                    List<SysMenuVo> toDelMenuList = new ArrayList<>();
                    // 过滤重复部分
                    if (assedMenuVos.size() != PlatformConstant.Character.ZERO_I.intValue()) {
                        for (SysMenuVo toAss : toAssMenuList) {
                            for (SysMenuVo ass : assedMenuVos) {
                                if (toAss.getMenuId().longValue() == ass.getMenuId().longValue()) {
                                    toDelMenuList.add(toAss);
                                    break;
                                }
                            }
                        }
                    }
                    toAssMenuList.removeAll(toDelMenuList);

                    sysUserDto.setUserId(user.getUserId());
                    sysUserDto.setToAssMenuList(toAssMenuList);
                    if (toAssMenuList.size() != PlatformConstant.Character.ZERO_I.intValue()) {
                        sysUserMapper.addSysUserMenuList(sysUserDto, operator);
                    }

                }

                if (toAssEnterList != null && toAssEnterList.size() != PlatformConstant.Character.ZERO_I.intValue()) {
                    // 复制已分配租户信息
                    // 已分配
                    List<SysUserEnterprise> assignedEnterpriseList = sysUserMapper.findAssnedEnterList(sysUserDto);
                    List<SysUserEnterprise> toDelEnterList = new ArrayList<>();
                    // 过滤重复部分
                    if (assignedEnterpriseList.size() != PlatformConstant.Character.ZERO_I.intValue()) {
                        for (SysUserEnterprise toAss : toAssEnterList) {
                            for (SysUserEnterprise ass : assignedEnterpriseList) {
                                if (toAss.getRootEnterpriseId().longValue() == ass.getRootEnterpriseId().longValue()) {
                                    toDelEnterList.add(toAss);
                                    break;
                                }
                            }
                        }
                    }
                    toAssEnterList.removeAll(toDelEnterList);

                    String del_ids = "";
                    String add_ids = "";
                    sysUserDto.setToAssEnterList(toAssEnterList);
                    if (toAssEnterList.size() != PlatformConstant.Character.ZERO_I.intValue()) {
                        sysUserMapper.assEnterprises(sysUserDto, operator);

                        for (SysUserEnterprise param : toAssEnterList) {
                            add_ids = add_ids + param.getRootEnterpriseId() + ",";
                        }
                        add_ids = add_ids.substring(0, add_ids.lastIndexOf(","));
                        sysUserDto.setToAssEnterList(toAssEnterList);

                        // 调用 Core 接口，同步分配的租户信息
                        sysUserDto.setUser_id(sysUserDto.getUserId());
                        sysUserDto.setAdd_ids(add_ids);
                        sysUserDto.setDel_ids(del_ids);
                        // http 请求
                        Map<String, Object> result = doCoreHttpRequest(sysUserDto, PlatformConstant.EnterpriseAnduserManage.ASSIGN_ENTERPRISE);
                        if (!result.get("status").toString().equals(PlatformConstant.ResResult.SUCCESS)) {
                            throw new ServiceException(ResultCode.UPDATE_USER_FAIL);
                        }

                        // 分配租户，调用基础资料接口
                        doDataBaseHttpReq(sysUserDto, operator, toAssEnterList, result);
                    }
                }

            }
        }

        return PlatformConstant.Character.ONE_I;
    }

    /**
     * 查询具体用户信息 - core
     *
     * @param: [sysUserDto]
     * @return: com.njwd.entity.platform.vo.SysUserVo
     * @author: zhuzs
     * @date: 2019-11-18
     */
    @Override
    public SysUserVo toFindUser(SysUserDto sysUserDto) {
        SysUserVo sysUserVo = new SysUserVo();
        sysUserVo.setAccount(sysUserDto.getAccount() == null ? "" : sysUserDto.getAccount());
        sysUserVo.setName(sysUserDto.getName() == null ? "" : sysUserDto.getName());
        sysUserVo.setMobile(sysUserDto.getMobile());
        sysUserVo.setUserId(sysUserDto.getUserId());

        // 获取已配置 菜单/权限 信息
        List<SysMenuVo> sysUserMenuVos = sysUserMapper.findAssedMenuTree(sysUserDto);
        List<SysMenuVo> rootMenuList = sysMenuService.rebuildData(sysUserMenuVos);

        // 获取已分配角色
        List<Long> roleIds = sysUserMapper.findAssedRoleIdList(sysUserDto);
        sysUserVo.setRoleIdList(roleIds);
        sysUserVo.setSysMenuList(rootMenuList);
        return sysUserVo;
    }

    /**
     * 查询用户列表
     *
     * @param: [sysUserDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.SysUserVo>
     * @author: zhuzs
     * @date: 2019-11-18
     */
    @Override
    public Page<SysUserVo> findUserList(SysUserDto sysUserDto) {
        Page<SysUserVo> page = sysUserDto.getPage();
        long current = page.getCurrent();
        long size = page.getSize();

        // http 请求
        Map<String, Object> result = doCoreHttpRequest(sysUserDto, PlatformConstant.EnterpriseAnduserManage.USER_ALL);
        if(! result.get("status").equals(PlatformConstant.Status.SUCCESS)){
            throw new ServiceException(ResultCode.FIND_ENTERPRISE_LIST_FAIL);
        }
        List<SysUserVo> sysUserVoList = JSONArray.parseArray(result.get("data").toString(), SysUserVo.class);

        long total = Long.valueOf(sysUserVoList.size());
        page.setTotal(total);

        // 按照手机号进行升序排列
        sortOperation(sysUserVoList);

        // 手动分页
        List<SysUserVo> userList = new ArrayList<>();
        if(total >= current*size){
            for(long i = (current-1)*size;i<current*size;i++){
                userList.add(sysUserVoList.get((int)i));
            }
        }else{
            for(long i = (current-1)*size;i<total;i++){
                userList.add(sysUserVoList.get((int)i));
            }
        }

        // 遍历用户 查询已分配租户
        SysUserDto param = new SysUserDto();
        for (SysUserVo userVo : userList) {
            param.setUser_id(userVo.getUser_id());
            List<SysUserEnterprise> sysUserEnterpriseList = sysUserMapper.findAssnedEnterList(param);
            String enterpriseNamesStr = sysUserEnterpriseList.stream().map(SysUserEnterprise::getRootEnterpriseName).collect(Collectors.joining(PlatformConstant.Character.COMMA));
            userVo.setEnterpriseNamesStr(enterpriseNamesStr);
        }

        page.setRecords(userList);
        return page;
    }

    /**
     * 查询 已分配的租户列表
     *
     * @param: [sysUserDto]
     * @return: java.util.List<com.njwd.entity.platform.SysUserEnterprise>
     * @author: zhuzs
     * @date: 2019-11-20
     */
    @Override
    public SysUserVo findAssedEnterList(SysUserDto sysUserDto) {
        SysUserVo sysUserVo = new SysUserVo();
        sysUserVo.setUser_id(sysUserDto.getUser_id());
        // 已分配租户信息
        List<SysUserEnterprise> assignedEnterpriseList = sysUserMapper.findAssnedEnterList(sysUserDto);
        sysUserVo.setAssignedEnterpriseList(assignedEnterpriseList);

        return sysUserVo;
    }

    /**
     * 获取用户信息 分页
     *
     * @param: [sysMenuDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.SysUserVo>
     * @author: zhuzs
     * @date: 2019-11-20
     */
    @Override
    public Page<SysUserVo> findUserPage(SysUserDto sysUserDto) {
        Page<SysUserVo> page = sysUserDto.getPage();
        // http 请求
        sysUserDto.setPageNo(page.getCurrent());
        sysUserDto.setPageSize(page.getSize());
        Map<String, Object> result = doCoreHttpRequest(sysUserDto, PlatformConstant.EnterpriseAnduserManage.USER_PAGE);
        Map<String, Object> data = (Map<String, Object>) result.get("data");

        List<SysUserVo> sysUserVoList = JSONArray.parseArray(data.get("listData").toString(), SysUserVo.class);
        Map<String, Object> pageMap = (Map<String, Object>) data.get("page");

        page.setRecords(sysUserVoList);
        page.setTotal(Integer.valueOf(pageMap.get("totalRecord").toString()));

        return page;
    }

    /**
     * 用户列表页 导出
     *
     * @param: [sysUserDto, response]
     * @return: void
     * @author: zhuzs
     * @date: 2019-11-26
     */
    @Override
    public void userListExport(SysUserDto sysUserDto, HttpServletResponse response) {
        Page<SysUserVo> page = sysUserDto.getPage();
        fileService.resetPage(page);

        fileService.exportExcel(response, findUserList(sysUserDto).getRecords(),
                new ExcelColumn("mobile", "手机号码"),
                new ExcelColumn("name", "员工姓名"),
                new ExcelColumn("account", "用户名"),
                new ExcelColumn("enterpriseNamesStr", "租户")
        );

    }

    /**
     * 排序操作
     *
     * @param: [sysUserVoList]
     * @return: void
     * @author: zhuzs
     * @date: 2019-12-02
     */
    @Override
    public void sortOperation(List<SysUserVo> sysUserVoList) {
        Collections.sort(sysUserVoList, new Comparator<SysUserVo>() {
            @Override
            public int compare(SysUserVo user1, SysUserVo user2) {

                String mobile1 = user1.getMobile().equals(PlatformConstant.Character.EMPT_STR)?"0":user1.getMobile();
                String mobile2 = user2.getMobile().equals(PlatformConstant.Character.EMPT_STR)?"0":user2.getMobile();
                if (Long.valueOf(mobile1) > Long.valueOf(mobile2)) {
                    return 1;
                }
                if (Long.valueOf(mobile1) == Long.valueOf(mobile2)) {
                    return 0;
                }
                return -1;
            }
        });
    }

    /**
     * Core http 请求
     *
     * @param: [param, childUrl]
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     * @author: zhuzs
     * @date: 2019-11-21
     */
    @Override
    public Map<String, Object> doCoreHttpRequest(BaseEntity param, String childUrl) {
        // 请求路径
        String url = ymlProperties.getNjwdCoreUrl() + childUrl;
        // 根据请求参数 生成 json_str
        param.setTimestamp(System.currentTimeMillis());
        param.setSign(UserUtil.generateSign());
        String json_str = JsonUtils.object2JsonIgNull(param);
        // Http请求 设置 url、参数

        return UserUtil.doPostRequest(url, json_str);
    }


    /**
     * 基础资料 http 请求
     *
     * @param: [sysUserDto, operator, toAssEnterList, result]
     * @return: void
     * @author: zhuzs
     * @date: 2019-11-25
     */
    private void doDataBaseHttpReq(SysUserDto sysUserDto, SysUserVo operator, List<SysUserEnterprise> toAssEnterList, Map<String, Object> result) {
        SysUserDto param = new SysUserDto();
        param.setUserId(sysUserDto.getUser_id());
        param.setName(sysUserDto.getName());
        param.setMobile(sysUserDto.getMobile());
        param.setCreatorId(operator.getUserId());
        param.setCreatorName(operator.getName());
        param.setToAssEnterList(toAssEnterList);
        // 待新增租户ID List
        List<Long> toAssEnterIdList = new ArrayList<>();
        List<RootEnterpriseVo> enterpriseVosInfo = JSONArray.parseArray(result.get("data").toString(), RootEnterpriseVo.class);
        if (enterpriseVosInfo != null && enterpriseVosInfo.size() != PlatformConstant.Character.ZERO_I) {
            Set<String> interfaceUrlSet = new LinkedHashSet<>();
            for (RootEnterpriseVo enterpriseVo : enterpriseVosInfo) {
                interfaceUrlSet.add(enterpriseVo.getInterface_url());
            }
            LinkedList<String> interfaceUrlList = new LinkedList(interfaceUrlSet);
            for (String interfaceUrl : interfaceUrlList) {
                // 每次循环置空
                toAssEnterIdList.clear();
                for (RootEnterpriseVo enterpriseVo : enterpriseVosInfo) {
                    if (interfaceUrl.equals(enterpriseVo.getInterface_url())) {
                        toAssEnterIdList.add(enterpriseVo.getRoot_enterprise_id());
                    }
                }

                String url = interfaceUrl + PlatformConstant.EnterpriseAnduserManage.BASEDATA_ADD_USER;
                param.setToAssEnterIdList(toAssEnterIdList);
                String json_str = JsonUtils.object2JsonIgNull(param);
                HttpUtils.restPostJsonStr(url, json_str);
            }
        }
    }

    /**
     * 用户菜单/按钮权限
     *
     * @param: [sysUserVo]
     * @return: void
     * @author: zhuzs
     * @date: 2019-11-26
     */
    private void fill(SysUserVo sysUserVo) {

        String sysMenuCodes = "";
        List<SysMenuVo> buttonList;
        // 获取用户有权限的菜单
        SysUserDto sysUserDto = new SysUserDto();
        FastUtils.copyProperties(sysUserVo, sysUserDto);
        // 查询非按钮菜单权限
        sysUserDto.setSelectType(PlatformConstant.SelectType.MENU);
        sysUserDto.setUser_id(sysUserDto.getUserId());
        List<SysMenuVo> menuList = sysUserMapper.findAssedMenuList(sysUserDto);
        //
        if (menuList.size() != PlatformConstant.Character.ZERO_I) {
            sysMenuCodes = menuList.stream().map(SysMenu::getCode).collect(Collectors.joining(PlatformConstant.Character.COMMA));
        }

        // 获取用户有权限的按钮
        Map<String, StringBuilder> sysButtonMap = new LinkedHashMap<>();
        sysUserDto.setSelectType(PlatformConstant.SelectType.BUTTON);
        buttonList = sysUserMapper.findAssedMenuList(sysUserDto);

        if (buttonList.size() != PlatformConstant.Character.ZERO_I) {
            for (SysMenuVo menu : menuList) {
                StringBuilder sysButtonCodes = sysButtonMap.computeIfAbsent(menu.getCode(), k -> new StringBuilder());
                for (SysMenuVo button : buttonList) {
                    if (button.getParentId().longValue() == menu.getMenuId().longValue()) {
                        if (sysButtonCodes.length() > 0) {
                            sysButtonCodes.append(Constant.Character.COMMA);
                        }
                        sysButtonCodes.append(button.getCode());
                    }
                }
            }
        }

        sysUserVo.setSysMenuCodes(sysMenuCodes);
        sysUserVo.setSysButtonMap(sysButtonMap);
    }

}
