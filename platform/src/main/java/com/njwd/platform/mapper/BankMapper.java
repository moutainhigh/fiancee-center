package com.njwd.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.Bank;
import com.njwd.entity.platform.dto.BankDto;
import com.njwd.entity.platform.vo.BankVo;
import org.apache.ibatis.annotations.Param;

/**
 * 银行mapper
 *
 * @author 周鹏
 * @date 2019/11/18
 */
public interface BankMapper extends BaseMapper<Bank> {
    /**
     * 查询分页列表
     *
     * @param page
     * @param bankDto
     * @return
     */
    Page<BankVo> findBankPage(Page<BankVo> page, @Param("bankDto") BankDto bankDto);

    /**
     * 根据id查询银行信息
     *
     * @param bankDto
     * @return
     */
    BankVo findBankById(@Param("bankDto") BankDto bankDto);
}