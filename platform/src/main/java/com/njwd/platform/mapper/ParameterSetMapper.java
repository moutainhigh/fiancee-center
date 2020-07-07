package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.ledger.vo.ParameterSetVo;

import java.util.List;


/**
 * @description:
 * @author: xdy
 * @create: 2019/8/14 10:59
 */
public interface ParameterSetMapper extends BaseMapper {

    List<ParameterSetVo> findParameterSet();

}
