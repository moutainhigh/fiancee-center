package com.njwd.entity.ledger.vo;

import com.njwd.entity.ledger.MultiColumnSchemeItem;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @description:
 * @author: xdy
 * @create: 2019/8/31 11:44
 */
@Getter
@Setter
public class MultiColumnReportVo {


    /**
     * 项目信息
     */
    private List<MultiColumnSchemeItem> subjectList;

    /**
     * 报表明细
     */
    private List<MultiColumnReportItemVo> itemList;

}
