package com.njwd.basedata.api;

import com.njwd.entity.basedata.dto.UserAccountBookEntityDto;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 用户配置核算主体 前端控制器
 *
 * @author 朱小明
 * @since 2019/07/03
 */
@RequestMapping("userAccountBookEntity")
public interface UserAccountBookEntityApi {

    @PostMapping("updateBySelf")
    Result updateBySelf(UserAccountBookEntityDto userAccountBookEntityDto);
}
