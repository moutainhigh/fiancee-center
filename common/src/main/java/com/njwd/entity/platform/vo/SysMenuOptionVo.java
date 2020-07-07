package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.SysMenuOption;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author liuxiang
 * @Description 菜单选项
 * @Date:14:19 2019/6/19
 **/
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SysMenuOptionVo extends SysMenuOption {

    private static final long serialVersionUID = -8277300708455958839L;

    /**
     * 是否選中 0否1是
     */
    private Byte isSelected;
    /**
     * 数据名称
     */
    private String dataName;
    /**
     * 选项名称
     */
    private String optionName;

    /**
     * 是否默认
     */
    private Byte isDefault;

    /**
     * 是否可以修改
     */
    private Byte isCanUpdate;
}