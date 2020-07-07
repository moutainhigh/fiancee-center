package com.njwd.entity.ledger;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author xyyxhcj@qq.com
 * @since 2019/7/24
 */
@Getter
@Setter
public class BalanceSubjectInitAuxiliaryItem implements Serializable {
    /**
    * 主键 默认自动递增
    */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 辅助核算余额明细id /	[wd_balance_subject_auxiliary] id
     */
    private Long balanceAuxiliaryId;

    /**
    * 账簿ID
    */
    private Long accountBookId;

    /**
    * 核算主体ID
    */
    private Long accountBookEntityId;

    /**
    * 科目id
    */
    private Long accountSubjectId;

    /**
     * 启用期间年度
     */
    private Integer periodYear;

    /**
     * 启用期间号
     */
    private Byte periodNum;

    /**
    * 核算项来源表
    */
    private String sourceTable;

    /**
    * 核算值ID
    */
    private Long itemValueId;

    /**
     * 核算编码
     */
    @TableField(exist = false)
    private String itemValueCode;

    /**
     * 核算值NAME
     */
    private String itemValueName;

    private static final long serialVersionUID = 1L;

    public StringBuilder getSign() {
        StringBuilder sign = new StringBuilder();
        return sign.append(sourceTable).append(itemValueId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BalanceSubjectAuxiliaryItem that = (BalanceSubjectAuxiliaryItem) o;
        return getSign().toString().equals(that.getSign().toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSign().toString());
    }
}
