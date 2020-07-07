package com.njwd.financeback.service;

import com.njwd.entity.basedata.vo.SysMenuOptionComplexVo;
import com.njwd.entity.platform.SysMenuOption;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/19 9:28
 */
public interface SysMenuOptionService {

    /**
     * 获取选项信息
     * @param sysMenuOption
     * @return
     */
    SysMenuOptionComplexVo findMenuOption(SysMenuOption sysMenuOption);

    /**
     * 修改选项
     * @param sysMenuOptionList
     * @return
     */
    int updateMenuOption(List<SysMenuOption> sysMenuOptionList);
}
