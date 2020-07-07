package com.njwd.platform.api;

import com.njwd.entity.platform.dto.CurrencyDto;
import com.njwd.entity.platform.vo.CurrencyVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Description 币种 api
 * @Date 2019/11/28 10:05
 * @Author 郑勇浩
 */
@RequestMapping("platform/currency")
public interface CurrencyApi {

	/**
	 * @Description 查询币种
	 * @Author 郑勇浩
	 * @Data 2019/11/28 10:06
	 * @Param [param]
	 * @return com.njwd.support.Result
	 */
	@PostMapping("findCurrency")
	Result findCurrency(@RequestBody CurrencyDto param);

	/**
	 * @Description 查询币种[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/28 10:06
	 * @Param [param]
	 * @return com.njwd.support.Result
	 */
	@PostMapping("findCurrencyPage")
	Result findCurrencyPage(@RequestBody CurrencyDto param);

	/**
	 * @Description 查询币种[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/28 10:06
	 * @Param [param]
	 * @return com.njwd.support.Result
	 */
	@PostMapping("findCurrencyList")
	Result<List<CurrencyVo>> findCurrencyList(@RequestBody CurrencyDto param);

}
