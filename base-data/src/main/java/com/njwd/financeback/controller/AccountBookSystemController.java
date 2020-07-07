package com.njwd.financeback.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.AccountBookSystem;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.AccountBookSystemDto;
import com.njwd.entity.basedata.vo.AccountBookSystemVo;
import com.njwd.entity.basedata.vo.AccountBookVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.vo.SysSystemVo;
import com.njwd.financeback.service.AccountBookSystemService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.ShiroUtils;
import com.njwd.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 账簿启用子系统记录
 *
 * @Author: Zhuzs
 * @Date: 2019-06-26 16:36
 */
@RestController
@RequestMapping("accountBookSystem")
public class AccountBookSystemController extends BaseController {
    @Autowired
    private AccountBookSystemService accountBookSystemService;


    /**
     * 查询 子系统及其状态 列表
     *
     * @param: [accountBookSystemDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.platform.vo.SysSystemVo>>
     * @author: zhuzs
     * @date: 2019-09-16 17:30
     */
    @RequestMapping("findList")
    public Result<List<SysSystemVo>> findList(@RequestBody AccountBookSystemDto accountBookSystemDto){
        SysUserVo operator = UserUtils.getUserVo();
        accountBookSystemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        return ok(accountBookSystemService.findList(accountBookSystemDto));
    }

    /**
     * 根据 账簿ID/子系统标识 查询已启用子系统
     *
     * @param: [accountBookSystemDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.basedata.vo.AccountBookSystemVo>>
     * @author: zhuzs
     * @date: 2019-09-16 17:30
     */
    @RequestMapping("findEnableList")
    public Result<List<AccountBookSystemVo>> findEnableList(@RequestBody AccountBookSystemDto accountBookSystemDto){
        SysUserVo operator = UserUtils.getUserVo();
        accountBookSystemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        return ok(accountBookSystemService.findEnableList(accountBookSystemDto));
    }

    /**
     * 根据子系统标识 查询当前企业已启用子系统列表
     *
     * @param: [accountBookSystemDto]
     * @return: com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountBookSystemVo>>
     * @author: zhuzs
     * @date: 2019-09-16 17:30
     */
    @RequestMapping("findLedgerList")
    public Result<Page<AccountBookSystemVo>> findLedgerList(@RequestBody AccountBookSystemDto accountBookSystemDto){
        SysUserVo operator = UserUtils.getUserVo();
        accountBookSystemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        return ok(accountBookSystemService.findLedgerList(accountBookSystemDto));
    }
    //根据子系统标识 查询当前企业用户已启用子系统列表
    @RequestMapping("findLedgerListByUserId")
    public Result<Page<AccountBookSystemVo>> findLedgerListByUserId(@RequestBody AccountBookSystemDto accountBookSystemDto){
        SysUserVo operator = UserUtils.getUserVo();
        accountBookSystemDto.setRootEnterpriseId(operator.getRootEnterpriseId());
        accountBookSystemDto.setUserId(operator.getUserId());
        return ok(accountBookSystemService.findLedgerListByUserId(accountBookSystemDto));
    }

    /**
     * 初始化子系统
     *
     * @param: [accountBookSystemDto]
     * @return: com.njwd.support.Result<java.lang.Integer>
     * @author: zhuzs
     * @date: 2019-09-16 17:30
     */
    @RequestMapping("initSystemById")
    public Result<Integer> initSystemById(@RequestBody AccountBookSystemDto accountBookSystemDto){
        return ok(accountBookSystemService.initSystemById(accountBookSystemDto));
    }

    /**
     * 反初始化子系统
     *
     * @param: [accountBookSystemDto]
     * @return: com.njwd.support.Result<java.lang.Integer>
     * @author: zhuzs
     * @date: 2019-09-16 17:30
     */
    @RequestMapping("antiInitSystemById")
    public Result<Integer> antiInitSystemById(@RequestBody AccountBookSystemDto accountBookSystemDto){
        return ok(accountBookSystemService.antiInitSystemById(accountBookSystemDto));
    }
    
    /**
     * @description: 获取账簿期间数据
     * @param: [accountBookSystemDto]
     * @return: com.njwd.support.Result<com.njwd.entity.basedata.AccountBookSystem> 
     * @author: xdy        
     * @create: 2019-08-14 16-42 
     */
    @RequestMapping("findAccountBookSystem")
    public Result<AccountBookSystem> findAccountBookSystem(@RequestBody AccountBookSystemDto accountBookSystemDto){
        return ok(accountBookSystemService.findAccountBookSystem(accountBookSystemDto));
    }

	/**
     * @Description 根据系统标识+账簿ID+会计年度+会计期间查询是否已初始化
     * @Author 朱小明
     * @Date 2019/8/14 16:16
     * @Param [accountBookSystemDto]
     * @return com.njwd.support.Result<java.lang.Integer>
     **/
    @RequestMapping("findInitStatusByCondition")
    public Result<AccountBookSystem> findInitStatusByCondition(@RequestBody AccountBookSystemDto accountBookSystemDto){
        FastUtils.checkParams(accountBookSystemDto.getSystemSign(),accountBookSystemDto.getAccountBookId());
        return ok(accountBookSystemService.findInitStatusByCondition(accountBookSystemDto));
    }

}
