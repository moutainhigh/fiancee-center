package com.njwd.entity.ledger;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.njwd.utils.DateUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description
 * @Author: ZhuHC
 * @Date: 2019/7/31 16:33
 */
@Getter
@Setter
public class GeneralReturn implements Serializable {

    private static final long serialVersionUID = -5654313754568394184L;
    /**
     * 主键
     */
    private Long id;
    /**
     * 凭证主键
     */
    private Long voucherId;
    /**
     * 科目编码
     */
    private String subjectCode;
    /**
     * 科目ID
     */
    private String subjectId;
    /**
     * 科目名称
     */
    private String subjectName;
    /**
     * 科目全名
     */
    private String subjectFullName;
    /**
     * 总账账簿
     */
    private String accountBookName;
    /**
     * 账簿ID
     */
    private Long accountBookId;
    /**
     * 核算主体
     */
    private String accountBookEntityName;

    /**
     * 核算主体ID
     */
    private Long accountBookEntityId;

    /**
     * 制单日期
     */
    @JsonFormat(pattern = DateUtils.PATTERN_DAY,timezone = "GMT+8")
    private Date voucherDate;
    /**
     * 会计年度
     */
    private int periodYear;
    /**
     * 期间
     */
    private byte periodNum;
    /**
     * 摘要
     */
    private String abstractContent;
    /**
     * 借方
     */
    private double debit;
    /**
     * 贷方
     */
    private double credit;
    /**
     *余额
     */
    private double balance;
    /**
     * 方向
     */
    private int balanceDirection;
    /**
     * 方向  0：借、1：贷   2：平
     */
    private String direction;
    /**
     * 对方科目
     */
    private String oppositeAccount;

    /**
     * 会计期间 年月
     */
    private Long periodYearNum;

    /**
     * 凭证字类型 1：记 、2：收、3：付、4：转
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
     * 序号
     */
    private Integer rowNum;
    /**
     * 凭证字号
     */
    private String credentialWordCode;
}
