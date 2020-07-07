package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.AreaDto;
import com.njwd.entity.platform.vo.AreaVo;
import com.njwd.platform.mapper.AreaMapper;
import com.njwd.platform.service.AreaService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 国际地区 service impl
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Service
public class AreaServiceImpl implements AreaService {

	@Resource
	private AreaMapper areaMapper;

	/**
	 * @Description 查询国家地区
	 * @Author 郑勇浩
	 * @Data 2019/11/19 10:56
	 * @Param [areaDto]
	 * @return com.njwd.entity.platform.vo.AreaVo
	 */
	@Override
	public AreaVo findArea(AreaDto param) {
		return areaMapper.findArea(param);
	}

	/**
	 * @Description 查询国家地区[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/19 10:56
	 * @Param [areaDto]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.AreaVo>
	 */
	@Override
	public Page<AreaVo> findAreaPage(AreaDto param) {
		Page<AreaVo> page = param.getPage();
		page = areaMapper.findAreaPage(page, param);
		return page;
	}

	/**
	 * @Description 查询国家地区[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/19 10:56
	 * @Param [areaDto]
	 * @return java.util.List<com.njwd.entity.platform.vo.AreaVo>
	 */
	@Override
	public List<AreaVo> findAreaList(AreaDto param) {
		return areaMapper.findAreaList(param);
	}
}
