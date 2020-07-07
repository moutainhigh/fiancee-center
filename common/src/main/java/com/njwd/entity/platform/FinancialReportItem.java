package com.njwd.entity.platform;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;

/**
 * @Author lj
 * @Description 报表项目库
 * @Date:14:14 2019/11/15
 **/
@Data
public class FinancialReportItem implements Serializable {
    private static final long serialVersionUID = -2126930463962938246L;
    /**
    * 主键 默认自动递增
    */
    @TableId(type= IdType.AUTO)
    private Long id;

    /**
    * 财务报告类型 【财务报告类型】表ID
    */
    private Long reportTypeId;

    /**
    * 编码
    */
    private String code;

    /**
    * 名称
    */
    private String name;

    /**
    * 包含标识 0：否、1：是
    */
    private Byte isContain;

    /**
    * 增减标识 0：减、1：加、2：没有标识、
    */
    private Byte isAdd;

    /**
    * 项目属性:1:资产、负债 2:利润表 3:现金流量表
    */
    private Byte itemType;

    /**
    * 流动标识 0：否、1：是
    */
    private Byte isFlow;

    /**
    * 启用标识 0：禁用、1：启用
    */
    private Byte isEnable;

    /**
     * 删除标识 0：未删除、1：删除
     */
    private Byte isDel;

    /**
     * 审核状态 0：未审核、1：已审核
     */
    @TableField(value = "is_approved")
    private Byte isApproved;

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

    /**
     * 操作信息:禁用人,禁用时间等
     */
    @TableField(exist = false)
    private ManagerInfo manageInfo;

    /**
     * 版本号
     */
    private Integer version;
}