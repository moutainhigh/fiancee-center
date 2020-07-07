package com.njwd.basedata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.BankAccount;
import com.njwd.entity.basedata.dto.BankAccountDto;
import com.njwd.entity.basedata.vo.BankAccountVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 银行账户 Mapper.
 *
 * @className: BankAccountServiceImpl
 * @author: 郑勇浩
 * @date: 2019-06-11 12:00
 */
public interface BankAccountMapper extends BaseMapper<BankAccount> {

	/**
	 * @Description 查询银行账户
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:39
	 * @Param [bankAccountDto]
	 * @return com.njwd.entity.basedata.vo.BankAccountVo
	 */
	BankAccountVo findOne(@Param("BankAccountDto") BankAccountDto bankAccountDto);


	/**
	 * @Description 查询银行账户 上 下 首 末
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:39
	 * @Param [bankAccountDto]
	 * @return com.njwd.entity.basedata.vo.BankAccountVo
	 */
	List<Long> findPreviousNextId(@Param("BankAccountDto") BankAccountDto bankAccountDto);

	/**
	 * @Description 查询是否存在 账户名称或编码相同的账号
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:39
	 * @Param [bankAccountDto]
	 * @return com.njwd.entity.basedata.BankAccount
	 */
	BankAccount findHasOne(@Param("BankAccountDto") BankAccountDto bankAccountDto);

	/**
	 * @Description 查询禁用状态删除状态
	 * @Author 郑勇浩
	 * @Data 2019/7/3 16:12
	 * @Param [bankAccountDto]
	 * @return com.njwd.entity.basedata.BankAccount
	 */
	BankAccount findEnableAndDel(@Param("BankAccountDto") BankAccountDto bankAccountDto);

	/**
	 * @Description 查询禁用状态删除状态多个id
	 * @Author 郑勇浩
	 * @Data 2019/7/4 10:07
	 * @Param [bankAccountDto]
	 * @return com.njwd.entity.basedata.BankAccount
	 */
	List<BankAccount> findEnableAndDelInIds(@Param("BankAccountDto") BankAccountDto bankAccountDto);

	/**
	 * @Description 查询是否存在 账户名称或编码相同的账号
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:39
	 * @Param [bankAccountDto]
	 * @return int
	 */
	int findHasAccountOrName(@Param("BankAccountDto") BankAccountDto bankAccountDto);

	/**
	 * @Description 查询银行账户分页
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:39
	 * @Param [bankAccountDto, page]
	 * @return java.util.List<com.njwd.entity.basedata.vo.BankAccountVo>
	 */
	Page<BankAccountVo> findPage(Page<BankAccountVo> page, @Param("BankAccountDto") BankAccountDto bankAccountDto);

	/**
	 * @Description 批量删除
	 * @Author 郑勇浩
	 * @Data 2019/9/21 16:40
	 * @Param [bankAccountDto]
	 * @return int
	 */
	int deleteBatch(@Param("BankAccountDto") BankAccountDto bankAccountDto);
}
