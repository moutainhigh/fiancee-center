package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.AccountSubjectAuxiliary;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * @Author liuxiang
 * @Description 会计科目辅助核算关系
 * @Date:14:17 2019/6/19
 **/
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AccountSubjectAuxiliaryVo extends AccountSubjectAuxiliary {

    private static final long serialVersionUID = 2464986526163304099L;

    /**
     * 辅助核算ID 【辅助核算】表ID
     */
    private String auxiliaryCodes;

    /**
     * 辅助核算名称 【辅助核算】表NAME
     */
    private String auxiliaryNames;

    /**
     * 辅助核算值来源表
     */
    private String auxiliarySourceTables;

    /**
     * 辅助核算ID集合 【辅助核算】表ID
     */
    private List<Long> itemValueIdList;

    /**
     * 辅助核算名称集合 【辅助核算】表NAME
     */
    private List<String> auxiliaryNameList;

    /**
     * 辅助核算名称集合 【辅助核算】表NAME
     */
    private List<String> sourceTableList;

    /**
     * 辅助核算信息
     */
    private List<AccountSubjectAuxiliaryVo> itemValueInfo;
    private String auxiliaryCode;
}
