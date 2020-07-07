package com.njwd.platform.api;

import com.njwd.entity.platform.dto.TaxCategoryDto;
import com.njwd.entity.platform.vo.TaxCategoryVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/18 9:48
 */
@RequestMapping("platform/taxCategory")
public interface TaxCategoryApi {

    @RequestMapping("findTaxCategoryList")
    Result<List<TaxCategoryVo>> findTaxCategoryList(@RequestBody TaxCategoryDto param);

}
