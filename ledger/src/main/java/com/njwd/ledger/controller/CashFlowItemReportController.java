package com.njwd.ledger.controller;

import com.njwd.entity.ledger.dto.BalanceCashFlowQueryDto;
import com.njwd.entity.ledger.dto.QueryVoucherEntryDto;
import com.njwd.entity.ledger.vo.BalanceCashFlowInitVo;
import com.njwd.entity.ledger.vo.BalanceCashFlowVo;
import com.njwd.entity.ledger.vo.VoucherEntryCashFlowVo;
import com.njwd.entity.ledger.vo.VoucherEntryVo;
import com.njwd.ledger.service.CashFlowItemReportService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.FastUtils;
import com.njwd.utils.UserUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName CashFlowItemReportController
 * @Description 现金流量项目报表Controller
 * @Author libao
 * @Date 2019/8/5 11:33
 */
@RestController
@RequestMapping("cashFlowItemReport")
public class CashFlowItemReportController extends BaseController {

	@Resource
	private CashFlowItemReportService cashFlowItemReportService;


    /**
     * @Author Libao
     * @Description 查询现金流量项目
     * @Date  2019/8/6 15:14
     * @Param [voucherEntryDto]
     * @return com.njwd.support.Result<java.util.List<com.njwd.entity.ledger.vo.VoucherEntryVo>>
     */
	@PostMapping("findCashFlowItemReport")
	public Result<List<VoucherEntryVo>> findCashFlowItemReport(@RequestBody QueryVoucherEntryDto queryVoucherEntryDto){
		//校验用户是否登陆
		UserUtils.getUserVo();
		//必传参数
		FastUtils.checkParams(queryVoucherEntryDto.getPeriodOperator(),
				queryVoucherEntryDto.getPeriodYears(),
				queryVoucherEntryDto.getPeriodNumbers(),
				queryVoucherEntryDto.getAccountBookIdOperator(),
				queryVoucherEntryDto.getAccountBookIds(),
				queryVoucherEntryDto.getAccountBookEntityIdOperator(),
				queryVoucherEntryDto.getAccountBookEntityIds(),
				queryVoucherEntryDto.getIsIncludeUnbooked()
				);
		//查询现金流量项目
		List<VoucherEntryVo> velist = cashFlowItemReportService.findCashFlowItemReport(queryVoucherEntryDto);
		return ok(velist);
	}

	/**
	 * @Author Libao
	 * @Description 查询现金流量项目明细
	 * @Date  2019/8/9 10:20
	 * @Param [queryVoucherEntryDto]
	 * @return com.njwd.support.Result<java.util.List<com.njwd.entity.ledger.vo.VoucherEntryVo>>
	 */
	@PostMapping("findCashFlowItemDetailReport")
	public Result<List<VoucherEntryVo>> findCashFlowItemDetailReport(@RequestBody QueryVoucherEntryDto queryVoucherEntryDto){
		//校验用户是否登陆
		UserUtils.getUserVo();
		//必传参数
		FastUtils.checkParams(queryVoucherEntryDto.getIsIncludeUnbooked(),
				queryVoucherEntryDto.getAccountBookIdOperator(),
				queryVoucherEntryDto.getAccountBookIds(),
				queryVoucherEntryDto.getAccountBookEntityIdOperator(),
				queryVoucherEntryDto.getAccountBookEntityIds(),
				queryVoucherEntryDto.getPeriodOperator(),
				queryVoucherEntryDto.getPeriodYears(),
				queryVoucherEntryDto.getPeriodNumbers());
		//查询现金流量项目明细
		List<VoucherEntryVo> velist = cashFlowItemReportService.findCashFlowItemDetailReport(queryVoucherEntryDto);
		return ok(velist);
	}
	/**
	 * @Author Libao
	 * @Description  现金流量汇总
	 * @Date  2019/8/6 16:05
	 * @Param [balanceCashFlowDto]
	 * @return com.njwd.support.Result<java.util.List<com.njwd.entity.ledger.vo.BalanceCashFlowVo>>
	 */
	@PostMapping("findBalanceCashFlowList")
	public Result<List<BalanceCashFlowVo>> findBalanceCashFlowList(@RequestBody BalanceCashFlowQueryDto balanceCashFlowQueryDto){
		//校验用户是否登陆
		UserUtils.getUserVo();
		//必传参数
		FastUtils.checkParams(balanceCashFlowQueryDto.getIsIncludeUnbooked(),
				balanceCashFlowQueryDto.getAccountBookIdOperator(),
				balanceCashFlowQueryDto.getAccountBookIds(),
				balanceCashFlowQueryDto.getAccountBookEntityIdOperator(),
				balanceCashFlowQueryDto.getAccountBookEntityIds(),
				balanceCashFlowQueryDto.getPeriodOperator(),
				balanceCashFlowQueryDto.getPeriodYears(),
				balanceCashFlowQueryDto.getPeriodNumbers(),
				balanceCashFlowQueryDto.getAccountBookEntityList());
		List<BalanceCashFlowVo> list = cashFlowItemReportService.findBalanceCashFlowList(balanceCashFlowQueryDto);
		return  ok(list);
	}

