package com.njwd.ledger.api;

import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/12 14:52
 */
@RequestMapping("ledger/initApi")
public interface SysInitDataApi {

    @RequestMapping("createTable")
    Result<Boolean> createTable();

}
