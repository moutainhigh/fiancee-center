package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/6 14:03
 */
@Getter
@Setter
public class SysInitData {

    /**
     * 主键 默认自动递增
     */
    @TableId(type= IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    private Long rootEnterpriseId;

    /**
     * 系统标识
     */
    private String systemSign;

    /**
     * 数据标识
     */
    private String dataSign;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 创建者
     */
    private String creatorName;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 更新者ID
     */
    private Long updatorId;

    /**
     * 更新者
     */
    private String updatorName;

}
