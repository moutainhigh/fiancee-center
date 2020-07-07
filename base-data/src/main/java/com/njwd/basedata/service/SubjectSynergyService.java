package com.njwd.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.SubjectSynergy;
import com.njwd.entity.platform.dto.SubjectSynergyDto;
import com.njwd.entity.platform.vo.SubjectSynergyVo;
import com.njwd.support.BatchResult;

/**
 * @author xyyxhcj@qq.com
 * @since 2019-10-28
 */

public interface SubjectSynergyService {
	/**
	 * 添加科目协同配置
	 *
	 * @param subjectSynergyDto subjectSynergyDto
	 * @param operator       operator
	 * @return java.lang.Long
	 * @author xyyxhcj@qq.com
	 * @date 2019/10/25 17:02
	 **/
	Long add(SubjectSynergyDto subjectSynergyDto, SysUserVo operator);

	/**
	 * 修改科目协同配置
	 *
	 * @param subjectSynergyDto subjectSynergyDto
	 * @param operator       operator
	 * @return java.lang.Long
	 * @author xyyxhcj@qq.com
	 * @date 2019/10/28 9:12
	 *
	 **/
	Long update(SubjectSynergyDto subjectSynergyDto, SysUserVo operator);

	/**
	 * 查详情
	 *
	 * @param subjectSynergy subjectSynergy
	 * @return com.njwd.entity.platform.vo.SubjectSynergyVo
	 * @author xyyxhcj@qq.com
	 * @date 2019/10/28 14:54
	 **/
	SubjectSynergyVo findDetail(SubjectSynergy subjectSynergy);

	/**
	 * 查询所有科目协同
	 * 刘遵通
	 * @param subjectSynergyDto
	 * @return
	 */
	Page<SubjectSynergyVo> findSubjectSynergyList(SubjectSynergyDto subjectSynergyDto);
	/**
	 * 删除
	 * 刘遵通
	 * @param subjectSynergyDto
	 * @return
	 */
	BatchResult deleteSubjectSynergy(SubjectSynergyDto subjectSynergyDto);
	/**
	 * 启用
	 * 刘遵通
	 * @param subjectSynergyDto
	 * @return
	 */
	BatchResult enableSubjectSynergy(SubjectSynergyDto subjectSynergyDto);
	/**
	 * 反启用
	 * 刘遵通
	 * @param subjectSynergyDto
	 * @return
	 */
	BatchResult reversalEnableSubjectSynergy(SubjectSynergyDto subjectSynergyDto);

}
