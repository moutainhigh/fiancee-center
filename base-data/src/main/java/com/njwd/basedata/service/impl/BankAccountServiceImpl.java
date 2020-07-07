package com.njwd.basedata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.basedata.cloudclient.BankFeignClient;
import com.njwd.basedata.mapper.BankAccountMapper;
import com.njwd.basedata.service.BankAccountCurrencyService;
import com.njwd.basedata.service.BankAccountService;
import com.njwd.basedata.service.BaseCustomService;
import com.njwd.common.Constant;
import com.njwd.common.MenuCodeConstant;
import com.njwd.entity.basedata.BankAccount;
import com.njwd.entity.basedata.ReferenceContext;
import com.njwd.entity.basedata.ReferenceDescription;
import com.njwd.entity.basedata.dto.BankAccountCurrencyDto;
import com.njwd.entity.basedata.dto.BankAccountDto;
import com.njwd.entity.basedata.vo.BankAccountVo;
import com.njwd.entity.basedata.vo.SysUserVo;
import com.njwd.entity.platform.dto.BankDto;
import com.njwd.entity.platform.dto.SysAuxDataDto;
import com.njwd.entity.platform.vo.BankVo;
import com.njwd.entity.platform.vo.SysAuxDataVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.financeback.service.SysAuxDataService;
import com.njwd.service.FileService;
import com.njwd.service.ReferenceRelationService;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import com.njwd.utils.RedisUtils;
import com.njwd.utils.ShiroUtils;
import com.njwd.utils.UserUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description 银行账户 service impl.
 * @Date 2019-06-11 12:00
 * @Author 郑勇浩
 */
@Service
public class BankAccountServiceImpl implements BankAccountService {

	@Resource
	private BankAccountMapper bankAccountMapper;
	@Resource
	private BankAccountServiceImpl bankAccountService;
	@Resource
	private BankAccountCurrencyService bankAccountCurrencyService;
	@Resource
	private FileService fileService;
	@Resource
	private SysAuxDataService sysAuxDataService;
	@Resource
	private BankFeignClient bankFeignClient;
	@Resource
	private ReferenceRelationService referenceRelationService;
	@Resource
	private BaseCustomService baseCustomService;

	/**
	 * @Description 新增银行账号
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:34
	 * @Param [bankAccountDto]
	 * @return int
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int addBankAccount(BankAccountDto bankAccountDto) {
		//权限校验
		checkUser(bankAccountDto.getIsEnterpriseAdmin(), bankAccountDto.getCompanyId());

		//USER INFO
		SysUserVo operator = UserUtils.getUserVo();
		bankAccountDto.setCreatorId(operator.getUserId());
		bankAccountDto.setCreatorName(operator.getName());
		bankAccountDto.setCreateCompanyId(bankAccountDto.getCompanyId());
		bankAccountDto.setUseCompanyId(bankAccountDto.getCompanyId());
		bankAccountDto.setRootEnterpriseId(operator.getRootEnterpriseId());
		//版本号
		bankAccountDto.setVersion(Constant.Number.ZERO);

		//银行账号 账号名称校验
		checkData(bankAccountDto);

		int result = bankAccountMapper.insert(bankAccountDto);
		//新增币种
		if (bankAccountDto.getBankAccountCurrencies() != null && bankAccountDto.getBankAccountCurrencies().length > 0) {
			BankAccountCurrencyDto bankAccountCurrencyDto = new BankAccountCurrencyDto();
			bankAccountCurrencyDto.setBankAccountId(bankAccountDto.getId());
			bankAccountCurrencyDto.setBankAccountCurrencies(bankAccountDto.getBankAccountCurrencies());
			bankAccountCurrencyService.insertBatch(bankAccountCurrencyDto);
		}
		return result;
	}

	/**
	 * @Description 删除
	 * @Author 郑勇浩
	 * @Data 2019/7/4 10:56
	 * @Param [bto]
	 * @return int
	 */
	@Override
	public BatchResult deleteBankAccount(BankAccountDto bto) {
		//封装返回结果
		fromIdToList(bto);
		//封装返回结果
		return updateStatusBatch(Constant.OperateType.DELETE, bto);
	}

	/**
	 * @Description 批量删除
	 * @Author 郑勇浩
	 * @Data 2019/7/2 14:41
	 * @Param []
	 * @return int
	 */
	@Override
	public BatchResult deleteBankAccountBatch(BankAccountDto bankAccountDto) {
		//封装返回结果
		return updateStatusBatch(Constant.OperateType.DELETE, bankAccountDto);
	}

