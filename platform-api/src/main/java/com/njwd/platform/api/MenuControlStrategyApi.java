package com.njwd.platform.api;

import com.njwd.entity.basedata.MenuControlStrategy;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/16 9:17
 */
@RequestMapping("platform/menuControlStrategy")
public interface MenuControlStrategyApi {

    @RequestMapping("findMenuControlStrategyList")
    Result<List<MenuControlStrategy>> findMenuControlStrategyList();

}
