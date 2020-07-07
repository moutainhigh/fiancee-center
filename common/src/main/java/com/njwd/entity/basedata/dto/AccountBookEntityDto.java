package com.njwd.entity.basedata.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.AccountBookEntity;
import com.njwd.entity.basedata.vo.AccountBookEntityVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author: Zhuzs
 * @Date: 2019-06-05 13:26
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccountBookEntityDto extends AccountBookEntity {

    /**
     * 核算主体的编码或者名称
     */
    private String codeOrName;

    /**
     * 核算主体名称
     */
    private String entityName;

    /**
     * 核算主体分页信息
     */
    Page<AccountBookEntityVo> page = new Page<>();

    /**
     * 核算账簿ID 集合
     */
    private List<Long> accountBookIdList;

    /**
     * 已选核算主体ID
     */
    private Long selectedEntityId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 公司ID
     */
    private Long companyId;

    /**
     * 是否是默认核算主体 0：否，1：是
     */
    private Byte isDefault;

    /**
     * 菜单编码
     */
    private String menuCode;

    /**
     * 已选择核算主体ID List
     */
    private List<Long> selectedIdList;

    /**
     * 未被引用的业务单元ID
     */
    private List<Long> notReferencesIdList;
}
