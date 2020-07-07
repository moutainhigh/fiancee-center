package com.njwd.financeback.controller;

import com.njwd.entity.basedata.dto.UserAccountBookEntityDto;
import com.njwd.financeback.service.UserAccountBookEntityService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户配置核算主体 前端控制器
 *
 * @author xyyxhcj@qq.com
 * @since 2019/07/03
 */
@RestController
@RequestMapping("userAccountBookEntity")
public class UserAccountBookEntityController extends BaseController {
	@Resource
	private UserAccountBookEntityService userAccountBookEntityService;
	@PostMapping("updateBySelf")
	public Result updateBySelf(@RequestBody UserAccountBookEntityDto userAccountBookEntityDto) {
		userAccountBookEntityService.updateBySelf(userAccountBookEntityDto);
		return ok(true);
	}
}