	/**
	 * @Description 更新银行账号
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:36
	 * @Param [bankAccountDto]
	 * @return int
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public long updateBankAccount(BankAccountDto bankAccountDto) {
		//权限校验
		checkUser(bankAccountDto.getIsEnterpriseAdmin(), bankAccountDto.getCompanyId());

		//查询该数据状态
		BankAccount bankAccount = bankAccountService.findBankAccount(bankAccountDto);
		if (bankAccount == null) {
			throw new ServiceException(ResultCode.RECORD_NOT_EXIST);
		}

		//如果版本号不同
		if (!bankAccountDto.getVersion().equals(bankAccount.getVersion())) {
			throw new ServiceException(ResultCode.VERSION_ERROR);
		}

		if (bankAccount.getIsDel().equals(Constant.Is.YES)) {
			throw new ServiceException(ResultCode.IS_DEL);
		}
		//银行账号 账号名称校验
		checkData(bankAccountDto);

		BankAccount sqlParam = new BankAccount();
		sqlParam.setId(bankAccountDto.getId());

		//更新银行账户
		bankAccountDto.setId(null);
		bankAccountDto.setVersion(bankAccount.getVersion());
		int result = bankAccountMapper.update(bankAccountDto, new QueryWrapper<>(sqlParam));
		if (result < 1) {
			throw new ServiceException(ResultCode.IS_CHANGE);
		}

		//删除原有币种数据
		BankAccountCurrencyDto bankAccountCurrencyDto = new BankAccountCurrencyDto();
		bankAccountCurrencyDto.setBankAccountId(sqlParam.getId());
		bankAccountCurrencyService.delete(bankAccountCurrencyDto);
		//新增币种
		if (bankAccountDto.getBankAccountCurrencies() != null && bankAccountDto.getBankAccountCurrencies().length > 0) {
			bankAccountCurrencyDto.setBankAccountCurrencies(bankAccountDto.getBankAccountCurrencies());
			bankAccountCurrencyService.insertBatch(bankAccountCurrencyDto);
		}
		bankAccountDto.setId(sqlParam.getId());
		//清除缓存
		RedisUtils.remove(Constant.RedisCache.BANK_ACCOUNT, bankAccountDto.getId());
		return bankAccountDto.getVersion();
	}

	/**
	 * @Description 反禁用
	 * @Author 郑勇浩
	 * @Data 2019/7/3 16:00
	 * @Param [bankAccountDto]
	 * @return int
	 */
	@Override
	public BatchResult enableBankAccount(BankAccountDto bto) {
		fromIdToList(bto);
		return updateStatusBatch(Constant.OperateType.ENABLE, bto);
	}

	/**
	 * @Description 批量反禁用
	 * @Author 郑勇浩
	 * @Data 2019/7/2 14:41
	 * @Param []
	 * @return int
	 */
	@Override
	public BatchResult enableBankAccountBatch(BankAccountDto bankAccountDto) {
		return updateStatusBatch(Constant.OperateType.ENABLE, bankAccountDto);
	}

	/**
	 * @Description 禁用
	 * @Author 郑勇浩
	 * @Data 2019/7/3 17:10
	 * @Param [bto]
	 * @return int
	 */
	@Override
	public BatchResult disableBankAccount(BankAccountDto bto) {
		fromIdToList(bto);
		return updateStatusBatch(Constant.OperateType.DISABLE, bto);
	}

	/**
	 * @Description 批量禁用
	 * @Author 郑勇浩
	 * @Data 2019/7/2 14:41
	 * @Param []
	 * @return int
	 */
	@Override
	public BatchResult disableBankAccountBatch(BankAccountDto bankAccountDto) {
		return updateStatusBatch(Constant.OperateType.DISABLE, bankAccountDto);
	}

	/**
	 * @Description 查询银行账号
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:34
	 * @Param [bankAccountDto]
	 * @return com.njwd.entity.basedata.vo.BankAccountVo
	 */
	@Override
	@Cacheable(value = Constant.RedisCache.BANK_ACCOUNT, key = "#bankAccountDto.id", unless = "#result == null")
	public BankAccountVo findBankAccount(BankAccountDto bankAccountDto) {
		return bankAccountMapper.findOne(bankAccountDto);
	}

	/**
	 * @Description 查询银行账号分页
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:34
	 * @Param [bankAccountDto]
	 * @return com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.BankAccountVo>
	 */
	@Override
	public Page<BankAccountVo> findBankAccountPage(BankAccountDto bankAccountDto) {
		if (CollectionUtils.isEmpty(bankAccountDto.getCompanyIdList())) {
			bankAccountDto.setCompanyIdList(null);
		}
		return bankAccountMapper.findPage(bankAccountDto.getPage(), bankAccountDto);
	}

