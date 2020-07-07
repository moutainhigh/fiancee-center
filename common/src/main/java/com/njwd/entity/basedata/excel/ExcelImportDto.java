package com.njwd.entity.basedata.excel;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @description: 导入excel前端传入实体
 * @author: fancl
 * @create: 2019-05-10
 */
@Getter
@Setter
public class ExcelImportDto implements Serializable {
    private String fileType;//文件类型 标识是哪种excel
    private String fileName;//文件名称 全程 包含后缀
    private String suffix;//文件后缀名

}
