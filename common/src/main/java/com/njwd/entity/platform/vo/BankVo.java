package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.Bank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author liuxiang
 * @Description 银行
 * @Date:14:14 2019/6/19
 **/
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BankVo extends Bank {

    private static final long serialVersionUID = 6046370292230220825L;
    /**
     * 银行类别名称
     */
    private String bankCategoryName;

    /**
     * 银行类别编码
     */
    private String bankCategoryCode;
}