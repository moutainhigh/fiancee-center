package com.njwd.entity.ledger;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class MultiColumnSchemeItem implements Serializable {
    private static final long serialVersionUID = -5167500808013297076L;
    /**
     * 主键 默认自动递增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 方案ID
     */
    @TableField(value = "scheme_id")
    private Long schemeId;

    /**
     * 栏目方向 0：借、1：贷
     */
    @TableField(value = "direction")
    private Byte direction;

    /**
     * 项目编码
     */
    @TableField(value = "item_code")
    private String itemCode;

    /**
     * 项目名称
     */
    @TableField(value = "item_name")
    private String itemName;

    /**
     * 项目主键
     */
    private Long itemId;

}