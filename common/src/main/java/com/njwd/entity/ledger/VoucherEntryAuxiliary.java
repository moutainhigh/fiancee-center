package com.njwd.entity.ledger;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("wd_voucher_entry_auxiliary_%s")
public class VoucherEntryAuxiliary implements Serializable {
    /**
    * 主键 默认自动递增
    */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
    * 凭证ID 【wd_voucher】表ID
    */
    private Long voucherId;

    /**
    * 凭证分录ID 【wd_voucher_entry】表ID
    */
    private Long entryId;

    /**
    * 核算来源表
    */
    private String sourceTable;

    /**
    * 核算项目值ID
    */
    private Long itemValueId;

    private static final long serialVersionUID = 1L;
}