package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.CostItemCompany;
import com.njwd.entity.platform.CostItem;
import com.njwd.entity.platform.dto.CostItemDto;
import com.njwd.entity.platform.vo.CostItemVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description: 费用项目
 * @author: xdy
 * @create: 2019-11-19 16:50
 */
public interface CostItemMapper extends BaseMapper<CostItem> {

    /**
     * @description: 根据主键查询费用项目
     * @param: [costItemDto]
     * @return: com.njwd.entity.platform.vo.CostItemVo
     * @author: xdy
     * @create: 2019-11-19 16:50
     */
    CostItemVo findCostItemById(CostItemDto costItemDto);

    /**
     * @description: 费用项目分页
     * @param: [page, costItemDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.CostItemVo>
     * @author: xdy
     * @create: 2019-11-19 16:50
     */
    Page<CostItemVo> findCostItemPage(Page<CostItemVo> page, @Param("costItemDto") CostItemDto costItemDto);

    /**
     * @description: 批量更新费用项目
     * @param: [costItemDto]
     * @return: int
     * @author: xdy
     * @create: 2019-11-19 16:50
     */
    int updateBatch(CostItemDto costItemDto);

    /**
     * @description: 费用项目列表
     * @param: [costItemDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.CostItemVo>
     * @author: xdy
     * @create: 2019-11-19 16:50
     */
    List<CostItemVo> findCostItemList(CostItemDto costItemDto);
    
    /**
     * @description: 
     * @param: [costItemList]
     * @return: void 
     * @author: xdy        
     * @create: 2019-12-03 14:02 
     */
    int addBatch(List<? extends CostItem> costItemList);
    
    /**
     * @description: 新增费用项目公司
     * @param: [costItemCompanyList]
     * @return: int 
     * @author: xdy        
     * @create: 2019-12-03 14:02 
     */
    int addCostItemCompany(CostItemDto costItemDto);
    
    /**
     * @description: 删除费用项目公司
     * @param: [costItemDto]
     * @return: int 
     * @author: xdy        
     * @create: 2019-12-03 14:52 
     */
    int deleteCostItemCompany(CostItemDto costItemDto);

}

