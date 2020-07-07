package com.njwd.entity.basedata.dto;

import com.njwd.entity.basedata.vo.SequenceVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description 流水号实体
 * @Date 2019/8/6 9:04
 * @Author 朱小明
 */
@Data
@EqualsAndHashCode(callSuper =  true)
public class SequenceDto extends SequenceVo {

    /**
     * 凭证字
     */
    private Byte credWord;
    /**
     * 年度
     */
    private Integer year;
    /**
     * 期间号
     */
    private Byte periodNo;
    /**
     * 账簿ID
     */
    private Long accountId;
    /**
     * 核算主体
     */
    private Long entityId;

    /**
     * 前缀
     */
    private String preCode;

    /**
     * 流水号位数
     */
    private int length;

    /**
     * 公司或企业ID
     */
    private Long rootEnterPriseId;

    /**
     * 主体类型：0、公司1、企业
     */
    private Byte type;

    /**
     * 凭证号
     */
    private Integer code;
}
