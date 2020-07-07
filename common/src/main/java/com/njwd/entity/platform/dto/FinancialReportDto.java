package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.FinancialReportVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 财务报告
 * @Date:9:51 2019/6/25
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FinancialReportDto extends FinancialReportVo {
    private static final long serialVersionUID = 753513122071848792L;

    private Page<FinancialReportVo> page = new Page();

    /**
     * 编码或名称
     **/
    private String codeOrName;

    /**
     * 修改前会计准则的流水号编码
     **/
    private String oldAccStandardCode;

    /**
     * 修改前报表类型的编码
     **/
    private String oldReportTypeCode;

    /**
     * 修改前的名称
     **/
    private String oldName;

    /**
     *操作的ID列表
     **/
    private List<FinancialReportVo> changeList;

    /**
     *会计准则ID列表
     **/
    private List<Long> accStandardIdList;

    /**
     *报表类型ID列表
     **/
    private List<Long> reportTypeIdList;

    /**
     * 发布消息
     */
    private MessageDto messageDto;
}