package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.CredentialWordVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description 凭证字 dto
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CredentialWordDto extends CredentialWordVo {

	private Page<CredentialWordVo> page = new Page<>();

	/**
	 * 模糊搜索名称或编码
	 */
	private String codeOrName;
	
	/**
	 * 发布消息
	 */
	private MessageDto messageDto;

}
