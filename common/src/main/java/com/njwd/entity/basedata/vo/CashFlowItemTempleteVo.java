package com.njwd.entity.basedata.vo;

import com.njwd.entity.platform.CashFlow;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author: Libao
 * @Description 现金流量项目模板Vo
 * @Date: 2019-06-11 16:16
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper=false)
public class CashFlowItemTempleteVo extends CashFlow {

    private static final long serialVersionUID = -7442114548065801787L;
    /**
     * 模板Id
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

}
