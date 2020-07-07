package com.njwd.ledger.controller;

import com.njwd.common.Constant;
import com.njwd.common.LedgerConstant;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.dto.AccountBookPeriodDto;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;
import com.njwd.ledger.service.AccountBookPeriodService;
import com.njwd.support.BaseController;
import com.njwd.support.Result;
import com.njwd.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 账簿期间表
 *
 * @author zhuzs
 * @date 2019-08-05 15:06
 */
@RequestMapping("accountBookPeriod")
@RestController
public class AccountBookPeriodController extends BaseController {
    @Autowired
    private AccountBookPeriodService accountBookPeriodService;

    /**
     * 新增账簿期间数据
     *
     * @param: [accountBookPeriod]
     * @return: com.njwd.support.Result<java.lang.Integer>
     * @author: zhuzs
     * @date: 2019-10-18
     */
    @RequestMapping("addAccountBookPeriod")
    public Result<Integer> addAccountBookPeriod(@RequestBody AccountBookPeriod accountBookPeriod) {
        return ok(accountBookPeriodService.addAccountBookPeriod(accountBookPeriod));
    }

    /**
     *  删除账簿期间数据
     *
     * @param: [accountBookPeriod]
     * @return: com.njwd.support.Result<java.lang.Integer>
     * @author: zhuzs
     * @date: 2019-10-18
     */
    @RequestMapping("deleteByAccountBookIdAndSystemSign")
    public Result<Integer> deleteByAccountBookIdAndSystemSign(@RequestBody AccountBookPeriod accountBookPeriod){
        return ok(accountBookPeriodService.deleteByAccountBookIdAndSystenSign(accountBookPeriod));
    }



    /**
     * 根据账簿ID、子系统标识 查询最小未结账 账簿期间
     *
     * @param: [accountBookPeriodDto]
     * @return: com.njwd.support.Result<com.njwd.entity.ledger.vo.AccountBookPeriodVo>
     * @author: zhuzs
     * @date: 2019-10-18
     */
    @RequestMapping("findLatestPeriodByAccBookIdAndSystemSign")
    public Result<AccountBookPeriodVo> findLatestPeriodByAccBookIdAndSystemSign(@RequestBody AccountBookPeriodDto accountBookPeriodDto){
        // 已开启
        accountBookPeriodDto.setStatus(Constant.Status.ON);
        // 未结账
        accountBookPeriodDto.setIsSettle(LedgerConstant.Ledger.ZERO);
        // 是否查询最小期间
        accountBookPeriodDto.setIsLeast(Constant.Is.YES);
        return ok(accountBookPeriodService.findPeriodByAccBookIdAndSystemSign(accountBookPeriodDto));
    }

    /**
     * 根据 期间年、期间号、账簿ID、子系统标识 查询账簿期间
     *
     * @param: [accountBookPeriodDto]
     * @return: com.njwd.support.Result<com.njwd.entity.ledger.vo.AccountBookPeriodVo>
     * @author: zhuzs
     * @date: 2019-10-16
     */
    @RequestMapping("findPeriodByAccBookIdAndSystemSign")
    public Result<AccountBookPeriodVo> findPeriodByAccBookIdAndSystemSign(@RequestBody AccountBookPeriodDto accountBookPeriodDto){
        return ok(accountBookPeriodService.findPeriodByAccBookIdAndSystemSign(accountBookPeriodDto));
    }

    /**
     * 根据 账簿ID 获取账簿期间可选范围
     *
     * @param: [accountBookPeriodDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.ledger.vo.AccountBookPeriodVo>>
     * @author: zhuzs
     * @date: 2019-10-16
     */
    @RequestMapping("findPeriodRangeByAccBookId")
    public Result<List<AccountBookPeriodVo>>  findPeriodRangeByAccBookId(@RequestBody AccountBookPeriodDto accountBookPeriodDto){
        accountBookPeriodDto.setRootEnterpriseId(UserUtils.getUserVo().getRootEnterpriseId());
        return ok(accountBookPeriodService.findPeriodRangeByAccBookIdsAndSystemSign(accountBookPeriodDto));
    }

    /**
     * 根据 账簿ID List 获取账簿期间可选范围
     *
     * @param: [accountBookPeriodDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.ledger.vo.AccountBookPeriodVo>>
     * @author: zhuzs
     * @date: 2019-10-18
     */
    @RequestMapping("findPeriodRangeByAccBookIds")
    public Result<List<AccountBookPeriodVo>> findPeriodRangeByAccBookIds(@RequestBody AccountBookPeriodDto accountBookPeriodDto){
        return ok(accountBookPeriodService.findPeriodRangeByAccBookIdsAndSystemSign(accountBookPeriodDto));
    }

    /**
     * 根据账簿ID、子系统标识 查询已打开账簿期间（未结账）
     *
     * @param: [accountBookPeriodDto]
     * @return: com.njwd.support.Result<java.util.List<com.njwd.entity.ledger.vo.AccountBookPeriodVo>>
     * @author: zhuzs
     * @date: 2019-10-16
     */
    @RequestMapping("findOpenedPeriodListByAccBookIdAndSystemSign")
    public Result<List<AccountBookPeriodVo>> findOpenedPeriodListByAccBookIdAndSystemSign(@RequestBody AccountBookPeriodDto accountBookPeriodDto){
        // 已开启
        accountBookPeriodDto.setStatus(LedgerConstant.Ledger.ONE);
        // 未结账
        accountBookPeriodDto.setIsSettle(LedgerConstant.Ledger.ZERO);
        return ok(accountBookPeriodService.findPeriodRangeByAccBookIdsAndSystemSign(accountBookPeriodDto));
    }
}

