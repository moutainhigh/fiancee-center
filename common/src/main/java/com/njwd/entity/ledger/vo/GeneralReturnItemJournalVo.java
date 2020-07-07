package com.njwd.entity.ledger.vo;

import com.njwd.entity.ledger.GeneralReturn;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description
 * @Author: ZhuHC
 * @Date: 2019/8/8 15:03
 */
@Getter
@Setter
public class GeneralReturnItemJournalVo extends GeneralReturn {
    /**
     * 0 期初余额 1 明细 2本期合计 3本年累计
     */
    private Integer sortNum;
    /**
     * 凭证字类型 	1：记 、2：收、3：付、4：转
     */
    private Byte credentialWord;
    /**
     * 凭证主号
     */
    private Integer mainCode;
    /**
     * 凭证子号
     */
    private Integer childCode;
    /**
     * 账簿ID+"_"+主体ID
     */
    private String signFlag;
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
