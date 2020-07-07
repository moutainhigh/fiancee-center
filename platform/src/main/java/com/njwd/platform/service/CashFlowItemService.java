package com.njwd.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.CashFlowItemDto;
import com.njwd.entity.platform.vo.CashFlowItemVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author lj
 * @Description 现金流量项目
 * @Date:16:26 2019/6/13
 **/
public interface CashFlowItemService {

    /**
     * 添加现金流量项目
     * @Author lj
     * @Date:10:52 2019/11/14
     * @param cashFlowItemDto
     * @return int
     **/
    Long addCashFlowItem(CashFlowItemDto cashFlowItemDto);

    /**
     * 删除现金流量项目
     * @Author lj
     * @Date:16:55 2019/11/14
     * @param cashFlowItemDto
     * @return com.njwd.support.BatchResult
     **/
    BatchResult delCashFlowItemBatch(CashFlowItemDto cashFlowItemDto);

    /**
     * 修改现金流量项目
     * @Author lj
     * @Date:15:02 2019/11/14
     * @param cashFlowItemDto
     * @return int
     **/
    Long updateCashFlowItem(CashFlowItemDto cashFlowItemDto);

    /**
     * 版本号校验
     * @Author lj
     * @Date:16:32 2019/11/14
     * @param cashFlowItemDto
     * @return void
     **/
    void checkVersion(CashFlowItemDto cashFlowItemDto);

    /**
     * 审核现金流量项目
     * @Author lj
     * @Date:9:07 2019/11/15
     * @param cashFlowItemDto
     * @return com.njwd.support.BatchResult
     **/
    BatchResult approveCashFlowItemBatch(CashFlowItemDto cashFlowItemDto);

    /**
     * 反审核现金流量项目
     * @Author lj
     * @Date:9:08 2019/11/15
     * @param cashFlowItemDto
     * @return com.njwd.support.BatchResult
     **/
    BatchResult disApproveCashFlowItemBatch(CashFlowItemDto cashFlowItemDto);

    /**
     * 发布现金流量项目
     * @Author lj
     * @Date:9:09 2019/11/15
     * @param cashFlowItemDto
     * @return com.njwd.support.BatchResult
     **/
    BatchResult releaseCashFlowItemBatch(CashFlowItemDto cashFlowItemDto);

    /**
     * 根据ID查询现金流量项目
     * @Author lj
     * @Date:16:57 2019/11/13
     * @param cashFlowItemDto
     * @return com.njwd.entity.platform.vo.CashFlowVo
     **/
    CashFlowItemVo findCashFlowItemById(CashFlowItemDto cashFlowItemDto);

    /**
     * @Description 查询现金流量项目列表
     * @Author lj
     * @Date:10:29
     * @Param [cashFlowItemDto]
     * @return java.util.List<com.njwd.platform.entity.vo.CashFlowItemVo>
     **/
    List<CashFlowItemVo> findCashFlowItemList(CashFlowItemDto cashFlowItemDto);

    /**
     * @Description 查询现金流量项目分页
     * @Author lj
     * @Date:10:29
     * @Param [cashFlowItemDto]
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.platform.entity.vo.CashFlowItemVo>
     **/
    Page<CashFlowItemVo> findCashFlowItemPage(CashFlowItemDto cashFlowItemDto);

    /**
     * 查询现金流量项目列表-初始化基础资料
     * @Author lj
     * @Date:17:29 2019/12/2
     * @param cashFlowItemDto
     * @return java.util.List<com.njwd.entity.platform.vo.CashFlowItemVo>
     **/
    List<CashFlowItemVo> findCashFlowItemNew(CashFlowItemDto cashFlowItemDto);

    /**
     * 查询现金流量项目分页
     * @Author lj
     * @Date:17:45 2019/11/13
     * @param cashFlowItemDto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.CashFlowItemVo>
     **/
    Page<CashFlowItemVo> findCashFlowItemPageNew(CashFlowItemDto cashFlowItemDto);

    /**
     * 导出
     * @param cashFlowItemDto
     * @param response
     */
    void exportExcel(CashFlowItemDto cashFlowItemDto, HttpServletResponse response);
}
