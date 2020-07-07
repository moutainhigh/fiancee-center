package com.njwd.platform.service.impl;

import com.njwd.entity.ledger.vo.ParameterSetVo;
import com.njwd.platform.mapper.ParameterSetMapper;
import com.njwd.platform.service.ParameterSetService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/14 11:00
 */
@Service
public class ParameterSetServiceImpl implements ParameterSetService {

    @Resource
    private ParameterSetMapper parameterSetMapper;

    @Override
    public List<ParameterSetVo> findParameterSet() {
        return parameterSetMapper.findParameterSet();
    }
}
