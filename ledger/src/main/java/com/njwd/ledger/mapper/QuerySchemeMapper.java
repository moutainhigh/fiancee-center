package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.ledger.QueryScheme;
import com.njwd.entity.ledger.dto.QuerySchemeDto;
import com.njwd.entity.ledger.vo.QuerySchemeVo;

import java.util.List;


public interface QuerySchemeMapper extends BaseMapper<QueryScheme> {

    List<QuerySchemeVo> findQueryScheme(QuerySchemeDto querySchemeDto);

    int updateQueryScheme(QuerySchemeDto querySchemeDto);

}