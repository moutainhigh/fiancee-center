package com.njwd.platform.api;

import com.njwd.entity.platform.dto.CashFlowDto;
import com.njwd.entity.platform.vo.CashFlowVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author lj
 * @Description 现金流量项目表(更多模板下拉框)
 * @Date:16:08 2019/6/13
 **/
@RequestMapping("platform/cashFlow")
public interface CashFlowApi {
    /**
     * @Description 查询现金流量项目表列表
     * @Param [CashFlowDto cashFlowDto]
     * @return java.lang.String
     **/
    @PostMapping("findCashFlowList")
    Result<List<CashFlowVo>> findCashFlowList(CashFlowDto cashFlowDto);

    /**
     * @Description 根据会计准则id、账簿类型id查询现金流量项目表列表
     * @Author lj
     * @Date:15:14 2019/6/25
     * @Param [cashFlowDto]
     * @return java.lang.String
     **/
    @PostMapping("findCashFlowListByStandIdAndTypeId")
    Result<List<CashFlowVo>> findCashFlowListByStandIdAndTypeId(CashFlowDto cashFlowDto);
}
