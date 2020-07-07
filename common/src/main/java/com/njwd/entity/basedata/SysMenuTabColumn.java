package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SysMenuTabColumn implements Serializable {
    /**
    * 主键 默认自动递增
    */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
    * 企业ID
    */
    private Long rootEnterpriseId;

    /**
    * 数据标识
    */
    private String menuCode;

    /**
    * 表名
    */
    private String tableName;

    /**
    * 表别名
    */
    private String tableAsName;

    /**
    * 字段名
    */
    private String columnName;

    /**
     * 字段中文名
     */
    private String columnRemark;

    /**
     * 数据转换类型
     */
    private String convertType;

    /**
    * 排序号
    */
    private Byte sortNum;

    /**
    * 是否排序 0：否、1：是
    */
    private Byte isSort;

    /**
    * 是否显示 0：否、1：是
    */
    private Byte isShow;

    /**
    * 创建时间
    */
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private Date createTime;

    /**
    * 创建人ID
    */
    private Long creatorId;

    /**
    * 创建人
    */
    private String creatorName;

    /**
     * 画面选项：0、user 1、admin
     */
    private Byte isEnterpriseAdmin;

    /**
     * 方案Id
     */
    private Long schemeId;

    /**
     * json字段名
     */
    private String columnJsonName;

    /**
     * 是否可编辑 0不可编辑，1可编辑
     */
    private Byte isEdit;

}