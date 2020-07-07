package com.njwd.service;

import com.njwd.entity.basedata.ReferenceContext;
import com.njwd.entity.basedata.ReferenceRelation;
import com.njwd.entity.basedata.ReferenceResult;
import com.njwd.service.impl.ReferenceRelationServiceImpl;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: xdy
 * @create: 2019/6/27 17:06
 */
public interface ReferenceRelationService {
    
    /**
     * @description: 是否被引用
     * @param: [businessModule, id]
     * @return: com.njwd.entity.basedata.ReferenceResult
     * @author: xdy        
     * @create: 2019-06-28 10-34 
     */
    ReferenceResult isReference(@NotNull String businessModule, @NotNull Long businessId);


    /**
     * @description: 是否被引用
     * @param: [businessModule, ids]
     * @return: com.njwd.entity.basedata.ReferenceContext
     * @author: xdy        
     * @create: 2019-06-28 10-34 
     */
    ReferenceContext isReference(@NotNull String businessModule, @NotNull List<Long> businessIds);

    List<Map<String,Object>> findBusinessData(ReferenceRelation referenceRelation);

    ReferenceResult isReferenceByCode(@NotNull String businessModule, @NotNull String code);

    ReferenceContext isReferenceByCode(@NotNull String businessModule, @NotNull List<String> codes);

    List<ReferenceRelation> findReferenceRelation(String businessModule);

    Integer findReferenceCount(ReferenceRelation referenceRelation);

    List<ReferenceRelation> findReferenceCountList(ReferenceRelation referenceRelation);

    ReferenceContext isReference0(@NotNull String businessModule, @NotNull List<Long> ids);

    ReferenceContext isReference(@NotNull String businessModule, @NotNull List<Long> ids, List<String> ignoreTables);
}
