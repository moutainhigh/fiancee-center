package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.CommonAbstractVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Description 常用摘要 dto
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommonAbstractDto extends CommonAbstractVo {

	private Page<CommonAbstractVo> page = new Page<>();

	/**
	 * 待处理列表
	 */
	private List<CommonAbstractVo> changeList;

	/**
	 * 模糊搜索名称或编码
	 */
	private String codeOrName;

	/**
	 * 平台主键
	 */
	private List<Long> platformIds;

	/**
	 * 发布消息
	 */
	private MessageDto messageDto;

}
