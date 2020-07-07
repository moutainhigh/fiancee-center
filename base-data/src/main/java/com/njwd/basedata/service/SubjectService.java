package com.njwd.basedata.service;

import com.njwd.entity.platform.Subject;
import com.njwd.entity.platform.dto.SubjectDto;

/**
 * @Description 科目表 service
 * @Date 2019/10/25 18:06
 * @Author 郑勇浩
 */
public interface SubjectService {

	/**
	 * @Description 查询租户科目信息
	 * @Author 郑勇浩
	 * @Data 2019/10/25 18:20
	 * @Param [subjectDto]
	 * @return com.njwd.entity.platform.vo.SubjectVo
	 */
	Subject findSubject(SubjectDto subjectDto);

}
