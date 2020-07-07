package com.njwd.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.ledger.QuerySchemeDetail;
import com.njwd.entity.ledger.dto.QuerySchemeDetailDto;

import java.util.List;

public interface QuerySchemeDetailMapper extends BaseMapper<QuerySchemeDetail> {

    int addBatch(List<QuerySchemeDetailDto> details);

}