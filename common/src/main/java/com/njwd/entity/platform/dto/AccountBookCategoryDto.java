package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.AccountBookCategoryVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author lj
 * @Description 账簿分类
 * @Date:10:47 2019/7/10
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountBookCategoryDto extends AccountBookCategoryVo {
    /** 用于分页 **/
    private Page<AccountBookCategoryVo> page = new Page<>();
    private Long rootEnterpriseId;
    /**
     * AccountBookCategoryDto集合   用于删除  修改  审核  发布
     */
    private List<AccountBookCategoryDto> editList;
    /**
     * 会计准则id集合
     */
    private List<Long> accStandardIds;

    /**
     *
     */
    private List<Long> platformIds;
    /**
     * 发布消息
     */
    private MessageDto messageDto;
}
