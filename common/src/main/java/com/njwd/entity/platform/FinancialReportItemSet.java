package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.*;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author liuxiang
 * @Description 财务报告项目明细设置
 * @Date:16:59 2019/8/1
 **/
@Data
public class FinancialReportItemSet implements Serializable {
    private static final long serialVersionUID = 1861689627800566455L;
    /**
     * 主键 默认自动递增
     */
     @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 财务报告ID 【财务报告】表ID
     */
    @TableField(value = "report_id")
    private Long reportId;

    /**
     * 项目库ID 【报告项目库】表ID
     */
    @TableField(value = "report_item_id")
    private Long reportItemId;

    /**
     * 编码
     */
    @TableField(value = "code")
    private String code;

    /**
     * 名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 方向 0：借、1：贷
     */
    @TableField(value = "direction")
    private Byte direction;

    /**
     * 级次 0：标题、1：一级、2：二级、3：三级、4：小计、5：合计、6：总计
     */
    @TableField(value = "level")
    private Byte level;

    /**
     * 启用标识 0：禁用、1：启用
     */
    @TableField(value = "is_enable")
    private Byte isEnable;

    /**
     * 删除标识 0：未删除、1：已删除
     */
    @TableField(value = "is_del")
    private Byte isDel;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 创建者ID
     */
    @TableField(value = "creator_id")
    private Long creatorId;

    /**
     * 创建者
     */
    @TableField(value = "creator_name")
    private String creatorName;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 更新者ID
     */
    @TableField(value = "updator_id")
    private Long updatorId;

    /**
     * 更新者
     */
    @TableField(value = "updator_name")
    private String updatorName;

    /**
     * 版本号 并发版本号
     */
    @Version
    private Integer version;

    /**
     * 取数源 0：科目余额表、1：现金流量项目汇总表、2：报表项目
     **/
    private Byte dataType;

    /**
     * 管理信息:禁用人等信息
     */
    @TableField(exist = false)
    private ManagerInfo manageInfo;

}