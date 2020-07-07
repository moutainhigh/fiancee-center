package com.njwd.entity.ledger;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @Author lj
 * @Description 期初录入记录表
 * @Date:16:46 2019/10/16
 **/
@Getter
@Setter
public class BalanceInitRecord implements Serializable {
    /**
     * 主键 默认自动递增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 核算主体id
     */
    private Long entityId;

    /**
     * 企业ID
     */
    private Long rootEnterpriseId;

    /**
     * 核算主体编码
     */
    private String entityCode;

    /**
     * 核算主体名称
     */
    private String entityName;

    /**
     * 所属账簿id
     */
    private Long accountBookId;

    /**
     * 所属账簿编码
     */
    private String accountBookCode;

    /**
     * 所属账簿名称
     */
    private String accountBookName;

    /**
     * 启用期间年度
     */
    private Integer periodYear;

    /**
     * 记账期间年号
     */
    private Byte periodNum;

    /**
     * 科目余额期初状态 0：未录入，1：未平衡:2：已平衡:3：已初始化
     */
    private Byte subjectStatus;

    /**
     * 现金流量期初状态 0：未录入，1：已录入，2：未启用，3：已初始化
     */
    private Byte cashStatus;

    private static final long serialVersionUID = 1L;
}