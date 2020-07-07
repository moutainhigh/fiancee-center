package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.AccountSubjectAuxiliaryVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author liuxiang
 * 前端入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountSubjectAuxiliaryDto<T> extends AccountSubjectAuxiliaryVo {

    private static final long serialVersionUID = -3564040663855287506L;

    private Page<T> page = new Page<>();

    /**
     * 下级会计科目id
     */
    private long childAccountSubjectId;

    /**
     * 编码或名称 查询条件
     */
    private String codeOrName;

    /**
     * 租户id
     */
    private Long rootEnterpriseId;

}