package com.njwd.entity.basedata.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.annotation.ExcelCell;
import com.njwd.entity.basedata.Staff;
import com.njwd.entity.basedata.vo.StaffVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author: Zhuhc
 * @Date: 2019/6/12 9:15
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StaffDto extends Staff {
    private static final long serialVersionUID = -3652228809361649449L;
    private Page<StaffVo> page = new Page();
    private String codeOrName;

    /**
     * 批量修改时传参-ID
     */
    private List<Long> ids;
    /**
     * 批量修改时传参-版本号 VERSION
     */
    private List<Integer> versions;
    /**
     * 查询上一条/下一条的code
     */
    private String currentCode;

    /**
     * 是否管理员  0：否 1：是
     */
    private Byte isEnterpriseAdmin;

    /**
     * 公司名称
     */
    @ExcelCell(index = 1)
    private String company;

    /**
     * 部门名称
     */
    @ExcelCell(index = 3)
    private String dept;

    /**
     * 创建公司编码
     */
    @ExcelCell(index = 0)
    private String companyCode;

    /**
     * 部门编码
     */
    @ExcelCell(index = 2)
    private String deptCode;

    private List<Long> companyIdList;
}