	/**
	 * @Author Libao
	 * @Description  现金流量项目汇总导出
	 * @Date  2019/8/27 9:46
	 * @Param [balanceCashFlowQueryDto, response]
	 * @return void
	 */
	@RequestMapping("exportBalanceCashFlowList")
	public void exportBalanceCashFlowList(@RequestBody BalanceCashFlowQueryDto balanceCashFlowQueryDto, HttpServletResponse response){
		//校验用户是否登陆
		UserUtils.getUserVo();
		//必传参数
		FastUtils.checkParams(balanceCashFlowQueryDto.getIsIncludeUnbooked(),
				balanceCashFlowQueryDto.getAccountBookIdOperator(),
				balanceCashFlowQueryDto.getAccountBookIds(),
				balanceCashFlowQueryDto.getAccountBookEntityIdOperator(),
				balanceCashFlowQueryDto.getAccountBookEntityIds(),
				balanceCashFlowQueryDto.getPeriodOperator(),
				balanceCashFlowQueryDto.getPeriodYears(),
				balanceCashFlowQueryDto.getPeriodNumbers(),
				balanceCashFlowQueryDto.getIsIncludeTotalAmount(),
				balanceCashFlowQueryDto.getAccountBookEntityList());
		cashFlowItemReportService.exportBalanceCashFlowList(balanceCashFlowQueryDto,response);

	}

	/**
	 * @Author Libao
	 * @Description  现金流量项目明细导出
	 * @Date  2019/8/27 9:46
	 * @Param [balanceCashFlowQueryDto, response]
	 * @return void
	 */
	@RequestMapping("exportCashFlowItemDetail")
	public void exportCashFlowItemDetail(@RequestBody QueryVoucherEntryDto queryVoucherEntryDto, HttpServletResponse response){
		//校验用户是否登陆
		UserUtils.getUserVo();
		//必传参数
		FastUtils.checkParams(queryVoucherEntryDto.getIsIncludeUnbooked(),
				queryVoucherEntryDto.getAccountBookIdOperator(),
				queryVoucherEntryDto.getAccountBookIds(),
				queryVoucherEntryDto.getAccountBookEntityIdOperator(),
				queryVoucherEntryDto.getAccountBookEntityIds(),
				queryVoucherEntryDto.getPeriodOperator(),
				queryVoucherEntryDto.getPeriodYears(),
				queryVoucherEntryDto.getPeriodNumbers());
		cashFlowItemReportService.exportCashFlowItemDetail(queryVoucherEntryDto,response);

	}

	/**
	 * @Author Libao
	 * @Description 现金流量报表导出
	 * @Date  2019/8/28 11:08
	 * @Param [queryVoucherEntryDto, response]
	 * @return void
	 */
	@RequestMapping("exportCashFlowItemReport")
	public void exportCashFlowItemReport(@RequestBody QueryVoucherEntryDto queryVoucherEntryDto, HttpServletResponse response){
		//校验用户是否登陆
		UserUtils.getUserVo();
		//必传参数
		FastUtils.checkParams(queryVoucherEntryDto.getPeriodOperator(),
				queryVoucherEntryDto.getPeriodYears(),
				queryVoucherEntryDto.getPeriodNumbers(),
				queryVoucherEntryDto.getAccountBookIdOperator(),
				queryVoucherEntryDto.getAccountBookIds(),
				queryVoucherEntryDto.getAccountBookEntityIdOperator(),
				queryVoucherEntryDto.getAccountBookEntityIds(),
				queryVoucherEntryDto.getIsIncludeUnbooked()
		);
		cashFlowItemReportService.exportCashFlowItemReport(queryVoucherEntryDto,response);
	}

	/**
	 * @Author Libao
	 * @Description 查询现金流量项目Id
	 * @Date  2019/9/5 15:33
	 * @Param []
	 * @return com.njwd.support.Result<com.njwd.entity.ledger.vo.BalanceCashFlowVo>
	 */
	@RequestMapping("findCashFlowItemUsed")
	public Result<BalanceCashFlowVo> findCashFlowItemUsed(){
		BalanceCashFlowVo voForCashItemId = new BalanceCashFlowVo();
		List<BalanceCashFlowVo> balanceCashFlowVos =cashFlowItemReportService.findCashFlowItemId();
		List<BalanceCashFlowInitVo> balanceCashFlowInitVos = cashFlowItemReportService.findCashFlowItemIdFromInit();
		List<VoucherEntryCashFlowVo> voucherEntryCashFlowVos =  cashFlowItemReportService.findCashFlowItemIdFromVoucher();
		List<Long> ids = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(balanceCashFlowVos)){
			for (BalanceCashFlowVo balanceCashFlowVo: balanceCashFlowVos){
				ids.add(balanceCashFlowVo.getItemId());
			}
		}
		if (CollectionUtils.isNotEmpty(balanceCashFlowInitVos)){
			for (BalanceCashFlowInitVo balanceCashFlowInitVo : balanceCashFlowInitVos){
				ids.add(balanceCashFlowInitVo.getItemId());

			}
		}
		if (CollectionUtils.isNotEmpty(voucherEntryCashFlowVos)){
			for (VoucherEntryCashFlowVo voucherEntryCashFlowVo : voucherEntryCashFlowVos){
				ids.add(voucherEntryCashFlowVo.getCashFlowItemId());
			}
		}
		voForCashItemId.setIds(ids);
		return ok(voForCashItemId);
	}


}
