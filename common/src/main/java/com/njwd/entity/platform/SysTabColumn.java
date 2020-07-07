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
 * @Description 表格配置项
 * @Date:15:56 2019/6/25
 **/
@Data
public class SysTabColumn implements Serializable {
    /**
     * 主键 默认自动递增
     */
     @TableId(value = "id", type = IdType.AUTO)
        private Long id;

    /**
     * 数据类型 0：数据源、1：菜单表格
     */
    @TableField(value = "data_type")
        private Byte dataType;

    /**
     * 数据标识 全局唯一
     */
    @TableField(value = "menu_code")
        private String menuCode;

    /**
     * 表名
     */
    @TableField(value = "table_name")
        private String tableName;

    /**
     * 表别名
     */
    @TableField(value = "table_as_name")
        private String tableAsName;

    /**
     * 表说明
     */
    @TableField(value = "table_remark")
        private String tableRemark;

    /**
     * 字段名
     */
    @TableField(value = "column_name")
        private String columnName;

    /**
     * 字段说明
     */
    @TableField(value = "column_remark")
        private String columnRemark;

    /**
     * 数据转换类型
     */
    @TableField(value = "convert_type")
        private String convertType;

    /**
     * 是否默认显示 0：否、1：是
     */
    @TableField(value = "is_show")
        private Byte isShow;

    /**
     * 显示排序
     */
    @TableField(value = "sort_num")
        private Byte sortNum;

    /**
     * 是否可排序 0：否、1：是
     */
    @TableField(value = "is_sort")
        private Byte isSort;

    /**
     * 是否启用 0：否、1：是
     */
    @TableField(value = "is_enable")
        private Byte isEnable;

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
     *是否是Admin 1 user 0 共享 2
     **/
    @TableField(value = "is_enterprise_admin")
        private Byte isEnterpriseAdmin;

    /**
     * json字段名
     */
    @TableField(value = "column_json_name")
    private String columnJsonName;

    /**
     * 是否可编辑 0不可编辑，1可编辑
     */
    @TableField(value = "is_edit")
    private Byte isEdit;

}