package com.njwd.entity.ledger;

import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 财务报告——导出
 *
 * @author: Zhuzs
 * @date: 2019-08-31
 */
@Data
public class FinancialReport implements Serializable {
    private static final long serialVersionUID = 364613219428810664L;
    /**
     * 核算账簿名称
     */
    private String accountBookName;

    /**
     * 核算账簿所占列数
     */
    private Integer columnNum;

    /**
     * 核算主体名称（公司未分帐核算）
     */
    private String defaultEntityName;

    /**
     * 核算主体List
     */
    List<AccountBookEntityVo> accountBookEntityVoList;

}
