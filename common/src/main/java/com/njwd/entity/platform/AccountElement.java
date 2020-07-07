package com.njwd.entity.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class AccountElement {
    /**
    * 主键 默认自动递增
    */
    private Long id;

    /**
    * 编码
    */
    private String code;

    /**
    * 名称
    */
    private String name;

    /**
     * 审核状态 0：未审核、1：已审核
     */
    private Byte isApproved;

    /**
    * 创建时间
    */
    private Date createTime;

    /**
    * 创建者ID
    */
    private Long creatorId;

    /**
    * 创建者
    */
    private String creatorName;
}
