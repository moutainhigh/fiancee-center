package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.Unit;
import com.njwd.entity.platform.dto.UnitDto;
import com.njwd.entity.platform.vo.UnitVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UnitMapper extends BaseMapper<Unit> {
    
    /**
     * @description: 
     * @param: [unitDto]
     * @return: com.njwd.entity.platform.vo.UnitVo 
     * @author: xdy        
     * @create: 2019-11-19 11:46 
     */
    UnitVo findUnitById(UnitDto unitDto);
    
    /**
     * @description: 
     * @param: [page, unitDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.UnitVo> 
     * @author: xdy        
     * @create: 2019-11-19 11:46 
     */
    Page<UnitVo> findUnitPage(Page<UnitVo> page,@Param("unitDto") UnitDto unitDto);
    
    /**
     * @description: 
     * @param: [unitDto]
     * @return: int 
     * @author: xdy        
     * @create: 2019-11-19 11:47 
     */
    int updateBatch(UnitDto unitDto);
    
    /**
     * @description: 
     * @param: [unitDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.UnitVo> 
     * @author: xdy        
     * @create: 2019-11-19 16:40
     */
    List<UnitVo> findUnitList(UnitDto unitDto);
}
