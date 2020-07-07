package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.ledger.AccountBookPeriodReport;
import com.njwd.entity.ledger.dto.AccountBookPeriodReportDto;
import com.njwd.entity.ledger.vo.AccountBookPeriodReportVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 * @author xyyxhcj@qq.com
 * @since 2019/7/24
 */
public interface AccountBookPeriodReportMapper extends BaseMapper<AccountBookPeriodReport> {
    
    /**
     * @description: 新增整理报告
     * @param: [reportList]
     * @return: int 
     * @author: xdy        
     * @create: 2019-10-08 11:42 
     */
    int addBatch(List<AccountBookPeriodReport> reportList);
    
    /**
     * @description: 查询整理报告
     * @param: [page, accountBookPeriodReportDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.ledger.vo.AccountBookPeriodReportVo> 
     * @author: xdy        
     * @create: 2019-10-08 11:43 
     */
    Page<AccountBookPeriodReportVo> findPage(@Param("page") Page<AccountBookPeriodReportVo> page, @Param("accountBookPeriodReportDto") AccountBookPeriodReportDto accountBookPeriodReportDto);

}