package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.AccountingPeriodType;
import com.njwd.entity.platform.vo.AccountingPeriodTypeVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
* @description:
* @author: lzt
* @create: 2019-11-20 09:51
*/
@Getter
@Setter
public class AccountingPeriodTypeDto extends AccountingPeriodType {
    private Page<AccountingPeriodTypeVo> page = new Page<>();
    /**
     * 编码 名称 期间代号
     */
    private String codeOrName;

}
