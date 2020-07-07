package com.njwd.ledger.api;

import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 账簿期间
 *
 * @author zhuzs
 * @date 2019-08-05 15:17
 */
@RequestMapping("ledger/accountBookPeriod")
public interface AccountBookPeriodApi {
    /**
     * 新增账簿期间数据
     *
     * @param: [accountBookPeriod]
     * @return: com.njwd.support.Result<java.lang.Integer>
     * @author: zhuzs
     * @date: 2019-10-21
     */
    @RequestMapping("addAccountBookPeriod")
    Result<Integer> addAccountBookPeriod(@RequestBody AccountBookPeriod accountBookPeriod);

    /**
     * 删除账簿期间数据
     *
     * @param: [accountBookPeriod]
     * @return: com.njwd.support.Result<java.lang.Integer>
     * @author: zhuzs
     * @date: 2019-10-21
     */
    @RequestMapping("deleteByAccountBookIdAndSystemSign")
    Result<Integer> deleteByAccountBookIdAndSystemSign(@RequestBody AccountBookPeriod accountBookPeriod);

    /**
     * 根据账簿ID、子系统标识 查询已打开账簿期间
     *
     * @param: [accountBookPeriodDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.ledger.vo.AccountBookPeriodVo>>
     * @author: zhuzs
     * @date: 2019-10-21
     */
    @RequestMapping("findOpenedPeriodListByAccBookIdAndSystemSign")
    Result<List<AccountBookPeriodVo>> findOpenedPeriodListByAccBookIdAndSystemSign(@RequestBody AccountBookPeriodDto accountBookPeriodDto);

    /**
     * 根据账簿ID、子系统标识 查询最小未结账 账簿期间
     *
     * @param: [accountBookPeriodDto]
     * @return: com.njwd.support.Result<com.njwd.entity.ledger.vo.AccountBookPeriodVo>
     * @author: zhuzs
     * @date: 2019-10-18
     */
    @RequestMapping("findPeriodByAccBookIdAndSystemSign")
    Result<AccountBookPeriodVo> findPeriodByAccBookIdAndSystemSign(@RequestBody AccountBookPeriodDto accountBookPeriodDto);

    /**
     * 根据 账簿ID 获取账簿期间可选范围
     *
     * @param: [accountBookPeriodDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.ledger.vo.AccountBookPeriodVo>>
     * @author: zhuzs
     * @date: 2019-10-21
     */
    @RequestMapping("findPeriodRangeByAccBookId")
    Result<List<AccountBookPeriodVo>>  findPeriodRangeByAccBookId(AccountBookPeriodDto accountBookPeriodDto);
}

