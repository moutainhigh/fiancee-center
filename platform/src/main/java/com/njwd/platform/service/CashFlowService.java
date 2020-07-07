package com.njwd.platform.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.CashFlowDto;
import com.njwd.entity.platform.vo.CashFlowVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author lj
 * @Description 现金流量项目表
 * @Date:16:04 2019/6/13
 **/
public interface CashFlowService {

    /**
     * 添加现金流量项目表
     * @Author lj
     * @Date:11:55 2019/11/12
     * @param cashFlowDto
     * @return int
     **/
    Long addCashFlow(CashFlowDto cashFlowDto);

    /**
     * 修改现金流量项目表
     * @Author lj
     * @Date:11:55 2019/11/12
     * @param cashFlowDto
     * @return int
     **/
    Long updateCashFlow(CashFlowDto cashFlowDto);

    /**
     * 删除现金流量项目表
     * @Author lj
     * @Date:17:25 2019/11/11
     * @param cashFlowDto
     * @return com.njwd.support.BatchResult
     **/
    BatchResult delCashFlowBatch(CashFlowDto cashFlowDto);

    /**
     * 审核现金流量项目表
     * @Author lj
     * @Date:13:45 2019/11/13
     * @param cashFlowDto
     * @return com.njwd.support.BatchResult
     **/
    BatchResult approveCashFlowBatch(CashFlowDto cashFlowDto);

    /**
     * 反审核现金流量项目表
     * @Author lj
     * @Date:14:06 2019/11/13
     * @param cashFlowDto
     * @return com.njwd.support.BatchResult
     **/
    BatchResult disApproveCashFlowBatch(CashFlowDto cashFlowDto);

    /**
     * 发布现金流量项目表
     * @Author lj
     * @Date:14:40 2019/11/13
     * @param cashFlowDto
     * @return com.njwd.support.BatchResult
     **/
    BatchResult releaseCashFlowBatch(CashFlowDto cashFlowDto);

    /**
     * @Description 查询现金流量项目表列表
     * @Author lj
     * @Date:10:29
     * @Param [cashFlowDto]
     * @return java.util.List<com.njwd.platform.entity.vo.CashFlowVo>
     **/
    List<CashFlowVo> findCashFlowList(CashFlowDto cashFlowDto);

    /**
     * 根据ID查询现金流量项目表
     * @Author lj
     * @Date:16:57 2019/11/13
     * @param cashFlowDto
     * @return com.njwd.entity.platform.vo.CashFlowVo
     **/
    CashFlowVo findCashFlowById(CashFlowDto cashFlowDto);

    /**
     * 查询现金流量项目表列表分页
     * @Author lj
     * @Date:11:15 2019/11/12
     * @param cashFlowDto
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.CashFlowVo>
     **/
    Page<CashFlowVo> findCashFlowListPage(CashFlowDto cashFlowDto);

    /**
     * 导出
     * @param cashFlowDto
     * @param response
     */
    void exportExcel(CashFlowDto cashFlowDto, HttpServletResponse response);

    /**
     * @Description 根据会计准则id、账簿类型id查询现金流量项目表列表
     * @Author lj
     * @Date:15:05 2019/6/25
     * @Param []
     * @return java.util.List<com.njwd.platform.entity.vo.CashFlowVo>
     **/
    List<CashFlowVo> findCashListByStandIdAndTypeId(CashFlowDto cashFlowDto);
}
