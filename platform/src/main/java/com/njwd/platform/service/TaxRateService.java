package com.njwd.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.TaxRateDto;
import com.njwd.entity.platform.vo.TaxRateVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description 税率 service
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
public interface TaxRateService {

	/**
	 * @Description 新增税率
	 * @Author 郑勇浩
	 * @Data 2019/11/13 17:43
	 * @Param [param]
	 * @return int
	 */
	int addTaxRate(TaxRateDto param);

	/**
	 * @Description 更新税率
	 * @Author 郑勇浩
	 * @Data 2019/11/14 14:56
	 * @Param [param]
	 * @return int
	 */
	int updateTaxRate(TaxRateDto param);

	/**
	 * @Description 批量删除税率
	 * @Author 郑勇浩
	 * @Data 2019/11/12 16:14
	 * @Param [param]
	 * @return com.njwd.support.BatchResult
	 */
	BatchResult deleteTaxRate(TaxRateDto param);

	/**
	 * @Description 批量审核税率
	 * @Author 郑勇浩
	 * @Data 2019/11/13 11:09
	 * @Param [param]
	 * @return com.njwd.support.BatchResult
	 */
	BatchResult approvedTaxRate(TaxRateDto param);

	/**
	 * @Description 批量反审核税率
	 * @Author 郑勇浩
	 * @Data 2019/11/13 11:09
	 * @Param [param]
	 * @return com.njwd.support.BatchResult
	 */
	BatchResult disapprovedTaxRate(TaxRateDto param);

	/**
	 * @Description 批量发布税率
	 * @Author 郑勇浩
	 * @Data 2019/11/13 11:09
	 * @Param [param]
	 * @return com.njwd.support.BatchResult
	 */
	BatchResult releasedTaxRate(TaxRateDto param);

	/**
	 * @Description 查询税率
	 * @Author 郑勇浩
	 * @Data 2019/11/15 11:44
	 * @Param [param]
	 * @return com.njwd.entity.platform.vo.TaxRateVo
	 */
	TaxRateVo findTaxRate(TaxRateDto param);

	/**
	 * @Description 查询税率
	 * @Author 郑勇浩
	 * @Data 2019/11/13 17:13
	 * @Param [param]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.TaxRateVo>
	 */
	Page<TaxRateVo> findTaxRatePage(TaxRateDto param);

	/**
	 * @Description 查询税率[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/13 17:18
	 * @Param [param]
	 * @return java.util.List<com.njwd.entity.platform.vo.TaxRateVo>
	 */
	List<TaxRateVo> findTaxRateList(TaxRateDto param);

	/**
	 * @Description 导出EXCEL
	 * @Author 郑勇浩
	 * @Data 2019/11/19 17:08
	 * @Param [response, param]
	 */
	void exportExcel(HttpServletResponse response, TaxRateDto param);
}
