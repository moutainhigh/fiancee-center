package com.njwd.ledger.service.impl;

import com.alibaba.excel.util.CollectionUtils;
import com.njwd.common.Constant;
import com.njwd.common.ExcelColumnConstant;
import com.njwd.common.LedgerConstant;
import com.njwd.entity.basedata.dto.AccountBookEntityDto;
import com.njwd.entity.ledger.dto.GeneralLedgerQueryDto;
import com.njwd.entity.ledger.vo.GeneralLedgerVo;
import com.njwd.entity.platform.AccountSubject;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.ledger.cloudclient.AccountSubjectFeignClient;
import com.njwd.ledger.mapper.GeneralLedgerMapper;
import com.njwd.ledger.service.GeneralLedgerService;
import com.njwd.service.FileService;
import com.njwd.utils.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description 总分类账ServiceImpl
 * @Date 2019/7/30 9:14
 * @Author 薛永利
 */
@Service
public class GeneralLedgerServiceImpl implements GeneralLedgerService {

	@Resource
	private GeneralLedgerMapper generalLedgerMapper;
	@Resource
	private AccountSubjectFeignClient accountSubjectFeignClient;
	@Resource
	private FileService fileService;

	/**
	 * @Description 查询总分类账
	 * @Author 郑勇浩
	 * @Data 2019/9/28 17:45
	 * @Param [generalLedgerQueryDto]
	 * @return java.util.List<com.njwd.entity.ledger.vo.GeneralLedgerVo>
	 */
	@Override
	public List<GeneralLedgerVo> findGeneralLedgerList(GeneralLedgerQueryDto param) {
		//查询会计科目 获取会计科目列
		List<AccountSubjectVo> accountSubjectList = findAccountSubjectList(param);
		if (CollectionUtils.isEmpty(accountSubjectList)) {
			return new ArrayList<>();
		}
		//查询符合条件的制单日期内的数据 最大最小区间
		if (StringUtils.isNotBlank(param.getVoucherDate1())) {
			if (!setParamByVoucherDate(param)) {
				return new ArrayList<>();
			}
		}

		List<GeneralLedgerVo> dataBaseList = generalLedgerMapper.findDetailLedgerList(param);
		if (CollectionUtils.isEmpty(accountSubjectList)) {
			return new ArrayList<>();
		}

		//生成以code为key的计算后数据
		HashMap<String, List<GeneralLedgerVo>> formatListHashMap = formatCodeMap(dataBaseList, accountSubjectList, param);
		//格式化输出账簿
		List<GeneralLedgerVo> formatData = formatReportForms(formatListHashMap, param, accountSubjectList, Constant.SortNum.PERIOD_NUM);
		//数据过滤
		filterData(formatData, param);
		//日期过滤
		filterDataByDate(formatData, param);
		return formatData;
	}

	/**
	 * @Description 查询明细分类账
	 * @Author 郑勇浩
	 * @Data 2019/9/30 17:13
	 * @Param [generalLedgerQueryDto]
	 * @return java.util.List<com.njwd.entity.ledger.vo.GeneralLedgerVo>
	 */
	@Override
	public List<GeneralLedgerVo> findDetailLedgerList(GeneralLedgerQueryDto param) {
		//查询会计科目 获取会计科目列
		List<AccountSubjectVo> accountSubjectList = findAccountSubjectList(param);
		if (CollectionUtils.isEmpty(accountSubjectList)) {
			return new ArrayList<>();
		}
		//查询符合条件的制单日期内的数据 最大最小区间
		if (StringUtils.isNotBlank(param.getVoucherDate1())) {
			if (!setParamByVoucherDate(param)) {
				return new ArrayList<>();
			}
		}

		List<GeneralLedgerVo> dataBaseList = generalLedgerMapper.findDetailLedgerList(param);
		if (CollectionUtils.isEmpty(accountSubjectList)) {
			return new ArrayList<>();
		}

		//生成以code为key的计算后数据
		HashMap<String, List<GeneralLedgerVo>> formatListHashMap = formatCodeMap(dataBaseList, accountSubjectList, param);
		//格式化输出账簿
		List<GeneralLedgerVo> formatData = formatReportForms(formatListHashMap, param, accountSubjectList, Constant.SortNum.DETAIL);
		//数据过滤
		filterData(formatData, param);
		//日期过滤
		filterDataByDate(formatData, param);
		return formatData;
	}

	/**
	 * @Description 导出总分类账excel
	 * @Author 薛永利
	 * @Date 2019/8/26 9:34
	 * @Param [generalLedgerQueryDto, response]
	 */
	@Override
	public void exportGeneralLedgerExcel(GeneralLedgerQueryDto generalLedgerQueryDto, HttpServletResponse response) {
		List<GeneralLedgerVo> generalLedgerVoList = findGeneralLedgerList(generalLedgerQueryDto);
		if (generalLedgerVoList != null) {
			fileService.exportExcel(response, generalLedgerVoList,
					ExcelColumnConstant.GeneralLedger.SUBJECT_CODE,
					ExcelColumnConstant.GeneralLedger.SUBJECT_NAME,
					ExcelColumnConstant.GeneralLedger.ACCOUNT_BOOK_NAME,
					ExcelColumnConstant.GeneralLedger.ACCOUNT_BOOK_ENTITY_NAME,
					ExcelColumnConstant.GeneralLedger.PERIOD_YEAR,
					ExcelColumnConstant.GeneralLedger.PERIOD_NUM,
					ExcelColumnConstant.GeneralLedger.SUMMARY,
					ExcelColumnConstant.GeneralLedger.DEBIT,
					ExcelColumnConstant.GeneralLedger.CREDIT,
					ExcelColumnConstant.GeneralLedger.BALANCE_DIRECTION,
					ExcelColumnConstant.GeneralLedger.BALANCE);
		}
	}

