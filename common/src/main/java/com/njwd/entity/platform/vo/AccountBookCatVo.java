package com.njwd.entity.platform.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author lj
 * @Description 账簿分类
 * @Date:17:14 2019/7/11
 **/
@Data
public class AccountBookCatVo implements Serializable {
    private static final long serialVersionUID = -1331607119448472003L;

    /**
     * 会计准则ID
     */
    private Long accStandardId;

    /**
     * 会计准则名称
     */
    private String accStandardName;

    /**
     * 币种集合
     */
    List<TCurrencyVo> currencys;

    /**
     * 税制集合
     */
    List<TTaxSystemVo> taxSystems;
}
