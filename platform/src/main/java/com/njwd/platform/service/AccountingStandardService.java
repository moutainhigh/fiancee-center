package com.njwd.platform.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.AccountingStandardDto;
import com.njwd.entity.platform.vo.AccountingStandardVo;

import java.util.List;

/**
 * @Author lj
 * @Description 会计准则
 * @Date:10:49 2019/6/13
 **/
public interface AccountingStandardService {

    /**
     * @Description 查询会计准则列表信息
     * @Author lj
     * @Date:10:26
     * @Param []
     * @return java.util.List<com.njwd.platform.entity.vo.AccountingStandardVo>
     **/
    List<AccountingStandardVo> findAccountingList();

    /**
     * @Description 查询单个会计准则信息
     * @Author lj
     * @Date:10:26
     * @Param [accountingStandardDto]
     * @return com.njwd.platform.entity.vo.AccountingStandardVo
     **/
    AccountingStandardVo findAccountingById(AccountingStandardDto accountingStandardDto);

    /**
     * 分页
     *
     * @param accountingStandardDto accountingStandardDto
     * @return Page<AccountingStandardVo>
     * @author xyyxhcj@qq.com
     * @date 2019/11/12 13:42
     **/
	Page<AccountingStandardVo> findPage(AccountingStandardDto accountingStandardDto);

    /**
     * 查详情
     *
     * @param accountingStandardDto accountingStandardDto
     * @return com.njwd.entity.platform.vo.AccountingStandardVo
     * @author xyyxhcj@qq.com
     * @date 2019/11/19 14:10
     **/
    AccountingStandardVo findDetail(AccountingStandardDto accountingStandardDto);
}
