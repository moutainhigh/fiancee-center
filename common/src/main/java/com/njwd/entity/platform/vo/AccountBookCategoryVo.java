package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.AccountBookCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author liuxiang
 * @Description 账簿分类
 * @Date:14:16 2019/6/24
 **/
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AccountBookCategoryVo extends AccountBookCategory {
    private static final long serialVersionUID = -1331607119448472002L;
    //会计准则名称
    private String accStandardName;
    private String currencyIds;
    private String currencyNames;
    private String taxSystemIds;
    private String taxSystemNames;
    //会计日历名称
    private String calendarName;
    //期间类型名称
    private String accountingPeriodTypeName;
    //科目表名称
    private String subjectName;
}