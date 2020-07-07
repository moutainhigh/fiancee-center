package com.njwd.entity.platform.dto;

import com.njwd.entity.platform.vo.SysTabColumnVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author liuxiang
 * 前端入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysTabColumnDto extends SysTabColumnVo {

    /**
     *是否是Admin 1 user 0 共享 2
     **/
    private Byte isEnterpriseAdmin;

    private static final long serialVersionUID = -7845036373705634834L;
}