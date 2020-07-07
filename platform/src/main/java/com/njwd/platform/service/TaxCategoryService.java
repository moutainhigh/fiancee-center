package com.njwd.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.TaxCategoryDto;
import com.njwd.entity.platform.vo.TaxCategoryVo;
import com.njwd.entity.platform.vo.TaxSystemVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description 税种 service
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
public interface TaxCategoryService {

	/**
	 * @Description 新增税种
	 * @Author 郑勇浩
	 * @Data 2019/11/14 17:55
	 * @Param [param]
	 * @return int
	 */
	int addTaxCategory(TaxCategoryDto param);

	/**
	 * @Description 更新税种
	 * @Author 郑勇浩
	 * @Data 2019/11/14 17:55
	 * @Param [param]
	 * @return int
	 */
	int updateTaxCategory(TaxCategoryDto param);

	/**
	 * @Description 批量删除税种
	 * @Author 郑勇浩
	 * @Data 2019/11/14 17:56
	 * @Param [taxSystemDto]
	 * @return com.njwd.support.BatchResult
	 */
	BatchResult deleteTaxCategory(TaxCategoryDto taxSystemDto);

	/**
	 * @Description 批量审核税种
	 * @Author 郑勇浩
	 * @Data 2019/11/14 17:56
	 * @Param [taxSystemDto]
	 * @return com.njwd.support.BatchResult
	 */
	BatchResult approvedTaxCategory(TaxCategoryDto taxSystemDto);

	/**
	 * @Description 批量反审核税种
	 * @Author 郑勇浩
	 * @Data 2019/11/14 17:56
	 * @Param [taxSystemDto]
	 * @return com.njwd.support.BatchResult
	 */
	BatchResult disapprovedTaxCategory(TaxCategoryDto taxSystemDto);

	/**
	 * @Description 批量发布税种
	 * @Author 郑勇浩
	 * @Data 2019/11/14 17:56
	 * @Param [taxSystemDto]
	 * @return com.njwd.support.BatchResult
	 */
	BatchResult releasedTaxCategory(TaxCategoryDto taxSystemDto);

	/**
	 * @Description 查询税种
	 * @Author 郑勇浩
	 * @Data 2019/11/15 9:33
	 * @Param [param]
	 * @return com.njwd.entity.platform.vo.TaxCategoryVo
	 */
	TaxCategoryVo findTaxCategory(TaxCategoryDto param);

	/**
	 * @Description 查询税种[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/15 9:33
	 * @Param [param]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.TaxCategoryVo>
	 */
	Page<TaxCategoryVo> findTaxCategoryPage(TaxCategoryDto param);

	/**
	 * @Description 导出EXCEL
	 * @Author 郑勇浩
	 * @Data 2019/11/19 17:34
	 * @Param [response, param]
	 */
	void exportExcel(HttpServletResponse response, TaxCategoryDto param);

	/**
	 * @Description 查询税种[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/15 9:34
	 * @Param [param]
	 * @return java.util.List<com.njwd.entity.platform.vo.TaxCategoryVo>
	 */
	List<TaxCategoryVo> findTaxCategoryList(TaxCategoryDto param);
}
