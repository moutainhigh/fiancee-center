package com.njwd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.njwd.entity.basedata.ReferenceRelation;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/27 17:01
 */
public interface ReferenceRelationMapper extends BaseMapper<ReferenceRelation> {

    Integer findReferenceCount(ReferenceRelation referenceRelation);

    List<ReferenceRelation> findReferenceCountList(ReferenceRelation referenceRelation);

    List<Map<String,Object>> findBusinessData(ReferenceRelation referenceRelation);

}
