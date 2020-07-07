package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.TaxSystem;
import com.njwd.entity.platform.dto.TaxSystemDto;
import com.njwd.entity.platform.vo.TaxSystemVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description 税收制度 mapper
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
public interface TaxSystemMapper extends BaseMapper<TaxSystem> {

	/**
	 * @Description 查询税制
	 * @Author 郑勇浩
	 * @Data 2019/11/13 17:34
	 * @Param [taxSystemDto]
	 * @return com.njwd.entity.platform.vo.TaxSystemVo
	 */
	TaxSystemVo findTaxSystem(@Param("taxSystemDto") TaxSystemDto taxSystemDto);

	/**
	 * @Description 查询税制[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/13 17:09
	 * @Param [page, currencyDto]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.TaxSystemVo>
	 */
	Page<TaxSystemVo> findTaxSystemPage(Page<TaxSystemVo> page, @Param("taxSystemDto") TaxSystemDto taxSystemDto);

	/**
	 * @Description 查询税制[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/13 17:17
	 * @Param [taxSystemDto]
	 * @return java.util.List<com.njwd.entity.platform.vo.TaxSystemVo>
	 */
	List<TaxSystemVo> findTaxSystemList(@Param("taxSystemDto") TaxSystemDto taxSystemDto);

	/**
	 * @Description 查询税制[列表]状态
	 * @Author 郑勇浩
	 * @Data 2019/11/13 17:40
	 * @Param [taxSystemDto]
	 * @return java.util.List<com.njwd.entity.platform.vo.TaxSystemVo>
	 */
	List<TaxSystemVo> findTaxSystemListStatus(@Param("taxSystemDto") TaxSystemDto taxSystemDto);

	/**
	 * @Description 批量修改状态
	 * @Author 郑勇浩
	 * @Data 2019/11/15 18:11
	 * @Param [taxSystemDto]
	 * @return int
	 */
	int updateStatusBatch(@Param("taxSystemDto") TaxSystemDto taxSystemDto);

}
