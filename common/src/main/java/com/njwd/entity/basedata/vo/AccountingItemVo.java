package com.njwd.entity.basedata.vo;

import com.njwd.entity.basedata.AccountingItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author 薛永利
 * @create 2019/6/12 10:58
 */

/**
 * @Description 自定义核算项目Vo 返回数据用
 * @Author 薛永利
 * @Date 2019/6/26 14:24
 */
@Data
@ToString(callSuper =  true)
@EqualsAndHashCode(callSuper = true)
public class AccountingItemVo extends AccountingItem {

    private static final long serialVersionUID = -8187833335921416489L;
    /**
     * 数据状态
     */
    private String statusName;
    /**
     * 归属公司
     */
    private String companyName;

    /**
     * 项目下值数量
     */
    private int valueCount;

    /**
     * 数据来源表
     */
    private String sourceTable;

    /**
     * 来源 0:平台 1:自定义
     */
    private Byte source;

    /**
     * 是否被使用 0:否 >0：是
     */
    private Integer ifUsed;
}
