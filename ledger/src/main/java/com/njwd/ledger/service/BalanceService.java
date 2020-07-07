package com.njwd.ledger.service;

import com.njwd.entity.ledger.dto.BalanceDto;
import com.njwd.entity.ledger.dto.BalanceSubjectQueryDto;
import com.njwd.entity.ledger.vo.BalanceSubjectVo;
import com.njwd.entity.ledger.vo.BalanceVo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 资产负债表——账簿
 *
 * @author zhuzs
 * @date 2019-08-01 17:39
 */
public interface BalanceService {

    /**
     * 资产负债表
     *
     * @param balanceDtos
     * @return
     */
    List<BalanceVo> getBalanceReport(List<BalanceDto> balanceDtos);

    /**
     *
     * @description:资产负债表——导出
     *
     * @author: Zhuzs
     * @create: 2019-09-02 14:48
     */
    void balanceReportExport(List<BalanceDto> balanceDtos, HttpServletResponse response);
}
