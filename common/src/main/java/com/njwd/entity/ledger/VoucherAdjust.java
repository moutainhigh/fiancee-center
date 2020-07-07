package com.njwd.entity.ledger;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/8 11:24
 */
@Getter
@Setter
public class VoucherAdjust extends AccountBookPeriod{

    /**
     * 0整理失败(需整理) 1整理成功(无需整理) 2无需整理
     */
    private Byte adjustStatus;

    /**
     * 整理信息
     */
    private String adjustMessage;

    /**
     * 断号凭证
     */
    private List<Voucher> brokenVoucher;


}
