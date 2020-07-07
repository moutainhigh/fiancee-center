package com.njwd.basedata.api;

import com.njwd.entity.platform.Subject;
import com.njwd.entity.platform.dto.SubjectDto;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Description 科目表 api
 * @Date 2019/10/25 18:06
 * @Author 郑勇浩
 */
@RequestMapping("financeback/subject")
public interface SubjectApi {

	/**
	 * @Description 查询租户科目信息
	 * @Author 郑勇浩
	 * @Data 2019/10/25 18:20
	 * @Param [subjectDto]
	 * @return com.njwd.entity.platform.vo.SubjectVo
	 */
	@RequestMapping("findSubject")
	Result<Subject> findSubject(@RequestBody SubjectDto subjectDto);
}
