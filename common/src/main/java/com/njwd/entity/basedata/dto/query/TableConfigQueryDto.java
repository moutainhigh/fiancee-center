package com.njwd.entity.basedata.dto.query;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @description: 表格配置QueryDto
 * @author: fancl
 * @create: 2019-06-19
 */
@Getter
@Setter
public class TableConfigQueryDto implements Serializable {
    /**
     * 租户id
     */
    private Long rootEnterpriseId;

    /**
     * 菜单code
     */
    private String menuCode;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 配置信息修改标识,当为true时后台才会做内容变更
     */
    private Boolean updateFlag;

    /**
     * 菜单: 0、USER 1、ADMIN
     */
    private Byte isEnterpriseAdmin;

    /**
     * 查询方案主键
     */
    private Long schemeId;

}
