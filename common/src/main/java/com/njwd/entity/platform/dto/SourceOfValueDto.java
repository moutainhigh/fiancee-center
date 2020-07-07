package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.SourceOfValue;
import lombok.Getter;
import lombok.Setter;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/14 14:22
 */
@Getter
@Setter
public class SourceOfValueDto extends SourceOfValue {

    Page<SourceOfValue> page = new Page<>();

    /**
     * 模糊搜索名称或编码
     */
    private String codeOrName;

}
