package com.njwd.entity.ledger.dto;

import com.njwd.entity.ledger.vo.BalanceInitRecordVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author lj
 * @Description 期初录入记录表Dto
 * @Date:16:48 2019/10/16
 **/
@Getter
@Setter
public class BalanceInitRecordDto extends BalanceInitRecordVo {

    /**
     * 公司id
     */
    private Long companyId;

    /**
     *账簿的编码或名称
     **/
    private String accountBookCodeOrName;

    /**
     *核算主体的编码或名称
     **/
    private String entityCodeOrName;

    /**
     *核算主体ID集合
     **/
    private List<Long> entityIdList;

    /**
     *账簿ID集合
     **/
    private List<Long> accountBookIdList;

    /**
     *批量插入期初录入记录信息
     **/
    private List<BalanceInitRecordDto> balanceInitRecordDtos;
}
