package com.njwd.ledger.service.impl;

import com.njwd.common.Constant;
import com.njwd.common.ExcelColumnConstant;
import com.njwd.common.LedgerConstant;
import com.njwd.entity.basedata.dto.AccountBookEntityDto;
import com.njwd.entity.ledger.BalanceCashFlowInit;
import com.njwd.entity.ledger.dto.BalanceCashFlowQueryDto;
import com.njwd.entity.ledger.dto.BalanceInitDto;
import com.njwd.entity.ledger.dto.QueryVoucherEntryDto;
import com.njwd.entity.ledger.vo.BalanceCashFlowInitVo;
import com.njwd.entity.ledger.vo.BalanceCashFlowVo;
import com.njwd.entity.ledger.vo.VoucherEntryCashFlowVo;
import com.njwd.entity.ledger.vo.VoucherEntryVo;
import com.njwd.entity.platform.dto.AccountSubjectDto;
import com.njwd.entity.platform.dto.CashFlowItemDto;
import com.njwd.entity.platform.vo.AccountSubjectVo;
import com.njwd.entity.platform.vo.CashFlowItemVo;
import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import com.njwd.ledger.cloudclient.AccountSubjectFeignClient;
import com.njwd.ledger.cloudclient.CashFlowReportClient;
import com.njwd.ledger.mapper.BalanceCashFlowInitMapper;
import com.njwd.ledger.mapper.BalanceCashFlowMapper;
import com.njwd.ledger.mapper.VoucherEntryCashFlowMapper;
import com.njwd.ledger.mapper.VoucherEntryMapper;
import com.njwd.ledger.service.CashFlowItemReportService;
import com.njwd.service.FileService;
import com.njwd.support.Result;
import com.njwd.utils.MergeUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @ClassName CashFlowItemReportServiceImpl
 * @Description 现金流量项目实现类
 * @Author libao
 * @Date 2019/8/5 11:37
 */
@Service
public class CashFlowItemReportServiceImpl implements CashFlowItemReportService {

	@Resource
	private BalanceCashFlowMapper balanceCashFlowMapper;

	@Resource
	private BalanceCashFlowInitMapper balanceCashFlowInitMapper;

	@Resource
	private VoucherEntryMapper voucherEntryMapper;

	@Resource
	private VoucherEntryCashFlowMapper voucherEntryCashFlowMapper;

	@Resource
	private CashFlowReportClient cashFlowReportClient;

	@Resource
	private AccountSubjectFeignClient accountSubjectFeignClient;
	@Resource
	private FileService fileService;


