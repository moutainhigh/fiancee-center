package com.njwd.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.UnitDto;
import com.njwd.entity.platform.vo.UnitVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/15 10:34
 */
public interface UnitService {
    
    /**
     * @description: 新增计量单位
     * @param: [unitDto]
     * @return: com.njwd.entity.platform.vo.UnitVo 
     * @author: xdy        
     * @create: 2019-11-19 11:28 
     */
    UnitVo addUnit(UnitDto unitDto);
    
    /**
     * @description: 删除计量单位
     * @param: [unitDto]
     * @return: com.njwd.support.BatchResult 
     * @author: xdy        
     * @create: 2019-11-19 11:28 
     */
    BatchResult deleteUnit(UnitDto unitDto);
    
    /**
     * @description: 修改计量单位
     * @param: [unitDto]
     * @return: com.njwd.entity.platform.vo.UnitVo 
     * @author: xdy        
     * @create: 2019-11-19 11:28 
     */
    UnitVo updateUnit(UnitDto unitDto);
    
    /**
     * @description: 根据ID查询计量单位
     * @param: [unitDto]
     * @return: com.njwd.entity.platform.vo.UnitVo 
     * @author: xdy        
     * @create: 2019-11-19 11:28 
     */
    UnitVo findUnitById(UnitDto unitDto);
    
    /**
     * @description: 计量单位分页
     * @param: [unitDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.UnitVo> 
     * @author: xdy        
     * @create: 2019-11-19 11:28 
     */
    Page<UnitVo> findUnitPage(UnitDto unitDto);
    
    /**
     * @description: 审核计量单位
     * @param: [unitDto]
     * @return: com.njwd.support.BatchResult 
     * @author: xdy        
     * @create: 2019-11-19 11:28 
     */
    BatchResult approveUnit(UnitDto unitDto);
    
    /**
     * @description: 反审核计量单位
     * @param: [unitDto]
     * @return: com.njwd.support.BatchResult 
     * @author: xdy        
     * @create: 2019-11-19 11:28 
     */
    BatchResult reverseApproveUnit(UnitDto unitDto);
    
    /**
     * @description: 发布计量单位
     * @param: [unitDto]
     * @return: com.njwd.support.BatchResult 
     * @author: xdy        
     * @create: 2019-11-19 11:28 
     */
    BatchResult releaseUnit(UnitDto unitDto);
    
    /**
     * @description: 导出计量单位
     * @param: [unitDto, response]
     * @return: void 
     * @author: xdy        
     * @create: 2019-11-19 11:28 
     */
    void exportExcel(UnitDto unitDto, HttpServletResponse response);
    
    /**
     * @description: 计量单位列表
     * @param: [unitDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.UnitVo> 
     * @author: xdy        
     * @create: 2019-11-19 16:40
     */
    List<UnitVo> findUnitList(UnitDto unitDto);
}
