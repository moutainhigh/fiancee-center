package com.njwd.entity.ledger.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.njwd.entity.ledger.Voucher;
import com.njwd.entity.ledger.vo.VoucherVo;
import com.njwd.utils.DateUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/07/24
 */
@Getter
@Setter
public class VoucherDto extends VoucherVo {
	private static final long serialVersionUID = 6934678237878914642L;
	private List<VoucherEntryDto> editEntryList = new LinkedList<>();
	private List<VoucherEntryCashFlowDto> editEntryCashFlowList = new LinkedList<>();
	private List<VoucherDto> editVoucherList = new LinkedList<>();
	private Page<VoucherDto> page = new Page<>();

	/**
	 * 总账账簿id
	 */
	private List<Long> accountBookIds;
	/**
	 * 核算主体id
	 */
	private List<Long> accountBookEntityIds;
	/**
	 * 制单日期
	 */
	@DateTimeFormat(pattern= DateUtils.PATTERN_DAY )
	@JsonFormat(pattern = DateUtils.PATTERN_DAY,timezone = "GMT+8")
	private List<Date> voucherDates;
	/**
	 * 凭证主号
	 */
	private List<Integer> mainCodes;
	/**
	 * 会计期间年月
	 */
	private List<Long> periodYearNumList;
	/**
	 * 会计期间年月--打印时使用，有多个区间
	 */
	private List<Integer> periodYearNumLists;
	/**
	 * 总账账簿id--打印时使用，有多个区间
	 */
	private List<List<Long>> accountBookIdLists;
	/**
	 * 核算主体id--打印时使用，有多个区间
	 */
	private List<List<Long>> accountBookEntityIdLists;
	/**
	 * 制单人ID
	 */
	private List<Long> creatorIds;
	/**
	 * 摘要
	 */
	private String abstractContent;
	/**
	 * 会计科目
	 */
	private List<Long> subjectCodes;
	/**
	 * 会计科目id
	 */
	private List<Long> subjectIdList;
	/**
	 * 金额
	 */
	private List<BigDecimal> amount;
	/**
	 * 辅助核算项目值ID
	 */
	private List<Long> itemValueIdList;
	/**
	 * 辅助核算来源表
	 */
	private String sourceTable;
	/**
	 * 来源单
	 **/
	private Voucher sourceVoucher;

	@Override
	public Integer getPeriodYearNum() {
		if (getPostingPeriodYear() != null && getPostingPeriodNum() != null) {
			return getPostingPeriodYear() * 100 + getPostingPeriodNum();
		} else {
			return super.getPeriodYearNum();
		}
	}
	/**
	 * 项目来源值id
	 **/
	private Long itemValueId;

	/**
	 * 凭证如果变更了期间 前端传变更前期间年
	 **/
	private Integer beforePeriodYear;

	/**
	 * 凭证如果变更了期间 前端传变更前期间号
	 */
	private Byte beforePeriodNum;

	/**
	 * 凭证 ID
	 */
	private List<Long> voucherIds;
	/**
	 * 凭证日期配置：0系统日期 1上一张凭证日期
	 **/
	private Byte voucherDateType;
	/**
	 * 是否展示明细 1：展示，0：不展示
	 */
	private Byte isDetailShow;
	/**
	 * 现金流量类型  0不需要指定现金流量 1未指定 2已指定
	 */
	private Byte cashFlowType;
    /**
     * 现金流量检查类型:-1 非现金类凭证 0 未检查 1 已检查
     */
    private Byte cashCheckType;
	/**
	 * 辅助核算项 内容
	 */
	private List<VoucherEntryAuxiliaryDto> sourceTableAndIdList;
	/**
	 * 符合 辅助核算的  凭证分录ID
	 */
	private List<Long> entryIdList;
	/**
	 * 过账失败未复核的状态
	 */
	private Byte tempReviewStatus;
	/**
	 * 用于标识导出 本级名称 0 全级名称 1
	 */
	private Byte flag;

}
