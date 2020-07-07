package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.CostItemCompany;
import com.njwd.entity.platform.CostItem;
import com.njwd.entity.platform.vo.CostItemVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
* @description:
* @author: xdy
* @create: 2019-11-19 16:39
*/
@Getter
@Setter
public class CostItemDto extends CostItem {

    private String codeOrName;

    private Page<CostItemVo> page = new Page<>();

    private List<Long> idList;

    private List<Long> platformIds;

    /**
     * 发布消息
     */
    private MessageDto messageDto;

    /**
     * 使用公司ID集合
     */
    private List<Long> useCompanyIds;

    /**
     * 引用费用项目
     */
    private List<CostItemDto> costItemDtoList;

    /**
     * 费用项目公司
     */
    private List<CostItemCompany> costItemCompanyList;

    /**
     * 公司ID集合
     */
    private List<Long> companyIds;

}
