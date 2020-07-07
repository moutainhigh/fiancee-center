package com.njwd.ledger.service;

import com.njwd.entity.ledger.dto.ItemJournalQueryDto;
import com.njwd.entity.ledger.vo.GeneralReturnItemJournalVo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface ItemJournalBackService {

    /**
     * 查询所有科目日记账的数据
     * @param itemJournalQueryDto
     * @return
     */
    List<GeneralReturnItemJournalVo> findGeneralReturnItemJournalList(ItemJournalQueryDto itemJournalQueryDto);

    /**
     * 导出科目日记账
     * @param itemJournalQueryDto
     * @param response
     */
    void exportExcel(ItemJournalQueryDto itemJournalQueryDto, HttpServletResponse response);
}
