package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.BalanceCashFlow;
import com.njwd.entity.ledger.BalanceCashFlowInit;
import com.njwd.entity.ledger.dto.BalanceCashFlowDto;
import com.njwd.entity.ledger.dto.BalanceCashFlowQueryDto;
import com.njwd.entity.ledger.dto.VoucherDto;
import com.njwd.entity.ledger.vo.BalanceCashFlowVo;
import com.njwd.entity.ledger.vo.PostPeriodBalanceVo;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/7/24
 */
public interface BalanceCashFlowMapper extends BaseMapper<BalanceCashFlow> {


	int addCashFlow(@Param("cash") BalanceCashFlowInit cash);

	int updateCashFlowAdd(@Param("cash") BalanceCashFlowInit cash);

	int updateCashFlowDel(@Param("cash") BalanceCashFlowInit cash);

	/**
	 * 更新发生额
	 *
	 * @param balanceCashFlows balanceCashFlows
	 * @param voucherDto       voucherDto
	 * @param updateType
	 * @return int
	 * @author xyyxhcj@qq.com
	 * @date 2019/8/13 20:05
	 **/
	int updateBatch(@Param("balanceCashFlows") Collection<BalanceCashFlowDto> balanceCashFlows, @Param("voucherDto") VoucherDto voucherDto, @Param("updateType") byte updateType);


	/**
	 * 过账时 修改现金流量过账金额
	 *
	 * @param cashFlowBalanceList
	 * @return
	 */
	int updateCashFlowBalanceForPostPeriod(@Param("cashFlowBalanceList") List<PostPeriodBalanceVo> cashFlowBalanceList);

	/**
	 * @description 过账 查询现金流量
	 * @author fancl
	 * @date 2019/8/22
	 * @param accountBookPeriod 账簿期间
	 * @return
	 */
	List<PostPeriodBalanceVo> findCashFlowBalanceBeforeUpdateForPostPeriod(@Param("accountBookPeriod") AccountBookPeriod accountBookPeriod);


	/**
	 * @Author Libao
	 * @Description 查询本期累计
	 * @Date  2019/9/2 10:00
	 * @Param [balanceCashFlowQueryDto]
	 * @return java.util.List<com.njwd.entity.ledger.vo.BalanceCashFlowVo>
	 */
	List<BalanceCashFlowVo> findBalanceCashFlowList(@Param("balanceCashFlowQueryDto") BalanceCashFlowQueryDto balanceCashFlowQueryDto);

	/**
	 * @Author Libao
	 * @Description 查询本年累计
	 * @Date  2019/9/2 10:00
	 * @Param [balanceCashFlowQueryDto]
	 * @return java.util.List<com.njwd.entity.ledger.vo.BalanceCashFlowVo>
	 */
	List<BalanceCashFlowVo> findBalanceCashFlowTotalAmount(@Param("balanceCashFlowQueryDto") BalanceCashFlowQueryDto balanceCashFlowQueryDto);

	/**
	 * @param balanceCashFlowDto
	 * @return java.util.List<com.njwd.entity.ledger.vo.BalanceCashFlowVo>
	 * @description: 根据核算主体查询指定期间现金流量项目余额
	 * @author LuoY
	 * @date 2019/8/9 14:32
	 */
	List<BalanceCashFlowVo> findBalanceCashFlowByCondition(BalanceCashFlowDto balanceCashFlowDto);

   /**
    * @Author Libao
    * @Description 查询现金流量项目Id
    * @Date  2019/9/5 14:50
    * @Param []
    * @return java.util.List<com.njwd.entity.ledger.vo.BalanceCashFlowInitVo>
    */
	List<BalanceCashFlowVo> findCashFlowItemId();


}