	/**
	 * @return java.util.List<com.njwd.entity.ledger.vo.BalanceCashFlowVo>
	 * @Author Libao
	 * @Description 查询现金流量汇总
	 * @Date 2019/8/29 15:54
	 * @Param [balanceCashFlowQueryDto]
	 */
	@Override
	public List<BalanceCashFlowVo> findBalanceCashFlowList(BalanceCashFlowQueryDto balanceCashFlowQueryDto) {
		//基础资料现金流量数据
		Result<List<CashFlowItemVo>> resultCashList = cashFlowReportClient.findCashFlowItemForReport(new CashFlowItemDto());
		List<CashFlowItemVo> cList = resultCashList.getData();
		//查询现金流量汇总数据本期
		List<BalanceCashFlowVo> bcList = balanceCashFlowMapper.findBalanceCashFlowList(balanceCashFlowQueryDto);
		//判断是否需要显示本年累计，需要则拼接本年累计
		if (Constant.Is.YES.equals(balanceCashFlowQueryDto.getIsIncludeTotalAmount())) {
			//查询现金流量汇总数据本年
			List<BalanceCashFlowVo> totalAmountList = balanceCashFlowMapper.findBalanceCashFlowTotalAmount(balanceCashFlowQueryDto);
			//查询现金流量余额初始化
			BalanceInitDto balanceInitDto = new BalanceInitDto();
			List<BalanceCashFlowInit> inits =balanceCashFlowInitMapper.selectBalanceCashFlowInitList(balanceInitDto);
			//如果本期累计和本年累计都为空
			if (CollectionUtils.isEmpty(bcList) && CollectionUtils.isEmpty(totalAmountList)) {
				return bcList;
			}
			//都不为空，拼接信息
			if (CollectionUtils.isNotEmpty(bcList) && CollectionUtils.isNotEmpty(totalAmountList)) {
				if (CollectionUtils.isNotEmpty(inits)){
					MergeUtil.merge(totalAmountList,inits,
							ts->ts.getAccountBookId() + "_" + ts.getAccountBookEntityId() + "_" + ts.getItemId(),init->init.getAccountBookId() + "_" + init.getAccountBookEntityId() + "_" + init.getItemId(),
							(ts, init)->{
								if (!init.getPeriodNum().equals(Constant.Number.INITIAL) && init.getPeriodYear().equals(balanceCashFlowQueryDto.getPeriodYears().get(1))){
									BigDecimal total = ts.getTotalAmount().add(init.getOpeningBalance());
									ts.setTotalAmount(total);
								}
							}
					);
				}
				MergeUtil.merge(bcList, totalAmountList,
						 bs->bs.getItemId() + "_" + bs.getAccountBookId() + "_" + bs.getAccountBookEntityId(),ts->ts.getItemId() + "_" + ts.getAccountBookId() + "_" + ts.getAccountBookEntityId(),
						//(bs, ts) -> ts.getItemId().equals(bs.getItemId()) && ts.getAccountBookId().equals(bs.getAccountBookId()) && ts.getAccountBookEntityId().equals(bs.getAccountBookEntityId()),
						(bs, ts) -> bs.setTotalAmount(ts.getTotalAmount()));
			} else if (CollectionUtils.isEmpty(bcList)) {
				bcList = new ArrayList<>();
				bcList.addAll(totalAmountList);
			}
		}

		List<BalanceCashFlowVo> resultList = new ArrayList<>();
		//拼接现金流量项目信息
		MergeUtil.merge(bcList, cList,
				 bs->bs.getItemId(),cs->cs.getId(),
				//(bs, cs) -> cs.getId().equals(bs.getItemId()),
				(bs, cs) -> {
					bs.setCode(cs.getCode());
					bs.setName(cs.getName());
					bs.setCashFlowDirection(cs.getCashFlowDirection());
					bs.setCashFlowId(cs.getCashFlowId());
					bs.setLevel(cs.getLevel());
				});
		//统计上级现金流量项目金额
		MergeUtil.mergeList(cList, bcList,
				(cash, bc) -> bc.getCode().indexOf(cash.getCode()) == 0 && cash.getCashFlowId().equals(bc.getCashFlowId()),
				(cash, bList) -> {
					if (bList.size() > 0) {
						//根据accountBookEntityId分组
						//Map<String, List<BalanceCashFlowVo>> collect = bList.stream().collect(Collectors.groupingBy(balanceCashFlowVo -> String.format("%s_%s_%s", balanceCashFlowVo.getAccountBookId(), balanceCashFlowVo.getAccountBookEntityId(),balanceCashFlowVo.getItemId())));
						Map<String, List<BalanceCashFlowVo>> collect = bList.stream().collect(Collectors.groupingBy(balanceCashFlowVo -> balanceCashFlowVo.getAccountBookEntityId().toString()));
						List<BalanceCashFlowVo> balanceCashFlowVos;
						for (String key : collect.keySet()) {
							//本期累计
							BigDecimal occurAmount = new BigDecimal(0);
							//本年累计
							BigDecimal totalAmount = new BigDecimal(0);
							balanceCashFlowVos = collect.get(key);
							for (BalanceCashFlowVo balanceCashFlowVo : balanceCashFlowVos) {
								if (balanceCashFlowVo.getOccurAmount() == null) {
									balanceCashFlowVo.setOccurAmount(new BigDecimal(0));
								}
								if (balanceCashFlowVo.getTotalAmount() == null) {
									balanceCashFlowVo.setTotalAmount(new BigDecimal(0));
								}
								//方向为流出
								if (balanceCashFlowVo.getCashFlowDirection() == Constant.CashFlowDirection.OUT) {
									//如果不是一级，则为一级以下，方向为流出
									if (!Constant.Level.ONE.equals(cash.getLevel())) {
										occurAmount = occurAmount.add(balanceCashFlowVo.getOccurAmount());
										if (Constant.Is.YES.equals(balanceCashFlowQueryDto.getIsIncludeTotalAmount())) {
											totalAmount = totalAmount.add(balanceCashFlowVo.getTotalAmount());
										}
									} else {
										//是一级数据，流出为负
										occurAmount = occurAmount.subtract(balanceCashFlowVo.getOccurAmount());
										if (Constant.Is.YES.equals(balanceCashFlowQueryDto.getIsIncludeTotalAmount())) {
											totalAmount = totalAmount.subtract(balanceCashFlowVo.getTotalAmount());
										}
									}
								} else if (balanceCashFlowVo.getCashFlowDirection() == Constant.CashFlowDirection.IN) {
									//数据为流入都为加
									occurAmount = occurAmount.add(balanceCashFlowVo.getOccurAmount());
									if (Constant.Is.YES.equals(balanceCashFlowQueryDto.getIsIncludeTotalAmount())) {
										totalAmount = totalAmount.add(balanceCashFlowVo.getTotalAmount());
									}
								}
							}
							BalanceCashFlowVo balanceCashFlowVo = new BalanceCashFlowVo();
							balanceCashFlowVo.setAccountBookEntityId(balanceCashFlowVos.get(0).getAccountBookEntityId());
							balanceCashFlowVo.setAccountBookId(balanceCashFlowVos.get(0).getAccountBookId());
							balanceCashFlowVo.setName(cash.getName());
							balanceCashFlowVo.setCode(cash.getCode());
							balanceCashFlowVo.setLevel(cash.getLevel());
							balanceCashFlowVo.setCashFlowId(balanceCashFlowVos.get(0).getCashFlowId());
							balanceCashFlowVo.setOccurAmount(occurAmount);
							if (Constant.Is.YES.equals(balanceCashFlowQueryDto.getIsIncludeTotalAmount())) {
								balanceCashFlowVo.setTotalAmount(totalAmount);
							}
							resultList.add(balanceCashFlowVo);
						}
					}
				}
		);
		//加上末级数据
		//resultList.addAll(bcList);
		//本期累计
		/*Map<StringBuffer,BigDecimal> map = new HashMap<>();
		//本年累计
		Map<StringBuffer,BigDecimal> map2 = new HashMap<>();
		for(BalanceCashFlowVo balanceCashFlowVo : resultList){
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append(balanceCashFlowVo.getAccountBookId()).append(",").append(balanceCashFlowVo.getAccountBookEntityId()).append(",").append(balanceCashFlowVo.getCode());
			if (map.containsKey(stringBuffer)){
				map.put(stringBuffer,map.get(stringBuffer).add(balanceCashFlowVo.getOccurAmount()));
			}else{
				map.put(stringBuffer,balanceCashFlowVo.getOccurAmount());
			}
			if (map2.containsKey(stringBuffer)){
				map2.put(stringBuffer,map.get(stringBuffer).add(balanceCashFlowVo.getTotalAmount()));
			}else{
				map2.put(stringBuffer,balanceCashFlowVo.getTotalAmount());
			}
		}*/

		/*List<BalanceCashFlowVo> resultList1 = new ArrayList<>();
		BalanceCashFlowVo balanceCashFlowVo = new BalanceCashFlowVo();
		for(Map.Entry<StringBuffer, BigDecimal> entry : map.entrySet()){
			String [] entity = entry.getKey().toString().split(",");
			balanceCashFlowVo.setAccountBookId(Long.parseLong(entity[0]));
			balanceCashFlowVo.setAccountBookEntityId(Long.parseLong(entity[1]));
			balanceCashFlowVo.setCode(entity[2]);
			balanceCashFlowVo.setOccurAmount(entry.getValue());
			resultList1.add(balanceCashFlowVo);
		}
		for(Map.Entry<StringBuffer, BigDecimal> entry1 : map2.entrySet()){
			String [] entity1 = entry1.getKey().toString().split(",");
			balanceCashFlowVo.setAccountBookId(Long.parseLong(entity[0]));
			balanceCashFlowVo.setAccountBookEntityId(Long.parseLong(entity[1]));
			balanceCashFlowVo.setCode(entity[2]);
			balanceCashFlowVo.setOccurAmount(entry.getValue());
			resultList1.add(balanceCashFlowVo);
		}*/
		//排序

		Collections.sort(resultList, Comparator.comparing(BalanceCashFlowVo::getAccountBookId).thenComparing(BalanceCashFlowVo::getAccountBookEntityId).thenComparing(BalanceCashFlowVo::getCode));

		//拼接账簿信息
		if (CollectionUtils.isNotEmpty(resultList)) {
			getAccBookAndEntityName(balanceCashFlowQueryDto, resultList);
		}
		return resultList;
	}

