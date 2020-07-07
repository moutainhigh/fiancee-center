package com.njwd.entity.base;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SysLog implements Serializable {
    /**
    * 主键id
    */
    @TableId(type= IdType.AUTO)
    private Long id;


    /**
    * 创建时间
    */
    private Date createTime;

    /**
     * 企业id  即：租户id
     */
    private Long rootEnterpriseId;

    /**
     * 操作人id
     */
    private Long creatorId;

    /**
    * 操作人登陆账号
    */
    private String creatorAccount;

    /**
    * 操作人实际名称
    */
    private String creatorName;
    /**
     * 手机号
     */
    private String mobile;


    /**
    * 子系统名称
    */
    private String sysName;
    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 操作类型
     */
    private String operateType;
    /**
     * 操作
     */
    private String operation;

    /**
    * 操作状态: 0正常 1异常
    */
    private Byte status;

    /**
    * 请求Url
    */
    private String openUrl;

    /**
    * IP地址
    */
    private String ipAddress;

    /**
     * 记录id
     */
    private String recordId;


}