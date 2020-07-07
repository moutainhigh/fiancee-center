package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.AuxiliaryItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author liuxiang
 * @Description 辅助核算
 * @Date:14:15 2019/6/19
 **/
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AuxiliaryItemVo extends AuxiliaryItem {
    private static final long serialVersionUID = 6453567514608298305L;

    /**
     * 来源模块名称
     **/
    private String sourceModelTypeName;

    /**
     * 来源名称集合
     **/
    private String auxiliarySources;

    /**
     * 值依赖名称集合
     **/
    private String auxiliaryDependents;

    /**
     * 依赖排序集合
     **/
    private String sortNums;

    /**
     * 名称字符串
     **/
    private String names;

    /**
     * 来源 0:平台 1:自定义
     */
    private Byte source;

    /**
     * 是否被使用 0:否 >0：是
     */
    private Integer ifUsed;

    /**
     * 是否预置 0:否 1:是
     */
    private Byte isInit;
}