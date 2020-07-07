package com.njwd.entity.ledger.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.ledger.AccountBookPeriod;
import com.njwd.entity.ledger.vo.AccountBookPeriodVo;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 *@description: 过账Dto
 *@author: fancl
 *@create: 2019-08-28 
 */
@Getter
@Setter
public class PostPeriodDto implements Serializable {

    List<AccountBookPeriod> periodList;
    /**
     * 分页信息
     */
    private Page<AccountBookPeriodVo> page = new Page<>();
}
