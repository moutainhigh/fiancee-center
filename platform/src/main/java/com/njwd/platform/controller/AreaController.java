package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.AreaDto;
import com.njwd.entity.platform.vo.AreaVo;
import com.njwd.platform.service.AreaService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 国家地区 controller
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@RestController
@RequestMapping("area")
public class AreaController extends BaseController {

	@Resource
	private AreaService areaService;


	/**
	 * @Description 查询国家地区
	 * @Author 郑勇浩
	 * @Data 2019/11/12 15:30
	 * @Param [param]
	 * @return com.njwd.support.Result<com.njwd.entity.platform.vo.AreaVo>
	 */
	@RequestMapping("findArea")
	public Result<AreaVo> findArea(@RequestBody AreaDto param) {
		FastUtils.checkParams(param.getId());
		return ok(areaService.findArea(param));
	}

	/**
	 * @Description 查询国家地区[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/19 11:01
	 * @Param [param]
	 * @return com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page < com.njwd.entity.platform.vo.AreaVo>>
	 */
	@RequestMapping("findAreaPage")
	public Result<Page<AreaVo>> findAreaPage(@RequestBody AreaDto param) {
		return ok(areaService.findAreaPage(param));
	}

	/**
	 * @Description 查询国家地区[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 14:33
	 * @Param [param]
	 * @return com.njwd.support.Result<java.util.List < com.njwd.entity.basedata.Area>>
	 */
	@RequestMapping("findAreaList")
	public Result<List<AreaVo>> findAreaList(@RequestBody AreaDto param) {
		return ok(areaService.findAreaList(param));
	}

}
