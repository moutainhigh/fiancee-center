package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.AccountingCalendar;
import com.njwd.entity.platform.AccountingPeriod;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author lj
 * @Description 会计日历
 * @Date:14:06 2019/6/26
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountingCalendarVo extends AccountingCalendar {
    private static final long serialVersionUID = 7541640938094648934L;
    //期间类型名称
    private String periodTypeName;
    //用于插入会计期间表
    private List<AccountingPeriod> accountingPeriodList;
}
