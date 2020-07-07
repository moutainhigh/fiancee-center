package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.BankVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author liuxiang
 * 前端入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BankDto extends BankVo {

    private static final long serialVersionUID = -3598206625295628015L;

    private Page<BankVo> page = new Page<>();

    private String codeOrName;

    private List<Long> categoryIdList;
}