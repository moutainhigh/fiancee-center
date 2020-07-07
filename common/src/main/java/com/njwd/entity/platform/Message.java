package com.njwd.entity.platform;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @author: xdy
 * @create: 2019/11/26 9:29
 */
@Data
public class Message {

    /**
     * 主键
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 菜单编码
     */
    private String menuCode;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 状态 0未读1已读
     */
    private Byte status;

    /**
     * 类型 0系统公告1资料更新
     */
    private Byte type;


    /**
     * 租户ID
     */
    private Long rootEnterpriseId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;

    /**
     * 修改人
     */
    private Long updatorId;

    /**
     * 修改人姓名
     */
    private String updatorName;

    /**
     * 修改时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;

}
