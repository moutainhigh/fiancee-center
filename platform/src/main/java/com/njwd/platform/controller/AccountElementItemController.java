package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.AccountBookCategory;
import com.njwd.entity.platform.AccountElementItem;
import com.njwd.entity.platform.dto.AccountBookCategoryDto;
import com.njwd.entity.platform.dto.AccountElementItemDto;
import com.njwd.entity.platform.vo.AccountElementItemVo;
import com.njwd.platform.service.AccountElementItemService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author lj
 * @Description 会计要素项
 * @Date:17:15 2019/6/25
 **/
@RestController
@RequestMapping("accountElementItem")
public class AccountElementItemController extends BaseController {
    @Resource
    private AccountElementItemService accountElementItemService;

    /**
     * 根据EleId查询会计要素项列表
     * @Author lj
     * @Date:11:28 2019/8/22
     * @param accountElementItemDto
     * @return java.util.List<com.njwd.entity.platform.vo.AccountElementItemVo>
     **/
    @PostMapping("findListByEleId")
    public Result<List<AccountElementItemVo>> findListByEleId(@RequestBody AccountElementItemDto accountElementItemDto){
        return ok(accountElementItemService.findListByEleId(accountElementItemDto));
    }

    /**
     * 查详情
     * @param accountElementItemDto
     * @return
     */
    @RequestMapping("selectById")
    public Result<AccountElementItem> selectById(@RequestBody AccountElementItemDto accountElementItemDto){
        AccountElementItem accountElementItem = accountElementItemService.selectById(accountElementItemDto);
        return  ok(accountElementItem);
    }
    /** 刘遵通
     * 查询页面 （分页）
     * @param accountElementItemDto
     * @return
     */
    @RequestMapping("findPage")
    public Result<Page<AccountElementItemVo>> findPage(@RequestBody AccountElementItemDto accountElementItemDto){
        Page<AccountElementItemVo> accountElementItemList = accountElementItemService.findPage(accountElementItemDto);
        return ok(accountElementItemList);
    }

    /**刘遵通
     * 会计要素表 分页查询
     * @param accountElementItemDto
     * @return
     */
    @RequestMapping("findAccountElementPage")
    public Result<Page<AccountElementItemVo>> findAccountElementPage(@RequestBody AccountElementItemDto accountElementItemDto){
        Page<AccountElementItemVo> accountElementPage = accountElementItemService.findAccountElementPage(accountElementItemDto);
        return ok(accountElementPage);
    }
}
