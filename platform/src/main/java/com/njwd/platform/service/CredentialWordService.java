package com.njwd.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.CredentialWordDto;
import com.njwd.entity.platform.vo.CredentialWordVo;

import java.util.List;

/**
 * @Description 凭证字 service
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
public interface CredentialWordService {

	/**
	 * @Description 查询凭证字
	 * @Author 郑勇浩
	 * @Data 2019/11/19 11:21
	 * @Param [credentialWordDto]
	 * @return com.njwd.entity.platform.vo.CredentialWordVo
	 */
	CredentialWordVo findCredentialWord(CredentialWordDto credentialWordDto);

	/**
	 * @Description 查询凭证字[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 9:44
	 * @Param [currencyDto]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.CurrencyVo>
	 */
	Page<CredentialWordVo> findCredentialWordPage(CredentialWordDto credentialWordDto);

	/**
	 * @Description 查询凭证字[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 10:35
	 * @Param [credentialWordDto]
	 * @return java.util.List<com.njwd.entity.platform.vo.CredentialWordVo>
	 */
	List<CredentialWordVo> findCredentialWordList(CredentialWordDto credentialWordDto);
}
