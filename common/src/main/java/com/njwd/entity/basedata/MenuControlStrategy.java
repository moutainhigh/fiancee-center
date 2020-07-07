package com.njwd.entity.basedata;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/15 14:06
 */
@Getter
@Setter
public class MenuControlStrategy {

    private Long id;
    /**
     * 租户主键
     */
    private Long rootEnterpriseId;
    /**
     * 菜单编码
     */
    private String menuCode;
    /**
     * 是否集团创建
     * 0：否、1：是
     */
    private Byte groupCreate;
    /**
     * 是否公司创建
     * 0：否、1：是
     */
    private Byte companyCreate;
    /**
     * 是否共享
     * 0：否、1：是
     */
    private Byte isShare;
    /**
     * 是否分配
     * 0：否、1：是
     */
    private Byte isDistribute;
    /**
     * 是否私有
     * 0：否、1：是
     */
    private Byte isPrivate;
    /**
     * 私有是否可升级分配
     * 0：否、1：是
     */
    private Byte isChangeToDistribute;
    private Date createTime;
    private Date updateTime;
    private String updatorName;
    private Long updatorId;

}
