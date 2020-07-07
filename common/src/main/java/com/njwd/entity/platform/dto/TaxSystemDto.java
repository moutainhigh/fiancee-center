package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.TaxSystemVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Description 税收制度 dto
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaxSystemDto extends TaxSystemVo {

	private Page<TaxSystemVo> page = new Page<>();

	/**
	 * 模糊搜索名称或编码
	 */
	private String codeOrName;

	/**
	 * 国家id列表
	 */
	private List<Long> areaIdList;

	/**
	 * 待处理列表
	 */
	private List<TaxSystemVo> changeList;

	/**
	 * 发布消息
	 */
	private MessageDto messageDto;

}
