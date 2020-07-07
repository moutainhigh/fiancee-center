package com.njwd.financeback.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.AccountBook;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.vo.AccountBookVo;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.platform.dto.AccountingPeriodDto;
import com.njwd.entity.platform.vo.AccountingPeriodVo;
import com.njwd.entity.platform.vo.FinancialReportItemSetVo;
import com.njwd.support.BatchResult;

import java.util.List;
import java.util.Set;

/**
 * 核算账簿
 *
 * @Author: Zhuzs
 * @Date: 2019-05-17 13:43
 */
public interface AccountBookService {

    /**
     * 根据ID查询核算账簿
     *
     * @param: [accountBookDto]
     * @return: com.njwd.entity.basedata.vo.AccountBookVo
     * @author: zhuzs
     * @date: 2019-09-16 17:23
     */
    AccountBookVo selectById(AccountBookDto accountBookDto);

    /**
     * 新增核算账簿
     *
     * @param: [accountBook]
     * @return: int
     * @author: zhuzs
     * @date: 2019-09-16 17:24
     */
    int addAccountBook(AccountBook accountBook);

    /**
     * 删除
     *
     * @param: [accountBookDto]
     * @return: com.njwd.support.BatchResult
     * @author: zhuzs
     * @date: 2019-09-16 17:24
     */
    BatchResult delete(AccountBookDto accountBookDto);

    /**
     * 批量删除
     *
     * @param: [accountBookDto]
     * @return: com.njwd.support.BatchResult
     * @author: zhuzs
     * @date: 2019-09-16 17:24
     */
    BatchResult deleteBatch(AccountBookDto accountBookDto);

    /**
     * 修改核算账簿
     *
     * @param: [accountBookDto]
     * @return: int
     * @author: zhuzs
     * @date: 2019-09-16 17:24
     */
    int updateAccountBook(AccountBookDto accountBookDto);

    /**
     * 根据 ID 询核算账簿
     *
     * @param: [accountBookDto]
     * @return: com.njwd.entity.basedata.vo.AccountBookVo
     * @author: zhuzs
     * @date: 2019-09-16 17:24
     */
    AccountBookVo findById(AccountBookDto accountBookDto);

    /**
     * 根据 公司ID 询核算账簿
     *
     * @param: [accountBookDto]
     * @return: com.njwd.entity.basedata.vo.AccountBookVo
     * @author: zhuzs
     * @date: 2019-09-16 17:24
     */
    AccountBookVo findByCompanyId(AccountBookDto accountBookDto);

    /**
     * 根据 公司ID list/账簿ID list 查询核算账簿（默认核算主体，是否启用总帐模块，已打开的会计期间，会计准则，科目表）
     *
     * @param: [accountBookDto]
     * @return: java.util.List<com.njwd.entity.basedata.vo.AccountBookVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:24
     */
    List<AccountBookVo> findAccBookDetailInfoByCompanyIdOrAccBookId(AccountBookDto accountBookDto);

    /**
     * 查询核算账簿列表（含 子系统信息及子系统启用状态信息） 分页 admin端
     *
     * @param: [accountBookDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.basedata.vo.AccountBookVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:24
     */
    Page<AccountBookVo> findAccountBookPage(AccountBookDto accountBookDto);

    /**
     * @Description 根据idSet查询数据
     * @Author 朱小明
     * @Date 2019/8/7 11:53
     * @Param [idSet]
     * @return java.util.List<com.njwd.entity.basedata.vo.AccountBookVo>
     **/
    List<AccountBookVo> findListByIdSet(Set<Long> idSet);

    /**
     * @description: 获取权限内所有账簿
     * @param: []
     * @return: java.util.List<com.njwd.entity.basedata.vo.AccountBookVo>
     * @author: xdy
     * @create: 2019-08-22 18-57
     */
    List<AccountBookVo> findAuthAll(AccountBookDto accountBookDto);

    /**
     * 查询权限内核算账簿列表 （含核算主体、最近已结账期间 信息）
     *
     * @param: [accountBookDto]
     * @return: java.util.List<com.njwd.entity.basedata.vo.AccountBookVo>
     * @author: zhuzs
     * @date: 2019-09-18 17:56
     */
    List<AccountBookVo> findAuthAllWithEntityInfo(AccountBookDto accountBookDto);

    /**
     * 会计期间预览数据-平台
     *
     * @param: [accountingPeriodDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.AccountingPeriodVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:25
     */
    List<AccountingPeriodVo> findAccountBookPeriod(AccountingPeriodDto accountingPeriodDto);

    /**
     * 资产负债表 预览数据
     *
     * @param: [accountBookDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.FinancialReportItemSetVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:25
     */
    List<FinancialReportItemSetVo> findAssetReportList(AccountBookDto accountBookDto);

    /**
     * 预览数据
     *
     * @param: [accountBookDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.FinancialReportItemSetVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:25
     */
    List<FinancialReportItemSetVo> findCashFlowReportList(AccountBookDto accountBookDto);

    /**
     * 预览数据
     *
     * @param: [accountBookDto]
     * @return: java.util.List<com.njwd.entity.platform.vo.FinancialReportItemSetVo>
     * @author: zhuzs
     * @date: 2019-09-16 17:25
     */
    List<FinancialReportItemSetVo> findProfitReportList(AccountBookDto accountBookDto);

    /**
    * @Description
    * @Author 朱小明
    * @Date 2019/9/16
    * @param accountBookPeriodDto
    * @return java.util.List<com.njwd.entity.platform.vo.AccountingPeriodVo>
    **/
    List<AccountingPeriodVo> findAccountingPeriodForUpd(AccountBookPeriodDto accountBookPeriodDto);
}
