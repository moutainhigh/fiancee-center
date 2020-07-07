package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.platform.dto.CurrencyDto;
import com.njwd.entity.platform.vo.CurrencyVo;
import com.njwd.platform.mapper.CurrencyMapper;
import com.njwd.platform.service.CurrencyService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 币种 service impl
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Service
public class CurrencyServiceImpl implements CurrencyService {

	@Resource
	private CurrencyMapper currencyMapper;

	/**
	 * @Description 查询币种
	 * @Author 郑勇浩
	 * @Data 2019/11/19 10:30
	 * @Param [currencyDto]
	 * @return com.njwd.entity.platform.vo.CurrencyVo
	 */
	@Override
	@Cacheable(value = PlatformConstant.RedisCache.CURRENCY, key = "#param.id", unless = "#result == null")
	public CurrencyVo findCurrency(CurrencyDto param) {
		return currencyMapper.findCurrency(param);
	}

	/**
	 * @Description 查询币种[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 9:44
	 * @Param [currencyDto]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.CurrencyVo>
	 */
	@Override
	public Page<CurrencyVo> findCurrencyPage(CurrencyDto param) {
		Page<CurrencyVo> page = param.getPage();
		page = currencyMapper.findCurrencyPage(page, param);
		return page;
	}

	/**
	 * @Description 查询币种[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 9:57
	 * @Param [currencyDto]
	 * @return java.util.List<com.njwd.entity.platform.vo.CurrencyVo>
	 */
	@Override
	public List<CurrencyVo> findCurrencyList(CurrencyDto param) {
		return currencyMapper.findCurrencyList(param);
	}

}