	/**
	 * @Description 导出明细分类账excel
	 * @Author 薛永利
	 * @Date 2019/8/26 9:34
	 * @Param [generalLedgerQueryDto, response]
	 */
	@Override
	public void exportDetailLedgerExcel(GeneralLedgerQueryDto generalLedgerQueryDto, HttpServletResponse response) {
		List<GeneralLedgerVo> generalLedgerVoList = findDetailLedgerList(generalLedgerQueryDto);
		fileService.exportExcel(response, generalLedgerVoList,
				ExcelColumnConstant.GeneralLedger.SUBJECT_CODE,
				ExcelColumnConstant.GeneralLedger.SUBJECT_NAME,
				ExcelColumnConstant.GeneralLedger.ACCOUNT_BOOK_NAME,
				ExcelColumnConstant.GeneralLedger.ACCOUNT_BOOK_ENTITY_NAME,
				ExcelColumnConstant.GeneralLedger.VOUCHER_DATE,
				ExcelColumnConstant.GeneralLedger.PERIOD_YEAR,
				ExcelColumnConstant.GeneralLedger.PERIOD_NUM,
				ExcelColumnConstant.GeneralLedger.VOUCHER_WORD,
				ExcelColumnConstant.GeneralLedger.SUMMARY,
				ExcelColumnConstant.GeneralLedger.DEBIT,
				ExcelColumnConstant.GeneralLedger.CREDIT,
				ExcelColumnConstant.GeneralLedger.BALANCE_DIRECTION,
				ExcelColumnConstant.GeneralLedger.BALANCE);
	}

	/**
	 * @Description 通过制单日期设置查询范围
	 * @Author 郑勇浩
	 * @Data 2019/10/9 16:41
	 * @Param []
	 * @return void
	 */
	private boolean setParamByVoucherDate(GeneralLedgerQueryDto param) {
		// 查询ids
		List<GeneralLedgerVo> param2 = generalLedgerMapper.findDetailInDataIds(param);
		if (CollectionUtils.isEmpty(param2) || param2.get(0) == null) {
			return false;
		}
		param.setVoucherIdList(new ArrayList<>());
		// 循环数据
		for (GeneralLedgerVo nowData : param2) {
			// 设置范围内的voucherId
			param.getVoucherIdList().add(nowData.getVoucherId());

			// 设置最小日期 最大日期
			if (param.getPeriodYearNum1() == 0 || param.getPeriodYearNum1() > nowData.getPeriodYearNum1()) {
				param.setPeriodYearNum1(nowData.getPeriodYearNum1());
			}
			if (param.getPeriodYearNum2() == 0 || param.getPeriodYearNum2() < nowData.getPeriodYearNum2()) {
				param.setPeriodYearNum2(nowData.getPeriodYearNum2());
			}
		}
		return true;
	}

	/**
	 * @Description 查询会计科目列表
	 * @Author 郑勇浩
	 * @Data 2019/10/14 9:15
	 * @Param [generalLedgerQueryDto]
	 * @return java.util.List<com.njwd.entity.platform.vo.AccountSubjectVo>
	 */
	private List<AccountSubjectVo> findAccountSubjectList(GeneralLedgerQueryDto param) {
		//如果level是0,则为最大值和最小值
		param.setLevel1(param.getLevel1() == Constant.Number.ZERO ? Constant.Level.ONE : param.getLevel1());
		param.setLevel2(param.getLevel2() == Constant.Number.ZERO ? Constant.Level.EIGHT : param.getLevel2());

		AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
		accountSubjectDto.setRootEnterpriseId(param.getRootEnterpriseId());
		//是否仅显示末级科目(0:否 1:是)
		if (param.getIsFinal() == Constant.Number.INITIAL) {
			accountSubjectDto.setIsFinal(param.getIsFinal());
		}
		//是否包含禁用科目(0:不包含 1:包含)
		accountSubjectDto.setIsIncludeEnable(param.getIsIncludeEnable());

		if (!CollectionUtils.isEmpty(param.getAccountCodes())) {
			//如果会计科目 等于 不为空
			accountSubjectDto.setCodes(param.getAccountCodes());
		} else if (StringUtil.isNotBlank(param.getAccountCode1()) && StringUtil.isNotBlank(param.getAccountCode2())) {
			//通过区间查询区间 区间 内的所有的code
			List<AccountSubjectVo> returnCodeList = findAccountSubjectCodeList(param);
			if (CollectionUtils.isEmpty(returnCodeList)) {
				return null;
			}
			accountSubjectDto.setCodes(returnCodeList.stream().map(AccountSubjectVo::getCode).collect(Collectors.toList()));
		}

		//获取条件内的数据
		List<AccountSubjectVo> accountSubjectList = accountSubjectFeignClient.findInfoForLedger(accountSubjectDto).getData();
		if (accountSubjectList == null) {
			return null;
		}
		//获取存在的科目ID
		param.setAccountSubjectIds(accountSubjectList.stream().map(AccountSubjectVo::getId).collect(Collectors.toList()));
		return accountSubjectList;
	}

