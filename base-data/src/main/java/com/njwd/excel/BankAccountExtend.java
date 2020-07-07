package com.njwd.excel;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.annotation.ExcelExtend;
import com.njwd.basedata.cloudclient.BankFeignClient;
import com.njwd.basedata.service.BankAccountService;
import com.njwd.common.Constant;
import com.njwd.entity.basedata.BankAccountCurrency;
import com.njwd.entity.basedata.BusinessUnit;
import com.njwd.entity.basedata.dto.BankAccountDto;
import com.njwd.entity.basedata.dto.BusinessUnitDto;
import com.njwd.entity.basedata.dto.CompanyDto;
import com.njwd.entity.basedata.vo.CompanyVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.dto.BankDto;
import com.njwd.entity.platform.vo.BankVo;
import com.njwd.entity.platform.vo.SysAuxDataVo;
import com.njwd.exception.ResultCode;
import com.njwd.fileexcel.check.CheckContext;
import com.njwd.fileexcel.check.CheckResult;
import com.njwd.fileexcel.extend.AddExtend;
import com.njwd.fileexcel.extend.CheckExtend;
import com.njwd.fileexcel.extend.CheckHandler;
import com.njwd.financeback.service.BusinessUnitService;
import com.njwd.financeback.service.CompanyService;
import com.njwd.service.FileService;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.ShiroUtils;
import com.njwd.utils.StringUtil;
import com.njwd.utils.UserUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 银行账号 Excel导入
 * @Date 2019/7/5 15:00
 * @Author 郑勇浩
 */
@Component
@ExcelExtend(type = Constant.TemplateType.BANK_ACCOUNT)
public class BankAccountExtend implements AddExtend<BankAccountDto>, CheckExtend {

	@Resource
	private BankAccountService bankAccountService;
	@Resource
	private BankFeignClient bankFeignClient;
	@Resource
	private CompanyService companyService;
	@Resource
	private BusinessUnitService businessUnitService;
	@Resource
	private FileService fileService;

	@Override
	public int addBatch(List<BankAccountDto> dataList) {
		int successCount = 0;
		//NULL
		if (CollectionUtils.isEmpty(dataList)) {
			return 0;
		}

		//获取币种 默认第一个(第一版只有一种)
		List<SysAuxDataVo> sysAuxDataList = bankAccountService.findAccCurrency();
		if (CollectionUtils.isEmpty(sysAuxDataList)) {
			return 0;
		}
		BankAccountCurrency bc = new BankAccountCurrency();
		bc.setCurrencyId(sysAuxDataList.get(0).getId());
		bc.setCurrencyName(sysAuxDataList.get(0).getName());

		//设值
		for (BankAccountDto data : dataList) {
			//添加币种
			data.setBankAccountCurrencies(new BankAccountCurrency[1]);
			data.getBankAccountCurrencies()[0] = bc;
			//必填
			if (insertBankAccount(data)) {
				successCount++;
			}
		}
		return successCount;
	}

	@Override
	public int add(BankAccountDto data) {
		if (insertBankAccount(data)) {
			return 1;
		}
		return 0;
	}

	@Override
	public void check(CheckContext checkContext) {
		//获取当前登录用户
		SysUserVo operator = UserUtils.getUserVo();
		Long rootEnterpriseId = operator.getRootEnterpriseId();

		Byte isEnterpriseAdmin = checkContext.getByteValue("isEnterpriseAdmin");
		if (isEnterpriseAdmin == null) {
			isEnterpriseAdmin = Constant.Is.NO;
		}

		//通用规则校验通过后，会校验自定义的规则
		checkContext
				.addSheetHandler("companyCode", checkCompany(isEnterpriseAdmin, rootEnterpriseId))
				.addSheetHandler("businessUnitCode", checkBusinessUnit(rootEnterpriseId))
				.addSheetHandler("account", checkAccount())
				.addSheetHandler("name", checkName())
				.addSheetHandler("depositBankName", getDepositBankNameCheckHandler());
	}

	/**
	 * @Description 校验银行账号
	 * @Author 郑勇浩
	 * @Data 2019/9/21 11:07
	 * @Param [accountList]
	 * @return com.njwd.fileexcel.extend.CheckHandler<com.njwd.entity.basedata.dto.BankAccountDto>
	 */
	private CheckHandler<BankAccountDto> checkAccount() {
		return data -> {
			if (StringUtil.isEmpty(data.getAccount())) {
				return CheckResult.error(ResultCode.PARAMS_NOT.message);
			}

			//空格验证
			if (StringUtil.regMatch(data.getAccount(), Constant.Regex.HAS_SPACE, false)) {
				return CheckResult.error(ResultCode.BANK_ACCOUNT_FAIL.message);
			}

			//特殊字符验证
			if (StringUtil.regMatch(data.getAccount(), Constant.Regex.HAS_SPECIAL, false)) {
				return CheckResult.error(ResultCode.BANK_ACCOUNT_FAIL.message);
			}

			//数字验证
			if (!StringUtil.isNumeric(data.getAccount())) {
				return CheckResult.error(ResultCode.BANK_ACCOUNT_FAIL.message);
			}
			return CheckResult.ok();
		};
	}

