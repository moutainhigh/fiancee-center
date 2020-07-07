package com.njwd.entity.ledger.vo;

import com.njwd.entity.ledger.MultiColumnScheme;
import com.njwd.entity.ledger.MultiColumnSchemeItem;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MultiColumnSchemeVo extends MultiColumnScheme {
    private static final long serialVersionUID = -4751177648574073310L;

    private List<MultiColumnSchemeItem> itemList;

    /**
     * 会计科目名称
     */
    private String accountSubjectName;

    /**
     * 查询类型
     */
    private String schemeTypeName;
}