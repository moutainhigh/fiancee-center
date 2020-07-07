package com.njwd.financeback.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.AccountBookEntityDto;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.financeback.service.AccountBookEntityService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: Zhuzs
 * @Date: 2019-06-24 11:49
 */
@RestController
@RequestMapping("accountBookEntity")
public class AccountBookEntityController extends BaseController {
    @Autowired
    private AccountBookEntityService accountBookEntityService;

    /**
     * 根据 账簿ID/公司ID 查询核算主体列表
     *
     * @param: [accountBookDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.basedata.vo.AccountBookEntityVo>>
     * @author: zhuzs
     * @date: 2019-09-16 17:29
     */
    @RequestMapping("findAccountBookEntityList")
    public Result<List<AccountBookEntityVo>> findAccountBookEntityList(@RequestBody AccountBookDto accountBookDto){
        SysUserVo operator = UserUtils.getUserVo();
        accountBookDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        return ok(accountBookEntityService.findAccountBookEntityList(accountBookDto));
    }

    /**
     * 根据 账簿ID list 查询核算主体列表 分页
     *
     * @param: [accountBookEntityDto]
     * @return: com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountBookEntityVo>>
     * @author: zhuzs
     * @date: 2019-09-16 17:29
     */
    @RequestMapping("findAccountBookEntityPageByAccBookIdList")
    public Result<Page<AccountBookEntityVo>> findAccountBookEntityPageByAccBookIdList(@RequestBody AccountBookEntityDto accountBookEntityDto){
        SysUserVo operator = UserUtils.getUserVo();
        accountBookEntityDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        return ok(accountBookEntityService.findAccountBookEntityPageByAccBookIdList(accountBookEntityDto));
    }

    /**
     * 根据 账簿ID list 查询核算主体列表
     *
     * @param: [accountBookEntityDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.basedata.vo.AccountBookEntityVo>>
     * @author: zhuzs
     * @date: 2019-09-16 17:30
     */
    @RequestMapping("findAccountBookEntityListByAccBookIdList")
    public Result<List<AccountBookEntityVo>> findAccountBookEntityListByAccBookIdList(@RequestBody AccountBookEntityDto accountBookEntityDto){
        SysUserVo operator = UserUtils.getUserVo();
        accountBookEntityDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        if(accountBookEntityDto.getMenuCode() != null){
            accountBookEntityDto.setUserId(operator.getUserId());
        }
        return ok(accountBookEntityService.findAccountBookEntityListByAccBookIdList(accountBookEntityDto));
    }

    /**
     * 查询 用户有操作权限的核算主体列表 分页（不传参数返回空）
     *
     * @param: [accountBookEntityDto]
     * @return: com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountBookEntityVo>>
     * @author: zhuzs
     * @date: 2019-09-16 17:30
     */
    @RequestMapping("findAuthOperationalEntityPage")
    public Result<Page<AccountBookEntityVo>> findAuthOperationalEntityPage(@RequestBody AccountBookEntityDto accountBookEntityDto){
        SysUserVo operator = UserUtils.getUserVo();
        accountBookEntityDto.setUserId(operator.getUserId());
        accountBookEntityDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        return ok(accountBookEntityService.findAuthOperationalEntityPage(accountBookEntityDto));
    }

    /**
     * 查询 用户有操作权限的核算主体列表 (不传参数返回空)
     *
     * @param: [accountBookEntityDto]
     * @return: com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountBookEntityVo>>
     * @author: zhuzs
     * @date: 2019-09-16 17:30
     */
    @RequestMapping("findAuthOperationalEntityList")
    public Result<Page<AccountBookEntityVo>> findAuthOperationalEntityList(@RequestBody AccountBookEntityDto accountBookEntityDto){
        SysUserVo operator = UserUtils.getUserVo();
        accountBookEntityDto.setUserId(operator.getUserId());
        accountBookEntityDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        return ok(accountBookEntityService.findAuthOperationalEntityList(accountBookEntityDto));
    }
    
    /**
     * @description: 
     * @param: [accountBookEntityDto]
     * @return: com.njwd.support.Result<com.njwd.entity.basedata.vo.AccountBookEntityVo> 
     * @author: xdy        
     * @create: 2019-10-17 09:15
     */
    @RequestMapping("findAccountBookEntityById")
    public Result<AccountBookEntityVo> findAccountBookEntityById(@RequestBody AccountBookEntityDto accountBookEntityDto){
        return ok(accountBookEntityService.findAccountBookEntityById(accountBookEntityDto));
    }

}
