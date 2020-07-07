package com.njwd.entity.basedata;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author xyyxhcj@qq.com
 * @since 2019/5/22
 */
@Data
public class SysUserRole implements Serializable {
    /**
    * 用户id
    */
    private Long userId;

    /**
    * 公司id
    */
    private Long companyId;

    /**
     * 租户id
     */
    private Long rootEnterpriseId;

    /**
    * 角色id
    */
    private Long roleId;

    /**
    * 创建人编码
    */
    private Long creatorId;
    private String creatorName;
    private Date createTime;

    private static final long serialVersionUID = 1L;
}