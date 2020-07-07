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
 * @Description 菜单选项
 * @Date:17:02 2019/6/14
 **/
@Data
public class SysMenuOption implements Serializable {
    private static final long serialVersionUID = -8620994438191302855L;
    /**
     * 主键 默认自动递增
     */
     @TableId(value = "id", type = IdType.AUTO)
        private Long id;

    /**
     * 菜单名称
     */
    @TableField(value = "menu_name")
        private String menuName;

    /**
     * 菜单标识
     */
    @TableField(value = "menu_code")
        private String menuCode;

    /**
     * 数据名称
     */
    @TableField(value = "data_name")
        private String dataName;

    /**
     * 数据类型 rule：控制策略、code：编码规则
     */
    @TableField(value = "data_type")
        private String dataType;

    /**
     * 选择方式 0：单选、1：多选
     */
    @TableField(value = "select_type")
        private Byte selectType;

    /**
     * 选项名称
     */
    @TableField(value = "option_name")
        private String optionName;

    /**
     * 选项值
     */
    @TableField(value = "option_value")
        private String optionValue;

    /**
     * 是否默认值 0：否、1：是
     */
    @TableField(value = "is_default")
        private Byte isDefault;

    /**
     * 是否可改 0：否、1：是
     */
    @TableField(value = "is_can_update")
        private Byte isCanUpdate;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
        private Date createTime;

    /**
     * 创建人ID
     */
    @TableField(value = "creator_id")
        private Long creatorId;

    /**
     * 创建人
     */
    @TableField(value = "creator_name")
        private String creatorName;

    /**
     * 企业ID 租户企业ID
     */
    private Long rootEnterpriseId;


}