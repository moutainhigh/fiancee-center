package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/7 9:02
 */
@Getter
@Setter
public class SysMenuOptionTable {

    private Long id;
    private String menuCode;
    private String businessTable;
    private Byte isLogicDel;
    private Byte isFilterRootEnterprise;
    @TableField(exist = false)
    private Long rootEnterpriseId;

}
