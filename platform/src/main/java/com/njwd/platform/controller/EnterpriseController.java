package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.RootEnterpriseDto;
import com.njwd.entity.platform.vo.RootEnterpriseVo;
import com.njwd.entity.platform.vo.SysSystemVo;
import com.njwd.platform.service.EnterpriseService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 租户
 *
 * @author zhuzs
 * @date 2019-11-15 10:16
 */
@RestController
@RequestMapping("enterprise")
public class EnterpriseController extends BaseController {

    @Autowired
    private EnterpriseService enterpriseService;

    /**
     * 查询开通财务系统的租户列表 分页
     *
     * @param: [rootEnterpriseDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.platform.vo.SysSystemVo>>
     * @author: zhuzs
     * @date: 2019-11-14
     */
    @PostMapping("findEnterprisePage")
    public Result<Page<RootEnterpriseVo>> findEnterprisePage(@RequestBody RootEnterpriseDto rootEnterpriseDto){
        return ok(enterpriseService.findEnterprisePage(rootEnterpriseDto));
    }

    /**
     * 获取 已购买子系统列表
     *
     * @param: [rootEnterpriseDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.platform.vo.SysSystemVo>>
     * @author: zhuzs
     * @date: 2019-11-15
     */
    @PostMapping("findEnableSystemList")
    public Result<List<SysSystemVo>> findEnableSystemList(@RequestBody RootEnterpriseDto rootEnterpriseDto){
        return ok(enterpriseService.findEnableSystemList(rootEnterpriseDto));
    }
}

