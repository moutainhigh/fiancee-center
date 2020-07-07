package com.njwd.platform.utils;


import com.alibaba.fastjson.JSONObject;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.base.SysLogCommon;
import com.njwd.entity.platform.vo.SysUserVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.utils.CheckUrlSignUtil;
import com.njwd.utils.HttpUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @param:
 * @return:
 * @author: zhuzs
 * @date: 2019-11-12
 */
@Component
public class UserUtil {
    /**
     * 获取UserInfo
     */
    public static SysUserVo getUserVo() {
        // 从shiro中获取
        PrincipalCollection principals = SecurityUtils.getSubject().getPrincipals();
        if (principals != null) {
            Object userVo = principals.getPrimaryPrincipal();
            return (SysUserVo) userVo;
        } else {
            throw new ServiceException(ResultCode.SYS_USER_INVALID);
        }
    }

    /**
     * 密码加密 并 返回 sign
     *
     * @param: [userLogin]  root_enterprise_id 给定值
     * @return: java.lang.String
     * @author: zhuzs
     * @date: 2019-11-14
     */
    public static String generateSign() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("system_code", PlatformConstant.EnterpriseAnduserManage.SYSTEM_CODE);
        params.put("root_enterprise_id", PlatformConstant.EnterpriseAnduserManage.ROOT_ENTERPRISE_ID);
        params.put("timestamp", System.currentTimeMillis());
        return CheckUrlSignUtil.getSign(params, PlatformConstant.EnterpriseAnduserManage.SYSTEM_CODE);

    }


    /**
     * Http post 请求
     *
     * @param: [json_str, userLogin]
     * @return: void
     * @author: zhuzs
     * @date: 2019-11-14
     */
    public static JSONObject doPostRequest(String url, String json_str) {
        Map<String, String> map = new HashMap<>();
        map.put("json_str", json_str);
        return JSONObject.parseObject(HttpUtils.restPost(url, null, map, String.class));
    }

    /**
     * @param ip          ip地址
     * @param sysName     子系统名称：财务后台 、总账
     * @param menuName    菜单名：公司、业务单元等
     * @param operation   操作名：新增、修改、删除，其他
     * @param operateType 操作类型：add、update、delete、other
     * @return
     */
    public static SysLogCommon getUserLogInfo(String ip, String sysName, String menuName, String operation, String operateType) {
        SysLogCommon sysLogCommon = new SysLogCommon(ip, sysName, menuName, operation, operateType);
        sysLogCommon.setCreateTime(new Date());
        sysLogCommon.setStatus((byte) 0);
        //获取用户信息
        // 从shiro中获取
        PrincipalCollection principals = SecurityUtils.getSubject().getPrincipals();
        if (principals != null) {
            Object userObj = principals.getPrimaryPrincipal();
            SysUserVo userVo = (SysUserVo) userObj;
            sysLogCommon.setCreatorId(userVo.getUserId());
            sysLogCommon.setCreatorName(userVo.getName());
            sysLogCommon.setMobile(userVo.getMobile());
            sysLogCommon.setCreatorAccount(userVo.getAccount());
        }
        return sysLogCommon;
    }

    /**
     * 该方法增加影响记录Id：recordId
     *
     * @param ip          ip地址
     * @param sysName     子系统名称：财务后台 、总账
     * @param menuName    菜单名：公司、业务单元等
     * @param operation   操作名：新增、修改、删除，其他
     * @param operateType 操作类型：add、update、delete、other
     * @param recordIds recordId所影响记录的id
     * @return
     */
    public static SysLogCommon getUserLogInfo2(String ip, String sysName, String menuName, String operation, String operateType, String recordIds) {
        SysLogCommon sysLogCommon = new SysLogCommon(ip, sysName, menuName, operation, operateType);
        sysLogCommon.setCreateTime(new Date());
        sysLogCommon.setStatus((byte) 0);
        //sysLogCommon.setRecordId(recordIds);
        //获取用户信息
        // 从shiro中获取
        PrincipalCollection principals = SecurityUtils.getSubject().getPrincipals();
        if (principals != null) {
            Object userObj = principals.getPrimaryPrincipal();
            SysUserVo userVo = (SysUserVo) userObj;
            sysLogCommon.setCreatorId(userVo.getUserId());
            sysLogCommon.setCreatorName(userVo.getName());
            sysLogCommon.setMobile(userVo.getMobile());
            sysLogCommon.setCreatorAccount(userVo.getAccount());
        }
        return sysLogCommon;
    }

}