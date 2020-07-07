package com.njwd.entity.ledger.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @description: 过账实体返回inner实体
 * @author: fancl
 * @create: 2019-08-15
 */
@Getter
@Setter
public class TransferItemVo implements Serializable {
    //账簿期间id
    private Long accountBookPeriodId;
    //过账状态,true为过账成功 ,为false时,具体错误检查信息在messageList中
    private Boolean transferFlag;
    //检查项列表
    private List<TransferDetail> messageList = new ArrayList<>();
}
