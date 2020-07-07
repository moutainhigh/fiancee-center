package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.TableField;
import com.njwd.entity.base.BaseModel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/5/23
 */
@Getter
@Setter
public class SysUserEnterprise extends BaseModel implements Serializable {
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 租户id
     */
    private Long rootEnterpriseId;
    /**
     * 默认登录公司id
     */
    private Long defaultCompanyId;
    /**
     * 是否默认上一条凭证摘要：0否 1是
     */
    private Byte isLastAbstract;
    /**
     * 凭证新增日期设置：0系统日期 1上一张凭证日期
     */
    private Byte voucherDateType;
    /**
     * 凭证列表的用户配置数据(json串)
     **/
    private String voucherListConfig;
    /**
     * 是否业务管理员：0否 1是
     */
    private Byte isAdmin;
    /**
     * 启用标识: 1启用 0禁用
     */
    private Byte isEnable;


    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    private Byte isDel;
    @TableField(exist = false)
    private Long id;
    @TableField(exist = false)
    private Integer version;
}