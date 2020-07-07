package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.AccountElementItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author lj
 * @Description 会计要素项
 * @Date:11:03 2019/8/22
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountElementItemVo extends AccountElementItem {
    /**
     * 会计要素表 ->名称
     */
    private String elementName;

}
