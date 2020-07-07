package com.njwd.entity.basedata.dto;

import com.njwd.entity.basedata.dto.query.TableConfigQueryDto;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @description: 表格配置新增Dto
 * @author: fancl
 * @create: 2019-06-19
 */
@Setter
@Getter
public class TableConfigCreateDto implements Serializable {

    //查询对象
    private TableConfigQueryDto tableConfigQueryDto;
    //
    List<SysMenuTabColumnDto> tabColumnDtoList;
}
