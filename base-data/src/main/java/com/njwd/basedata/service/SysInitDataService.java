package com.njwd.basedata.service;

import com.njwd.entity.basedata.vo.SysInitDataVo;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/6 14:10
 */
public interface SysInitDataService {

    SysInitDataVo findSysInitData();

    SysInitDataVo initData();
}
