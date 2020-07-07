package com.njwd.platform.api;

import com.njwd.entity.ledger.vo.ParameterSetVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/14 11:18
 */
@RequestMapping("platform/parameterSet")
public interface ParameterSetApi {

    @RequestMapping("findParameterSet")
    Result<List<ParameterSetVo>> findParameterSet();

}
