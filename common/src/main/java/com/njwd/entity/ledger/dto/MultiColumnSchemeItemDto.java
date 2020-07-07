package com.njwd.entity.ledger.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.ledger.MultiColumnSchemeItem;
import com.njwd.entity.ledger.vo.MultiColumnSchemeItemVo;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author liuxiang
 * @Description 多栏账方案明细
 * @Date:11:54 2019/7/29
 **/
@Getter
@Setter
public class MultiColumnSchemeItemDto extends MultiColumnSchemeItemVo {
    private static final long serialVersionUID = -2138260312713644909L;

    /**
     * 多栏明细账分页参数
     **/
    private Page<MultiColumnSchemeItemVo> page=new Page<>();

    /**
     * 多栏明细数组
     **/
    private MultiColumnSchemeItem[] schemeItems;

    /**
     * 总账账簿ID
     **/
    private Long accountBookId;

    /**
     * 核算主体ID数组
     **/
    private Long[] accountBookEntityIds;

    /**
     * 开始期间年
     **/
    private Long startPeriodYear;

    /**
     * 结束期间年
     **/
    private Long endPeriodYear;

    /**
     * 开始期间号
     **/
    private Long startPeriodNum;

    /**
     * 结束期间号
     **/
    private Long endPeriodNum;

    /**
     * 包含未记账凭证: 1 包含 0 不包含
     */
    private  Byte includeUnBookVoucher;

    /**
     * 不包含损益结转凭证: 2 包含
     */
    private  Byte includeLossTransferVoucher;

    /**
     * 企业ID
     **/
    private Long rootEnterpriseId;

    /**
     * 凭证字号
     **/
    private String voucherWordNum;
}