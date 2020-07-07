package com.njwd.basedata.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.BankAccountDto;
import com.njwd.entity.basedata.excel.ExcelRequest;
import com.njwd.entity.basedata.excel.ExcelResult;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.basedata.vo.BankAccountVo;
import com.njwd.entity.platform.dto.BankDto;
import com.njwd.entity.platform.vo.SysAuxDataVo;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description 银行账户 controller.
 * @Date 2019-06-11 12:00
 * @Author 朱小明
 */
@RequestMapping("financeback/bankAccount")
public interface BankAccountApi {

	/**
	 * @return java.lang.String
	 * @Description 新增银行账号
	 *
	 * @Author 朱小明
	 * @Data 2019/6/20 17:37
	 * @Param [bto]
	 */
	@PostMapping("addBankAccount")
	Result<Long> addBankAccount(BankAccountDto bto);

	/**
	 * @return java.lang.String
	 * @Description 删除
	 * @Author 朱小明
	 * @Data 2019/7/3 16:29
	 * @Param [bankAccountDto]
	 */
	@PostMapping("deleteBankAccount")
	Result<Integer> deleteBankAccount(BankAccountDto bto);

	/**
	 * @return java.lang.String
	 * @Description 批量删除
	 * @Author 朱小明
	 * @Data 2019/6/20 17:37
	 * @Param [bankAccountDto]
	 */
	@PostMapping("deleteBankAccountBatch")
	Result<BatchResult> deleteBankAccountBatch(BankAccountDto bto);

	/**
	 * @return java.lang.String
	 * @Description 更新银行账号
	 * @Author 朱小明
	 * @Data 2019/6/20 17:37
	 * @Param [bto]
	 */
	@PostMapping("updateBankAccount")
	Result updateBankAccount(BankAccountDto bto);

	/**
	 * @return java.lang.String
	 * @Description 反禁用
	 * @Author 朱小明
	 * @Data 2019/7/3 16:29
	 * @Param [bankAccountDto]
	 */
	@PostMapping("enableBankAccount")
	Result<Integer> enableBankAccount(BankAccountDto bto);

	/**
	 * @return java.lang.String
	 * @Description 批量反禁用
	 * @Author 朱小明
	 * @Data 2019/6/20 17:37
	 * @Param [bankAccountDto]
	 */
	@PostMapping("enableBankAccountBatch")
	Result<BatchResult> enableBankAccountBatch(BankAccountDto bto);

	/**
	 * @return java.lang.String
	 * @Description 禁用
	 * @Author 朱小明
	 * @Data 2019/7/3 16:29
	 * @Param [bankAccountDto]
	 */
	@PostMapping("disableBankAccount")
	Result<Integer> disableBankAccount(BankAccountDto bto);

	/**
	 * @return java.lang.String
	 * @Description 批量禁用
	 * @Author 朱小明
	 * @Data 2019/6/20 17:37
	 * @Param [bankAccountDto]
	 */
	@PostMapping("disableBankAccountBatch")
	Result<BatchResult> disableBankAccountBatch(BankAccountDto bto);

	/**
	 * @return java.lang.String
	 * @Description 查询银行账号
	 * @Author 朱小明
	 * @Data 2019/6/20 17:37
	 * @Param [bankAccountDto]
	 */
	@RequestMapping("findBankAccount")
	Result<BankAccountVo> findBankAccount(BankAccountDto bankAccountDto);

	/**
	 * @return java.lang.String
	 * @Description 查询银行账号分页
	 * @Author 朱小明
	 * @Data 2019/6/20 17:37
	 * @Param [bankAccountDto]
	 */
	@RequestMapping("findBankAccountPage")
	Result<Page<BankAccountVo>> findBankAccountPage(BankAccountDto bankAccountDto);

	/**
	 * @return java.lang.String
	 * @Description 查询是否存在 账户名称或编码相同的账号
	 * @Author 朱小明
	 * @Data 2019/6/20 17:37
	 * @Param [bankAccountDto]
	 */
	@RequestMapping("findHasAccountOrName")
	Result<Boolean> findHasAccountOrName(BankAccountDto bankAccountDto);


	/**
	 * @return java.lang.String
	 * @Description 账户类型 辅助资料
	 * @Author 朱小明
	 * @Data 2019/6/26 15:58
	 * @Param []
	 */
	@RequestMapping("findAccType")
	Result<List<SysAuxDataVo>> findAccType();

	/**
	 * @return java.lang.String
	 * @Description 账户用途 辅助资料
	 * @Author 朱小明
	 * @Data 2019/6/26 15:58
	 * @Param []
	 */
	@RequestMapping("findAccUsage")
	Result<List<SysAuxDataVo>> findAccUsage();

	/**
	 * @return java.lang.String
	 * @Description 币种 辅助资料
	 * @Author 朱小明
	 * @Data 2019/7/1 9:17
	 * @Param []
	 */
	@RequestMapping("findAccCurrency")
	Result<List<SysAuxDataVo>> findAccCurrency();

	/**
	 * @return java.lang.String
	 * @Description 开户银行
	 * @Author 朱小明
	 * @Data 2019/7/1 9:17
	 * @Param []
	 */
	@RequestMapping("findBankPage")
	Result findBankPage(BankDto bankDto);

	/**
	 * @return java.lang.String
	 * @Description 查询核算主体分页
	 * @Author 朱小明
	 * @Data 2019/7/1 9:17
	 * @Param []
	 */
	@RequestMapping("findAccountBookEntityList")
	Result<Page<AccountBookEntityVo>> findAccountBookEntityList(AccountBookDto dto);

//	/**
//	 * @Description Excel 导出
//	 * @Author 朱小明
//	 * @Data 2019/6/20 17:38
//	 * @Param [bankAccountDto, response]
//	 */
//	@RequestMapping("exportExcel")
//	void exportExcel(BankAccountDto bankAccountDto, HttpServletResponse response);

	/**
	 * @return org.springframework.http.ResponseEntity
	 * @Description 下载银行账号的模板
	 * @Author 朱小明
	 * @Data 2019/6/20 17:38
	 * @Param []
	 */
	@RequestMapping("downloadTemplate")
	ResponseEntity downloadTemplate() throws Exception;

	/**
	 * @return java.lang.String
	 * @Description 上传并校验银行账号Excel
	 * @Author 朱小明
	 * @Data 2019/6/20 17:38
	 * @Param [file]
	 */
	@RequestMapping("uploadAndCheckExcel")
	Result<ExcelResult> uploadAndCheckExcel(MultipartFile file);

	/**
	 * @return java.lang.String
	 * @Description 导入Excel
	 * @Author 朱小明
	 * @Data 2019/7/8 10:21
	 * @Param [uuid]
	 */
	@RequestMapping("importExcel")
	Result<ExcelResult> importExcel(ExcelRequest request);

}