	/**
	 * @Description 通过区间查询区间区间内的所有的code
	 * @Author 郑勇浩
	 * @Data 2019/10/23 16:06
	 * @Param []
	 */
	private List<AccountSubjectVo> findAccountSubjectCodeList(GeneralLedgerQueryDto param) {
		//　如果会计科目 区间 不为空
		AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
		// 查询的code区间
		List<String> codeList = new ArrayList<>();
		codeList.add(param.getAccountCode1());
		codeList.add(param.getAccountCode2());
		accountSubjectDto.setCodes(codeList);
		// 要查询的level区间
		List<Integer> levelList = new ArrayList<>();
		levelList.add(param.getLevel1());
		levelList.add(param.getLevel2());
		accountSubjectDto.setSubjectLevels(levelList);
		// 是否包含禁用科目(0:不包含 1:包含)
		accountSubjectDto.setIsIncludeEnable(param.getIsIncludeEnable());
		// 是否仅显示末级科目(0:否 1:是)
		if (param.getIsFinal() == Constant.Number.INITIAL) {
			accountSubjectDto.setIsFinal(param.getIsFinal());
		}
		// 科目表ID 对应【科目】表ID
		accountSubjectDto.setSubjectId(param.getSubjectId());
		// 会计科目编码查询类型
		accountSubjectDto.setSubjectCodeOperator(Constant.Number.INITIAL);
		// 根据条件查询未删除的科目信息
		AccountSubjectVo returnVo = accountSubjectFeignClient.findSubjectInfoByParam(accountSubjectDto).getData();
		if (returnVo == null) {
			return null;
		}
		return returnVo.getAccountSubjectList();
	}

	/**
	 * @Description 组装code为key的HasMap数据
	 * @Author 郑勇浩
	 * @Data 2019/9/30 14:21
	 * @Param [dataList, accountSubjectList, param]
	 * @return java.util.HashMap<java.lang.String, java.util.List < com.njwd.entity.ledger.vo.GeneralLedgerVo>>
	 */
	private HashMap<String, List<GeneralLedgerVo>> formatCodeMap(List<GeneralLedgerVo> dataList, List<AccountSubjectVo> accountSubjectList, GeneralLedgerQueryDto param) {
		HashMap<String, List<GeneralLedgerVo>> returnMap = new HashMap<>();

		//上一条数据的科目编码 账簿信息
		AccountSubjectVo accountSubject = null;
		AccountBookEntityDto accountBookEntity = null;
		//循环数据
		for (GeneralLedgerVo gl : dataList) {
			//判断是否是新的账簿 赋值新的账簿信息
			if (accountBookEntity == null || !accountBookEntity.getId().equals(gl.getAccountBookEntityId())) {
				accountBookEntity = getAccountBookEntity(gl.getAccountBookEntityId(), param.getAccountBookEntityList());
				if (accountBookEntity == null) {
					continue;
				}
			}

			//判断是否是新的科目 赋值新的科目信息
			if (accountSubject == null || !accountSubject.getId().equals(gl.getAccountSubjectId())) {
				accountSubject = getAccountSubject(accountSubjectList, gl.getAccountSubjectId(), null, null, param.getFullNameFlag());
				if (accountSubject == null) {
					continue;
				}
				returnMap.put(accountSubject.getCode(), new ArrayList<>());
			}

			//为对象赋值账簿信息
			gl.setAccountBookName(accountBookEntity.getAccountBookName());
			gl.setAccountBookEntityName(accountBookEntity.getEntityName());
			//为对象赋值科目信息
			gl.setSubjectCode(accountSubject.getCode());
			gl.setSubjectName(accountSubject.getName());
			gl.setUpCode(accountSubject.getUpCode());
			gl.setLevel(accountSubject.getLevel());
			gl.setBalanceDirection(accountSubject.getBalanceDirection());
			returnMap.get(accountSubject.getCode()).add(gl);

			//向父节点 添加该数据
			setDataToParentCode(returnMap, gl, gl.getUpCode(), accountSubjectList, param);
		}
		return returnMap;
	}

	/**
	 * @Description 向父节点添加数据
	 * @Author 郑勇浩
	 * @Data 2019/10/9 11:22
	 * @Param [codeMap, childData, upCode, accountSubjectList]
	 */
	private void setDataToParentCode(HashMap<String, List<GeneralLedgerVo>> codeMap, GeneralLedgerVo childData, String upCode, List<AccountSubjectVo> accountSubjectList, GeneralLedgerQueryDto param) {
		GeneralLedgerVo nowData;
		//NOT NULL
		if (childData == null || StringUtil.isBlank(upCode)) {
			return;
		}
		//查询父级科目信息
		AccountSubject accountSubject = getAccountSubject(accountSubjectList, null, upCode, codeMap, param.getFullNameFlag());
		if (accountSubject == null) {
			return;
		}

		//如果为空则新生成 否则重新计算原有数据
		List<GeneralLedgerVo> parentList = codeMap.get(upCode);
		if (CollectionUtils.isEmpty(parentList)) {
			//新的map key
			parentList = new ArrayList<>();
			nowData = cloneGeneralLedgerVo(childData, accountSubject, null, null);
			nowData.setDebit(childData.getSelfDebit());
			nowData.setCredit(childData.getSelfCredit());
			parentList.add(nowData);
			codeMap.put(upCode, parentList);
		} else {
			//添加计算父类数据
			nowData = addParentCodeData(codeMap.get(upCode), childData);
		}
		//继续
		setDataToParentCode(codeMap, childData, nowData.getUpCode(), accountSubjectList, param);
	}