	/**
	 * @Description 校验银行账号
	 * @Author 郑勇浩
	 * @Data 2019/9/21 11:07
	 * @Param [nameList]
	 * @return com.njwd.fileexcel.extend.CheckHandler<com.njwd.entity.basedata.dto.BankAccountDto>
	 */
	private CheckHandler<BankAccountDto> checkName() {
		return data -> {
			if (StringUtil.isEmpty(data.getName())) {
				return CheckResult.error(ResultCode.PARAMS_NOT.message);
			}

			//空格验证
			if (StringUtil.regMatch(data.getName(), Constant.Regex.HAS_SPACE, false)) {
				return CheckResult.error(ResultCode.BANK_NAME_FAIL.message);
			}

			//特殊字符验证
			if (StringUtil.regMatch(data.getName(), Constant.Regex.HAS_SPECIAL, false)) {
				return CheckResult.error(ResultCode.BANK_NAME_FAIL.message);
			}

			return CheckResult.ok();
		};
	}

	/**
	 * @Description 验证创建公司编码
	 * @Author 郑勇浩
	 * @Data 2019/9/20 17:19
	 * @Param [isEnterpriseAdmin, rootEnterpriseId]
	 * @return com.njwd.fileexcel.extend.CheckHandler<com.njwd.entity.basedata.dto.BankAccountDto>
	 */
	private CheckHandler<BankAccountDto> checkCompany(Byte isEnterpriseAdmin, Long rootEnterpriseId) {
		return data -> {
			//不存在rootEnterpriseId
			if (rootEnterpriseId == null) {
				//登录过期
				return CheckResult.error(ResultCode.SYS_USER_INVALID.message);
			}

			//查询公司是否存在
			CompanyDto companyParam = new CompanyDto();
			companyParam.setCode(data.getCompanyCode());
			companyParam.setRootEnterpriseId(rootEnterpriseId);
			CompanyVo returnCompany = companyService.findCompanyByIdOrCodeOrName(companyParam);
			if (returnCompany == null || returnCompany.getId() == null) {
				return CheckResult.error(ResultCode.COMPANY_NOT_EXIST.message);
			}

			//校验公司权限
			if (isEnterpriseAdmin.equals(Constant.Is.NO) && !ShiroUtils.hasPerm(Constant.MenuDefine.BANK_ACCOUNT_IMPORT, returnCompany.getId())) {
				return CheckResult.error(ResultCode.PERMISSION_NOT.message);
			}

			//放入结果值
			data.setCompanyName(returnCompany.getName());
			data.setCompanyId(returnCompany.getId());
			data.setCreateCompanyId(returnCompany.getId());
			data.setUseCompanyId(returnCompany.getId());
			data.setHasSubAccount(returnCompany.getHasSubAccount());
			return CheckResult.ok();
		};
	}

	/**
	 * @Description 查询业务单元是否存在
	 * @Author 郑勇浩
	 * @Data 2019/12/3 11:22
	 * @Param [rootEnterpriseId]
	 * @return com.njwd.fileexcel.extend.CheckHandler<com.njwd.entity.basedata.dto.BankAccountDto>
	 */
	private CheckHandler<BankAccountDto> checkBusinessUnit(Long rootEnterpriseId) {
		return data -> {
			//查询业务单元是否存在
			BusinessUnitDto businessUnitDto = new BusinessUnitDto();
			businessUnitDto.setCode(data.getBusinessUnitCode());
			businessUnitDto.setCompanyId(data.getUseCompanyId());
			businessUnitDto.setRootEnterpriseId(rootEnterpriseId);
			BusinessUnit businessUnit = businessUnitService.findBusinessByCode(businessUnitDto);
			if (businessUnit == null) {
				return CheckResult.error(ResultCode.BUSINESS_UNIT_NO.message);
			}
			return CheckResult.ok();
		};
	}


	/**
	 * @Description 开户银行验证
	 * @Author 郑勇浩
	 * @Data 2019/8/5 17:33
	 * @Param []
	 * @return com.njwd.fileexcel.extend.CheckHandler<com.njwd.entity.basedata.dto.BankAccountDto>
	 */
	private CheckHandler<BankAccountDto> getDepositBankNameCheckHandler() {
		return data -> {
			//查询开户银行是否存在
			BankDto platformBankDto = new BankDto();
			platformBankDto.setCodeOrName(data.getDepositBankName());
			fileService.resetPage(platformBankDto.getPage());
			Result<Page<BankVo>> pageResult = bankFeignClient.findBankPage(platformBankDto);
			Page<BankVo> returnPfb = pageResult.getData();

			//NULL
			if (CollectionUtils.isEmpty(returnPfb.getRecords())) {
				return CheckResult.error(ResultCode.BANK_NOT_EXIST.message);
			}

			for (BankVo pfb : returnPfb.getRecords()) {
				//如果存在名字相同的开户行
				if (data.getDepositBankName().equals(pfb.getName())) {
					//设置值
					data.setDepositBankId(pfb.getId());
					return CheckResult.ok();
				}
			}
			//否则银行账号不存在
			return CheckResult.error(ResultCode.RECORD_NOT_EXIST.message);
		};
	}

	/**
	 * @Description 新增账户
	 * @Author 郑勇浩
	 * @Data 2019/9/20 17:35
	 * @Param [isEnterpriseAdmin, companyId]
	 */
	private Boolean insertBankAccount(BankAccountDto bto) {
		//必填
		FastUtils.checkParams(bto.getCompanyId(), bto.getDepositBankId(), bto.getDepositBankName(), bto.getBusinessUnitId(), bto.getAccount(), bto.getName(), bto.getAccType(), bto.getAccTypeName(), bto.getAccUsage(), bto.getAccUsageName(), bto.getBankAccountCurrencies());

		//ADD 账号信息(银行账号，账户名称租户内唯一)
		bankAccountService.addBankAccount(bto);
		return true;
	}


}
