package com.njwd.entity.ledger.vo;

import com.njwd.common.Constant;
import com.njwd.entity.ledger.QueryScheme;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/7/30 16:06
 */
@Getter
@Setter
public class QuerySchemeVo extends QueryScheme {

    private List<QuerySchemeDetailVo> details;

    /**
     * 0版本不一致1一致
     */
    private Byte isVersionSame = Constant.Is.YES;

}
