package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
/**
 *
 * @author xyyxhcj@qq.com
 * @since 2019/5/21
 */
@Data
public class SysRole implements Serializable {
    /**
    * 岗位/角色id
    */
    @TableId(type = IdType.AUTO)
    private Long roleId;

    /**
    * 租户id
    */
    private Long rootEnterpriseId;

    /**
    * 编码
    */
    private String code;

    /**
    * 岗位/角色名称
    */
    private String name;

    /**
    * 排序
    */
    private Byte sort;

    /**
    * 启用标识: 1启用 0禁用
    */
    private Byte isEnable;

    /**
    * 备注
    */
    private String remark;

    /**
    * 创建人编码
    */
    private Long creatorId;

    private String creatorName;

    /**
    * 创建时间
    */
    private Date createTime;

    /**
    * 修改人编码
    */
    private Long updatorId;

    private String updatorName;

    /**
    * 修改时间
    */
    private Date updateTime;

    /**
    * 删除标识: 0未删除 1删除
    */
    private Byte isDel;

    private static final long serialVersionUID = 1L;
}