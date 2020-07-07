package com.njwd.entity.ledger.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.ledger.AccountBookPeriodReport;
import com.njwd.entity.ledger.vo.AccountBookPeriodReportVo;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author xyyxhcj@qq.com
 * @since 2019/7/24
 */
@Getter
@Setter
public class AccountBookPeriodReportDto extends AccountBookPeriodReport {

    Page<AccountBookPeriodReportVo> page = new Page<>();

}