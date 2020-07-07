package com.njwd.ledger.service;

import com.njwd.entity.ledger.dto.BalanceInitDto;
import com.njwd.entity.ledger.vo.BalanceInitCheckVo;
import com.njwd.entity.ledger.vo.BalanceInitVo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author lj
 * @Description 总账初始化
 * @Date:11:15 2019/8/12
 **/
public interface BalanceInitService {

    /**
     * 账簿初始化
     * @Author lj
     * @Date:11:27 2019/8/12
     * @param balanceInitDto
     * @return com.njwd.support.BatchResult
     **/
    BalanceInitVo balanceInit(BalanceInitDto balanceInitDto);

    /**
     * 账簿初始化批量校验
     * @Author lj
     * @Date:11:27 2019/8/12
     * @param balanceInitDto
     * @return com.njwd.support.BatchResult
     **/
    BalanceInitVo balanceInitBatchCheck(BalanceInitDto balanceInitDto);

    /**
     * 账簿初始化批量更新数据
     * @Author lj
     * @Date:11:27 2019/8/12
     * @param balanceInitDto
     * @return com.njwd.support.BatchResult
     **/
    BalanceInitVo balanceInitBatchUpdate(BalanceInitDto balanceInitDto);

    /**
     * 账簿反初始化
     * @Author lj
     * @Date:11:27 2019/8/12
     * @param balanceInitDto
     * @return com.njwd.support.BatchResult
     **/
    BalanceInitVo balanceDisInit(BalanceInitDto balanceInitDto);

    /**
     * 账簿反初始化批量校验
     * @Author lj
     * @Date:11:27 2019/8/12
     * @param balanceInitDto
     * @return com.njwd.support.BatchResult
     **/
    BalanceInitVo balanceDisInitBatchCheck(BalanceInitDto balanceInitDto);

    /**
     * 账簿反初始化批量更新数据
     * @Author lj
     * @Date:11:27 2019/8/12
     * @param balanceInitDto
     * @return com.njwd.support.BatchResult
     **/
    BalanceInitVo balanceDisInitBatchUpdate(BalanceInitDto balanceInitDto);

    /**
     * @description: 导出校验错误信息excel
     * @param: [balanceInitVo, response]
     * @return: void
     * @author: xdy
     * @create: 2019-10-22 15:19
     */
    void exportExcel(BalanceInitVo balanceInitVo, HttpServletResponse response);
}
