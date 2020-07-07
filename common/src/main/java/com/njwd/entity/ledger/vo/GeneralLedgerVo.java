package com.njwd.entity.ledger.vo;

import com.njwd.entity.ledger.dto.GeneralLedgerQueryDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @Description 总分类账vo
 * @Date 2019/7/29 16:51
 * @Author 薛永利
 */
@Getter
@Setter
@ToString(callSuper = true)
public class GeneralLedgerVo extends GeneralLedgerQueryDto {
	/**
	 * 账簿ID
	 */
	private Long accountBookId;
	/**
	 * 核算主体ID
	 */
	private Long accountBookEntityId;
	/**
	 * 数据排序 0 期初余额 1 明细 2本期合计 3本年累计
	 */
	private byte num;
	/**
	 * 科目上级编码
	 */
	private String upCode;
	/**
	 * 科目编码
	 */
	private String subjectCode;
	/**
	 * 科目名称
	 */
	private String subjectName;
	/**
	 * 科目显示次级
	 */
	private byte level;
	/**
	 * 总账账簿名称
	 */
	private String accountBookName;
	/**
	 * 核算主体名称
	 */
	private String accountBookEntityName;
	/**
	 * 对方科目
	 */
	private String oppositeSubject;
	/**
	 * 凭证字号
	 */
	private String voucherWord;
	/**
	 * 凭证ID
	 */
	private Long voucherId;
	/**
	 * 凭证主号
	 */
	private String mainCode;
	/**
	 * 制单日期
	 */
	private String voucherDate;
	/**
	 * 制单日期时间戳
	 */
	private Long voucherDateTime;
	/**
	 * 期间开始日期
	 */
	private String startDate1;
	/**
	 * 期间结束日期
	 */
	private String endDate1;
	/**
	 * 期间开始日期
	 */
	private String startDate2;
	/**
	 * 期间结束日期
	 */
	private String endDate2;
	/**
	 * 期间年度
	 */
	private int periodYearNum;
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
	private String summary;
	/**
	 * 借方
	 */
	private BigDecimal debit = BigDecimal.ZERO;
	/**
	 * 贷方
	 */
	private BigDecimal credit = BigDecimal.ZERO;
	/**
	 * 自身借方
	 */
	private BigDecimal selfDebit = BigDecimal.ZERO;
	/**
	 * 自身贷方
	 */
	private BigDecimal selfCredit = BigDecimal.ZERO;
	/**
	 *余额
	 */
	private BigDecimal balance;

	/**
	 * 期初余额
	 */
	private BigDecimal openingBalance = BigDecimal.ZERO;

	/**
	 * 方向 0 借 1 贷 2 平
	 */
	private int balanceDirection;

	/**
	 * 数据类型 0 期初余额 1 明细 2本期合计 3本年累计
	 */
	private int type;
	/**
	 * 期间年度
	 */
	private int periodYearNum1;
	/**
	 * 期间年度
	 */
	private int periodYearNum2;
	/**
	 * 本期凭证数量
	 */
	private int voucherCount;
}
