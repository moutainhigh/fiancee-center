package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.CredentialWord;
import com.njwd.entity.platform.dto.CredentialWordDto;
import com.njwd.entity.platform.vo.CredentialWordVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description 凭证字 mapper
 * @Date 2019/11/12 9:21
 * @Author 郑勇浩
 */
public interface CredentialWordMapper extends BaseMapper<CredentialWord> {

	/**
	 * @Description 查询凭证字
	 * @Author 郑勇浩
	 * @Data 2019/11/19 11:21
	 * @Param [credentialWordDto]
	 * @return com.njwd.entity.platform.vo.CredentialWordVo
	 */
	CredentialWordVo findCredentialWord(@Param("credentialWordDto") CredentialWordDto credentialWordDto);

	/**
	 * @Description 查询凭证字[分页]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 9:37
	 * @Param [page, currencyDto]
	 * @return java.util.List<com.njwd.entity.platform.vo.CurrencyVo>
	 */
	Page<CredentialWordVo> findCredentialWordPage(Page<CredentialWordVo> page, @Param("credentialWordDto") CredentialWordDto credentialWordDto);

	/**
	 * @Description 查询凭证字[列表]
	 * @Author 郑勇浩
	 * @Data 2019/11/12 10:34
	 * @Param [credentialWordDto]
	 * @return java.util.List<com.njwd.entity.platform.vo.CredentialWordVo>
	 */
	List<CredentialWordVo> findCredentialWordList(@Param("credentialWordDto") CredentialWordDto credentialWordDto);

}
