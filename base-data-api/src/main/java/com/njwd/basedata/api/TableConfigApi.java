package com.njwd.basedata.api;

import com.njwd.entity.basedata.dto.TableConfigCreateDto;
import com.njwd.entity.basedata.dto.query.TableConfigQueryDto;
import com.njwd.support.Result;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @description: 表格配置Controller
 * @author: 朱小明
 * @create: 2019-05-21
 */
@RequestMapping("tableConfig")
public interface TableConfigApi {

    @RequestMapping("findList")
    Result findList(TableConfigQueryDto queryDto);

    @RequestMapping("findUserList")
    Result findUserList(TableConfigQueryDto queryDto);

    /**
     * 新增或修改表格
     *
     * @param configCreateDto
     * @return
     */
    @RequestMapping("batchUpdate")
    Result batchUpdate(TableConfigCreateDto configCreateDto);

}
