package com.njwd.entity.platform;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
/**
 *
 * @author xyyxhcj@qq.com
 * @since 2019/11/11
 */
@Getter
@Setter
@ToString
public class SettlementMethod {
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
    * 结算方式类别
    */
    private Long settleMethod;

    /**
    * 业务分类
    */
    private Long busiType;

    /**
    * 审核状态 0：未审核、1：已审核
    */
    private Boolean isApproved;

    /**
    * 发布状态 0：未发布、1：已发布
    */
    private Boolean isReleased;

    /**
    * 0禁用 1启用
    */
    private Integer isEnable;

    /**
    * 是否删除 0：未删除、1：删除
    */
    private Boolean isDel;

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

    /**
    * 更新时间
    */
    private Date updateTime;

    /**
    * 更新者ID
    */
    private Long updatorId;

    /**
    * 更新者
    */
    private String updatorName;

    /**
    * 扩展信息
    */
    private Object manageInfo;
}
