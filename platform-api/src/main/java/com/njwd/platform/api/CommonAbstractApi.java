package com.njwd.platform.api;

import com.njwd.entity.basedata.vo.CommonAbstractVo;
import com.njwd.entity.platform.dto.CommonAbstractDto;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/14 9:57
 */
@RequestMapping("platform/commonAbstract")
public interface CommonAbstractApi {

    @RequestMapping("findCommonAbstractList")
    Result<List<CommonAbstractVo>> findCommonAbstractList(@RequestBody CommonAbstractDto commonAbstractDto);

}
