package com.njwd.entity.basedata.vo;

import com.njwd.entity.basedata.Company;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * @Author: Zhuzs
 * @Date: 2019-05-16 17:19
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CompanyVo extends Company {

    private static final long serialVersionUID = -4845662180424548802L;

    private String isAccountingName;

    /**
     * 引用状态
     */
    private Byte isRef;

    /**
     * 账簿ID
     */
    private Long accountBookId;

    /**
     * 公司列表
     */
    private List<CompanyVo> companyVoList;

    /**
     * 核算主体（不启用分帐核算）
     */
    private AccountBookEntityVo accountBookEntityVo;
}
