package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.njwd.entity.base.ManagerInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author liuxiang
 * @Description 现金流量项目表
 * @Date:15:54 2019/6/25
 **/
@Data
public class CashFlow implements Serializable {
    private static final long serialVersionUID = 7406565961588753084L;
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
     * 会计准则 【会计准则】表ID
     */
    @TableField(value = "acc_standard_id")
        private Long accStandardId;

    /**
     * 是否基准 0：否、1：是
     */
    @TableField(value = "is_base")
        private Byte isBase;

    /**
     * 账簿类型 【账簿类型】表ID
     */
    @TableField(value = "account_type_id")
        private Long accountTypeId;

    /**
     * 归属现金流量项目表
     */
    @TableField(value = "parent_id")
        private Long parentId;

    /**
     * 项目最大级次
     */
    @TableField(value = "max_level")
        private String maxLevel;

    /**
     * 最大级次
     */
    @TableField(value = "max_level_num")
    private Byte maxLevelNum;

    /**
     * 是否删除 0：未删除、1：删除
     */
    @TableField(value = "is_del")
        private Byte isDel;

    /**
     * 审核状态 0：未审核、1：已审核
     */
    @TableField(value = "is_approved")
    private Byte isApproved;

    /**
     * 发布状态 0：未发布、1：已发布
     */
    @TableField(value = "is_released")
    private Byte isReleased;

    /**
     * 启用标识 0：禁用、1：启用
     */
    @TableField(value = "is_enable")
        private Byte isEnable;

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
     * 基准现金流量表Id
     */
    private Long cashFlowId;
    /**
     * 基准现金流量表名称
     */
    private String cashFlowName;

    /**
     * 现金流量项目表模板ID
     */
    private Long templateCashFlowId;
    /**
     * 现金流量项目表模板ID
     */
    private String templateCashFlowName;
    /**
     * 账簿ID
     */
    private Long accountBookTypeId;

    /**
     * 账簿类型名称
     */
    private String accountBookTypeName;


    /**
     * 会计准则名称
     */
    private String accStandardName;

    /**
     * 企业Id
     */
    private Long rootEnterpriseId;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 管理信息:禁用人等信息
     */
    @TableField(exist = false)
    private ManagerInfo manageInfo;

    /**
     * 平台ID 初始化
     */
    private Long platformId;

}