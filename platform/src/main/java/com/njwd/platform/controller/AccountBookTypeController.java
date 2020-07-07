package com.njwd.platform.controller;

import com.njwd.entity.platform.vo.AccountBookTypeVo;
import com.njwd.platform.service.AccountBookTypeService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author lj
 * @Description 账簿类型
 * @Date:17:15 2019/6/25
 **/
@RestController
@RequestMapping("accountBookType")
public class AccountBookTypeController extends BaseController {
    @Autowired
    private AccountBookTypeService accountBookTypeService;

    /**
     * @Description 查询账簿类型列表
     * @Author liuxiang
     * @Date:15:03 2019/7/2
     * @Param []
     * @return java.lang.String
     **/
    @PostMapping("findAccountBookTypeList")
    public Result<List<AccountBookTypeVo>> findAccountBookTypeList(){
        return ok(accountBookTypeService.findAccountBookTypeList());
    }
}
