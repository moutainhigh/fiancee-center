package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.SubjectSynergy;
import com.njwd.entity.platform.dto.SubjectSynergyDto;
import com.njwd.entity.platform.vo.SubjectSynergyVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/10/25
 */
public interface SubjectSynergyMapper extends BaseMapper<SubjectSynergy> {
	/**
	 * 查详情
	 *
	 * @param id id
	 * @return com.njwd.entity.platform.vo.SubjectSynergyVo
	 * @author xyyxhcj@qq.com
	 * @date 2019/10/28 14:55
	 **/
	SubjectSynergyVo findById(@Param("id") Long id);

	/**
	 * 查询所有科目协同
	 * 刘遵通
	 * @param page
	 * @param subjectSynergyDto
	 * @return
	 */
	Page<SubjectSynergyVo> findSubjectSynergyList(@Param("page") Page<SubjectSynergyVo> page, @Param("subjectSynergyDto") SubjectSynergyDto subjectSynergyDto);

	/**
	 * 根据id查询出多条数据
	 * @param subjectSynergyDto
	 * @return
	 */
	List<SubjectSynergyVo> findSubjectSynergyListById(@Param("subjectSynergyDto") SubjectSynergyDto subjectSynergyDto);

	int updateOrdeleteSubjectSynergy(@Param("subjectSynergyDto") SubjectSynergyDto subjectSynergyDto,@Param("subjectSynergyList") List<SubjectSynergyVo> subjectSynergyList);

}
