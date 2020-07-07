package com.njwd.entity.basedata;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: Zhuzs
 * @Date: 2019-05-29 09:52
 */
@Data
public class UserRole implements Serializable {

    private static final long serialVersionUID = -1089722798581608332L;
    /**
     * 企业ID
     */
    private Long rootEnterpriseId;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 用户姓名
     */
    private String userName;
    /**
     * 用户电话号码
     */
    private String mobile;
    /**
     * 公司ID
     */
    private Long companyId;
    /**
     * 公司编码
     */
    private String companyCode;
    /**
     * 公司名称
     */
    private String companyName;
    /**
     * 岗位/角色ID
     */
    private Long roleId;
    /**
     * 岗位/角色编码
     */
    private String roleCode;
    /**
     * 岗位/角色名称
     */
    private String roleName;
}
