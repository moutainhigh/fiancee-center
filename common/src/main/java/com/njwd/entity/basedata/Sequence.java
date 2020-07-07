package com.njwd.entity.basedata;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
public class Sequence implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 序列名称
     */
    private String seqName;

    /**
     * 当前值
     */
    private Integer currentVal;

    /**
     * 步长(跨度)
     */
    private Integer incrementVal;

    /**
     * 【公司】表ID或企业租户表ID
     */
    private Long valId;

    /**
     * 0：公司 1：租户企业ID
     */
    private Byte seqType;

    private static final long serialVersionUID = 1L;
}