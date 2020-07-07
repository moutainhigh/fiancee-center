package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.platform.AccountingStandardCurrency;
import com.njwd.entity.platform.vo.AccountingStandardCurrencyVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
/**
 *
 * @author xyyxhcj@qq.com
 * @since 2019/11/19
 */
public interface AccountingStandardCurrencyMapper extends BaseMapper<AccountingStandardCurrency> {
	/**
	 * 查准则下的币种
	 *
	 * @param accStandardId accStandardId
	 * @return java.util.List<com.njwd.entity.platform.vo.AccountingStandardCurrencyVo>
	 * @author xyyxhcj@qq.com
	 * @date 2019/11/19 14:18
	 **/
	List<AccountingStandardCurrencyVo> findByAccStandardId(@Param("accStandardId")Long accStandardId);

}
