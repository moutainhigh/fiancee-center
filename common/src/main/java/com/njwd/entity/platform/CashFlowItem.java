package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.njwd.annotation.ExcelCell;
import com.njwd.entity.base.BaseModel;
import com.njwd.entity.base.ManagerInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Author liuxiang
 * @Description 现金流量项目
 * @Date:15:54 2019/6/25
 **/
@Getter
@Setter
public class CashFlowItem extends BaseModel {
    private static final long serialVersionUID = 325849604572135484L;
    /**
     * 主键 默认自动递增
     */
     @TableId(value = "id", type = IdType.AUTO)
        private Long id;

    /**
     * 上级编码
     */
    @ExcelCell(index = 2)
    @TableId(value = "up_code")
        private String upCode;

    /**
     * 编码
     */
    @ExcelCell(index = 0)
    @TableField(value = "code")
        private String code;

    /**
     * 名称
     */
    @ExcelCell(index = 1)
    @TableField(value = "name")
        private String name;

    /**
     * 含上级项目名称
     */
    @TableId(value = "full_name")
        private String fullName;

    /**
     * 0：流出、1：流入
     */
    @TableField(value = "cash_flow_direction")
        private Byte cashFlowDirection;


    /**
     * 归属现金流量项目表 【WD_CASH_FLOW】表ID
     */
    @TableField(value = "cash_flow_id")
        private Long cashFlowId;

    /**
     * 归属现金流量项目表 【WD_CASH_FLOW】表ID
     */
    @TableField(value = "is_interior_contact")
        private Byte isInteriorContact;

    /**
     * 级次
     */
    @TableField(value = "level")
        private Byte level;



    /**
     * 发布状态 0：未发布、1：已发布
     */
    @TableField(value = "is_released")
        private Byte isReleased;

    /**
     * 审核状态 0：未审核、1：已审核
     */
    @TableField(value = "is_approved")
    private Byte isApproved;

    /**
     * 启用标识 0：禁用、1：启用
     */
    @TableField(value = "is_enable")
        private Byte isEnable;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;


    /**
     * 是否为末级项目 0：否、1：是
     */
    private Byte isFinal;

    /**
     * 是否系统预置 0：否、1：是
     */
    private Byte isInit;

    /**
     * 公司
     */
    private Long companyId;
    /**
     * 使用公司
     */
    private Long useCompanyId;

    /**
     * 备注
     */
    @ExcelCell(index = 3)
    private String remark;

    /**
     * 租户企业ID
     */
    private Long rootEnterpriseId;

    /**
     * 操作信息:禁用人,禁用时间等
     */
    @TableField(exist = true)
    private ManagerInfo manageInfo;

    /**
     * 是否存在预置下级 0：否、1：是
     */
    private Byte isExistNextInit;

    /**
     * 平台ID 初始化
     */
    private Long platformId;

}