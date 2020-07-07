package com.njwd.entity.ledger;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 *
 * @author xyyxhcj@qq.com
 * @since 2019/7/24
 */
@Getter
@Setter
public class AccountBookPeriodReport implements Serializable {
    /**
    * 主键 默认自动递增
    */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
    * 租户ID
    */
    private Long rootEnterpriseId;

    /**
    * 账簿期间记录ID
    */
    private Long periodId;

    /**
     * 账簿ID
     */
    private Long accountBookId;

    /**
     * 账簿名称
     */
    private String accountBookName;

    /**
    * 核算主体ID
    */
    private Long accountBookEntityId;

    /**
    * 核算主体名称
    */
    private String accountBookEntityName;

    /**
    * 凭证字
    */
    private String credentialWordName;

    /**
    * 整理前凭证号
    */
    private String oldVoucherCode;

    /**
    * 整理后凭证号
    */
    private String newVoucherCode;

    private static final long serialVersionUID = 1L;
}