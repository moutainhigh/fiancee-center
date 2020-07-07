package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.SysAuxDataVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author liuxiang
 * 前端入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysAuxDataDto extends SysAuxDataVo {

    private static final long serialVersionUID = -6246288047047742368L;

    private String names;

    private String codeOrName;

    private Page<SysAuxDataVo> page = new Page<>();
}