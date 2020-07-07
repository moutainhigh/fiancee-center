package com.njwd.basedata.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.dto.AccountBookEntityDto;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.support.Result;
import com.njwd.utils.UserUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Description 账簿API
 * @Author 朱小明
 * @Date 2019/8/1 9:02
 **/
@RequestMapping("financeback/accountBookEntity")
public interface AccountBookEntityApi {

    /**
     * 查询 用户有操作权限的核算主体列表 分页（不传参数返回空）
     * @param accountBookEntityDto
     * @return
     */
    @RequestMapping("findAuthOperationalEntityPage")
    Result<Page<AccountBookEntityVo>> findAuthOperationalEntityPage(AccountBookEntityDto accountBookEntityDto);

    /**
     * 查询 用户有操作权限的核算主体列表 (不传参数返回空)
     * @param accountBookEntityDto
     * @return
     */
    @RequestMapping("findAuthOperationalEntityList")
    Result<Page<AccountBookEntityVo>>  findAuthOperationalEntityList(AccountBookEntityDto accountBookEntityDto);

    /**
     * 查询核算主体列表
     *
     * @param accountBookEntityDto
     * @return
     */
    @RequestMapping("findAccountBookEntityListByAccBookIdList")
    Result<List<AccountBookEntityVo>> findAccountBookEntityListByAccBookIdList(AccountBookEntityDto accountBookEntityDto);

    /**
     * @description: 
     * @param: [accountBookEntityDto]
     * @return: com.njwd.support.Result<com.njwd.entity.basedata.vo.AccountBookEntityVo> 
     * @author: xdy        
     * @create: 2019-10-17 09:13 
     */
    @RequestMapping("findAccountBookEntityById")
    Result<AccountBookEntityVo> findAccountBookEntityById(@RequestBody AccountBookEntityDto accountBookEntityDto);

}