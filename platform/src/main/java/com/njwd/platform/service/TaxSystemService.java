package com.njwd.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.TaxSystemDto;
import com.njwd.entity.platform.vo.TaxSystemVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description 税收制度 service
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
public interface TaxSystemService {

	/**
	 * @Description 新增税制
	 * @Author 郑勇浩
	 * @Data 2019/11/13 17:43
	 * @Param [param]
	 * @return int
	 */
	int addTaxSystem(TaxSystemDto param);

	/**
	 * @Description 更新税制
	 * @Author 郑勇浩
	 * @Data 2019/11/14 14:56
	 * @Param [param]
	 * @return int
	 */
	int updateTaxSystem(TaxSystemDto param);

	/**
	 * @Description 批量删除税制
	 * @Author 郑勇浩
	 * @Data 2019/11/12 16:14
	 * @Param [taxSystemDto]
	 * @return com.njwd.support.BatchResult
	 */
	BatchResult deleteTaxSystem(TaxSystemDto taxSystemDto);

	/**
	 * @Description 批量审核税制
	 * @Author 郑勇浩
	 * @Data 2019/11/13 11:09
	 * @Param [taxSystemDto]
	 * @return com.njwd.support.BatchResult
	 */
	BatchResult approvedTaxSystem(TaxSystemDto taxSystemDto);

	/**
	 * @Description 批量反审核税制
	 * @Author 郑勇浩
	 * @Data 2019/11/13 11:09
	 * @Param [taxSystemDto]
	 * @return com.njwd.support.BatchResult
	 */
	BatchResult disapprovedTaxSystem(TaxSystemDto taxSystemDto);

	/**
	 * @Description 批量发布税制
	 * @Author 郑勇浩
	 * @Data 2019/11/13 11:09
	 * @Param [taxSystemDto]
	 * @return com.njwd.support.BatchResult
	 */
	BatchResult releasedTaxSystem(TaxSystemDto taxSystemDto);

	/**
	 * @Description 查询税制
	 * @Author 郑勇浩
	 * @Data 2019/11/13 17:43
	 * @Param [param]
	 * @return int
	 */
	TaxSystemVo findTaxSystem(TaxSystemDto param);

	/**
	 * @Description 查询税制
	 * @Author 郑勇浩
	 * @Data 2019/11/13 17:13
	 * @Param [param]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.TaxSystemVo>
	 */
	Page<TaxSystemVo> findTaxSystemPage(TaxSystemDto param);

	/**
	 * @Description 查询税制[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/13 17:18
	 * @Param [param]
	 * @return java.util.List<com.njwd.entity.platform.vo.TaxSystemVo>
	 */
	List<TaxSystemVo> findTaxSystemList(TaxSystemDto param);

	/**
	 * @Description 导出EXCEL
	 * @Author 郑勇浩
	 * @Data 2019/11/19 17:08
	 * @Param [response, param]
	 */
	void exportExcel(HttpServletResponse response, TaxSystemDto param);
}
