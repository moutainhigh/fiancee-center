package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author liuxiang
 * @Description 银行类别
 * @Date:17:02 2019/6/14
 **/
@Data
public class BankCategory implements Serializable {
    private static final long serialVersionUID = 7262996800771717596L;
    /**
     * 主键 默认自动递增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 标识
     */
    private String identify;

    /**
     * 审核状态 0未审核 1已审核
     */
    private Byte isApproved;

    /**
     * 启用标识 0：禁用、1：启用
     */
    private Byte isEnable;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建者
     */
    private String creatorName;

    /**
     * 更新者ID
     */
    private Long updatorId;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 更新者
     */
    private String updatorName;

    /**
     * 操作信息:禁用人,禁用时间等
     */
    private ManagerInfo manageInfo;
}