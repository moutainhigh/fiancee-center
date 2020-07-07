package com.njwd.basedata.controller;

import com.njwd.basedata.service.SubjectService;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.Subject;
import com.njwd.entity.platform.dto.SubjectDto;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.UserUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Description 科目表 controller
 * @Date 2019/10/25 18:06
 * @Author 郑勇浩
 */
@RestController
@RequestMapping("subject")
public class SubjectController extends BaseController {

	@Resource
	private SubjectService subjectservice;

	/**
	 * @Description 查询租户科目信息
	 * @Author 郑勇浩
	 * @Data 2019/10/25 18:20
	 * @Param [subjectDto]
	 * @return com.njwd.entity.platform.vo.SubjectVo
	 */
	@RequestMapping("findSubject")
	public Result<Subject> findSubject(@RequestBody SubjectDto subjectDto) {
		//必填
		FastUtils.checkParams(subjectDto.getId());
		SysUserVo operator = UserUtils.getUserVo();
		subjectDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		//查询
		return ok(subjectservice.findSubject(subjectDto));
	}


}
