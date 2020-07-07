package com.njwd.entity.ledger.dto;

import com.njwd.entity.ledger.vo.ParameterSetSubVo;
import com.njwd.entity.ledger.vo.ParameterSetVo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 参数设置 Dto
 * @Date 2019/10/16 15:18
 * @Author 郑勇浩
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ParameterSetSubDto extends ParameterSetSubVo {

	/**
	 * 参数设置列表(主从表融合)
	 */
	private List<ParameterSetVo> insertList = new ArrayList<>();

	/**
	 * 已存在的数据
	 */
	private List<ParameterSetVo> updateList = new ArrayList<>();


}
