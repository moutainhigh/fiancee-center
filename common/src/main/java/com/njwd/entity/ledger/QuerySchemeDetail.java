package com.njwd.entity.ledger;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @description:
 * @author: xdy
 * @create: 2019/7/30 9:10
 */
@Getter
@Setter
public class QuerySchemeDetail implements Serializable {

    /**
     * 主键 默认自动递增
     */
    @TableId(type = IdType.AUTO)
    @JsonIgnore
    private Long id;

    /**
     * 查询方案ID
     */
    @JsonIgnore
    private Long schemeId;

    /**
     * 列名称 列名称
     */
    private String colName;

    /**
     * 列类型 字符、整形、日期型
     */
    @JsonIgnore
    private Byte colType;

    /**
     * 关系操作符
     */
    private Byte relationalOperator;

    /**
     *单值、多值、区间第一个值
     */
    @JsonIgnore
    private String firstValue;

    /**
     * 值名称
     */
    @JsonIgnore
    private String firstName;

    /**
     * 区间第二个值
     */
    @JsonIgnore
    private String secondValue;

    /**
     * 值名称
     */
    @JsonIgnore
    private String secondName;


}
