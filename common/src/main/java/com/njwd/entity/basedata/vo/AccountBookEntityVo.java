package com.njwd.entity.basedata.vo;

import com.njwd.entity.basedata.AccountBookEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author: Zhuzs
 * @Date: 2019-06-05 11:21
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AccountBookEntityVo extends AccountBookEntity {
    private static final long serialVersionUID = -6875421380423520675L;
    /**
     * 核算主体 类型名称
     */
    private String formName;

    /**
     * 核算主体名称
     */
    private String entityName;

    /**
     * 核算主体编码
     */
    private String entityCode;

    /**
     * 核算主体编码
     */
    private String code;

    /**
     * 公司ID
     */
    private Long companyId;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 账簿类型ID
     */
    private Long accountBookTypeId;

    /**
     * 账簿类型名称
     */
    private String accountBookTypeName;

    /**
     * 核算主体所占列数
     */
    private Integer columnNum;

    /**
     * 核算账簿code
     */
    private String accountBookCode;

    /**
     * 对应entity_name 前端展示用
     */
    private String name;
}
