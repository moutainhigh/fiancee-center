package com.njwd.entity.ledger.vo;

import com.njwd.entity.ledger.VoucherEntryAuxiliary;
import lombok.Getter;
import lombok.Setter;

/**
 * @author xyyxhcj@qq.com
 * @since 2019/07/30
 */
@Getter
@Setter
public class VoucherEntryAuxiliaryVo extends VoucherEntryAuxiliary {
    private static final long serialVersionUID = 7825055921083213358L;

    /**
     * 核算项目编码
     */
    private String projectCode;

    /**
     * 核算项目名称
     */
    private String projectName;

    /**
     * 核算项目全名
     */
    private String projectFullName;

    /**
     * 来源名称
     */
    private String projectAuxiliaryName;

    /**
     * 核算值ID拼接
     */
    private String itemValueIds;

    /**
     * 辅助核算项表名拼接
     */
    private String sourceTables;

    /**
     * 辅助核算项表名拼接
     */
    private String entryIds;

}
