package com.njwd.entity.basedata.dto;

import com.njwd.entity.basedata.vo.SysMenuTabColumnVo;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 表格配置用户端返回实体
 */
@Getter
@Setter
public class TableConfigComplexDto implements Serializable {
    //运营平台全部表格配置
    List<SysMenuTabColumnVo> globalConfigList;
    //租户自定义表格配置
    List<SysMenuTabColumnVo> localConfigList;


}
