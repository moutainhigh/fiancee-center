package com.njwd.entity.ledger.vo;

import com.njwd.entity.ledger.CommonAuxiliary;
import com.njwd.entity.ledger.GeneralReturn;
import lombok.Getter;
import lombok.Setter;

import java.util.List;



/**
 * @Description
 * @Author: ZhuHC
 * @Date: 2019/7/30 16:31
 */
@Getter
@Setter
public class GeneralReturnAuxiliaryVo extends GeneralReturn {

    /**
     * 来源表
     */
    private String sourceTable;
    /**
     * 与来源表 关联ID
     */
    private Long itemValueId;

    /**
     * 0 期初余额 1 明细 2本期合计 3本年累计
     */
    private Integer sortNum;

    /**
     * 凭证ID 【wd_voucher】表ID
     */
    private Long voucherId;

    /**
     * 凭证分录ID 【wd_voucher_entry】表ID
     */
    private Long entryId;

    /**
     * 辅助核算内容
     */
    private List<CommonAuxiliary> commonAuxiliaryList;

    /**
     * 标识
     */
    private StringBuilder keySign;
    /**
     * 辅助核算编码
     */
    private String auxiliaryInfoCode;
    /**
     * 辅助核算姓名
     */
    private String auxiliaryInfoName;
    /**
     * 账簿ID+"_"+主体ID+"_"+科目ID
     */
    private String signFlag;
    /**
     * 来源表
     */
    private String sourceTables;
    /**
     * 与来源表 关联ID
     */
    private String itemValueIds;
    /**
     * 科目上级编码
     */
    private String upCode;
    /**
     * 科目显示次级
     */
    private byte level;
    /**
     * 制单日期时间戳
     */
    private Long voucherDateTime;
    /**
     * 自身借方
     */
    private double selfDebit;
    /**
     * 自身贷方
     */
    private double selfCredit;
    /**
     * 期初余额
     */
    private double openingBalance;

    /**
     * 期间年度
     */
    private Long periodYearNum1;
    /**
     * 期间年度
     */
    private Long periodYearNum2;
    /**
     * 本期凭证数量
     */
    private int voucherCount;
}
