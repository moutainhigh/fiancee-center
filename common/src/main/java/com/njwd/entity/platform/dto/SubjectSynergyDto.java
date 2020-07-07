package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.SubjectSynergyVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/10/25
 */
@Getter
@Setter
public class SubjectSynergyDto extends SubjectSynergyVo {
    /** 用于分页 **/
	private Page<SubjectSynergyVo> page = new Page<>();

	/**
	 * 会计科目id集合 用于查询列表
	 */
	private List<Long> subjectIds;
	/**
	 * 公司间协同id集合 用于删除
	 */
	private List<SubjectSynergyDto> editList;

	private Integer status;

	/**
	 * 发布消息
	 */
	private MessageDto messageDto;

	@Override
	public Integer getPeriodYearNum() {
		if (getPeriodYear() != null && getPeriodNum() != null) {
			return getPeriodYear() * 100 + getPeriodNum();
		} else {
			return super.getPeriodYearNum();
		}
	}
}
