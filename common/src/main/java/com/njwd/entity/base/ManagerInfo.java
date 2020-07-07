package com.njwd.entity.base;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description 处理信息
 * @Date 2019/8/15 12:49
 * @Author 朱小明
 */
@Data
public class ManagerInfo implements Serializable {
    /**
     * 禁用人名
     */
    private String disabledUserName;

    /**
     * 禁用人ID
     */
    private Long disabledUserId;

    /**
     * 禁用时间
     */
    private Date disabledTime;

    /**
     * 启用人名
     */
    private String enabledUserName;

    /**
     * 启用人ID
     */
    private Long enabledUserId;

    /**
     * 启用时间
     */
    private Date enabledTime;


    /**
     * 过账人
     */
    private String transferItemUserName;

    /**
     * 过账人ID
     */
    private Long transferItemUserId;

    /**
     * 过账时间
     */
    private Date transferItemTime;

    /**
     * 最后打印时间
     */
    private Date lastPrintTime;

    private Long approvedUserId;

    private String approvedUserName;

    private Date approvedTime;

    private Long reversalApprovedUserId;

    private String reversalApprovedUserName;

    private Date reversalApprovedTime;

    private Long deletedUserId;

    private String deletedUserName;

    private Date deletedTime;

    private Long releasedUserId;

    private String releasedUserName;

    private Date releasedTime;
}
