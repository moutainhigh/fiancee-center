package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.AccountingPeriod;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author liuxiang
 * @Description 会计期间
 * @Date:11:02 2019/6/28
 **/
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AccountingPeriodVo extends AccountingPeriod {
    private static final long serialVersionUID = -5977258040577297693L;

    /**
     * 当前期间年
     */
    private Integer customPeriodYear;

    /**
     * 当前期间号
     */
    private Integer customPeriodNum;
}