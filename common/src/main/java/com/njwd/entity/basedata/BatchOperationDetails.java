package com.njwd.entity.basedata;

import lombok.Data;

import java.util.List;

/**
 * 批量操作结果
 *
 * @author zhuzs
 * @date 2019-07-04 14:20
 */
@Data
public class BatchOperationDetails {
    /**
     * 操作结果详情信息
     */
    List<BatchOperationMessage> batchOperationMessageList;

    /**
     * 操作成功的记录的ID 集合
     */
    List<Long> successIds;
}

