package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.Unit;
import com.njwd.entity.platform.vo.UnitVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/15 10:35
 */
@Getter
@Setter
public class UnitDto extends Unit {

    /**
     * 编码或名称
     */
    private String codeOrName;

    /**
     * 分页
     */
    private Page<UnitVo> page = new Page<>();

    /**
     * ID集合
     */
    private List<Long> idList;

    /**
     * 平台ID集合
     */
    private List<Long> platformIds;

    /**
     * 发布消息
     */
    private MessageDto messageDto;

}
