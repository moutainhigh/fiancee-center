package com.njwd.entity.ledger.vo;

import com.njwd.common.Constant;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author wuweiming
 * @Description 现金日记账
 * @since 2019/08/07 17:02
 */
@Getter
@Setter
public class BalanceSubjectCashJournalVo {

	/**
	 * 账簿id
	 */
	private Long accountBookId;

	/**
	 * 凭证分录ID
	 */
	private Long id;

	/**
	 * 科目id
	 */
	private Long subjectId;

	/**
	 * 科目编码
	 */
	private String subjectCode;

	/**
	 * 科目名称
	 */
	private String subjectName;

	/**
	 * 科目全称
	 */
	private String subjectFullName;

	/**
	 * 核算主题ID
	 */
	private Long accountBookEntityId;

	/**
	 * 核算主题
	 */
	private String accountBookEntityName;

	/**
	 * 凭证id
	 */
	private Long voucherId;

	/**
	 * 制单日期
	 */
	private String voucherDate;

	/**
	 * 记账期间年
	 */
	private Integer postingPeriodYear;

	/**
	 * 记账期间号
	 */
	private Byte postingPeriodNum;

	/**
	 * 记账期间年号
	 */
	private Integer periodYearNum;

	/**
	 * 凭证字类型
	 */
	private int credentialWord;

	/**
	 * 凭证主号
	 */
	private String mainCode;

	/**
	 * 摘要
	 */
	private String firstAbstract;

	/**
	 * 对方科目
	 */
	private String oppositeSubjectName;

	/**
	 * 借方金额
	 */
	private BigDecimal debitAmount;

	/**
	 * 贷方金额
	 */
	private BigDecimal creditAmount;

	/**
	 * 损益本期借方
	 */
	private BigDecimal syDebitAmount;

	/**
	 * 损益本期贷方
	 */
	private BigDecimal syCreditAmount;

	/**
	 * 余额方向(0:借方,1:贷方)
	 */
	private Byte balanceDirection;

	/**
	 * 余额方向(0:借方,1:贷方,2:平)
	 */
	private String balanceDirectionName;

	/**
	 * 期初余额
	 */
	private BigDecimal openingBalance;

	/**
	 * 期末余额
	 */
	private BigDecimal closingBalance;

	/**
	 * （辅助核算）期初余额
	 */
	private BigDecimal auxiliaryOpeningBalance;

	/**
	 * (辅助核算)期末余额
	 */
	private BigDecimal auxiliaryClosingBalance;

	/**
	 * 余额
	 */
	private BigDecimal balance;

	/**
	 * 余额累计
	 */
	private BigDecimal sumBalance;

	/**
	 * 核算来源表
	 */
	private String sourceTable;

	/**
	 * 核算项目值ID
	 */
	private Long itemValueId;

    /**
     * 辅助核算name
     */
    private String itemValueName;

	/**
	 * 辅助核算code
	 */
	private String itemValueCode;

	/**
	 * 凭证字号
	 */
	private String wordAndCod;

	/**
	 * 辅助核算List
	 */
	private List<VoucherEntryAuxiliaryVo> voucherEntryAuxiliaryVoList;

	public String getWordAndCod() {
		if (credentialWord == 1){
			wordAndCod = "记-"+mainCode;
		}else if (credentialWord == 2){
			wordAndCod = "收-"+mainCode;
		}else if (credentialWord == 3){
			wordAndCod = "付-"+mainCode;
		}else if (credentialWord == 4){
			wordAndCod = "转-"+mainCode;
		}
		return wordAndCod;
	}

	public void setWordAndCod(String wordAndCod) {
		this.wordAndCod = wordAndCod;
	}

	public String getBalanceDirectionName(){
		if (balance.compareTo(BigDecimal.ZERO) == 0){
			balanceDirectionName = Constant.BalanceDirectionName.FLAT;
		}else if (Constant.BalanceDirection.DEBIT.equals(balanceDirection)){
			balanceDirectionName = Constant.BalanceDirectionName.DEBIT;
		}else if (Constant.BalanceDirection.CREDIT.equals(balanceDirection)){
			balanceDirectionName = Constant.BalanceDirectionName.CREDIT;
		}
		return balanceDirectionName;
	}

}