	/**
	 * @Description 查询是否存在 账户名称或编码相同的账号(COUNT)
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:36
	 * @Param [bankAccountDto]
	 * @return boolean
	 */
	@Override
	public boolean findHasAccountOrName(BankAccountDto bankAccountDto) {
		//存在重复
		if (bankAccountMapper.findHasAccountOrName(bankAccountDto) > 0) {
			if (StringUtils.isNoneBlank(bankAccountDto.getAccount())) {
				//银行账号已存在
				throw new ServiceException(ResultCode.BANK_ACCOUNT_EXIST);
			} else {
				//账号名称已存在
				throw new ServiceException(ResultCode.BANK_NAME_EXIST);
			}
		}
		//可以使用
		return true;
	}

	/**
	 * @Description 查询账户类型 辅助资料
	 * @Author 郑勇浩
	 * @Data 2019/6/26 15:55
	 * @Param []
	 * @return java.util.List<com.njwd.entity.basedata.SysAuxData>
	 */
	@Override
	public List<SysAuxDataVo> findAccType() {
		SysAuxDataDto dataType = new SysAuxDataDto();
		dataType.setType("acc_type");
		// platform
		return sysAuxDataService.findAuxDataList(dataType);
	}

	/**
	 * @Description 查询账户用途 辅助资料
	 * @Author 郑勇浩
	 * @Data 2019/6/26 15:55
	 * @Param []
	 * @return java.util.List<com.njwd.entity.basedata.SysAuxData>
	 */
	@Override
	public List<SysAuxDataVo> findAccUsage() {
		SysAuxDataDto dataType = new SysAuxDataDto();
		dataType.setType(Constant.SysAuxDataCode.ACCOUNT_USAGE);
		// 平台接口
		return sysAuxDataService.findAuxDataList(dataType);
	}

	/**
	 * @Description 查询币种 辅助资料
	 * @Author 郑勇浩
	 * @Data 2019/7/1 9:16
	 * @Param []
	 * @return java.util.List<com.njwd.entity.basedata.SysAuxData>
	 */
	@Override
	public List<SysAuxDataVo> findAccCurrency() {
		SysAuxDataDto dataType = new SysAuxDataDto();
		dataType.setType(Constant.SysAuxDataCode.ACCOUNT_CURRENCY);
		// 平台接口
		return sysAuxDataService.findAuxDataList(dataType);
	}

	/**
	 * @Description 查询开户行 分页
	 * @Author 郑勇浩
	 * @Data 2019/7/3 14:46
	 * @Param [platformBankDto]
	 * @return java.lang.String
	 */
	@Override
	public Result<Page<BankVo>> findBankPage(BankDto platformBankDto) {
		return bankFeignClient.findBankPage(platformBankDto);
	}

	/**
	 * @Description Excel 导出
	 * @Author 郑勇浩
	 * @Data 2019/6/20 17:36
	 * @Param [bankAccountDto, response, type]
	 */
	@Override
	public void exportExcel(BankAccountDto bankAccountDto, HttpServletResponse response) {
		Page<BankAccountVo> page = new Page<>();
		fileService.resetPage(page);
		Page<BankAccountVo> bankAccountVoPage = bankAccountMapper.findPage(page, bankAccountDto);
		if (CollectionUtils.isEmpty(bankAccountVoPage.getRecords())) {
			fileService.exportExcel(response, bankAccountVoPage.getRecords());
		}
		//不同身份导出不同表格
		fileService.exportExcel(response, bankAccountVoPage.getRecords(), MenuCodeConstant.BANK_ACCOUNT, bankAccountDto.getIsEnterpriseAdmin());
	}

	/**
	 * @Description 银行账号 账号名称校验
	 * @Author 郑勇浩
	 * @Data 2019/7/3 14:47
	 * @Param [bankAccountDto]
	 */
	private void checkData(BankAccountDto bankAccountDto) {
		//校验 银行账号 账户名称唯一性
		BankAccount hasBk = bankAccountMapper.findHasOne(bankAccountDto);
		if (hasBk != null) {
			//账户校验
			if (bankAccountDto.getAccount().equals(hasBk.getAccount().trim())) {
				throw new ServiceException(ResultCode.BANK_ACCOUNT_EXIST);
			} else if (bankAccountDto.getName().equals(hasBk.getName().trim())) {
				throw new ServiceException(ResultCode.BANK_NAME_EXIST);
			}
		}
	}

