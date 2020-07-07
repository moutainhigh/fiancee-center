package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author lj
 * @Description 科目类别
 * @Date:11:17 2019/8/22
 **/
@Data
public class SubjectCategory implements Serializable {
    /**
     * 主键 默认自动递增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会计要素项id 【会计要素项】表id
     */
    @TableField(value = "element_item_id")
    private Long elementItemId;

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
     * 方向 0：借方 1：贷方
     */
    @TableField(value = "direction")
    private Byte direction;

    /**
     * 是否是以前年度损益调整科目 0：否 1：是
     */
    @TableField(value = "is_past_adjust")
    private Byte isPastAdjust;

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
     * 审核状态 0：未审核、1：已审核
     */
    @TableField(value = "is_approved")
    private Byte isApproved;

    private static final long serialVersionUID = 1L;
}