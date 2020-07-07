package com.njwd.platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.AccountingStandardDto;
import com.njwd.entity.platform.vo.AccountingStandardVo;
import com.njwd.platform.mapper.AccountingStandardCurrencyMapper;
import com.njwd.platform.mapper.AccountingStandardMapper;
import com.njwd.platform.service.AccountingStandardService;
import com.njwd.utils.FastUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author lj
 * @Description 会计准则
 * @Date:10:51 2019/6/13
 **/
@Service
public class AccountingStandardServiceImpl implements AccountingStandardService{

    @Resource
    private AccountingStandardMapper accountingStandardMapper;
    @Resource
    private AccountingStandardCurrencyMapper accStandardCurrencyMapper;

    /**
     * @Description 查询会计准则列表信息
     * @Author liuxiang
     * @Date:15:46 2019/7/2
     * @Param []
     * @return java.util.List<com.njwd.platform.entity.vo.AccountingStandardVo>
     **/
    @Override
    @Cacheable(value = "accountingList")
    public List<AccountingStandardVo> findAccountingList() {
        return accountingStandardMapper.findAccountingList();
    }

    /**
     * @Description 查询单个会计准则信息
     * @Author liuxiang
     * @Date:15:46 2019/7/2
     * @Param [accountingStandardDto]
     * @return com.njwd.platform.entity.vo.AccountingStandardVo
     **/
    @Override
    @Cacheable(value = "accountingById", key = "#accountingStandardDto.id+''",unless="#result == null")
    public AccountingStandardVo findAccountingById(AccountingStandardDto accountingStandardDto) {
        return accountingStandardMapper.findAccountingById(accountingStandardDto);
    }

    /**
     * 分页
     *
     * @param accountingStandardDto accountingStandardDto
     * @return Page<AccountingStandardVo>
     * @author xyyxhcj@qq.com
     * @date 2019/11/12 13:42
     **/
    @Override
    public Page<AccountingStandardVo> findPage(AccountingStandardDto accountingStandardDto) {
        Page<AccountingStandardVo> page = accountingStandardDto.getPage();
        return page.setRecords(accountingStandardMapper.findPage(accountingStandardDto, page));
    }

    /**
     * 查详情
     *
     * @param accountingStandardDto accountingStandardDto
     * @return com.njwd.entity.platform.vo.AccountingStandardVo
     * @author xyyxhcj@qq.com
     * @date 2019/11/19 14:10
     **/
    @Override
    public AccountingStandardVo findDetail(AccountingStandardDto accountingStandardDto) {
        FastUtils.checkParams(accountingStandardDto.getId());
        AccountingStandardVo detail = accountingStandardMapper.findDetail(accountingStandardDto.getId());
        FastUtils.checkNull(detail);
        detail.setAccStandardCurrencyList(accStandardCurrencyMapper.findByAccStandardId(accountingStandardDto.getId()));
        return detail;
    }
}
