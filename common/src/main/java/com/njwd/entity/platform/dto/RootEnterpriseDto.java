package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.RootEnterpriseVo;
import lombok.Data;
import lombok.ToString;

/**
 * 租户
 *
 * @author zhuzs
 * @date 2019-11-14 12:02
 */
@Data
@ToString(callSuper = true)
public class RootEnterpriseDto extends RootEnterpriseVo {
    private static final long serialVersionUID = -5370013673538424010L;
    private Page<RootEnterpriseVo> page = new Page<>();

    /**
     *
     */
    private Long user_id;
}

