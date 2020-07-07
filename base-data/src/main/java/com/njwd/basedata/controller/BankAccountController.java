package com.njwd.basedata.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.service.BankAccountService;
import com.njwd.common.Constant;
import com.njwd.common.LogConstant;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.dto.BankAccountDto;
import com.njwd.entity.basedata.excel.ExcelRequest;
import com.njwd.entity.basedata.excel.ExcelResult;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import com.njwd.entity.basedata.vo.BankAccountVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.dto.BankDto;
import com.njwd.entity.platform.vo.BankVo;
import com.njwd.entity.platform.vo.SysAuxDataVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.financeback.service.AccountBookEntityService;
import com.njwd.logger.SenderService;
import com.njwd.service.FileService;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.BaseController;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.HttpUtils;
import com.njwd.utils.ShiroUtils;
import com.njwd.utils.UserUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @Description 银行账户 controller.
 * @Date 2019-06-11 12:00
 * @Author 郑勇浩
 */
@RestController
@RequestMapping("bankAccount")
public class BankAccountController extends BaseController {

	@Resource
	private BankAccountService bankAccountService;
	@Resource
	private AccountBookEntityService accountBookEntityService;
	@Resource
	private SenderService senderService;
	@Resource
	private FileService fileService;
	@Resource
	private ReferenceRelationService referenceRelationService;

