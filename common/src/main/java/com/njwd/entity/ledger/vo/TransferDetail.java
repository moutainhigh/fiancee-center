package com.njwd.entity.ledger.vo;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @description: 过账失败明细实体
 * @author: fancl
 * @create: 2019-08-15
 */

@Getter
@Setter
public class TransferDetail implements Serializable {
    //账簿期间id
    private Long periodId;
    //检查项类型：isNull(不存在)   settled(已结账) unAudit(未审核) unReview(未复核) broken(断号的)
    private String checkType;
    //结果描述
    private String messageDesc;
    //id 的 list
    private List<Long> idList;
}
