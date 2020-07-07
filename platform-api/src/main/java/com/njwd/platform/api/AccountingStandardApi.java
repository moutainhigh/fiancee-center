package com.njwd.platform.api;

import com.njwd.entity.platform.dto.AccountingStandardDto;
import com.njwd.entity.platform.vo.AccountingStandardVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 会计准则
 * @Date:17:07 2019/7/12
 **/
@RequestMapping("platform/accountingStandard")
public interface AccountingStandardApi {

    /**
     * @Description 查询单个会计准则信息
     * @Author liuxiang
     * @Date:17:07 2019/7/12
     * @Param [accountingStandardDto]
     * @return java.lang.String
     **/
    @PostMapping("findAccountingById")
    Result<List<AccountingStandardVo>> findAccountingById(AccountingStandardDto accountingStandardDto);

    /**
     * @Description 查询会计准则列表信息
     * @Param []
     * @return java.lang.String
     **/
    @PostMapping("findAccountingList")
    Result<List<AccountingStandardVo>> findAccountingList();
}
