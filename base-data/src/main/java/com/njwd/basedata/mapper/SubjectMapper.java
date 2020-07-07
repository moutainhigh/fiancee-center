package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.platform.Subject;
import com.njwd.entity.platform.vo.SubjectVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description 科目mapper
 * @Author 周鹏
 * @Date 2019/6/21
 */
public interface SubjectMapper extends BaseMapper<Subject> {
	/**
	 * 查询科目表信息
	 *
	 * @param subject
	 * @return int
	 * @author: 周鹏
	 * @create: 2019/7/26
	 */
	Subject findInfoByParam(@Param("subject") Subject subject);

	/**
	 * @description: 批量插入
	 * @param: [subjects]
	 * @return: int
	 * @author: xdy
	 * @create: 2019-08-12 16-01
	 */
	int addBatch(List<SubjectVo> subjectVoList);

}
