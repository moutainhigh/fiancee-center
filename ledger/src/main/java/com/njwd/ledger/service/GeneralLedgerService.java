package com.njwd.ledger.service;


import com.njwd.entity.ledger.dto.GeneralLedgerQueryDto;
import com.njwd.entity.ledger.vo.GeneralLedgerVo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description 总分类账Service
 * @Date 2019/7/30 9:14
 * @Author 薛永利
 */
public interface GeneralLedgerService {
	/**
	 * @Description 查询总分类账
	 * @Author 郑勇浩
	 * @Data 2019/9/27 16:49
	 * @Param []
	 * @return java.util.List<com.njwd.entity.ledger.vo.GeneralLedgerVo>
	 */
	List<GeneralLedgerVo> findGeneralLedgerList(GeneralLedgerQueryDto generalLedgerQueryDto);

	/**
	 * @Description 查询明细分类账
	 * @Author 郑勇浩
	 * @Data 2019/9/30 17:15
	 * @Param [generalLedgerQueryDto]
	 * @return java.util.List<com.njwd.entity.ledger.vo.GeneralLedgerVo>
	 */
	List<GeneralLedgerVo> findDetailLedgerList(GeneralLedgerQueryDto generalLedgerQueryDto);

	/**
	 * @Description 导出总分类账excel
	 * @Author 薛永利
	 * @Date 2019/8/26 9:34
	 * @Param [generalLedgerQueryDto, response]
	 * @return void
	 */
	void exportGeneralLedgerExcel(GeneralLedgerQueryDto generalLedgerQueryDto, HttpServletResponse response);

	/**
	 * @Description 导出明细分类账excel
	 * @Author 薛永利
	 * @Date 2019/8/26 9:34
	 * @Param [generalLedgerQueryDto, response]
	 * @return void
	 */
	void exportDetailLedgerExcel(GeneralLedgerQueryDto generalLedgerQueryDto, HttpServletResponse response);
}
