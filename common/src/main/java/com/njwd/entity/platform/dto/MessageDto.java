package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.MessageVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/26 10:13
 */
@Getter
@Setter
public class MessageDto extends MessageVo {

    /**
     * 租户ID集合
     */
    private List<Long> rootEnterpriseIds;

    /**
     * ID集合
     */
    private List<Long> ids;

    private Page<MessageVo> page = new Page<>();

}
