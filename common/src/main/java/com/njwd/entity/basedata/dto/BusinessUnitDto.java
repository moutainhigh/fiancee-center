package com.njwd.entity.basedata.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.njwd.entity.basedata.vo.BusinessUnitVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 业务单元
 *
 * @Author: Zhuzs
 * @Date: 2019-05-16 16:17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessUnitDto extends BusinessUnitVo {
    private static final long serialVersionUID = -2201089720242059444L;

    private Page<BusinessUnitVo> page = new Page<>();
    /**
     * 业务单元ID 集合
     */
    private List<Long> idList;
    /**
     * 公司 ID 集合
     */
    private List<Long> companyIdList;
    /**
     * 业务单元 编码或者名称
     */
    private String codeOrName;

    /**
     * 是否自定义编码 0：系统编码；1：自定义编码
     */
    private Byte isAutoCode;
    /**
     * 标识是否后台管理界面 0：否 1：是
     */
    private Byte isEnterpriseAdmin;

    /**
     * 公司编码
     */
    private String companyCode;

    /**
     * 公司是否分账核算 0：否、1：是
     */
    private Byte hasSubAccount;

    /**
     * 是否建账 0：否、1：是
     */
    private Byte isAccounting;

    /**
     * 数据类型 0：客户、1：供应商
     */
    private Byte dataType;

    /**
     * 编码集合
     */
    private List<String> codes;
}
