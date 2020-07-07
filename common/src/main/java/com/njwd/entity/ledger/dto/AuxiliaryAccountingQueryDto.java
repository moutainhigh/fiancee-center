package com.njwd.entity.ledger.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.njwd.entity.base.query.BaseLedgerQueryDto;
import com.njwd.entity.basedata.dto.AccountBookEntityDto;
import com.njwd.entity.ledger.CommonAuxiliary;
import com.njwd.entity.ledger.vo.GeneralReturnAuxiliaryVo;
import com.njwd.utils.DateUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author: ZhuHC
 * @Date: 2019/8/8 9:02
 */
@Getter
@Setter
public class AuxiliaryAccountingQueryDto extends BaseLedgerQueryDto {

    Page<GeneralReturnAuxiliaryVo> page = new Page<>();
    /**
     * 制单日期
     */
    @DateTimeFormat(pattern= DateUtils.PATTERN_DAY )
    @JsonFormat(pattern = DateUtils.PATTERN_DAY,timezone = "GMT+8")
    private List<Date> voucherDates;
    /**
     * 会计科目查询类型 0等于 1期间 2包含
     */
    private Byte accountSubjectOperator;
    /**
     * 会计科目CODE
     */
    private List<Long> subjectCodes;
    /**
     * 会计科目id
     */
    private List<Long> subjectIdList;
    /**
     * 是否显示科目全名(0:不显示 1:显示)
     */
    private Byte isShowFullName;
    /**
     * 是否显示对方科目(0:不显示 1:显示)
     */
    private Byte isShowOppositeSubject;
    /**
     * 辅助核算项目ID
     */
    private Long itemId;
    /**
     * 辅助核算项目值ID
     */
    private List<Long> itemValueIdList;
    /**
     * 辅助核算来源表
     */
    private String sourceTable;
    /**
     * 租户ID
     */
    private Long rootEnterpriseId;
    /**
     * 会计期间年月
     */
    private List<Long> periodYearNum;
    /**
     * 辅助核算项
     */
    private Map<String,List<Long>> sourceTableAndValueList;
    /**
     * 辅助核算项 核算项与值 1对1
     */
    private List<CommonAuxiliary> sourceTableAndValue;
    /**
     * 账簿ID
     */
    private Long accountBookId;

    /**
     * 核算主体ID
     */
    private Long accountBookEntityId;
    /**
     * 科目ID
     */
    private Long accountSubjectId;
    /**
     * 核算主体相关信息集合
     */
    private List<AccountBookEntityDto> accountBookEntityList;
    /**
     * 值来源表集合
     */
    private List<String> sourceTableList;
    /**
     * 符合 辅助核算的  余额/分录 ID
     */
    private List<Long> auxiliaryIdList;
    /**
     * 辅助核算余额
     */
    private BalanceSubjectAuxiliaryItemQueryDto subjectAuxiliaryItemQueryDto;
    /**
     * 辅助核算凭证
     */
    private VoucherEntryAuxiliaryDto voucherEntryAuxiliaryDto;
    /**
     * 会计期间年月
     */
    private Long periodYearAndNum;

    private List<AuxiliaryAccountingQueryDto> dtoList;
    /**
     * 用于第0期参数
     */
    private Long periodNum;
    /**
     * 会计科目编码
     */
    private String accountCode1;
    /**
     * 会计科目编码
     */
    private String accountCode2;

    /**
     * 会计期间内的ID列表
     */
    private List<Long> voucherIdList;
}
