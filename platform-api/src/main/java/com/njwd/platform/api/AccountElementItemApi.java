package com.njwd.platform.api;

import com.njwd.entity.platform.dto.AccountElementItemDto;
import com.njwd.entity.platform.vo.AccountElementItemVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author lj
 * @Description 会计要素项
 * @Date:17:15 2019/6/25
 **/
@RequestMapping("platform/accountElementItem")
public interface AccountElementItemApi {

    /**
     * 根据EleId查询会计要素项列表
     * @Author lj
     * @Date:11:28 2019/8/22
     * @param accountElementItemDto
     * @return java.util.List<com.njwd.entity.platform.vo.AccountElementItemVo>
     **/
    @PostMapping("findListByEleId")
    Result<List<AccountElementItemVo>> findListByEleId(AccountElementItemDto accountElementItemDto);
}
