package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.CommonAbstract;
import com.njwd.entity.platform.dto.CommonAbstractDto;
import com.njwd.entity.platform.vo.CommonAbstractVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description 常用摘要 mapper
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
public interface CommonAbstractMapper extends BaseMapper<CommonAbstract> {

	/**
	 * @Description 查询常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/11/12 15:25
	 * @Param [commonAbstractDto]
	 * @return com.njwd.entity.platform.vo.CommonAbstractVo
	 */
	CommonAbstractVo findCommonAbstract(@Param("commonAbstractDto") CommonAbstractDto commonAbstractDto);

	/**
	 * @Description 查询是否存在常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/11/12 15:25
	 * @Param [commonAbstractDto]
	 * @return com.njwd.entity.platform.vo.CommonAbstractVo
	 */
	CommonAbstractVo findExistCommonAbstract(@Param("commonAbstractDto") CommonAbstractDto commonAbstractDto);

	/**
	 * @Description 查询常用摘要[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 9:37
	 * @Param [page, currencyDto]
	 * @return java.util.List<com.njwd.entity.platform.vo.CurrencyVo>
	 */
	Page<CommonAbstractVo> findCommonAbstractPage(Page<CommonAbstractVo> page, @Param("commonAbstractDto") CommonAbstractDto commonAbstractDto);

	/**
	 * @Description 查询常用摘要[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 14:31
	 * @Param [commonAbstractDto]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.CommonAbstractVo>
	 */
	List<CommonAbstractVo> findCommonAbstractList(@Param("commonAbstractDto") CommonAbstractDto commonAbstractDto);

	/**
	 * @Description 查询常用摘要[列表]状态
	 * @Author 郑勇浩
	 * @Data 2019/11/12 17:02
	 * @Param [commonAbstractDto]
	 * @return java.util.List<com.njwd.entity.platform.vo.CommonAbstractVo>
	 */
	List<CommonAbstractVo> findCommonAbstractListStatus(@Param("commonAbstractDto") CommonAbstractDto commonAbstractDto);

	/**
	 * @Description 批量修改状态
	 * @Author 郑勇浩
	 * @Data 2019/11/15 16:25
	 * @Param [commonAbstractDto]
	 * @return int
	 */
	int updateStatusBatch(@Param("commonAbstractDto") CommonAbstractDto commonAbstractDto);
}
