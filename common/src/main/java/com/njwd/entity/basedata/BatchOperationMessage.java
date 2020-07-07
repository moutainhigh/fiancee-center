package com.njwd.entity.basedata;

import lombok.Data;

import java.io.Serializable;

/**
 * 批量操作 返回值
 *
 * @author zhuzs
 * @date 2019-07-03 17:36
 */
@Data
public class BatchOperationMessage implements Serializable {
    /**
     * 记录ID
     */
    private Long id;

    /**
     * 状态 0：失败；1：成功
     */
    private Byte status;

    /**
     * 详情
     */
    private String details;

    /**
     * 标识
     */
    private String sign;
}