	/**
	 * @return java.util.List<com.njwd.entity.ledger.vo.VoucherEntryVo>
	 * @Author Libao
	 * @Description 查询现金流量报表
	 * @Date 2019/8/6 15:22
	 * @Param [queryVoucherEntryDto]
	 */
	@Override
	public List<VoucherEntryVo> findCashFlowItemReport(QueryVoucherEntryDto queryVoucherEntryDto) {
		//基础资料现金流量数据
		Result<List<CashFlowItemVo>> resultCashList = cashFlowReportClient.findCashFlowItemForReport(new CashFlowItemDto());
		List<CashFlowItemVo> cList = resultCashList.getData();

		//现金流量项目分录
		List<VoucherEntryVo> vListCashTemp = voucherEntryMapper.findCashFlowItemCashReport(queryVoucherEntryDto);
		//根据分录Id去重
		List<VoucherEntryVo> vListCash = vListCashTemp.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o-> o.getId()))),ArrayList::new));
		//非现金流量项目
		List<VoucherEntryVo> vListUnCash = voucherEntryMapper.findCashFlowItemUnCashReport(queryVoucherEntryDto);
		//合并结果，包含现金非现金信息
		vListCash.addAll(vListUnCash);
		if (CollectionUtils.isEmpty(vListCash)) {
			return vListCash;
		}
		//排序
		Collections.sort(vListCash, Comparator.comparing(VoucherEntryVo::getVoucherId).thenComparing(VoucherEntryVo::getRowNum));
		//查询基础资料科目信息
		List<AccountSubjectVo> sList = getSubjectInfo(vListCash);
		//拼接现金流量信息
		if (cList != null && !cList.isEmpty()) {
			MergeUtil.merge(vListCash, cList,
					vs->vs.getCashFlowItemId(), cs->cs.getId(),
					//(vs, cs) -> cs.getId().equals(vs.getCashFlowItemId()),
					(vs, cs) -> {
						vs.setCode(cs.getCode());
						vs.setName(cs.getName());
					});
		}

		//拼接科目信息
		if (sList != null && !sList.isEmpty()) {
			MergeUtil.merge(vListCash, sList,
					vs->vs.getAccountSubjectId(),ss-> ss.getId(),
					//(vs, ss) -> ss.getId().equals(vs.getAccountSubjectId()),
					(vs, ss) -> {
						vs.setSubjectName(ss.getName());
						vs.setSubjectCode(ss.getCode());
						vs.setSubjectFullName(ss.getFullName());
					});
		}
		return vListCash;
	}

	/**
	 * @param queryVoucherEntryDto
	 * @return java.util.List<com.njwd.entity.ledger.vo.VoucherEntryVo>
	 * @Author Libao
	 * @Description 查询现金流量项目明细
	 * @Date 2019/8/9 10:00
	 * @Param [queryVoucherEntryDto]
	 */
	@Override
	public List<VoucherEntryVo> findCashFlowItemDetailReport(QueryVoucherEntryDto queryVoucherEntryDto) {
		//凭证分录现金流量分析明细
		List<VoucherEntryVo> vList = voucherEntryMapper.findCashFlowItemDetailReport(queryVoucherEntryDto);
		if (CollectionUtils.isEmpty(vList)) {
			return vList;
		}
		List<VoucherEntryVo> resultList = new LinkedList<>();
		//创建集合Ids用于调基础资料现金流量项目接口
		List<Long> ids = new ArrayList<>();
		for (VoucherEntryVo voucherEntryVo : vList) {
			ids.add(voucherEntryVo.getCashFlowItemId());
		}
		//集合去重
		List<Long> newIds = ids.stream().distinct().collect(Collectors.toList());
		CashFlowItemDto cashFlowItemDto = new CashFlowItemDto();
		cashFlowItemDto.setIds(newIds);
		//基础资料现金流量数据
		Result<List<CashFlowItemVo>> resultCashList = cashFlowReportClient.findCashFlowItemForReport(cashFlowItemDto);
		List<CashFlowItemVo> cList = resultCashList.getData();
		//查询基础资料科目信息
		List<AccountSubjectVo> sList = getSubjectInfo(vList);
		//拼接现金流量信息
		MergeUtil.merge(vList, cList,
				vs->vs.getCashFlowItemId(),cs->cs.getId(),
				//(vs, cs) -> cs.getId().equals(vs.getCashFlowItemId()),
				(vs, cs) -> {
					vs.setCode(cs.getCode());
					vs.setName(cs.getName());
					vs.setCashFlowId(cs.getCashFlowId());
					vs.setCashFlowDirection(cs.getCashFlowDirection());
				});
		//拼接科目信息
		if (sList != null && !sList.isEmpty()) {
			MergeUtil.merge(vList, sList,
					vs->vs.getAccountSubjectId(),ss -> ss.getId(),
					//(vs, ss) -> ss.getId().equals(vs.getAccountSubjectId()),
					(vs, ss) -> {
						vs.setSubjectName(ss.getCode() + "/" + ss.getName());
						vs.setSubjectFullName(ss.getCode() + "/" + ss.getFullName());
						vs.setAuxiliaryNames(ss.getAuxiliaryNames());
					});
		}
		//统计上级现金流量项目金额
		MergeUtil.mergeList(cList, vList,
				(cash, ve) -> ve.getCode().indexOf(cash.getCode()) == 0 && cash.getCashFlowId().equals(ve.getCashFlowId()),
				(cash, veList) -> {
					if (veList.size() > 0) {
						//根据accountBookEntityId分组
						Map<String, List<VoucherEntryVo>> collect = veList.stream().collect(Collectors.groupingBy(e -> e.getAccountBookEntityId().toString()));
						List<VoucherEntryVo> voucherEntryVoList;
						BigDecimal upAmount;
						for (String key : collect.keySet()) {
							upAmount = new BigDecimal(0);
							voucherEntryVoList = collect.get(key);
							for (VoucherEntryVo voucherEntryVo : voucherEntryVoList) {
								if (voucherEntryVo.getCurrencyAmount() == null) {
									voucherEntryVo.setCurrencyAmount(new BigDecimal(0));
								}
								//方向为流出
								if (voucherEntryVo.getCashFlowDirection() == Constant.CashFlowDirection.OUT) {
									//如果不是一级，则为一级以下，方向为流出
									if (!Constant.Level.ONE.equals(cash.getLevel())) {
										upAmount = upAmount.add(voucherEntryVo.getCurrencyAmount());
									} else {
										//是一级数据，流出为负
										upAmount = upAmount.subtract(voucherEntryVo.getCurrencyAmount());
									}
								} else if (voucherEntryVo.getCashFlowDirection() == Constant.CashFlowDirection.IN) {
									//数据为流入都为加
									upAmount = upAmount.add(voucherEntryVo.getCurrencyAmount());
								}
							}
							VoucherEntryVo voucherEntryVo = new VoucherEntryVo();
							voucherEntryVo.setAccountBookEntityId(voucherEntryVoList.get(0).getAccountBookEntityId());
							voucherEntryVo.setAccountBookId(voucherEntryVoList.get(0).getAccountBookId());
							voucherEntryVo.setAccountBookName(voucherEntryVoList.get(0).getAccountBookName());
							voucherEntryVo.setAccountBookEntityName(voucherEntryVoList.get(0).getAccountBookEntityName());
							voucherEntryVo.setName(cash.getName());
							voucherEntryVo.setCode(cash.getCode());
							voucherEntryVo.setCashFlowId(voucherEntryVoList.get(0).getCashFlowId());
							voucherEntryVo.setCurrencyAmount(upAmount);
							resultList.add(voucherEntryVo);
						}
					}
				}
		);
		resultList.addAll(vList);
		//排序
		Collections.sort(resultList, Comparator.comparing(VoucherEntryVo::getAccountBookId).thenComparing(VoucherEntryVo::getAccountBookEntityId).thenComparing(VoucherEntryVo::getCode));
		return resultList;
	}


	/**
	 * @return java.util.List<com.njwd.entity.ledger.BalanceCashFlow>
	 * @Author Libao
	 * @Description 查询现金流量项目Id
	 * @Date 2019/9/5 14:34
	 * @Param []
	 */
	@Override
	public List<BalanceCashFlowVo> findCashFlowItemId() {
		return balanceCashFlowMapper.findCashFlowItemId();
	}

	/**
	 * @return java.util.List<BalanceCashFlowInit>
	 * @Author Libao
	 * @Description 查询现金流量项目id
	 * @Date 2019/9/5 14:34
	 * @Param []
	 */
	@Override
	public List<BalanceCashFlowInitVo> findCashFlowItemIdFromInit() {
		return balanceCashFlowInitMapper.findCashFlowItemIdFromInit();
	}

	/**
	 * @return java.util.List<com.njwd.entity.ledger.VoucherEntryCashFlow>
	 * @Author Libao
	 * @Description 查询现金流量项目Id
	 * @Date 2019/9/5 14:35
	 * @Param []
	 */
	@Override
	public List<VoucherEntryCashFlowVo> findCashFlowItemIdFromVoucher() {
		return voucherEntryCashFlowMapper.findCashFlowItemIdFromVoucher();
	}

	/**
	 * @param balanceCashFlowQueryDto
	 * @param response
	 * @return void
	 * @Author Libao
	 * @Description 导出现金流量汇总数据
	 * @Date 2019/8/27 9:25
	 * @Param [balanceCashFlowQueryDto, response]
	 */
	@Override
	public void exportBalanceCashFlowList(BalanceCashFlowQueryDto balanceCashFlowQueryDto, HttpServletResponse response) {
		List<BalanceCashFlowVo> list = findBalanceCashFlowList(balanceCashFlowQueryDto);
		//判断是否有值
		checkExportData(list);
		//拼接账簿信息
		getAccBookAndEntityName(balanceCashFlowQueryDto, list);
		if (Constant.Is.YES.equals(balanceCashFlowQueryDto.getIsIncludeTotalAmount())) {
			fileService.exportExcel(response,
					list,
					LedgerConstant.LedgerExportName.LEDGER_CASH_FLOW_ITEM_COLLECT,
					ExcelColumnConstant.CashFlowItemReport.ACCOUNT_BOOK_NAME,
					ExcelColumnConstant.CashFlowItemReport.ACCOUNT_BOOK_ENTITY_NAME,
					ExcelColumnConstant.CashFlowItemReport.NAME,
					ExcelColumnConstant.CashFlowItemReport.OCCUR_AMOUNT,
					ExcelColumnConstant.CashFlowItemReport.TOTAL_AMOUNT
			);
		} else {
			fileService.exportExcel(response,
					list,
					LedgerConstant.LedgerExportName.LEDGER_CASH_FLOW_ITEM_COLLECT,
					ExcelColumnConstant.CashFlowItemReport.ACCOUNT_BOOK_NAME,
					ExcelColumnConstant.CashFlowItemReport.ACCOUNT_BOOK_ENTITY_NAME,
					ExcelColumnConstant.CashFlowItemReport.NAME,
					ExcelColumnConstant.CashFlowItemReport.OCCUR_AMOUNT
			);
		}

	}

	/**
	 * @param queryVoucherEntryDto
	 * @param response
	 * @return void
	 * @Author Libao
	 * @Description 导出现金流量明细
	 * @Date 2019/8/27 14:25
	 * @Param [queryVoucherEntryDto, response]
	 */
	@Override
	public void exportCashFlowItemDetail(QueryVoucherEntryDto queryVoucherEntryDto, HttpServletResponse response) {
		List<VoucherEntryVo> list = findCashFlowItemDetailReport(queryVoucherEntryDto);
		checkExportData(list);
		//拼接凭证字号
		getCredentialWordCode(list);
		if (Constant.Is.YES.equals(queryVoucherEntryDto.getIsShowFullName())) {
			fileService.exportExcel(response,
					list,
					LedgerConstant.LedgerExportName.LEDGER_CASH_FLOW_ITEM_DETAIL,
					ExcelColumnConstant.CashFlowItemReport.ACCOUNT_BOOK_NAME,
					ExcelColumnConstant.CashFlowItemReport.ACCOUNT_BOOK_ENTITY_NAME,
					ExcelColumnConstant.CashFlowItemReport.NAME,
					ExcelColumnConstant.CashFlowItemReport.VOUCHER_DATE,
					ExcelColumnConstant.CashFlowItemReport.CREDENTIAL_WORD_CODE,
					ExcelColumnConstant.CashFlowItemReport.ABSTRACT_CONTENT,
					ExcelColumnConstant.CashFlowItemReport.SUBJECT_FULL,
					ExcelColumnConstant.CashFlowItemReport.AUXILIARY_NAMES,
					ExcelColumnConstant.CashFlowItemReport.CURRENCY_AMOUNT
			);
		} else {
			fileService.exportExcel(response,
					list,
					LedgerConstant.LedgerExportName.LEDGER_CASH_FLOW_ITEM_DETAIL,
					ExcelColumnConstant.CashFlowItemReport.ACCOUNT_BOOK_NAME,
					ExcelColumnConstant.CashFlowItemReport.ACCOUNT_BOOK_ENTITY_NAME,
					ExcelColumnConstant.CashFlowItemReport.NAME,
					ExcelColumnConstant.CashFlowItemReport.VOUCHER_DATE,
					ExcelColumnConstant.CashFlowItemReport.CREDENTIAL_WORD_CODE,
					ExcelColumnConstant.CashFlowItemReport.ABSTRACT_CONTENT,
					ExcelColumnConstant.CashFlowItemReport.SUBJECT,
					ExcelColumnConstant.CashFlowItemReport.AUXILIARY_NAMES,
					ExcelColumnConstant.CashFlowItemReport.CURRENCY_AMOUNT
			);

		}

	}


	/**
	 * @param queryVoucherEntryDto
	 * @param response
	 * @return void
	 * @Author Libao
	 * @Description 导出现金流量报表
	 * @Date 2019/8/28 10:00
	 * @Param [queryVoucherEntryDto, response]
	 */
	@Override
	public void exportCashFlowItemReport(QueryVoucherEntryDto queryVoucherEntryDto, HttpServletResponse response) {
		List<VoucherEntryVo> list = findCashFlowItemReport(queryVoucherEntryDto);
		checkExportData(list);
		//拼接凭证字号
		getCredentialWordCode(list);
		if (Constant.Is.YES.equals(queryVoucherEntryDto.getIsShowFullName())) {
			fileService.exportExcel(response,
					list,
					LedgerConstant.LedgerExportName.LEDGER_CASH_FLOW_ITEM,
					ExcelColumnConstant.CashFlowItemReport.ACCOUNT_BOOK_NAME,
					ExcelColumnConstant.CashFlowItemReport.ACCOUNT_BOOK_ENTITY_NAME,
					ExcelColumnConstant.CashFlowItemReport.VOUCHER_DATE,
					ExcelColumnConstant.CashFlowItemReport.CREDENTIAL_WORD_CODE,
					ExcelColumnConstant.CashFlowItemReport.ABSTRACT_CONTENT,
					ExcelColumnConstant.CashFlowItemReport.SUBJECT_CODE,
					ExcelColumnConstant.CashFlowItemReport.SUBJECT_FULL_NAME,
					ExcelColumnConstant.CashFlowItemReport.DEBIT_AMOUNT,
					ExcelColumnConstant.CashFlowItemReport.CREDIT_AMOUNT,
					ExcelColumnConstant.CashFlowItemReport.CURRENCY_AMOUNT,
					ExcelColumnConstant.CashFlowItemReport.CODE,
					ExcelColumnConstant.CashFlowItemReport.NAME
			);
		} else {
			fileService.exportExcel(response,
					list,
					LedgerConstant.LedgerExportName.LEDGER_CASH_FLOW_ITEM,
					ExcelColumnConstant.CashFlowItemReport.ACCOUNT_BOOK_NAME,
					ExcelColumnConstant.CashFlowItemReport.ACCOUNT_BOOK_ENTITY_NAME,
					ExcelColumnConstant.CashFlowItemReport.VOUCHER_DATE,
					ExcelColumnConstant.CashFlowItemReport.CREDENTIAL_WORD_CODE,
					ExcelColumnConstant.CashFlowItemReport.ABSTRACT_CONTENT,
					ExcelColumnConstant.CashFlowItemReport.SUBJECT_CODE,
					ExcelColumnConstant.CashFlowItemReport.SUBJECT_NAME,
					ExcelColumnConstant.CashFlowItemReport.DEBIT_AMOUNT,
					ExcelColumnConstant.CashFlowItemReport.CREDIT_AMOUNT,
					ExcelColumnConstant.CashFlowItemReport.CURRENCY_AMOUNT,
					ExcelColumnConstant.CashFlowItemReport.CODE,
					ExcelColumnConstant.CashFlowItemReport.NAME
			);
		}
	}

	/**
	 * @return void
	 * @Author Libao
	 * @Description 导出时判断是否有值
	 * @Date 2019/8/30 11:15
	 * @Param [list]
	 */
	private void checkExportData(List<?> list) {
		if (list == null || list.isEmpty()) {
			throw new ServiceException(ResultCode.CASH_FLOW_ITEM_NO_DATA_EXPORT);
		}
	}

	/**
	 * @return void
	 * @Author Libao
	 * @Description 凭证字号拼接
	 * @Date 2019/8/29 15:21
	 * @Param [list]
	 */
	private void getCredentialWordCode(List<VoucherEntryVo> list) {
		for (VoucherEntryVo voucherEntryVo : list) {
			if (voucherEntryVo.getCredentialWord() != null && voucherEntryVo.getMainCode() != null) {
				switch (voucherEntryVo.getCredentialWord()) {
					case 1:
						voucherEntryVo.setCredentialWordCode("记-" + voucherEntryVo.getMainCode());
						break;
					case 2:
						voucherEntryVo.setCredentialWordCode("收-" + voucherEntryVo.getMainCode());
						break;
					case 3:
						voucherEntryVo.setCredentialWordCode("付-" + voucherEntryVo.getMainCode());
						break;
					case 4:
						voucherEntryVo.setCredentialWordCode("转-" + voucherEntryVo.getMainCode());
						break;
					default:
						voucherEntryVo.setCredentialWordCode("其他");
						break;
				}
			}
		}
	}

	/**
	 * @return java.util.List<com.njwd.entity.platform.vo.AccountSubjectVo>
	 * @Author Libao
	 * @Description 查询基础资料科目信息
	 * @Date 2019/8/29 15:53
	 * @Param [vListCash]
	 */
	private List<AccountSubjectVo> getSubjectInfo(List<VoucherEntryVo> vListCash) {
		//基础资料科目数据拼接
		List<Long> subjectIds = new ArrayList<>();
		//获取科目Id作为参数，精确查询，提高效率
		for (VoucherEntryVo voucherEntryVo : vListCash) {
			subjectIds.add(voucherEntryVo.getAccountSubjectId());
		}
		List<Long> newSubjectIds = subjectIds.stream().distinct().collect(Collectors.toList());
		AccountSubjectDto accountSubjectDto = new AccountSubjectDto();
		accountSubjectDto.setIds(newSubjectIds);
		accountSubjectDto.setSubjectCodeOperator(Constant.Number.ANTI_INITLIZED);
		//查询辅助核算
		accountSubjectDto.setIfFindAuxiliary(Constant.Number.INITIAL);
		Result<List<AccountSubjectVo>> resultSubjectList = accountSubjectFeignClient.findInfoForLedger(accountSubjectDto);
		return resultSubjectList.getData();
	}

	/**
	 * @return void
	 * @Author Libao
	 * @Description 获取账簿名称，核算主体名称
	 * @Date 2019/8/29 15:56
	 * @Param [balanceCashFlowQueryDto, list]
	 */
	private void getAccBookAndEntityName(BalanceCashFlowQueryDto balanceCashFlowQueryDto, List<BalanceCashFlowVo> list) {
		//拼接核算账簿、核算主体信息
		List<AccountBookEntityDto> accountBookEntityList = balanceCashFlowQueryDto.getAccountBookEntityList();
		MergeUtil.merge(list, accountBookEntityList,
				balanceCashFlowVo->balanceCashFlowVo.getAccountBookEntityId(),accountBookEntity->accountBookEntity.getEntityId(),
				//(balanceCashFlowVo, accountBookEntity) -> balanceCashFlowVo.getAccountBookEntityId().equals(accountBookEntity.getEntityId()),
				(balanceCashFlowVo, accountBookEntity) -> {
					balanceCashFlowVo.setAccountBookEntityName(accountBookEntity.getEntityName());
					balanceCashFlowVo.setAccountBookName(accountBookEntity.getAccountBookName());
				}
		);
	}
}
