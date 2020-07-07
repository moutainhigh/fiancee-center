package com.njwd.entity.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class SourceOfValue {
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
     * 表
     */
    private String sourceTable;

    /**
     * 资料类型
     */
    private String model;

    /**
    * 资料id 对应字典表id
    */
    private Long auxId;

    /**
    * 资料类型 对应字典表type
    */
    private String auxType;

    /**
    * 资料名称 对应字典表name
    */
    private String auxName;

    /**
    * 创建时间
    */
    private Date createTime;

    /**
    * 创建人id
    */
    private Long creatorId;

    /**
    * 创建人
    */
    private String creatorName;

    /**
    * 修改时间
    */
    private Date updateTime;

    /**
    * 修改人id
    */
    private Long updatorId;

    /**
    * 修改人
    */
    private String updatorName;

    /**
    * 审核状态 0 未审核 1 已审核
    */
    private Byte isApproved;

    /**
    * 扩展信息
    */
    private Object manageInfo;
}
