package com.njwd.entity.basedata;

import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/29 15:20
 */
@Data
public class CostItemCompany {

    /**
     *  主键
     */
    private Long id;

    /**
     *  费用项目ID
     */
    private Long costItemId;

    /**
     *  公司ID
     */
    private Long companyId;

    /**
     *  是否删除
     */
    private Byte isDel;

    /**
     * 创建人
     */
    private Long creatorId;

    /**
     * 创建人名称
     */
    private String creatorName;

    /**
     * 创建时间
     */
    private Date createTime;

}
