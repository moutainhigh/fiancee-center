package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.BankCategoryVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author liuxiang
 * 前端入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BankCategoryDto extends BankCategoryVo {

    private static final long serialVersionUID = -3188888423848036399L;

    private Page<BankCategoryVo> page = new Page<>();

    private String codeOrName;

}