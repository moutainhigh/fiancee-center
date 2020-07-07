package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.CurrencyDto;
import com.njwd.entity.platform.vo.CurrencyVo;
import com.njwd.platform.service.CurrencyService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 币种 controller
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@RestController
@RequestMapping("currency")
public class CurrencyController extends BaseController {

	@Resource
	private CurrencyService currencyService;

	/**
	 * @Description 查询币种
	 * @Author 郑勇浩
	 * @Data 2019/11/19 11:14
	 * @Param [param]
	 * @return com.njwd.support.Result<com.njwd.entity.platform.vo.CurrencyVo>
	 */
	@RequestMapping("findCurrency")
	public Result<CurrencyVo> findCurrency(@RequestBody CurrencyDto param) {
		FastUtils.checkParams(param.getId());
		return ok(currencyService.findCurrency(param));
	}

	/**
	 * @Description 查询币种[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 9:46
	 * @Param [currencyDto]
	 * @return com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page < com.njwd.entity.platform.vo.CurrencyVo>>
	 */
	@RequestMapping("findCurrencyPage")
	public Result<Page<CurrencyVo>> findCurrencyPage(@RequestBody CurrencyDto param) {
		return ok(currencyService.findCurrencyPage(param));
	}

	/**
	 * @Description 查询币种[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 9:59
	 * @Param [currencyDto]
	 * @return com.njwd.support.Result<java.util.List < com.njwd.entity.platform.vo.CurrencyVo>>
	 */
	@RequestMapping("findCurrencyList")
	public Result<List<CurrencyVo>> findCurrencyList(@RequestBody CurrencyDto param) {
		return ok(currencyService.findCurrencyList(param));
	}
}
