package com.njwd.financeback.controller;


import com.njwd.entity.platform.SysMenuOption;
import com.njwd.financeback.service.SysMenuOptionService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 菜单选项设置 前端控制器
 * </p>
 *
 * @author xdy
 * @since 2019-06-19
 */
@RestController
@RequestMapping("sysMenuOption")
public class SysMenuOptionController extends BaseController {

    @Resource
    private SysMenuOptionService sysMenuOptionService;

    /**
     * 选项信息
     * @param sysMenuOption
     * @return
     */
    @RequestMapping("findMenuOption")
    public Result findMenuOption(@RequestBody SysMenuOption sysMenuOption){
         return ok(sysMenuOptionService.findMenuOption(sysMenuOption));
    }

    /**
     * 修改选项
     * @param sysMenuOptionList
     * @return
     */
    @RequestMapping("updateMenuOption")
    public Result updateMenuOption(@RequestBody List<SysMenuOption> sysMenuOptionList){
        return confirm(sysMenuOptionService.updateMenuOption(sysMenuOptionList));
    }


}
