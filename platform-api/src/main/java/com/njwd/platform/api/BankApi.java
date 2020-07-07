package com.njwd.platform.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.dto.BankDto;
import com.njwd.entity.platform.vo.BankVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 银行及银行类别公共Api
 * @Date:17:58 2019/6/13
 **/
@RequestMapping("platform/bank")
public interface BankApi {


    /**
     * @Description 查询银行及银行类别列表
     * @Param []
     * @return java.lang.String
     **/
    @PostMapping("findBankPage")
    Result<Page<BankVo>> findBankPage(BankDto platformBankDto);

    /**
     * @Description 查询银行及银行类别列表
     * @Param []
     * @return java.lang.String
     **/
    @PostMapping("findBankList")
    Result<List<BankVo>> findBankList();

    /**
     * @Description 根据ID查询银行及银行类别
     * @Param [bank]
     * @return java.lang.String
     **/
    @PostMapping("findBankById")
    Result<BankVo> findBankById(BankDto platformBankDto);


}
