package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.AuxiliaryItemVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author liuxiang
 * 前端入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuxiliaryItemDto extends AuxiliaryItemVo {
    private static final long serialVersionUID = 6371723027765178968L;

    private String codeOrName;

    /**
     * 名称字符串
     **/
    private String names;

    private Page<AuxiliaryItemVo> page = new Page<>();

    /**
     * 主键集合
     */
    private List<Long> idList;

    /**
     * 发布消息
     */
    private MessageDto messageDto;

    /**
     *
     */
    List<Long> excludedIds;

}