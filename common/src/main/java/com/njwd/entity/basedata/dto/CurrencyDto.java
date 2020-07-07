package com.njwd.entity.basedata.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.vo.CurrencyVo;
import com.njwd.entity.platform.dto.MessageDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Description 币种 dto
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CurrencyDto extends CurrencyVo {

	private Page<CurrencyVo> page = new Page<>();

	/**
	 * 添加列表
	 */
	private List<CurrencyVo> changeList;

	/**
	 * 模糊搜索名称或编码
	 */
	private String codeOrName;

	/**
	 * 平台主键集合，初始化使用
	 */
	private List<Long> platformIds;

	/**
	 * 是否被wd_accounting_standard和wd_tax_system使用
	 */
	private Byte used;

	/**
	 * 发布消息
	 */
	private MessageDto messageDto;

	/**
	 * 是否管理员 0:否 1:是
	 */
	private Byte isEnterpriseAdmin = Constant.Is.NO;

}
