package com.njwd.ledger.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.ledger.MultiColumnScheme;
import com.njwd.entity.ledger.dto.MultiColumnReportDto;
import com.njwd.entity.ledger.dto.MultiColumnSchemeDto;
import com.njwd.entity.ledger.vo.MultiColumnReportVo;
import com.njwd.entity.ledger.vo.MultiColumnSchemeVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author liuxiang
 * @Description 多栏账方案
 * @Date:14:25 2019/7/29
 **/
public interface MultiColumnSchemeService {

    /**
     * @Description 新增多栏账方案设置
     * @Author liuxiang
     * @Date:17:14 2019/7/30
     * @Param [multiColumnSchemeDto]
     * @return int
     **/
    MultiColumnSchemeVo addMultiColumnScheme(MultiColumnSchemeDto multiColumnSchemeDto);


    /**
     * @Description 批量删除多栏账方案
     * @Author liuxiang
     * @Date:15:57 2019/7/29
     * @Param [commonAbstractDto]
     * @return com.njwd.system.support.BatchResult
     **/
    BatchResult deleteMultiColumnSchemeBatch(MultiColumnSchemeDto multiColumnSchemeDto);

    /**
     * @Description 修改多栏账方案设置
     * @Author liuxiang
     * @Date:13:52 2019/7/31
     * @Param [multiColumnSchemeDto]
     * @return int
     **/
    int updateMultiColumnScheme(MultiColumnSchemeDto multiColumnSchemeDto);
    /**
     * @Description 根据CodeOrName查询多栏账方案分页
     * @Author liuxiang
     * @Date:14:25 2019/7/29
     * @Param [multiColumnSchemeDto]
     * @return java.util.List<com.njwd.ledger.entity.vo.MultiColumnSchemeVo>
     **/
    Page<MultiColumnSchemeVo> findMultiColumnSchemePageByCodeOrName(MultiColumnSchemeDto multiColumnSchemeDto);


    /**
     * @Description 查询未删除状态的多栏账方案
     * @Author liuxiang
     * @Date:11:01 2019/8/2
     * @Param []
     * @return java.util.List<com.njwd.ledger.entity.vo.MultiColumnSchemeVo>
     **/
    List<MultiColumnSchemeVo> findMultiColumnSchemeList();

    /**
     * @return com.njwd.ledger.entity.vo.MultiColumnSchemeVo
     * @Description 根据ID查询多栏账方案
     * @Author liuxiang
     * @Date:10:49 2019/7/31
     * @Param [multiColumnSchemeDto]
     **/
    MultiColumnSchemeVo findMultiColumnSchemeById(MultiColumnSchemeDto multiColumnSchemeDto);
    
    /**
     * @description: 获取多栏明细账
     * @param: [multiColumnReportDto]
     * @return: com.njwd.entity.ledger.vo.MultiColumnReportVo 
     * @author: xdy        
     * @create: 2019-08-31 11-56 
     */
    MultiColumnReportVo findMultiColumnReport(MultiColumnReportDto multiColumnReportDto);

    /**
     * @description: 导出多栏明细账
     * @param: [multiColumnReportDto, response]
     * @return: void 
     * @author: xdy        
     * @create: 2019-09-05 09:35 
     */
    void exportMultiColumnReport(MultiColumnReportDto multiColumnReportDto, HttpServletResponse response);
}