	/**
	 * @Description 判断账簿年月为父级数据赋值
	 * @Author 郑勇浩
	 * @Data 2019/9/29 19:52
	 * @Param [parentList, childList]
	 * @return void
	 */
	private GeneralLedgerVo addParentCodeData(List<GeneralLedgerVo> parentList, GeneralLedgerVo childGl) {
		//添加的数据，返回使用
		GeneralLedgerVo returnData;

		//判断添加位置
		for (int i = 0; i < parentList.size(); i++) {
			GeneralLedgerVo parentGl = parentList.get(i);
			//判断 总账账簿
			if (parentGl.getAccountBookId() > childGl.getAccountBookId()) {
				returnData = childDataToParentData(childGl, parentGl, true);
				parentList.add(i, returnData);
				return returnData;
			} else if (parentGl.getAccountBookId() < childGl.getAccountBookId()) {
				continue;
			}
			//判断 账簿主体
			if (parentGl.getAccountBookEntityId() > childGl.getAccountBookEntityId()) {
				returnData = childDataToParentData(childGl, parentGl, true);
				parentList.add(i, returnData);
				return returnData;
			} else if (parentGl.getAccountBookEntityId() < childGl.getAccountBookEntityId()) {
				continue;
			}
			//判断年份
			if (parentGl.getPeriodYear() > childGl.getPeriodYear()) {
				returnData = childDataToParentData(childGl, parentGl, true);
				parentList.add(i, returnData);
				return returnData;
			} else if (parentGl.getPeriodYear() < childGl.getPeriodYear()) {
				continue;
			}
			//判断月份
			if (parentGl.getPeriodNum() > childGl.getPeriodNum()) {
				returnData = childDataToParentData(childGl, parentGl, true);
				parentList.add(i, returnData);
				return returnData;
			} else if (parentGl.getPeriodNum() < childGl.getPeriodNum()) {
				continue;
			}
			//判断凭证
			if (childGl.getVoucherId() == null && parentGl.getVoucherId() != null) {
				returnData = childDataToParentData(childGl, parentGl, true);
				parentList.add(i, returnData);
				return returnData;
			} else if (childGl.getVoucherId() != null && parentGl.getVoucherId() == null) {
				continue;
			} else if (childGl.getVoucherId() != null && parentGl.getVoucherId() != null) {
				//判断凭证时间
				if (parentGl.getVoucherDateTime() > childGl.getVoucherDateTime()) {
					returnData = childDataToParentData(childGl, parentGl, true);
					parentList.add(i, returnData);
					return returnData;
				} else if (parentGl.getVoucherDateTime() < childGl.getVoucherDateTime()) {
					continue;
				}

				//判断凭证id
				if (parentGl.getVoucherId() > childGl.getVoucherId()) {
					returnData = childDataToParentData(childGl, parentGl, true);
					parentList.add(i, returnData);
					return returnData;
				} else {
					continue;
				}
			}

			//如果都相同则累加
			returnData = childDataToParentData(childGl, parentGl, false);
			parentList.set(i, returnData);
			return returnData;
		}
		//否则添加到最后
		returnData = childDataToParentData(childGl, parentList.get(0), true);
		parentList.add(returnData);
		return returnData;
	}

