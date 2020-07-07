package com.njwd.entity.platform.vo;

import com.njwd.entity.platform.RootEnterprise;
import lombok.Data;
import lombok.ToString;

import java.util.List;


/**
 * 租户
 *
 * @author zhuzs
 * @date 2019-11-14 12:02
 */
@Data
@ToString(callSuper = true)
public class RootEnterpriseVo extends RootEnterprise {
    private static final long serialVersionUID = 1602866214694261502L;

    /**
     * 页码
     */
    private Long pageNo;

    /**
     * 每页条数
     */
    private Long pageSize;

    /**
     * 总页数
     */
    private Integer totalPage;

    /**
     * 总的记录数
     */
    private Integer totalRecord;

    //
    private Integer infoNum;

    private Integer server_id;

    private String server_net;

    private String interface_url;

    private Long root_enterprise_id;
}

