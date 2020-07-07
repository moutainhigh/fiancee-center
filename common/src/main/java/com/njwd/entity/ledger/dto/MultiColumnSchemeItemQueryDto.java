package com.njwd.entity.ledger.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.base.query.BaseLedgerQueryDto;
import com.njwd.entity.ledger.MultiColumnSchemeItem;
import com.njwd.entity.ledger.vo.MultiColumnSchemeItemVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 多栏账方案明细
 * @Date:11:54 2019/7/29
 **/
@Getter
@Setter
public class MultiColumnSchemeItemQueryDto extends BaseLedgerQueryDto {
    private static final long serialVersionUID = -2138260312713644909L;

    /**
     * 多栏账方案明细ID
     **/
    private Long schemeItemId;

    /**
     * 多栏账方案ID
     **/
    private Long schemeId;

    /**
     * 多栏明细账分页参数
     **/
    private Page<MultiColumnSchemeItemVo> page=new Page<>();

    /**
     * 多栏明细数组
     **/
    private MultiColumnSchemeItem[] schemeItems;


    /**
     * 企业ID
     **/
    private Long rootEnterpriseId;

    /**
     * 明细方向
     **/
    private Byte direction;

    /**
     * 余额方向 0：借方、1：贷方
     **/
    private Byte balanceDirection;

    /**
     * 总账账簿id
     */
    private Long accountBookId;


    /**
     * 期间年度
     */
    private Integer periodYear;

    /**
     * 期间号
     */
    private Byte periodNumber;

    /**
     * 制单日期
     */
    private List<String> voucherDate;

    /**
     * 凭证ID
     */
    private Long voucherId;

    /**
     * 科目id(会计科目ID)
     */
    private Long subjectId;


}