	/**
	 * @Description 格式化输出账簿
	 * @Author 郑勇浩
	 * @Data 2019/10/14 15:28
	 * @Param [hashMap, param, accountSubjectList, type]
	 * @return java.util.List<com.njwd.entity.ledger.vo.GeneralLedgerVo>
	 */
	private List<GeneralLedgerVo> formatReportForms(HashMap<String, List<GeneralLedgerVo>> hashMap, GeneralLedgerQueryDto param, List<AccountSubjectVo> accountSubjectList, Integer type) {
		List<GeneralLedgerVo> returnData = new LinkedList<>();

		//是否需要 期初 期末
		boolean needOpen;
		boolean needClose;
		//本期合计 本年累计
		int voucherCount = 0;
		BigDecimal[] middleList = new BigDecimal[]{Constant.Number.ZEROB, Constant.Number.ZEROB, Constant.Number.ZEROB};
		BigDecimal[] finishList = new BigDecimal[]{Constant.Number.ZEROB, Constant.Number.ZEROB, Constant.Number.ZEROB};

		//根据accountSubject排序 计算 生成期初期中期末
		GeneralLedgerVo openGv;
		GeneralLedgerVo nowData;
		//明细分类账 分支过滤数据
		AccountSubjectVo checkAccountSubject = null;
		if (type.equals(Constant.SortNum.DETAIL) && param.getAccountCodes() != null && param.getAccountCodes().size() == Constant.Number.ONE) {
			checkAccountSubject = getAccountSubject(accountSubjectList, null, param.getAccountCodes().get(0), hashMap, param.getFullNameFlag());
		}
		for (AccountSubjectVo accountSubject : accountSubjectList) {
			//超出范围则跳过
			if (accountSubject.getLevel() < param.getLevel1() || accountSubject.getLevel() > param.getLevel2()) {
				continue;
			}
			//如果Map中不存在对应code则跳过
			if (hashMap.get(accountSubject.getCode()) == null) {
				continue;
			}
			//明细分类账 分支判断 过滤
			if (checkAccountSubject != null && !accountSubject.getLevel().equals(checkAccountSubject.getLevel())) {
				continue;
			}

			List<GeneralLedgerVo> nowList = hashMap.get(accountSubject.getCode());
			for (int i = 0; i < nowList.size(); i++) {
				nowData = nowList.get(i);

				//加上自身值
				nowData.setDebit(nowData.getSelfDebit().add(nowData.getDebit()));
				nowData.setCredit(nowData.getSelfCredit().add(nowData.getCredit()));
				nowData.setSelfDebit(Constant.Number.ZEROB);
				nowData.setSelfCredit(Constant.Number.ZEROB);

				//判断是否需要期初期中期末
				needOpen = needOpen(nowData, nowList, i);
				needClose = needClose(nowData, nowList, i, type);

				//计算
				if (needOpen) {
					middleList = new BigDecimal[]{Constant.Number.ZEROB, Constant.Number.ZEROB, sumOpeningBalance(nowData, i, nowList)};
					finishList = new BigDecimal[]{Constant.Number.ZEROB, Constant.Number.ZEROB, Constant.Number.ZEROB};
				}
				calc(nowData);

				//期中数据 期末数据
				middleList[0] = middleList[0].add(nowData.getDebit());
				middleList[1] = middleList[1].add(nowData.getCredit());
				finishList[0] = finishList[0].add(nowData.getDebit());
				finishList[1] = finishList[1].add(nowData.getCredit());

				//期初余额
				if (needOpen) {
					if (type.equals(Constant.SortNum.PERIOD_NUM) && !needCode(nowData, nowList, i)) {
						nowData.setSubjectCode(null);
						nowData.setSubjectName(null);
					}
					openGv = cloneGeneralLedgerVo(nowData, null, null, Constant.SortNum.OPENING);
					openGv.setBalance(middleList[2]);
					returnData.add(openGv);
				}
				//明细
				nowData.setOpeningBalance(middleList[2]);
				nowData.setType(Constant.SortNum.DETAIL);
				calc(nowData);
				//是否需要添加余额数据
				if (nowData.getVoucherId() != null) {
					voucherCount++;
					if (type.equals(Constant.SortNum.DETAIL)) {
						returnData.add(nowData);
					}
				}
				middleList[2] = nowData.getBalance();
				nowData.setBalanceDirection(nowData.getBalance().compareTo(BigDecimal.ZERO) == Constant.Number.ZERO ? Constant.BalanceDirection.FLAT : nowData.getBalanceDirection());

				if (needClose) {
					if (type.equals(Constant.SortNum.PERIOD_NUM)) {
						nowData.setSubjectCode(null);
						nowData.setSubjectName(null);
					}
					//期末
					GeneralLedgerVo gl;
					gl = cloneGeneralLedgerVo(nowData, null, null, Constant.SortNum.PERIOD_NUM);
					gl.setDebit(middleList[0]);
					gl.setCredit(middleList[1]);
					gl.setBalance(middleList[2]);
					gl.setBalanceDirection(gl.getBalance().compareTo(BigDecimal.ZERO) == Constant.Number.ZERO ? Constant.BalanceDirection.FLAT : gl.getBalanceDirection());
					gl.setVoucherCount(voucherCount);
					returnData.add(gl);

					middleList[0] = Constant.Number.ZEROB;
					middleList[1] = Constant.Number.ZEROB;
					voucherCount = Constant.Number.ZERO;
					middleList[2] = gl.getBalance();
					finishList[2] = gl.getBalance();
					//年末
					gl = cloneGeneralLedgerVo(nowData, null, null, Constant.SortNum.PERIOD_YEAR);
					gl.setDebit(finishList[0]);
					gl.setCredit(finishList[1]);
					gl.setBalance(finishList[2]);
					gl.setBalanceDirection(gl.getBalance().compareTo(BigDecimal.ZERO) == Constant.Number.ZERO ? Constant.BalanceDirection.FLAT : gl.getBalanceDirection());
					returnData.add(gl);
				}
			}

			//查询到末尾code则不再继续
			if (StringUtil.isNotBlank(param.getAccountCode2()) && param.getAccountCode2().equals(accountSubject.getCode())) {
				break;
			}
		}
		return returnData;
	}

	/**
	 * @Description 是否需要期初
	 * @Author 郑勇浩
	 * @Data 2019/10/14 17:12
	 * @Param [nowData, dataList, index]
	 * @return boolean
	 */
	private boolean needOpen(GeneralLedgerVo nowData, List<GeneralLedgerVo> dataList, int index) {
		if (index == 0) {
			return true;
		}
		GeneralLedgerVo prevData = dataList.get(index - Constant.Number.ONE);
		return !(nowData.getAccountBookEntityId().equals(prevData.getAccountBookEntityId())
				&& nowData.getPeriodYear() == prevData.getPeriodYear());
	}

	/**
	 * @Description 是否需要科目编码
	 * @Author 郑勇浩
	 * @Data 2019/10/14 17:12
	 * @Param [nowData, dataList, index, type]
	 * @return boolean
	 */
	private boolean needCode(GeneralLedgerVo nowData, List<GeneralLedgerVo> dataList, int index) {
		if (index == 0) {
			return true;
		}
		GeneralLedgerVo prevData = dataList.get(index - Constant.Number.ONE);
		return !nowData.getAccountBookEntityId().equals(prevData.getAccountBookEntityId());
	}

	/**
	 * @Description 是否需要期末年末
	 * @Author 郑勇浩
	 * @Data 2019/10/14 17:19
	 * @Param [nowData, nowList, i, type]
	 * @return boolean
	 */
	private boolean needClose(GeneralLedgerVo nowData, List<GeneralLedgerVo> dataList, int index, Integer type) {
		if (index == dataList.size() - Constant.Number.ONE) {
			return true;
		}
		GeneralLedgerVo nextData = dataList.get(index + Constant.Number.ONE);
		return !nowData.getAccountBookEntityId().equals(nextData.getAccountBookEntityId())
				|| nowData.getPeriodYear() != nextData.getPeriodYear()
				|| nowData.getPeriodNum() != nextData.getPeriodNum();
	}

