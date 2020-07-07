package com.njwd.basedata.service;

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
public interface CostItemService {

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
    
    /**
     * @description: 禁用费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.BatchResult 
     * @author: xdy        
     * @create: 2019-11-29 09:48 
     */
    BatchResult forbiddenCostItem(CostItemDto costItemDto);
    
    /**
     * @description: 反禁用费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.BatchResult 
     * @author: xdy        
     * @create: 2019-11-29 09:53 
     */
    BatchResult antiForbiddenCostItem(CostItemDto costItemDto);
    
    /**
     * @description: 分配费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.BatchResult 
     * @author: xdy        
     * @create: 2019-11-29 10:01 
     */
    BatchResult allotCostItem(CostItemDto costItemDto);
    
    /**
     * @description: 取消分配费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.BatchResult 
     * @author: xdy        
     * @create: 2019-11-29 10:03 
     */
    BatchResult cancelAllotCostItem(CostItemDto costItemDto);

    
    /**
     * @description: 升级费用项目
     * @param: [costItemDto]
     * @return: com.njwd.entity.platform.vo.CostItemVo 
     * @author: xdy        
     * @create: 2019-11-29 10:07 
     */
    CostItemVo upgradeAllotCostItem(CostItemDto costItemDto);
    
    /**
     * @description: 引入费用项目
     * @param: [costItemDto]
     * @return: com.njwd.support.BatchResult 
     * @author: xdy        
     * @create: 2019-11-29 10:14 
     */
    BatchResult bringInCostItem(CostItemDto costItemDto);
}

