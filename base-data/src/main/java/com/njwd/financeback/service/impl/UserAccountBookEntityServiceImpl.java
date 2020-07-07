package com.njwd.financeback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.UserAccountBookEntity;
import com.njwd.entity.basedata.dto.UserAccountBookEntityDto;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.financeback.mapper.UserAccountBookEntityMapper;
import com.njwd.financeback.service.UserAccountBookEntityService;
import com.njwd.utils.UserUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/07/03
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserAccountBookEntityServiceImpl implements UserAccountBookEntityService {
	@Resource
	private UserAccountBookEntityMapper userAccountBookEntityMapper;

	@Override
	public void updateBySelf(UserAccountBookEntityDto userAccountBookEntityDto) {
		// 先将其他主体设为非默认
		UserAccountBookEntity bookEntity = new UserAccountBookEntity();
		bookEntity.setIsDefault(Constant.Is.NO);
		UserAccountBookEntity bookEntityQuery = new UserAccountBookEntity();
		bookEntityQuery.setCompanyId(userAccountBookEntityDto.getCompanyId());
		SysUserVo operator = UserUtils.getUserVo();
		bookEntityQuery.setRootEnterpriseId(operator.getRootEnterpriseId());
		bookEntityQuery.setUserId(operator.getUserId());
		userAccountBookEntityMapper.update(bookEntity, new QueryWrapper<>(bookEntityQuery).ne(Constant.ColumnName.ACCOUNT_BOOK_ENTITY_ID, userAccountBookEntityDto.getAccountBookEntityId()));
		bookEntity.setIsDefault(Constant.Is.YES);
		bookEntityQuery.setAccountBookEntityId(userAccountBookEntityDto.getAccountBookEntityId());
		userAccountBookEntityMapper.update(bookEntity, new QueryWrapper<>(bookEntityQuery));
	}
}
