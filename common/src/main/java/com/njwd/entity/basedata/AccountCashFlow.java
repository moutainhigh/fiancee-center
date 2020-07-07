package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * 现金流量项目表
 *
 * @author zhuzs
 * @date 2019-07-11 19:04
 */
@Data
public class AccountCashFlow {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 企业ID
     */
    private Long rootEnterpriseId;

    /**
     * 基准现金流量表ID
     */
    private Long cashFlowId;

    /**
     * 基准现金流量表
     */
    private String cashFlowName;

    /**
     * 现金流量表模板ID
     */
    private Long templateCashFlowId;

    /**
     * 现金流量表模板
     */
    private String templateCashFlowName;

    /**
     * 账簿类型ID
     */
    private Long accountBookTypeId;

    /**
     * 账簿类型名称
     */
    private String accountBookTypeName;

    /**
     * 会计准则ID
     */
    private Long accStandardId;

    /**
     * 会计准则名称
     */
    private String accStandardName;

    /**
     * 最大层级
     */
    private String maxLevel;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 创建者
     */
    private String creatorName;
}

