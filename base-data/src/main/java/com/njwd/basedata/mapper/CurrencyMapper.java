package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.Currency;
import com.njwd.entity.basedata.dto.CurrencyDto;
import com.njwd.entity.basedata.vo.CurrencyVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import org.apache.ibatis.annotations.Param;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;

/**
 * @Description 币种 mapper
 * @Date 2019/11/27 9:21
 * @Author 郑勇浩
 */
public interface CurrencyMapper extends BaseMapper<Currency> {

	/**
	 * @Description 批量新增
	 * @Author xdy
	 * @Data 2019/11/27 16:56
	 * @Param [currencyList]
	 */
	int addBatch(List<? extends Currency> currencyList);

	/**
	 * @Description 批量更新
	 * @Author 郑勇浩
	 * @Data 2019/11/29 9:31
	 * @Param [idList, type, operator, tableName]
	 * @return int
	 */
	int batchProcess(@Param("idList") @NotEmpty Collection<Long> idList, @Param("type") @NotNull int type, @Param("operator") @NotNull SysUserVo operator);

	/**
	 * @Description 查询币种
	 * @Author 郑勇浩
	 * @Data 2019/11/27 17:02
	 * @Param [currencyDto]
	 * @return com.njwd.entity.basedata.vo.CurrencyVo
	 */
	CurrencyVo findCurrency(@Param("currencyDto") CurrencyDto currencyDto);

	/**
	 * @Description 查询币种[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/27 17:30
	 * @Param [page, currencyDto]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.CurrencyVo>
	 */
	Page<CurrencyVo> findCurrencyPage(Page<CurrencyVo> page, @Param("currencyDto") CurrencyDto currencyDto);

	/**
	 * @Description 查询存在的平台币种
	 * @Author 郑勇浩
	 * @Data 2019/11/28 10:30
	 * @Param [currencyDto]
	 * @return java.util.List<java.lang.Long>
	 */
	List<Long> findCurrencyPlatformIds(@Param("currencyDto") CurrencyDto currencyDto);

	/**
	 * @Description 查询币种[列表]状态
	 * @Author 郑勇浩
	 * @Data 2019/11/28 16:38
	 * @Param [currencyDto]
	 * @return java.util.List<com.njwd.entity.basedata.vo.CurrencyVo>
	 */
	List<CurrencyVo> findCurrencyListStatus(@Param("currencyDto") CurrencyDto currencyDto);

}
