package com.njwd.platform.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.platform.dto.RootEnterpriseDto;
import com.njwd.entity.platform.dto.SysSystemDto;
import com.njwd.entity.platform.vo.RootEnterpriseVo;
import com.njwd.entity.platform.vo.SysSystemVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.platform.config.YmlProperties;
import com.njwd.platform.service.EnterpriseService;
import com.njwd.platform.service.SysSystemService;
import com.njwd.platform.utils.UserUtil;
import com.njwd.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 租户
 *
 * @author zhuzs
 * @date 2019-11-15 10:19
 */
@Service
public class EnterpriseServiceImpl implements EnterpriseService {
    @Autowired
    private SysSystemService sysSystemService;
    @Autowired
    private YmlProperties ymlProperties;

    /**
     * 获取租户列表
     *
     * @param: [sysUserDto] enterpriseName 模糊搜索
     * @return: java.util.List<com.njwd.entity.platform.vo.SysUserVo>
     * @author: zhuzs
     * @date: 2019-11-13
     */
    @Override
    public Page<RootEnterpriseVo> findEnterprisePage(RootEnterpriseDto rootEnterpriseDto) {
        Page<RootEnterpriseVo> rootEnterpriseVoPage = rootEnterpriseDto.getPage();
        // 请求路径
        String url = ymlProperties.getNjwdCoreUrl()+ PlatformConstant.EnterpriseAnduserManage.FIND_ENTERPRISE_LIST_FOR_PAGE;
        rootEnterpriseDto.setTimestamp(System.currentTimeMillis());
        rootEnterpriseDto.setSign(UserUtil.generateSign());
        rootEnterpriseDto.setPageNo(rootEnterpriseVoPage.getCurrent());
        rootEnterpriseDto.setPageSize(rootEnterpriseVoPage.getSize());
        // Http请求 设置 url、参数
        String json_str = JsonUtils.object2JsonIgNull(rootEnterpriseDto);
        JSONObject result = UserUtil.doPostRequest(url,json_str);
        if(!result.get("status").equals(PlatformConstant.Status.SUCCESS)){
            throw new ServiceException(ResultCode.FIND_ENTERPRISE_LIST_FAIL);
        }
        // 处理返回结果
        Map<String,Object> resultMap = (Map<String,Object>)result.get("data");
        List<RootEnterpriseVo> rootEnterpriseVos = (List<RootEnterpriseVo>)resultMap.get("listData");
        Map<String,Object> pageMap = (Map<String,Object>)resultMap.get("page");

        rootEnterpriseVoPage.setTotal(Integer.valueOf(pageMap.get("totalRecord").toString()));
        rootEnterpriseVoPage.setRecords(rootEnterpriseVos);


        return rootEnterpriseVoPage;
    }

    /**
     * 获取 已购买子系统列表
     *
     * @param: [rootEnterpriseDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.SysSystemVo>
     * @author: zhuzs
     * @date: 2019-11-15
     */
    @Override
    public List<SysSystemVo> findEnableSystemList(RootEnterpriseDto rootEnterpriseDto) {
        SysSystemDto sysSystemDto = new SysSystemDto();
        sysSystemDto.setRootEnterpriseId(rootEnterpriseDto.getId());
        return sysSystemService.findSysSystemList(sysSystemDto);
    }

}