	/**
	 * @Description 新增银行账号
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:37
	 * @Param [bto]
	 * @return java.lang.String
	 */
	@PostMapping("addBankAccount")
	public Result<Long> addBankAccount(@RequestBody BankAccountDto bto) {
		//必填
		FastUtils.checkParams(bto.getCompanyId(), bto.getDepositBankId(), bto.getDepositBankName(), bto.getBusinessUnitId(), bto.getAccount(), bto.getName(), bto.getAccType(), bto.getAccTypeName(), bto.getAccUsage(), bto.getAccUsageName(), bto.getBankAccountCurrencies());

		//INSERT 账号信息(银行账号，账户名称租户内唯一)
		if (bankAccountService.addBankAccount(bto) > 0) {
			senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.bankAccount, LogConstant.operation.add, LogConstant.operation.add_type, bto.getId().toString()));
		}
		return ok(bto.getId());
	}

	/**
	 * @Description 删除
	 * @Author 郑勇浩
	 * @Data 2019/7/3 16:29
	 * @Param [bankAccountDto]
	 * @return java.lang.String
	 *
	 */
	@PostMapping("deleteBankAccount")
	public Result<BatchResult> deleteBankAccount(@RequestBody BankAccountDto bto) {
		//必填
		FastUtils.checkParams(bto.getId(), bto.getVersion());
		//信息用户
		setUpdateUserInfo(bto);

		//DELETE 删除
		BatchResult result = bankAccountService.deleteBankAccount(bto);
		if (result.getSuccessList().size() > 0) {
			senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.bankAccount, LogConstant.operation.delete, LogConstant.operation.delete_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 批量删除
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:37
	 * @Param [bankAccountDto]
	 * @return java.lang.String
	 */
	@PostMapping("deleteBankAccountBatch")
	public Result<BatchResult> deleteBankAccountBatch(@RequestBody BankAccountDto bto) {
		//ids 为空直接返回
		if (CollectionUtils.isEmpty(bto.getIdList()) || CollectionUtils.isEmpty(bto.getVersionList())
				|| bto.getIdList().size() != bto.getVersionList().size()) {
			throw new ServiceException(ResultCode.PARAMS_NOT);
		}
		setUpdateUserInfo(bto);

		//DELETE 批量删除
		BatchResult result = bankAccountService.deleteBankAccountBatch(bto);
		if (result.getSuccessList().size() > 0) {
			senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.bankAccount, LogConstant.operation.delete, LogConstant.operation.delete_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 更新银行账号
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:37
	 * @Param [bto]
	 * @return java.lang.String
	 */
	@PostMapping("updateBankAccount")
	public Result<Long> updateBankAccount(@RequestBody BankAccountDto bto) {
		//必填
		FastUtils.checkParams(bto.getId(), bto.getCompanyId(), bto.getDepositBankId(), bto.getDepositBankName(), bto.getBusinessUnitId(), bto.getAccount(), bto.getName(), bto.getAccType(), bto.getAccTypeName(), bto.getAccUsage(), bto.getAccUsageName(), bto.getBankAccountCurrencies(), bto.getVersion());

		setUpdateUserInfo(bto);
		//UPDATE 账号信息(银行账号，账户名称租户内唯一)
		long result = bankAccountService.updateBankAccount(bto);
		if (result > 0) {
			senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.bankAccount, LogConstant.operation.update, LogConstant.operation.update_type, bto.getId().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 反禁用
	 * @Author 郑勇浩
	 * @Data 2019/7/3 16:29
	 * @Param [bankAccountDto]
	 * @return java.lang.String
	 */
	@PostMapping("enableBankAccount")
	public Result<BatchResult> enableBankAccount(@RequestBody BankAccountDto bto) {
		//必填
		FastUtils.checkParams(bto.getId(), bto.getVersion());

		//ENABLE 反禁用
		BatchResult result = bankAccountService.enableBankAccount(bto);
		if (result.getSuccessList().size() > 0) {
			senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.bankAccount, LogConstant.operation.antiForbidden, LogConstant.operation.antiForbidden_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 批量反禁用
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:37
	 * @Param [bankAccountDto]
	 * @return java.lang.String
	 */
	@PostMapping("enableBankAccountBatch")
	public Result<BatchResult> enableBankAccountBatch(@RequestBody BankAccountDto bto) {
		//ids 为空直接返回
		if (CollectionUtils.isEmpty(bto.getIdList()) || CollectionUtils.isEmpty(bto.getVersionList()) || bto.getIdList().size() != bto.getVersionList().size()) {
			throw new ServiceException(ResultCode.PARAMS_NOT);
		}

		//ENABLE 批量反禁用
		BatchResult result = bankAccountService.enableBankAccountBatch(bto);
		if (result.getSuccessList().size() > 0) {
			senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.bankAccount, LogConstant.operation.antiForbidden, LogConstant.operation.antiForbidden_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 禁用
	 * @Author 郑勇浩
	 * @Data 2019/7/3 16:29
	 * @Param [bankAccountDto]
	 * @return java.lang.String
	 */
	@PostMapping("disableBankAccount")
	public Result<BatchResult> disableBankAccount(@RequestBody BankAccountDto bto) {
		//必填
		FastUtils.checkParams(bto.getId(), bto.getVersion());

		//DISABLE 禁用
		BatchResult result = bankAccountService.disableBankAccount(bto);
		if (result.getSuccessList().size() > 0) {
			senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.bankAccount, LogConstant.operation.forbidden, LogConstant.operation.forbidden_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 批量禁用
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:37
	 * @Param [bankAccountDto]
	 * @return java.lang.String
	 */
	@PostMapping("disableBankAccountBatch")
	public Result<BatchResult> disableBankAccountBatch(@RequestBody BankAccountDto bto) {
		//ids 为空直接返回
		if (CollectionUtils.isEmpty(bto.getIdList()) || CollectionUtils.isEmpty(bto.getVersionList())
				|| bto.getIdList().size() != bto.getVersionList().size()) {
			throw new ServiceException(ResultCode.PARAMS_NOT);
		}

		//DISABLE 批量禁用
		BatchResult result = bankAccountService.disableBankAccountBatch(bto);
		if (result.getSuccessList().size() > 0) {
			senderService.sendLog(UserUtils.getUserLogInfo2(HttpUtils.getIpAddr(getRequest()), LogConstant.sysName.FinanceBackSys, LogConstant.menuName.bankAccount, LogConstant.operation.forbidden, LogConstant.operation.forbidden_type, result.getSuccessList().toString()));
		}
		return ok(result);
	}

	/**
	 * @Description 查询银行账号
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:37
	 * @Param [bankAccountDto]
	 * @return java.lang.String
	 */
	@RequestMapping("findBankAccount")
	public Result<BankAccountVo> findBankAccount(@RequestBody BankAccountDto bankAccountDto) {
		//必填
		FastUtils.checkParams(bankAccountDto.getId());

		//查询银行账号
		BankAccountVo resultData = bankAccountService.findBankAccount(bankAccountDto);
		if (resultData == null) {
			return ok(null);
		}

		//判斷是否引用
		resultData.setReference(referenceRelationService.isReference(Constant.Reference.BANK_ACCOUNT, bankAccountDto.getId()).isReference());
		resultData.setId(resultData.getId());

		//权限校验
		if (bankAccountDto.getIsEnterpriseAdmin().equals(Constant.Is.NO)) {
			ShiroUtils.checkPerm(Constant.MenuDefine.BANK_ACCOUNT_FIND, resultData.getUseCompanyId());
		}

		return ok(resultData);
	}

	/**
	 * @Description 查询银行账号分页
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:37
	 * @Param [bankAccountDto]
	 * @return java.lang.String
	 */
	@RequestMapping("findBankAccountPage")
	public Result<Page<BankAccountVo>> findBankAccountPage(@RequestBody BankAccountDto bankAccountDto) {
		SysUserVo operator = UserUtils.getUserVo();
		bankAccountDto.setRootEnterpriseId(operator.getRootEnterpriseId());

		//校验公司ID LIST权限
		checkCompanyListPerm(bankAccountDto.getIsEnterpriseAdmin(), bankAccountDto.getCompanyIdList(), Constant.MenuDefine.BANK_ACCOUNT_FIND);

		//业务端 默认为当前公司id进行查询
		if (bankAccountDto.getIsEnterpriseAdmin().equals(Constant.Is.NO) &&
				CollectionUtils.isEmpty(bankAccountDto.getCompanyIdList())) {
			List<Long> companyIdList = new ArrayList<>();
			companyIdList.add(operator.getDefaultCompanyId());
			bankAccountDto.setCompanyIdList(companyIdList);
		}

		return ok(bankAccountService.findBankAccountPage(bankAccountDto));
	}

	/**
	 * @Description 查询是否存在 账户名称或编码相同的账号
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:37
	 * @Param [bankAccountDto]
	 * @return java.lang.String
	 */
	@RequestMapping("findHasAccountOrName")
	public Result<Boolean> findHasAccountOrName(@RequestBody BankAccountDto bankAccountDto) {
		SysUserVo operator = UserUtils.getUserVo();
		bankAccountDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		return ok(bankAccountService.findHasAccountOrName(bankAccountDto));
	}


	/**
	 * @Description 账户类型 辅助资料
	 * @Author 郑勇浩
	 * @Data 2019/6/26 15:58
	 * @Param []
	 * @return java.lang.String
	 */
	@RequestMapping("findAccType")
	public Result<List<SysAuxDataVo>> findAccType() {
		return ok(bankAccountService.findAccType());
	}

	/**
	 * @Description 账户用途 辅助资料
	 * @Author 郑勇浩
	 * @Data 2019/6/26 15:58
	 * @Param []
	 * @return java.lang.String
	 */
	@RequestMapping("findAccUsage")
	public Result<List<SysAuxDataVo>> findAccUsage() {
		return ok(bankAccountService.findAccUsage());
	}

	/**
	 * @Description 币种 辅助资料
	 * @Author 郑勇浩
	 * @Data 2019/7/1 9:17
	 * @Param []
	 * @return java.lang.String
	 */
	@RequestMapping("findAccCurrency")
	public Result<List<SysAuxDataVo>> findAccCurrency() {
		return ok(bankAccountService.findAccCurrency());
	}

	/**
	 * @Description 开户银行
	 * @Author 郑勇浩
	 * @Data 2019/7/1 9:17
	 * @Param []
	 * @return java.lang.String
	 */
	@RequestMapping("findBankPage")
	public Result<Page<BankVo>> findBankPage(@RequestBody BankDto bankDto) {
		return bankAccountService.findBankPage(bankDto);
	}

	/**
	 * @Description 查询核算主体分页
	 * @Author 郑勇浩
	 * @Data 2019/7/1 9:17
	 * @Param []
	 * @return java.lang.String
	 */
	@RequestMapping("findAccountBookEntityList")
	public Result<Page<AccountBookEntityVo>> findAccountBookEntityList(@RequestBody AccountBookDto dto) {
		//必填校验
		FastUtils.checkParams(dto.getCompanyId(), dto.getCompanyHasSubAccount());

		SysUserVo operator = UserUtils.getUserVo();
		dto.setRootEnterpriseId(operator.getRootEnterpriseId());
		return ok(accountBookEntityService.findAccountBookEntityPage(dto));
	}

	/**
	 * @Description Excel 导出
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:38
	 * @Param [bankAccountDto, response]
	 */
	@RequestMapping("exportExcel")
	public void exportExcel(@RequestBody BankAccountDto bankAccountDto, HttpServletResponse response) {
		SysUserVo operator = UserUtils.getUserVo();
		bankAccountDto.setRootEnterpriseId(operator.getRootEnterpriseId());

		//校验公司ID LIST权限
		checkCompanyListPerm(bankAccountDto.getIsEnterpriseAdmin(), bankAccountDto.getCompanyIdList(), Constant.MenuDefine.BANK_ACCOUNT_EXPORT);

		//业务端 默认为当前公司id进行查询
		if (bankAccountDto.getIsEnterpriseAdmin().equals(Constant.Is.NO) &&
				CollectionUtils.isEmpty(bankAccountDto.getCompanyIdList())) {
			List<Long> companyIdList = new ArrayList<>();
			companyIdList.add(operator.getDefaultCompanyId());
			bankAccountDto.setCompanyIdList(companyIdList);
		}

		bankAccountService.exportExcel(bankAccountDto, response);
	}

	/**
	 * @Description 下载银行账号的模板
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:38
	 * @Param []
	 * @return org.springframework.http.ResponseEntity
	 */
	@RequestMapping("downloadTemplate")
	public ResponseEntity downloadTemplate() throws Exception {
		return fileService.downloadExcelTemplate(Constant.TemplateType.BANK_ACCOUNT);
	}

	/**
	 * @Description 上传并校验银行账号Excel
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:38
	 * @Param [file]
	 * @return java.lang.String
	 */
	@RequestMapping("uploadAndCheckExcel")
	public Result<ExcelResult> uploadAndCheckExcel(@RequestParam(value = "file") MultipartFile file) {
		return ok(fileService.uploadAndCheckExcel(file, Constant.TemplateType.BANK_ACCOUNT));
	}

	/**
	 * @Description 导入Excel
	 * @Author 郑勇浩
	 * @Data 2019/7/8 10:21
	 * @Param [uuid]
	 * @return java.lang.String
	 */
	@RequestMapping("importExcel")
	public Result<ExcelResult> importExcel(@RequestBody ExcelRequest request) {
		FastUtils.checkParams(request.getUuid());
		return ok(fileService.importExcel(request.getUuid()));
	}

	/**
	 * @Description 设置更新用户信息
	 * @Author 郑勇浩
	 * @Data 2019/7/3 14:47
	 * @Param [bankAccountDto]
	 */
	private void setUpdateUserInfo(BankAccountDto bankAccountDto) {
		//登录用户
		SysUserVo operator = UserUtils.getUserVo();
		bankAccountDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		bankAccountDto.setUpdatorId(operator.getUserId());
		bankAccountDto.setUpdatorName(operator.getName());
		bankAccountDto.setUpdateTime(new Date());
	}

	/**
	 * @Description 校验公司ID列表
	 * @Author 郑勇浩
	 * @Data 2019/9/20 17:40
	 * @Param [companyIdList, roleName]
	 */
	private void checkCompanyListPerm(Byte isEnterpriseAdmin, List<Long> companyIdList, String roleName) {
		//如果是admin或者公司列表是空则跳过
		if (isEnterpriseAdmin.equals(Constant.Is.YES) || CollectionUtils.isEmpty(companyIdList)) {
			return;
		}

		//校验权限
		List<Long> deleteIds = new LinkedList<>();
		for (Long companyId : companyIdList) {
			if (!companyId.equals(Constant.Number.ZEROL) && !ShiroUtils.hasPerm(roleName, companyId)) {
				deleteIds.add(companyId);
			}
		}

		companyIdList.removeAll(deleteIds);
	}

}
