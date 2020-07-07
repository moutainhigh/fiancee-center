package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.njwd.entity.base.BaseModel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户——租户关联表
 *
 * @author: zhuzs
 * @date: 2019-11-12
 */
@Data
public class SysUserEnterprise implements Serializable {

    private static final long serialVersionUID = 4654975272602163289L;
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 租户id
     */
    private Long rootEnterpriseId;

    /**
     * 租户名称
     */
    private String rootEnterpriseName;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 创建人姓名
     */
    private String creatorName;

    /**
     * 创建时间
     */
    private Date createTime;
}