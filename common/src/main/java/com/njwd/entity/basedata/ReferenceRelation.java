package com.njwd.entity.basedata;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @description: 业务模块(被引用)与引用模块关系
 * @author: xdy
 * @create: 2019/6/27 16:21
 */
@Getter
@Setter
public class ReferenceRelation {

    private Long id;
    private String businessModule;
    private String businessTable;
    private String businessColumn;
    private String businessServiceName;
    private String referenceModule;
    private String referenceDescription;
    private String referenceTable;
    private String referenceColumn;
    private String filterCondition;
    private Byte isLogicDel;
    private Byte sort;
    private Byte isFilterRootEnterprise;
    private String serviceName;

    @TableField(exist = false)
    private String[] referenceColumnArr;
    @TableField(exist = false)
    private Object businessKey;
    @TableField(exist = false)
    private List<Object> businessKeys;
    @TableField(exist = false)
    private Long rootEnterpriseId;
    @TableField(exist = false)
    private List<Long> businessIds;
    @TableField(exist = false)
    private Long businessId;
    @TableField(exist = false)
    private Integer referenceCount;


}
