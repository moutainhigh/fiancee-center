package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.FinancialReportItemVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author lj
 * @Description 报表项目库
 * @Date:14:19 2019/11/15
 **/
@Getter
@Setter
public class FinancialReportItemDto extends FinancialReportItemVo {

    private Page<FinancialReportItemVo> page =new Page();

    /**
     *编码或名称
     **/
    private String codeOrName;

    /**
     *修改前名称
     **/
    private String oldName;

    /**
     *修改前财务报告类型 【财务报告类型】表ID
     **/
    private Long oldReportTypeId;

    /**
     *操作的ID列表
     **/
    private List<FinancialReportItemVo> changeList;

    /**
     *报表类型ID列表
     **/
    private List<Long> reportTypeIdList;
}
