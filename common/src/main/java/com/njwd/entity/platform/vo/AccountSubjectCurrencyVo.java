package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.AccountSubjectCurrency;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author liuxiang
 * @Description 会计科目币种关系
 * @Date:14:16 2019/6/19
 **/
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AccountSubjectCurrencyVo extends AccountSubjectCurrency {

    private static final long serialVersionUID = 3069832031388211980L;
}