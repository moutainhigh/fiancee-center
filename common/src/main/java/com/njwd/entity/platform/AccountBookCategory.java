package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.Version;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author liuxiang
 * @Description 账簿分类
 * @Date:15:51 2019/6/25
 **/
@Data
public class AccountBookCategory implements Serializable {
    private static final long serialVersionUID = 3816573112398032627L;
    /**
     * 主键 默认自动递增
     */
     @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * 账簿类型 【账簿类型】表ID
     */
    @TableField(value = "account_type_id")
    private Long accountTypeId;

    /**
     * 会计准则 【会计准则】表ID
     */
    @TableField(value = "acc_standard_id")
    private Long accStandardId;

    /**
     * 科目表 【科目表】表ID
     */
    @TableField(value = "subject_id")
    private Long subjectId;

    /**
     * 会计日历 【会计日历】表ID
     */
    @TableField(value = "acc_calendar_id")
    private Long accCalendarId;

    /**
     * 现金流量项目表 【现金流量项目表】表ID
     */
    @TableField(value = "cash_flow_item_id")
    private Long cashFlowItemId;

    /**
     * 记账本位币 【币种】表ID
     */
    @TableField(value = "currency_id")
    private Long currencyId;

    /**
     * 税收制度 【税收制度】表ID
     */
    @TableField(value = "tax_system_id")
    private Long taxSystemId;

    /**
     * 共享状态 0：共享、1：私有
     */
    @TableField(value = "share_status")
    private Byte shareStatus;

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
     * 是否删除 0：未删除、1：删除
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
     * 管理信息:禁用人等信息
     */
    @TableField(exist = false)
    private ManagerInfo manageInfo;
    /**
     * 期间类型
     */
    @TableField(value = "type_code")
    private String typeCode;

    /**
     * 租户ID
     */
    private Long rootEnterpriseId;

    /**
     * 平台ID
     */
    private Long platformId;
    /**
     * 版本号 并发版本号
     */
    @Version
    private Integer version;

}