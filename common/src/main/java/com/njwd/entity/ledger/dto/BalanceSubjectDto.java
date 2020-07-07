package com.njwd.entity.ledger.dto;

import com.njwd.entity.ledger.vo.BalanceSubjectVo;
import lombok.Getter;
import lombok.Setter;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/08/13
 */
@Getter
@Setter
public class BalanceSubjectDto extends BalanceSubjectVo {
	/**
	 * 是否更新已过账数据 1是0否
	 **/
	private byte isPost;

	/**
	 * 是否更新损益数据 1是0否
	 **/
	private byte isSy;
}
