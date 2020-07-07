package com.njwd.ledger.api;

import com.njwd.entity.ledger.vo.BalanceCashFlowVo;
import com.njwd.exception.ServiceException;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @ClassName CashFlowItemLedgerApi
 * @Description 现金流量项目总账接口Api
 * @Author admin
 * @Date 2019/9/5 15:34
 */
@RequestMapping("ledger/cashFlowItemReport")
public interface CashFlowItemLedgerApi {

	/**
	 * @Author Libao
	 * @Description 查询现金流量项目Id
	 * @Date  2019/9/5 15:33
	 * @Param []
	 * @return com.njwd.support.Result<com.njwd.entity.ledger.vo.BalanceCashFlowVo>
	 */
	@RequestMapping("findCashFlowItemUsed")
	Result<BalanceCashFlowVo> findCashFlowItemUsed() throws ServiceException;

}
