package com.njwd.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.dto.CommonAbstractDto;
import com.njwd.entity.basedata.vo.CommonAbstractVo;
import com.njwd.support.BatchResult;

import java.util.List;

/**
 * @Description 常用摘要 service.
 * @Date 2019/7/25 9:34
 * @Author 郑勇浩
 */
public interface CommonAbstractService {

	/**
	 * @Description 新增常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/7/25 9:49
	 * @Param [commonAbstractDto]
	 * @return int
	 */
	int addCommonAbstract(CommonAbstractDto commonAbstractDto);

	/**
	 * @Description 删除常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/7/25 11:02
	 * @Param [bto]
	 * @return int
	 */
	int deleteCommonAbstract(CommonAbstractDto commonAbstractDto);

	/**
	 * @Description 批量删除
	 * @Author 郑勇浩
	 * @Data 2019/7/25 11:53
	 * @Param [commonAbstractDto]
	 * @return com.njwd.system.support.BatchResult
	 */
	BatchResult deleteCommonAbstractBatch(CommonAbstractDto commonAbstractDto);

	/**
	 * @Description updateCommonAbstract
	 * @Author 郑勇浩
	 * @Data 2019/7/25 11:31
	 * @Param [commonAbstractDto]
	 * @return int
	 */
	long updateCommonAbstract(CommonAbstractDto commonAbstractDto);

	/**
	 * @Description 查询常用摘要
	 * @Author 郑勇浩
	 * @Data 2019/7/25 14:04
	 * @Param [commonAbstractDto]
	 * @return com.njwd.ledger.entity.vo.CommonAbstractVo
	 */
	CommonAbstractVo findCommonAbstract(CommonAbstractDto commonAbstractDto);

	/**
	 * @Description 查询常用摘要分页
	 * @Author 郑勇浩
	 * @Data 2019/7/25 14:50
	 * @Param [commonAbstractDto]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.ledger.entity.vo.CommonAbstractVo>
	 */
	Page<CommonAbstractVo> findCommonAbstractPage(CommonAbstractDto commonAbstractDto);
	
	/**
	 * @description: 获取待引用的常用摘要
	 * @param: [commonAbstractDto]
	 * @return: java.util.List<com.njwd.entity.basedata.vo.CommonAbstractVo> 
	 * @author: xdy        
	 * @create: 2019-11-14 09:45 
	 */
    List<CommonAbstractVo> findBringInCommonAbstract(CommonAbstractDto commonAbstractDto);
}
