package com.njwd.entity.platform;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author liuxiang
 * @Description 辅助资料
 * @Date:17:02 2019/6/14
 **/
@Data
public class SysAuxData implements Serializable {
    private static final long serialVersionUID = 3345212525037618327L;
    /**
     * 主键 默认自动递增
     */
     @TableId(value = "id", type = IdType.AUTO)
        private Long id;

    /**
     * 资料类型
     */
    @TableField(value = "type")
        private String type;

    /**
     * 资料值编码
     */
    @TableField(value = "CODE")
        private String code;

    /**
     * 资料值名称
     */
    @TableField(value = "NAME")
        private String name;

    /**
     * 资料备注
     */
    @TableField(value = "remark")
        private String remark;

    /**
     * 是否默认 0：否、1：是
     */
    @TableField(value = "is_default")
        private Byte isDefault;

    /**
     * 是否启用 0：否、1：是
     */
    @TableField(value = "is_enable")
        private Byte isEnable;

}