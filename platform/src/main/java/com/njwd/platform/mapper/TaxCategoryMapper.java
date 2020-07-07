package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.TaxCategory;
import com.njwd.entity.platform.dto.TaxCategoryDto;
import com.njwd.entity.platform.vo.TaxCategoryVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description 税种 mapper
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
public interface TaxCategoryMapper extends BaseMapper<TaxCategory> {

	/**
	 * @Description 查询税种
	 * @Author 郑勇浩
	 * @Data 2019/11/14 17:47
	 * @Param [taxCategoryDto]
	 * @return com.njwd.entity.platform.vo.TaxCategoryVo
	 */
	TaxCategoryVo findTaxCategory(@Param("taxCategoryDto") TaxCategoryDto taxCategoryDto);

	/**
	 * @Description 查询税种[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/14 17:47
	 * @Param [page, taxCategoryDto]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.TaxCategoryVo>
	 */
	Page<TaxCategoryVo> findTaxCategoryPage(Page<TaxCategoryVo> page, @Param("taxCategoryDto") TaxCategoryDto taxCategoryDto);

	/**
	 * @Description 查询税种[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/14 17:47
	 * @Param [taxCategoryDto]
	 * @return java.util.List<com.njwd.entity.platform.vo.TaxCategoryVo>
	 */
	List<TaxCategoryVo> findTaxCategoryList(@Param("taxCategoryDto") TaxCategoryDto taxCategoryDto);

	/**
	 * @Description 查询税种[列表]状态
	 * @Author 郑勇浩
	 * @Data 2019/11/14 17:47
	 * @Param [taxCategoryDto]
	 * @return java.util.List<com.njwd.entity.platform.vo.TaxCategoryVo>
	 */
	List<TaxCategoryVo> findTaxCategoryListStatus(@Param("taxCategoryDto") TaxCategoryDto taxCategoryDto);

}
