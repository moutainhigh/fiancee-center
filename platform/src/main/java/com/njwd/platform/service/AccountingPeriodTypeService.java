package com.njwd.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.AccountingPeriodTypeDto;
import com.njwd.entity.platform.vo.AccountingPeriodTypeVo;
import com.njwd.support.BatchResult;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @description: 会计期间类型
 * @author: lzt
 * @create: 2019-11-20 09:51
 */
public interface AccountingPeriodTypeService{

    /**
     * @description: 会计期间类型分页
     * @param: [accountingPeriodTypeDto]
     * @return: com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.njwd.entity.platform.vo.AccountingPeriodTypeVo>
     * @author: lzt
     * @create: 2019-11-20 09:51
     */
    Page<AccountingPeriodTypeVo> findAccountingPeriodTypePage(AccountingPeriodTypeDto accountingPeriodTypeDto);

}

