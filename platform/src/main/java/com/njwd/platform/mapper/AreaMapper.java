package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.Area;
import com.njwd.entity.platform.dto.AreaDto;
import com.njwd.entity.platform.vo.AreaVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description 国家地区 mapper
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
public interface AreaMapper extends BaseMapper<Area> {

	/**
	 * @Description 查询国家地区
	 * @Author 郑勇浩
	 * @Data 2019/11/19 10:27
	 * @Param [areaDto]
	 * @return com.njwd.entity.platform.vo.AreaVo
	 */
	AreaVo findArea(@Param("areaDto") AreaDto areaDto);

	/**
	 * @Description 查询国家地区[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 9:37
	 * @Param [page, areaDto]
	 * @return java.util.List<com.njwd.entity.platform.vo.AreaVo>
	 */
	Page<AreaVo> findAreaPage(Page<AreaVo> page, @Param("areaDto") AreaDto areaDto);

	/**
	 * @Description 查询国家地区[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 9:56
	 * @Param []
	 * @return java.util.List<com.njwd.entity.platform.vo.AreaVo>
	 */
	List<AreaVo> findAreaList(@Param("areaDto") AreaDto areaDto);

}
