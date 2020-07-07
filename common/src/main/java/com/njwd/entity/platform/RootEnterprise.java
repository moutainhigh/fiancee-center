package com.njwd.entity.platform;

import lombok.Data;

/**
 * 租户
 *
 * @author zhuzs
 * @date 2019-11-14 12:02
 */
@Data
public class RootEnterprise extends BaseEntity {

    private static final long serialVersionUID = -6529041970241079935L;
    /**
     * 租户ID
     */
    private Long id;

    /**
     * 租户名称
     */
    private String name;

    /**
     * 联络人
     */
    private String linkman;

    /**
     * 联系方式
     */
    private String telephone;

    /**
     * 固定电话
     */
    private String linktel;

    /**
     * 地址
     */
    private String address;

}