	/**
	 * @Description 过滤数据
	 * @Author 郑勇浩
	 * @Data 2019/9/30 14:40
	 * @Param [formatData, param]
	 */
	private void filterData(List<GeneralLedgerVo> formatData, GeneralLedgerQueryDto param) {
		//上一个期初的位置
		int prevOpen = 0;
		int prevYear = 0;
		int i = 0;
		GeneralLedgerVo nowData;
		GeneralLedgerVo nextData;
		String subjectCode = null;
		String subjectName = null;
		//判断是否有制单日期数据列表
		boolean hasVoucherList = !CollectionUtils.isEmpty(param.getVoucherIdList());
		while (i < formatData.size()) {
			nowData = formatData.get(i);

			if (StringUtil.isNotBlank(nowData.getSubjectCode())) {
				subjectCode = nowData.getSubjectCode();
				subjectName = nowData.getSubjectName();
			}

			//如果是第零期
			if (formatData.get(i).getPeriodNum() == 0) {
				//如果是最后三笔数据
				if (i + Constant.Number.THREE >= formatData.size() && formatData.get(i).getType() == Constant.SortNum.OPENING) {
					formatData.remove(i);
					formatData.remove(i);
					formatData.remove(i);
					break;
				}

				//如果不是零期的年末累计则删除
				if (formatData.get(i).getType() != Constant.SortNum.PERIOD_YEAR) {
					formatData.remove(i);
					continue;
				}
				//否则把年末变为期初
				nextData = formatData.get(i + Constant.Number.ONE);
				nowData.setSubjectCode(subjectCode);
				nowData.setSubjectName(subjectName);
				changeToOpeningData(nowData, nextData);
				continue;
			}

			//如果是期初则记录期初位置
			if (nowData.getType() == Constant.SortNum.OPENING) {
				prevOpen = i;
				prevYear = prevOpen;
				//如果这是最后一笔数据
				if (i + 1 == formatData.size()) {
					formatData.remove(i);
					return;
				}
				//如果只有三笔数据
				if (formatData.size() < Constant.Number.THREE) {
					formatData.clear();
					return;
				}
				//如果下一笔数据也是期初
				if (formatData.get(i + 1).getType() == Constant.SortNum.OPENING) {
					formatData.remove(i);
					continue;
				}
				i++;
				continue;
			}
			//记录上一个年末位置
			if (nowData.getType() == Constant.SortNum.PERIOD_YEAR) {
				prevYear = i;
			}

			//如果有制单日期 判断凭证是否在制单日期内
			if (hasVoucherList && nowData.getType() == Constant.SortNum.DETAIL && !param.getVoucherIdList().contains(nowData.getVoucherId())) {
				formatData.remove(nowData);
				//回到期初
				i = prevOpen;
				continue;
			}

			//如果本期无发生
			boolean happenNo = param.getShowCondition().equals(LedgerConstant.ReportShowCondition.HAPPEN_NO) &&
					nowData.getType() == Constant.SortNum.PERIOD_NUM &&
					nowData.getVoucherCount() == Constant.Number.ZERO;
			//余额为零且本期无发生不显示
			boolean happenBalanceNo = param.getShowCondition().equals(LedgerConstant.ReportShowCondition.HAPPEN_BALANCE_NO) &&
					nowData.getType() == Constant.SortNum.PERIOD_NUM &&
					nowData.getVoucherCount() == Constant.Number.ZERO &&
					nowData.getOpeningBalance().compareTo(BigDecimal.ZERO) == Constant.Number.ZERO;
			if (happenNo || happenBalanceNo) {
				if (nowData.getCredit().compareTo(BigDecimal.ZERO) == Constant.Number.ZERO
						&& nowData.getDebit().compareTo(BigDecimal.ZERO) == Constant.Number.ZERO) {
					for (int j = 0; j < (i - prevYear) + 1; j++) {
						formatData.remove(prevYear + 1);
					}
					//回到期初
					i = prevYear;
					continue;
				}
			} else if (param.getShowCondition().equals(LedgerConstant.ReportShowCondition.BALANCE_NO)
					&& nowData.getType() == Constant.SortNum.PERIOD_YEAR
					&& nowData.getBalance().compareTo(BigDecimal.ZERO) == Constant.Number.ZERO) {
				//余额为零不显示
				for (int j = 0; j < (i - prevOpen); j++) {
					formatData.remove(prevOpen + 1);
				}
				//回到期初
				i = prevOpen;
				continue;
			}
			i++;
		}
	}

	/**
	 * @Description 设置日期区间
	 * @Author 郑勇浩
	 * @Data 2019/10/9 14:59
	 * @Param [formatData, param]
	 */
	private void filterDataByDate(List<GeneralLedgerVo> formatData, GeneralLedgerQueryDto param) {
		//日期过滤
		int i = 0;
		String subjectCode = null;
		String subjectName = null;
		GeneralLedgerVo nowData;
		GeneralLedgerVo nextData;
		//如果空则判断日期，否则判断id是否存在
		if (CollectionUtils.isEmpty(param.getVoucherIdList())) {
			while (i < formatData.size()) {
				nowData = formatData.get(i);

				if (StringUtil.isNotBlank(nowData.getSubjectCode())) {
					subjectCode = nowData.getSubjectCode();
					subjectName = nowData.getSubjectName();
				}

				if (nowData.getPeriodYearNum() < param.getPeriodYearNum1() || nowData.getPeriodYearNum() > param.getPeriodYearNum2()) {
					if (nowData.getType() == Constant.SortNum.PERIOD_YEAR && (i + 1) < formatData.size()) {
						nextData = formatData.get(i + 1);
						nextData.setSubjectCode(subjectCode);
						nextData.setSubjectName(subjectName);
						changeToOpeningData(nowData, nextData);
					} else {
						formatData.remove(nowData);
						continue;
					}
				}
				i++;
			}
		}
	}

