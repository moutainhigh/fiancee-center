package com.njwd.entity.ledger;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @description:
 * @author: xdy
 * @create: 2019/7/29 9:12
 */
@Getter
@Setter
public class QueryScheme implements Serializable {

    /**
     * 主键 默认自动递增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 菜单编码
     */
    private String menuCode;

    /**
     * 方案名称
     */
    private String schemeName;

    /**
     * 企业id
     */
    @JsonIgnore
    private Long rootEnterpriseId;

    /**
     * 创建人
     */
    @JsonIgnore
    private Long creatorId;

    /**
     * 创建时间
     */
    @JsonIgnore
    private Date createTime;

    /**
     * 修改人
     */
    @JsonIgnore
    private Long updatorId;

    /**
     * 修改时间
     */
    @JsonIgnore
    private Date updateTime;

    /**
     * 是否默认方案 0：否、1：是
     */
    private Byte isDefault;

    /**
     * 是否删除 0：否、1：是
     */
    @JsonIgnore
    @TableLogic
    private Byte isDel;


    private Integer version;

}
