package com.njwd.platform.api;

import com.njwd.entity.platform.vo.TaxSystemVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author lj
 * @Description 税制
 * @Date:10:55 2019/6/13
 **/
@RequestMapping("platform/taxSystem")
public interface TaxSystemApi {

    /**
     * @Description 查询会计准则用到的税制列表
     * @Author liuxiang
     * @Date:15:21 2019/7/2
     * @Param []
     * @return java.lang.String
     **/
    @PostMapping("findTaxSystemList")
    Result<List<TaxSystemVo>> findTaxSystemList();
}
