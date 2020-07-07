package com.njwd.platform.api;

import com.njwd.entity.platform.dto.UnitDto;
import com.njwd.entity.platform.vo.UnitVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/15 16:22
 */
@RequestMapping("platform/unit")
public interface UnitApi {

    @RequestMapping("findUnitList")
    Result<List<UnitVo>> findUnitList(@RequestBody UnitDto unitDto);

}
