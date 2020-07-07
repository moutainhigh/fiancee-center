package com.njwd.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.dto.CurrencyDto;
import com.njwd.entity.basedata.vo.CurrencyVo;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;

import javax.servlet.http.HttpServletResponse;

/**
 * @Description 币种 service.
 * @Date 2019/11/27 15:51
 * @Author 郑勇浩
 */
public interface CurrencyService {

	/**
	 * @Description 批量新增币种
	 * @Author 郑勇浩
	 * @Data 2019/11/28 15:49
	 * @Param [param]
	 * @return int
	 */
	int addCurrencyBatch(CurrencyDto param);

	/**
	 * @Description 更新币种信息
	 * @Author 郑勇浩
	 * @Data 2019/11/28 14:43
	 * @Param [param]
	 * @return int
	 */
	int updateCurrency(CurrencyDto param);

	/**
	 * @Description 批量删除币种
	 * @Author 郑勇浩
	 * @Data 2019/11/28 16:35
	 * @Param [param]
	 * @return com.njwd.support.BatchResult
	 */
	BatchResult deleteCurrencyBatch(CurrencyDto param);

	/**
	 * @Description 批量启用币种
	 * @Author 郑勇浩
	 * @Data 2019/11/28 16:35
	 * @Param [param]
	 * @return com.njwd.support.BatchResult
	 */
	BatchResult enableCurrencyBatch(CurrencyDto param);

	/**
	 * @Description 批量禁用币种
	 * @Author 郑勇浩
	 * @Data 2019/11/28 16:35
	 * @Param [param]
	 * @return com.njwd.support.BatchResult
	 */
	BatchResult disableCurrencyBatch(CurrencyDto param);

	/**
	 * @Description 查询币种
	 * @Author 郑勇浩
	 * @Data 2019/11/27 16:38
	 * @Param [param]
	 * @return com.njwd.entity.basedata.vo.CurrencyVo
	 */
	CurrencyVo findCurrency(CurrencyDto param);

	/**
	 * @Description 查询币种[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/27 16:38
	 * @Param [param]
	 * @return com.njwd.entity.basedata.vo.CurrencyVo
	 */
	Page<CurrencyVo> findCurrencyPage(CurrencyDto param);

	/**
	 * @Description 查询平台币种[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/28 10:14
	 * @Param [param]
	 * @return com.njwd.support.Result
	 */
	Result findPlatformCurrencyList(CurrencyDto param);

	/**
	 * @Description Excel 导出
	 * @Author 郑勇浩
	 * @Data 2019/12/3 15:59
	 * @Param [response, currencyDto]
	 */
	void exportExcel(HttpServletResponse response, CurrencyDto currencyDto);

}
