package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.SysSystem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.Date;

/**
 * @author liuxiang
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SysSystemVo extends SysSystem {

    private static final long serialVersionUID = 4549406948819290124L;

    /**
     * 核算账簿ID
     */
    private Long accountBookId;
    /**
     * 核算账簿名称
     */
    private String accountBookName;

    /**
     * 状态 0:未启用;1:已启用
     */
    private Byte status;
    /**
     * 启用期间年度
     */
    private Integer periodYear;
    /**
     * 启用期间号
     */
    private Byte periodNum;
    /**
     * 启用人ID
     */
    private Long operatorId;
    /**
     * 启用人
     */
    private String operatorName;
    /**
     * 启用时间
     */
    private Date operateTime;

    /**
     * 现金流量启用标识 0:否；1:是
     */
    private Byte cashFlowEnableStatus;
}