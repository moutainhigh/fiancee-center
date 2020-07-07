package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.TaxCategoryVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Description 税种 dto
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaxCategoryDto extends TaxCategoryVo {

	private Page<TaxCategoryVo> page = new Page<>();

	/**
	 * 模糊搜索名称或编码
	 */
	private String codeOrName;

	/**
	 * 税制id列表
	 */
	private List<Long> taxSystemIdList;

	/**
	 * 待处理列表
	 */
	private List<TaxCategoryVo> changeList;

	/**
	 * 平台主键集合
	 */
	private List<Long> platformIds;

	/**
	 * 发布消息
	 */
	private MessageDto messageDto;

}
