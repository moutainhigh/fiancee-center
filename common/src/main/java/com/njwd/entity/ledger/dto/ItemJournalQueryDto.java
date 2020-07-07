package com.njwd.entity.ledger.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.njwd.entity.base.query.BaseLedgerQueryDto;
import com.njwd.entity.basedata.dto.AccountBookEntityDto;
import com.njwd.entity.ledger.vo.GeneralReturnItemJournalVo;
import com.njwd.utils.DateUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @Description
 * @Author: ZhuHC
 * @Date: 2019/8/8 14:43
 */
@Getter
@Setter
public class ItemJournalQueryDto extends BaseLedgerQueryDto {

    Page<GeneralReturnItemJournalVo> page = new Page<>();
    /**
     * 制单日期
     */
    @DateTimeFormat(pattern= DateUtils.PATTERN_DAY )
    @JsonFormat(pattern = DateUtils.PATTERN_DAY,timezone = "GMT+8")
    private List<Date> voucherDates;
    /**
     * 会计科目查询类型
     */
    private Byte accountSubjectOperator;
    /**
     * 会计科目
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
     * 仅显示末级科目 (1:仅显示)
     */
    private Byte isOnlyShowFinal;
    /**
     * 会计科目显示级次
     */
    private List<Long> levels;
    /**
     * 租户ID
     */
    private Long rootEnterpriseId;
    /**
     * 会计期间年月
     */
    private List<Long> periodYearNum;
    /**
     * 是否包含禁用科目（0不包含 1包含）
     */
    private Byte isIncludeEnable;
    /**
     * 会计科目编码
     */
    private List<String> codes;
    /**
     * 核算主体相关信息集合
     */
    private List<AccountBookEntityDto> accountBookEntityList;
    /**
     * 用于第0期参数
     */
    private Long periodNum;
    /**
     * 会计期间内的ID列表
     */
    private List<Long> voucherIdList;
}
