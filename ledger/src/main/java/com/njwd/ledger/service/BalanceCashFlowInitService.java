package com.njwd.ledger.service;

import com.njwd.entity.ledger.dto.BalanceCashFlowInitDto;
import com.njwd.entity.ledger.dto.BalanceInitRecordDto;
import com.njwd.entity.ledger.vo.BalanceCashFlowInitVo;
import com.njwd.entity.ledger.vo.BalanceInitRecordVo;
import com.njwd.entity.platform.vo.CashFlowItemVo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author lj
 * @Description 现金流量期初接口
 * @Date:9:56 2019/8/8
 **/
public interface BalanceCashFlowInitService {
    /**
     * 添加核算主体
     * @Author lj
     * @Date:10:10 2019/8/2
     * @param balanceInitRecordDto
     * @return int
     **/
    int addBalanceInitRecordBatch(BalanceInitRecordDto balanceInitRecordDto);

    /**
     * 现金流量期初录入
     * @Author lj
     * @Date:10:10 2019/8/2
     * @param balanceCashFlowInitDto
     * @return int
     **/
    int addCashFlowInitBatch(BalanceCashFlowInitDto balanceCashFlowInitDto);

    /**
     * 根据核算主体ID清空期初数据
     * @Author lj
     * @Date:15:42 2019/7/25
     * @param balanceCashFlowInitDto
     * @return int
     **/
    int deleteBalCashFlowBatch(BalanceCashFlowInitDto balanceCashFlowInitDto);

    /**
     * 删除核算主体
     * @Author lj
     * @Date:15:42 2019/7/25
     * @param balanceInitRecordDto
     * @return int
     **/
    int deleteBalanceInitRecord(BalanceInitRecordDto balanceInitRecordDto);

    /**
     * 拉取当前所在公司账簿，自动带出账簿中有数据的核算主体及当前所在核算主体，账簿对应现金流量信息,账簿启用期间
     * @Author lj
     * @Date:11:00 2019/7/31
     * @param balanceCashFlowInitDto
     * @return com.njwd.ledger.entity.vo.BalanceCashFlowInitVo
     **/
    BalanceCashFlowInitVo findAccountBookByComId(BalanceCashFlowInitDto balanceCashFlowInitDto);

    /**
     * a)	当前账簿启用二级核算，核算主体默认选中公司本部，可选择到其他核算主体，流量项目中显示属性为内部往来现金流量；
     * b)	当前账簿未启用二级核算，核算主体默认选中公司本部且置灰不可选择，项目列表中不显示属性为内部往来的现金流量
     * @Author lj
     * @Date:10:12 2019/7/29
     * @param balanceCashFlowInitDto
     * @return List<AccountSubjectVo>
     **/
    List<CashFlowItemVo> findCashFlow(BalanceCashFlowInitDto balanceCashFlowInitDto);

    /**
     *  查询帐簿总账现金流量启用状态
     *
     * @param balanceCashFlowInitDto
     * @return BalanceCashFlowInitVo
     * @Author lj
     * @Date:10:12 2019/7/29
     **/
    BalanceCashFlowInitVo findAccCashFlowStatus(BalanceCashFlowInitDto balanceCashFlowInitDto);

    /**
     * 查询期初录入记录列表
     *
     * @param balanceInitRecordDto
     * @return
     */
    List<BalanceInitRecordVo> findListByParam(BalanceInitRecordDto balanceInitRecordDto);

    /**
     * 查询期初录入表账簿id列表
     *
     * @param balanceInitRecordDto
     * @return
     */
    List<Long> findAccoutBookIdListByParam(BalanceInitRecordDto balanceInitRecordDto);

    /**
     * 查询期初录入表核算主体id列表
     *
     * @param balanceInitRecordDto
     * @return
     */
    List<Long> findEntityIdListByParam(BalanceInitRecordDto balanceInitRecordDto);
    
    /**
     * @description: 导出excel
     * @param: [balanceCashFlowInitDto, response]
     * @return: void 
     * @author: xdy        
     * @create: 2019-10-22 15:19 
     */
    void exportExcel(BalanceCashFlowInitDto balanceCashFlowInitDto, HttpServletResponse response);
}
