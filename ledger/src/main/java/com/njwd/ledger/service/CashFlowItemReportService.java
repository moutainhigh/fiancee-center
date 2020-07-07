package com.njwd.ledger.service;

import com.njwd.entity.ledger.dto.BalanceCashFlowQueryDto;
import com.njwd.entity.ledger.dto.QueryVoucherEntryDto;
import com.njwd.entity.ledger.vo.BalanceCashFlowInitVo;
import com.njwd.entity.ledger.vo.BalanceCashFlowVo;
import com.njwd.entity.ledger.vo.VoucherEntryCashFlowVo;
import com.njwd.entity.ledger.vo.VoucherEntryVo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
/**
 * @Author Libao
 * @Description CashFlowItemReportService 现金流量项目报表service
 * @Date  2019/8/30 17:22
 */
public interface CashFlowItemReportService {

   /**
    * @Author Libao
    * @Description 现金流量项目汇总报表
    * @Date  2019/8/30 17:22
    * @Param [balanceCashFlowQueryDto]
    * @return java.util.List<BalanceCashFlowVo>
    */
	List<BalanceCashFlowVo> findBalanceCashFlowList(BalanceCashFlowQueryDto balanceCashFlowQueryDto);
    /**
     * @Author Libao
     * @Description 查询现金流量报表
     * @Date  2019/8/6 15:22
     * @Param [queryVoucherEntryDto]
     * @return java.util.List<com.njwd.entity.ledger.vo.VoucherEntryVo>
     */
	List<VoucherEntryVo> findCashFlowItemReport(QueryVoucherEntryDto queryVoucherEntryDto);

    /**
     * @Author Libao
     * @Description 查询现金流量项目明细
     * @Date  2019/8/9 10:00
     * @Param [queryVoucherEntryDto]
     * @return java.util.List<com.njwd.entity.ledger.vo.VoucherEntryVo>
     */
	List<VoucherEntryVo>  findCashFlowItemDetailReport(QueryVoucherEntryDto queryVoucherEntryDto);


    /**
     * @Author Libao
     * @Description 查询现金流量项目Id
     * @Date  2019/9/5 14:34
     * @Param []
     * @return java.util.List<com.njwd.entity.ledger.BalanceCashFlow>
     */
	List<BalanceCashFlowVo>  findCashFlowItemId();

	/**
	 * @Author Libao
	 * @Description  查询现金流量项目id
	 * @Date  2019/9/5 14:34
	 * @Param []
	 * @return java.util.List<BalanceCashFlowInit>
	 */
	List<BalanceCashFlowInitVo>  findCashFlowItemIdFromInit();

	/**
	 * @Author Libao
	 * @Description 查询现金流量项目Id
	 * @Date  2019/9/5 14:35
	 * @Param []
	 * @return java.util.List<com.njwd.entity.ledger.VoucherEntryCashFlow>
	 */
	List<VoucherEntryCashFlowVo>  findCashFlowItemIdFromVoucher();

	/**
	 * @Author Libao
	 * @Description 导出现金流量汇总数据
	 * @Date  2019/8/27 9:25
	 * @Param [balanceCashFlowQueryDto, response]
	 * @return void
	 */
	void exportBalanceCashFlowList(BalanceCashFlowQueryDto balanceCashFlowQueryDto, HttpServletResponse response);

	/**
	 * @Author Libao
	 * @Description 导出现金流量明细
	 * @Date  2019/8/27 14:25
	 * @Param [queryVoucherEntryDto, response]
	 * @return void
	 */
	void exportCashFlowItemDetail(QueryVoucherEntryDto queryVoucherEntryDto, HttpServletResponse response);

    /**
     * @Author Libao
     * @Description 导出现金流量报表
     * @Date  2019/8/28 10:00
     * @Param [queryVoucherEntryDto, response]
     * @return void
     */
	void exportCashFlowItemReport(QueryVoucherEntryDto queryVoucherEntryDto, HttpServletResponse response);

}
