package com.njwd.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.CommonAbstractDto;
import com.njwd.entity.platform.vo.CommonAbstractVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description 常用摘要 service
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
public interface CommonAbstractService {

	/**
	 * @Description 新增常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/11/12 15:23
	 * @Param [commonAbstractDto]
	 * @return int
	 */
	int addCommonAbstract(CommonAbstractDto commonAbstractDto);

	/**
	 * @Description 更新常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/11/13 13:55
	 * @Param [commonAbstractDto]
	 * @return long
	 */
	int updateCommonAbstract(CommonAbstractDto commonAbstractDto);

	/**
	 * @Description 批量删除常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/11/12 16:14
	 * @Param [commonAbstractDto]
	 * @return com.njwd.support.BatchResult
	 */
	BatchResult deleteCommonAbstract(CommonAbstractDto commonAbstractDto);

	/**
	 * @Description 批量审核常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/11/13 11:09
	 * @Param [commonAbstractDto]
	 * @return com.njwd.support.BatchResult
	 */
	BatchResult approvedCommonAbstract(CommonAbstractDto commonAbstractDto);

	/**
	 * @Description 批量反审核常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/11/13 11:09
	 * @Param [commonAbstractDto]
	 * @return com.njwd.support.BatchResult
	 */
	BatchResult disapprovedCommonAbstract(CommonAbstractDto commonAbstractDto);

	/**
	 * @Description 批量发布常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/11/13 11:09
	 * @Param [commonAbstractDto]
	 * @return com.njwd.support.BatchResult
	 */
	BatchResult releasedCommonAbstract(CommonAbstractDto commonAbstractDto);

	/**
	 * @Description 查询常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/11/12 15:24
	 * @Param [commonAbstractDto]
	 * @return com.njwd.entity.platform.vo.CommonAbstractVo
	 */
	CommonAbstractVo findCommonAbstract(CommonAbstractDto commonAbstractDto);

	/**
	 * @Description 查询常用摘要[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 14:24
	 * @Param [commonAbstractDto]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.CommonAbstractVo>
	 */
	Page<CommonAbstractVo> findCommonAbstractPage(CommonAbstractDto commonAbstractDto);

	/**
	 * @Description 查询常用摘要[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 14:32
	 * @Param [commonAbstractDto]
	 * @return java.util.List<com.njwd.entity.basedata.CommonAbstract>
	 */
	List<CommonAbstractVo> findCommonAbstractList(CommonAbstractDto commonAbstractDto);

	/**
	 * @Description 导出EXCEL
	 * @Author 郑勇浩
	 * @Data 2019/11/19 17:08
	 * @Param [response, param]
	 */
	void exportExcel(HttpServletResponse response, CommonAbstractDto param);
}