	/**
	 * @Description 数据克隆，防止污染原有数据
	 * @Author 郑勇浩
	 * @Data 2019/10/14 14:35
	 * @Param [sourceData, accountSubject, parentData, type]
	 * @return com.njwd.entity.ledger.vo.GeneralLedgerVo
	 */
	private GeneralLedgerVo cloneGeneralLedgerVo(GeneralLedgerVo sourceData, AccountSubject accountSubject, GeneralLedgerVo parentData, Integer type) {
		GeneralLedgerVo targetData = new GeneralLedgerVo();
		//判断科目信息继承方式
		if (accountSubject != null) {
			targetData.setAccountSubjectId(accountSubject.getId());
			targetData.setSubjectCode(accountSubject.getCode());
			targetData.setSubjectName(accountSubject.getName());
			targetData.setUpCode(accountSubject.getUpCode());
			targetData.setLevel(accountSubject.getLevel());
			targetData.setBalanceDirection(accountSubject.getBalanceDirection());
		} else if (parentData != null) {
			targetData.setAccountSubjectId(parentData.getAccountSubjectId());
			targetData.setSubjectCode(parentData.getSubjectCode());
			targetData.setSubjectName(parentData.getSubjectName());
			targetData.setUpCode(parentData.getUpCode());
			targetData.setLevel(parentData.getLevel());
			targetData.setBalanceDirection(parentData.getBalanceDirection());
		} else {
			targetData.setAccountSubjectId(sourceData.getAccountSubjectId());
			targetData.setSubjectCode(sourceData.getSubjectCode());
			targetData.setSubjectName(sourceData.getSubjectName());
			targetData.setUpCode(sourceData.getUpCode());
			targetData.setLevel(sourceData.getLevel());
			targetData.setBalanceDirection(sourceData.getBalanceDirection());
		}
		//克隆原有信息
		targetData.setAccountBookId(sourceData.getAccountBookId());
		targetData.setAccountBookName(sourceData.getAccountBookName());
		targetData.setAccountBookEntityId(sourceData.getAccountBookEntityId());
		targetData.setAccountBookEntityName(sourceData.getAccountBookEntityName());
		targetData.setPeriodYear(sourceData.getPeriodYear());
		targetData.setPeriodNum(sourceData.getPeriodNum());
		targetData.setPeriodYearNum(sourceData.getPeriodYearNum());
		//明细用
		if (sourceData.getVoucherId() != null) {
			targetData.setVoucherId(sourceData.getVoucherId());
			targetData.setVoucherDate(sourceData.getVoucherDate());
			targetData.setVoucherDateTime(sourceData.getVoucherDateTime());
			targetData.setVoucherWord(sourceData.getVoucherWord());
			targetData.setMainCode(sourceData.getMainCode());
		}
		//根据类型赋值
		if (type != null) {
			targetData.setDebit(Constant.Number.ZEROB);
			targetData.setCredit(Constant.Number.ZEROB);
			targetData.setVoucherWord(null);
			targetData.setMainCode(null);
			//期初数据　期末 年末
			if (type.equals(Constant.SortNum.OPENING)) {
				targetData.setType(Constant.SortNum.OPENING);
				targetData.setSummary(Constant.ReportFormRowType.OPENING);
				targetData.setBalance(Constant.Number.ZEROB);
				targetData.setBalanceDirection(sourceData.getOpeningBalance().compareTo(BigDecimal.ZERO) == Constant.Number.ZERO ? Constant.BalanceDirection.FLAT : sourceData.getBalanceDirection());
				if (targetData.getVoucherDate() != null) {
					targetData.setVoucherDate(sourceData.getVoucherDate().substring(0, 8) + "01");
				}
			} else if (type.equals(Constant.SortNum.PERIOD_NUM)) {
				targetData.setType(Constant.SortNum.PERIOD_NUM);
				targetData.setSummary(Constant.ReportFormRowType.PERIOD_NUM);
				targetData.setOpeningBalance(targetData.getOpeningBalance());
				targetData.setVoucherDate(null);
			} else if (type.equals(Constant.SortNum.PERIOD_YEAR)) {
				targetData.setType(Constant.SortNum.PERIOD_YEAR);
				targetData.setSummary(Constant.ReportFormRowType.PERIOD_YEAR);
				targetData.setVoucherDate(null);
			}
		} else {
			targetData.setOpeningBalance(sourceData.getOpeningBalance());
			targetData.setType(sourceData.getType());
			targetData.setSummary(sourceData.getSummary());
			targetData.setDebit(sourceData.getDebit());
			targetData.setCredit(sourceData.getCredit());
			targetData.setBalance(sourceData.getBalance());
		}
		return targetData;
	}

	/**
	 * @Description 将数据改为年初数据
	 * @Author 郑勇浩
	 * @Data 2019/10/24 11:42
	 * @Param [generalLedgerVo]
	 */
	private void changeToOpeningData(GeneralLedgerVo nowData, GeneralLedgerVo nextData) {
		nowData.setDebit(Constant.Number.ZEROB);
		nowData.setCredit(Constant.Number.ZEROB);
		nowData.setPeriodYearNum(nextData.getPeriodYearNum());
		nowData.setPeriodNum(nextData.getPeriodNum());
		nowData.setType(Constant.SortNum.OPENING);
		nowData.setSummary(Constant.ReportFormRowType.OPENING);
		nowData.setBalanceDirection(nowData.getBalance().compareTo(BigDecimal.ZERO) == Constant.Number.ZERO ? Constant.BalanceDirection.FLAT : nowData.getBalanceDirection());
	}

