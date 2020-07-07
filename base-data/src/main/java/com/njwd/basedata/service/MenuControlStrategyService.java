package com.njwd.basedata.service;

import com.njwd.entity.basedata.MenuControlStrategy;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/28 9:22
 */
public interface MenuControlStrategyService {
    
    /**
     * @description: 获取租户控制策略
     * @param: [menuCode]
     * @return: com.njwd.entity.basedata.MenuControlStrategy 
     * @author: xdy        
     * @create: 2019-08-28 09-32 
     */
    MenuControlStrategy findMenuControlStrategy(String menuCode);

}
