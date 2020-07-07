package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.SysSystemVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author liuxiang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysSystemDto extends SysSystemVo {
    private static final long serialVersionUID = 2271896206291247854L;

    private Page<SysSystemVo> page = new Page<>();

    /**
     * 账簿的编码或者名称
     */
    private String accBookCodeOrName;
}