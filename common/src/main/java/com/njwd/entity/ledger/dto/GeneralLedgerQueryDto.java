package com.njwd.entity.ledger.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.base.query.BaseLedgerQueryDto;
import com.njwd.entity.ledger.vo.GeneralLedgerVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Description 总分类账dto
 * @Date 2019/7/29 16:45
 * @Author 薛永利
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GeneralLedgerQueryDto extends BaseLedgerQueryDto {
	private Page<GeneralLedgerVo> page = new Page<>();
	/**
	 * 制单日期
	 */
	private String voucherDate1;
	/**
	 * 制单日期
	 */
	private String voucherDate2;
	/**
	 * 期间年度
	 */
	private int periodYearNum1;
	/**
	 * 期间年度
	 */
	private int periodYearNum2;
	/**
	 * 会计科目编码
	 */
	private String accountCode1;
	/**
	 * 会计科目编码
	 */
	private String accountCode2;
	/**
	 * 会计科目编码
	 */
	private List<String> accountCodes;
	/**
	 * 会计科目ID
	 */
	private List<Long> accountSubjectIds;
	/**
	 * 会计期间内的ID列表
	 */
	private List<Long> voucherIdList;
	/**
	 * 会计科目ID
	 */
	private Long accountSubjectId;
	/**
	 * 科目显示次级
	 */
	private int level1;
	/**
	 * 科目显示次级
	 */
	private int level2;
	/**
	 * 是否仅显示末级科目(0:否 1:是)
	 */
	private byte isFinal;
	/**
	 * 是否包含禁用科目(0:不包含 1:包含)
	 */
	private byte isIncludeEnable;
	/**
	 * 显示科目全名 0 不显示科目全名 1 显示科目全名
	 */
	private byte fullNameFlag;
	/**
	 * 显示对方科目 0 显示对方科目 1 不显示对方科目
	 */
	private byte oppositeSubjectFlag;
	/**
	 * 企业id
	 */
	private Long rootEnterpriseId;
	/**
	 * 账簿对应subjectId
	 */
	private Long subjectId;
}
