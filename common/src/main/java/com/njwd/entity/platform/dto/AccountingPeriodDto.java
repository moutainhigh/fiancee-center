package com.njwd.entity.platform.dto;

import com.njwd.entity.platform.vo.AccountingPeriodVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author liuxiang
 * @Description 会计期间
 * @Date:11:02 2019/6/28
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountingPeriodDto extends AccountingPeriodVo {
    private static final long serialVersionUID = 6762771134567530125L;

    /**
     * 自定义会计年度
     */
    private Integer customPeriodYear;

    /**
     * 自定义期间号
     */
    private Integer customPeriodNum;
}