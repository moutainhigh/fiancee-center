package com.njwd.entity.platform.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.platform.vo.FinancialReportItemSetVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 财务报告项目明细设置
 * @Date:16:59 2019/8/1
 **/
@Getter
@Setter
@TableName(value = "wd_financial_report_item_set")
public class FinancialReportItemSetDto extends FinancialReportItemSetVo {
    private static final long serialVersionUID = 4868455839209516259L;

    private Page<FinancialReportItemSetVo> page = new Page<>();

    /**
     * 批量删除
     **/
    private List<FinancialReportItemSetVo> changeList;

    /**
     * 表达式
     **/
    private String formula;

    /**
     * 修改前名称
     **/
    private String oldName;

    /**
     * 是否重分类  0：否  1：是
     **/
    private Byte rearrange;

    /**
     * 排序
     **/
    private Integer sort;

    /**
     * 修改前排序
     **/
    private Integer oldSort;

    /**
     * 编码或名称
     **/
    private String codeOrName;
}