package com.njwd.ledger.service;

import com.njwd.entity.ledger.dto.AuxiliaryAccountingQueryDto;
import com.njwd.entity.ledger.vo.GeneralReturnAuxiliaryVo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface AuxiliaryAccountingBackService {
    /**
     * 查询所有的辅助核算明细账数据
     * @param auxiliaryAccountingQueryDto
     * @return
     */
    List<GeneralReturnAuxiliaryVo> findAuxiliaryDetailList(AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto);

    /**
     * 导出辅助核算明细账数据
     * @param auxiliaryAccountingQueryDto
     * @param response
     */
    void exportExcel(AuxiliaryAccountingQueryDto auxiliaryAccountingQueryDto, HttpServletResponse response);
}
