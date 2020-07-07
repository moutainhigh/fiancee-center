package com.njwd.entity.basedata.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.annotation.ExcelCell;
import com.njwd.entity.basedata.vo.AccountingItemValueVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Description 自定义核算项目大区值Dto 接收参数用
 * @Author 薛永利
 * @Date 2019/6/26 14:23
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountingItemValueDto extends AccountingItemValueVo {

    private static final long serialVersionUID = 45773122520969621L;
    private Page<AccountingItemValueVo> page = new Page<>();
    private String codeOrName;
    /**
     * 批量修改时传参
     */
    private List<Long> ids;
    /**
     * 版本号批量传参
     */
    private List<Integer> versions;
    /**
     * 选择多个公司时传值
     */
    private List<Long> companyIds;

    /**
     * 项目编码
     */
    @ExcelCell(index = 3)
    private String itemCode;
    /**
     * 公司编码
     */
    @ExcelCell(index = 0)
    private String companyCode;
    /**
     * 批量导出时传参
     */
    private String exportIds;
    /**
     * 是否是租户管理员 0：否 1：是
     */
    private Byte isEnterpriseAdmin;
    /**
     * 公司名称
     */
    @ExcelCell(index = 1)
    private String companyName;
    /**
     * 使用公司名称
     */
    private String useCompanyName;
    /**
     * 项目名称
     */
    @ExcelCell(index = 4)
    private String itemName;

    /**
     * 数据来源表
     */
    private String sourceTable;
}
