package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.AreaVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Description 国家地区 dto
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AreaDto extends AreaVo {

	private Page<AreaVo> page = new Page<>();

	/**
	 * 待处理列表
	 */
	private List<AreaVo> changeList;

	/**
	 * 模糊搜索名称或编码
	 */
	private String codeOrName;

}
