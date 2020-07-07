package com.njwd.entity.ledger.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author lj
 * @Description 期初辅助核算给前端用
 * @Date:13:59 2019/10/25
 **/
@Setter
@Getter
public class BalanceSubjectInitExtVo {
    /**
     * key
     */
    private String key;

    /**
     * name
     */
    private String name;

    /**
     * 0 默认辅助核算 1自定义辅助核算
     */
    private Byte type;
}
