package com.njwd.platform.api;

import com.njwd.entity.platform.dto.CostItemDto;
import com.njwd.entity.platform.vo.CostItemVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/19 17:53
 */
@RequestMapping("platform/costItem")
public interface CostItemApi {

    @RequestMapping("findCostItemList")
    Result<List<CostItemVo>> findCostItemList(@RequestBody CostItemDto costItemDto);

}
