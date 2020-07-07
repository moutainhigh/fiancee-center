package com.njwd.financeback.service;

import com.njwd.entity.basedata.dto.UserAccountBookEntityDto;

/**
 * @author xyyxhcj@qq.com
 * @since 2019-07-03
 */

public interface UserAccountBookEntityService {
	/**
	 * 修改默认核算主体
	 * @param userAccountBookEntityDto company_id+account_book_entity_id
	 */
	void updateBySelf(UserAccountBookEntityDto userAccountBookEntityDto);
}
