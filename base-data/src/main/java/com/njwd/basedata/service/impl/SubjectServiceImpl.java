package com.njwd.basedata.service.impl;

import com.njwd.basedata.mapper.SubjectMapper;
import com.njwd.basedata.service.SubjectService;
import com.njwd.entity.platform.Subject;
import com.njwd.entity.platform.dto.SubjectDto;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Description 科目表 service Impl
 * @Date 2019/10/25 18:07
 * @Author 郑勇浩
 */
@Service
public class SubjectServiceImpl implements SubjectService {

	@Resource
	private SubjectMapper subjectMapper;

	/**
	 * @Description 查询租户科目信息
	 * @Author 郑勇浩
	 * @Data 2019/10/25 18:20
	 * @Param [subjectDto]
	 * @return com.njwd.entity.platform.vo.SubjectVo
	 */
	@Override
	public Subject findSubject(SubjectDto subjectDto) {
		return subjectMapper.findInfoByParam(subjectDto);
	}
}
