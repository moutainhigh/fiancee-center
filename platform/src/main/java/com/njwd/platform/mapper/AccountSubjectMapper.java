package com.njwd.platform.mapper;


import com.baomidou.mybatisplus.annotation.SqlParser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.AccountSubject;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.entity.platform.vo.FindAccountSubjectListVo;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * @Author liuxiang
 * @Description 更多科目模板
 * @Date:14:21 2019/6/19
 **/
public interface AccountSubjectMapper extends BaseMapper<AccountSubject> {

	/**
	 * @return java.util.List<com.njwd.platform.entity.vo.FindAccountSubjectListVo>
	 * @Description 更多科目模板分页查询
	 * @Author liuxiang
	 * @Date:15:26 2019/7/2
	 * @Param [accountSubjectDto, page]
	 **/
	Page<FindAccountSubjectListVo> findAccountSubjectPage(Page<FindAccountSubjectListVo> page, @Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);


	/**
	 * @return java.util.List<com.njwd.platform.entity.vo.FindAccountSubjectListVo>
	 * @Description 更多科目模板列表查询
	 * @Author liuxiang
	 * @Date:15:26 2019/7/2
	 * @Param [accountSubjectVo]
	 **/
	List<FindAccountSubjectListVo> findAccountSubjectList(AccountSubjectDto accountSubjectDto);

	/**
	 * 分页
	 *
	 * @param accountSubjectDto accountSubjectDto
	 * @param page              page
	 * @return java.util.List<com.njwd.entity.platform.vo.AccountSubjectVo>
	 * @author xyyxhcj@qq.com
	 * @date 2019/11/13 15:26
	 **/
	List<AccountSubjectVo> findPage(@Param("accountSubjectDto") AccountSubjectDto<AccountSubjectVo> accountSubjectDto, @Param("page") Page<AccountSubjectVo> page);

	/**
	 * 查询用于校验的数据
	 *
	 * @param ids ids
	 * @return java.util.List<com.njwd.entity.platform.vo.AccountSubjectVo>
	 * @author xyyxhcj@qq.com
	 * @date 2019/11/20 13:53
	 **/
	@SqlParser(filter = true)
	List<AccountSubjectVo> findByIdIn(@Param("ids") Collection<Long> ids);

	/**
	 * findDetail
	 *
	 * @param id id
	 * @return com.njwd.entity.platform.vo.AccountSubjectVo
	 * @author xyyxhcj@qq.com
	 * @date 2019/11/20 14:22
	 **/
	AccountSubjectVo findDetail(@Param("id") Long id);

	/**
	 * findList
	 *
	 * @param accountSubjectDto accountSubjectDto
	 * @return java.util.List<com.njwd.entity.platform.vo.AccountSubjectVo>
	 * @author xyyxhcj@qq.com
	 * @date 2019/11/20 15:37
	 **/
	List<AccountSubjectVo> findList(@Param("accountSubjectDto") AccountSubjectDto accountSubjectDto);

	/**
	 * 批量更新isFinal
	 *
	 * @param isFinal isFinal
	 * @param ids     ids
	 * @return int
	 * @author xyyxhcj@qq.com
	 * @date 2019/11/21 19:46
	 **/
	int updateIsFinalByIdIn(@Param("isFinal") Byte isFinal, @Param("ids") Collection<Long> ids);
}
