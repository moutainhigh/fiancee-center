package com.njwd.platform.api;

import com.njwd.entity.platform.vo.AccountBookTypeVo;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author lj
 * @Description 账簿类型
 * @Date:14:37 2019/6/26
 **/
@RequestMapping("platform/accountBookType")
public interface AccountBookTypeApi {
    /**
     * @Description 查询账簿类型列表
     * @Author lj
     * @Date:17:14 2019/6/25
     * @Param []
     * @return java.lang.String
     **/
    @PostMapping("findAccountBookTypeList")
    Result<List<AccountBookTypeVo>> findAccountBookTypeList();
}
