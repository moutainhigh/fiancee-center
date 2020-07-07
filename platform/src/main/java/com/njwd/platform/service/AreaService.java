package com.njwd.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.AreaDto;
import com.njwd.entity.platform.vo.AreaVo;

import java.util.List;

/**
 * @Description 国家地区 service
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
public interface AreaService {

	/**
	 * @Description 查询国家地区
	 * @Author 郑勇浩
	 * @Data 2019/11/19 10:56
	 * @Param [areaDto]
	 * @return com.njwd.entity.platform.vo.AreaVo
	 */
	AreaVo findArea(AreaDto areaDto);

	/**
	 * @Description 查询国家地区[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/19 10:56
	 * @Param [areaDto]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.AreaVo>
	 */
	Page<AreaVo> findAreaPage(AreaDto areaDto);

	/**
	 * @Description 查询国家地区[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/19 10:56
	 * @Param [areaDto]
	 * @return java.util.List<com.njwd.entity.platform.vo.AreaVo>
	 */
	List<AreaVo> findAreaList(AreaDto areaDto);

}