	/**
	 * @Description 获取对应科目信息
	 * @Author 郑勇浩
	 * @Data 2019/9/29 19:05
	 * @Param [id, code, accountSubjectList]
	 * @return com.njwd.entity.platform.vo.AccountSubjectVo
	 */
	private AccountSubjectVo getAccountSubject(List<AccountSubjectVo> accountSubjectList, Long id, String code, HashMap<String, List<GeneralLedgerVo>> codeMap, byte fullNameFlag) {
		//如果不是空的 先从codeMap里面获取，如果没有再去循环获取
		if (code != null) {
			List<GeneralLedgerVo> codeList = codeMap.get(code);
			if (!CollectionUtils.isEmpty(codeList)) {
				GeneralLedgerVo nowDate = codeList.get(0);
				AccountSubjectVo accountSubject = new AccountSubjectDto<>();
				accountSubject.setId(nowDate.getAccountSubjectId());
				accountSubject.setCode(nowDate.getSubjectCode());
				accountSubject.setSubjectName(nowDate.getSubjectName());
				accountSubject.setUpCode(nowDate.getUpCode());
				accountSubject.setLevel(nowDate.getLevel());
				accountSubject.setBalanceDirection((byte) nowDate.getBalanceDirection());
				return accountSubject;
			}
		}

		//如果有ID则根据ID查询 没有则根据CODE查询
		boolean idEquals;
		boolean codeEquals;
		for (AccountSubjectVo accountSubject : accountSubjectList) {
			idEquals = id != null && id.equals(accountSubject.getId());
			codeEquals = code != null && code.equals(accountSubject.getCode());
			if (idEquals || codeEquals) {
				//科目全名
				if (fullNameFlag == Constant.Number.ONEB) {
					accountSubject.setName(accountSubject.getFullName());
				}
				return accountSubject;
			}
		}
		return null;
	}

	/**
	 * @Description 获取对应账簿信息
	 * @Author 郑勇浩
	 * @Data 2019/10/9 10:55
	 * @Param [id, accountBookEntityList]
	 * @return com.njwd.entity.basedata.dto.AccountBookEntityDto
	 */
	private AccountBookEntityDto getAccountBookEntity(Long accountBookEntityId, List<AccountBookEntityDto> accountBookEntityList) {
		for (AccountBookEntityDto accountBookEntity : accountBookEntityList) {
			if (accountBookEntityId.equals(accountBookEntity.getId())) {
				return accountBookEntity;
			}
		}
		return null;
	}

	/**
	 * @Description 子类复制给父类
	 * @Author 郑勇浩
	 * @Data 2019/9/30 14:10
	 * @Param [childData, parentData, isNew]
	 * @return com.njwd.entity.ledger.vo.GeneralLedgerVo
	 */
	private GeneralLedgerVo childDataToParentData(GeneralLedgerVo childData, GeneralLedgerVo parentData, Boolean isNew) {
		GeneralLedgerVo returnData;

		//判断是克隆 还是 累加
		if (isNew) {
			returnData = cloneGeneralLedgerVo(childData, null, parentData, null);
			//期初还是子集的
			returnData.setOpeningBalance(Constant.Number.ZEROB);
			returnData.setDebit(Constant.Number.ZEROB);
			returnData.setCredit(Constant.Number.ZEROB);
		} else {
			returnData = parentData;
		}

		//如果借贷方向不同
		if (parentData.getBalanceDirection() != childData.getBalanceDirection()) {
			returnData.setOpeningBalance(returnData.getOpeningBalance().subtract(childData.getOpeningBalance()));
			returnData.setDebit(returnData.getDebit().add(childData.getSelfDebit()));
			returnData.setCredit(returnData.getCredit().add(childData.getSelfCredit()));
		} else {
			returnData.setOpeningBalance(returnData.getOpeningBalance().add(childData.getOpeningBalance()));
			returnData.setDebit(returnData.getDebit().add(childData.getSelfDebit()));
			returnData.setCredit(returnData.getCredit().add(childData.getSelfCredit()));
		}
		//计算
//		calc(returnData);
		return returnData;
	}

	/**
	 * @Description 计算期初值
	 * @Author 郑勇浩
	 * @Data 2019/10/15 10:25
	 * @Param [nowData, dataList]
	 * @return void
	 */
	private BigDecimal sumOpeningBalance(GeneralLedgerVo nowData, int index, List<GeneralLedgerVo> dataList) {
		if (index == dataList.size() - 1) {
			return nowData.getOpeningBalance();
		}
		BigDecimal returnDouble = Constant.Number.ZEROB;
		GeneralLedgerVo gv;
		for (; index < dataList.size(); index++) {
			gv = dataList.get(index);
			if (gv.getAccountBookEntityId().equals(nowData.getAccountBookEntityId()) && gv.getPeriodYear() == nowData.getPeriodYear() && gv.getPeriodNum() == nowData.getPeriodNum()) {
				returnDouble = returnDouble.add(gv.getOpeningBalance());
			} else {
				return returnDouble;
			}
		}
		return returnDouble;
	}

	/**
	 * @Description 计算
	 * @Author 郑勇浩
	 * @Data 2019/9/28 18:01
	 * @Param [generalLedgerVo]
	 */
	private void calc(GeneralLedgerVo gv) {
		if (gv.getBalanceDirection() == Constant.BalanceDirection.DEBIT) {
			gv.setBalance(gv.getOpeningBalance().add(gv.getSelfDebit()).add(gv.getDebit()).subtract(gv.getSelfCredit()).subtract(gv.getCredit()));
		} else if (gv.getBalanceDirection() == Constant.BalanceDirection.CREDIT) {
			gv.setBalance(gv.getOpeningBalance().add(gv.getSelfCredit()).add(gv.getCredit()).subtract(gv.getSelfDebit()).subtract(gv.getDebit()));
		}
	}

}
