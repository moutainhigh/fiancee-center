package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author liuxiang
 * @Description 银行
 * @Date:17:01 2019/6/14
 **/
@Data
public class Bank implements Serializable {
    private static final long serialVersionUID = 8071428781650619724L;
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
     * 银行类别 【银行类别】表ID
     */
    private Long categoryId;

    /**
     * 数据状态 0：未生效、1：已生效
     */
    private Byte status;

    /**
     * 所在省
     */
    private String province;

    /**
     * 所在市
     */
    private String city;

    /**
     * 联行号
     */
    private String lineNumber;

    /**
     * 审核状态 0未审核 1已审核
     */
    private Byte isApproved;

    /**
     * 发布状态 0：未发布、1：已发布
     */
    private Byte isReleased;

    /**
     * 启用标识 0：禁用、1：启用
     */
    private Byte isEnable;

    /**
     * 是否删除 0：未删除、1：删除
     */
    private Byte isDel;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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