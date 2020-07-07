package com.njwd.entity.basedata.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.AccountBookSystem;
import com.njwd.entity.basedata.vo.AccountBookSystemVo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 账簿启用子系统记录
 *
 * @Author: Zhuzs
 * @Date: 2019-06-21 11:31
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AccountBookSystemDto extends AccountBookSystem {
    private static final long serialVersionUID = -5815799277816063708L;

    private Page<AccountBookSystemVo> page = new Page<>();

    /**
     * 账簿的编码或者名称
     */
    private String accBookCodeOrName;
    /**
     * userId
     */
    private Long userId;

    /**
     * 前端菜单code
     */
    private String menuCode;
}