	/**
	 * @Description 批量更新状态
	 * @Author 郑勇浩
	 * @Data 2019/8/19 16:51
	 * @Param [type, bto]
	 * @return com.njwd.support.BatchResult
	 */
	private BatchResult updateStatusBatch(int type, BankAccountDto bto) {
		//初始化
		BatchResult result = new BatchResult();
		result.setFailList(new LinkedList<>());
		result.setSuccessList(new ArrayList<>());

		//查询待查询的所有数据的状态
		List<BankAccount> bankAccountList = bankAccountMapper.findEnableAndDelInIds(bto);

		//循环判断错误
		for (BankAccount bankAccount : bankAccountList) {
			//判断是否有该数据的权限
			if (bto.getIsEnterpriseAdmin().equals(Constant.Is.NO) && !ShiroUtils.hasPerm(getRoleName(type), bankAccount.getUseCompanyId())) {
				addFailResult(result, bankAccount.getId(), ResultCode.PERMISSION_NOT.message);
				continue;
			}

			//判断版本号是否相同
			int index = bto.getIdList().indexOf(bankAccount.getId());
			if (!bto.getVersionList().get(index).equals(bankAccount.getVersion())) {
				addFailResult(result, bankAccount.getId(), ResultCode.VERSION_ERROR.message);
				continue;
			}

			//判断删除状态
			if (bankAccount.getIsDel().equals(Constant.Is.YES)) {
				addFailResult(result, bankAccount.getId(), ResultCode.IS_DEL.message);
				continue;
			}

			//判断反禁用 禁用状态
			if (type == Constant.OperateType.ENABLE && Constant.Is.YES.equals(bankAccount.getIsEnable())) {
				addFailResult(result, bankAccount.getId(), ResultCode.IS_ENABLE.message);
				continue;
			} else if (type == Constant.OperateType.DISABLE && Constant.Is.NO.equals(bankAccount.getIsEnable())) {
				addFailResult(result, bankAccount.getId(), ResultCode.IS_DISABLE.message);
				continue;
			}

			//通过验证的数据
			result.getSuccessList().add(bankAccount.getId());
		}

		//防止没有数据
		if (result.getSuccessList().size() == 0) {
			return result;
		}

		//判断是否被引用
		ReferenceContext referenceContext = referenceRelationService.isReference(Constant.Reference.BANK_ACCOUNT, result.getSuccessList());
		result.getFailList().addAll(referenceContext.getReferences());
		result.getSuccessList().removeAll(referenceContext.getReferences().stream().map(ReferenceDescription::getBusinessId).collect(Collectors.toList()));
		if (result.getSuccessList().size() == 0) {
			return result;
		}

		//SQL PARAM
		BankAccount sqlParam = new BankAccount();
		sqlParam.setRootEnterpriseId(bto.getRootEnterpriseId());
		//生成更新条件
		bto.setRootEnterpriseId(null);
		bto.setBatchIds(result.getSuccessList());
		if (type == Constant.OperateType.ENABLE) {
			//启用
			bto.setIsEnable(Constant.Is.YES);
			baseCustomService.batchEnable(bto, bto.getIsEnable(), bankAccountMapper, null);
		} else if (type == Constant.OperateType.DISABLE) {
			//禁用
			bto.setIsEnable(Constant.Is.NO);
			baseCustomService.batchEnable(bto, bto.getIsEnable(), bankAccountMapper, null);
		} else {
			//删除
			bto.setIsDel(Constant.Is.YES);
			bankAccountMapper.deleteBatch(bto);
		}
		//清除成功修改的redis缓存
		RedisUtils.removeBatch(Constant.RedisCache.BANK_ACCOUNT, result.getSuccessList());
		return result;
	}

	/**
	 * @Description 添加失败原因
	 * @Author 郑勇浩
	 * @Data 2019/9/10 15:03
	 * @Param [id, failMessage]
	 */
	private void addFailResult(BatchResult result, Long id, String failMessage) {
		ReferenceDescription<BankAccountVo> fd = new ReferenceDescription<>();
		fd.setBusinessId(id);
		fd.setReferenceDescription(failMessage);
		result.getFailList().add(fd);
	}

	/**
	 * @Description 用户权限校验
	 * @Author 郑勇浩
	 * @Data 2019/9/10 11:31
	 * @Param [isEnterpriseAdmin, roleName, companyId]
	 */
	private void checkUser(Byte isEnterpriseAdmin, Long companyId) {
		//业务端
		if (isEnterpriseAdmin.equals(Constant.Is.NO)) {
			ShiroUtils.checkPerm(Constant.MenuDefine.BANK_ACCOUNT_EDIT, companyId);
		}
	}

	/**
	 * @Description 获取权限名称
	 * @Author 郑勇浩
	 * @Data 2019/9/18 14:41
	 * @Param [type]
	 * @return java.lang.String
	 */
	private String getRoleName(int type) {
		if (type == Constant.OperateType.ENABLE) {
			return Constant.MenuDefine.BANK_ACCOUNT_ENABLE;
		} else if (type == Constant.OperateType.DISABLE) {
			return Constant.MenuDefine.BANK_ACCOUNT_DISABLE;
		} else {
			return Constant.MenuDefine.BANK_ACCOUNT_DELETE;
		}
	}

	private void fromIdToList(BankAccountDto dto) {
		dto.setIdList(new ArrayList<>());
		dto.getIdList().add(dto.getId());
		dto.setVersionList(new ArrayList<>());
		dto.getVersionList().add(dto.getVersion());
	}
}
