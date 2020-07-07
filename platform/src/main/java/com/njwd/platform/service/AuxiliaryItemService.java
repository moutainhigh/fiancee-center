package com.njwd.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.SourceOfValue;
import com.njwd.entity.platform.dto.AuxiliaryItemDto;
import com.njwd.entity.platform.dto.SourceOfValueDto;
import com.njwd.entity.platform.dto.SubjectDto;
import com.njwd.entity.platform.vo.AuxiliaryItemVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author liuxiang
 * @Description 辅助核算
 * @Date:16:17 2019/6/25
 **/
public interface AuxiliaryItemService {

    /**
     * @return com.njwd.platform.entity.vo.AuxiliaryItemVo
     * @Description 根据ID查询辅助核算
     * @Author lj
     * @Date:10:26
     * @Param [auxiliaryItemVo]
     **/
    AuxiliaryItemVo findAuxiliaryItemById(AuxiliaryItemDto auxiliaryDto);

    /**
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.platform.entity.vo.AuxiliaryItemVo>
     * @Description 查询辅助核算分页
     * @Author lj
     * @Date:10:27
     * @Param [auxiliaryItemDto]
     **/
    Page<AuxiliaryItemVo> findAuxiliaryItemPage(AuxiliaryItemDto auxiliaryItemDto);

    /**
     * @Description 查询辅助核算列表
     * @Author liuxiang
     * @Date:15:48 2019/7/2
     * @Param [auxiliaryDto]
     * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.platform.entity.vo.AuxiliaryItemVo>
     **/
    List<AuxiliaryItemVo> findAuxiliaryItemList(AuxiliaryItemDto auxiliaryDto);

    /**
     * @return java.util.List<com.njwd.platform.entity.vo.AuxiliaryItemVo>
     * @Description 根据名称字符串查询辅助核算
     * @Author lj
     * @Date:10:27
     * @Param [auxiliaryItemVo]
     **/
    List<AuxiliaryItemVo> findAuxiliaryItemListByNames(AuxiliaryItemDto auxiliaryDto);

    /**
     * @Description 查询所有未删除的辅助核算
     * @Author wuweiming
     * @Param []
     * @return List<AuxiliaryItemVo>
     **/
    List<AuxiliaryItemVo> findAllAuxiliaryItem();
    
    /**
     * @description: 查询值来源
     * @param: [sourceOfValueDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.SourceOfValue> 
     * @author: xdy        
     * @create: 2019-11-21 15:53 
     */
    Page<SourceOfValue> findSourceOfValuePage(SourceOfValueDto sourceOfValueDto);
    
    /**
     * @description: 新增核算项目
     * @param: [auxiliaryItemDto]
     * @return: com.njwd.entity.platform.vo.AuxiliaryItemVo 
     * @author: xdy        
     * @create: 2019-11-21 15:53 
     */
    AuxiliaryItemVo addAuxiliaryItem(AuxiliaryItemDto auxiliaryItemDto);

    /**
     * @description: 修改核算项目
     * @param: [auxiliaryItemDto]
     * @return: com.njwd.entity.platform.vo.AuxiliaryItemVo
     * @author: xdy
     * @create: 2019-11-21 15:53
     */
    AuxiliaryItemVo updateAuxiliaryItem(AuxiliaryItemDto auxiliaryItemDto);
    
    /**
     * @description: 删除核算项目
     * @param: [auxiliaryItemDto]
     * @return: com.njwd.support.BatchResult 
     * @author: xdy        
     * @create: 2019-11-21 15:53 
     */
    BatchResult deleteAuxiliaryItem(AuxiliaryItemDto auxiliaryItemDto);
    
    /**
     * @description: 审核核算项目
     * @param: [auxiliaryItemDto]
     * @return: com.njwd.support.BatchResult 
     * @author: xdy        
     * @create: 2019-11-21 15:54 
     */
    BatchResult approveAuxiliaryItem(AuxiliaryItemDto auxiliaryItemDto);
    
    /**
     * @description: 发布核算项目
     * @param: [auxiliaryItemDto]
     * @return: com.njwd.support.BatchResult 
     * @author: xdy        
     * @create: 2019-11-21 15:54 
     */
    BatchResult releaseAuxiliaryItem(AuxiliaryItemDto auxiliaryItemDto);
    
    /**
     * @description: 反审核核算项目
     * @param: [auxiliaryItemDto]
     * @return: com.njwd.support.BatchResult 
     * @author: xdy        
     * @create: 2019-11-21 15:54 
     */
    BatchResult reversalApproveAuxiliaryItem(AuxiliaryItemDto auxiliaryItemDto);
    
    /**
     * @description: 导出核算项目
     * @param: [auxiliaryItemDto, response]
     * @return: void 
     * @author: xdy        
     * @create: 2019-11-21 15:54 
     */
    void exportExcel(AuxiliaryItemDto auxiliaryItemDto, HttpServletResponse response);

    /**
     * 查询科目表配置的辅助核算
     *
     * @param subjectDto
     * @return Result
     * @author 周鹏
     * @date 2019/12/3
     */
    List<AuxiliaryItemVo> findBySubjectId(SubjectDto subjectDto);
}
