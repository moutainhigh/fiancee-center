package com.njwd.platform.service;

import com.njwd.entity.ledger.vo.ParameterSetVo;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/14 10:59
 */
public interface ParameterSetService {

    List<ParameterSetVo> findParameterSet();

}
