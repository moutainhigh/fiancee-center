package com.njwd.ledger.service;


import com.njwd.entity.ledger.dto.BalanceSubjectCashJournalQueryDto;
import com.njwd.entity.ledger.vo.BalanceSubjectCashJournalVo;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.support.Result;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author wuweiming
 * @since 2019-08-13
 */
public interface CashJournalService {

	/**
	 * 根据条件查询科目信息
	 * @param dto
	 * @return Result<AccountSubjectVo>
	 * @author: wuweiming
	 * @create: 2019/09/18
	 */
	AccountSubjectVo findSubjectInfoByParamWithData(BalanceSubjectCashJournalQueryDto dto);

	/**
	 * @Description 查询 现金日记账/银行日记账
	 * @Author wuweiming
	 * @Data 2019/08/07 17:02
	 * @Param BalanceSubjectCashJournalQueryDto
	 * @return com.njwd.ledger.entity.vo.BalanceSubjectCashJournalVo
	 */
	List<BalanceSubjectCashJournalVo> findCashJournalList(BalanceSubjectCashJournalQueryDto dto);

	/**
	 * Excel 导出
	 * @param dto
	 * @param response
	 * @author: wuweiming
	 * @create: 2019/6/12
	 */
	void exportExcel(BalanceSubjectCashJournalQueryDto dto, HttpServletResponse response);

	/**
	 * Excel 导出
	 * @param dto
	 * @param response
	 * @author: wuweiming
	 * @create: 2019/9/30
	 */
	void exportExcelAll(BalanceSubjectCashJournalQueryDto dto, HttpServletResponse response);

}
