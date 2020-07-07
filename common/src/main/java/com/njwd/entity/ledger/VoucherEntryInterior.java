package com.njwd.entity.ledger;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
/**
 *
 * @author xyyxhcj@qq.com
 * @since 2019/8/26
 */
@Getter
@Setter
@TableName("wd_voucher_entry_interior_%s")
public class VoucherEntryInterior {
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
    * 对方协同凭证ID 【wd_voucher】表
    */
    private Long interiorVoucherId;

    /**
    * 对方协同凭证分录ID 【wd_voucher_entry】表ID
    */
    private Long interiorEntryId;
}