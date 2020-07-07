package com.njwd.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.CredentialWordDto;
import com.njwd.entity.platform.vo.CredentialWordVo;
import com.njwd.platform.service.CredentialWordService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 币种 controller
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@RestController
@RequestMapping("credentialWord")
public class CredentialWordController extends BaseController {

	@Resource
	private CredentialWordService credentialWordService;

	/**
	 * @Description 查询凭证字
	 * @Author 郑勇浩
	 * @Data 2019/11/19 11:22
	 * @Param [param]
	 * @return com.njwd.support.Result<com.njwd.entity.platform.vo.CredentialWordVo>
	 */
	@RequestMapping("findCredentialWord")
	public Result<CredentialWordVo> findCredentialWord(@RequestBody CredentialWordDto param) {
		return ok(credentialWordService.findCredentialWord(param));
	}

	/**
	 * @Description 查询凭证字[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 10:21
	 * @Param [param]
	 * @return com.njwd.support.Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page < com.njwd.entity.platform.vo.CredentialWordVo>>
	 */
	@RequestMapping("findCredentialWordPage")
	public Result<Page<CredentialWordVo>> findCredentialWordPage(@RequestBody CredentialWordDto param) {
		return ok(credentialWordService.findCredentialWordPage(param));
	}

	/**
	 * @Description 查询凭证字[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 10:37
	 * @Param [param]
	 * @return com.njwd.support.Result<java.util.List < com.njwd.entity.platform.vo.CredentialWordVo>>
	 */
	@RequestMapping("findCredentialWordList")
	public Result<List<CredentialWordVo>> findCredentialWordList(@RequestBody CredentialWordDto param) {
		return ok(credentialWordService.findCredentialWordList(param));
	}
}
