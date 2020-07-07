package com.njwd.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.CostItemDto;
import com.njwd.entity.platform.vo.CostItemVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @description: 费用项目
 * @author: xdy
 * @create: 2019-11-19 16:39
 */
public interface CostItemService{

    /**
     * @description: 新增费用项目
     * @param: [costItemDto]
     * @return: com.njwd.entity.platform.vo.CostItemVo
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    CostItemVo addCostItem(CostItemDto costItemDto);

    /**
     * @description: 删除费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.BatchResult
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    BatchResult deleteCostItem(CostItemDto costItemDto);

    /**
     * @description: 修改费用项目
     * @param: [costItemDto]
     * @return: com.njwd.entity.platform.vo.CostItemVo
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    CostItemVo updateCostItem(CostItemDto costItemDto);

    /**
     * @description: 根据主键查询费用项目
     * @param: [costItemDto]
     * @return: com.njwd.entity.platform.vo.CostItemVo
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    CostItemVo findCostItemById(CostItemDto costItemDto);

    /**
     * @description: 费用项目分页
     * @param: [costItemDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.CostItemVo>
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    Page<CostItemVo> findCostItemPage(CostItemDto costItemDto);

    /**
     * @description: 审核费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.BatchResult
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    BatchResult approveCostItem(CostItemDto costItemDto);

    /**
     * @description: 反审核费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.BatchResult
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    BatchResult reverseApproveCostItem(CostItemDto costItemDto);

    /**
     * @description: 发布费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.BatchResult
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    BatchResult releaseCostItem(CostItemDto costItemDto);

    /**
     * @description: 导出费用项目
     * @param: [costItemDto, response]
     * @return: void
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    void exportExcel(CostItemDto costItemDto, HttpServletResponse response);

    /**
     * @description: 费用项目列表
     * @param: [costItemDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.CostItemVo>
     * @author: xdy
     * @create: 2019-11-19 16:39
     */
    List<CostItemVo> findCostItemList(CostItemDto costItemDto);

}

