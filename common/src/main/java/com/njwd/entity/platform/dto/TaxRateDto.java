package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.TaxRateVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Description 税率 dto
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaxRateDto extends TaxRateVo {

	private Page<TaxRateVo> page = new Page<>();

	/**
	 * 模糊搜索名称或编码
	 */
	private String codeOrName;

	/**
	 * 税制列表
	 */
	private List<Long> taxSystemIdList;

	/**
	 * 税种列表
	 */
	private List<Long> taxCategoryIdList;

	/**
	 * 待处理列表
	 */
	private List<TaxRateVo> changeList;

	/**
	 * 发布消息
	 */
	private MessageDto messageDto;

}
