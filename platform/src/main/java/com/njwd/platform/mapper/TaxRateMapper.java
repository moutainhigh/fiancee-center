package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.TaxRate;
import com.njwd.entity.platform.dto.TaxRateDto;
import com.njwd.entity.platform.vo.TaxRateVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description 税率 mapper
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
public interface TaxRateMapper extends BaseMapper<TaxRate> {

	/**
	 * @Description 查询税率
	 * @Author 郑勇浩
	 * @Data 2019/11/15 11:33
	 * @Param [taxRateDto]
	 * @return com.njwd.entity.platform.vo.TaxRateVo
	 */
	TaxRateVo findTaxRate(@Param("taxRateDto") TaxRateDto taxRateDto);

	/**
	 * @Description 查询税率[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/15 11:33
	 * @Param [page, taxRateDto]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.TaxRateVo>
	 */
	Page<TaxRateVo> findTaxRatePage(Page<TaxRateVo> page, @Param("taxRateDto") TaxRateDto taxRateDto);

	/**
	 * @Description 查询税率[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/15 11:33
	 * @Param [taxRateDto]
	 * @return java.util.List<com.njwd.entity.platform.vo.TaxRateVo>
	 */
	List<TaxRateVo> findTaxRateList(@Param("taxRateDto") TaxRateDto taxRateDto);

	/**
	 * @Description 查询税率[列表]状态
	 * @Author 郑勇浩
	 * @Data 2019/11/15 11:33
	 * @Param [taxRateDto]
	 * @return java.util.List<com.njwd.entity.platform.vo.TaxRateVo>
	 */
	List<TaxRateVo> findTaxRateListStatus(@Param("taxRateDto") TaxRateDto taxRateDto);

}
