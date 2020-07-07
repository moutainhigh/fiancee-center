package com.njwd.basedata.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.dto.BankAccountDto;
import com.njwd.entity.basedata.vo.BankAccountVo;
import com.njwd.entity.platform.dto.BankDto;
import com.njwd.entity.platform.vo.BankVo;
import com.njwd.entity.platform.vo.SysAuxDataVo;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description 银行账户 service.
 * @Date 2019-06-11 12:00
 * @Author 郑勇浩
 */
public interface BankAccountService {

	/**
	 * @Description 新增银行账号
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:34
	 * @Param [bankAccountDto]
	 * @return int
	 */
	int addBankAccount(BankAccountDto bankAccountDto);

	/**
	 * @Description 更新银行账号
	 * @Author 郑勇浩
	 * @Data 2019/7/3 16:00
	 * @Param [bankAccountDto]
	 * @return int
	 */
	BatchResult deleteBankAccount(BankAccountDto bankAccountDto);

	/**
	 * @Description 批量删除
	 * @Author 郑勇浩
	 * @Data 2019/7/2 14:41
	 * @Param []
	 * @return int
	 */
	BatchResult deleteBankAccountBatch(BankAccountDto bankAccountDto);

	/**
	 * @Description 更新银行账号
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:36
	 * @Param [bankAccountDto]
	 * @return int
	 */
	long updateBankAccount(BankAccountDto bankAccountDto);

	/**
	 * @Description 反禁用
	 * @Author 郑勇浩
	 * @Data 2019/7/3 16:00
	 * @Param [bankAccountDto]
	 * @return int
	 */
	BatchResult enableBankAccount(BankAccountDto bankAccountDto);

	/**
	 * @Description 批量反禁用
	 * @Author 郑勇浩
	 * @Data 2019/7/2 14:41
	 * @Param []
	 * @return int
	 */
	BatchResult enableBankAccountBatch(BankAccountDto bankAccountDto);

	/**
	 * @Description 禁用
	 * @Author 郑勇浩
	 * @Data 2019/7/3 16:00
	 * @Param [bankAccountDto]
	 * @return int
	 */
	BatchResult disableBankAccount(BankAccountDto bankAccountDto);

	/**
	 * @Description 批量禁用
	 * @Author 郑勇浩
	 * @Data 2019/7/2 14:41
	 * @Param []
	 * @return int
	 */
	BatchResult disableBankAccountBatch(BankAccountDto bankAccountDto);

	/**
	 * @Description 查询银行账号
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:34
	 * @Param [bankAccountDto]
	 * @return com.njwd.entity.basedata.vo.BankAccountVo
	 */
	BankAccountVo findBankAccount(BankAccountDto bankAccountDto);

	/**
	 * @Description 查询银行账号分页
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:34
	 * @Param [bankAccountDto]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.BankAccountVo>
	 */
	Page<BankAccountVo> findBankAccountPage(BankAccountDto bankAccountDto);

	/**
	 * @Description 查询是否存在 账户名称或编码相同的账号(COUNT)
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:36
	 * @Param [bankAccountDto]
	 * @return boolean
	 */
	boolean findHasAccountOrName(BankAccountDto bankAccountDto);

	/**
	 * @Description 查询账户类型 辅助资料
	 * @Author 郑勇浩
	 * @Data 2019/6/26 15:55
	 * @Param []
	 * @return java.util.List<com.njwd.entity.basedata.SysAuxData>
	 */
	List<SysAuxDataVo> findAccType();

	/**
	 * @Description 查询账户用途 辅助资料
	 * @Author 郑勇浩
	 * @Data 2019/6/26 15:55
	 * @Param []
	 * @return java.util.List<com.njwd.entity.basedata.SysAuxData>
	 */
	List<SysAuxDataVo> findAccUsage();

	/**
	 * @Description 查询币种 辅助资料
	 * @Author 郑勇浩
	 * @Data 2019/7/1 9:16
	 * @Param []
	 * @return java.util.List<com.njwd.entity.basedata.SysAuxData>
	 */
	List<SysAuxDataVo> findAccCurrency();

	/**
	 * @Description 查询开户行 分页
	 * @Author 郑勇浩
	 * @Data 2019/7/3 14:46
	 * @Param [platformBankDto]
	 * @return java.lang.String
	 */
	Result<Page<BankVo>> findBankPage(BankDto bankDto);

	/**
	 * @Description Excel 导出
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:36
	 * @Param [bankAccountDto, responses]
	 */
	void exportExcel(BankAccountDto bankAccountDto, HttpServletResponse response);

}
