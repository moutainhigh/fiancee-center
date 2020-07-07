package com.njwd.ledger.service;


import com.njwd.entity.ledger.dto.QuerySchemeDto;
import com.njwd.entity.ledger.vo.QuerySchemeVo;

import java.util.List;

/**
 * 查询方案接口
 */
public interface QuerySchemeService {

    QuerySchemeVo addOrUpdate(QuerySchemeDto querySchemeDto);

    int delQueryScheme(QuerySchemeDto querySchemeDto);

    QuerySchemeVo findQuerySchemeById(QuerySchemeDto querySchemeDto);

    List<QuerySchemeVo> findQueryScheme(QuerySchemeDto querySchemeDto);
}
