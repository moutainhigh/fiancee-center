package com.njwd.ledger.service;


import com.njwd.entity.ledger.dto.ItemJournalQueryDto;
import com.njwd.entity.ledger.vo.GeneralReturnItemJournalVo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author ZhuHC
 * @Date  2019/8/6 17:32
 * @Param 
 * @return 
 * @Description 
 */
public interface ItemJournalService {


    /**
     * @Author ZhuHC
     * @Date  2019/8/8 16:27
     * @Param [itemJournalQueryDto]
     * @return com.njwd.support.Result<java.util.List<com.njwd.entity.ledger.vo.GeneralReturnItemJournalVo>>
     * @Description 科目日记账  查询
     */
    List<GeneralReturnItemJournalVo> getItemJournalDetails(ItemJournalQueryDto itemJournalQueryDto);

    /**
     * @Author ZhuHC
     * @Date  2019/8/30 15:52
     * @Param [itemJournalQueryDto, response]
     * @return void
     * @Description 导出
     */
    void exportExcel(ItemJournalQueryDto itemJournalQueryDto, HttpServletResponse response);

    /**
     * @Author ZhuHC
     * @Date  2019/8/30 15:52
     * @Param [itemJournalQueryDto, response]
     * @return void
     * @Description 全部导出
     */
    void exportAllExcel(ItemJournalQueryDto itemJournalQueryDto, HttpServletResponse response);
}
