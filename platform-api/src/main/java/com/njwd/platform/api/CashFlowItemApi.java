package com.njwd.platform.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.CashFlowItemDto;
import com.njwd.entity.platform.vo.CashFlowItemVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 现金流量项目
 * @Date:17:10 2019/7/12
 **/
@RequestMapping("platform/cashFlowItem")
public interface CashFlowItemApi {
    /**
     * @Description 查询现金流量项目列表
     * @Param []
     * @return java.lang.String
     **/
    @PostMapping("findCashFlowItemList")
    Result<List<CashFlowItemVo>> findCashFlowItemList(CashFlowItemDto cashFlowItemDto);

    @PostMapping("findCashFlowItemListNew")
    Result<List<CashFlowItemVo>> findCashFlowItemListNew(CashFlowItemDto cashFlowItemDto);

    /**
     * @Author lj
     * @Description 查询现金流量项目分页
     * @Date:10:00 2019/6/19
     **/
    @PostMapping("findCashFlowItemPage")
    Result<Page<CashFlowItemVo>> findCashFlowItemPage(CashFlowItemDto cashFlowItemDto);
}
