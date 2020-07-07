package com.njwd.basedata.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.AccountingPeriod;
import com.njwd.entity.basedata.BatchOperationDetails;
import com.njwd.entity.basedata.dto.AccountBookDto;
import com.njwd.entity.basedata.vo.AccountBookVo;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.platform.dto.AccountingPeriodDto;
import com.njwd.entity.platform.vo.AccountingPeriodVo;
import com.njwd.support.BatchResult;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 核算账簿
 *
 * @Author 朱小明
 * @Date: 2019-06-18 17:02
 */
@RequestMapping("/financeback/accountBook")
public interface AccountBookApi {


    @RequestMapping("selectById")
    Result<AccountBookVo> selectById(AccountBookDto accountBookDto);
    /**
     * 删除核算账簿
     * @param accountBookDto
     * @return
     */
    @RequestMapping("deleteBatchById")
    Result<BatchResult> deleteBatchById(AccountBookDto accountBookDto);

    /**
     * 启用子系统
     * @param accountBookDto
     * @return
     */
    @RequestMapping("enableAccountBookSystem")
    Result<Integer> enableAccountBookSystem(AccountBookDto accountBookDto);

    /**
     * 批量 启用子系统
     * @param accountBookDtos
     * @return
     */
    @RequestMapping("enableAccountBookSystemBatch")
    Result<BatchOperationDetails> enableAccountBookSystemBatch(List<AccountBookDto> accountBookDtos);

    /**
     * 反启用子系统
     * @param accountBookDto
     * @return
     */
    @RequestMapping("antiEnableAccountBookSystem")
    Result<BatchOperationDetails> antiEnableAccountBookSystem(AccountBookDto accountBookDto);

    /**
     * 批量 反启用子系统
     * @param accountBookDtos
     * @return
     */
    @RequestMapping("antiEnableAccountBookSystemBatch")
    Result<BatchOperationDetails> antiEnableAccountBookSystemBatch(List<AccountBookDto> accountBookDtos);

    /**
     * 获取会计期间
     *
     * @param accountingPeriodDto
     * @return
     */
    @RequestMapping("findAccountingPeriod")
    Result<List<AccountingPeriod>> findAccountingPeriod (AccountingPeriodDto accountingPeriodDto);

    /**
     * 根据 ID 查询核算账簿
     * @param accountBookDto
     * @return
     */
    @RequestMapping("findAccountBookById")
    Result<AccountBookVo> findAccountBookById(AccountBookDto accountBookDto);

    /**
     * 根据 公司ID 查询核算账簿
     * @param accountBookDto
     * @return
     */
    @RequestMapping("findAccountBookByCompanyId")
    Result<AccountBookVo> findAccountBookByCompanyId(AccountBookDto accountBookDto);

    /**
     * 查询核算账簿列表
     * @param accountBookDto
     * @return
     */
    @RequestMapping("findAccountBookPage")
    Result<Page<AccountBookVo>> findAccountBookPage(AccountBookDto accountBookDto);

    /**
     * @Description 根据账簿idSet查询账簿列表信息
     * @Author 朱小明
     * @Date 2019/8/7 11:40
     * @Param
     * @return
     **/
    @PostMapping("findAccountBookListByIdSet")
    Result<List<AccountBookVo>> findAccountBookListByIdSet(AccountBookDto accountBookDto);
    
    /**
     * @description: 获取权限内所有账簿
     * @param: []
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.basedata.vo.AccountBookVo>> 
     * @author: xdy        
     * @create: 2019-08-22 18-47
     */
    @PostMapping("findAuthAll")
    Result<List<AccountBookVo>> findAuthAll(@RequestBody AccountBookDto accountBookDto);

    /**
    * @Description 查询账簿下X期的会计期间
    * @Author 朱小明
    * @Date 2019/9/16
    * @param accountBookPeriodDto
    * @return com.njwd.support.Result<java.util.List<com.njwd.entity.platform.vo.AccountingPeriodVo>>
    **/
    @PostMapping("findAccountingPeriodForUpd")
    Result<List<AccountingPeriodVo>> findAccountingPeriodForUpd(AccountBookPeriodDto accountBookPeriodDto);
}
