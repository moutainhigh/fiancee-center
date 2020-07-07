package com.njwd.basedata.api;


import com.njwd.entity.platform.SysMenuOption;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * <p>
 * 菜单选项设置 前端控制器
 * </p>
 *
 * @author 朱小明
 * @since 2019-06-19
 */
@RequestMapping("sysMenuOption")
public interface SysMenuOptionApi {

    /**
     * 选项信息
     * @param sysMenuOption
     * @return
     */
    @RequestMapping("findMenuOption")
    Result findMenuOption(SysMenuOption sysMenuOption);

    /**
     * 修改选项
     * @param sysMenuOptionList
     * @return
     */
    @RequestMapping("updateMenuOption")
    Result updateMenuOption(List<SysMenuOption> sysMenuOptionList);


}
