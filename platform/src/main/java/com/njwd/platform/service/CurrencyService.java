package com.njwd.platform.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.CurrencyDto;
import com.njwd.entity.platform.vo.CurrencyVo;

import java.util.List;


/**
 * @Description 币种 service
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
public interface CurrencyService {

	/**
	 * @Description 查询币种
	 * @Author 郑勇浩
	 * @Data 2019/11/19 10:30
	 * @Param [currencyDto]
	 * @return com.njwd.entity.platform.vo.CurrencyVo
	 */
	CurrencyVo findCurrency(CurrencyDto currencyDto);

	/**
	 * @Description 查询币种[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 9:44
	 * @Param [currencyDto]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.CurrencyVo>
	 */
	Page<CurrencyVo> findCurrencyPage(CurrencyDto currencyDto);

	/**
	 * @Description 查询币种[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 9:57
	 * @Param [currencyDto]
	 * @return java.util.List<com.njwd.entity.platform.vo.CurrencyVo>
	 */
	List<CurrencyVo> findCurrencyList(CurrencyDto currencyDto);
}
