package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.CashFlowVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author lj
 * @Description //TODO
 * @Date:15:37 2019/6/12
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class CashFlowDto extends CashFlowVo {
    private static final long serialVersionUID = -8281557267007897462L;
    /**
     *编码或名称
     **/
    private String codeOrName;

    /**
     *操作的ID列表
     **/
    private List<Long> ids;

    /**
     *操作的ID列表
     **/
    private List<CashFlowVo> changeList;

    /**
     *会计准则ID列表
     **/
    private List<Long> accStandardIdList;

    private Page<CashFlowVo> page = new Page();

    /**
     *修改前的会计准则Id
     **/
    private Long oldAccStandardId;

    /**
     * 发布消息
     */
    private MessageDto messageDto;
}
