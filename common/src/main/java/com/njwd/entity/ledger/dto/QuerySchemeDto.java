package com.njwd.entity.ledger.dto;

import com.njwd.entity.ledger.QueryScheme;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/7/30 9:56
 */
@Getter
@Setter
public class QuerySchemeDto extends QueryScheme {

    private List<QuerySchemeDetailDto> details;

}
