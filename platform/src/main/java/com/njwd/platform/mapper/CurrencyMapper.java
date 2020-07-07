package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.Currency;
import com.njwd.entity.platform.dto.CurrencyDto;
import com.njwd.entity.platform.vo.CurrencyVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description 币种 mapper
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
public interface CurrencyMapper extends BaseMapper<Currency> {

	/**
	 * @return com.njwd.entity.platform.vo.CurrencyVo
	 * @Description 查询币种
	 * @Author 郑勇浩
	 * @Data 2019/11/19 10:27
	 * @Param [currencyDto]
	 */
	CurrencyVo findCurrency(@Param("currencyDto") CurrencyDto currencyDto);

	/**
	 * @return java.util.List<com.njwd.entity.platform.vo.CurrencyVo>
	 * @Description 查询币种[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 9:37
	 * @Param [page, currencyDto]
	 */
	Page<CurrencyVo> findCurrencyPage(Page<CurrencyVo> page, @Param("currencyDto") CurrencyDto currencyDto);

	/**
	 * @return java.util.List<com.njwd.entity.platform.vo.CurrencyVo>
	 * @Description 查询币种[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 9:56
	 * @Param []
	 */
	List<CurrencyVo> findCurrencyList(@Param("currencyDto") CurrencyDto currencyDto);

	/**
	 * 查 会计科目使用的币种
	 *
	 * @param accSubjectId accSubjectId
	 * @return java.util.List<com.njwd.entity.platform.vo.CurrencyVo>
	 * @author xyyxhcj@qq.com
	 * @date 2019/11/20 15:00
	 **/
	List<CurrencyVo> findAccSubjectCurrencyList(@Param("accSubjectId") Long accSubjectId);


	/**
	 * @Description 查询所有的币种列表
	 * @Author liuxiang
	 * @Date:15:31 2019/7/2
	 * @Param []
	 * @return java.util.List<com.njwd.platform.entity.vo.CurrencyVo>
	 **/
//    List<CurrencyVo> findAllCurrencyList();
}
