package com.njwd.basedata.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.dto.CommonAbstractDto;
import com.njwd.entity.basedata.vo.CommonAbstractVo;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Description 常用摘要 controller.
 * @Date 2019/8/8 13:36
 * @Author 郑勇浩
 */
@RequestMapping("commonAbstract")
public interface CommonAbstractApi {

	/**
	 * @Description 新增常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/7/25 10:08
	 * @Param [bto]
	 * @return java.lang.String
	 */
	@PostMapping("addCommonAbstract")
	Result<Long> addCommonAbstract(CommonAbstractDto dto);

	/**
	 * @Description 删除常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/7/25 10:58
	 * @Param [bto]
	 * @return java.lang.String
	 */
	@PostMapping("deleteCommonAbstract")
	Result<Integer> deleteCommonAbstract(CommonAbstractDto dto);

	/**
	 * @Description 批量删除
	 * @Author 郑勇浩
	 * @Data 2019/7/25 11:52
	 * @Param [dto]
	 * @return java.lang.String
	 */
	@PostMapping("deleteCommonAbstractBatch")
	Result<BatchResult> deleteCommonAbstractBatch(CommonAbstractDto dto);

	/**
	 * @Description 更新常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/7/25 11:25
	 * @Param [CommonAbstractDto]
	 * @return java.lang.String
	 */
	@PostMapping("updateCommonAbstract")
	Result updateCommonAbstract(CommonAbstractDto dto);

	/**
	 * @Description 查询常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/7/25 14:28
	 * @Param [CommonAbstractDto]
	 * @return java.lang.String
	 */
	@RequestMapping("findCommonAbstract")
	Result<CommonAbstractVo> findCommonAbstract(CommonAbstractDto dto);

	/**
	 * @Description 查询常用摘要分页
	 * @Author 郑勇浩
	 * @Data 2019/7/25 14:28
	 * @Param [CommonAbstractDto]
	 * @return java.lang.String
	 */
	@RequestMapping("findCommonAbstractPage")
	Result<Page<CommonAbstractVo>> findCommonAbstractPage(CommonAbstractDto dto);
}
