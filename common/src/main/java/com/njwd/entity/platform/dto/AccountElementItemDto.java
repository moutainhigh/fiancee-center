package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.AccountElementItemVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author lj
 * @Description 会计要素项
 * @Date:11:03 2019/8/22
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountElementItemDto extends AccountElementItemVo {
    /** 用于分页 **/
    private Page<AccountElementItemVo> page = new Page<>();
    /**会计要素表id集合**/
    private List<Long> ids;
    /**
     * 编码 名称
     */
    private String codeOrName;
}
