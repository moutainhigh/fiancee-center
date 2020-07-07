package com.njwd.entity.ledger.dto;

import com.njwd.entity.basedata.dto.AccountBookEntityDto;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 资产负债表——账簿
 *
 * @author zhuzs
 * @date 2019-08-01 17:43
 */
@Data
public class BalanceDto implements Serializable {
    private static final long serialVersionUID = 4056824702578502741L;
    /**
     * 账簿ID
     */
    private Long accountBookId;

    /**
     * 账簿名称
     */
    private String accountBookName;

    /**
     * 核算主体ID
     */
    private Long accountBookEntityId;

    /**
     * 核算主体ID List
     */
    private List<Long> accountBookEntityIdList;

    /**
     * 核算主体 List
     */
    private List<AccountBookEntityDto> accountBookEntityDtoList;

    /**
     * 资产负债表ID 需要前端传参
     */
    private Long balanceSheetId;

     /**
     * 科目表ID
     */
    private Long subjectId;

    /**
     * 公司ID
     */
    private Long companyId;

    /**
     * 启用期间年度
     */
    private Integer periodYear;

    /**
     * 启用期间号
     */
    private Byte periodNum;

    /**
     * 是否包含未过账 0：否，1：是
     */
    private Byte posting;

    /**
     * 重分类 0：否，1：是
     */
    private Byte rearrange;
}

