package com.njwd.entity.ledger;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description
 * @Author: ZhuHC
 * @Date: 2019/8/8 17:40
 */
@Data
public class CommonAuxiliary implements Serializable {
    /**
     * ID
     */
    private Long id;
    /**
     * 名稱
     */
    private String name;
    /**
     * 全名
     */
    private String fullName;
    /**
     * 編碼
     */
    private String code;
    /**
     * 来源表
     */
    private String sourceTable;
    /**
     * 与明细关联ID
     */
    private Long detailId;
    /**
     * 来源名称
     */
    private String auxiliaryName;
    /**
     * 0：流出、1：流入
     */
    private Byte cashFlowDirection;

    public StringBuilder getSign() {
        StringBuilder sign = new StringBuilder();
        return sign.append(getSourceTable()).append(getId());
    }

    @Override
    public int hashCode() {
        return getSign().toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
