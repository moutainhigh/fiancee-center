package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.common.PlatformConstant;
import com.njwd.entity.platform.dto.CredentialWordDto;
import com.njwd.entity.platform.vo.CredentialWordVo;
import com.njwd.platform.mapper.CredentialWordMapper;
import com.njwd.platform.service.CredentialWordService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 凭证字 service impl
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
@Service
public class CredentialWordServiceImpl implements CredentialWordService {

	@Resource
	private CredentialWordMapper credentialWordMapper;

	/**
	 * @Description 查询凭证字
	 * @Author 郑勇浩
	 * @Data 2019/11/19 11:22
	 * @Param [param]
	 * @return com.njwd.entity.platform.vo.CredentialWordVo
	 */
	@Override
	@Cacheable(value = PlatformConstant.RedisCache.CREDENTIAL_WORD, key = "#param.id", unless = "#result == null")
	public CredentialWordVo findCredentialWord(CredentialWordDto param) {
		return credentialWordMapper.findCredentialWord(param);
	}

	/**
	 * @Description 查询凭证字[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 10:17
	 * @Param [credentialWordDto]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.CredentialWordVo>
	 */
	@Override
	public Page<CredentialWordVo> findCredentialWordPage(CredentialWordDto param) {
		Page<CredentialWordVo> page = param.getPage();
		page = credentialWordMapper.findCredentialWordPage(page, param);
		return page;
	}

	/**
	 * @Description 查询凭证字[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 10:36
	 * @Param [param]
	 * @return java.util.List<com.njwd.entity.platform.vo.CredentialWordVo>
	 */
	@Override
	public List<CredentialWordVo> findCredentialWordList(CredentialWordDto param) {
		return credentialWordMapper.findCredentialWordList(param);
	}

}
