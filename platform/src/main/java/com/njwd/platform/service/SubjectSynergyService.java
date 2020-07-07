package com.njwd.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.SubjectSynergy;
import com.njwd.entity.platform.dto.SubjectSynergyDto;
import com.njwd.entity.platform.vo.SubjectSynergyVo;
import com.njwd.support.BatchResult;

/**
 * @author xyyxhcj@qq.com
 * @since 2019-10-25
 */

public interface SubjectSynergyService {
	/**
	 * 添加科目协同配置
	 *
	 * @param subjectSynergy subjectSynergy
	 * @return java.lang.Long
	 * @author xyyxhcj@qq.com
	 * @date 2019/10/25 17:02
	 **/
	Long add(SubjectSynergy subjectSynergy);

	/**
	 * 修改科目协同配置
	 *
	 * @param subjectSynergy subjectSynergy
	 * @return java.lang.Long
	 * @author xyyxhcj@qq.com
	 * @date 2019/10/28 9:12
	 *
	 **/
	Long update(SubjectSynergy subjectSynergy);

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
	 * 审核
	 * 刘遵通
	 * @param subjectSynergyDto
	 * @return
	 */
	BatchResult checkApprove(SubjectSynergyDto subjectSynergyDto);

	/**
	 * 反审核
	 * 刘遵通
	 * @param subjectSynergyDto
	 * @return
	 */
	BatchResult reversalApprove(SubjectSynergyDto subjectSynergyDto);

	/**
	 * 发布
	 * 刘遵通
	 * @param subjectSynergyDto
	 * @return
	 */
	BatchResult release(SubjectSynergyDto subjectSynergyDto);
}
