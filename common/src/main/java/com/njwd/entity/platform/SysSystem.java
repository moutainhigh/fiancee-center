package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author liuxiang
 * @Description 子系统说明
 * @Date:17:03 2019/6/14
 **/
@Data
public class SysSystem implements Serializable {

    private static final long serialVersionUID = -5741158884907976235L;

    public SysSystem() {

    }

    public SysSystem(String systemName, String systemSign) {
        this.systemName = systemName;
        this.systemSign = systemSign;
    }

    /**
     * 主键 默认自动递增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 企业ID
     */
    private Long rootEnterpriseId;

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 系统标识
     */
    private String systemSign;

    /**
     * 购买时间
     */
    private Date buyTime;

    /**
     * 有效期限
     */
    private Date validityPeriod;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人
     */
    private String creatorName;

    /**
     * 创建人ID
     */
    private Long creatorId;

}