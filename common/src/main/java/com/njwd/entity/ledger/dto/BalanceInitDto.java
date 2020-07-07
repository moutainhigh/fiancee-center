package com.njwd.entity.ledger.dto;

import com.njwd.entity.ledger.vo.BalanceInitVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author lj
 * @Description 总账初始化
 * @Date:11:25 2019/8/12
 **/
@Getter
@Setter
public class BalanceInitDto extends BalanceInitVo {
    private List<BalanceInitDto> balanceInitList;

    /**
     * 账簿子系统ID
     */
    private Long accountBookSystemId;

    /**
     * 账簿ID
     */
    private Long accountBookId;

    /**
     * 账簿名
     */
    private String accountBookName;

    /**
     * 启用期间年度
     */
    private Integer periodYear;

    /**
     * 启用期间号
     */
    private Integer periodNum;

    /**
     * 核算账簿对应的公司id
     */
    private Long companyId;

}
