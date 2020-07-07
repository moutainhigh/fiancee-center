package com.njwd.basedata.controller;

import com.njwd.support.BaseController;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 测试调用接口
 * @Date 2019/8/27 17:34
 * @Author 郑勇浩
 */
@RestController
@RequestMapping("connect")
public class ConnectTestController extends BaseController {

	@GetMapping("main")
	public Result main() {
		return ok();
	}
}
