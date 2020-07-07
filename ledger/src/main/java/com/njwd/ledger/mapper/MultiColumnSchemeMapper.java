package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.ledger.BalanceSubject;
import com.njwd.entity.ledger.BalanceSubjectAuxiliary;
import com.njwd.entity.ledger.MultiColumnScheme;
import com.njwd.entity.ledger.VoucherEntry;
import com.njwd.entity.ledger.dto.MultiColumnReportDto;
import com.njwd.entity.ledger.dto.MultiColumnSchemeDto;
import com.njwd.entity.ledger.vo.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 多栏账方案
 * @Date:13:41 2019/7/29
 **/
public interface MultiColumnSchemeMapper extends BaseMapper<MultiColumnScheme> {
    
    
    /**
     * @description: 新增多栏账方案
     * @param: [multiColumnScheme]
     * @return: int 
     * @author: xdy        
     * @create: 2019-10-08 11:26 
     */
    int addMultiColumnScheme(MultiColumnScheme multiColumnScheme);
    
    /**
     * @description: 修改多栏账方案
     * @param: [multiColumnScheme]
     * @return: int 
     * @author: xdy        
     * @create: 2019-10-08 11:27 
     */
    int updateMultiColumnScheme(MultiColumnScheme multiColumnScheme);

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
     * @return java.util.List<com.njwd.ledger.entity.vo.MultiColumnSchemeVo>
     * @Description 根据CodeOrName查询多栏账方案分页
     * @Author liuxiang
     * @Date:14:25 2019/7/29
     * @Param [multiColumnSchemeDto]
     **/
    Page<MultiColumnSchemeVo> findMultiColumnSchemePage(@Param("page") Page<MultiColumnSchemeVo> page, @Param("multiColumnSchemeDto") MultiColumnSchemeDto multiColumnSchemeDto);

    /**
     * @return java.util.List<com.njwd.ledger.entity.vo.MultiColumnSchemeVo>
     * @Description 查询多栏账的id的is_del状态
     * @Author liuxiang
     * @Date:16:17 2019/7/29
     * @Param [multiColumnSchemeDto]
     **/
    List<MultiColumnSchemeVo> findStatusByIdList(MultiColumnSchemeDto multiColumnSchemeDto);

    /**
     * @description: 获取凭证分录
     * @param: [multiColumnReportDto]
     * @return: java.util.List<com.njwd.entity.ledger.vo.VoucherEntryVo> 
     * @author: xdy        
     * @create: 2019-08-31 14-34 
     */
    List<VoucherEntryVo> findVoucherEntry(MultiColumnReportDto multiColumnReportDto);
    
    /**
     * @description: 获取科目余额
     * @param: [multiColumnReportDto]
     * @return: java.util.List<com.njwd.entity.ledger.vo.BalanceSubjectVo> 
     * @author: xdy        
     * @create: 2019-08-31 16:00 
     */
    List<BalanceSubjectVo> findBalanceSubject(MultiColumnReportDto multiColumnReportDto);
    
    /**
     * @description: 获取辅助核算余额
     * @param: [multiColumnReportDto]
     * @return: java.util.List<com.njwd.entity.ledger.vo.BalanceSubjectAuxiliaryVo> 
     * @author: xdy        
     * @create: 2019-09-04 14:30 
     */
    List<BalanceSubjectAuxiliaryVo> findBalanceSubjectAuxiliary(MultiColumnReportDto multiColumnReportDto);

    /**
     * @description: 获取最小期间
     * @param: [multiColumnReportDto]
     * @return: com.njwd.entity.ledger.vo.AccountBookPeriodVo 
     * @author: xdy        
     * @create: 2019-09-19 17:55 
     */
    AccountBookPeriodVo findMinPeriod(MultiColumnReportDto multiColumnReportDto);

}