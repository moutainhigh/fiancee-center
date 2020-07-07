package com.njwd.entity.basedata;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/15 13:59
 */
@Getter
@Setter
public class MenuCodingRules {

    private Long id;
    private Long rootEnterpriseId;
    private String menuCode;
    private Byte codeType;
    private Byte inputCodeLength;
    private String autoPrefixChar;
    private Byte serialNumberType;
    private Byte serialNumberLength;
    private String fixedSerialNumber;
    private Date createTime;
    private Date updateTime;
    private String updatorName;
    private Long updatorId;

}
