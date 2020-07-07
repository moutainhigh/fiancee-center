package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.CommonAbstract;
import com.njwd.entity.basedata.dto.CommonAbstractDto;
import com.njwd.entity.basedata.vo.CommonAbstractVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/7/24
 */
public interface CommonAbstractMapper extends BaseMapper<CommonAbstract> {

	/**
	 * @description: 批量插入
	 * @param: [commonAbstractList]
	 * @return: int
	 * @author: xdy
	 * @create: 2019-08-14 10-14
	 */
	int addBatch(List<? extends CommonAbstract> commonAbstractList);

	/**
	 * @Description 查询常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/7/25 14:06
	 * @Param []
	 * @return com.njwd.ledger.entity.vo.CommonAbstractVo
	 */
	CommonAbstractVo findOne(@Param("commonAbstractDto") CommonAbstractDto commonAbstractDto);

	/**
	 * @Description 查询常用摘要分页
	 * @Author 郑勇浩
	 * @Data 2019/7/25 14:48
	 * @Param [page, commonAbstractDto]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.ledger.entity.vo.CommonAbstractVo>
	 */
	Page<CommonAbstractVo> findPage(Page<CommonAbstractVo> page, @Param("commonAbstractDto") CommonAbstractDto commonAbstractDto);

	/**
	 * @Description 查询是否存在重复
	 * @Author 郑勇浩
	 * @Data 2019/7/25 10:30
	 * @Param [commonAbstractDto]
	 * @return java.lang.Integer
	 */
	Integer findHasRepeat(@Param("commonAbstractDto") CommonAbstractDto commonAbstractDto);

	/**
	 * @Description 查询id的状态
	 * @Author 郑勇浩
	 * @Data 2019/7/25 11:06
	 * @Param [commonAbstractDto]
	 * @return com.njwd.ledger.entity.CommonAbstract
	 */
	CommonAbstract findStatusById(@Param("commonAbstractDto") CommonAbstractDto commonAbstractDto);

	/**
	 * @Description 查询id List的状态
	 * @Author 郑勇浩
	 * @Data 2019/7/25 13:38
	 * @Param [commonAbstractDto]
	 * @return java.util.List<com.njwd.ledger.entity.CommonAbstract>
	 */
	List<CommonAbstract> findStatusByIdList(@Param("commonAbstractDto") CommonAbstractDto commonAbstractDto);
	
	/**
	 * @description: 获取平台同步数据
	 * @param: [commonAbstractDto]
	 * @return: java.util.List<com.njwd.entity.basedata.vo.CommonAbstractVo> 
	 * @author: xdy        
	 * @create: 2019-11-14 10:00 
	 */
	List<CommonAbstractVo> findPlatformCommonAbstract(CommonAbstractDto commonAbstractDto);


}
